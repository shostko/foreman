package by.shostko.statushandler

import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.processors.FlowableProcessor
import io.reactivex.processors.ReplayProcessor

@Suppress("UNCHECKED_CAST")
class StatusHandlerWrapper<E> : StatusHandler<E>() {

    private val wrapperProcessor: FlowableProcessor<StatusHandler<E>> = ReplayProcessor.create()

    private val handlers: Flowable<Set<StatusHandler<E>>> = wrapperProcessor.scan(emptySet<StatusHandler<E>>()) { oldSet, newHandler ->
        oldSet.toMutableSet().apply { add(newHandler) }
    }.share()

    override val status: Flowable<Status<E>> = handlers.map { set -> set.map { it.status } }
        .switchMap { statusFlowables ->
            when (statusFlowables.size) {
                0 -> Flowable.never()
                1 -> statusFlowables.first()
                else -> Flowable.combineLatest(statusFlowables.toTypedArray()) { arr ->
                    val statuses = arr.mapNotNull { it as? Status<E> }
                    val failed = statuses.firstOrNull { it.isFailed() }
                    val working = statuses.firstOrNull { it.isWorking() }
                    return@combineLatest when {
                        failed == null && working == null -> SimpleStatus<E>(Direction.NONE, null, null)
                        failed != null && working != null -> SimpleStatus(working.direction, failed.throwable, failed.error)
                        failed != null && working == null -> SimpleStatus(Direction.NONE, failed.throwable, failed.error)
                        failed == null && working != null -> SimpleStatus<E>(working.direction, null, null)
                        else -> throw IllegalStateException("Should never be thrown!")
                    } as Status<E>
                }
            }
        }.distinctUntilChanged()

    override val action: Flowable<Action>
        get() = throw UnsupportedOperationException("Wrapper can't handle request to action")

    fun wrap(handler: StatusHandler<E>): StatusHandlerWrapper<E> = this.apply {
        wrapperProcessor.onNext(handler)
    }

    // region action

    override fun proceed() = throw UnsupportedOperationException("Wrapper can't handle proceed method")

    override fun refresh() = throw UnsupportedOperationException("Wrapper can't handle refresh method")

    override fun retry() = throw UnsupportedOperationException("Wrapper can't handle retry method")

    // endregion

    // region status

    override fun updateWorking() = throw UnsupportedOperationException("Wrapper can't handle updateWorking method")

    override fun updateWorkingForward() = throw UnsupportedOperationException("Wrapper can't handle updateWorkingForward method")

    override fun updateWorkingBackward() = throw UnsupportedOperationException("Wrapper can't handle updateWorkingBackward method")

    override fun updateFailed(throwable: Throwable) = throw UnsupportedOperationException("Wrapper can't handle updateFailed method")

    override fun updateFailed(map: Map<String, Any>) = throw UnsupportedOperationException("Wrapper can't handle updateFailed method")

    override fun updateSuccess() = throw UnsupportedOperationException("Wrapper can't handle updateSuccess method")

    // endregion

    // region requests

    override fun <T> wrapFlowableRequest(errorItem: T?, callable: () -> Flowable<T>): Flowable<T> =
        throw UnsupportedOperationException("Wrapper can't handle wrapFlowableRequest method")

    override fun <T> prepareFlowableRequest(errorItem: T?, callable: () -> Flowable<T>): Flowable<T> =
        throw UnsupportedOperationException("Wrapper can't handle prepareFlowableRequest method")

    override fun <T> wrapSingleRequest(errorItem: T?, callable: () -> Single<T>): Flowable<T> =
        throw UnsupportedOperationException("Wrapper can't handle wrapSingleRequest method")

    override fun <T> prepareSingleRequest(errorItem: T?, callable: () -> Single<T>): Flowable<T> =
        throw UnsupportedOperationException("Wrapper can't handle prepareSingleRequest method")

    override fun wrapCompletableRequest(callable: () -> Completable): Flowable<Unit> =
        throw UnsupportedOperationException("Wrapper can't handle wrapCompletableRequest method")

    override fun prepareCompletableRequest(callable: () -> Completable): Flowable<Unit> =
        throw UnsupportedOperationException("Wrapper can't handle prepareCompletableRequest method")

    // endregion
}