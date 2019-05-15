package by.shostko.statusprocessor.viewmodel

import androidx.annotation.CallSuper
import androidx.recyclerview.widget.RecyclerView
import by.shostko.statusprocessor.Direction
import by.shostko.statusprocessor.StatusProcessor
import io.reactivex.Flowable
import io.reactivex.functions.BiFunction
import io.reactivex.processors.BehaviorProcessor

@Suppress("unused")
abstract class BaseViewModel : LifecycledViewModel() {

    companion object {
        private const val EMPTY = ""
    }

    protected val statusProcessor by lazy { StatusProcessor(this) }

    private val itemCountFlowableProcessor = BehaviorProcessor.createDefault(true)

    private val messageFlowableProcessor = BehaviorProcessor.createDefault(EMPTY)

    private var adapterDataObserver: AdapterObserver? = null

    val progress: Flowable<Direction> = Flowable.combineLatest(
        statusProcessor.status
            .distinctUntilChanged()
            .map { it.direction },
        itemCountFlowableProcessor
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

    val error: Flowable<String> = Flowable.merge(
        statusProcessor.status.map { it.message ?: EMPTY },
        messageFlowableProcessor
    )
        .distinctUntilChanged()

    @CallSuper
    open fun retry() {
        messageFlowableProcessor.onNext(EMPTY)
        statusProcessor.retry()
    }

    @CallSuper
    open fun refresh() {
        messageFlowableProcessor.onNext(EMPTY)
        statusProcessor.refresh()
    }

    protected fun postCollectionSize(itemCount: Int) {
        itemCountFlowableProcessor.onNext(itemCount == 0)
    }

    fun registerWith(adapter: RecyclerView.Adapter<*>) {
        if (adapterDataObserver != null) {
            throw UnsupportedOperationException("You need to unregister from previous adapter!")
        }
        adapterDataObserver = AdapterObserver(adapter)
        adapter.registerAdapterDataObserver(adapterDataObserver!!)
    }

    fun unregisterFrom(adapter: RecyclerView.Adapter<*>) {
        if (adapterDataObserver?.adapter == adapter) {
            adapter.unregisterAdapterDataObserver(adapterDataObserver!!)
            adapterDataObserver = null
        }
    }

    private inner class AdapterObserver(internal val adapter: RecyclerView.Adapter<*>) : RecyclerView.AdapterDataObserver() {

        override fun onChanged() {
            postCollectionSize(adapter.itemCount)
        }

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
}