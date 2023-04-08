package by.shostko.statushandler.viewmodel

import androidx.annotation.CallSuper
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModel

abstract class LifecycledViewModel : ViewModel(), LifecycleOwner {

    private val registryDelegate = lazy {
        LifecycleRegistry(this).apply {
            markState(Lifecycle.State.STARTED)
        }
    }
    private val registry: LifecycleRegistry by registryDelegate

    override val lifecycle: Lifecycle
        get() = registry

    @CallSuper
    override fun onCleared() {
        super.onCleared()
        if (registryDelegate.isInitialized()) {
            registry.markState(Lifecycle.State.DESTROYED)
        }
    }
}