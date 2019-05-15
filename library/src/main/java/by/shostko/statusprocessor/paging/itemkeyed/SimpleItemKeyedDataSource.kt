package by.shostko.statusprocessor.paging.itemkeyed

import by.shostko.statusprocessor.BaseStatusProcessor

@Suppress("unused")
abstract class SimpleItemKeyedDataSource<K, V>(
    statusProcessor: BaseStatusProcessor<*>
) : BaseItemKeyedDataSource<K, V>(statusProcessor) {

    override fun onLoadInitial(params: LoadInitialParams<K>, callback: LoadInitialCallback<V>) {
        val list = onLoad(params.requestedInitialKey, params.requestedLoadSize)
        onSuccessResult(list, params, callback)
    }

    override fun onLoadAfter(params: LoadParams<K>, callback: LoadCallback<V>) {
        val list = onLoad(params.key, params.requestedLoadSize)
        onSuccessResult(list, params, callback)
    }

    override fun onLoadBefore(params: LoadParams<K>, callback: LoadCallback<V>) {
        val list = onLoad(params.key, params.requestedLoadSize)
        onSuccessResult(list, params, callback)
    }

    @Throws(Throwable::class)
    protected abstract fun onLoad(key: K?, requestedLoadSize: Int): List<V>
}