@file:Suppress("unused")

package by.shostko.statushandler.rx

import by.shostko.statushandler.AwaitStatusHandler
import by.shostko.statushandler.PreparedStatusHandler
import by.shostko.statushandler.StatusHandler
import by.shostko.statushandler.WrappedStatusHandler
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Scheduler
import io.reactivex.processors.FlowableProcessor
import io.reactivex.processors.PublishProcessor
import io.reactivex.schedulers.Schedulers

internal abstract class RxCallbackCompletableStatusHandler<P : Any?>(
    private val scheduler: Scheduler,
    private val func: (P, StatusHandler.Callback) -> Completable
) : BaseCompletableStatusHandler<P>() {

    override val resultCompletable: Completable by lazy {
        actionFlowable
            .switchMapCompletable { param ->
                Completable.defer { func(param.value, this) }
                    .subscribeOn(scheduler)
                    .doOnError { failed(it) }
                    .onErrorResumeNext { Completable.complete() }
            }
    }
}

internal class WrappedCallbackCompletableStatusHandler(
    scheduler: Scheduler,
    func: (StatusHandler.Callback) -> Completable
) : RxCallbackCompletableStatusHandler<Unit>(scheduler, { _, callback -> func(callback) }),
    WrappedStatusHandler {

    private val actionProcessor: FlowableProcessor<Unit> = PublishProcessor.create()

    override val actionFlowable: Flowable<Optional<Unit>> = actionProcessor.startOnce(Unit).map { Optional(it) }

    override fun refresh() {
        actionProcessor.onNext(Unit)
    }
}

internal class PreparedCallbackCompletableStatusHandler(
    scheduler: Scheduler,
    func: (StatusHandler.Callback) -> Completable
) : RxCallbackCompletableStatusHandler<Unit>(scheduler, { _, callback -> func(callback) }),
    PreparedStatusHandler {

    private val actionProcessor: FlowableProcessor<Unit> = PublishProcessor.create()

    override val actionFlowable: Flowable<Optional<Unit>> = actionProcessor.map { Optional(it) }

    override fun proceed() {
        actionProcessor.onNext(Unit)
    }
}

internal class AwaitCallbackCompletableStatusHandler<P : Any?>(
    scheduler: Scheduler,
    func: (P, StatusHandler.Callback) -> Completable
) : RxCallbackCompletableStatusHandler<P>(scheduler, func),
    AwaitStatusHandler<P> {

    private val actionProcessor: FlowableProcessor<Optional<P>> = PublishProcessor.create()

    override val actionFlowable: Flowable<Optional<P>> = actionProcessor.hide()

    override fun proceed(param: P) {
        actionProcessor.onNext(Optional(param))
    }
}

fun StatusHandler.Companion.wrapCompletableWithCallback(
    scheduler: Scheduler = Schedulers.io(),
    func: (StatusHandler.Callback) -> Completable
): WrappedStatusHandler = WrappedCallbackCompletableStatusHandler(scheduler, func)

fun StatusHandler.Companion.prepareCompletableWithCallback(
    scheduler: Scheduler = Schedulers.io(),
    func: (StatusHandler.Callback) -> Completable
): PreparedStatusHandler = PreparedCallbackCompletableStatusHandler(scheduler, func)

fun <P : Any?> StatusHandler.Companion.awaitCompletableWithCallback(
    scheduler: Scheduler = Schedulers.io(),
    func: (P, StatusHandler.Callback) -> Completable
): AwaitStatusHandler<P> = AwaitCallbackCompletableStatusHandler(scheduler, func)