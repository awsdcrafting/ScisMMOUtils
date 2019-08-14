package eu.scisneromam.mc.data

import java.util.*
import kotlin.math.pow

/**
 * Project: ScisUtils
 * Initially created by scisneromam on 10.08.2019.
 * @author scisneromam
 * ---------------------------------------------------------------------
 * Copyright © 2019 | scisneromam | All rights reserved.
 */
class XPLevel(
    val uuid: UUID,
    level: Int = 1,
    xp: Long = 0,
    private val mathFunction: (Int) -> Long = MathFunctions.DEFAULT.value
)
{
    var level: Int = level
        private set

    var xp: Long = xp
        private set

    fun addXP(xp: Long): XPLevel
    {
        val summed = this.xp + xp
        val xpToNextLevel = xpToNextLevel()
        this.xp = if (summed > xpToNextLevel)
        {
            this.level++
            summed - xpToNextLevel
        } else
        {
            summed
        }
        return this
    }

    operator fun plusAssign(n: Long)
    {
        addXP(n)
    }

    operator fun plus(n: Long): XPLevel
    {
        return XPLevel(uuid, level, xp).addXP(n)
    }


    fun xpToNextLevel(): Long
    {
        return xpToNextLevel(level, mathFunction)
    }

    override fun toString(): String
    {
        return "XPLevel(uuid=$uuid, level=$level, xp=$xp)"
    }

    companion object
    {

        fun xpToNextLevel(curLevel: Int, mathFunction: (Int) -> Long): Long
        {
            return mathFunction.invoke(curLevel)
        }
    }

    enum class MathFunctions(val value: (Int) -> Long)
    {
        DEFAULT({ level -> (level.toDouble().pow(1.1) * 1_000).pow(1.1).toLong() })
    }
}