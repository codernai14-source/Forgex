<template>
  <div class="system-config">
    <!-- 页面标题 -->
    <a-page-header :title="t('system.config.title')" />

    <a-card :bordered="false" :loading="loading">
      <a-form
        :model="formData"
        :label-col="{ span: 6 }"
        :wrapper-col="{ span: 16 }"
        layout="horizontal"
      >
        <!-- ==================== 系统基本信息 =================== -->
        <a-divider orientation="left">{{ t('system.config.basicInfo') }}</a-divider>

        <a-form-item :label="t('system.config.systemName')" name="systemName">
          <a-input v-model:value="formData.systemName" :placeholder="t('system.config.systemNamePlaceholder')" />
        </a-form-item>

        <a-form-item :label="t('system.config.systemLogo')" name="systemLogo">
          <a-space>
            <a-input
              v-model:value="formData.systemLogo"
              :placeholder="t('system.config.logoUrlPlaceholder')"
              style="flex: 1"
            />
            <a-upload
              :show-upload-list="false"
              :before-upload="handleLogoBeforeUpload"
              :customRequest="handleLogoUpload"
              accept="image/*"
            >
              <template #trigger>
                <a-button type="primary" :loading="logoUploading">
                  {{ logoUploading ? t('common.uploading') : t('common.upload') }}
                </a-button>
              </template>
            </a-upload>
          </a-space>
        </a-form-item>

        <a-form-item :label="t('system.config.systemVersion')" name="systemVersion">
          <a-input v-model:value="formData.systemVersion" :placeholder="t('system.config.versionPlaceholder')" />
        </a-form-item>

        <!-- ==================== 登录页配置 =================== -->
        <a-divider orientation="left">{{ t('system.config.loginPage') }}</a-divider>

        <a-form-item :label="t('system.config.loginTitle')" name="loginPageTitle">
          <a-input v-model:value="formData.loginPageTitle" :placeholder="t('system.config.loginTitlePlaceholder')" />
        </a-form-item>

        <a-form-item :label="t('system.config.loginSubtitle')" name="loginPageSubtitle">
          <a-input v-model:value="formData.loginPageSubtitle" :placeholder="t('system.config.loginSubtitlePlaceholder')" />
        </a-form-item>

        <a-form-item :label="t('system.config.backgroundType')" name="loginBackgroundType">
          <a-radio-group v-model:value="formData.loginBackgroundType">
            <a-radio value="video">{{ t('system.config.bgVideo') }}</a-radio>
            <a-radio value="image">{{ t('system.config.bgImage') }}</a-radio>
            <a-radio value="color">{{ t('system.config.bgColor') }}</a-radio>
          </a-radio-group>
        </a-form-item>

        <!-- 背景视频配置 -->
        <a-form-item
          v-if="formData.loginBackgroundType === 'video'"
          :label="t('system.config.bgVideo')"
          name="loginBackgroundVideo"
        >
          <a-space>
            <a-input
              v-model:value="formData.loginBackgroundVideo"
              :placeholder="t('system.config.bgVideoPlaceholder')"
              style="flex: 1"
            />
            <a-upload
              :show-upload-list="false"
              :before-upload="handleVideoBeforeUpload"
              :custom-request="handleVideoUpload"
              accept="video/*"
            >
              <template #trigger>
                <a-button type="primary" :loading="videoUploading">
                  {{ videoUploading ? t('common.uploading') : t('common.upload') }}
                </a-button>
              </template>
            </a-upload>
          </a-space>
        </a-form-item>

        <!-- 背景图片配置 -->
        <a-form-item
          v-if="formData.loginBackgroundType === 'image'"
          :label="t('system.config.bgImage')"
          name="loginBackgroundImage"
        >
          <a-space>
            <a-input
              v-model:value="formData.loginBackgroundImage"
              :placeholder="t('system.config.bgImagePlaceholder')"
              style="flex: 1"
            />
            <a-upload
              :show-upload-list="false"
              :before-upload="handleBgImageBeforeUpload"
              :custom-request="handleBgImageUpload"
              accept="image/*"
            >
              <template #trigger>
                <a-button type="primary" :loading="bgImageUploading">
                  {{ bgImageUploading ? t('common.uploading') : t('common.upload') }}
                </a-button>
              </template>
            </a-upload>
          </a-space>
        </a-form-item>

        <!-- 背景颜色配置 -->
        <a-form-item
          v-if="formData.loginBackgroundType === 'color'"
          :label="t('system.config.bgColor')"
          name="loginBackgroundColor"
        >
          <a-space>
            <a-input
              v-model:value="formData.loginBackgroundColor"
              style="flex: 1"
              placeholder="#000000"
            >
              <template #prefix>
                <div :style="{ backgroundColor: formData.loginBackgroundColor, width: '20px', height: '20px', borderRadius: '2px', border: '1px solid #d9d9d9' }"></div>
              </template>
            </a-input>
            <a-input type="color" v-model:value="formData.loginBackgroundColor" style="width: 50px" />
          </a-space>
        </a-form-item>

        <a-form-item :label="t('system.config.loginStyle')" name="loginStyle">
          <a-radio-group v-model:value="formData.loginStyle">
            <a-radio value="cyber">{{ t('system.config.styleCyber') }}</a-radio>
            <a-radio value="simple">{{ t('system.config.styleSimple') }}</a-radio>
            <a-radio value="classic">{{ t('system.config.styleClassic') }}</a-radio>
          </a-radio-group>
        </a-form-item>

        <a-form-item :label="t('system.config.showOAuth')" name="showOAuthLogin">
          <a-switch v-model:checked="formData.showOAuthLogin" />
        </a-form-item>

        <!-- ==================== 主题配色 =================== -->
        <a-divider orientation="left">{{ t('system.config.themeColor') }}</a-divider>

        <a-form-item :label="t('system.config.primaryColor')" name="primaryColor">
          <a-space>
            <a-input
              v-model:value="formData.primaryColor"
              placeholder="#05d9e8"
              style="flex: 1"
            >
              <template #prefix>
                <div :style="{ backgroundColor: formData.primaryColor, width: '20px', height: '20px', borderRadius: '2px', border: '1px solid #d9d9d9' }"></div>
              </template>
            </a-input>
            <a-input type="color" v-model:value="formData.primaryColor" style="width: 50px" />
          </a-space>
        </a-form-item>

        <a-form-item :label="t('system.config.secondaryColor')" name="secondaryColor">
          <a-space>
            <a-input
              v-model:value="formData.secondaryColor"
              placeholder="#ff2a6d"
              style="flex: 1"
            >
              <template #prefix>
                <div :style="{ backgroundColor: formData.secondaryColor, width: '20px', height: '20px', borderRadius: '2px', border: '1px solid #d9d9d9' }"></div>
              </template>
            </a-input>
            <a-input type="color" v-model:value="formData.secondaryColor" style="width: 50px" />
          </a-space>
        </a-form-item>

        <!-- ==================== 版权信息 =================== -->
        <a-divider orientation="left">{{ t('system.config.copyrightInfo') }}</a-divider>

        <a-form-item :label="t('system.config.copyright')" name="copyright">
          <a-input v-model:value="formData.copyright" :placeholder="t('system.config.copyrightPlaceholder')" />
        </a-form-item>

        <a-form-item :label="t('system.config.copyrightLink')" name="copyrightLink">
          <a-input v-model:value="formData.copyrightLink" placeholder="https://example.com" />
        </a-form-item>

        <!-- 操作按钮 -->
        <a-form-item :wrapper-col="{ span: 24 }">
          <a-space>
            <a-button type="primary" @click="handleSave" :loading="saving">
              {{ t('common.save') }}
            </a-button>
            <a-button @click="handleReset">
              {{ t('common.reset') }}
            </a-button>
            <a-button @click="handlePreview">
              {{ t('common.preview') }}
            </a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <!-- 预览弹窗 -->
    <a-modal
      v-model:open="previewVisible"
      :title="t('system.config.previewTitle')"
      width="800px"
      :footer="null"
    >
      <div class="preview-container">
        <div class="preview-login-page" :style="{ backgroundColor: previewConfig.loginBackgroundColor }">
          <video
            v-if="previewConfig.loginBackgroundType === 'video' && previewConfig.loginBackgroundVideo"
            class="preview-bg-video"
            autoplay
            muted
            loop
            playsinline
            :src="previewConfig.loginBackgroundVideo"
          ></video>
          <img
            v-if="previewConfig.loginBackgroundType === 'image' && previewConfig.loginBackgroundImage"
            class="preview-bg-image"
            :src="formatPreviewImage(previewConfig.loginBackgroundImage)"
          />
          <div class="preview-content">
            <div class="preview-brand">
              <img v-if="formatPreviewImage(previewConfig.systemLogo)" :src="formatPreviewImage(previewConfig.systemLogo)" class="preview-logo" />
              <span v-else class="preview-system-name">{{ previewConfig.systemName }}</span>
            </div>
            <div class="preview-title">{{ previewConfig.loginPageTitle }}</div>
            <div class="preview-subtitle">{{ previewConfig.loginPageSubtitle }}</div>
            <div class="preview-copyright">{{ previewConfig.copyright }}</div>
          </div>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { useI18n } from 'vue-i18n'
import { getSystemBasicConfig, setSystemBasicConfig } from '../../../api/system/config'
import type { SystemBasicConfig } from '../../../api/system/config'

const { t } = useI18n()

const loading = ref(false)
const saving = ref(false)
const logoUploading = ref(false)
const videoUploading = ref(false)
const bgImageUploading = ref(false)
const previewVisible = ref(false)

const formData = ref<SystemBasicConfig>({
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
  showOAuthLogin: true,
  primaryColor: '#05d9e8',
  secondaryColor: '#ff2a6d'
})

const previewConfig = ref<SystemBasicConfig>({ ...formData.value })

async function loadData() {
  try {
    loading.value = true
    const data = await getSystemBasicConfig()
    if (data) {
      formData.value = { ...data }
      previewConfig.value = { ...data }
    }
  } catch (e) {
    message.error(t('common.loadFailed'))
  } finally {
    loading.value = false
  }
}

async function handleSave() {
  try {
    saving.value = true
    await setSystemBasicConfig(formData.value)
    message.success(t('common.saveSuccess'))
    previewConfig.value = { ...formData.value }
  } catch (e) {
    message.error(t('common.saveFailed'))
  } finally {
    saving.value = false
  }
}

function handleReset() {
  formData.value = {
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
    showOAuthLogin: true,
    primaryColor: '#05d9e8',
    secondaryColor: '#ff2a6d'
  }
  message.info(t('common.resetSuccess'))
}

function handlePreview() {
  previewConfig.value = { ...formData.value }
  previewVisible.value = true
}

function formatPreviewImage(value: string) {
  if (!value) return ''
  if (value.startsWith('data:')) {
    return value
  }
  return value.startsWith('/') ? value : ''
}

function handleLogoBeforeUpload(file: File) {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isImage) {
    message.error(t('common.imageOnly'))
    return false
  }
  if (!isLt2M) {
    message.error(t('common.sizeLimit'))
    return false
  }
  return true
}

async function handleLogoUpload(file: File) {
  logoUploading.value = true
  try {
    const base64 = await fileToBase64(file)
    formData.value.systemLogo = base64
    message.success(t('common.uploadSuccess'))
  } catch (e) {
    message.error(t('common.uploadFailed'))
  } finally {
    logoUploading.value = false
  }
}

function handleVideoBeforeUpload(file: File) {
  const isVideo = file.type.startsWith('video/')
  const isLt20M = file.size / 1024 / 1024 < 20
  if (!isVideo) {
    message.error(t('common.videoOnly'))
    return false
  }
  if (!isLt20M) {
    message.error(t('common.sizeLimit'))
    return false
  }
  return true
}

async function handleVideoUpload(file: File) {
  videoUploading.value = true
  try {
    const base64 = await fileToBase64(file)
    formData.value.loginBackgroundVideo = base64
    message.success(t('common.uploadSuccess'))
  } catch (e) {
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

async function handleBgImageUpload(file: File) {
  bgImageUploading.value = true
  try {
    const base64 = await fileToBase64(file)
    formData.value.loginBackgroundImage = base64
    message.success(t('common.uploadSuccess'))
  } catch (e) {
    message.error(t('common.uploadFailed'))
  } finally {
    bgImageUploading.value = false
  }
}

function fileToBase64(file: File): Promise<string> {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.readAsDataURL(file)
    reader.onload = () => resolve(reader.result as string)
    reader.onerror = reject
  })
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.system-config {
  padding: 24px;
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
</style>
