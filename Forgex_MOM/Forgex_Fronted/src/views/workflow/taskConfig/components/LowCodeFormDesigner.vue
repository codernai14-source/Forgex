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

const headerDescription = computed(() =>
  'FormCreate Ant Design Vue 设计器，支持组件尺寸、事件、布局、定位和更多官方组件。'
)

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

<style scoped>
.low-code-form-designer {
  --lcd-shell-bg: #f3f4f6;
  --lcd-panel-bg: #ffffff;
  --lcd-panel-muted-bg: #f8fafc;
  --lcd-canvas-bg: #eef2f7;
  --lcd-stage-bg: #ffffff;
  --lcd-control-bg: #ffffff;
  --lcd-control-hover-bg: #f8fafc;
  --lcd-border: #e2e8f0;
  --lcd-border-strong: #cbd5e1;
  --lcd-text-primary: #111827;
  --lcd-text-secondary: #6b7280;
  --lcd-text-muted: #94a3b8;
  --lcd-accent: var(--fx-primary, #1677ff);
  --lcd-accent-soft: rgba(22, 119, 255, 0.12);
  --lcd-accent-strong: rgba(22, 119, 255, 0.2);
  --lcd-shadow: 0 18px 40px rgba(15, 23, 42, 0.08);
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-height: 760px;
}

.low-code-form-designer.low-code-form-designer--dark {
  --lcd-shell-bg: #11161d;
  --lcd-panel-bg: #1b2129;
  --lcd-panel-muted-bg: #232a34;
  --lcd-canvas-bg: #0f141b;
  --lcd-stage-bg: #181e26;
  --lcd-control-bg: #232a34;
  --lcd-control-hover-bg: #2a3340;
  --lcd-border: #2d3748;
  --lcd-border-strong: #3a4758;
  --lcd-text-primary: #e5edf7;
  --lcd-text-secondary: #9aa8ba;
  --lcd-text-muted: #64748b;
  --lcd-accent-soft: rgba(22, 119, 255, 0.18);
  --lcd-accent-strong: rgba(22, 119, 255, 0.28);
  --lcd-shadow: 0 20px 48px rgba(0, 0, 0, 0.38);
}

.designer-header,
.designer-shell {
  border: 1px solid var(--lcd-border);
  border-radius: 18px;
  background: var(--lcd-panel-bg);
  box-shadow: var(--lcd-shadow);
}

.designer-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 18px 20px;
}

.designer-header h3 {
  margin: 0 0 6px;
  color: var(--lcd-text-primary);
  font-size: 18px;
}

.designer-header p {
  margin: 0;
  color: var(--lcd-text-secondary);
  font-size: 13px;
}

.designer-shell {
  min-height: 700px;
  overflow: hidden;
}

.designer-body {
  min-height: 700px;
}

.designer-shell :deep(._fc-designer) {
  min-height: 700px;
  background: var(--lcd-shell-bg) !important;
  color: var(--lcd-text-primary);
}

.designer-shell :deep(.ant-layout),
.designer-shell :deep(.ant-layout-sider),
.designer-shell :deep(.ant-layout-header),
.designer-shell :deep(.ant-layout-content) {
  background: transparent !important;
}

.designer-shell :deep(._fc-l),
.designer-shell :deep(._fc-r),
.designer-shell :deep(._fc-l-menu),
.designer-shell :deep(._fc-l-tabs),
.designer-shell :deep(._fc-r-tabs),
.designer-shell :deep(._fc-r-config),
.designer-shell :deep(._fc-r .ant-layout-content) {
  background: var(--lcd-panel-bg) !important;
}

.designer-shell :deep(._fc-l-menu),
.designer-shell :deep(._fc-m-tools),
.designer-shell :deep(._fc-m ._fc-m-con) {
  background: var(--lcd-panel-muted-bg) !important;
}

.designer-shell :deep(._fc-m),
.designer-shell :deep(.ant-layout-content ._fc-m) {
  background: var(--lcd-canvas-bg) !important;
}

.designer-shell :deep(._fc-m-drag),
.designer-shell :deep(.draggable-drag),
.designer-shell :deep(._fd-draggable-drag.drag-holder),
.designer-shell :deep(._fc-m-input-handle) {
  background: var(--lcd-stage-bg) !important;
}

.designer-shell :deep(._fc-l),
.designer-shell :deep(._fc-r),
.designer-shell :deep(._fc-l-menu),
.designer-shell :deep(._fc-l-tabs),
.designer-shell :deep(._fc-r-tabs),
.designer-shell :deep(._fc-m-tools),
.designer-shell :deep(._fc-r-title),
.designer-shell :deep(._fd-config-item),
.designer-shell :deep(.ant-tabs-nav) {
  border-color: var(--lcd-border) !important;
}

.designer-shell :deep(._fc-l-menu-item),
.designer-shell :deep(._fc-l-tab),
.designer-shell :deep(._fc-r-tab),
.designer-shell :deep(._fc-l-label),
.designer-shell :deep(._fc-l-info),
.designer-shell :deep(._fc-l-title),
.designer-shell :deep(._fc-l-title i),
.designer-shell :deep(._fc-l-item),
.designer-shell :deep(._fc-l-item ._fc-l-name),
.designer-shell :deep(._fc-l-item ._fc-l-icon),
.designer-shell :deep(._fc-l-item i),
.designer-shell :deep(._fc-r-title),
.designer-shell :deep(._fc-tree-node),
.designer-shell :deep(.fc-configured),
.designer-shell :deep(.ant-form-item-label > label),
.designer-shell :deep(.ant-tabs-tab),
.designer-shell :deep(.ant-btn),
.designer-shell :deep(.ant-typography),
.designer-shell :deep(.fc-icon) {
  color: var(--lcd-text-primary) !important;
}

.designer-shell :deep(.icon-ai.bright) {
  background: none !important;
  -webkit-text-fill-color: currentColor !important;
}

.designer-shell :deep(._fc-l-info),
.designer-shell :deep(.fc-configured),
.designer-shell :deep(.ant-form-item-explain),
.designer-shell :deep(.ant-form-item-extra),
.designer-shell :deep(.ant-select-arrow),
.designer-shell :deep(.ant-input::placeholder),
.designer-shell :deep(textarea.ant-input::placeholder) {
  color: var(--lcd-text-secondary) !important;
}

.designer-shell :deep(.ant-menu-item-selected),
.designer-shell :deep(._fc-l-tab.active),
.designer-shell :deep(._fc-r-tab.active),
.designer-shell :deep(._fc-l-menu-item.active),
.designer-shell :deep(._fc-tree-node.active),
.designer-shell :deep(._fd-menu-item.is-active) {
  background: var(--lcd-accent-soft) !important;
  color: var(--lcd-accent) !important;
}

.designer-shell :deep(._fc-l ._fc-l-tab.active),
.designer-shell :deep(._fc-r ._fc-r-tab.active) {
  border-bottom-color: var(--lcd-accent) !important;
}

.designer-shell :deep(._fc-l-group),
.designer-shell :deep(._fc-l-item),
.designer-shell :deep(._fd-menu-item),
.designer-shell :deep(._fc-r ._fc-group-container),
.designer-shell :deep(._fd-config-item > ._fd-ci-head) {
  background: var(--lcd-panel-bg) !important;
  border-color: var(--lcd-border) !important;
}

.designer-shell :deep(._fc-l-item:hover),
.designer-shell :deep(._fd-menu-item:hover),
.designer-shell :deep(._fc-tree-node:hover),
.designer-shell :deep(._fc-m-tools ._fd-m-extend:hover) {
  background: var(--lcd-control-hover-bg) !important;
  color: var(--lcd-text-primary) !important;
}

.designer-shell :deep(._fc-l-item:hover),
.designer-shell :deep(._fc-l-item:hover ._fc-l-name),
.designer-shell :deep(._fc-l-item:hover ._fc-l-icon),
.designer-shell :deep(._fc-l-item:hover i),
.designer-shell :deep(._fd-menu-item.is-active),
.designer-shell :deep(._fc-manage-text),
.designer-shell :deep(._fc-l-menu-item.active i),
.designer-shell :deep(._fc-tree-node.active .icon-more),
.designer-shell :deep(._fc-m-tools-l .devices .fc-icon.active) {
  color: var(--lcd-accent) !important;
}

.designer-shell :deep(._fc-l-group ._fc-l-list) {
  background: transparent !important;
}

.designer-shell :deep(._fc-l-item),
.designer-shell :deep(._fc-l-item ._fc-l-name),
.designer-shell :deep(._fc-l-item ._fc-l-icon),
.designer-shell :deep(._fc-l-item i) {
  opacity: 1 !important;
  -webkit-text-fill-color: currentColor !important;
}

.designer-shell :deep(._fc-m .form-create ._fc-l-item),
.designer-shell :deep(._fc-m .form-create ._fc-field-node),
.designer-shell :deep(._fc-child-empty),
.designer-shell :deep(._fd-aTooltip-drag.drag-holder),
.designer-shell :deep(._fd-fcInlineForm-drag.drag-holder),
.designer-shell :deep(._fd-fcDialog-drag.drag-holder),
.designer-shell :deep(._fd-fcDrawer-drag.drag-holder),
.designer-shell :deep(._fd-tableFormColumn-drag.drag-holder),
.designer-shell :deep(._fd-aTabPane-drag.drag-holder),
.designer-shell :deep(._fd-group-drag.drag-holder),
.designer-shell :deep(._fd-subForm-drag.drag-holder),
.designer-shell :deep(._fd-stepFormItem-drag.drag-holder),
.designer-shell :deep(._fd-aCard-drag.drag-holder),
.designer-shell :deep(._fd-aCollapsePanel-drag.drag-holder) {
  background: var(--lcd-control-bg) !important;
  border-color: var(--lcd-border-strong) !important;
  color: var(--lcd-text-primary) !important;
}

.designer-shell :deep(._fc-child-empty:after),
.designer-shell :deep(._fd-aTooltip-drag.drag-holder:after),
.designer-shell :deep(._fd-fcInlineForm-drag.drag-holder:after),
.designer-shell :deep(._fd-fcDialog-drag.drag-holder:after),
.designer-shell :deep(._fd-fcDrawer-drag.drag-holder:after),
.designer-shell :deep(._fd-tableFormColumn-drag.drag-holder:after),
.designer-shell :deep(._fd-aTabPane-drag.drag-holder:after),
.designer-shell :deep(._fd-group-drag.drag-holder:after),
.designer-shell :deep(._fd-subForm-drag.drag-holder:after),
.designer-shell :deep(._fd-stepFormItem-drag.drag-holder:after),
.designer-shell :deep(._fd-aCard-drag.drag-holder:after),
.designer-shell :deep(._fd-aCollapsePanel-drag.drag-holder:after) {
  color: var(--lcd-text-muted) !important;
}

.designer-shell :deep(.ant-btn-primary) {
  background: var(--lcd-accent) !important;
  border-color: var(--lcd-accent) !important;
}

.designer-shell :deep(.ant-btn-default),
.designer-shell :deep(.ant-btn),
.designer-shell :deep(._fd-btn-primary),
.designer-shell :deep(._fd-btn-success),
.designer-shell :deep(._fd-btn-danger),
.designer-shell :deep(._fc-m-tools ._fd-m-extend) {
  background: var(--lcd-control-bg) !important;
  border-color: var(--lcd-border) !important;
  color: var(--lcd-text-primary) !important;
  box-shadow: none !important;
}

.designer-shell :deep(.ant-input),
.designer-shell :deep(.ant-input-affix-wrapper),
.designer-shell :deep(.ant-input-number),
.designer-shell :deep(.ant-input-number-input),
.designer-shell :deep(.ant-input-group-addon),
.designer-shell :deep(.ant-select-selector),
.designer-shell :deep(.ant-picker),
.designer-shell :deep(.ant-radio-button-wrapper),
.designer-shell :deep(.ant-checkbox-wrapper),
.designer-shell :deep(.ant-input-textarea),
.designer-shell :deep(.ant-mentions),
.designer-shell :deep(.CodeMirror-scroll),
.designer-shell :deep(.CodeMirror-gutters) {
  background: var(--lcd-control-bg) !important;
  border-color: var(--lcd-border) !important;
  color: var(--lcd-text-primary) !important;
  box-shadow: none !important;
}

.designer-shell :deep(.ant-input-affix-wrapper:hover),
.designer-shell :deep(.ant-input:hover),
.designer-shell :deep(.ant-input-number:hover),
.designer-shell :deep(.ant-picker:hover),
.designer-shell :deep(.ant-select-selector:hover),
.designer-shell :deep(.ant-radio-button-wrapper:hover),
.designer-shell :deep(.ant-btn:hover) {
  border-color: var(--lcd-accent) !important;
}

.designer-shell :deep(.ant-input-affix-wrapper-focused),
.designer-shell :deep(.ant-input:focus),
.designer-shell :deep(.ant-input-focused),
.designer-shell :deep(.ant-input-number-focused),
.designer-shell :deep(.ant-picker-focused),
.designer-shell :deep(.ant-select-focused .ant-select-selector),
.designer-shell :deep(.ant-radio-button-wrapper-checked) {
  border-color: var(--lcd-accent) !important;
  box-shadow: 0 0 0 2px var(--lcd-accent-soft) !important;
}

.designer-shell :deep(.ant-radio-button-wrapper-checked:not(.ant-radio-button-wrapper-disabled)) {
  background: var(--lcd-accent-soft) !important;
  color: var(--lcd-accent) !important;
}

.designer-shell :deep(._fc-m-tools .line),
.designer-shell :deep(._fd-row-line),
.designer-shell :deep(.ant-divider),
.designer-shell :deep(.ant-tabs-ink-bar) {
  background: var(--lcd-border) !important;
}

.designer-shell :deep(.ant-tabs-ink-bar) {
  background: var(--lcd-accent) !important;
}

.designer-shell :deep(._fc-l-close),
.designer-shell :deep(._fc-r-close),
.designer-shell :deep(._fc-l-open),
.designer-shell :deep(._fc-r-open) {
  background: var(--lcd-panel-bg) !important;
  border: 1px solid var(--lcd-border) !important;
  color: var(--lcd-text-secondary) !important;
}

.designer-shell :deep(.ant-empty-description),
.designer-shell :deep(.ant-select-selection-placeholder) {
  color: var(--lcd-text-muted) !important;
}

@media (max-width: 960px) {
  .designer-header {
    flex-direction: column;
    align-items: flex-start;
  }
}

:global(._fd-preview-dialog .ant-modal-content),
:global(._fd-config-dialog .ant-modal-content) {
  background: #ffffff !important;
  color: #111827 !important;
}

:global(._fd-preview-dialog .ant-modal-header),
:global(._fd-config-dialog .ant-modal-header) {
  background: #ffffff !important;
  border-bottom: 1px solid #e2e8f0 !important;
}

:global(html[data-theme='dark'] ._fd-preview-dialog .ant-modal-content),
:global(html[data-theme='dark'] ._fd-config-dialog .ant-modal-content) {
  background: #1b2129 !important;
  color: #e5edf7 !important;
}

:global(html[data-theme='dark'] ._fd-preview-dialog .ant-modal-header),
:global(html[data-theme='dark'] ._fd-config-dialog .ant-modal-header) {
  background: #1b2129 !important;
  border-bottom: 1px solid #2d3748 !important;
}
</style>
