package eu.scisneromam.mc.utils;

import eu.scisneromam.mc.scisutils.Main;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.BlockIterator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Project: HammerSpigotPlugin
 * Initially created by scisneromam on 11.01.2019.
 *
 * @author scisneromam
 * ---------------------------------------------------------------------
 * Copyright © 2019 | scisneromam | All rights reserved.
 */
public class MinecraftUtils
{

    public static final boolean DEBUG = false;

    public static List<Block> getBlocksInLine(Player player, int range)
    {
        List<Block> blockList = new ArrayList<>();
        BlockIterator iter = new BlockIterator(player, range);
        Block lastBlock;
        while (iter.hasNext())
        {
            lastBlock = iter.next();
            blockList.add(lastBlock);
            if (lastBlock.getType().isSolid())
            {
                break;
            }
        }
        return blockList;
    }

    public static void breakBlock(Main main, Block block, Player player, ItemStack tool, String debugName, boolean debug)
    {
        Collection<ItemStack> drops = block.getDrops(tool);
        if (drops.isEmpty() || block.getBlockData().getMaterial() == Material.AIR || drops.stream().allMatch(it -> it.getType() == Material.AIR))
        {
            debug("Not breaking " + block + " because we would not get anything", debugName, debug);
            return;
        }

        Location bLoc = block.getLocation();
        Location pLoc = player.getLocation();
        double dy = bLoc.getY() - pLoc.getY();
        if (Math.abs(bLoc.getX() - pLoc.getX()) < 1.0 && Math.abs(bLoc.getZ() - pLoc.getZ()) < 1.0 && dy <= 0 && dy >= -1.1)
        {
            if (!player.isFlying() && !player.getAllowFlight())
            {
                debug("Not breaking " + block + " because it is directly under the player", debugName, debug);

                return;
            }
        }

        ItemMeta meta = tool.getItemMeta();
        if (meta instanceof Damageable)
        {
            int maxDurability = tool.getType().getMaxDurability() - 1;
            if (((Damageable) meta).getDamage() >= maxDurability)
            {
                if (player.hasPermission("scisUtils.cheat.keepTool"))
                {
                    ((Damageable) meta).setDamage(Math.max(((Damageable) meta).getDamage() - 1, 0));
                    tool.setItemMeta(meta);
                } else
                {
                    debug("Not breaking " + block + "because the tool would break too", debugName, debug);
                    return;
                }
            }
        }


        debug("Breaking " + block, debugName, debug);
        main.getNmsAbstract().breakBlock(player, block);
        debug("Broke " + block, debugName, debug);
    }

    public static void debug(String message, String debugName)
    {
        if (DEBUG)
        {
            System.out.println("[ScisUtils DEBUG] [" + debugName + "] " + message);
        }
    }

    public static void debug(String message, String debugName, boolean debug)
    {
        if (debug)
        {
            System.out.println("[ScisUtils DEBUG] [" + debugName + "] " + message);
        }
    }

}
