package com.hieupnd.wordflash.notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import java.time.LocalDate

class DailyReminderWorker(
    ctx: Context,
    params: WorkerParameters
) : CoroutineWorker(ctx, params) {

    override suspend fun doWork(): Result {
        val prefs = applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lastStudyDate = prefs.getString(KEY_LAST_STUDY_DATE, "")
        val today = LocalDate.now().toString()
        if (lastStudyDate != today) {
            NotificationHelper.showReminder(applicationContext)
        }
        return Result.success()
    }

    companion object {
        const val PREFS_NAME = "wordflash_prefs"
        const val KEY_LAST_STUDY_DATE = "last_study_date"
    }
}
