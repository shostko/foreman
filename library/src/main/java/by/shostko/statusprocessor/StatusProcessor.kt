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

class StatusProcessor(lifecycleOwner: LifecycleOwner) : BaseStatusProcessor<Status>(lifecycleOwner) {

    override fun createStatusLoading(direction: Direction): Status = Status.loading(direction)

    override fun createStatusError(throwable: Throwable): Status = Status.error(throwable)

    override fun createStatusSuccess(): Status = Status.success()
}

@Suppress("MemberVisibilityCanBePrivate", "unused")
abstract class BaseStatusProcessor<STATUS : BaseStatus<*>>(private val lifecycleOwner: LifecycleOwner) {

    @Suppress("LeakingThis")
    private val statusProcessor: FlowableProcessor<STATUS> = BehaviorProcessor.createDefault(createStatusLoading(Direction.FULL))

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

    fun updateLoading(direction: Direction) = update(createStatusLoading(direction))

    fun updateError(throwable: Throwable) = update(createStatusError(throwable))

    fun updateSuccess() = update(createStatusSuccess())

    protected abstract fun createStatusLoading(direction: Direction): STATUS

    protected abstract fun createStatusError(throwable: Throwable): STATUS

    protected abstract fun createStatusSuccess(): STATUS

    fun <T> wrapSingleRequest(errorItem: T? = null, callable: () -> Single<T>): Flowable<T> =
        action.startWith(Action.REFRESH)
            .doOnNext { updateLoading(Direction.FULL) }
            .switchMapSingle { _ ->
                Single.defer(callable)
                    .subscribeOn(Schedulers.io())
                    .doOnSuccess { updateSuccess() }
                    .doOnError { updateError(it) }
                    .onErrorResumeNext { if (errorItem == null) Single.never() else Single.just(errorItem) }
            }

    fun <T> wrapOneRequest(errorItem: T? = null, callable: () -> T): Flowable<T> = wrapSingleRequest(errorItem, { Single.just(callable.invoke()) })
}