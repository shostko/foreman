@file:Suppress("unused")

package by.shostko.statushandler.paging

import by.shostko.statushandler.Status

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
    this is RefreshingStatusWrapper -> this
    else -> throw RuntimeException("Can't extract refresh status from not PagingStatus")
}

fun Status.extractAppend(): Status = when {
    isInitial -> this
    this is PagingStatus -> Status.create(isWorkingAppend, throwableAppend)
    this is RefreshingStatusWrapper -> Status.Initial
    else -> throw RuntimeException("Can't extract append status from not PagingStatus")
}

fun Status.extractPrepend(): Status = when {
    isInitial -> this
    this is PagingStatus -> Status.create(isWorkingPrepend, throwablePrepend)
    this is RefreshingStatusWrapper -> Status.Initial
    else -> throw RuntimeException("Can't extract prepend status from not PagingStatus")
}

fun Status.asRefreshingStatusWrapper(): Status = if (isInitial || this is PagingStatus) this else RefreshingStatusWrapper(this)

private class RefreshingStatusWrapper(status: Status) : Status(status.working, status.throwable) {
    override fun toString(): String = StringBuilder("RefreshingStatus{").apply {
        if (isSuccess) {
            append("SUCCESS")
        } else {
            val initialLength = length
            if (isWorking) {
                append("REFRESHING")
            }
            if (isFailed) {
                if (length > initialLength) {
                    append(";")
                }
                append("FAILED")
                if (throwable != null) {
                    append(':')
                    append(throwable)
                }
            }
        }
        append('}')
    }.toString()
}