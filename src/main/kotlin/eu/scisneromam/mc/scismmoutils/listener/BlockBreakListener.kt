package eu.scisneromam.mc.scismmoutils.listener

import eu.scisneromam.mc.scismmoutils.functions.Function
import eu.scisneromam.mc.scismmoutils.functions.Hammer
import eu.scisneromam.mc.scismmoutils.functions.Miner
import eu.scisneromam.mc.scismmoutils.inventory.PageManager
import eu.scisneromam.mc.scismmoutils.main.Main
import eu.scisneromam.mc.scismmoutils.utils.MCUtils
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.bukkit.Location
import org.bukkit.entity.Item
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
class BlockBreakListener(main: Main) : EventListener<BlockBreakEvent>(main)
{

    class HandledLocation(val location: Location)
    {
        var handledBreak: Boolean = false
        var handledDrop: Boolean = false

        fun isCompletelyHandled(): Boolean = handledBreak && handledDrop
    }

    companion object
    {
        const val DEFAULT_BATCH_SIZE: Int = 500
    }

    var batchSizePerPlayer: Int = DEFAULT_BATCH_SIZE
    private val itemsPerPlayer: MutableMap<Player, MutableList<ItemStack>> = ConcurrentHashMap()
    private val locations: MutableMap<Location, HandledLocation> = ConcurrentHashMap()
    private val activatedFunctionsPerPlayer: MutableMap<Player, MutableSet<Function<BlockBreakEvent>>> =
        ConcurrentHashMap()
    private val pageManagers: MutableMap<Player, PageManager> = HashMap()

    val miner: Miner = Miner(this)
    val hammer: Hammer = Hammer(this)


    init
    {
        batchSizePerPlayer = main.config.getInt("batchSizePerPlayer")
        if (batchSizePerPlayer == 0)
        {
            batchSizePerPlayer = DEFAULT_BATCH_SIZE
        }

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

    fun addBreakLocations(player: Player, locations: List<Location>)
    {
        MCUtils.debug("BreakLocations are getting added", "BatchBreaker")
        val list = ArrayList(locations)
        for (location in list)
        {
            this.locations[location] = HandledLocation(location)
        }
        main.server.scheduler.runTask(main) {
            ->
            for (location in list)
            {
                MCUtils.breakBlock(
                    main,
                    location.block,
                    player,
                    player.inventory.itemInMainHand,
                    "BatchBreaker", MCUtils.DEBUG
                )
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
        event.isCancelled = true

        val items: MutableList<Item> = ArrayList(event.items)
        event.items.clear()
        GlobalScope.launch {
            val loc = locations.getOrDefault(event.block.location, null) ?: return@launch

            loc.handledDrop = true
            if (loc.isCompletelyHandled())
            {
                locations.remove(event.block.location)
            }

            itemsPerPlayer.getOrPut(event.player, { Collections.synchronizedList(ArrayList()) })
                .addAll(items.map { it.itemStack })
        }

    }

    @EventHandler(priority = EventPriority.HIGHEST)
    override fun onEvent(event: BlockBreakEvent)
    {
        if (event.isCancelled)
        {
            return
        }

        MCUtils.debug("Handling event $event", "BatchBreaker")

        val loc = locations.getOrDefault(event.block.location, null)

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
        pageManagers.getOrPut(player, { PageManager(player, main.pageListener) })

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