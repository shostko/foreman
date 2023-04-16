package by.shostko.foreman

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*

internal class FlowWorker<T : Any?> internal constructor(
    task: Flow<T>,
    scope: CoroutineScope? = null,
    tag: String? = null,
) : NoParamWorker<T, Throwable>(tag) {

    override val scope = scope ?: CoroutineScope(SupervisorJob())
    private var job: Job? = null

    private val task: Flow<T> = task
        .onStart { save(Report.Working) }
        .onEach { save(Report.Success(it)) }
        .catch { save(Report.Failed(it)) }

    override fun launch() {
        job?.cancel(CancellationException("Cancelling this cause launching new job for this worker!"))
        job = task.launchIn(scope)
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
internal class FlowWorker1<P : Any?, T : Any?> internal constructor(
    task: suspend (P) -> Flow<T>,
    scope: CoroutineScope? = null,
    tag: String? = null,
) : OneParamWorker<P, T, Throwable>(tag) {

    override val scope = scope ?: CoroutineScope(SupervisorJob())
    private var job: Job? = null

    private val paramFlow: MutableSharedFlow<P> = MutableSharedFlow()

    private val task: Flow<T> = paramFlow
        .flatMapLatest(task)
        .onStart { save(Report.Working) }
        .onEach { save(Report.Success(it)) }
        .catch { save(Report.Failed(it)) }

    override fun launch(param: P) {
        if (job == null) {
            job = task.launchIn(scope)
        }
        paramFlow.tryEmit(param)
    }
}
