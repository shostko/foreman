package by.shostko.foreman.sample

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onEach

class MainRepository {

    suspend fun doAwesomeWork() {
        delay(2000L)
    }

    suspend fun getAwesomeValue(): String {
        delay(1000L)
        return "Awesome value"
    }

    suspend fun getParametrizedValue(param: Long): String {
        delay(1000L)
        return "Param value: $param"
    }

    fun observeLoremValues(): Flow<String> =
        flowOf(
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
            "Integer at sem facilisis, eleifend lacus nec, imperdiet enim.",
            "Proin risus purus, ullamcorper aliquam pulvinar ac, finibus in est.",
            "In vestibulum suscipit dui sit amet fringilla.",
            "Vivamus nec sapien tempus, feugiat eros ut, molestie diam.",
        ).onEach { delay(1000L) }

    suspend fun neverSucceed(): String {
        delay(5000L)
        throw UnsupportedOperationException("Always fails")
    }
}
