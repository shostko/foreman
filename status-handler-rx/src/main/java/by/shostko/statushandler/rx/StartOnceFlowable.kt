package by.shostko.statushandler.rx

import io.reactivex.Flowable
import java.util.concurrent.atomic.AtomicBoolean

internal fun <T> Flowable<T>.startOnce(value: T): Flowable<T> = StartOnceFlowable(this, value)

private class StartOnceFlowable<T>(
    source: Flowable<T>,
    private val value: T
) : InitialValueFlowable.WithSource<T>(source) {

    private val once: AtomicBoolean = AtomicBoolean(false)

    override val initialValue: T?
        get() = if (once.getAndSet(true)) null else value
}
