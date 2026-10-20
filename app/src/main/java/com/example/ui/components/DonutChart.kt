package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CategorySpendSummary
import com.example.model.CurrencyConfig
import com.example.ui.theme.ChartBlue
import com.example.ui.theme.ChartCoral
import com.example.ui.theme.ChartGreen
import com.example.ui.theme.ChartOrange
import com.example.ui.theme.ChartPurple
import com.example.ui.theme.ChartStriped
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.math.BigDecimal

val ReferenceSegmentColors = listOf(
    ChartPurple,
    ChartBlue,
    ChartGreen,
    ChartOrange,
    ChartCoral,
    Color(0xFF9E77ED),
    Color(0xFF06AED4)
)

@Composable
fun ReferenceDonutChart(
    categories: List<CategorySpendSummary>,
    totalAmount: BigDecimal,
    currency: CurrencyConfig,
    isExpense: Boolean = true,
    modifier: Modifier = Modifier
) {
    val progress = remember { Animatable(0f) }

    LaunchedEffect(categories, totalAmount) {
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
        )
    }

    Box(
        modifier = modifier
            .size(260.dp)
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 34.dp.toPx()
            val diameter = size.minDimension - strokeWidth
            val arcSize = Size(diameter, diameter)
            val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)

            if (categories.isEmpty() || totalAmount <= BigDecimal.ZERO) {
                // Empty placeholder ring
                drawArc(
                    color = Color(0xFFE9ECF4),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth)
                )
            } else {
                var currentAngle = -90f
                val gapAngle = 6f
                val totalGap = gapAngle * categories.size
                val availableSweep = (360f - totalGap).coerceAtLeast(0f)

                categories.forEachIndexed { index, cat ->
                    val color = ReferenceSegmentColors[index % ReferenceSegmentColors.size]
                    val fraction = (cat.percentageOfTotal / 100f).coerceIn(0.01f, 1f)
                    val rawSweep = fraction * availableSweep
                    val animatedSweep = rawSweep * progress.value

                    if (animatedSweep > 0.5f) {
                        drawArc(
                            color = color,
                            startAngle = currentAngle,
                            sweepAngle = animatedSweep,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }
                    currentAngle += rawSweep + gapAngle
                }
            }
        }

        // Center Content: "Total Expenses" and Amount in ৳
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isExpense) "Total Expenses" else "Total Income",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                color = TextSecondary,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = currency.format(totalAmount),
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp
                ),
                color = TextPrimary
            )
        }

        // Callout Pill (e.g. top category percentage badge)
        val topCategory = categories.firstOrNull()
        if (topCategory != null) {
            Surface(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 4.dp),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF1E1743),
                shadowElevation = 6.dp
            ) {
                Text(
                    text = "${String.format("%.0f", topCategory.percentageOfTotal)}%",
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                )
            }
        }
    }
}
