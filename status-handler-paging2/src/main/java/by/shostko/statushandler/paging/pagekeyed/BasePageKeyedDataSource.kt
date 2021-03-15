@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package by.shostko.statushandler.paging.pagekeyed

import androidx.paging.PageKeyedDataSource
import by.shostko.statushandler.Status
import by.shostko.statushandler.StatusHandler
import by.shostko.statushandler.paging.*
import by.shostko.statushandler.paging.Logger
import by.shostko.statushandler.paging.updateAppend
import by.shostko.statushandler.paging.updatePrepend

abstract class BasePageKeyedDataSource<K, V>(
    private val statusHandlerCallback: StatusHandler.Callback
) : PageKeyedDataSource<K, V>(), PagingDataSource {

    protected open val tag: String = javaClass.simpleName

    private var status: Status = Status.Initial

    private var retryInitial: (() -> Any)? = null
    private var retryAfter: (() -> Any)? = null
    private var retryBefore: (() -> Any)? = null

    @Synchronized
    private fun updateStatus(updater: (Status) -> Status) {
        status = updater(status)
        statusHandlerCallback.status(status)
    }

    override fun refresh() {
        invalidate()
        retryInitial = null
        retryAfter = null
        retryBefore = null
    }

    override fun retry() {
        retryInitial?.apply { retryInitial = null }?.invoke()
        retryAfter?.apply { retryAfter = null }?.invoke()
        retryBefore?.apply { retryBefore = null }?.invoke()
    }

    final override fun loadInitial(params: LoadInitialParams<K>, callback: LoadInitialCallback<K, V>) {
        Logger.d(tag, "loadInitial for %s", params.asString())
        retryInitial = null
        retryAfter = null
        retryBefore = null
        updateStatus { Status.create(true, null) }
        try {
            onLoadInitial(params, callback)
        } catch (e: Throwable) {
            onFailedResultInitial(e, params, callback)
        }
    }

    protected abstract fun onLoadInitial(params: LoadInitialParams<K>, callback: LoadInitialCallback<K, V>)

    final override fun loadAfter(params: LoadParams<K>, callback: LoadCallback<K, V>) {
        Logger.d(tag, "loadAfter for %s", params.asString())
        retryAfter = null
        updateStatus { it.updateAppend(true, null) }
        try {
            onLoadAfter(params, callback)
        } catch (e: Throwable) {
            onFailedResultAfter(e, params, callback)
        }
    }

    protected abstract fun onLoadAfter(params: LoadParams<K>, callback: LoadCallback<K, V>)

    final override fun loadBefore(params: LoadParams<K>, callback: LoadCallback<K, V>) {
        Logger.d(tag, "loadBefore for %s", params.asString())
        retryBefore = null
        updateStatus { it.updatePrepend(true, null) }
        try {
            onLoadBefore(params, callback)
        } catch (e: Throwable) {
            onFailedResultBefore(e, params, callback)
        }
    }

    protected abstract fun onLoadBefore(params: LoadParams<K>, callback: LoadCallback<K, V>)

    protected fun onSuccessResultInitial(
        list: List<V>,
        previousPageKey: K?,
        nextPageKey: K?,
        params: LoadInitialParams<K>,
        callback: LoadInitialCallback<K, V>
    ) {
        if (!isInvalid) {
            Logger.d(
                tag,
                "onSuccessResult %d items (previousPageKey=%s, nextPageKey=%s) for %s",
                list.size, previousPageKey, nextPageKey, params.asString()
            )
            updateStatus { it.updateRefresh(false, null) }
            callback.onResult(list, previousPageKey, nextPageKey)
        }
    }

    protected fun onSuccessResultAfter(
        list: List<V>,
        nextPageKey: K?,
        params: LoadParams<K>,
        callback: LoadCallback<K, V>
    ) {
        if (!isInvalid) {
            Logger.d(
                tag,
                "onSuccessResult %d items (nextPageKey=%s) for %s",
                list.size, nextPageKey, params.asString()
            )
            updateStatus { it.updateAppend(false, null) }
            callback.onResult(list, nextPageKey)
        }
    }

    protected fun onSuccessResultBefore(
        list: List<V>,
        previousPageKey: K?,
        params: LoadParams<K>,
        callback: LoadCallback<K, V>
    ) {
        if (!isInvalid) {
            Logger.d(
                tag,
                "onSuccessResult %d items (previousPageKey=%s) for %s",
                list.size, previousPageKey, params.asString()
            )
            updateStatus { it.updatePrepend(false, null) }
            callback.onResult(list, previousPageKey)
        }
    }

    protected fun onFailedResultInitial(
        e: Throwable,
        params: LoadInitialParams<K>,
        callback: LoadInitialCallback<K, V>
    ) {
        if (!isInvalid) {
            Logger.e(tag, e, "Error during loadInitial for %s", params.asString())
            retryInitial = { loadInitial(params, callback) }
            updateStatus { it.updateRefresh(false, e) }
        }
    }

    protected fun onFailedResultAfter(
        e: Throwable,
        params: LoadParams<K>,
        callback: LoadCallback<K, V>
    ) {
        if (!isInvalid) {
            Logger.e(tag, e, "Error during loadAfter for %s", params.asString())
            retryAfter = { loadAfter(params, callback) }
            updateStatus { it.updateAppend(false, e) }
        }
    }

    protected fun onFailedResultBefore(
        e: Throwable,
        params: LoadParams<K>,
        callback: LoadCallback<K, V>
    ) {
        if (!isInvalid) {
            Logger.e(tag, e, "Error during loadBefore for %s", params.asString())
            retryBefore = { loadBefore(params, callback) }
            updateStatus { it.updatePrepend(false, e) }
        }
    }
}