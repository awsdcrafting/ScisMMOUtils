package eu.scisneromam.mc.scismmoutils.database

/**
 * Project: ScisUtils
 * Initially created by scisneromam on 10.08.2019.
 * @author scisneromam
 * ---------------------------------------------------------------------
 * Copyright © 2019 | scisneromam | All rights reserved.
 */
abstract class DBFunction(val database: DBConnection)
{

    abstract fun save()

}