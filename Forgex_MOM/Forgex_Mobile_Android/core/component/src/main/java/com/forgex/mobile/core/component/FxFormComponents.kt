package com.forgex.mobile.core.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import com.forgex.mobile.core.designsystem.FxColors
import com.forgex.mobile.core.designsystem.FxSpacing
import com.forgex.mobile.core.designsystem.rememberFxAdaptiveMetrics

/**
 * 标准表单页面骨架。
 *
 * 统一处理表单页的滚动容器、内容最大宽度和区块间距，
 * 便于业务页面专注于字段编排。
 *
 * @param title 页面标题
 * @param subtitle 页面说明文案
 * @param modifier 页面容器修饰符
 * @param bottomBar 底部操作区
 * @param content 表单内容
 */
@Composable
fun FxFormPage(
    title: String? = null,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    bottomBar: @Composable (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val metrics = rememberFxAdaptiveMetrics()

    FxPageScaffold(
        title = title,
        modifier = modifier,
        bottomBar = bottomBar
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(vertical = metrics.sectionSpacing)
                .widthIn(max = metrics.formMaxWidth),
            verticalArrangement = Arrangement.spacedBy(metrics.sectionSpacing)
        ) {
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            content()
        }
    }
}

/**
 * 标准文本输入组件。
 *
 * 统一支持必填、错误提示、帮助文案、字符计数、清空按钮等基础交互，
 * 作为业务页面中大多数字段的默认输入实现。
 *
 * @param value 当前值
 * @param onValueChange 值变化回调
 * @param label 字段标题
 * @param modifier 组件修饰符
 * @param placeholder 占位文案
 * @param supportingText 帮助说明文案
 * @param errorText 错误提示文案
 * @param readOnly 是否只读
 * @param enabled 是否可用
 * @param required 是否必填
 * @param singleLine 是否单行
 * @param visualTransformation 文本转换器
 * @param keyboardOptions 软键盘配置
 * @param keyboardActions 软键盘行为回调
 * @param leadingIcon 前置图标
 * @param trailingContent 后置自定义内容
 * @param maxLength 最大输入长度
 * @param clearable 是否显示清空按钮
 */
@Composable
fun FxTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    supportingText: String? = null,
    errorText: String? = null,
    readOnly: Boolean = false,
    enabled: Boolean = true,
    required: Boolean = false,
    singleLine: Boolean = true,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    leadingIcon: (@Composable (() -> Unit))? = null,
    trailingContent: (@Composable RowScope.() -> Unit)? = null,
    maxLength: Int? = null,
    clearable: Boolean = false
) {
    val displayedValue = remember(value, maxLength) {
        maxLength?.let(value::take) ?: value
    }

    OutlinedTextField(
        value = displayedValue,
        onValueChange = { changedValue ->
            val nextValue = maxLength?.let(changedValue::take) ?: changedValue
            onValueChange(nextValue)
        },
        modifier = modifier.fillMaxWidth(),
        label = {
            Text(text = fxLabelText(label = label, required = required))
        },
        placeholder = placeholder?.let {
            { Text(text = it) }
        },
        supportingText = {
            FxFieldSupportingRow(
                supportingText = supportingText,
                errorText = errorText,
                currentLength = displayedValue.length,
                maxLength = maxLength
            )
        },
        leadingIcon = leadingIcon,
        trailingIcon = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                trailingContent?.invoke(this)
                if (clearable && enabled && !readOnly && displayedValue.isNotEmpty()) {
                    IconButton(onClick = { onValueChange("") }) {
                        Icon(
                            imageVector = Icons.Outlined.Cancel,
                            contentDescription = null
                        )
                    }
                }
            }
        },
        readOnly = readOnly,
        enabled = enabled,
        singleLine = singleLine,
        maxLines = if (singleLine) 1 else Int.MAX_VALUE,
        isError = !errorText.isNullOrBlank(),
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions
    )
}

/**
 * 标准搜索输入组件。
 *
 * 统一提供搜索图标、清空交互和软键盘搜索动作，
 * 适用于列表筛选和快速检索场景。
 *
 * @param value 当前值
 * @param onValueChange 值变化回调
 * @param label 字段标题
 * @param modifier 组件修饰符
 * @param placeholder 占位文案
 * @param supportingText 帮助说明文案
 * @param errorText 错误提示文案
 * @param enabled 是否可用
 * @param onSearch 触发搜索时的回调
 */
@Composable
fun FxSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    supportingText: String? = null,
    errorText: String? = null,
    enabled: Boolean = true,
    onSearch: (() -> Unit)? = null
) {
    FxTextField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        modifier = modifier,
        placeholder = placeholder ?: label,
        supportingText = supportingText,
        errorText = errorText,
        enabled = enabled,
        clearable = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSearch?.invoke() }),
        leadingIcon = {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = null
            )
        }
    )
}

/**
 * 标准密码输入组件。
 *
 * 统一提供明文/密文切换能力，便于登录和敏感信息录入场景复用。
 *
 * @param value 当前值
 * @param onValueChange 值变化回调
 * @param label 字段标题
 * @param modifier 组件修饰符
 * @param placeholder 占位文案
 * @param supportingText 帮助说明文案
 * @param errorText 错误提示文案
 * @param enabled 是否可用
 * @param required 是否必填
 */
@Composable
fun FxPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    supportingText: String? = null,
    errorText: String? = null,
    enabled: Boolean = true,
    required: Boolean = false
) {
    var passwordVisible by remember { mutableStateOf(false) }

    FxTextField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        modifier = modifier,
        placeholder = placeholder,
        supportingText = supportingText,
        errorText = errorText,
        enabled = enabled,
        required = required,
        visualTransformation = if (passwordVisible) {
            VisualTransformation.None
        } else {
            PasswordVisualTransformation()
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done
        ),
        leadingIcon = {
            Icon(
                imageVector = Icons.Outlined.Lock,
                contentDescription = null
            )
        },
        trailingContent = {
            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                Icon(
                    imageVector = if (passwordVisible) {
                        Icons.Outlined.VisibilityOff
                    } else {
                        Icons.Outlined.Visibility
                    },
                    contentDescription = null
                )
            }
        }
    )
}

/**
 * 标准数字输入组件。
 *
 * 统一处理整数、小数和负数输入过滤逻辑，避免业务页面重复实现数值校验。
 *
 * @param value 当前值
 * @param onValueChange 值变化回调
 * @param label 字段标题
 * @param modifier 组件修饰符
 * @param placeholder 占位文案
 * @param supportingText 帮助说明文案
 * @param errorText 错误提示文案
 * @param enabled 是否可用
 * @param required 是否必填
 * @param allowDecimal 是否允许小数
 * @param allowNegative 是否允许负数
 */
@Composable
fun FxNumberField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    supportingText: String? = null,
    errorText: String? = null,
    enabled: Boolean = true,
    required: Boolean = false,
    allowDecimal: Boolean = false,
    allowNegative: Boolean = false
) {
    FxTextField(
        value = value,
        onValueChange = { changedValue ->
            val normalized = changedValue.filterIndexed { index, current ->
                when {
                    current.isDigit() -> true
                    current == '-' -> allowNegative && index == 0 && !changedValue.take(index).contains('-')
                    current == '.' -> allowDecimal && changedValue.take(index).count { it == '.' } == 0
                    else -> false
                }
            }
            onValueChange(normalized)
        },
        label = label,
        modifier = modifier,
        placeholder = placeholder,
        supportingText = supportingText,
        errorText = errorText,
        enabled = enabled,
        required = required,
        keyboardOptions = KeyboardOptions(
            keyboardType = if (allowDecimal) KeyboardType.Decimal else KeyboardType.Number,
            imeAction = ImeAction.Done
        )
    )
}

/**
 * 标准选择输入组件。
 *
 * 统一使用只读字段承载选择结果，并提供点击整行触发选择面板的交互协议。
 *
 * @param label 字段标题
 * @param value 当前显示值
 * @param modifier 组件修饰符
 * @param placeholder 占位文案
 * @param supportingText 帮助说明文案
 * @param errorText 错误提示文案
 * @param enabled 是否可用
 * @param required 是否必填
 * @param onClick 点击回调
 */
@Composable
fun FxSelectField(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    supportingText: String? = null,
    errorText: String? = null,
    enabled: Boolean = true,
    required: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    Box(
        modifier = modifier.fillMaxWidth()
    ) {
        FxTextField(
            value = value,
            onValueChange = {},
            label = label,
            placeholder = placeholder,
            supportingText = supportingText,
            errorText = errorText,
            enabled = enabled,
            required = required,
            readOnly = true,
            trailingContent = {
                Icon(
                    imageVector = Icons.Outlined.ArrowDropDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        )
        if (onClick != null) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable(enabled = enabled, onClick = onClick)
            )
        }
    }
}

/**
 * 标准日期选择组件。
 *
 * 统一复用选择字段协议，保持日期类字段交互一致。
 *
 * @param label 字段标题
 * @param value 当前显示值
 * @param modifier 组件修饰符
 * @param placeholder 占位文案
 * @param supportingText 帮助说明文案
 * @param errorText 错误提示文案
 * @param enabled 是否可用
 * @param required 是否必填
 * @param onClick 点击回调
 */
@Composable
fun FxDateField(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    supportingText: String? = null,
    errorText: String? = null,
    enabled: Boolean = true,
    required: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    FxSelectField(
        label = label,
        value = value,
        modifier = modifier,
        placeholder = placeholder,
        supportingText = supportingText,
        errorText = errorText,
        enabled = enabled,
        required = required,
        onClick = onClick
    )
}

/**
 * 标准日期范围选择组件。
 *
 * 统一复用选择字段协议，适用于查询条件和范围录入场景。
 *
 * @param label 字段标题
 * @param value 当前显示值
 * @param modifier 组件修饰符
 * @param placeholder 占位文案
 * @param supportingText 帮助说明文案
 * @param errorText 错误提示文案
 * @param enabled 是否可用
 * @param required 是否必填
 * @param onClick 点击回调
 */
@Composable
fun FxDateRangeField(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    supportingText: String? = null,
    errorText: String? = null,
    enabled: Boolean = true,
    required: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    Box(modifier = modifier.fillMaxWidth()) {
        FxTextField(
            value = value,
            onValueChange = {},
            label = label,
            placeholder = placeholder,
            supportingText = supportingText,
            errorText = errorText,
            enabled = enabled,
            required = required,
            readOnly = true,
            trailingContent = {
                Icon(
                    imageVector = Icons.Outlined.CalendarMonth,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        )
        if (onClick != null) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable(enabled = enabled, onClick = onClick)
            )
        }
    }
}

/**
 * 标准表单区块组件。
 *
 * 统一表单分组标题、说明和容器样式，减少各页面重复拼装卡片结构。
 *
 * @param title 区块标题
 * @param modifier 组件修饰符
 * @param description 区块说明文案
 * @param content 区块内容
 */
@Composable
fun FxFormSection(
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(FxSpacing.large),
            verticalArrangement = Arrangement.spacedBy(FxSpacing.medium)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(FxSpacing.xSmall)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (!description.isNullOrBlank()) {
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            content()
        }
    }
}

/**
 * 标准表单项容器。
 *
 * 在需要将字段说明与自定义内容分离时，可统一复用该容器组件。
 *
 * @param label 字段标题
 * @param modifier 组件修饰符
 * @param required 是否必填
 * @param description 字段说明
 * @param content 自定义内容
 */
@Composable
fun FxFormItem(
    label: String,
    modifier: Modifier = Modifier,
    required: Boolean = false,
    description: String? = null,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(FxSpacing.small)
    ) {
        Text(
            text = fxLabelText(label = label, required = required),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        if (!description.isNullOrBlank()) {
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        content()
    }
}

/**
 * 标准只读展示块。
 *
 * 用于详情页或只读表单中复用统一的只读样式。
 *
 * @param label 字段标题
 * @param value 展示值
 * @param modifier 组件修饰符
 */
@Composable
fun FxFormReadonlyBlock(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(FxSpacing.xSmall)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * 表单底部操作栏。
 *
 * 统一处理主次按钮布局以及手机/Pad 最大宽度约束，
 * 适合作为保存、提交、返回等操作区复用。
 *
 * @param confirmText 主按钮文案
 * @param onConfirm 主按钮点击回调
 * @param modifier 组件修饰符
 * @param secondaryText 次按钮文案
 * @param onSecondary 次按钮点击回调
 * @param confirmEnabled 主按钮是否可用
 * @param secondaryEnabled 次按钮是否可用
 * @param loading 是否处于加载中
 */
@Composable
fun FxFormFooterBar(
    confirmText: String,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    secondaryText: String? = null,
    onSecondary: (() -> Unit)? = null,
    confirmEnabled: Boolean = true,
    secondaryEnabled: Boolean = true,
    loading: Boolean = false
) {
    val metrics = rememberFxAdaptiveMetrics()

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = metrics.formMaxWidth)
                .padding(vertical = FxSpacing.medium),
            horizontalArrangement = Arrangement.spacedBy(FxSpacing.medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (secondaryText != null && onSecondary != null) {
                OutlinedButton(
                    onClick = onSecondary,
                    enabled = secondaryEnabled && !loading,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = secondaryText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Button(
                onClick = onConfirm,
                enabled = confirmEnabled && !loading,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = confirmText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * 必填标记文本。
 *
 * @param label 原始标题
 * @param required 是否必填
 * @return 带必填标识的标题富文本
 */
@Composable
internal fun fxLabelText(label: String, required: Boolean) = buildAnnotatedString {
    if (required) {
        withStyle(style = SpanStyle(color = FxColors.Error)) {
            append("* ")
        }
    }
    append(label)
}

/**
 * 字段底部辅助信息行。
 *
 * @param supportingText 帮助说明
 * @param errorText 错误提示
 * @param currentLength 当前字符长度
 * @param maxLength 最大字符长度
 */
@Composable
internal fun FxFieldSupportingRow(
    supportingText: String?,
    errorText: String?,
    currentLength: Int,
    maxLength: Int?
) {
    val message = errorText ?: supportingText
    val messageColor = if (!errorText.isNullOrBlank()) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    if (message.isNullOrBlank() && maxLength == null) {
        return
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (!message.isNullOrBlank()) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = messageColor,
                modifier = Modifier.weight(1f)
            )
        } else {
            Box(modifier = Modifier.weight(1f))
        }

        if (maxLength != null) {
            Text(
                text = "$currentLength/$maxLength",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}


