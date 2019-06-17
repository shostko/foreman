@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package by.shostko.statushandler.viewmodel

import androidx.annotation.CallSuper
import androidx.recyclerview.widget.RecyclerView
import by.shostko.statushandler.*
import io.reactivex.Flowable
import io.reactivex.functions.BiFunction
import io.reactivex.processors.BehaviorProcessor

abstract class SimpleViewModel : CustomViewModel<Unit>(Unit, SimpleStatusFactory()) {
    abstract class Message : CustomViewModel<String>("", MessageStatus.Factory())
    abstract class Class : CustomViewModel<java.lang.Class<out Throwable>>(NoErrorThrowable::class.java, ClassStatus.Factory())
}

abstract class CustomViewModel<E>(noError: E, factory: Status.Factory<E>) : LifecycledViewModel() {

    private val noErrorPair = Pair(NoErrorThrowable(), noError)

    protected val statusHandler by lazy { StatusHandler(factory) }

    protected val itemsEmptyFlowableProcessor = BehaviorProcessor.createDefault(true)

    private var itemsDataObserver: BaseItemsObserver? = null

    val status: Flowable<Status<E>> = statusHandler.status

    val progress: Flowable<Direction> = Flowable.combineLatest(
        statusHandler.status
            .distinctUntilChanged()
            .map { it.direction },
        itemsEmptyFlowableProcessor
            .distinctUntilChanged(),
        BiFunction<Direction, Boolean, Direction> { direction, isEmpty ->
            when {
                direction == Direction.NONE -> Direction.NONE
                isEmpty -> Direction.FULL
                direction == Direction.FULL -> Direction.BACKWARD
                else -> direction
            }
        })
        .distinctUntilChanged()

    val throwable: Flowable<Throwable> = statusHandler.status.map { it.throwable ?: noErrorPair.first }

    val error: Flowable<E> = statusHandler.status.map { it.error ?: noErrorPair.second }

    @CallSuper
    open fun retry() {
        statusHandler.retry()
    }

    @CallSuper
    open fun refresh() {
        statusHandler.refresh()
    }

    protected fun postCollectionSize(collection: Collection<*>) {
        itemsEmptyFlowableProcessor.onNext(collection.isEmpty())
    }

    protected fun postCollectionSize(itemCount: Int) {
        itemsEmptyFlowableProcessor.onNext(itemCount == 0)
    }

    fun registerWith(adapter: RecyclerView.Adapter<*>) {
        if (itemsDataObserver != null) {
            throw UnsupportedOperationException("You need to unregister from previous adapter!")
        }
        itemsDataObserver = if (adapter is RealItemsCountProvider) {
            RealItemsObserver(adapter)
        } else {
            RecyclerItemsObserver(adapter)
        }
        itemsDataObserver?.let { adapter.registerAdapterDataObserver(it) }
    }

    fun unregisterFrom(adapter: RecyclerView.Adapter<*>) {
        if (itemsDataObserver?.anchor === adapter) {
            adapter.unregisterAdapterDataObserver(itemsDataObserver!!)
            itemsDataObserver = null
        }
    }

    private abstract inner class BaseItemsObserver(internal val anchor: Any) : RecyclerView.AdapterDataObserver() {

        override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
            onChanged()
        }

        override fun onItemRangeChanged(positionStart: Int, itemCount: Int, payload: Any?) {
            onChanged()
        }

        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            onChanged()
        }

        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
            onChanged()
        }

        override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
            onChanged()
        }
    }

    private inner class RecyclerItemsObserver(internal val adapter: RecyclerView.Adapter<*>) :
        BaseItemsObserver(adapter) {
        override fun onChanged() {
            postCollectionSize(adapter.itemCount)
        }
    }

    private inner class RealItemsObserver(internal val adapter: RealItemsCountProvider) : BaseItemsObserver(adapter) {
        override fun onChanged() {
            postCollectionSize(adapter.getRealItemsCount())
        }
    }
}