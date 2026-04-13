package com.forgex.mobile.feature.message

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.forgex.mobile.core.network.model.message.SysMessageVO

const val MESSAGE_ROUTE = "message"
const val MESSAGE_UNREAD_ROUTE = "message/unread"
const val MESSAGE_READ_ROUTE = "message/read"

enum class MessageEntryMode(
    val title: String,
    val description: String
) {
    HOME("消息中心", "展示未读消息"),
    UNREAD("未读消息", "当前未读消息列表"),
    READ("已读消息", "已处理消息列表")
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = entryMode.title,
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    text = entryMode.description,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { viewModel.load(entryMode, force = true) }) {
                    Text("刷新")
                }
                Button(onClick = onBack) {
                    Text("返回")
                }
            }
        }

        when {
            uiState.isLoading -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            !uiState.errorMessage.isNullOrBlank() -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = uiState.errorMessage ?: "消息列表加载失败",
                        color = MaterialTheme.colorScheme.error
                    )
                    Button(
                        onClick = { viewModel.load(entryMode, force = true) },
                        modifier = Modifier.padding(top = 12.dp)
                    ) {
                        Text("重试")
                    }
                }
            }

            uiState.messages.isEmpty() -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("暂无消息数据")
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
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
}

@Composable
private fun MessageCard(
    message: SysMessageVO,
    canMarkRead: Boolean,
    isReading: Boolean,
    onMarkRead: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = message.title.orEmpty().ifBlank { "无标题消息" },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text("发送人：${message.senderName.orEmpty().ifBlank { "--" }}")
            Text("类型：${message.messageType.orEmpty().ifBlank { "--" }}")
            Text("时间：${message.createTime.orEmpty().ifBlank { "--" }}")
            if (!message.content.isNullOrBlank()) {
                Text("内容：${message.content}")
            }
            if (!message.linkUrl.isNullOrBlank()) {
                Text("链接：${message.linkUrl}")
            }

            if (canMarkRead && message.status == 0) {
                Button(
                    onClick = onMarkRead,
                    enabled = !isReading,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(if (isReading) "处理中..." else "标记已读")
                }
            }
        }
    }
}
