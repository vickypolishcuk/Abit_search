package com.example.course_work.ui.majority_page

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
import androidx.compose.foundation.clickable
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
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.course_work.functions.LoginViewModel
import com.example.course_work.functions.clearCurrentUser
import com.example.course_work.functions.dataStore
import com.example.course_work.functions.getUniversitiesInfo
import com.example.course_work.functions.parseMajority
import com.example.course_work.models.Majority
import com.example.course_work.models.University
import com.example.course_work.ui.fixed_element.Footer
import com.example.course_work.ui.icons.AppIcons
import com.example.course_work.ui.fixed_element.UserMenu
import com.example.course_work.ui.theme.Input
import com.example.course_work.ui.theme.Title
import kotlinx.coroutines.launch


@Composable
fun MajorityPage(
    loginViewModel: LoginViewModel,
    goBack: () -> Unit,
    goToLogin: () -> Unit,
    goToRegistr: () -> Unit,
    goToHistory: () -> Unit,
    goToRateList: (param :String) -> Unit,
    universityHref: String,
    regionHref: String,
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
        val majorityList = remember { mutableStateOf<List<Majority>>(emptyList()) }
        val searchQuery = remember { mutableStateOf("") }
        val filteredMajorityList = remember { mutableStateOf<List<Majority>>(emptyList()) }
        val mutuniversityInfo = remember { mutableStateOf<University?>(null) }

        // Завантаження даних у фоновому режимі
        LaunchedEffect(Unit) {
            try {
                majorityList.value = parseMajority(universityHref)
                filteredMajorityList.value = majorityList.value
                mutuniversityInfo.value = getUniversitiesInfo(regionHref, universityHref)
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
                                filteredMajorityList.value = search(query, majorityList.value)
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
                    text = "Спеціальності",
                    style = Title,
                    modifier = Modifier
                        .width(262.dp)
                )
            }
            // Поле вводу
            Spacer(modifier = Modifier.height(80.dp))

            // Виведення інформації про університет
            mutuniversityInfo.value?.let { CustomTableInfo(it) }
            Spacer(modifier = Modifier.height(20.dp))

            // Відображаємо таблицю для кожного елемента
            if (filteredMajorityList.value.isNotEmpty()) {
                filteredMajorityList.value.forEach { data ->
                    if (data.href.isNotEmpty()) { // Перевірка, чи не порожній href
                        CustomTable({ goToRateList(data.href) }, data)
                        Spacer(modifier = Modifier.height(7.dp))
                    }
                }
            } else {
                Text("Завантаження даних, зачекайте будь ласка..", modifier = Modifier.padding(16.dp), color = Color.Black)
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
                        context.dataStore.edit { preferences ->
                            preferences.remove(stringPreferencesKey("username"))
                            preferences.remove(stringPreferencesKey("password"))
                        }
                        isDialogVisible = false
                        clearCurrentUser(context)
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

fun search(query: String, list: List<Majority>): List<Majority> {
    if (query.isEmpty()) return list
    return list.filter { majorityData ->
        majorityData.name.contains(query, ignoreCase = true) ||
                majorityData.institution.contains(query, ignoreCase = true) ||
                majorityData.description.contains(query, ignoreCase = true) ||
                majorityData.okr.contains(query, ignoreCase = true) ||
                majorityData.freePlace.contains(query, ignoreCase = true) ||
                majorityData.totalPlace.contains(query, ignoreCase = true) ||
                majorityData.numbers.contains(query, ignoreCase = true) ||
                majorityData.href.contains(query, ignoreCase = true)
    }
}

@Composable
fun CustomTableInfo(universityInfo: University) {
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
                text = universityInfo.name,
                color = Color.Black,
                fontSize = 18.sp,
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
                    text = "БМ",
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    color = Color.Black,
                    modifier = Modifier.weight(1f).padding(5.dp),
                )
                Text(
                    text = universityInfo.freePlace,
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
                    text = "ВМ",
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    color = Color.Black,
                    modifier = Modifier.weight(1f).padding(5.dp),
                )
                Text(
                    text = universityInfo.totalPlace,
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
                    text = "Заяви",
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    color = Color.Black,
                    modifier = Modifier.weight(1f).padding(5.dp),
                )
                Text(
                    text = universityInfo.countOfClaims,
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
                    text = "Оригінали",
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    color = Color.Black,
                    modifier = Modifier.weight(1f).padding(5.dp),
                )
                Text(
                    text = universityInfo.doc,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    color = Color.Black,
                    modifier = Modifier.weight(1f).padding(5.dp),
                )
            }
        }
    }
}

@Composable
fun CustomTable(goToRateList: (param: String) -> Unit, majorityData: Majority) {
    Column(
        modifier = Modifier
            .padding(start = 15.dp, end = 15.dp)
            .background(LightYellow, shape = RoundedCornerShape(10.dp))
            .border(1.dp, color = Grey, shape = RoundedCornerShape(10.dp))
            .clickable { goToRateList(majorityData.href) }
    ) {
        // Верхній рядок з великим текстом
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max),

            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = majorityData.name,
                color = Color.Black,
                fontSize = 16.sp,
                modifier = Modifier.weight(1f).padding(5.dp),
                textAlign = TextAlign.Center,
            )
        }
        HorizontalDivider(thickness = 1.dp, color = Grey)
        // Другий рядок
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Text(
                text = majorityData.institution,
                color = Color.Black,
                fontSize = 14.sp,
                modifier = Modifier.weight(1f).padding(5.dp),
                textAlign = TextAlign.Center,
            )
        }
        HorizontalDivider(thickness = 1.dp, color = Grey)
        // Третій рядок з предметами
        Row(
            modifier = Modifier.fillMaxWidth()
                .height(IntrinsicSize.Max),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Row(modifier = Modifier.weight(0.6f)) {
                Text(
                    text = majorityData.description,
                    color = Color.Black,
                    fontSize = 12.sp,
                    modifier = Modifier.weight(1f).padding(5.dp),
                    textAlign = TextAlign.Center,
                )
            }
            VerticalDivider(thickness = 1.dp, color = Grey)
            Column(modifier = Modifier.weight(0.4f)) {
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
                            text = "ОКР",
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            color = Color.Black,
                            modifier = Modifier.weight(1f).padding(5.dp),
                        )
                        Text(
                            text = majorityData.okr,
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
                            text = "БМ",
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            color = Color.Black,
                            modifier = Modifier.weight(1f).padding(5.dp),
                        )
                        Text(
                            text = majorityData.freePlace,
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
                            text = "ВМ",
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            color = Color.Black,
                            modifier = Modifier.weight(1f).padding(5.dp),
                        )
                        Text(
                            text = majorityData.totalPlace,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            color = Color.Black,
                            modifier = Modifier.weight(1f).padding(5.dp),
                        )
                    }
                }
                HorizontalDivider(thickness = 1.dp, color = Grey)
                // Рядок з сумою
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Text(
                        text = "К",
                        fontSize = 16.sp,
                        modifier = Modifier.weight(0.5f).padding(5.dp),
                        textAlign = TextAlign.Center,
                        color = Color.Black,
                    )
                    Text(
                        text = majorityData.numbers,
                        fontSize = 16.sp,
                        modifier = Modifier.weight(1.5f).padding(5.dp),
                        textAlign = TextAlign.Center,
                        color = Color.Black,
                    )
                }
            }
        }
    }
}

