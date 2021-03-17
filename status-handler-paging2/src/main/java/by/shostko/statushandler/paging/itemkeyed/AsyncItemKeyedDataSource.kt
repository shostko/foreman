package by.shostko.statushandler.paging.itemkeyed

import by.shostko.statushandler.StatusHandler

@Suppress("unused")
abstract class AsyncItemKeyedDataSource<K, V>(
    statusHandlerCallback: StatusHandler.Callback
) : BaseItemKeyedDataSource<K, V>(statusHandlerCallback) {

    final override fun onLoadInitial(params: LoadInitialParams<K>, callback: LoadInitialCallback<V>) {
        onLoadInitial(params.requestedInitialKey, params.requestedLoadSize, object : CallbackInitial<V>() {
            override fun onSuccessResult(list: List<V>, position: Int, total: Int) {
                onSuccessResultInitial(list, position, total, params, callback)
            }

            override fun onSuccessResult(list: List<V>) {
                onSuccessResultInitial(list, params, callback)
            }

            override fun onFailedResult(e: Throwable) {
                onFailedResultInitial(e, params, callback)
            }
        })
    }

    final override fun onLoadAfter(params: LoadParams<K>, callback: LoadCallback<V>) {
        onLoadAfter(params.key, params.requestedLoadSize, object : Callback<V>() {
            override fun onSuccessResult(list: List<V>) {
                onSuccessResultAfter(list, params, callback)
            }

            override fun onFailedResult(e: Throwable) {
                onFailedResultAfter(e, params, callback)
            }
        })
    }

    final override fun onLoadBefore(params: LoadParams<K>, callback: LoadCallback<V>) {
        onLoadBefore(params.key, params.requestedLoadSize, object : Callback<V>() {
            override fun onSuccessResult(list: List<V>) {
                onSuccessResultBefore(list, params, callback)
            }

            override fun onFailedResult(e: Throwable) {
                onFailedResultBefore(e, params, callback)
            }
        })
    }

    @Throws(Throwable::class)
    protected abstract fun onLoadInitial(key: K?, requestedLoadSize: Int, callback: CallbackInitial<V>)

    @Throws(Throwable::class)
    protected abstract fun onLoadAfter(key: K, requestedLoadSize: Int, callback: Callback<V>)

    @Throws(Throwable::class)
    protected abstract fun onLoadBefore(key: K, requestedLoadSize: Int, callback: Callback<V>)

    protected abstract class Callback<V> {
        abstract fun onSuccessResult(list: List<V>)
        abstract fun onFailedResult(e: Throwable)
    }

    protected abstract class CallbackInitial<V> : Callback<V>() {
        abstract fun onSuccessResult(list: List<V>, position: Int, total: Int)
    }
}