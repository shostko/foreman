@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package by.shostko.statushandler

enum class Direction {
    BACKWARD,
    FORWARD,
    FULL,
    NONE
}

abstract class Status<E>(
    open val direction: Direction,
    open val throwable: Throwable?,
    open val error: E?
) {
    fun isWorking() = direction != Direction.NONE
    fun isFailed() = throwable != null || error != null
    fun isSuccess() = direction == Direction.NONE && throwable == null && error == null

    interface Factory<E> {
        fun createWorking(direction: Direction): Status<E>
        fun createFailed(throwable: Throwable): Status<E>
        fun createSuccess(): Status<E>
    }
}

data class CustomErrorStatus<E>(
    override val direction: Direction,
    override val throwable: Throwable?,
    override val error: E?
) : Status<E>(direction, throwable, error) {
    abstract class Factory<E> : Status.Factory<E> {
        override fun createWorking(direction: Direction): Status<E> = LoadingStatus(direction)
        override fun createSuccess(): Status<E> = SuccessStatus()
    }
}

open class SimpleStatusFactory : Status.Factory<Unit> {
    override fun createWorking(direction: Direction): Status<Unit> = LoadingStatus(direction)
    override fun createFailed(throwable: Throwable): Status<Unit> = FailedStatus(throwable)
    override fun createSuccess(): Status<Unit> = SuccessStatus()
}

data class MessageStatus(
    override val direction: Direction,
    override val throwable: Throwable?,
    override val error: String?
) : Status<String>(direction, throwable, error) {
    open class Factory : Status.Factory<String> {
        override fun createWorking(direction: Direction) = MessageStatus(direction, null, null)
        override fun createFailed(throwable: Throwable) = MessageStatus(Direction.NONE, throwable, throwable.message)
        override fun createSuccess() = MessageStatus(Direction.NONE, null, null)
    }
}

private class SuccessStatus<E> : Status<E>(Direction.NONE, null, null) {
    override fun toString(): String {
        return "Status{SUCCESS}"
    }
}

private data class LoadingStatus<E>(override val direction: Direction) : Status<E>(direction, null, null) {
    override fun toString(): String {
        return "Status{LOADING:$direction}"
    }
}

private data class FailedStatus<E>(override val throwable: Throwable) : Status<E>(Direction.NONE, throwable, null) {
    override fun toString(): String {
        return "Status{FAILED:${throwable::class.java.simpleName}"
    }
}