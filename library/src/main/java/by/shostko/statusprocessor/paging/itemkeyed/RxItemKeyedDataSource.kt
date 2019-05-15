package by.shostko.statusprocessor.paging.itemkeyed

import by.shostko.statusprocessor.StatusProcessor
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider
import com.uber.autodispose.autoDisposable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

@Suppress("unused")
abstract class RxItemKeyedDataSource<K, V>(
    statusProcessor: StatusProcessor,
    private val scheduler: Scheduler = Schedulers.io()
    ) : BaseItemKeyedDataSource<K, V>(statusProcessor) {

    private val scopeProvider by lazy { AndroidLifecycleScopeProvider.from(this) }

    override fun onLoadInitial(params: LoadInitialParams<K>, callback: LoadInitialCallback<V>) {
        onLoad(params.requestedInitialKey, params.requestedLoadSize)
            .subscribeOn(scheduler)
            .observeOn(scheduler)
            .autoDisposable(scopeProvider)
            .subscribe({
                onSuccessResult(it, params, callback)
            }, {
                onFailedResultInitial(it, params, callback)
            })
    }

    override fun onLoadAfter(params: LoadParams<K>, callback: LoadCallback<V>) {
        onLoad(params.key, params.requestedLoadSize)
            .subscribeOn(scheduler)
            .observeOn(scheduler)
            .autoDisposable(scopeProvider)
            .subscribe({
                onSuccessResult(it, params, callback)
            }, {
                onFailedResultAfter(it, params, callback)
            })
    }

    override fun onLoadBefore(params: LoadParams<K>, callback: LoadCallback<V>) {
        onLoad(params.key, params.requestedLoadSize)
            .subscribeOn(scheduler)
            .observeOn(scheduler)
            .autoDisposable(scopeProvider)
            .subscribe({
                onSuccessResult(it, params, callback)
            }, {
                onFailedResultBefore(it, params, callback)
            })
    }

    @Throws(Throwable::class)
    protected abstract fun onLoad(key: K?, requestedLoadSize: Int): Single<List<V>>
}