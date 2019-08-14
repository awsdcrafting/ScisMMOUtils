package eu.scisneromam.mc.scismmoutils.reflection

import org.bukkit.block.Block
import org.bukkit.entity.Player
import java.lang.reflect.Constructor
import java.lang.reflect.Field
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

/**
 * Project: ScisUtils
 * Initially created by scisneromam on 14.08.2019.
 * @author scisneromam
 * ---------------------------------------------------------------------
 * Copyright © 2019 | scisneromam | All rights reserved.
 */
class NMSBlockBreak(val nmsLoader: NMSLoader)
{

    private lateinit var entityPlayerClass: Class<*>
    private lateinit var playerInteractManagerClass: Class<*>
    private lateinit var playerInteractManagerField: Field
    private lateinit var blockPositionClass: Class<*>
    private lateinit var blockPositionConstructor: Constructor<*>
    private lateinit var craftPlayerClass: Class<*>
    private lateinit var getHandleMethod: Method
    private lateinit var breakBlockMethod: Method

    private val wasSuccessful = loadNMS()

    private fun loadNMS(): Boolean
    {
        entityPlayerClass = nmsLoader.getNMSClass("EntityPlayer") ?: return false
        playerInteractManagerClass = nmsLoader.getNMSClass("PlayerInteractManager") ?: return false
        playerInteractManagerField = nmsLoader.getField(entityPlayerClass, "playerInteractManager") ?: return false
        blockPositionClass = nmsLoader.getNMSClass("BlockPosition") ?: return false
        blockPositionConstructor =
            nmsLoader.getConstructor(blockPositionClass, Integer::class.java, Integer::class.java, Integer::class.java)
                ?: return false
        craftPlayerClass = nmsLoader.getCBClass("entity.CraftPlayer") ?: return false
        getHandleMethod = nmsLoader.getMethod(craftPlayerClass, "getHandle") ?: return false
        breakBlockMethod =
            nmsLoader.getMethod(playerInteractManagerClass, "breakBlock", blockPositionClass) ?: return false
        return true
    }


    fun breakBlock(player: Player, block: Block)
    {
        if (!wasSuccessful)
        {
            block.breakNaturally(player.inventory.itemInMainHand)
            return
        }

        try
        {
            val craftPlayer = getHandleMethod.invoke(player)
            val playerInteractManager = playerInteractManagerField.get(craftPlayer)
            val blockPosition = blockPositionConstructor.newInstance(block.x, block.y, block.z)
            breakBlockMethod.invoke(playerInteractManager, blockPosition)
        } catch (e: IllegalAccessException)
        {

        } catch (e: IllegalArgumentException)
        {

        } catch (e: InvocationTargetException)
        {

        } catch (e: InstantiationException)
        {

        }
    }

}

