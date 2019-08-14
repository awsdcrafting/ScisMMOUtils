package eu.scisneromam.mc.scismmoutils.listener

import eu.scisneromam.mc.scismmoutils.main.Main
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

/**
 * Project: ScisUtils
 * Initially created by scisneromam on 31.07.2019.
 * @author scisneromam
 * ---------------------------------------------------------------------
 * Copyright © 2019 | scisneromam | All rights reserved.
 */
abstract class EventListener<E : Event>(val main: Main) : Listener
{
    @EventHandler
    abstract fun onEvent(event: E)

}