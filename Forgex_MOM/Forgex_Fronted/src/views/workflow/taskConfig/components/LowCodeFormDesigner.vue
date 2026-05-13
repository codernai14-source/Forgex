<template>
  <div
    class="low-code-form-designer"
    :class="{ 'low-code-form-designer--dark': isDarkTheme }"
  >
    <header class="designer-header">
      <div>
        <h3>{{ t('workflow.taskConfig.lowCodeDesigner.paletteTitle') }}</h3>
        <p>{{ headerDescription }}</p>
      </div>
      <a-space>
        <a-button @click="handlePreview">
          {{ t('workflow.taskConfig.lowCodeDesigner.preview') }}
        </a-button>
        <a-button danger @click="handleClear">
          {{ t('workflow.taskConfig.lowCodeDesigner.clear') }}
        </a-button>
      </a-space>
    </header>

    <div class="designer-shell">
      <fc-designer
        ref="designerRef"
        class="designer-body"
        :config="designerConfig"
        @change-field="handleDesignerChange"
        @active="handleDesignerChange"
        @create="handleDesignerChange"
        @copy="handleDesignerChange"
        @delete="handleDesignerChange"
        @drag="handleDesignerChange"
        @sort-up="handleDesignerChange"
        @sort-down="handleDesignerChange"
        @paste-rule="handleDesignerChange"
        @clear="handleDesignerChange"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import type { Config } from '@form-create/antd-designer'
import type { Rule, Options } from '@form-create/ant-design-vue'
import { Modal } from 'ant-design-vue'
import { useI18n } from 'vue-i18n'
import {
  buildFormCreatePayload,
  normalizeLowCodeFormSchema,
  stringifyLowCodeFormSchema,
  type LowCodeFormSchema,
} from './lowCodeSchema'

interface Props {
  modelValue?: string
}

interface Emits {
  (e: 'update:modelValue', value: string): void
  (e: 'schema-change', value: LowCodeFormSchema): void
}

interface DesignerExpose {
  getRule: () => Rule[]
  getOption: () => Options
  setRule: (rule: Rule[] | string) => void
  setOptions: (option: Options | string) => void
  openPreview: () => void
  clearDragRule: () => void
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: '',
})

const emit = defineEmits<Emits>()
const { t } = useI18n({ useScope: 'global' })

const designerRef = ref<DesignerExpose>()
const syncingFromOutside = ref(false)
const initialized = ref(false)
const lastSerialized = ref('')
const isDarkTheme = ref(false)
let themeObserver: MutationObserver | null = null

const headerDescription = computed(() => t('workflow.taskConfig.lowCodeDesigner.headerDescription'))
const designerConfig = computed<Config>(() => ({
  showEventForm: true,
  showStyleForm: true,
  showFormConfig: true,
  showDevice: true,
  showPreviewBtn: true,
  showSaveBtn: false,
  showJsonPreview: true,
  showCustomProps: true,
  showInputData: true,
  showMenuBar: true,
  showAi: false,
  configFormOrder: ['base', 'props', 'style', 'event', 'validate'],
  formOptions: {
    form: {
      layout: 'vertical',
      labelAlign: 'right',
      size: 'middle',
      colon: false,
      labelCol: {
        style: {
          width: '120px',
        },
      },
      wrapperCol: {
        span: 24,
      },
    },
    row: {
      gutter: 16,
    },
    submitBtn: false,
    resetBtn: false,
  },
}))

watch(
  () => props.modelValue,
  async value => {
    const normalized = normalizeLowCodeFormSchema(value)
    const serialized = stringifyLowCodeFormSchema(normalized)

    if (serialized === lastSerialized.value && initialized.value) {
      return
    }

    syncingFromOutside.value = true
    await nextTick()
    designerRef.value?.setRule(normalized.rule || [])
    designerRef.value?.setOptions(normalized.option || {})
    lastSerialized.value = stringifyLowCodeFormSchema(normalized)
    initialized.value = true
    syncingFromOutside.value = false
  },
  { immediate: true }
)

function syncThemeMode() {
  if (typeof document === 'undefined') {
    return
  }
  isDarkTheme.value = document.documentElement.getAttribute('data-theme') === 'dark'
}

onMounted(() => {
  syncThemeMode()

  if (typeof document === 'undefined' || typeof MutationObserver === 'undefined') {
    return
  }

  themeObserver = new MutationObserver(() => {
    syncThemeMode()
  })

  themeObserver.observe(document.documentElement, {
    attributes: true,
    attributeFilter: ['data-theme'],
  })
})

onBeforeUnmount(() => {
  themeObserver?.disconnect()
  themeObserver = null
})

function handleDesignerChange() {
  if (syncingFromOutside.value || !designerRef.value) {
    return
  }

  const rule = designerRef.value.getRule?.() || []
  const option = designerRef.value.getOption?.() || {}
  const schema = normalizeLowCodeFormSchema(buildFormCreatePayload(rule, option))
  const serialized = stringifyLowCodeFormSchema(schema)

  if (serialized === lastSerialized.value) {
    return
  }

  lastSerialized.value = serialized
  emit('update:modelValue', serialized)
  emit('schema-change', schema)
}

function handlePreview() {
  designerRef.value?.openPreview?.()
}

function handleClear() {
  Modal.confirm({
    title: t('common.tip'),
    content: t('workflow.taskConfig.lowCodeDesigner.clearConfirm'),
    okText: t('common.confirm'),
    cancelText: t('common.cancel'),
    onOk() {
      designerRef.value?.clearDragRule?.()
      handleDesignerChange()
    },
  })
}
</script>

<style scoped lang="less" src="@/styles/views/workflow/taskConfig/components/low-code-form-designer.less"></style>


