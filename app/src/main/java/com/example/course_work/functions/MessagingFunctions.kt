package com.example.course_work.functions

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.course_work.MainActivity
import com.example.course_work.R
import com.google.firebase.firestore.FirebaseFirestore
import android.content.Context
import com.example.course_work.models.InputData
import com.example.course_work.models.Search
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


fun sendNotification(context: Context, searchData: List<Search>, inputData: InputData) {
    println("sendNotification")
    val intent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
        val searchDataJson = Json.encodeToString(searchData)
        val inputDataJson = Json.encodeToString(inputData)
        putExtra("updated_data", searchDataJson)  // передача даних через повідомлення
        putExtra("input_data", inputDataJson)
    }

    val pendingIntent = PendingIntent.getActivity(
        context,
        0,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val channelId = "fcm_default_channel"
    val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
    val notificationBuilder = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(R.drawable.main_icon)
        .setContentTitle("Пошук завершено")
        .setContentText("Пошук завершився успішно! Ви можете переглянути результати вже зараз.")
        .setAutoCancel(true)
        .setSound(defaultSoundUri)
        .setContentIntent(pendingIntent)
        .setPriority(NotificationCompat.PRIORITY_HIGH)

    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            channelId,
            "Channel human readable title",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        notificationManager.createNotificationChannel(channel)
    }

    val notificationId = System.currentTimeMillis().toInt()
    notificationManager.notify(notificationId, notificationBuilder.build())
}

fun sendNotificationToCurrentUser(context: Context, searchData: List<Search>, inputData: InputData) {
    println("sendNotificationToCurrentUser")
    val db = FirebaseFirestore.getInstance()

    // Отримуємо токен поточного пристрою
    FirebaseMessaging.getInstance().token.addOnSuccessListener { currentToken ->
        db.collection("notifications")
            .document(currentToken) // Токен використовується як унікальний ID документа
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // Документ знайдено — викликаємо sendNotification
                    sendNotification(context, searchData, inputData)
                } else {
                    Log.w("Firestore", "No document found for token: $currentToken")
                }
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error fetching document", e)
            }
    }.addOnFailureListener { e ->
        Log.w("Firebase", "Failed to fetch current token", e)
    }
}
