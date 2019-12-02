package by.shostko.statushandler.paging

import androidx.paging.ItemKeyedDataSource
import androidx.paging.PageKeyedDataSource
import androidx.paging.PositionalDataSource
import io.reactivex.Single

fun Any.asString() = toString()

fun <K> ItemKeyedDataSource.LoadInitialParams<K>.asString() =
    "LoadInitialParams(requestedInitialKey=$requestedInitialKey, requestedLoadSize=$requestedLoadSize, placeholdersEnabled=$placeholdersEnabled)"

fun <K> ItemKeyedDataSource.LoadParams<K>.asString() =
    "LoadParams(key=$key, requestedLoadSize=$requestedLoadSize)"

fun <K> PageKeyedDataSource.LoadInitialParams<K>.asString() =
    "LoadInitialParams(requestedLoadSize=$requestedLoadSize, placeholdersEnabled=$placeholdersEnabled)"

fun <K> PageKeyedDataSource.LoadParams<K>.asString() =
    "LoadParams(key=$key, requestedLoadSize=$requestedLoadSize)"

fun PositionalDataSource.LoadInitialParams.asString() =
    "LoadInitialParams(requestedStartPosition=$requestedStartPosition, requestedLoadSize=$requestedLoadSize, pageSize=$pageSize, placeholdersEnabled=$placeholdersEnabled)"

fun PositionalDataSource.LoadRangeParams.asString() =
    "LoadRangeParams(startPosition=$startPosition, loadSize=$loadSize)"

fun <T> Single<T>.blockingGetWithoutWrap(): T = try {
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