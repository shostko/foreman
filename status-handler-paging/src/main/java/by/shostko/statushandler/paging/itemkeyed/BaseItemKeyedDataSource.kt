package by.shostko.statushandler.paging.itemkeyed

import androidx.paging.ItemKeyedDataSource
import by.shostko.statushandler.Action
import by.shostko.statushandler.Direction
import by.shostko.statushandler.StatusHandler
import by.shostko.statushandler.paging.asString
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

abstract class BaseItemKeyedDataSource<K, V>(
    protected val statusHandler: StatusHandler<*>
) : ItemKeyedDataSource<K, V>() {

    protected open val tag: String = javaClass.simpleName

    private var retryFunction: (() -> Any)? = null

    init {
        val disposable = statusHandler.action
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .subscribe({
                Timber.tag(tag).d("%s action requested", it)
                when (it) {
                    Action.RETRY -> retryFunction?.apply { retryFunction = null }?.invoke()
                    else -> invalidate()
                }
            }, { Timber.tag(tag).e(it, "Error during listening actions") })
        addInvalidatedCallback { disposable.dispose() }
    }

    final override fun loadInitial(params: LoadInitialParams<K>, callback: LoadInitialCallback<V>) {
        Timber.tag(tag).d("loadInitial for %s", params.asString())
        statusHandler.updateWorking()
        try {
            onLoadInitial(params, callback)
        } catch (e: Throwable) {
            onFailedResultInitial(e, params, callback)
        }
    }

    @Throws(Throwable::class)
    protected abstract fun onLoadInitial(params: LoadInitialParams<K>, callback: LoadInitialCallback<V>)

    final override fun loadAfter(params: LoadParams<K>, callback: LoadCallback<V>) {
        Timber.tag(tag).d("loadAfter for %s", params.asString())
        statusHandler.updateWorkingForward()
        try {
            onLoadAfter(params, callback)
        } catch (e: Throwable) {
            onFailedResultAfter(e, params, callback)
        }
    }

    @Throws(Throwable::class)
    protected abstract fun onLoadAfter(params: LoadParams<K>, callback: LoadCallback<V>)

    final override fun loadBefore(params: LoadParams<K>, callback: LoadCallback<V>) {
        Timber.tag(tag).d("loadBefore for %s", params.asString())
        statusHandler.updateWorkingBackward()
        try {
            onLoadBefore(params, callback)
        } catch (e: Throwable) {
            onFailedResultBefore(e, params, callback)
        }
    }

    @Throws(Throwable::class)
    protected abstract fun onLoadBefore(params: LoadParams<K>, callback: LoadCallback<V>)

    protected fun onSuccessResult(
        list: List<V>,
        params: LoadInitialParams<K>,
        callback: LoadInitialCallback<V>
    ) {
        if (!isInvalid) {
            Timber.tag(tag).d("onSuccessResult %d items for %s", list.size, params.asString())
            retryFunction = null
            statusHandler.updateSuccess()
            callback.onResult(list)
        }
    }

    protected fun onSuccessResult(
        list: List<V>,
        params: LoadParams<K>,
        callback: LoadCallback<V>
    ) {
        if (!isInvalid) {
            Timber.tag(tag).d("onSuccessResult %d items for %s", list.size, params.asString())
            retryFunction = null
            statusHandler.updateSuccess()
            callback.onResult(list)
        }
    }

    protected fun onFailedResultInitial(
        e: Throwable,
        params: LoadInitialParams<K>,
        callback: LoadInitialCallback<V>
    ) {
        if (!isInvalid) {
            Timber.tag(tag).e(e, "Error during loadInitial for %s", params.asString())
            retryFunction = { loadInitial(params, callback) }
            statusHandler.updateFailed(e)
        }
    }

    protected fun onFailedResultBefore(
        e: Throwable,
        params: LoadParams<K>,
        callback: LoadCallback<V>
    ) {
        if (!isInvalid) {
            Timber.tag(tag).e(e, "Error during loadBefore for %s", params.asString())
            statusHandler.updateFailed(e)
            retryFunction = { loadBefore(params, callback) }
        }
    }

    protected fun onFailedResultAfter(
        e: Throwable,
        params: LoadParams<K>,
        callback: LoadCallback<V>
    ) {
        if (!isInvalid) {
            Timber.tag(tag).e(e, "Error during loadAfter for %s", params.asString())
            retryFunction = { loadAfter(params, callback) }
            statusHandler.updateFailed(e)
        }
    }
}