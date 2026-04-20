package com.forgex.mobile.feature.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.forgex.mobile.core.ui.R
import kotlinx.coroutines.flow.collectLatest

@Composable
fun ServerSettingsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ServerSettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.events.collectLatest { event ->
            if (event is ServerSettingsEvent.Saved) {
                onBack()
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 520.dp),
            shape = MaterialTheme.shapes.large,
            color = Color(0xFFF5F8FF)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.server_settings_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = stringResource(R.string.server_settings_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF5B6478)
                )
                Text(
                    text = stringResource(R.string.server_settings_default, uiState.defaultEndpoint),
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = if (uiState.usingCustomEndpoint) {
                        stringResource(R.string.server_settings_current_custom, uiState.currentEndpoint)
                    } else {
                        stringResource(R.string.server_settings_current_default, uiState.currentEndpoint)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF1976D2)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = uiState.scheme == "http",
                        onClick = { viewModel.updateScheme("http") },
                        label = { Text("http") },
                        enabled = !uiState.isSaving
                    )
                    FilterChip(
                        selected = uiState.scheme == "https",
                        onClick = { viewModel.updateScheme("https") },
                        label = { Text("https") },
                        enabled = !uiState.isSaving
                    )
                }

                OutlinedTextField(
                    value = uiState.host,
                    onValueChange = viewModel::updateHost,
                    label = { Text(stringResource(R.string.server_settings_host)) },
                    singleLine = true,
                    enabled = !uiState.isSaving,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = uiState.port,
                    onValueChange = viewModel::updatePort,
                    label = { Text(stringResource(R.string.server_settings_port)) },
                    singleLine = true,
                    enabled = !uiState.isSaving,
                    modifier = Modifier.fillMaxWidth()
                )

                val errorMessageRes = uiState.errorMessageRes
                if (errorMessageRes != null) {
                    Text(
                        text = stringResource(errorMessageRes),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                val messageRes = uiState.messageRes
                if (messageRes != null) {
                    Text(
                        text = stringResource(messageRes),
                        color = Color(0xFF2E7D32),
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Button(
                    onClick = viewModel::saveEndpoint,
                    enabled = !uiState.isSaving,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.server_settings_save))
                }
                OutlinedButton(
                    onClick = viewModel::resetToDefault,
                    enabled = !uiState.isSaving,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.server_settings_reset))
                }
                OutlinedButton(
                    onClick = onBack,
                    enabled = !uiState.isSaving,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        }
    }
}
