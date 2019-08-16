package eu.scisneromam.mc.scismmoutils.inventory

import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import java.util.function.Consumer

/**
 * Project: ScisUtils
 * Initially created by scisneromam on 15.08.2019.
 * @author scisneromam
 * ---------------------------------------------------------------------
 * Copyright © 2019 | scisneromam | All rights reserved.
 */
class PageSortedPageManager(
    player: Player,
    name: String = "",
    parent: PageManager? = null,
    maxSize: Int = -1,
    sizePerPage: Int = 45,
    allowStorage: Boolean = true
) : PageManager(player, "$name Pages", parent, -1, 45, false)
{
    val pageManager: PageManager = PageManager(player, name, this, maxSize, sizePerPage, allowStorage)

    var lastSlot: Pair<Int, Int> = Pair(0, -1)

    init
    {
        addButton(createButton(0))
    }

    override fun addItems(itemStacks: MutableCollection<ItemStack>)
    {
        pageManager.addItems(itemStacks)
    }

    override fun fillPlayerInventory()
    {
        pageManager.fillPlayerInventory()
    }

    override fun isEmpty(): Boolean
    {
        return pageManager.isEmpty()
    }

    override fun getInventory(): Inventory
    {
        return pageManager.inventory
    }

    fun createButton(pageNumber: Int): GuiButton
    {
        return GuiButton.create(
            ItemCreator(Material.PAPER).setDisplayName("Page ${pageNumber + 1}").create(),
            createConsumer(pageNumber)
        )
    }

    fun createConsumer(pageNumber: Int): Consumer<InventoryClickEvent>
    {
        return Consumer {
            it.isCancelled = true
            pageManager.displayInventory(pageNumber)
        }
    }

    override fun displayInventory(delay: Long)
    {
        var lastSlot = lastSlot.first * sizePerPage + lastSlot.second
        val size = pageManager.size
        while (lastSlot >= size)
        {
            removeLastButton()
            lastSlot--
        }
        while (lastSlot < size - 1)
        {
            lastSlot++
            addButton(createButton(lastSlot))
        }


        super.displayInventory(delay)
    }

    fun removeLastButton()
    {
        inventoryPages[lastSlot.first].inventory.clear(lastSlot.second)

        lastSlot = if (lastSlot.second - 1 < 0)
        {
            if (lastSlot.first - 1 < 0)
            {
                Pair(0, 0)
            } else
            {
                inventoryPages.removeAt(lastSlot.first)
                Pair(lastSlot.first - 1, getInventory(lastSlot.first - 1).size - 1)
            }
        } else
        {
            Pair(lastSlot.first, lastSlot.second - 1)
        }

    }

    fun addButton(guiButton: GuiButton)
    {
        lastSlot = if (lastSlot.second + 1 >= getInventory(lastSlot.first).size)
        {
            createAndAddInventory()
            Pair(lastSlot.first + 1, 0)
        } else
        {
            Pair(lastSlot.first, lastSlot.second + 1)
        }
        getInventory(lastSlot.first).setGuiButton(lastSlot.second, guiButton)

    }

}