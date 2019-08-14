package eu.scisneromam.mc.scismmoutils.database

import eu.scisneromam.mc.scismmoutils.main.Main
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.TransactionManager
import java.io.File

/**
 * Project: ScisUtils
 * Initially created by scisneromam on 06.08.2019.
 *
 * @author scisneromam
 * ---------------------------------------------------------------------
 * Copyright © 2019 | scisneromam | All rights reserved.
 */
class DBConnection(val main: Main)
{

    val breakXpFunction: BreakXPFunction = BreakXPFunction(this)
    val db by lazy {
        main.dataFolder.mkdirs()
        Database.connect("jdbc:sqlite:${File(main.dataFolder, "data.db").absolutePath}", driver = "org.sqlite.JDBC")
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
        main.registerListener(breakXpFunction)
    }

    fun setupDB()
    {
        transaction {
            SchemaUtils.createMissingTablesAndColumns(BreakXPLevelTable)
        }
    }

}
