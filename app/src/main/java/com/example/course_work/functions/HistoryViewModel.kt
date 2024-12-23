package com.example.course_work.functions

import com.example.course_work.models.History
import androidx.lifecycle.ViewModel
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class HistoryViewModel : ViewModel() {
    private val _history = MutableStateFlow<List<History>>(emptyList())
    val history: StateFlow<List<History>> = _history

    fun loadUserHistory(userId: String) {
        getHistory(
            username = userId,
            onSuccess = { userHistory ->
                _history.value = userHistory // Оновлюємо історію після успішного отримання
            },
            onFailure = { exception ->
                Log.e("HistoryViewModel", "Error loading user history", exception)
            }
        )
    }
}