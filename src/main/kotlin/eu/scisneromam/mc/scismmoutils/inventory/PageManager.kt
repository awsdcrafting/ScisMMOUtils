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
class PageManager(val player: Player, val paginator: Paginator) : InventoryHolder
{

    private val inventories: MutableList<InventoryPage> = ArrayList()
    private var selectedInventory: Int = 0

    init
    {
        createAndAddInventory()
    }

    override fun getInventory(): Inventory
    {
        return inventories[selectedInventory].inventory
    }

    //todo fix ConcurrentModificationException
    fun addItems(itemStacks: MutableCollection<ItemStack>)
    {
        val leftOver = ArrayList<ItemStack>()
        for (inventory in inventories)
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
    private fun fillInventory(
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

    fun fillPlayerInventory()
    {
        val leftOver: MutableList<ItemStack> = ArrayList()

        for (i in 0 until inventories[selectedInventory].size)
        {
            val item = inventories[selectedInventory].inventory.getItem(i) ?: continue
            leftOver.addAll(player.inventory.addItem(item).values)
        }
        inventories[selectedInventory].clear()
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
        inventories.add(inventoryPage)
    }

    fun cleanUpInventory(): Boolean
    {
        if (selectedInventory > 0 && inventories[selectedInventory].isEmpty())
        {
            inventories.removeAt(selectedInventory)
            selectedInventory--
            return true
        }
        return false
    }

    fun cleanUpInventories()
    {
        selectedInventory = inventories.size - 1
        while (selectedInventory > 0)
        {
            cleanUpInventory()
        }
    }

    fun selectNextInventory()
    {
        selectedInventory++
        if (selectedInventory >= inventories.size)
        {
            selectedInventory = 0
        }
    }

    fun selectPreviousInventory()
    {
        selectedInventory--
        if (selectedInventory < 0)
        {
            selectedInventory = inventories.size - 1
        }
    }

    fun displayNextInventory(delay: Long = 1)
    {
        cleanUpInventory()
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
        when (delay)
        {
            0L -> player.openInventory(inventories[selectedInventory].inventory)
            1L -> paginator.main.server.scheduler.runTask(
                paginator.main
            ) { -> player.openInventory(inventories[selectedInventory].inventory) }
            else -> paginator.main.server.scheduler.runTaskLater(
                paginator.main,
                { -> inventories[selectedInventory].inventory },
                delay
            )
        }
    }

    fun executeListener(event: InventoryClickEvent)
    {
        inventories[selectedInventory].executeListener(event)
    }

}