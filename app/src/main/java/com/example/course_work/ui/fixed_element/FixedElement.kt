package com.example.course_work.ui.fixed_element

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import com.example.course_work.ui.icons.AppIcons
import com.example.course_work.ui.theme.Blue
import com.example.course_work.ui.theme.LightBlue
import com.example.course_work.ui.theme.TextOnBotton


@Composable
fun UserMenu(onDismiss: () -> Unit, fun1: () -> Unit, fun2: () -> Unit, login: Boolean) {
    val buttonText1: String
    val buttonText2: String

    if (login) {
        buttonText1 = "Історія пошуку"
        buttonText2 = "Вийти"
    } else {
        buttonText1 = "Зареєструватися"
        buttonText2 = "Увійти"
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.4f))
            .clickable ( onClick = onDismiss ), // Закриває при натисканні поза межами діалогу
    )
    // Віконце діалогу
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.BottomCenter // Центрування діалогу внизу екрана
    ) {
        Box(
            modifier = Modifier
                .offset(y= (-43).dp)
                .size(120.dp),
            contentAlignment = Alignment.Center
        ) {
            // Картинка трикутника
            Icon(
                painter = AppIcons.Triangle(), // Використання іконки
                contentDescription = "User Icon",
                tint = Blue,
                modifier = Modifier.size(60.dp) // Розмір трикутника
            )
        }
        Box(
            modifier = Modifier
                .offset(y= (-103).dp)
                .fillMaxWidth(0.9f)
                .background(Blue, shape = RoundedCornerShape(10.dp))
                .padding(20.dp),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(16.dp).align(Alignment.Center)
            ) {
                Button(
                    onClick = { fun1() },
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .height(50.dp)
                        .fillMaxWidth(0.9f)
                        .background(LightBlue, shape = RoundedCornerShape(10.dp)),
                    colors = ButtonDefaults.buttonColors(containerColor = LightBlue)
                ) {
                    Text(
                        text = buttonText1,
                        style = TextOnBotton,
                    )
                }
                Spacer(modifier = Modifier.height(20.dp)) // Відступ між кнопками
                Button(
                    onClick = { fun2() },
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .height(50.dp)
                        .fillMaxWidth(0.9f)
                        .background(LightBlue, shape = RoundedCornerShape(10.dp)),
                    colors = ButtonDefaults.buttonColors(containerColor = LightBlue)
                ) {
                    Text(
                        text = buttonText2,
                        style = TextOnBotton
                    )
                }
            }
        }
    }
}

@Composable
fun Footer(navigate: () -> Unit, onIconClick: () -> Unit, mainPage: Boolean = false) {
    Box(
        modifier = Modifier
            .fillMaxSize() // Заповнює весь екран
    ) {
        // Прямокутник, закріплений унизу
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp) // Висота прямокутника
                .align(Alignment.BottomCenter) // Закріплення внизу екрана
                .background(
                    Blue, // Синій фон
                    shape = RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp) // Заокруглення верхніх кутів
                )
        ) {
            // Перша половина: кнопка "На головну"
            Box(
                modifier = Modifier
                    .size(50.dp) // Розмір кола
                    .align(Alignment.BottomStart) // Вирівнювання по центру
                    .background(Blue, shape = CircleShape) // Синє коло
                    .clickable { navigate() },
                contentAlignment = Alignment.Center
            ) {
                val icon: Painter = if (mainPage) {
                    AppIcons.RateList()
                } else {
                    AppIcons.ArrowLeft()
                }
                Icon(
                    painter = icon,
                    contentDescription = "На головну",
                    tint = Color.Black,
                    modifier = Modifier.size(27.dp)
                )
            }
            // Коло з іконкою, яке виступає за межі прямокутника
            Box(
                modifier = Modifier
                    .size(50.dp) // Розмір кола
                    .align(Alignment.BottomEnd) // Вирівнювання по краю
                    .background(Blue, shape = CircleShape) // Синє коло
                    .clickable { onIconClick() }, // Відкриття віконця при натисканні
                contentAlignment = Alignment.Center
            ) {
                // Іконка всередині кола
                Icon(
                    painter = AppIcons.User(), // Використання власної іконки
                    contentDescription = "User Icon",
                    tint = Color.Black,
                    modifier = Modifier.size(30.dp) // Розмір іконки
                )
            }
        }
    }
}

@Composable
fun showErrorDialog(showDialog: MutableState<Boolean>, errorMessage: String) {
    if (showDialog.value) {
        AlertDialog(
            onDismissRequest = { showDialog.value = false },
            title = { Text("Помилка") },
            text = { Text(errorMessage) },
            confirmButton = {
                Button(onClick = { showDialog.value = false }) {
                    Text("ОК")
                }
            }
        )
    }
}
