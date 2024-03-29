package eu.scisneromam.mc.scismmoutils.functions

import eu.scisneromam.mc.scismmoutils.listener.BlockBreakListener
import eu.scisneromam.mc.scismmoutils.utils.MCUtils
import eu.scisneromam.mc.scismmoutils.utils.MCUtils.getBlocksInLine
import eu.scisneromam.mc.scismmoutils.utils.dropsAreEmpty
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.inventory.ItemStack
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors

/**
 * Project: ScisUtils
 * Initially created by scisneromam on 31.07.2019.
 * @author scisneromam
 * ---------------------------------------------------------------------
 * Copyright © 2019 | scisneromam | All rights reserved.
 */
class Hammer(override val listener: BlockBreakListener) : Function<BlockBreakEvent>(FunctionType.HAMMER, listener)
{
    val modePerPlayer: MutableMap<Player, Mode> = ConcurrentHashMap()


    override fun willHandle(event: BlockBreakEvent): Boolean
    {
        MCUtils.debug("Handling event $event", "Hammer")

        if (event.block.dropsAreEmpty())
        {
            return false
        }


        val middle = event.block.location

        val blockList = getBlocksInLine(event.player, 10)

        if (blockList.size >= 2 && blockList[blockList.size - 1].location == middle)
        {
            middle.block.getFace(blockList[blockList.size - 2]) // opposite of player facing
        } else
        {
            null
        } ?: return false
        return true
    }

    override fun handle(event: BlockBreakEvent)
    {

        val middle = event.block.location

        val blockList = getBlocksInLine(event.player, 10)

        val blockFace: BlockFace = if (blockList[blockList.size - 1].location == middle)
        {
            middle.block.getFace(blockList[blockList.size - 2]) // opposite of player facing
        } else
        {
            null
        } ?: return


        breakBlocks(event.player, middle, event.player.inventory.itemInMainHand, blockFace)
    }

    private fun breakBlocks(player: Player, middleBlock: Location, tool: ItemStack, blockFace: BlockFace)
    {

        val list: MutableList<Location> = ArrayList()

        val mode = modePerPlayer.getOrPut(player, { Mode() })
        val lrRad = mode.leftRightSize
        val udRad = if (mode.mode == HammerMode.INDIVIDUALLY)
        {
            mode.upDownSize
        } else
        {
            mode.leftRightSize
        }
        val depth = if (mode.mode != HammerMode.CUBE)
        {
            mode.depth
        } else
        {
            mode.leftRightSize * 2
        }

        var lrMod = 0
        var udMod = 0
        val subSets = (lrRad * 2) * (udRad * 2) * (depth) > listener.batchSize * 25
        if (subSets)
        {
            //player.sendPrefixedMessage("")
        }

        val playerFacing = player.facing

        for (mod1 in -lrRad..lrRad)
        {
            MCUtils.debug("$mod1 of $lrRad", "Hammer")
            when (playerFacing)
            {
                BlockFace.NORTH -> lrMod = mod1
                BlockFace.SOUTH -> lrMod = -mod1
                BlockFace.EAST -> lrMod = mod1
                BlockFace.WEST -> lrMod = -mod1
            }
            for (mod2 in -udRad..udRad)
            {
                when (playerFacing)
                {
                    BlockFace.NORTH -> udMod = -mod2
                    BlockFace.SOUTH -> udMod = -mod2
                    BlockFace.EAST -> udMod = -mod2
                    BlockFace.WEST -> udMod = -mod2
                }

                for (mod3 in depth downTo 0)
                {
                    if (mod3 == 0 && mod2 == 0 && mod1 == 0)
                    {
                        continue
                    }
                    var location: Location? = null

                    val dMod = when (blockFace)
                    {
                        BlockFace.SOUTH,
                        BlockFace.EAST,
                        BlockFace.UP -> -mod3

                        else -> mod3
                    }

                    when (blockFace)
                    {
                        BlockFace.UP, BlockFace.DOWN -> location =
                            Location(
                                middleBlock.world,
                                middleBlock.x + lrMod,
                                middleBlock.y + dMod,
                                middleBlock.z + udMod
                            )

                        BlockFace.EAST, BlockFace.EAST_NORTH_EAST, BlockFace.EAST_SOUTH_EAST, BlockFace.WEST, BlockFace.WEST_SOUTH_WEST, BlockFace.WEST_NORTH_WEST -> location =
                            Location(
                                middleBlock.world,
                                middleBlock.x + dMod,
                                middleBlock.y + udMod,
                                middleBlock.z + lrMod
                            )

                        BlockFace.NORTH, BlockFace.NORTH_NORTH_EAST, BlockFace.NORTH_NORTH_WEST, BlockFace.SOUTH, BlockFace.SOUTH_SOUTH_EAST, BlockFace.SOUTH_SOUTH_WEST -> location =
                            Location(
                                middleBlock.world,
                                middleBlock.x + lrMod,
                                middleBlock.y + udMod,
                                middleBlock.z + dMod
                            )
                    }
                    if (location != null)
                    {
                        val block = location.block
                        if (block.type != Material.AIR)
                        {
                            if (block.getDrops(tool).isNotEmpty())
                            {
                                list.add(location)
                            }
                        }
                    }
                    if (list.size >= listener.batchSize)
                    {
                        listener.addBreakLocations(player, list)
                        list.clear()
                    }
                }

            }
        }
        list.add(middleBlock)
        listener.addBreakLocations(player, list)
    }


    enum class HammerMode
    {
        INDIVIDUALLY,
        SQUARE,
        CUBE
    }

    class Mode(
        var upDownSize: Int = 1,
        var leftRightSize: Int = 1,
        var depth: Int = 0,
        var mode: HammerMode = HammerMode.SQUARE
    )
}