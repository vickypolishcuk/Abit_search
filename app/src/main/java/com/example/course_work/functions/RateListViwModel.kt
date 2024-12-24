package com.example.course_work.functions

import androidx.lifecycle.ViewModel
import com.example.course_work.models.MajorityInfo
import com.example.course_work.models.RateData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class RateListViwModel : ViewModel() {
    private val _rateData = MutableStateFlow<List<RateData>>(emptyList()) // Список спеціальностей
    val rateData: StateFlow<List<RateData>> get() = _rateData // Доступ до списку

    private val _majorityInfo = MutableStateFlow<MajorityInfo?>(null)
    val majorityInfo: StateFlow<MajorityInfo?> get() = _majorityInfo

    fun setRateData(newRateData: List<RateData>) {
        _rateData.value = newRateData
    }

    fun setMajorityInfo(newMajorityInfo: MajorityInfo?) {
        _majorityInfo.value = newMajorityInfo
    }
}