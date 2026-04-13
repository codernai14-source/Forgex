package com.forgex.mobile.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.forgex.mobile.core.ui.device.DeviceType

const val HOME_ROUTE = "home"

@Composable
fun HomeScreen(
    deviceType: DeviceType,
    onMenuClick: (HomeMenuItem) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(text = "首页 (${deviceType.name})", style = MaterialTheme.typography.headlineSmall)
        Text(text = "菜单已按后端 routes 动态渲染")

        if (uiState.isLoading) {
            CircularProgressIndicator()
        }

        if (!uiState.errorMessage.isNullOrBlank()) {
            Text(
                text = uiState.errorMessage ?: "",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 480.dp)
            )
            Button(onClick = viewModel::refreshMenus) {
                Text("重试")
            }
        }

        uiState.menuItems.forEach { item ->
            val renderTypeHint = item.pageRenderType
                ?.takeIf { it.isNotBlank() }
                ?: item.menuMode
                    ?.takeIf { it.isNotBlank() }
                    ?.let { "mode=$it" }
            val subtitle = when {
                item.componentKey.isNotBlank() && !renderTypeHint.isNullOrBlank() ->
                    "${item.path} · ${item.componentKey} · $renderTypeHint"
                item.componentKey.isNotBlank() -> "${item.path} · ${item.componentKey}"
                else -> item.path
            }

            Button(
                onClick = { onMenuClick(item) },
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 480.dp)
            ) {
                Column(horizontalAlignment = Alignment.Start) {
                    Text(text = item.title)
                    Text(text = subtitle, color = Color.White.copy(alpha = 0.8f))
                }
            }
        }
    }
}
