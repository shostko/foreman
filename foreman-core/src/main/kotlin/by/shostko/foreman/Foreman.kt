package by.shostko.foreman

import kotlinx.coroutines.flow.Flow

object Foreman {

    var logger: Logger? = null

    internal fun log(tag: String?, report: Report<*, *>) {
        if (!tag.isNullOrBlank()) {
            logger?.invoke(tag, report)
        }
    }

    fun <T> prepare(
        tag: String? = null,
        task: suspend () -> T,
    ) = CoroutineWorker(
        task = task,
        tag = tag,
    )

    fun <P, T> prepare(
        tag: String? = null,
        task: suspend (P) -> T,
    ) = CoroutineWorker1(
        task = task,
        tag = tag,
    )

    fun <T> prepare(
        tag: String? = null,
        task: Flow<T>,
    ) = FlowWorker(
        task = task,
        tag = tag,
    )

    fun <P, T> prepare(
        tag: String? = null,
        task: suspend (P) -> Flow<T>,
    ) = FlowWorker1(
        task = task,
        tag = tag,
    )

    fun <T> prepareDirect(
        tag: String? = null,
        task: () -> T,
    ) = DirectWorker(
        task = task,
        tag = tag,
    )

    fun <P, T> prepareDirect(
        tag: String? = null,
        task: (P) -> T,
    ) = DirectWorker1(
        task = task,
        tag = tag,
    )
}
