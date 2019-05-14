@file:Suppress("unused")

package by.shostko.statusprocessor

import androidx.work.Data
import androidx.work.workDataOf
import by.shostko.statusprocessor.Const.KEY_DIRECTION
import by.shostko.statusprocessor.Const.KEY_ERROR_MESSAGE_LOCALIZED
import by.shostko.statusprocessor.Const.KEY_MESSAGE
import by.shostko.statusprocessor.Const.KEY_STATUS

enum class Status {
    SUCCESS,
    ERROR,
    LOADING
}

enum class Direction {
    BACKWARD,
    FORWARD,
    FULL,
    NONE
}

data class LoadingStatus(
    val status: Status,
    val direction: Direction,
    val message: String?
) {

    companion object {

        fun loading(direction: Direction): LoadingStatus {
            return LoadingStatus(Status.LOADING, direction, null)
        }

        fun loading(): LoadingStatus {
            return LoadingStatus(Status.LOADING, Direction.FULL, null)
        }

        fun loadingBackward(): LoadingStatus {
            return LoadingStatus(Status.LOADING, Direction.BACKWARD, null)
        }

        fun loadingForward(): LoadingStatus {
            return LoadingStatus(Status.LOADING, Direction.FORWARD, null)
        }

        fun success(message: String? = null): LoadingStatus {
            return LoadingStatus(Status.SUCCESS, Direction.NONE, message)
        }

        fun error(message: String? = null): LoadingStatus {
            return LoadingStatus(Status.ERROR, Direction.NONE, message)
        }

        fun error(throwable: Throwable): LoadingStatus {
            return LoadingStatus(Status.ERROR, Direction.NONE, throwable.message)
        }
    }
}

fun LoadingStatus.toWorkerData(): Data = workDataOf(
    KEY_STATUS to status.name,
    KEY_DIRECTION to direction.name,
    KEY_MESSAGE to message
)

fun LoadingStatus.Companion.fromWorkerData(data: Data): LoadingStatus = LoadingStatus(
    status = data.getString(KEY_STATUS)?.let { Status.valueOf(it) } ?: throw UnsupportedOperationException("$KEY_STATUS should be provided"),
    direction = data.getString(KEY_DIRECTION)?.let { Direction.valueOf(it) }
        ?: throw UnsupportedOperationException("$KEY_DIRECTION should be provided"),
    message = data.getString(KEY_MESSAGE)
)

fun LoadingStatus.Companion.fromWorkerFailedData(data: Data): LoadingStatus = LoadingStatus.error(
    message = data.getString(KEY_ERROR_MESSAGE_LOCALIZED)
)