@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package by.shostko.statushandler.paging

import androidx.paging.CombinedLoadStates
import androidx.paging.PagingDataAdapter
import by.shostko.statushandler.BaseStatusHandler

internal class PagingStatusHandlerImpl(
    func: () -> PagingDataAdapter<*, *>
) : BaseStatusHandler(), PagingStatusHandler {

    private val adapter: PagingDataAdapter<*, *> by lazy(func)

    private val adapterListener: (CombinedLoadStates) -> Unit by lazy {
        object : (CombinedLoadStates) -> Unit {
            override fun invoke(states: CombinedLoadStates) {
                status(PagingStatus(states))
            }
        }
    }

    override fun onFirstListenerAdded() {
        adapter.addLoadStateListener(adapterListener)
    }

    override fun onLastListenerRemoved() {
        adapter.removeLoadStateListener(adapterListener)
    }

    override fun retry() {
        adapter.retry()
    }

    override fun refresh() {
        adapter.refresh()
    }
}