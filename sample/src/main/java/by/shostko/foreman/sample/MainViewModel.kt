package by.shostko.foreman.sample

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import by.shostko.foreman.*

class MainViewModel(
    private val repository: MainRepository,
) : ViewModel() {

    companion object {
        fun factory() = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T = MainViewModel(
                repository = MainRepository(),
            ) as T
        }
    }

    val awesomeUnit = Foreman.prepare("unit", repository::doAwesomeWork)
    val awesomeValue = Foreman.prepare("value", repository::getAwesomeValue)
    val parametrizedValue = Foreman.prepare("param", repository::getParametrizedValue)
    val loremValues = Foreman.prepare("lorem", repository.observeLoremValues())
    val neverSucceed = Foreman.prepare("fails", repository::neverSucceed)
    val combinedDefault2345 = awesomeValue
        .combineWith(parametrizedValue)
        .combineWith(loremValues)
        .combineWith(neverSucceed, tag = "combined2345")
    val combinedDefault234 = awesomeValue
        .combineWith(parametrizedValue)
        .combineWith(loremValues, tag = "combined234")
    val combinedDefault432 = loremValues
        .combineWith(parametrizedValue)
        .combineWith(awesomeValue, tag = "combined432")
    val combinedCustom = Foreman.combine(
        workers = listOf(awesomeValue, parametrizedValue, loremValues),
        tag = "custom",
    ) { reports ->
        if (reports.all { it is Report.Success }) {
            Report.Success(
                buildString {
                    reports.forEachIndexed { index, report ->
                        if (index != 0) {
                            append('\n')
                        }
                        append(report.resultOrNull)
                    }
                }
            )
        } else {
            reports.firstOrNull { it is Report.Failed }
                ?: reports.firstOrNull { it is Report.Working }
                ?: reports.firstOrNull { it is Report.Success }
                ?: reports.firstOrNull()
                ?: Report.Initial
        }
    }

    init {
        awesomeUnit.launch(viewModelScope)
        awesomeValue.launch(viewModelScope)
        parametrizedValue.launch(System.currentTimeMillis(), viewModelScope)
        loremValues.launch(viewModelScope)
        neverSucceed.launch(viewModelScope)
    }

    fun onSomeEvent() {
        parametrizedValue.launch(System.currentTimeMillis(), viewModelScope)
    }
}
