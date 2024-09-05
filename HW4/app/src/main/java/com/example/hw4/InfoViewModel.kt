package com.example.hw4

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class InfoViewModel: ViewModel() {
    private var passedCity: String = ""
    private var passedEvent: String = ""
    var passedResult = MutableLiveData<String>()

    fun setCity(cityName: String) {
        passedCity = cityName
    }

    fun setEvent(eventName: String) {
        passedEvent = eventName
    }

    fun setResults(results: String) {
        passedResult.value = results
    }

    fun returnCity(): String {
        return passedCity
    }

    fun returnEvent(): String {
        return passedEvent
    }
}