package eu.scisneromam.mc.scismmoutils.utils

/**
 * Project: ScisUtils
 * Initially created by scisneromam on 10.09.2019.
 * @author scisneromam
 * ---------------------------------------------------------------------
 * Copyright © 2019 | scisneromam | All rights reserved.
 * @param maxSize The maximum size of the cache
 */
class CacheMap<K, V>(
    protected val maxSize: Int,
    initialSize: Int = 16,
    loadFactor: Float = 0.75F,
    accessOrder: Boolean = true
) : LinkedHashMap<K, V>(initialSize, loadFactor, accessOrder)
{
    override fun removeEldestEntry(eldest: MutableMap.MutableEntry<K, V>?): Boolean
    {
        if (maxSize <= 0)
        {
            return false
        }
        return size > maxSize
    }

}