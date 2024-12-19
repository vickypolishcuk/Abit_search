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

    fun search(pib: String, currentOption: String, selectedRegion: String) {
        viewModelScope.launch {
            val results = parseSearchAsync(pib, currentOption, selectedRegion)
            _searchResults.value = results // Оновлюємо StateFlow з результатами
        }
    }
    fun setSearchResults(results: List<Search>) {
        _searchResults.value = results
    }
}