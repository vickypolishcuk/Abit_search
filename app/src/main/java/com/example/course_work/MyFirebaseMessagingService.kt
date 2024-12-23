package com.example.course_work

import android.annotation.SuppressLint
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.example.course_work.functions.sendNotification
import com.example.course_work.models.Search
import kotlinx.serialization.json.Json

@SuppressLint("MissingFirebaseInstanceTokenRefresh")
class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        println("onMessageReceived")
        remoteMessage.notification?.let {
            // Якщо є сповіщення, можна показати його на екрані
            sendNotification(this, emptyList())
        }

        // Перевіряємо, чи є додаткові дані в повідомленні
        remoteMessage.data.isNotEmpty().let {
            // Якщо є дані (наприклад, список пошукових результатів)
            val searchDataJson = remoteMessage.data["updated_data"]
            println("searchDataJson=${searchDataJson}")
            searchDataJson?.let {
                val searchData: List<Search> = Json.decodeFromString(it)
                // Тепер передаємо ці дані до методу sendNotification для відправки повідомлення
                sendNotification(this, searchData)
            }
        }
    }
}
