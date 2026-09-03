package com.hieupnd.wordflash.presentation.settings

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.hieupnd.wordflash.data.local.ReviewSettingsStore
import com.hieupnd.wordflash.domain.model.DailyStats
import com.hieupnd.wordflash.presentation.components.StreakCard
import com.hieupnd.wordflash.presentation.review.ReviewViewModel
import com.hieupnd.wordflash.presentation.stats.StatsViewModel
import com.hieupnd.wordflash.presentation.sync.SyncViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt
import androidx.compose.ui.res.stringResource
import com.hieupnd.wordflash.R
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import com.hieupnd.wordflash.data.local.LanguageStore
import androidx.activity.compose.LocalActivity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    innerPadding: PaddingValues,
    onBack: () -> Unit,
    syncViewModel: SyncViewModel = hiltViewModel(),
    reviewViewModel: ReviewViewModel = hiltViewModel(LocalContext.current as ComponentActivity),
    statsViewModel: StatsViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val syncUiState by syncViewModel.uiState.collectAsStateWithLifecycle()
    val reviewUiState by reviewViewModel.uiState.collectAsStateWithLifecycle()
    val statsUiState by statsViewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val activity = LocalActivity.current

    var showTimePicker by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) showTimePicker = true
    }

    val configErrorMessage = stringResource(R.string.settings_sign_in_config_error)
    val networkErrorMessage = stringResource(R.string.settings_sign_in_network_error)
    val signInFailedFormat = stringResource(R.string.settings_sign_in_failed_code)

    val signInLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            syncViewModel.onGoogleSignInResult(account)
        } catch (e: ApiException) {
            val message = when (e.statusCode) {
                GoogleSignInStatusCodes.SIGN_IN_CANCELLED -> null
                CommonStatusCodes.DEVELOPER_ERROR -> configErrorMessage
                CommonStatusCodes.NETWORK_ERROR -> networkErrorMessage
                else -> signInFailedFormat.format(e.statusCode)
            }
            syncViewModel.onGoogleSignInError(message)
        }
    }

    LaunchedEffect(Unit) { statsViewModel.loadStats() }

    Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
        TopAppBar(
            title = { Text(stringResource(R.string.title_settings)) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                }
            }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Account & Sync section
            SectionHeader(stringResource(R.string.settings_section_account))
            AccountSyncCard(
                syncUiState = syncUiState,
                onSignIn = { signInLauncher.launch(syncViewModel.googleSignInClient.signInIntent) },
                onSync = { syncViewModel.sync() },
                onSignOut = { syncViewModel.signOut() }
            )

            // Notifications section
            SectionHeader(stringResource(R.string.settings_section_notifications))
            NotificationCard(
                notificationHour = reviewUiState.notificationHour,
                notificationMinute = reviewUiState.notificationMinute,
                onToggle = {
                    if (reviewUiState.notificationHour >= 0) {
                        reviewViewModel.cancelNotification()
                    } else {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                            != PackageManager.PERMISSION_GRANTED
                        ) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            showTimePicker = true
                        }
                    }
                }
            )

            // Review session section
            SectionHeader(stringResource(R.string.settings_section_review))
            ReviewSessionCard(
                vocabSize = reviewUiState.vocabSessionSize,
                sentenceSize = reviewUiState.sentenceSessionSize,
                onSizesChange = reviewViewModel::setSessionSizes
            )

            // Language section
            SectionHeader(stringResource(R.string.settings_section_language))
            LanguageCard(
                current = settingsViewModel.getLanguage(),
                onSelect = { tag ->
                    if (tag != settingsViewModel.getLanguage()) {
                        settingsViewModel.setLanguage(tag)
                        activity?.recreate()
                    }
                }
            )

            // AI section
            SectionHeader(stringResource(R.string.settings_section_ai))
            GeminiApiKeyCard(settingsViewModel = settingsViewModel)

            // Statistics section
            SectionHeader(stringResource(R.string.settings_section_stats))
            StreakCard(
                currentStreak = reviewUiState.currentStreak,
                longestStreak = reviewUiState.longestStreak
            )
            if (statsUiState.dailyStats.isNotEmpty()) {
                ActivityChartCard(stats = statsUiState.dailyStats)
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }

    if (showTimePicker) {
        NotificationTimePickerDialog(
            initialHour = if (reviewUiState.notificationHour >= 0) reviewUiState.notificationHour else 20,
            initialMinute = reviewUiState.notificationMinute,
            onDismiss = { showTimePicker = false },
            onConfirm = { hour, minute ->
                reviewViewModel.setNotificationTime(hour, minute)
                showTimePicker = false
            }
        )
    }

    syncUiState.syncError?.let { error ->
        AlertDialog(
            onDismissRequest = syncViewModel::clearError,
            title = { Text(stringResource(R.string.settings_sync_error_title)) },
            text = { Text(error) },
            confirmButton = { TextButton(onClick = syncViewModel::clearError) { Text("OK") } }
        )
    }

    syncUiState.syncResult?.let { result ->
        AlertDialog(
            onDismissRequest = syncViewModel::clearSyncResult,
            title = { Text(stringResource(R.string.settings_sync_success_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(stringResource(R.string.settings_sync_uploaded, result.vocabUploaded, result.sentenceUploaded))
                    if (result.vocabAdded > 0 || result.vocabUpdated > 0)
                        Text(stringResource(R.string.settings_sync_vocab_down, result.vocabAdded, result.vocabUpdated))
                    if (result.sentenceAdded > 0 || result.sentenceUpdated > 0)
                        Text(stringResource(R.string.settings_sync_sentence_down, result.sentenceAdded, result.sentenceUpdated))
                }
            },
            confirmButton = {
                TextButton(onClick = syncViewModel::clearSyncResult) { Text("OK") }
            }
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 4.dp)
    )
}

@Composable
private fun AccountSyncCard(
    syncUiState: com.hieupnd.wordflash.presentation.sync.SyncUiState,
    onSignIn: () -> Unit,
    onSync: () -> Unit,
    onSignOut: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (syncUiState.currentUser != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.AccountCircle,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            syncUiState.currentUser!!.displayName ?: stringResource(R.string.settings_user_fallback),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            syncUiState.currentUser!!.email ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                HorizontalDivider()
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onSync,
                        modifier = Modifier.weight(1f),
                        enabled = !syncUiState.isSyncing
                    ) {
                        if (syncUiState.isSyncing) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = MaterialTheme.colorScheme.onPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.settings_syncing))
                        } else {
                            Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.settings_sync))
                        }
                    }
                    OutlinedButton(onClick = onSignOut, modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.settings_sign_out))
                    }
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.AccountCircle,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(stringResource(R.string.settings_not_signed_in), style = MaterialTheme.typography.bodyLarge)
                        Text(
                            stringResource(R.string.settings_sign_in_prompt),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Button(onClick = onSignIn, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.settings_sign_in_google))
                }
            }
        }
    }
}

@Composable
private fun GeminiApiKeyCard(settingsViewModel: SettingsViewModel) {
    var apiKeyText by rememberSaveable { mutableStateOf(settingsViewModel.getStoredApiKey()) }
    var keyVisible by remember { mutableStateOf(false) }
    var saved by remember { mutableStateOf(false) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = stringResource(
                    if (settingsViewModel.hasCustomKey()) R.string.settings_api_key_configured
                    else R.string.settings_api_key_missing
                ),
                style = MaterialTheme.typography.bodySmall,
                color = if (settingsViewModel.hasCustomKey()) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.error
            )
            OutlinedTextField(
                value = apiKeyText,
                onValueChange = { apiKeyText = it; saved = false },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Gemini API Key") },
                placeholder = { Text(stringResource(R.string.settings_api_key_hint)) },
                singleLine = true,
                visualTransformation = if (keyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton(onClick = { keyVisible = !keyVisible }) {
                        Icon(
                            if (keyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = stringResource(if (keyVisible) R.string.action_hide else R.string.action_show)
                        )
                    }
                }
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        settingsViewModel.saveApiKey(apiKeyText)
                        saved = true
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(if (saved) R.string.action_saved_exclaim else R.string.action_save))
                }
                if (settingsViewModel.hasCustomKey()) {
                    OutlinedButton(
                        onClick = {
                            settingsViewModel.saveApiKey("")
                            apiKeyText = ""
                            saved = false
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.settings_delete_key))
                    }
                }
            }
        }
    }
}

@Composable
private fun LanguageCard(
    current: String,
    onSelect: (String) -> Unit
) {
    val options = listOf(
        LanguageStore.VIETNAMESE to R.string.settings_language_vietnamese,
        LanguageStore.ENGLISH to R.string.settings_language_english,
    )
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                options.forEachIndexed { index, (tag, labelRes) ->
                    SegmentedButton(
                        selected = current == tag,
                        onClick = { onSelect(tag) },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size)
                    ) {
                        Text(stringResource(labelRes))
                    }
                }
            }
            Text(
                text = stringResource(R.string.settings_language_note),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ReviewSessionCard(
    vocabSize: Int,
    sentenceSize: Int,
    onSizesChange: (Int, Int) -> Unit
) {
    var vocabValue by remember(vocabSize) { mutableStateOf(vocabSize.toFloat()) }
    var sentenceValue by remember(sentenceSize) { mutableStateOf(sentenceSize.toFloat()) }

    val commit = { onSizesChange(vocabValue.roundToInt(), sentenceValue.roundToInt()) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SessionSizeSlider(
                label = stringResource(R.string.settings_words_per_session),
                value = vocabValue,
                min = ReviewSettingsStore.VOCAB_MIN,
                max = ReviewSettingsStore.VOCAB_MAX,
                steps = 8,
                onValueChange = { vocabValue = it },
                onValueChangeFinished = commit
            )
            HorizontalDivider()
            SessionSizeSlider(
                label = stringResource(R.string.settings_sentences_per_session),
                value = sentenceValue,
                min = ReviewSettingsStore.SENTENCE_MIN,
                max = ReviewSettingsStore.SENTENCE_MAX,
                steps = 3,
                onValueChange = { sentenceValue = it },
                onValueChangeFinished = commit
            )
            Text(
                text = stringResource(R.string.settings_cards_per_session, vocabValue.roundToInt() + sentenceValue.roundToInt()),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SessionSizeSlider(
    label: String,
    value: Float,
    min: Int,
    max: Int,
    steps: Int,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = value.roundToInt().toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            onValueChangeFinished = onValueChangeFinished,
            valueRange = min.toFloat()..max.toFloat(),
            steps = steps
        )
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = min.toString(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = max.toString(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.End
            )
        }
    }
}

@Composable
private fun NotificationCard(
    notificationHour: Int,
    notificationMinute: Int,
    onToggle: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                if (notificationHour >= 0) Icons.Default.Notifications else Icons.Default.NotificationsOff,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = if (notificationHour >= 0) MaterialTheme.colorScheme.primary
                       else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    stringResource(if (notificationHour >= 0) R.string.settings_reminder_on else R.string.settings_reminder_off),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                if (notificationHour >= 0) {
                    Text(
                        stringResource(R.string.settings_reminder_at, notificationHour, notificationMinute),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            TextButton(onClick = onToggle) {
                Text(stringResource(if (notificationHour >= 0) R.string.settings_turn_off else R.string.settings_turn_on))
            }
        }
    }
}

@Composable
private fun ActivityChartCard(stats: List<DailyStats>) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.stats_activity_7_days),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(4.dp))
            ChartLegend()
            Spacer(Modifier.height(16.dp))
            BarChart(stats = stats)
        }
    }
}

private val colorVocab = Color(0xFF4CAF50)
private val colorSentence = Color(0xFF2196F3)
private val colorReview = Color(0xFFFF9800)

@Composable
private fun ChartLegend() {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        LegendItem(color = colorVocab, label = stringResource(R.string.stats_legend_words))
        LegendItem(color = colorSentence, label = stringResource(R.string.stats_legend_sentences))
        LegendItem(color = colorReview, label = stringResource(R.string.stats_legend_reviews))
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(color))
        Text(text = label, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun BarChart(stats: List<DailyStats>) {
    val maxValue = stats.maxOf { maxOf(it.vocabAdded, it.sentencesAdded, it.reviewCount) }.coerceAtLeast(1)
    val dayFormatter = DateTimeFormatter.ofPattern("dd/MM")
    val today = LocalDate.now()
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Canvas(modifier = Modifier.fillMaxWidth().height(160.dp)) {
            val chartWidth = size.width
            val chartHeight = size.height
            val slotWidth = chartWidth / stats.size
            val barGroupWidth = slotWidth * 0.7f
            val barWidth = barGroupWidth / 3f
            val gap = slotWidth * 0.15f

            for (i in 0..4) {
                val y = chartHeight - (i.toFloat() / 4) * chartHeight
                drawLine(color = surfaceVariant, start = Offset(0f, y), end = Offset(chartWidth, y), strokeWidth = 1.dp.toPx())
            }

            stats.forEachIndexed { index, day ->
                val slotLeft = index * slotWidth + gap
                val vocabH = (day.vocabAdded.toFloat() / maxValue) * chartHeight
                val sentenceH = (day.sentencesAdded.toFloat() / maxValue) * chartHeight
                val reviewH = (day.reviewCount.toFloat() / maxValue) * chartHeight
                drawRect(color = colorVocab, topLeft = Offset(slotLeft, chartHeight - vocabH), size = Size(barWidth, vocabH.coerceAtLeast(2.dp.toPx())))
                drawRect(color = colorSentence, topLeft = Offset(slotLeft + barWidth, chartHeight - sentenceH), size = Size(barWidth, sentenceH.coerceAtLeast(2.dp.toPx())))
                drawRect(color = colorReview, topLeft = Offset(slotLeft + barWidth * 2, chartHeight - reviewH), size = Size(barWidth, reviewH.coerceAtLeast(2.dp.toPx())))
            }
        }

        Row(modifier = Modifier.fillMaxWidth()) {
            stats.forEach { day ->
                val todayLabel = stringResource(R.string.stats_today)
                val label = if (day.date == today) todayLabel else day.date.format(dayFormatter)
                Text(
                    text = label,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 9.sp,
                    color = if (day.date == today) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotificationTimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int, Int) -> Unit
) {
    val timePickerState = rememberTimePickerState(initialHour = initialHour, initialMinute = initialMinute, is24Hour = true)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_pick_reminder_time)) },
        text = { TimePicker(state = timePickerState) },
        confirmButton = {
            TextButton(onClick = { onConfirm(timePickerState.hour, timePickerState.minute) }) { Text(stringResource(R.string.action_confirm)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        }
    )
}
