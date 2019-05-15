package by.shostko.statusprocessor.paging.itemkeyed

import by.shostko.statusprocessor.StatusProcessor

@Suppress("unused")
abstract class AsyncItemKeyedDataSource<K, V>(
    statusProcessor: StatusProcessor
) : BaseItemKeyedDataSource<K, V>(statusProcessor) {

    override fun onLoadInitial(params: LoadInitialParams<K>, callback: LoadInitialCallback<V>) {
        onLoad(params.requestedInitialKey, params.requestedLoadSize, CallbackImpl({
            onSuccessResult(it, params, callback)
        }, {
            onFailedResultInitial(it, params, callback)
        }))
    }

    override fun onLoadAfter(params: LoadParams<K>, callback: LoadCallback<V>) {
        val key = nextKey(params.key)
        onLoad(key, params.requestedLoadSize, CallbackImpl({
            onSuccessResult(it, params, callback)
        }, {
            onFailedResultAfter(it, params, callback)
        }))
    }

    override fun onLoadBefore(params: LoadParams<K>, callback: LoadCallback<V>) {
        val key = prevKey(params.key)
        onLoad(key, params.requestedLoadSize, CallbackImpl({
            onSuccessResult(it, params, callback)
        }, {
            onFailedResultBefore(it, params, callback)
        }))
    }

    protected abstract fun nextKey(key: K): K?

    protected abstract fun prevKey(key: K): K?

    @Throws(Throwable::class)
    protected abstract fun onLoad(key: K?, requestedLoadSize: Int, callback: Callback<V>)

    companion object {

        abstract class Callback<V> {
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
}