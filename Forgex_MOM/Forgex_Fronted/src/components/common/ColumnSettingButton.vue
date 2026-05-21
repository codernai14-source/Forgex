<template>
  <a-popover
    v-model:open="dropdownOpen"
    trigger="click"
    placement="bottomRight"
    :arrow="false"
    overlay-class-name="fx-column-setting-popover"
    :overlay-inner-style="popoverOverlayInnerStyle"
    :get-popup-container="getColumnSettingPopupContainer"
    :destroy-tooltip-on-hide="false"
  >
    <template #content>
      <div class="column-setting-panel" :style="panelSurfaceStyle">
        <div class="column-setting-header">
          <span class="title">{{ t('system.tableConfig.columnSetting.title') }}</span>
          <a-space>
            <a-button type="link" size="small" @click="handleReset">
              {{ t('system.tableConfig.columnSetting.reset') }}
            </a-button>
            <a-button type="primary" size="small" :loading="saving" @click="handleSave">
              {{ t('common.save') }}
            </a-button>
          </a-space>
        </div>

        <div class="column-setting-body">
          <VueDraggableNext
            v-model="localColumns"
            item-key="field"
            class="column-list"
            handle=".column-drag-handle"
            @end="normalizeLocalOrder"
          >
            <div v-for="col in localColumns" :key="col.field" class="column-item">
              <div class="column-drag-handle" :title="t('system.tableConfig.columnSetting.dragSort')">
                <MenuOutlined />
              </div>
              <div class="column-item-left">
                <a-checkbox v-model:checked="col.visible" :disabled="col.field === ACTION_FIELD">
                  {{ col.title || col.field }}
                </a-checkbox>
              </div>
              <div class="column-width-control">
                <a-input-number
                  v-model:value="col.width"
                  size="small"
                  :min="MIN_COLUMN_WIDTH"
                  :max="MAX_COLUMN_WIDTH"
                  :precision="0"
                  :disabled="col.field === ACTION_FIELD"
                  :placeholder="t('system.tableConfig.width')"
                  @change="value => handleWidthInput(col, value)"
                />
              </div>
            </div>
          </VueDraggableNext>
        </div>

        <div class="column-setting-footer">
          <span class="hint">{{ t('system.tableConfig.columnSetting.hint') }}</span>
        </div>
      </div>
    </template>

    <a-button type="primary" html-type="button" class="column-setting-btn">
      <template #icon>
        <SettingOutlined />
      </template>
      {{ t('system.tableConfig.columnSetting.title') }}
    </a-button>
  </a-popover>
</template>

<script setup lang="ts">
import { nextTick, ref, watch } from 'vue'
import type { CSSProperties } from 'vue'
import { useI18n } from 'vue-i18n'
import { MenuOutlined, SettingOutlined } from '@ant-design/icons-vue'
import { VueDraggableNext } from 'vue-draggable-next'
import {
  getUserColumns,
  resetUserColumns,
  saveUserColumns,
  type FxTableColumn,
  type UserColumnItem,
} from '@/api/system/tableConfig'
import { resolveI18nText } from '@/utils/i18n'

const MIN_COLUMN_WIDTH = 60
const MAX_COLUMN_WIDTH = 800
const ACTION_FIELD = 'action'

type LocalColumn = {
  field: string
  title: string
  visible: boolean
  order: number
  width?: number
}

const popoverOverlayInnerStyle: CSSProperties = {
  padding: 0,
  backgroundColor: 'var(--fx-bg-elevated)',
  color: 'var(--fx-text-primary)',
  boxShadow: 'none',
}

const panelSurfaceStyle: CSSProperties = {
  backgroundColor: 'var(--fx-bg-elevated)',
  color: 'var(--fx-text-primary)',
  border: '1px solid var(--fx-border-color)',
  borderRadius: 'var(--fx-radius-lg)',
  padding: '12px',
  width: '360px',
  minWidth: '360px',
  maxWidth: '480px',
}

function getColumnSettingPopupContainer(triggerNode: HTMLElement): HTMLElement {
  const layout = triggerNode?.closest?.('.fx-main-layout')
  return layout instanceof HTMLElement ? layout : document.body
}

const props = defineProps<{
  tableCode: string
  columns: FxTableColumn[]
}>()

const emit = defineEmits<{
  (e: 'change', columns: FxTableColumn[]): void
}>()

const { t } = useI18n()
const dropdownOpen = ref(false)
const saving = ref(false)
const localColumns = ref<LocalColumn[]>([])

function normalizeColumnWidth(width: unknown) {
  const numeric = typeof width === 'number' ? width : Number(width)
  if (!Number.isFinite(numeric)) {
    return undefined
  }
  return Math.max(MIN_COLUMN_WIDTH, Math.min(MAX_COLUMN_WIDTH, Math.round(numeric)))
}

function normalizeLocalOrder() {
  localColumns.value.forEach((col, index) => {
    col.order = index
    if (col.field !== ACTION_FIELD) {
      col.width = normalizeColumnWidth(col.width)
    }
  })
}

function toLocalColumn(col: FxTableColumn, index: number): LocalColumn {
  return {
    field: col.field,
    title: resolveI18nText(col.titleI18nJson ?? col.title, col.title || col.field),
    visible: col.field === ACTION_FIELD ? true : col.visible !== false,
    order: col.order ?? index,
    width: normalizeColumnWidth(col.width),
  }
}

function initLocalColumns() {
  localColumns.value = props.columns.map(toLocalColumn).sort((a, b) => a.order - b.order)
  normalizeLocalOrder()
}

async function loadUserConfig() {
  try {
    const result = await getUserColumns(props.tableCode)
    if (result?.columns?.length) {
      localColumns.value = result.columns.map(toLocalColumn).sort((a, b) => a.order - b.order)
      normalizeLocalOrder()
      return
    }
    initLocalColumns()
  } catch (error) {
    console.error('[ColumnSettingButton] load user columns failed:', error)
    initLocalColumns()
  }
}

watch(dropdownOpen, open => {
  if (!open) return
  void nextTick(() => {
    initLocalColumns()
    void loadUserConfig()
  })
})

function handleWidthInput(col: LocalColumn, value: number | string | null) {
  if (col.field === ACTION_FIELD) return
  col.width = normalizeColumnWidth(value)
}

async function handleSave() {
  saving.value = true
  try {
    normalizeLocalOrder()
    const columns: UserColumnItem[] = localColumns.value.map((col, index) => ({
      field: col.field,
      visible: col.field === ACTION_FIELD ? true : col.visible,
      order: index,
      width: col.field === ACTION_FIELD ? undefined : normalizeColumnWidth(col.width),
    }))

    await saveUserColumns({
      tableCode: props.tableCode,
      columns,
    })

    dropdownOpen.value = false

    const updatedColumns = props.columns
      .map(baseCol => {
        const localCol = localColumns.value.find(item => item.field === baseCol.field)
        return {
          ...baseCol,
          visible: localCol?.field === ACTION_FIELD ? true : localCol?.visible ?? true,
          order: localCol?.order ?? 0,
          width: localCol?.field === ACTION_FIELD
            ? baseCol.width
            : (normalizeColumnWidth(localCol?.width) ?? baseCol.width),
        }
      })
      .sort((a, b) => (a.order ?? 0) - (b.order ?? 0))

    emit('change', updatedColumns)
  } catch (error) {
    console.error('[ColumnSettingButton] save user columns failed:', error)
  } finally {
    saving.value = false
  }
}

async function handleReset() {
  try {
    await resetUserColumns(props.tableCode)
    initLocalColumns()
    dropdownOpen.value = false
    emit('change', props.columns)
  } catch (error) {
    console.error('[ColumnSettingButton] reset user columns failed:', error)
  }
}

watch(
  () => props.columns,
  () => {
    if (dropdownOpen.value) {
      void loadUserConfig()
    }
  },
  { deep: true },
)

watch(
  () => props.tableCode,
  () => {
    if (dropdownOpen.value) {
      void loadUserConfig()
    }
  },
)
</script>

<style scoped lang="less" src="@/styles/components/common/column-setting-button.less"></style>

<style lang="less" src="@/styles/components/common/column-setting-button-global.less"></style>
