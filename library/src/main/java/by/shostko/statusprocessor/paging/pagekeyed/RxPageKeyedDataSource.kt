package by.shostko.statusprocessor.paging.pagekeyed

import by.shostko.statusprocessor.BaseStatusProcessor
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider
import com.uber.autodispose.autoDisposable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

@Suppress("MemberVisibilityCanBePrivate", "unused", "CheckResult")
abstract class RxPageKeyedDataSource<K, V>(
    statusProcessor: BaseStatusProcessor<*>,
    private val scheduler: Scheduler = Schedulers.io(),
    protected val firstPageKey: K
) : BasePageKeyedDataSource<K, V>(statusProcessor) {

    private val scopeProvider by lazy { AndroidLifecycleScopeProvider.from(this) }

    override fun onLoadInitial(params: LoadInitialParams<K>, callback: LoadInitialCallback<K, V>) {
        onLoad(firstPageKey, params.requestedLoadSize)
            .subscribeOn(scheduler)
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

    override fun onLoadAfter(params: LoadParams<K>, callback: LoadCallback<K, V>) {
        onLoad(params.key, params.requestedLoadSize)
            .subscribeOn(scheduler)
            .observeOn(scheduler)
            .autoDisposable(scopeProvider)
            .subscribe({
                val nextPageKey = nextKey(firstPageKey)
                onSuccessResultAfter(it, nextPageKey, params, callback)
            }, {
                onFailedResultAfter(it, params, callback)
            })
    }

    override fun onLoadBefore(params: LoadParams<K>, callback: LoadCallback<K, V>) {
        onLoad(params.key, params.requestedLoadSize)
            .subscribeOn(scheduler)
            .observeOn(scheduler)
            .autoDisposable(scopeProvider)
            .subscribe({
                val previousPageKey = prevKey(firstPageKey)
                onSuccessResultBefore(it, previousPageKey, params, callback)
            }, {
                onFailedResultBefore(it, params, callback)
            })
    }

    protected abstract fun nextKey(key: K): K?

    protected abstract fun prevKey(key: K): K?

    @Throws(Throwable::class)
    protected abstract fun onLoad(key: K, requestedLoadSize: Int): Single<List<V>>
}