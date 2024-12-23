package com.example.course_work.ui.registr_page

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.course_work.functions.getUsersFromFirestore
import com.example.course_work.functions.saveUserToFirestore
import com.example.course_work.ui.fixed_element.Footer
import com.example.course_work.ui.fixed_element.UserMenu
import com.example.course_work.ui.fixed_element.showErrorDialog
import com.example.course_work.ui.theme.Grey
import com.example.course_work.ui.theme.Input
import com.example.course_work.ui.theme.LightBlue
import com.example.course_work.ui.theme.LightYellow
import com.example.course_work.ui.theme.TextOnBotton
import com.example.course_work.ui.theme.Title
import kotlinx.coroutines.launch

@Composable
fun RegistrPage(
    goBack: () -> Unit,
    goToLogin: () -> Unit,
    goToRegistr: () -> Unit,
) {
    var isDialogVisible by remember { mutableStateOf(false) }
    val login by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val showDialog = remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightYellow) // Світло-жовтий фон
    ) {
        Column(
            modifier = Modifier.verticalScroll(rememberScrollState())
                .weight(1f)
        ) {
            Spacer(modifier = Modifier.height(130.dp))
            // Основний текст
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Реєстрація",
                    style = Title,
                    modifier = Modifier
                        .width(262.dp)
                )
            }
            // Поле вводу
            Spacer(modifier = Modifier.height(50.dp))
            var textName by remember { mutableStateOf("") } // Стан тексту
            var textPassword by remember { mutableStateOf("") } // Стан тексту
            var textPasswordRepeat by remember { mutableStateOf("") } // Стан тексту
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(60.dp)
                    .align(Alignment.CenterHorizontally)
                    .background(Grey, shape = RoundedCornerShape(10.dp))
                    .padding(16.dp),
            ) {
                // Placeholder (якщо текст порожній)
                if (textName.isEmpty()) {
                    Text(
                        text = "Ім'я", // Placeholder
                        color = Color.Gray, // Сірий колір тексту
                        fontSize = 16.sp
                    )
                }
                // BasicTextField для вводу тексту
                BasicTextField(
                    value = textName,
                    onValueChange = {
                        textName = it
                    },
                    modifier = Modifier.fillMaxSize(),
                    textStyle = Input
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(60.dp)
                    .align(Alignment.CenterHorizontally)
                    .background(Grey, shape = RoundedCornerShape(10.dp))
                    .padding(16.dp),
            ) {
                // Placeholder (якщо текст порожній)
                if (textPassword.isEmpty()) {
                    Text(
                        text = "Пароль", // Placeholder
                        color = Color.Gray, // Сірий колір тексту
                        fontSize = 16.sp
                    )
                }
                // BasicTextField для вводу тексту
                BasicTextField(
                    value = textPassword,
                    onValueChange = {
                        textPassword = it
                    },
                    modifier = Modifier.fillMaxSize(),
                    textStyle = Input
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(60.dp)
                    .align(Alignment.CenterHorizontally)
                    .background(Grey, shape = RoundedCornerShape(10.dp))
                    .padding(16.dp),
            ) {
                // Placeholder (якщо текст порожній)
                if (textPasswordRepeat.isEmpty()) {
                    Text(
                        text = "Повторіть пароль", // Placeholder
                        color = Color.Gray, // Сірий колір тексту
                        fontSize = 16.sp
                    )
                }
                // BasicTextField для вводу тексту
                BasicTextField(
                    value = textPasswordRepeat,
                    onValueChange = {
                        textPasswordRepeat = it
                    },
                    modifier = Modifier.fillMaxSize(),
                    textStyle = Input
                )
            }
            // Кнопка "Зареєструватися"
            Spacer(modifier = Modifier.height(30.dp)) // Відступ
            Button(
                onClick = {
                    coroutineScope.launch {
                        if (textPassword != "" && textName != "") {
                            if (textPassword == textPasswordRepeat) {
                                getUsersFromFirestore(
                                    onSuccess = { savedUsers ->
                                        if (savedUsers.none { it.username == textName }) {
                                            saveUserToFirestore(
                                                username = textName,
                                                password = textPassword,
                                                onSuccess = {
                                                    // Користувача успішно збережено
                                                    goToLogin()
                                                },
                                                onFailure = { exception ->
                                                    // Помилка при збереженні користувача
                                                    showDialog.value = true
                                                    errorMessage = exception.message ?: "Помилка при збереженні користувача."
                                                }
                                            )
                                        } else {
                                            showDialog.value = true
                                            errorMessage = "Користувач з таким іменем уже існує. Будь ласка введіть інше ім'я"
                                        }
                                    },
                                    onFailure = { exception ->
                                        // Обробка помилки
                                        showDialog.value = true
                                        println("Помилка при отриманні користувачів: ${exception.message}")
                                    }
                                )
                            } else {
                                showDialog.value = true
                                errorMessage = "Паролі не збігаються."
                            }
                        } else {
                            showDialog.value = true
                            errorMessage = "Поля не мають бути порожніми."
                        }
                    }
                },
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .height(50.dp)
                    .fillMaxWidth(0.7f)
                    .background(LightBlue, shape = RoundedCornerShape(10.dp)),
                colors = ButtonDefaults.buttonColors(containerColor = LightBlue)
            ) {
                Text(
                    text = "Зареєструватися",
                    style = TextOnBotton
                )
            }
            showErrorDialog(showDialog, errorMessage)
            Spacer(modifier = Modifier.height(80.dp)) // Зсунення елементів вниз
        }
    }
    Footer(goBack, onIconClick = { isDialogVisible = !isDialogVisible })
    // Вікно діалогу
    if (isDialogVisible) {
        UserMenu(
            onDismiss = { isDialogVisible = false },
            fun1 = {
                // Перехід до реєстрації
                goToRegistr()
            },
            fun2 = {
                // Перехід до авторизації
                goToLogin()
            },
            login
        )
    }
}