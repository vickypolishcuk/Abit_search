package com.example.course_work.functions

import android.content.Context
import android.util.Log
import com.example.course_work.models.History
import com.example.course_work.models.Search
import com.example.course_work.models.User
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

// Функція реєстрації користувача
fun saveUserToFirestore(username: String, password: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    val db = FirebaseFirestore.getInstance()
    db.collection("users").whereEqualTo("username", username).get()
        .addOnSuccessListener { querySnapshot ->
            if (querySnapshot.isEmpty) {
                val userData = hashMapOf(
                    "username" to username,
                    "password" to password,
                    "history" to emptyList<History>()
                )
                db.collection("users").add(userData)
                    .addOnSuccessListener { documentReference ->
                        // Отримуємо ID документа після його додавання
                        val userId = documentReference.id

                        // Оновлюємо документ з додаванням userId
                        documentReference.update("userId", userId)
                            .addOnSuccessListener {
                                onSuccess() // Викликаємо успішну функцію
                            }
                            .addOnFailureListener { e ->
                                onFailure(e) // Викликаємо функцію помилки при невдачі
                            }
                    }
                    .addOnFailureListener { e -> onFailure(e) }
            } else {
                onFailure(Exception("Username already exists"))
            }
        }
        .addOnFailureListener { e -> onFailure(e) }
}

// Функція отримання всіх користувачів
fun getUsersFromFirestore(onSuccess: (List<User>) -> Unit, onFailure: (Exception) -> Unit) {
    val db = FirebaseFirestore.getInstance()
    println("Connect to db")

    db.collection("users").get()
        .addOnSuccessListener { querySnapshot ->
            val users = querySnapshot.documents.mapNotNull { doc ->
                val userId = doc.id // Отримуємо ID документа
                val username = doc.getString("username") ?: return@mapNotNull null
                val password = doc.getString("password") ?: return@mapNotNull null
                println("userId=${userId}, username=${username}, password=${password}")
                val history = (doc.get("history") as? List<HashMap<String, Any>>)?.map { map ->
                    History(
                        name = map["name"] as? String ?: "",
                        year = map["year"] as? String ?: "",
                        region = map["region"] as? String ?: "",
                        point = map["point"] as? String ?: "",
                        date = map["date"] as? String ?: "",
                        data = (map["data"] as? List<HashMap<String, Any>>)?.map { searchMap ->
                            Search(
                                okr = searchMap["okr"] as? String ?: "",
                                status = searchMap["status"] as? String ?: "",
                                numberOfPlace = searchMap["numberOfPlace"] as? String ?: "",
                                priority = searchMap["priority"] as? String ?: "",
                                point = searchMap["point"] as? String ?: "",
                                sbo = searchMap["sbo"] as? String ?: "",
                                consistOfSubjects = (searchMap["consistOfSubjects"] as? List<String>) ?: emptyList(),
                                consistOfPoint = (searchMap["consistOfPoint"] as? List<String>) ?: emptyList(),
                                university = searchMap["university"] as? String ?: "",
                                majority = searchMap["majority"] as? String ?: "",
                                kvota = searchMap["kvota"] as? String ?: "",
                                doc = searchMap["doc"] as? String ?: "",
                                hrefToRateList = searchMap["hrefToRateList"] as? String ?: ""
                            )
                        } ?: emptyList()
                    )
                } ?: emptyList()

                User(userId, username, password, history)
            }
            onSuccess(users)
        }
        .addOnFailureListener { e -> onFailure(e) }
}

// Функції для роботи з історією
// Функція для очищення історії користувача
fun clearUserHistoryFirestore(username: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    getUsersFromFirestore(
        onSuccess = { users ->
            // Знаходимо користувача за username
            val user = users.find { it.username == username }
            if (user != null) {
                val userId = user.userId // Тепер отримуємо userId
                val db = FirebaseFirestore.getInstance()
                db.collection("users").document(userId)
                    .update("history", emptyList<History>())
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { e -> onFailure(e) }
            } else {
                Log.e("Firestore", "User not found with username: $username")
            }
        },
        onFailure = { e ->
            Log.e("Firestore", "Error fetching users", e)
        }
    )
}

// Функція для додавання елемента до історії користувача
fun addHistoryItem(username: String, newHistoryItem: History) {
    getUsersFromFirestore(
        onSuccess = { users ->
            // Знаходимо користувача за username
            val user = users.find { it.username == username }
            if (user != null) {
                val userId = user.userId // Тепер отримуємо userId
                val db = FirebaseFirestore.getInstance()

                db.collection("users").document(userId)
                    .update("history", FieldValue.arrayUnion(newHistoryItem))
                    .addOnSuccessListener {
                        Log.d("Firestore", "History item added successfully")
                    }
                    .addOnFailureListener { e ->
                        Log.e("Firestore", "Error adding history item", e)
                    }
            } else {
                Log.e("Firestore", "User not found with username: $username")
            }
        },
        onFailure = { e ->
            Log.e("Firestore", "Error fetching users", e)
        }
    )
}

// Функція для отримання історії користувача
fun getHistory(username: String, onSuccess: (List<History>) -> Unit, onFailure: (Exception) -> Unit) {
    getUsersFromFirestore(
        onSuccess = { users ->
            // Знаходимо користувача за username
            val user = users.find { it.username == username }
            if (user != null) {
                val userId = user.userId // Тепер отримуємо userId
                val db = FirebaseFirestore.getInstance()

                db.collection("users").document(userId)
                    .get()
                    .addOnSuccessListener { document ->
                        val history = document.toObject(User::class.java)?.history ?: emptyList()
                        onSuccess(history)
                    }
                    .addOnFailureListener { e ->
                        Log.e("Firestore", "Error fetching history", e)
                        onFailure(e)
                    }
            } else {
                Log.e("Firestore", "User not found with username: $username")
            }
        },
        onFailure = { e ->
            Log.e("Firestore", "Error fetching users", e)
        }
    )
}


// Функції для залогіненого користувача
// Функція збереження залогіненого користувача
fun saveCurrentUser(context: Context, username: String) {
    val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    sharedPreferences.edit().putString("current_user", username).apply()
}

// Функція отримання залогіненого користувача
fun getCurrentUser(context: Context): String? {
    val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    return sharedPreferences.getString("current_user", null)
}

// Функція очищення залогіненого користувача
fun clearCurrentUser(context: Context) {
    val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    sharedPreferences.edit().remove("current_user").apply()
}
