package eu.scisneromam.mc.inventory

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import java.util.function.Consumer

/**
 * Project: ScisUtils
 * Initially created by scisneromam on 13.08.2019.
 * @author scisneromam
 * ---------------------------------------------------------------------
 * Copyright © 2019 | scisneromam | All rights reserved.
 */
class InventoryPage(val player: Player, val pageManager: PageManager, val size: Int = 45)
{
    val inventory: Inventory = Bukkit.createInventory(pageManager, size + 9)
    val buttons: MutableMap<Int, GuiButton> = HashMap()

    init
    {
        setUpHotbar()
    }

    fun setUpHotbar()
    {
        val leftStack = ItemStack(Material.ARROW)
        val leftMeta = leftStack.itemMeta
        if (leftMeta != null)
        {
            leftMeta.setDisplayName("Previous Page")


            leftStack.itemMeta = leftMeta
        }

        val rightStack = ItemStack(Material.ARROW)
        val rightMeta = rightStack.itemMeta
        if (rightMeta != null)
        {
            rightMeta.setDisplayName("Next Page")


            rightStack.itemMeta = rightMeta
        }
        val leftButton = createPreviousPageButton(leftStack)
        val rightButton = createNextPageButton(rightStack)
        setGuiButton(size, leftButton)
        setGuiButton(size + 8, rightButton)
        val hotBarStack = ItemStack(Material.GRAY_STAINED_GLASS_PANE)
        val hotBarMeta = hotBarStack.itemMeta
        if (hotBarMeta != null)
        {
            hotBarMeta.setDisplayName("")
            hotBarStack.itemMeta = hotBarMeta
        }
        for (i in 1..7)
        {
            setGuiButton(size + i, createGuiButton(hotBarStack, Consumer { it.isCancelled = true }))
        }
    }

    fun isEmpty(): Boolean
    {
        for (i in 0 until size)
        {
            val item = inventory.getItem(i) ?: continue
            if (item.type != Material.AIR)
            {
                return false
            }
        }
        return true
    }

    fun setGuiButton(index: Int, guiButton: GuiButton)
    {
        buttons[index] = guiButton
        inventory.setItem(index, guiButton.itemStack)
    }

    fun getGuiButton(index: Int): GuiButton?
    {
        return buttons[index]
    }

    fun executeListener(event: InventoryClickEvent)
    {
        if (event.inventory != inventory)
        {
            return
        }
        getGuiButton(event.slot)?.listener?.accept(event)
    }

    fun createNextPageButton(itemStack: ItemStack): GuiButton
    {
        val button = GuiButton(itemStack)
        button.listener = Consumer {
            it.isCancelled = true
            pageManager.displayNextInventory()
        }
        return button
    }

    fun createPreviousPageButton(itemStack: ItemStack): GuiButton
    {
        val button = GuiButton(itemStack)
        button.listener = Consumer {
            it.isCancelled = true
            pageManager.displayPreviousInventory()
        }
        return button
    }

    fun createGuiButton(itemStack: ItemStack, consumer: Consumer<InventoryClickEvent>): GuiButton
    {
        val button = GuiButton(itemStack)
        button.listener = consumer
        return button
    }

    fun clear()
    {
        for (i in 0 until size)
        {
            inventory.clear(i)
        }
    }

}