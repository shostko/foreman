@file:Suppress("unused")

package by.shostko.statushandler.v2.combined

import by.shostko.statushandler.v2.BaseStatusHandler
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

sealed class StatusHandlerCombinationStrategy {
    object Merge : StatusHandlerCombinationStrategy()
    object First : StatusHandlerCombinationStrategy()
    object Last : StatusHandlerCombinationStrategy()
    object OnlyFirstInitialized : StatusHandlerCombinationStrategy()
    class CombineLatest(val statusCombinationStrategy: StatusCombinationStrategy) : StatusHandlerCombinationStrategy()
}

internal class CombinedStatusHandler(
    private val statusHandler1: StatusHandler,
    private val statusHandler2: StatusHandler,
    private val strategy: StatusHandlerCombinationStrategy
) : BaseStatusHandler() {

    private var initialized: Boolean = false

    private val listener1: StatusHandler.OnStatusListener = object :
        StatusHandler.OnStatusListener {
        override fun onStatus(status: Status) {
            when (strategy) {
                StatusHandlerCombinationStrategy.First, StatusHandlerCombinationStrategy.Merge -> status(status)
                StatusHandlerCombinationStrategy.OnlyFirstInitialized -> {
                    if (!status.isInitial && !initialized) {
                        statusHandler2.removeOnStatusListener(listener2)
                        initialized = true
                    }
                    status(status)
                }
                is StatusHandlerCombinationStrategy.CombineLatest -> status(strategy.statusCombinationStrategy(status, statusHandler2.status))
            }
        }
    }

    private val listener2: StatusHandler.OnStatusListener = object :
        StatusHandler.OnStatusListener {
        override fun onStatus(status: Status) {
            when (strategy) {
                StatusHandlerCombinationStrategy.Last, StatusHandlerCombinationStrategy.Merge -> status(status)
                StatusHandlerCombinationStrategy.OnlyFirstInitialized -> {
                    if (!status.isInitial && !initialized) {
                        statusHandler1.removeOnStatusListener(listener1)
                        initialized = true
                    }
                    status(status)
                }
                is StatusHandlerCombinationStrategy.CombineLatest -> status(strategy.statusCombinationStrategy(statusHandler1.status, status))
            }
        }
    }

    override fun addOnStatusListener(listener: StatusHandler.OnStatusListener) {
        val sizeBefore = onStatusListeners.size
        super.addOnStatusListener(listener)
        if (sizeBefore == 0 && onStatusListeners.size > 0) {
            if (strategy !== StatusHandlerCombinationStrategy.Last) {
                statusHandler1.addOnStatusListener(listener1)
            }
            if (strategy !== StatusHandlerCombinationStrategy.First) {
                statusHandler2.addOnStatusListener(listener2)
            }
        }
    }

    override fun removeOnStatusListener(listener: StatusHandler.OnStatusListener) {
        val sizeBefore = onStatusListeners.size
        super.removeOnStatusListener(listener)
        if (sizeBefore > 0 && onStatusListeners.size == 0) {
            if (strategy !== StatusHandlerCombinationStrategy.Last) {
                statusHandler1.removeOnStatusListener(listener1)
            }
            if (strategy !== StatusHandlerCombinationStrategy.First) {
                statusHandler2.removeOnStatusListener(listener2)
            }
        }
    }
}

fun StatusHandler.Companion.combine(
    sh1: StatusHandler,
    sh2: StatusHandler,
    strategy: StatusHandlerCombinationStrategy = StatusHandlerCombinationStrategy.CombineLatest(StatusCombinationStrategy.Default)
): StatusHandler = CombinedStatusHandler(sh1, sh2, strategy)

fun StatusHandler.combineWith(
    other: StatusHandler,
    strategy: StatusHandlerCombinationStrategy = StatusHandlerCombinationStrategy.CombineLatest(StatusCombinationStrategy.Default)
): StatusHandler = CombinedStatusHandler(this, other, strategy)