package by.shostko.statushandler.paging

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.paging.PagingDataAdapter
import by.shostko.statushandler.*
import by.shostko.statushandler.combined.CombinedValueStatusHandler
import by.shostko.statushandler.combined.StatusCombinationStrategy
import by.shostko.statushandler.combined.combineWith

internal class CombinedPagingValueStatusHandler<V : Any> private constructor(
    private val statusHandlerWrapper: LazyStatusHandler<PagingStatusHandler>,
    statusHandler: StatusHandler,
    valueHandler: ValueHandler<V>
) : CombinedValueStatusHandler<V>(
    statusHandler = statusHandler.map { it.asRefreshingStatusWrapper() }.combineWith(statusHandlerWrapper, CombinationStrategy),
    valueHandler = valueHandler
), PagingValueStatusHandler<V> {

    constructor(statusHandler: StatusHandler, valueHandler: ValueHandler<V>) : this(LazyStatusHandler(), statusHandler, valueHandler)

    private var lifecycleObserver: InternalLifecycleObserver? = null

    override fun retry() {
        statusHandlerWrapper.wrapped?.retry() ?: throw IllegalStateException("Attach this status handler to adapter before call 'retry'")
    }

    override fun refresh() {
        statusHandlerWrapper.wrapped?.refresh() ?: throw IllegalStateException("Attach this status handler to adapter before call 'refresh'")
    }

    override fun attach(adapter: PagingDataAdapter<*, *>, lifecycle: Lifecycle?) {
        if (statusHandlerWrapper.wrapped != null) {
            throw IllegalStateException("Detach previous adapter before attaching new one")
        }
        statusHandlerWrapper.wrapped = adapter.statusHandler()
        lifecycle?.let { it.addObserver(InternalLifecycleObserver(it).apply { lifecycleObserver = this }) }
    }

    override fun detach() {
        statusHandlerWrapper.wrapped = null
        lifecycleObserver?.let { it.lifecycle.removeObserver(it) }
    }

    private inner class InternalLifecycleObserver(
        val lifecycle: Lifecycle
    ) : LifecycleObserver {
        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        fun onDestroy() {
            lifecycle.removeObserver(this)
            detach()
        }
    }

    private object CombinationStrategy : StatusCombinationStrategy {
        override fun invoke(s1: Status, s2: Status): Status = when {
            !s1.isSuccess -> s1
            s2.isInitial -> Status.Working(Status.WORKING).asRefreshingStatusWrapper()
            else -> s2
        }
    }
}