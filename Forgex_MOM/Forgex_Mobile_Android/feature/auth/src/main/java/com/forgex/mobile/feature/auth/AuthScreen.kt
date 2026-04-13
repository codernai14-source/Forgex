package com.forgex.mobile.feature.auth

import android.os.Build
import android.util.Base64
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import com.forgex.mobile.feature.auth.data.CaptchaMode
import com.forgex.mobile.feature.auth.data.SliderCaptcha
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.collectLatest

const val AUTH_ROUTE = "auth"
const val SERVER_SETTINGS_ROUTE = "auth/server-settings"
const val REGISTER_ROUTE = "auth/register"

/**
 * 登录主页面：
 * 1. 展示系统配置驱动的标题/Logo/背景。
 * 2. 承载账号登录、验证码校验、租户选择。
 * 3. 提供服务器设置与注册入口。
 */
@Composable
fun AuthScreen(
    onLoginSuccess: () -> Unit,
    onOpenServerSettings: () -> Unit,
    onOpenRegister: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val imageLoader = rememberGifImageLoader()

    LaunchedEffect(viewModel) {
        viewModel.events.collectLatest { event ->
            if (event is AuthEvent.LoginCompleted) {
                onLoginSuccess()
            }
        }
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // 背景层：系统配置图/GIF/颜色
        LoginBackground(
            imageLoader = imageLoader,
            backgroundRaw = uiState.loginBackgroundImage,
            backgroundType = uiState.loginBackgroundType,
            backgroundColor = uiState.loginBackgroundColor,
            serverOrigin = uiState.serverOrigin
        )

        // 遮罩层：提升前景卡片可读性
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xA61A1F3A),
                            Color(0xCC0B1025)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (uiState.step == AuthStep.LOGIN) {
                LoginCard(
                    uiState = uiState,
                    imageLoader = imageLoader,
                    onOpenServerSettings = onOpenServerSettings,
                    onOpenRegister = onOpenRegister,
                    onSwitchLoginMethod = viewModel::switchLoginMethod,
                    onAccountChange = viewModel::updateAccount,
                    onPasswordChange = viewModel::updatePassword,
                    onCaptchaChange = viewModel::updateCaptcha,
                    onSliderProgressChange = viewModel::updateSliderProgress,
                    onVerifySlider = viewModel::verifySliderCaptcha,
                    onRefreshCaptcha = { viewModel.refreshCaptcha(silent = false) },
                    onSubmit = viewModel::submitLogin
                )
            } else {
                TenantSelectionCard(
                    uiState = uiState,
                    onSelectTenant = viewModel::selectTenant,
                    onConfirm = viewModel::confirmTenantSelection,
                    onBack = viewModel::backToLogin
                )
            }

            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
            }
        }
    }
}

@Composable
private fun LoginCard(
    uiState: AuthUiState,
    imageLoader: ImageLoader,
    onOpenServerSettings: () -> Unit,
    onOpenRegister: (String) -> Unit,
    onSwitchLoginMethod: (LoginMethod) -> Unit,
    onAccountChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onCaptchaChange: (String) -> Unit,
    onSliderProgressChange: (Float) -> Unit,
    onVerifySlider: () -> Unit,
    onRefreshCaptcha: () -> Unit,
    onSubmit: () -> Unit
) {
    // 登录区采用卡片容器，方便后续主题皮肤切换
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 480.dp),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xEAF8FAFF)
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LogoHeader(
                imageLoader = imageLoader,
                systemName = uiState.systemName,
                title = uiState.loginTitle,
                subtitle = uiState.loginSubtitle,
                logoRaw = uiState.systemLogo,
                serverOrigin = uiState.serverOrigin,
                onOpenServerSettings = onOpenServerSettings
            )

            Text(
                text = "当前服务器: ${uiState.serverOrigin}",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF506080),
                modifier = Modifier.fillMaxWidth()
            )

            LoginMethodRow(
                selected = uiState.selectedLoginMethod,
                showOAuthLogin = true,
                onSwitchLoginMethod = onSwitchLoginMethod
            )

            OutlinedTextField(
                value = uiState.account,
                onValueChange = onAccountChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("账号") },
                singleLine = true,
                enabled = !uiState.isLoading
            )

            OutlinedTextField(
                value = uiState.password,
                onValueChange = onPasswordChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("密码") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                enabled = !uiState.isLoading
            )

            when (uiState.captchaMode) {
                CaptchaMode.IMAGE -> {
                    ImageCaptchaPanel(
                        imageLoader = imageLoader,
                        captcha = uiState.captcha,
                        captchaImageRaw = uiState.captchaImageBase64,
                        serverOrigin = uiState.serverOrigin,
                        loading = uiState.isLoading,
                        onCaptchaChange = onCaptchaChange,
                        onRefreshCaptcha = onRefreshCaptcha
                    )
                }

                CaptchaMode.SLIDER -> {
                    SliderCaptchaPanel(
                        imageLoader = imageLoader,
                        sliderCaptcha = uiState.sliderCaptcha,
                        sliderProgress = uiState.sliderProgress,
                        sliderToken = uiState.sliderToken,
                        loading = uiState.isLoading,
                        onSliderProgressChange = onSliderProgressChange,
                        onVerifySlider = onVerifySlider,
                        onRefreshCaptcha = onRefreshCaptcha
                    )
                }

                CaptchaMode.NONE -> {
                    Text(
                        text = "当前环境无需验证码",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF5B6478),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Text(
                text = if (uiState.publicKeyLoaded) {
                    "已启用 SM2 公钥预热，密码会加密后传输"
                } else {
                    "未获取到公钥，当前按联调模式发送密码"
                },
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF5B6478),
                modifier = Modifier.fillMaxWidth()
            )

            if (!uiState.errorMessage.isNullOrBlank()) {
                Text(
                    text = uiState.errorMessage.orEmpty(),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Button(
                onClick = onSubmit,
                enabled = !uiState.isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "登录")
            }

            OutlinedButton(
                onClick = {
                    onOpenRegister(resolveRegisterUrl(uiState.registerUrl, uiState.serverOrigin))
                },
                enabled = !uiState.isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("注册")
            }
        }
    }
}

@Composable
private fun LogoHeader(
    imageLoader: ImageLoader,
    systemName: String,
    title: String,
    subtitle: String,
    logoRaw: String,
    serverOrigin: String,
    onOpenServerSettings: () -> Unit
) {
    val logoModel = remember(logoRaw, serverOrigin) {
        resolveImageModel(logoRaw, serverOrigin)
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        if (logoModel != null) {
            AsyncImage(
                model = logoModel,
                imageLoader = imageLoader,
                contentDescription = "system-logo",
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .border(2.dp, Color(0xFF18B9D8), CircleShape)
                    .clickable(onClick = onOpenServerSettings),
                contentScale = ContentScale.Crop
            )
        } else {
            Surface(
                modifier = Modifier
                    .size(76.dp)
                    .clickable(onClick = onOpenServerSettings),
                shape = CircleShape,
                color = Color(0xFF0D6B89)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = systemName.trim().take(1).ifBlank { "F" },
                        color = Color.White,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Text(
            text = title.ifBlank { systemName },
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF1E2A45),
            modifier = Modifier.padding(top = 8.dp)
        )
        if (subtitle.isNotBlank()) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF5B6478),
                modifier = Modifier.padding(top = 2.dp)
            )
        }
        Text(
            text = "点击系统图标可切换服务器",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF1486A2),
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
private fun LoginMethodRow(
    selected: LoginMethod,
    showOAuthLogin: Boolean,
    onSwitchLoginMethod: (LoginMethod) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selected == LoginMethod.ACCOUNT_PASSWORD,
                onClick = { onSwitchLoginMethod(LoginMethod.ACCOUNT_PASSWORD) },
                label = { Text("账号登录") }
            )
            if (showOAuthLogin) {
                AssistChip(
                    onClick = { onSwitchLoginMethod(LoginMethod.WECHAT) },
                    label = { Text("微信(开发中)") }
                )
                AssistChip(
                    onClick = { onSwitchLoginMethod(LoginMethod.DING_TALK) },
                    label = { Text("钉钉(开发中)") }
                )
            }
        }
        if (showOAuthLogin) {
            AssistChip(
                onClick = { onSwitchLoginMethod(LoginMethod.FEI_SHU) },
                label = { Text("飞书(开发中)") }
            )
        }
    }
}

@Composable
private fun ImageCaptchaPanel(
    imageLoader: ImageLoader,
    captcha: String,
    captchaImageRaw: String,
    serverOrigin: String,
    loading: Boolean,
    onCaptchaChange: (String) -> Unit,
    onRefreshCaptcha: () -> Unit
) {
    OutlinedTextField(
        value = captcha,
        onValueChange = onCaptchaChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text("验证码") },
        singleLine = true,
        enabled = !loading
    )

    val captchaModel = remember(captchaImageRaw, serverOrigin) {
        resolveImageModel(captchaImageRaw, serverOrigin)
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier
                .weight(1f)
                .height(58.dp)
                .border(1.dp, Color(0xFFCFD8E8), RoundedCornerShape(10.dp)),
            shape = RoundedCornerShape(10.dp),
            color = Color.White
        ) {
            if (captchaModel != null) {
                AsyncImage(
                    model = captchaModel,
                    imageLoader = imageLoader,
                    contentDescription = "captcha",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds
                )
            } else {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "验证码加载失败",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF6B7280)
                    )
                }
            }
        }
        OutlinedButton(
            onClick = onRefreshCaptcha,
            enabled = !loading
        ) {
            Text("刷新")
        }
    }
}

@Composable
private fun SliderCaptchaPanel(
    imageLoader: ImageLoader,
    sliderCaptcha: SliderCaptcha?,
    sliderProgress: Float,
    sliderToken: String,
    loading: Boolean,
    onSliderProgressChange: (Float) -> Unit,
    onVerifySlider: () -> Unit,
    onRefreshCaptcha: () -> Unit
) {
    // 滑块数据还没拿到时，给出显式加载反馈和重试入口
    if (sliderCaptcha == null) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("滑块验证码加载中...", color = Color(0xFF6B7280))
            OutlinedButton(onClick = onRefreshCaptcha, enabled = !loading) {
                Text("重试")
            }
        }
        return
    }

    val backgroundModel = remember(sliderCaptcha.backgroundImageBase64) {
        resolveImageModel(sliderCaptcha.backgroundImageBase64, "")
    }
    val templateModel = remember(sliderCaptcha.templateImageBase64) {
        resolveImageModel(sliderCaptcha.templateImageBase64, "")
    }
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
            .height((220 / imageAspectRatio).dp.coerceAtLeast(120.dp))
    ) {
        // 通过后端原始尺寸等比换算，确保滑块位移与校验值一致
        val boxWidthPx = constraints.maxWidth.toFloat().coerceAtLeast(1f)
        val boxHeightPx = constraints.maxHeight.toFloat().coerceAtLeast(1f)
        val templateWidthPx = (boxWidthPx * templateWidthRate).coerceAtLeast(8f)
        val templateHeightPx = (boxHeightPx * templateHeightRate).coerceAtLeast(8f)
        val maxOffsetPx = (boxWidthPx - templateWidthPx).coerceAtLeast(0f)
        val currentOffsetPx = sliderProgress.coerceIn(0f, 1f) * maxOffsetPx

        if (backgroundModel != null) {
            AsyncImage(
                model = backgroundModel,
                imageLoader = imageLoader,
                contentDescription = "slider-background",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds
            )
        }

        if (templateModel != null) {
            AsyncImage(
                model = templateModel,
                imageLoader = imageLoader,
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
    }

    Slider(
        value = sliderProgress.coerceIn(0f, 1f),
        onValueChange = onSliderProgressChange,
        valueRange = 0f..1f,
        enabled = !loading,
        modifier = Modifier.fillMaxWidth()
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = onVerifySlider,
            enabled = !loading
        ) {
            Text("验证滑块")
        }
        OutlinedButton(
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
        color = if (sliderToken.isNotBlank()) MaterialTheme.colorScheme.primary else Color(0xFF6B7280),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun TenantSelectionCard(
    uiState: AuthUiState,
    onSelectTenant: (String) -> Unit,
    onConfirm: () -> Unit,
    onBack: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 460.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xEAF8FAFF)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "请选择租户",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF1E2A45)
            )

            if (uiState.tenants.isEmpty()) {
                Text(text = "暂无租户可选", color = Color(0xFF6B7280))
            } else {
                uiState.tenants.forEach { tenant ->
                    FilterChip(
                        selected = uiState.selectedTenantId == tenant.id,
                        onClick = { onSelectTenant(tenant.id) },
                        label = { Text(tenant.name) },
                        enabled = !uiState.isLoading,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            if (!uiState.errorMessage.isNullOrBlank()) {
                Text(
                    text = uiState.errorMessage.orEmpty(),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Button(
                onClick = onConfirm,
                enabled = !uiState.isLoading && !uiState.selectedTenantId.isNullOrBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("确认进入")
            }

            OutlinedButton(
                onClick = onBack,
                enabled = !uiState.isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("返回登录")
            }
        }
    }
}

@Composable
private fun LoginBackground(
    imageLoader: ImageLoader,
    backgroundRaw: String,
    backgroundType: String,
    backgroundColor: String,
    serverOrigin: String
) {
    // 未配置背景图时默认尝试 /loading.gif，满足“优先 GIF 背景”的体验需求
    val backgroundModel = remember(backgroundRaw, serverOrigin) {
        val preferred = if (backgroundRaw.isBlank()) {
            "$serverOrigin/loading.gif"
        } else {
            backgroundRaw
        }
        resolveImageModel(preferred, serverOrigin)
    }

    val parsedColor = parseHexColor(backgroundColor)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(parsedColor)
    ) {
        if (backgroundType.equals("image", ignoreCase = true) && backgroundModel != null) {
            AsyncImage(
                model = backgroundModel,
                imageLoader = imageLoader,
                contentDescription = "login-background",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
private fun rememberGifImageLoader(): ImageLoader {
    val context = LocalContext.current
    return remember(context) {
        ImageLoader.Builder(context)
            .components {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }
            .crossfade(true)
            .build()
    }
}

private fun parseHexColor(raw: String): Color {
    val input = raw.trim()
    if (input.isEmpty()) return Color(0xFF0B1E48)
    return runCatching { Color(android.graphics.Color.parseColor(input)) }
        .getOrElse { Color(0xFF0B1E48) }
}

/**
 * 统一解析图片来源：
 * 1. 绝对 URL；
 * 2. dataUri；
 * 3. base64；
 * 4. 站内相对路径（自动拼接当前服务器地址）。
 */
private fun resolveImageModel(raw: String, serverOrigin: String): Any? {
    val value = raw.trim()
    if (value.isBlank()) return null
    if (value.startsWith("http://") || value.startsWith("https://")) return value
    if (value.startsWith("data:image")) return value
    if (value.startsWith("/")) return "$serverOrigin$value"

    val noSpace = value.replace("\\s".toRegex(), "")
    if (isLikelyBase64(noSpace)) {
        return runCatching { Base64.decode(noSpace.substringAfter("base64,", noSpace), Base64.DEFAULT) }
            .getOrElse { "data:image/png;base64,$noSpace" }
    }
    return "$serverOrigin/$value"
}

/**
 * 注册链接解析：
 * - 外链保持原样；
 * - 相对路径按当前服务器地址拼接成可访问 URL。
 */
private fun resolveRegisterUrl(raw: String, serverOrigin: String): String {
    val value = raw.trim()
    if (value.isBlank()) return ""
    if (value.startsWith("http://") || value.startsWith("https://")) return value
    if (value.startsWith("/")) return "$serverOrigin$value"
    return "$serverOrigin/$value"
}

/**
 * 轻量判定字符串是否可能是 base64 图片内容。
 */
private fun isLikelyBase64(raw: String): Boolean {
    if (raw.length < 40) return false
    return raw.matches(Regex("^[A-Za-z0-9+/=,:;._-]+$"))
}
