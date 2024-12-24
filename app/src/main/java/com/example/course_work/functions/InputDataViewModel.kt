package com.example.course_work.functions

import androidx.lifecycle.ViewModel
import com.example.course_work.models.InputData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class InputDataViewModel : ViewModel() {
    private val _inputData = MutableStateFlow(InputData()) // Список спеціальностей
    val inputData: StateFlow<InputData> get() = _inputData // Доступ до списку

    fun setInputData(inputData: InputData) {
        _inputData.value = inputData
    }
}