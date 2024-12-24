package com.example.course_work.functions

import androidx.lifecycle.ViewModel
import com.example.course_work.models.Majority
import com.example.course_work.models.University
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MajorityViewModel : ViewModel() {
    private val _majorities = MutableStateFlow<List<Majority>>(emptyList()) // Список спеціальностей
    val majorities: StateFlow<List<Majority>> get() = _majorities // Доступ до списку

    private val _universityInfo = MutableStateFlow<University?>(null)
    val universityInfo: StateFlow<University?> get() = _universityInfo

    fun setMajorities(newMajorities: List<Majority>) {
        _majorities.value = newMajorities
    }

    fun setUniversityInfo(university: University?) {
        _universityInfo.value = university
    }
}