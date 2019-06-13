package by.shostko.statushandler.paging.pagekeyed

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import by.shostko.statushandler.Action
import by.shostko.statushandler.StatusProcessor
import by.shostko.statushandler.Direction
import by.shostko.statushandler.extension.asString
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider
import com.uber.autodispose.autoDisposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

@Suppress("MemberVisibilityCanBePrivate", "unused")
@SuppressLint("CheckResult")
abstract class BasePageKeyedDataSource<K, V>(
    protected val statusProcessor: StatusProcessor<*>
) : PageKeyedLifecycledDataSource<K, V>() {

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

    final override fun loadInitial(params: LoadInitialParams<K>, callback: LoadInitialCallback<K, V>) {
        Timber.tag(tag).d("loadInitial for %s", params.asString())
        statusProcessor.updateWorking(Direction.FULL)
        try {
            onLoadInitial(params, callback)
        } catch (e: Throwable) {
            onFailedResultInitial(e, params, callback)
        }
    }

    protected abstract fun onLoadInitial(params: LoadInitialParams<K>, callback: LoadInitialCallback<K, V>)

    final override fun loadAfter(params: LoadParams<K>, callback: LoadCallback<K, V>) {
        Timber.tag(tag).d("loadAfter for %s", params.asString())
        statusProcessor.updateWorking(Direction.FORWARD)
        try {
            onLoadAfter(params, callback)
        } catch (e: Throwable) {
            onFailedResultAfter(e, params, callback)
        }
    }

    protected abstract fun onLoadAfter(params: LoadParams<K>, callback: LoadCallback<K, V>)

    final override fun loadBefore(params: LoadParams<K>, callback: LoadCallback<K, V>) {
        Timber.tag(tag).d("loadBefore for %s", params.asString())
        statusProcessor.updateWorking(Direction.BACKWARD)
        try {
            onLoadBefore(params, callback)
        } catch (e: Throwable) {
            onFailedResultBefore(e, params, callback)
        }
    }

    protected abstract fun onLoadBefore(params: LoadParams<K>, callback: LoadCallback<K, V>)

    protected fun onSuccessResultInitial(
        list: List<V>,
        previousPageKey: K?,
        nextPageKey: K?,
        params: LoadInitialParams<K>,
        callback: LoadInitialCallback<K, V>
    ) {
        Timber.tag(tag).d(
            "onSuccessResult %d items (previousPageKey=%s, nextPageKey=%s) for %s",
            list.size, previousPageKey, nextPageKey, params.asString()
        )
        retryFunction = null
        statusProcessor.updateSuccess()
        callback.onResult(list, previousPageKey, nextPageKey)
    }

    protected fun onSuccessResultAfter(
        list: List<V>,
        nextPageKey: K?,
        params: LoadParams<K>,
        callback: LoadCallback<K, V>
    ) {
        Timber.tag(tag).d(
            "onSuccessResult %d items (nextPageKey=%s) for %s",
            list.size, nextPageKey, params.asString()
        )
        retryFunction = null
        statusProcessor.updateSuccess()
        callback.onResult(list, nextPageKey)
    }

    protected fun onSuccessResultBefore(
        list: List<V>,
        previousPageKey: K?,
        params: LoadParams<K>,
        callback: LoadCallback<K, V>
    ) {
        Timber.tag(tag).d(
            "onSuccessResult %d items (previousPageKey=%s) for %s",
            list.size, previousPageKey, params.asString()
        )
        retryFunction = null
        statusProcessor.updateSuccess()
        callback.onResult(list, previousPageKey)
    }

    protected fun onFailedResultInitial(
        e: Throwable,
        params: LoadInitialParams<K>,
        callback: LoadInitialCallback<K, V>
    ) {
        Timber.tag(tag).e(e, "Error during loadInitial for %s", params.asString())
        retryFunction = { loadInitial(params, callback) }
        statusProcessor.updateFailed(e)
    }

    protected fun onFailedResultAfter(
        e: Throwable,
        params: LoadParams<K>,
        callback: LoadCallback<K, V>
    ) {
        Timber.tag(tag).e(e, "Error during loadAfter for %s", params.asString())
        retryFunction = { loadAfter(params, callback) }
        statusProcessor.updateFailed(e)
    }

    protected fun onFailedResultBefore(
        e: Throwable,
        params: LoadParams<K>,
        callback: LoadCallback<K, V>
    ) {
        Timber.tag(tag).e(e, "Error during loadBefore for %s", params.asString())
        retryFunction = { loadBefore(params, callback) }
        statusProcessor.updateFailed(e)
    }
}