package by.shostko.statusprocessor.paging.itemkeyed

import by.shostko.statusprocessor.LoadingStatus
import by.shostko.statusprocessor.StatusProcessor
import by.shostko.statusprocessor.paging.positional.BasePositionalDataSource
import timber.log.Timber

@Suppress("unused")
abstract class SimpleItemKeyedDataSource<K, V>(
    statusProcessor: StatusProcessor
) : BasePositionalDataSource<K, V>(statusProcessor) {

    override fun loadInitial(params: LoadInitialParams<K>, callback: LoadInitialCallback<V>) {
        val key = params.requestedInitialKey
        Timber.tag(tag).d("loadInitial with key %s", key)
        statusProcessor.update(LoadingStatus.loading())
        val list = load(key)
        if (list != null) {
            val nextKey = next(list)
            if (nextKey == null) {
                statusProcessor.update(LoadingStatus.success())
            } else {
                statusProcessor.update(LoadingStatus.loadingForward())
            }
        }
        onResult(list, params, callback)
    }

    override fun loadAfter(params: LoadParams<K>, callback: LoadCallback<V>) {
        val key = getNextKey(params.key)
        Timber.tag(tag).d("loadAfter with key %s", key)
        statusProcessor.update(LoadingStatus.loadingForward())
        val list = load(key)
        if (list != null) {
            val nextKey = next(list)
            if (nextKey == null) {
                statusProcessor.update(LoadingStatus.success())
            } else {
                statusProcessor.update(LoadingStatus.loadingForward())
            }
        }
        onResultAfter(list, params, callback)
    }

    override fun loadBefore(params: LoadParams<K>, callback: LoadCallback<V>) {
        val key = getPrevKey(params.key)
        Timber.tag(tag).d("loadBefore with key %s", key)
        statusProcessor.update(LoadingStatus.loadingBackward())
        val list = load(key)
        if (list != null) {
            val prevKey = prev(list)
            if (prevKey == null) {
                statusProcessor.update(LoadingStatus.success())
            } else {
                statusProcessor.update(LoadingStatus.loadingBackward())
            }
        }
        onResultBefore(list, params, callback)
    }

    private fun load(key: K?): List<V>? {
        return try {
            loadForKey(key)
        } catch (e: Throwable) {
            Timber.tag(tag).e(e, "Error during loading values list for key %s", key)
            statusProcessor.update(LoadingStatus.error(e))
            null
        }
    }

    private fun next(items: List<V>): K? = if (items.isEmpty()) null else getNextKey(getKey(items.last()))

    private fun prev(items: List<V>): K? = if (items.isEmpty()) null else getPrevKey(getKey(items.first()))

    protected abstract fun getNextKey(key: K): K?

    protected abstract fun getPrevKey(key: K): K?

    @Throws(Throwable::class)
    protected abstract fun loadForKey(key: K?): List<V>

}