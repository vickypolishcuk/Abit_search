package com.example.course_work.functions

import com.example.course_work.models.History
import androidx.lifecycle.ViewModel
import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class HistoryViewModel : ViewModel() {
    private val _history = MutableStateFlow<List<History>>(emptyList())
    val history: StateFlow<List<History>> = _history

    fun loadUserHistory(context: Context) {
        viewModelScope.launch {
            val userHistory = getUserHistory(context) ?: emptyList()
            _history.value = userHistory
        }
    }
}