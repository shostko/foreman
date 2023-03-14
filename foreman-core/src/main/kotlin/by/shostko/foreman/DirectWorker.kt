package by.shostko.foreman

class DirectWorker<T : Any?> internal constructor(
    private val task: () -> T,
    tag: String? = null,
) : Worker<T, Throwable>(tag) {

    fun launch() {
        // TODO should all prev processes be canceled?
        try {
            save(Report.Working)
            val result = task()
            save(Report.Success(result))
        } catch (th: Throwable) {
            save(Report.Failed(th))
        }
    }
}

class DirectWorker1<P : Any?, T : Any?> internal constructor(
    private val task: (P) -> T,
    tag: String? = null,
) : Worker<T, Throwable>(tag) {

    fun launch(param: P) {
        // TODO should all prev processes be canceled?
        try {
            save(Report.Working)
            val result = task(param)
            save(Report.Success(result))
        } catch (th: Throwable) {
            save(Report.Failed(th))
        }
    }
}
