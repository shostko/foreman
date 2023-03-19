package by.shostko.foreman

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.lang.ref.WeakReference
import java.util.WeakHashMap

abstract class Worker<T : Any?, E : Any>(
    val tag: String? = null,
) {
    companion object {
        // required for ext
    }

    private val reportFlowInitializer = lazy { MutableStateFlow<Report<T, E>>(Report.Initial) }
    private val reportFlowMutable by reportFlowInitializer
    val reportFlow: StateFlow<Report<T, E>> by lazy {
        if (!reportFlowInitializer.isInitialized()) {
            reportFlowMutable.value = report
        }
        reportFlowMutable.asStateFlow()
    }

    private val listeners = ArrayList<WeakReference<OnReportUpdatedListener<T, E>>>(1)

    var report: Report<T, E> = Report.Initial
        private set(value) {
            val prev = field
            field = value
            onReportUpdated(from = prev, to = value)
        }

    private fun onReportUpdated(from: Report<T, E>, to: Report<T, E>) {
        if (reportFlowInitializer.isInitialized()) {
            reportFlowMutable.value = to
        }
        listeners.forEach { reference ->
            reference.get()?.invoke(from, to)
            // TODO remove null references
        }
    }

    fun addOnReportUpdatedListener(listener: OnReportUpdatedListener<T, E>) {
        listeners.add(WeakReference(listener))
    }

    fun removeOnReportUpdatedListener(listener: OnReportUpdatedListener<T, E>) {
        listeners.removeAll { it.get() == listener}
    }

    internal fun save(report: Report<T, E>) {
        this.report = report
        Foreman.log(tag, report)
    }
}
