package by.shostko.statusprocessor.paging.itemkeyed

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.paging.ItemKeyedDataSource

abstract class ItemKeyedLifecycledDataSource<K, V> : ItemKeyedDataSource<K, V>(), LifecycleOwner {

    private val lifecycleRegistry: LifecycleRegistry by lazy { LifecycleRegistry(this) }

    init {
        addInvalidatedCallback { lifecycleRegistry.markState(Lifecycle.State.DESTROYED) }
        lifecycleRegistry.markState(Lifecycle.State.STARTED)
    }

    override fun getLifecycle(): Lifecycle = lifecycleRegistry
}