package by.shostko.statusprocessor.paging.itemkeyed

import android.annotation.SuppressLint
import by.shostko.statusprocessor.Action
import by.shostko.statusprocessor.BaseStatusProcessor
import by.shostko.statusprocessor.extension.asString
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider
import com.uber.autodispose.autoDisposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

@Suppress("MemberVisibilityCanBePrivate", "unused")
@SuppressLint("CheckResult")
abstract class BaseItemKeyedDataSource<K, V>(
    protected val statusProcessor: BaseStatusProcessor<*>
) : ItemKeyedLifecycledDataSource<K, V>() {

    protected open val tag: String = javaClass.simpleName

    private val scopeProvider by lazy { AndroidLifecycleScopeProvider.from(this) }

    private var retryFunction: (() -> Any)? = null

    init {
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

    final override fun loadInitial(params: LoadInitialParams<K>, callback: LoadInitialCallback<V>) {
        Timber.tag(tag).d("loadInitial for %s", params.asString())
        statusProcessor.updateLoading()
        try {
            onLoadInitial(params, callback)
        } catch (e: Throwable) {
            onFailedResultInitial(e, params, callback)
        }
    }

    @Throws(Throwable::class)
    protected abstract fun onLoadInitial(params: LoadInitialParams<K>, callback: LoadInitialCallback<V>)

    final override fun loadAfter(params: LoadParams<K>, callback: LoadCallback<V>) {
        Timber.tag(tag).d("loadAfter for %s", params.asString())
        statusProcessor.updateLoadingForward()
        try {
            onLoadAfter(params, callback)
        } catch (e: Throwable) {
            onFailedResultAfter(e, params, callback)
        }
    }

    @Throws(Throwable::class)
    protected abstract fun onLoadAfter(params: LoadParams<K>, callback: LoadCallback<V>)

    final override fun loadBefore(params: LoadParams<K>, callback: LoadCallback<V>) {
        Timber.tag(tag).d("loadBefore for %s", params.asString())
        statusProcessor.updateLoadingBackward()
        try {
            onLoadBefore(params, callback)
        } catch (e: Throwable) {
            onFailedResultBefore(e, params, callback)
        }
    }

    @Throws(Throwable::class)
    protected abstract fun onLoadBefore(params: LoadParams<K>, callback: LoadCallback<V>)

    protected fun onSuccessResult(
        list: List<V>,
        params: LoadInitialParams<K>,
        callback: LoadInitialCallback<V>
    ) {
        retryFunction = null
        statusProcessor.updateSuccess()
        callback.onResult(list)
    }

    protected fun onSuccessResult(
        list: List<V>,
        params: LoadParams<K>,
        callback: LoadCallback<V>
    ) {
        retryFunction = null
        statusProcessor.updateSuccess()
        callback.onResult(list)
    }

    protected fun onFailedResultInitial(
        e: Throwable,
        params: LoadInitialParams<K>,
        callback: LoadInitialCallback<V>
    ) {
        Timber.tag(tag).e(e, "Error during loadInitial for %s", params.asString())
        retryFunction = { loadInitial(params, callback) }
        statusProcessor.updateError(e)
    }

    protected fun onFailedResultBefore(
        e: Throwable,
        params: LoadParams<K>,
        callback: LoadCallback<V>
    ) {
        Timber.tag(tag).e(e, "Error during loadBefore for %s", params.asString())
        statusProcessor.updateError(e)
        retryFunction = { loadBefore(params, callback) }
    }

    protected fun onFailedResultAfter(
        e: Throwable,
        params: LoadParams<K>,
        callback: LoadCallback<V>
    ) {
        Timber.tag(tag).e(e, "Error during loadAfter for %s", params.asString())
        retryFunction = { loadAfter(params, callback) }
        statusProcessor.updateError(e)
    }
}