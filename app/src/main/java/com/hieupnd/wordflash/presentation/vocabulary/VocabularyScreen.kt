package com.hieupnd.wordflash.presentation.vocabulary

import android.app.Activity
import android.speech.tts.TextToSpeech
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.hieupnd.wordflash.presentation.sync.SyncViewModel
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SuggestionChip
import androidx.compose.ui.layout.ContentScale
import com.hieupnd.wordflash.domain.model.Example
import com.hieupnd.wordflash.domain.model.VocabularyCard
import com.hieupnd.wordflash.domain.model.WordMeaning
import com.hieupnd.wordflash.presentation.components.WordFlashAsyncImage
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VocabularyScreen(
    innerPadding: PaddingValues,
    viewModel: VocabularyViewModel = hiltViewModel(),
    syncViewModel: SyncViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val syncUiState by syncViewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    var showAccountMenu by remember { mutableStateOf(false) }
    val signInLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            syncViewModel.onGoogleSignInResult(account)
        } catch (e: ApiException) {
            val message = when (e.statusCode) {
                GoogleSignInStatusCodes.SIGN_IN_CANCELLED -> null // người dùng tự huỷ hoặc nhấn Back
                CommonStatusCodes.DEVELOPER_ERROR -> "Lỗi cấu hình: kiểm tra SHA-1 trong Firebase Console"
                CommonStatusCodes.NETWORK_ERROR -> "Lỗi mạng, thử lại sau"
                else -> "Đăng nhập thất bại (mã lỗi: ${e.statusCode})"
            }
            syncViewModel.onGoogleSignInError(message)
        }
    }

    val tts = remember {
        var instance: TextToSpeech? = null
        instance = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                instance?.language = Locale.US
            }
        }
        instance
    }
    DisposableEffect(Unit) { onDispose { tts?.shutdown() } }

    Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
        TopAppBar(
            title = { Text("WordFlash") },
            actions = {
                if (syncUiState.currentUser != null) {
                    if (syncUiState.isSyncing) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp).padding(end = 4.dp))
                    } else {
                        IconButton(onClick = { syncViewModel.sync() }) {
                            Icon(Icons.Default.Sync, contentDescription = "Đồng bộ")
                        }
                    }
                }
                Box {
                    IconButton(onClick = { showAccountMenu = true }) {
                        Icon(Icons.Default.Person, contentDescription = "Tài khoản")
                    }
                    DropdownMenu(expanded = showAccountMenu, onDismissRequest = { showAccountMenu = false }) {
                        if (syncUiState.currentUser != null) {
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(syncUiState.currentUser!!.displayName ?: "Người dùng", style = MaterialTheme.typography.bodyMedium)
                                        Text(syncUiState.currentUser!!.email ?: "", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                },
                                onClick = {}
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text("Đăng xuất") },
                                onClick = { syncViewModel.signOut(); showAccountMenu = false }
                            )
                        } else {
                            DropdownMenuItem(
                                text = { Text("Đăng nhập với Google") },
                                onClick = {
                                    showAccountMenu = false
                                    signInLauncher.launch(syncViewModel.googleSignInClient.signInIntent)
                                }
                            )
                        }
                    }
                }
            }
        )

        SecondaryTabRow(selectedTabIndex = uiState.selectedTab) {
            Tab(
                selected = uiState.selectedTab == 0,
                onClick = { viewModel.onTabSelected(0) },
                text = { Text("Bộ sưu tập (${uiState.savedCards.size})") }
            )
            Tab(
                selected = uiState.selectedTab == 1,
                onClick = { viewModel.onTabSelected(1) },
                text = { Text("Tìm kiếm") }
            )
        }

        when (uiState.selectedTab) {
            0 -> CollectionTab(
                cards = uiState.savedCards,
                onSpeak = { word -> tts?.speak(word, TextToSpeech.QUEUE_FLUSH, null, null) },
                onUpdateLevel = viewModel::updateMemorizationLevel,
                onEdit = viewModel::startEdit,
                onDelete = viewModel::requestDelete
            )
            1 -> SearchTab(
                uiState = uiState,
                onQueryChange = viewModel::onSearchQueryChange,
                onSearch = {
                    focusManager.clearFocus()
                    viewModel.searchWord()
                },
                onViMeaningChange = viewModel::onViMeaningChange,
                onIpaChange = viewModel::onIpaChange,
                onCustomImageUrlChange = viewModel::onCustomImageUrlChange,
                onAddManualExample = viewModel::addManualExample,
                onRemoveManualExample = viewModel::removeManualExample,
                onSave = viewModel::saveVocabularyCard,
                onSpeak = { word -> tts?.speak(word, TextToSpeech.QUEUE_FLUSH, null, null) },
                onSuggestionClick = { word ->
                    viewModel.onSearchQueryChange(word)
                    focusManager.clearFocus()
                    viewModel.searchWord()
                }
            )
        }
    }

    // Edit dialog
    uiState.editingCard?.let { card ->
        var editMeaning by remember(card.id) { mutableStateOf(card.meaning) }
        var editIpa by remember(card.id) { mutableStateOf(card.ipa) }
        var editCustomImageUrl by remember(card.id) { mutableStateOf(card.imageUrl) }
        var editExamples by remember(card.id) { mutableStateOf(card.examples) }
        var newExampleEn by remember(card.id) { mutableStateOf("") }
        var newExampleVi by remember(card.id) { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { viewModel.cancelEdit() },
            title = { Text("Sửa: ${card.word}") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
                ) {
                    OutlinedTextField(
                        value = editMeaning,
                        onValueChange = { editMeaning = it },
                        label = { Text("Nghĩa tiếng Việt") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editIpa,
                        onValueChange = { editIpa = it },
                        label = { Text("IPA") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    HorizontalDivider()
                    Text(
                        "Hình ảnh",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    if (editCustomImageUrl.isNotEmpty()) {
                        WordFlashAsyncImage(
                            url = editCustomImageUrl,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }

                    OutlinedTextField(
                        value = editCustomImageUrl,
                        onValueChange = { editCustomImageUrl = it },
                        label = { Text("URL ảnh (tuỳ chọn)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        trailingIcon = {
                            if (editCustomImageUrl.isNotBlank()) {
                                IconButton(onClick = { editCustomImageUrl = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Xoá", modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    )

                    HorizontalDivider()
                    Text(
                        "Câu ví dụ",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    editExamples.forEachIndexed { index, example ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        example.enSentence,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontStyle = FontStyle.Italic
                                    )
                                    if (example.viSentence.isNotEmpty()) {
                                        Text(
                                            example.viSentence,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                IconButton(
                                    onClick = { editExamples = editExamples.toMutableList().also { it.removeAt(index) } },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Xoá", modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                    OutlinedTextField(
                        value = newExampleEn,
                        onValueChange = { newExampleEn = it },
                        label = { Text("Câu tiếng Anh") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                    )
                    OutlinedTextField(
                        value = newExampleVi,
                        onValueChange = { newExampleVi = it },
                        label = { Text("Nghĩa tiếng Việt (tuỳ chọn)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            if (newExampleEn.isNotBlank()) {
                                editExamples = editExamples + Example(newExampleEn.trim(), newExampleVi.trim())
                                newExampleEn = ""
                                newExampleVi = ""
                            }
                        })
                    )
                    Button(
                        onClick = {
                            if (newExampleEn.isNotBlank()) {
                                editExamples = editExamples + Example(newExampleEn.trim(), newExampleVi.trim())
                                newExampleEn = ""
                                newExampleVi = ""
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = newExampleEn.isNotBlank()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Thêm ví dụ")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.saveEdit(card.copy(meaning = editMeaning, ipa = editIpa, imageUrl = editCustomImageUrl.trim(), examples = editExamples))
                }) {
                    Text("Lưu")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cancelEdit() }) { Text("Huỷ") }
            }
        )
    }

    // Delete confirm dialog
    if (uiState.deleteConfirmId != null) {
        AlertDialog(
            onDismissRequest = viewModel::cancelDelete,
            title = { Text("Xác nhận xoá") },
            text = { Text("Bạn chắc chắn muốn xoá từ này?") },
            confirmButton = {
                TextButton(onClick = viewModel::confirmDelete) { Text("Xoá") }
            },
            dismissButton = {
                TextButton(onClick = viewModel::cancelDelete) { Text("Huỷ") }
            }
        )
    }

    // Save/edit/delete error dialog
    uiState.saveError?.let { error ->
        AlertDialog(
            onDismissRequest = viewModel::clearSaveError,
            title = { Text("Lỗi") },
            text = { Text(error) },
            confirmButton = { TextButton(onClick = viewModel::clearSaveError) { Text("OK") } }
        )
    }

    // Sync error dialog
    syncUiState.syncError?.let { error ->
        AlertDialog(
            onDismissRequest = syncViewModel::clearError,
            title = { Text("Lỗi đồng bộ") },
            text = { Text(error) },
            confirmButton = { TextButton(onClick = syncViewModel::clearError) { Text("OK") } }
        )
    }

    // Sync result dialog
    syncUiState.syncResult?.let { result ->
        AlertDialog(
            onDismissRequest = syncViewModel::clearSyncResult,
            title = { Text("Đồng bộ thành công") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Từ vựng: +${result.vocabAdded} mới, cập nhật ${result.vocabUpdated}")
                    Text("Câu: +${result.sentenceAdded} mới, cập nhật ${result.sentenceUpdated}")
                }
            },
            confirmButton = {
                TextButton(onClick = syncViewModel::clearSyncResult) { Text("OK") }
            }
        )
    }
}

@Composable
private fun SearchTab(
    uiState: VocabularyUiState,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onViMeaningChange: (String) -> Unit,
    onIpaChange: (String) -> Unit,
    onCustomImageUrlChange: (String) -> Unit,
    onAddManualExample: (Example) -> Unit,
    onRemoveManualExample: (Int) -> Unit,
    onSave: () -> Unit,
    onSpeak: (String) -> Unit,
    onSuggestionClick: (String) -> Unit
) {
    var newExampleEn by remember { mutableStateOf("") }
    var newExampleVi by remember { mutableStateOf("") }
    LaunchedEffect(uiState.dictionaryEntry?.word) {
        newExampleEn = ""
        newExampleVi = ""
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = onQueryChange,
                label = { Text("Nhập từ tiếng Anh...") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onSearch() }),
                trailingIcon = {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    } else {
                        IconButton(onClick = onSearch) {
                            Icon(Icons.Default.Search, contentDescription = "Tìm kiếm")
                        }
                    }
                }
            )
        }

        uiState.error?.let { error ->
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = error, color = MaterialTheme.colorScheme.onErrorContainer)
                        if (uiState.suggestions.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Có thể bạn muốn tìm:",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                uiState.suggestions.forEach { suggestion ->
                                    SuggestionChip(
                                        onClick = { onSuggestionClick(suggestion) },
                                        label = { Text(suggestion) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        uiState.dictionaryEntry?.let { entry ->
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = entry.word,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            if (entry.audioUrl.isNotEmpty() || entry.word.isNotEmpty()) {
                                IconButton(onClick = { onSpeak(entry.word) }) {
                                    Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Phát âm")
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = uiState.ipaInput,
                            onValueChange = onIpaChange,
                            label = { Text("IPA (tuỳ chọn)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            placeholder = { Text("/.../ ") }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        entry.meanings.forEach { meaning ->
                            WordMeaningSection(meaning)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = uiState.viMeaning,
                            onValueChange = onViMeaningChange,
                            label = { Text("Nghĩa tiếng Việt (tuỳ chọn)") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2,
                            enabled = !uiState.isLoadingGeminiInfo,
                            trailingIcon = {
                                if (uiState.isLoadingGeminiInfo) {
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "Ảnh minh hoạ",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = uiState.customImageUrl,
                            onValueChange = onCustomImageUrlChange,
                            label = { Text("Nhập URL ảnh (tuỳ chọn)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        if (uiState.customImageUrl.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            WordFlashAsyncImage(
                                url = uiState.customImageUrl,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "Câu ví dụ",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        if (uiState.isLoadingGeminiInfo) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp))
                                Text("Đang tải câu ví dụ từ Gemini...", style = MaterialTheme.typography.bodySmall)
                            }
                        } else {
                            uiState.dictionaryExamples.forEach { example ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text(
                                            "• ${example.enSentence}",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontStyle = FontStyle.Italic
                                        )
                                        if (example.viSentence.isNotEmpty()) {
                                            Text(
                                                "  ${example.viSentence}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                        }
                        uiState.manualExamples.forEachIndexed { index, example ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            example.enSentence,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontStyle = FontStyle.Italic
                                        )
                                        if (example.viSentence.isNotEmpty()) {
                                            Text(
                                                example.viSentence,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSecondaryContainer
                                            )
                                        }
                                    }
                                    IconButton(
                                        onClick = { onRemoveManualExample(index) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = "Xoá", modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                        OutlinedTextField(
                            value = newExampleEn,
                            onValueChange = { newExampleEn = it },
                            label = { Text("Câu ví dụ tiếng Anh") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                        )
                        OutlinedTextField(
                            value = newExampleVi,
                            onValueChange = { newExampleVi = it },
                            label = { Text("Dịch tiếng Việt (tuỳ chọn)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = {
                                if (newExampleEn.isNotBlank()) {
                                    onAddManualExample(Example(newExampleEn.trim(), newExampleVi.trim()))
                                    newExampleEn = ""
                                    newExampleVi = ""
                                }
                            })
                        )
                        Button(
                            onClick = {
                                if (newExampleEn.isNotBlank()) {
                                    onAddManualExample(Example(newExampleEn.trim(), newExampleVi.trim()))
                                    newExampleEn = ""
                                    newExampleVi = ""
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = newExampleEn.isNotBlank()
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Thêm câu ví dụ")
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                if (newExampleEn.isNotBlank()) {
                                    onAddManualExample(Example(newExampleEn.trim(), newExampleVi.trim()))
                                    newExampleEn = ""
                                    newExampleVi = ""
                                }
                                onSave()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !uiState.isSaved
                        ) {
                            if (uiState.isSaved) {
                                Icon(Icons.Default.Check, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Đã lưu")
                            } else {
                                Text("Thêm Flashcard")
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
private fun WordMeaningSection(meaning: WordMeaning) {
    Column {
        Text(
            text = meaning.partOfSpeech,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.secondary,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(4.dp))
        meaning.definitions.take(3).forEachIndexed { index, definition ->
            Text(
                text = "${index + 1}. ${definition.definition}",
                style = MaterialTheme.typography.bodyMedium
            )
            if (definition.example.isNotEmpty()) {
                Text(
                    text = "  \"${definition.example}\"",
                    style = MaterialTheme.typography.bodySmall,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
        }
    }
}

@Composable
private fun CollectionTab(
    cards: List<VocabularyCard>,
    onSpeak: (String) -> Unit,
    onUpdateLevel: (String, Int) -> Unit,
    onEdit: (VocabularyCard) -> Unit,
    onDelete: (String) -> Unit
) {
    if (cards.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = "Bộ sưu tập trống.\nHãy tìm kiếm và lưu từ vựng!",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(cards, key = { it.id }) { card ->
            VocabularyCardItem(
                card = card,
                onSpeak = onSpeak,
                onUpdateLevel = onUpdateLevel,
                onEdit = onEdit,
                onDelete = onDelete
            )
        }
    }
}

@Composable
private fun VocabularyCardItem(
    card: VocabularyCard,
    onSpeak: (String) -> Unit,
    onUpdateLevel: (String, Int) -> Unit,
    onEdit: (VocabularyCard) -> Unit,
    onDelete: (String) -> Unit
) {
    val levelColors = listOf(
        MaterialTheme.colorScheme.errorContainer,
        MaterialTheme.colorScheme.tertiaryContainer,
        MaterialTheme.colorScheme.primaryContainer
    )
    val levelLabels = listOf("Không nhớ", "Hơi nhớ", "Đã nhớ")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 2.dp,
                color = levelColors.getOrElse(card.memorizationLevel) { MaterialTheme.colorScheme.outline },
                shape = MaterialTheme.shapes.medium
            )
    ) {
        Column(modifier = Modifier.padding(0.dp)) {
            if (card.imageUrl.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp, start = 12.dp, end = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    WordFlashAsyncImage(
                        url = card.imageUrl,
                        modifier = Modifier
                            .fillMaxWidth(0.67f)
                            .aspectRatio(4f / 3f)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Fit
                    )
                }
            }
            Column(
                modifier = Modifier
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = card.word,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { onSpeak(card.word) }) {
                        Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Phát âm", modifier = Modifier.size(20.dp))
                    }
                    IconButton(onClick = { onEdit(card) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Sửa", modifier = Modifier.size(20.dp))
                    }
                    IconButton(onClick = { onDelete(card.id) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Xóa", modifier = Modifier.size(20.dp))
                    }
                }
                if (card.ipa.isNotEmpty()) {
                    Text(
                        text = card.ipa,
                        style = MaterialTheme.typography.bodySmall,
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (card.meaning.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = card.meaning, style = MaterialTheme.typography.bodyMedium)
                }
                if (card.examples.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(4.dp))
                    card.examples.take(3).forEach { example ->
                        Text(
                            "• ${example.enSentence}",
                            style = MaterialTheme.typography.bodySmall,
                            fontStyle = FontStyle.Italic,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (example.viSentence.isNotEmpty()) {
                            Text(
                                "  ${example.viSentence}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                AssistChip(
                    onClick = { onUpdateLevel(card.id, (card.memorizationLevel + 1) % 3) },
                    label = { Text(levelLabels.getOrElse(card.memorizationLevel) { "Không nhớ" }, style = MaterialTheme.typography.labelSmall) }
                )
            }
        }
    }
}
