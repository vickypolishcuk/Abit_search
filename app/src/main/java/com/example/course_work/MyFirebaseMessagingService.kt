package com.example.course_work

import android.annotation.SuppressLint
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.example.course_work.functions.sendNotification
import com.example.course_work.models.InputData
import com.example.course_work.models.Search
import kotlinx.serialization.json.Json

@SuppressLint("MissingFirebaseInstanceTokenRefresh")
class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        remoteMessage.notification?.let {
            // Якщо є сповіщення, показуємо його на екрані
            sendNotification(this, emptyList(), InputData())
        }
        // Перевіряємо, чи є додаткові дані в повідомленні
        remoteMessage.data.isNotEmpty().let {
            val inputDataJson = remoteMessage.data["input_data"]
            val inputData: InputData? = inputDataJson?.let {
                try {
                    Json.decodeFromString<InputData>(it) // Декодуємо InputData
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            }
            val searchDataJson = remoteMessage.data["updated_data"]
            val searchData: List<Search>? = searchDataJson?.let {
                try {
                    Json.decodeFromString<List<Search>>(it) // Декодуємо SearchData
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            }
            if (inputData != null){
                if (!searchData.isNullOrEmpty()) {
                    sendNotification(this, searchData, inputData)
                }
                else {
                    sendNotification(this, emptyList(), inputData)
                }
            }
        }
    }
}
