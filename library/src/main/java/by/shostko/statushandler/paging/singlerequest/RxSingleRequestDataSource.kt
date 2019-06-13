package by.shostko.statushandler.paging.singlerequest

import by.shostko.statushandler.StatusProcessor
import by.shostko.statushandler.paging.pagekeyed.BasePageKeyedDataSource
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider
import com.uber.autodispose.autoDisposable
import io.reactivex.Scheduler
import io.reactivex.Single

@Suppress("unused")
abstract class RxSingleRequestDataSource<V>(
    statusProcessor: StatusProcessor<*>,
    private val scheduler: Scheduler? = null
) : BasePageKeyedDataSource<Int, V>(statusProcessor) {

    private val scopeProvider by lazy { AndroidLifecycleScopeProvider.from(this) }

    override fun onLoadInitial(params: LoadInitialParams<Int>, callback: LoadInitialCallback<Int, V>) {
        val single = onLoad()
        if (scheduler == null) {
            onSuccessResultInitial(single.blockingGet(), null, null, params, callback)
        } else {
            single.subscribeOn(scheduler)
                .observeOn(scheduler)
                .autoDisposable(scopeProvider)
                .subscribe({
                    onSuccessResultInitial(it, null, null, params, callback)
                }, {
                    onFailedResultInitial(it, params, callback)
                })
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