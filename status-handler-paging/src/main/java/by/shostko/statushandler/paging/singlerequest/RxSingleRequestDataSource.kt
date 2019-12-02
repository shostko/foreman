package by.shostko.statushandler.paging.singlerequest

import by.shostko.statushandler.StatusHandler
import by.shostko.statushandler.paging.blockingGetWithoutWrap
import by.shostko.statushandler.paging.pagekeyed.BasePageKeyedDataSource
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable

@Suppress("unused")
abstract class RxSingleRequestDataSource<V>(
    statusHandler: StatusHandler<*>,
    private val scheduler: Scheduler? = null
) : BasePageKeyedDataSource<Int, V>(statusHandler) {

    private val disposable = CompositeDisposable()

    init {
        addInvalidatedCallback { disposable.dispose() }
    }

    override fun onLoadInitial(params: LoadInitialParams<Int>, callback: LoadInitialCallback<Int, V>) {
        val single = onLoad()
        if (scheduler == null) {
            onSuccessResultInitial(single.blockingGetWithoutWrap(), null, null, params, callback)
        } else {
            disposable.add(
                single.subscribeOn(scheduler)
                    .observeOn(scheduler)
                    .subscribe({
                        onSuccessResultInitial(it, null, null, params, callback)
                    }, {
                        onFailedResultInitial(it, params, callback)
                    })
            )
        }
    }

    override fun onLoadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, V>) {
        onSuccessResultAfter(emptyList(), null, params, callback)
    }

    override fun onLoadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, V>) {
        onSuccessResultBefore(emptyList(), null, params, callback)
    }

    @Throws(Throwable::class)
    protected abstract fun onLoad(): Single<List<V>>
}