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
import com.hieupnd.wordflash.domain.model.DailyStats
import com.hieupnd.wordflash.presentation.components.StreakCard
import com.hieupnd.wordflash.presentation.review.ReviewViewModel
import com.hieupnd.wordflash.presentation.stats.StatsViewModel
import com.hieupnd.wordflash.presentation.sync.SyncViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

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

    var showTimePicker by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) showTimePicker = true
    }

    val signInLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            syncViewModel.onGoogleSignInResult(account)
        } catch (e: ApiException) {
            val message = when (e.statusCode) {
                GoogleSignInStatusCodes.SIGN_IN_CANCELLED -> null
                CommonStatusCodes.DEVELOPER_ERROR -> "Lỗi cấu hình: kiểm tra SHA-1 trong Firebase Console"
                CommonStatusCodes.NETWORK_ERROR -> "Lỗi mạng, thử lại sau"
                else -> "Đăng nhập thất bại (mã lỗi: ${e.statusCode})"
            }
            syncViewModel.onGoogleSignInError(message)
        }
    }

    LaunchedEffect(Unit) { statsViewModel.loadStats() }

    Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
        TopAppBar(
            title = { Text("Cài đặt") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
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
            SectionHeader("Tài khoản & Đồng bộ")
            AccountSyncCard(
                syncUiState = syncUiState,
                onSignIn = { signInLauncher.launch(syncViewModel.googleSignInClient.signInIntent) },
                onSync = { syncViewModel.sync() },
                onSignOut = { syncViewModel.signOut() }
            )

            // Notifications section
            SectionHeader("Thông báo")
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

            // AI section
            SectionHeader("Cài đặt AI")
            GeminiApiKeyCard(settingsViewModel = settingsViewModel)

            // Statistics section
            SectionHeader("Thống kê")
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
            title = { Text("Lỗi đồng bộ") },
            text = { Text(error) },
            confirmButton = { TextButton(onClick = syncViewModel::clearError) { Text("OK") } }
        )
    }

    syncUiState.syncResult?.let { result ->
        AlertDialog(
            onDismissRequest = syncViewModel::clearSyncResult,
            title = { Text("Đồng bộ thành công") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Đã tải lên: ${result.vocabUploaded} từ vựng, ${result.sentenceUploaded} câu")
                    if (result.vocabAdded > 0 || result.vocabUpdated > 0)
                        Text("Từ vựng tải về: +${result.vocabAdded} mới, cập nhật ${result.vocabUpdated}")
                    if (result.sentenceAdded > 0 || result.sentenceUpdated > 0)
                        Text("Câu tải về: +${result.sentenceAdded} mới, cập nhật ${result.sentenceUpdated}")
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
                            syncUiState.currentUser!!.displayName ?: "Người dùng",
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
                            Text("Đang đồng bộ...")
                        } else {
                            Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Đồng bộ")
                        }
                    }
                    OutlinedButton(onClick = onSignOut, modifier = Modifier.weight(1f)) {
                        Text("Đăng xuất")
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
                        Text("Chưa đăng nhập", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            "Đăng nhập để đồng bộ dữ liệu trên nhiều thiết bị",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Button(onClick = onSignIn, modifier = Modifier.fillMaxWidth()) {
                    Text("Đăng nhập với Google")
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
                text = if (settingsViewModel.hasCustomKey()) "API key đã được cấu hình"
                       else "Chưa có API key — tính năng Gemini sẽ không hoạt động",
                style = MaterialTheme.typography.bodySmall,
                color = if (settingsViewModel.hasCustomKey()) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.error
            )
            OutlinedTextField(
                value = apiKeyText,
                onValueChange = { apiKeyText = it; saved = false },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Gemini API Key") },
                placeholder = { Text("Nhập API key của bạn") },
                singleLine = true,
                visualTransformation = if (keyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton(onClick = { keyVisible = !keyVisible }) {
                        Icon(
                            if (keyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (keyVisible) "Ẩn" else "Hiện"
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
                    Text(if (saved) "Đã lưu!" else "Lưu")
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
                        Text("Xóa key")
                    }
                }
            }
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
                    if (notificationHour >= 0) "Nhắc nhở hàng ngày" else "Nhắc nhở tắt",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                if (notificationHour >= 0) {
                    Text(
                        "Lúc %02d:%02d".format(notificationHour, notificationMinute),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            TextButton(onClick = onToggle) {
                Text(if (notificationHour >= 0) "Tắt" else "Bật")
            }
        }
    }
}

@Composable
private fun ActivityChartCard(stats: List<DailyStats>) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Hoạt động 7 ngày qua",
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
        LegendItem(color = colorVocab, label = "Từ thêm")
        LegendItem(color = colorSentence, label = "Câu thêm")
        LegendItem(color = colorReview, label = "Lượt ôn")
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
                val label = if (day.date == today) "Hôm nay" else day.date.format(dayFormatter)
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
        title = { Text("Chọn giờ nhắc nhở hàng ngày") },
        text = { TimePicker(state = timePickerState) },
        confirmButton = {
            TextButton(onClick = { onConfirm(timePickerState.hour, timePickerState.minute) }) { Text("Xác nhận") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Huỷ") }
        }
    )
}
