package com.forgex.mobile.feature.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.forgex.mobile.feature.auth.data.CaptchaMode
import com.forgex.mobile.feature.auth.data.SliderCaptcha
import kotlin.math.roundToInt
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
                captchaMode = uiState.captchaMode,
                captcha = uiState.captcha,
                captchaImageBase64 = uiState.captchaImageBase64,
                sliderCaptcha = uiState.sliderCaptcha,
                sliderProgress = uiState.sliderProgress,
                sliderToken = uiState.sliderToken,
                loading = uiState.isLoading,
                publicKeyLoaded = uiState.publicKeyLoaded,
                onAccountChange = viewModel::updateAccount,
                onPasswordChange = viewModel::updatePassword,
                onCaptchaChange = viewModel::updateCaptcha,
                onSliderProgressChange = viewModel::updateSliderProgress,
                onVerifySlider = viewModel::verifySliderCaptcha,
                onRefreshCaptcha = { viewModel.refreshCaptcha(silent = false) },
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
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 420.dp)
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
    captchaMode: CaptchaMode,
    captcha: String,
    captchaImageBase64: String,
    sliderCaptcha: SliderCaptcha?,
    sliderProgress: Float,
    sliderToken: String,
    loading: Boolean,
    publicKeyLoaded: Boolean,
    onAccountChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onCaptchaChange: (String) -> Unit,
    onSliderProgressChange: (Float) -> Unit,
    onVerifySlider: () -> Unit,
    onRefreshCaptcha: () -> Unit,
    onSubmit: () -> Unit
) {
    OutlinedTextField(
        value = account,
        onValueChange = onAccountChange,
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 420.dp),
        label = { Text("账号") },
        singleLine = true,
        enabled = !loading
    )

    OutlinedTextField(
        value = password,
        onValueChange = onPasswordChange,
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 420.dp),
        label = { Text("密码") },
        singleLine = true,
        visualTransformation = PasswordVisualTransformation(),
        enabled = !loading
    )

    when (captchaMode) {
        CaptchaMode.IMAGE -> {
            ImageCaptchaPanel(
                captcha = captcha,
                captchaImageBase64 = captchaImageBase64,
                loading = loading,
                onCaptchaChange = onCaptchaChange,
                onRefreshCaptcha = onRefreshCaptcha
            )
        }

        CaptchaMode.SLIDER -> {
            SliderCaptchaPanel(
                sliderCaptcha = sliderCaptcha,
                sliderProgress = sliderProgress,
                sliderToken = sliderToken,
                loading = loading,
                onSliderProgressChange = onSliderProgressChange,
                onVerifySlider = onVerifySlider,
                onRefreshCaptcha = onRefreshCaptcha
            )
        }

        CaptchaMode.NONE -> {
            Text(
                text = "当前环境无需验证码",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 420.dp)
            )
        }
    }

    Text(
        text = if (publicKeyLoaded) {
            "已启用 SM2 公钥预热，登录密码将加密传输"
        } else {
            "暂未获取到加密公钥，当前按联调模式发送密码"
        },
        style = MaterialTheme.typography.bodySmall,
        color = Color.Gray,
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 420.dp)
    )

    Button(
        onClick = onSubmit,
        enabled = !loading,
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 420.dp)
    ) {
        Text("登录")
    }
}

@Composable
private fun ImageCaptchaPanel(
    captcha: String,
    captchaImageBase64: String,
    loading: Boolean,
    onCaptchaChange: (String) -> Unit,
    onRefreshCaptcha: () -> Unit
) {
    OutlinedTextField(
        value = captcha,
        onValueChange = onCaptchaChange,
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 420.dp),
        label = { Text("验证码") },
        singleLine = true,
        enabled = !loading
    )

    val captchaModel = toDataUri(captchaImageBase64)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 420.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = rememberAsyncImagePainter(model = captchaModel),
            contentDescription = "captcha",
            modifier = Modifier
                .weight(1f)
                .height(56.dp),
            contentScale = ContentScale.FillBounds
        )
        Button(
            onClick = onRefreshCaptcha,
            enabled = !loading
        ) {
            Text("刷新")
        }
    }
}

@Composable
private fun SliderCaptchaPanel(
    sliderCaptcha: SliderCaptcha?,
    sliderProgress: Float,
    sliderToken: String,
    loading: Boolean,
    onSliderProgressChange: (Float) -> Unit,
    onVerifySlider: () -> Unit,
    onRefreshCaptcha: () -> Unit
) {
    if (sliderCaptcha == null) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 420.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("滑块验证码加载中...", color = Color.Gray)
            Button(onClick = onRefreshCaptcha, enabled = !loading) {
                Text("重试")
            }
        }
        return
    }

    val backgroundModel = toDataUri(sliderCaptcha.backgroundImageBase64)
    val templateModel = toDataUri(sliderCaptcha.templateImageBase64)
    val bgWidth = sliderCaptcha.backgroundImageWidth.coerceAtLeast(1)
    val bgHeight = sliderCaptcha.backgroundImageHeight.coerceAtLeast(1)
    val templateWidth = sliderCaptcha.templateImageWidth.coerceAtLeast(1)
    val templateHeight = sliderCaptcha.templateImageHeight.coerceAtLeast(1)

    val imageAspectRatio = bgWidth.toFloat() / bgHeight.toFloat()
    val templateWidthRate = (templateWidth.toFloat() / bgWidth.toFloat()).coerceIn(0.05f, 1f)
    val templateHeightRate = (templateHeight.toFloat() / bgHeight.toFloat()).coerceIn(0.05f, 1f)

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 420.dp)
            .height((220 / imageAspectRatio).dp.coerceAtLeast(120.dp))
    ) {
        val boxWidthPx = constraints.maxWidth.toFloat().coerceAtLeast(1f)
        val boxHeightPx = constraints.maxHeight.toFloat().coerceAtLeast(1f)
        val templateWidthPx = (boxWidthPx * templateWidthRate).coerceAtLeast(8f)
        val templateHeightPx = (boxHeightPx * templateHeightRate).coerceAtLeast(8f)
        val maxOffsetPx = (boxWidthPx - templateWidthPx).coerceAtLeast(0f)
        val currentOffsetPx = sliderProgress.coerceIn(0f, 1f) * maxOffsetPx

        Image(
            painter = rememberAsyncImagePainter(model = backgroundModel),
            contentDescription = "slider-background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )

        Image(
            painter = rememberAsyncImagePainter(model = templateModel),
            contentDescription = "slider-template",
            modifier = Modifier
                .offset {
                    IntOffset(
                        currentOffsetPx.roundToInt(),
                        ((boxHeightPx - templateHeightPx) / 2f).roundToInt()
                    )
                }
                .size(
                    width = maxWidth * (templateWidthPx / boxWidthPx),
                    height = maxHeight * (templateHeightPx / boxHeightPx)
                ),
            contentScale = ContentScale.FillBounds
        )
    }

    Slider(
        value = sliderProgress.coerceIn(0f, 1f),
        onValueChange = onSliderProgressChange,
        valueRange = 0f..1f,
        enabled = !loading,
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 420.dp)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 420.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = onVerifySlider,
            enabled = !loading
        ) {
            Text("验证滑块")
        }
        Button(
            onClick = onRefreshCaptcha,
            enabled = !loading
        ) {
            Text("刷新")
        }
    }

    Text(
        text = if (sliderToken.isNotBlank()) {
            "滑块验证已通过，可直接登录"
        } else {
            "拖动拼图后点击“验证滑块”"
        },
        style = MaterialTheme.typography.bodySmall,
        color = if (sliderToken.isNotBlank()) MaterialTheme.colorScheme.primary else Color.Gray,
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 420.dp)
    )
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
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 420.dp)
    )

    if (tenants.isEmpty()) {
        Text(text = "暂无租户可选", color = Color.Gray)
        return
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 420.dp),
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
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 420.dp)
    ) {
        Text("确认进入")
    }

    Button(
        onClick = onBack,
        enabled = !loading,
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 420.dp)
    ) {
        Text("返回登录")
    }
}

private fun toDataUri(rawBase64: String): String {
    return if (rawBase64.startsWith("data:image")) {
        rawBase64
    } else {
        "data:image/png;base64,$rawBase64"
    }
}
