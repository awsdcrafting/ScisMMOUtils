package eu.scisneromam.mc.scismmoutils.database

import eu.scisneromam.mc.scismmoutils.data.XPLevel
import eu.scisneromam.mc.scismmoutils.main.Main
import eu.scisneromam.mc.scismmoutils.utils.CacheMap
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
 *
 * @param database The connection to the database
 * @param cache If players should be cached
 * @param offlineCacheSize The size of the offline cache, the offline cache holds members for the cacheClearDelay after they left
 * @param cacheClearDelay After the user disconnects he will be cleared from the cache after this amount of ticks (default value is 30 min (30 * 60 * 20))
 */
abstract class XPFunction<E : LongEntity>(
    database: DBConnection,
    var cache: Boolean = true,
    var offlineCacheSize: Int = 50,
    var cacheClearDelay: Long = 30 * 60 * 20
) :
    DBFunction(database), Listener
{

    protected val onlineCache: MutableMap<UUID, XPLevel> = HashMap()
    protected val offlineCache: MutableMap<UUID, XPLevel> = CacheMap(offlineCacheSize)
    protected val taskMap: MutableMap<UUID, Int> = HashMap()

    /**
     * @return returns true if the player performed a level up
     */
    fun addXP(uuid: UUID, xp: Long): Boolean
    {
        return if (cache)
        {
            val xpLevel = onlineCache.getOrPut(uuid, { getXPLevelFromDB(uuid) })
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

    /**
     * Gets the XPLevelEntity from the database
     * @param uuid the uuid to get the XPLevelEntity for
     * @return - The associated XPLevelEntity or a new one if there was none in the database
     */
    abstract fun getXPLevelEntity(uuid: UUID): E

    /**
     * Gets the XPLevel for the given UUID from the database
     * This method should call the [getXPLevelEntity] method
     * @param uuid - The UUID to get the XPLevel for
     * @return - The XPLevel for the given uuid or a new one if there was none in the database
     */
    abstract fun getXPLevelFromDB(uuid: UUID): XPLevel

    fun getXPLevel(uuid: UUID): XPLevel
    {
        return if (cache)
        {
            onlineCache.getOrPut(uuid, { getXPLevelFromDB(uuid) })
        } else
        {
            getXPLevelFromDB(uuid)
        }
    }

    override fun save()
    {
        offlineCache.forEach { (_, xpLevel) -> save(xpLevel, false) }
        onlineCache.forEach { (_, xpLevel) -> save(xpLevel, false) }

        database.transaction {
            flushCache()
        }
    }

    abstract fun save(xpLevel: XPLevel, flushCache: Boolean = true)

    @EventHandler(priority = EventPriority.MONITOR)
    fun onPlayerJoin(event: PlayerJoinEvent)
    {
        if (cache)
        {
            removeTask(event.player.uniqueId)
            val cachedXPLevel = offlineCache.remove(event.player.uniqueId)
            if (cachedXPLevel != null)
            {
                onlineCache[event.player.uniqueId] = cachedXPLevel
            }
            if (!onlineCache.containsKey(event.player.uniqueId))
            {
                onlineCache[event.player.uniqueId] = getXPLevelFromDB(event.player.uniqueId)
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onPlayerLeave(event: PlayerQuitEvent)
    {
        if (cache)
        {
            val xpLevel = onlineCache.remove(event.player.uniqueId) ?: return
            offlineCache[event.player.uniqueId] = xpLevel
            save(xpLevel)
            createTask(event.player.uniqueId)
        }
    }

    protected fun save(uuid: UUID)
    {
        val xpLevel = offlineCache.remove(uuid) ?: return
        save(xpLevel)
    }

    private fun createTask(uuid: UUID)
    {
        val id = Main.MAIN.server.scheduler.scheduleSyncDelayedTask(Main.MAIN, { save(uuid) }, cacheClearDelay)
        if (id == -1)
        {
            return
        }
        taskMap[uuid] = id
    }

    private fun removeTask(uuid: UUID)
    {
        val id = taskMap.remove(uuid) ?: return
        Main.MAIN.server.scheduler.cancelTask(id)
    }


}