package by.shostko.statusprocessor.viewmodel

import androidx.annotation.CallSuper
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModel

abstract class LifecycledViewModel : ViewModel(), LifecycleOwner {

    private val registry: LifecycleRegistry by lazy {
        LifecycleRegistry(this).apply {
            markState(Lifecycle.State.STARTED)
        }
    }

    override fun getLifecycle(): Lifecycle = registry

    @CallSuper
    override fun onCleared() {
        super.onCleared()
        registry.markState(Lifecycle.State.DESTROYED)
    }
}