<template>
  <div class="system-config">
    <a-card :bordered="false" :loading="loading">
      <a-tabs v-model:activeKey="activeTab">
        <a-tab-pane key="system" :tab="t('system.config.tabSystem')">
          <a-form
            :model="basicConfig"
            :label-col="{ span: 6 }"
            :wrapper-col="{ span: 16 }"
            layout="horizontal"
          >
            <a-form-item :label="t('system.config.systemName')" name="systemName">
              <a-input
                v-model:value="basicConfig.systemName"
                :placeholder="t('system.config.systemNamePlaceholder')"
              />
            </a-form-item>

            <a-form-item :label="t('system.config.systemLogo')" name="systemLogo">
              <div class="system-logo-upload">
                <AvatarUpload v-model="basicConfig.systemLogo" @success="handleLogoUploadSuccess" />
              </div>
            </a-form-item>

            <a-form-item :label="t('system.config.systemVersion')" name="systemVersion">
              <a-input
                v-model:value="basicConfig.systemVersion"
                :placeholder="t('system.config.systemVersionPlaceholder')"
              />
            </a-form-item>

            <a-form-item :label="t('system.config.copyright')" name="copyright">
              <a-input
                v-model:value="basicConfig.copyright"
                :placeholder="t('system.config.copyrightPlaceholder')"
              />
            </a-form-item>

            <a-form-item :label="t('system.config.copyrightLink')" name="copyrightLink">
              <a-input
                v-model:value="basicConfig.copyrightLink"
                :placeholder="t('system.config.copyrightLinkPlaceholder')"
              />
            </a-form-item>

            <a-form-item :wrapper-col="{ span: 24 }">
              <a-space>
                <a-button type="primary" :loading="savingSystem" @click="saveSystemConfig">
                  {{ t('common.save') }}
                </a-button>
                <a-button @click="resetSystemConfig">{{ t('common.reset') }}</a-button>
              </a-space>
            </a-form-item>
          </a-form>
        </a-tab-pane>

        <a-tab-pane key="portal" :tab="t('system.config.tabPortal')">
          <a-form
            :model="basicConfig"
            :label-col="{ span: 6 }"
            :wrapper-col="{ span: 16 }"
            layout="horizontal"
          >
            <a-form-item :label="t('system.config.loginTitle')" name="loginPageTitle">
              <a-input
                v-model:value="basicConfig.loginPageTitle"
                :placeholder="t('system.config.loginTitlePlaceholder')"
              />
            </a-form-item>

            <a-form-item :label="t('system.config.loginSubtitle')" name="loginPageSubtitle">
              <a-input
                v-model:value="basicConfig.loginPageSubtitle"
                :placeholder="t('system.config.loginSubtitlePlaceholder')"
              />
            </a-form-item>

            <a-form-item :label="t('system.config.loginLayout')" name="loginLayout">
              <a-radio-group v-model:value="basicConfig.loginLayout">
                <a-radio value="center">{{ t('system.config.layoutCenter') }}</a-radio>
                <a-radio value="split">{{ t('system.config.layoutSplit') }}</a-radio>
                <a-radio value="compact">{{ t('system.config.layoutCompact') }}</a-radio>
              </a-radio-group>
            </a-form-item>

            <a-form-item :label="t('system.config.loginStyle')" name="loginStyle">
              <a-radio-group v-model:value="basicConfig.loginStyle">
                <a-radio value="cyber">{{ t('system.config.styleCyber') }}</a-radio>
                <a-radio value="simple">{{ t('system.config.styleSimple') }}</a-radio>
                <a-radio value="classic">{{ t('system.config.styleClassic') }}</a-radio>
              </a-radio-group>
            </a-form-item>

            <a-form-item :label="t('system.config.backgroundType')" name="loginBackgroundType">
              <a-radio-group v-model:value="basicConfig.loginBackgroundType">
                <a-radio value="video">{{ t('system.config.bgVideo') }}</a-radio>
                <a-radio value="image">{{ t('system.config.bgImage') }}</a-radio>
                <a-radio value="color">{{ t('system.config.bgColor') }}</a-radio>
              </a-radio-group>
            </a-form-item>

            <a-form-item
              v-if="basicConfig.loginBackgroundType === 'video'"
              :label="t('system.config.bgVideo')"
              name="loginBackgroundVideo"
            >
              <a-upload
                v-model:file-list="videoFileList"
                :before-upload="handleVideoBeforeUpload"
                :custom-request="handleVideoUpload"
                :show-upload-list="false"
                accept="video/*"
              >
                <a-space>
                  <a-button type="primary" :loading="videoUploading">
                    <UploadOutlined />
                    {{ videoUploading ? t('common.uploading') : t('common.uploadVideo') }}
                  </a-button>
                  <a-button v-if="basicConfig.loginBackgroundVideo" @click="handleVideoRemove">
                    <DeleteOutlined />
                    {{ t('common.remove') }}
                  </a-button>
                </a-space>
              </a-upload>
              <div v-if="basicConfig.loginBackgroundVideo" class="media-preview">
                <video
                  :src="formatMediaUrl(basicConfig.loginBackgroundVideo)"
                  controls
                  class="video-preview"
                ></video>
              </div>
            </a-form-item>

            <a-form-item
              v-if="basicConfig.loginBackgroundType === 'image'"
              :label="t('system.config.bgImage')"
              name="loginBackgroundImage"
            >
              <a-upload
                v-model:file-list="bgImageFileList"
                :before-upload="handleBgImageBeforeUpload"
                :custom-request="handleBgImageUpload"
                :show-upload-list="false"
                accept="image/*"
              >
                <a-space>
                  <a-avatar
                    v-if="basicConfig.loginBackgroundImage"
                    :size="120"
                    :src="formatMediaUrl(basicConfig.loginBackgroundImage)"
                  >
                    <template #icon>
                      <PictureOutlined />
                    </template>
                  </a-avatar>
                  <a-button type="primary" :loading="bgImageUploading">
                    <UploadOutlined />
                    {{ bgImageUploading ? t('common.uploading') : t('common.uploadImage') }}
                  </a-button>
                  <a-button v-if="basicConfig.loginBackgroundImage" @click="handleBgImageRemove">
                    <DeleteOutlined />
                    {{ t('common.remove') }}
                  </a-button>
                </a-space>
              </a-upload>
            </a-form-item>

            <a-form-item
              v-if="basicConfig.loginBackgroundType === 'color'"
              :label="t('system.config.bgColor')"
              name="loginBackgroundColor"
            >
              <a-space>
                <a-input
                  v-model:value="basicConfig.loginBackgroundColor"
                  :placeholder="t('system.config.bgColorPlaceholder')"
                />
                <a-input type="color" v-model:value="basicConfig.loginBackgroundColor" class="color-input" />
              </a-space>
            </a-form-item>

            <a-form-item :label="t('system.config.showOAuth')" name="showOAuthLogin">
              <a-switch v-model:checked="basicConfig.showOAuthLogin" />
            </a-form-item>

            <a-form-item :label="t('system.config.primaryColor')" name="primaryColor">
              <a-input
                v-model:value="basicConfig.primaryColor"
                :placeholder="t('system.config.primaryColorPlaceholder')"
              />
            </a-form-item>

            <a-form-item :label="t('system.config.secondaryColor')" name="secondaryColor">
              <a-input
                v-model:value="basicConfig.secondaryColor"
                :placeholder="t('system.config.secondaryColorPlaceholder')"
              />
            </a-form-item>

            <a-form-item :wrapper-col="{ span: 24 }">
              <a-space>
                <a-button type="primary" :loading="savingPortal" @click="savePortalConfig">
                  {{ t('common.save') }}
                </a-button>
                <a-button @click="resetPortalConfig">{{ t('common.reset') }}</a-button>
                <a-button @click="openPreview">{{ t('common.preview') }}</a-button>
              </a-space>
            </a-form-item>
          </a-form>
        </a-tab-pane>

        <a-tab-pane key="security" :tab="t('system.config.tabSecurity')">
          <a-form
            :model="securityConfig"
            :label-col="{ span: 6 }"
            :wrapper-col="{ span: 16 }"
            layout="horizontal"
          >
            <a-divider orientation="left">{{ t('system.config.captchaConfig') }}</a-divider>

            <a-form-item :label="t('system.config.captchaMode')" name="captcha.mode">
              <a-radio-group v-model:value="securityConfig.captcha.mode">
                <a-radio value="none">{{ t('system.config.captchaNone') }}</a-radio>
                <a-radio value="image">{{ t('system.config.captchaImage') }}</a-radio>
                <a-radio value="slider">{{ t('system.config.captchaSlider') }}</a-radio>
              </a-radio-group>
            </a-form-item>

            <template v-if="securityConfig.captcha.mode === 'image'">
              <a-form-item :label="t('system.config.imageKeyPrefix')" name="captcha.image.keyPrefix">
                <a-input v-model:value="securityConfig.captcha.image.keyPrefix" />
              </a-form-item>
              <a-form-item :label="t('system.config.imageExpireSeconds')" name="captcha.image.expireSeconds">
                <a-input-number v-model:value="securityConfig.captcha.image.expireSeconds" :min="30" :max="600" />
              </a-form-item>
              <a-form-item :label="t('system.config.imageWidth')" name="captcha.image.width">
                <a-input-number v-model:value="securityConfig.captcha.image.width" :min="80" :max="300" />
              </a-form-item>
              <a-form-item :label="t('system.config.imageHeight')" name="captcha.image.height">
                <a-input-number v-model:value="securityConfig.captcha.image.height" :min="30" :max="120" />
              </a-form-item>
              <a-form-item :label="t('system.config.imageLength')" name="captcha.image.length">
                <a-input-number v-model:value="securityConfig.captcha.image.length" :min="3" :max="8" />
              </a-form-item>
            </template>

            <template v-if="securityConfig.captcha.mode === 'slider'">
              <a-form-item :label="t('system.config.sliderKeyPrefix')" name="captcha.slider.keyPrefix">
                <a-input v-model:value="securityConfig.captcha.slider.keyPrefix" />
              </a-form-item>
              <a-form-item :label="t('system.config.sliderSecondaryKeyPrefix')" name="captcha.slider.secondaryKeyPrefix">
                <a-input v-model:value="securityConfig.captcha.slider.secondaryKeyPrefix" />
              </a-form-item>
              <a-form-item :label="t('system.config.sliderTokenExpireSeconds')" name="captcha.slider.tokenExpireSeconds">
                <a-input-number v-model:value="securityConfig.captcha.slider.tokenExpireSeconds" :min="30" :max="600" />
              </a-form-item>
              <a-form-item :label="t('system.config.sliderProvider')" name="captcha.slider.provider">
                <a-input v-model:value="securityConfig.captcha.slider.provider" />
              </a-form-item>
              <a-form-item :label="t('system.config.sliderSecondaryEnabled')" name="captcha.slider.secondaryEnabled">
                <a-switch v-model:checked="securityConfig.captcha.slider.secondaryEnabled" />
              </a-form-item>
            </template>

            <a-divider orientation="left">{{ t('system.config.passwordPolicy') }}</a-divider>

            <a-form-item :label="t('system.config.passwordStore')" name="passwordPolicy.store">
              <a-select v-model:value="securityConfig.passwordPolicy.store">
                <a-select-option value="bcrypt">bcrypt</a-select-option>
                <a-select-option value="sm2">sm2</a-select-option>
                <a-select-option value="sm4">sm4</a-select-option>
                <a-select-option value="argon2">argon2</a-select-option>
                <a-select-option value="scrypt">scrypt</a-select-option>
                <a-select-option value="pbkdf2">pbkdf2</a-select-option>
              </a-select>
            </a-form-item>

            <a-form-item :label="t('system.config.defaultPassword')" name="passwordPolicy.defaultPassword">
              <a-input-password
                v-model:value="securityConfig.passwordPolicy.defaultPassword"
                :placeholder="t('system.config.defaultPasswordPlaceholder')"
              />
            </a-form-item>

            <a-form-item :label="t('system.config.passwordMinLength')" name="passwordPolicy.minLength">
              <a-input-number v-model:value="securityConfig.passwordPolicy.minLength" :min="6" :max="32" />
            </a-form-item>

            <a-form-item :label="t('system.config.passwordRequireNumbers')" name="passwordPolicy.requireNumbers">
              <a-switch v-model:checked="securityConfig.passwordPolicy.requireNumbers" />
            </a-form-item>

            <a-form-item :label="t('system.config.passwordRequireUppercase')" name="passwordPolicy.requireUppercase">
              <a-switch v-model:checked="securityConfig.passwordPolicy.requireUppercase" />
            </a-form-item>

            <a-form-item :label="t('system.config.passwordRequireLowercase')" name="passwordPolicy.requireLowercase">
              <a-switch v-model:checked="securityConfig.passwordPolicy.requireLowercase" />
            </a-form-item>

            <a-form-item :label="t('system.config.passwordRequireSymbols')" name="passwordPolicy.requireSymbols">
              <a-switch v-model:checked="securityConfig.passwordPolicy.requireSymbols" />
            </a-form-item>

            <a-divider orientation="left">{{ t('system.config.loginSecurity') }}</a-divider>

            <a-form-item :label="t('system.config.failWindowMinutes')" name="loginSecurity.failWindowMinutes">
              <a-input-number
                v-model:value="securityConfig.loginSecurity.failWindowMinutes"
                :min="1"
                :max="1440"
                :style="{ width: '100%' }"
              />
            </a-form-item>

            <a-form-item :label="t('system.config.maxFailCount')" name="loginSecurity.maxFailCount">
              <a-input-number
                v-model:value="securityConfig.loginSecurity.maxFailCount"
                :min="1"
                :max="20"
                :style="{ width: '100%' }"
              />
            </a-form-item>

            <a-form-item :label="t('system.config.lockMinutes')" name="loginSecurity.lockMinutes">
              <a-input-number
                v-model:value="securityConfig.loginSecurity.lockMinutes"
                :min="1"
                :max="1440"
                :style="{ width: '100%' }"
              />
            </a-form-item>

            <a-divider orientation="left">{{ t('system.config.transportCrypto') }}</a-divider>

            <a-form-item :label="t('system.config.transportAlgorithm')" name="cryptoTransport.algorithm">
              <a-select v-model:value="securityConfig.cryptoTransport.algorithm">
                <a-select-option value="SM2">SM2</a-select-option>
              </a-select>
            </a-form-item>

            <a-form-item :label="t('system.config.transportCipher')" name="cryptoTransport.cipher">
              <a-select v-model:value="securityConfig.cryptoTransport.cipher">
                <a-select-option value="BCD">BCD</a-select-option>
                <a-select-option value="Hex">Hex</a-select-option>
              </a-select>
            </a-form-item>

            <a-form-item :label="t('system.config.transportPublicKey')" name="cryptoTransport.publicKey">
              <a-textarea
                v-model:value="securityConfig.cryptoTransport.publicKey"
                :rows="3"
                :placeholder="t('system.config.transportPublicKeyPlaceholder')"
              />
            </a-form-item>

            <a-form-item :label="t('system.config.transportPrivateKey')" name="cryptoTransport.privateKey">
              <a-textarea
                v-model:value="securityConfig.cryptoTransport.privateKey"
                :rows="3"
                :placeholder="t('system.config.transportPrivateKeyPlaceholder')"
              />
            </a-form-item>

            <a-form-item :wrapper-col="{ span: 24 }">
              <a-space>
                <a-button type="primary" :loading="savingSecurity" @click="saveSecurityConfig">
                  {{ t('common.save') }}
                </a-button>
                <a-button @click="resetSecurityConfig">{{ t('common.reset') }}</a-button>
              </a-space>
            </a-form-item>
          </a-form>
        </a-tab-pane>

        <a-tab-pane key="email" :tab="t('system.config.tabEmail')">
          <div class="email-config-layout">
            <div class="email-provider-list">
              <button
                v-for="provider in emailProviderOptions"
                :key="provider.value"
                type="button"
                class="email-provider-card"
                :class="{ 'email-provider-card--active': emailConfig.providerType === provider.value }"
                @click="handleEmailProviderChange(provider.value)"
              >
                <span class="email-provider-card__title">{{ t(provider.titleKey) }}</span>
                <span class="email-provider-card__desc">{{ t(provider.descKey) }}</span>
              </button>
            </div>

            <div class="email-config-main">
              <div class="email-config-banner">
                <div class="email-config-banner__label">{{ t('system.config.emailProvider') }}</div>
                <div class="email-config-banner__text">{{ t('system.config.emailProviderHint') }}</div>
              </div>

              <a-form
                :model="emailConfig"
                :label-col="{ span: 6 }"
                :wrapper-col="{ span: 16 }"
                layout="horizontal"
              >
                <a-form-item :label="t('system.config.senderAccount')" name="senderAccount">
                  <a-input
                    v-model:value="emailConfig.senderAccount"
                    :placeholder="t('system.config.senderAccountPlaceholder')"
                  />
                </a-form-item>

                <a-form-item :label="t('system.config.senderPassword')" name="senderPassword">
                  <a-input-password
                    v-model:value="emailConfig.senderPassword"
                    :placeholder="t('system.config.senderPasswordPlaceholder')"
                  />
                </a-form-item>

                <a-form-item :label="t('system.config.smtpHost')" name="smtpHost">
                  <a-input
                    v-model:value="emailConfig.smtpHost"
                    :placeholder="t('system.config.smtpHostPlaceholder')"
                  />
                </a-form-item>

                <a-form-item :label="t('system.config.smtpPort')" name="smtpPort">
                  <a-input-number
                    v-model:value="emailConfig.smtpPort"
                    :min="1"
                    :max="65535"
                    :style="{ width: '100%' }"
                  />
                </a-form-item>

                <a-form-item :label="t('system.config.authEnabled')" name="authEnabled">
                  <a-switch v-model:checked="emailConfig.authEnabled" />
                </a-form-item>

                <a-form-item :label="t('system.config.sslEnabled')" name="sslEnabled">
                  <a-switch v-model:checked="emailConfig.sslEnabled" />
                </a-form-item>

                <a-form-item :label="t('system.config.starttlsEnabled')" name="starttlsEnabled">
                  <a-switch v-model:checked="emailConfig.starttlsEnabled" />
                </a-form-item>

                <a-form-item :wrapper-col="{ span: 24 }">
                  <a-space>
                    <a-button type="primary" :loading="savingEmail" @click="saveEmail">
                      {{ t('common.save') }}
                    </a-button>
                    <a-button @click="resetEmailConfig">{{ t('common.reset') }}</a-button>
                  </a-space>
                </a-form-item>
              </a-form>
            </div>
          </div>
        </a-tab-pane>

        <a-tab-pane key="upload" :tab="t('system.config.tabUpload')">
          <a-form
            :model="fileUploadConfig"
            :label-col="{ span: 6 }"
            :wrapper-col="{ span: 16 }"
            layout="horizontal"
          >
            <a-form-item :label="t('system.config.storageType')" name="storageType">
              <a-select v-model:value="fileUploadConfig.storageType">
                <a-select-option value="LOCAL">{{ t('system.config.storageLocal') }}</a-select-option>
                <a-select-option value="OSS">{{ t('system.config.storageOss') }}</a-select-option>
                <a-select-option value="MINIO">{{ t('system.config.storageMinio') }}</a-select-option>
              </a-select>
            </a-form-item>

            <a-form-item :label="t('system.config.localUploadPath')" name="localUploadPath">
              <a-input
                v-model:value="fileUploadConfig.localUploadPath"
                :placeholder="t('system.config.localUploadPathPlaceholder')"
              />
            </a-form-item>

            <a-form-item :label="t('system.config.accessPrefix')" name="accessPrefix">
              <a-input
                v-model:value="fileUploadConfig.accessPrefix"
                :placeholder="t('system.config.accessPrefixPlaceholder')"
              />
            </a-form-item>

            <a-form-item :label="t('system.config.providerConfigJson')" name="providerConfigJson">
              <a-textarea
                v-model:value="fileUploadConfig.providerConfigJson"
                :rows="5"
                :placeholder="t('system.config.providerConfigJsonPlaceholder')"
              />
            </a-form-item>

            <a-form-item :wrapper-col="{ span: 24 }">
              <a-space>
                <a-button type="primary" :loading="savingUpload" @click="saveFileUploadConfig">
                  {{ t('common.save') }}
                </a-button>
                <a-button @click="resetFileUploadConfig">{{ t('common.reset') }}</a-button>
              </a-space>
            </a-form-item>
          </a-form>
        </a-tab-pane>
      </a-tabs>
    </a-card>

    <a-modal v-model:open="previewVisible" :title="t('system.config.previewTitle')" width="800px" :footer="null">
      <div class="preview-container">
        <div class="preview-login-page" :style="{ backgroundColor: basicConfig.loginBackgroundColor }">
          <video
            v-if="basicConfig.loginBackgroundType === 'video' && basicConfig.loginBackgroundVideo"
            class="preview-bg-video"
            autoplay
            muted
            loop
            playsinline
            :src="formatMediaUrl(basicConfig.loginBackgroundVideo)"
          ></video>
          <img
            v-if="basicConfig.loginBackgroundType === 'image' && basicConfig.loginBackgroundImage"
            class="preview-bg-image"
            :src="formatMediaUrl(basicConfig.loginBackgroundImage)"
            alt="background"
          />
          <div class="preview-content">
            <div class="preview-brand">
              <img
                v-if="formatMediaUrl(basicConfig.systemLogo)"
                :src="formatMediaUrl(basicConfig.systemLogo)"
                class="preview-logo"
                alt="logo"
              />
              <span v-else class="preview-system-name">{{ basicConfig.systemName }}</span>
            </div>
            <div class="preview-title">{{ basicConfig.loginPageTitle }}</div>
            <div class="preview-subtitle">{{ basicConfig.loginPageSubtitle }}</div>
            <div class="preview-copyright">{{ basicConfig.copyright }}</div>
          </div>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import type { UploadFile } from 'ant-design-vue'
import { message } from 'ant-design-vue'
import { useI18n } from 'vue-i18n'
import { DeleteOutlined, PictureOutlined, UploadOutlined } from '@ant-design/icons-vue'
import AvatarUpload from '@/components/AvatarUpload.vue'
import { uploadFile } from '@/api/system/file'
import {
  createDefaultEmailConfig,
  createDefaultFileUploadConfig,
  createDefaultSecurityConfig,
  createDefaultSystemBasicConfig,
  getEmailConfig,
  getFileUploadConfig,
  getSecurityConfig,
  getSystemBasicConfig,
  setEmailConfig,
  setFileUploadConfig,
  setSecurityConfig,
  setSystemBasicConfig,
  type EmailConfig,
  type FileUploadConfig,
  type SecurityConfig,
  type SystemBasicConfig,
} from '@/api/system/config'

const { t } = useI18n()

const activeTab = ref('system')
const loading = ref(false)
const previewVisible = ref(false)

const savingSystem = ref(false)
const savingPortal = ref(false)
const savingSecurity = ref(false)
const savingEmail = ref(false)
const savingUpload = ref(false)
const videoUploading = ref(false)
const bgImageUploading = ref(false)

const videoFileList = ref<UploadFile[]>([])
const bgImageFileList = ref<UploadFile[]>([])

const basicConfig = ref<SystemBasicConfig>(createDefaultSystemBasicConfig())
const securityConfig = ref<SecurityConfig>(createDefaultSecurityConfig())
const emailConfig = ref<EmailConfig>(createDefaultEmailConfig())
const fileUploadConfig = ref<FileUploadConfig>(createDefaultFileUploadConfig())

type EmailProviderPreset = 'local' | 'aliyun' | 'qq'

const emailProviderOptions: Array<{
  value: EmailProviderPreset
  titleKey: string
  descKey: string
}> = [
  {
    value: 'local',
    titleKey: 'system.config.providerLocalTitle',
    descKey: 'system.config.providerLocalDesc',
  },
  {
    value: 'aliyun',
    titleKey: 'system.config.providerAliyunTitle',
    descKey: 'system.config.providerAliyunDesc',
  },
  {
    value: 'qq',
    titleKey: 'system.config.providerQqTitle',
    descKey: 'system.config.providerQqDesc',
  },
]

const emailProviderPresets: Record<EmailProviderPreset, Partial<EmailConfig>> = {
  local: {
    providerType: 'local',
    smtpHost: '',
    smtpPort: 465,
    authEnabled: true,
    sslEnabled: true,
    starttlsEnabled: true,
  },
  aliyun: {
    providerType: 'aliyun',
    smtpHost: 'smtp.qiye.aliyun.com',
    smtpPort: 465,
    authEnabled: true,
    sslEnabled: true,
    starttlsEnabled: true,
  },
  qq: {
    providerType: 'qq',
    smtpHost: 'smtp.qq.com',
    smtpPort: 465,
    authEnabled: true,
    sslEnabled: true,
    starttlsEnabled: false,
  },
}

function normalizeSystemBasicConfig(config: Partial<SystemBasicConfig> | null | undefined): SystemBasicConfig {
  const defaults = createDefaultSystemBasicConfig()
  return {
    ...defaults,
    ...(config || {}),
    loginLayout: (config?.loginLayout || defaults.loginLayout) as SystemBasicConfig['loginLayout'],
  }
}

function normalizeSecurityConfig(config: Partial<SecurityConfig> | null | undefined): SecurityConfig {
  const defaults = createDefaultSecurityConfig()
  return {
    captcha: {
      ...defaults.captcha,
      ...(config?.captcha || {}),
      image: {
        ...defaults.captcha.image,
        ...(config?.captcha?.image || {}),
      },
      slider: {
        ...defaults.captcha.slider,
        ...(config?.captcha?.slider || {}),
      },
    },
    passwordPolicy: {
      ...defaults.passwordPolicy,
      ...(config?.passwordPolicy || {}),
    },
    loginSecurity: {
      ...defaults.loginSecurity,
      ...(config?.loginSecurity || {}),
    },
    cryptoTransport: {
      ...defaults.cryptoTransport,
      ...(config?.cryptoTransport || {}),
    },
  }
}

function normalizeEmailConfig(config: Partial<EmailConfig> | null | undefined): EmailConfig {
  const defaults = createDefaultEmailConfig()
  return {
    ...defaults,
    ...(config || {}),
    providerType: (config?.providerType || defaults.providerType) as EmailConfig['providerType'],
    smtpPort: typeof config?.smtpPort === 'number' ? config.smtpPort : defaults.smtpPort,
  }
}

function normalizeFileUploadConfig(config: Partial<FileUploadConfig> | null | undefined): FileUploadConfig {
  const defaults = createDefaultFileUploadConfig()
  return {
    ...defaults,
    ...(config || {}),
    storageType: ((config?.storageType || defaults.storageType) as FileUploadConfig['storageType']),
  }
}

function formatMediaUrl(value: string): string {
  const url = String(value || '')
  if (!url) return ''
  if (url.startsWith('data:') || url.startsWith('http://') || url.startsWith('https://')) {
    return url
  }
  if (url.startsWith('/')) {
    return url.startsWith('/api') ? url : `/api${url}`
  }
  return `/api/${url}`
}

async function loadAllConfig() {
  loading.value = true
  try {
    const [basic, security, email, upload] = await Promise.all([
      getSystemBasicConfig(),
      getSecurityConfig(),
      getEmailConfig(),
      getFileUploadConfig(),
    ])
    basicConfig.value = normalizeSystemBasicConfig(basic)
    securityConfig.value = normalizeSecurityConfig(security)
    emailConfig.value = normalizeEmailConfig(email)
    fileUploadConfig.value = normalizeFileUploadConfig(upload)
  } catch (e) {
    message.error(t('common.loadFailed'))
  } finally {
    loading.value = false
  }
}

async function saveSystemConfig() {
  savingSystem.value = true
  try {
    await setSystemBasicConfig(basicConfig.value)
    message.success(t('common.saveSuccess'))
  } catch (e) {
    message.error(t('common.saveFailed'))
  } finally {
    savingSystem.value = false
  }
}

async function savePortalConfig() {
  savingPortal.value = true
  try {
    await setSystemBasicConfig(basicConfig.value)
    message.success(t('common.saveSuccess'))
  } catch (e) {
    message.error(t('common.saveFailed'))
  } finally {
    savingPortal.value = false
  }
}

async function saveSecurityConfig() {
  savingSecurity.value = true
  try {
    await setSecurityConfig(securityConfig.value)
    message.success(t('common.saveSuccess'))
  } catch (e) {
    message.error(t('common.saveFailed'))
  } finally {
    savingSecurity.value = false
  }
}

async function saveFileUploadConfig() {
  savingUpload.value = true
  try {
    await setFileUploadConfig(fileUploadConfig.value)
    message.success(t('common.saveSuccess'))
  } catch (e) {
    message.error(t('common.saveFailed'))
  } finally {
    savingUpload.value = false
  }
}

async function saveEmail() {
  savingEmail.value = true
  try {
    await setEmailConfig(normalizeEmailConfig(emailConfig.value))
    message.success(t('common.saveSuccess'))
  } catch (e) {
    message.error(t('common.saveFailed'))
  } finally {
    savingEmail.value = false
  }
}

function resetSystemConfig() {
  const defaults = createDefaultSystemBasicConfig()
  basicConfig.value = {
    ...basicConfig.value,
    systemName: defaults.systemName,
    systemLogo: defaults.systemLogo,
    systemVersion: defaults.systemVersion,
    copyright: defaults.copyright,
    copyrightLink: defaults.copyrightLink,
  }
  message.info(t('common.resetSuccess'))
}

function resetPortalConfig() {
  const defaults = createDefaultSystemBasicConfig()
  basicConfig.value = {
    ...basicConfig.value,
    loginPageTitle: defaults.loginPageTitle,
    loginPageSubtitle: defaults.loginPageSubtitle,
    loginBackgroundType: defaults.loginBackgroundType,
    loginBackgroundVideo: defaults.loginBackgroundVideo,
    loginBackgroundImage: defaults.loginBackgroundImage,
    loginBackgroundColor: defaults.loginBackgroundColor,
    loginStyle: defaults.loginStyle,
    loginLayout: defaults.loginLayout,
    showOAuthLogin: defaults.showOAuthLogin,
    primaryColor: defaults.primaryColor,
    secondaryColor: defaults.secondaryColor,
  }
  videoFileList.value = []
  bgImageFileList.value = []
  message.info(t('common.resetSuccess'))
}

function resetSecurityConfig() {
  securityConfig.value = createDefaultSecurityConfig()
  message.info(t('common.resetSuccess'))
}

function resetEmailConfig() {
  emailConfig.value = createDefaultEmailConfig()
  message.info(t('common.resetSuccess'))
}

function resetFileUploadConfig() {
  fileUploadConfig.value = createDefaultFileUploadConfig()
  message.info(t('common.resetSuccess'))
}

function handleEmailProviderChange(provider: EmailProviderPreset) {
  const current = emailConfig.value
  const preset = emailProviderPresets[provider]
  emailConfig.value = normalizeEmailConfig({
    ...current,
    ...preset,
    providerType: provider,
    senderAccount: current.senderAccount,
    senderPassword: current.senderPassword,
  })
}

function openPreview() {
  previewVisible.value = true
}

function handleVideoRemove() {
  basicConfig.value.loginBackgroundVideo = ''
  videoFileList.value = []
}

function handleBgImageRemove() {
  basicConfig.value.loginBackgroundImage = ''
  bgImageFileList.value = []
}

function handleVideoBeforeUpload(file: File) {
  const isVideo = file.type.startsWith('video/')
  const isLt200M = file.size / 1024 / 1024 < 200
  if (!isVideo) {
    message.error(t('common.videoOnly'))
    return false
  }
  if (!isLt200M) {
    message.error(t('system.config.videoSizeLimit'))
    return false
  }
  return true
}

async function handleVideoUpload(options: any) {
  videoUploading.value = true
  try {
    const fileUrl = await uploadFile(options.file)
    basicConfig.value.loginBackgroundVideo = fileUrl
    videoFileList.value = [{ uid: options.file.uid, name: options.file.name, status: 'done', url: fileUrl }]
    options.onSuccess?.(fileUrl)
  } catch (e) {
    videoFileList.value = [{ uid: options.file.uid, name: options.file.name, status: 'error' }]
    options.onError?.(e)
    message.error(t('common.uploadFailed'))
  } finally {
    videoUploading.value = false
  }
}

function handleBgImageBeforeUpload(file: File) {
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isImage) {
    message.error(t('common.imageOnly'))
    return false
  }
  if (!isLt5M) {
    message.error(t('common.sizeLimit'))
    return false
  }
  return true
}

async function handleBgImageUpload(options: any) {
  bgImageUploading.value = true
  try {
    const fileUrl = await uploadFile(options.file)
    basicConfig.value.loginBackgroundImage = fileUrl
    bgImageFileList.value = [{ uid: options.file.uid, name: options.file.name, status: 'done', url: fileUrl }]
    options.onSuccess?.(fileUrl)
  } catch (e) {
    bgImageFileList.value = [{ uid: options.file.uid, name: options.file.name, status: 'error' }]
    options.onError?.(e)
    message.error(t('common.uploadFailed'))
  } finally {
    bgImageUploading.value = false
  }
}

function handleLogoUploadSuccess() {
  message.success(t('common.uploadSuccess'))
}

onMounted(() => {
  loadAllConfig()
})
</script>

<style scoped>
.system-config {
  padding: 0;
  height: 100%;
  min-height: 0;
  overflow: auto;
}

.system-logo-upload {
  display: inline-block;
}

.system-logo-upload :deep(.avatar-upload) {
  align-items: flex-start;
}

.system-logo-upload :deep(.avatar-upload .avatar-uploader .ant-upload) {
  width: 160px;
  height: 80px;
  border-radius: 8px;
  overflow: hidden;
}

.system-logo-upload :deep(.avatar-upload .avatar-image) {
  width: 100%;
  height: 100%;
  object-fit: contain;
  background-color: #fafafa;
}

.system-logo-upload :deep(.avatar-upload .upload-tips) {
  margin-top: 8px;
  text-align: left;
}

.media-preview {
  margin-top: 8px;
}

.video-preview {
  max-width: 300px;
  max-height: 200px;
}

.color-input {
  width: 50px;
}

.email-config-layout {
  display: grid;
  grid-template-columns: 240px minmax(0, 1fr);
  gap: 20px;
}

.email-provider-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.email-provider-card {
  border: 1px solid #d9e4f5;
  border-radius: 16px;
  background: linear-gradient(160deg, #ffffff 0%, #f6f9ff 100%);
  padding: 16px;
  text-align: left;
  cursor: pointer;
  transition: all 0.2s ease;
  box-shadow: 0 8px 20px rgba(15, 23, 42, 0.04);
}

.email-provider-card:hover,
.email-provider-card--active {
  border-color: #1677ff;
  transform: translateY(-1px);
  box-shadow: 0 14px 28px rgba(22, 119, 255, 0.12);
}

.email-provider-card__title {
  display: block;
  font-size: 15px;
  font-weight: 600;
  color: #132238;
}

.email-provider-card__desc {
  display: block;
  margin-top: 8px;
  font-size: 13px;
  line-height: 1.6;
  color: #5f6f85;
}

.email-config-main {
  border: 1px solid #eef3fb;
  border-radius: 20px;
  padding: 24px 16px 8px;
  background:
    radial-gradient(circle at top right, rgba(22, 119, 255, 0.12), transparent 34%),
    linear-gradient(180deg, #ffffff 0%, #fbfcff 100%);
}

.email-config-banner {
  margin: 0 16px 24px;
  padding: 16px 18px;
  border-radius: 16px;
  background: linear-gradient(135deg, #12314f 0%, #1f4f7b 55%, #2f7cc0 100%);
  color: #fff;
}

.email-config-banner__label {
  font-size: 13px;
  opacity: 0.78;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.email-config-banner__text {
  margin-top: 8px;
  font-size: 14px;
  line-height: 1.7;
}

.preview-container {
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  overflow: hidden;
}

.preview-login-page {
  position: relative;
  width: 100%;
  height: 400px;
  overflow: hidden;
}

.preview-bg-video {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.preview-bg-image {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.preview-content {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 100%;
  padding: 24px;
  background: linear-gradient(180deg, rgba(13, 2, 33, 0.7), rgba(13, 2, 33, 0.9));
}

.preview-brand {
  font-family: 'Orbitron', sans-serif;
  font-weight: 700;
  color: #fff;
  margin-bottom: 24px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.preview-logo {
  width: 80px;
  height: 80px;
  object-fit: cover;
}

.preview-system-name {
  font-size: 24px;
  text-shadow: 0 0 6px #05d9e8, 0 0 12px #05d9e8;
}

.preview-title {
  color: #9ca3af;
  margin-top: 6px;
  margin-bottom: 18px;
  font-size: 14px;
}

.preview-subtitle {
  color: #9ca3af;
  margin-bottom: 24px;
  font-size: 14px;
}

.preview-copyright {
  position: absolute;
  bottom: 16px;
  left: 0;
  right: 0;
  text-align: center;
  color: #9ca3af;
  font-size: 12px;
}

@media (max-width: 960px) {
  .email-config-layout {
    grid-template-columns: 1fr;
  }

  .email-provider-list {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  }
}
</style>
