package by.shostko.statushandler.paging

import android.util.Log

internal object Logger {

    private const val ENABLED = false

    fun e(tag: String, throwable: Throwable, message: String, vararg args: Any?) {
        if (ENABLED) {
            Log.e(tag, message.format(*args), throwable)
        }
    }

    fun d(tag: String, message: String, vararg args: Any?) {
        if (ENABLED) {
            Log.v(tag, message.format(*args))
        }
    }
}