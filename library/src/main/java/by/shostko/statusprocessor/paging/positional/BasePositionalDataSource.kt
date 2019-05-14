package by.shostko.statusprocessor.paging.positional

import android.annotation.SuppressLint
import by.shostko.statusprocessor.Action
import by.shostko.statusprocessor.StatusProcessor
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider
import com.uber.autodispose.autoDisposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

@Suppress("MemberVisibilityCanBePrivate", "unused")
@SuppressLint("CheckResult")
abstract class BasePositionalDataSource<V>(
    protected val statusProcessor: StatusProcessor
) : PositionalLifecycledDataSource<V>() {

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
}