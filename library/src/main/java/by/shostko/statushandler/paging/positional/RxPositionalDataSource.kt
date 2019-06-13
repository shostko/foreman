package by.shostko.statushandler.paging.positional

import by.shostko.statushandler.StatusProcessor
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider
import com.uber.autodispose.autoDisposable
import io.reactivex.Scheduler
import io.reactivex.Single

@Suppress("unused")
abstract class RxPositionalDataSource<V>(
    statusProcessor: StatusProcessor<*>,
    private val scheduler: Scheduler? = null
) : BasePositionalDataSource<V>(statusProcessor) {

    private val scopeProvider by lazy { AndroidLifecycleScopeProvider.from(this) }

    override fun onLoadInitial(params: LoadInitialParams, callback: LoadInitialCallback<V>) {
        val single = onLoad(params.requestedStartPosition, params.requestedLoadSize)
        if (scheduler == null) {
            onSuccessResult(single.blockingGet(), params, callback)
        } else {
            single.subscribeOn(scheduler)
                .observeOn(scheduler)
                .autoDisposable(scopeProvider)
                .subscribe({
                    onSuccessResult(it, params, callback)
                }, {
                    onFailedResult(it, params, callback)
                })
        }
    }

    override fun onLoadRange(params: LoadRangeParams, callback: LoadRangeCallback<V>) {
        val single = onLoad(params.startPosition, params.loadSize)
        if (scheduler == null) {
            onSuccessResult(single.blockingGet(), params, callback)
        } else {
            single.subscribeOn(scheduler)
                .observeOn(scheduler)
                .autoDisposable(scopeProvider)
                .subscribe({
                    onSuccessResult(it, params, callback)
                }, {
                    onFailedResult(it, params, callback)
                })
        }
    }

    @Throws(Throwable::class)
    protected abstract fun onLoad(startPosition: Int, loadSize: Int): Single<List<V>>
}