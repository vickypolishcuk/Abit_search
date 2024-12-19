package com.example.course_work.functions

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class LoginViewModel : ViewModel() {
    private val _isLoggedIn = MutableStateFlow(false) // Початкове значення false
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn

    fun logIn() {
        _isLoggedIn.value = true
    }

    fun logOut() {
        _isLoggedIn.value = false
    }
}