package eu.scisneromam.mc.scismmoutils.main

import co.aikar.commands.PaperCommandManager
import eu.scisneromam.mc.scismmoutils.commands.HammerCommand
import eu.scisneromam.mc.scismmoutils.commands.MinerCommand
import eu.scisneromam.mc.scismmoutils.database.BreakXPLevelEntity
import eu.scisneromam.mc.scismmoutils.database.DBConnection
import eu.scisneromam.mc.scismmoutils.inventory.PageListener
import eu.scisneromam.mc.scismmoutils.listener.BlockBreakListener
import eu.scisneromam.mc.scismmoutils.reflection.NMSBlockBreak
import eu.scisneromam.mc.scismmoutils.reflection.NMSLoader
import eu.scisneromam.mc.scismmoutils.utils.MCUtils
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin

/**
 * Project: ScisUtils
 * Initially created by scisneromam on 30.07.2019.
 * @author scisneromam
 * ---------------------------------------------------------------------
 * Copyright © 2019 | scisneromam | All rights reserved.
 */
class Main : JavaPlugin()
{
    companion object
    {
        private val r = ChatColor.RESET.toString()
        private val go = ChatColor.GOLD.toString()
        val PREFIX: String = "$r[${go}ScisUtils$r] "
        lateinit var MAIN: Main
    }

    //nms
    lateinit var nmsLoader: NMSLoader
    lateinit var nmsBlockBreak: NMSBlockBreak

    //db
    lateinit var dbConnection: DBConnection
    //listener
    lateinit var blockBreakListener: BlockBreakListener
    //CommandManager
    lateinit var commandManager: PaperCommandManager
    //ItemSave
    lateinit var pageListener: PageListener


    private val testOnly = false


    override fun onEnable()
    {
        logger.info("loading")

        MAIN = this

        if (testOnly)
            return

        nmsLoader =
            NMSLoader(Bukkit.getServer().javaClass.getPackage().name.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[3])
        nmsBlockBreak = NMSBlockBreak(nmsLoader)

        dbConnection = DBConnection()


        //addListeners
        dbConnection.addListeners()
        blockBreakListener = BlockBreakListener()
        registerListener(blockBreakListener)

        pageListener = PageListener()
        registerListener(pageListener)

        dbConnection.setupDB()

        commandManager = PaperCommandManager(this)
        commandManager.enableUnstableAPI("help")

        commandManager.registerCommand(HammerCommand())
        commandManager.registerCommand(MinerCommand())

        logger.info("loaded")
    }

    override fun onDisable()
    {
        dbConnection.saveAll()

        MCUtils.debug(dbConnection.transaction {
            BreakXPLevelEntity.all().map { it.toXPLevel().toString() }.joinToString { "\n" }
        }, "Main")
    }

    fun registerListener(listener: Listener)
    {
        server.pluginManager.registerEvents(listener, this)
    }


}