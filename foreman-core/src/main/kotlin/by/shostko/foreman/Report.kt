package by.shostko.foreman

sealed class Report<out T : Any?, out E : Any> {

    object Initial : Report<Nothing, Nothing>() {
        override fun toString(): String = "Report{Initial}"
    }

    object Working : Report<Nothing, Nothing>() {
        override fun toString(): String = "Report{Working}"
    }

    data class Failed<out E : Any>(
        val error: E,
    ) : Report<Nothing, E>() {
        override fun toString(): String = "Report{Failed:$error}"
    }

    data class Success<out T : Any?>(
        val result: T
    ) : Report<T, Nothing>() {
        val isEmpty: Boolean
            get() = result == null || (result is List<*> && result.isEmpty())

        override fun toString(): String = if (result is List<*>) {
            "Report{Success:${result.size}"
        } else {
            "Report{Success:$result}"
        }
    }

    val resultOrNull: T?
        get() = (this as? Success)?.result
}

