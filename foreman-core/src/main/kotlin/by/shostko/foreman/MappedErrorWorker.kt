package by.shostko.foreman

internal class MappedErrorWorker<T : Any?, E1 : Any, E2 : Any>(
    source: Worker<T, E1>,
    private val mapper: ErrorMapper<E1, E2>,
    tag: String? = null,
) : Worker<T, E2>(tag) {

    private val listener = object : OnReportUpdatedListener<T, E1> {
        override fun invoke(from: Report<T, E1>, to: Report<T, E1>) {
            when (to) {
                is Report.Initial -> Report.Initial
                is Report.Working -> Report.Working
                is Report.Success -> Report.Success(to.result)
                is Report.Failed -> Report.Failed(mapper(to.error))
            }
        }
    }

    init {
        source.addOnReportUpdatedListener(listener)
    }
}

fun <T : Any?, E1 : Any, E2 : Any> Foreman.mapError(
    source: Worker<T, E1>,
    mapper: ErrorMapper<E1, E2>,
    tag: String? = null,
): Worker<T, E2> = MappedErrorWorker(
    tag = tag,
    source = source,
    mapper = mapper,
)

fun <T : Any?, E1 : Any, E2 : Any> Worker<T, E1>.mapError(
    mapper: ErrorMapper<E1, E2>,
    tag: String? = null,
): Worker<T, E2> = MappedErrorWorker(
    tag = tag,
    source = this,
    mapper = mapper,
)
