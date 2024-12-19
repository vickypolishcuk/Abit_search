package com.example.course_work.functions

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.course_work.models.History
import com.example.course_work.models.Search
import com.example.course_work.models.User
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

// Функції для списку користувачів
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

// Ключ для зберігання списку користувачів
private val USERS_KEY = stringPreferencesKey("users_list")

suspend fun saveUser(context: Context, user: User) {
    context.dataStore.edit { preferences ->
        val usersJson = preferences[USERS_KEY] ?: "[]"
        val userList = Json.decodeFromString<MutableList<User>>(usersJson)

        // Перевіряємо, чи користувач з таким ім'ям вже існує
        if (userList.none { it.username == user.username }) {
            userList.add(user) // Додаємо нового користувача
            preferences[USERS_KEY] = Json.encodeToString(userList) // Зберігаємо список
        }
    }
}

//suspend fun getUsers(context: Context): List<User> {
//    val preferences = context.dataStore.data.firstOrNull()
//    val usersJson = preferences?.get(USERS_KEY) ?: "[]"
//    return Json.decodeFromString(usersJson)
//}

suspend fun getUsers(context: Context): List<User> {
    val preferences = context.dataStore.data.firstOrNull()
    val usersJson = preferences?.get(USERS_KEY) ?: "[]"

    return try {
        Json.decodeFromString(usersJson)
    } catch (e: Exception) {
        // Якщо структура змінилася, спробуємо обробити це
        val legacyUsers = Json.parseToJsonElement(usersJson).jsonArray
        legacyUsers.map { legacyUser ->
            val userObject = legacyUser.jsonObject
            User(
                username = userObject["username"]?.jsonPrimitive?.content ?: "",
                password = userObject["password"]?.jsonPrimitive?.content ?: "",
                history = userObject["history"]?.jsonArray?.map { legacyHistory ->
                    val historyObject = legacyHistory.jsonObject
                    History(
                        name = historyObject["name"]?.jsonPrimitive?.content ?: "",
                        year = historyObject["year"]?.jsonPrimitive?.content ?: "",
                        region = historyObject["region"]?.jsonPrimitive?.content ?: "",
                        point = historyObject["point"]?.jsonPrimitive?.content ?: "",
                        date = historyObject["date"]?.jsonPrimitive?.content ?: "",
                        data = historyObject["data"]?.jsonArray?.map { legacySearch ->
                            val searchObject = legacySearch.jsonObject
                            Search(
                                okr = searchObject["okr"]?.jsonPrimitive?.content ?: "",
                                status = searchObject["status"]?.jsonPrimitive?.content ?: "",
                                numberOfPlace = searchObject["numberOfPlace"]?.jsonPrimitive?.content ?: "",
                                priority = searchObject["priority"]?.jsonPrimitive?.content ?: "",
                                point = searchObject["point"]?.jsonPrimitive?.content ?: "",
                                sbo = searchObject["sbo"]?.jsonPrimitive?.content ?: "",
                                consistOfSubjects = searchObject["consistOfSubjects"]?.jsonArray?.map { it.jsonPrimitive.content }
                                    ?: emptyList(),
                                consistOfPoint = searchObject["consistOfPoint"]?.jsonArray?.map { it.jsonPrimitive.content }
                                    ?: emptyList(),
                                university = searchObject["university"]?.jsonPrimitive?.content ?: "",
                                majority = searchObject["majority"]?.jsonPrimitive?.content ?: "",
                                kvota = searchObject["kvota"]?.jsonPrimitive?.content ?: "",
                                doc = searchObject["doc"]?.jsonPrimitive?.content ?: "",
                                hrefToRateList = searchObject["hrefToRateList"]?.jsonPrimitive?.content ?: ""
                            )
                        } ?: emptyList()
                    )
                }?.toMutableList() ?: mutableListOf()
            )
        }
    }
}

suspend fun removeUser(context: Context, username: String) {
    context.dataStore.edit { preferences ->
        val usersJson = preferences[USERS_KEY] ?: "[]"
        val userList = Json.decodeFromString<MutableList<User>>(usersJson)

        // Видаляємо користувача за ім'ям
        val updatedList = userList.filter { it.username != username }
        preferences[USERS_KEY] = Json.encodeToString(updatedList) // Оновлюємо список
    }
}

// Функції для взаємодії з історією
suspend fun clearUserHistory(context: Context, username: String) {
    context.dataStore.edit { preferences ->
        val usersJson = preferences[USERS_KEY] ?: "[]"
        val userList = Json.decodeFromString<MutableList<User>>(usersJson)

        // Знаходимо користувача і очищуємо його історію
        val updatedList = userList.map { user ->
            if (user.username == username) user.copy(history = mutableListOf())
            else user
        }

        preferences[USERS_KEY] = Json.encodeToString(updatedList) // Оновлюємо список
    }
}

suspend fun addHistoryToUser(context: Context, username: String, history: History) {
    context.dataStore.edit { preferences ->
        val usersJson = preferences[USERS_KEY] ?: "[]"
        val userList = Json.decodeFromString<MutableList<User>>(usersJson)

        // Знаходимо користувача і додаємо запис в історію
        val updatedList = userList.map { user ->
            println("user.username=${user.username}, username=${username}, history=${history}")
            if (user.username == username) {
                user.copy(history = user.history.apply { add(history) })
            } else user
        }

        preferences[USERS_KEY] = Json.encodeToString(updatedList) // Оновлюємо список
    }
}

suspend fun getUserHistory(context: Context): List<History> {
    val currentUser = getCurrentUser(context) // Отримуємо поточного користувача
    if (currentUser != null) {
        val users = getUsers(context) // Отримуємо список усіх користувачів
        val user = users.find { it.username == currentUser } // Знаходимо залогіненого користувача
        println("user=${user}")
        return user?.history ?: emptyList() // Повертаємо його історію або порожній список
    }
    return emptyList() // Якщо користувача немає, повертаємо порожній список
}

// Функції для залогіненого користувача
private val CURRENT_USER_KEY = stringPreferencesKey("current_user")

suspend fun saveCurrentUser(context: Context, username: String) {
    context.dataStore.edit { preferences ->
        preferences[CURRENT_USER_KEY] = username
    }
}

suspend fun getCurrentUser(context: Context): String? {
    val preferences = context.dataStore.data.firstOrNull()
    return preferences?.get(CURRENT_USER_KEY)
}

suspend fun clearCurrentUser(context: Context) {
    context.dataStore.edit { preferences ->
        preferences.remove(CURRENT_USER_KEY)
    }
}


suspend fun clearDataStore(context: Context) {
    context.dataStore.edit { preferences ->
        preferences.clear() // Очищає всі дані
    }
}
