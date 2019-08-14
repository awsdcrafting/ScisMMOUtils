package eu.scisneromam.mc.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.CommandHelp
import co.aikar.commands.annotation.*
import eu.scisneromam.mc.scisutils.Main
import eu.scisneromam.mc.utils.sendPrefixedMessage
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

/**
 * Project: ScisUtils
 * Initially created by scisneromam on 12.08.2019.
 * @author scisneromam
 * ---------------------------------------------------------------------
 * Copyright © 2019 | scisneromam | All rights reserved.
 */

@CommandAlias("miner")
@CommandPermission("scisUtils.miner.use")
@Description("Miner")
class MinerCommand(val main: Main) : BaseCommand()
{

    @Default
    @Subcommand("toggle")
    fun onToggle(player: Player)
    {
        if (main.dbConnection.breakXpFunction.getXPLevel(player.uniqueId).level < 3)
        {
            player.sendPrefixedMessage("To use miner you have to be break level 3")
            return
        }

        val on = main.blockBreakListener.toggleFunction(player, main.blockBreakListener.miner)
        val msg = if (on)
        {
            "on"
        } else
        {
            "off"
        }
        player.sendPrefixedMessage("Miner is now toggled $msg")
    }

    @Subcommand("help")
    @HelpCommand
    fun onHelp(sender: CommandSender, help: CommandHelp)
    {
        sender.sendMessage("Miner Help")
        help.showHelp()
    }
}