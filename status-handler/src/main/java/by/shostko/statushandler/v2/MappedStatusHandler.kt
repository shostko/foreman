package by.shostko.statushandler.v2

private class MappedStatusHandler(
    private val statusHandler: StatusHandler,
    private val mapper: (Status) -> Status
) : AbsStatusHandler(), StatusHandler.OnStatusListener {

    override val status: Status
        get() = mapper(statusHandler.status)

    override fun onStatus(status: Status) {
        notifyListeners(mapper(status))
    }

    override fun onFirstListenerAdded() {
        statusHandler.addOnStatusListener(this)
    }

    override fun onLastListenerRemoved() {
        statusHandler.removeOnStatusListener(this)
    }
}

fun StatusHandler.map(mapper: (Status) -> Status): StatusHandler = MappedStatusHandler(this, mapper)