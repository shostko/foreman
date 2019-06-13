@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package by.shostko.statusprocessor

abstract class BaseLoadingStatus<E>(open val direction: Direction, open val throwable: Throwable?, open val error: E?) {
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

data class LoadingStatus(
    override val direction: Direction,
    override val throwable: Throwable?,
    override val error: String?
) : BaseLoadingStatus<String>(direction, throwable, error) {

    companion object {

        fun success(): LoadingStatus {
            return LoadingStatus(Direction.NONE, null, null)
        }

        fun loading(direction: Direction): LoadingStatus {
            return LoadingStatus(direction, null, null)
        }

        fun loading(): LoadingStatus {
            return LoadingStatus(Direction.FULL, null, null)
        }

        fun loadingBackward(): LoadingStatus {
            return LoadingStatus(Direction.BACKWARD, null, null)
        }

        fun loadingForward(): LoadingStatus {
            return LoadingStatus(Direction.FORWARD, null, null)
        }

        fun error(error: String? = null): LoadingStatus {
            return LoadingStatus(Direction.NONE, null, error)
        }

        fun error(throwable: Throwable, error: String?): LoadingStatus {
            return LoadingStatus(Direction.NONE, throwable, error)
        }

        fun error(throwable: Throwable): LoadingStatus {
            return LoadingStatus(Direction.NONE, throwable, throwable.message)
        }
    }
}