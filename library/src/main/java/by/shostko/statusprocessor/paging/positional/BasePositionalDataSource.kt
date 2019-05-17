package by.shostko.statusprocessor.paging.positional

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import by.shostko.statusprocessor.Action
import by.shostko.statusprocessor.BaseStatusProcessor
import by.shostko.statusprocessor.LoadingStatus
import by.shostko.statusprocessor.extension.asString
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider
import com.uber.autodispose.autoDisposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

@Suppress("MemberVisibilityCanBePrivate", "unused")
@SuppressLint("CheckResult")
abstract class BasePositionalDataSource<V>(
    protected val statusProcessor: BaseStatusProcessor<*>
) : PositionalLifecycledDataSource<V>() {

    protected open val tag: String = javaClass.simpleName

    private val scopeProvider by lazy { AndroidLifecycleScopeProvider.from(this) }

    private var retryFunction: (() -> Any)? = null

    init {
        Handler(Looper.getMainLooper()).post {
            statusProcessor.action
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
        statusProcessor.updateLoading()
        try {
            onLoadInitial(params, callback)
        } catch (e: Throwable) {
            onFailedResult(e, params, callback)
        }
    }

    protected abstract fun onLoadInitial(params: LoadInitialParams, callback: LoadInitialCallback<V>)

    final override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<V>) {
        Timber.tag(tag).d("loadRange for %s", params.asString())
        statusProcessor.updateLoading()
        try {
            onLoadRange(params, callback)
        } catch (e: Throwable) {
            onFailedResult(e, params, callback)
        }
    }

    protected abstract fun onLoadRange(params: LoadRangeParams, callback: LoadRangeCallback<V>)

    protected fun onResult(
        list: List<V>?,
        position: Int?,
        params: LoadInitialParams,
        callback: LoadInitialCallback<V>
    ) {
        retryFunction = if (list == null || position == null) {
            { loadInitial(params, callback) }
        } else {
            callback.onResult(list, position)
            null
        }
    }

    protected fun onResultRange(
        list: List<V>?,
        params: LoadRangeParams,
        callback: LoadRangeCallback<V>
    ) {
        retryFunction = if (list == null) {
            { loadRange(params, callback) }
        } else {
            callback.onResult(list)
            null
        }
    }

    protected fun onSuccessResult(
        list: List<V>,
        frontPosition: Int,
        totalCount: Int,
        params: LoadInitialParams,
        callback: LoadInitialCallback<V>
    ) {
        retryFunction = null
        statusProcessor.updateSuccess()
        callback.onResult(list, frontPosition, totalCount)
    }

    protected fun onSuccessResult(
        list: List<V>,
        frontPosition: Int,
        params: LoadInitialParams,
        callback: LoadInitialCallback<V>
    ) {
        retryFunction = null
        statusProcessor.updateSuccess()
        callback.onResult(list, frontPosition)
    }

    protected fun onSuccessResult(
        list: List<V>,
        params: LoadInitialParams,
        callback: LoadInitialCallback<V>
    ) {
        retryFunction = null
        statusProcessor.updateSuccess()
        callback.onResult(list, params.requestedStartPosition)
    }

    protected fun onSuccessResult(
        list: List<V>,
        params: LoadRangeParams,
        callback: LoadRangeCallback<V>
    ) {
        retryFunction = null
        statusProcessor.updateSuccess()
        callback.onResult(list)
    }

    protected fun onFailedResult(
        e: Throwable,
        params: LoadInitialParams,
        callback: LoadInitialCallback<V>
    ) {
        Timber.tag(tag).e(e, "Error during loadInitial for %s", params.asString())
        retryFunction = { loadInitial(params, callback) }
        statusProcessor.updateError(e)
    }

    protected fun onFailedResult(
        e: Throwable,
        params: LoadRangeParams,
        callback: LoadRangeCallback<V>
    ) {
        Timber.tag(tag).e(e, "Error during loadAfter for %s", params.asString())
        retryFunction = { loadRange(params, callback) }
        statusProcessor.updateError(e)
    }
}