package com.hieupnd.wordflash.presentation.review

import android.speech.tts.TextToSpeech
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.foundation.layout.size
import androidx.compose.ui.layout.ContentScale
import com.hieupnd.wordflash.presentation.components.WordFlashAsyncImage
import com.hieupnd.wordflash.presentation.components.contentColorFor as levelContentColorFor
import com.hieupnd.wordflash.presentation.components.memorizationColors
import com.hieupnd.wordflash.presentation.components.memorizationLabelRes
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hieupnd.wordflash.domain.model.ReviewItem
import java.util.Locale
import androidx.compose.ui.res.stringResource
import com.hieupnd.wordflash.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewScreen(
    innerPadding: PaddingValues,
    onNavigateToSettings: () -> Unit = {},
    viewModel: ReviewViewModel = hiltViewModel(LocalActivity.current as ComponentActivity)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

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
            title = { Text(stringResource(R.string.nav_review)) },
            actions = {
                if (uiState.hasStudiedToday) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = stringResource(R.string.review_studied_today),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = 4.dp).size(24.dp)
                    )
                }
                IconButton(onClick = viewModel::restartSession) {
                    Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.review_restart))
                }
                IconButton(onClick = onNavigateToSettings) {
                    Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.title_settings))
                }
            }
        )

        when {
            uiState.isComplete -> CompletionScreen(
                hasStudiedToday = uiState.hasStudiedToday,
                onRestart = viewModel::restartSession
            )
            uiState.reviewItems.isEmpty() -> EmptyReviewScreen()
            else -> ReviewContent(
                uiState = uiState,
                onFlip = viewModel::flipCard,
                onRate = viewModel::rateCard,
                onSpeak = { word -> tts?.speak(word, TextToSpeech.QUEUE_FLUSH, null, null) }
            )
        }
    }
}

@Composable
private fun ReviewContent(
    uiState: ReviewUiState,
    onFlip: () -> Unit,
    onRate: (Int) -> Unit,
    onSpeak: (String) -> Unit
) {
    val current = uiState.currentItem ?: return

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LinearProgressIndicator(
            progress = { uiState.progress },
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = "${uiState.currentIndex + 1} / ${uiState.totalItems}",
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(top = 4.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))

        val density = LocalDensity.current
        val rotation by animateFloatAsState(
            targetValue = if (uiState.isFlipped) 180f else 0f,
            animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
            label = "cardFlip"
        )
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(340.dp)
                .clickable { onFlip() }
                .graphicsLayer {
                    rotationY = rotation
                    cameraDistance = 8 * density.density
                },
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                if (rotation <= 90f) {
                    CardFront(item = current, onSpeak = onSpeak)
                } else {
                    Box(modifier = Modifier.graphicsLayer { rotationY = 180f }) {
                        CardBack(item = current)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = if (!uiState.isFlipped) stringResource(R.string.review_tap_to_reveal) else stringResource(R.string.review_rate_prompt),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (uiState.isFlipped) {
            RatingButtons(onRate = onRate)
        }
    }
}

@Composable
private fun CardFront(item: ReviewItem, onSpeak: (String) -> Unit) {
    val scrollState = rememberScrollState()
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        when (item) {
            is ReviewItem.VocabItem -> {
                if (item.card.imageUrl.isNotEmpty()) {
                    WordFlashAsyncImage(
                        url = item.card.imageUrl,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .padding(8.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Fit
                    )
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = item.card.word,
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(onClick = { onSpeak(item.card.word) }) {
                            Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = stringResource(R.string.action_pronounce))
                        }
                    }
                    if (item.card.ipa.isNotEmpty()) {
                        Text(
                            text = item.card.ipa,
                            style = MaterialTheme.typography.titleMedium,
                            fontStyle = FontStyle.Italic,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            is ReviewItem.SentenceItem -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item.card.sentence,
                        style = MaterialTheme.typography.titleLarge,
                        fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun CardBack(item: ReviewItem) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        when (item) {
            is ReviewItem.VocabItem -> {
                Text(
                    text = item.card.meaning,
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.SemiBold
                )
                if (item.card.examples.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    item.card.examples.take(2).forEach { example ->
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "\"${example.enSentence}\"",
                            style = MaterialTheme.typography.bodySmall,
                            fontStyle = FontStyle.Italic,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        if (example.viSentence.isNotEmpty()) {
                            Text(
                                text = example.viSentence,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
            is ReviewItem.SentenceItem -> {
                if (item.card.description.isNotEmpty()) {
                    Text(
                        text = item.card.description,
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                if (item.card.relatedExamples.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    item.card.relatedExamples.take(2).forEach { ex ->
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "\"${ex.enSentence}\"",
                            style = MaterialTheme.typography.bodySmall,
                            fontStyle = FontStyle.Italic,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        if (ex.viSentence.isNotEmpty()) {
                            Text(
                                text = ex.viSentence,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RatingButtons(onRate: (Int) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        memorizationLabelRes.forEachIndexed { level, labelRes ->
            Button(
                onClick = { onRate(level) },
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 2.dp, vertical = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = memorizationColors[level],
                    contentColor = levelContentColorFor(level)
                )
            ) {
                Text(
                    text = stringResource(labelRes),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 2
                )
            }
        }
    }
}

@Composable
private fun CompletionScreen(hasStudiedToday: Boolean, onRestart: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
            if (hasStudiedToday) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            Text(
                text = stringResource(R.string.review_session_complete),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (hasStudiedToday) stringResource(R.string.review_done_today)
                       else stringResource(R.string.review_done_all),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onRestart) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.review_again))
            }
        }
    }
}

@Composable
private fun EmptyReviewScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = stringResource(R.string.review_no_cards),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(32.dp)
        )
    }
}
