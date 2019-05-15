package by.shostko.statusprocessor.paging.positional

import by.shostko.statusprocessor.BaseStatusProcessor
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider
import com.uber.autodispose.autoDisposable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

@Suppress("unused")
abstract class RxPositionalDataSource<V>(
    statusProcessor: BaseStatusProcessor<*>,
    private val scheduler: Scheduler = Schedulers.io()
) : BasePositionalDataSource<V>(statusProcessor) {

    private val scopeProvider by lazy { AndroidLifecycleScopeProvider.from(this) }

    override fun onLoadInitial(params: LoadInitialParams, callback: LoadInitialCallback<V>) {
        onLoad(params.requestedStartPosition, params.requestedLoadSize)
            .subscribeOn(scheduler)
            .observeOn(scheduler)
            .autoDisposable(scopeProvider)
            .subscribe({
                onSuccessResult(it, params, callback)
            }, {
                onFailedResult(it, params, callback)
            })
    }

    override fun onLoadRange(params: LoadRangeParams, callback: LoadRangeCallback<V>) {
        onLoad(params.startPosition, params.loadSize)
            .subscribeOn(scheduler)
            .observeOn(scheduler)
            .autoDisposable(scopeProvider)
            .subscribe({
                onSuccessResult(it, params, callback)
            }, {
                onFailedResult(it, params, callback)
            })
    }

    @Throws(Throwable::class)
    protected abstract fun onLoad(startPosition: Int, loadSize: Int): Single<List<V>>
}