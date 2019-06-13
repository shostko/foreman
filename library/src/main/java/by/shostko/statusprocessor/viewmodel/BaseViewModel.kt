package by.shostko.statusprocessor.viewmodel

import androidx.annotation.CallSuper
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import by.shostko.statusprocessor.Status
import by.shostko.statusprocessor.StatusProcessor
import by.shostko.statusprocessor.Direction
import by.shostko.statusprocessor.RealItemsCountProvider
import io.reactivex.Flowable
import io.reactivex.functions.BiFunction
import io.reactivex.processors.BehaviorProcessor

@Suppress("unused", "MemberVisibilityCanBePrivate")
abstract class BaseViewModel<E>(
    private val noError: E,
    statusProcessorCreator: (LifecycleOwner) -> StatusProcessor<out Status<E>>
) : LifecycledViewModel() {

    protected val statusProcessor by lazy { statusProcessorCreator.invoke(this) }

    protected val itemsEmptyFlowableProcessor = BehaviorProcessor.createDefault(true)

    private val errorFlowableProcessor = BehaviorProcessor.createDefault(noError)

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

    val error: Flowable<E> = Flowable.merge(
        statusProcessor.status.map { it.error ?: noError },
        errorFlowableProcessor
    )
        .distinctUntilChanged()

    @CallSuper
    open fun retry() {
        errorFlowableProcessor.onNext(noError)
        statusProcessor.retry()
    }

    @CallSuper
    open fun refresh() {
        errorFlowableProcessor.onNext(noError)
        statusProcessor.refresh()
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