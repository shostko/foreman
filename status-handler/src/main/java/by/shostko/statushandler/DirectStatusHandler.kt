@file:Suppress("unused")

package by.shostko.statushandler

import android.os.Handler
import android.os.Looper

fun StatusHandler.Companion.wrapAction(
    handler: Handler = Handler(Looper.getMainLooper()),
    func: () -> Unit
) = wrap(handler, ActionWrapper(func))

fun StatusHandler.Companion.prepareAction(
    handler: Handler = Handler(Looper.getMainLooper()),
    func: () -> Unit
) = prepare(handler, ActionWrapper(func))

fun <P> StatusHandler.Companion.awaitAction(
    handler: Handler = Handler(Looper.getMainLooper()),
    func: (P) -> Unit
) = await(handler, ParametrizedActionWrapper(func))

fun <V : Any> StatusHandler.Companion.wrapCallable(
    handler: Handler = Handler(Looper.getMainLooper()),
    func: () -> V
) = wrap(handler, CallableWrapper(func))

fun <V : Any> StatusHandler.Companion.prepareCallable(
    handler: Handler = Handler(Looper.getMainLooper()),
    func: () -> V
) = prepare(handler, CallableWrapper(func))

fun <V : Any, P : Any?> StatusHandler.Companion.awaitCallable(
    handler: Handler = Handler(Looper.getMainLooper()),
    func: (P) -> V
) = await(handler, ParametrizedCallableWrapper(func))

private class ActionWrapper(
    private val action: () -> Unit
) : (StatusHandler.Callback) -> Unit {
    override fun invoke(callback: StatusHandler.Callback) {
        try {
            callback.working()
            action()
            callback.success()
        } catch (th: Throwable) {
            callback.failed(th)
        }
    }
}

private class ParametrizedActionWrapper<P : Any?>(
    private val action: (P) -> Unit
) : (P, StatusHandler.Callback) -> Unit {
    override fun invoke(param: P, callback: StatusHandler.Callback) {
        try {
            callback.working()
            action(param)
            callback.success()
        } catch (th: Throwable) {
            callback.failed(th)
        }
    }
}

private class CallableWrapper<V : Any>(
    private val action: () -> V
) : (ValueStatusHandler.Callback<V>) -> Unit {
    override fun invoke(callback: ValueStatusHandler.Callback<V>) {
        try {
            callback.working()
            val result = action()
            callback.value(result)
            callback.success()
        } catch (th: Throwable) {
            callback.failed(th)
        }
    }
}

private class ParametrizedCallableWrapper<P : Any?, V : Any>(
    private val action: (P) -> V
) : (P, ValueStatusHandler.Callback<V>) -> Unit {
    override fun invoke(param: P, callback: ValueStatusHandler.Callback<V>) {
        try {
            callback.working()
            val result = action(param)
            callback.value(result)
            callback.success()
        } catch (th: Throwable) {
            callback.failed(th)
        }
    }
}