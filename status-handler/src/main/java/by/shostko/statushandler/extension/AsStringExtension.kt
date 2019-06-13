package by.shostko.statushandler.extension

import androidx.paging.ItemKeyedDataSource
import androidx.paging.PageKeyedDataSource
import androidx.paging.PositionalDataSource

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