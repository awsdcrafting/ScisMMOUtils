package eu.scisneromam.mc.scismmoutils.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.CommandHelp
import co.aikar.commands.annotation.*
import eu.scisneromam.mc.scismmoutils.main.Main.Companion.MAIN
import eu.scisneromam.mc.scismmoutils.utils.sendPrefixedMessage
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
class MinerCommand() : BaseCommand()
{

    @Default
    @Subcommand("toggle")
    fun onToggle(player: Player)
    {
        if (MAIN.dbConnection.breakXpFunction.getXPLevel(player.uniqueId).level < 3)
        {
            player.sendPrefixedMessage("To use miner you have to be break level 3")
            return
        }

        val on = MAIN.blockBreakListener.toggleFunction(player, MAIN.blockBreakListener.miner)
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