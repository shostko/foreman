@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package by.shostko.statushandler.viewmodel

import androidx.recyclerview.widget.RecyclerView
import by.shostko.statushandler.*
import io.reactivex.Flowable

abstract class MultiProcessingViewModel<E>(private val noError: E, factory: Status.Factory<E>?) : CustomViewModel<E>(noError, factory) {

    private val registry: MutableMap<Any, StatusDelegate<E>> = HashMap()

    private fun delegate(key: Any): StatusDelegate<E> = registry[key] ?: synchronized(registry) {
        registry[key] ?: run {
            val statusHandler = StatusHandlerImpl(requireFactory())
            val statusDelegate = StatusDelegate(noError, statusHandler)
            registry[key] = statusDelegate
            statusDelegate
        }
    }

    protected fun statusHandler(key: Any) = delegate(key).statusHandler

    override fun createBaseStatusHandler(factory: Status.Factory<E>): StatusHandler<E> = StatusHandlerWrapper()

    fun proceed(key: Any) = delegate(key).proceed()

    fun refresh(key: Any) = delegate(key).refresh()

    fun retry(key: Any) = delegate(key).retry()

    fun status(key: Any): Flowable<Status<E>> = delegate(key).status

    fun hasItems(key: Any): Flowable<Boolean> = delegate(key).hasItems

    fun progress(key: Any): Flowable<Direction> = delegate(key).progress

    fun throwable(key: Any): Flowable<Throwable> = delegate(key).throwable

    fun error(key: Any): Flowable<E> = delegate(key).error

    protected fun postCollectionSize(key: Any, collection: Collection<*>) = delegate(key).postCollectionSize(collection)

    protected fun postCollectionSize(key: Any, itemCount: Int) = delegate(key).postCollectionSize(itemCount)

    fun registerWith(key: Any, adapter: RecyclerView.Adapter<*>) = delegate(key).registerWith(adapter)

    fun unregisterFrom(key: Any, adapter: RecyclerView.Adapter<*>) = delegate(key).unregisterFrom(adapter)
}