package by.shostko.statusprocessor.paging.singlerequest

import by.shostko.statusprocessor.LoadingStatus
import by.shostko.statusprocessor.StatusProcessor
import by.shostko.statusprocessor.paging.pagekeyed.BasePageKeyedDataSource
import timber.log.Timber

abstract class SingleRequestDataSource<V>(statusProcessor: StatusProcessor) :
    BasePageKeyedDataSource<Int, V>(statusProcessor) {

    override fun loadInitial(params: LoadInitialParams<Int>, callback: LoadInitialCallback<Int, V>) {
        Timber.tag(tag).d("loadInitial")
        statusProcessor.update(LoadingStatus.loading())
        val list = try {
            load().apply { statusProcessor.update(LoadingStatus.success()) }
        } catch (e: Throwable) {
            Timber.tag(tag).e(e, "Error during loading values")
            statusProcessor.update(LoadingStatus.error(e))
            null
        }
        onResult(list, null, null, params, callback)
    }

    override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, V>) {
        onResultAfter(emptyList(), null, params, callback)
    }

    override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, V>) {
        onResultBefore(emptyList(), null, params, callback)
    }

    @Throws(Throwable::class)
    protected abstract fun load(): List<V>
}