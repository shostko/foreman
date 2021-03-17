package by.shostko.statushandler.paging.positional

import by.shostko.statushandler.StatusHandler

@Suppress("unused")
abstract class AsyncPositionalDataSource<V>(
    statusHandlerCallback: StatusHandler.Callback
) : BasePositionalDataSource<V>(statusHandlerCallback) {

    final override fun onLoadInitial(params: LoadInitialParams, callback: LoadInitialCallback<V>) {
        onLoadInitial(params.requestedStartPosition, params.requestedLoadSize, object : CallbackInitial<V>() {
            override fun onSuccessResult(list: List<V>, frontPosition: Int) {
                onSuccessResult(list, frontPosition, params, callback)
            }

            override fun onSuccessResult(list: List<V>, frontPosition: Int, total: Int) {
                onSuccessResult(list, frontPosition, total, params, callback)
            }

            override fun onFailedResult(e: Throwable) {
                onFailedResult(e, params, callback)
            }
        })
    }

    final override fun onLoadRange(params: LoadRangeParams, callback: LoadRangeCallback<V>) {
        onLoadRange(params.startPosition, params.loadSize, object : Callback<V>() {
            override fun onSuccessResult(list: List<V>) {
                onSuccessResult(list, params, callback)
            }

            override fun onFailedResult(e: Throwable) {
                onFailedResult(e, params, callback)
            }
        })
    }

    @Throws(Throwable::class)
    protected abstract fun onLoadInitial(startPosition: Int, loadSize: Int, callback: CallbackInitial<V>)

    @Throws(Throwable::class)
    protected abstract fun onLoadRange(startPosition: Int, loadSize: Int, callback: Callback<V>)

    protected abstract class CallbackInitial<V> {
        abstract fun onSuccessResult(list: List<V>, frontPosition: Int)
        abstract fun onSuccessResult(list: List<V>, frontPosition: Int, total: Int)
        abstract fun onFailedResult(e: Throwable)
    }

    protected abstract class Callback<V> {
        abstract fun onSuccessResult(list: List<V>)
        abstract fun onFailedResult(e: Throwable)
    }
}