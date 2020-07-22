@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package by.shostko.statushandler.v2

open class Status(
    val working: Int,
    val throwable: Throwable?
) {
    val isInitial: Boolean
        get() = this === Initial
    val isFailed: Boolean
        get() = throwable != null
    val isSuccess: Boolean
        get() = this !== Initial && working == NOT_WORKING && throwable == null
    val isWorking: Boolean
        get() = working and WORKING == WORKING

    final override fun equals(other: Any?): Boolean = this === other || (other is Status && working == other.working && throwable == other.throwable)

    final override fun hashCode(): Int = working * 31 + (throwable?.hashCode() ?: 0)

    override fun toString(): String = "Status{Working:$working;Throwable:$throwable}"

    object Initial : Status(NOT_WORKING, null) {
        override fun toString(): String = "Status{INITIAL}"
    }

    object Success : Status(NOT_WORKING, null) {
        override fun toString(): String = "Status{SUCCESS}"
    }

    class Working(working: Int) : Status(working, null) {
        override fun toString(): String = "Status{WORKING:$working}"
    }

    class Failed(throwable: Throwable?) : Status(NOT_WORKING, throwable ?: ThrowableNotProvided) {
        override fun toString(): String = if (throwable === ThrowableNotProvided) "Status{FAILED}" else "Status{FAILED:$throwable}"
    }

    companion object {
        // core
        const val NOT_WORKING = 0
        const val WORKING = 1
        // paging ext (see TODO url)
        const val WORKING_APPEND = 2
        const val WORKING_PREPEND = 4

        fun create(working: Int, throwable: Throwable?): Status = when {
            working == NOT_WORKING && throwable == null -> Success
            working == NOT_WORKING && throwable != null -> Failed(throwable)
            working != NOT_WORKING && throwable == null -> Working(working)
            else -> Status(working, throwable)
        }

        fun create(working: Boolean, throwable: Throwable?): Status = when {
            !working && throwable == null -> Success
            !working && throwable != null -> Failed(throwable)
            working && throwable == null -> Working(WORKING)
            else -> Status(WORKING, throwable)
        }
    }
}

private object ThrowableNotProvided: Throwable()