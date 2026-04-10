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
        message="提示"
        description="以下为报表预览效果，可通过 URL 参数传递查询条件。"
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

const visible = computed({
  get: () => props.open,
  set: (value) => emit('update:open', value),
})

const iframeRef = ref<HTMLIFrameElement>()
const showTips = ref(true)
const iframeLoaded = ref(false)

const previewTitle = computed(() => {
  const engineName = props.engineType === 'UREPORT' ? 'UReport2' : 'JimuReport'
  return `${engineName} 报表预览`
})

const previewUrl = computed(() => {
  if (!props.reportCode) {
    return ''
  }
  return getPreviewUrl(props.reportCode, props.engineType)
})

function handleIframeLoad() {
  iframeLoaded.value = true
  console.log('报表预览加载完成')
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

<style scoped lang="less">
.preview-container {
  display: flex;
  flex-direction: column;
  height: calc(85vh - 120px);
  min-height: 0;
  
  .iframe-wrapper {
    flex: 1;
    min-height: 0;
    overflow: hidden;
    border: 1px solid #d9d9d9;
    border-radius: 2px;
    
    .preview-iframe {
      width: 100%;
      height: 100%;
      border: none;
    }
  }
}
</style>
