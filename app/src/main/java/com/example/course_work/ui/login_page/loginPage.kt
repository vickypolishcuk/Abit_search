package com.example.course_work.ui.login_page

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
import androidx.compose.runtime.collectAsState
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
import com.example.course_work.functions.LoginViewModel
import com.example.course_work.functions.getUsers
import com.example.course_work.functions.saveCurrentUser
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
fun LoginPage(
    loginViewModel: LoginViewModel,
    goBack: () -> Unit,
    goToLogin: () -> Unit,
    goToRegistr: () -> Unit,
) {
    var isDialogVisible by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val showDialog = remember { mutableStateOf(false) }

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
                    text = "Вхід",
                    style = Title,
                    modifier = Modifier
                        .width(262.dp)
                )
            }
            // Поле вводу
            Spacer(modifier = Modifier.height(80.dp))
            var textName by remember { mutableStateOf("") } // Стан тексту
            var textPassword by remember { mutableStateOf("") } // Стан тексту
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

            Spacer(modifier = Modifier.height(30.dp))

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

            // Кнопка "Пошук"
            Spacer(modifier = Modifier.height(30.dp)) // Відступ
            Button(
                onClick = {
                    coroutineScope.launch {
                        val savedUsers = getUsers(context) // Отримуємо список усіх збережених користувачів
                        val matchedUser = savedUsers.find { it.username == textName && it.password == textPassword }
                        if (matchedUser != null) {
                            // Успішний вхід
                            saveCurrentUser(context, matchedUser.username)
                            loginViewModel.logIn()
                            goBack()
                        } else {
                            // Помилка авторизації
                            showDialog.value = true
                        }
                    }
                },
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .width(150.dp)
                    .height(50.dp)
                    .background(LightBlue, shape = RoundedCornerShape(10.dp)),
                colors = ButtonDefaults.buttonColors(containerColor = LightBlue)
            ) {
                Text(
                    text = "Увійти",
                    style = TextOnBotton
                )
            }
            showErrorDialog(showDialog, "Невірно введені логін чи пароль")
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
            login = loginViewModel.isLoggedIn.collectAsState().value
        )
    }
}