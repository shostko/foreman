package by.shostko.statusprocessor.paging.itemkeyed

import by.shostko.statusprocessor.StatusProcessor

@Suppress("unused")
abstract class SimpleItemKeyedDataSource<K, V>(
    statusProcessor: StatusProcessor
) : BaseItemKeyedDataSource<K, V>(statusProcessor) {

    override fun onLoadInitial(params: LoadInitialParams<K>, callback: LoadInitialCallback<V>) {
        val list = onLoad(params.requestedInitialKey, params.requestedLoadSize)
        onSuccessResult(list, params, callback)
    }

    override fun onLoadAfter(params: LoadParams<K>, callback: LoadCallback<V>) {
        val key = nextKey(params.key)
        val list = onLoad(key, params.requestedLoadSize)
        onSuccessResult(list, params, callback)
    }

    override fun onLoadBefore(params: LoadParams<K>, callback: LoadCallback<V>) {
        val key = prevKey(params.key)
        val list = onLoad(key, params.requestedLoadSize)
        onSuccessResult(list, params, callback)
    }

    protected abstract fun nextKey(key: K): K?

    protected abstract fun prevKey(key: K): K?

    @Throws(Throwable::class)
    protected abstract fun onLoad(key: K?, requestedLoadSize: Int): List<V>
}