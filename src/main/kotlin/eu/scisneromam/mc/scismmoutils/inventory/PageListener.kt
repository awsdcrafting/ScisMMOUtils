package eu.scisneromam.mc.scismmoutils.inventory

import eu.scisneromam.mc.scismmoutils.listener.EventListener
import eu.scisneromam.mc.scismmoutils.main.Main
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack

/**
 * Project: ScisUtils
 * Initially created by scisneromam on 13.08.2019.
 * @author scisneromam
 * ---------------------------------------------------------------------
 * Copyright © 2019 | scisneromam | All rights reserved.
 */
class PageListener(main: Main) : EventListener<InventoryClickEvent>(main)
{

    @EventHandler
    override fun onEvent(event: InventoryClickEvent)
    {
        (event.inventory.holder as? PageManager)?.executeListener(event)
    }



}