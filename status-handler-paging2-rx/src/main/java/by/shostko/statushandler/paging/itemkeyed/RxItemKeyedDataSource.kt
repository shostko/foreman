@file:Suppress("unused")

package by.shostko.statushandler.paging.itemkeyed

import by.shostko.statushandler.StatusHandler
import by.shostko.statushandler.paging.blockingGetWithoutWrap
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable

abstract class RxItemKeyedDataSource<K, V>(
    statusHandlerCallback: StatusHandler.Callback,
    private val scheduler: Scheduler? = null
) : BaseItemKeyedDataSource<K, V>(statusHandlerCallback) {

    private val disposableDelegate = lazy { CompositeDisposable() }
    private val disposable: CompositeDisposable
        get() {
            if (!disposableDelegate.isInitialized()) {
                addInvalidatedCallback { disposableDelegate.value.dispose() }
            }
            return disposableDelegate.value
        }

    final override fun onLoadInitial(params: LoadInitialParams<K>, callback: LoadInitialCallback<V>) {
        val single = onLoadInitial(params.requestedInitialKey, params.requestedLoadSize)
        if (scheduler == null) {
            onSuccessResult(single.blockingGetWithoutWrap(), params, callback)
        } else {
            disposable.add(
                single.subscribeOn(scheduler)
                    .observeOn(scheduler)
                    .subscribe({
                        onSuccessResult(it, params, callback)
                    }, {
                        onFailedResultInitial(it, params, callback)
                    })
            )
        }
    }

    final override fun onLoadAfter(params: LoadParams<K>, callback: LoadCallback<V>) {
        val single = onLoadAfter(params.key, params.requestedLoadSize)
        if (scheduler == null) {
            onSuccessResultAfter(single.blockingGetWithoutWrap(), params, callback)
        } else {
            disposable.add(
                single.subscribeOn(scheduler)
                    .observeOn(scheduler)
                    .subscribe({
                        onSuccessResultAfter(it, params, callback)
                    }, {
                        onFailedResultAfter(it, params, callback)
                    })
            )
        }
    }

    final override fun onLoadBefore(params: LoadParams<K>, callback: LoadCallback<V>) {
        val single = onLoadBefore(params.key, params.requestedLoadSize)
        if (scheduler == null) {
            onSuccessResultBefore(single.blockingGetWithoutWrap(), params, callback)
        } else {
            disposable.add(
                single.subscribeOn(scheduler)
                    .observeOn(scheduler)
                    .subscribe({
                        onSuccessResultBefore(it, params, callback)
                    }, {
                        onFailedResultBefore(it, params, callback)
                    })
            )
        }
    }

    @Throws(Throwable::class)
    protected abstract fun onLoadInitial(key: K?, requestedLoadSize: Int): Single<List<V>>

    @Throws(Throwable::class)
    protected abstract fun onLoadAfter(key: K?, requestedLoadSize: Int): Single<List<V>>

    @Throws(Throwable::class)
    protected abstract fun onLoadBefore(key: K?, requestedLoadSize: Int): Single<List<V>>
}