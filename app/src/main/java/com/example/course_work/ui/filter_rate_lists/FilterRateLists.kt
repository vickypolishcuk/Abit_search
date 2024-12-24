package com.example.course_work.ui.filter_rate_lists

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.example.course_work.ui.icons.AppIcons
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.example.course_work.functions.LoginViewModel
import com.example.course_work.functions.clearCurrentUser
import com.example.course_work.functions.getUniversitiesByRegion
import com.example.course_work.models.Regions
import com.example.course_work.models.UniversityData
import com.example.course_work.ui.fixed_element.Footer
import com.example.course_work.ui.fixed_element.UserMenu
import com.example.course_work.ui.theme.Input
import com.example.course_work.ui.theme.Title
import kotlinx.coroutines.launch


@Composable
fun FilterRateLists(
    loginViewModel: LoginViewModel,
    goBack: () -> Unit,
    goToLogin: () -> Unit,
    goToRegistr: () -> Unit,
    goToHistory: () -> Unit,
    goToMajority: (universityHref: String, regionHref: String) -> Unit,
    regionsList: List<Regions>,
) {
    val isLoggedIn by loginViewModel.isLoggedIn.collectAsState()
    var isDialogVisible by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // Списки опцій
    val yearOptions = listOf("2024", "2023", "2022", "2021", "2020", "2019", "2018")//, "2017", "2016", "2015", "2014")
    val listOfRegions = regionsList

    // Стан для кожного вибору
    var selectedYear by remember { mutableStateOf(yearOptions.first()) }
    var selectedRegion by remember { mutableStateOf(listOfRegions.first().name) }
    var selectedInstitution by remember { mutableStateOf("") }
    var newUniversityList by remember { mutableStateOf(emptyList<UniversityData>()) }
    var selectedUniversity by remember { mutableStateOf<UniversityData?>(null) }
    var isLoadedUniversities by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        newUniversityList = getUniversitiesByRegion(regionsList.find { it.name == selectedRegion }!!.href, selectedYear)
        selectedUniversity = newUniversityList.firstOrNull()!!
        isLoadedUniversities = true
        selectedInstitution = selectedUniversity!!.name
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightYellow) // Світло-жовтий фон
    ) {
        // Прокручувана частина між хедером і футером
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
                    text = "Фільтр",
                    style = Title,
                    modifier = Modifier
                        .width(262.dp)
                )
            }
            Spacer(modifier = Modifier.height(80.dp))
            // Опції вибору для фільтрації даних
            Box(
                modifier = Modifier
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center // Центрування вмісту
            ) {
                SelectWithLabel(
                    label = "Рік",
                    options = yearOptions,
                    selectedOption = selectedYear,
                    onOptionSelected = { year ->
                        selectedYear = year
                        coroutineScope.launch {
                            newUniversityList = getUniversitiesByRegion(
                                regionsList.find { it.name == selectedRegion }!!.href,
                                selectedYear
                            )
                            selectedUniversity = newUniversityList.firstOrNull()!!
                            selectedInstitution = selectedUniversity!!.name
                        }
                    }
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center // Центрування вмісту
            ) {
                SelectWithLabel(
                    label = "Регіон",
                    options = listOfRegions.map { it.name },
                    selectedOption = selectedRegion,
                    onOptionSelected = { region ->
                        selectedRegion = region
                        coroutineScope.launch {
                            newUniversityList = getUniversitiesByRegion(
                                regionsList.find { it.name == selectedRegion }!!.href,
                                selectedYear
                            )
                            selectedUniversity = newUniversityList.firstOrNull()!!
                            selectedInstitution = selectedUniversity!!.name
                        }
                    }
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            if (isLoadedUniversities) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center // Центрування вмісту
                ) {
                    SelectWithLabel(
                        label = "Заклад",
                        options = newUniversityList.map { it.name },
                        selectedOption = selectedInstitution,
                        onOptionSelected = { institution ->
                            val university = newUniversityList.find { it.name == institution }
                            university?.let {
                                selectedUniversity = it
                            }
                            selectedInstitution = institution
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(30.dp)) // Відступ
            if (isLoadedUniversities) {
                Button(
                    onClick = {
                        val parameter = selectedUniversity!!.href.replace("2024", selectedYear)
                        val parameter2 =
                            regionsList.find { it.name == selectedRegion }!!.href.replace(
                                "2024",
                                selectedYear
                            )
                        println("parameter=${parameter}, parameter2=${parameter2}")
                        goToMajority(parameter, parameter2)
                    },
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .width(150.dp)
                        .height(50.dp)
                        .fillMaxWidth(0.8f)
                        .background(LightBlue, shape = RoundedCornerShape(10.dp)),
                    colors = ButtonDefaults.buttonColors(containerColor = LightBlue)
                ) {
                    Text(
                        text = "Пошук",
                        color = Color.Black,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            Spacer(modifier = Modifier.height(80.dp)) // Відступ від кінця сторінки
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
                        clearCurrentUser(context) // Виклик функції очищення поточного користувача
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

// Для ExposedDropdownMenuBox та ще 2 - пояснюємо, що це експерементальні інструменти
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectWithLabel(label: String, options: List<String>, selectedOption: String,
                    onOptionSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    var currentOption by remember { mutableStateOf(options.first()) }

    LaunchedEffect(selectedOption) {
        currentOption = selectedOption
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = Input,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(10.dp))
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth(0.85f)
                .align(Alignment.CenterHorizontally)
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
                    unfocusedContainerColor = Grey, // Колір фону без фокуса
                    focusedContainerColor = Grey, // Колір фону у фокусі
                    unfocusedTextColor = Color.Black, // Колір тексту без фокуса
                    focusedTextColor = Color.Black // Колір тексту у фокусі
                ),
                modifier = Modifier.menuAnchor()
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally)
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                scrollState = scrollState,
                modifier = Modifier.fillMaxWidth()
                    .background(Grey, shape = RoundedCornerShape(10.dp)),
            ) {
                options.withIndex().forEach { (index, option) ->

                    DropdownMenuItem(
                        modifier = Modifier.fillMaxWidth(),
                        text = {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.CenterHorizontally), // Центрує текст у межах елемента
                                contentAlignment = Alignment.Center // Центрує вміст Box
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
                            onOptionSelected(option)
                            expanded = false
                        }
                    )
                    // Додати роздільник, якщо це не останній елемент
                    if (index < options.size - 1) {
                        HorizontalDivider(thickness = 1.dp, color = Color.Gray)
                    }
                }
            }
            LaunchedEffect(expanded) {
                if (expanded) {
                    scrollState.scrollTo(scrollState.maxValue)
                }
            }
        }
    }
}

