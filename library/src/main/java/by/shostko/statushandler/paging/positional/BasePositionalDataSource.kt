package by.shostko.statushandler.paging.positional

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import by.shostko.statushandler.Action
import by.shostko.statushandler.StatusHandler
import by.shostko.statushandler.Direction
import by.shostko.statushandler.extension.asString
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider
import com.uber.autodispose.autoDisposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

@Suppress("MemberVisibilityCanBePrivate", "unused")
@SuppressLint("CheckResult")
abstract class BasePositionalDataSource<V>(
    protected val statusHandler: StatusHandler<*>
) : PositionalLifecycledDataSource<V>() {

    protected open val tag: String = javaClass.simpleName

    private val scopeProvider by lazy { AndroidLifecycleScopeProvider.from(this) }

    private var retryFunction: (() -> Any)? = null

    init {
        Handler(Looper.getMainLooper()).post {
            statusHandler.action
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .autoDisposable(scopeProvider)
                .subscribe({
                    Timber.tag(tag).d("%s action requested", it)
                    when (it) {
                        Action.RETRY -> retryFunction?.apply { retryFunction = null }?.invoke()
                        else -> invalidate()
                    }
                }, { Timber.tag(tag).e(it, "Error during listening actions") })
        }
    }

    final override fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<V>) {
        Timber.tag(tag).d("loadInitial for %s", params.asString())
        statusHandler.updateWorking(Direction.FULL)
        try {
            onLoadInitial(params, callback)
        } catch (e: Throwable) {
            onFailedResult(e, params, callback)
        }
    }

    protected abstract fun onLoadInitial(params: LoadInitialParams, callback: LoadInitialCallback<V>)

    final override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<V>) {
        Timber.tag(tag).d("loadRange for %s", params.asString())
        statusHandler.updateWorking(Direction.FULL)
        try {
            onLoadRange(params, callback)
        } catch (e: Throwable) {
            onFailedResult(e, params, callback)
        }
    }

    protected abstract fun onLoadRange(params: LoadRangeParams, callback: LoadRangeCallback<V>)

    protected fun onSuccessResult(
        list: List<V>,
        frontPosition: Int,
        totalCount: Int,
        params: LoadInitialParams,
        callback: LoadInitialCallback<V>
    ) {
        Timber.tag(tag).d(
            "onSuccessResult %d (frontPosition=%d, totalCount=%d) items for %s",
            list.size, frontPosition, totalCount, params.asString()
        )
        retryFunction = null
        statusHandler.updateSuccess()
        callback.onResult(list, frontPosition, totalCount)
    }

    protected fun onSuccessResult(
        list: List<V>,
        frontPosition: Int,
        params: LoadInitialParams,
        callback: LoadInitialCallback<V>
    ) {
        Timber.tag(tag).d(
            "onSuccessResult %d (frontPosition=%d) items for %s",
            list.size, frontPosition, params.asString()
        )
        retryFunction = null
        statusHandler.updateSuccess()
        callback.onResult(list, frontPosition)
    }

    protected fun onSuccessResult(
        list: List<V>,
        params: LoadInitialParams,
        callback: LoadInitialCallback<V>
    ) {
        Timber.tag(tag).d("onSuccessResult %d items for %s", list.size, params.asString())
        retryFunction = null
        statusHandler.updateSuccess()
        callback.onResult(list, params.requestedStartPosition)
    }

    protected fun onSuccessResult(
        list: List<V>,
        params: LoadRangeParams,
        callback: LoadRangeCallback<V>
    ) {
        Timber.tag(tag).d("onSuccessResult %d items for %s", list.size, params.asString())
        retryFunction = null
        statusHandler.updateSuccess()
        callback.onResult(list)
    }

    protected fun onFailedResult(
        e: Throwable,
        params: LoadInitialParams,
        callback: LoadInitialCallback<V>
    ) {
        Timber.tag(tag).e(e, "Error during loadInitial for %s", params.asString())
        retryFunction = { loadInitial(params, callback) }
        statusHandler.updateFailed(e)
    }

    protected fun onFailedResult(
        e: Throwable,
        params: LoadRangeParams,
        callback: LoadRangeCallback<V>
    ) {
        Timber.tag(tag).e(e, "Error during loadAfter for %s", params.asString())
        retryFunction = { loadRange(params, callback) }
        statusHandler.updateFailed(e)
    }
}