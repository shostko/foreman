package by.shostko.statushandler.paging.itemkeyed

import by.shostko.statushandler.StatusProcessor

@Suppress("unused")
abstract class AsyncItemKeyedDataSource<K, V>(
    statusProcessor: StatusProcessor<*>
) : BaseItemKeyedDataSource<K, V>(statusProcessor) {

    final override fun onLoadInitial(params: LoadInitialParams<K>, callback: LoadInitialCallback<V>) {
        onLoadInitial(params.requestedInitialKey, params.requestedLoadSize, CallbackImpl({
            onSuccessResult(it, params, callback)
        }, {
            onFailedResultInitial(it, params, callback)
        }))
    }

    final override fun onLoadAfter(params: LoadParams<K>, callback: LoadCallback<V>) {
        onLoadAfter(params.key, params.requestedLoadSize, CallbackImpl({
            onSuccessResult(it, params, callback)
        }, {
            onFailedResultAfter(it, params, callback)
        }))
    }

    final override fun onLoadBefore(params: LoadParams<K>, callback: LoadCallback<V>) {
        onLoadBefore(params.key, params.requestedLoadSize, CallbackImpl({
            onSuccessResult(it, params, callback)
        }, {
            onFailedResultBefore(it, params, callback)
        }))
    }

    @Throws(Throwable::class)
    protected abstract fun onLoadInitial(key: K?, requestedLoadSize: Int, callback: Callback<V>)

    @Throws(Throwable::class)
    protected abstract fun onLoadAfter(key: K, requestedLoadSize: Int, callback: Callback<V>)

    @Throws(Throwable::class)
    protected abstract fun onLoadBefore(key: K, requestedLoadSize: Int, callback: Callback<V>)

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