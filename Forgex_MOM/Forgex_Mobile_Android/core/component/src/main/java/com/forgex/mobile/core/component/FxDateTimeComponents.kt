package com.forgex.mobile.core.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

private val fxDateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
private val fxDateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
private val fxTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

internal fun parseFxDate(value: String): LocalDate? = runCatching {
    LocalDate.parse(value, fxDateFormatter)
}.getOrNull()

internal fun formatFxDate(value: LocalDate): String = value.format(fxDateFormatter)

internal fun parseFxDateTime(value: String): LocalDateTime? = runCatching {
    LocalDateTime.parse(value, fxDateTimeFormatter)
}.getOrNull()

internal fun formatFxDateTime(value: LocalDateTime): String = value.format(fxDateTimeFormatter)

internal fun parseFxTime(value: String): LocalTime? = runCatching {
    LocalTime.parse(value, fxTimeFormatter)
}.getOrNull()

internal fun formatFxTime(value: LocalTime): String = value.format(fxTimeFormatter)

internal fun formatFxDateRange(start: LocalDate, end: LocalDate): String =
    "${formatFxDate(start)} 至 ${formatFxDate(end)}"

private fun LocalDate.toPickerMillis(): Long =
    atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

private fun Long.toPickerDate(): LocalDate =
    Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FxDatePickerField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    supportingText: String? = null,
    errorText: String? = null,
    enabled: Boolean = true,
    required: Boolean = false,
    dialogTitle: String = label
) {
    var visible by remember { mutableStateOf(false) }
    val initialDate = parseFxDate(value) ?: LocalDate.now()
    val state = rememberDatePickerState(initialSelectedDateMillis = initialDate.toPickerMillis())

    FxSelectField(
        label = label,
        value = value,
        modifier = modifier,
        placeholder = placeholder,
        supportingText = supportingText,
        errorText = errorText,
        enabled = enabled,
        required = required,
        onClick = { visible = true }
    )
    if (visible) {
        DatePickerDialog(
            onDismissRequest = { visible = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { onValueChange(formatFxDate(it.toPickerDate())) }
                    visible = false
                }) { Text("确定") }
            },
            dismissButton = {
                TextButton(onClick = { visible = false }) { Text("取消") }
            }
        ) {
            DatePicker(state = state, title = { Text(dialogTitle) })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FxDateRangePickerField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    supportingText: String? = null,
    errorText: String? = null,
    enabled: Boolean = true,
    required: Boolean = false,
    dialogTitle: String = label
) {
    var visible by remember { mutableStateOf(false) }
    val state = rememberDateRangePickerState()

    FxSelectField(
        label = label,
        value = value,
        modifier = modifier,
        placeholder = placeholder,
        supportingText = supportingText,
        errorText = errorText,
        enabled = enabled,
        required = required,
        onClick = { visible = true }
    )
    if (visible) {
        DatePickerDialog(
            onDismissRequest = { visible = false },
            confirmButton = {
                TextButton(onClick = {
                    val start = state.selectedStartDateMillis?.toPickerDate()
                    val end = state.selectedEndDateMillis?.toPickerDate()
                    if (start != null && end != null) onValueChange(formatFxDateRange(start, end))
                    visible = false
                }) { Text("确定") }
            },
            dismissButton = {
                TextButton(onClick = { visible = false }) { Text("取消") }
            }
        ) {
            DateRangePicker(state = state, title = { Text(dialogTitle) })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FxDateTimePickerField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    supportingText: String? = null,
    errorText: String? = null,
    enabled: Boolean = true,
    required: Boolean = false,
    dialogTitle: String = label
) {
    var dateDialogVisible by remember { mutableStateOf(false) }
    var timeDialogVisible by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf(parseFxDateTime(value)?.toLocalDate() ?: LocalDate.now()) }
    val initialDateTime = parseFxDateTime(value) ?: LocalDateTime.now()
    val dateState = rememberDatePickerState(initialSelectedDateMillis = initialDateTime.toLocalDate().toPickerMillis())
    val timeState = rememberTimePickerState(initialHour = initialDateTime.hour, initialMinute = initialDateTime.minute)

    FxSelectField(
        label = label,
        value = value,
        modifier = modifier,
        placeholder = placeholder,
        supportingText = supportingText,
        errorText = errorText,
        enabled = enabled,
        required = required,
        onClick = { dateDialogVisible = true }
    )
    if (dateDialogVisible) {
        DatePickerDialog(
            onDismissRequest = { dateDialogVisible = false },
            confirmButton = {
                TextButton(onClick = {
                    dateState.selectedDateMillis?.let {
                        selectedDate = it.toPickerDate()
                        dateDialogVisible = false
                        timeDialogVisible = true
                    }
                }) { Text("下一步") }
            },
            dismissButton = {
                TextButton(onClick = { dateDialogVisible = false }) { Text("取消") }
            }
        ) { DatePicker(state = dateState, title = { Text(dialogTitle) }) }
    }
    if (timeDialogVisible) {
        AlertDialog(
            onDismissRequest = { timeDialogVisible = false },
            title = { Text("选择时间") },
            text = { TimePicker(state = timeState) },
            confirmButton = {
                Button(onClick = {
                    onValueChange(formatFxDateTime(LocalDateTime.of(selectedDate, LocalTime.of(timeState.hour, timeState.minute))))
                    timeDialogVisible = false
                }) { Text("确定") }
            },
            dismissButton = {
                TextButton(onClick = { timeDialogVisible = false }) { Text("取消") }
            }
        )
    }
}
