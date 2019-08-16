package eu.scisneromam.mc.scismmoutils.listener

import eu.scisneromam.mc.scismmoutils.functions.Function
import eu.scisneromam.mc.scismmoutils.functions.Hammer
import eu.scisneromam.mc.scismmoutils.functions.Miner
import eu.scisneromam.mc.scismmoutils.inventory.PageManager
import eu.scisneromam.mc.scismmoutils.inventory.TypeSortedPageManager
import eu.scisneromam.mc.scismmoutils.main.Main.Companion.MAIN
import eu.scisneromam.mc.scismmoutils.utils.MCUtils
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockDropItemEvent
import org.bukkit.inventory.ItemStack
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentSkipListSet
import kotlin.collections.ArrayList
import kotlin.math.min

/**
 * Project: ScisUtils
 * Initially created by scisneromam on 30.07.2019.
 * @author scisneromam
 * ---------------------------------------------------------------------
 * Copyright © 2019 | scisneromam | All rights reserved.
 */
class BlockBreakListener : EventListener<BlockBreakEvent>()
{

    companion object
    {
        const val DEFAULT_BATCH_SIZE: Int = 2_500
    }

    var batchSize: Int = DEFAULT_BATCH_SIZE
    var batchSizePerPlayer: Int = batchSize / 10
    private val locationsPerPlayer: MutableMap<Player, MutableSet<Location>> = ConcurrentHashMap()
    private val itemsPerPlayer: MutableMap<Player, MutableList<ItemStack>> = ConcurrentHashMap()
    private val locations: MutableSet<HandledLocation> = ConcurrentHashMap.newKeySet()
    private val activatedFunctionsPerPlayer: MutableMap<Player, MutableSet<Function<BlockBreakEvent>>> =
        ConcurrentHashMap()
    private val pageManagers: MutableMap<Player, PageManager> = HashMap()

    val miner: Miner = Miner(this)
    val hammer: Hammer = Hammer(this)


    init
    {
        batchSize = MAIN.config.getInt("batchSizePerPlayer")
        if (batchSize == 0)
        {
            batchSize = DEFAULT_BATCH_SIZE
        }
        batchSizePerPlayer = MAIN.config.getInt("batchSizePerPlayer")
        if (batchSizePerPlayer == 0)
        {
            batchSizePerPlayer = batchSize / 10
        }


        MAIN.server.scheduler.scheduleSyncRepeatingTask(MAIN, {
            var todo = batchSize
            var maxTodo: Int
            do
            {
                maxTodo = 0
                for ((player, set) in locationsPerPlayer)
                {
                    maxTodo += set.size
                    val max = min(min(batchSizePerPlayer, todo), set.size)

                    val sub = set.take(max)
                    sub.forEach { location: Location ->
                        MCUtils.breakBlock(
                            location.block,
                            player,
                            player.inventory.itemInMainHand,
                            "BatchBreaker", MCUtils.DEBUG
                        )
                    }
                    set.removeAll(sub)
                    todo -= max
                    val items = itemsPerPlayer.remove(player)
                    if (items != null && items.isNotEmpty())
                    {
                        addItems(player, items)
                    }
                }
            } while (todo > 0 && maxTodo > 0)
        }, 2L, 2L)
    }


    fun toggleFunction(player: Player, function: Function<BlockBreakEvent>): Boolean
    {
        val functions = activatedFunctionsPerPlayer.getOrPut(player, { ConcurrentSkipListSet() })
        return if (functions.contains(function))
        {
            functions.remove(function)
            false
        } else
        {
            functions.add(function)
            true
        }
    }

    fun addBreakLocations(player: Player, locations: List<Location>, sendMessage: Boolean = false)
    {
        MCUtils.debug("BreakLocations are getting added", "BatchBreaker")
        val list: MutableList<Location> = ArrayList()
        list.addAll(locations)
        for (location in list)
        {
            this.locations.add(HandledLocation(location))
        }
        locationsPerPlayer.getOrPut(player, { ConcurrentHashMap.newKeySet() }).addAll(locations)
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onItemDrops(event: BlockDropItemEvent)
    {
        if (event.isCancelled)
        {
            return
        }

        val loc = locations.find { it == event.block.location } ?: return
        loc.handledDrop = true
        if (loc.isCompletelyHandled())
        {
            locations.remove(event.block.location)
        }
        event.isCancelled = true

        itemsPerPlayer.getOrPut(event.player, { ArrayList() }).addAll(event.items.map { it.itemStack })
        //event.items.clear()
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    override fun onEvent(event: BlockBreakEvent)
    {
        if (event.isCancelled)
        {
            return
        }

        val loc = locations.find { it == event.block.location }

        if (loc != null)
        {
            loc.handledBreak = true
            if (loc.isCompletelyHandled())
            {
                locations.remove(event.block.location)
            }
            return
        }

        if (event.player.isSneaking)
        {
            return
        }

        val functions = activatedFunctionsPerPlayer.getOrPut(event.player, { ConcurrentSkipListSet() })
        if (functions.isEmpty())
        {
            return
        }

        for (function in functions)
        {
            if (function.willHandle(event))
            {
                event.isCancelled = true
                GlobalScope.launch {
                    function.handle(event)
                }
                break
            }
        }
    }

    private fun getPageManager(player: Player) =
        pageManagers.getOrPut(player, { TypeSortedPageManager(player, "BlockBreak Inventory", allowStorage = false) })

    fun addItems(player: Player, itemStacks: MutableCollection<ItemStack>)
    {
        getPageManager(player).addItems(itemStacks)
    }

    fun fillInventory(player: Player)
    {
        getPageManager(player).fillPlayerInventory()
    }

    fun openInventory(player: Player)
    {
        getPageManager(player).displayInventory()
    }

}