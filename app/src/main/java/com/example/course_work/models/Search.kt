package com.example.course_work.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class Search(
    val okr: String, // окр (магістр / бакалавр)
    val status: String, // статус
    val numberOfPlace: String, // номер у списку
    val priority: String, // пріоритет заяви
    val point: String, // конкурсний бал
    val sbo: String, // середній бал документа про освіту
    val consistOfSubjects: List<String>,// складові конкурсного балу
    val consistOfPoint: List<String>,// складові конкурсного балу
    val university: String, // навчальний заклад
    val majority: String, // спеціальність
    val kvota: String, // квота
    val doc: String, // документи
    val hrefToRateList: String
)

fun List<Search>.toJson(): String = Json.encodeToString(this)
