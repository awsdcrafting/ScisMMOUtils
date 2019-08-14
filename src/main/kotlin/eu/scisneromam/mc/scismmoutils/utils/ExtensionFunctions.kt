package eu.scisneromam.mc.scismmoutils.utils

import eu.scisneromam.mc.scismmoutils.main.Main
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.entity.Player
import kotlin.math.absoluteValue

/**
 * Project: ScisUtils
 * Initially created by scisneromam on 30.07.2019.
 * @author scisneromam
 * ---------------------------------------------------------------------
 * Copyright © 2019 | scisneromam | All rights reserved.
 */

fun Location.isInMaxDistance(other: Location, maxDist: Int): Boolean
{
    val dx = (x - other.x).absoluteValue
    val dy = (y - other.y).absoluteValue
    val dz = (z - other.z).absoluteValue
    val isInMaxRange = dx <= maxDist && dy <= maxDist && dz <= maxDist
    return isInMaxRange
}

fun Player.sendPrefixedMessage(message: String)
{
    sendMessage("${Main.PREFIX}$message")
}

fun Block.dropsAreEmpty(): Boolean
{
    return drops.isEmpty() || type == Material.AIR || drops.stream().allMatch { it.type == Material.AIR }
}