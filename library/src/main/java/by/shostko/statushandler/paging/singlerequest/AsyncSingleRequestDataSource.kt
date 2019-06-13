package by.shostko.statushandler.paging.singlerequest

import by.shostko.statushandler.StatusHandler
import by.shostko.statushandler.paging.pagekeyed.BasePageKeyedDataSource

@Suppress("unused")
abstract class AsyncSingleRequestDataSource<V>(statusHandler: StatusHandler<*>) :
    BasePageKeyedDataSource<Int, V>(statusHandler) {

    override fun onLoadInitial(params: LoadInitialParams<Int>, callback: LoadInitialCallback<Int, V>) {
        onLoad(CallbackImpl({
            onSuccessResultInitial(it, null, null, params, callback)
        }, {
            onFailedResultInitial(it, params, callback)
        }))
    }

    override fun onLoadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, V>) {
        onSuccessResultAfter(emptyList(), null, params, callback)
    }

    override fun onLoadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, V>) {
        onSuccessResultBefore(emptyList(), null, params, callback)
    }

    @Throws(Throwable::class)
    protected abstract fun onLoad(callback: Callback<V>)

    protected abstract class Callback<V> {
        abstract fun onSuccessResult(list: List<V>)
        abstract fun onFailedResult(e: Throwable)
    }

    private class CallbackImpl<V>(
        private val successFun: ((List<V>) -> Any),
        private val failedFun: ((Throwable) -> Any)
    ) : Callback<V>() {

        override fun onSuccessResult(list: List<V>) {
            successFun.invoke(list)
        }

        override fun onFailedResult(e: Throwable) {
            failedFun.invoke(e)
        }
    }
}