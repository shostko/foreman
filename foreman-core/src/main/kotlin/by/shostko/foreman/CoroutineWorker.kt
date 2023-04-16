package by.shostko.foreman

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlin.coroutines.cancellation.CancellationException

class CoroutineWorker<T : Any?> internal constructor(
    private val task: suspend () -> T,
    private val scope: CoroutineScope? = null,
    tag: String? = null,
) : Worker<T, Throwable>(tag) {

    private val defaultScope by lazy { CoroutineScope(SupervisorJob()) }
    private var job: Job? = null

    fun launch(scope: CoroutineScope? = null) {
        job?.cancel(CancellationException("Cancelling this cause launching new job for this worker!"))
        val scopeToUse = scope ?: this.scope ?: defaultScope
        job = scopeToUse.launch {
            try {
                save(Report.Working)
                val result = task()
                save(Report.Success(result))
            } catch (e: CancellationException) {
                throw e
            } catch (th: Throwable) {
                save(Report.Failed(th))
            }
        }
    }
}

class CoroutineWorker1<P : Any?, T : Any?> internal constructor(
    private val task: suspend (P) -> T,
    private val scope: CoroutineScope? = null,
    tag: String? = null,
) : Worker<T, Throwable>(tag) {

    private val defaultScope by lazy { CoroutineScope(SupervisorJob()) }
    private var job: Job? = null

    fun push(param: P) {
        launch(param, null)
    }

    fun launch(param: P, scope: CoroutineScope? = null) {
        job?.cancel(CancellationException("Cancelling this cause launching new job for this worker!"))
        val scopeToUse = scope ?: this.scope ?: defaultScope
        job = scopeToUse.launch {
            try {
                save(Report.Working)
                val result = task(param)
                save(Report.Success(result))
            } catch (e: CancellationException) {
                throw e
            } catch (th: Throwable) {
                save(Report.Failed(th))
            }
        }
    }
}
