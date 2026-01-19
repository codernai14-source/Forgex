<template>
  <div class="avatar-upload">
    <a-upload
      name="file"
      list-type="picture-card"
      class="avatar-uploader"
      :show-upload-list="false"
      :before-upload="beforeUpload"
      :custom-request="customRequest"
    >
      <img v-if="displayUrl" :src="displayUrl" alt="avatar" class="avatar-image" />
      <div v-else class="upload-placeholder">
        <loading-outlined v-if="loading"></loading-outlined>
        <plus-outlined v-else></plus-outlined>
        <div class="upload-text">上传头像</div>
      </div>
    </a-upload>
    <div class="upload-tips">
      <p>支持 JPG、PNG、GIF 格式</p>
      <p>文件大小不超过 2MB</p>
    </div>

    <!-- 裁剪弹窗 -->
    <a-modal
      v-model:open="cropperVisible"
      title="头像裁剪"
      :width="600"
      @ok="handleCrop"
      @cancel="cancelCrop"
      :confirmLoading="uploading"
    >
      <div class="cropper-content">
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
import { ref, watch, computed } from 'vue'
import { message } from 'ant-design-vue'
import { PlusOutlined, LoadingOutlined } from '@ant-design/icons-vue'
import { uploadAvatar } from '@/api/profile'
import 'vue-cropper/dist/index.css'
import { VueCropper } from 'vue-cropper'

interface Props {
  modelValue?: string
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: ''
})

const emit = defineEmits<{
  'update:modelValue': [value: string]
  'success': [url: string]
}>()

const imageUrl = ref(props.modelValue)
const loading = ref(false)
const uploading = ref(false)

// 裁剪相关
const cropperVisible = ref(false)
const cropperImg = ref('')
const cropperRef = ref()
const currentFile = ref<File | null>(null)

// 监听 props.modelValue 变化，同步更新 imageUrl
watch(() => props.modelValue, (val) => {
  imageUrl.value = val
})

// 处理图片显示 URL，如果是相对路径则自动拼接 /api
const displayUrl = computed(() => {
  const url = imageUrl.value
  if (!url) return ''
  if (url.startsWith('data:') || url.startsWith('http') || url.startsWith('https')) {
    return url
  }
  // 处理相对路径
  if (url.startsWith('/')) {
    return url.startsWith('/api') ? url : `/api${url}`
  }
  return `/api/${url}`
})

/**
 * 上传前校验并打开裁剪
 */
function beforeUpload(file: File) {
  const isImage = file.type === 'image/jpeg' || file.type === 'image/png' || file.type === 'image/gif'
  if (!isImage) {
    message.error('只能上传 JPG、PNG、GIF 格式的图片！')
    return false
  }
  
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isLt2M) {
    message.error('图片大小不能超过 2MB！')
    return false
  }

  // 读取文件显示在裁剪框
  const reader = new FileReader()
  reader.onload = (e) => {
    cropperImg.value = e.target?.result as string
    currentFile.value = file
    cropperVisible.value = true
  }
  reader.readAsDataURL(file)
  
  // 阻止默认上传
  return false
}

// 这里的 customRequest 其实不会被触发，因为 beforeUpload 返回了 false
// 但为了类型兼容保留
function customRequest() {}

/**
 * 确认裁剪并上传
 */
function handleCrop() {
  uploading.value = true
  cropperRef.value.getCropBlob(async (blob: Blob) => {
    try {
      // 创建一个新的 File 对象
      const fileName = currentFile.value?.name || 'avatar.png'
      const file = new File([blob], fileName, { type: 'image/png' })
      
      loading.value = true
      const url = await uploadAvatar(file)
      imageUrl.value = url
      emit('update:modelValue', url)
      emit('success', url)
      message.success('头像上传成功')
      cropperVisible.value = false
    } catch (error) {
      console.error('头像上传失败:', error)
      message.error('头像上传失败')
    } finally {
      loading.value = false
      uploading.value = false
    }
  })
}

function cancelCrop() {
  cropperVisible.value = false
  cropperImg.value = ''
  currentFile.value = null
}
</script>

<style scoped lang="less">
.avatar-upload {
  display: flex;
  flex-direction: column;
  align-items: center;
  
  .avatar-uploader {
    :deep(.ant-upload) {
      width: 128px;
      height: 128px;
      border-radius: 50%;
      overflow: hidden;
    }
  }
  
  .avatar-image {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
  
  .upload-placeholder {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    height: 100%;
    color: #999;
    
    .anticon {
      font-size: 32px;
      margin-bottom: 8px;
    }
    
    .upload-text {
      font-size: 14px;
    }
  }
  
  .upload-tips {
    margin-top: 16px;
    text-align: center;
    color: #999;
    font-size: 12px;
    
    p {
      margin: 4px 0;
    }
  }
}

.cropper-content {
  height: 400px;
  width: 100%;
}
</style>
