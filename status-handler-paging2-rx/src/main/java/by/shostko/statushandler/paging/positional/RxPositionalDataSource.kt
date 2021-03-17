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

    final override fun onLoadInitial(params: LoadInitialParams, callback: LoadInitialCallback<V>) {
        val single = onLoadInitial(params.requestedStartPosition, params.requestedLoadSize)
        if (scheduler == null) {
            val (list, frontPosition, total) = single.blockingGetWithoutWrap()
            if (total == null) {
                onSuccessResult(list, frontPosition, params, callback)
            } else {
                onSuccessResult(list, frontPosition, total, params, callback)
            }
        } else {
            disposable.add(
                single.subscribeOn(scheduler)
                    .observeOn(scheduler)
                    .subscribe({ (list, frontPosition, total) ->
                        if (total == null) {
                            onSuccessResult(list, frontPosition, params, callback)
                        } else {
                            onSuccessResult(list, frontPosition, total, params, callback)
                        }
                    }, {
                        onFailedResult(it, params, callback)
                    })
            )
        }
    }

    final override fun onLoadRange(params: LoadRangeParams, callback: LoadRangeCallback<V>) {
        val single = onLoadRange(params.startPosition, params.loadSize)
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
    protected abstract fun onLoadInitial(startPosition: Int, loadSize: Int): Single<InitialResult<V>>

    @Throws(Throwable::class)
    protected abstract fun onLoadRange(startPosition: Int, loadSize: Int): Single<List<V>>

    protected fun result(list: List<V>, frontPosition: Int, total: Int): InitialResult<V> = InitialResult(list, frontPosition, total)

    protected data class InitialResult<V>(
        val list: List<V>,
        val frontPosition: Int,
        val total: Int? = null
    )
}