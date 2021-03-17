@file:Suppress("unused")

package by.shostko.statushandler.paging.pagekeyed

import by.shostko.statushandler.StatusHandler

abstract class AsyncPageKeyedDataSource<K, V>(
    statusHandlerCallback: StatusHandler.Callback
) : BasePageKeyedDataSource<K, V>(statusHandlerCallback) {

    final override fun onLoadInitial(params: LoadInitialParams<K>, callback: LoadInitialCallback<K, V>) {
        onLoadInitial(params.requestedLoadSize, object : CallbackInitial<K, V>() {
            override fun onSuccessResult(list: List<V>, previousPageKey: K, nextPageKey: K) {
                onSuccessResultInitial(list, previousPageKey, nextPageKey, params, callback)
            }

            override fun onFailedResult(e: Throwable) {
                onFailedResultInitial(e, params, callback)
            }
        })
    }

    final override fun onLoadAfter(params: LoadParams<K>, callback: LoadCallback<K, V>) {
        onLoadAfter(params.key, params.requestedLoadSize, object : CallbackAfter<K, V>() {
            override fun onSuccessResult(list: List<V>, nextPageKey: K) {
                onSuccessResultAfter(list, nextPageKey, params, callback)
            }

            override fun onFailedResult(e: Throwable) {
                onFailedResultAfter(e, params, callback)
            }
        })
    }

    final override fun onLoadBefore(params: LoadParams<K>, callback: LoadCallback<K, V>) {
        onLoadBefore(params.key, params.requestedLoadSize, object : CallbackBefore<K, V>() {
            override fun onSuccessResult(list: List<V>, previousPageKey: K) {
                onSuccessResultBefore(list, previousPageKey, params, callback)
            }

            override fun onFailedResult(e: Throwable) {
                onFailedResultBefore(e, params, callback)
            }
        })
    }

    @Throws(Throwable::class)
    protected abstract fun onLoadInitial(requestedLoadSize: Int, callback: CallbackInitial<K, V>)

    @Throws(Throwable::class)
    protected abstract fun onLoadAfter(key: K, requestedLoadSize: Int, callback: CallbackAfter<K, V>)

    @Throws(Throwable::class)
    protected abstract fun onLoadBefore(key: K, requestedLoadSize: Int, callback: CallbackBefore<K, V>)

    protected abstract class CallbackInitial<K, V> {
        abstract fun onSuccessResult(list: List<V>, previousPageKey: K, nextPageKey: K)
        abstract fun onFailedResult(e: Throwable)
    }

    protected abstract class CallbackAfter<K, V> {
        abstract fun onSuccessResult(list: List<V>, nextPageKey: K)
        abstract fun onFailedResult(e: Throwable)
    }

    protected abstract class CallbackBefore<K, V> {
        abstract fun onSuccessResult(list: List<V>, previousPageKey: K)
        abstract fun onFailedResult(e: Throwable)
    }
}