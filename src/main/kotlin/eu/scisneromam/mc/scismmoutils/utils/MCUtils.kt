package eu.scisneromam.mc.scismmoutils.utils

import eu.scisneromam.mc.scismmoutils.main.Main
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.util.BlockIterator

import java.util.ArrayList
import kotlin.math.abs

/**
 * Project: HammerSpigotPlugin
 * Initially created by scisneromam on 11.01.2019.
 *
 * @author scisneromam
 * ---------------------------------------------------------------------
 * Copyright © 2019 | scisneromam | All rights reserved.
 */
object MCUtils
{

    const val DEBUG = false

    fun getBlocksInLine(player: Player, range: Int): List<Block>
    {
        val blockList = ArrayList<Block>()
        val iter = BlockIterator(player, range)
        var lastBlock: Block
        while (iter.hasNext())
        {
            lastBlock = iter.next()
            blockList.add(lastBlock)
            if (lastBlock.type.isSolid)
            {
                break
            }
        }
        return blockList
    }

    fun breakBlock(main: Main, block: Block, player: Player, tool: ItemStack, debugName: String, debug: Boolean)
    {
        val drops = block.getDrops(tool)
        if (drops.isEmpty() || block.blockData.material == Material.AIR || drops.stream().allMatch { it -> it.type == Material.AIR })
        {
            debug("Not breaking $block because we would not get anything", debugName, debug)
            return
        }

        val bLoc = block.location
        val pLoc = player.location
        val dy = bLoc.y - pLoc.y
        if (abs(bLoc.x - pLoc.x) < 1.0 && abs(bLoc.z - pLoc.z) < 1.0 && dy <= 0 && dy >= -1.1)
        {
            if (!player.isFlying && !player.allowFlight)
            {
                debug("Not breaking $block because it is directly under the player", debugName, debug)

                return
            }
        }

        val meta = tool.itemMeta
        if (meta is Damageable)
        {
            val maxDurability = tool.type.maxDurability - 1
            if ((meta as Damageable).damage >= maxDurability)
            {
                if (player.hasPermission("scisUtils.cheat.keepTool"))
                {
                    (meta as Damageable).damage = Math.max((meta as Damageable).damage - 1, 0)
                    tool.itemMeta = meta
                } else
                {
                    debug("Not breaking " + block + "because the tool would break too", debugName, debug)
                    return
                }
            }
        }


        debug("Breaking $block", debugName, debug)
        main.nmsBlockBreak.breakBlock(player, block)
        debug("Broke $block", debugName, debug)
    }

    fun debug(message: String, debugName: String)
    {
        if (DEBUG)
        {
            println("[ScisUtils DEBUG] [$debugName] $message")
        }
    }

    fun debug(message: String, debugName: String, debug: Boolean)
    {
        if (debug)
        {
            println("[ScisUtils DEBUG] [$debugName] $message")
        }
    }

}
