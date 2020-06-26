@file:Suppress("unused")

package by.shostko.statushandler.v2

fun StatusHandler.Companion.wrapAction(func: () -> Unit) = wrap(ActionWrapper(func))
fun StatusHandler.Companion.prepareAction(func: () -> Unit) = prepare(ActionWrapper(func))
fun <P> StatusHandler.Companion.awaitAction(func: (P) -> Unit) = await(ParametrizedActionWrapper(func))

fun <V : Any> StatusHandler.Companion.wrapCallable(func: () -> V) = wrap(CallableWrapper(func))
fun <V : Any> StatusHandler.Companion.prepareCallable(func: () -> V)= prepare(CallableWrapper(func))
fun <V : Any, P : Any?> StatusHandler.Companion.awaitCallable(func: (P) -> V) = await(ParametrizedCallableWrapper(func))

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

private class ParametrizedActionWrapper<P: Any?>(
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

private class CallableWrapper<V: Any>(
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

private class ParametrizedCallableWrapper<P: Any?, V: Any>(
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