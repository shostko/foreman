@file:Suppress("CanBeParameter", "unused")

package by.shostko.statushandler.viewmodel

import androidx.recyclerview.widget.RecyclerView
import by.shostko.statushandler.*
import io.reactivex.Flowable
import io.reactivex.functions.BiFunction
import io.reactivex.processors.BehaviorProcessor

internal class StatusDelegate<E>(noError: E, internal val statusHandler: StatusHandler<E>) {

    private val noErrorPair = Pair(NoErrorThrowable(), noError)

    private val itemsEmptyFlowableProcessor = BehaviorProcessor.createDefault(true)

    private var itemsDataObserver: BaseItemsObserver? = null

    fun proceed() {
        statusHandler.proceed()
    }

    fun refresh() {
        statusHandler.refresh()
    }

    fun retry() {
        statusHandler.retry()
    }

    val status: Flowable<Status<E>> = statusHandler.status

    val hasItems: Flowable<Boolean> = itemsEmptyFlowableProcessor.map { !it }

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

    fun postCollectionSize(collection: Collection<*>) {
        itemsEmptyFlowableProcessor.onNext(collection.isEmpty())
    }

    fun postCollectionSize(itemCount: Int) {
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