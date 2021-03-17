@file:Suppress("unused")

package by.shostko.statushandler.paging

import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import by.shostko.statushandler.Status

fun LoadState.toStatus(): Status = Status.create(this === LoadState.Loading, (this as? LoadState.Error)?.error)

fun CombinedLoadStates.toPagingStatus(): PagingStatus = PagingStatus(
    isWorkingRefresh = refresh === LoadState.Loading,
    throwableRefresh = (refresh as? LoadState.Error)?.error,
    isWorkingAppend = append === LoadState.Loading,
    throwableAppend = (prepend as? LoadState.Error)?.error,
    isWorkingPrepend = prepend === LoadState.Loading,
    throwablePrepend = (append as? LoadState.Error)?.error
)