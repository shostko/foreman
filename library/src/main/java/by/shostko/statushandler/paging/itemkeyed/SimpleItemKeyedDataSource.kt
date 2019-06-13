package by.shostko.statushandler.paging.itemkeyed

import by.shostko.statushandler.StatusHandler

@Suppress("unused")
abstract class SimpleItemKeyedDataSource<K, V>(
    statusHandler: StatusHandler<*>
) : BaseItemKeyedDataSource<K, V>(statusHandler) {

    final override fun onLoadInitial(params: LoadInitialParams<K>, callback: LoadInitialCallback<V>) {
        val list = onLoadInitial(params.requestedInitialKey, params.requestedLoadSize)
        onSuccessResult(list, params, callback)
    }

    final override fun onLoadAfter(params: LoadParams<K>, callback: LoadCallback<V>) {
        val list = onLoadAfter(params.key, params.requestedLoadSize)
        onSuccessResult(list, params, callback)
    }

    final override fun onLoadBefore(params: LoadParams<K>, callback: LoadCallback<V>) {
        val list = onLoadBefore(params.key, params.requestedLoadSize)
        onSuccessResult(list, params, callback)
    }

    @Throws(Throwable::class)
    protected abstract fun onLoadInitial(key: K?, requestedLoadSize: Int): List<V>

    @Throws(Throwable::class)
    protected abstract fun onLoadAfter(key: K, requestedLoadSize: Int): List<V>

    @Throws(Throwable::class)
    protected abstract fun onLoadBefore(key: K, requestedLoadSize: Int): List<V>
}