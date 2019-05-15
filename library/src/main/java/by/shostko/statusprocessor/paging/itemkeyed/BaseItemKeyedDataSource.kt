package by.shostko.statusprocessor.paging.itemkeyed

import android.annotation.SuppressLint
import by.shostko.statusprocessor.Action
import by.shostko.statusprocessor.StatusProcessor
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider
import com.uber.autodispose.autoDisposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

@Suppress("MemberVisibilityCanBePrivate", "unused")
@SuppressLint("CheckResult")
abstract class BaseItemKeyedDataSource<K, V>(
    protected val statusProcessor: StatusProcessor
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

    protected fun onResult(
        list: List<V>?,
        params: LoadInitialParams<K>,
        callback: LoadInitialCallback<V>
    ) {
        retryFunction = if (list == null) {
            { loadInitial(params, callback) }
        } else {
            callback.onResult(list)
            null
        }
    }

    protected fun onResultAfter(
        list: List<V>?,
        params: LoadParams<K>,
        callback: LoadCallback<V>
    ) {
        retryFunction = if (list == null) {
            { loadAfter(params, callback) }
        } else {
            callback.onResult(list)
            null
        }
    }

    protected fun onResultBefore(
        list: List<V>?,
        params: LoadParams<K>,
        callback: LoadCallback<V>
    ) {
        retryFunction = if (list == null) {
            { loadBefore(params, callback) }
        } else {
            callback.onResult(list)
            null
        }
    }
}