<template>
  <div class="avatar-upload">
    <a-upload
      name="file"
      list-type="picture-card"
      class="avatar-uploader"
      :show-upload-list="false"
      :before-upload="beforeUpload"
      :custom-request="handleUpload"
    >
      <img v-if="imageUrl" :src="imageUrl" alt="avatar" class="avatar-image" />
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
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { message } from 'ant-design-vue'
import { PlusOutlined, LoadingOutlined } from '@ant-design/icons-vue'
import { uploadAvatar } from '@/api/profile'

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

/**
 * 上传前校验
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
  
  return true
}

/**
 * 自定义上传
 */
async function handleUpload(options: any) {
  const { file } = options
  
  loading.value = true
  try {
    const url = await uploadAvatar(file)
    imageUrl.value = url
    emit('update:modelValue', url)
    emit('success', url)
    message.success('头像上传成功')
  } catch (error) {
    console.error('头像上传失败:', error)
    message.error('头像上传失败')
  } finally {
    loading.value = false
  }
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
</style>
