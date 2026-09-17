<template>
  <div class="fx-file-preview">
    <div v-if="loading" class="fx-file-preview__state">
      <a-spin />
      <span>{{ t('layout.help.previewLoading') }}</span>
    </div>

    <div v-else-if="errorMessage" class="fx-file-preview__state">
      <p>{{ errorMessage }}</p>
      <a-space>
        <a-button v-if="normalizedUrl" type="link" @click="openExternal">{{ t('layout.help.openNewWindow') }}</a-button>
        <a-button v-if="normalizedUrl" type="link" @click="downloadFile">{{ t('layout.help.download') }}</a-button>
      </a-space>
    </div>

    <iframe
      v-else-if="previewMode === 'pdf'"
      class="fx-file-preview__frame"
      :src="previewSrc"
      title="pdf-preview"
      @error="fallbackPdfToOffice"
    />

    <component
      :is="officeComponent"
      v-else-if="officeComponent && previewSrc"
      :key="`${previewMode}-${previewSrc}`"
      class="fx-file-preview__office"
      :src="previewSrc"
    />

    <pre v-else-if="previewMode === 'text'" class="fx-file-preview__text">{{ textContent }}</pre>

    <div v-else-if="previewMode === 'markdown'" class="fx-file-preview__markdown" v-html="markdownHtml" />

    <video
      v-else-if="previewMode === 'video'"
      class="fx-file-preview__video"
      :src="previewSrc"
      controls
    />

    <div v-else class="fx-file-preview__state">
      <p>{{ fallbackHint }}</p>
      <a-space>
        <a-button v-if="normalizedUrl" type="link" @click="openExternal">{{ t('layout.help.openNewWindow') }}</a-button>
        <a-button v-if="normalizedUrl" type="link" @click="downloadFile">{{ t('layout.help.download') }}</a-button>
      </a-space>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, defineAsyncComponent, onUnmounted, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { normalizeMediaUrl } from '@/utils/media'

/**
 * 通用文件预览组件。
 * <p>
 * 同源文件先拉成 Blob 再交给 iframe / vue-office / video，避免直接把带鉴权路径丢给 Office 组件。
 * PDF 失败后再懒加载 {@code @vue-office/pdf}。
 * </p>
 *
 * @author Forgex Team
 * @version 1.1.0
 * @see normalizeMediaUrl
 */
const props = defineProps<{
  fileUrl?: string
  fileExt?: string
  fileName?: string
  sourceType?: string
  externalUrl?: string
}>()

const { t } = useI18n({ useScope: 'global' })
const loading = ref(false)
const errorMessage = ref('')
const textContent = ref('')
const markdownHtml = ref('')
const blobUrl = ref('')
const previewMode = ref<'pdf' | 'office-docx' | 'office-excel' | 'office-pdf' | 'text' | 'markdown' | 'video' | 'fallback'>('fallback')

const VueOfficeDocx = defineAsyncComponent(async () => {
  await import('@vue-office/docx/lib/index.css')
  return (await import('@vue-office/docx')).default
})
const VueOfficeExcel = defineAsyncComponent(async () => {
  await import('@vue-office/excel/lib/index.css')
  return (await import('@vue-office/excel')).default
})
const VueOfficePdf = defineAsyncComponent(async () => {
  return (await import('@vue-office/pdf')).default
})

const ext = computed(() => String(props.fileExt || props.fileName?.split('.').pop() || '').replace('.', '').toLowerCase())
const rawUrl = computed(() => {
  if (props.sourceType === 'EXTERNAL_URL') {
    return String(props.externalUrl || props.fileUrl || '')
  }
  return String(props.fileUrl || props.externalUrl || '')
})
const normalizedUrl = computed(() => normalizeMediaUrl(rawUrl.value) || rawUrl.value)
const previewSrc = computed(() => blobUrl.value || normalizedUrl.value)

const officeComponent = computed(() => {
  if (previewMode.value === 'office-docx') {
    return VueOfficeDocx
  }
  if (previewMode.value === 'office-excel') {
    return VueOfficeExcel
  }
  if (previewMode.value === 'office-pdf') {
    return VueOfficePdf
  }
  return null
})

const fallbackHint = computed(() => {
  if (ext.value === 'doc') {
    return t('layout.help.docNotPreviewable')
  }
  return t('layout.help.previewFailed')
})

watch(
  () => [normalizedUrl.value, ext.value, props.sourceType],
  () => {
    void resolvePreview()
  },
  { immediate: true }
)

onUnmounted(() => {
  revokeBlobUrl()
})

/**
 * 释放上一次预览占用的 Blob 地址。
 */
function revokeBlobUrl() {
  if (blobUrl.value) {
    URL.revokeObjectURL(blobUrl.value)
    blobUrl.value = ''
  }
}

/**
 * 按扩展名分流预览，优先把同源文件转成 Blob。
 */
async function resolvePreview() {
  errorMessage.value = ''
  textContent.value = ''
  markdownHtml.value = ''
  revokeBlobUrl()
  if (!normalizedUrl.value) {
    previewMode.value = 'fallback'
    return
  }
  if (props.sourceType === 'EXTERNAL_URL' && ext.value !== 'mp4' && !normalizedUrl.value.toLowerCase().endsWith('.mp4')) {
    previewMode.value = 'fallback'
    return
  }
  if (ext.value === 'doc') {
    previewMode.value = 'fallback'
    return
  }
  if (ext.value === 'txt') {
    await loadText()
    return
  }
  if (ext.value === 'md') {
    await loadMarkdown()
    return
  }
  if (ext.value === 'pdf') {
    await loadBinaryPreview('pdf')
    return
  }
  if (ext.value === 'docx') {
    await loadBinaryPreview('office-docx')
    return
  }
  if (ext.value === 'xls' || ext.value === 'xlsx') {
    await loadBinaryPreview('office-excel')
    return
  }
  if (ext.value === 'mp4') {
    await loadBinaryPreview('video')
    return
  }
  previewMode.value = 'fallback'
}

/**
 * 带凭证拉取二进制文件并生成 Blob 地址。
 *
 * @param mode 预览模式
 */
async function loadBinaryPreview(mode: 'pdf' | 'office-docx' | 'office-excel' | 'video') {
  loading.value = true
  try {
    const response = await fetch(normalizedUrl.value, { credentials: 'include' })
    if (!response.ok) {
      throw new Error('binary fetch failed')
    }
    const blob = await response.blob()
    if (!blob || blob.size === 0) {
      throw new Error('empty blob')
    }
    blobUrl.value = URL.createObjectURL(blob)
    previewMode.value = mode
  } catch {
    if (mode === 'pdf' || mode === 'video') {
      previewMode.value = mode
      return
    }
    errorMessage.value = t('layout.help.previewFailed')
    previewMode.value = 'fallback'
  } finally {
    loading.value = false
  }
}

/**
 * 加载纯文本。
 */
async function loadText() {
  loading.value = true
  try {
    const response = await fetch(normalizedUrl.value, { credentials: 'include' })
    if (!response.ok) {
      throw new Error('text fetch failed')
    }
    textContent.value = await response.text()
    previewMode.value = 'text'
  } catch {
    errorMessage.value = t('layout.help.previewFailed')
    previewMode.value = 'fallback'
  } finally {
    loading.value = false
  }
}

/**
 * 加载 Markdown 并消毒。
 */
async function loadMarkdown() {
  loading.value = true
  try {
    const response = await fetch(normalizedUrl.value, { credentials: 'include' })
    if (!response.ok) {
      throw new Error('markdown fetch failed')
    }
    const source = await response.text()
    const [{ marked }, DOMPurify] = await Promise.all([
      import('marked'),
      import('dompurify'),
    ])
    const rendered = await marked.parse(source)
    markdownHtml.value = DOMPurify.default.sanitize(String(rendered))
    previewMode.value = 'markdown'
  } catch {
    errorMessage.value = t('layout.help.previewFailed')
    previewMode.value = 'fallback'
  } finally {
    loading.value = false
  }
}

/**
 * 原生 PDF iframe 失败时回退到 vue-office。
 */
function fallbackPdfToOffice() {
  previewMode.value = 'office-pdf'
}

function openExternal() {
  if (normalizedUrl.value) {
    window.open(normalizedUrl.value, '_blank', 'noopener,noreferrer')
  }
}

function downloadFile() {
  if (!normalizedUrl.value) {
    return
  }
  const link = document.createElement('a')
  link.href = normalizedUrl.value
  link.download = props.fileName || `help.${ext.value || 'file'}`
  link.target = '_blank'
  link.rel = 'noopener noreferrer'
  link.click()
}
</script>

<style scoped lang="less">
.fx-file-preview {
  min-height: 560px;
  height: 100%;
}

.fx-file-preview__state,
.fx-file-preview__text,
.fx-file-preview__markdown,
.fx-file-preview__frame,
.fx-file-preview__office,
.fx-file-preview__video {
  width: 100%;
  min-height: 560px;
}

.fx-file-preview__state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: var(--ant-color-text-secondary);
}

.fx-file-preview__text,
.fx-file-preview__markdown {
  margin: 0;
  padding: 16px;
  overflow: auto;
  background: var(--ant-color-fill-quaternary);
  border-radius: 8px;
}

.fx-file-preview__frame,
.fx-file-preview__video {
  border: 0;
  border-radius: 8px;
  background: #111;
}

.fx-file-preview__office {
  overflow: auto;
  background: #fff;
  border-radius: 8px;
}
</style>
