package by.shostko.statusprocessor.paging.pagekeyed

import by.shostko.statusprocessor.StatusProcessor
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider
import com.uber.autodispose.autoDisposable
import io.reactivex.Scheduler
import io.reactivex.Single

@Suppress("MemberVisibilityCanBePrivate", "unused", "CheckResult")
abstract class RxPageKeyedDataSource<K, V>(
    statusProcessor: StatusProcessor<*>,
    protected val firstPageKey: K,
    private val scheduler: Scheduler? = null
) : BasePageKeyedDataSource<K, V>(statusProcessor) {

    private val scopeProvider by lazy { AndroidLifecycleScopeProvider.from(this) }

    override fun onLoadInitial(params: LoadInitialParams<K>, callback: LoadInitialCallback<K, V>) {
        val single = onLoad(firstPageKey, params.requestedLoadSize)
        if (scheduler == null) {
            val result = single.blockingGet()
            val previousPageKey = prevKey(firstPageKey)
            val nextPageKey = nextKey(firstPageKey)
            onSuccessResultInitial(result, previousPageKey, nextPageKey, params, callback)
        } else {
            single.subscribeOn(scheduler)
                .observeOn(scheduler)
                .autoDisposable(scopeProvider)
                .subscribe({
                    val previousPageKey = prevKey(firstPageKey)
                    val nextPageKey = nextKey(firstPageKey)
                    onSuccessResultInitial(it, previousPageKey, nextPageKey, params, callback)
                }, {
                    onFailedResultInitial(it, params, callback)
                })
        }
    }

    override fun onLoadAfter(params: LoadParams<K>, callback: LoadCallback<K, V>) {
        val single = onLoad(params.key, params.requestedLoadSize)
        if (scheduler == null) {
            val result = single.blockingGet()
            val nextPageKey = nextKey(params.key)
            onSuccessResultAfter(result, nextPageKey, params, callback)
        } else {
            single.subscribeOn(scheduler)
                .observeOn(scheduler)
                .autoDisposable(scopeProvider)
                .subscribe({
                    val nextPageKey = nextKey(params.key)
                    onSuccessResultAfter(it, nextPageKey, params, callback)
                }, {
                    onFailedResultAfter(it, params, callback)
                })
        }
    }

    override fun onLoadBefore(params: LoadParams<K>, callback: LoadCallback<K, V>) {
        val single = onLoad(params.key, params.requestedLoadSize)
        if (scheduler == null) {
            val result = single.blockingGet()
            val previousPageKey = prevKey(params.key)
            onSuccessResultBefore(result, previousPageKey, params, callback)
        } else {
            single.subscribeOn(scheduler)
                .observeOn(scheduler)
                .autoDisposable(scopeProvider)
                .subscribe({
                    val previousPageKey = prevKey(params.key)
                    onSuccessResultBefore(it, previousPageKey, params, callback)
                }, {
                    onFailedResultBefore(it, params, callback)
                })
        }
    }

    protected abstract fun nextKey(key: K): K?

    protected abstract fun prevKey(key: K): K?

    @Throws(Throwable::class)
    protected abstract fun onLoad(key: K, requestedLoadSize: Int): Single<List<V>>
}