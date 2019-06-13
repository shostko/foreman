@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package by.shostko.statusprocessor

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

    interface Factory<STATUS : Status<*>> {
        fun createWorking(direction: Direction): STATUS
        fun createFailed(throwable: Throwable): STATUS
        fun createSuccess(): STATUS
    }
}

private object SuccessStatus : Status<Any>(Direction.NONE, null, null) {
    override fun toString(): String {
        return "Status{SUCCESS}"
    }
}

private data class LoadingStatus(override val direction: Direction) : Status<Any>(direction, null, null) {
    override fun toString(): String {
        return "Status{LOADING:$direction}"
    }
}

private data class FailedStatus(override val throwable: Throwable) : Status<Any>(Direction.NONE, throwable, null) {
    override fun toString(): String {
        return "Status{FAILED:${throwable::class.java.simpleName}"
    }
}

data class StringStatus(
    override val direction: Direction,
    override val throwable: Throwable?,
    override val error: String?
) : Status<String>(direction, throwable, error)

open class StringStatusFactory : Status.Factory<StringStatus> {
    override fun createWorking(direction: Direction) = StringStatus(direction, null, null)
    override fun createFailed(throwable: Throwable) = StringStatus(Direction.NONE, throwable, throwable.message)
    override fun createSuccess() = StringStatus(Direction.NONE, null, null)
}

open class SimpleStatusFactory : Status.Factory<Status<*>> {
    override fun createWorking(direction: Direction): Status<*> = LoadingStatus(direction)
    override fun createFailed(throwable: Throwable): Status<*> = FailedStatus(throwable)
    override fun createSuccess(): Status<*> = SuccessStatus
}