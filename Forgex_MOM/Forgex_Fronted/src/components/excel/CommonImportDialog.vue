<template>
  <a-modal
    v-model:open="dialogVisible"
    width="1280px"
    :confirm-loading="submitting"
    :footer="null"
    :closable="false"
    wrap-class-name="common-import-modal"
    destroy-on-close
    @cancel="handleCancel"
  >
    <div class="common-import">
      <div class="common-import__header">
        <h2>{{ t('system.excel.commonImport.title') }}</h2>
        <button class="common-import__close" type="button" :aria-label="t('common.close')" @click="handleCancel">
          <CloseOutlined />
        </button>
      </div>

      <div class="common-import__body">
        <div class="common-import__main">
          <section class="common-import__panel common-import__file-panel">
            <div class="common-import__section-title">{{ t('system.excel.commonImport.selectFile') }}</div>
            <a-alert
              v-if="configLoadError"
              type="error"
              show-icon
              :message="configLoadError"
              class="common-import__alert"
            />

            <a-upload-dragger
              class="common-import__upload"
              :show-upload-list="false"
              :before-upload="handleBeforeUpload"
              accept=".xlsx,.xls,.csv"
              :disabled="configLoading"
            >
              <div class="common-import__upload-content">
                <InboxOutlined class="common-import__upload-icon" />
                <div class="common-import__upload-title">
                  {{ t('system.excel.commonImport.dragText') }}
                  <span>{{ t('system.excel.commonImport.chooseFile') }}</span>
                </div>
                <div class="common-import__upload-hint">{{ t('system.excel.commonImport.uploadHint') }}</div>
              </div>
            </a-upload-dragger>

            <div v-if="fileName" class="common-import__file-row">
              <div class="common-import__file-icon">
                <FileExcelOutlined />
              </div>
              <div class="common-import__file-info">
                <div class="common-import__file-name">{{ fileName }}</div>
                <div class="common-import__file-size">{{ fileSizeText }}</div>
              </div>
              <button class="common-import__link" type="button" @click="triggerFileSelect">
                {{ t('system.excel.commonImport.reselect') }}
              </button>
            </div>
          </section>

          <section class="common-import__panel common-import__preview-panel">
            <div class="common-import__preview-head">
              <div class="common-import__preview-title">
                {{ t('system.excel.commonImport.previewTitle') }}
                <span v-if="validationPassed" class="common-import__check">
                  <CheckCircleOutlined />
                  {{ t('system.excel.commonImport.validationPassed') }}
                </span>
                <button
                  v-else-if="validationErrors.length"
                  class="common-import__error-link"
                  type="button"
                  @click="errorModalVisible = true"
                >
                  <CloseCircleOutlined />
                  {{ t('system.excel.commonImport.validationFailed') }}
                </button>
              </div>
              <a-select
                v-if="sheetSummaries.length > 1"
                v-model:value="activeSheetCode"
                class="common-import__sheet-select"
                size="small"
              >
                <a-select-option v-for="sheet in sheetSummaries" :key="sheet.sheetCode" :value="sheet.sheetCode">
                  {{ sheet.sheetName }}
                </a-select-option>
              </a-select>
            </div>

            <div v-if="activeSheetSummary" class="common-import__preview-table-wrap">
              <a-table
                :columns="activeSheetSummary.columns"
                :data-source="activeSheetSummary.previewRows"
                :pagination="false"
                size="small"
                :scroll="{ x: 'max-content' }"
                row-key="__rowKey"
              />
            </div>
            <div v-else class="common-import__empty-preview">
              {{ t('system.excel.commonImport.noPreview') }}
            </div>

            <div class="common-import__preview-footer">
              <div>
                <span>{{ t('system.excel.commonImport.summaryPrefix', { total: totalRows, valid: validRows }) }}</span>
                <span class="common-import__invalid-count">
                  {{ t('system.excel.commonImport.summaryInvalid', { invalid: validationErrors.length }) }}
                </span>
              </div>
              <div class="common-import__preview-actions">
                <button class="common-import__link" type="button" :disabled="downloading" @click="handleDownloadTemplate">
                  <DownloadOutlined />
                  {{ t('system.excel.downloadTemplate') }}
                </button>
                <button
                  class="common-import__link"
                  type="button"
                  :disabled="!activeSheetSummary"
                  @click="activeSheetSummary && openAllRows(activeSheetSummary.sheetCode)"
                >
                  <EyeOutlined />
                  {{ t('system.excel.commonImport.viewAll') }}
                </button>
              </div>
            </div>
          </section>
        </div>

        <aside class="common-import__panel common-import__settings">
          <div class="common-import__section-title">{{ t('system.excel.commonImport.importSettings') }}</div>
          <div class="common-import__mode-title">{{ t('system.excel.commonImport.importMode') }}</div>
          <a-radio-group v-model:value="importMode" class="common-import__mode-list">
            <a-radio
              v-for="mode in importModeOptions"
              :key="mode.value"
              class="common-import__mode-item"
              :value="mode.value"
            >
              <span class="common-import__mode-name">{{ mode.label }}</span>
              <span class="common-import__mode-desc">{{ mode.desc }}</span>
            </a-radio>
          </a-radio-group>

          <div v-if="result" class="common-import__result-card">
            <div class="common-import__result-title">{{ t('system.excel.commonImport.resultTitle') }}</div>
            <div class="common-import__result-grid">
              <span>{{ t('system.excel.commonImport.totalRows') }} {{ result.totalCount || 0 }}</span>
              <span>{{ t('system.excel.commonImport.createdCount') }} {{ result.createdCount || 0 }}</span>
              <span>{{ t('system.excel.commonImport.updatedCount') }} {{ result.updatedCount || 0 }}</span>
              <span>{{ t('system.excel.commonImport.failedCount') }} {{ result.failedCount || 0 }}</span>
            </div>
          </div>
        </aside>
      </div>

      <div class="common-import__footer">
        <a-button class="common-import__footer-btn" @click="handleCancel">
          {{ t('common.cancel') }}
        </a-button>
        <a-button
          type="primary"
          class="common-import__footer-btn common-import__footer-btn--primary"
          :loading="submitting"
          :disabled="!canSubmit"
          @click="handleSubmit"
        >
          {{ t('common.confirm') }}
        </a-button>
      </div>
    </div>

    <a-modal
      v-model:open="allRowsVisible"
      :title="currentAllRowsTitle"
      width="1100px"
      :footer="null"
    >
      <a-table
        :columns="currentAllRowsColumns"
        :data-source="currentAllRows"
        size="small"
        :pagination="{ pageSize: 20, showSizeChanger: true }"
        :scroll="{ x: 'max-content', y: 520 }"
        row-key="__rowKey"
      />
    </a-modal>

    <a-modal
      v-model:open="errorModalVisible"
      :title="t('system.excel.commonImport.validationFailed')"
      width="760px"
      :footer="null"
    >
      <ul class="common-import__errors common-import__errors--modal">
        <li v-for="item in validationErrors" :key="item">{{ item }}</li>
      </ul>
    </a-modal>
  </a-modal>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { message } from 'ant-design-vue'
import {
  CheckCircleOutlined,
  CloseCircleOutlined,
  CloseOutlined,
  DownloadOutlined,
  EyeOutlined,
  FileExcelOutlined,
  InboxOutlined,
} from '@ant-design/icons-vue'
import * as XLSX from 'xlsx'
import { downloadTemplate, executeImport, importConfigDetailByCode } from '@/api/system/excel'
import { getLocale } from '@/locales'

type ImportMode = 'ADD' | 'UPDATE' | 'COVER'

interface ImportItem {
  sheetCode?: string
  sheetName?: string
  importField?: string
  i18nJson?: string
  fieldRemark?: string
  required?: boolean
  orderNum?: number
}

interface SheetConfig {
  sheetCode: string
  sheetName: string
  items: ImportItem[]
}

interface SheetSummary {
  sheetCode: string
  sheetName: string
  total: number
  columns: Array<{ title: string; dataIndex: string; key: string; width: number }>
  previewRows: Array<Record<string, any>>
  allRows: Array<Record<string, any>>
}

interface Props {
  open?: boolean
  tableCode: string
}

const props = withDefaults(defineProps<Props>(), {
  open: false,
})

const emit = defineEmits<{
  (e: 'update:open', value: boolean): void
  (e: 'success', result: any): void
}>()

const { t } = useI18n()

const dialogVisible = ref(false)
const importMode = ref<ImportMode>('ADD')
const configLoading = ref(false)
const downloading = ref(false)
const submitting = ref(false)
const config = ref<any>()
const configLoadError = ref('')
const fileName = ref('')
const fileSize = ref(0)
const importData = ref<Record<string, Array<Record<string, any>>>>({})
const sheetSummaries = ref<SheetSummary[]>([])
const validationErrors = ref<string[]>([])
const result = ref<any>()
const activeSheetCode = ref('')
const allRowsVisible = ref(false)
const errorModalVisible = ref(false)
const currentAllRowsSheetCode = ref('')

const maxFileSize = 10 * 1024 * 1024
const allowedExtensions = ['xlsx', 'xls', 'csv']

const totalRows = computed(() => sheetSummaries.value.reduce((sum, item) => sum + item.total, 0))
const previewRowsCount = computed(() => sheetSummaries.value.reduce((sum, item) => sum + item.previewRows.length, 0))
const canSubmit = computed(() => !!config.value && totalRows.value > 0 && validationErrors.value.length === 0 && !submitting.value)
const validRows = computed(() => Math.max(totalRows.value - validationErrors.value.length, 0))
const validationPassed = computed(() => !!fileName.value && totalRows.value > 0 && validationErrors.value.length === 0)
const activeSheetSummary = computed(() => sheetSummaries.value.find(item => item.sheetCode === activeSheetCode.value) || sheetSummaries.value[0])
const currentAllRowsSheet = computed(() => sheetSummaries.value.find(item => item.sheetCode === currentAllRowsSheetCode.value))
const currentAllRows = computed(() => currentAllRowsSheet.value?.allRows || [])
const currentAllRowsColumns = computed(() => currentAllRowsSheet.value?.columns || [])
const currentAllRowsTitle = computed(() => currentAllRowsSheet.value?.sheetName || t('system.excel.commonImport.viewAll'))
const fileSizeText = computed(() => formatFileSize(fileSize.value))
const importModeOptions = computed(() => [
  {
    value: 'ADD' as ImportMode,
    label: t('system.excel.commonImport.modeAddData'),
    desc: t('system.excel.commonImport.modeAddDesc'),
  },
  {
    value: 'UPDATE' as ImportMode,
    label: t('system.excel.commonImport.modeUpdateData'),
    desc: t('system.excel.commonImport.modeUpdateDesc'),
  },
  {
    value: 'COVER' as ImportMode,
    label: t('system.excel.commonImport.modeCoverData'),
    desc: t('system.excel.commonImport.modeCoverDesc'),
  },
])

watch(
  () => props.open,
  async (value) => {
    dialogVisible.value = value
    if (value) {
      resetParsedState()
      await loadConfig()
    }
  },
  { immediate: true },
)

watch(
  () => dialogVisible.value,
  value => emit('update:open', value),
)

function resetParsedState() {
  fileName.value = ''
  fileSize.value = 0
  importMode.value = 'ADD'
  importData.value = {}
  sheetSummaries.value = []
  validationErrors.value = []
  result.value = undefined
  activeSheetCode.value = ''
  allRowsVisible.value = false
  errorModalVisible.value = false
  currentAllRowsSheetCode.value = ''
}

async function loadConfig() {
  if (!props.tableCode) {
    configLoadError.value = t('system.excel.commonImport.configMissing')
    return
  }

  configLoading.value = true
  configLoadError.value = ''
  try {
    config.value = await importConfigDetailByCode({ tableCode: props.tableCode })
  } catch (error: any) {
    config.value = undefined
    configLoadError.value = error?.message || t('system.excel.commonImport.configLoadFailed')
  } finally {
    configLoading.value = false
  }
}

async function handleBeforeUpload(file: File) {
  result.value = undefined
  if (!config.value) {
    await loadConfig()
  }
  if (!config.value) {
    return false
  }

  const errors: string[] = []
  const extension = getExtension(file.name)
  if (!allowedExtensions.includes(extension)) {
    errors.push(t('system.excel.commonImport.invalidFileType'))
  }
  if (file.size > maxFileSize) {
    errors.push(t('system.excel.commonImport.fileTooLarge'))
  }
  if (errors.length) {
    validationErrors.value = errors
    fileName.value = file.name
    fileSize.value = file.size
    return false
  }

  try {
    await parseFile(file, extension)
  } catch (error) {
    console.error('parse import file failed:', error)
    validationErrors.value = [t('system.excel.commonImport.parseFailed')]
    fileName.value = file.name
    fileSize.value = file.size
  }
  return false
}

async function parseFile(file: File, extension: string) {
  const buffer = await file.arrayBuffer()
  const workbook = XLSX.read(buffer, {
    type: 'array',
    cellDates: true,
  })
  const sheetConfigs = buildSheetConfigs()
  const errors: string[] = []
  const data: Record<string, Array<Record<string, any>>> = {}
  const summaries: SheetSummary[] = []

  if (extension === 'csv' && sheetConfigs.length > 1) {
    errors.push(t('system.excel.commonImport.csvSingleSheetOnly'))
  }

  for (const [sheetIndex, sheetConfig] of sheetConfigs.entries()) {
    const worksheetName = resolveWorksheetName(workbook, sheetConfig, sheetConfigs.length === 1 ? 0 : sheetIndex)
    const worksheet = worksheetName ? workbook.Sheets[worksheetName] : undefined
    if (!worksheet) {
      errors.push(t('system.excel.commonImport.sheetMissing', { sheet: sheetConfig.sheetName }))
      data[sheetConfig.sheetCode] = []
      continue
    }

    const matrix = XLSX.utils.sheet_to_json<any[]>(worksheet, {
      header: 1,
      defval: '',
      raw: false,
      blankrows: false,
    })
    const parsed = parseWorksheet(matrix, sheetConfig)
    errors.push(...parsed.errors)
    data[sheetConfig.sheetCode] = parsed.rows
    summaries.push({
      sheetCode: sheetConfig.sheetCode,
      sheetName: sheetConfig.sheetName,
      total: parsed.rows.length,
      columns: buildColumns(sheetConfig.items),
      previewRows: decorateRows(parsed.rows.slice(0, 5), sheetConfig.sheetCode),
      allRows: decorateRows(parsed.rows, sheetConfig.sheetCode),
    })
  }

  if (Object.values(data).every(rows => rows.length === 0)) {
    errors.push(t('system.excel.commonImport.emptyData'))
  }

  fileName.value = file.name
  fileSize.value = file.size
  importData.value = data
  sheetSummaries.value = summaries
  validationErrors.value = Array.from(new Set(errors))
  activeSheetCode.value = summaries[0]?.sheetCode || ''
}

function parseWorksheet(matrix: any[][], sheetConfig: SheetConfig) {
  const errors: string[] = []
  const expectedHeaders = sheetConfig.items.map(item => normalizeHeader(buildHeaderLabel(item)))
  const headerRowIndex = findHeaderRowIndex(matrix, expectedHeaders)
  if (headerRowIndex < 0) {
    return { rows: [], errors: [t('system.excel.commonImport.emptySheet', { sheet: sheetConfig.sheetName })] }
  }

  const headerRow = matrix[headerRowIndex] || []
  expectedHeaders.forEach((expected, index) => {
    const actual = normalizeHeader(headerRow[index])
    if (actual !== expected) {
      errors.push(t('system.excel.commonImport.headerMismatch', {
        sheet: sheetConfig.sheetName,
        column: index + 1,
        expected,
        actual: actual || '-',
      }))
    }
  })

  const rows: Array<Record<string, any>> = []
  matrix.slice(headerRowIndex + 1).forEach((row, rowIndex) => {
    const isEmpty = sheetConfig.items.every((_, index) => isBlank(row[index]))
    if (isEmpty) {
      return
    }

    const record: Record<string, any> = {}
    sheetConfig.items.forEach((item, index) => {
      const field = item.importField || ''
      record[field] = normalizeCellValue(row[index])
      if (item.required && isBlank(row[index])) {
        errors.push(t('system.excel.commonImport.requiredMissing', {
          sheet: sheetConfig.sheetName,
          row: headerRowIndex + rowIndex + 2,
          field: buildHeaderLabel(item),
        }))
      }
    })
    rows.push(record)
  })

  return { rows, errors }
}

function buildSheetConfigs(): SheetConfig[] {
  const items: ImportItem[] = Array.isArray(config.value?.items) ? config.value.items : []
  const grouped = new Map<string, SheetConfig>()
  const ordered = [...items].sort((a, b) => {
    const sheetCompare = String(a.sheetCode || 'main').localeCompare(String(b.sheetCode || 'main'))
    if (sheetCompare !== 0) {
      return sheetCompare
    }
    return Number(a.orderNum ?? 0) - Number(b.orderNum ?? 0)
  })

  ordered.forEach(item => {
    const sheetCode = item.sheetCode || 'main'
    if (!grouped.has(sheetCode)) {
      grouped.set(sheetCode, {
        sheetCode,
        sheetName: item.sheetName || (sheetCode === 'main' ? t('system.excel.commonImport.mainSheet') : sheetCode),
        items: [],
      })
    }
    grouped.get(sheetCode)!.items.push(item)
  })

  return Array.from(grouped.values())
}

function buildColumns(items: ImportItem[]) {
  return items.map(item => ({
    title: buildHeaderLabel(item),
    dataIndex: item.importField || '',
    key: item.importField || '',
    width: 160,
  }))
}

function buildHeaderLabel(item: ImportItem) {
  const label = resolveI18nText(item.i18nJson, item.importField || '')
  let header = label || item.importField || ''
  if (item.required) {
    header += ' *'
  }
  if (item.fieldRemark) {
    header += ` (${item.fieldRemark})`
  }
  return header
}

function resolveI18nText(i18nJson: string | undefined, fallback: string) {
  if (!i18nJson) {
    return fallback
  }
  try {
    const data = JSON.parse(i18nJson)
    const locale = getLocale()
    return data?.[locale] || data?.['zh-CN'] || data?.['en-US'] || fallback
  } catch {
    return fallback
  }
}

function resolveWorksheetName(workbook: XLSX.WorkBook, sheetConfig: SheetConfig, fallbackIndex: number) {
  const names = workbook.SheetNames || []
  return names.find(name => normalizeSheetName(name) === normalizeSheetName(sheetConfig.sheetName))
    || names.find(name => normalizeSheetName(name) === normalizeSheetName(sheetConfig.sheetCode))
    || names[fallbackIndex]
}

function normalizeSheetName(value: unknown) {
  return String(value ?? '').trim().toLowerCase()
}

function normalizeHeader(value: unknown) {
  return String(value ?? '')
    .replace(/\*/g, '')
    .replace(/\s*\([^)]*\)\s*$/g, '')
    .trim()
}

function findHeaderRowIndex(matrix: any[][], expectedHeaders: string[]) {
  let bestIndex = -1
  let bestScore = 0
  matrix.forEach((row, index) => {
    const score = expectedHeaders.reduce((count, expected, columnIndex) => {
      return normalizeHeader(row[columnIndex]) === expected ? count + 1 : count
    }, 0)
    if (score > bestScore) {
      bestScore = score
      bestIndex = index
    }
  })
  if (bestScore > 0) {
    return bestIndex
  }
  return matrix.findIndex(row => row.some(cell => String(cell ?? '').trim()))
}

function normalizeCellValue(value: unknown) {
  if (value instanceof Date) {
    return XLSX.SSF.format('yyyy-mm-dd hh:mm:ss', value)
  }
  const text = String(value ?? '').trim()
  return text === '' ? null : text
}

function decorateRows(rows: Array<Record<string, any>>, sheetCode: string) {
  return rows.map((row, index) => ({
    __rowKey: `${sheetCode}-${index}`,
    ...row,
  }))
}

function isBlank(value: unknown) {
  return value === undefined || value === null || String(value).trim() === ''
}

function getExtension(name: string) {
  return name.split('.').pop()?.toLowerCase() || ''
}

function formatFileSize(size: number) {
  if (!size) {
    return '-'
  }
  if (size < 1024) {
    return `${size} B`
  }
  if (size < 1024 * 1024) {
    return `${(size / 1024).toFixed(1)} KB`
  }
  return `${(size / 1024 / 1024).toFixed(1)} MB`
}

function triggerFileSelect() {
  const uploader = document.querySelector('.common-import-modal .ant-upload input[type="file"]') as HTMLInputElement | null
  uploader?.click()
}

function openAllRows(sheetCode: string) {
  currentAllRowsSheetCode.value = sheetCode
  allRowsVisible.value = true
}

async function handleDownloadTemplate() {
  downloading.value = true
  try {
    const resp: any = await downloadTemplate({ tableCode: props.tableCode })
    const blob = new Blob([resp.data], {
      type: resp.headers?.['content-type'] || 'application/octet-stream',
    })
    const text = await blob.slice(0, 64).text()
    if (text.trim().startsWith('{')) {
      const fullText = await blob.text()
      const parsed = JSON.parse(fullText)
      message.error(parsed.message || t('system.excel.downloadFailed'))
      return
    }
    const url = window.URL.createObjectURL(blob)
    const anchor = document.createElement('a')
    anchor.href = url
    anchor.download = `import-template-${props.tableCode}.xlsx`
    document.body.appendChild(anchor)
    anchor.click()
    document.body.removeChild(anchor)
    window.URL.revokeObjectURL(url)
    message.success(t('system.excel.downloadSuccess'))
  } catch (error) {
    console.error('download import template failed:', error)
    message.error(t('system.excel.downloadFailed'))
  } finally {
    downloading.value = false
  }
}

async function handleSubmit() {
  if (!canSubmit.value) {
    return
  }
  submitting.value = true
  try {
    result.value = await executeImport({
      tableCode: props.tableCode,
      importMode: importMode.value,
      importConfig: config.value,
      importData: importData.value,
    })
    message.success(t('system.excel.commonImport.importSuccess'))
    emit('success', result.value)
  } finally {
    submitting.value = false
  }
}

function handleCancel() {
  dialogVisible.value = false
}
</script>

<style scoped lang="less">
.common-import {
  --ci-bg: var(--fx-bg-elevated, #1a2029);
  --ci-panel: var(--fx-bg-container, #1f2732);
  --ci-panel-soft: color-mix(in srgb, var(--fx-bg-container, #1f2732) 88%, var(--fx-primary, #1677ff) 12%);
  --ci-border: color-mix(in srgb, var(--fx-border-color, #2d3748) 72%, transparent);
  --ci-border-strong: color-mix(in srgb, var(--fx-text-tertiary, #64748b) 42%, transparent);
  --ci-text: var(--fx-text-primary, #e2e8f0);
  --ci-muted: var(--fx-text-secondary, #94a3b8);
  --ci-subtle: var(--fx-text-tertiary, #64748b);
  --ci-primary: var(--fx-primary, #1677ff);
  --ci-success: var(--fx-success, #49aa19);
  --ci-error: var(--fx-error, #dc4446);
  display: grid;
  grid-template-rows: auto 1fr auto;
  min-height: 620px;
  color: var(--ci-text);
}

.common-import__header,
.common-import__preview-head,
.common-import__preview-footer,
.common-import__file-row,
.common-import__footer,
.common-import__preview-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.common-import__header {
  margin-bottom: 20px;

  h2 {
    margin: 0;
    color: var(--ci-text);
    font-size: 24px;
    font-weight: 700;
    line-height: 1.2;
  }
}

.common-import__close {
  width: 34px;
  height: 34px;
  border: 0;
  border-radius: var(--fx-radius, 6px);
  color: var(--ci-muted);
  background: transparent;
  cursor: pointer;
  font-size: 22px;
  line-height: 1;

  &:hover {
    color: var(--ci-text);
    background: color-mix(in srgb, var(--fx-fill, #1e293b) 62%, transparent);
  }
}

.common-import__body {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 340px;
  gap: 16px;
  min-height: 470px;
}

.common-import__main {
  display: grid;
  grid-template-rows: auto minmax(0, 1fr);
  gap: 16px;
  min-width: 0;
}

.common-import__panel {
  border: 1px solid var(--ci-border);
  border-radius: var(--fx-radius-lg, 8px);
  background:
    linear-gradient(145deg, color-mix(in srgb, var(--ci-panel) 96%, white 4%), var(--ci-panel));
  box-shadow: inset 0 1px 0 color-mix(in srgb, white 5%, transparent);
}

.common-import__file-panel {
  padding: 18px 18px 16px;
}

.common-import__preview-panel {
  display: grid;
  grid-template-rows: auto minmax(0, 1fr) auto;
  min-height: 250px;
  padding: 18px;
}

.common-import__settings {
  padding: 18px;
}

.common-import__section-title,
.common-import__preview-title,
.common-import__mode-title {
  color: var(--ci-text);
  font-size: 16px;
  font-weight: 700;
}

.common-import__section-title {
  margin-bottom: 14px;
}

.common-import__upload {
  display: block;

  :deep(.ant-upload-drag) {
    min-height: 118px;
    border-color: var(--ci-border-strong);
    border-style: dashed;
    border-radius: var(--fx-radius-lg, 8px);
    background: color-mix(in srgb, var(--fx-bg-base, #0f1419) 68%, var(--ci-panel) 32%);
    transition: border-color 0.2s ease, background 0.2s ease;

    &:hover {
      border-color: var(--ci-primary);
      background: color-mix(in srgb, var(--fx-bg-base, #0f1419) 58%, var(--ci-primary) 8%);
    }
  }

  :deep(.ant-upload) {
    padding: 22px 16px;
  }
}

.common-import__upload-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}

.common-import__upload-icon {
  color: var(--ci-primary);
  font-size: 34px;
}

.common-import__upload-title {
  color: var(--ci-text);
  font-size: 16px;
  font-weight: 700;

  span {
    color: var(--ci-primary);
  }
}

.common-import__upload-hint {
  color: var(--ci-muted);
  font-size: 13px;
}

.common-import__file-row {
  min-height: 56px;
  margin-top: 12px;
  padding: 10px 14px;
  border: 1px solid color-mix(in srgb, var(--ci-border) 85%, transparent);
  border-radius: var(--fx-radius-lg, 8px);
  background: color-mix(in srgb, var(--fx-bg-base, #0f1419) 54%, transparent);
  gap: 12px;
}

.common-import__file-icon {
  display: grid;
  place-items: center;
  width: 30px;
  height: 34px;
  border-radius: 4px;
  color: #fff;
  background: var(--ci-success);
  font-size: 20px;
  flex: 0 0 auto;
}

.common-import__file-info {
  min-width: 0;
  flex: 1;
}

.common-import__file-name {
  overflow: hidden;
  color: var(--ci-text);
  font-size: 15px;
  font-weight: 600;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.common-import__file-size {
  margin-top: 2px;
  color: var(--ci-muted);
  font-size: 13px;
}

.common-import__alert {
  margin-bottom: 12px;
  border-radius: var(--fx-radius, 6px);
}

.common-import__preview-head {
  gap: 12px;
  margin-bottom: 14px;
}

.common-import__preview-title {
  display: flex;
  align-items: center;
  gap: 14px;
}

.common-import__check,
.common-import__error-link {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  border: 0;
  background: transparent;
  font-size: 14px;
  font-weight: 600;
}

.common-import__check {
  color: var(--ci-success);
}

.common-import__error-link {
  color: var(--ci-error);
  cursor: pointer;
}

.common-import__sheet-select {
  min-width: 130px;
}

.common-import__preview-table-wrap {
  overflow: hidden;
  border: 1px solid color-mix(in srgb, var(--ci-border) 86%, transparent);
  border-radius: var(--fx-radius-lg, 8px);

  :deep(.ant-table) {
    color: var(--ci-text);
    background: color-mix(in srgb, var(--fx-bg-base, #0f1419) 54%, transparent);
  }

  :deep(.ant-table-thead > tr > th) {
    height: 42px;
    border-bottom-color: color-mix(in srgb, var(--ci-border) 80%, transparent);
    color: var(--ci-muted);
    background: color-mix(in srgb, var(--ci-primary) 18%, var(--ci-panel) 82%);
    font-size: 14px;
    font-weight: 700;
  }

  :deep(.ant-table-tbody > tr > td) {
    height: 36px;
    border-bottom-color: color-mix(in srgb, var(--ci-border) 58%, transparent);
    color: var(--ci-text);
    background: color-mix(in srgb, var(--fx-bg-base, #0f1419) 36%, transparent);
    font-size: 14px;
  }

  :deep(.ant-table-tbody > tr:hover > td) {
    background: color-mix(in srgb, var(--ci-primary) 10%, var(--fx-bg-base, #0f1419) 90%);
  }
}

.common-import__empty-preview {
  display: grid;
  min-height: 150px;
  place-items: center;
  border: 1px dashed var(--ci-border-strong);
  border-radius: var(--fx-radius-lg, 8px);
  color: var(--ci-muted);
  background: color-mix(in srgb, var(--fx-bg-base, #0f1419) 50%, transparent);
}

.common-import__preview-footer {
  gap: 12px;
  padding-top: 12px;
  color: var(--ci-muted);
  font-size: 14px;
}

.common-import__invalid-count {
  margin-left: 8px;
  color: var(--ci-error);
  font-weight: 700;
}

.common-import__preview-actions {
  gap: 20px;
  flex: 0 0 auto;
}

.common-import__link {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  border: 0;
  color: var(--ci-primary);
  background: transparent;
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;

  &:disabled {
    color: var(--fx-text-disabled, #475569);
    cursor: not-allowed;
  }
}

.common-import__mode-title {
  margin: 34px 0 16px;
}

.common-import__mode-list {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.common-import__mode-item {
  align-items: flex-start;
  margin: 0;
  color: var(--ci-text);

  :deep(.ant-radio) {
    align-self: flex-start;
    margin-top: 2px;
  }

  :deep(.ant-radio-inner) {
    border-color: var(--ci-border-strong);
    background: transparent;
  }

  :deep(.ant-radio-checked .ant-radio-inner) {
    border-color: var(--ci-primary);
    background: transparent;

    &::after {
      background-color: var(--ci-primary);
    }
  }

  :deep(.ant-radio-wrapper) {
    color: var(--ci-text);
  }
}

.common-import__mode-name,
.common-import__mode-desc {
  display: block;
}

.common-import__mode-name {
  color: var(--ci-text);
  font-size: 15px;
  font-weight: 700;
  line-height: 1.25;
}

.common-import__mode-desc {
  margin-top: 5px;
  color: var(--ci-muted);
  font-size: 13px;
  line-height: 1.45;
}

.common-import__result-card {
  margin-top: 34px;
  padding: 16px;
  border: 1px solid color-mix(in srgb, var(--ci-primary) 28%, var(--ci-border) 72%);
  border-radius: var(--fx-radius-lg, 8px);
  background: color-mix(in srgb, var(--ci-primary) 8%, transparent);
}

.common-import__result-title {
  margin-bottom: 12px;
  color: var(--ci-text);
  font-weight: 700;
}

.common-import__result-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 8px;
  color: var(--ci-muted);
  font-size: 13px;
}

.common-import__footer {
  justify-content: flex-end;
  gap: 14px;
  padding-top: 20px;
}

.common-import__footer-btn {
  min-width: 96px;
  height: 38px;
  border-color: var(--ci-border);
  color: var(--ci-text);
  background: color-mix(in srgb, var(--ci-panel) 72%, transparent);
  font-weight: 600;

  &:hover {
    border-color: var(--ci-primary);
    color: var(--ci-primary);
    background: color-mix(in srgb, var(--ci-primary) 8%, var(--ci-panel) 92%);
  }
}

.common-import__footer-btn--primary {
  border-color: var(--ci-primary);
  color: #fff;
  background: var(--ci-primary);
  box-shadow: 0 8px 18px color-mix(in srgb, var(--ci-primary) 28%, transparent);

  &:hover,
  &:focus {
    border-color: var(--fx-primary-hover, var(--ci-primary));
    color: #fff;
    background: var(--fx-primary-hover, var(--ci-primary));
  }
}

.common-import__errors {
  margin: 0;
  padding-left: 18px;
  color: var(--ci-text);
}

.common-import__errors--modal {
  max-height: 520px;
  overflow: auto;
}

:global(.common-import-modal .ant-modal) {
  top: 32px;
  max-width: calc(100vw - 48px);
  padding-bottom: 32px;
}

:global(.common-import-modal .ant-modal-content) {
  overflow: hidden;
  border: 1px solid color-mix(in srgb, var(--fx-border-color, #2d3748) 76%, transparent);
  border-radius: var(--fx-radius-lg, 8px);
  background:
    radial-gradient(circle at 20% 0%, color-mix(in srgb, var(--fx-primary, #1677ff) 10%, transparent), transparent 30%),
    var(--fx-bg-elevated, #1a2029);
  box-shadow: 0 26px 70px rgba(0, 0, 0, 0.42);
}

:global(.common-import-modal .ant-modal-body) {
  padding: 24px 22px 24px;
}

@media (max-width: 1180px) {
  .common-import {
    min-height: auto;
  }

  .common-import__body {
    grid-template-columns: 1fr;
  }

  .common-import__settings {
    min-height: 260px;
  }
}

@media (max-width: 768px) {
  .common-import__header {
    margin-bottom: 18px;
  }

  .common-import__body,
  .common-import__main {
    gap: 12px;
  }

  .common-import__file-panel,
  .common-import__preview-panel,
  .common-import__settings {
    padding: 16px;
  }

  .common-import__preview-footer {
    align-items: flex-start;
    flex-direction: column;
  }

  .common-import__footer {
    padding-top: 18px;
  }
}
</style>
