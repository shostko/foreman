@file:Suppress("unused", "CanBeParameter", "MemberVisibilityCanBePrivate")

package by.shostko.statushandler.paging

import by.shostko.statushandler.Status

class PagingStatus internal constructor(
    override val isWorkingRefresh: Boolean,
    override val throwableRefresh: Throwable?,
    override val isWorkingAppend: Boolean,
    override val throwableAppend: Throwable?,
    override val isWorkingPrepend: Boolean,
    override val throwablePrepend: Throwable?
) : CorePagingStatus(
    working = (if (isWorkingRefresh) WORKING else NOT_WORKING)
            or (if (isWorkingAppend) WORKING_APPEND else NOT_WORKING)
            or (if (isWorkingPrepend) WORKING_PREPEND else NOT_WORKING),
    throwable = if (throwableRefresh != null || throwableAppend != null || throwablePrepend != null) {
        PagingThrowable(throwableRefresh, throwableAppend, throwablePrepend)
    } else {
        null
    }
)

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