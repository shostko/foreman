package by.shostko.statusprocessor.paging.itemkeyed

import by.shostko.statusprocessor.BaseStatusProcessor
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider
import com.uber.autodispose.autoDisposable
import io.reactivex.Scheduler
import io.reactivex.Single

@Suppress("unused")
abstract class RxItemKeyedDataSource<K, V>(
    statusProcessor: BaseStatusProcessor<*>,
    private val scheduler: Scheduler? = null
) : BaseItemKeyedDataSource<K, V>(statusProcessor) {

    private val scopeProvider by lazy { AndroidLifecycleScopeProvider.from(this) }

    final override fun onLoadInitial(params: LoadInitialParams<K>, callback: LoadInitialCallback<V>) {
        val single = onLoadInitial(params.requestedInitialKey, params.requestedLoadSize)
        if (scheduler == null) {
            onSuccessResult(single.blockingGet(), params, callback)
        } else {
            single.subscribeOn(scheduler)
                .observeOn(scheduler)
                .autoDisposable(scopeProvider)
                .subscribe({
                    onSuccessResult(it, params, callback)
                }, {
                    onFailedResultInitial(it, params, callback)
                })
        }
    }

    final override fun onLoadAfter(params: LoadParams<K>, callback: LoadCallback<V>) {
        val single = onLoadAfter(params.key, params.requestedLoadSize)
        if (scheduler == null) {
            onSuccessResult(single.blockingGet(), params, callback)
        } else {
            single.subscribeOn(scheduler)
                .observeOn(scheduler)
                .autoDisposable(scopeProvider)
                .subscribe({
                    onSuccessResult(it, params, callback)
                }, {
                    onFailedResultAfter(it, params, callback)
                })
        }
    }

    final override fun onLoadBefore(params: LoadParams<K>, callback: LoadCallback<V>) {
        val single = onLoadBefore(params.key, params.requestedLoadSize)
        if (scheduler == null) {
            onSuccessResult(single.blockingGet(), params, callback)
        } else {
            single.subscribeOn(scheduler)
                .observeOn(scheduler)
                .autoDisposable(scopeProvider)
                .subscribe({
                    onSuccessResult(it, params, callback)
                }, {
                    onFailedResultBefore(it, params, callback)
                })
        }
    }

    @Throws(Throwable::class)
    protected abstract fun onLoadInitial(key: K?, requestedLoadSize: Int): Single<List<V>>

    @Throws(Throwable::class)
    protected abstract fun onLoadAfter(key: K?, requestedLoadSize: Int): Single<List<V>>

    @Throws(Throwable::class)
    protected abstract fun onLoadBefore(key: K?, requestedLoadSize: Int): Single<List<V>>
}