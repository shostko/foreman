@file:Suppress("unused")

package by.shostko.statushandler.paging.singlerequest

import by.shostko.statushandler.StatusHandler
import by.shostko.statushandler.paging.pagekeyed.BasePageKeyedDataSource

abstract class AsyncSingleRequestDataSource<V>(
    statusHandlerCallback: StatusHandler.Callback
) : BasePageKeyedDataSource<Int, V>(statusHandlerCallback) {

    override fun onLoadInitial(params: LoadInitialParams<Int>, callback: LoadInitialCallback<Int, V>) {
        onLoad(object : Callback<V>() {
            override fun onSuccessResult(list: List<V>) {
                onSuccessResultInitial(list, null, null, params, callback)
            }

            override fun onFailedResult(e: Throwable) {
                onFailedResultInitial(e, params, callback)
            }
        })
    }

    override fun onLoadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, V>) {
        onSuccessResultAfter(emptyList(), null, params, callback)
    }

    override fun onLoadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, V>) {
        onSuccessResultBefore(emptyList(), null, params, callback)
    }

    @Throws(Throwable::class)
    protected abstract fun onLoad(callback: Callback<V>)

    protected abstract class Callback<V> {
        abstract fun onSuccessResult(list: List<V>)
        abstract fun onFailedResult(e: Throwable)
    }
}