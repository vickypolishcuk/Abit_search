package com.example.course_work

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.course_work.ui.theme.Course_workTheme
import com.example.course_work.ui.search_page.SearchPage
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.course_work.functions.LoginViewModel
import com.example.course_work.functions.SearchFunViewModel
import com.example.course_work.functions.getRegions
import com.example.course_work.models.Regions
import com.example.course_work.models.Search
import com.example.course_work.models.toJson
import com.example.course_work.ui.filter_rate_lists.FilterRateLists
import com.example.course_work.ui.history_page.HistoryPage
import com.example.course_work.ui.login_page.LoginPage
import com.example.course_work.ui.majority_page.MajorityPage
import com.example.course_work.ui.registr_page.RegistrPage
import com.example.course_work.ui.rate_list_page.RateListPage
import kotlinx.serialization.json.Json

import android.Manifest
import android.content.ContentValues.TAG
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging


enum class Routes {
    SearchPage,
    FilterRateListsPage,
    RegistrPage,
    LoginPage,
    HistoryPage,
    MajorityPage,
    RateListPage,
}

class MainActivity : ComponentActivity() {
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted: Boolean ->
        if (isGranted) {
            // FCM SDK (and your app) can post notifications.
        } else {
            // TODO: Inform user that that your app will not show notifications.
        }
    }
    private fun askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                // FCM SDK (and your app) can post notifications.
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                // TODO: display an educational UI explaining to the user the features that will be enabled
                //       by them granting the POST_NOTIFICATION permission. This UI should provide the user
                //       "OK" and "No thanks" buttons. If the user selects "OK," directly request the permission.
                //       If the user selects "No thanks," allow the user to continue without notifications.
            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun saveTokenToFirestore(token: String) {
        val db = FirebaseFirestore.getInstance()
        // Додаємо токен у Firestore в колекцію `notifications`
        val deviceData = hashMapOf(
            "fcmToken" to token,
            "deviceId" to Build.DEVICE, // інформація про пристрій
            "createdAt" to System.currentTimeMillis()
        )

        db.collection("notifications")
            .document(token) // Використовуємо токен як унікальний ідентифікатор
            .set(deviceData)
            .addOnSuccessListener {
                Log.d("Firestore", "Token successfully saved to Firestore")
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error writing token to Firestore", e)
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        askNotificationPermission()
        enableEdgeToEdge()
        setContent {
            FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                    return@OnCompleteListener
                }
                // Get new FCM registration token
                val token = task.result
                Log.d("FCM", "FCM Token: $token")
                // Зберігаємо токен для подальшого використання
                saveTokenToFirestore(token)
            })
            Course_workTheme {
                val loginViewModel: LoginViewModel = viewModel()
                val searchFunViewModel: SearchFunViewModel = viewModel()
                val navController = rememberNavController()
                val message = intent.getStringExtra("updated_data")
                println("message=${message}")
                message?.let {
                    Log.d("SearchPage", "Received message: $it")
                }
                val regions = remember { mutableStateOf<List<Regions>>(emptyList()) }
                val isDataLoaded = remember { mutableStateOf(false) }
                LaunchedEffect(Unit) {
                    try {
                        regions.value = getRegions()
                        isDataLoaded.value = true
                        message?.let {
                            val searchData = Json.decodeFromString<List<Search>>(it)
                            // Оновлюємо список пошуку, передавши дані до ViewModel або компонента
                            searchFunViewModel.setSearchResults(searchData)
                        }
                    } catch (e: Exception) {
                        println("Помилка при завантаженні даних: ${e.message}")
                    }
                }
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    if (isDataLoaded.value) {
                        NavHost(
                            navController = navController,
                            startDestination = Routes.SearchPage.name,
                            modifier = Modifier.padding(innerPadding)
                        ) {
                            composable(route = Routes.SearchPage.name + "?searchData={searchData}") {
                                val searchDataJson = navController.currentBackStackEntry?.arguments?.getString("searchData")
                                val searchData = searchDataJson?.let { Json.decodeFromString<List<Search>>(it) } ?: emptyList()
                                SearchPage(
                                    loginViewModel = loginViewModel,
                                    searchFunViewModel = searchFunViewModel,
                                    goToFilterRateLists = { navController.navigate(route = Routes.FilterRateListsPage.name) },
                                    goToLogin = { navController.navigate(route = Routes.LoginPage.name) },
                                    goToRegistr = { navController.navigate(route = Routes.RegistrPage.name) },
                                    goToHistory = { navController.navigate(route = Routes.HistoryPage.name) },
                                    goToRateList = { param ->
                                        navController.navigate(
                                            "${Routes.RateListPage.name}/${
                                                param
                                                    .replace("/", "_")
                                            }"
                                        )
                                    },
                                    regionsList = regions.value.toList(),
                                    searchQuery = searchData,
                                )
                            }
                            composable(
                                route = Routes.FilterRateListsPage.name,
                                arguments = listOf(
                                    navArgument("universitiesList") { defaultValue = "Default" },
                                )
                            )
                            {
                                FilterRateLists(
                                    loginViewModel = loginViewModel,
                                    goBack = { navController.navigate(route = Routes.SearchPage.name) },
                                    goToLogin = { navController.navigate(route = Routes.LoginPage.name) },
                                    goToRegistr = { navController.navigate(route = Routes.RegistrPage.name) },
                                    goToHistory = { navController.navigate(route = Routes.HistoryPage.name) },
                                    goToMajority = { parameter, parameter2 ->
                                        navController.navigate(
                                            "${Routes.MajorityPage.name}/" +
                                                    "${parameter.replace("/", "_")}/" +
                                                    parameter2.replace("/", "_")
                                        )
                                    },
                                    regionsList = regions.value.toList(),
                                )
                            }
                            composable(route = Routes.LoginPage.name) {
                                LoginPage(
                                    loginViewModel = loginViewModel,
                                    goBack = { navController.navigate(route = Routes.SearchPage.name) },
                                    goToLogin = { navController.navigate(route = Routes.LoginPage.name) },
                                    goToRegistr = { navController.navigate(route = Routes.RegistrPage.name) },
                                )
                            }
                            composable(route = Routes.RegistrPage.name) {
                                RegistrPage(
                                    goBack = { navController.navigate(route = Routes.SearchPage.name) },
                                    goToLogin = { navController.navigate(route = Routes.LoginPage.name) },
                                    goToRegistr = { navController.navigate(route = Routes.RegistrPage.name) },
                                )
                            }
                            composable(route = Routes.HistoryPage.name) {
                                HistoryPage(
                                    loginViewModel = loginViewModel,
                                    goBack = { navController.navigate(route = Routes.SearchPage.name) },
                                    goToHistory = { navController.navigate(route = Routes.HistoryPage.name) },
                                    goToSearchPage = { searchQuery ->
                                        navController.navigate(route = Routes.SearchPage.name + "?searchData=${searchQuery.toJson()}")
                                    }
                                )
                            }
                            composable(route = "${Routes.MajorityPage.name}/{parameter}/{parameter2}",
                                arguments = listOf(
                                    navArgument("parameter") { defaultValue = "Default" },
                                    navArgument("parameter2") { defaultValue = "Default" }
                                )
                            ) { backStackEntry ->
                                var parameter =
                                    backStackEntry.arguments?.getString("parameter") ?: "Default"
                                parameter = parameter.replace("_", "/")
                                var parameter2 =
                                    backStackEntry.arguments?.getString("parameter2") ?: "Default"
                                parameter2 = parameter2.replace("_", "/")
                                MajorityPage(
                                    loginViewModel = loginViewModel,
                                    goBack = { navController.navigate(route = Routes.SearchPage.name) },
                                    goToLogin = { navController.navigate(route = Routes.LoginPage.name) },
                                    goToRegistr = { navController.navigate(route = Routes.RegistrPage.name) },
                                    goToHistory = { navController.navigate(route = Routes.HistoryPage.name) },
                                    goToRateList = { param ->
                                        navController.navigate(
                                            "${Routes.RateListPage.name}/${
                                                param
                                                    .replace("/", "_")
                                            }"
                                        )
                                    },
                                    universityHref = parameter,
                                    regionHref = parameter2
                                )
                            }
                            composable(route = "${Routes.RateListPage.name}/{parameter}",
                                arguments = listOf(navArgument("parameter") {
                                    defaultValue = "Default"
                                })
                            )
                            { backStackEntry ->
                                var param =
                                    backStackEntry.arguments?.getString("parameter") ?: "Default"
                                param = param.replace("_", "/")
                                RateListPage(
                                    loginViewModel = loginViewModel,
                                    goBack = { navController.navigate(route = Routes.SearchPage.name) },
                                    goToLogin = { navController.navigate(route = Routes.LoginPage.name) },
                                    goToRegistr = { navController.navigate(route = Routes.RegistrPage.name) },
                                    goToHistory = { navController.navigate(route = Routes.HistoryPage.name) },
                                    parameter = param,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}