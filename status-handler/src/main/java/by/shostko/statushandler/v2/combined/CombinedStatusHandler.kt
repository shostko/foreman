@file:Suppress("unused")

package by.shostko.statushandler.v2.combined

import by.shostko.statushandler.v2.AbsStatusHandler
import by.shostko.statushandler.v2.Status
import by.shostko.statushandler.v2.StatusHandler

interface StatusCombinationStrategy : (Status, Status) -> Status {

    companion object

    object Default : StatusCombinationStrategy {
        override fun invoke(s1: Status, s2: Status): Status = when {
            s1 == s2 -> s2
            s1.isInitial -> s2
            s2.isInitial -> s1
            s1.isSuccess -> s2
            s2.isSuccess -> s1
            s1.isFailed && !s2.isFailed -> s1
            !s1.isFailed && s2.isFailed -> s2
            s1.isFailed && s2.isFailed -> s2
            else -> Status.Working(s1.working or s2.working)
        }
    }

    object AlwaysFirst : StatusCombinationStrategy {
        override fun invoke(s1: Status, s2: Status): Status = s1
    }

    object AlwaysSecond : StatusCombinationStrategy {
        override fun invoke(s1: Status, s2: Status): Status = s2
    }
}

private class CombinedStatusHandler(
    private val statusHandler1: StatusHandler,
    private val statusHandler2: StatusHandler,
    private val strategy: StatusCombinationStrategy
) : AbsStatusHandler() {

    override val status: Status
        get() = strategy(statusHandler1.status, statusHandler2.status)

    private val listener1: StatusHandler.OnStatusListener = object : StatusHandler.OnStatusListener {
        override fun onStatus(status: Status) {
            notifyListeners(strategy(status, statusHandler2.status))
        }
    }

    private val listener2: StatusHandler.OnStatusListener = object : StatusHandler.OnStatusListener {
        override fun onStatus(status: Status) {
            notifyListeners(strategy(statusHandler1.status, status))
        }
    }

    override fun onFirstListenerAdded() {
        statusHandler1.addOnStatusListener(listener1)
        statusHandler2.addOnStatusListener(listener2)
    }

    override fun onLastListenerRemoved() {
        statusHandler1.removeOnStatusListener(listener1)
        statusHandler2.removeOnStatusListener(listener2)
    }
}

fun StatusHandler.Companion.combine(
    sh1: StatusHandler,
    sh2: StatusHandler,
    strategy: StatusCombinationStrategy = StatusCombinationStrategy.Default
): StatusHandler = CombinedStatusHandler(sh1, sh2, strategy)

fun StatusHandler.combineWith(
    other: StatusHandler,
    strategy: StatusCombinationStrategy = StatusCombinationStrategy.Default
): StatusHandler = CombinedStatusHandler(this, other, strategy)