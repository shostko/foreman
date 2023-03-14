package by.shostko.foreman

class EasyLogger(
    private val logger: (String) -> Unit,
) : Logger {
    override fun invoke(tag: String, report: Report<*, *>) {
        when (report) {
            is Report.Initial -> logger("$tag -> Initial")
            is Report.Working -> logger("$tag -> Working")
            is Report.Success -> when (val result = report.result) {
                Unit -> logger("$tag -> Success")
                is List<*> -> logger("$tag -> Success: size=${result.size}")
                else -> logger("$tag -> Success: $result")
            }
            is Report.Failed -> logger("$tag -> Failed: ${report.error}")
        }
    }
}
