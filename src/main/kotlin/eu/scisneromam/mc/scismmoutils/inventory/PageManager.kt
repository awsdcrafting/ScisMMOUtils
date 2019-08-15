package eu.scisneromam.mc.scismmoutils.inventory

import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack

/**
 * Project: ScisUtils
 * Initially created by scisneromam on 13.08.2019.
 * @author scisneromam
 * ---------------------------------------------------------------------
 * Copyright © 2019 | scisneromam | All rights reserved.
 */
open class PageManager(
    val player: Player,
    protected val pageListener: PageListener,
    val name: String = "",
    var maxSize: Int = -1
) :
    InventoryHolder
{

    protected val inventoryPages: MutableList<InventoryPage> = ArrayList()
    var selectedInventory: Int = 0
        set(value)
        {
            field = when
            {
                value >= inventoryPages.size -> inventoryPages.size - 1
                value < 0 -> 0
                else -> value
            }
        }

    init
    {
        createAndAddInventory()
    }

    override fun getInventory(): Inventory
    {
        return inventoryPages[selectedInventory].inventory
    }

    //todo fix ConcurrentModificationException
    open fun addItems(itemStacks: MutableCollection<ItemStack>)
    {
        val leftOver = ArrayList<ItemStack>()
        for (inventory in inventoryPages)
        {
            fillInventory(inventory.inventory, itemStacks, leftOver)
        }
        while (itemStacks.isNotEmpty())
        {
            val inventory = createAndAddInventory()
            fillInventory(inventory.inventory, itemStacks, leftOver)
        }
    }

    /**
     * Attempts to fill the inventory with the provided itemStacks
     * All excess items are returned in the provided itemStacks collection
     * @param inventory The inventory to fill
     * @param itemStacks The itemStacks to fill the inventory with, excess items will be returned in this list
     * @param leftOver This will be used to temporary store the excess items, the excess items will be transferred into the itemStacks collection
     * @return nothing as all excess items are stored in the itemStacks collection
     */
    protected fun fillInventory(
        inventory: Inventory,
        itemStacks: MutableCollection<ItemStack>,
        leftOver: MutableCollection<ItemStack> = ArrayList()
    )
    {
        for (itemStack in itemStacks)
        {
            leftOver.addAll(inventory.addItem(itemStack).values)
        }
        itemStacks.clear()
        itemStacks.addAll(leftOver)
        leftOver.clear()
    }

    open fun fillPlayerInventory()
    {
        val leftOver: MutableList<ItemStack> = ArrayList()

        for (i in 0 until inventoryPages[selectedInventory].size)
        {
            val item = inventoryPages[selectedInventory].inventory.getItem(i) ?: continue
            leftOver.addAll(player.inventory.addItem(item).values)
        }
        inventoryPages[selectedInventory].clear()
        cleanUpInventory()

        addItems(leftOver)
    }

    fun createAndAddInventory(): InventoryPage
    {
        val page = createInventory()
        addInventory(page)
        return page
    }

    fun createInventory(): InventoryPage
    {
        return InventoryPage(player, this)
    }

    fun addInventory(inventoryPage: InventoryPage)
    {
        if (maxSize > 0 && inventoryPages.size >= maxSize)
        {
            inventoryPages.add(inventoryPage)
        }
    }

    fun cleanUpInventory(): Boolean
    {
        if (maxSize > 0 && inventoryPages.size > 1 && inventoryPages[selectedInventory].isEmpty())
        {
            inventoryPages.removeAt(selectedInventory)
            selectPreviousInventory()
            return true
        }
        return false
    }

    fun cleanUpInventories()
    {
        selectedInventory = inventoryPages.size - 1
        while (selectedInventory > 0)
        {
            cleanUpInventory()
        }
    }

    fun selectNextInventory()
    {
        selectedInventory++
        if (selectedInventory >= inventoryPages.size)
        {
            selectedInventory = 0
        }
    }

    fun selectPreviousInventory()
    {
        selectedInventory--
        if (selectedInventory < 0)
        {
            selectedInventory = inventoryPages.size - 1
        }
    }

    fun displayNextInventory(delay: Long = 1)
    {
        cleanUpInventory()
        if (selectedInventory == inventoryPages.size - 1 && selectedInventory < maxSize - 1)
        {
            createAndAddInventory()
        }
        selectNextInventory()
        displayInventory(delay)
    }

    fun displayPreviousInventory(delay: Long = 1)
    {
        if (!cleanUpInventory())
        {
            selectPreviousInventory()
        }
        displayInventory(delay)
    }

    fun displayInventory(delay: Long = 1)
    {
        val inventoryPage = inventoryPages[selectedInventory]
        inventoryPage.pageNumber = selectedInventory + 1
        inventoryPage.updatePageIndicator()
        val inventory = inventoryPage.inventory

        when (delay)
        {
            0L -> player.openInventory(inventory)
            1L -> pageListener.main.server.scheduler.runTask(
                pageListener.main
            ) { -> player.openInventory(inventory) }
            else -> pageListener.main.server.scheduler.runTaskLater(
                pageListener.main,
                { -> player.openInventory(inventory) },
                delay
            )
        }
    }

    fun executeListener(event: InventoryClickEvent)
    {
        inventoryPages[selectedInventory].executeListener(event)
    }

}