package com.forgex.mobile.core.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.inset
import androidx.compose.ui.unit.dp
import kotlin.math.max
import kotlin.math.min

data class FxChartPoint(val label: String, val value: Float)

/** 饼图/环形图扇区数据，color 为空时按序取默认色板。 */
data class FxChartSlice(val label: String, val value: Float, val color: Color? = null)

private val fxChartPalette = listOf(
    Color(0xFF3B82F6),
    Color(0xFF10B981),
    Color(0xFFF59E0B),
    Color(0xFFEF4444),
    Color(0xFF8B5CF6),
    Color(0xFF06B6D4),
    Color(0xFFF472B6),
    Color(0xFF84CC16)
)

@Composable
fun FxLineChart(
    points: List<FxChartPoint>,
    modifier: Modifier = Modifier,
    lineColor: Color = MaterialTheme.colorScheme.primary,
    fillColor: Color = lineColor.copy(alpha = 0.12f)
) {
    FxChartContainer(points, modifier) {
        if (points.size < 2) return@FxChartContainer
        val maxValue = max(points.maxOf { it.value }, 1f)
        val step = size.width / (points.size - 1)
        val coordinates = points.mapIndexed { index, point ->
            Offset(index * step, size.height - (point.value / maxValue) * size.height)
        }
        val linePath = Path().apply {
            moveTo(coordinates.first().x, coordinates.first().y)
            coordinates.drop(1).forEach { lineTo(it.x, it.y) }
        }
        val fillPath = Path().apply {
            addPath(linePath)
            lineTo(coordinates.last().x, size.height)
            lineTo(coordinates.first().x, size.height)
            close()
        }
        drawPath(fillPath, fillColor)
        drawPath(linePath, lineColor, style = Stroke(width = 4f))
        coordinates.forEach { drawCircle(lineColor, radius = 5f, center = it) }
    }
}

@Composable
fun FxBarChart(
    points: List<FxChartPoint>,
    modifier: Modifier = Modifier,
    barColor: Color = MaterialTheme.colorScheme.primary
) {
    FxChartContainer(points, modifier) {
        if (points.isEmpty()) return@FxChartContainer
        val maxValue = max(points.maxOf { it.value }, 1f)
        val slot = size.width / points.size
        points.forEachIndexed { index, point ->
            val barWidth = slot * 0.62f
            val left = index * slot + (slot - barWidth) / 2f
            val top = size.height - (point.value / maxValue) * size.height
            drawRoundRect(
                color = barColor,
                topLeft = Offset(left, top),
                size = Size(barWidth, size.height - top),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
            )
        }
    }
}

@Composable
private fun FxChartContainer(
    points: List<FxChartPoint>,
    modifier: Modifier,
    draw: androidx.compose.ui.graphics.drawscope.DrawScope.() -> Unit
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(Modifier.fillMaxWidth().height(180.dp).padding(horizontal = 8.dp, vertical = 12.dp), onDraw = draw)
        if (points.isNotEmpty()) {
            Text(
                text = points.joinToString("    ") { "${it.label}: ${it.value}" },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 饼图。按扇区占比绘制，无数据或合计为 0 时不绘制图形。
 */
@Composable
fun FxPieChart(
    slices: List<FxChartSlice>,
    modifier: Modifier = Modifier,
    chartSize: androidx.compose.ui.unit.Dp = 160.dp
) {
    val emptyColor = MaterialTheme.colorScheme.outlineVariant
    FxPieChartContainer(slices, modifier, chartSize) { sweepAngles, total ->
        var startAngle = -90f
        slices.forEachIndexed { index, slice ->
            val sweep = sweepAngles[index]
            drawArc(
                color = slice.color ?: fxChartPalette[index % fxChartPalette.size],
                startAngle = startAngle,
                sweepAngle = sweep,
                useCenter = true
            )
            startAngle += sweep
        }
        if (total <= 0f) {
            drawCircle(
                color = emptyColor,
                radius = min(size.width, size.height) / 2f
            )
        }
    }
}

/**
 * 环形图。与饼图同数据协议，中心镂空并展示合计值。
 */
@Composable
fun FxDonutChart(
    slices: List<FxChartSlice>,
    modifier: Modifier = Modifier,
    chartSize: androidx.compose.ui.unit.Dp = 160.dp,
    ringWidth: Float = 36f,
    centerLabel: String? = null
) {
    val emptyColor = MaterialTheme.colorScheme.outlineVariant
    FxPieChartContainer(
        slices = slices,
        modifier = modifier,
        chartSize = chartSize,
        center = if (centerLabel == null) {
            null
        } else {
            {
                Text(
                    text = centerLabel,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    ) { sweepAngles, total ->
        var startAngle = -90f
        slices.forEachIndexed { index, slice ->
            val sweep = sweepAngles[index]
            drawArc(
                color = slice.color ?: fxChartPalette[index % fxChartPalette.size],
                startAngle = startAngle,
                sweepAngle = sweep,
                useCenter = false,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = ringWidth)
            )
            startAngle += sweep
        }
        if (total <= 0f) {
            drawCircle(
                color = emptyColor,
                radius = (min(size.width, size.height) - ringWidth) / 2f,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = ringWidth)
            )
        }
    }
}

@Composable
private fun FxPieChartContainer(
    slices: List<FxChartSlice>,
    modifier: Modifier,
    chartSize: androidx.compose.ui.unit.Dp,
    center: (@Composable () -> Unit)? = null,
    draw: androidx.compose.ui.graphics.drawscope.DrawScope.(sweepAngles: FloatArray, total: Float) -> Unit
) {
    Column(modifier = modifier.fillMaxWidth()) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(chartSize)
                .padding(horizontal = 8.dp, vertical = 12.dp),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            Canvas(Modifier.fillMaxSize()) {
                val diameter = min(size.width, size.height)
                val total = slices.fold(0f) { acc, slice -> acc + slice.value.coerceAtLeast(0f) }
                val sweepAngles = FloatArray(slices.size) { index ->
                    if (total > 0f) slices[index].value / total * 360f else 0f
                }
                val insetHorizontal = (size.width - diameter) / 2f
                val insetVertical = (size.height - diameter) / 2f
                inset(
                    left = insetHorizontal,
                    top = insetVertical,
                    right = insetHorizontal,
                    bottom = insetVertical
                ) {
                    draw(sweepAngles, total)
                }
            }
            center?.invoke()
        }
        if (slices.isNotEmpty()) {
            slices.forEachIndexed { index, slice ->
                Text(
                    text = "● ${slice.label}: ${slice.value}",
                    style = MaterialTheme.typography.bodySmall,
                    color = slice.color ?: fxChartPalette[index % fxChartPalette.size]
                )
            }
        }
    }
}
