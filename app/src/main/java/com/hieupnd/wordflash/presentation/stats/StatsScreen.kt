package com.hieupnd.wordflash.presentation.stats

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hieupnd.wordflash.domain.model.DailyStats
import com.hieupnd.wordflash.presentation.components.StreakCard
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    innerPadding: PaddingValues,
    viewModel: StatsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.loadStats() }

    Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
        TopAppBar(title = { Text("Thống kê") })

        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Column
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StreakCard(
                currentStreak = uiState.currentStreak,
                longestStreak = uiState.longestStreak
            )

            if (uiState.dailyStats.isNotEmpty()) {
                ActivityChartCard(stats = uiState.dailyStats)
            }
        }
    }
}

@Composable
private fun ActivityChartCard(stats: List<DailyStats>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
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
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(text = label, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun BarChart(stats: List<DailyStats>) {
    val maxValue = stats.maxOf { maxOf(it.vocabAdded, it.sentencesAdded, it.reviewCount) }.coerceAtLeast(1)
    val dayFormatter = DateTimeFormatter.ofPattern("dd/MM")
    val today = LocalDate.now()

    val barColor1 = colorVocab
    val barColor2 = colorSentence
    val barColor3 = colorReview
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
        ) {
            val chartWidth = size.width
            val chartHeight = size.height
            val slotWidth = chartWidth / stats.size
            val barGroupWidth = slotWidth * 0.7f
            val barWidth = barGroupWidth / 3f
            val gap = slotWidth * 0.15f

            // Gridlines
            val gridLines = 4
            for (i in 0..gridLines) {
                val y = chartHeight - (i.toFloat() / gridLines) * chartHeight
                drawLine(
                    color = surfaceVariant,
                    start = Offset(0f, y),
                    end = Offset(chartWidth, y),
                    strokeWidth = 1.dp.toPx()
                )
            }

            stats.forEachIndexed { index, day ->
                val slotLeft = index * slotWidth + gap
                val vocabH = (day.vocabAdded.toFloat() / maxValue) * chartHeight
                val sentenceH = (day.sentencesAdded.toFloat() / maxValue) * chartHeight
                val reviewH = (day.reviewCount.toFloat() / maxValue) * chartHeight

                drawRect(
                    color = barColor1,
                    topLeft = Offset(slotLeft, chartHeight - vocabH),
                    size = Size(barWidth, vocabH.coerceAtLeast(2.dp.toPx()))
                )
                drawRect(
                    color = barColor2,
                    topLeft = Offset(slotLeft + barWidth, chartHeight - sentenceH),
                    size = Size(barWidth, sentenceH.coerceAtLeast(2.dp.toPx()))
                )
                drawRect(
                    color = barColor3,
                    topLeft = Offset(slotLeft + barWidth * 2, chartHeight - reviewH),
                    size = Size(barWidth, reviewH.coerceAtLeast(2.dp.toPx()))
                )
            }
        }

        // X-axis date labels
        Row(modifier = Modifier.fillMaxWidth()) {
            stats.forEach { day ->
                val label = if (day.date == today) "Hôm nay" else day.date.format(dayFormatter)
                Text(
                    text = label,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 9.sp,
                    color = if (day.date == today)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
