package by.shostko.statushandler.paging.positional

import by.shostko.statushandler.StatusHandler
import by.shostko.statushandler.paging.blockingGetWithoutWrap
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable

@Suppress("unused")
abstract class RxPositionalDataSource<V>(
    statusHandlerCallback: StatusHandler.Callback,
    private val scheduler: Scheduler? = null
) : BasePositionalDataSource<V>(statusHandlerCallback) {

    private val disposableDelegate = lazy { CompositeDisposable() }
    private val disposable: CompositeDisposable
        get() {
            if (!disposableDelegate.isInitialized()) {
                addInvalidatedCallback { disposableDelegate.value.dispose() }
            }
            return disposableDelegate.value
        }

    override fun onLoadInitial(params: LoadInitialParams, callback: LoadInitialCallback<V>) {
        val single = onLoad(params.requestedStartPosition, params.requestedLoadSize)
        if (scheduler == null) {
            onSuccessResult(single.blockingGetWithoutWrap(), params, callback)
        } else {
            disposable.add(
                single.subscribeOn(scheduler)
                    .observeOn(scheduler)
                    .subscribe({
                        onSuccessResult(it, params, callback)
                    }, {
                        onFailedResult(it, params, callback)
                    })
            )
        }
    }

    override fun onLoadRange(params: LoadRangeParams, callback: LoadRangeCallback<V>) {
        val single = onLoad(params.startPosition, params.loadSize)
        if (scheduler == null) {
            onSuccessResult(single.blockingGetWithoutWrap(), params, callback)
        } else {
            disposable.add(
                single.subscribeOn(scheduler)
                    .observeOn(scheduler)
                    .subscribe({
                        onSuccessResult(it, params, callback)
                    }, {
                        onFailedResult(it, params, callback)
                    })
            )
        }
    }

    @Throws(Throwable::class)
    protected abstract fun onLoad(startPosition: Int, loadSize: Int): Single<List<V>>
}