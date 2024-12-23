package com.example.course_work.ui.rate_list_page

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.course_work.ui.theme.LightYellow
import com.example.course_work.ui.theme.Grey
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.example.course_work.functions.LoginViewModel
import com.example.course_work.functions.clearCurrentUser
import com.example.course_work.functions.parseRateList
import com.example.course_work.functions.parseMajorityInfo
import com.example.course_work.models.MajorityInfo
import com.example.course_work.models.RateData
import com.example.course_work.ui.fixed_element.Footer
import com.example.course_work.ui.icons.AppIcons
import com.example.course_work.ui.fixed_element.UserMenu
import com.example.course_work.ui.theme.Input
import com.example.course_work.ui.theme.Title
import kotlinx.coroutines.launch

@Composable
fun RateListPage(
    loginViewModel: LoginViewModel,
    goBack: () -> Unit,
    goToLogin: () -> Unit,
    goToRegistr: () -> Unit,
    goToHistory: () -> Unit,
    parameter: String,
) {
    val isLoggedIn by loginViewModel.isLoggedIn.collectAsState()
    var isDialogVisible by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightYellow) // Світло-жовтий фон
    ) {
        // Стан для збереження завантажених даних
        val rateList = remember { mutableStateOf<List<RateData>>(emptyList()) }
        val searchQuery = remember { mutableStateOf("") }
        val filteredList = remember { mutableStateOf<List<RateData>>(emptyList()) }
        val majorityInfo = remember { mutableStateOf<MajorityInfo?>(null) }

        // Завантаження даних у фоновому режимі
        LaunchedEffect(Unit) {
            try {
                rateList.value = parseRateList(parameter)
                filteredList.value = rateList.value
                majorityInfo.value = parseMajorityInfo(parameter)
                println(majorityInfo)
            } catch (e: Exception) {
                println("Помилка при завантаженні даних: ${e.message}")
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically // Вирівнювання по вертикалі
            ) {
                // Поле вводу для пошуку
                Column(
                    modifier = Modifier.weight(0.9f)
                ) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .background(Grey, shape = RoundedCornerShape(10.dp))
                            .padding(10.dp),
                    ) {
                        if (searchQuery.value.isEmpty()) {
                            Text(
                                text = "Введіть текст для пошуку", // Placeholder
                                color = Color.Gray, // Сірий колір тексту
                                fontSize = 16.sp
                            )
                        }
                        BasicTextField(
                            value = searchQuery.value,
                            onValueChange = { query ->
                                searchQuery.value = query
                                filteredList.value = search(query, rateList.value)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = Input
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp)) // Відступ між полями
                // Іконка пошуку
                Column(
                    modifier = Modifier.weight(0.1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        painter = AppIcons.Search(),
                        contentDescription = "Пошук",
                        tint = Color.Black,
                        modifier = Modifier.size(27.dp)
                    )
                }
            }
        }
        Column(
            modifier = Modifier.verticalScroll(rememberScrollState())
                .weight(1f)
        ) {
            Spacer(modifier = Modifier.height(75.dp))
            // Основний текст
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Рейтинговий список",
                    style = Title,
                    modifier = Modifier
                        .width(262.dp)
                )
            }
            Spacer(modifier = Modifier.height(80.dp))
            // Відображаємо таблицю для кожного елемента
            majorityInfo.value?.let { CustomTableInfo(it) }
            Spacer(modifier = Modifier.height(20.dp))

            // Відображаємо таблицю для кожного елемента
            if (filteredList.value.isNotEmpty()) {
                filteredList.value.take(1000).forEach { data -> // Обмеження виведення абітурієнтів до 1000 осіб
                    CustomTable(data)
                    Spacer(modifier = Modifier.height(7.dp))
                }
            } else {
                Text("Завантаження даних, зачекайте будь ласка.. \n\nМожливо ці дані відсутні. Спробуйте іншу спеціальність.", modifier = Modifier.padding(16.dp), color = Color.Black)
            }
            Spacer(modifier = Modifier.height(80.dp)) // Зсунення елементів вниз
        }
    }
    Footer(goBack, onIconClick = { isDialogVisible = !isDialogVisible })

    // Вікно діалогу
    if (isDialogVisible) {
        UserMenu(
            onDismiss = { isDialogVisible = false },
            fun1 = {
                if (isLoggedIn) {
                    // Перехід до історії
                    goToHistory()
                } else {
                    // Перехід до реєстрації
                    goToRegistr()
                }
            },
            fun2 = {
                if (isLoggedIn) {
                    // Розлогінення
                    coroutineScope.launch {
                        val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                        with(sharedPreferences.edit()) {
                            remove("username")
                            remove("password")
                            apply()
                        }

                        isDialogVisible = false
                        clearCurrentUser(context) // Виклик нової функції
                        loginViewModel.logOut()
                    }
                } else {
                    // Перехід до авторизації
                    goToLogin()
                }
            },
            isLoggedIn
        )
    }
}

fun search(query: String, list: List<RateData>): List<RateData> {
    if (query.isEmpty()) return list
    return list.filter { rateData ->
        rateData.number.contains(query, ignoreCase = true) ||
                rateData.priority.contains(query, ignoreCase = true) ||
                rateData.name.contains(query, ignoreCase = true) ||
                rateData.status.contains(query, ignoreCase = true) ||
                rateData.consistOfSubjects.any { it.contains(query, ignoreCase = true) } ||
                rateData.consistOfPoint.any { it.contains(query, ignoreCase = true) } ||
                rateData.point.contains(query, ignoreCase = true) ||
                rateData.kvota.contains(query, ignoreCase = true) ||
                rateData.doc.contains(query, ignoreCase = true)
    }
}

@Composable
fun CustomTableInfo(majorityInfo: MajorityInfo) {
    Column(
        modifier = Modifier
            .padding(start = 15.dp, end = 15.dp)
            .background(LightYellow, shape = RoundedCornerShape(10.dp))
            .border(1.dp, color = Grey, shape = RoundedCornerShape(10.dp))
    ) {
        // Верхній рядок з великим текстом
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max),

            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = majorityInfo.title,
                color = Color.Black,
                fontSize = 18.sp,
                modifier = Modifier.weight(1f).padding(10.dp),
                textAlign = TextAlign.Center,
            )
        }
        HorizontalDivider(thickness = 1.dp, color = Grey)
        // Верхній рядок з великим текстом
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max),

            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = majorityInfo.majorityDetails,
                color = Color.Black,
                fontSize = 16.sp,
                modifier = Modifier.weight(1f).padding(5.dp),
                textAlign = TextAlign.Center,
            )
        }
        HorizontalDivider(thickness = 1.dp, color = Grey)
        // Верхній рядок з великим текстом
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max),

            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = majorityInfo.subTitle,
                color = Color.Black,
                fontSize = 16.sp,
                modifier = Modifier.weight(1f).padding(5.dp),
                textAlign = TextAlign.Center,
            )
        }
        HorizontalDivider(thickness = 1.dp, color = Grey)
        // Третій рядок з предметами
        Row (
            modifier = Modifier.fillMaxWidth()
                .height(IntrinsicSize.Max),
            horizontalArrangement = Arrangement.Center
        ) {
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = majorityInfo.seatsInfo,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    color = Color.Black,
                    modifier = Modifier.weight(1f).padding(5.dp),
                )
                Text(
                    text = majorityInfo.applicationInfo,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    color = Color.Black,
                    modifier = Modifier.weight(1f).padding(5.dp),
                )
                if (majorityInfo.passingScore.isNotEmpty()) {
                    Text(
                        text = "Прохідний бал на бюджет ${majorityInfo.passingScore}",
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        color = Color.Black,
                        modifier = Modifier
                            .weight(1f)
                            .padding(5.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CustomTable(rateData: RateData) {
    Column(
        modifier = Modifier
            .padding(start = 15.dp, end = 15.dp)
            .background(LightYellow, shape = RoundedCornerShape(10.dp))
            .border(1.dp, color = Grey, shape = RoundedCornerShape(10.dp))
    ) {
        // Верхній рядок з великим текстом
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max),

            horizontalArrangement = Arrangement.Center
        ) {
            Column(
                modifier = Modifier.weight(0.8f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "№",
                    color = Color.Black,
                    fontSize = 16.sp,
                    modifier = Modifier.weight(1f).padding(5.dp),
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = rateData.number,
                    color = Color.Black,
                    fontSize = 16.sp,
                    modifier = Modifier.weight(1f).padding(5.dp),
                    textAlign = TextAlign.Center,
                )
            }
            VerticalDivider(thickness = 1.dp, color = Grey)
            Column(
                modifier = Modifier.weight(0.5f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "П",
                    color = Color.Black,
                    fontSize = 16.sp,
                    modifier = Modifier.weight(1f).padding(5.dp),
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = rateData.priority,
                    color = Color.Black,
                    fontSize = 16.sp,
                    modifier = Modifier.weight(1f).padding(5.dp),
                    textAlign = TextAlign.Center,
                )
            }
            VerticalDivider(thickness = 1.dp, color = Grey)
            Column(
                modifier = Modifier.weight(1.67f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = rateData.name,
                    color = Color.Black,
                    fontSize = 16.sp,
                    modifier = Modifier.weight(1f).padding(5.dp),
                    textAlign = TextAlign.Center,
                )
            }
            VerticalDivider(thickness = 1.dp, color = Grey)
            Column(
                modifier = Modifier.weight(2f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = rateData.status,
                    color = Color.Black,
                    fontSize = 16.sp,
                    modifier = Modifier.weight(1f).padding(5.dp),
                    textAlign = TextAlign.Center,
                )
            }
        }
        HorizontalDivider(thickness = 1.dp, color = Grey)
        // Другий рядок
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(modifier = Modifier.weight(2.3f).padding(5.dp)) {
                rateData.consistOfSubjects.forEach { subject ->
                    Text(
                        text = subject,
                        fontSize = 14.sp,
                        color = Color.Black,
                    )
                }
            }
            Column(modifier = Modifier.weight(0.7f).padding(5.dp)) {
                rateData.consistOfPoint.forEach { point ->
                    Text(
                        text = point,
                        fontSize = 14.sp,
                        color = Color.Black,
                    )
                }
            }
            VerticalDivider(thickness = 1.dp, color = Grey)
            Column(modifier = Modifier.weight(2f)) {
                Row (
                    modifier = Modifier.fillMaxWidth()
                        .height(IntrinsicSize.Max),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Σ",
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        color = Color.Black,
                        modifier = Modifier.weight(0.5f).padding(5.dp, top=7.dp, bottom = 7.dp),
                    )
                    Text(
                        text = rateData.point,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        color = Color.Black,
                        modifier = Modifier.weight(1.5f).padding(5.dp, top=7.dp, bottom = 7.dp),
                    )
                }
                HorizontalDivider(thickness = 1.dp, color = Grey)
                Row (
                    modifier = Modifier.fillMaxWidth()
                        .height(IntrinsicSize.Max),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Квота",
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            color = Color.Black,
                            modifier = Modifier.weight(1f).padding(5.dp),
                        )
                        Text(
                            text = rateData.kvota,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            color = Color.Black,
                            modifier = Modifier.weight(1f).padding(5.dp),
                        )
                    }
                    VerticalDivider(thickness = 1.dp, color = Grey)
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "ПВМ/ВЗ",
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            color = Color.Black,
                            modifier = Modifier.weight(1f).padding(5.dp),
                        )
                        Text(
                            text = rateData.doc,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            color = Color.Black,
                            modifier = Modifier.weight(1f).padding(5.dp),
                        )
                    }
                }
            }
        }
    }
}

