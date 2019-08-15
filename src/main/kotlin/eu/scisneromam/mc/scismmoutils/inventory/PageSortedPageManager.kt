package eu.scisneromam.mc.scismmoutils.inventory

import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/**
 * Project: ScisUtils
 * Initially created by scisneromam on 15.08.2019.
 * @author scisneromam
 * ---------------------------------------------------------------------
 * Copyright © 2019 | scisneromam | All rights reserved.
 */
class PageSortedPageManager(
    player: Player,
    pageListener: PageListener,
    name: String = "",
    maxSize: Int = -1
) : PageManager(player, pageListener, name, -1)
{
    val pageManager: PageManager = PageManager(player, pageListener, name, maxSize)

    override fun addItems(itemStacks: MutableCollection<ItemStack>)
    {
        pageManager.addItems(itemStacks)
    }

    override fun fillPlayerInventory()
    {
        pageManager.fillPlayerInventory()
    }
}