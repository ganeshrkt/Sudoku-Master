package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.TimeHistoryPoint
import java.util.Locale

@Composable
fun TimeHistoryGraph(
    historyPoints: List<TimeHistoryPoint>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Timeline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Time Score Trend",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                if (historyPoints.isNotEmpty()) {
                    val avgTime = historyPoints.map { it.timeSeconds }.average().toInt()
                    Text(
                        text = "Avg: ${formatSeconds(avgTime)}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (historyPoints.isEmpty()) {
                EmptyGraphState(modifier = Modifier.height(180.dp))
            } else {
                GraphCanvasWithTooltip(
                    points = historyPoints,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                )
            }
        }
    }
}

@Composable
private fun GraphCanvasWithTooltip(
    points: List<TimeHistoryPoint>,
    modifier: Modifier = Modifier
) {
    var selectedPointIndex by remember { mutableStateOf<Int?>(null) }
    val primaryColor = MaterialTheme.colorScheme.primary
    val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
    val avgTime = points.map { it.timeSeconds }.average().toFloat()

    val minTime = points.minOf { it.timeSeconds }
    val maxTime = points.maxOf { it.timeSeconds }
    val timeRange = (maxTime - minTime).coerceAtLeast(60) // at least 1 min spread
    val paddedMin = (minTime - (timeRange * 0.1f)).coerceAtLeast(0f)
    val paddedMax = maxTime + (timeRange * 0.15f)

    Column(modifier = modifier) {
        // Selected Tooltip banner
        val selectedPoint = selectedPointIndex?.let { if (it in points.indices) points[it] else null }
        if (selectedPoint != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp)
                    .background(
                        MaterialTheme.colorScheme.primaryContainer,
                        RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${selectedPoint.difficulty.displayName} • ${selectedPoint.dateLabel}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Time: ${formatSeconds(selectedPoint.timeSeconds)}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold
                )
            }
        } else {
            Text(
                text = "Tap any dot to view game details",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(points) {
                        detectTapGestures { tapOffset ->
                            val n = points.size
                            if (n <= 1) {
                                selectedPointIndex = 0
                                return@detectTapGestures
                            }
                            val stepX = size.width / (n - 1)
                            val clickedIndex = ((tapOffset.x + (stepX / 2)) / stepX).toInt().coerceIn(0, n - 1)
                            selectedPointIndex = clickedIndex
                        }
                    }
            ) {
                val width = size.width
                val height = size.height
                val n = points.size

                if (n == 0) return@Canvas

                // Draw horizontal grid lines (3 lines)
                for (i in 0..2) {
                    val y = height * (i / 2f)
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, y),
                        end = Offset(width, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                // Average guideline (dotted)
                val avgY = height - ((avgTime - paddedMin) / (paddedMax - paddedMin)) * height
                if (avgY in 0f..height) {
                    drawLine(
                        color = primaryColor.copy(alpha = 0.4f),
                        start = Offset(0f, avgY),
                        end = Offset(width, avgY),
                        strokeWidth = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                    )
                }

                if (n == 1) {
                    // Single point in center
                    val pt = points[0]
                    val x = width / 2f
                    val y = height / 2f
                    drawCircle(color = pt.difficulty.color, radius = 6.dp.toPx(), center = Offset(x, y))
                    return@Canvas
                }

                val stepX = width / (n - 1)
                val coords = points.mapIndexed { index, point ->
                    val x = index * stepX
                    val yFraction = ((point.timeSeconds - paddedMin) / (paddedMax - paddedMin)).coerceIn(0f, 1f)
                    val y = height - (yFraction * height)
                    Offset(x, y)
                }

                // Fill gradient under curve
                val fillPath = Path().apply {
                    moveTo(coords.first().x, height)
                    for (c in coords) {
                        lineTo(c.x, c.y)
                    }
                    lineTo(coords.last().x, height)
                    close()
                }

                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            primaryColor.copy(alpha = 0.35f),
                            primaryColor.copy(alpha = 0.02f)
                        )
                    )
                )

                // Line path
                val strokePath = Path().apply {
                    moveTo(coords.first().x, coords.first().y)
                    for (i in 1 until coords.size) {
                        lineTo(coords[i].x, coords[i].y)
                    }
                }

                drawPath(
                    path = strokePath,
                    color = primaryColor,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )

                // Draw circles for data points
                coords.forEachIndexed { i, offset ->
                    val pt = points[i]
                    val isSelected = selectedPointIndex == i
                    val radius = if (isSelected) 8.dp.toPx() else 4.5.dp.toPx()

                    drawCircle(
                        color = pt.difficulty.color,
                        radius = radius,
                        center = offset
                    )
                    drawCircle(
                        color = Color.White,
                        radius = (radius - 2.dp.toPx()).coerceAtLeast(1f),
                        center = offset
                    )
                    drawCircle(
                        color = pt.difficulty.color,
                        radius = (radius - 3.5.dp.toPx()).coerceAtLeast(1f),
                        center = offset
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyGraphState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Timeline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "No Games Completed Yet",
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Solve a puzzle to begin tracking your speed over time!",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

fun formatSeconds(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return String.format(Locale.getDefault(), "%02d:%02d", m, s)
}
