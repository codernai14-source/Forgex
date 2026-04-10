package com.forgex.mobile.feature.auth

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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.flow.collectLatest

const val AUTH_ROUTE = "auth"

@Composable
fun AuthScreen(
    onLoginSuccess: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.events.collectLatest { event ->
            if (event is AuthEvent.LoginCompleted) {
                onLoginSuccess()
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "移动端登录", style = MaterialTheme.typography.headlineSmall)

        if (uiState.step == AuthStep.LOGIN) {
            LoginForm(
                account = uiState.account,
                password = uiState.password,
                captcha = uiState.captcha,
                captchaId = uiState.captchaId,
                loading = uiState.isLoading,
                onAccountChange = viewModel::updateAccount,
                onPasswordChange = viewModel::updatePassword,
                onCaptchaChange = viewModel::updateCaptcha,
                onCaptchaIdChange = viewModel::updateCaptchaId,
                onSubmit = viewModel::submitLogin
            )
        } else {
            TenantSelection(
                tenants = uiState.tenants,
                selectedTenantId = uiState.selectedTenantId,
                loading = uiState.isLoading,
                onSelectTenant = viewModel::selectTenant,
                onConfirm = viewModel::confirmTenantSelection,
                onBack = viewModel::backToLogin
            )
        }

        if (!uiState.errorMessage.isNullOrBlank()) {
            Text(
                text = uiState.errorMessage ?: "",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.fillMaxWidth().widthIn(max = 420.dp)
            )
        }

        if (uiState.isLoading) {
            CircularProgressIndicator(modifier = Modifier.padding(top = 8.dp))
        }
    }
}

@Composable
private fun LoginForm(
    account: String,
    password: String,
    captcha: String,
    captchaId: String,
    loading: Boolean,
    onAccountChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onCaptchaChange: (String) -> Unit,
    onCaptchaIdChange: (String) -> Unit,
    onSubmit: () -> Unit
) {
    OutlinedTextField(
        value = account,
        onValueChange = onAccountChange,
        modifier = Modifier.fillMaxWidth().widthIn(max = 420.dp),
        label = { Text("账号") },
        singleLine = true,
        enabled = !loading
    )
    OutlinedTextField(
        value = password,
        onValueChange = onPasswordChange,
        modifier = Modifier.fillMaxWidth().widthIn(max = 420.dp),
        label = { Text("密码") },
        singleLine = true,
        visualTransformation = PasswordVisualTransformation(),
        enabled = !loading
    )
    OutlinedTextField(
        value = captcha,
        onValueChange = onCaptchaChange,
        modifier = Modifier.fillMaxWidth().widthIn(max = 420.dp),
        label = { Text("验证码（可选）") },
        singleLine = true,
        enabled = !loading
    )
    OutlinedTextField(
        value = captchaId,
        onValueChange = onCaptchaIdChange,
        modifier = Modifier.fillMaxWidth().widthIn(max = 420.dp),
        label = { Text("验证码ID（可选）") },
        singleLine = true,
        enabled = !loading
    )

    Button(
        onClick = onSubmit,
        enabled = !loading,
        modifier = Modifier.fillMaxWidth().widthIn(max = 420.dp)
    ) {
        Text("登录")
    }
}

@Composable
private fun TenantSelection(
    tenants: List<com.forgex.mobile.core.network.model.auth.TenantVO>,
    selectedTenantId: String?,
    loading: Boolean,
    onSelectTenant: (String) -> Unit,
    onConfirm: () -> Unit,
    onBack: () -> Unit
) {
    Text(
        text = "请选择租户",
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.fillMaxWidth().widthIn(max = 420.dp)
    )

    if (tenants.isEmpty()) {
        Text(text = "暂无租户可选", color = Color.Gray)
        return
    }

    Column(
        modifier = Modifier.fillMaxWidth().widthIn(max = 420.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        tenants.forEach { tenant ->
            FilterChip(
                selected = selectedTenantId == tenant.id,
                onClick = { onSelectTenant(tenant.id) },
                label = { Text(tenant.name) },
                enabled = !loading,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    Button(
        onClick = onConfirm,
        enabled = !loading && !selectedTenantId.isNullOrBlank(),
        modifier = Modifier.fillMaxWidth().widthIn(max = 420.dp)
    ) {
        Text("确认进入")
    }

    Button(
        onClick = onBack,
        enabled = !loading,
        modifier = Modifier.fillMaxWidth().widthIn(max = 420.dp)
    ) {
        Text("返回登录")
    }
}
