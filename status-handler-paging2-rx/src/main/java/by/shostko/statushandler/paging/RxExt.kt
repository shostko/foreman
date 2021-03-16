package by.shostko.statushandler.paging

import io.reactivex.Single

internal fun <T> Single<T>.blockingGetWithoutWrap(): T = try {
    onErrorResumeNext {
        when (it) {
            is java.lang.Error -> Single.error<T>(it)
            is RuntimeException -> Single.error<T>(it)
            else -> Single.error<T>(WrappingException(it))
        }
    }.blockingGet()
} catch (e: WrappingException) {
    throw e.cause
}

private class WrappingException(override val cause: Throwable) : RuntimeException(cause)