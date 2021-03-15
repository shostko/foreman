@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package by.shostko.statushandler.paging.positional

import androidx.paging.PositionalDataSource
import by.shostko.statushandler.Status
import by.shostko.statushandler.StatusHandler
import by.shostko.statushandler.paging.*
import by.shostko.statushandler.paging.Logger
import by.shostko.statushandler.paging.updateAppendPrepend
import by.shostko.statushandler.paging.updateRefresh

abstract class BasePositionalDataSource<V>(
    private val statusHandlerCallback: StatusHandler.Callback
) : PositionalDataSource<V>(), PagingDataSource {

    protected open val tag: String = javaClass.simpleName

    private var status: Status = Status.Initial

    private var anchor: Int? = null
    private var retryInitial: (() -> Any)? = null
    private var retryRange: (() -> Any)? = null

    @Synchronized
    private fun updateStatus(updater: (Status) -> Status) {
        status = updater(status)
        statusHandlerCallback.status(status)
    }

    override fun refresh() {
        invalidate()
        retryInitial = null
        retryRange = null
    }

    override fun retry() {
        retryInitial?.apply { retryInitial = null }?.invoke()
        retryRange?.apply { retryRange = null }?.invoke()
    }

    final override fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<V>) {
        Logger.d(tag, "loadInitial for %s", params.asString())
        retryInitial = null
        retryRange = null
        updateStatus { Status.create(true, null) }
        try {
            onLoadInitial(params, callback)
        } catch (e: Throwable) {
            onFailedResult(e, params, callback)
        }
    }

    protected abstract fun onLoadInitial(params: LoadInitialParams, callback: LoadInitialCallback<V>)

    final override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<V>) {
        Logger.d(tag, "loadRange for %s", params.asString())
        retryRange = null
        updateStatusWithAnchor(params,
            refresh = { Status.create(true, null) },
            append = { it.updateAppendPrepend(true, null, false, null) },
            prepend = { it.updateAppendPrepend(false, null, true, null) })
        try {
            onLoadRange(params, callback)
        } catch (e: Throwable) {
            onFailedResult(e, params, callback)
        }
    }

    protected abstract fun onLoadRange(params: LoadRangeParams, callback: LoadRangeCallback<V>)

    protected fun onSuccessResult(
        list: List<V>,
        frontPosition: Int,
        totalCount: Int,
        params: LoadInitialParams,
        callback: LoadInitialCallback<V>
    ) {
        if (!isInvalid) {
            Logger.d(
                tag,
                "onSuccessResult %d (frontPosition=%d, totalCount=%d) items for %s",
                list.size, frontPosition, totalCount, params.asString()
            )
            anchor = frontPosition + list.size - 1
            updateStatus { it.updateRefresh(false, null) }
            callback.onResult(list, frontPosition, totalCount)
        }
    }

    protected fun onSuccessResult(
        list: List<V>,
        frontPosition: Int,
        params: LoadInitialParams,
        callback: LoadInitialCallback<V>
    ) {
        if (!isInvalid) {
            Logger.d(
                tag,
                "onSuccessResult %d (frontPosition=%d) items for %s",
                list.size, frontPosition, params.asString()
            )
            anchor = frontPosition + list.size - 1
            updateStatus { it.updateRefresh(false, null) }
            callback.onResult(list, frontPosition)
        }
    }

    protected fun onSuccessResult(
        list: List<V>,
        params: LoadInitialParams,
        callback: LoadInitialCallback<V>
    ) {
        if (!isInvalid) {
            Logger.d(tag, "onSuccessResult %d items for %s", list.size, params.asString())
            anchor = params.requestedStartPosition + list.size - 1
            updateStatus { it.updateRefresh(false, null) }
            callback.onResult(list, params.requestedStartPosition)
        }
    }

    protected fun onSuccessResult(
        list: List<V>,
        params: LoadRangeParams,
        callback: LoadRangeCallback<V>
    ) {
        if (!isInvalid) {
            Logger.d(tag, "onSuccessResult %d items for %s", list.size, params.asString())
            anchor = params.startPosition + list.size - 1
            updateStatusWithAnchor(params,
                refresh = { Status.create(false, null) },
                append = { it.updateAppendPrepend(false, null, false, null) },
                prepend = { it.updateAppendPrepend(false, null, false, null) })
            callback.onResult(list)
        }
    }

    protected fun onFailedResult(
        e: Throwable,
        params: LoadInitialParams,
        callback: LoadInitialCallback<V>
    ) {
        if (!isInvalid) {
            Logger.e(tag, e, "Error during loadInitial for %s", params.asString())
            retryInitial = { loadInitial(params, callback) }
            updateStatus { it.updateRefresh(false, e) }
        }
    }

    protected fun onFailedResult(
        e: Throwable,
        params: LoadRangeParams,
        callback: LoadRangeCallback<V>
    ) {
        if (!isInvalid) {
            Logger.e(tag, e, "Error during loadAfter for %s", params.asString())
            retryRange = { loadRange(params, callback) }
            updateStatusWithAnchor(params,
                refresh = { Status.create(false, e) },
                append = { it.updateAppendPrepend(false, e, false, null) },
                prepend = { it.updateAppendPrepend(false, null, false, e) })
        }
    }

    private fun updateStatusWithAnchor(
        params: LoadRangeParams,
        refresh: (Status) -> Status,
        append: (Status) -> Status,
        prepend: (Status) -> Status
    ) {
        anchor.let { anchor ->
            when {
                anchor == null -> updateStatus(refresh)
                anchor < params.startPosition -> updateStatus(append)
                else -> updateStatus(prepend)
            }
        }
    }
}