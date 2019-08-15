package eu.scisneromam.mc.scismmoutils.functions

import eu.scisneromam.mc.scismmoutils.listener.BlockBreakListener
import eu.scisneromam.mc.scismmoutils.main.Main.Companion.MAIN
import eu.scisneromam.mc.scismmoutils.utils.MCUtils
import eu.scisneromam.mc.scismmoutils.utils.dropsAreEmpty
import eu.scisneromam.mc.scismmoutils.utils.isInMaxDistance
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent

/**
 * Project: ScisUtils
 * Initially created by scisneromam on 30.07.2019.
 * @author scisneromam
 * ---------------------------------------------------------------------
 * Copyright © 2019 | scisneromam | All rights reserved.
 */
class Miner(override val listener: BlockBreakListener) : Function<BlockBreakEvent>(FunctionType.MINER, listener)
{

    val woods: MutableList<Material> = ArrayList()
    val leaves: MutableList<Material> = ArrayList()
    val ores: MutableList<Material> = ArrayList()

    init
    {
        for (value in Material.values())
        {
            val name = value.name.toLowerCase()
            if (name.contains("legacy") || !value.isBlock)
            {
                continue
            }
            if (name.contains("wood") || name.contains("log"))
            {
                woods.add(value)
            }
            if (name.contains("leave"))
            {
                leaves.add(value)
            }
            if (name.contains("ore"))
            {
                ores.add(value)
            }
        }
        ores.add(Material.GLOWSTONE)
    }

    override fun willHandle(event: BlockBreakEvent): Boolean
    {
        MCUtils.debug("Handling event $event", "Miner")
        if (event.block.dropsAreEmpty())
        {
            MCUtils.debug("Stopping because there would be no drops", "Miner")
            return false
        }

        val material = event.block.blockData.material
        return when
        {
            leaves.contains(material) || woods.contains(material) || ores.contains(material) -> true
            else -> false
        }
    }

    override fun handle(event: BlockBreakEvent)
    {
        val material = event.block.blockData.material
        val maxDistance = MAIN.dbConnection.breakXpFunction.getXPLevelFromDB(event.player.uniqueId).level
        when
        {
            leaves.contains(material) -> findLocations(
                event.player,
                event.block.location,
                listOf(material),
                maxDistance
            )

            woods.contains(material) -> findLocations(
                event.player,
                event.block.location,
                getSameTree(material),
                maxDistance
            )

            ores.contains(material) -> findLocations(event.player, event.block.location, listOf(material), maxDistance)

        }
    }

    private fun getSameTree(material: Material): List<Material>
    {
        var name = material.name.toLowerCase()
        if (name.startsWith("stripped_"))
        {
            name = name.substring("stripped_".length)
        }
        name = name.substring(0, name.lastIndexOf("_"))

        val list: MutableList<Material> = ArrayList()

        for (mat in woods + leaves)
        {
            if (mat.name.toLowerCase().contains(name))
            {
                list.add(mat)
            }
        }

        return list
    }

    private fun findLocations(player: Player, start: Location, materials: List<Material>, maxDistance: Int = 3)
    {
        MCUtils.debug("Finding Locations", "Miner")
        val finishedLocations: MutableSet<Location> = HashSet()
        val locations: MutableSet<Location> = HashSet()

        locations.add(start)

        while (locations.isNotEmpty())
        {
            val location = locations.first()
            locations.remove(location)
            finishedLocations.add(location)

            for (x in -1..1)
            {
                for (y in -1..1)
                {
                    for (z in -1..1)
                    {
                        val block = Location(
                            location.world,
                            location.x + x.toDouble(),
                            location.y + y.toDouble(),
                            location.z + z.toDouble()
                        ).block

                        val bloc = block.location

                        when
                        {
                            block.blockData.material !in materials -> MCUtils.debug(
                                "Not Selecting $block because it is the wrong material",
                                "Miner"
                            )
                            !bloc.isInMaxDistance(start, maxDistance) -> MCUtils.debug(
                                "Not Selecting $block because it is not in range",
                                "Miner"
                            )
                            finishedLocations.contains(bloc) -> MCUtils.debug(
                                "Not Selecting $block because it is already selected",
                                "Miner"
                            )
                            else ->
                            {
                                MCUtils.debug(
                                    "Selecting $block because it is in range and the valid material",
                                    "Miner"
                                )
                                locations.add(bloc)
                            }
                        }
                    }
                }
            }
        }

        listener.addBreakLocations(player, finishedLocations.toList())
    }

}