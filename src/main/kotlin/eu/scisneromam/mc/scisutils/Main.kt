package eu.scisneromam.mc.scisutils

import co.aikar.commands.PaperCommandManager
import eu.scisneromam.mc.commands.HammerCommand
import eu.scisneromam.mc.commands.MinerCommand
import eu.scisneromam.mc.database.BreakXPLevelEntity
import eu.scisneromam.mc.database.DBConnection
import eu.scisneromam.mc.inventory.Paginator
import eu.scisneromam.mc.listener.BlockBreakListener
import eu.scisneromam.mc.utils.MinecraftUtils
import eu.scisneromam.mc.utils.NMSAbstract
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
    }

    //nms
    lateinit var nmsAbstract: NMSAbstract
    //db
    lateinit var dbConnection: DBConnection
    //listener
    lateinit var blockBreakListener: BlockBreakListener
    //CommandManager
    lateinit var commandManager: PaperCommandManager
    //ItemSave
    lateinit var paginator: Paginator

    private val testOnly = false


    override fun onEnable()
    {
        logger.info("loading")

        if (testOnly)
            return

        nmsAbstract =
            NMSAbstract(Bukkit.getServer().javaClass.getPackage().name.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[3])
        dbConnection = DBConnection(this)


        //addListeners
        dbConnection.addListeners()
        blockBreakListener = BlockBreakListener(this)
        registerListener(blockBreakListener)

        paginator = Paginator(this)
        registerListener(paginator)

        dbConnection.setupDB()

        commandManager = PaperCommandManager(this)
        commandManager.enableUnstableAPI("help")

        commandManager.registerCommand(HammerCommand(this))
        commandManager.registerCommand(MinerCommand(this))

        logger.info("loaded")
    }

    override fun onDisable()
    {
        dbConnection.saveAll()

        MinecraftUtils.debug(dbConnection.transaction {
            BreakXPLevelEntity.all().map { it.toXPLevel().toString() }.joinToString { "\n" }
        }, "Main")
    }

    fun registerListener(listener: Listener)
    {
        server.pluginManager.registerEvents(listener, this)
    }


}