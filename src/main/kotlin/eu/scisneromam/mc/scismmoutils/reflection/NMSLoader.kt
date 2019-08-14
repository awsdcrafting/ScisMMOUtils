package eu.scisneromam.mc.scismmoutils.reflection

import java.lang.reflect.Constructor
import java.lang.reflect.Field
import java.lang.reflect.Method

/**
 * Project: ScisUtils
 * Initially created by scisneromam on 14.08.2019.
 * @author scisneromam
 * ---------------------------------------------------------------------
 * Copyright © 2019 | scisneromam | All rights reserved.
 */
class NMSLoader(version: String)
{
    val nmsPackage = "net.minecraft.server.$version"
    val cbPackage = "org.bukkit.craftbukkit.$version"

    fun getNMSClass(className: String): Class<*>?
    {
        return getClass(nmsPackage, className)
    }

    fun getCBClass(className: String): Class<*>?
    {
        return getClass(cbPackage, className)
    }
    //todo exception handling

    fun getClass(classPackage: String, className: String): Class<*>?
    {
        try
        {
            return Class.forName("$classPackage.$className")
        } catch (e: ClassNotFoundException)
        {

        } catch (e: LinkageError)
        {

        } catch (e: ExceptionInInitializerError)
        {

        }
        return null
    }

    fun getMethod(clazz: Class<*>, name: String, vararg paramTypes: Class<*>): Method?
    {
        try
        {
            return clazz.getMethod(name, *paramTypes)
        } catch (e: NoSuchMethodException)
        {

        } catch (e: SecurityException)
        {

        }
        return null
    }

    fun getField(clazz: Class<*>, name: String): Field?
    {
        try
        {
            val field = clazz.getDeclaredField(name)
            field.isAccessible = true
            return field
        } catch (e: NoSuchFieldException)
        {

        } catch (e: SecurityException)
        {

        }
        return null
    }

    fun getConstructor(clazz: Class<*>, vararg paramTypes: Class<*>): Constructor<*>?
    {
        try
        {
            clazz.getConstructor(*paramTypes)
        } catch (e: NoSuchMethodException)
        {

        } catch (e: SecurityException)
        {

        }
        return null
    }


}