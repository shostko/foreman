@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package by.shostko.statushandler.paging.itemkeyed

import androidx.paging.ItemKeyedDataSource
import by.shostko.statushandler.Status
import by.shostko.statushandler.StatusHandler
import by.shostko.statushandler.paging.*

abstract class BaseItemKeyedDataSource<K, V>(
    private val statusHandlerCallback: StatusHandler.Callback
) : ItemKeyedDataSource<K, V>(), PagingController {

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

    final override fun loadInitial(params: LoadInitialParams<K>, callback: LoadInitialCallback<V>) {
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

    @Throws(Throwable::class)
    protected abstract fun onLoadInitial(params: LoadInitialParams<K>, callback: LoadInitialCallback<V>)

    final override fun loadAfter(params: LoadParams<K>, callback: LoadCallback<V>) {
        Logger.d(tag, "loadAfter for %s", params.asString())
        retryAfter = null
        updateStatus { it.updateAppend(true, null) }
        try {
            onLoadAfter(params, callback)
        } catch (e: Throwable) {
            onFailedResultAfter(e, params, callback)
        }
    }

    @Throws(Throwable::class)
    protected abstract fun onLoadAfter(params: LoadParams<K>, callback: LoadCallback<V>)

    final override fun loadBefore(params: LoadParams<K>, callback: LoadCallback<V>) {
        Logger.d(tag, "loadBefore for %s", params.asString())
        retryBefore = null
        updateStatus { it.updatePrepend(true, null) }
        try {
            onLoadBefore(params, callback)
        } catch (e: Throwable) {
            onFailedResultBefore(e, params, callback)
        }
    }

    @Throws(Throwable::class)
    protected abstract fun onLoadBefore(params: LoadParams<K>, callback: LoadCallback<V>)

    protected fun onSuccessResultInitial(
        list: List<V>,
        position: Int,
        total: Int,
        params: LoadInitialParams<K>,
        callback: LoadInitialCallback<V>
    ) {
        if (!isInvalid) {
            Logger.d(tag, "onSuccessResult %d items (position=%s; total=%s) for %s", list.size, position, total, params.asString())
            updateStatus { it.updateRefresh(false, null) }
            callback.onResult(list, position, total)
        }
    }

    protected fun onSuccessResultInitial(
        list: List<V>,
        params: LoadInitialParams<K>,
        callback: LoadInitialCallback<V>
    ) {
        if (!isInvalid) {
            Logger.d(tag, "onSuccessResult %d items for %s", list.size, params.asString())
            updateStatus { it.updateRefresh(false, null) }
            callback.onResult(list)
        }
    }

    protected fun onSuccessResultBefore(
        list: List<V>,
        params: LoadParams<K>,
        callback: LoadCallback<V>
    ) {
        if (!isInvalid) {
            Logger.d(tag, "onSuccessResultBefore %d items for %s", list.size, params.asString())
            updateStatus { it.updatePrepend(false, null) }
            callback.onResult(list)
        }
    }

    protected fun onSuccessResultAfter(
        list: List<V>,
        params: LoadParams<K>,
        callback: LoadCallback<V>
    ) {
        if (!isInvalid) {
            Logger.d(tag, "onSuccessResultAfter %d items for %s", list.size, params.asString())
            updateStatus { it.updateAppend(false, null) }
            callback.onResult(list)
        }
    }

    protected fun onFailedResultInitial(
        e: Throwable,
        params: LoadInitialParams<K>,
        callback: LoadInitialCallback<V>
    ) {
        if (!isInvalid) {
            Logger.e(tag, e, "Error during loadInitial for %s", params.asString())
            retryInitial = { loadInitial(params, callback) }
            updateStatus { it.updateRefresh(false, e) }
        }
    }

    protected fun onFailedResultBefore(
        e: Throwable,
        params: LoadParams<K>,
        callback: LoadCallback<V>
    ) {
        if (!isInvalid) {
            Logger.e(tag, e, "Error during loadBefore for %s", params.asString())
            retryBefore = { loadBefore(params, callback) }
            updateStatus { it.updatePrepend(false, e) }
        }
    }

    protected fun onFailedResultAfter(
        e: Throwable,
        params: LoadParams<K>,
        callback: LoadCallback<V>
    ) {
        if (!isInvalid) {
            Logger.e(tag, e, "Error during loadAfter for %s", params.asString())
            retryAfter = { loadAfter(params, callback) }
            updateStatus { it.updateAppend(false, e) }
        }
    }
}