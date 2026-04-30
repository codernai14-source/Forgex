package com.forgex.mobile.feature.message

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.forgex.mobile.core.component.FxListItem
import com.forgex.mobile.core.component.FxListPage
import com.forgex.mobile.core.ui.R
import com.forgex.mobile.core.ui.i18n.resolveAppText
import com.forgex.mobile.core.network.model.message.SysMessageVO

const val MESSAGE_ROUTE = "message"
const val MESSAGE_UNREAD_ROUTE = "message/unread"
const val MESSAGE_READ_ROUTE = "message/read"

enum class MessageEntryMode {
    HOME,
    UNREAD,
    READ
}

@Composable
fun MessageScreen(
    entryMode: MessageEntryMode = MessageEntryMode.HOME,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MessageViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(entryMode) {
        viewModel.load(entryMode)
    }

    FxListPage(
        title = entryMode.title(),
        loading = uiState.isLoading,
        error = uiState.errorMessage ?: resolveAppText(uiState.errorText),
        emptyText = stringResource(R.string.message_empty),
        hasData = uiState.messages.isNotEmpty(),
        onRetry = { viewModel.load(entryMode, force = true) }
    ) {
        Column(
            modifier = modifier.padding(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = entryMode.description(),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { viewModel.load(entryMode, force = true) }) {
                        Text(stringResource(R.string.common_refresh))
                    }
                    Button(onClick = onBack) {
                        Text(stringResource(R.string.common_back))
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(uiState.messages, key = { it.id ?: it.hashCode().toLong() }) { message ->
                    MessageCard(
                        message = message,
                        canMarkRead = entryMode != MessageEntryMode.READ,
                        isReading = (message.id != null && uiState.readingIds.contains(message.id)),
                        onMarkRead = {
                            val id = message.id ?: return@MessageCard
                            viewModel.markRead(id, entryMode)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun MessageCard(
    message: SysMessageVO,
    canMarkRead: Boolean,
    isReading: Boolean,
    onMarkRead: () -> Unit
) {
    val subtitle = buildString {
        append(stringResource(R.string.message_sender, message.senderName.orEmpty().ifBlank { stringResource(R.string.placeholder_dash) }))
        append("\n")
        append(stringResource(R.string.message_type, message.messageType.orEmpty().ifBlank { stringResource(R.string.placeholder_dash) }))
        append("\n")
        append(stringResource(R.string.message_time, message.createTime.orEmpty().ifBlank { stringResource(R.string.placeholder_dash) }))
        if (!message.content.isNullOrBlank()) {
            append("\n")
            append(stringResource(R.string.message_content, message.content ?: ""))
        }
        if (!message.linkUrl.isNullOrBlank()) {
            append("\n")
            append(stringResource(R.string.message_link, message.linkUrl ?: ""))
        }
    }

    FxListItem(
        title = message.title.orEmpty().ifBlank { stringResource(R.string.message_no_title) },
        subtitle = subtitle,
        status = if (message.status == 0) "Unread" else "Read",
        trailing = {
            if (canMarkRead && message.status == 0) {
                Button(
                    onClick = onMarkRead,
                    enabled = !isReading
                ) {
                    Text(
                        if (isReading) stringResource(R.string.common_processing)
                        else stringResource(R.string.message_mark_read)
                    )
                }
            }
        }
    )
}

@Composable
private fun MessageEntryMode.title(): String {
    return when (this) {
        MessageEntryMode.HOME -> stringResource(R.string.message_center_title)
        MessageEntryMode.UNREAD -> stringResource(R.string.message_unread_title)
        MessageEntryMode.READ -> stringResource(R.string.message_read_title)
    }
}

@Composable
private fun MessageEntryMode.description(): String {
    return when (this) {
        MessageEntryMode.HOME -> stringResource(R.string.message_center_desc)
        MessageEntryMode.UNREAD -> stringResource(R.string.message_unread_desc)
        MessageEntryMode.READ -> stringResource(R.string.message_read_desc)
    }
}
