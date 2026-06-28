package com.hieupnd.wordflash.presentation.review

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.hieupnd.wordflash.domain.model.ReviewItem
import com.hieupnd.wordflash.domain.repository.SentenceRepository
import com.hieupnd.wordflash.domain.repository.VocabularyRepository
import com.hieupnd.wordflash.domain.usecase.review.GetReviewCardsUseCase
import com.hieupnd.wordflash.domain.usecase.sentence.UpdateSentenceMemorizationUseCase
import com.hieupnd.wordflash.domain.usecase.vocabulary.UpdateVocabularyMemorizationUseCase
import com.hieupnd.wordflash.notification.DailyReminderWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject

private const val VOCAB_SESSION_SIZE = 20
private const val SENTENCE_SESSION_SIZE = 5
private const val WORK_TAG = "daily_reminder"

@HiltViewModel
class ReviewViewModel @Inject constructor(
    private val getReviewCardsUseCase: GetReviewCardsUseCase,
    private val updateVocabMemorizationUseCase: UpdateVocabularyMemorizationUseCase,
    private val updateSentenceMemorizationUseCase: UpdateSentenceMemorizationUseCase,
    private val vocabRepository: VocabularyRepository,
    private val sentenceRepository: SentenceRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val prefs = context.getSharedPreferences(DailyReminderWorker.PREFS_NAME, Context.MODE_PRIVATE)

    private val _uiState = MutableStateFlow(ReviewUiState())
    val uiState: StateFlow<ReviewUiState> = _uiState.asStateFlow()

    init {
        loadNotificationPrefs()
        loadSession()
    }

    private fun loadNotificationPrefs() {
        val hour = prefs.getInt(KEY_NOTIFICATION_HOUR, -1)
        val minute = prefs.getInt(KEY_NOTIFICATION_MINUTE, 0)
        var currentStreak = prefs.getInt(DailyReminderWorker.KEY_CURRENT_STREAK, 0)
        var longestStreak = prefs.getInt(DailyReminderWorker.KEY_LONGEST_STREAK, 0)

        // Migration: nếu user đã học hôm nay (dữ liệu cũ) nhưng streak chưa được track lần nào
        val today = LocalDate.now().toString()
        val lastStudy = prefs.getString(DailyReminderWorker.KEY_LAST_STUDY_DATE, "")
        if (lastStudy == today && !prefs.contains(DailyReminderWorker.KEY_CURRENT_STREAK)) {
            currentStreak = 1
            longestStreak = 1
            prefs.edit()
                .putInt(DailyReminderWorker.KEY_CURRENT_STREAK, currentStreak)
                .putInt(DailyReminderWorker.KEY_LONGEST_STREAK, longestStreak)
                .apply()
        }

        _uiState.update {
            it.copy(
                notificationHour = hour,
                notificationMinute = minute,
                currentStreak = currentStreak,
                longestStreak = longestStreak
            )
        }
    }

    private fun loadSession() {
        viewModelScope.launch {
            applyDecay()
            val today = LocalDate.now().toString()
            val lastStudy = prefs.getString(DailyReminderWorker.KEY_LAST_STUDY_DATE, "")
            val studiedToday = lastStudy == today

            val allItems = getReviewCardsUseCase().first()
            val vocabItems = allItems.filterIsInstance<ReviewItem.VocabItem>().take(VOCAB_SESSION_SIZE)
            val sentenceItems = allItems.filterIsInstance<ReviewItem.SentenceItem>().take(SENTENCE_SESSION_SIZE)
            val items = vocabItems + sentenceItems

            _uiState.update {
                it.copy(
                    reviewItems = items,
                    totalItems = items.size,
                    currentIndex = 0,
                    isFlipped = false,
                    isComplete = items.isEmpty(),
                    hasStudiedToday = studiedToday
                )
            }
        }
    }

    private suspend fun applyDecay() {
        val now = System.currentTimeMillis()
        val decayThreshold = 7 * 24 * 60 * 60 * 1000L

        vocabRepository.getAllCards().first().forEach { card ->
            val lastReviewed = card.lastReviewedAt.takeIf { it > 0 } ?: card.updatedAt
            if (now - lastReviewed > decayThreshold && card.memorizationLevel > 0) {
                updateVocabMemorizationUseCase(card.id, card.memorizationLevel - 1)
            }
        }

        sentenceRepository.getAllCards().first().forEach { card ->
            val lastReviewed = card.lastReviewedAt.takeIf { it > 0 } ?: card.updatedAt
            if (now - lastReviewed > decayThreshold && card.memorizationLevel > 0) {
                updateSentenceMemorizationUseCase(card.id, card.memorizationLevel - 1)
            }
        }
    }

    fun flipCard() {
        _uiState.update { it.copy(isFlipped = !it.isFlipped) }
    }

    fun rateCard(level: Int) {
        val state = _uiState.value
        val current = state.currentItem ?: return
        viewModelScope.launch {
            when (current) {
                is ReviewItem.VocabItem -> updateVocabMemorizationUseCase(current.id, level)
                is ReviewItem.SentenceItem -> updateSentenceMemorizationUseCase(current.id, level)
            }
            markStudiedToday()
            val nextIndex = state.currentIndex + 1
            if (nextIndex >= state.reviewItems.size) {
                _uiState.update { it.copy(isComplete = true) }
            } else {
                _uiState.update { it.copy(currentIndex = nextIndex, isFlipped = false) }
            }
        }
    }

    private fun markStudiedToday() {
        val today = LocalDate.now().toString()
        val yesterday = LocalDate.now().minusDays(1).toString()
        val lastStudy = prefs.getString(DailyReminderWorker.KEY_LAST_STUDY_DATE, "")

        if (lastStudy == today) {
            _uiState.update { it.copy(hasStudiedToday = true) }
            return
        }

        val currentStreak = if (lastStudy == yesterday)
            prefs.getInt(DailyReminderWorker.KEY_CURRENT_STREAK, 0) + 1
        else 1
        val longestStreak = maxOf(prefs.getInt(DailyReminderWorker.KEY_LONGEST_STREAK, 0), currentStreak)

        prefs.edit()
            .putString(DailyReminderWorker.KEY_LAST_STUDY_DATE, today)
            .putInt(DailyReminderWorker.KEY_CURRENT_STREAK, currentStreak)
            .putInt(DailyReminderWorker.KEY_LONGEST_STREAK, longestStreak)
            .apply()
        _uiState.update {
            it.copy(
                hasStudiedToday = true,
                currentStreak = currentStreak,
                longestStreak = longestStreak
            )
        }
    }

    fun restartSession() {
        loadSession()
    }

    fun setNotificationTime(hour: Int, minute: Int) {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (!after(now)) add(Calendar.DAY_OF_YEAR, 1)
        }
        val initialDelay = target.timeInMillis - now.timeInMillis

        val request = PeriodicWorkRequestBuilder<DailyReminderWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_TAG,
            ExistingPeriodicWorkPolicy.REPLACE,
            request
        )

        prefs.edit()
            .putInt(KEY_NOTIFICATION_HOUR, hour)
            .putInt(KEY_NOTIFICATION_MINUTE, minute)
            .apply()

        _uiState.update { it.copy(notificationHour = hour, notificationMinute = minute) }
    }

    fun cancelNotification() {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_TAG)
        prefs.edit().putInt(KEY_NOTIFICATION_HOUR, -1).apply()
        _uiState.update { it.copy(notificationHour = -1) }
    }

    companion object {
        private const val KEY_NOTIFICATION_HOUR = "notification_hour"
        private const val KEY_NOTIFICATION_MINUTE = "notification_minute"
    }
}
