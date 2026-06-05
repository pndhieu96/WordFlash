package com.hieupnd.wordflash.presentation.sentence

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.border
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.remember
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hieupnd.wordflash.domain.model.Example
import com.hieupnd.wordflash.domain.model.SentenceCard
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SentenceScreen(
    innerPadding: PaddingValues,
    onNavigateToSettings: () -> Unit = {},
    viewModel: SentenceViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            delay(1500)
            viewModel.clearSaveSuccess()
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
        TopAppBar(
            title = { Text("Cấu Trúc Câu") },
            actions = {
                IconButton(onClick = onNavigateToSettings) {
                    Icon(Icons.Default.Settings, contentDescription = "Cài đặt")
                }
            }
        )

        TabRow(selectedTabIndex = uiState.selectedTab) {
            Tab(
                selected = uiState.selectedTab == 0,
                onClick = { viewModel.onTabSelected(0) },
                text = { Text("Bộ sưu tập (${uiState.savedCards.size})") }
            )
            Tab(
                selected = uiState.selectedTab == 1,
                onClick = { viewModel.onTabSelected(1) },
                text = { Text("Tạo cấu trúc") }
            )
        }

        when (uiState.selectedTab) {
            0 -> SentenceCollectionTab(
                cards = uiState.savedCards,
                collectionQuery = uiState.collectionQuery,
                onCollectionQueryChange = viewModel::onCollectionQueryChange,
                onUpdateLevel = viewModel::updateMemorizationLevel,
                onEdit = viewModel::startEdit,
                onDelete = viewModel::requestDelete
            )
            1 -> CreateSentenceTab(uiState = uiState, viewModel = viewModel)
        }
    }

    // Edit dialog
    uiState.editingCard?.let { card ->
        var editSentence by remember(card.id) { mutableStateOf(card.sentence) }
        var editDescription by remember(card.id) { mutableStateOf(card.description) }
        var editExamples by remember(card.id) { mutableStateOf(card.relatedExamples) }
        var newExampleInput by remember(card.id) { mutableStateOf("") }
        var newExampleViInput by remember(card.id) { mutableStateOf("") }
        val scrollState = rememberScrollState()
        AlertDialog(
            onDismissRequest = viewModel::cancelEdit,
            title = { Text("Sửa cấu trúc") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth().verticalScroll(scrollState)
                ) {
                    OutlinedTextField(
                        value = editSentence,
                        onValueChange = { editSentence = it },
                        label = { Text("Cấu trúc câu") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = editDescription,
                        onValueChange = { editDescription = it },
                        label = { Text("Mô tả / ghi chú") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                    HorizontalDivider()
                    Text(
                        "Ví dụ liên quan",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (editExamples.isNotEmpty()) {
                        editExamples.forEachIndexed { index, example ->
                            Row(
                                verticalAlignment = Alignment.Top,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
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
                        value = newExampleInput,
                        onValueChange = { newExampleInput = it },
                        label = { Text("Câu tiếng Anh") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = newExampleViInput,
                            onValueChange = { newExampleViInput = it },
                            label = { Text("Nghĩa tiếng Việt (tuỳ chọn)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = {
                                if (newExampleInput.isNotBlank()) {
                                    editExamples = editExamples + Example(newExampleInput.trim(), newExampleViInput.trim())
                                    newExampleInput = ""
                                    newExampleViInput = ""
                                }
                            })
                        )
                        IconButton(onClick = {
                            if (newExampleInput.isNotBlank()) {
                                editExamples = editExamples + Example(newExampleInput.trim(), newExampleViInput.trim())
                                newExampleInput = ""
                                newExampleViInput = ""
                            }
                        }) {
                            Icon(Icons.Default.Add, contentDescription = "Thêm ví dụ")
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.saveEdit(card.copy(
                        sentence = editSentence.trim().ifEmpty { card.sentence },
                        description = editDescription,
                        relatedExamples = editExamples
                    ))
                }) {
                    Text("Lưu")
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::cancelEdit) { Text("Huỷ") }
            }
        )
    }

    // Delete confirm dialog
    if (uiState.deleteConfirmId != null) {
        AlertDialog(
            onDismissRequest = viewModel::cancelDelete,
            title = { Text("Xác nhận xoá") },
            text = { Text("Bạn chắc chắn muốn xoá cấu trúc câu này?") },
            confirmButton = {
                TextButton(onClick = viewModel::confirmDelete) { Text("Xoá") }
            },
            dismissButton = {
                TextButton(onClick = viewModel::cancelDelete) { Text("Huỷ") }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CreateSentenceTab(uiState: SentenceUiState, viewModel: SentenceViewModel) {
    val focusManager = LocalFocusManager.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                "Chọn thành phần câu",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                "Chọn tab → nhấn chip để xem mô tả → nhấn + để thêm vào cấu trúc",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            TabRow(selectedTabIndex = uiState.selectedComponentTab) {
                Tab(
                    selected = uiState.selectedComponentTab == 0,
                    onClick = { viewModel.onComponentTabSelected(0) },
                    text = { Text("Loại từ") }
                )
                Tab(
                    selected = uiState.selectedComponentTab == 1,
                    onClick = { viewModel.onComponentTabSelected(1) },
                    text = { Text("Thành phần") }
                )
                Tab(
                    selected = uiState.selectedComponentTab == 2,
                    onClick = { viewModel.onComponentTabSelected(2) },
                    text = { Text("Tùy chỉnh") }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            when (uiState.selectedComponentTab) {
                0 -> WordTypeTabContent(
                    focusedKey = uiState.focusedWordType,
                    onFocus = viewModel::onWordTypeFocused,
                    onAdd = viewModel::addWordType
                )
                1 -> SentenceRoleTabContent(
                    focusedKey = uiState.focusedSentenceRole,
                    onFocus = viewModel::onSentenceRoleFocused,
                    onAdd = viewModel::addSentenceRole
                )
                2 -> CustomTabContent(
                    nameInput = uiState.customInputName,
                    descInput = uiState.customInputDesc,
                    onNameChange = viewModel::onCustomInputNameChange,
                    onDescChange = viewModel::onCustomInputDescChange,
                    onAdd = viewModel::addCustomItem
                )
            }
        }

        item { HorizontalDivider() }

        item {
            Text(
                "Cấu trúc đang xây dựng",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (uiState.structureItems.isEmpty()) {
                Text(
                    "Chưa có thành phần nào. Chọn từ các tab bên trên để thêm.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontStyle = FontStyle.Italic
                )
            } else {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    uiState.structureItems.forEachIndexed { index, item ->
                        InputChip(
                            selected = false,
                            onClick = {},
                            label = {
                                Text(if (item.viName.isNotEmpty()) "${item.displayName} (${item.viName})" else item.displayName)
                            },
                            trailingIcon = {
                                IconButton(
                                    onClick = { viewModel.removeStructureItemAt(index) },
                                    modifier = Modifier.size(16.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Xoá", modifier = Modifier.size(12.dp))
                                }
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Text(
                        text = uiState.sentence,
                        modifier = Modifier.padding(10.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        item { HorizontalDivider() }

        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Mô tả & ví dụ", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                if (uiState.structureItems.isNotEmpty()) {
                    if (uiState.isLoadingGemini) {
                        androidx.compose.material3.CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    } else {
                        TextButton(onClick = viewModel::generateFromGemini) {
                            Text("Tự động điền từ Gemini")
                        }
                    }
                }
            }
            uiState.geminiError?.let { error ->
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = uiState.description,
                onValueChange = viewModel::onDescriptionChange,
                label = { Text("Mô tả cách dùng & ghi chú") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )
        }

        item {
            Text("Ví dụ liên quan", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = uiState.newExample,
                onValueChange = viewModel::onNewExampleChange,
                label = { Text("Câu tiếng Anh") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = uiState.newExampleVi,
                    onValueChange = viewModel::onNewExampleViChange,
                    label = { Text("Nghĩa tiếng Việt (tuỳ chọn)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        viewModel.addExample()
                        focusManager.clearFocus()
                    })
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = { viewModel.addExample(); focusManager.clearFocus() }) {
                    Icon(Icons.Default.Add, contentDescription = "Thêm ví dụ")
                }
            }
        }

        itemsIndexed(uiState.relatedExamples) { index, example ->
            Row(verticalAlignment = Alignment.Top, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("• ${example.enSentence}", style = MaterialTheme.typography.bodyMedium, fontStyle = FontStyle.Italic)
                    if (example.viSentence.isNotEmpty()) {
                        Text("  ${example.viSentence}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                IconButton(onClick = { viewModel.removeExampleAt(index) }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Xóa", modifier = Modifier.size(16.dp))
                }
            }
        }

        uiState.error?.let { error ->
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Text(error, modifier = Modifier.padding(12.dp), color = MaterialTheme.colorScheme.onErrorContainer)
                }
            }
        }

        item {
            Button(
                onClick = viewModel::saveSentence,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (uiState.saveSuccess) "Đã lưu!" else "Lưu cấu trúc")
            }
        }

        item { Spacer(modifier = Modifier.height(8.dp)) }
    }
}

@Composable
private fun SentenceCollectionTab(
    cards: List<SentenceCard>,
    collectionQuery: String,
    onCollectionQueryChange: (String) -> Unit,
    onUpdateLevel: (String, Int) -> Unit,
    onEdit: (SentenceCard) -> Unit,
    onDelete: (String) -> Unit
) {
    val filteredCards = remember(cards, collectionQuery) {
        if (collectionQuery.isBlank()) cards
        else cards.filter { card ->
            card.sentence.contains(collectionQuery, ignoreCase = true) ||
                card.description.contains(collectionQuery, ignoreCase = true)
        }
    }

    val levelLabels = listOf("Không nhớ", "Hơi nhớ", "Đã nhớ")
    val levelColors = listOf(
        MaterialTheme.colorScheme.errorContainer,
        MaterialTheme.colorScheme.tertiaryContainer,
        MaterialTheme.colorScheme.primaryContainer
    )

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = collectionQuery,
            onValueChange = onCollectionQueryChange,
            placeholder = { Text("Tìm trong bộ sưu tập...") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (collectionQuery.isNotEmpty()) {
                    IconButton(onClick = { onCollectionQueryChange("") }) {
                        Icon(Icons.Default.Close, contentDescription = "Xoá", modifier = Modifier.size(16.dp))
                    }
                }
            }
        )

        if (cards.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "Chưa có cấu trúc nào.\nHãy tạo ở tab Tạo cấu trúc!",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            return@Column
        }

        if (filteredCards.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "Không tìm thấy cấu trúc nào phù hợp.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            return@Column
        }

    LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(filteredCards, key = { it.id }) { card ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 2.dp,
                        color = levelColors.getOrElse(card.memorizationLevel) { MaterialTheme.colorScheme.outline },
                        shape = MaterialTheme.shapes.medium
                    )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = card.sentence,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { onEdit(card) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Sửa", modifier = Modifier.size(20.dp))
                        }
                        IconButton(onClick = { onDelete(card.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Xóa", modifier = Modifier.size(20.dp))
                        }
                    }
                    if (card.description.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = card.description, style = MaterialTheme.typography.bodySmall)
                    }
                    if (card.relatedExamples.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        card.relatedExamples.forEach { ex ->
                            Text("• ${ex.enSentence}", style = MaterialTheme.typography.bodySmall, fontStyle = FontStyle.Italic, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            if (ex.viSentence.isNotEmpty()) {
                                Text("  ${ex.viSentence}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WordTypeTabContent(
    focusedKey: String?,
    onFocus: (String) -> Unit,
    onAdd: (String) -> Unit
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        EnglishWordTypes.ALL.forEach { type ->
            FilterChip(
                selected = focusedKey == type.key,
                onClick = { onFocus(type.key) },
                label = { Text(type.enName) }
            )
        }
    }
    AnimatedVisibility(visible = focusedKey != null) {
        focusedKey?.let { key ->
            EnglishWordTypes.ALL.find { it.key == key }?.let { type ->
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    type.enName,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "(${type.viName})",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                type.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                type.positionNote,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary,
                                fontStyle = FontStyle.Italic
                            )
                        }
                        IconButton(onClick = { onAdd(key) }) {
                            Icon(Icons.Default.Add, contentDescription = "Thêm")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SentenceRoleTabContent(
    focusedKey: String?,
    onFocus: (String) -> Unit,
    onAdd: (String) -> Unit
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        EnglishSentenceRoles.ALL.forEach { role ->
            FilterChip(
                selected = focusedKey == role.key,
                onClick = { onFocus(role.key) },
                label = { Text(role.enName) }
            )
        }
    }
    AnimatedVisibility(visible = focusedKey != null) {
        focusedKey?.let { key ->
            EnglishSentenceRoles.ALL.find { it.key == key }?.let { role ->
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    role.enName,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "(${role.viName})",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                role.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = { onAdd(key) }) {
                            Icon(Icons.Default.Add, contentDescription = "Thêm")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CustomTabContent(
    nameInput: String,
    descInput: String,
    onNameChange: (String) -> Unit,
    onDescChange: (String) -> Unit,
    onAdd: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = nameInput,
            onValueChange = onNameChange,
            label = { Text("Tên thành phần") },
            placeholder = { Text("VD: Phrasal Verb, Cụm danh từ...") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
        )
        OutlinedTextField(
            value = descInput,
            onValueChange = onDescChange,
            label = { Text("Mô tả (tuỳ chọn)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                onAdd()
                focusManager.clearFocus()
            })
        )
        Button(
            onClick = {
                onAdd()
                focusManager.clearFocus()
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = nameInput.isNotBlank()
        ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Thêm vào cấu trúc")
        }
    }
}
