package by.shostko.statushandler.v2.paging

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.paging.CombinedLoadStates
import androidx.paging.PagingDataAdapter
import by.shostko.statushandler.v2.BaseStatusHandler
import by.shostko.statushandler.v2.ValueHandler

class PagingValueStatusHandlerImpl<V : Any>(
    private val valueHandler: ValueHandler<V>
) : BaseStatusHandler(), PagingValueStatusHandler<V> {

    override val value: V?
        get() = valueHandler.value

    private var adapter: PagingDataAdapter<*, *>? = null

    private val adapterListener: (CombinedLoadStates) -> Unit by lazy {
        object : (CombinedLoadStates) -> Unit {
            override fun invoke(states: CombinedLoadStates) {
                status(PagingStatus(states))
            }
        }
    }

    private var lifecycleObserver: InternalLifecycleObserver? = null

    override fun onFirstListenerAdded() {
        adapter?.addLoadStateListener(adapterListener)
    }

    override fun onLastListenerRemoved() {
        adapter?.removeLoadStateListener(adapterListener)
    }

    override fun retry() {
        adapter?.retry() ?: throw IllegalStateException("Attach this status handler to adapter before call 'retry'")
    }

    override fun refresh() {
        adapter?.refresh() ?: throw IllegalStateException("Attach this status handler to adapter before call 'refresh'")
    }

    override fun attach(adapter: PagingDataAdapter<*, *>, lifecycle: Lifecycle?) {
        if (this.adapter != null) {
            detach()
        }
        this.adapter = adapter
        if (hasListeners()) {
            adapter.addLoadStateListener(adapterListener)
        }
        lifecycle?.let { it.addObserver(InternalLifecycleObserver(it).apply { lifecycleObserver = this }) }
    }

    override fun detach() {
        if (hasListeners()) {
            adapter?.removeLoadStateListener(adapterListener)
        }
        lifecycleObserver?.let { it.lifecycle.removeObserver(it) }
        this.adapter = null
    }

    override fun addOnValueListener(listener: ValueHandler.OnValueListener<V>) {
        valueHandler.addOnValueListener(listener)
    }

    override fun removeOnValueListener(listener: ValueHandler.OnValueListener<V>) {
        valueHandler.removeOnValueListener(listener)
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
}