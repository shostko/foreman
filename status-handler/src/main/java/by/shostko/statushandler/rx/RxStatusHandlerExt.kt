@file:Suppress("unused")

package by.shostko.statushandler.rx

import by.shostko.statushandler.Status
import by.shostko.statushandler.StatusHandler
import by.shostko.statushandler.ValueHandler

val StatusHandler.statusFlowable: InitialValueFlowable<Status>
    get() = StatusHandlerFlowable(this)

val <V : Any> ValueHandler<V>.valueFlowable: InitialValueFlowable<V>
    get() = if (this is BaseFlowableStatusHandler<*, V>) {
        RxValueHandlerFlowable(this)
    } else {
        ValueHandlerFlowable(this)
    }

private class StatusHandlerFlowable(
    private val statusHandler: StatusHandler
) : InitialValueFlowable.WithListener<Status>() {

    override val initialValue: Status
        get() = statusHandler.status

    override fun createListener(onNextValue: (Status) -> Unit) = Listener(statusHandler, onNextValue)

    private class Listener(
        private val statusHandler: StatusHandler,
        private val onNextValue: (Status) -> Unit
    ) : WithListener.Listener, StatusHandler.OnStatusListener {

        override fun onStatus(status: Status) {
            onNextValue(status)
        }

        override fun addListener() {
            statusHandler.addOnStatusListener(this)
        }

        override fun removeListener() {
            statusHandler.removeOnStatusListener(this)
        }
    }
}

private class RxValueHandlerFlowable<V : Any>(
    private val valueHandler: BaseFlowableStatusHandler<*, V>
) : InitialValueFlowable.WithSource<V>(valueHandler.valueFlowable) {
    override val initialValue: V?
        get() = valueHandler.value
}

private class ValueHandlerFlowable<V : Any>(
    private val valueHandler: ValueHandler<V>
) : InitialValueFlowable.WithListener<V>() {

    override val initialValue: V?
        get() = valueHandler.value

    override fun createListener(onNextValue: (V) -> Unit) = Listener(valueHandler, onNextValue)

    private class Listener<V : Any>(
        private val valueHandler: ValueHandler<V>,
        private val onNextValue: (V) -> Unit
    ) : WithListener.Listener, ValueHandler.OnValueListener<V> {

        override fun onValue(value: V) {
            onNextValue(value)
        }

        override fun addListener() {
            valueHandler.addOnValueListener(this)
        }

        override fun removeListener() {
            valueHandler.removeOnValueListener(this)
        }
    }
}