package com.example.course_work.ui.search_page

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.course_work.ui.theme.LightBlue
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.example.course_work.models.Search
import com.example.course_work.ui.fixed_element.Footer
import com.example.course_work.ui.icons.AppIcons
import com.example.course_work.ui.fixed_element.UserMenu
import com.example.course_work.ui.theme.Input
import com.example.course_work.ui.theme.TextOnBotton
import com.example.course_work.ui.theme.Title
import kotlinx.coroutines.launch
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.course_work.functions.LoginViewModel
import com.example.course_work.functions.SearchFunViewModel
import com.example.course_work.functions.addHistoryToUser
import com.example.course_work.functions.clearCurrentUser
import com.example.course_work.functions.dataStore
import com.example.course_work.functions.getCurrentUser
import com.example.course_work.models.History
import com.example.course_work.models.Regions
import com.example.course_work.ui.fixed_element.showErrorDialog
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchPage(
    loginViewModel: LoginViewModel = viewModel(),
    searchFunViewModel: SearchFunViewModel,
    goToFilterRateLists: () -> Unit,
    goToLogin: () -> Unit,
    goToRegistr: () -> Unit,
    goToHistory: () -> Unit,
    goToRateList: (parameter: String) -> Unit,
    regionsList: List<Regions>,
    searchQuery: List<Search>? = null
) {
    val isLoggedIn by loginViewModel.isLoggedIn.collectAsState()
    var isDialogVisible by remember { mutableStateOf(false) }
    val searchResults by searchFunViewModel.searchResults.collectAsState() // Запис даних у пам'ять
    val coroutineScope = rememberCoroutineScope()
    var yearExpanded by remember { mutableStateOf(false) } // Стан для відкриття/закриття меню
    var regionExpanded by remember { mutableStateOf(false) } // Стан для відкриття/закриття меню
    var isExpanded by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState() // Стейт для прокрутки меню
    val context = LocalContext.current
    val showDialog = remember { mutableStateOf(false) }
    val showWaitingMessage = remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    var name by remember { mutableStateOf("") } // Стан тексту
    var surname by remember { mutableStateOf("") } // Стан тексту
    var patronymic by remember { mutableStateOf("") } // Стан тексту
    var point by remember { mutableStateOf("") } // Стан тексту

    val yearOptions = listOf("2024", "2023", "2022", "2021", "2020", "2019", "2018")
    var currentOption by remember { mutableStateOf("2024") } // Поточний вибір (рік)
    var selectedRegion by remember { mutableStateOf(regionsList.first().name) }

    LaunchedEffect(searchQuery) {
        if (!searchQuery.isNullOrEmpty()) {
            searchFunViewModel.setSearchResults(searchQuery)
        }
    }

    fun isValidText(input: String): Boolean {
        return input.isNotBlank() &&                // Не порожній
                !input.all { it.isDigit() } &&      // Не всі символи числа
                input.firstOrNull()?.isLetter() != false // Не починається з літери
    }
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
                    text = "Пошук абітурієнтів",
                    style = Title,
                    modifier = Modifier
                        .width(262.dp)
                )
            }
            // Поле вводу
            Spacer(modifier = Modifier.height(120.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .align(Alignment.CenterHorizontally)
            ) {
                Column {
                    // Лейбл над полем вводу
                    Text(
                        text = "Введіть прізвище абітурієнта повністю:", // Лейбл
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            color = Color.Black
                        ),
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally) // Вирівнювання лейбла
                            .padding(bottom = 4.dp) // Простір між лейблом і полем
                    )

                    // Поле вводу
                    Box(
                        modifier = Modifier
                            .height(60.dp)
                            .background(Grey, shape = RoundedCornerShape(10.dp))
                            .align(Alignment.CenterHorizontally)
                            .padding(10.dp)
                    ) {
                        BasicTextField(
                            value = surname,
                            onValueChange = {
                                surname = it
                            },
                            modifier = Modifier
                                .fillMaxSize(),
                            textStyle = Input
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .align(Alignment.CenterHorizontally)
            ) {
                Column {
                    // Лейбл над полем вводу
                    Text(
                        text = "Введіть ім'я абітурієнта або лише першу букву:", // Лейбл
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            color = Color.Black
                        ),
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally) // Вирівнювання лейбла
                            .padding(bottom = 4.dp) // Простір між лейблом і полем
                    )

                    // Поле вводу
                    Box(
                        modifier = Modifier
                            .height(60.dp)
                            .background(Grey, shape = RoundedCornerShape(10.dp))
                            .align(Alignment.CenterHorizontally)
                            .padding(10.dp)
                    ) {
                        BasicTextField(
                            value = name,
                            onValueChange = {
                                name = it
                            },
                            modifier = Modifier
                                .fillMaxSize(),
                            textStyle = Input
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .align(Alignment.CenterHorizontally)
            ) {
                Column {
                    // Лейбл над полем вводу
                    Text(
                        text = "Введіть по-батькові абітурієнта або лише першу букву:", // Лейбл
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            color = Color.Black
                        ),
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally) // Вирівнювання лейбла
                            .padding(bottom = 4.dp) // Простір між лейблом і полем
                    )

                    // Поле вводу
                    Box(
                        modifier = Modifier
                            .height(60.dp)
                            .background(Grey, shape = RoundedCornerShape(10.dp))
                            .align(Alignment.CenterHorizontally)
                            .padding(10.dp)
                    ) {
                        BasicTextField(
                            value = patronymic,
                            onValueChange = {
                                patronymic = it
                            },
                            modifier = Modifier
                                .fillMaxSize(),
                            textStyle = Input
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .align(Alignment.CenterHorizontally)
            ) {
                Column {
                    // Лейбл над полем вводу
                    Text(
                        text = "Виберіть рік вступу (необов'язково):",
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            color = Color.Black
                        ),
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally) // Вирівнювання по центру
                            .padding(bottom = 4.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    // Поле вводу
                    Box(
                        modifier = Modifier
                            .height(60.dp)
                            .background(Grey, shape = RoundedCornerShape(10.dp))
                            .align(Alignment.CenterHorizontally)
                    ) {
                        ExposedDropdownMenuBox(
                            expanded = yearExpanded,
                            onExpandedChange = { yearExpanded = !yearExpanded },
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Grey, shape = RoundedCornerShape(10.dp)),
                        ) {
                            TextField(
                                value = currentOption,
                                onValueChange = {},
                                textStyle = TextStyle(
                                    fontSize = 20.sp,
                                    color = Color.Black,
                                    fontWeight = FontWeight.Medium,
                                    textAlign = TextAlign.Center
                                ),
                                readOnly = true,
                                trailingIcon = {
                                    Icon(
                                        painter = AppIcons.ArrowDown(),
                                        contentDescription = "Drop down",
                                        tint = Color.Black,
                                        modifier = Modifier.size(27.dp)
                                    )
                                },
                                colors = TextFieldDefaults.colors(
                                    unfocusedContainerColor = Grey,
                                    focusedContainerColor = Grey,
                                    unfocusedTextColor = Color.Black,
                                    focusedTextColor = Color.Black
                                ),
                                modifier = Modifier.menuAnchor()
                                    .fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = yearExpanded,
                                onDismissRequest = { yearExpanded = false },
                                scrollState = scrollState,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Grey, shape = RoundedCornerShape(10.dp)),
                            ) {
                                yearOptions.withIndex().forEach { (index, option) ->
                                    DropdownMenuItem(
                                        modifier = Modifier.fillMaxWidth(),
                                        text = {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .align(Alignment.CenterHorizontally),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = option,
                                                    textAlign = TextAlign.Center,
                                                    style = Input
                                                )
                                            }
                                        },
                                        onClick = {
                                            currentOption = option
                                            yearExpanded = false
                                        }
                                    )
                                    if (index < yearOptions.size - 1) {
                                        HorizontalDivider(thickness = 1.dp, color = Color.Gray)
                                    }
                                }
                            }
                            LaunchedEffect(yearExpanded) {
                                if (yearExpanded) {
                                    scrollState.scrollTo(scrollState.maxValue)
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .align(Alignment.CenterHorizontally)
            ) {
                Column {
                    // Лейбл над полем вводу
                    Text(
                        text = "Виберіть регіон вступу (необов'язково):",
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            color = Color.Black
                        ),
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally) // Вирівнювання по центру
                            .padding(bottom = 4.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    // Поле вводу
                    Box(
                        modifier = Modifier
                            .height(60.dp)
                            .background(Grey, shape = RoundedCornerShape(10.dp))
                            .align(Alignment.CenterHorizontally)
                    ) {
                        ExposedDropdownMenuBox(
                            expanded = regionExpanded,
                            onExpandedChange = { regionExpanded = !regionExpanded },
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Grey, shape = RoundedCornerShape(10.dp)),
                        ) {
                            TextField(
                                value = selectedRegion,
                                onValueChange = {},
                                textStyle = TextStyle(
                                    fontSize = 20.sp,
                                    color = Color.Black,
                                    fontWeight = FontWeight.Medium,
                                    textAlign = TextAlign.Center
                                ),
                                readOnly = true,
                                trailingIcon = {
                                    Icon(
                                        painter = AppIcons.ArrowDown(),
                                        contentDescription = "Drop down",
                                        tint = Color.Black,
                                        modifier = Modifier.size(27.dp)
                                    )
                                },
                                colors = TextFieldDefaults.colors(
                                    unfocusedContainerColor = Grey,
                                    focusedContainerColor = Grey,
                                    unfocusedTextColor = Color.Black,
                                    focusedTextColor = Color.Black
                                ),
                                modifier = Modifier.menuAnchor()
                                    .fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = regionExpanded,
                                onDismissRequest = { regionExpanded = false },
                                scrollState = scrollState,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Grey, shape = RoundedCornerShape(10.dp)),
                            ) {
                                regionsList.withIndex().forEach { (index, option) ->
                                    DropdownMenuItem(
                                        modifier = Modifier.fillMaxWidth(),
                                        text = {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .align(Alignment.CenterHorizontally),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = option.name,
                                                    textAlign = TextAlign.Center,
                                                    style = Input
                                                )
                                            }
                                        },
                                        onClick = {
                                            selectedRegion = option.name
                                            regionExpanded = false
                                        }
                                    )
                                    if (index < regionsList.size - 1) {
                                        HorizontalDivider(thickness = 1.dp, color = Color.Gray)
                                    }
                                }
                            }
                            LaunchedEffect(regionExpanded) {
                                if (regionExpanded) {
                                    scrollState.scrollTo(scrollState.maxValue)
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .align(Alignment.CenterHorizontally)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Лейбл над полем вводу
                        Text(
                            text = "Введіть середній бал документа про освіту (необов'язково):",
                            style = TextStyle(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center,
                                color = Color.Black
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        // Кнопка для розгортання/згортання
                        Box(
                            modifier = Modifier
                                .size(40.dp) // Розмір кнопки
                                .background(Color.Gray.copy(alpha = 0.2f), shape = RoundedCornerShape(10.dp)) // Сірий фон з прозорістю
                                .clickable(onClick = {isExpanded = !isExpanded}), // Обробка кліку
                            contentAlignment = Alignment.Center // Центрування крапок у середині
                        ) {
                            Text(
                                text = "...",
                                style = TextStyle(
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Gray // Сірий колір для крапок
                                )
                            )
                        }
                    }
                    // Додатковий текст, якщо розгорнуто
                    if (isExpanded) {
                        Text(
                            text = "З 2022 року неактуально, 2021 рік - в 200-бальній шкалі, до 2021 року - в 12-бальній шкалі.",
                            style = TextStyle(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Normal,
                                textAlign = TextAlign.Center,
                                color = Color.Gray
                            ),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    // Поле вводу
                    Box(
                        modifier = Modifier
                            .height(60.dp)
                            .background(Grey, shape = RoundedCornerShape(10.dp))
                            .align(Alignment.CenterHorizontally)
                            .padding(10.dp)
                    ) {
                        BasicTextField(
                            value = point,
                            onValueChange = {
                                point = it
                            },
                            modifier = Modifier
                                .fillMaxSize(),
                            textStyle = Input
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))

            // Кнопка "Пошук"
            Spacer(modifier = Modifier.height(30.dp)) // Відступ
            Button(
                onClick = {
                    coroutineScope.launch {
                        showWaitingMessage.value = true
                        if (isValidText(surname) && isValidText(name) && isValidText(patronymic)) {
                            surname = surname.split(" ").firstOrNull().orEmpty()
                            val pib =
                                "${surname.lowercase().replaceFirstChar {
                                    if (it.isLowerCase()) it.titlecase(
                                        Locale.getDefault()
                                    ) else it.toString()
                                }} ${name[0].uppercaseChar()}. ${patronymic[0].uppercaseChar()}."
                            println("pib=${pib}")
                            point = when {
                                currentOption >= "2021" -> {
                                    point.toIntOrNull()?.let {
                                        point
                                    } ?: ""
                                }
                                currentOption < "2021" -> {
                                    point.toDoubleOrNull()?.let {
                                        point
                                    } ?: ""
                                }
                                else -> ""
                            }
                            searchFunViewModel.search(pib, currentOption, regionsList.find { it.name == selectedRegion }!!.href)
                            showWaitingMessage.value = false
                            if (isLoggedIn) {
                                val currentUser = getCurrentUser(context)!!
                                println("currentUser=${currentUser}")
                                val currentDate = SimpleDateFormat(
                                    "dd.MM.yyyy",
                                    Locale.getDefault()
                                ).format(Date())
                                val query = History(pib, currentOption, selectedRegion, point, currentDate, searchResults)
                                addHistoryToUser(context, currentUser, query)
                            }
                        } else {
                            showDialog.value = true
                            errorMessage = "Прізвище, ім'я та по-батькові не повинні бути порожніми," +
                                    " починатися з цифри та повністю складатися з чисел"
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
                    text = "Пошук",
                    style = TextOnBotton
                )
            }
            Spacer(modifier = Modifier.height(40.dp))

            if (showWaitingMessage.value) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Обробка даних, зачекайте будь ласка",
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal,
                            textAlign = TextAlign.Center,
                        ),
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            if (searchResults.isNotEmpty()) {
                if (point != "") {
                    val filteredResults = searchResults.filter { search ->
                        // Порівнюємо середній бал документа (search.sbo) з введеним значенням (point)
                        search.sbo == point // Змінити порівняння за потребою
                    }
                    if (filteredResults.isNotEmpty()) {
                        // Проходимо по кожному елементу в списку і відображаємо CustomTable
                        filteredResults.forEach { search ->
                            CustomTable(search) { goToRateList(search.hrefToRateList) }
                            Spacer(modifier = Modifier.height(7.dp))
                        }
                    }
                } else {
                    searchResults.forEach { search ->
                        CustomTable(search) { goToRateList(search.hrefToRateList) }
                        Spacer(modifier = Modifier.height(7.dp))
                    }
                }
            }
            showErrorDialog(showDialog, errorMessage)
            Spacer(modifier = Modifier.height(80.dp)) // Зсунення елементів вниз
        }
    }
    Footer(goToFilterRateLists, onIconClick = { isDialogVisible = !isDialogVisible }, true)

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

@Composable
fun CustomTable(searchResult: Search, goToRateList: () -> Unit) {
    Column(
        modifier = Modifier
            .padding(16.dp)
            .background(LightYellow, shape = RoundedCornerShape(10.dp))
            .border(1.dp, color = Grey, shape = RoundedCornerShape(10.dp))
            .clickable { goToRateList() }
    ) {
        // Верхній рядок з великим текстом
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max),

            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = searchResult.majority,
                fontSize = 14.sp,
                modifier = Modifier.weight(3f).padding(5.dp),
                textAlign = TextAlign.Center,
                color = Color.Black,
            )
        }
        HorizontalDivider(thickness = 1.dp, color = Grey)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max),

            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = searchResult.university,
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
                text = searchResult.kvota,
                fontSize = 14.sp,
                modifier = Modifier.weight(0.62f).padding(5.dp),
                textAlign = TextAlign.Center,
                color = Color.Black,
            )
            VerticalDivider(thickness = 1.dp, color = Grey)
            Text(
                text = searchResult.doc,
                fontSize = 14.sp,
                modifier = Modifier.weight(0.62f).padding(5.dp),
                textAlign = TextAlign.Center,
                color = Color.Black,
            )
            VerticalDivider(thickness = 1.dp, color = Grey)
            Text(
                text = searchResult.status,
                fontSize = 14.sp,
                modifier = Modifier.weight(2.77f).padding(5.dp),
                textAlign = TextAlign.Center,
                color = Color.Black,
            )
            VerticalDivider(thickness = 1.dp, color = Grey)
            Text(
                text = searchResult.sbo,
                fontSize = 14.sp,
                modifier = Modifier.weight(1f).padding(5.dp),
                textAlign = TextAlign.Center,
                color = Color.Black,
            )
        }

        HorizontalDivider(thickness = 1.dp, color = Grey)

        // Третій рядок з предметами
        Row(
            modifier = Modifier.fillMaxWidth()
                .height(IntrinsicSize.Max),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Row(modifier = Modifier.weight(0.66f)) {
                Column(modifier = Modifier.weight(2.3f).padding(5.dp)) {
                    searchResult.consistOfSubjects.forEach { subject ->
                        Text(
                            text = subject,
                            fontSize = 14.sp,
                            color = Color.Black,
                        )
                    }
                }
                Column(modifier = Modifier.weight(0.7f).padding(5.dp)) {
                    searchResult.consistOfPoint.forEach { point ->
                        Text(
                            text = point,
                            fontSize = 14.sp,
                            color = Color.Black,
                        )
                    }
                }
            }
            VerticalDivider(thickness = 1.dp, color = Grey)
            Column(modifier = Modifier.weight(0.34f)) {
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
                            text = searchResult.okr,
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
                            text = "П",
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            color = Color.Black,
                            modifier = Modifier.weight(1f).padding(5.dp),
                        )
                        Text(
                            text = searchResult.priority,
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
                            text = "№",
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            color = Color.Black,
                            modifier = Modifier.weight(1f).padding(5.dp),
                        )
                        Text(
                            text = searchResult.numberOfPlace,
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
                        text = "Σ",
                        fontSize = 16.sp,
                        modifier = Modifier.weight(0.5f).padding(5.dp),
                        textAlign = TextAlign.Center,
                        color = Color.Black,
                    )
                    Text(
                        text = searchResult.point,
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

