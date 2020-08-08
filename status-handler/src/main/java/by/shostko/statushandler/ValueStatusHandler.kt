@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package by.shostko.statushandler

import android.os.Handler
import android.os.Looper
import java.util.*

interface ValueHandler<V : Any> {

    val value: V?

    fun addOnValueListener(listener: OnValueListener<V>)
    fun removeOnValueListener(listener: OnValueListener<V>)

    interface OnValueListener<V> {
        fun onValue(value: V)
    }

    interface Callback<V> {
        fun value(value: V)
    }
}

interface ValueStatusHandler<V : Any> : StatusHandler, ValueHandler<V> {
    interface Callback<V> : StatusHandler.Callback, ValueHandler.Callback<V>
}

fun <V : Any> StatusHandler.Companion.wrap(
    handler: Handler = Handler(Looper.getMainLooper()),
    func: (ValueStatusHandler.Callback<V>) -> Unit
): WrappedValueStatusHandler<V> = WrappedValueStatusHandlerImpl(handler, func)

fun <V : Any> StatusHandler.Companion.prepare(
    handler: Handler = Handler(Looper.getMainLooper()),
    func: (ValueStatusHandler.Callback<V>) -> Unit
): PreparedValueStatusHandler<V> = PreparedValueStatusHandlerImpl(handler, func)

fun <P : Any?, V : Any> StatusHandler.Companion.await(
    handler: Handler = Handler(Looper.getMainLooper()),
    func: (P, ValueStatusHandler.Callback<V>) -> Unit
): AwaitValueStatusHandler<P, V> = AwaitValueStatusHandlerImpl(handler, func)

interface WrappedValueStatusHandler<V : Any> : ValueStatusHandler<V>, WrappedStatusHandler

interface PreparedValueStatusHandler<V : Any> : ValueStatusHandler<V>, PreparedStatusHandler

interface AwaitValueStatusHandler<P : Any?, V : Any> : ValueStatusHandler<V>, AwaitStatusHandler<P>

internal abstract class BaseValueStatusHandler<V : Any> : BaseStatusHandler(), ValueStatusHandler<V>, ValueStatusHandler.Callback<V> {

    @Volatile
    final override var value: V? = null
        private set(value) {
            if (field != value && value != null) {
                field = value
                onValueListeners.forEach { it.onValue(value) } // TODO synchronize
            }
        }

    protected val onValueListeners: MutableSet<ValueHandler.OnValueListener<V>> = HashSet()

    override fun addOnStatusListener(listener: StatusHandler.OnStatusListener) {
        val sizeBefore = onStatusListeners.size
        onStatusListeners.add(listener)
        if (sizeBefore == 0 && onStatusListeners.size > 0 && onValueListeners.size == 0) {
            onFirstListenerAdded()
        }
    }

    override fun removeOnStatusListener(listener: StatusHandler.OnStatusListener) {
        val sizeBefore = onStatusListeners.size
        onStatusListeners.remove(listener)
        if (sizeBefore > 0 && onStatusListeners.size == 0 && onValueListeners.size == 0) {
            onLastListenerRemoved()
        }
    }

    override fun addOnValueListener(listener: ValueHandler.OnValueListener<V>) {
        val sizeBefore = onValueListeners.size
        onValueListeners.add(listener)
        if (sizeBefore == 0 && onValueListeners.size > 0 && onStatusListeners.size == 0) {
            onFirstListenerAdded()
        }
    }

    override fun removeOnValueListener(listener: ValueHandler.OnValueListener<V>) {
        val sizeBefore = onValueListeners.size
        onValueListeners.remove(listener)
        if (sizeBefore > 0 && onValueListeners.size == 0 && onStatusListeners.size == 0) {
            onLastListenerRemoved()
        }
    }

    override fun hasListeners(): Boolean = super.hasListeners() || onValueListeners.size > 0

    override fun value(value: V) {
        this.value = value
    }
}

abstract class AbsValueHandler<V: Any> : ValueHandler<V> {

    protected val onValueListeners: MutableSet<ValueHandler.OnValueListener<V>> = HashSet()

    protected fun notifyListeners(value: V) {
        onValueListeners.forEach { it.onValue(value) } // TODO synchronize
    }

    override fun addOnValueListener(listener: ValueHandler.OnValueListener<V>) {
        val sizeBefore = onValueListeners.size
        onValueListeners.add(listener)
        if (sizeBefore == 0 && onValueListeners.size > 0) {
            onFirstListenerAdded()
        }
    }

    override fun removeOnValueListener(listener: ValueHandler.OnValueListener<V>) {
        val sizeBefore = onValueListeners.size
        onValueListeners.remove(listener)
        if (sizeBefore > 0 && onValueListeners.size == 0) {
            onLastListenerRemoved()
        }
    }

    protected open fun hasListeners(): Boolean = onValueListeners.size > 0

    protected open fun onFirstListenerAdded() {}

    protected open fun onLastListenerRemoved() {}
}

internal class WrappedValueStatusHandlerImpl<V : Any>(
    private val handler: Handler,
    private val func: (ValueStatusHandler.Callback<V>) -> Unit
) : BaseValueStatusHandler<V>(), WrappedValueStatusHandler<V> {

    init {
        refresh()
    }

    override fun refresh() {
        handler.post { func(this) }
    }
}

internal class PreparedValueStatusHandlerImpl<V : Any>(
    private val handler: Handler,
    private val func: (ValueStatusHandler.Callback<V>) -> Unit
) : BaseValueStatusHandler<V>(), PreparedValueStatusHandler<V> {

    override fun proceed() {
        handler.post { func(this) }
    }
}

internal class AwaitValueStatusHandlerImpl<P : Any?, V : Any>(
    private val handler: Handler,
    private val func: (P, ValueStatusHandler.Callback<V>) -> Unit
) : BaseValueStatusHandler<V>(), AwaitValueStatusHandler<P, V> {

    override fun proceed(param: P) {
        handler.post { func(param, this) }
    }
}
