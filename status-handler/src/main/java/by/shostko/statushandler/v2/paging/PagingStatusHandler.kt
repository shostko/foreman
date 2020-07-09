@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package by.shostko.statushandler.v2.paging

import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.PagingDataAdapter
import by.shostko.statushandler.v2.Status
import by.shostko.statushandler.v2.StatusHandler
import by.shostko.statushandler.v2.ValueStatusHandler
import by.shostko.statushandler.v2.WrappedStatusHandler

interface PagingStatusHandler : WrappedStatusHandler {
    fun retry()
}

interface PagingValueStatusHandler<V : Any> : PagingStatusHandler, ValueStatusHandler<V>

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
    override val message: String?
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

fun StatusHandler.Companion.wrapPaging(func: () -> PagingDataAdapter<*, *>): PagingStatusHandler = PagingStatusHandlerImpl(func)
fun StatusHandler.Companion.wrapPaging(adapter: PagingDataAdapter<*, *>): PagingStatusHandler = PagingStatusHandlerImpl { adapter }

fun <V : Any> ValueStatusHandler<V>.attach(
    adapter: PagingDataAdapter<*, *>
): PagingValueStatusHandler<V> = CombinedPagingValueStatusHandler(
    statusHandler = PagingStatusHandlerImpl { adapter },
    valueHandler = this
)

fun <V : Any> ValueStatusHandler<V>.attach(
    func: () -> PagingDataAdapter<*, *>
): PagingValueStatusHandler<V> = CombinedPagingValueStatusHandler(
    statusHandler = PagingStatusHandlerImpl(func),
    valueHandler = this
)

fun PagingDataAdapter<*, *>.statusHandler(): PagingStatusHandler = PagingStatusHandlerImpl { this }

// TODO handle merging initial StatusHandler and PagingStatusHandler

val Status.isWorkingRefresh: Boolean
    get() = if (this is PagingStatus) isWorkingRefresh else isWorking
val Status.isWorkingAppend: Boolean
    get() = if (this is PagingStatus) isWorkingAppend else false
val Status.isWorkingPrepend: Boolean
    get() = if (this is PagingStatus) isWorkingPrepend else false
val Status.throwableRefresh: Throwable?
    get() = if (this is PagingStatus) throwableRefresh else null
val Status.throwableAppend: Throwable?
    get() = if (this is PagingStatus) throwableAppend else null
val Status.throwablePrepend: Throwable?
    get() = if (this is PagingStatus) throwablePrepend else null

fun Status.extractRefresh(): Status = when {
    isInitial -> this
    this is PagingStatus -> Status.create(isWorkingRefresh, throwableRefresh)
    else -> throw RuntimeException("Can't extract refresh status from not PagingStatus")
}

fun Status.extractAppend(): Status = when {
    isInitial -> this
    this is PagingStatus -> Status.create(isWorkingAppend, throwableAppend)
    else -> throw RuntimeException("Can't extract append status from not PagingStatus")
}

fun Status.extractPrepend(): Status = when {
    isInitial -> this
    this is PagingStatus -> Status.create(isWorkingPrepend, throwablePrepend)
    else -> throw RuntimeException("Can't extract prepend status from not PagingStatus")
}