<template>
  <div class="login-wrap">
    <video
      v-if="systemConfig.loginBackgroundType === 'video'"
      class="bg-video"
      autoplay
      muted
      loop
      playsinline
      :src="formatMediaUrl(systemConfig.loginBackgroundVideo)"
    ></video>
    <img
      v-if="systemConfig.loginBackgroundType === 'image'"
      class="bg-video"
      :src="formatMediaUrl(systemConfig.loginBackgroundImage)"
    />
    <div class="mask" :style="{ backgroundColor: systemConfig.loginBackgroundType === 'color' ? systemConfig.loginBackgroundColor : '' }"></div>
    <div class="grid"></div>
    <div class="content" :class="`layout-${systemConfig.loginLayout || 'center'}`">
      <div class="brand" v-show="!tenantOpen">
        <img v-if="formatMediaUrl(systemConfig.systemLogo)" :src="formatMediaUrl(systemConfig.systemLogo)" class="brand-logo" />
        <span v-else class="brand-blue">{{ systemConfig.systemName.split('_')[0] }}</span
        ><span v-if="!formatMediaUrl(systemConfig.systemLogo)" class="brand-red">_{{ systemConfig.systemName.split('_')[1] || 'MOM' }}</span>
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
              :placeholder="i18nT('common.login.accountPlaceholder')"
            />
          </div>
          <div class="field">
            <label class="cyber-label">{{ i18nT('common.login.passwordLabel') }}</label>
            <input
              class="cyber-input"
              type="password"
              v-model="password"
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
                :placeholder="i18nT('common.login.captchaPlaceholder')"
              />
              <img class="captcha-img" :src="imageBase64" @click="loadImage" />
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
            <a-select
              v-if="languages.length > 0"
              v-model:value="selectedLang"
              size="small"
              class="lang-select-compact"
              :dropdownMatchSelectWidth="false"
              @change="onLangChange"
            >
              <a-select-option v-for="l in languages" :key="l.id" :value="l.langCode">
                <span class="lang-option">
                  <span class="lang-emoji">{{ resolveLangEmoji(l.langCode) }}</span>
                  <span class="lang-label">{{ l.langName }}</span>
                </span>
              </a-select-option>
            </a-select>
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
          <div class="divider" v-if="systemConfig.showOAuthLogin"><span>{{ i18nT('common.login.moreLoginMethods') }}</span></div>
          <div class="oauth-row" v-if="systemConfig.showOAuthLogin">
            <button type="button" class="oauth-btn gitee" title="Gitee">
              <img src="/tubiao/GITEE.svg" alt="Gitee" />
            </button>
            <button type="button" class="oauth-btn wechat" :title="i18nT('common.login.platform.wechat')" @click="onOAuth('WECHAT')">
              <img src="/tubiao/weixin2.svg" alt="微信" />
            </button>
            <button type="button" class="oauth-btn dingtalk" :title="i18nT('common.login.platform.dingtalk')" @click="onOAuth('DINGTALK')">
              <img src="/tubiao/dingding.svg" alt="钉钉" />
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
              <img v-if="t.logo" :src="formatTenantLogo(t.logo)" alt="logo" />
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
import { ref, onMounted, nextTick, computed } from 'vue'
import { message } from 'ant-design-vue'
import { useI18n } from 'vue-i18n'
import {
  login,
  chooseTenant,
  getPublicKey,
  getSocialAuthorizeUrl,
  updateTenantPreferences
} from '../../../api/auth/login'
import { captchaImage, captchaSlider, captchaSliderValidate } from '../../../api/auth/captcha'
import { getRoutes } from '../../../api/system/route'
import router, { injectDynamicRoutes } from '../../../router'
import { getLoginCaptcha, getSystemBasicConfig } from '../../../api/system/config'
import { reloadTenantIgnore } from '../../../api/system/tenant'
import { getInitStatus } from '../../../api/system/init'
import { listEnabledLanguages, type LanguageType } from '../../../api/system/i18n'
import { sm2 } from 'sm-crypto'
import { useUserStore } from '@/stores/user'
import { usePermissionStore } from '@/stores/permission'
import { getCurrentUserInfo } from '@/api/profile'
import type { SystemBasicConfig } from '../../../api/system/config'
import { getLocale, setLocale } from '@/locales'

interface SliderCaptchaChallenge {
  id: string
  backgroundImage: string
  templateImage: string
  backgroundImageWidth: number
  backgroundImageHeight: number
  templateImageWidth: number
  templateImageHeight: number
}

interface SliderTrackPoint {
  x: number
  y: number
  t: number
  type: string
}

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

// 初始化 stores
const userStore = useUserStore()
const permissionStore = usePermissionStore()

const { t: i18nT } = useI18n({ useScope: 'global' })

const account = ref('admin')
const password = ref('password')
const remember = ref(false)
const captcha = ref('')
const captchaId = ref('')
const imageBase64 = ref('')
const mode = ref<'none' | 'image' | 'slider'>('none')
const tenants = ref<{ id: string; name: string; logo?: string; intro?: string; isDefault?: boolean }[]>([])
const tenantOpen = ref(false)
const chosenTenant = ref<string | null>(null)
const sliderOpen = ref(false)
const sliderVerifying = ref(false)
const sliderVerified = ref(false)
const sliderChallenge = ref<SliderCaptchaChallenge | null>(null)
const sliderValue = ref(0)
const sliderTrackStartAt = ref(0)
const logging = ref(false)
const publicKeyCache = ref<string>('')
const showSort = ref(false)
const languages = ref<LanguageType[]>([])
const selectedLang = ref<string>(getLocale())

const systemConfig = ref<SystemBasicConfig>({
  systemName: 'FORGEX_MOM',
  systemLogo: '',
  systemVersion: '1.0.0',
  copyright: '© 2025 FORGEX_MOM',
  copyrightLink: '#',
  loginPageTitle: '欢迎来到FORGEX_MOM！',
  loginPageSubtitle: '',
  loginBackgroundType: 'video',
  loginBackgroundVideo: '/jws.mp4',
  loginBackgroundImage: '',
  loginBackgroundColor: '#0d0221',
  loginStyle: 'cyber',
  loginLayout: 'center',
  showOAuthLogin: true,
  primaryColor: '#05d9e8',
  secondaryColor: '#ff2a6d'
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

function formatMediaUrl(value: string): string {
  const url = String(value || '')
  if (!url) return ''
  if (url.startsWith('data:') || url.startsWith('http://') || url.startsWith('https://')) {
    return url
  }
  // 处理 /files/ 开头的路径（文件上传返回的路径）
  if (url.startsWith('/files/')) {
    return `/api${url}`
  }
  if (url.startsWith('/')) {
    return url.startsWith('/api') ? url : `/api${url}`
  }
  return `/api/${url}`
}

function formatTenantLogo(url?: string) {
  if (!url) return ''
  if (url.startsWith('data:') || url.startsWith('http://') || url.startsWith('https://')) {
    return url
  }
  // 处理 /files/ 开头的路径（文件上传返回的路径）
  if (url.startsWith('/files/')) {
    return `/api${url}`
  }
  if (url.startsWith('/')) {
    return url.startsWith('/api') ? url : `/api${url}`
  }
  return `/api/${url}`
}

function formatLangIcon(icon?: string) {
  if (!icon) return ''
  if (icon.startsWith('data:') || icon.startsWith('http://') || icon.startsWith('https://')) {
    return icon
  }
  // 处理 /files/ 开头的路径（文件上传返回的路径）
  if (icon.startsWith('/files/')) {
    return `/api${icon}`
  }
  if (icon.startsWith('/')) {
    return icon.startsWith('/api') ? icon : `/api${icon}`
  }
  return `/api/${icon}`
}

function resolveLangEmoji(langCode: string) {
  const code = String(langCode || '').toUpperCase()
  if (code.startsWith('ZH-CN')) return '🇨🇳'
  if (code.startsWith('ZH-TW')) return '🇹🇼'
  if (code.startsWith('EN-US')) return '🇺🇸'
  if (code.startsWith('JA-JP')) return '🇯🇵'
  if (code.startsWith('KO-KR')) return '🇰🇷'
  return '🌐'
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
    chosenTenant.value = null
    let pwdToSend = password.value
    try {
      if (!publicKeyCache.value) {
        publicKeyCache.value = await getPublicKey()
      }
      if (publicKeyCache.value) {
        const cipherHex = sm2.doEncrypt(password.value, publicKeyCache.value, 1)
        pwdToSend = cipherHex
      }
    } catch (e) {}
    const res = await login({
      account: account.value,
      password: pwdToSend,
      captcha: captcha.value,
      captchaId: mode.value === 'image' ? captchaId.value : undefined
    })
    tenants.value = Array.isArray(res) ? res : []
    if (tenants.value.length > 0) {
      tenantOpen.value = true
    } else {
      message.error(i18nT('common.login.msg.noTenantBound'))
    }
  } catch (e) {
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
  console.log('[Login] confirmTenant called, chosenTenant:', chosenTenant.value)
  
  if (!chosenTenant.value) {
    message.warning(i18nT('common.login.msg.selectTenantFirst'))
    return
  }
  const current = tenants.value.find(t => t.id === chosenTenant.value)
  if (!current) {
    message.error(i18nT('common.login.msg.invalidTenant'))
    tenantOpen.value = false
    return
  }
  try {
    const result = await chooseTenant({ 
      tenantId: chosenTenant.value,
      account: account.value
    })
    // 后端返回当前用户完整信息，token 通过 cookie 传递
    if (result && result.account) {
      // 存储用户信息到 Pinia Store（包含头像等）
      userStore.setUserInfo({
        account: result.account,
        username: result.username || result.account || account.value,
        email: result.email,
        phone: result.phone,
        avatar: result.avatar,
        tenantId: String(result.tenantId || chosenTenant.value),
        tenantName: current.name
      })
      
      // 将 account 和 tenantId 存储到 sessionStorage，用于路由守卫检查
      sessionStorage.setItem('account', result.account)
      sessionStorage.setItem('tenantId', String(result.tenantId || chosenTenant.value))
      
      // 获取路由时需要传递 account 和 tenantId
      const routesRes = await getRoutes({
        account: account.value,
        tenantId: chosenTenant.value
      })
      
      console.log('[Login] Routes response:', routesRes)
      
      // 存储按钮权限到 Pinia Store
      if (routesRes && routesRes.buttons) {
        permissionStore.setPermissions(routesRes.buttons)
        console.log('[Login] Permissions stored to Pinia:', routesRes.buttons)
      } else {
        // 如果没有权限数据，存储空数组
        permissionStore.setPermissions([])
        console.log('[Login] No permissions found, stored empty array')
      }
      
      // 存储路由和模块信息
      if (routesRes && routesRes.routes) {
        permissionStore.setRoutes(routesRes.routes)
      }
      if (routesRes && routesRes.modules) {
        permissionStore.setModules(routesRes.modules)
      }
      
      await injectDynamicRoutes(routesRes)
      
      console.log('[Login] Dynamic routes injected successfully')
      
      tenantOpen.value = false
      console.log('[Login] Tenant dialog closed')
      
      if (remember.value) {
        localStorage.setItem('fx-remember-account', account.value)
      } else {
        localStorage.removeItem('fx-remember-account')
      }
      
      // 等待 DOM 更新和路由准备完成
      await nextTick()
      await router.isReady()
      console.log('[Login] Router is ready')
      
      // 跳转到系统管理主页
      const targetPath = '/workspace/sys/dashboard'
      
      console.log('[Login] Navigating to:', targetPath)
      
      // 直接导航，不需要 setTimeout
      router.push(targetPath).then(() => {
        console.log('[Login] Navigation completed')
      }).catch((err) => {
        console.error('[Login] Navigation error:', err)
      })
    } else {
      message.error(i18nT('common.login.msg.chooseTenantFailed'))
    }
  } catch (e: any) {
    // http拦截器已经显示了错误，这里不再重复显示
    console.error('选择租户失败:', e)
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
      backgroundImage: formatMediaUrl(String(challenge?.backgroundImage || '')),
      templateImage: formatMediaUrl(String(challenge?.templateImage || '')),
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
    message.error(i18nT('common.operationFailed'))
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

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Orbitron:wght@500;700&display=swap');
.login-wrap {
  position: relative;
  height: 100%;
}
.bg-video {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.mask {
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, rgba(13, 2, 33, 0.7), rgba(13, 2, 33, 0.9));
}
.grid {
  position: absolute;
  inset: 0;
  opacity: 0.28;
  background-image: url("data:image/svg+xml,%3Csvg width='20' height='20' viewBox='0 0 20 20' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='%239C92AC' fill-opacity='0.2' fill-rule='evenodd'%3E%3Ccircle cx='3' cy='3' r='3'/%3E%3Ccircle cx='13' cy='13' r='3'/%3E%3C/g%3E%3C/svg%3E");
}
.content {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 100%;
  padding: 24px;
}

.content.layout-split {
  align-items: flex-start;
  justify-content: center;
  padding-left: clamp(24px, 9vw, 140px);
}

.content.layout-compact .glass-card {
  width: 320px;
  padding: 18px;
}
.brand {
  font-family: 'Orbitron', sans-serif;
  font-weight: 700;
  color: #fff;
  margin-bottom: 24px;
  position: relative;
  animation: glitch 2s infinite;
  text-align: center;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
}
.brand-logo {
  width: 60px;
  height: 60px;
  object-fit: cover;
}
.brand-blue {
  color: #05d9e8;
  text-shadow: 0 0 6px #05d9e8, 0 0 12px #05d9e8;
  font-size: clamp(24px, 5vw, 42px);
}
.brand-red {
  color: #ff2a6d;
  text-shadow: 0 0 6px #ff2a6d, 0 0 12px #ff2a6d;
  font-size: clamp(24px, 5vw, 42px);
  margin-left: 4px;
}
.brand-line {
  position: absolute;
  left: 0;
  right: 0;
  bottom: -6px;
  height: 2px;
  background: linear-gradient(90deg, #d300c5, #05d9e8);
  opacity: 0.7;
}
.brand-sub {
  color: #9ca3af;
  margin-top: 6px;
  margin-bottom: 8px;
  font-size: 14px;
}
.brand-sub-desc {
  color: #9ca3af;
  margin-top: 0;
  margin-bottom: 18px;
  font-size: 13px;
}
.glass-card {
  width: 380px;
  padding: 24px;
  background: transparent;
  border-radius: 10px;
  border: 1px solid rgba(5, 217, 232, 0.45);
  box-shadow:
    0 0 16px rgba(5, 217, 232, 0.25),
    0 0 24px rgba(211, 0, 197, 0.12);
}

.lang-option {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.lang-emoji {
  width: 18px;
  height: 18px;
  line-height: 18px;
  text-align: center;
  font-size: 14px;
  flex-shrink: 0;
}

.lang-label {
  line-height: 18px;
  flex: 1;
}
.cyber-form {
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.field {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.cyber-label {
  color: #cfd8dc;
  font-size: 14px;
}
.cyber-input {
  width: 100%;
  padding: 10px 12px;
  border-radius: 8px;
  border: 1px solid #4b5563;
  background: transparent;
  color: #e5e7eb;
  outline: none;
}
.cyber-input::placeholder {
  color: #94a3b8;
}
.cyber-input:hover {
  border-color: #05d9e8;
}
.cyber-input:focus {
  border-color: #05d9e8;
  box-shadow: 0 0 8px rgba(5, 217, 232, 0.45);
}
.captcha-row {
  display: flex;
  gap: 12px;
  align-items: center;
}
.captcha-input {
  flex: 1;
}
.captcha-img {
  width: 120px;
  height: 40px;
  border-radius: 8px;
  border: 1px solid #4b5563;
  cursor: pointer;
  object-fit: cover;
  transition: border-color 0.2s ease;
}
.captcha-img:hover {
  border-color: #05d9e8;
}
.form-tools {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 13px;
  color: #cfd8dc;
  gap: 12px;
}
.remember {
  display: flex;
  align-items: center;
  gap: 8px;
}
.remember input {
  width: 16px;
  height: 16px;
  accent-color: #05d9e8;
}

.lang-select-compact {
  width: 120px !important;
  flex-shrink: 0;
}

.lang-select-compact :deep(.ant-select-selector) {
  background: rgba(15, 23, 42, 0.3) !important;
  border-color: rgba(75, 85, 99, 0.5) !important;
  color: #e5e7eb !important;
  backdrop-filter: blur(8px);
  height: 28px !important;
  padding: 0 8px !important;
}

.lang-select-compact :deep(.ant-select-selector:hover) {
  border-color: rgba(5, 217, 232, 0.6) !important;
  background: rgba(15, 23, 42, 0.4) !important;
}

.lang-select-compact :deep(.ant-select-focused .ant-select-selector) {
  border-color: #05d9e8 !important;
  box-shadow: 0 0 8px rgba(5, 217, 232, 0.45) !important;
  background: rgba(15, 23, 42, 0.5) !important;
}

.lang-select-compact :deep(.ant-select-selection-item) {
  color: #e5e7eb !important;
  display: flex;
  align-items: center;
  gap: 6px;
  line-height: 26px !important;
  font-size: 13px;
}

.lang-select-compact :deep(.ant-select-arrow) {
  color: #9ca3af !important;
}

.lang-select-compact :deep(.ant-select-dropdown) {
  background: rgba(15, 23, 42, 0.95) !important;
  border: 1px solid rgba(5, 217, 232, 0.45) !important;
  box-shadow: 0 0 16px rgba(5, 217, 232, 0.25) !important;
  backdrop-filter: blur(12px);
}

.lang-select-compact :deep(.ant-select-item) {
  color: #e5e7eb !important;
  background: transparent !important;
  font-size: 13px;
}

.lang-select-compact :deep(.ant-select-item:hover) {
  background: rgba(5, 217, 232, 0.15) !important;
}

.lang-select-compact :deep(.ant-select-item-option-selected) {
  background: rgba(5, 217, 232, 0.25) !important;
  color: #05d9e8 !important;
}

.forgot {
  color: #05d9e8;
  text-decoration: none;
  white-space: nowrap;
}
.forgot:hover {
  color: #d300c5;
}
.block-btn {
  width: 100%;
  margin-top: 12px;
  padding: 10px 0;
  border: none;
  border-radius: 999px;
  background: linear-gradient(90deg, var(--primary-color, #05d9e8), var(--secondary-color, #ff2a6d));
  color: #fff;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}
.btn-disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
.divider {
  margin: 16px 0 8px;
  text-align: center;
  color: #9ca3af;
  font-size: 12px;
}
.oauth-row {
  display: flex;
  justify-content: center;
  gap: 16px;
}
.oauth-btn {
  width: 40px;
  height: 40px;
  border-radius: 999px;
  border: 1px solid rgba(148, 163, 184, 0.6);
  background: rgba(15, 23, 42, 0.7);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}
.oauth-btn img {
  width: 22px;
  height: 22px;
}
.copyright {
  position: absolute;
  bottom: 16px;
  left: 0;
  right: 0;
  text-align: center;
  color: #9ca3af;
  font-size: 12px;
}
.identity-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, rgba(13, 2, 33, 0.7), rgba(13, 2, 33, 0.9));
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 24px;
  z-index: 10;
}
.identity-container {
  max-width: 960px;
  width: 100%;
  position: relative;
}
.tenant-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 200px));
  gap: 16px;
  justify-content: center;
}
.tenant-grid.single {
  max-width: 200px;
  margin: 0 auto;
}
.tenant-card {
  position: relative;
  padding: 24px 18px;
  border-radius: 16px;
  background: transparent;
  border: 1px solid rgba(5, 217, 232, 0.45);
  box-shadow:
    0 0 16px rgba(5, 217, 232, 0.25),
    0 0 24px rgba(211, 0, 197, 0.12);
  cursor: pointer;
  display: flex;
  flex-direction: column;
  gap: 12px;
  align-items: center;
  transition: transform 0.18s ease, box-shadow 0.18s ease, border-color 0.18s ease;
}
.tenant-card.big {
  transform: scale(1.06);
}
.tenant-card:hover {
  transform: scale(1.02);
  border-color: #05d9e8;
}
.tenant-card.selected {
  border-color: #05d9e8;
  box-shadow:
    0 0 16px rgba(5, 217, 232, 0.45),
    0 0 24px rgba(211, 0, 197, 0.25);
  transform: scale(1.02);
}
.tenant-logo {
  flex-shrink: 0;
  width: 64px;
  height: 64px;
  border-radius: 999px;
  background: rgba(5, 217, 232, 0.1);
  border: 2px solid rgba(5, 217, 232, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}
.tenant-logo img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.logo-fallback {
  color: #e5e7eb;
  font-size: 24px;
  font-weight: 700;
}
.tenant-info {
  flex: 1;
  text-align: center;
  width: 100%;
}
.tenant-name {
  color: #e5e7eb;
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 6px;
}
.tenant-intro {
  color: #9ca3af;
  font-size: 13px;
  line-height: 1.4;
}
.tenant-tools {
  display: flex;
  gap: 6px;
  margin-top: 8px;
  z-index: 10;
  position: relative;
}
.identity-actions {
  margin-top: 32px;
  text-align: center;
  z-index: 10;
  position: relative;
}
.action-btn {
  min-width: 140px;
  height: 40px;
  border-radius: 999px;
  font-size: 15px;
  cursor: pointer;
  transition: all 0.2s ease;
}
.action-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(5, 217, 232, 0.3);
}
.action-btn.primary {
  margin-left: 12px;
  background: linear-gradient(90deg, #05d9e8, #d300c5);
  border: none;
  color: #fff;
}
.action-btn.primary:hover {
  box-shadow: 0 4px 16px rgba(5, 217, 232, 0.5);
}
.identity-global-tools {
  position: absolute;
  top: 24px;
  right: 24px;
  z-index: 20;
}

.slider-panel {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.slider-canvas {
  position: relative;
  border-radius: 10px;
  overflow: hidden;
  border: 1px solid #334155;
  background: rgba(15, 23, 42, 0.5);
  margin: 0 auto;
}

.slider-bg {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.slider-piece {
  position: absolute;
  pointer-events: none;
  user-select: none;
  filter: drop-shadow(0 2px 6px rgba(0, 0, 0, 0.35));
}

.slider-actions {
  display: flex;
  justify-content: flex-end;
}

.spinner {
  width: 14px;
  height: 14px;
  border-radius: 999px;
  border: 2px solid rgba(15, 23, 42, 0.2);
  border-top-color: #05d9e8;
  animation: spin 0.8s linear infinite;
}
.star {
  color: #fbbf24;
}

@media (max-width: 992px) {
  .content.layout-split {
    align-items: center;
    padding-left: 24px;
  }
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
</style>
