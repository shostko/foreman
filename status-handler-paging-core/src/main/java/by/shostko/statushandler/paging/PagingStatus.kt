@file:Suppress("MemberVisibilityCanBePrivate")

package by.shostko.statushandler.paging

import by.shostko.statushandler.Status

abstract class CorePagingStatus(working: Int, throwable: Throwable?) : Status(working, throwable) {
    abstract val isWorkingRefresh: Boolean
    abstract val isWorkingAppend: Boolean
    abstract val isWorkingPrepend: Boolean
    abstract val throwableRefresh: Throwable?
    abstract val throwableAppend: Throwable?
    abstract val throwablePrepend: Throwable?

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