package by.shostko.statushandler.paging.positional

import by.shostko.statushandler.StatusProcessor

@Suppress("unused")
abstract class AsyncPositionalDataSource<V>(
    statusProcessor: StatusProcessor<*>
) : BasePositionalDataSource<V>(statusProcessor) {

    override fun onLoadInitial(params: LoadInitialParams, callback: LoadInitialCallback<V>) {
        onLoad(params.requestedStartPosition, params.requestedLoadSize, CallbackImpl({
            onSuccessResult(it, params, callback)
        }, {
            onFailedResult(it, params, callback)
        }))
    }

    override fun onLoadRange(params: LoadRangeParams, callback: LoadRangeCallback<V>) {
        onLoad(params.startPosition, params.loadSize, CallbackImpl({
            onSuccessResult(it, params, callback)
        }, {
            onFailedResult(it, params, callback)
        }))
    }

    @Throws(Throwable::class)
    protected abstract fun onLoad(startPosition: Int, loadSize: Int, callback: Callback<V>)

    protected abstract class Callback<V> {
        abstract fun onSuccessResult(list: List<V>)
        abstract fun onFailedResult(e: Throwable)
    }

    private class CallbackImpl<V>(
        private val successFun: ((List<V>) -> Any),
        private val failedFun: ((Throwable) -> Any)
    ) : Callback<V>() {

        override fun onSuccessResult(list: List<V>) {
            successFun.invoke(list)
        }

        override fun onFailedResult(e: Throwable) {
            failedFun.invoke(e)
        }
    }
}