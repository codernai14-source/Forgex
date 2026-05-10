<template>
  <div class="android-version-page">
    <FxDynamicTable
      ref="tableRef"
      table-code="AndroidVersionTable"
      :request="handleRequest"
      :show-query-form="true"
      :show-column-setting="true"
    >
      <template #toolbar>
        <a-space :size="8">
          <a-button type="primary" @click="openUploadDialog">{{ t('common.add') }}</a-button>
        </a-space>
      </template>

      <template #status="{ record }">
        <a-tag :color="record.status === 1 ? 'green' : 'default'">
          {{ record.status === 1 ? t('common.enabled') : t('common.disabled') }}
        </a-tag>
      </template>

      <template #fileSize="{ record }">
        <span>{{ formatFileSize(record.fileSize) }}</span>
      </template>

      <template #fileUrl="{ record }">
        <a-space v-if="record.fileUrl">
          <a :href="normalizeMediaUrl(record.fileUrl)" target="_blank">{{ t('common.download') }}</a>
          <a @click="copyText(record.fileUrl)">{{ t('common.copyLink') }}</a>
        </a-space>
        <span v-else>-</span>
      </template>

      <template #action="{ record }">
        <a-space>
          <a @click="openEditDialog(record)">{{ t('common.edit') }}</a>
          <a style="color: #ff4d4f" @click="handleDelete(record.id)">{{ t('common.delete') }}</a>
        </a-space>
      </template>
    </FxDynamicTable>

    <BaseFormDialog
      v-model:open="dialogVisible"
      :title="dialogMode === 'upload' ? t('system.androidVersion.uploadTitle') : t('system.androidVersion.editTitle')"
      :loading="saving"
      :width="720"
      @submit="handleSubmit"
    >
      <a-form
        ref="formRef"
        :model="formData"
        :rules="rules"
        :label-col="{ span: 5 }"
        :wrapper-col="{ span: 18 }"
      >
        <a-form-item :label="t('system.androidVersion.versionCode')" name="versionCode">
          <a-input-number
            v-model:value="formData.versionCode"
            :min="1"
            :precision="0"
            style="width: 100%"
          />
        </a-form-item>

        <a-form-item :label="t('system.androidVersion.versionName')" name="versionName">
          <a-input v-model:value="formData.versionName" :placeholder="t('system.androidVersion.versionNamePlaceholder')" />
        </a-form-item>

        <a-form-item v-if="dialogMode === 'edit'" :label="t('common.status')" name="status">
          <a-radio-group v-model:value="formData.status">
            <a-radio :value="1">{{ t('common.enabled') }}</a-radio>
            <a-radio :value="0">{{ t('common.disabled') }}</a-radio>
          </a-radio-group>
        </a-form-item>

        <a-form-item v-if="dialogMode === 'upload'" :label="t('system.androidVersion.apkFile')" name="file">
          <a-upload
            :before-upload="beforeUpload"
            :show-upload-list="true"
            :max-count="1"
            :disabled="saving"
            accept=".apk,application/vnd.android.package-archive"
          >
            <a-button :disabled="saving">{{ t('system.androidVersion.chooseApk') }}</a-button>
          </a-upload>
          <div v-if="selectedFile" class="file-hint">
            {{ selectedFile.name }} ({{ formatFileSize(selectedFile.size) }})
          </div>
        </a-form-item>

        <a-form-item v-if="dialogMode === 'upload' && uploadTask.uploadId" :label="t('system.androidVersion.uploadProgress')">
          <div class="upload-progress">
            <a-progress :percent="uploadTask.progress" :status="uploadTask.status === 'FAILED' ? 'exception' : undefined" />
            <div class="upload-progress__meta">
              <span>{{ uploadTask.statusText }}</span>
              <span>{{ uploadTask.uploadedChunks }}/{{ uploadTask.totalChunks }} {{ t('system.androidVersion.chunkCount') }}</span>
            </div>
            <a-button v-if="saving" size="small" danger @click="handleCancelUpload">{{ t('system.androidVersion.cancelUpload') }}</a-button>
          </div>
        </a-form-item>

        <a-form-item :label="t('system.androidVersion.changelog')" name="changelog">
          <a-textarea
            v-model:value="formData.changelog"
            :rows="5"
            :placeholder="t('system.androidVersion.changelogPlaceholder')"
          />
        </a-form-item>
      </a-form>
    </BaseFormDialog>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { message, Modal } from 'ant-design-vue'
import { useI18n } from 'vue-i18n'
import FxDynamicTable from '@/components/common/FxDynamicTable.vue'
import BaseFormDialog from '@/components/common/BaseFormDialog.vue'
import {
cancelAndroidVersionUpload,
  completeAndroidVersionUpload,
  deleteAndroidVersion,
  getAndroidVersionPage,
  getAndroidVersionUploadStatus,
  initAndroidVersionUpload,
  updateAndroidVersion,
  uploadAndroidVersionChunk,
  type AndroidUploadTask,
  type AndroidVersionItem,
  type AndroidVersionSaveParam,
} from '@/api/system/androidVersion'
import { normalizeMediaUrl } from '@/utils/media'

type DialogMode = 'upload' | 'edit'

const CHUNK_SIZE = 8 * 1024 * 1024
const MAX_RETRY = 3
const HASH_MAX_SIZE = 128 * 1024 * 1024
const TASK_STORAGE_PREFIX = 'fx-android-version-upload:'

const tableRef = ref()
const formRef = ref()
const { t } = useI18n()
const dialogVisible = ref(false)
const saving = ref(false)
const dialogMode = ref<DialogMode>('upload')
const selectedFile = ref<File | null>(null)
const cancelRequested = ref(false)

const uploadTask = reactive({
  uploadId: '',
  progress: 0,
  uploadedChunks: 0,
  totalChunks: 0,
  status: '',
  statusText: '',
})

const formData = reactive<AndroidVersionSaveParam>({
  id: undefined,
  versionCode: 1,
  versionName: '',
  changelog: '',
  status: 1,
})

const rules = computed(() => ({
  versionCode: [{ required: true, message: t('system.androidVersion.versionCodeRequired'), trigger: 'change' }],
  versionName: [{ required: true, message: t('system.androidVersion.versionNameRequired'), trigger: 'blur' }],
}))

async function handleRequest(payload: {
  page: { current: number; pageSize: number }
  query: Record<string, any>
}) {
  const result = await getAndroidVersionPage({
    ...payload.query,
    pageNum: payload.page.current,
    pageSize: payload.page.pageSize,
  })
  return {
    records: result.records || [],
    total: result.total || 0,
  }
}

function resetForm() {
  formData.id = undefined
  formData.versionCode = 1
  formData.versionName = ''
  formData.changelog = ''
  formData.status = 1
  selectedFile.value = null
  resetUploadTask()
}

function resetUploadTask() {
  uploadTask.uploadId = ''
  uploadTask.progress = 0
  uploadTask.uploadedChunks = 0
  uploadTask.totalChunks = 0
  uploadTask.status = ''
  uploadTask.statusText = t('system.androidVersion.waitingUpload')
  cancelRequested.value = false
}

function openUploadDialog() {
  dialogMode.value = 'upload'
  resetForm()
  dialogVisible.value = true
}

function openEditDialog(record: AndroidVersionItem) {
  dialogMode.value = 'edit'
  formData.id = record.id
  formData.versionCode = record.versionCode
  formData.versionName = record.versionName
  formData.changelog = record.changelog || ''
  formData.status = record.status ?? 1
  selectedFile.value = null
  resetUploadTask()
  dialogVisible.value = true
}

function beforeUpload(file: File) {
  const isApk = file.name.toLowerCase().endsWith('.apk')
  if (!isApk) {
    message.error(t('system.androidVersion.apkOnly'))
    return false
  }
  selectedFile.value = file
  resetUploadTask()
  return false
}

async function handleSubmit() {
  await formRef.value?.validate?.()
  if (dialogMode.value === 'upload' && !selectedFile.value) {
    message.error(t('system.androidVersion.selectApkFirst'))
    return
  }

  saving.value = true
  cancelRequested.value = false
  try {
    if (dialogMode.value === 'upload' && selectedFile.value) {
      await uploadByChunks(selectedFile.value)
      message.success(t('system.androidVersion.uploadSuccess'))
    } else {
      await updateAndroidVersion(formData)
    }
    dialogVisible.value = false
    tableRef.value?.refresh?.()
  } finally {
    saving.value = false
  }
}

async function uploadByChunks(file: File) {
  uploadTask.statusText = t('system.androidVersion.initializingUpload')
  const totalChunks = Math.ceil(file.size / CHUNK_SIZE)
  const cacheKey = getUploadCacheKey(file)
  let task = await restoreUploadTask(cacheKey, file)
  if (!task) {
    task = await initAndroidVersionUpload({
      fileName: file.name,
      fileSize: file.size,
      chunkSize: CHUNK_SIZE,
      totalChunks,
      fileHash: await calculateFileHash(file),
    })
    localStorage.setItem(cacheKey, task.uploadId)
  }

  let status = await getAndroidVersionUploadStatus(task.uploadId)
  syncUploadTask(status)

  const uploadedSet = new Set(status.uploadedChunks || [])
  for (let index = 0; index < totalChunks; index++) {
    if (cancelRequested.value) {
      throw new Error(t('system.androidVersion.uploadCancelled'))
    }
    if (uploadedSet.has(index)) {
      continue
    }
    const start = index * CHUNK_SIZE
    const end = Math.min(file.size, start + CHUNK_SIZE)
    const chunk = file.slice(start, end)
    status = await uploadChunkWithRetry(task.uploadId, index, chunk)
    syncUploadTask(status)
  }

  uploadTask.statusText = t('system.androidVersion.mergingPackage')
  await completeAndroidVersionUpload(task.uploadId, formData)
  localStorage.removeItem(cacheKey)
  uploadTask.progress = 100
  uploadTask.statusText = t('system.androidVersion.uploadCompleted')
}

async function uploadChunkWithRetry(uploadId: string, chunkIndex: number, chunk: Blob): Promise<AndroidUploadTask> {
  let lastError: any
  for (let retry = 1; retry <= MAX_RETRY; retry++) {
    try {
      uploadTask.statusText = t('system.androidVersion.uploadingChunk', {
        current: chunkIndex + 1,
        total: uploadTask.totalChunks || '',
      })
      return await uploadAndroidVersionChunk(uploadId, chunkIndex, chunk)
    } catch (error) {
      lastError = error
      if (retry >= MAX_RETRY) {
        break
      }
      uploadTask.statusText = t('system.androidVersion.chunkRetry', {
        current: chunkIndex + 1,
        retry,
        max: MAX_RETRY - 1,
      })
    }
  }
  throw lastError
}

async function handleCancelUpload() {
  cancelRequested.value = true
  if (uploadTask.uploadId) {
    await cancelAndroidVersionUpload(uploadTask.uploadId)
    if (selectedFile.value) {
      localStorage.removeItem(getUploadCacheKey(selectedFile.value))
    }
  }
  uploadTask.statusText = t('system.androidVersion.cancelled')
  saving.value = false
}

function syncUploadTask(task: AndroidUploadTask) {
  uploadTask.uploadId = task.uploadId
  uploadTask.uploadedChunks = task.uploadedCount || task.uploadedChunks?.length || 0
  uploadTask.totalChunks = task.totalChunks || uploadTask.totalChunks
  uploadTask.status = task.status
  uploadTask.progress = task.totalChunks ? Math.floor((uploadTask.uploadedChunks / task.totalChunks) * 100) : 0
  uploadTask.statusText = task.status === 'FAILED'
    ? (task.errorMessage || t('system.androidVersion.uploadFailed'))
    : t('system.androidVersion.uploading')
}

async function calculateFileHash(file: File) {
  if (file.size > HASH_MAX_SIZE) {
    return ''
  }
  if (!window.crypto?.subtle) {
    return ''
  }
  const buffer = await file.arrayBuffer()
  const digest = await window.crypto.subtle.digest('SHA-256', buffer)
  return Array.from(new Uint8Array(digest))
    .map(byte => byte.toString(16).padStart(2, '0'))
    .join('')
}

async function restoreUploadTask(cacheKey: string, file: File) {
  const uploadId = localStorage.getItem(cacheKey)
  if (!uploadId) {
    return null
  }
  try {
    const task = await getAndroidVersionUploadStatus(uploadId)
    if (
      task.fileName === file.name
      && task.fileSize === file.size
      && task.status === 'UPLOADING'
    ) {
      return task
    }
  } catch (error) {
    // stale cache is ignored and a new task will be created
  }
  localStorage.removeItem(cacheKey)
  return null
}

function getUploadCacheKey(file: File) {
  return `${TASK_STORAGE_PREFIX}${file.name}:${file.size}:${file.lastModified}`
}

function handleDelete(id?: number) {
  if (!id) {
    return
  }
  Modal.confirm({
    title: t('system.androidVersion.deleteConfirmTitle'),
    content: t('message.deleteConfirmContent'),
    okText: t('common.confirm'),
    cancelText: t('common.cancel'),
    onOk: async () => {
      await deleteAndroidVersion(id)
      tableRef.value?.refresh?.()
    },
  })
}

async function copyText(text?: string) {
  if (!text) {
    return
  }
  await navigator.clipboard.writeText(normalizeMediaUrl(text))
  message.success(t('system.androidVersion.downloadLinkCopied'))
}

function formatFileSize(size?: number) {
  if (!size || size <= 0) {
    return '-'
  }
  if (size < 1024) {
    return `${size} B`
  }
  if (size < 1024 * 1024) {
    return `${(size / 1024).toFixed(2)} KB`
  }
  if (size < 1024 * 1024 * 1024) {
    return `${(size / 1024 / 1024).toFixed(2)} MB`
  }
  return `${(size / 1024 / 1024 / 1024).toFixed(2)} GB`
}
</script>

<style scoped lang="less">
.android-version-page {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
}

.file-hint {
  margin-top: 8px;
  color: #666;
  font-size: 12px;
}

.upload-progress {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.upload-progress__meta {
  display: flex;
  justify-content: space-between;
  color: #666;
  font-size: 12px;
}
</style>
