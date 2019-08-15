package eu.scisneromam.mc.scismmoutils.inventory

import eu.scisneromam.mc.scismmoutils.main.Main.Companion.MAIN
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryDragEvent
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
    val name: String = "",
    val parent: PageManager? = null,
    var maxSize: Int = -1,
    val sizePerPage: Int = 45,
    val allowStorage: Boolean = true
) :
    InventoryHolder
{

    protected val inventoryPages: MutableList<InventoryPage> = ArrayList()

    val size: Int
        get()
        {
            return inventoryPages.size
        }

    init
    {
        createAndAddInventory()
    }

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


    override fun getInventory(): Inventory
    {
        return inventoryPages[selectedInventory].inventory
    }

    fun getInventory(index: Int): InventoryPage
    {
        val index = when
        {
            index < 0 -> 0
            index >= size -> size - 1
            else -> index
        }
        return inventoryPages[index]
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
        return InventoryPage(player, this, size = sizePerPage, parent = parent, allowStorage = allowStorage)
    }

    fun addInventory(inventoryPage: InventoryPage)
    {
        if (!(maxSize > 0 && inventoryPages.size >= maxSize))
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
        if (selectedInventory >= inventoryPages.size - 1)
        {
            selectedInventory = 0
        } else
        {
            selectedInventory++
        }
    }

    fun selectPreviousInventory()
    {
        if (selectedInventory <= 0)
        {
            selectedInventory = inventoryPages.size - 1
        } else
        {
            selectedInventory--
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

    open fun displayInventory(delay: Long = 1)
    {
        val inventoryPage = inventoryPages[selectedInventory]
        inventoryPage.pageNumber = selectedInventory + 1
        inventoryPage.updatePageIndicator()
        val inventory = inventoryPage.inventory

        when (delay)
        {
            0L -> player.openInventory(inventory)
            1L -> MAIN.server.scheduler.runTask(
                MAIN
            ) { -> player.openInventory(inventory) }
            else -> MAIN.server.scheduler.runTaskLater(
                MAIN,
                { -> player.openInventory(inventory) },
                delay
            )
        }
    }

    fun displayInventory(index: Int, delay: Long = 1)
    {
        selectedInventory = index
        displayInventory(delay)
    }

    fun executeListener(event: InventoryClickEvent)
    {
        inventoryPages[selectedInventory].executeListener(event)
    }

    fun executeListener(event: InventoryDragEvent)
    {
        inventoryPages[selectedInventory].executeListener(event)
    }

}