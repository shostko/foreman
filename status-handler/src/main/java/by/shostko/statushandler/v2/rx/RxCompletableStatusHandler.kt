@file:Suppress("unused")

package by.shostko.statushandler.v2.rx

import by.shostko.statushandler.v2.*
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Scheduler
import io.reactivex.disposables.Disposable
import io.reactivex.processors.FlowableProcessor
import io.reactivex.processors.PublishProcessor
import io.reactivex.schedulers.Schedulers

internal abstract class BaseCompletableStatusHandler<P : Any?>(
    private val scheduler: Scheduler,
    private val func: (P) -> Completable
) : BaseStatusHandler() {

    protected abstract val actionFlowable: Flowable<Optional<P>>

    private val resultFlowable: Completable by lazy {
        actionFlowable
            .doOnNext { working() }
            .switchMapCompletable { param ->
                Completable.defer { func(param.value) }
                    .subscribeOn(scheduler)
                    .doOnComplete { success() }
                    .doOnError { failed(it) }
                    .onErrorResumeNext { Completable.complete() }
            }
    }

    private var disposabe: Disposable? = null

    override fun onFirstListenerAdded() {
        disposabe = resultFlowable.subscribe()
    }

    override fun onLastListenerRemoved() {
        disposabe?.dispose()
    }
}

internal class WrappedCompletableStatusHandler(
    scheduler: Scheduler,
    func: () -> Completable
) : BaseCompletableStatusHandler<Unit>(scheduler, { func() }),
    WrappedStatusHandler {

    private val actionProcessor: FlowableProcessor<Unit> = PublishProcessor.create()

    override val actionFlowable: Flowable<Optional<Unit>> = actionProcessor.startOnce(Unit).map { Optional(it) }

    override fun refresh() {
        actionProcessor.onNext(Unit)
    }
}

internal class PreparedCompletableStatusHandler(
    scheduler: Scheduler,
    func: () -> Completable
) : BaseCompletableStatusHandler<Unit>(scheduler, { func() }),
    PreparedStatusHandler {

    private val actionProcessor: FlowableProcessor<Unit> = PublishProcessor.create()

    override val actionFlowable: Flowable<Optional<Unit>> = actionProcessor.map { Optional(it) }

    override fun proceed() {
        actionProcessor.onNext(Unit)
    }
}

internal class AwaitCompletableStatusHandler<P : Any?>(
    scheduler: Scheduler,
    func: (P) -> Completable
) : BaseCompletableStatusHandler<P>(scheduler, func),
    AwaitStatusHandler<P> {

    private val actionProcessor: FlowableProcessor<Optional<P>> = PublishProcessor.create()

    override val actionFlowable: Flowable<Optional<P>> = actionProcessor.hide()

    override fun proceed(param: P) {
        actionProcessor.onNext(Optional(param))
    }
}

fun StatusHandler.Companion.wrapCompletable(
    scheduler: Scheduler = Schedulers.io(),
    func: () -> Completable
): WrappedStatusHandler = WrappedCompletableStatusHandler(scheduler, func)

fun StatusHandler.Companion.prepareCompletable(
    scheduler: Scheduler = Schedulers.io(),
    func: () -> Completable
): PreparedStatusHandler = PreparedCompletableStatusHandler(scheduler, func)

fun <P : Any?> StatusHandler.Companion.awaitCompletable(
    scheduler: Scheduler = Schedulers.io(),
    func: (P) -> Completable
): AwaitStatusHandler<P> = AwaitCompletableStatusHandler(scheduler, func)