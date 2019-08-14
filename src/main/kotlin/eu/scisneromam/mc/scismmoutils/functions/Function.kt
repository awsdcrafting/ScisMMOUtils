package eu.scisneromam.mc.scismmoutils.functions

import eu.scisneromam.mc.scismmoutils.listener.EventListener
import org.bukkit.event.Event

/**
 * Project: ScisUtils
 * Initially created by scisneromam on 30.07.2019.
 * @author scisneromam
 * ---------------------------------------------------------------------
 * Copyright © 2019 | scisneromam | All rights reserved.
 */
abstract class Function<E : Event>(val type: FunctionType, open val listener : EventListener<E>) : Comparable<Function<E>>
{


    override fun compareTo(other: Function<E>): Int = type.compareTo(other.type)

    abstract fun handle(event: E)
    abstract fun willHandle(event: E): Boolean


}