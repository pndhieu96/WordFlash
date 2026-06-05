package com.hieupnd.wordflash.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.hieupnd.wordflash.MainActivity
import com.hieupnd.wordflash.R

object NotificationHelper {
    const val CHANNEL_ID = "daily_reminder"
    const val ACTION_NAVIGATE_TO_REVIEW = "com.hieupnd.wordflash.NAVIGATE_TO_REVIEW"
    private const val NOTIFICATION_ID = 1001

    fun createChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Nhắc nhở ôn tập",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Thông báo nhắc nhở ôn tập từ vựng hàng ngày"
        }
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    fun showReminder(context: Context) {
        val intent = Intent(context, MainActivity::class.java).apply {
            action = ACTION_NAVIGATE_TO_REVIEW
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Nhắc nhở ôn tập")
            .setContentText("Hôm nay bạn chưa ôn tập từ vựng. Hãy học ngay!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val manager = context.getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, notification)
    }
}
