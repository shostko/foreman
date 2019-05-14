package by.shostko.statusprocessor.paging.positional

import by.shostko.statusprocessor.LoadingStatus
import by.shostko.statusprocessor.StatusProcessor
import timber.log.Timber

@Suppress("unused")
abstract class SimplePositionalDataSource<V>(
    statusProcessor: StatusProcessor
) : BasePositionalDataSource<V>(statusProcessor) {

    override fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<V>) {
        val startPosition = params.requestedStartPosition
        val loadSize = params.requestedStartPosition
        Timber.tag(tag).d("loadInitial from %s, size %s", startPosition, loadSize)
        statusProcessor.update(LoadingStatus.loading())
        val list = load(startPosition, loadSize)
        if (list != null) {
            if (list.isEmpty()) {
                statusProcessor.update(LoadingStatus.success())
            } else {
                statusProcessor.update(LoadingStatus.loadingForward())
            }
        }
        onResult(list, startPosition, params, callback)
    }

    override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<V>) {
        val startPosition = params.startPosition
        val loadSize = params.loadSize
        Timber.tag(tag).d("loadRange from %s, size %s", startPosition, loadSize)
        statusProcessor.update(LoadingStatus.loadingForward())
        val list = load(startPosition, loadSize)
        if (list != null) {
            if (list.isEmpty()) {
                statusProcessor.update(LoadingStatus.success())
            } else {
                statusProcessor.update(LoadingStatus.loadingForward())
            }
        }
        onResultRange(list, params, callback)
    }

    private fun load(startPosition: Int, loadSize: Int): List<V>? {
        return try {
            loadForKey(startPosition, loadSize)
        } catch (e: Throwable) {
            Timber.tag(tag).e(e, "Error during loading values list from %s, size %s", startPosition, loadSize)
            statusProcessor.update(LoadingStatus.error(e))
            null
        }
    }

    @Throws(Throwable::class)
    protected abstract fun loadForKey(startPosition: Int, loadSize: Int): List<V>
}