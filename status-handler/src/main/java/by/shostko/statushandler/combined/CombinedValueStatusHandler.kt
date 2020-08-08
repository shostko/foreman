@file:Suppress("unused")

package by.shostko.statushandler.combined

import by.shostko.statushandler.Status
import by.shostko.statushandler.StatusHandler
import by.shostko.statushandler.ValueHandler
import by.shostko.statushandler.ValueStatusHandler

open class CombinedValueStatusHandler<V : Any>(
    private val statusHandler: StatusHandler,
    private val valueHandler: ValueHandler<V>
) : ValueStatusHandler<V> {

    override val status: Status
        get() = statusHandler.status

    override val value: V?
        get() = valueHandler.value

    override fun addOnStatusListener(listener: StatusHandler.OnStatusListener) {
        statusHandler.addOnStatusListener(listener)
    }

    override fun removeOnStatusListener(listener: StatusHandler.OnStatusListener) {
        statusHandler.removeOnStatusListener(listener)
    }

    override fun addOnValueListener(listener: ValueHandler.OnValueListener<V>) {
        valueHandler.addOnValueListener(listener)
    }

    override fun removeOnValueListener(listener: ValueHandler.OnValueListener<V>) {
        valueHandler.removeOnValueListener(listener)
    }
}