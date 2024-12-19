package com.example.course_work.models

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val username: String,
    val password: String,
    val history: MutableList<History> = mutableListOf()
)