@file:Suppress("unused")

package by.shostko.statushandler.paging.pagekeyed

import by.shostko.statushandler.StatusHandler
import by.shostko.statushandler.paging.blockingGetWithoutWrap
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable

abstract class RxPageKeyedDataSource<K, V>(
    statusHandlerCallback: StatusHandler.Callback,
    private val scheduler: Scheduler? = null
) : BasePageKeyedDataSource<K, V>(statusHandlerCallback) {

    private val disposableDelegate = lazy { CompositeDisposable() }
    private val disposable: CompositeDisposable
        get() {
            if (!disposableDelegate.isInitialized()) {
                addInvalidatedCallback { disposableDelegate.value.dispose() }
            }
            return disposableDelegate.value
        }

    final override fun onLoadInitial(params: LoadInitialParams<K>, callback: LoadInitialCallback<K, V>) {
        val single = onLoadInitial(params)
        if (scheduler == null) {
            val (prev, list, next) = single.blockingGetWithoutWrap()
            onSuccessResultInitial(list, prev, next, params, callback)
        } else {
            disposable.add(
                single.subscribeOn(scheduler)
                    .observeOn(scheduler)
                    .subscribe({ (prev, list, next) ->
                        onSuccessResultInitial(list, prev, next, params, callback)
                    }, {
                        onFailedResultInitial(it, params, callback)
                    })
            )
        }
    }

    final override fun onLoadAfter(params: LoadParams<K>, callback: LoadCallback<K, V>) {
        val single = onLoadAfter(params)
        if (scheduler == null) {
            val (list, next) = single.blockingGetWithoutWrap()
            onSuccessResultAfter(list, next, params, callback)
        } else {
            disposable.add(
                single.subscribeOn(scheduler)
                    .observeOn(scheduler)
                    .subscribe({ (list, next) ->
                        onSuccessResultAfter(list, next, params, callback)
                    }, {
                        onFailedResultAfter(it, params, callback)
                    })
            )
        }
    }

    final override fun onLoadBefore(params: LoadParams<K>, callback: LoadCallback<K, V>) {
        val single = onLoadBefore(params)
        if (scheduler == null) {
            val (prev, list) = single.blockingGetWithoutWrap()
            onSuccessResultBefore(list, prev, params, callback)
        } else {
            disposable.add(
                single.subscribeOn(scheduler)
                    .observeOn(scheduler)
                    .subscribe({ (prev, list) ->
                        onSuccessResultBefore(list, prev, params, callback)
                    }, {
                        onFailedResultBefore(it, params, callback)
                    })
            )
        }
    }

    @Throws(Throwable::class)
    protected abstract fun onLoadInitial(params: LoadInitialParams<K>): Single<InitialResult<K, V>>

    @Throws(Throwable::class)
    protected abstract fun onLoadAfter(params: LoadParams<K>): Single<AfterResult<K, V>>

    @Throws(Throwable::class)
    protected abstract fun onLoadBefore(params: LoadParams<K>): Single<BeforeResult<K, V>>

    protected fun result(prev: K?, list: List<V>, next: K?): InitialResult<K, V> = InitialResult(prev, list, next)

    protected fun result(list: List<V>, next: K?): AfterResult<K, V> = AfterResult(list, next)

    protected fun result(prev: K?, list: List<V>): BeforeResult<K, V> = BeforeResult(prev, list)

    protected data class InitialResult<K, V>(
        val prev: K?,
        val list: List<V>,
        val next: K?
    )

    protected data class AfterResult<K, V>(
        val list: List<V>,
        val next: K?
    )

    protected data class BeforeResult<K, V>(
        val prev: K?,
        val list: List<V>
    )
}