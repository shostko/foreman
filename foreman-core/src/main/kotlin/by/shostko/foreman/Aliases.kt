package by.shostko.foreman

typealias OnReportUpdatedListener<T, E> = (from: Report<T, E>, to: Report<T, E>) -> Unit

typealias ErrorMapper<E1, E2> = (E1) -> E2

typealias ReportCombinationStrategy<T, E> = (List<Report<T, E>>) -> Report<T, E>

typealias Logger = (tag: String, report: Report<*, *>) -> Unit
