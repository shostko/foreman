@file:Suppress("unused", "CanBeParameter", "MemberVisibilityCanBePrivate")

package by.shostko.statushandler.paging

import by.shostko.statushandler.Status

private class PagingStatusImpl(
    override val isWorkingRefresh: Boolean,
    override val throwableRefresh: Throwable?,
    override val isWorkingAppend: Boolean,
    override val throwableAppend: Throwable?,
    override val isWorkingPrepend: Boolean,
    override val throwablePrepend: Throwable?
) : PagingStatus(
    working = (if (isWorkingRefresh) WORKING else NOT_WORKING)
            or (if (isWorkingAppend) WORKING_APPEND else NOT_WORKING)
            or (if (isWorkingPrepend) WORKING_PREPEND else NOT_WORKING),
    throwable = if (throwableRefresh != null || throwableAppend != null || throwablePrepend != null) {
        PagingThrowable(throwableRefresh, throwableAppend, throwablePrepend)
    } else {
        null
    }
)

internal fun Status.updateRefresh(isWorking: Boolean, throwable: Throwable?): PagingStatus =
    if (this is PagingStatusImpl) {
        PagingStatusImpl(
            isWorkingRefresh = isWorking,
            throwableRefresh = throwable,
            isWorkingAppend = isWorkingAppend,
            throwableAppend = throwableAppend,
            isWorkingPrepend = isWorkingPrepend,
            throwablePrepend = throwablePrepend
        )
    } else {
        PagingStatusImpl(
            isWorkingRefresh = isWorking,
            throwableRefresh = throwable,
            isWorkingAppend = false,
            throwableAppend = null,
            isWorkingPrepend = false,
            throwablePrepend = null
        )
    }

internal fun Status.updateAppend(isWorking: Boolean, throwable: Throwable?): PagingStatus =
    if (this is PagingStatusImpl) {
        PagingStatusImpl(
            isWorkingRefresh = isWorkingRefresh,
            throwableRefresh = throwableRefresh,
            isWorkingAppend = isWorking,
            throwableAppend = throwable,
            isWorkingPrepend = isWorkingPrepend,
            throwablePrepend = throwablePrepend
        )
    } else {
        PagingStatusImpl(
            isWorkingRefresh = this.isWorking,
            throwableRefresh = this.throwable,
            isWorkingAppend = isWorking,
            throwableAppend = throwable,
            isWorkingPrepend = false,
            throwablePrepend = null
        )
    }

internal fun Status.updatePrepend(isWorking: Boolean, throwable: Throwable?): PagingStatus =
    if (this is PagingStatusImpl) {
        PagingStatusImpl(
            isWorkingRefresh = isWorkingRefresh,
            throwableRefresh = throwableRefresh,
            isWorkingAppend = isWorkingAppend,
            throwableAppend = throwableAppend,
            isWorkingPrepend = isWorking,
            throwablePrepend = throwable
        )
    } else {
        PagingStatusImpl(
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
): PagingStatus =
    if (this is PagingStatusImpl) {
        PagingStatusImpl(
            isWorkingRefresh = isWorkingRefresh,
            throwableRefresh = throwableRefresh,
            isWorkingAppend = isWorkingAppend,
            throwableAppend = throwableAppend,
            isWorkingPrepend = isWorkingPrepend,
            throwablePrepend = throwablePrepend
        )
    } else {
        PagingStatusImpl(
            isWorkingRefresh = this.isWorking,
            throwableRefresh = this.throwable,
            isWorkingAppend = isWorkingAppend,
            throwableAppend = throwableAppend,
            isWorkingPrepend = isWorkingPrepend,
            throwablePrepend = throwablePrepend
        )
    }