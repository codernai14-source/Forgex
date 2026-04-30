package com.forgex.mobile.core.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * 状态标签组件。
 *
 * 统一封装圆角状态标记样式，适用于列表、详情和表格场景中的轻量状态展示。
 *
 * @param text 标签文案
 * @param modifier 组件修饰符
 * @param color 标签背景色
 * @param textColor 标签文字颜色
 */
@Composable
fun FxStatusBadge(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    textColor: Color = MaterialTheme.colorScheme.onPrimary
) {
    Text(
        text = text,
        color = textColor,
        style = MaterialTheme.typography.labelMedium,
        modifier = modifier
            .background(color = color, shape = RoundedCornerShape(999.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    )
}
