package eu.scisneromam.mc.scismmoutils.inventory

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryDragEvent
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
class InventoryPage(
    val player: Player,
    val pageManager: PageManager,
    var pageNumber: Int = 1,
    val parent: PageManager? = null,
    val size: Int = 45,
    val allowStorage: Boolean = true
)
{
    val inventory: Inventory = Bukkit.createInventory(pageManager, size + 9, pageManager.name)
    val buttons: MutableMap<Int, GuiButton> = HashMap()

    init
    {
        setUpHotbar()
    }

    fun setUpHotbar()
    {
        val leftStack = ItemCreator(Material.ARROW).setDisplayName("Previous page").create()
        val rightStack = ItemCreator(Material.ARROW).setDisplayName("Next page").create()
        val leftButton = createPreviousPageButton(leftStack)
        val rightButton = createNextPageButton(rightStack)
        setGuiButton(size, leftButton)
        setGuiButton(size + 8, rightButton)
        val hotBarStack = ItemCreator(Material.GRAY_STAINED_GLASS_PANE).setDisplayName("").create()
        for (i in 1..7)
        {
            setGuiButton(size + i, GuiButton.create(hotBarStack, Consumer { it.isCancelled = true }))
        }
        updatePageIndicator()
    }

    fun updatePageIndicator()
    {
        val pageIndicatorStack = if (parent == null)
        {
            ItemCreator(Material.PAPER).setDisplayName("Page $pageNumber").create()
        } else
        {
            ItemCreator(Material.PAPER).setDisplayName("Page $pageNumber")
                .setLore(listOf("Click here to go up")).create()
        }
        setGuiButton(size + 4, GuiButton.create(pageIndicatorStack, Consumer {
            it.isCancelled = true
            parent?.displayInventory()
        }))
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

    fun removeItem(index: Int)
    {
        inventory.clear(index)
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
        if (!allowStorage)
        {
            if (event.clickedInventory == event.whoClicked.inventory)
            {
                if (event.isShiftClick)
                {
                    val item = event.currentItem
                    if (item != null && item.type != Material.AIR)
                    {
                        event.isCancelled = true
                    }
                }
            } else
            {
                val item = event.cursor
                if (item != null && item.type != Material.AIR)
                {
                    event.isCancelled = true
                }
            }

            if (event.isCancelled)
            {
                return
            }
        }

        if (event.inventory != inventory)
        {
            return
        }
        getGuiButton(event.slot)?.listener?.accept(event)
    }

    fun executeListener(event: InventoryDragEvent)
    {
        if (!allowStorage)
        {
            val item = event.oldCursor
            val size = event.inventory.size
            for (i in event.rawSlots)
            {
                if (i < size)
                {
                    event.isCancelled = true
                    return
                }
            }

        }
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


    fun clear()
    {
        for (i in 0 until size)
        {
            inventory.clear(i)
        }
    }

}