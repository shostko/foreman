@file:Suppress("unused")

package by.shostko.statushandler.rx

import by.shostko.statushandler.*
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Scheduler
import io.reactivex.disposables.Disposable
import io.reactivex.processors.FlowableProcessor
import io.reactivex.processors.PublishProcessor
import io.reactivex.schedulers.Schedulers

internal abstract class BaseCompletableStatusHandler<P : Any?> : BaseStatusHandler() {

    protected abstract val actionFlowable: Flowable<Optional<P>>

    protected abstract val resultCompletable: Completable

    private var disposabe: Disposable? = null

    override fun onFirstListenerAdded() {
        disposabe = resultCompletable.subscribe()
    }

    override fun onLastListenerRemoved() {
        disposabe?.dispose()
    }
}

internal abstract class RxCompletableStatusHandler<P : Any?>(
    private val scheduler: Scheduler,
    private val func: (P) -> Completable
) : BaseCompletableStatusHandler<P>() {

    override val resultCompletable: Completable by lazy {
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
}

internal class WrappedCompletableStatusHandler(
    scheduler: Scheduler,
    func: () -> Completable
) : RxCompletableStatusHandler<Unit>(scheduler, { func() }),
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
) : RxCompletableStatusHandler<Unit>(scheduler, { func() }),
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
) : RxCompletableStatusHandler<P>(scheduler, func),
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