@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package by.shostko.statushandler.viewmodel

import by.shostko.statushandler.*

abstract class SimpleViewModel : ProcessingViewModel<Unit>(Unit, SimpleStatusFactory()) {
    abstract class Message : CustomViewModel<String>("", MessageStatus.Factory())
    abstract class Class : CustomViewModel<java.lang.Class<out Throwable>>(NoErrorThrowable::class.java, ClassStatus.Factory())
}

abstract class ProcessingViewModel<E>(noError: E, factory: Status.Factory<E>?) : CustomViewModel<E>(noError, factory) {

    final override fun createBaseStatusHandler(factory: Status.Factory<E>): StatusHandler<E> = StatusHandlerImpl(factory)

    fun proceed() = delegate.proceed()

    fun refresh() = delegate.refresh()

    fun retry() = delegate.retry()
}