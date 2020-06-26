package by.shostko.statushandler.v2.rx

import java.util.NoSuchElementException

internal data class Optional<T : Any?>(
    val value: T
) {
    fun get(): T = value ?: throw NoSuchElementException("No value present")
    fun getOrElse(other: T): T = value ?: other
    fun getOrElse(otherProvider: () -> T): T = value ?: otherProvider()
    fun getOrNull(): T? = value
    fun isPresent(): Boolean = value != null
    fun ifPresent(consumer: (T) -> Unit) {
        if (value != null) consumer(value)
    }
}