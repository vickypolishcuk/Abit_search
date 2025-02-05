package com.example.course_work.ui.history_page

import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.course_work.functions.LoginViewModel
import com.example.course_work.models.History
import com.example.course_work.ui.fixed_element.Footer
import com.example.course_work.ui.fixed_element.UserMenu
import com.example.course_work.ui.theme.Grey
import com.example.course_work.ui.theme.Input
import com.example.course_work.ui.theme.LightYellow
import com.example.course_work.ui.theme.Title
import kotlinx.coroutines.launch
import com.example.course_work.functions.HistoryViewModel
import com.example.course_work.functions.InputDataViewModel
import com.example.course_work.functions.clearCurrentUser
import com.example.course_work.functions.clearUserHistoryFirestore
import com.example.course_work.functions.getCurrentUser
import com.example.course_work.models.InputData
import com.example.course_work.models.Search
import com.example.course_work.ui.icons.AppIcons

@Composable
fun HistoryPage(
    loginViewModel: LoginViewModel,
    inputDataViewModel: InputDataViewModel,
    goBack: () -> Unit,
    goToHistory: () -> Unit,
    goToSearchPage: (List<Search>) -> Unit,
) {
    var showDialog by remember { mutableStateOf(false) }
    var isDialogVisible by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val viewModel: HistoryViewModel = viewModel()
    val history by viewModel.history.collectAsState()
    val currentUser = getCurrentUser(context)

    if (currentUser != null) {
        LaunchedEffect(Unit) {
            viewModel.loadUserHistory(currentUser) // Завантаження історії при першому запуску
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightYellow) // Світло-жовтий фон
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
                .clickable {
                    showDialog = true
                },
            contentAlignment = Alignment.CenterEnd
        ) {
            // Іконка
            Icon(
                painter = AppIcons.Basket(), // Використання власної іконки
                contentDescription = "Basket Icon",
                tint = Color.Black,
                modifier = Modifier.size(30.dp) // Розмір іконки
            )
        }
        // Діалог для підтвердження видалення
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false }, // Закрити діалог
                title = { Text("Підтвердження") },
                text = { Text("Чи дійсно ви бажаєте видалити дані?") },
                confirmButton = {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                val username = getCurrentUser(context)!!
                                clearUserHistoryFirestore(
                                    username = username,
                                    onSuccess = {
                                        // Після успішного очищення історії оновлюємо ViewModel
                                        viewModel.loadUserHistory(username)
                                        showDialog = false // Закриваємо діалог
                                    },
                                    onFailure = { exception ->
                                        // Обробка помилки
                                        Log.e("ConfirmButton", "Error clearing user history", exception)
                                    }
                                )
                            }
                            showDialog = false // Закриваємо діалог
                        }
                    ) {
                        Text("Так")
                    }
                },
                dismissButton = {
                    Button(onClick = { showDialog = false }) {
                        Text("Ні")
                    }
                }
            )
        }
        // Прокручувана частина
        Box(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Column {
                Spacer(modifier = Modifier.height(30.dp))
                // Основний текст
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Історія пошуку",
                        style = Title,
                        modifier = Modifier.width(262.dp)
                    )
                }
                Spacer(modifier = Modifier.height(60.dp))
                if (history.isEmpty()) {
                    // Якщо історія порожня
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Історія порожня",
                            style = Input,
                            color = Color.Black
                        )
                    }
                } else {
                    history.indices.reversed().forEach { index ->
                        val searchQuery = history[index]
                        HistoryItem(searchQuery, goToSearchPage, inputDataViewModel)
                        Spacer(modifier = Modifier.height(8.dp)) // Проміжок між елементами
                    }
                }
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
    Footer(goBack, onIconClick = { isDialogVisible = !isDialogVisible })

    // Вікно діалогу
    if (isDialogVisible) {
        UserMenu(
            onDismiss = { isDialogVisible = false },
            fun1 = {
                // Перехід до історії
                goToHistory()
            },
            fun2 = {
                // Розлогінення
                coroutineScope.launch {
                    val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                    with(sharedPreferences.edit()) {
                        remove("username")
                        remove("password")
                        apply()
                    }
                    isDialogVisible = false
                    clearCurrentUser(context) // Виклик функції очищення поточного користувача
                    loginViewModel.logOut()
                    goBack()
                }
            },
            login = loginViewModel.isLoggedIn.collectAsState().value
        )
    }
}

@Composable
fun HistoryItem(searchQuery: History, goToSearchPage: (List<Search>) -> Unit, inputDataViewModel: InputDataViewModel) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .background(Grey, shape = RoundedCornerShape(10.dp))
            .padding(16.dp)
            .clickable {
                val parts = searchQuery.name.split(" ")
                val newInputData = InputData(
                    parts[0],
                    parts[1][0].toString(),
                    parts[2][0].toString(),
                    searchQuery.year,
                    searchQuery.region,
                    searchQuery.point
                )
                inputDataViewModel.setInputData(newInputData)
                goToSearchPage(searchQuery.data)
            }
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Ліва частина
            Text(
                text = "${searchQuery.name} ${searchQuery.year} ${searchQuery.point}\n${searchQuery.region}",
                textAlign = TextAlign.Start,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
            // Права частина
            Text(
                text = searchQuery.date,
                textAlign = TextAlign.End,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
        }
    }
}
