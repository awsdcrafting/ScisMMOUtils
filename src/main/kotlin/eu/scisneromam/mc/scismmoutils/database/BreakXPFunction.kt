package eu.scisneromam.mc.scismmoutils.database

import eu.scisneromam.mc.scismmoutils.data.XPLevel
import eu.scisneromam.mc.scismmoutils.utils.sendPrefixedMessage
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.block.BlockBreakEvent
import java.util.*

/**
 * Project: ScisUtils
 * Initially created by scisneromam on 10.08.2019.
 * @author scisneromam
 * ---------------------------------------------------------------------
 * Copyright © 2019 | scisneromam | All rights reserved.
 */
class BreakXPFunction(database: DBConnection, cache: Boolean = true) : XPFunction<BreakXPLevelEntity>(database, cache)
{

    override fun getXPLevelEntity(uuid: UUID): BreakXPLevelEntity
    {
        return database.transaction {
            BreakXPLevelEntity.find {
                BreakXPLevelTable.uuid eq uuid.toString()
            }.firstOrNull() ?: BreakXPLevelEntity.new {
                this.uuid = uuid.toString()
            }
        }
    }

    /**
     * Gets the XPLevel for the given UUID from the DataBase
     * @param uuid - The UUID to get the XPLevel for
     * @return - The XPLevel for the given uuid or a new one if there was none in the database
     */
    override fun getXPLevelFromDB(uuid: UUID): XPLevel
    {
        return database.transaction {
            getXPLevelEntity(uuid).toXPLevel()
        }
    }

    override fun save(xpLevel: XPLevel, flushCache: Boolean)
    {
        database.transaction {
            val entity = getXPLevelEntity(xpLevel.uuid)
            entity.level = xpLevel.level
            entity.xp = xpLevel.xp
            if (flushCache)
            {
                flushCache()
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onBlockBreak(event: BlockBreakEvent)
    {
        if (event.isCancelled)
        {
            return
        }
        val silkTouch = event.player.inventory.itemInMainHand.enchantments.keys.contains(Enchantment.SILK_TOUCH)
        val xp: Long = when (event.block.blockData.material)
        {
            Material.COAL_ORE,
            Material.NETHER_QUARTZ_ORE -> 5
            Material.IRON_ORE -> 10
            Material.GOLD_ORE,
            Material.LAPIS_ORE,
            Material.REDSTONE_ORE -> 15
            Material.DIAMOND_ORE,
            Material.EMERALD_ORE -> 25
            else -> 1
        }
        if (xp > 0 && (xp == 1L || !silkTouch))
        {
            if (addXP(event.player.uniqueId, xp))
            {
                event.player.sendPrefixedMessage("You are now level ${getXPLevel(event.player.uniqueId).level}")
            }
        }
    }
}