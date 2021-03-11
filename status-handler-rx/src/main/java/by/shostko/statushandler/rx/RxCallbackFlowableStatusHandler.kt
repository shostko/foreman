@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package by.shostko.statushandler.rx

import by.shostko.statushandler.AwaitValueStatusHandler
import by.shostko.statushandler.PreparedValueStatusHandler
import by.shostko.statushandler.StatusHandler
import by.shostko.statushandler.WrappedValueStatusHandler
import io.reactivex.*
import io.reactivex.processors.FlowableProcessor
import io.reactivex.processors.PublishProcessor
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.atomic.AtomicBoolean

internal abstract class RxCallbackFlowableStatusHandler<P : Any?, V : Any>(
    private val scheduler: Scheduler, // TODO think about second scheduler
    private val func: (P, StatusHandler.Callback) -> Flowable<V>
) : BaseFlowableStatusHandler<P, V>() {

    final override val valueFlowable: Flowable<V> by lazy {
        actionFlowable
            .switchMap { param ->
                Flowable.defer { func(param.value, this) }
                    .subscribeOn(scheduler)
                    .doOnNext { value(it) }
                    .onErrorResumeNext { th: Throwable ->
                        failed(th)
                        Flowable.empty()
                    }
            }
            .share()
    }
}

internal class WrappedCallbackFlowableStatusHandler<V : Any>(
    scheduler: Scheduler,
    func: (StatusHandler.Callback) -> Flowable<V>
) : RxCallbackFlowableStatusHandler<Unit, V>(scheduler, { _, callback -> func(callback) }),
    WrappedValueStatusHandler<V> {

    private val actionProcessor: FlowableProcessor<Unit> = PublishProcessor.create()

    private val started: AtomicBoolean = AtomicBoolean(false)

    override val actionFlowable: Flowable<Optional<Unit>> = actionProcessor.startOnce(Unit).map { Optional(it) }.share()

    override fun refresh() {
        actionProcessor.onNext(Unit)
    }
}

internal class PreparedCallbackFlowableStatusHandler<V : Any>(
    scheduler: Scheduler,
    func: (StatusHandler.Callback) -> Flowable<V>
) : RxCallbackFlowableStatusHandler<Unit, V>(scheduler, { _, callback -> func(callback) }),
    PreparedValueStatusHandler<V> {

    private val actionProcessor: FlowableProcessor<Unit> = PublishProcessor.create()

    override val actionFlowable: Flowable<Optional<Unit>> = actionProcessor.map { Optional(it) }

    override fun proceed() {
        actionProcessor.onNext(Unit)
    }
}

internal class AwaitCallbackFlowableStatusHandler<P : Any?, V : Any>(
    scheduler: Scheduler,
    func: (P, StatusHandler.Callback) -> Flowable<V>
) : RxCallbackFlowableStatusHandler<P, V>(scheduler, func),
    AwaitValueStatusHandler<P, V> {

    private val actionProcessor: FlowableProcessor<Optional<P>> = PublishProcessor.create()

    override val actionFlowable: Flowable<Optional<P>> = actionProcessor.hide()

    override fun proceed(param: P) {
        actionProcessor.onNext(Optional(param))
    }
}

fun <V : Any> StatusHandler.Companion.wrapFlowableWithCallback(
    scheduler: Scheduler = Schedulers.io(),
    func: (StatusHandler.Callback) -> Flowable<V>
): WrappedValueStatusHandler<V> = WrappedCallbackFlowableStatusHandler(scheduler, func)

fun <V : Any> StatusHandler.Companion.prepareFlowableWithCallback(
    scheduler: Scheduler = Schedulers.io(),
    func: (StatusHandler.Callback) -> Flowable<V>
): PreparedValueStatusHandler<V> = PreparedCallbackFlowableStatusHandler(scheduler, func)

fun <P : Any?, V : Any> StatusHandler.Companion.awaitFlowableWithCallback(
    scheduler: Scheduler = Schedulers.io(),
    func: (P, StatusHandler.Callback) -> Flowable<V>
): AwaitValueStatusHandler<P, V> = AwaitCallbackFlowableStatusHandler(scheduler, func)

fun <V : Any> StatusHandler.Companion.wrapObservableWithCallback(
    scheduler: Scheduler = Schedulers.io(),
    func: (StatusHandler.Callback) -> Observable<V>
): WrappedValueStatusHandler<V> = WrappedCallbackFlowableStatusHandler(scheduler) { func(it).toFlowable(BackpressureStrategy.LATEST) }

fun <V : Any> StatusHandler.Companion.prepareObservableWithCallback(
    scheduler: Scheduler = Schedulers.io(),
    func: (StatusHandler.Callback) -> Observable<V>
): PreparedValueStatusHandler<V> = PreparedCallbackFlowableStatusHandler(scheduler) { func(it).toFlowable(BackpressureStrategy.LATEST) }

fun <P : Any?, V : Any> StatusHandler.Companion.awaitObservableWithCallback(
    scheduler: Scheduler = Schedulers.io(),
    func: (P, StatusHandler.Callback) -> Observable<V>
): AwaitValueStatusHandler<P, V> = AwaitCallbackFlowableStatusHandler(scheduler) { param, callback -> func(param, callback).toFlowable(BackpressureStrategy.LATEST) }

fun <V : Any> StatusHandler.Companion.wrapSingleWithCallback(
    scheduler: Scheduler = Schedulers.io(),
    func: (StatusHandler.Callback) -> Single<V>
): WrappedValueStatusHandler<V> = WrappedCallbackFlowableStatusHandler(scheduler) { func(it).toFlowable() }

fun <V : Any> StatusHandler.Companion.prepareSingleWithCallback(
    scheduler: Scheduler = Schedulers.io(),
    func: (StatusHandler.Callback) -> Single<V>
): PreparedValueStatusHandler<V> = PreparedCallbackFlowableStatusHandler(scheduler) { func(it).toFlowable() }

fun <P : Any?, V : Any> StatusHandler.Companion.awaitSingleWithCallback(
    scheduler: Scheduler = Schedulers.io(),
    func: (P, StatusHandler.Callback) -> Single<V>
): AwaitValueStatusHandler<P, V> = AwaitCallbackFlowableStatusHandler(scheduler) { param, callback -> func(param, callback).toFlowable() }

fun <V : Any> StatusHandler.Companion.wrapMaybeWithCallback(
    scheduler: Scheduler = Schedulers.io(),
    func: (StatusHandler.Callback) -> Maybe<V>
): WrappedValueStatusHandler<V> = WrappedCallbackFlowableStatusHandler(scheduler) { func(it).toFlowable() }

fun <V : Any> StatusHandler.Companion.prepareMaybeWithCallback(
    scheduler: Scheduler = Schedulers.io(),
    func: (StatusHandler.Callback) -> Maybe<V>
): PreparedValueStatusHandler<V> = PreparedCallbackFlowableStatusHandler(scheduler) { func(it).toFlowable() }

fun <P : Any?, V : Any> StatusHandler.Companion.awaitMaybeWithCallback(
    scheduler: Scheduler = Schedulers.io(),
    func: (P, StatusHandler.Callback) -> Maybe<V>
): AwaitValueStatusHandler<P, V> = AwaitCallbackFlowableStatusHandler(scheduler) { param, callback -> func(param, callback).toFlowable() }