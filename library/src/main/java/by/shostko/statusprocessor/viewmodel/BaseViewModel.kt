@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package by.shostko.statusprocessor.viewmodel

import androidx.annotation.CallSuper
import androidx.recyclerview.widget.RecyclerView
import by.shostko.statusprocessor.*
import io.reactivex.Flowable
import io.reactivex.functions.BiFunction
import io.reactivex.processors.BehaviorProcessor

abstract class SimpleViewModel : CustomViewModel<Unit>(Unit, SimpleStatusFactory())

abstract class CustomViewModel<E>(noError: E, factory: Status.Factory<E>) : LifecycledViewModel() {

    private val noErrorPair: Pair<Throwable, E> = Pair(Throwable(), noError)

    protected val statusProcessor by lazy { StatusProcessor(this, factory) }

    protected val itemsEmptyFlowableProcessor = BehaviorProcessor.createDefault(true)

    private val errorFlowableProcessor = BehaviorProcessor.createDefault(noErrorPair)

    private var itemsDataObserver: BaseItemsObserver? = null

    val progress: Flowable<Direction> = Flowable.combineLatest(
        statusProcessor.status
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

    val throwable: Flowable<Throwable> = Flowable.merge(
        statusProcessor.status.map { it.throwable ?: noErrorPair.first },
        errorFlowableProcessor.map { it.first }
    ).distinctUntilChanged()

    val error: Flowable<E> = Flowable.merge(
        statusProcessor.status.map { it.error ?: noErrorPair.second },
        errorFlowableProcessor.map { it.second }
    ).distinctUntilChanged()

    @CallSuper
    open fun retry() {
        errorFlowableProcessor.onNext(noErrorPair)
        statusProcessor.retry()
    }

    @CallSuper
    open fun refresh() {
        errorFlowableProcessor.onNext(noErrorPair)
        statusProcessor.refresh()
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

    private inner class RecyclerItemsObserver(internal val adapter: RecyclerView.Adapter<*>) : BaseItemsObserver(adapter) {
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