package eu.scisneromam.mc.scismmoutils.data

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
    var level: Int = 1,
    var xp: Long = 0,
    private val mathFunction: (Int) -> Long = MathFunctions.DEFAULT.value
)
{

    fun addLevel(level: Int): XPLevel
    {
        this.level += level
        adjustLevel()
        return this
    }

    fun removeLevel(level: Int): XPLevel
    {
        this.level -= level
        adjustLevel()
        return this
    }

    fun adjustLevel(): XPLevel
    {
        if (xp < 0)
        {
            removeXP(0)
        } else
        {
            addXP(0)
        }
        return this
    }

    fun addXP(xp: Long): XPLevel
    {
        var summed = this.xp + xp
        var xpToNextLevel = xpToNextLevel()

        while (summed > xpToNextLevel)
        {
            this.level++
            summed -= xpToNextLevel
            xpToNextLevel = xpToNextLevel()
        }
        this.xp = summed
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

    fun removeXP(xp: Long): XPLevel
    {
        var minus = this.xp - xp
        while (minus < 0)
        {
            this.level--
            if (level == 1)
            {
                minus = 0
                break
            }
            minus = xpToNextLevel() - minus
        }
        this.xp = minus
        return this
    }

    operator fun minusAssign(n: Long)
    {
        removeXP(n)
    }

    operator fun minus(n: Long): XPLevel
    {
        return XPLevel(uuid, level, xp).removeXP(n)
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