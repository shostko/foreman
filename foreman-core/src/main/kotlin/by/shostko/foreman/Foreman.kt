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

    fun <T> prepare(task: suspend () -> T) = CoroutineWorker(task = task)
    fun <T> prepare(tag: String?, task: suspend () -> T) = CoroutineWorker(task = task, tag = tag)
    fun <T> prepare(scope: CoroutineScope?, task: suspend () -> T) = CoroutineWorker(task = task, scope = scope)
    fun <T> prepare(scope: CoroutineScope?, tag: String?, task: suspend () -> T) = CoroutineWorker(task = task, scope = scope, tag = tag)

    fun <P, T> prepare(task: suspend (P) -> T) = CoroutineWorker1(task = task)
    fun <P, T> prepare(tag: String?, task: suspend (P) -> T) = CoroutineWorker1(task = task, tag = tag)
    fun <P, T> prepare(scope: CoroutineScope?, task: suspend (P) -> T) = CoroutineWorker1(task = task, scope = scope)
    fun <P, T> prepare(scope: CoroutineScope?, tag: String?, task: suspend (P) -> T) = CoroutineWorker1(task = task, scope = scope, tag = tag)

    fun <T> prepareFlow(task: Flow<T>) = FlowWorker(task = task)
    fun <T> prepareFlow(tag: String?, task: Flow<T>) = FlowWorker(task = task, tag = tag)
    fun <T> prepareFlow(scope: CoroutineScope?, task: Flow<T>) = FlowWorker(task = task, scope = scope)
    fun <T> prepareFlow(scope: CoroutineScope?, tag: String?, task: Flow<T>) = FlowWorker(task = task, scope = scope, tag = tag)

    fun <P, T> prepareFlow(task: suspend (P) -> Flow<T>) = FlowWorker1(task = task)
    fun <P, T> prepareFlow(tag: String?, task: suspend (P) -> Flow<T>) = FlowWorker1(task = task, tag = tag)
    fun <P, T> prepareFlow(scope: CoroutineScope?, task: suspend (P) -> Flow<T>) = FlowWorker1(task = task, scope = scope)
    fun <P, T> prepareFlow(scope: CoroutineScope?, tag: String?, task: suspend (P) -> Flow<T>) = FlowWorker1(task = task, scope = scope, tag = tag)
}
