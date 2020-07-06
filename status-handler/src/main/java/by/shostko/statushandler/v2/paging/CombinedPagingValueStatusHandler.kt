package by.shostko.statushandler.v2.paging

import androidx.paging.PagingData
import by.shostko.statushandler.v2.ValueHandler
import by.shostko.statushandler.v2.combined.CombinedValueStatusHandler

internal class CombinedPagingValueStatusHandler<T : Any>(
    private val statusHandler: PagingStatusHandler,
    valueHandler: ValueHandler<PagingData<T>>
) : CombinedValueStatusHandler<PagingData<T>>(statusHandler, valueHandler), PagingValueStatusHandler<T> {

    override fun retry() {
        statusHandler.retry()
    }

    override fun refresh() {
        statusHandler.refresh()
    }
}