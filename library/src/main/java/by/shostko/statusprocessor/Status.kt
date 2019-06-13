@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package by.shostko.statusprocessor

abstract class BaseStatus<E>(open val direction: Direction, open val throwable: Throwable?, open val error: E?) {
    fun isLoading() = direction != Direction.NONE
    fun isFailed() = throwable != null || error != null
    fun isSuccess() = direction == Direction.NONE && throwable == null && error == null
}

enum class Direction {
    BACKWARD,
    FORWARD,
    FULL,
    NONE
}

data class Status(
    override val direction: Direction,
    override val throwable: Throwable?,
    override val error: String?
) : BaseStatus<String>(direction, throwable, error) {

    companion object {

        fun success(): Status {
            return Status(Direction.NONE, null, null)
        }

        fun loading(direction: Direction): Status {
            return Status(direction, null, null)
        }

        fun loading(): Status {
            return Status(Direction.FULL, null, null)
        }

        fun loadingBackward(): Status {
            return Status(Direction.BACKWARD, null, null)
        }

        fun loadingForward(): Status {
            return Status(Direction.FORWARD, null, null)
        }

        fun error(error: String? = null): Status {
            return Status(Direction.NONE, null, error)
        }

        fun error(throwable: Throwable, error: String?): Status {
            return Status(Direction.NONE, throwable, error)
        }

        fun error(throwable: Throwable): Status {
            return Status(Direction.NONE, throwable, throwable.message)
        }
    }
}