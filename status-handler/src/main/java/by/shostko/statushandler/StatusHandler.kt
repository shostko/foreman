@file:Suppress("unused", "MemberVisibilityCanBePrivate", "RedundantLambdaArrow")

package by.shostko.statushandler

import io.reactivex.*
import io.reactivex.processors.BehaviorProcessor
import io.reactivex.processors.FlowableProcessor
import io.reactivex.processors.PublishProcessor
import io.reactivex.schedulers.Schedulers

enum class Action {
    PROCEED,
    REFRESH,
    RETRY
}

abstract class StatusHandler<E> {

    abstract val status: Flowable<Status<E>>
    abstract val action: Flowable<Action>

    abstract fun proceed()
    abstract fun refresh()
    abstract fun retry()

    abstract fun updateWorking()
    abstract fun updateWorkingForward()
    abstract fun updateWorkingBackward()
    abstract fun updateFailed(throwable: Throwable)
    abstract fun updateFailed(map: Map<String, Any>)
    abstract fun updateSuccess()

    fun <T> wrapFlowableRequest(callable: () -> Flowable<T>): Flowable<T> = wrapFlowableRequest(null, callable)
    abstract fun <T> wrapFlowableRequest(errorItem: T?, callable: () -> Flowable<T>): Flowable<T>
    fun <T> prepareFlowableRequest(callable: () -> Flowable<T>): Flowable<T> = prepareFlowableRequest(null, callable)
    abstract fun <T> prepareFlowableRequest(errorItem: T?, callable: () -> Flowable<T>): Flowable<T>

    fun <T> wrapObservableRequest(callable: () -> Observable<T>): Flowable<T> = prepareFlowableRequest(null, callable.toFlowable())
    fun <T> wrapObservableRequest(errorItem: T?, callable: () -> Observable<T>): Flowable<T> = prepareFlowableRequest(errorItem, callable.toFlowable())
    fun <T> prepareObservableRequest(callable: () -> Observable<T>): Flowable<T> = prepareFlowableRequest(null, callable.toFlowable())
    fun <T> prepareObservableRequest(errorItem: T?, callable: () -> Observable<T>): Flowable<T> = prepareFlowableRequest(errorItem, callable.toFlowable())

    fun <T> wrapSingleRequest(callable: () -> Single<T>): Flowable<T> = wrapSingleRequest(null, callable)
    abstract fun <T> wrapSingleRequest(errorItem: T?, callable: () -> Single<T>): Flowable<T>
    fun <T> prepareSingleRequest(callable: () -> Single<T>): Flowable<T> = prepareSingleRequest(null, callable)
    abstract fun <T> prepareSingleRequest(errorItem: T?, callable: () -> Single<T>): Flowable<T>

    abstract fun wrapCompletableRequest(callable: () -> Completable): Flowable<Unit>
    abstract fun prepareCompletableRequest(callable: () -> Completable): Flowable<Unit>

    fun <T> wrapCallableRequest(callable: () -> T): Flowable<T> = wrapSingleRequest(null) { Single.fromCallable(callable) }
    fun <T> wrapCallableRequest(errorItem: T?, callable: () -> T): Flowable<T> = wrapSingleRequest(errorItem) { Single.fromCallable(callable) }
    fun <T> prepareCallableRequest(callable: () -> T): Flowable<T> = prepareSingleRequest(null) { Single.fromCallable(callable) }
    fun <T> prepareCallableRequest(errorItem: T?, callable: () -> T): Flowable<T> = prepareSingleRequest(errorItem) { Single.fromCallable(callable) }
    fun wrapActionRequest(action: () -> Unit): Flowable<Unit> = wrapCompletableRequest { Completable.fromAction(action) }
    fun prepareActionRequest(action: () -> Unit): Flowable<Unit> = prepareCompletableRequest { Completable.fromAction(action) }

    protected fun <T> (() -> Observable<T>).toFlowable(): (() -> Flowable<T>) = { this().toFlowable(BackpressureStrategy.LATEST) }
}

class StatusHandlerImpl<E>(private val factory: Status.Factory<E>) : StatusHandler<E>() {

    private val statusProcessor: FlowableProcessor<Status<E>> = BehaviorProcessor.createDefault(factory.createInitial())

    private val actionProcessor: FlowableProcessor<Action> = PublishProcessor.create()

    override val status: Flowable<Status<E>> = statusProcessor.hide()

    override val action: Flowable<Action> = actionProcessor.hide()

    // region action

    override fun proceed() {
        actionProcessor.onNext(Action.PROCEED)
    }

    override fun refresh() {
        actionProcessor.onNext(Action.REFRESH)
    }

    override fun retry() {
        actionProcessor.onNext(Action.RETRY)
    }

    //endregion

    // region status

    override fun updateWorking() = statusProcessor.onNext(factory.createWorking(Direction.FULL))

    override fun updateWorkingForward() = statusProcessor.onNext(factory.createWorking(Direction.FORWARD))

    override fun updateWorkingBackward() = statusProcessor.onNext(factory.createWorking(Direction.BACKWARD))

    override fun updateFailed(throwable: Throwable) = statusProcessor.onNext(factory.createFailed(throwable))

    override fun updateFailed(map: Map<String, Any>) = statusProcessor.onNext(factory.createFailed(map))

    override fun updateSuccess() = statusProcessor.onNext(factory.createSuccess())

    //endregion

    // region Flowable

    override fun <T> wrapFlowableRequest(errorItem: T?, callable: () -> Flowable<T>): Flowable<T> =
        action.startWith(Action.PROCEED)
            .doOnNext { updateWorking() }
            .switchMap {
                Flowable.defer(callable)
                    .subscribeOn(Schedulers.io())
                    .doOnNext { updateSuccess() }
                    .doOnError { updateFailed(it) }
                    .onErrorResumeNext { _: Throwable -> if (errorItem == null) Flowable.never() else Flowable.just(errorItem) }
            }

    override fun <T> prepareFlowableRequest(errorItem: T?, callable: () -> Flowable<T>): Flowable<T> =
        action.doOnNext { updateWorking() }
            .switchMap {
                Flowable.defer(callable)
                    .subscribeOn(Schedulers.io())
                    .doOnNext { updateSuccess() }
                    .doOnError { updateFailed(it) }
                    .onErrorResumeNext { _: Throwable -> if (errorItem == null) Flowable.never() else Flowable.just(errorItem) }
            }

    // endregion

    // region Single

    override fun <T> wrapSingleRequest(errorItem: T?, callable: () -> Single<T>): Flowable<T> =
        action.startWith(Action.PROCEED)
            .doOnNext { updateWorking() }
            .switchMapSingle {
                Single.defer(callable)
                    .subscribeOn(Schedulers.io())
                    .doOnSuccess { updateSuccess() }
                    .doOnError { updateFailed(it) }
                    .onErrorResumeNext { if (errorItem == null) Single.never() else Single.just(errorItem) }
            }

    override fun <T> prepareSingleRequest(errorItem: T?, callable: () -> Single<T>): Flowable<T> =
        action.doOnNext { updateWorking() }
            .switchMapSingle {
                Single.defer(callable)
                    .subscribeOn(Schedulers.io())
                    .doOnSuccess { updateSuccess() }
                    .doOnError { updateFailed(it) }
                    .onErrorResumeNext { if (errorItem == null) Single.never() else Single.just(errorItem) }
            }

    // endregion

    // region Completable

    override fun wrapCompletableRequest(callable: () -> Completable): Flowable<Unit> =
        action.startWith(Action.PROCEED)
            .doOnNext { updateWorking() }
            .switchMapSingle {
                Completable.defer(callable)
                    .subscribeOn(Schedulers.io())
                    .doOnComplete { updateSuccess() }
                    .doOnError { updateFailed(it) }
                    .onErrorResumeNext { Completable.never() }
                    .toSingleDefault(Unit)
            }

    override fun prepareCompletableRequest(callable: () -> Completable): Flowable<Unit> =
        action.doOnNext { updateWorking() }
            .switchMapSingle {
                Completable.defer(callable)
                    .subscribeOn(Schedulers.io())
                    .doOnComplete { updateSuccess() }
                    .doOnError { updateFailed(it) }
                    .onErrorResumeNext { Completable.never() }
                    .toSingleDefault(Unit)
            }

    // endregion
}