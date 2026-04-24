package com.forgex.mobile.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.forgex.mobile.core.common.i18n.AppText
import com.forgex.mobile.core.model.FxScanResult
import com.forgex.mobile.core.common.result.AppResult
import com.forgex.mobile.core.datastore.ServerEndpointConfig
import com.forgex.mobile.core.datastore.SessionStore
import com.forgex.mobile.core.network.di.BaseUrl
import com.forgex.mobile.core.network.i18n.AppLanguageManager
import com.forgex.mobile.core.network.model.auth.SystemBasicConfig
import com.forgex.mobile.core.ui.R
import com.forgex.mobile.feature.auth.data.AuthRepository
import com.forgex.mobile.feature.auth.data.CaptchaMode
import dagger.hilt.android.lifecycle.HiltViewModel
import java.net.URI
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val sessionStore: SessionStore,
    private val appLanguageManager: AppLanguageManager,
    @BaseUrl private val baseUrl: String
) : ViewModel() {

    private val defaultServerOrigin: String = resolveDefaultOrigin(baseUrl)

    private val _uiState = MutableStateFlow(AuthUiState(serverOrigin = defaultServerOrigin))
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<AuthEvent>()
    val events: SharedFlow<AuthEvent> = _events.asSharedFlow()

    init {
        observeServerEndpoint()
        observeLanguageState()
        preloadSystemBasicConfig()
        refreshCaptchaModeAndData(silent = true)
        preloadPublicKey()
    }

    fun updateAccount(value: String) {
        _uiState.update {
            it.clearError().copy(
                account = value.trim(),
                latestAccountScanResult = null
            )
        }
    }

    fun updatePassword(value: String) {
        _uiState.update { it.clearError().copy(password = value) }
    }

    fun updateCaptcha(value: String) {
        _uiState.update { it.clearError().copy(captcha = value.trim()) }
    }

    fun updateSliderProgress(value: Float) {
        val normalized = value.coerceIn(0f, 1f)
        _uiState.update {
            it.clearError().copy(
                sliderProgress = normalized,
                sliderToken = ""
            )
        }
    }

    fun switchLoginMethod(method: LoginMethod) {
        if (method == LoginMethod.ACCOUNT_PASSWORD) {
            _uiState.update {
                it.copy(
                    loginStage = AuthLoginStage.PASSWORD_FORM,
                    selectedLoginMethod = method,
                    errorMessage = null,
                    errorText = null
                )
            }
            return
        }
        _uiState.update {
            it.copy(
                loginStage = AuthLoginStage.ENTRY,
                selectedLoginMethod = method,
                errorMessage = null,
                errorText = AppText.Resource(R.string.auth_third_party_pending)
            )
        }
        emitErrorMessage(AppText.Resource(R.string.auth_third_party_pending), null)
    }

    fun openPasswordLogin() {
        _uiState.update {
            it.copy(
                loginStage = AuthLoginStage.PASSWORD_FORM,
                selectedLoginMethod = LoginMethod.ACCOUNT_PASSWORD,
                errorMessage = null,
                errorText = null
            )
        }
    }

    fun backToLoginEntry() {
        _uiState.update {
            it.copy(
                loginStage = AuthLoginStage.ENTRY,
                errorMessage = null,
                errorText = null
            )
        }
    }

    fun refreshCaptcha(silent: Boolean = false) {
        when (_uiState.value.captchaMode) {
            CaptchaMode.IMAGE -> refreshImageCaptcha(silent)
            CaptchaMode.SLIDER -> refreshSliderCaptcha(silent)
            CaptchaMode.NONE -> refreshCaptchaModeAndData(silent)
        }
    }

    private fun observeServerEndpoint() {
        viewModelScope.launch {
            sessionStore.serverEndpoint.collectLatest { endpoint ->
                _uiState.update {
                    it.copy(serverOrigin = resolveServerOrigin(endpoint))
                }
            }
        }
    }

    private fun observeLanguageState() {
        viewModelScope.launch {
            appLanguageManager.observeUiState().collectLatest { state ->
                _uiState.update {
                    it.copy(
                        languageState = it.languageState.copy(
                            mode = state.mode,
                            currentLanguageTag = state.currentLanguageTag,
                            defaultLanguageTag = state.defaultLanguageTag,
                            languages = state.availableLanguages
                        )
                    )
                }
            }
        }
    }

    private fun preloadSystemBasicConfig() {
        viewModelScope.launch {
            when (val result = authRepository.loadSystemBasicConfig()) {
                is AppResult.Success -> applySystemConfig(result.data)
                is AppResult.Error -> sessionStore.saveSystemName(_uiState.value.systemName)
                AppResult.Loading -> Unit
            }
        }
    }

    private suspend fun applySystemConfig(config: SystemBasicConfig) {
        val systemName = config.systemName.safeOrDefault("FORGEX_MOM")
        val loginTitle = config.loginPageTitle.safeOrDefault("")
        val loginSubtitle = config.loginPageSubtitle.safeOrDefault("")
        val loginBackgroundType = config.loginBackgroundType.safeOrDefault("image")
        val loginBackgroundImage = config.loginBackgroundImage.safeOrDefault("")
        val loginBackgroundColor = config.loginBackgroundColor.safeOrDefault("#0B1E48")
        val showOAuthLogin = config.showOAuthLogin ?: true
        val showRegisterEntry = config.showRegisterEntry ?: true
        val registerUrl = config.registerUrl.safeOrDefault("/register")
        val primaryColor = config.primaryColor.safeOrDefault("#05d9e8")
        val secondaryColor = config.secondaryColor.safeOrDefault("#ff2a6d")

        sessionStore.saveSystemName(systemName)
        _uiState.update {
            it.copy(
                systemName = systemName,
                loginTitle = loginTitle,
                loginSubtitle = loginSubtitle,
                systemLogo = config.systemLogo.safeOrDefault(""),
                loginBackgroundType = loginBackgroundType,
                loginBackgroundImage = loginBackgroundImage,
                loginBackgroundColor = loginBackgroundColor,
                showOAuthLogin = showOAuthLogin,
                showRegisterEntry = showRegisterEntry,
                registerUrl = registerUrl,
                primaryColor = primaryColor,
                secondaryColor = secondaryColor
            )
        }
    }

    private fun refreshCaptchaModeAndData(silent: Boolean) {
        viewModelScope.launch {
            if (!silent) {
                _uiState.update { it.copy(isLoading = true, errorMessage = null, errorText = null) }
            }

            when (val result = authRepository.loadCaptchaMode()) {
                is AppResult.Success -> {
                    when (result.data) {
                        CaptchaMode.IMAGE -> {
                            _uiState.update {
                                it.copy(
                                    captchaMode = CaptchaMode.IMAGE,
                                    sliderCaptcha = null,
                                    sliderProgress = 0f,
                                    sliderToken = "",
                                    captcha = "",
                                    captchaId = "",
                                    captchaImageBase64 = "",
                                    isLoading = false,
                                    errorMessage = null,
                                    errorText = null
                                )
                            }
                            refreshImageCaptcha(silent = true)
                        }

                        CaptchaMode.SLIDER -> {
                            _uiState.update {
                                it.copy(
                                    captchaMode = CaptchaMode.SLIDER,
                                    captcha = "",
                                    captchaId = "",
                                    captchaImageBase64 = "",
                                    sliderProgress = 0f,
                                    sliderToken = "",
                                    sliderCaptcha = null,
                                    isLoading = false,
                                    errorMessage = null,
                                    errorText = null
                                )
                            }
                            refreshSliderCaptcha(silent = true)
                        }

                        CaptchaMode.NONE -> {
                            _uiState.update {
                                it.copy(
                                    captchaMode = CaptchaMode.NONE,
                                    captcha = "",
                                    captchaId = "",
                                    captchaImageBase64 = "",
                                    sliderProgress = 0f,
                                    sliderToken = "",
                                    sliderCaptcha = null,
                                    isLoading = false,
                                    errorMessage = null,
                                    errorText = null
                                )
                            }
                        }
                    }
                }

                is AppResult.Error -> {
                    _uiState.update {
                        it.copy(
                            captchaMode = CaptchaMode.IMAGE,
                            isLoading = false,
                            errorMessage = if (silent) null else result.message,
                            errorText = if (silent) null else result.appText
                        )
                    }
                    refreshImageCaptcha(silent = true)
                }

                AppResult.Loading -> _uiState.update { it.copy(isLoading = true) }
            }
        }
    }

    private fun refreshImageCaptcha(silent: Boolean = false) {
        viewModelScope.launch {
            if (!silent) {
                _uiState.update { it.copy(isLoading = true, errorMessage = null, errorText = null) }
            }

            when (val result = authRepository.loadImageCaptcha()) {
                is AppResult.Success -> {
                    _uiState.update {
                        it.copy(
                            captchaMode = CaptchaMode.IMAGE,
                            captchaId = result.data.captchaId,
                            captchaImageBase64 = result.data.imageBase64,
                            captcha = "",
                            sliderCaptcha = null,
                            sliderProgress = 0f,
                            sliderToken = "",
                            isLoading = false,
                            errorMessage = null,
                            errorText = null
                        )
                    }
                }

                is AppResult.Error -> {
                    _uiState.update {
                        it.copy(
                            captchaMode = CaptchaMode.IMAGE,
                            captchaId = "",
                            captchaImageBase64 = "",
                            captcha = "",
                            isLoading = false,
                            errorMessage = if (silent) null else result.message,
                            errorText = if (silent) null else result.appText
                        )
                    }
                }

                AppResult.Loading -> _uiState.update { it.copy(isLoading = true) }
            }
        }
    }

    private fun refreshSliderCaptcha(silent: Boolean = false) {
        viewModelScope.launch {
            if (!silent) {
                _uiState.update { it.copy(isLoading = true, errorMessage = null, errorText = null) }
            }

            when (val result = authRepository.loadSliderCaptcha()) {
                is AppResult.Success -> {
                    _uiState.update {
                        it.copy(
                            captchaMode = CaptchaMode.SLIDER,
                            sliderCaptcha = result.data,
                            sliderProgress = 0f,
                            sliderToken = "",
                            captcha = "",
                            captchaId = result.data.id,
                            captchaImageBase64 = "",
                            isLoading = false,
                            errorMessage = null,
                            errorText = null
                        )
                    }
                }

                is AppResult.Error -> {
                    _uiState.update {
                        it.copy(
                            captchaMode = CaptchaMode.NONE,
                            sliderCaptcha = null,
                            sliderProgress = 0f,
                            sliderToken = "",
                            captcha = "",
                            captchaId = "",
                            isLoading = false,
                            errorMessage = if (silent) null else result.message,
                            errorText = if (silent) null else result.appText
                        )
                    }
                }

                AppResult.Loading -> _uiState.update { it.copy(isLoading = true) }
            }
        }
    }

    fun verifySliderCaptcha() {
        val snapshot = _uiState.value
        val slider = snapshot.sliderCaptcha
        if (snapshot.captchaMode != CaptchaMode.SLIDER || slider == null) {
            _uiState.update {
                it.copy(
                    errorMessage = null,
                    errorText = AppText.Resource(R.string.auth_slider_mode_invalid)
                )
            }
            return
        }

        val maxLeft = (slider.backgroundImageWidth - slider.templateImageWidth).coerceAtLeast(0)
        if (maxLeft <= 0) {
            _uiState.update {
                it.copy(
                    errorMessage = null,
                    errorText = AppText.Resource(R.string.auth_slider_param_invalid)
                )
            }
            return
        }

        val left = snapshot.sliderProgress * maxLeft.toFloat()
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, errorText = null) }
            when (
                val result = authRepository.validateSliderCaptcha(
                    captchaId = slider.id,
                    left = left,
                    bgImageWidth = slider.backgroundImageWidth,
                    bgImageHeight = slider.backgroundImageHeight,
                    templateImageWidth = slider.templateImageWidth,
                    templateImageHeight = slider.templateImageHeight
                )
            ) {
                is AppResult.Success -> {
                    _uiState.update {
                        it.copy(
                            sliderToken = result.data,
                            captchaId = slider.id,
                            isLoading = false,
                            errorMessage = null,
                            errorText = null
                        )
                    }
                }

                is AppResult.Error -> {
                    _uiState.update {
                        it.copy(
                            sliderToken = "",
                            isLoading = false,
                            errorMessage = result.message,
                            errorText = result.appText
                        )
                    }
                }

                AppResult.Loading -> _uiState.update { it.copy(isLoading = true) }
            }
        }
    }

    private fun preloadPublicKey() {
        viewModelScope.launch {
            when (val result = authRepository.loadPublicKey()) {
                is AppResult.Success -> {
                    _uiState.update {
                        it.copy(
                            publicKeyLoaded = true,
                            publicKey = result.data
                        )
                    }
                }

                else -> {
                    _uiState.update {
                        it.copy(
                            publicKeyLoaded = false,
                            publicKey = ""
                        )
                    }
                }
            }
        }
    }

    fun selectTenant(tenantId: String) {
        _uiState.update {
            it.copy(
                selectedTenantId = tenantId,
                latestTenantScanResult = null,
                errorMessage = null,
                errorText = null
            )
        }
    }

    /**
     * 应用账号扫描结果到登录表单。
     */
    fun applyAccountScan(result: FxScanResult) {
        val account = result.rawValue.trim()
        if (account.isBlank()) {
            return
        }
        _uiState.update {
            it.clearError().copy(
                account = account,
                latestAccountScanResult = result
            )
        }
    }

    /**
     * 按扫描结果匹配租户并直接选中。
     */
    fun applyTenantScan(result: FxScanResult) {
        val keyword = result.rawValue.trim()
        if (keyword.isBlank()) {
            return
        }
        val matchedTenant = _uiState.value.tenants.firstOrNull { tenant ->
            tenant.id.equals(keyword, ignoreCase = true) ||
                tenant.name.equals(keyword, ignoreCase = true) ||
                tenant.name.contains(keyword, ignoreCase = true)
        } ?: return
        _uiState.update {
            it.copy(
                selectedTenantId = matchedTenant.id,
                latestTenantScanResult = result,
                errorMessage = null,
                errorText = null
            )
        }
    }

    /**
     * 标记账号扫描结果已被页面消费。
     */
    fun consumeAccountScanResult() {
        _uiState.update { it.copy(latestAccountScanResult = null) }
    }

    /**
     * 标记租户扫描结果已被页面消费。
     */
    fun consumeTenantScanResult() {
        _uiState.update { it.copy(latestTenantScanResult = null) }
    }

    fun showLanguageDialog() {
        _uiState.update {
            it.copy(
                languageState = it.languageState.copy(languageDialogVisible = true)
            )
        }
    }

    fun dismissLanguageDialog() {
        _uiState.update {
            it.copy(
                languageState = it.languageState.copy(languageDialogVisible = false)
            )
        }
    }

    fun followDefaultLanguage() {
        viewModelScope.launch {
            appLanguageManager.followSystem()
            dismissLanguageDialog()
        }
    }

    fun selectLanguage(languageTag: String) {
        viewModelScope.launch {
            appLanguageManager.selectLanguage(languageTag)
            dismissLanguageDialog()
        }
    }

    fun backToLogin() {
        _uiState.update {
            it.copy(
                step = AuthStep.LOGIN,
                tenants = emptyList(),
                selectedTenantId = null,
                errorMessage = null,
                errorText = null
            )
        }
    }

    fun submitLogin() {
        val snapshot = _uiState.value
        if (snapshot.selectedLoginMethod != LoginMethod.ACCOUNT_PASSWORD) {
            _uiState.update {
                it.copy(
                    errorMessage = null,
                    errorText = AppText.Resource(R.string.auth_third_party_pending)
                )
            }
            emitErrorMessage(AppText.Resource(R.string.auth_third_party_pending), null)
            return
        }
        if (snapshot.account.isBlank() || snapshot.password.isBlank()) {
            _uiState.update {
                it.copy(
                    errorMessage = null,
                    errorText = AppText.Resource(R.string.auth_required_account_password)
                )
            }
            emitErrorMessage(AppText.Resource(R.string.auth_required_account_password), null)
            return
        }

        when (snapshot.captchaMode) {
            CaptchaMode.IMAGE -> {
                if (snapshot.captchaId.isBlank() || snapshot.captcha.isBlank()) {
                    _uiState.update {
                        it.copy(
                            errorMessage = null,
                            errorText = AppText.Resource(R.string.auth_required_captcha)
                        )
                    }
                    emitErrorMessage(AppText.Resource(R.string.auth_required_captcha), null)
                    return
                }
            }

            CaptchaMode.SLIDER -> {
                if (snapshot.sliderToken.isBlank()) {
                    _uiState.update {
                        it.copy(
                            errorMessage = null,
                            errorText = AppText.Resource(R.string.auth_required_slider)
                        )
                    }
                    emitErrorMessage(AppText.Resource(R.string.auth_required_slider), null)
                    return
                }
            }

            CaptchaMode.NONE -> Unit
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, errorText = null) }

            val captcha = when (snapshot.captchaMode) {
                CaptchaMode.IMAGE -> snapshot.captcha
                CaptchaMode.SLIDER -> snapshot.sliderToken
                CaptchaMode.NONE -> null
            }
            val captchaId = when (snapshot.captchaMode) {
                CaptchaMode.IMAGE -> snapshot.captchaId
                else -> null
            }

            when (
                val result = authRepository.login(
                    account = snapshot.account,
                    password = snapshot.password,
                    captcha = captcha,
                    captchaId = captchaId,
                    publicKey = snapshot.publicKey.takeIf { it.isNotBlank() }
                )
            ) {
                is AppResult.Success -> {
                    val tenants = result.data
                    if (tenants.isEmpty()) {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = null,
                                errorText = AppText.Resource(R.string.auth_no_tenant)
                            )
                        }
                    } else {
                        val defaultTenant = tenants.firstOrNull { tenant -> tenant.isDefault == true }
                            ?: tenants.first()
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                step = AuthStep.TENANT_SELECTION,
                                tenants = tenants,
                                selectedTenantId = defaultTenant.id,
                                errorMessage = null,
                                errorText = null
                            )
                        }
                    }
                }

                is AppResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.message,
                            errorText = result.appText
                        )
                    }
                    emitErrorMessage(result.appText, result.message)
                    refreshCaptcha(silent = true)
                }

                AppResult.Loading -> _uiState.update { it.copy(isLoading = true) }
            }
        }
    }

    fun confirmTenantSelection() {
        val snapshot = _uiState.value
        val tenantId = snapshot.selectedTenantId

        if (snapshot.account.isBlank()) {
            _uiState.update {
                it.copy(
                    errorMessage = null,
                    errorText = AppText.Resource(R.string.auth_account_empty)
                )
            }
            emitErrorMessage(AppText.Resource(R.string.auth_account_empty), null)
            return
        }
        if (tenantId.isNullOrBlank()) {
            _uiState.update {
                it.copy(
                    errorMessage = null,
                    errorText = AppText.Resource(R.string.auth_tenant_required)
                )
            }
            emitErrorMessage(AppText.Resource(R.string.auth_tenant_required), null)
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, errorText = null) }

            when (val chooseResult = authRepository.chooseTenant(tenantId, snapshot.account)) {
                is AppResult.Success -> {
                    authRepository.loadUserRoutes(snapshot.account)
                    _uiState.update { it.copy(isLoading = false) }
                    _events.emit(AuthEvent.LoginCompleted)
                }

                is AppResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = chooseResult.message,
                            errorText = chooseResult.appText
                        )
                    }
                    emitErrorMessage(chooseResult.appText, chooseResult.message)
                }

                AppResult.Loading -> _uiState.update { it.copy(isLoading = true) }
            }
        }
    }

    private fun resolveServerOrigin(endpoint: ServerEndpointConfig?): String {
        if (endpoint == null || endpoint.host.isBlank()) {
            return defaultServerOrigin
        }
        val scheme = endpoint.scheme.ifBlank { "http" }
        return "$scheme://${endpoint.host}:${endpoint.port}"
    }

    private fun resolveDefaultOrigin(defaultBaseUrl: String): String {
        return runCatching {
            val uri = URI(defaultBaseUrl)
            val scheme = uri.scheme?.ifBlank { "http" } ?: "http"
            val host = uri.host?.ifBlank { "10.0.2.2" } ?: "10.0.2.2"
            val port = if (uri.port > 0) {
                uri.port
            } else if (scheme.equals("https", ignoreCase = true)) {
                443
            } else {
                80
            }
            "$scheme://$host:$port"
        }.getOrElse { "http://10.0.2.2:9000" }
    }

    private fun String?.safeOrDefault(default: String): String {
        return this?.trim().takeUnless { it.isNullOrBlank() } ?: default
    }

    private fun AuthUiState.clearError(): AuthUiState {
        return copy(errorMessage = null, errorText = null)
    }

    private fun emitErrorMessage(appText: AppText?, fallbackMessage: String?) {
        viewModelScope.launch {
            val normalizedFallback = fallbackMessage?.takeIf { it.isNotBlank() }
            if (appText != null || normalizedFallback != null) {
                _events.emit(AuthEvent.ShowMessage(appText, normalizedFallback))
            }
        }
    }
}
