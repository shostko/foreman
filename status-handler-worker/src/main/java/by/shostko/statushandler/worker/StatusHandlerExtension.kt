@file:Suppress("unused")

package by.shostko.statushandler.worker

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.work.WorkInfo
import androidx.work.WorkManager
import by.shostko.statushandler.Action
import by.shostko.statushandler.StatusHandler
import io.reactivex.Flowable
import io.reactivex.FlowableTransformer
import io.reactivex.functions.BiFunction
import java.util.*

private fun <E> StatusHandler<E>.startWorkAndObserveStatus(lifecycleOwner: LifecycleOwner, callable: () -> UUID) {
    val uuid = callable.invoke()
    Handler(Looper.getMainLooper()).post {
        WorkManager.getInstance().getWorkInfoByIdLiveData(uuid)
            .observe(lifecycleOwner, Observer { info ->
                info?.state?.let {
                    when {
                        !it.isFinished -> updateWorking()
                        it == WorkInfo.State.FAILED -> updateFailed(info.outputData.keyValueMap)
                        else -> updateSuccess()
                    }
                }
            })
    }
}

fun <E, T> StatusHandler<E>.combineWithWorker(lifecycleOwner: LifecycleOwner, callable: () -> UUID): FlowableTransformer<T, T> =
    FlowableTransformer { upstream ->
        Flowable.combineLatest(
            action.startWith(Action.REFRESH)
                .doOnNext { startWorkAndObserveStatus(lifecycleOwner, callable) }
                .ignoreElements()
                .toFlowable<Unit>()
                .startWith(Unit)
                .distinctUntilChanged(),
            upstream, BiFunction { _, t -> t })
    }

fun <E, T> StatusHandler<E>.prepareWithWorker(lifecycleOwner: LifecycleOwner, callable: () -> UUID): FlowableTransformer<T, T> =
    FlowableTransformer { upstream ->
        Flowable.combineLatest(
            action.doOnNext { startWorkAndObserveStatus(lifecycleOwner, callable) }
                .ignoreElements()
                .toFlowable<Unit>()
                .startWith(Unit)
                .distinctUntilChanged(),
            upstream, BiFunction { _, t -> t })
    }