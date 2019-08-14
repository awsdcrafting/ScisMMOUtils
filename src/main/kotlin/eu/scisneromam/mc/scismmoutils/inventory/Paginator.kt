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
class Paginator(main: Main) : EventListener<InventoryClickEvent>(main)
{
    private val pageManagers: MutableMap<Player, PageManager> = HashMap()

    @EventHandler
    override fun onEvent(event: InventoryClickEvent)
    {
        (event.inventory.holder as? PageManager)?.executeListener(event)
    }

    private fun getPageManager(player: Player) = pageManagers.getOrPut(player, { PageManager(player, this) })

    fun addItems(player: Player, itemStacks: MutableCollection<ItemStack>)
    {
        getPageManager(player).addItems(itemStacks)
    }

    fun fillInventory(player: Player)
    {
        getPageManager(player).fillPlayerInventory()
    }

    fun openInventory(player: Player)
    {
        getPageManager(player).displayInventory()
    }

}