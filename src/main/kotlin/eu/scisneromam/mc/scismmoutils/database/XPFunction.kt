package eu.scisneromam.mc.scismmoutils.database

import eu.scisneromam.mc.scismmoutils.data.XPLevel
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.jetbrains.exposed.dao.LongEntity
import java.util.*

/**
 * Project: ScisUtils
 * Initially created by scisneromam on 10.08.2019.
 * @author scisneromam
 * ---------------------------------------------------------------------
 * Copyright © 2019 | scisneromam | All rights reserved.
 */
abstract class XPFunction<E : LongEntity>(database: DBConnection, var cache: Boolean = true) :
    DBFunction(database), Listener
{


    private val map: MutableMap<UUID, XPLevel> = HashMap()

    /**
     * @return returns true if the player performed a level up
     */
    fun addXP(uuid: UUID, xp: Long): Boolean
    {
        return if (cache)
        {
            val xpLevel = map.getOrPut(uuid, { getXPLevelFromDB(uuid) })
            val level = xpLevel.level
            xpLevel.addXP(xp)
            xpLevel.level > level
        } else
        {
            val xpLevel = getXPLevelFromDB(uuid)
            val level = xpLevel.level
            xpLevel.addXP(xp)
            save(xpLevel)
            xpLevel.level > level
        }
    }

    abstract fun getXPLevelEntity(uuid: UUID): E

    /**
     * Gets the XPLevel for the given UUID from the DataBase
     * @param uuid - The UUID to get the XPLevel for
     * @return - The XPLevel for the given uuid or a new one if there was none in the database
     */
    abstract fun getXPLevelFromDB(uuid: UUID): XPLevel

    fun getXPLevel(uuid: UUID): XPLevel
    {
        return if (cache)
        {
            map.getOrPut(uuid, { getXPLevelFromDB(uuid) })
        } else
        {
            getXPLevelFromDB(uuid)
        }
    }

    override fun save()
    {
        map.forEach { (_, xpLevel) -> save(xpLevel) }
    }

    abstract fun save(xpLevel: XPLevel)

    @EventHandler(priority = EventPriority.MONITOR)
    fun onPlayerJoin(event: PlayerJoinEvent)
    {
        if (cache)
        {
            map[event.player.uniqueId] = getXPLevelFromDB(event.player.uniqueId)
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onPlayerLeave(event: PlayerQuitEvent)
    {
        if (cache)
        {
            val xpLevel = map.remove(event.player.uniqueId) ?: return
            save(xpLevel)
        }
    }

}