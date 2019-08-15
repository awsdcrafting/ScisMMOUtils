package eu.scisneromam.mc.scismmoutils.inventory

import org.bukkit.Material
import org.bukkit.OfflinePlayer
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import org.bukkit.inventory.meta.SkullMeta

/**
 * Project: ScisUtils
 * Initially created by scisneromam on 14.08.2019.
 * @author scisneromam
 * ---------------------------------------------------------------------
 * Copyright © 2019 | scisneromam | All rights reserved.
 */
class ItemCreator(material: Material)
{
    private val itemStack: ItemStack = ItemStack(material)

    fun setDisplayName(name: String): ItemCreator
    {
        val itemMeta = itemStack.itemMeta
        if (itemMeta != null)
        {
            itemMeta.setDisplayName(name)
            itemStack.itemMeta = itemMeta
        }
        return this
    }

    fun setHeadOwner(owner: OfflinePlayer): ItemCreator
    {
        val itemMeta = itemStack.itemMeta
        if ((itemStack.type == Material.PLAYER_HEAD || itemStack.type == Material.PLAYER_WALL_HEAD) && itemMeta is SkullMeta)
        {
            itemMeta.owningPlayer = owner
        }
        return this
    }

    fun setLore(lore: List<String>): ItemCreator
    {
        val itemMeta = itemStack.itemMeta
        if (itemMeta != null)
        {
            itemMeta.lore = lore
        }
        return this
    }

    fun setAmount(amount: Int): ItemCreator
    {
        itemStack.amount = amount
        return this
    }

    fun setDurability(durability: Int): ItemCreator
    {
        val itemMeta = itemStack.itemMeta
        if (itemMeta is Damageable)
        {
            itemMeta.damage = itemStack.type.maxDurability - durability
        }
        return this
    }


    fun create(): ItemStack
    {
        return itemStack
    }

}