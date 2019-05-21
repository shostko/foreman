@file:Suppress("unused")

package by.shostko.statusprocessor

import androidx.work.Data
import androidx.work.workDataOf
import by.shostko.statusprocessor.Const.KEY_DIRECTION
import by.shostko.statusprocessor.Const.KEY_ERROR
import by.shostko.statusprocessor.Const.KEY_ERROR_MESSAGE_LOCALIZED
import by.shostko.statusprocessor.Const.KEY_STATUS

abstract class BaseLoadingStatus<E>(open val direction: Direction, open val error: E?)

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
    override val direction: Direction,
    override val error: String?
) : BaseLoadingStatus<String>(direction, error) {

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

        fun success(error: String? = null): LoadingStatus {
            return LoadingStatus(Status.SUCCESS, Direction.NONE, error)
        }

        fun error(error: String? = null): LoadingStatus {
            return LoadingStatus(Status.ERROR, Direction.NONE, error)
        }

        fun error(throwable: Throwable): LoadingStatus {
            return LoadingStatus(Status.ERROR, Direction.NONE, throwable.message)
        }
    }
}

fun LoadingStatus.toWorkerData(): Data = workDataOf(
    KEY_STATUS to status.name,
    KEY_DIRECTION to direction.name,
    KEY_ERROR to error
)

fun LoadingStatus.Companion.fromWorkerData(data: Data): LoadingStatus = LoadingStatus(
    status = data.getString(KEY_STATUS)?.let { Status.valueOf(it) } ?: throw UnsupportedOperationException("$KEY_STATUS should be provided"),
    direction = data.getString(KEY_DIRECTION)?.let { Direction.valueOf(it) }
        ?: throw UnsupportedOperationException("$KEY_DIRECTION should be provided"),
    error = data.getString(KEY_ERROR)
)

fun LoadingStatus.Companion.fromWorkerFailedData(data: Data): LoadingStatus = error(
    error = data.getString(KEY_ERROR_MESSAGE_LOCALIZED)
)