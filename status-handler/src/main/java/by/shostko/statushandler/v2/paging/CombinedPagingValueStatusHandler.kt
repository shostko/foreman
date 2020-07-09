package by.shostko.statushandler.v2.paging

import androidx.lifecycle.Lifecycle
import androidx.paging.PagingDataAdapter
import by.shostko.statushandler.v2.ValueHandler
import by.shostko.statushandler.v2.combined.CombinedValueStatusHandler

internal class CombinedPagingValueStatusHandler<V : Any>(
    private val statusHandler: PagingStatusHandler,
    valueHandler: ValueHandler<V>
) : CombinedValueStatusHandler<V>(statusHandler, valueHandler), PagingValueStatusHandler<V> {

    override fun retry() {
        statusHandler.retry()
    }

    override fun refresh() {
        statusHandler.refresh()
    }

    override fun attach(adapter: PagingDataAdapter<*, *>, lifecycle: Lifecycle?) {
    }

    override fun detach() {
    }
}