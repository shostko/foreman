package by.shostko.statusprocessor.viewmodel

import by.shostko.statusprocessor.StatusProcessor

@Suppress("unused")
abstract class SimpleViewModel : BaseViewModel<String>(EMPTY, { StatusProcessor(it) }) {

    companion object {
        private const val EMPTY = ""
    }
}