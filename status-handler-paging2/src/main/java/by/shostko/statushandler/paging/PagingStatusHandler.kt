@file:Suppress("unused")

package by.shostko.statushandler.paging

import by.shostko.statushandler.*

interface PagingValueStatusHandler<V : Any> : WrappedValueStatusHandler<V> {
    fun retry()
}

interface PagingDataSource {
    fun refresh()
    fun retry()
}

fun <V : PagingDataSource> ValueStatusHandler<V>.withPaging(): PagingValueStatusHandler<V> = PagingValueStatusHandlerImpl(this)

private class PagingValueStatusHandlerImpl<V : PagingDataSource>(
    private val wrapped: ValueStatusHandler<V>
) : PagingValueStatusHandler<V> {

    override val status: Status
        get() = wrapped.status

    override val value: V?
        get() = wrapped.value

    override fun addOnStatusListener(listener: StatusHandler.OnStatusListener) = wrapped.addOnStatusListener(listener)

    override fun removeOnStatusListener(listener: StatusHandler.OnStatusListener) = wrapped.removeOnStatusListener(listener)

    override fun addOnValueListener(listener: ValueHandler.OnValueListener<V>) = wrapped.addOnValueListener(listener)

    override fun removeOnValueListener(listener: ValueHandler.OnValueListener<V>) = wrapped.removeOnValueListener(listener)

    override fun retry() {
        wrapped.value?.refresh()
    }

    override fun refresh() {
        wrapped.value?.retry()
    }
}