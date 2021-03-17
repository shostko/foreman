package by.shostko.statushandler.paging.positional

import by.shostko.statushandler.StatusHandler

@Suppress("unused")
abstract class DirectPositionalDataSource<V>(
    statusHandlerCallback: StatusHandler.Callback
) : BasePositionalDataSource<V>(statusHandlerCallback) {

    final override fun onLoadInitial(params: LoadInitialParams, callback: LoadInitialCallback<V>) {
        val (list, frontPosition, total) = onLoadInitial(params.requestedStartPosition, params.requestedLoadSize)
        if (total == null) {
            onSuccessResult(list, frontPosition, params, callback)
        } else {
            onSuccessResult(list, frontPosition, total, params, callback)
        }
    }

    final override fun onLoadRange(params: LoadRangeParams, callback: LoadRangeCallback<V>) {
        val list = onLoadRange(params.startPosition, params.loadSize)
        onSuccessResult(list, params, callback)
    }

    @Throws(Throwable::class)
    protected abstract fun onLoadInitial(startPosition: Int, loadSize: Int): InitialResult<V>

    @Throws(Throwable::class)
    protected abstract fun onLoadRange(startPosition: Int, loadSize: Int): List<V>

    protected fun result(list: List<V>, frontPosition: Int, total: Int): InitialResult<V> = InitialResult(list, frontPosition, total)

    protected data class InitialResult<V>(
        val list: List<V>,
        val frontPosition: Int,
        val total: Int? = null
    )
}