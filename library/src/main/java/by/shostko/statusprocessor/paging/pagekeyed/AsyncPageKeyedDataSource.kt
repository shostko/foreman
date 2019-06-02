package by.shostko.statusprocessor.paging.pagekeyed

import by.shostko.statusprocessor.BaseStatusProcessor

@Suppress("MemberVisibilityCanBePrivate", "unused", "CheckResult")
abstract class AsyncPageKeyedDataSource<K, V>(
    statusProcessor: BaseStatusProcessor<*>,
    protected val firstPageKey: K
) : BasePageKeyedDataSource<K, V>(statusProcessor) {

    override fun onLoadInitial(params: LoadInitialParams<K>, callback: LoadInitialCallback<K, V>) {
        onLoad(firstPageKey, params.requestedLoadSize, CallbackImpl({
            val previousPageKey = prevKey(firstPageKey)
            val nextPageKey = nextKey(firstPageKey)
            onSuccessResultInitial(it, previousPageKey, nextPageKey, params, callback)
        }, {
            onFailedResultInitial(it, params, callback)
        }))
    }

    override fun onLoadAfter(params: LoadParams<K>, callback: LoadCallback<K, V>) {
        onLoad(params.key, params.requestedLoadSize, CallbackImpl({
            val nextPageKey = nextKey(params.key)
            onSuccessResultAfter(it, nextPageKey, params, callback)
        }, {
            onFailedResultAfter(it, params, callback)
        }))
    }

    override fun onLoadBefore(params: LoadParams<K>, callback: LoadCallback<K, V>) {
        onLoad(params.key, params.requestedLoadSize, CallbackImpl({
            val previousPageKey = prevKey(params.key)
            onSuccessResultBefore(it, previousPageKey, params, callback)
        }, {
            onFailedResultBefore(it, params, callback)
        }))
    }

    protected abstract fun nextKey(key: K): K?

    protected abstract fun prevKey(key: K): K?

    @Throws(Throwable::class)
    protected abstract fun onLoad(key: K, requestedLoadSize: Int, callback: Callback<V>)

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