package by.shostko.statushandler.paging.pagekeyed

import by.shostko.statushandler.StatusHandler
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable

@Suppress("unused", "MemberVisibilityCanBePrivate")
abstract class RxPageKeyedDataSource<K, V>(
    statusHandler: StatusHandler<*>,
    protected val firstPageKey: K,
    private val scheduler: Scheduler? = null
) : BasePageKeyedDataSource<K, V>(statusHandler) {

    private val disposable = CompositeDisposable()

    init {
        addInvalidatedCallback { disposable.dispose() }
    }

    override fun onLoadInitial(params: LoadInitialParams<K>, callback: LoadInitialCallback<K, V>) {
        val single = onLoad(firstPageKey, params.requestedLoadSize)
        if (scheduler == null) {
            val result = single.blockingGet()
            val previousPageKey = prevKey(firstPageKey)
            val nextPageKey = nextKey(firstPageKey)
            onSuccessResultInitial(result, previousPageKey, nextPageKey, params, callback)
        } else {
            disposable.add(
                single.subscribeOn(scheduler)
                    .observeOn(scheduler)
                    .subscribe({
                        val previousPageKey = prevKey(firstPageKey)
                        val nextPageKey = nextKey(firstPageKey)
                        onSuccessResultInitial(it, previousPageKey, nextPageKey, params, callback)
                    }, {
                        onFailedResultInitial(it, params, callback)
                    })
            )
        }
    }

    override fun onLoadAfter(params: LoadParams<K>, callback: LoadCallback<K, V>) {
        val single = onLoad(params.key, params.requestedLoadSize)
        if (scheduler == null) {
            val result = single.blockingGet()
            val nextPageKey = nextKey(params.key)
            onSuccessResultAfter(result, nextPageKey, params, callback)
        } else {
            disposable.add(
                single.subscribeOn(scheduler)
                    .observeOn(scheduler)
                    .subscribe({
                        val nextPageKey = nextKey(params.key)
                        onSuccessResultAfter(it, nextPageKey, params, callback)
                    }, {
                        onFailedResultAfter(it, params, callback)
                    })
            )
        }
    }

    override fun onLoadBefore(params: LoadParams<K>, callback: LoadCallback<K, V>) {
        val single = onLoad(params.key, params.requestedLoadSize)
        if (scheduler == null) {
            val result = single.blockingGet()
            val previousPageKey = prevKey(params.key)
            onSuccessResultBefore(result, previousPageKey, params, callback)
        } else {
            disposable.add(
                single.subscribeOn(scheduler)
                    .observeOn(scheduler)
                    .subscribe({
                        val previousPageKey = prevKey(params.key)
                        onSuccessResultBefore(it, previousPageKey, params, callback)
                    }, {
                        onFailedResultBefore(it, params, callback)
                    })
            )
        }
    }

    protected abstract fun nextKey(key: K): K?

    protected abstract fun prevKey(key: K): K?

    @Throws(Throwable::class)
    protected abstract fun onLoad(key: K, requestedLoadSize: Int): Single<List<V>>
}