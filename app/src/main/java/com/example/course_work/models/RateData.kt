package com.example.course_work.models

data class RateData(
    val name: String,
    val number: String,
    val priority: String,
    val status: String,
    val point: String,
    val kvota: String,
    val doc: String,
    val consistOfSubjects: List<String>,// складові конкурсного балу (предмети)
    val consistOfPoint: List<String>,// складові конкурсного балу (бали)
)
