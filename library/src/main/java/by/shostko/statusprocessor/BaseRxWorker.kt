@file:Suppress("unused")

package by.shostko.statusprocessor

import android.content.Context
import androidx.annotation.CallSuper
import androidx.work.Data
import androidx.work.RxWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import by.shostko.statusprocessor.Const.KEY_ERROR_MESSAGE
import by.shostko.statusprocessor.Const.KEY_ERROR_MESSAGE_LOCALIZED
import io.reactivex.Completable
import io.reactivex.Single

abstract class BaseRxWorker(context: Context, workerParameters: WorkerParameters) : RxWorker(context, workerParameters) {

    internal open fun throwableToData(throwable: Throwable): Data = workDataOf(
        KEY_ERROR_MESSAGE to throwable.message,
        KEY_ERROR_MESSAGE_LOCALIZED to throwable.localizedMessage
    )

    protected open fun throwableToResult(throwable: Throwable): Result = Result.failure(throwableToData(throwable))
}

abstract class BaseRxSingleWorker(context: Context, workerParameters: WorkerParameters) : BaseRxWorker(context, workerParameters) {

    @CallSuper
    override fun createWork(): Single<Result> = createSingleWork()
        .onErrorReturn(this::throwableToResult)

    protected abstract fun createSingleWork(): Single<Result>
}

abstract class BaseRxCompletableWorker(context: Context, workerParameters: WorkerParameters) : BaseRxWorker(context, workerParameters) {

    @CallSuper
    override fun createWork(): Single<Result> = createCompletableWork()
        .toSingleDefault(Result.success())
        .onErrorReturn(this::throwableToResult)

    protected abstract fun createCompletableWork(): Completable
}