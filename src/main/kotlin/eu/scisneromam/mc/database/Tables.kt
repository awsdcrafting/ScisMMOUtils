package eu.scisneromam.mc.database

import eu.scisneromam.mc.data.XPLevel
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.LongIdTable
import java.util.*

/**
 * Project: ScisUtils
 * Initially created by scisneromam on 06.08.2019.
 * @author scisneromam
 * ---------------------------------------------------------------------
 * Copyright © 2019 | scisneromam | All rights reserved.
 */
object BreakXPLevelTable : LongIdTable()
{
    val uuid = varchar("uuid", 36)
    val level = integer("level").default(1)
    val xp = long("xp").default(0)
}

class BreakXPLevelEntity(id: EntityID<Long>) : LongEntity(id)
{
    companion object : LongEntityClass<BreakXPLevelEntity>(BreakXPLevelTable)

    var uuid by BreakXPLevelTable.uuid
    var level by BreakXPLevelTable.level
    var xp by BreakXPLevelTable.xp

    fun toXPLevel(): XPLevel
    {
        return XPLevel(UUID.fromString(uuid), level, xp)
    }
}

