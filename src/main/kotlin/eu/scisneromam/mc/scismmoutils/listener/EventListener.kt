package eu.scisneromam.mc.scismmoutils.listener

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
abstract class EventListener<E : Event> : Listener
{
    @EventHandler
    abstract fun onEvent(event: E)

}