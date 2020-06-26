@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package by.shostko.statushandler.v2

sealed class Status(
    open val working: Int,
    open val throwable: Throwable?
) {
    val isInitial: Boolean
        get() = this === Initial
    val isFailed: Boolean
        get() = throwable != null
    val isSuccess: Boolean
        get() = this !== Initial && working == NOT_WORKING && throwable == null
    val isWorking: Boolean
        get() = working != NOT_WORKING

    final override fun equals(other: Any?): Boolean = this === other || (other is Status && working == other.working && throwable == other.throwable)

    final override fun hashCode(): Int = working * 31 + (throwable?.hashCode() ?: 0)

    override fun toString(): String = "Status{Working:$working;Throwable:$throwable}"

    object Initial : Status(NOT_WORKING, null) {
        override fun toString(): String = "Status{INITIAL}"
    }

    object Success : Status(NOT_WORKING, null) {
        override fun toString(): String = "Status{SUCCESS}"
    }

    class Working(override val working: Int) : Status(working, null) {
        override fun toString(): String = "Status{WORKING:$working}"
    }

    class Failed(override val throwable: Throwable?) : Status(NOT_WORKING, throwable) {
        override fun toString(): String = if (throwable == null) "Status{FAILED}" else "Status{FAILED:$throwable}"
    }

    companion object {
        const val NOT_WORKING = 0
        const val WORKING = 1
    }
}