package by.shostko.foreman

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

class FlowWorker<T : Any?> internal constructor(
    task: Flow<T>,
    tag: String? = null,
) : Worker<T, Throwable>(tag) {

    val task: Flow<T> = task
        .onStart { save(Report.Working) }
        .onEach { save(Report.Success(it)) }
        .catch { save(Report.Failed(it)) }

    fun launch(scope: CoroutineScope) {
        task.launchIn(scope)
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class FlowWorker1<P : Any?, T : Any?> internal constructor(
    task: suspend (P) -> Flow<T>,
    tag: String? = null,
) : Worker<T, Throwable>(tag,) {

    private val paramFlow: MutableSharedFlow<P> = MutableSharedFlow()

    val task: Flow<T> = paramFlow
        .flatMapLatest(task)
        .onStart { save(Report.Working) }
        .onEach { save(Report.Success(it)) }
        .catch { save(Report.Failed(it)) }

    fun push(param: P) {
        paramFlow.tryEmit(param)
        // TODO handle false result?
    }

    fun launch(scope: CoroutineScope) {
        task.launchIn(scope)
    }
}
