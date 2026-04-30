package com.forgex.mobile.core.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Icon
import com.forgex.mobile.core.designsystem.FxSpacing
import com.forgex.mobile.core.model.FxScanResult
import com.forgex.mobile.core.model.FxScanSource
import com.forgex.mobile.core.ui.R

private val DefaultAcceptedExternalSources = setOf(
    FxScanSource.PDA_BROADCAST,
    FxScanSource.HARDWARE_KEYBOARD,
    FxScanSource.NFC,
    FxScanSource.CAMERA
)

/**
 * 标准自动补全输入组件。
 *
 * 统一处理输入联想、建议项下拉、异步加载提示和外部扫描/NFC 回填协议，
 * 适用于物料、客户、库位等高频模糊检索场景。
 *
 * @param value 当前输入值
 * @param onValueChange 值变化回调
 * @param label 字段标题
 * @param suggestions 建议项列表
 * @param onSuggestionSelected 选择建议项回调
 * @param modifier 组件修饰符
 * @param placeholder 占位文案
 * @param supportingText 帮助说明文案
 * @param errorText 错误提示文案
 * @param enabled 是否可用
 * @param required 是否必填
 * @param readOnly 是否只读
 * @param loading 是否处于建议项加载中
 * @param maxLength 最大输入长度
 * @param emptyText 无匹配建议项时的提示文案
 * @param suggestionLabel 建议项展示标题映射
 * @param suggestionSupportingText 建议项辅助文案映射
 * @param latestExternalValue 最近一次外部输入结果
 * @param acceptedExternalSources 允许自动回填的外部来源
 * @param onExternalValueConsumed 外部输入消费完成回调
 * @param onSearch 主动搜索回调
 * @param maxSuggestions 最大展示建议项数量
 * @param suggestionContent 自定义建议项内容
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> FxAutoCompleteField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    suggestions: List<T>,
    onSuggestionSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    supportingText: String? = null,
    errorText: String? = null,
    enabled: Boolean = true,
    required: Boolean = false,
    readOnly: Boolean = false,
    loading: Boolean = false,
    maxLength: Int? = null,
    emptyText: String = stringResource(R.string.component_autocomplete_empty),
    suggestionLabel: (T) -> String = { it.toString() },
    suggestionSupportingText: ((T) -> String?)? = null,
    latestExternalValue: FxScanResult? = null,
    acceptedExternalSources: Set<FxScanSource> = DefaultAcceptedExternalSources,
    onExternalValueConsumed: (() -> Unit)? = null,
    onSearch: (() -> Unit)? = null,
    maxSuggestions: Int = 6,
    suggestionContent: (@Composable (T) -> Unit)? = null
) {
    var expanded by remember { mutableStateOf(false) }
    val visibleSuggestions = remember(suggestions, maxSuggestions) {
        suggestions.take(maxSuggestions.coerceAtLeast(0))
    }
    val hasMenuContent = loading || visibleSuggestions.isNotEmpty() || value.isNotBlank()
    val menuExpanded = expanded && enabled && hasMenuContent

    LaunchedEffect(latestExternalValue?.timestamp) {
        val result = latestExternalValue ?: return@LaunchedEffect
        if (result.source !in acceptedExternalSources) {
            return@LaunchedEffect
        }
        onValueChange(result.rawValue)
        expanded = true
        onSearch?.invoke()
        onExternalValueConsumed?.invoke()
    }

    ExposedDropdownMenuBox(
        expanded = menuExpanded,
        onExpandedChange = { shouldExpand ->
            expanded = shouldExpand && enabled && hasMenuContent
        },
        modifier = modifier.fillMaxWidth()
    ) {
        FxTextField(
            value = value,
            onValueChange = { changedValue ->
                onValueChange(changedValue)
                expanded = changedValue.isNotBlank() || loading || suggestions.isNotEmpty()
            },
            label = label,
            modifier = Modifier.menuAnchor(),
            placeholder = placeholder ?: label,
            supportingText = supportingText,
            errorText = errorText,
            enabled = enabled,
            required = required,
            readOnly = readOnly,
            maxLength = maxLength,
            clearable = !readOnly,
            keyboardOptions = KeyboardOptions(
                imeAction = if (onSearch != null) ImeAction.Search else ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onSearch = {
                    onSearch?.invoke()
                    expanded = true
                },
                onDone = {
                    if (onSearch != null) {
                        onSearch.invoke()
                        expanded = true
                    }
                }
            ),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = null
                )
            },
            trailingContent = {
                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(18.dp)
                            .padding(end = FxSpacing.xSmall),
                        strokeWidth = 2.dp
                    )
                } else if (hasMenuContent) {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuExpanded)
                }
            }
        )

        ExposedDropdownMenu(
            expanded = menuExpanded,
            onDismissRequest = { expanded = false }
        ) {
            when {
                loading -> {
                    DropdownMenuItem(
                        text = {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(FxSpacing.small),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                                Text(text = stringResource(R.string.common_loading))
                            }
                        },
                        onClick = {},
                        enabled = false
                    )
                }

                visibleSuggestions.isEmpty() -> {
                    DropdownMenuItem(
                        text = { Text(text = emptyText) },
                        onClick = {},
                        enabled = false
                    )
                }

                else -> {
                    visibleSuggestions.forEach { suggestion ->
                        DropdownMenuItem(
                            text = {
                                if (suggestionContent != null) {
                                    suggestionContent(suggestion)
                                } else {
                                    FxDefaultAutoCompleteSuggestion(
                                        title = suggestionLabel(suggestion),
                                        supportingText = suggestionSupportingText?.invoke(suggestion)
                                    )
                                }
                            },
                            onClick = {
                                onValueChange(suggestionLabel(suggestion))
                                onSuggestionSelected(suggestion)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * 自动补全建议项默认内容。
 *
 * @param title 主标题
 * @param supportingText 辅助说明
 */
@Composable
private fun FxDefaultAutoCompleteSuggestion(
    title: String,
    supportingText: String?
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(FxSpacing.xSmall)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        if (!supportingText.isNullOrBlank()) {
            Text(
                text = supportingText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

