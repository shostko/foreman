@file:Suppress("MemberVisibilityCanBePrivate")

package by.shostko.statushandler.paging

import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import by.shostko.statushandler.Status

class PagingStatus internal constructor(private val states: CombinedLoadStates) : Status(
    working = (if (states.refresh === LoadState.Loading) WORKING else NOT_WORKING)
        or (if (states.append === LoadState.Loading) WORKING_APPEND else NOT_WORKING)
        or (if (states.prepend === LoadState.Loading) WORKING_PREPEND else NOT_WORKING),
    throwable = if (states.refresh is LoadState.Error || states.append is LoadState.Error || states.prepend is LoadState.Error) {
        PagingThrowable(states)
    } else {
        null
    }
) {
    val isWorkingRefresh: Boolean
        get() = isWorking
    val isWorkingAppend: Boolean
        get() = working and WORKING_APPEND == WORKING_APPEND
    val isWorkingPrepend: Boolean
        get() = working and WORKING_PREPEND == WORKING_PREPEND
    val throwableRefresh: Throwable?
        get() = (states.refresh as? LoadState.Error)?.error
    val throwableAppend: Throwable?
        get() = (states.append as? LoadState.Error)?.error
    val throwablePrepend: Throwable?
        get() = (states.prepend as? LoadState.Error)?.error

    override fun toString(): String = StringBuilder("PagingStatus{").apply {
        val initialLength = length
        if (isWorkingRefresh) {
            append("REFRESHING")
        }
        if (isWorkingAppend) {
            if (length > initialLength) {
                append(";")
            }
            append("APPENDING")
        }
        if (isWorkingPrepend) {
            if (length > initialLength) {
                append(";")
            }
            append("PREPENDING")
        }
        if (throwable != null) {
            if (length > initialLength) {
                append(";")
            }
            append("ERROR:")
            append(throwable.message)
        }
        if (length == initialLength) {
            append("SUCCESS")
        }
        append('}')
    }.toString()
}

class PagingThrowable(
    val states: CombinedLoadStates
) : Throwable() {
    override val message: String
        get() = StringBuilder().apply {
            (states.refresh as? LoadState.Error)?.error?.let {
                append("refresh='")
                append(it.message)
                append('\'')
            }
            (states.prepend as? LoadState.Error)?.error?.let {
                if (length > 0) {
                    append("; ")
                }
                append("prepend='")
                append(it.message)
                append('\'')
            }
            (states.append as? LoadState.Error)?.error?.let {
                if (length > 0) {
                    append("; ")
                }
                append("append='")
                append(it.message)
                append('\'')
            }
        }.toString()
}