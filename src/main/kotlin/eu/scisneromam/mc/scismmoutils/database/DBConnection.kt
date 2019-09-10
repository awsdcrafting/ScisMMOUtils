package eu.scisneromam.mc.scismmoutils.database

import eu.scisneromam.mc.scismmoutils.main.Main.Companion.MAIN
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.TransactionManager
import java.io.File
import java.sql.Connection

/**
 * Project: ScisUtils
 * Initially created by scisneromam on 06.08.2019.
 *
 * @author scisneromam
 * ---------------------------------------------------------------------
 * Copyright © 2019 | scisneromam | All rights reserved.
 */
class DBConnection(val mode: String = "sqlite")
{

    val breakXpFunction: BreakXPFunction = BreakXPFunction(this)
    val db by lazy {
        when (mode)
        {
            "mysql" -> mysqlConnection()
            else -> sqliteConnection()
        }
    }

    private fun sqliteConnection(): Database
    {
        MAIN.dataFolder.mkdirs()
        val db =
            Database.connect("jdbc:sqlite:${File(MAIN.dataFolder, "data.db").absolutePath}", driver = "org.sqlite.JDBC")
        TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE
        return db
    }

    private fun mysqlConnection(): Database
    {
        TODO()
    }

    fun <T> transaction(statement: Transaction.() -> T): T
    {
        db
        return org.jetbrains.exposed.sql.transactions.transaction(
            TransactionManager.manager.defaultIsolationLevel,
            TransactionManager.manager.defaultRepetitionAttempts,
            db,
            statement
        )
    }

    fun saveAll()
    {
        breakXpFunction.save()


        transaction {
            flushCache()
        }
    }

    fun addListeners()
    {
        MAIN.registerListener(breakXpFunction)
    }

    fun setupDB()
    {
        transaction {
            SchemaUtils.createMissingTablesAndColumns(BreakXPLevelTable)
        }
    }

}
