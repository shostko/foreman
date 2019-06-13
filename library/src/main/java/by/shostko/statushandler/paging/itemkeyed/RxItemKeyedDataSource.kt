package by.shostko.statushandler.paging.itemkeyed

import by.shostko.statushandler.StatusHandler
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable

@Suppress("unused")
abstract class RxItemKeyedDataSource<K, V>(
    statusHandler: StatusHandler<*>,
    private val scheduler: Scheduler? = null
) : BaseItemKeyedDataSource<K, V>(statusHandler) {

    private val disposable = CompositeDisposable()

    init {
        addInvalidatedCallback { disposable.dispose() }
    }

    final override fun onLoadInitial(params: LoadInitialParams<K>, callback: LoadInitialCallback<V>) {
        val single = onLoadInitial(params.requestedInitialKey, params.requestedLoadSize)
        if (scheduler == null) {
            onSuccessResult(single.blockingGet(), params, callback)
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
            onSuccessResult(single.blockingGet(), params, callback)
        } else {
            disposable.add(
                single.subscribeOn(scheduler)
                    .observeOn(scheduler)
                    .subscribe({
                        onSuccessResult(it, params, callback)
                    }, {
                        onFailedResultAfter(it, params, callback)
                    })
            )
        }
    }

    final override fun onLoadBefore(params: LoadParams<K>, callback: LoadCallback<V>) {
        val single = onLoadBefore(params.key, params.requestedLoadSize)
        if (scheduler == null) {
            onSuccessResult(single.blockingGet(), params, callback)
        } else {
            disposable.add(
                single.subscribeOn(scheduler)
                    .observeOn(scheduler)
                    .subscribe({
                        onSuccessResult(it, params, callback)
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