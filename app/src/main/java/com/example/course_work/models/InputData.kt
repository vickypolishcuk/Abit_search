package com.example.course_work.models

import kotlinx.serialization.Serializable

@Serializable
data class InputData(
    val surname: String = "",
    val name: String = "",
    val patronymic: String = "",
    val selectedYear: String = "",
    val selectedRegion: String = "",
    val point: String = "",
)
