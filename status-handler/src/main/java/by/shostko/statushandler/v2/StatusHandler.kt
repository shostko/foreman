@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package by.shostko.statushandler.v2

import android.os.Handler
import android.os.Looper
import java.util.*
import kotlin.collections.HashSet

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
        fun wrap(func: (Callback) -> Unit): WrappedStatusHandler = WrappedStatusHandlerImpl(func)
        fun prepare(func: (Callback) -> Unit): PreparedStatusHandler = PreparedStatusHandlerImpl(func)
        fun <P : Any?> await(func: (P, Callback) -> Unit): AwaitStatusHandler<P> = AwaitStatusHandlerImpl(func)
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

internal abstract class BaseStatusHandler : StatusHandler, StatusHandler.Callback {

    final override var status: Status = Status.Initial
        private set(value) {
            if (field != value) {
                field = value
                onStatusListeners.forEach { it.onStatus(value) } // TODO synchronize
            }
        }

    protected val onStatusListeners: MutableSet<StatusHandler.OnStatusListener> = HashSet()

    override fun addOnStatusListener(listener: StatusHandler.OnStatusListener) {
        onStatusListeners.add(listener)
    }

    override fun removeOnStatusListener(listener: StatusHandler.OnStatusListener) {
        onStatusListeners.remove(listener)
    }

    override fun status(status: Status) {
        this.status = status
    }
}

internal class WrappedStatusHandlerImpl(
    private val func: (StatusHandler.Callback) -> Unit
) : BaseStatusHandler(), WrappedStatusHandler {

    init {
        Handler(Looper.getMainLooper()).post {
            refresh()
        }
    }

    override fun refresh() {
        func(this)
    }
}

internal class PreparedStatusHandlerImpl(
    private val func: (StatusHandler.Callback) -> Unit
) : BaseStatusHandler(), PreparedStatusHandler {

    override fun proceed() {
        func(this)
    }
}

internal class AwaitStatusHandlerImpl<P>(
    private val func: (P, StatusHandler.Callback) -> Unit
) : BaseStatusHandler(), AwaitStatusHandler<P> {

    override fun proceed(param: P) {
        func(param, this)
    }
}