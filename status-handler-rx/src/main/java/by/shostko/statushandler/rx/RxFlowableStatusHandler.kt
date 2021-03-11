@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package by.shostko.statushandler.rx

import by.shostko.statushandler.*
import by.shostko.statushandler.BaseValueStatusHandler
import io.reactivex.*
import io.reactivex.disposables.Disposable
import io.reactivex.processors.FlowableProcessor
import io.reactivex.processors.PublishProcessor
import io.reactivex.schedulers.Schedulers

internal abstract class BaseFlowableStatusHandler<P : Any?, V : Any>() : BaseValueStatusHandler<V>() {

    protected abstract val actionFlowable: Flowable<Optional<P>>

    internal abstract val valueFlowable: Flowable<V>

    private val resultCompletable: Completable by lazy { valueFlowable.ignoreElements() }

    private var disposabe: Disposable? = null

    override fun onFirstListenerAdded() {
        disposabe = resultCompletable.subscribe()
    }

    override fun onLastListenerRemoved() {
        disposabe?.dispose()
    }
}

internal abstract class RxFlowableStatusHandler<P : Any?, V : Any>(
    private val scheduler: Scheduler, // TODO think about second scheduler
    private val func: (P) -> Flowable<V>
) : BaseFlowableStatusHandler<P, V>() {

    final override val valueFlowable: Flowable<V> by lazy {
        actionFlowable
            .doOnNext { working() }
            .switchMap { param ->
                Flowable.defer { func(param.value) }
                    .subscribeOn(scheduler)
                    .doOnNext {
                        value(it)
                        success()
                    }
                    .onErrorResumeNext { th: Throwable ->
                        failed(th)
                        Flowable.empty()
                    }
            }
            .share()
    }
}

internal class WrappedFlowableStatusHandler<V : Any>(
    scheduler: Scheduler,
    func: () -> Flowable<V>
) : RxFlowableStatusHandler<Unit, V>(scheduler, { func() }),
    WrappedValueStatusHandler<V> {

    private val actionProcessor: FlowableProcessor<Unit> = PublishProcessor.create()

    override val actionFlowable: Flowable<Optional<Unit>> = actionProcessor.startOnce(Unit).map { Optional(it) }.share()

    override fun refresh() {
        actionProcessor.onNext(Unit)
    }
}

internal class PreparedFlowableStatusHandler<V : Any>(
    scheduler: Scheduler,
    func: () -> Flowable<V>
) : RxFlowableStatusHandler<Unit, V>(scheduler, { func() }),
    PreparedValueStatusHandler<V> {

    private val actionProcessor: FlowableProcessor<Unit> = PublishProcessor.create()

    override val actionFlowable: Flowable<Optional<Unit>> = actionProcessor.map { Optional(it) }

    override fun proceed() {
        actionProcessor.onNext(Unit)
    }
}

internal class AwaitFlowableStatusHandler<P : Any?, V : Any>(
    scheduler: Scheduler,
    func: (P) -> Flowable<V>
) : RxFlowableStatusHandler<P, V>(scheduler, func),
    AwaitValueStatusHandler<P, V> {

    private val actionProcessor: FlowableProcessor<Optional<P>> = PublishProcessor.create()

    override val actionFlowable: Flowable<Optional<P>> = actionProcessor.hide()

    override fun proceed(param: P) {
        actionProcessor.onNext(Optional(param))
    }
}

fun <V : Any> StatusHandler.Companion.wrapFlowable(
    scheduler: Scheduler = Schedulers.io(),
    func: () -> Flowable<V>
): WrappedValueStatusHandler<V> = WrappedFlowableStatusHandler(scheduler, func)

fun <V : Any> StatusHandler.Companion.prepareFlowable(
    scheduler: Scheduler = Schedulers.io(),
    func: () -> Flowable<V>
): PreparedValueStatusHandler<V> = PreparedFlowableStatusHandler(scheduler, func)

fun <P : Any?, V : Any> StatusHandler.Companion.awaitFlowable(
    scheduler: Scheduler = Schedulers.io(),
    func: (P) -> Flowable<V>
): AwaitValueStatusHandler<P, V> = AwaitFlowableStatusHandler(scheduler, func)

fun <V : Any> StatusHandler.Companion.wrapObservable(
    scheduler: Scheduler = Schedulers.io(),
    func: () -> Observable<V>
): WrappedValueStatusHandler<V> = WrappedFlowableStatusHandler(scheduler) { func().toFlowable(BackpressureStrategy.LATEST) }

fun <V : Any> StatusHandler.Companion.prepareObservable(
    scheduler: Scheduler = Schedulers.io(),
    func: () -> Observable<V>
): PreparedValueStatusHandler<V> = PreparedFlowableStatusHandler(scheduler) { func().toFlowable(BackpressureStrategy.LATEST) }

fun <P : Any?, V : Any> StatusHandler.Companion.awaitObservable(
    scheduler: Scheduler = Schedulers.io(),
    func: (P) -> Observable<V>
): AwaitValueStatusHandler<P, V> = AwaitFlowableStatusHandler(scheduler) { func(it).toFlowable(BackpressureStrategy.LATEST) }

fun <V : Any> StatusHandler.Companion.wrapSingle(
    scheduler: Scheduler = Schedulers.io(),
    func: () -> Single<V>
): WrappedValueStatusHandler<V> = WrappedFlowableStatusHandler(scheduler) { func().toFlowable() }

fun <V : Any> StatusHandler.Companion.prepareSingle(
    scheduler: Scheduler = Schedulers.io(),
    func: () -> Single<V>
): PreparedValueStatusHandler<V> = PreparedFlowableStatusHandler(scheduler) { func().toFlowable() }

fun <P : Any?, V : Any> StatusHandler.Companion.awaitSingle(
    scheduler: Scheduler = Schedulers.io(),
    func: (P) -> Single<V>
): AwaitValueStatusHandler<P, V> = AwaitFlowableStatusHandler(scheduler) { func(it).toFlowable() }

fun <V : Any> StatusHandler.Companion.wrapMaybe(
    scheduler: Scheduler = Schedulers.io(),
    func: () -> Maybe<V>
): WrappedValueStatusHandler<V> = WrappedFlowableStatusHandler(scheduler) { func().toFlowable() }

fun <V : Any> StatusHandler.Companion.prepareMaybe(
    scheduler: Scheduler = Schedulers.io(),
    func: () -> Maybe<V>
): PreparedValueStatusHandler<V> = PreparedFlowableStatusHandler(scheduler) { func().toFlowable() }

fun <P : Any?, V : Any> StatusHandler.Companion.awaitMaybe(
    scheduler: Scheduler = Schedulers.io(),
    func: (P) -> Maybe<V>
): AwaitValueStatusHandler<P, V> = AwaitFlowableStatusHandler(scheduler) { func(it).toFlowable() }