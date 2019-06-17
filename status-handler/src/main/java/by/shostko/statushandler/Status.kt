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
        fun createSuccess(): Status<E>
        fun createSuccess(map: Map<String, Any>): Status<E>
        fun createFailed(throwable: Throwable): Status<E>
        fun createFailed(map: Map<String, Any>): Status<E>
    }
}

data class SimpleStatus<E>(
    override val direction: Direction,
    override val throwable: Throwable?,
    override val error: E?
) : Status<E>(direction, throwable, error) {
    abstract class Factory<E> : Status.Factory<E> {
        override fun createWorking(direction: Direction): Status<E> = LoadingStatus(direction)
        override fun createSuccess(): Status<E> = SuccessStatus()
        override fun createSuccess(map: Map<String, Any>): Status<E> = SuccessStatus()
        override fun createFailed(throwable: Throwable): Status<E> = FailedStatus(throwable)
        override fun createFailed(map: Map<String, Any>): Status<E> = FailedMapStatus(map)
    }
}

data class MessageStatus(
    override val direction: Direction,
    override val throwable: Throwable?
) : Status<String>(direction, throwable, throwable?.message) {
    open class Factory : Status.Factory<String> {
        override fun createWorking(direction: Direction) = MessageStatus(direction, null)
        override fun createSuccess() = MessageStatus(Direction.NONE, null)
        override fun createSuccess(map: Map<String, Any>) = MessageStatus(Direction.NONE, null)
        override fun createFailed(throwable: Throwable) = MessageStatus(Direction.NONE, throwable)
        override fun createFailed(map: Map<String, Any>) = MessageStatus(Direction.NONE, MapThrowable(map))
    }
}

data class ClassStatus(
    override val direction: Direction,
    override val throwable: Throwable?
) : Status<Class<out Throwable>>(direction, throwable, throwable?.javaClass) {
    open class Factory : Status.Factory<Class<out Throwable>> {
        override fun createWorking(direction: Direction) = ClassStatus(direction, null)
        override fun createSuccess() = ClassStatus(Direction.NONE, null)
        override fun createSuccess(map: Map<String, Any>) = ClassStatus(Direction.NONE, null)
        override fun createFailed(throwable: Throwable) = ClassStatus(Direction.NONE, throwable)
        override fun createFailed(map: Map<String, Any>) = ClassStatus(Direction.NONE, MapThrowable(map))
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

private data class FailedMapStatus<E>(val map: Map<String, Any>) : Status<E>(Direction.NONE, MapThrowable(map), null) {
    override fun toString(): String {
        return "Status{FAILED:Map(${map.entries.joinToString()})"
    }
}

object SimpleStatusFactory : SimpleStatus.Factory<Unit>()