@file:Suppress("unused")

package by.shostko.statushandler.paging.itemkeyed

import by.shostko.statushandler.StatusHandler

abstract class DirectItemKeyedDataSource<K, V>(
    statusHandlerCallback: StatusHandler.Callback
) : BaseItemKeyedDataSource<K, V>(statusHandlerCallback) {

    final override fun onLoadInitial(params: LoadInitialParams<K>, callback: LoadInitialCallback<V>) {
        val (list, position, total) = onLoadInitial(params.requestedInitialKey, params.requestedLoadSize)
        onSuccessResultInitial(list, position, total, params, callback)
    }

    final override fun onLoadAfter(params: LoadParams<K>, callback: LoadCallback<V>) {
        val list = onLoadAfter(params.key, params.requestedLoadSize)
        onSuccessResultAfter(list, params, callback)
    }

    final override fun onLoadBefore(params: LoadParams<K>, callback: LoadCallback<V>) {
        val list = onLoadBefore(params.key, params.requestedLoadSize)
        onSuccessResultBefore(list, params, callback)
    }

    @Throws(Throwable::class)
    protected abstract fun onLoadInitial(key: K?, requestedLoadSize: Int): InitialResult<V>

    @Throws(Throwable::class)
    protected abstract fun onLoadAfter(key: K, requestedLoadSize: Int): List<V>

    @Throws(Throwable::class)
    protected abstract fun onLoadBefore(key: K, requestedLoadSize: Int): List<V>

    protected fun result(list: List<V>, position: Int, total: Int): InitialResult<V> = InitialResult(list, position, total)

    protected data class InitialResult<V>(
        val list: List<V>,
        val position: Int,
        val total: Int
    )
}