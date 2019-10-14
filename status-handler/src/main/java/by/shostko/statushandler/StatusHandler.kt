@file:Suppress("unused", "MemberVisibilityCanBePrivate")

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

class StatusHandler<E>(private val factory: Status.Factory<E>) {

    private val statusProcessor: FlowableProcessor<Status<E>> = BehaviorProcessor.createDefault(factory.createInitial())

    private val actionProcessor: FlowableProcessor<Action> = PublishProcessor.create()

    val status: Flowable<Status<E>> = statusProcessor.hide()

    val action: Flowable<Action> = actionProcessor.hide()

    // region action

    fun proceed() {
        actionProcessor.onNext(Action.PROCEED)
    }

    fun refresh() {
        actionProcessor.onNext(Action.REFRESH)
    }

    fun retry() {
        actionProcessor.onNext(Action.RETRY)
    }

    //endregion

    // region status

    fun updateWorking() = statusProcessor.onNext(factory.createWorking(Direction.FULL))

    fun updateWorkingForward() = statusProcessor.onNext(factory.createWorking(Direction.FORWARD))

    fun updateWorkingBackward() = statusProcessor.onNext(factory.createWorking(Direction.BACKWARD))

    fun updateFailed(throwable: Throwable) = statusProcessor.onNext(factory.createFailed(throwable))

    fun updateFailed(map: Map<String, Any>) = statusProcessor.onNext(factory.createFailed(map))

    fun updateSuccess() = statusProcessor.onNext(factory.createSuccess())

    //endregion

    // region Flowable

    fun <T> wrapFlowableRequest(callable: () -> Flowable<T>): Flowable<T> = wrapFlowableRequest(null, callable)

    fun <T> wrapFlowableRequest(errorItem: T?, callable: () -> Flowable<T>): Flowable<T> =
        action.startWith(Action.PROCEED)
            .doOnNext { updateWorking() }
            .switchMap {
                Flowable.defer(callable)
                    .subscribeOn(Schedulers.io())
                    .doOnNext { updateSuccess() }
                    .doOnError { updateFailed(it) }
                    .onErrorResumeNext { th: Throwable -> if (errorItem == null) Flowable.never() else Flowable.just(errorItem) }
            }

    fun <T> prepareFlowableRequest(callable: () -> Flowable<T>): Flowable<T> = prepareFlowableRequest(null, callable)

    fun <T> prepareFlowableRequest(errorItem: T?, callable: () -> Flowable<T>): Flowable<T> =
        action.doOnNext { updateWorking() }
            .switchMap {
                Flowable.defer(callable)
                    .subscribeOn(Schedulers.io())
                    .doOnNext { updateSuccess() }
                    .doOnError { updateFailed(it) }
                    .onErrorResumeNext { th: Throwable -> if (errorItem == null) Flowable.never() else Flowable.just(errorItem) }
            }

    // endregion

    // region Observable

    private fun <T> (() -> Observable<T>).toFlowable(): (() -> Flowable<T>) = { this().toFlowable(BackpressureStrategy.LATEST) }

    fun <T> wrapObservableRequest(callable: () -> Observable<T>): Flowable<T> = prepareFlowableRequest(null, callable.toFlowable())

    fun <T> wrapObservableRequest(errorItem: T?, callable: () -> Observable<T>): Flowable<T> = prepareFlowableRequest(errorItem, callable.toFlowable())

    fun <T> prepareObservableRequest(callable: () -> Observable<T>): Flowable<T> = prepareFlowableRequest(null, callable.toFlowable())

    fun <T> prepareObservableRequest(errorItem: T?, callable: () -> Observable<T>): Flowable<T> = prepareFlowableRequest(errorItem, callable.toFlowable())

    // endregion

    // region Single

    fun <T> wrapSingleRequest(callable: () -> Single<T>): Flowable<T> = wrapSingleRequest(null, callable)

    fun <T> wrapSingleRequest(errorItem: T?, callable: () -> Single<T>): Flowable<T> =
        action.startWith(Action.PROCEED)
            .doOnNext { updateWorking() }
            .switchMapSingle {
                Single.defer(callable)
                    .subscribeOn(Schedulers.io())
                    .doOnSuccess { updateSuccess() }
                    .doOnError { updateFailed(it) }
                    .onErrorResumeNext { if (errorItem == null) Single.never() else Single.just(errorItem) }
            }

    fun <T> prepareSingleRequest(callable: () -> Single<T>): Flowable<T> = prepareSingleRequest(null, callable)

    fun <T> prepareSingleRequest(errorItem: T?, callable: () -> Single<T>): Flowable<T> =
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

    fun wrapCompletableRequest(callable: () -> Completable): Flowable<Unit> =
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

    fun prepareCompletableRequest(callable: () -> Completable): Flowable<Unit> =
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

    // region Not Reactive

    fun <T> wrapCallableRequest(callable: () -> T): Flowable<T> = wrapSingleRequest(null) { Single.fromCallable(callable) }

    fun <T> wrapCallableRequest(errorItem: T?, callable: () -> T): Flowable<T> = wrapSingleRequest(errorItem) { Single.fromCallable(callable) }

    fun <T> prepareCallableRequest(callable: () -> T): Flowable<T> = prepareSingleRequest(null) { Single.fromCallable(callable) }

    fun <T> prepareCallableRequest(errorItem: T?, callable: () -> T): Flowable<T> = prepareSingleRequest(errorItem) { Single.fromCallable(callable) }

    fun wrapActionRequest(action: () -> Unit): Flowable<Unit> = wrapCompletableRequest { Completable.fromAction(action) }

    fun prepareActionRequest(action: () -> Unit): Flowable<Unit> = prepareCompletableRequest { Completable.fromAction(action) }

    // endregion
}