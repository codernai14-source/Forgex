<template>
  <div class="login-wrap">
    <video
      v-if="systemConfig.loginBackgroundType === 'video'"
      class="bg-video"
      autoplay
      muted
      loop
      playsinline
      :src="resolveMediaUrl(systemConfig.loginBackgroundVideo)"
    ></video>
    <img
      v-if="systemConfig.loginBackgroundType === 'image'"
      class="bg-video"
      :src="resolveMediaUrl(systemConfig.loginBackgroundImage)"
      alt="login-background"
    />
    <div class="mask" :style="{ backgroundColor: systemConfig.loginBackgroundType === 'color' ? systemConfig.loginBackgroundColor : '' }"></div>
    <div class="grid"></div>
    <div class="content" :class="`layout-${systemConfig.loginLayout || 'center'}`">
      <div class="brand" v-show="!tenantOpen">
        <img v-if="resolveMediaUrl(systemConfig.systemLogo)" :src="resolveMediaUrl(systemConfig.systemLogo)" class="brand-logo" alt="system-logo" />
        <span v-else class="brand-blue">{{ systemConfig.systemName.split('_')[0] }}</span
        ><span v-if="!resolveMediaUrl(systemConfig.systemLogo)" class="brand-red">_{{ systemConfig.systemName.split('_')[1] || 'MOM' }}</span>
        <div class="brand-line"></div>
      </div>
      <div class="brand-sub" v-show="!tenantOpen">{{ systemConfig.loginPageTitle }}</div>
      <div class="brand-sub-desc" v-show="!tenantOpen">{{ systemConfig.loginPageSubtitle }}</div>
      <div class="glass-card" v-show="!tenantOpen">
        <form class="cyber-form" @submit.prevent="onPreLogin">
          <div class="field">
            <label class="cyber-label">{{ i18nT('common.login.accountLabel') }}</label>
            <input
              class="cyber-input"
              type="text"
              v-model="account"
              autocomplete="username"
              :placeholder="i18nT('common.login.accountPlaceholder')"
            />
          </div>
          <div class="field">
            <label class="cyber-label">{{ i18nT('common.login.passwordLabel') }}</label>
            <input
              class="cyber-input"
              type="password"
              v-model="password"
              autocomplete="current-password"
              :placeholder="i18nT('common.login.passwordPlaceholder')"
            />
          </div>
          <div class="field" v-if="mode === 'image'">
            <label class="cyber-label">{{ i18nT('common.login.captchaLabel') }}</label>
            <div class="captcha-row">
              <input
                class="cyber-input captcha-input"
                type="text"
                v-model="captcha"
                autocomplete="off"
                :placeholder="i18nT('common.login.captchaPlaceholder')"
              />
              <img class="captcha-img" :src="imageBase64" alt="captcha" @click="loadImage" />
            </div>
          </div>
          <div class="field" v-if="mode === 'slider'">
            <label class="cyber-label">{{ i18nT('common.login.behaviorCaptchaLabel') }}</label>
            <div>
              <a-button size="small" @click="openSlider">{{ i18nT('common.login.startSlider') }}</a-button>
              <span style="margin-left: 8px; color: #9ca3af;"
                >{{ sliderVerified ? i18nT('common.success') : i18nT('common.login.sliderAutoFillTip') }}</span
              >
            </div>
          </div>
          <div class="form-tools">
            <label class="remember">
              <input type="checkbox" v-model="remember" />
              <span>{{ i18nT('common.login.rememberMe') }}</span>
            </label>
            <a-dropdown v-if="languages.length > 0" placement="bottom" trigger="click" overlay-class-name="login-lang-dropdown">
              <button type="button" class="lang-switch-compact__trigger" :title="currentLanguageLabel">
                <img :src="LANG_SWITCH_ICON_SRC" alt="language" class="lang-switch-compact__icon" />
              </button>
              <template #overlay>
                <a-menu :selected-keys="[selectedLang]" @click="onLanguageMenuClick">
                  <a-menu-item v-for="l in languages" :key="l.langCode">
                    <span class="lang-menu-item" :class="{ active: selectedLang === l.langCode }">
                      {{ getLanguageLabel(l) }}
                    </span>
                  </a-menu-item>
                </a-menu>
              </template>
            </a-dropdown>
            <a class="forgot" href="#">{{ i18nT('common.login.forgotPassword') }}</a>
          </div>
          <button
            type="submit"
            class="btn-gradient block-btn"
            :disabled="logging"
            :class="{ 'btn-disabled': logging }"
            :style="{ '--primary-color': systemConfig.primaryColor, '--secondary-color': systemConfig.secondaryColor }"
          >
            <span>{{ i18nT('common.login.submit') }}</span>
            <span v-if="logging" class="spinner"></span>
          </button>
          <div v-if="showRegisterEntry" class="register-hint">
            <span>{{ i18nT('common.login.noAccount') }}</span>
            <a class="register-link" href="#" @click.prevent="goToRegister">{{ i18nT('common.login.register') }}</a>
          </div>
          <div class="divider" v-if="systemConfig.showOAuthLogin"><span>{{ i18nT('common.login.moreLoginMethods') }}</span></div>
          <div class="oauth-row" v-if="systemConfig.showOAuthLogin">
            <button type="button" class="oauth-btn gitee" title="Gitee">
              <img src="/tubiao/GITEE.svg" alt="Gitee" />
            </button>
            <button type="button" class="oauth-btn wechat" :title="i18nT('common.login.platform.wechat')" @click="onOAuth('WECHAT')">
              <img src="/tubiao/weixin2.svg" :alt="i18nT('common.login.platform.wechat')" />
            </button>
            <button type="button" class="oauth-btn dingtalk" :title="i18nT('common.login.platform.dingtalk')" @click="onOAuth('DINGTALK')">
              <img src="/tubiao/dingding.svg" :alt="i18nT('common.login.platform.dingtalk')" />
            </button>
          </div>
        </form>
      </div>
      <div class="copyright">{{ systemConfig.copyright }}</div>
    </div>
    <div v-if="tenantOpen" class="identity-overlay">
      <div class="identity-global-tools">
        <a-button size="small" type="default" @click="toggleSort">{{ i18nT('common.login.setSort') }}</a-button>
      </div>
      <div class="identity-container">
        <div
          class="tenant-grid identity-grid"
          :class="{ single: tenants.length === 1 }"
        >
          <div
            v-for="(t, idx) in tenants"
            :key="t.id"
            class="tenant-card"
            :class="{ selected: t.id === chosenTenant, big: tenants.length === 1 }"
            @click="choose(t)"
          >
            <div class="tenant-logo">
              <img v-if="t.logo" :src="resolveTenantLogo(t.logo)" alt="logo" />
              <div v-else class="logo-fallback">
                {{ t.name?.[0] || 'T' }}
              </div>
            </div>
            <div class="tenant-info">
              <div class="tenant-name">{{ t.name }}</div>
              <div class="tenant-intro">{{ t.intro || i18nT('common.login.noIntro') }}</div>
            </div>
            <div class="tenant-tools" v-if="showSort" @click.stop>
              <a-button size="small" @click="moveUp(idx)" style="margin-right: 6px;"
                >{{ i18nT('common.login.moveUp') }}</a-button
              >
              <a-button size="small" @click="moveDown(idx)" style="margin-right: 6px;"
                >{{ i18nT('common.login.moveDown') }}</a-button
              >
              <a-button
                size="small"
                type="default"
                :class="{ star: t.isDefault === true }"
                @click="toggleDefault(t)"
                >{{ i18nT('common.login.defaultTenant') }}</a-button>
            </div>
          </div>
        </div>
        <div class="tenant-actions identity-actions">
          <a-button
            v-if="showSort"
            type="default"
            @click="savePreferences"
            class="action-btn"
            >{{ i18nT('common.login.saveSort') }}</a-button
          >
          <a-button
            type="primary"
            @click="confirmTenant"
            :loading="tenantConfirming"
            :disabled="tenantConfirming"
            class="action-btn primary"
            >{{ i18nT('common.login.chooseIdentity') }}</a-button
          >
        </div>
      </div>
    </div>
    <a-modal
      v-model:open="sliderOpen"
      :title="i18nT('common.login.sliderTitle')"
      :footer="null"
      width="720"
      @afterOpen="initSlider"
    >
      <div class="slider-panel">
        <div
          class="slider-canvas"
          :style="{
            width: `${sliderPreviewWidth}px`,
            height: `${sliderPreviewHeight}px`
          }"
        >
          <img
            v-if="sliderChallenge?.backgroundImage"
            class="slider-bg"
            :src="sliderChallenge.backgroundImage"
            alt="slider-background"
          />
          <img
            v-if="sliderChallenge?.templateImage"
            class="slider-piece"
            :src="sliderChallenge.templateImage"
            alt="slider-piece"
            :style="{
              width: `${sliderTemplateWidth * sliderPreviewScale}px`,
              height: `${sliderTemplateHeight * sliderPreviewScale}px`,
              left: `${sliderValue * sliderPreviewScale}px`,
              top: `${sliderPieceTop * sliderPreviewScale}px`
            }"
          />
        </div>
        <a-slider
          v-model:value="sliderValue"
          :min="0"
          :max="sliderMax"
          :tooltip="{ open: false }"
          @change="onSliderDrag"
        />
        <div class="slider-actions">
          <a-space>
            <a-button @click="initSlider">{{ i18nT('common.refresh') }}</a-button>
            <a-button type="primary" :loading="sliderVerifying" @click="verifySliderCaptcha">
              {{ i18nT('common.confirm') }}
            </a-button>
          </a-space>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { message } from 'ant-design-vue'
import { useI18n } from 'vue-i18n'
import {
  login,
  chooseTenant,
  getSocialAuthorizeUrl,
  updateTenantPreferences,
  type TenantOption
} from '../../../api/auth/login'
import { captchaImage, captchaSlider, captchaSliderValidate } from '../../../api/auth/captcha'
import { getRoutes } from '../../../api/system/route'
import router, { PERSONAL_HOME_PATH, injectDynamicRoutes } from '../../../router'
import { getLoginCaptcha, getSystemBasicConfig } from '../../../api/system/config'
import { reloadTenantIgnore } from '../../../api/system/tenant'
import { listEnabledLanguages, type LanguageType } from '../../../api/system/i18n'
import { encryptSensitiveText } from '@/utils/crypto'
import { useUserStore } from '@/stores/user'
import { usePermissionStore } from '@/stores/permission'
import type { SystemBasicConfig } from '../../../api/system/config'
import { getLocale, setLocale } from '@/locales'
import { getLanguageDisplayName, LANG_SWITCH_ICON_SRC } from '@/utils/language'
import { normalizeMediaUrl } from '@/utils/media'

/**
 * 后端返回的滑块验证码数据结构。
 */
interface SliderCaptchaChallenge {
  id: string
  backgroundImage: string
  templateImage: string
  backgroundImageWidth: number
  backgroundImageHeight: number
  templateImageWidth: number
  templateImageHeight: number
}

/**
 * 用户拖动滑块时记录的单个轨迹点。
 */
interface SliderTrackPoint {
  x: number
  y: number
  t: number
  type: string
}

/**
 * 提交给后端的滑块校验完整载荷。
 */
interface SliderTrackPayload {
  bgImageWidth: number
  bgImageHeight: number
  templateImageWidth: number
  templateImageHeight: number
  startTime: number
  stopTime: number
  left: number
  top: number
  trackList: SliderTrackPoint[]
}

// 初始化登录和租户选择流程共用的状态仓库。
const userStore = useUserStore()
const permissionStore = usePermissionStore()

const { t: i18nT } = useI18n({ useScope: 'global' })

const account = ref('')
const password = ref('')
const remember = ref(false)
const captcha = ref('')
const captchaId = ref('')
const imageBase64 = ref('')
const mode = ref<'none' | 'image' | 'slider'>('none')
const tenants = ref<TenantOption[]>([])
const interactionCode = ref('')
const tenantOpen = ref(false)
const chosenTenant = ref<string | null>(null)
const sliderOpen = ref(false)
const sliderVerifying = ref(false)
const sliderVerified = ref(false)
const sliderChallenge = ref<SliderCaptchaChallenge | null>(null)
const sliderValue = ref(0)
const sliderTrackStartAt = ref(0)
const logging = ref(false)
const tenantConfirming = ref(false)
const showSort = ref(false)
const languages = ref<LanguageType[]>([])
const selectedLang = ref<string>(getLocale())

const systemConfig = ref<SystemBasicConfig>({
  systemName: 'FORGEX_MOM',
  systemLogo: '',
  systemVersion: '1.0.0',
  copyright: '© 2025 FORGEX_MOM',
  copyrightLink: '#',
  loginPageTitle: i18nT('common.login.defaultTitle'),
  loginPageSubtitle: '',
  loginBackgroundType: 'image',
  loginBackgroundVideo: '/loading.mp4',
  loginBackgroundImage: '/back.jpg',
  loginBackgroundColor: '#0d0221',
  loginStyle: 'cyber',
  loginLayout: 'center',
  showOAuthLogin: true,
  showRegisterEntry: true,
  registerUrl: '/register',
  primaryColor: '#05d9e8',
  secondaryColor: '#ff2a6d'
})

const showRegisterEntry = computed(() => systemConfig.value.showRegisterEntry !== false)

const currentLanguageLabel = computed(() => {
  return getLanguageDisplayName(languages.value.find(l => l.langCode === selectedLang.value))
})

const sliderTemplateWidth = computed(() => {
  return sliderChallenge.value?.templateImageWidth && sliderChallenge.value.templateImageWidth > 0
    ? sliderChallenge.value.templateImageWidth
    : 52
})

const sliderTemplateHeight = computed(() => {
  return sliderChallenge.value?.templateImageHeight && sliderChallenge.value.templateImageHeight > 0
    ? sliderChallenge.value.templateImageHeight
    : 52
})

const sliderMax = computed(() => {
  if (!sliderChallenge.value) return 0
  const max = sliderChallenge.value.backgroundImageWidth - sliderTemplateWidth.value
  return max > 0 ? max : 0
})

const sliderPieceTop = computed(() => {
  if (!sliderChallenge.value) return 0
  const top = (sliderChallenge.value.backgroundImageHeight - sliderTemplateHeight.value) / 2
  return top > 0 ? top : 0
})

const sliderPreviewScale = computed(() => {
  if (!sliderChallenge.value || !sliderChallenge.value.backgroundImageWidth) return 1
  const maxWidth = 520
  const w = sliderChallenge.value.backgroundImageWidth
  return w > maxWidth ? maxWidth / w : 1
})

const sliderPreviewWidth = computed(() => {
  if (!sliderChallenge.value) return 520
  return sliderChallenge.value.backgroundImageWidth * sliderPreviewScale.value
})

const sliderPreviewHeight = computed(() => {
  if (!sliderChallenge.value) return 280
  return sliderChallenge.value.backgroundImageHeight * sliderPreviewScale.value
})

function resolveMediaUrl(value: string): string {
  return normalizeMediaUrl(value)
}

function resolveTenantLogo(url?: string): string {
  return resolveMediaUrl(String(url || ''))
}

function resolveLangIcon(icon?: string): string {
  return resolveMediaUrl(String(icon || ''))
}

function formatMediaUrl(value: string): string {
  return normalizeMediaUrl(value)
}

function formatTenantLogo(url?: string) {
  return normalizeMediaUrl(url)
}

function formatLangIcon(icon?: string) {
  return normalizeMediaUrl(icon)
}

function getLanguageLabel(language: LanguageType) {
  return getLanguageDisplayName(language)
}

function isExternalUrl(url: string) {
  return /^https?:\/\//i.test(String(url || '').trim())
}

function resolveRegisterUrl() {
  const configured = String(systemConfig.value.registerUrl || '').trim()
  return configured || '/register'
}

async function goToRegister() {
  const target = resolveRegisterUrl()
  if (!target) {
    return
  }
  if (isExternalUrl(target)) {
    window.open(target, '_blank', 'noopener')
    return
  }
  await router.push(target)
}

async function refreshLoginCaptchaAfterFailure() {
  captcha.value = ''
  if (mode.value === 'image') {
    await loadImage()
    return
  }
  if (mode.value === 'slider') {
    sliderVerified.value = false
    sliderValue.value = 0
    sliderChallenge.value = null
    if (sliderOpen.value) {
      await initSlider()
    }
  }
}

async function loadLanguages() {
  try {
    const list = await listEnabledLanguages()
    languages.value = Array.isArray(list) ? list : []
    if (languages.value.length === 0) {
      return
    }

    const current = selectedLang.value
    const matchCurrent = languages.value.some(l => l.langCode === current)
    if (matchCurrent) {
      return
    }

    const def = languages.value.find(l => l.isDefault === true)
    const next = def?.langCode || languages.value[0].langCode
    selectedLang.value = next
    setLocale(next as any)
  } catch (_) {}
}

function onLangChange(val: string) {
  if (!val) return
  selectedLang.value = val
  setLocale(val as any)
}

function onLanguageMenuClick(info: { key?: string }) {
  const nextLang = String(info?.key || '')
  if (!nextLang || nextLang === selectedLang.value) {
    return
  }
  onLangChange(nextLang)
}

async function loadMode() {
  try {
    const cfg = await getLoginCaptcha()
    mode.value = cfg && cfg.mode ? cfg.mode : 'none'
    captcha.value = ''
    captchaId.value = ''
    sliderVerified.value = false
    sliderChallenge.value = null
    sliderValue.value = 0
    if (mode.value === 'image') {
      await loadImage()
    }
  } catch (_) {}
}

async function loadImage() {
  try {
    const img = await captchaImage()
    captchaId.value = img && img.captchaId ? img.captchaId : ''
    imageBase64.value = img && img.imageBase64 ? 'data:image/png;base64,' + img.imageBase64 : ''
  } catch (_) {}
}

async function onPreLogin() {
  try {
    if (logging.value) {
      return
    }
    if (mode.value === 'slider' && !sliderVerified.value) {
      message.warning(i18nT('common.login.startSlider'))
      return
    }
    if (mode.value === 'image' && (!captchaId.value || !captcha.value)) {
      message.warning(i18nT('common.login.captchaPlaceholder'))
      return
    }
    logging.value = true
    tenantOpen.value = false
    tenants.value = []
    interactionCode.value = ''
    chosenTenant.value = null
    let pwdToSend = ''
    try {
      pwdToSend = await encryptSensitiveText(password.value)
    } catch (e: any) {
      message.error(e?.message || i18nT('common.operationFailed'))
      return
    }
    const res = await login({
      account: account.value,
      password: pwdToSend,
      captcha: captcha.value,
      captchaId: mode.value === 'image' ? captchaId.value : undefined
    })
    tenants.value = Array.isArray(res?.tenants) ? res.tenants : []
    interactionCode.value = res?.interactionCode || ''
    if (tenants.value.length > 0) {
      tenantOpen.value = true
    } else {
      message.error(i18nT('common.login.msg.noTenantBound'))
    }
  } catch (e) {
    await refreshLoginCaptchaAfterFailure()
  } finally {
    logging.value = false
  }
}

async function onOAuth(platform: 'WECHAT' | 'DINGTALK') {
  try {
    const res = await getSocialAuthorizeUrl(platform)
    const url = (res as any)?.data ?? res
    if (!url) {
      message.error(i18nT('common.login.msg.oauthDisabled'))
      return
    }
    window.location.href = url
  } catch (e) {
    message.error(i18nT('common.login.msg.oauthUrlFailed'))
  }
}

async function confirmTenant() {
  if (!chosenTenant.value || tenantConfirming.value) {
    if (tenantConfirming.value) return
    message.warning(i18nT('common.login.msg.selectTenantFirst'))
    return
  }
  const current = tenants.value.find(t => t.id === chosenTenant.value)
  if (!current) {
    message.error(i18nT('common.login.msg.invalidTenant'))
    tenantOpen.value = false
    return
  }
  const transitionLoader = (window as any).__fxLoginTransitionLoader
  let loadingHandedOff = false
  tenantConfirming.value = true
  transitionLoader?.show?.()

  try {
    const result = await chooseTenant({ 
      tenantId: chosenTenant.value,
      account: account.value,
      interactionCode: interactionCode.value
    })
    interactionCode.value = ''
    // 后端会返回当前租户上下文，认证信息仍通过 Cookie 维持。
    if (result && result.account) {
      // 先缓存当前用户信息，便于外层框架立即渲染头像和名称。
      const nextTenantId = String(result.tenantId || chosenTenant.value)
      userStore.setUserInfo({
        account: result.account,
        username: result.username || result.account || account.value,
        email: result.email,
        phone: result.phone,
        avatar: result.avatar,
        tenantId: nextTenantId,
        tenantName: current.name
      })
      
      // 将账号和租户写入 sessionStorage，供路由守卫和刷新恢复使用。
      sessionStorage.setItem('account', result.account)
      sessionStorage.setItem('tenantId', nextTenantId)
      
      // 确认租户后再拉取该租户下的路由配置。
      const routesRes = await getRoutes({
        account: result.account,
        tenantId: nextTenantId
      })
      
      // 进入系统前先保存按钮级权限，避免页面初次渲染缺权限数据。
      if (routesRes && routesRes.buttons) {
        permissionStore.setPermissions(routesRes.buttons)
      } else {
        // 后端未返回权限时主动清空，避免沿用旧租户的残留权限。
        permissionStore.setPermissions([])
      }
      
      // 保存路由和模块信息，供菜单渲染与路由控制使用。
      if (routesRes && routesRes.routes) {
        permissionStore.setRoutes(routesRes.routes)
      }
      if (routesRes && routesRes.modules) {
        permissionStore.setModules(routesRes.modules)
      }
      
      await injectDynamicRoutes(routesRes)
      
      if (remember.value) {
        localStorage.setItem('fx-remember-account', account.value)
      } else {
        localStorage.removeItem('fx-remember-account')
      }
      
      // 主框架挂载完成后会释放加载层，整个初始化过程不会回闪登录页。
      await router.replace(PERSONAL_HOME_PATH)
      loadingHandedOff = true
    } else {
      message.error(i18nT('common.login.msg.chooseTenantFailed'))
    }
  } catch (e: any) {
    interactionCode.value = ''
    tenantOpen.value = false
    tenants.value = []
    chosenTenant.value = null
    console.error('[Login] 选择租户或初始化系统失败:', e)
    message.error(i18nT('common.login.msg.chooseTenantFailed'))
  } finally {
    tenantConfirming.value = false
    if (!loadingHandedOff) {
      transitionLoader?.hide?.()
    }
  }
}

async function openSlider() {
  sliderOpen.value = true
}

async function initSlider() {
  try {
    const raw = await captchaSlider()
    const challenge = raw as any
    const bgWidth = Number(challenge?.backgroundImageWidth || 280)
    const bgHeight = Number(challenge?.backgroundImageHeight || 158)
    const tplWidth = Number(challenge?.templateImageWidth || 52)
    const tplHeight = Number(challenge?.templateImageHeight || 52)
    sliderChallenge.value = {
      id: String(challenge?.id || ''),
      backgroundImage: resolveMediaUrl(String(challenge?.backgroundImage || '')),
      templateImage: resolveMediaUrl(String(challenge?.templateImage || '')),
      backgroundImageWidth: bgWidth > 0 ? bgWidth : 280,
      backgroundImageHeight: bgHeight > 0 ? bgHeight : 158,
      templateImageWidth: tplWidth > 0 ? tplWidth : 52,
      templateImageHeight: tplHeight > 0 ? tplHeight : 52
    }
    sliderValue.value = 0
    sliderTrackStartAt.value = Date.now()
    sliderVerified.value = false
    captcha.value = ''
  } catch (e) {
    sliderChallenge.value = null
    sliderVerified.value = false
    captcha.value = ''
    message.error(i18nT('common.loadFailed'))
  }
}

function buildSliderTrackList(targetX: number, duration: number): SliderTrackPoint[] {
  const pointCount = 18
  const list: SliderTrackPoint[] = []
  for (let i = 0; i < pointCount; i++) {
    const progress = i / (pointCount - 1)
    const eased = 1 - Math.pow(1 - progress, 2)
    const x = Number((targetX * eased).toFixed(2))
    const y = Number((2 + Math.sin(progress * Math.PI * 1.8) * 2 + (Math.random() - 0.5) * 0.8).toFixed(2))
    const t = Number((duration * progress).toFixed(2))
    list.push({
      x,
      y,
      t,
      type: 'move'
    })
  }
  return list
}

function onSliderDrag(value: number) {
  sliderValue.value = Number.isFinite(value) ? value : 0
}

async function verifySliderCaptcha() {
  if (!sliderChallenge.value?.id) {
    message.warning(i18nT('common.loadFailed'))
    return
  }
  if (sliderValue.value <= 0) {
    message.warning(i18nT('common.login.startSlider'))
    return
  }
  sliderVerifying.value = true
  try {
    const startTime = sliderTrackStartAt.value || Date.now()
    const duration = Math.max(Date.now() - startTime, 420)
    const stopTime = startTime + duration
    const payload: SliderTrackPayload = {
      bgImageWidth: sliderChallenge.value.backgroundImageWidth,
      bgImageHeight: sliderChallenge.value.backgroundImageHeight,
      templateImageWidth: sliderChallenge.value.templateImageWidth,
      templateImageHeight: sliderChallenge.value.templateImageHeight,
      startTime,
      stopTime,
      left: sliderValue.value,
      top: sliderPieceTop.value,
      trackList: buildSliderTrackList(sliderValue.value, duration)
    }
    const token = await captchaSliderValidate({
      id: sliderChallenge.value.id,
      track: payload
    })
    captcha.value = String(token || '')
    sliderVerified.value = !!captcha.value
    if (sliderVerified.value) {
      sliderOpen.value = false
      message.success(i18nT('common.success'))
    } else {
      message.error(i18nT('common.operationFailed'))
      await initSlider()
    }
  } catch (e) {
    sliderVerified.value = false
    captcha.value = ''
    await initSlider()
  } finally {
    sliderVerifying.value = false
  }
}

function choose(t: { id: string }) {
  chosenTenant.value = t.id
}

function toggleSort() {
  showSort.value = !showSort.value
}

function moveUp(idx: number) {
  if (idx <= 0) return
  const arr = tenants.value.slice()
  const tmp = arr[idx - 1]
  arr[idx - 1] = arr[idx]
  arr[idx] = tmp
  tenants.value = arr
}

function moveDown(idx: number) {
  const arr = tenants.value.slice()
  if (idx >= arr.length - 1) return
  const tmp = arr[idx + 1]
  arr[idx + 1] = arr[idx]
  arr[idx] = tmp
  tenants.value = arr
}

async function toggleDefault(t: any) {
  try {
    const newVal = t.isDefault === true ? false : true
    await updateTenantPreferences({
      account: account.value,
      ordered: tenants.value.map(t => t.id),
      defaultTenantId: newVal === true ? t.id : undefined
    })
    await reloadTenantIgnore()
    t.isDefault = newVal
    message.success(i18nT('common.login.msg.defaultTenantUpdated'))
  } catch (e) {
    message.error(i18nT('common.login.msg.updateDefaultTenantFailed'))
  }
}

async function savePreferences() {
  try {
    await updateTenantPreferences({
      account: account.value,
      ordered: tenants.value.map(t => t.id)
    })
    await reloadTenantIgnore()
    message.success(i18nT('common.login.msg.sortSaved'))
  } catch (e) {
    message.error(i18nT('common.login.msg.sortSaveFailed'))
  }
}

onMounted(async () => {
  try {
    const remembered = localStorage.getItem('fx-remember-account')
    if (remembered) {
      account.value = remembered
      remember.value = true
    }
  } catch (_) {}
  
  try {
    const config = await getSystemBasicConfig()
    if (config) {
      systemConfig.value = { ...config }
    }
  } catch (_) {}
  
  await loadLanguages()
  await loadMode()
})

</script>

<style scoped lang="less" src="@/styles/views/auth/login/index.less"></style>

<style lang="less" src="@/styles/views/auth/login/index-global.less"></style>
