package com.example.course_work.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp

// Стиль для тексту кнопок
val TextOnBotton = TextStyle(
    color = Color.Black,
    fontSize = 24.sp,
    fontWeight = FontWeight.SemiBold
)

// Стиль для заголовку
val Title = TextStyle(
    fontSize = 36.sp,
    fontWeight = FontWeight.ExtraBold,
    color = Color.Black,
    lineHeight = 44.sp, // Відступ між рядками
    textAlign = TextAlign.Center
)

// Стиль введеного тексту
val Input = TextStyle(
    fontSize = 20.sp,
    fontWeight = FontWeight.Medium,
    color = Color.Black
)