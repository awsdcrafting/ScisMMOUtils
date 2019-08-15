package eu.scisneromam.mc.scismmoutils.inventory

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.event.inventory.InventoryEvent

/**
 * Project: ScisUtils
 * Initially created by scisneromam on 13.08.2019.
 * @author scisneromam
 * ---------------------------------------------------------------------
 * Copyright © 2019 | scisneromam | All rights reserved.
 */
class InventoryListener : Listener
{

    private fun InventoryEvent.pageManager(): PageManager? = inventory.holder as? PageManager

    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent)
    {
        event.pageManager()?.executeListener(event)
    }

    @EventHandler
    fun onInventoryDrag(event: InventoryDragEvent)
    {
        event.pageManager()?.executeListener(event)
    }


}