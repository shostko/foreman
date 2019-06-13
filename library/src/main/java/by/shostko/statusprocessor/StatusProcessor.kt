@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package by.shostko.statusprocessor

import androidx.lifecycle.LifecycleOwner
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.processors.BehaviorProcessor
import io.reactivex.processors.FlowableProcessor
import io.reactivex.processors.PublishProcessor
import io.reactivex.schedulers.Schedulers

enum class Action {
    REFRESH,
    RETRY
}

class StatusProcessor<STATUS : Status<*>>(private val lifecycleOwner: LifecycleOwner, private val factory: Status.Factory<STATUS>) {

    @Suppress("LeakingThis")
    private val statusProcessor: FlowableProcessor<STATUS> = BehaviorProcessor.createDefault(factory.createWorking(Direction.FULL))

    private val actionProcessor: FlowableProcessor<Action> = PublishProcessor.create()

    val status: Flowable<STATUS> = statusProcessor.hide()

    val action: Flowable<Action> = actionProcessor.hide()

    fun retry() {
        actionProcessor.onNext(Action.RETRY)
    }

    fun refresh() {
        actionProcessor.onNext(Action.REFRESH)
    }

    fun update(loadingStatus: STATUS) {
        statusProcessor.onNext(loadingStatus)
    }

    fun updateWorking(direction: Direction) = update(factory.createWorking(direction))

    fun updateFailed(throwable: Throwable) = update(factory.createFailed(throwable))

    fun updateSuccess() = update(factory.createSuccess())

    fun <T> wrapSingleRequest(errorItem: T? = null, callable: () -> Single<T>): Flowable<T> =
        action.startWith(Action.REFRESH)
            .doOnNext { updateWorking(Direction.FULL) }
            .switchMapSingle { _ ->
                Single.defer(callable)
                    .subscribeOn(Schedulers.io())
                    .doOnSuccess { updateSuccess() }
                    .doOnError { updateFailed(it) }
                    .onErrorResumeNext { if (errorItem == null) Single.never() else Single.just(errorItem) }
            }

    fun <T> wrapOneRequest(errorItem: T? = null, callable: () -> T): Flowable<T> = wrapSingleRequest(errorItem, { Single.just(callable.invoke()) })
}