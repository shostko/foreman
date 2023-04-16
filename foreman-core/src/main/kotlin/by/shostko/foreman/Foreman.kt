package by.shostko.foreman

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

object Foreman {

    var logger: Logger? = null

    internal fun log(tag: String?, report: Report<*, *>) {
        if (!tag.isNullOrBlank()) {
            logger?.invoke(tag, report)
        }
    }

    fun <T> prepare(task: suspend () -> T): NoParamWorker<T, Throwable> = CoroutineWorker(task = task)
    fun <T> prepare(tag: String?, task: suspend () -> T): NoParamWorker<T, Throwable> = CoroutineWorker(task = task, tag = tag)
    fun <T> prepare(scope: CoroutineScope?, task: suspend () -> T): NoParamWorker<T, Throwable> = CoroutineWorker(task = task, scope = scope)
    fun <T> prepare(scope: CoroutineScope?, tag: String?, task: suspend () -> T): NoParamWorker<T, Throwable> = CoroutineWorker(task = task, scope = scope, tag = tag)

    fun <T> prepareAndLaunch(task: suspend () -> T) = prepare(task).apply { launch() }
    fun <T> prepareAndLaunch(tag: String?, task: suspend () -> T) = prepare(tag, task).apply { launch() }
    fun <T> prepareAndLaunch(scope: CoroutineScope?, task: suspend () -> T) = prepare(scope, task).apply { launch() }
    fun <T> prepareAndLaunch(scope: CoroutineScope?, tag: String?, task: suspend () -> T) = prepare(scope, tag, task).apply { launch() }

    fun <P, T> prepare(task: suspend (P) -> T): OneParamWorker<P, T, Throwable> = CoroutineWorker1(task = task)
    fun <P, T> prepare(tag: String?, task: suspend (P) -> T): OneParamWorker<P, T, Throwable> = CoroutineWorker1(task = task, tag = tag)
    fun <P, T> prepare(scope: CoroutineScope?, task: suspend (P) -> T): OneParamWorker<P, T, Throwable> = CoroutineWorker1(task = task, scope = scope)
    fun <P, T> prepare(scope: CoroutineScope?, tag: String?, task: suspend (P) -> T): OneParamWorker<P, T, Throwable> = CoroutineWorker1(task = task, scope = scope, tag = tag)

    fun <P, T> prepareAndLaunch(param: P, task: suspend (P) -> T) = prepare(task).apply { launch(param) }
    fun <P, T> prepareAndLaunch(param: P, tag: String?, task: suspend (P) -> T) = prepare(tag, task).apply { launch(param) }
    fun <P, T> prepareAndLaunch(param: P, scope: CoroutineScope?, task: suspend (P) -> T) = prepare(scope, task).apply { launch(param) }
    fun <P, T> prepareAndLaunch(param: P, scope: CoroutineScope?, tag: String?, task: suspend (P) -> T) = prepare(scope, tag, task).apply { launch(param) }

    fun <T> prepareFlow(task: Flow<T>): NoParamWorker<T, Throwable> = FlowWorker(task = task)
    fun <T> prepareFlow(tag: String?, task: Flow<T>): NoParamWorker<T, Throwable> = FlowWorker(task = task, tag = tag)
    fun <T> prepareFlow(scope: CoroutineScope?, task: Flow<T>): NoParamWorker<T, Throwable> = FlowWorker(task = task, scope = scope)
    fun <T> prepareFlow(scope: CoroutineScope?, tag: String?, task: Flow<T>): NoParamWorker<T, Throwable> = FlowWorker(task = task, scope = scope, tag = tag)

    fun <T> prepareAndLaunchFlow(task: Flow<T>) = prepareFlow(task).apply { launch() }
    fun <T> prepareAndLaunchFlow(tag: String?, task: Flow<T>) = prepareFlow(tag, task).apply { launch() }
    fun <T> prepareAndLaunchFlow(scope: CoroutineScope?, task: Flow<T>) = prepareFlow(scope, task).apply { launch() }
    fun <T> prepareAndLaunchFlow(scope: CoroutineScope?, tag: String?, task: Flow<T>) = prepareFlow(scope, tag, task).apply { launch() }

    fun <P, T> prepareFlow(task: suspend (P) -> Flow<T>): OneParamWorker<P, T, Throwable> = FlowWorker1(task = task)
    fun <P, T> prepareFlow(tag: String?, task: suspend (P) -> Flow<T>): OneParamWorker<P, T, Throwable> = FlowWorker1(task = task, tag = tag)
    fun <P, T> prepareFlow(scope: CoroutineScope?, task: suspend (P) -> Flow<T>): OneParamWorker<P, T, Throwable> = FlowWorker1(task = task, scope = scope)
    fun <P, T> prepareFlow(scope: CoroutineScope?, tag: String?, task: suspend (P) -> Flow<T>): OneParamWorker<P, T, Throwable> = FlowWorker1(task = task, scope = scope, tag = tag)

    fun <P, T> prepareAndLaunchFlow(param: P, task: suspend (P) -> Flow<T>) = prepareFlow(task).apply { launch(param) }
    fun <P, T> prepareAndLaunchFlow(param: P, tag: String?, task: suspend (P) -> Flow<T>) = prepareFlow(tag, task).apply { launch(param) }
    fun <P, T> prepareAndLaunchFlow(param: P, scope: CoroutineScope?, task: suspend (P) -> Flow<T>) = prepareFlow(scope, task).apply { launch(param) }
    fun <P, T> prepareAndLaunchFlow(param: P, scope: CoroutineScope?, tag: String?, task: suspend (P) -> Flow<T>) = prepareFlow(scope, tag, task).apply { launch(param) }
}
