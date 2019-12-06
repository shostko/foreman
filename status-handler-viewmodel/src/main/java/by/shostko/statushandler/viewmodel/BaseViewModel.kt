@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package by.shostko.statushandler.viewmodel

import androidx.recyclerview.widget.RecyclerView
import by.shostko.statushandler.Direction
import by.shostko.statushandler.Status
import by.shostko.statushandler.StatusHandler
import io.reactivex.Flowable

abstract class CustomViewModel<E> @JvmOverloads constructor(noError: E, private val factory: Status.Factory<E>? = null) : LifecycledViewModel() {

    protected val statusHandler by lazy { createBaseStatusHandler(requireFactory()) }

    internal abstract fun createBaseStatusHandler(factory: Status.Factory<E>): StatusHandler<E>

    protected open fun requireFactory(): Status.Factory<E> = factory
        ?: throw UnsupportedOperationException("There is no correct Factory. You should provide it in constructor or override requireFactory()")

    internal val delegate: StatusDelegate<E> = StatusDelegate(noError, statusHandler)

    val status: Flowable<Status<E>> = delegate.status

    val hasItems: Flowable<Boolean> = delegate.hasItems

    val progress: Flowable<Direction> = delegate.progress

    val throwable: Flowable<Throwable> = delegate.throwable

    val error: Flowable<E> = delegate.error

    protected fun postCollectionSize(collection: Collection<*>) = delegate.postCollectionSize(collection)

    protected fun postCollectionSize(itemCount: Int) = delegate.postCollectionSize(itemCount)

    fun registerWith(adapter: RecyclerView.Adapter<*>) = delegate.registerWith(adapter)

    fun unregisterFrom(adapter: RecyclerView.Adapter<*>) = delegate.unregisterFrom(adapter)
}