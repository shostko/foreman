@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package by.shostko.statushandler.v2.paging

import androidx.lifecycle.Lifecycle
import androidx.paging.PagingDataAdapter
import by.shostko.statushandler.v2.StatusHandler
import by.shostko.statushandler.v2.ValueStatusHandler
import by.shostko.statushandler.v2.WrappedStatusHandler

interface PagingStatusHandler : WrappedStatusHandler {
    fun retry()
}

interface PagingValueStatusHandler<V : Any> : PagingStatusHandler, ValueStatusHandler<V> {
    fun attach(adapter: PagingDataAdapter<*, *>, lifecycle: Lifecycle? = null)
    fun detach()
}

fun StatusHandler.Companion.wrapPaging(func: () -> PagingDataAdapter<*, *>): PagingStatusHandler = PagingStatusHandlerImpl(func)
fun StatusHandler.Companion.wrapPaging(adapter: PagingDataAdapter<*, *>): PagingStatusHandler = PagingStatusHandlerImpl { adapter }

fun PagingDataAdapter<*, *>.statusHandler(): PagingStatusHandler = PagingStatusHandlerImpl { this }

fun <V : Any> ValueStatusHandler<V>.withPaging(): PagingValueStatusHandler<V> = CombinedPagingValueStatusHandler(this, this)