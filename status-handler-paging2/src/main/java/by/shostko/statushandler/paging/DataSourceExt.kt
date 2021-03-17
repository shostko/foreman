package by.shostko.statushandler.paging

import androidx.paging.ItemKeyedDataSource
import androidx.paging.PageKeyedDataSource
import androidx.paging.PositionalDataSource

internal fun <K> ItemKeyedDataSource.LoadInitialParams<K>.asString() =
    "LoadInitialParams(requestedInitialKey=$requestedInitialKey, requestedLoadSize=$requestedLoadSize, placeholdersEnabled=$placeholdersEnabled)"

internal fun <K> ItemKeyedDataSource.LoadParams<K>.asString() =
    "LoadParams(key=$key, requestedLoadSize=$requestedLoadSize)"

internal fun <K> PageKeyedDataSource.LoadInitialParams<K>.asString() =
    "LoadInitialParams(requestedLoadSize=$requestedLoadSize, placeholdersEnabled=$placeholdersEnabled)"

internal fun <K> PageKeyedDataSource.LoadParams<K>.asString() =
    "LoadParams(key=$key, requestedLoadSize=$requestedLoadSize)"

internal fun PositionalDataSource.LoadInitialParams.asString() =
    "LoadInitialParams(requestedStartPosition=$requestedStartPosition, requestedLoadSize=$requestedLoadSize, pageSize=$pageSize, placeholdersEnabled=$placeholdersEnabled)"

internal fun PositionalDataSource.LoadRangeParams.asString() =
    "LoadRangeParams(startPosition=$startPosition, loadSize=$loadSize)"
