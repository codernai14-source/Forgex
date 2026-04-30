package com.forgex.mobile.core.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.forgex.mobile.core.model.FxTableAlign
import com.forgex.mobile.core.model.FxTableColumn

/**
 * 标准表格组件。
 *
 * 统一处理列头、行数据、横向滚动和点击交互，适合 Pad 和高信息密度场景复用。
 *
 * @param columns 表格列配置
 * @param rows 数据行
 * @param rowContent 行内容转换器
 * @param modifier 组件修饰符
 * @param onRowClick 行点击回调
 */
@Composable
fun <T> FxTable(
    columns: List<FxTableColumn>,
    rows: List<T>,
    rowContent: @Composable (T) -> List<String>,
    modifier: Modifier = Modifier,
    onRowClick: ((T) -> Unit)? = null
) {
    val scrollState = rememberScrollState()

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                columns.forEach { column ->
                    FxTableCell(
                        text = column.title,
                        column = column,
                        textStyle = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            rows.forEachIndexed { index, row ->
                val values = rowContent(row)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            if (onRowClick != null) {
                                Modifier.clickable { onRowClick(row) }
                            } else {
                                Modifier
                            }
                        )
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    columns.forEachIndexed { columnIndex, column ->
                        FxTableCell(
                            text = values.getOrNull(columnIndex).orEmpty(),
                            column = column,
                            textStyle = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (index != rows.lastIndex) {
                    HorizontalDivider()
                }
            }
        }
    }
}

/**
 * 单元格内容组件。
 *
 * @param text 单元格文案
 * @param column 列配置
 * @param textStyle 文本样式
 * @param color 文本颜色
 */
@Composable
private fun FxTableCell(
    text: String,
    column: FxTableColumn,
    textStyle: TextStyle,
    color: Color
) {
    Box(
        modifier = Modifier.widthIn(min = (column.width ?: 96).dp),
        contentAlignment = when (column.align) {
            FxTableAlign.START -> Alignment.CenterStart
            FxTableAlign.CENTER -> Alignment.Center
            FxTableAlign.END -> Alignment.CenterEnd
        }
    ) {
        Text(
            text = text,
            style = textStyle,
            color = color
        )
    }
}
