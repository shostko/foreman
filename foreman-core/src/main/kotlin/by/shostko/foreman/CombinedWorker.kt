package by.shostko.foreman

internal class CombinedWorker<T : Any?, E : Any>(
    private val workers: List<Worker<T, E>>,
    private val strategy: ReportCombinationStrategy<T, E>,
    tag: String? = null,
) : Worker<T, E>(tag) {

    private val listeners = List(workers.size) {
        Listener(it)
    }

    init {
        workers.forEachIndexed { index, worker ->
            worker.addOnReportUpdatedListener(listeners[index])
        }
    }

    private inner class Listener(
        private val indexToUpdate: Int,
    ) : OnReportUpdatedListener<T, E> {
        override fun invoke(from: Report<T, E>, to: Report<T, E>) {
            val reports = workers.mapIndexed { index, worker ->
                if (index == indexToUpdate) {
                    to
                } else {
                    worker.report
                }
            }
            val resolved = strategy(reports)
            save(resolved)
        }
    }
}

open class DefaultReportCombinationStrategy<T : Any?, E : Any> : ReportCombinationStrategy<T, E> {
    override fun invoke(list: List<Report<T, E>>): Report<T, E> =
        list.firstOrNull { it is Report.Failed }
            ?: list.firstOrNull { it is Report.Working }
            ?: list.firstOrNull { it is Report.Success }
            ?: list.firstOrNull()
            ?: Report.Initial
}

fun <T : Any?, E : Any> Foreman.combine(
    workers: List<Worker<T, E>>,
    tag: String? = null,
    strategy: ReportCombinationStrategy<T, E> = DefaultReportCombinationStrategy(),
): Worker<T, E> = CombinedWorker(
    tag = tag,
    workers = workers,
    strategy = strategy,
)

fun <T : Any?, E : Any> Foreman.combine(
    worker1: Worker<T, E>,
    worker2: Worker<T, E>,
    tag: String? = null,
    strategy: ReportCombinationStrategy<T, E> = DefaultReportCombinationStrategy(),
): Worker<T, E> = CombinedWorker(
    tag = tag,
    workers = arrayListOf(worker1, worker2),
    strategy = strategy,
)

fun <T : Any?, E : Any> Worker<T, E>.combineWith(
    other: Worker<T, E>,
    tag: String? = null,
    strategy: ReportCombinationStrategy<T, E> = DefaultReportCombinationStrategy(),
): Worker<T, E> = CombinedWorker(
    tag = tag,
    workers = arrayListOf(this, other),
    strategy = strategy,
)
