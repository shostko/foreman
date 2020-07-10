@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package by.shostko.statushandler.v2

import android.os.Handler
import android.os.Looper

interface StatusHandler {

    val status: Status

    fun addOnStatusListener(listener: OnStatusListener)
    fun removeOnStatusListener(listener: OnStatusListener)

    interface OnStatusListener {
        fun onStatus(status: Status)
    }

    interface Callback {
        fun status(status: Status)
        fun success() = status(Status.Success)
        fun working() = status(Status.Working(Status.WORKING))
        fun working(flag: Int) = status(Status.Working(flag))
        fun failed(throwable: Throwable?) = status(Status.Failed(throwable))
    }

    companion object {
        fun wrap(
            handler: Handler = Handler(Looper.getMainLooper()),
            func: (Callback) -> Unit
        ): WrappedStatusHandler = WrappedStatusHandlerImpl(handler, func)

        fun prepare(
            handler: Handler = Handler(Looper.getMainLooper()),
            func: (Callback) -> Unit
        ): PreparedStatusHandler = PreparedStatusHandlerImpl(handler, func)

        fun <P : Any?> await(
            handler: Handler = Handler(Looper.getMainLooper()),
            func: (P, Callback) -> Unit
        ): AwaitStatusHandler<P> = AwaitStatusHandlerImpl(handler, func)
    }
}

interface WrappedStatusHandler : StatusHandler {
    fun refresh()
}

interface PreparedStatusHandler : StatusHandler {
    fun proceed()
}

interface AwaitStatusHandler<P : Any?> : StatusHandler {
    fun proceed(param: P)
}

abstract class AbsStatusHandler : StatusHandler {

    protected val onStatusListeners: MutableSet<StatusHandler.OnStatusListener> = HashSet()

    protected fun notifyListeners(status: Status) {
        onStatusListeners.forEach { it.onStatus(status) } // TODO synchronize
    }

    override fun addOnStatusListener(listener: StatusHandler.OnStatusListener) {
        val sizeBefore = onStatusListeners.size
        onStatusListeners.add(listener)
        if (sizeBefore == 0 && onStatusListeners.size > 0) {
            onFirstListenerAdded()
        }
    }

    override fun removeOnStatusListener(listener: StatusHandler.OnStatusListener) {
        val sizeBefore = onStatusListeners.size
        onStatusListeners.remove(listener)
        if (sizeBefore > 0 && onStatusListeners.size == 0) {
            onLastListenerRemoved()
        }
    }

    protected open fun hasListeners(): Boolean = onStatusListeners.size > 0

    protected open fun onFirstListenerAdded() {}

    protected open fun onLastListenerRemoved() {}
}

abstract class BaseStatusHandler : AbsStatusHandler(), StatusHandler.Callback {

    @Volatile
    final override var status: Status = Status.Initial
        private set(value) {
            if (field != value) {
                field = value
                notifyListeners(value)
            }
        }

    override fun status(status: Status) {
        this.status = status
    }
}

internal class WrappedStatusHandlerImpl(
    private val handler: Handler,
    private val func: (StatusHandler.Callback) -> Unit
) : BaseStatusHandler(), WrappedStatusHandler {

    init {
        refresh()
    }

    override fun refresh() {
        handler.post { func(this) }
    }
}

internal class PreparedStatusHandlerImpl(
    private val handler: Handler,
    private val func: (StatusHandler.Callback) -> Unit
) : BaseStatusHandler(), PreparedStatusHandler {

    override fun proceed() {
        handler.post { func(this) }
    }
}

internal class AwaitStatusHandlerImpl<P>(
    private val handler: Handler,
    private val func: (P, StatusHandler.Callback) -> Unit
) : BaseStatusHandler(), AwaitStatusHandler<P> {

    override fun proceed(param: P) {
        handler.post { func(param, this) }
    }
}