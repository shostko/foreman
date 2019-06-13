package by.shostko.statushandler.paging.pagekeyed

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.paging.PageKeyedDataSource

abstract class PageKeyedLifecycledDataSource<K, V> : PageKeyedDataSource<K, V>(), LifecycleOwner {

    private val lifecycleRegistry: LifecycleRegistry by lazy { LifecycleRegistry(this) }

    init {
        addInvalidatedCallback { lifecycleRegistry.markState(Lifecycle.State.DESTROYED) }
        lifecycleRegistry.markState(Lifecycle.State.STARTED)
    }

    override fun getLifecycle(): Lifecycle = lifecycleRegistry
}