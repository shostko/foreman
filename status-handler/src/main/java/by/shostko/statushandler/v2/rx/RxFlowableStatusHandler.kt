@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package by.shostko.statushandler.v2.rx

import by.shostko.statushandler.v2.*
import io.reactivex.*
import io.reactivex.disposables.Disposable
import io.reactivex.processors.FlowableProcessor
import io.reactivex.processors.PublishProcessor
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.atomic.AtomicBoolean

internal abstract class BaseFlowableStatusHandler<P : Any?, V : Any>(
    private val scheduler: Scheduler, // TODO think about second scheduler
    private val func: (P) -> Flowable<V>
) : BaseValueStatusHandler<V>() {

    protected abstract val actionFlowable: Flowable<Optional<P>>

    internal val valueFlowable: Flowable<V> by lazy {
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

    private val resultCompletable: Completable by lazy { valueFlowable.ignoreElements() }

    private var disposabe: Disposable? = null

    private fun subscribe() {
        disposabe = resultCompletable.subscribe()
    }

    private fun dispose() {
        disposabe?.dispose()
    }

    override fun addOnStatusListener(listener: StatusHandler.OnStatusListener) {
        val sizeBefore = onStatusListeners.size
        super.addOnStatusListener(listener)
        if (sizeBefore == 0 && onStatusListeners.size > 0 && onValueListeners.size == 0) {
            subscribe()
        }
    }

    override fun removeOnStatusListener(listener: StatusHandler.OnStatusListener) {
        val sizeBefore = onStatusListeners.size
        super.removeOnStatusListener(listener)
        if (sizeBefore > 0 && onStatusListeners.size == 0 && onValueListeners.size == 0) {
            dispose()
        }
    }

    override fun addOnValueListener(listener: ValueHandler.OnValueListener<V>) {
        val sizeBefore = onValueListeners.size
        super.addOnValueListener(listener)
        if (sizeBefore == 0 && onValueListeners.size > 0 && onStatusListeners.size == 0) {
            subscribe()
        }
    }

    override fun removeOnValueListener(listener: ValueHandler.OnValueListener<V>) {
        val sizeBefore = onValueListeners.size
        super.removeOnValueListener(listener)
        if (sizeBefore > 0 && onValueListeners.size == 0 && onStatusListeners.size == 0) {
            dispose()
        }
    }
}

internal class WrappedFlowableStatusHandler<V : Any>(
    scheduler: Scheduler,
    func: () -> Flowable<V>
) : BaseFlowableStatusHandler<Unit, V>(scheduler, { func() }),
    WrappedValueStatusHandler<V> {

    private val actionProcessor: FlowableProcessor<Unit> = PublishProcessor.create()

    private val started: AtomicBoolean = AtomicBoolean(false)

    override val actionFlowable: Flowable<Optional<Unit>> = actionProcessor.startOnce(Unit).map { Optional(it) }.share()

    override fun refresh() {
        actionProcessor.onNext(Unit)
    }
}

internal class PreparedFlowableStatusHandler<V : Any>(
    scheduler: Scheduler,
    func: () -> Flowable<V>
) : BaseFlowableStatusHandler<Unit, V>(scheduler, { func() }),
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
) : BaseFlowableStatusHandler<P, V>(scheduler, func),
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