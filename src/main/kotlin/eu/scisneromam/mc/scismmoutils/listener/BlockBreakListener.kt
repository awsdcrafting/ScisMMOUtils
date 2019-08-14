package eu.scisneromam.mc.scismmoutils.listener

import eu.scisneromam.mc.scismmoutils.functions.Function
import eu.scisneromam.mc.scismmoutils.functions.Hammer
import eu.scisneromam.mc.scismmoutils.functions.Miner
import eu.scisneromam.mc.scismmoutils.main.Main
import eu.scisneromam.mc.scismmoutils.utils.MinecraftUtils
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
import kotlin.math.min

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
        const val DEFAULT_BATCH_SIZE: Int = 2_500
    }

    var batchSize: Int = DEFAULT_BATCH_SIZE
    var batchSizePerPlayer: Int = batchSize / 10
    private val locationsPerPlayer: MutableMap<Player, MutableSet<Location>> = ConcurrentHashMap()
    private val itemsPerPlayer: MutableMap<Player, MutableList<ItemStack>> = ConcurrentHashMap()
    private val locations: MutableMap<Location, HandledLocation> = ConcurrentHashMap()
    private val activatedFunctionsPerPlayer: MutableMap<Player, MutableSet<Function<BlockBreakEvent>>> =
        ConcurrentHashMap()

    val miner: Miner = Miner(this)
    val hammer: Hammer = Hammer(this)


    init
    {
        batchSize = main.config.getInt("batchSize")
        if (batchSize == 0)
        {
            batchSize = DEFAULT_BATCH_SIZE
        }
        batchSizePerPlayer = main.config.getInt("batchSizePerPlayer")
        if (batchSizePerPlayer == 0)
        {
            batchSizePerPlayer = batchSize / 10
        }


        main.server.scheduler.scheduleSyncRepeatingTask(main, {
            var todo = batchSize
            var maxTodo: Int
            do
            {
                maxTodo = 0
                for (entry in locationsPerPlayer)
                {
                    maxTodo += entry.value.size
                    val max = min(min(batchSizePerPlayer, todo), entry.value.size)

                    val sub = entry.value.take(max)
                    sub.forEach { location: Location ->
                        MinecraftUtils.breakBlock(
                            main,
                            location.block,
                            entry.key,
                            entry.key.inventory.itemInMainHand,
                            "BatchBreaker", MinecraftUtils.DEBUG
                        )
                    }
                    entry.value.removeAll(sub)
                    todo -= max
                    val items = itemsPerPlayer.remove(entry.key)
                    if (items != null && items.isNotEmpty())
                    {
                        main.paginator.addItems(entry.key, items)
                    }
                }
            } while (todo > 0 && maxTodo > 0)
        }, 1L, 1L)
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
        MinecraftUtils.debug("BreakLocations are getting added", "BatchBreaker")
        for (location in locations)
        {
            this.locations[location] = HandledLocation(location)
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

        MinecraftUtils.debug("Handling event $event", "BatchBreaker")

        val loc = locations.getOrDefault(event.block.location, null)

        if (loc != null)
        {
            loc.handledDrop = true
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

}