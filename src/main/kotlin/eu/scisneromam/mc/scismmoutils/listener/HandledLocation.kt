package eu.scisneromam.mc.scismmoutils.listener

import org.bukkit.Location

class HandledLocation(private val location: Location) :
    Location(location.world, location.x, location.y, location.z, location.yaw, location.pitch)
{
    var handledBreak: Boolean = false
    var handledDrop: Boolean = false

    fun isCompletelyHandled(): Boolean = handledBreak && handledDrop

    /**
     * We have to overwrite equals because we only want to know if the locations are equal
     */
    override fun equals(other: Any?): Boolean
    {
        return other == location
    }


    override fun hashCode(): Int
    {
        return location.hashCode()
    }

}