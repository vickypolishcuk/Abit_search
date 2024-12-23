package com.example.course_work.functions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.course_work.models.Search
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SearchFunViewModel : ViewModel() {
    private val _searchResults = MutableStateFlow<List<Search>>(emptyList()) // Створюємо StateFlow для результатів пошуку
    val searchResults: StateFlow<List<Search>> get() = _searchResults // Отримуємо StateFlow для читання

    private val _onSearchComplete = MutableStateFlow<(List<Search>) -> Unit>({})
    val onSearchComplete: StateFlow<(List<Search>) -> Unit> get() = _onSearchComplete

    private val _isParsing = MutableStateFlow(false) // Стан обробки
    val isParsing: StateFlow<Boolean> get() = _isParsing

    fun search(pib: String, currentOption: String, selectedRegion: String, onComplete: (List<Search>) -> Unit = {}) {
        _onSearchComplete.value = onComplete // Задаємо дію після завершення парсингу
        viewModelScope.launch {
            _isParsing.value = true // Початок обробки
            val results = parseSearchAsync(pib, currentOption, selectedRegion)
            _searchResults.value = results
            _onSearchComplete.value.invoke(results) // Викликаємо дію після завершення
            _onSearchComplete.value = {} // Скидаємо дію
            _isParsing.value = false // Завершення обробки
        }
    }
    fun setSearchResults(results: List<Search>) {
        _searchResults.value = results
    }
}