package eu.scisneromam.mc.scismmoutils.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.CommandHelp
import co.aikar.commands.annotation.*
import eu.scisneromam.mc.scismmoutils.functions.Hammer
import eu.scisneromam.mc.scismmoutils.main.Main
import eu.scisneromam.mc.scismmoutils.utils.sendPrefixedMessage
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.lang.Integer.min

/**
 * Project: ScisUtils
 * Initially created by scisneromam on 12.08.2019.
 * @author scisneromam
 * ---------------------------------------------------------------------
 * Copyright © 2019 | scisneromam | All rights reserved.
 */

@CommandAlias("hammer")
@CommandPermission("scisUtils.hammer.use")
@Description("Hammer")
class HammerCommand(val main: Main) : BaseCommand()
{

    @Default
    @Subcommand("toggle")
    fun onToggle(player: Player)
    {
        val on = main.blockBreakListener.toggleFunction(player, main.blockBreakListener.hammer)
        val msg = if (on)
        {
            "on"
        } else
        {
            "off"
        }
        player.sendPrefixedMessage("Hammer is now toggled $msg")
    }

    @Subcommand("help")
    @HelpCommand
    fun onHelp(sender: CommandSender, help: CommandHelp)
    {
        help.showHelp()
    }

    @Subcommand("mode")
    @Syntax("individually | square | cube")
    @Description("Sets the mode of the hammer")
    @CommandCompletion("individually|square|cube")
    fun onMode(player: Player, @Optional arg: String?)
    {
        val mode = main.blockBreakListener.hammer.modePerPlayer.getOrPut(player, { Hammer.Mode() })

        if (arg == null)
        {
            player.sendPrefixedMessage("Your current mode is ${mode.mode}")
        } else
        {
            when (arg.toLowerCase())
            {
                "individually" ->
                {
                    mode.mode = Hammer.HammerMode.INDIVIDUALLY
                }
                "square" ->
                {
                    mode.mode = Hammer.HammerMode.SQUARE
                }
                "cube" ->
                {
                    mode.mode = Hammer.HammerMode.CUBE
                }

                else ->
                {
                    player.sendPrefixedMessage("You provided an invalid mode")
                    return
                }
            }
            player.sendPrefixedMessage("Set mode to $arg")
        }
    }

    @Subcommand("radius")
    @Syntax("[lrRad] [udRad] [depth]")
    @Description("Sets the radius of the hammer")
    fun onRadius(player: Player, @Optional lrRad: Int?, @Optional udRad: Int?, @Optional depth: Int?)
    {
        val mode = main.blockBreakListener.hammer.modePerPlayer.getOrPut(player, { Hammer.Mode() })
        val level = main.dbConnection.breakXpFunction.getXPLevel(player.uniqueId).level
        if (lrRad == null && udRad == null && depth == null)
        {
        } else
        {
            if (lrRad != null && lrRad >= 0)
            {
                mode.leftRightSize = min(lrRad, level * 2)
            }
            if (udRad != null && udRad >= 0)
            {
                mode.upDownSize = min(udRad, level * 2)
            }
            if (depth != null && depth >= 0)
            {
                mode.depth = min(depth, level * 4)
            }
        }
        player.sendPrefixedMessage("Your current radii are ${mode.leftRightSize} ${mode.upDownSize} ${mode.depth}")
    }

    @Subcommand("items")
    @Syntax("info | give | show | list")
    @Description("Fills your inventory with your mined items")
    @CommandCompletion("info|give|show|list")
    fun onItems(player: Player, @Optional arg: String?)
    {
        val itemSave = main.paginator
        if (arg == null)
        {
            player.sendPrefixedMessage("//todo")
            //player.sendPrefixedMessage("You have ${itemSave.itemAmount} items over ${itemSave.itemStackAmount} ItemStacks")
        } else
        {
            when (arg.toLowerCase())
            {
                "give" -> itemSave.fillInventory(player)
                "show" -> itemSave.openInventory(player)
                "list" ->
                {
                    player.sendPrefixedMessage("//todo")
                }
                else -> player.sendPrefixedMessage("//todo")

                //else -> player.sendPrefixedMessage("You have ${itemSave.itemAmount} items over ${itemSave.itemStackAmount} ItemStacks")
            }
        }
    }
}