@file:Suppress("unused")

package by.shostko.statushandler.paging

import androidx.paging.DataSource
import by.shostko.statushandler.*

interface PagingValueStatusHandler<V : Any> : WrappedValueStatusHandler<V> {
    fun retry()
}

interface PagingController {
    fun refresh()
    fun retry()
}

interface PagingDataSource {
    abstract class Factory<K, V>(
        private val statusHandlerCallback: StatusHandler.Callback
    ) : DataSource.Factory<K, V>(), PagingController {

        private var lastDataSource: PagingController? = null

        final override fun refresh() {
            lastDataSource?.refresh()
        }

        final override fun retry() {
            lastDataSource?.retry()
        }

        final override fun create(): DataSource<K, V> =
            create(statusHandlerCallback).also { newDataSource ->
                check(newDataSource is PagingController) {
                    "PagingDataSource.Factory should create DataSource that implements PagingController! Created: $newDataSource"
                }
                lastDataSource = newDataSource
                newDataSource.addInvalidatedCallback {
                    if (lastDataSource === newDataSource) {
                        lastDataSource = null
                    }
                }
            }

        protected abstract fun create(statusHandlerCallback: StatusHandler.Callback): DataSource<K, V>
    }
}

fun <V : PagingController> ValueStatusHandler<V>.withPaging(): PagingValueStatusHandler<V> = PagingValueStatusHandlerImpl(this, this, this)

fun <V1 : PagingDataSource.Factory<*, *>, V2 : Any> ValueStatusHandler<V1>.mapWithPaging(mapper: (V1) -> V2): PagingValueStatusHandler<V2> =
    PagingValueStatusHandlerImpl(this, mapValue(mapper), this)

private class PagingValueStatusHandlerImpl<C : PagingController, V : Any>(
    private val statusHandler: StatusHandler,
    private val valueHandler: ValueHandler<V>,
    private val pagingController: ValueHandler<C>
) : PagingValueStatusHandler<V> {

    override val status: Status
        get() = statusHandler.status

    override val value: V?
        get() = valueHandler.value

    override fun addOnStatusListener(listener: StatusHandler.OnStatusListener) = statusHandler.addOnStatusListener(listener)

    override fun removeOnStatusListener(listener: StatusHandler.OnStatusListener) = statusHandler.removeOnStatusListener(listener)

    override fun addOnValueListener(listener: ValueHandler.OnValueListener<V>) = valueHandler.addOnValueListener(listener)

    override fun removeOnValueListener(listener: ValueHandler.OnValueListener<V>) = valueHandler.removeOnValueListener(listener)

    override fun retry() {
        pagingController.value?.retry()
    }

    override fun refresh() {
        pagingController.value?.refresh()
    }
}