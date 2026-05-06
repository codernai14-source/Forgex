<!--
  - Copyright 2026 coder_nai@163.com
  -
  - Licensed under the Apache License, Version 2.0 (the "License");
  - you may not use this file except in compliance with the License.
  - You may obtain a copy of the License at
  -
  - http://www.apache.org/licenses/LICENSE-2.0
  -
  - Unless required by applicable law or agreed to in writing, software
  - distributed under the License is distributed on an "AS IS" BASIS,
  - WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  - See the License for the specific language governing permissions and
  - limitations under the License.
  -->

<template>
  <div class="register-wrap" :style="themeVars">
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
      alt="register-background"
    />
    <div
      class="mask"
      :style="{ backgroundColor: systemConfig.loginBackgroundType === 'color' ? systemConfig.loginBackgroundColor : '' }"
    ></div>
    <div class="grid"></div>

    <main class="register-shell">
      <section class="register-copy">
        <div class="brand">
          <img
            v-if="resolveMediaUrl(systemConfig.systemLogo)"
            :src="resolveMediaUrl(systemConfig.systemLogo)"
            class="brand-logo"
            alt="system-logo"
          />
          <template v-else>
            <span class="brand-blue">{{ brandNamePrefix }}</span>
            <span class="brand-red">{{ brandNameSuffix }}</span>
          </template>
          <div class="brand-line"></div>
        </div>
        <div class="brand-sub">{{ systemConfig.loginPageTitle }}</div>
        <div v-if="systemConfig.loginPageSubtitle" class="brand-sub-desc">
          {{ systemConfig.loginPageSubtitle }}
        </div>
      </section>

      <section class="register-panel">
        <div class="panel-tools">
          <span class="panel-system">{{ systemConfig.systemName }}</span>
          <a-dropdown
            v-if="languages.length > 0"
            placement="bottomRight"
            trigger="click"
            overlay-class-name="register-lang-dropdown"
          >
            <button
              type="button"
              class="lang-switch-compact__trigger"
              :aria-label="i18nT('common.login.languageLabel')"
              :title="currentLanguageLabel"
            >
              <img :src="LANG_SWITCH_ICON_SRC" alt="language" class="lang-switch-compact__icon" />
            </button>
            <template #overlay>
              <a-menu :selected-keys="[selectedLang]" @click="onLanguageMenuClick">
                <a-menu-item v-for="language in languages" :key="language.langCode">
                  <span class="lang-menu-item" :class="{ active: selectedLang === language.langCode }">
                    {{ getLanguageLabel(language) }}
                  </span>
                </a-menu-item>
              </a-menu>
            </template>
          </a-dropdown>
        </div>

        <template v-if="registerSuccess">
          <div class="success-mark"></div>
          <h1 class="register-title">{{ i18nT('common.register.successTitle') }}</h1>
          <p class="register-desc">{{ i18nT('common.register.successDesc') }}</p>
          <a-button type="primary" size="large" block class="primary-submit" @click="goLogin">
            {{ i18nT('common.register.goLogin') }}
          </a-button>
        </template>

        <template v-else>
          <h1 class="register-title">{{ i18nT('common.register.title') }}</h1>
          <p class="register-desc">{{ i18nT('common.register.subtitle') }}</p>

          <a-form
            ref="formRef"
            :model="formData"
            :rules="rules"
            layout="vertical"
            class="register-form"
          >
            <a-form-item :label="i18nT('common.register.inviteCode')" name="inviteCode">
              <div class="inline-row">
                <a-input
                  v-model:value="formData.inviteCode"
                  autocomplete="off"
                  :placeholder="i18nT('common.register.inviteCodePlaceholder')"
                  @change="resetInviteState"
                />
                <a-button class="secondary-action" :loading="checkingInvite" @click="handleCheckInvite">
                  {{ i18nT('common.register.checkInvite') }}
                </a-button>
              </div>
              <div v-if="inviteValid === true" class="invite-check-ok">
                <span class="state-dot"></span>
                {{ i18nT('common.register.inviteValid') }}
              </div>
              <div v-if="inviteValid === false" class="invite-check-fail">
                <span class="state-dot"></span>
                {{ inviteError }}
              </div>
            </a-form-item>

            <div class="form-grid">
              <a-form-item :label="i18nT('common.register.account')" name="account">
                <a-input
                  v-model:value="formData.account"
                  autocomplete="username"
                  :placeholder="i18nT('common.register.accountPlaceholder')"
                />
              </a-form-item>

              <a-form-item :label="i18nT('common.register.username')" name="username">
                <a-input
                  v-model:value="formData.username"
                  autocomplete="name"
                  :placeholder="i18nT('common.register.usernamePlaceholder')"
                />
              </a-form-item>
            </div>

            <div class="form-grid">
              <a-form-item :label="i18nT('common.register.password')" name="password">
                <a-input-password
                  v-model:value="formData.password"
                  autocomplete="new-password"
                  :placeholder="i18nT('common.register.passwordPlaceholder')"
                />
              </a-form-item>

              <a-form-item :label="i18nT('common.register.confirmPassword')" name="confirmPassword">
                <a-input-password
                  v-model:value="formData.confirmPassword"
                  autocomplete="new-password"
                  :placeholder="i18nT('common.register.confirmPasswordPlaceholder')"
                />
              </a-form-item>
            </div>

            <div class="form-grid">
              <a-form-item :label="i18nT('common.register.phone')" name="phone">
                <a-input
                  v-model:value="formData.phone"
                  autocomplete="tel"
                  :placeholder="i18nT('common.register.phonePlaceholder')"
                />
              </a-form-item>

              <a-form-item :label="i18nT('common.register.email')" name="email">
                <a-input
                  v-model:value="formData.email"
                  autocomplete="email"
                  :placeholder="i18nT('common.register.emailPlaceholder')"
                />
              </a-form-item>
            </div>

            <a-form-item
              v-if="captchaMode === 'image'"
              :label="i18nT('common.register.captcha')"
              name="captcha"
            >
              <div class="inline-row">
                <a-input
                  v-model:value="formData.captcha"
                  autocomplete="off"
                  :placeholder="i18nT('common.register.captchaPlaceholder')"
                />
                <img
                  v-if="captchaImage"
                  :src="captchaImage"
                  class="captcha-img"
                  :title="i18nT('common.register.refreshCaptcha')"
                  alt="captcha"
                  @click="loadCaptcha"
                />
              </div>
            </a-form-item>

            <a-form-item v-if="captchaMode === 'slider'" :label="i18nT('common.register.behaviorCaptcha')">
              <div class="slider-trigger">
                <a-button class="secondary-action" @click="openSlider">
                  {{ sliderVerified ? i18nT('common.register.sliderVerified') : i18nT('common.register.startSlider') }}
                </a-button>
                <span :class="['slider-state', { verified: sliderVerified }]">
                  {{ sliderVerified ? i18nT('common.success') : i18nT('common.register.sliderPending') }}
                </span>
              </div>
            </a-form-item>

            <a-form-item class="submit-item">
              <a-button
                type="primary"
                block
                size="large"
                class="primary-submit"
                :loading="submitting"
                @click="handleRegister"
              >
                {{ i18nT('common.register.submit') }}
              </a-button>
            </a-form-item>

            <div class="register-footer">
              <span>{{ i18nT('common.register.haveAccount') }}</span>
              <a @click="goLogin">{{ i18nT('common.register.backToLogin') }}</a>
            </div>
          </a-form>
        </template>
      </section>
    </main>

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
import { computed, onMounted, reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { message } from 'ant-design-vue'
import router from '@/router'
import {
  createDefaultSystemBasicConfig,
  getLoginCaptcha,
  getSystemBasicConfig,
  type SystemBasicConfig,
} from '@/api/system/config'
import { register, checkInviteCode, getPublicKey } from '@/api/auth/login'
import {
  captchaImage as getImageCaptcha,
  captchaSlider,
  captchaSliderValidate,
} from '@/api/auth/captcha'
import { listEnabledLanguages, type LanguageType } from '@/api/system/i18n'
import { getAvailableLocales, getLocale, setLocale, type LocaleCode } from '@/locales'
import { getLanguageDisplayName, LANG_SWITCH_ICON_SRC } from '@/utils/language'
import { normalizeMediaUrl } from '@/utils/media'
import { sm2 } from 'sm-crypto'

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

type CaptchaMode = 'none' | 'image' | 'slider'

const { t: i18nT } = useI18n({ useScope: 'global' })
const systemConfig = ref<SystemBasicConfig>(createDefaultSystemBasicConfig())

const formRef = ref()
const submitting = ref(false)
const registerSuccess = ref(false)
const languages = ref<LanguageType[]>([])
const selectedLang = ref<LocaleCode>(getLocale())

const captchaMode = ref<CaptchaMode>('none')
const captchaImage = ref('')
const captchaId = ref('')
const publicKeyCache = ref('')

const checkingInvite = ref(false)
const inviteValid = ref<boolean | null>(null)
const inviteError = ref('')

const sliderOpen = ref(false)
const sliderVerifying = ref(false)
const sliderVerified = ref(false)
const sliderChallenge = ref<SliderCaptchaChallenge | null>(null)
const sliderValue = ref(0)
const sliderTrackStartAt = ref(0)

const formData = reactive({
  inviteCode: '',
  account: '',
  username: '',
  password: '',
  confirmPassword: '',
  phone: '',
  email: '',
  captcha: '',
})

const themeVars = computed(() => ({
  '--primary-color': systemConfig.value.primaryColor || '#05d9e8',
  '--secondary-color': systemConfig.value.secondaryColor || '#ff2a6d',
}))

const brandNamePrefix = computed(() => {
  return String(systemConfig.value.systemName || 'FORGEX_MOM').split('_')[0] || 'FORGEX'
})

const brandNameSuffix = computed(() => {
  const parts = String(systemConfig.value.systemName || 'FORGEX_MOM').split('_')
  return `_${parts[1] || 'MOM'}`
})

const currentLanguageLabel = computed(() => {
  return getLanguageDisplayName(languages.value.find(language => language.langCode === selectedLang.value))
})

const rules = computed(() => ({
  inviteCode: [{ required: true, message: i18nT('common.register.validation.inviteRequired'), trigger: 'blur' }],
  account: [
    { required: true, message: i18nT('common.register.validation.accountRequired'), trigger: 'blur' },
    { min: 3, max: 32, message: i18nT('common.register.validation.accountLength'), trigger: 'blur' },
  ],
  username: [{ required: true, message: i18nT('common.register.validation.usernameRequired'), trigger: 'blur' }],
  password: [
    { required: true, message: i18nT('common.register.validation.passwordRequired'), trigger: 'blur' },
    { min: 6, message: i18nT('common.register.validation.passwordLength'), trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: i18nT('common.register.validation.confirmPasswordRequired'), trigger: 'blur' },
    {
      validator: (_rule: unknown, value: string) => {
        if (value !== formData.password) {
          return Promise.reject(i18nT('common.register.validation.passwordMismatch'))
        }
        return Promise.resolve()
      },
      trigger: 'blur',
    },
  ],
  captcha: captchaMode.value === 'image'
    ? [{ required: true, message: i18nT('common.register.validation.captchaRequired'), trigger: 'blur' }]
    : [],
}))

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
  const width = sliderChallenge.value.backgroundImageWidth
  return width > maxWidth ? maxWidth / width : 1
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

function getLanguageLabel(language: LanguageType) {
  return getLanguageDisplayName(language)
}

function buildFallbackLanguages(): LanguageType[] {
  return getAvailableLocales().map((item, index) => ({
    id: index + 1,
    langCode: item.value,
    langName: item.label,
    langNameEn: item.label,
    icon: '',
    orderNum: index + 1,
    enabled: true,
    isDefault: item.value === 'zh-CN',
    createBy: '',
    createTime: '',
    updateBy: null,
    updateTime: '',
    deleted: 0,
  }))
}

function onLanguageMenuClick(info: { key?: string }) {
  const nextLang = String(info?.key || '')
  if (!nextLang || nextLang === selectedLang.value) {
    return
  }
  selectedLang.value = nextLang as LocaleCode
  setLocale(nextLang as LocaleCode)
}

function resetInviteState() {
  inviteValid.value = null
  inviteError.value = ''
}

async function goLogin() {
  await router.push('/login')
}

async function loadLanguages() {
  try {
    const list = await listEnabledLanguages()
    languages.value = Array.isArray(list) && list.length > 0 ? list : buildFallbackLanguages()
  } catch (_) {
    languages.value = buildFallbackLanguages()
  }

  const matchCurrent = languages.value.some(language => language.langCode === selectedLang.value)
  if (matchCurrent || languages.value.length === 0) {
    return
  }

  const defaultLanguage = languages.value.find(language => language.isDefault === true)
  const nextLang = defaultLanguage?.langCode || languages.value[0].langCode
  selectedLang.value = nextLang as LocaleCode
  setLocale(nextLang as LocaleCode)
}

async function loadCaptchaMode() {
  try {
    const config = await getLoginCaptcha()
    const nextMode = config?.mode || 'none'
    captchaMode.value = ['none', 'image', 'slider'].includes(nextMode) ? nextMode as CaptchaMode : 'none'
    formData.captcha = ''
    captchaId.value = ''
    sliderVerified.value = false
    sliderChallenge.value = null
    sliderValue.value = 0
    if (captchaMode.value === 'image') {
      await loadCaptcha()
    }
  } catch (_) {
    captchaMode.value = 'none'
  }
}

async function loadCaptcha() {
  if (captchaMode.value !== 'image') {
    return
  }
  try {
    const res = await getImageCaptcha() as any
    captchaId.value = res?.captchaId || ''
    captchaImage.value = res?.imageBase64 ? `data:image/png;base64,${res.imageBase64}` : ''
  } catch (_) {}
}

async function refreshCaptchaAfterFailure() {
  formData.captcha = ''
  if (captchaMode.value === 'image') {
    await loadCaptcha()
    return
  }
  if (captchaMode.value === 'slider') {
    sliderVerified.value = false
    sliderValue.value = 0
    sliderChallenge.value = null
    if (sliderOpen.value) {
      await initSlider()
    }
  }
}

async function handleCheckInvite() {
  const inviteCode = formData.inviteCode.trim()
  if (!inviteCode) {
    message.warning(i18nT('common.register.validation.inviteRequired'))
    return
  }
  checkingInvite.value = true
  inviteValid.value = null
  inviteError.value = ''
  try {
    await checkInviteCode(inviteCode)
    inviteValid.value = true
  } catch (e: any) {
    inviteValid.value = false
    inviteError.value = e?.message || i18nT('common.register.inviteInvalid')
  } finally {
    checkingInvite.value = false
  }
}

async function handleRegister() {
  try {
    if (captchaMode.value === 'slider' && !sliderVerified.value) {
      message.warning(i18nT('common.register.validation.sliderRequired'))
      return
    }

    await formRef.value?.validate()
    submitting.value = true

    let pwdToSend = formData.password
    try {
      if (!publicKeyCache.value) {
        publicKeyCache.value = await getPublicKey() as string
      }
      if (publicKeyCache.value) {
        pwdToSend = sm2.doEncrypt(formData.password, publicKeyCache.value, 1)
      }
    } catch (_) {}

    await register({
      account: formData.account.trim(),
      username: formData.username.trim(),
      password: pwdToSend,
      phone: formData.phone.trim() || undefined,
      email: formData.email.trim() || undefined,
      inviteCode: formData.inviteCode.trim(),
      captcha: formData.captcha || undefined,
      captchaId: captchaMode.value === 'image' ? captchaId.value || undefined : undefined,
    })

    registerSuccess.value = true
  } catch (e: any) {
    if (e?.errorFields) return
    await refreshCaptchaAfterFailure()
  } finally {
    submitting.value = false
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
      templateImageHeight: tplHeight > 0 ? tplHeight : 52,
    }
    sliderValue.value = 0
    sliderTrackStartAt.value = Date.now()
    sliderVerified.value = false
    formData.captcha = ''
  } catch (_) {
    sliderChallenge.value = null
    sliderVerified.value = false
    formData.captcha = ''
    message.error(i18nT('common.loadFailed'))
  }
}

function buildSliderTrackList(targetX: number, duration: number): SliderTrackPoint[] {
  const pointCount = 18
  const list: SliderTrackPoint[] = []
  for (let i = 0; i < pointCount; i++) {
    const progress = i / (pointCount - 1)
    const eased = 1 - Math.pow(1 - progress, 2)
    list.push({
      x: Number((targetX * eased).toFixed(2)),
      y: Number((2 + Math.sin(progress * Math.PI * 1.8) * 2 + (Math.random() - 0.5) * 0.8).toFixed(2)),
      t: Number((duration * progress).toFixed(2)),
      type: 'move',
    })
  }
  return list
}

function onSliderDrag(value: number | [number, number]) {
  const nextValue = Array.isArray(value) ? value[0] : value
  sliderValue.value = Number.isFinite(nextValue) ? nextValue : 0
}

async function verifySliderCaptcha() {
  if (!sliderChallenge.value?.id) {
    message.warning(i18nT('common.loadFailed'))
    return
  }
  if (sliderValue.value <= 0) {
    message.warning(i18nT('common.register.startSlider'))
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
      trackList: buildSliderTrackList(sliderValue.value, duration),
    }
    const token = await captchaSliderValidate({
      id: sliderChallenge.value.id,
      track: payload,
    })
    formData.captcha = String(token || '')
    sliderVerified.value = !!formData.captcha
    if (sliderVerified.value) {
      sliderOpen.value = false
      message.success(i18nT('common.success'))
    } else {
      message.error(i18nT('common.operationFailed'))
      await initSlider()
    }
  } catch (_) {
    sliderVerified.value = false
    formData.captcha = ''
    await initSlider()
  } finally {
    sliderVerifying.value = false
  }
}

onMounted(async () => {
  try {
    const config = await getSystemBasicConfig()
    if (config) {
      systemConfig.value = {
        ...createDefaultSystemBasicConfig(),
        ...config,
      }
    }
  } catch (_) {}

  await loadLanguages()
  await loadCaptchaMode()

  try {
    publicKeyCache.value = await getPublicKey() as string
  } catch (_) {}
})
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Orbitron:wght@500;700&display=swap');

.register-wrap {
  position: relative;
  min-height: 100vh;
  height: 100%;
  overflow: hidden;
  background: #0d0221;
  --auth-field-bg: rgba(15, 23, 42, 0.46);
}

.bg-video {
  position: fixed;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.mask {
  position: fixed;
  inset: 0;
  background: linear-gradient(180deg, rgba(13, 2, 33, 0.68), rgba(13, 2, 33, 0.9));
}

.grid {
  position: fixed;
  inset: 0;
  opacity: 0.28;
  background-image: url("data:image/svg+xml,%3Csvg width='20' height='20' viewBox='0 0 20 20' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='%239C92AC' fill-opacity='0.2' fill-rule='evenodd'%3E%3Ccircle cx='3' cy='3' r='3'/%3E%3Ccircle cx='13' cy='13' r='3'/%3E%3C/g%3E%3C/svg%3E");
}

.register-shell {
  position: relative;
  z-index: 1;
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(520px, 560px);
  align-items: center;
  gap: clamp(28px, 6vw, 84px);
  box-sizing: border-box;
  width: 100%;
  min-height: 100vh;
  padding: clamp(18px, 3vh, 36px) clamp(24px, 7vw, 112px);
}

.register-copy {
  min-width: 0;
  color: #e5e7eb;
}

.brand {
  position: relative;
  display: inline-flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 26px;
  font-family: 'Orbitron', sans-serif;
  font-weight: 700;
}

.brand-logo {
  width: 64px;
  height: 64px;
  object-fit: cover;
}

.brand-blue {
  color: #05d9e8;
  text-shadow: 0 0 6px #05d9e8, 0 0 12px #05d9e8;
  font-size: clamp(28px, 5vw, 48px);
}

.brand-red {
  color: #ff2a6d;
  text-shadow: 0 0 6px #ff2a6d, 0 0 12px #ff2a6d;
  font-size: clamp(28px, 5vw, 48px);
}

.brand-line {
  position: absolute;
  left: 0;
  right: 0;
  bottom: -8px;
  height: 2px;
  background: linear-gradient(90deg, #d300c5, #05d9e8);
  opacity: 0.75;
}

.brand-sub {
  max-width: 560px;
  color: #e5e7eb;
  font-size: clamp(20px, 3vw, 30px);
  line-height: 1.35;
  font-weight: 600;
}

.brand-sub-desc {
  max-width: 560px;
  margin-top: 12px;
  color: #9ca3af;
  font-size: 14px;
  line-height: 1.8;
}

.register-panel {
  justify-self: end;
  box-sizing: border-box;
  width: min(560px, 100%);
  min-width: 0;
  max-height: calc(100vh - 36px);
  overflow-y: auto;
  padding: 24px 28px 18px;
  border: 1px solid rgba(5, 217, 232, 0.4);
  border-radius: 8px;
  background: rgba(7, 13, 32, 0.54);
  box-shadow:
    0 0 18px rgba(5, 217, 232, 0.24),
    0 0 32px rgba(211, 0, 197, 0.12);
  color: #e5e7eb;
  backdrop-filter: blur(8px);
}

.panel-tools {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 16px;
}

.panel-system {
  min-width: 0;
  overflow: hidden;
  color: #67e8f9;
  font-size: 13px;
  line-height: 1.4;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.lang-switch-compact__trigger {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  width: 30px;
  height: 30px;
  padding: 0;
  border: 1px solid rgba(75, 85, 99, 0.5);
  border-radius: 50%;
  background: rgba(15, 23, 42, 0.3);
  cursor: pointer;
  transition: border-color 0.2s ease, box-shadow 0.2s ease, background 0.2s ease;
}

.lang-switch-compact__trigger:hover,
.lang-switch-compact__trigger:focus-visible {
  outline: none;
  border-color: rgba(5, 217, 232, 0.7);
  background: rgba(15, 23, 42, 0.45);
  box-shadow: 0 0 8px rgba(5, 217, 232, 0.35);
}

.lang-switch-compact__icon {
  width: 21px;
  height: 21px;
  border-radius: 50%;
  object-fit: contain;
}

.register-title {
  margin: 0;
  color: #ffffff;
  font-size: 24px;
  line-height: 1.25;
  font-weight: 700;
}

.register-desc {
  margin: 8px 0 16px;
  color: #cbd5e1;
  font-size: 14px;
  line-height: 1.7;
}

.register-form {
  text-align: left;
}

.register-form :deep(.ant-form-item) {
  margin-bottom: 11px;
}

.register-form :deep(.ant-form-item-label) {
  padding-bottom: 3px;
}

.register-form :deep(.ant-form-item-label > label) {
  height: auto;
  color: #cfd8dc !important;
  font-size: 13px;
}

.register-form :deep(.ant-form-item-required::before) {
  color: #ff4d4f !important;
}

.register-form :deep(.ant-btn > span) {
  letter-spacing: 0;
}

.register-form :deep(.ant-input),
.register-form :deep(.ant-input-affix-wrapper) {
  min-height: 38px;
  border-color: #4b5563;
  border-radius: 8px;
  background: var(--auth-field-bg) !important;
  color: #e5e7eb;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

.register-form :deep(.ant-input-affix-wrapper::before) {
  background: var(--auth-field-bg) !important;
}

.register-form :deep(.ant-input-affix-wrapper > input.ant-input) {
  background: transparent !important;
}

.register-form :deep(.ant-input:hover),
.register-form :deep(.ant-input-affix-wrapper:hover) {
  border-color: var(--primary-color);
}

.register-form :deep(.ant-input:focus),
.register-form :deep(.ant-input-focused),
.register-form :deep(.ant-input-affix-wrapper-focused) {
  border-color: var(--primary-color);
  box-shadow: 0 0 8px color-mix(in srgb, var(--primary-color) 45%, transparent);
}

.register-form :deep(.ant-input::placeholder),
.register-form :deep(.ant-input-password input::placeholder) {
  color: #94a3b8;
}

.register-form :deep(.ant-input-password input),
.register-form :deep(.ant-input-affix-wrapper .ant-input) {
  min-height: auto;
  border: 0;
  background: transparent !important;
  box-shadow: none;
  color: #e5e7eb;
}

.register-form :deep(.ant-input:-webkit-autofill),
.register-form :deep(.ant-input:-webkit-autofill:hover),
.register-form :deep(.ant-input:-webkit-autofill:focus),
.register-form :deep(.ant-input-affix-wrapper input:-webkit-autofill),
.register-form :deep(.ant-input-affix-wrapper input:-webkit-autofill:hover),
.register-form :deep(.ant-input-affix-wrapper input:-webkit-autofill:focus) {
  -webkit-text-fill-color: #e5e7eb !important;
  caret-color: var(--primary-color);
  box-shadow: 0 0 0 1000px var(--auth-field-bg) inset !important;
  -webkit-box-shadow: 0 0 0 1000px var(--auth-field-bg) inset !important;
  transition: background-color 9999s ease-out 0s, color 9999s ease-out 0s;
}

.register-form :deep(.ant-input-password-icon) {
  color: #9ca3af;
}

.register-form :deep(.ant-input-affix-wrapper) {
  background: transparent !important;
}

.inline-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.inline-row :deep(.ant-input) {
  flex: 1;
  min-width: 0;
}

.secondary-action {
  flex-shrink: 0;
  min-width: 82px;
  border-color: rgba(5, 217, 232, 0.42);
  border-radius: 8px;
  background: rgba(15, 23, 42, 0.28);
  color: #dff7ff;
}

.secondary-action:hover,
.secondary-action:focus {
  border-color: var(--primary-color);
  background: rgba(5, 217, 232, 0.12);
  color: #ffffff;
}

.field-help {
  margin-top: 6px;
  color: #94a3b8;
  font-size: 12px;
  line-height: 1.5;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.form-grid :deep(.ant-form-item) {
  min-width: 0;
}

.captcha-img {
  flex-shrink: 0;
  width: 120px;
  height: 40px;
  border: 1px solid #4b5563;
  border-radius: 8px;
  object-fit: cover;
  cursor: pointer;
  transition: border-color 0.2s ease;
}

.captcha-img:hover {
  border-color: var(--primary-color);
}

.invite-check-ok,
.invite-check-fail {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 6px;
  font-size: 12px;
  line-height: 1.5;
}

.invite-check-ok {
  color: #86efac;
}

.invite-check-fail {
  color: #fca5a5;
}

.state-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: currentColor;
  box-shadow: 0 0 8px currentColor;
}

.slider-trigger {
  display: flex;
  align-items: center;
  gap: 12px;
  min-height: 40px;
}

.slider-state {
  color: #94a3b8;
  font-size: 13px;
}

.slider-state.verified {
  color: #86efac;
}

.submit-item {
  margin-top: 4px;
}

.primary-submit {
  height: 40px;
  border: 0;
  border-radius: 999px;
  background: linear-gradient(90deg, var(--primary-color), var(--secondary-color));
  color: #ffffff;
  font-weight: 600;
  box-shadow: 0 8px 22px rgba(5, 217, 232, 0.22);
}

.primary-submit:hover,
.primary-submit:focus {
  color: #ffffff;
  filter: brightness(1.05);
  box-shadow: 0 10px 26px rgba(5, 217, 232, 0.3);
}

.register-footer {
  display: flex;
  justify-content: center;
  gap: 6px;
  margin-top: 6px;
  color: #cbd5e1;
  font-size: 13px;
}

.register-footer a {
  color: var(--primary-color);
  cursor: pointer;
  text-decoration: none;
}

.register-footer a:hover {
  color: #d300c5;
}

.success-mark {
  width: 42px;
  height: 42px;
  margin-bottom: 16px;
  border: 1px solid rgba(134, 239, 172, 0.6);
  border-radius: 50%;
  background:
    linear-gradient(135deg, transparent 47%, #86efac 48%, #86efac 56%, transparent 57%),
    linear-gradient(45deg, transparent 41%, #86efac 42%, #86efac 51%, transparent 52%);
  box-shadow: 0 0 16px rgba(134, 239, 172, 0.2);
}

.slider-panel {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.slider-canvas {
  position: relative;
  overflow: hidden;
  margin: 0 auto;
  border: 1px solid #334155;
  border-radius: 10px;
  background: rgba(15, 23, 42, 0.5);
}

.slider-bg {
  display: block;
  width: 100%;
  height: 100%;
  object-fit: cover;
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

@media (max-width: 1080px) {
  .register-shell {
    grid-template-columns: minmax(0, 1fr);
    align-content: center;
    justify-items: center;
  }

  .register-copy {
    width: min(560px, 100%);
  }

  .register-panel {
    justify-self: center;
  }
}

@media (max-width: 640px) {
  .register-wrap {
    height: auto;
    overflow-y: auto;
  }

  .register-shell {
    min-height: 100vh;
    padding: 20px;
  }

  .register-copy {
    display: none;
  }

  .register-panel {
    width: 100%;
    max-height: none;
    overflow: visible;
    padding: 22px;
  }

  .form-grid {
    grid-template-columns: 1fr;
    gap: 0;
  }

  .inline-row {
    flex-wrap: wrap;
    align-items: stretch;
  }

  .secondary-action {
    width: 100%;
    min-width: 74px;
    padding-inline: 12px;
  }

  .inline-row .secondary-action {
    flex-basis: 100%;
  }

  .captcha-img {
    width: 100%;
  }
}
</style>

<style lang="less">
.register-lang-dropdown {
  .ant-dropdown-menu {
    padding: 6px;
    background: rgba(15, 23, 42, 0.96) !important;
    border: 1px solid rgba(5, 217, 232, 0.28) !important;
    border-radius: 12px;
    box-shadow: 0 12px 32px rgba(2, 6, 23, 0.45) !important;
    backdrop-filter: blur(10px);
  }

  .ant-dropdown-menu-item {
    margin: 0;
    border-radius: 8px;
    color: #e5f7ff !important;
    font-size: 13px;
    line-height: 1.5;
  }

  .ant-dropdown-menu-item:hover,
  .ant-dropdown-menu-item-active {
    background: rgba(5, 217, 232, 0.14) !important;
  }

  .ant-dropdown-menu-item-selected {
    background: linear-gradient(90deg, rgba(5, 217, 232, 0.16), rgba(255, 42, 109, 0.16)) !important;
  }

  .ant-dropdown-menu-title-content,
  .lang-menu-item {
    color: #e5f7ff !important;
  }

  .lang-menu-item.active,
  .ant-dropdown-menu-item-selected .lang-menu-item {
    color: #22d3ee !important;
    text-shadow: 0 0 10px rgba(34, 211, 238, 0.18);
  }
}
</style>
