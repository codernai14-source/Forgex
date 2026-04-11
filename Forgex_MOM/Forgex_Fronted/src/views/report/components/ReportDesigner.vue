<template>
  <a-modal
    v-model:open="visible"
    :title="designerTitle"
    width="90%"
    :height="'85%'"
    :footer="null"
    :destroy-on-close="false"
    @ok="handleOk"
    @cancel="handleCancel"
  >
    <div class="designer-container">
      <a-alert
        v-if="showTips"
        message="提示"
        description="在报表设计器中完成设计后，请点击保存按钮。关闭此窗口将自动刷新列表。"
        type="info"
        show-icon
        closable
        style="margin-bottom: 12px"
        @close="showTips = false"
      />
      
      <div class="iframe-wrapper">
        <iframe
          ref="iframeRef"
          :src="designerUrl"
          class="designer-iframe"
          frameborder="0"
          @load="handleIframeLoad"
        />
      </div>
    </div>
  </a-modal>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { getDesignerUrl } from '@/api/report'

interface Props {
  open: boolean
  reportCode?: string
  engineType?: 'UREPORT' | 'JIMU'
}

interface Emits {
  (e: 'update:open', value: boolean): void
  (e: 'ok'): void
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

const designerTitle = computed(() => {
  const engineName = props.engineType === 'UREPORT' ? 'UReport2' : 'JimuReport'
  return `${engineName} 报表设计器`
})

const designerUrl = computed(() => {
  if (!props.reportCode) {
    return ''
  }
  return getDesignerUrl(props.reportCode, props.engineType)
})

function handleIframeLoad() {
  iframeLoaded.value = true
  console.log('报表设计器加载完成')
}

function handleOk() {
  emit('ok')
}

function handleCancel() {
  emit('ok') // 关闭时也触发 ok 事件，刷新列表
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
.designer-container {
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
    
    .designer-iframe {
      width: 100%;
      height: 100%;
      border: none;
    }
  }
}
</style>
