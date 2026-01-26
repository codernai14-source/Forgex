<template>
  <div class="system-config">
    <a-card :bordered="false" :loading="loading" style="margin-top: 0;">
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
          <div class="logo-upload-wrapper">
            <!-- 长方形logo预览区域，类似头像上传的预览效果 -->
            <div class="logo-preview-avatar">
              <img v-if="systemLogoUrl" :src="systemLogoUrl" alt="system logo" />
              <div v-else class="logo-upload-placeholder">
                <SettingOutlined class="logo-upload-icon" />
                <div class="logo-upload-text">{{ t('common.uploadLogo') }}</div>
              </div>
            </div>
            
            <div class="logo-upload-actions">
              <a-upload
                v-model:file-list="logoFileList"
                :before-upload="handleLogoBeforeUpload"
                :custom-request="handleLogoUpload"
                :show-upload-list="false"
                accept="image/*"
              >
                <a-button type="primary" :loading="logoUploading">
                  <UploadOutlined /> {{ logoUploading ? t('common.uploading') : t('common.uploadLogo') }}
                </a-button>
              </a-upload>
              <a-button v-if="formData.systemLogo" @click="handleLogoRemove" style="margin-left: 8px;">
                <DeleteOutlined /> {{ t('common.remove') }}
              </a-button>
            </div>
          </div>
          <div style="margin-top: 8px; font-size: 12px; color: #9ca3af;">
            {{ t('system.config.logoTips') }}
          </div>
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
          <a-upload
            v-model:file-list="videoFileList"
            :before-upload="handleVideoBeforeUpload"
            :custom-request="handleVideoUpload"
            :show-upload-list="false"
            accept="video/*"
          >
            <a-space>
              <a-button type="primary" :loading="videoUploading" block>
                <UploadOutlined /> {{ videoUploading ? t('common.uploading') : t('common.uploadVideo') }}
              </a-button>
              <a-button v-if="formData.loginBackgroundVideo" @click="handleVideoRemove">
                <DeleteOutlined /> {{ t('common.remove') }}
              </a-button>
            </a-space>
          </a-upload>
          <div v-if="formData.loginBackgroundVideo" style="margin-top: 8px;">
            <video :src="formatMediaUrl(formData.loginBackgroundVideo)" controls style="max-width: 300px; max-height: 200px;"></video>
          </div>
        </a-form-item>

        <!-- 背景图片配置 -->
        <a-form-item
          v-if="formData.loginBackgroundType === 'image'"
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
                :size="120"
                v-if="formData.loginBackgroundImage"
                :src="formatMediaUrl(formData.loginBackgroundImage)"
              >
                <template #icon>
                  <PictureOutlined />
                </template>
              </a-avatar>
              <a-button type="primary" :loading="bgImageUploading" block>
                <UploadOutlined /> {{ bgImageUploading ? t('common.uploading') : t('common.uploadImage') }}
              </a-button>
              <a-button v-if="formData.loginBackgroundImage" @click="handleBgImageRemove">
                <DeleteOutlined /> {{ t('common.remove') }}
              </a-button>
            </a-space>
          </a-upload>
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
            :src="formatMediaUrl(previewConfig.loginBackgroundVideo)"
          ></video>
          <img
            v-if="previewConfig.loginBackgroundType === 'image' && previewConfig.loginBackgroundImage"
            class="preview-bg-image"
            :src="formatMediaUrl(previewConfig.loginBackgroundImage)"
          />
          <div class="preview-content">
            <div class="preview-brand">
              <img v-if="formatMediaUrl(previewConfig.systemLogo)" :src="formatMediaUrl(previewConfig.systemLogo)" class="preview-logo" />
              <span v-else class="preview-system-name">{{ previewConfig.systemName }}</span>
            </div>
            <div class="preview-title">{{ previewConfig.loginPageTitle }}</div>
            <div class="preview-subtitle">{{ previewConfig.loginPageSubtitle }}</div>
            <div class="preview-copyright">{{ previewConfig.copyright }}</div>
          </div>
        </div>
      </div>
    </a-modal>

    <!-- 图片剪裁弹窗 - 复用头像上传的vue-cropper方案 -->
    <a-modal
      v-model:open="cropVisible"
      :title="t('common.cropImage')"
      width="600px"
      @ok="handleCropConfirm"
      @cancel="handleCropCancel"
      :confirmLoading="logoUploading"
      destroyOnClose
    >
      <div class="crop-content">
        <vue-cropper
          ref="cropperRef"
          :img="cropperImg"
          :outputSize="1"
          outputType="png"
          :info="true"
          :full="false"
          :fixed="true"
          :fixedNumber="[1, 1]"
          :canMove="true"
          :canMoveBox="true"
          :centerBox="true"
          :autoCrop="true"
          :autoCropWidth="200"
          :autoCropHeight="200"
        ></vue-cropper>
      </div>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, onMounted, watch } from 'vue'
import { message, Modal } from 'ant-design-vue'
import { useI18n } from 'vue-i18n'
import { SettingOutlined, UploadOutlined, DeleteOutlined, PictureOutlined, CameraOutlined } from '@ant-design/icons-vue'
import { getSystemBasicConfig, setSystemBasicConfig } from '../../../api/system/config'
import { uploadFile } from '../../../api/system/file'
import type { SystemBasicConfig } from '../../../api/system/config'
import type { UploadFile } from 'ant-design-vue'
// 复用头像上传的剪裁组件
import 'vue-cropper/dist/index.css'
import { VueCropper } from 'vue-cropper'

const { t } = useI18n()

const loading = ref(false)
const saving = ref(false)
const logoUploading = ref(false)
const videoUploading = ref(false)
const bgImageUploading = ref(false)
const previewVisible = ref(false)

// 图片剪裁相关 - 复用头像上传的vue-cropper方案
const cropVisible = ref(false)
const cropperImg = ref('')
const cropperRef = ref()
const currentLogoFile = ref<File | null>(null)

// 文件列表
const logoFileList = ref<UploadFile[]>([])
const videoFileList = ref<UploadFile[]>([])
const bgImageFileList = ref<UploadFile[]>([])

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

const systemLogoUrl = computed(() => formatMediaUrl(formData.value.systemLogo))

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
  // 清空文件列表
  logoFileList.value = []
  videoFileList.value = []
  bgImageFileList.value = []
  message.info(t('common.resetSuccess'))
}

function handlePreview() {
  previewConfig.value = { ...formData.value }
  previewVisible.value = true
}

// 处理Logo删除
function handleLogoRemove() {
  formData.value.systemLogo = ''
  logoFileList.value = []
}

// 处理视频删除
function handleVideoRemove() {
  formData.value.loginBackgroundVideo = ''
  videoFileList.value = []
}

// 处理背景图片删除
function handleBgImageRemove() {
  formData.value.loginBackgroundImage = ''
  bgImageFileList.value = []
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
  
  // 读取文件显示在裁剪框，复用头像上传的逻辑
  const reader = new FileReader()
  reader.onload = (e) => {
    cropperImg.value = e.target?.result as string
    currentLogoFile.value = file
    cropVisible.value = true
  }
  reader.readAsDataURL(file)
  
  return false // 阻止自动上传，使用剪裁后的图片上传
}

async function handleLogoUpload(options: any) {
  // 这个函数现在不会被直接调用，因为我们在beforeUpload中返回了false
  // 所有上传都通过剪裁确认后处理
}

// 处理剪裁取消
function handleCropCancel() {
  cropVisible.value = false
  cropperImg.value = ''
  currentLogoFile.value = null
}

// 处理剪裁确认 - 复用头像上传的vue-cropper逻辑
async function handleCropConfirm() {
  if (!cropperRef.value) return
  
  logoUploading.value = true
  
  try {
    // 使用vue-cropper的API获取剪裁后的blob
    cropperRef.value.getCropBlob(async (blob: Blob) => {
      if (blob && currentLogoFile.value) {
        // 创建新的File对象
        const fileName = currentLogoFile.value?.name || 'logo.png'
        const file = new File([blob], fileName, { type: 'image/png' })
        
        // 上传剪裁后的文件，使用silentError避免http拦截器自动显示消息
        const fileUrl = await uploadFile(file, { silentError: true })
        
        // 更新logo路径，触发预览和系统logo更新
        formData.value.systemLogo = fileUrl
        
        // 更新文件列表
        logoFileList.value = [{ 
          uid: String((currentLogoFile.value as any).uid || Date.now()), 
          name: currentLogoFile.value.name, 
          status: 'done', 
          url: fileUrl 
        }]
        
        // 自动保存配置
        try {
          await setSystemBasicConfig(formData.value)
          message.success(t('common.uploadSuccess'))
        } catch (e: any) {
          console.error('自动保存配置失败:', e)
          message.warning(t('common.uploadSuccess') + '，但保存配置失败，请手动保存')
        }
        
        // 关闭弹窗并重置状态
        cropVisible.value = false
        cropperImg.value = ''
        currentLogoFile.value = null
      }
    })
  } catch (e: any) {
    console.error('Logo剪裁上传失败:', e)
    message.error(t('common.uploadFailed'))
    
    // 更新文件列表为错误状态
    if (currentLogoFile.value) {
      logoFileList.value = [{ 
        uid: currentLogoFile.value.uid, 
        name: currentLogoFile.value.name, 
        status: 'error' 
      }]
    }
    
    // 关闭弹窗并重置状态
    cropVisible.value = false
    cropperImg.value = ''
    currentLogoFile.value = null
  } finally {
    logoUploading.value = false
  }
}

function handleVideoBeforeUpload(file: File) {
  const isVideo = file.type.startsWith('video/')
  const isLt200M = file.size / 1024 / 1024 < 200
  if (!isVideo) {
    message.error(t('common.videoOnly'))
    return false
  }
  if (!isLt200M) {
    message.error('视频大小不能超过200MB')
    return false
  }
  return true
}

async function handleVideoUpload(options: any) {
  videoUploading.value = true
  try {
    // 使用文件上传接口
    const fileUrl = await uploadFile(options.file)
    formData.value.loginBackgroundVideo = fileUrl
    // 更新文件列表
    videoFileList.value = [{ uid: options.file.uid, name: options.file.name, status: 'done', url: fileUrl }]
    
    // 自动保存配置
    try {
      await setSystemBasicConfig(formData.value)
      message.success(t('common.uploadSuccess'))
    } catch (e: any) {
      console.error('自动保存配置失败:', e)
      message.warning(t('common.uploadSuccess') + '，但保存配置失败，请手动保存')
    }
  } catch (e: any) {
    console.error('视频上传失败:', e)
    message.error(t('common.uploadFailed'))
    // 更新文件列表为错误状态
    videoFileList.value = [{ uid: options.file.uid, name: options.file.name, status: 'error' }]
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
    // 使用文件上传接口
    const fileUrl = await uploadFile(options.file)
    formData.value.loginBackgroundImage = fileUrl
    // 更新文件列表
    bgImageFileList.value = [{ uid: options.file.uid, name: options.file.name, status: 'done', url: fileUrl }]
    
    // 自动保存配置
    try {
      await setSystemBasicConfig(formData.value)
      message.success(t('common.uploadSuccess'))
    } catch (e: any) {
      console.error('自动保存配置失败:', e)
      message.warning(t('common.uploadSuccess') + '，但保存配置失败，请手动保存')
    }
  } catch (e: any) {
    console.error('背景图片上传失败:', e)
    message.error(t('common.uploadFailed'))
    // 更新文件列表为错误状态
    bgImageFileList.value = [{ uid: options.file.uid, name: options.file.name, status: 'error' }]
  } finally {
    bgImageUploading.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.system-config {
  padding: 0;
  margin-top: 0;
  height: 100%;
  min-height: 0;
  overflow: auto;
}

/* Logo上传预览样式 */
.logo-upload-wrapper {
  display: flex;
  align-items: center;
  gap: 24px;
}

/* 长方形logo预览区域样式 */
.logo-preview-avatar {
  width: 128px;
  height: 64px;
  border: 1px solid #d9d9d9;
  border-radius: 8px;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f5f5f5;
  cursor: pointer;
  transition: all 0.3s ease;
}

.logo-preview-avatar:hover {
  border-color: #1890ff;
  box-shadow: 0 0 8px rgba(24, 144, 255, 0.2);
}

.logo-preview-avatar img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

/* 上传占位符样式 */
.logo-upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  color: #9ca3af;
}

.logo-upload-icon {
  font-size: 24px;
  margin-bottom: 8px;
}

.logo-upload-text {
  font-size: 14px;
}

/* 上传操作按钮区域 */
.logo-upload-actions {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

/* 剪裁弹窗样式 - 复用头像上传的vue-cropper样式 */
.crop-content {
  height: 400px;
  width: 100%;
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
