package by.shostko.foreman

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*

class FlowWorker<T : Any?> internal constructor(
    task: Flow<T>,
    private val scope: CoroutineScope? = null,
    tag: String? = null,
) : Worker<T, Throwable>(tag) {

    private val defaultScope by lazy { CoroutineScope(SupervisorJob()) }
    private var job: Job? = null

    val task: Flow<T> = task
        .onStart { save(Report.Working) }
        .onEach { save(Report.Success(it)) }
        .catch { save(Report.Failed(it)) }

    fun launch(scope: CoroutineScope? = null) {
        job?.cancel(CancellationException("Cancelling this cause launching new job for this worker!"))
        job = task.launchIn(scope ?: this.scope ?: defaultScope)
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class FlowWorker1<P : Any?, T : Any?> internal constructor(
    task: suspend (P) -> Flow<T>,
    private val scope: CoroutineScope? = null,
    tag: String? = null,
) : Worker<T, Throwable>(tag) {

    private val defaultScope by lazy { CoroutineScope(SupervisorJob()) }
    private var job: Job? = null

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

    fun launch(scope: CoroutineScope? = null) {
        job?.cancel(CancellationException("Cancelling this cause launching new job for this worker!"))
        job = task.launchIn(scope ?: this.scope ?: defaultScope)
    }
}
