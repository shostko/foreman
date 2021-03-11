@file:Suppress("unused")

package by.shostko.statushandler

class LazyStatusHandler<S : StatusHandler> : BaseStatusHandler(), StatusHandler.OnStatusListener {

    var wrapped: S? = null
        set(value) {
            field?.removeOnStatusListener(this)
            field = value
            if (value != null && hasListeners()) {
                status(value.status)
                value.addOnStatusListener(this)
            }
        }

    override fun onFirstListenerAdded() {
        wrapped?.addOnStatusListener(this)
    }

    override fun onLastListenerRemoved() {
        wrapped?.removeOnStatusListener(this)
    }

    override fun onStatus(status: Status) {
        status(status)
    }
}