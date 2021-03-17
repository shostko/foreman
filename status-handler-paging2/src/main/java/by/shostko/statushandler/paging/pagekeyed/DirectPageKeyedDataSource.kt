package by.shostko.statushandler.paging.pagekeyed

import by.shostko.statushandler.StatusHandler

@Suppress("MemberVisibilityCanBePrivate", "unused", "CheckResult")
abstract class DirectPageKeyedDataSource<K, V>(
    statusHandlerCallback: StatusHandler.Callback
) : BasePageKeyedDataSource<K, V>(statusHandlerCallback) {

    final override fun onLoadInitial(params: LoadInitialParams<K>, callback: LoadInitialCallback<K, V>) {
        val (previousPageKey, list, nextPageKey) = onLoadInitial(params.requestedLoadSize)
        onSuccessResultInitial(list, previousPageKey, nextPageKey, params, callback)
    }

    final override fun onLoadAfter(params: LoadParams<K>, callback: LoadCallback<K, V>) {
        val (list, nextPageKey) = onLoadAfter(params.key, params.requestedLoadSize)
        onSuccessResultAfter(list, nextPageKey, params, callback)
    }

    final override fun onLoadBefore(params: LoadParams<K>, callback: LoadCallback<K, V>) {
        val (previousPageKey, list) = onLoadBefore(params.key, params.requestedLoadSize)
        onSuccessResultBefore(list, previousPageKey, params, callback)
    }

    @Throws(Throwable::class)
    protected abstract fun onLoadInitial(requestedLoadSize: Int): InitialResult<K, V>

    @Throws(Throwable::class)
    protected abstract fun onLoadAfter(key: K, requestedLoadSize: Int): AfterResult<K, V>

    @Throws(Throwable::class)
    protected abstract fun onLoadBefore(key: K, requestedLoadSize: Int): BeforeResult<K, V>

    protected fun result(prev: K?, list: List<V>, next: K?): InitialResult<K, V> = InitialResult(prev, list, next)

    protected fun result(list: List<V>, next: K?): AfterResult<K, V> = AfterResult(list, next)

    protected fun result(prev: K?, list: List<V>): BeforeResult<K, V> = BeforeResult(prev, list)

    protected data class InitialResult<K, V>(
        val prev: K?,
        val list: List<V>,
        val next: K?
    )

    protected data class AfterResult<K, V>(
        val list: List<V>,
        val next: K?
    )

    protected data class BeforeResult<K, V>(
        val prev: K?,
        val list: List<V>
    )
}