package eu.scisneromam.mc.scismmoutils.listener

import eu.scisneromam.mc.scismmoutils.functions.Function
import eu.scisneromam.mc.scismmoutils.functions.Hammer
import eu.scisneromam.mc.scismmoutils.functions.Miner
import eu.scisneromam.mc.scismmoutils.inventory.PageManager
import eu.scisneromam.mc.scismmoutils.main.Main.Companion.MAIN
import eu.scisneromam.mc.scismmoutils.utils.MCUtils
import eu.scisneromam.mc.scismmoutils.utils.sendPrefixedMessage
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
        const val DEFAULT_BATCH_SIZE: Int = 500
    }

    var batchSizePerPlayer: Int = DEFAULT_BATCH_SIZE
    private val itemsPerPlayer: MutableMap<Player, MutableList<ItemStack>> = ConcurrentHashMap()
    private val locations: MutableSet<HandledLocation> = ConcurrentHashMap.newKeySet()
    private val activatedFunctionsPerPlayer: MutableMap<Player, MutableSet<Function<BlockBreakEvent>>> =
        ConcurrentHashMap()
    private val pageManagers: MutableMap<Player, PageManager> = HashMap()

    val miner: Miner = Miner(this)
    val hammer: Hammer = Hammer(this)


    init
    {
        batchSizePerPlayer = MAIN.config.getInt("batchSizePerPlayer")
        if (batchSizePerPlayer == 0)
        {
            batchSizePerPlayer = DEFAULT_BATCH_SIZE
        }


        MAIN.server.scheduler.scheduleSyncRepeatingTask(MAIN, { ->

            for ((player, items) in itemsPerPlayer)
            {
                addItems(player, ArrayList(items))
            }

        }, 4L, 4L)
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
        MAIN.server.scheduler.scheduleSyncDelayedTask(MAIN) {
            ->
            for (location in list)
            {
                MCUtils.breakBlock(
                    location.block,
                    player,
                    player.inventory.itemInMainHand,
                    "BatchBreaker", MCUtils.DEBUG
                )
            }
            if (sendMessage)
            {
                player.sendPrefixedMessage("Finished breaking blocks")
            }
        }
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
        pageManagers.getOrPut(player, { PageManager(player, "BlockBreak Inventory") })

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