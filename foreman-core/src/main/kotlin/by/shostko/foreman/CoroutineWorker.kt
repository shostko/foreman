package by.shostko.foreman

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

internal class CoroutineWorker<T : Any?> internal constructor(
    private val task: suspend () -> T,
    scope: CoroutineScope? = null,
    tag: String? = null,
) : NoParamWorker<T, Throwable>(tag) {

    override val scope = scope ?: CoroutineScope(SupervisorJob())
    private var job: Job? = null

    override fun launch() {
        job?.cancel(CancellationException("Cancelling this cause launching new job for this worker!"))
        job = scope.launch {
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

internal class CoroutineWorker1<P : Any?, T : Any?> internal constructor(
    private val task: suspend (P) -> T,
    scope: CoroutineScope? = null,
    tag: String? = null,
) : OneParamWorker<P, T, Throwable>(tag) {

    override val scope = scope ?: CoroutineScope(SupervisorJob())
    private var job: Job? = null

    override fun launch(param: P) {
        job?.cancel(CancellationException("Cancelling this cause launching new job for this worker!"))
        job = scope.launch {
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

internal class CoroutineWorker2<P1 : Any?, P2 : Any?, T : Any?> internal constructor(
    private val task: suspend (P1, P2) -> T,
    scope: CoroutineScope? = null,
    tag: String? = null,
) : TwoParamWorker<P1, P2, T, Throwable>(tag) {

    override val scope = scope ?: CoroutineScope(SupervisorJob())
    private var job: Job? = null

    override fun launch(param1: P1, param2: P2) {
        job?.cancel(CancellationException("Cancelling this cause launching new job for this worker!"))
        job = scope.launch {
            try {
                save(Report.Working)
                val result = task(param1, param2)
                save(Report.Success(result))
            } catch (e: CancellationException) {
                throw e
            } catch (th: Throwable) {
                save(Report.Failed(th))
            }
        }
    }
}
