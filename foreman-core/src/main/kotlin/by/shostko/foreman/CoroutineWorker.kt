package by.shostko.foreman

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class CoroutineWorker<T : Any?> internal constructor(
    private val task: suspend () -> T,
    tag: String? = null,
) : Worker<T, Throwable>(tag) {

    fun launch(scope: CoroutineScope) {
        // TODO should all prev processes be canceled?
        scope.launch {
            try {
                save(Report.Working)
                val result = task()
                save(Report.Success(result))
            } catch (th: Throwable) {
                save(Report.Failed(th))
            }
        }
    }
}

class CoroutineWorker1<P : Any?, T : Any?> internal constructor(
    private val task: suspend (P) -> T,
    tag: String? = null,
) : Worker<T, Throwable>(tag) {

    fun launch(param: P, scope: CoroutineScope) {
        // TODO should all prev processes be canceled?
        scope.launch {
            try {
                save(Report.Working)
                val result = task(param)
                save(Report.Success(result))
            } catch (th: Throwable) {
                save(Report.Failed(th))
            }
        }
    }
}
