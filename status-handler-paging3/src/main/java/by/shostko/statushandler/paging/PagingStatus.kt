@file:Suppress("MemberVisibilityCanBePrivate")

package by.shostko.statushandler.paging

import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import by.shostko.statushandler.Status

class PagingStatus internal constructor(private val states: CombinedLoadStates) : CorePagingStatus(
    working = (if (states.refresh === LoadState.Loading) WORKING else NOT_WORKING)
            or (if (states.append === LoadState.Loading) WORKING_APPEND else NOT_WORKING)
            or (if (states.prepend === LoadState.Loading) WORKING_PREPEND else NOT_WORKING),
    throwable = if (states.refresh is LoadState.Error || states.append is LoadState.Error || states.prepend is LoadState.Error) {
        PagingThrowable(
            throwableRefresh = (states.refresh as LoadState.Error).error,
            throwableAppend = (states.prepend as LoadState.Error).error,
            throwablePrepend = (states.append as LoadState.Error).error
        )
    } else {
        null
    }
) {
    override val isWorkingRefresh: Boolean
        get() = isWorking
    override val isWorkingAppend: Boolean
        get() = working and WORKING_APPEND == WORKING_APPEND
    override val isWorkingPrepend: Boolean
        get() = working and WORKING_PREPEND == WORKING_PREPEND
    override val throwableRefresh: Throwable?
        get() = (states.refresh as? LoadState.Error)?.error
    override val throwableAppend: Throwable?
        get() = (states.append as? LoadState.Error)?.error
    override val throwablePrepend: Throwable?
        get() = (states.prepend as? LoadState.Error)?.error
}