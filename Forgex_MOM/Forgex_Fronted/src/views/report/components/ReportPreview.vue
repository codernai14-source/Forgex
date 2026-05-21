<template>
  <a-modal
    v-model:open="visible"
    :title="previewTitle"
    width="90%"
    :height="'85%'"
    :footer="null"
    :destroy-on-close="true"
    @cancel="handleCancel"
  >
    <div class="preview-container">
      <a-alert
        v-if="showTips"
        :message="t('common.tip')"
        :description="t('report.preview.tip')"
        type="info"
        show-icon
        closable
        style="margin-bottom: 12px"
        @close="showTips = false"
      />
      
      <div class="iframe-wrapper">
        <iframe
          ref="iframeRef"
          :src="previewUrl"
          class="preview-iframe"
          frameborder="0"
          @load="handleIframeLoad"
        />
      </div>
    </div>
  </a-modal>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { getPreviewUrl } from '@/api/report'

interface Props {
  open: boolean
  reportCode?: string
  engineType?: 'UREPORT' | 'JIMU'
}

interface Emits {
  (e: 'update:open', value: boolean): void
}

const props = withDefaults(defineProps<Props>(), {
  reportCode: '',
  engineType: 'UREPORT',
})

const emit = defineEmits<Emits>()
const { t } = useI18n()

const visible = computed({
  get: () => props.open,
  set: (value) => emit('update:open', value),
})

const iframeRef = ref<HTMLIFrameElement>()
const showTips = ref(true)
const iframeLoaded = ref(false)

const previewTitle = computed(() => {
  const engineName = props.engineType === 'UREPORT' ? 'UReport2' : 'JimuReport'
  return t('report.preview.title', { engineName })
})

const previewUrl = computed(() => {
  if (!props.reportCode) {
    return ''
  }
  return getPreviewUrl(props.reportCode, props.engineType)
})

function handleIframeLoad() {
  iframeLoaded.value = true
  console.log('[ReportPreview] iframe loaded')
}

function handleCancel() {
  emit('update:open', false)
}

watch(
  () => props.open,
  (newVal) => {
    if (newVal) {
      showTips.value = true
      iframeLoaded.value = false
    }
  }
)
</script>

<style scoped lang="less" src="@/styles/views/report/components/report-preview.less"></style>
