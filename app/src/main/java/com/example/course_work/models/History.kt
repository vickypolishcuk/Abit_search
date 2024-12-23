package com.example.course_work.models

import kotlinx.serialization.Serializable

@Serializable
data class History(
    val name: String = "",
    val year: String = "",
    val region: String = "",
    val point: String = "",
    val date: String = "",
    val data: List<Search> = emptyList()
)
