package com.forgex.mobile.feature.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.forgex.mobile.core.component.FxFormFooterBar
import com.forgex.mobile.core.component.FxFormSection
import com.forgex.mobile.core.component.FxPageScaffold
import com.forgex.mobile.core.component.scanner.FxScanActionBar
import com.forgex.mobile.core.component.scanner.FxScanInputBox
import com.forgex.mobile.core.device.FxScannerManager
import com.forgex.mobile.core.model.FxScanResult
import com.forgex.mobile.core.ui.R
import kotlinx.coroutines.flow.collectLatest

/**
 * 服务器配置页，基于公共表单组件承接环境地址维护与扫描回填。
 */
@Composable
fun ServerSettingsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ServerSettingsViewModel = hiltViewModel(),
    scannerBridgeViewModel: ServerSettingsScanViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scannerManager = remember(scannerBridgeViewModel) { scannerBridgeViewModel.scannerManager }

    LaunchedEffect(viewModel) {
        viewModel.events.collectLatest { event ->
            if (event is ServerSettingsEvent.Saved) {
                onBack()
            }
        }
    }

    LaunchedEffect(scannerManager) {
        scannerManager.results.collectLatest { result ->
            routeServerScanResult(
                result = result,
                onHostResult = viewModel::applyHostScan,
                onPortResult = viewModel::applyPortScan
            )
            scannerManager.clearCachedResult()
        }
    }

    FxPageScaffold(title = stringResource(R.string.server_settings_title)) { _ ->
        Column(
            modifier = modifier
                .verticalScroll(rememberScrollState())
                .padding(vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Text(
                text = stringResource(R.string.server_settings_desc),
                style = MaterialTheme.typography.bodyMedium
            )

            FxFormSection(title = stringResource(R.string.server_settings_title)) {
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
                    color = MaterialTheme.colorScheme.primary
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

                FxScanInputBox(
                    value = uiState.host,
                    onValueChange = viewModel::updateHost,
                    label = stringResource(R.string.server_settings_host),
                    enabled = !uiState.isSaving,
                    latestScanResult = uiState.latestHostScanResult,
                    onScanConsumed = viewModel::consumeHostScanResult
                )

                FxScanInputBox(
                    value = uiState.port,
                    onValueChange = viewModel::updatePort,
                    label = stringResource(R.string.server_settings_port),
                    enabled = !uiState.isSaving,
                    latestScanResult = uiState.latestPortScanResult,
                    onScanConsumed = viewModel::consumePortScanResult
                )

                FxScanActionBar(
                    hint = stringResource(R.string.scan_hint_server),
                    enabled = !uiState.isSaving,
                    onActionClick = {}
                )
            }

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
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            FxFormFooterBar(
                confirmText = stringResource(R.string.server_settings_save),
                onConfirm = viewModel::saveEndpoint,
                secondaryText = stringResource(R.string.server_settings_reset),
                onSecondary = viewModel::resetToDefault
            )

            FxFormFooterBar(
                confirmText = stringResource(R.string.common_cancel),
                onConfirm = onBack
            )
        }
    }
}

/**
 * 根据扫描内容路由到主机或端口输入。
 */
private fun routeServerScanResult(
    result: FxScanResult,
    onHostResult: (FxScanResult) -> Unit,
    onPortResult: (FxScanResult) -> Unit
) {
    val rawValue = result.rawValue.trim()
    if (rawValue.isBlank()) {
        return
    }
    if (rawValue.all(Char::isDigit)) {
        onPortResult(result)
        return
    }
    onHostResult(result)
}

/**
 * 扫描管理器桥接 ViewModel，负责向 Compose 层暴露全局单例。
 */
@dagger.hilt.android.lifecycle.HiltViewModel
class ServerSettingsScanViewModel @javax.inject.Inject constructor(
    val scannerManager: FxScannerManager
) : androidx.lifecycle.ViewModel()
