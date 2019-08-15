package eu.scisneromam.mc.scismmoutils.inventory

import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import java.util.function.Consumer

/**
 * Project: ScisUtils
 * Initially created by scisneromam on 13.08.2019.
 * @author scisneromam
 * ---------------------------------------------------------------------
 * Copyright © 2019 | scisneromam | All rights reserved.
 */
class GuiButton(val itemStack: ItemStack)
{
    var listener: Consumer<InventoryClickEvent>? = null

    companion object
    {
        fun create(itemStack: ItemStack, consumer: Consumer<InventoryClickEvent>?): GuiButton
        {
            val button = GuiButton(itemStack)
            button.listener = consumer
            return button
        }
    }

}