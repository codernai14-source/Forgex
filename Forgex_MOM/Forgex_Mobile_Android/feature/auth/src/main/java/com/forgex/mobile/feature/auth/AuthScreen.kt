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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import com.forgex.mobile.core.ui.R
import com.forgex.mobile.core.ui.i18n.resolveAppText
import com.forgex.mobile.feature.auth.data.CaptchaMode
import com.forgex.mobile.feature.auth.data.SliderCaptcha
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.collectLatest

const val AUTH_ROUTE = "auth"
const val SERVER_SETTINGS_ROUTE = "auth/server-settings"
const val REGISTER_ROUTE = "auth/register"

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

    Box(modifier = modifier.fillMaxSize()) {
        LoginBackground(
            imageLoader = imageLoader,
            backgroundRaw = uiState.loginBackgroundImage,
            backgroundType = uiState.loginBackgroundType,
            backgroundColor = uiState.loginBackgroundColor,
            serverOrigin = uiState.serverOrigin
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFF7F9FF),
                            Color(0xFFF2F4FB)
                        )
                    )
                )
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (uiState.step == AuthStep.LOGIN) {
                when (uiState.loginStage) {
                    AuthLoginStage.ENTRY -> LoginEntryCard(
                        uiState = uiState,
                        imageLoader = imageLoader,
                        onOpenServerSettings = onOpenServerSettings,
                        onOpenPasswordLogin = viewModel::openPasswordLogin,
                        onSelectThirdParty = viewModel::switchLoginMethod
                    )

                    AuthLoginStage.PASSWORD_FORM -> LoginCard(
                        uiState = uiState,
                        imageLoader = imageLoader,
                        onOpenServerSettings = onOpenServerSettings,
                        onOpenRegister = onOpenRegister,
                        onBackToEntry = viewModel::backToLoginEntry,
                        onAccountChange = viewModel::updateAccount,
                        onPasswordChange = viewModel::updatePassword,
                        onCaptchaChange = viewModel::updateCaptcha,
                        onSliderProgressChange = viewModel::updateSliderProgress,
                        onVerifySlider = viewModel::verifySliderCaptcha,
                        onRefreshCaptcha = { viewModel.refreshCaptcha(silent = false) },
                        onSubmit = viewModel::submitLogin
                    )
                }
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
private fun LoginEntryCard(
    uiState: AuthUiState,
    imageLoader: ImageLoader,
    onOpenServerSettings: () -> Unit,
    onOpenPasswordLogin: () -> Unit,
    onSelectThirdParty: (LoginMethod) -> Unit
) {
    val errorText = uiState.errorMessage ?: resolveAppText(uiState.errorText)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 420.dp),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            LogoHeader(
                imageLoader = imageLoader,
                systemName = uiState.systemName,
                title = uiState.loginTitle.ifBlank { stringResource(R.string.auth_login_select_title) },
                subtitle = uiState.loginSubtitle.ifBlank { stringResource(R.string.auth_login_select_subtitle) },
                logoRaw = uiState.systemLogo,
                serverOrigin = uiState.serverOrigin,
                onOpenServerSettings = onOpenServerSettings
            )

            Spacer(modifier = Modifier.height(34.dp))

            LoginEntryButton(
                icon = Icons.Outlined.Lock,
                text = stringResource(R.string.auth_login_method_account),
                primary = true,
                onClick = onOpenPasswordLogin
            )

            Spacer(modifier = Modifier.height(14.dp))

            LoginEntryButton(
                icon = Icons.Outlined.Notifications,
                text = stringResource(R.string.auth_login_method_dingtalk),
                primary = false,
                onClick = { onSelectThirdParty(LoginMethod.DING_TALK) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            LoginEntryButton(
                icon = Icons.Outlined.Person,
                text = stringResource(R.string.auth_login_method_wechat),
                primary = false,
                onClick = { onSelectThirdParty(LoginMethod.WECHAT) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            LoginEntryButton(
                icon = Icons.Outlined.Person,
                text = stringResource(R.string.auth_login_method_gitee),
                primary = false,
                onClick = { onSelectThirdParty(LoginMethod.GITEE) }
            )

            Spacer(modifier = Modifier.height(22.dp))

            Text(
                text = stringResource(R.string.auth_server_current, uiState.serverOrigin),
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF7A7F91)
            )

            if (!errorText.isNullOrBlank()) {
                Text(
                    text = errorText,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 14.dp)
                )
            }
        }
    }
}

@Composable
private fun LoginEntryButton(
    icon: ImageVector,
    text: String,
    primary: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp),
        shape = RoundedCornerShape(28.dp),
        contentPadding = PaddingValues(horizontal = 18.dp),
        colors = if (primary) {
            ButtonDefaults.buttonColors(
                containerColor = Color(0xFF3E63F5),
                contentColor = Color.White
            )
        } else {
            ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = Color(0xFF1F2433)
            )
        }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.size(10.dp))
            Text(text = text, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun LoginCard(
    uiState: AuthUiState,
    imageLoader: ImageLoader,
    onOpenServerSettings: () -> Unit,
    onOpenRegister: (String) -> Unit,
    onBackToEntry: () -> Unit,
    onAccountChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onCaptchaChange: (String) -> Unit,
    onSliderProgressChange: (Float) -> Unit,
    onVerifySlider: () -> Unit,
    onRefreshCaptcha: () -> Unit,
    onSubmit: () -> Unit
) {
    val title = uiState.loginTitle.ifBlank { stringResource(R.string.auth_login_method_account) }
    val errorText = uiState.errorMessage ?: resolveAppText(uiState.errorText)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 480.dp),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFDFEFF)
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(onClick = onBackToEntry) {
                    Icon(Icons.Outlined.ArrowBack, contentDescription = null)
                    Spacer(modifier = Modifier.size(6.dp))
                    Text(stringResource(R.string.auth_back_to_login_methods))
                }
            }

            LogoHeader(
                imageLoader = imageLoader,
                systemName = uiState.systemName,
                title = title,
                subtitle = uiState.loginSubtitle,
                logoRaw = uiState.systemLogo,
                serverOrigin = uiState.serverOrigin,
                onOpenServerSettings = onOpenServerSettings
            )

            Text(
                text = stringResource(R.string.auth_server_current, uiState.serverOrigin),
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF506080),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = uiState.account,
                onValueChange = onAccountChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.auth_account)) },
                singleLine = true,
                enabled = !uiState.isLoading
            )

            OutlinedTextField(
                value = uiState.password,
                onValueChange = onPasswordChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.auth_password)) },
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
                        text = stringResource(R.string.auth_captcha_not_required),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF5B6478),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Text(
                text = if (uiState.publicKeyLoaded) {
                    stringResource(R.string.auth_public_key_ready)
                } else {
                    stringResource(R.string.auth_public_key_missing)
                },
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF5B6478),
                modifier = Modifier.fillMaxWidth()
            )

            if (!errorText.isNullOrBlank()) {
                Text(
                    text = errorText,
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
                Text(text = stringResource(R.string.auth_login))
            }

            if (uiState.showRegisterEntry) {
                OutlinedButton(
                    onClick = {
                        onOpenRegister(resolveRegisterUrl(uiState.registerUrl, uiState.serverOrigin))
                    },
                    enabled = !uiState.isLoading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.auth_register))
                }
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
            text = stringResource(R.string.auth_click_logo_server),
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF1486A2),
            modifier = Modifier.padding(top = 4.dp)
        )
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
        label = { Text(stringResource(R.string.auth_captcha)) },
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
                        text = stringResource(R.string.auth_captcha_load_failed),
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
            Text(stringResource(R.string.auth_captcha_refresh))
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
    if (sliderCaptcha == null) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.auth_slider_loading),
                color = Color(0xFF6B7280)
            )
            OutlinedButton(onClick = onRefreshCaptcha, enabled = !loading) {
                Text(stringResource(R.string.common_retry))
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
            Text(stringResource(R.string.auth_slider_verify))
        }
        OutlinedButton(
            onClick = onRefreshCaptcha,
            enabled = !loading
        ) {
            Text(stringResource(R.string.common_refresh))
        }
    }

    Text(
        text = if (sliderToken.isNotBlank()) {
            stringResource(R.string.auth_slider_verified)
        } else {
            stringResource(R.string.auth_slider_hint)
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
    val errorText = uiState.errorMessage ?: resolveAppText(uiState.errorText)

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
                text = stringResource(R.string.auth_tenant_select),
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF1E2A45)
            )

            if (uiState.tenants.isEmpty()) {
                Text(
                    text = stringResource(R.string.auth_tenant_empty),
                    color = Color(0xFF6B7280)
                )
            } else {
                uiState.tenants.forEach { tenant ->
                    OutlinedButton(
                        onClick = { onSelectTenant(tenant.id) },
                        enabled = !uiState.isLoading,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (uiState.selectedTenantId == tenant.id) {
                                Color(0xFFE8EEFF)
                            } else {
                                Color.White
                            }
                        )
                    ) {
                        Text(tenant.name)
                    }
                }
            }

            if (!errorText.isNullOrBlank()) {
                Text(
                    text = errorText,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Button(
                onClick = onConfirm,
                enabled = !uiState.isLoading && !uiState.selectedTenantId.isNullOrBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.auth_tenant_enter))
            }

            OutlinedButton(
                onClick = onBack,
                enabled = !uiState.isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.auth_back_login))
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
    if (input.isEmpty()) return Color(0xFFF4F6FB)
    return runCatching { Color(android.graphics.Color.parseColor(input)) }
        .getOrElse { Color(0xFFF4F6FB) }
}

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

private fun resolveRegisterUrl(raw: String, serverOrigin: String): String {
    val value = raw.trim()
    if (value.isBlank()) return ""
    if (value.startsWith("http://") || value.startsWith("https://")) return value
    if (value.startsWith("/")) return "$serverOrigin$value"
    return "$serverOrigin/$value"
}

private fun isLikelyBase64(raw: String): Boolean {
    if (raw.length < 40) return false
    return raw.matches(Regex("^[A-Za-z0-9+/=,:;._-]+$"))
}
