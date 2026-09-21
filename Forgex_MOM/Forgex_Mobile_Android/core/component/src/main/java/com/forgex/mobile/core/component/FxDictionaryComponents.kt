package com.forgex.mobile.core.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/** A stable code/label pair returned by dictionary APIs. */
data class FxDictOption(
    val value: String,
    val label: String,
    val color: Color? = null,
    val enabled: Boolean = true
)

/** Resolves dictionary values while keeping unknown backend values visible. */
class FxDictionaryResolver(
    options: List<FxDictOption>,
    private val fallbackLabel: String? = null
) {
    private val byValue = options.associateBy { it.value }

    fun resolve(value: String?): FxDictOption? = value?.let(byValue::get)

    fun labelOf(value: String?): String {
        if (value.isNullOrBlank()) return fallbackLabel ?: ""
        return resolve(value)?.label ?: fallbackLabel ?: value
    }
}

/** A dictionary-aware status tag that keeps unknown values readable. */
@Composable
fun FxDictTag(
    value: String?,
    options: List<FxDictOption>,
    modifier: Modifier = Modifier,
    fallbackLabel: String? = null,
    fallbackColor: Color = MaterialTheme.colorScheme.primary,
    textColor: Color? = null
) {
    val resolver = remember(options, fallbackLabel) {
        FxDictionaryResolver(options, fallbackLabel)
    }
    val option = resolver.resolve(value)
    FxStatusBadge(
        text = resolver.labelOf(value),
        modifier = modifier,
        color = option?.color ?: fallbackColor,
        textColor = textColor ?: MaterialTheme.colorScheme.onPrimary
    )
}

/** A single-select dictionary field backed by a Material dialog. */
@Composable
fun FxDictSelect(
    label: String,
    value: String?,
    options: List<FxDictOption>,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    supportingText: String? = null,
    errorText: String? = null,
    enabled: Boolean = true,
    required: Boolean = false,
    dialogTitle: String = label
) {
    var dialogVisible by remember { mutableStateOf(false) }
    val resolver = remember(options) { FxDictionaryResolver(options) }

    FxSelectField(
        label = label,
        value = resolver.labelOf(value),
        modifier = modifier,
        placeholder = placeholder,
        supportingText = supportingText,
        errorText = errorText,
        enabled = enabled,
        required = required,
        onClick = { dialogVisible = true }
    )

    if (dialogVisible) {
        AlertDialog(
            onDismissRequest = { dialogVisible = false },
            title = { Text(dialogTitle) },
            text = {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    items(options, key = { it.value }) { option ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = option.enabled) {
                                    onValueChange(option.value)
                                    dialogVisible = false
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = option.value == value,
                                onClick = if (option.enabled) {
                                    {
                                        onValueChange(option.value)
                                        dialogVisible = false
                                    }
                                } else null,
                                enabled = option.enabled
                            )
                            Text(
                                text = option.label,
                                color = if (option.enabled) {
                                    MaterialTheme.colorScheme.onSurface
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                modifier = Modifier.weight(1f)
                            )
                            if (option.value == value) {
                                Icon(
                                    imageVector = Icons.Outlined.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { dialogVisible = false }) {
                    Text("取消")
                }
            }
        )
    }
}
