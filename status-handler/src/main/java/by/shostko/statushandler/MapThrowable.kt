@file:Suppress("unused", "CanBeParameter", "MemberVisibilityCanBePrivate")

package by.shostko.statushandler

class MapThrowable(val map: Map<String, Any>) : Throwable(map.entries.joinToString()) {
    override fun toString(): String = "MapThrowable($message)"
}