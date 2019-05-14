package by.shostko.statusprocessor.paging.pagekeyed

import by.shostko.statusprocessor.LoadingStatus
import by.shostko.statusprocessor.StatusProcessor
import timber.log.Timber

@Suppress("MemberVisibilityCanBePrivate", "unused", "CheckResult")
abstract class SimplePageKeyedDataSource<K, V>(
    statusProcessor: StatusProcessor,
    protected val firstPageKey: K,
    protected val lastPageKey: K? = null
) : BasePageKeyedDataSource<K, V>(statusProcessor) {

    override fun loadInitial(params: LoadInitialParams<K>, callback: LoadInitialCallback<K, V>) {
        Timber.tag(tag).d("loadInitial with key %s", firstPageKey)
        statusProcessor.update(LoadingStatus.loading())
        val list = load(firstPageKey)
        val nextKey = next(firstPageKey)
        if (list != null) {
            if (nextKey == null) {
                statusProcessor.update(LoadingStatus.success())
            } else {
                statusProcessor.update(LoadingStatus.loadingForward())
            }
        }
        onResult(list, null, nextKey, params, callback)
    }

    override fun loadAfter(params: LoadParams<K>, callback: LoadCallback<K, V>) {
        val key = params.key
        Timber.tag(tag).d("loadAfter with key %s", key)
        statusProcessor.update(LoadingStatus.loadingForward())
        val list = load(key)
        val nextKey = next(key)
        if (list != null) {
            if (nextKey == null) {
                statusProcessor.update(LoadingStatus.success())
            } else {
                statusProcessor.update(LoadingStatus.loadingForward())
            }
        }
        onResultAfter(list, nextKey, params, callback)
    }

    override fun loadBefore(params: LoadParams<K>, callback: LoadCallback<K, V>) {
        val key = params.key
        Timber.tag(tag).d("loadBefore with key %s", key)
        statusProcessor.update(LoadingStatus.loadingBackward())
        val list = load(key)
        val prevKey = prev(key)
        if (list != null) {
            if (prevKey == null) {
                statusProcessor.update(LoadingStatus.success())
            } else {
                statusProcessor.update(LoadingStatus.loadingBackward())
            }
        }
        onResultBefore(list, prevKey, params, callback)
    }

    private fun load(key: K): List<V>? {
        return try {
            loadForKey(key)
        } catch (e: Throwable) {
            Timber.tag(tag).e(e, "Error during loading values list for key %s", key)
            statusProcessor.update(LoadingStatus.error(e))
            null
        }
    }

    private fun next(key: K): K? = if (lastPageKey != null && lastPageKey == key) null else getNextKey(key)

    private fun prev(key: K): K? = if (firstPageKey == key) null else getPrevKey(key)

    protected abstract fun getNextKey(key: K): K?

    protected abstract fun getPrevKey(key: K): K?

    @Throws(Throwable::class)
    protected abstract fun loadForKey(key: K): List<V>

}