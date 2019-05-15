package by.shostko.statusprocessor.paging.singlerequest

import by.shostko.statusprocessor.BaseStatusProcessor
import by.shostko.statusprocessor.paging.pagekeyed.BasePageKeyedDataSource

@Suppress("unused")
abstract class SimpleSingleRequestDataSource<V>(statusProcessor: BaseStatusProcessor<*>) :
    BasePageKeyedDataSource<Int, V>(statusProcessor) {

    override fun onLoadInitial(params: LoadInitialParams<Int>, callback: LoadInitialCallback<Int, V>) {
        onSuccessResultInitial(onLoad(), null, null, params, callback)
    }

    override fun onLoadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, V>) {
        onSuccessResultAfter(emptyList(), null, params, callback)
    }

    override fun onLoadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, V>) {
        onSuccessResultBefore(emptyList(), null, params, callback)
    }

    @Throws(Throwable::class)
    protected abstract fun onLoad(): List<V>
}