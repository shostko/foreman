@file:Suppress("MemberVisibilityCanBePrivate")

package by.shostko.statushandler.paging

import by.shostko.statushandler.Status

class PagingStatus(
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

private class PagingThrowable(
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

    override fun toString(): String = StringBuilder("PagingThrowable(")
        .append(message)
        .append(')')
        .toString()
}