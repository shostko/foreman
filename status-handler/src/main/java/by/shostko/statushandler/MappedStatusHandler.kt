package by.shostko.statushandler

import by.shostko.statushandler.combined.CombinedValueStatusHandler

private class MappedStatusHandler(
    private val statusHandler: StatusHandler,
    private val mapper: (Status) -> Status
) : AbsStatusHandler(), StatusHandler.OnStatusListener {

    override val status: Status
        get() = mapper(statusHandler.status)

    override fun onStatus(status: Status) {
        notifyListeners(mapper(status))
    }

    override fun onFirstListenerAdded() {
        statusHandler.addOnStatusListener(this)
    }

    override fun onLastListenerRemoved() {
        statusHandler.removeOnStatusListener(this)
    }
}

fun StatusHandler.map(mapper: (Status) -> Status): StatusHandler = MappedStatusHandler(this, mapper)

private class MappedValueHandler<V1 : Any, V2 : Any>(
    private val valueHandler: ValueHandler<V1>,
    private val mapper: (V1) -> V2
) : AbsValueHandler<V2>(), ValueHandler.OnValueListener<V1> {

    override val value: V2?
        get() = valueHandler.value?.let(mapper)

    override fun onFirstListenerAdded() {
        valueHandler.addOnValueListener(this)
    }

    override fun onLastListenerRemoved() {
        valueHandler.removeOnValueListener(this)
    }

    override fun onValue(value: V1) {
        notifyListeners(mapper(value))
    }
}

fun <V1 : Any, V2 : Any> ValueHandler<V1>.map(mapper: (V1) -> V2): ValueHandler<V2> = MappedValueHandler(this, mapper)

fun <V1 : Any, V2 : Any> ValueStatusHandler<V1>.mapValue(mapper: (V1) -> V2): ValueStatusHandler<V2> = CombinedValueStatusHandler(this, MappedValueHandler(this, mapper))