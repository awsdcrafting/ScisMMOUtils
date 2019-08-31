package eu.scisneromam.mc.scismmoutils.inventory

import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import java.util.*
import java.util.function.Consumer

/**
 * Project: ScisUtils
 * Initially created by scisneromam on 15.08.2019.
 * @author scisneromam
 * ---------------------------------------------------------------------
 * Copyright © 2019 | scisneromam | All rights reserved.
 */
class TypeSortedPageManager(
    player: Player,
    name: String = "",
    parent: PageManager? = null,
    maxSize: Int = -1,
    sizePerPage: Int = 45,
    allowStorage: Boolean = true
) : PageManager(player, "$name Types", parent, -1, 45, false)
{

    val subMaxSize = maxSize
    val subSizePerPage = sizePerPage
    val subAllowStorage = allowStorage
    val subName = name

    fun createPageManager(): PageManager =
        PageSortedPageManager(player, subName, this, subMaxSize, subSizePerPage, subAllowStorage)

    var lastSlot: Pair<Int, Int> = Pair(0, -1)

    val pageMap: MutableMap<Material, PageManager> = EnumMap(org.bukkit.Material::class.java)

    init
    {

    }

    override fun addItems(itemStacks: Collection<ItemStack>)
    {
        val map = itemStacks.groupBy { it.type }
        for ((type, list) in map)
        {
            if (!pageMap.containsKey(type))
            {
                addButton(createButton(type))
            }

            pageMap.getOrPut(type, { createPageManager() }).addItems(ArrayList(list))
        }
    }

    override fun fillPlayerInventory()
    {
        for (pageManager in pageMap.values)
        {
            pageManager.fillPlayerInventory()
        }
    }

    fun createButton(type: Material): GuiButton
    {
        return GuiButton.create(
            ItemCreator(type).create(),
            createConsumer(type)
        )
    }

    fun createConsumer(type: Material): Consumer<InventoryClickEvent>
    {
        return Consumer {
            it.isCancelled = true
            pageMap.getOrPut(type, { createPageManager() }).displayInventory()
        }
    }

    override fun displayInventory(delay: Long)
    {
        //todo clear empty

        super.displayInventory(delay)
    }

    override fun isEmpty(): Boolean
    {
        for (value in pageMap.values)
        {
            if (!value.isEmpty())
            {
                return false
            }
        }
        return true
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