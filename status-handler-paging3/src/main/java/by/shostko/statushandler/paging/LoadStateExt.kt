package by.shostko.statushandler.paging

import androidx.paging.LoadState
import by.shostko.statushandler.Status

fun LoadState.toStatus(): Status = Status.create(this is LoadState.Loading, (this as? LoadState.Error)?.error)