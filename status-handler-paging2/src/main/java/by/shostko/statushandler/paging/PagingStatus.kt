@file:Suppress("unused", "CanBeParameter", "MemberVisibilityCanBePrivate")

package by.shostko.statushandler.paging

import by.shostko.statushandler.Status

class PagingStatus internal constructor(
    val isWorkingRefresh: Boolean,
    val throwableRefresh: Throwable?,
    val isWorkingAppend: Boolean,
    val throwableAppend: Throwable?,
    val isWorkingPrepend: Boolean,
    val throwablePrepend: Throwable?
) : Status(
    working = (if (isWorkingRefresh) WORKING else NOT_WORKING)
            or (if (isWorkingAppend) WORKING_APPEND else NOT_WORKING)
            or (if (isWorkingPrepend) WORKING_PREPEND else NOT_WORKING),
    throwable = if (throwableRefresh != null || throwableAppend != null || throwablePrepend != null) {
        PagingThrowable(throwableRefresh, throwableAppend, throwablePrepend)
    } else {
        null
    }
) {
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
    val throwableRefresh: Throwable?,
    val throwableAppend: Throwable?,
    val throwablePrepend: Throwable?
) : Throwable() {
    override val message: String
        get() = StringBuilder().apply {
            throwableRefresh?.let {
                append("refresh='")
                append(it.message)
                append('\'')
            }
            throwableAppend?.let {
                if (length > 0) {
                    append("; ")
                }
                append("prepend='")
                append(it.message)
                append('\'')
            }
            throwablePrepend?.let {
                if (length > 0) {
                    append("; ")
                }
                append("append='")
                append(it.message)
                append('\'')
            }
        }.toString()
}

internal fun Status.updateRefresh(isWorking: Boolean, throwable: Throwable?) =
    if (this is PagingStatus) {
        PagingStatus(
            isWorkingRefresh = isWorking,
            throwableRefresh = throwable,
            isWorkingAppend = isWorkingAppend,
            throwableAppend = throwableAppend,
            isWorkingPrepend = isWorkingPrepend,
            throwablePrepend = throwablePrepend
        )
    } else {
        PagingStatus(
            isWorkingRefresh = isWorking,
            throwableRefresh = throwable,
            isWorkingAppend = false,
            throwableAppend = null,
            isWorkingPrepend = false,
            throwablePrepend = null
        )
    }

internal fun Status.updateAppend(isWorking: Boolean, throwable: Throwable?) =
    if (this is PagingStatus) {
        PagingStatus(
            isWorkingRefresh = isWorkingRefresh,
            throwableRefresh = throwableRefresh,
            isWorkingAppend = isWorking,
            throwableAppend = throwable,
            isWorkingPrepend = isWorkingPrepend,
            throwablePrepend = throwablePrepend
        )
    } else {
        PagingStatus(
            isWorkingRefresh = this.isWorking,
            throwableRefresh = this.throwable,
            isWorkingAppend = isWorking,
            throwableAppend = throwable,
            isWorkingPrepend = false,
            throwablePrepend = null
        )
    }

internal fun Status.updatePrepend(isWorking: Boolean, throwable: Throwable?) =
    if (this is PagingStatus) {
        PagingStatus(
            isWorkingRefresh = isWorkingRefresh,
            throwableRefresh = throwableRefresh,
            isWorkingAppend = isWorkingAppend,
            throwableAppend = throwableAppend,
            isWorkingPrepend = isWorking,
            throwablePrepend = throwable
        )
    } else {
        PagingStatus(
            isWorkingRefresh = this.isWorking,
            throwableRefresh = this.throwable,
            isWorkingAppend = false,
            throwableAppend = null,
            isWorkingPrepend = isWorking,
            throwablePrepend = throwable
        )
    }

internal fun Status.updateAppendPrepend(
    isWorkingAppend: Boolean, throwableAppend: Throwable?,
    isWorkingPrepend: Boolean, throwablePrepend: Throwable?
) = if (this is PagingStatus) {
    PagingStatus(
        isWorkingRefresh = isWorkingRefresh,
        throwableRefresh = throwableRefresh,
        isWorkingAppend = isWorkingAppend,
        throwableAppend = throwableAppend,
        isWorkingPrepend = isWorkingPrepend,
        throwablePrepend = throwablePrepend
    )
} else {
    PagingStatus(
        isWorkingRefresh = this.isWorking,
        throwableRefresh = this.throwable,
        isWorkingAppend = isWorkingAppend,
        throwableAppend = throwableAppend,
        isWorkingPrepend = isWorkingPrepend,
        throwablePrepend = throwablePrepend
    )
}