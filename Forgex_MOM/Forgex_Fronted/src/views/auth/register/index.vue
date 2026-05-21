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

<style scoped lang="less" src="@/styles/views/auth/register/index.less"></style>

<style lang="less" src="@/styles/views/auth/register/index-global.less"></style>
