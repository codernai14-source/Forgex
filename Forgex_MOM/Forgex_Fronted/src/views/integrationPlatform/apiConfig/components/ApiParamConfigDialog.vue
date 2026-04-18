<template>
  <a-modal
    v-model:open="visible"
    :title="`${t('integration.paramConfig.title')} - ${apiConfig?.apiName || apiConfig?.apiCode || ''}`"
    width="1200px"
    :footer="null"
    destroy-on-close
    @cancel="handleClose"
  >
    <a-spin :spinning="loading">
      <div class="param-config-layout">
        <section class="param-panel">
          <div class="panel-header">
            <div>
              <h3>{{ t('integration.paramConfig.requestFields') }}</h3>
              <p>{{ t('integration.paramConfig.requestStructure') }}</p>
            </div>
            <a-button size="small" @click="openJsonImport('REQUEST')">
              {{ t('integration.paramConfig.importRequestJson') }}
            </a-button>
          </div>

          <a-tree
            v-if="requestTree.length"
            :tree-data="buildTreeData(requestTree)"
            default-expand-all
            block-node
            @select="(_, info) => handleSelectField('source', info.node.dataRef)"
          />
          <a-empty v-else :description="t('integration.paramConfig.empty')" />
        </section>

        <section class="param-panel">
          <div class="panel-header">
            <div>
              <h3>{{ t('integration.paramConfig.responseFields') }}</h3>
              <p>{{ t('integration.paramConfig.responseStructure') }}</p>
            </div>
            <a-button size="small" @click="openJsonImport('RESPONSE')">
              {{ t('integration.paramConfig.importResponseJson') }}
            </a-button>
          </div>

          <a-tree
            v-if="responseTree.length"
            :tree-data="buildTreeData(responseTree)"
            default-expand-all
            block-node
            @select="(_, info) => handleSelectField('target', info.node.dataRef)"
          />
          <a-empty v-else :description="t('integration.paramConfig.empty')" />
        </section>

        <section class="mapping-panel">
          <div class="panel-header">
            <div>
              <h3>{{ t('integration.mapping.title') }}</h3>
              <p>{{ t('integration.mapping.direction') }}</p>
            </div>
            <a-select v-model:value="mappingDirection" style="width: 180px" @change="reloadMappings">
              <a-select-option value="INBOUND">{{ t('integration.mapping.requestToResponse') }}</a-select-option>
              <a-select-option value="OUTBOUND">{{ t('integration.mapping.responseToRequest') }}</a-select-option>
            </a-select>
          </div>

          <a-alert
            v-if="warnings.length"
            type="warning"
            show-icon
            class="mapping-alert"
            :message="warnings.join(' / ')"
          />

          <a-space class="mapping-toolbar">
            <a-button @click="handleAddMapping">{{ t('integration.mapping.addMapping') }}</a-button>
            <a-button @click="handleAutoMatch">{{ t('integration.mapping.autoMatch') }}</a-button>
            <a-button type="primary" :loading="savingMappings" @click="handleSaveMappings">
              {{ t('integration.common.saveMapping') }}
            </a-button>
          </a-space>

          <a-table
            size="small"
            :pagination="false"
            :columns="mappingColumns"
            :data-source="mappingRows"
            :row-key="(_, index) => String(index)"
            class="mapping-table"
          >
            <template #bodyCell="{ column, record, index }">
              <template v-if="column.key === 'sourceFieldPath'">
                <a-select
                  v-model:value="record.sourceFieldPath"
                  show-search
                  :placeholder="t('integration.mapping.sourceUnselected')"
                  :options="fieldOptions.source"
                  style="width: 100%"
                  @change="() => syncMappingMeta(index)"
                />
              </template>

              <template v-else-if="column.key === 'targetFieldPath'">
                <a-select
                  v-model:value="record.targetFieldPath"
                  show-search
                  :placeholder="t('integration.mapping.targetUnselected')"
                  :options="fieldOptions.target"
                  style="width: 100%"
                  @change="() => syncMappingMeta(index)"
                />
              </template>

              <template v-else-if="column.key === 'type'">
                <a-tag :color="isTypeCompatible(record.sourceType, record.targetType) ? 'blue' : 'orange'">
                  {{ record.sourceType || '-' }} -> {{ record.targetType || '-' }}
                </a-tag>
              </template>

              <template v-else-if="column.key === 'remark'">
                <a-input v-model:value="record.remark" />
              </template>

              <template v-else-if="column.key === 'action'">
                <a @click="removeMapping(index)">{{ t('common.delete') }}</a>
              </template>
            </template>
          </a-table>
        </section>
      </div>
    </a-spin>

    <a-modal
      v-model:open="jsonDialogOpen"
      :title="jsonDialogTitle"
      width="760px"
      :confirm-loading="parsingJson || importingJson"
      @ok="handleImportPreview"
    >
      <a-form layout="vertical">
        <a-form-item :label="t('integration.paramConfig.jsonInputLabel')">
          <a-textarea
            v-model:value="jsonText"
            :rows="8"
            :placeholder="t('integration.paramConfig.jsonInputPlaceholder')"
          />
        </a-form-item>
      </a-form>

      <a-space class="preview-toolbar">
        <a-button :loading="parsingJson" @click="handleParsePreview">
          {{ t('integration.common.parseJson') }}
        </a-button>
        <span>{{ t('integration.paramConfig.parsePreviewDesc') }}</span>
      </a-space>

      <a-tree
        v-if="previewTree.length"
        :tree-data="buildTreeData(previewTree)"
        default-expand-all
        class="preview-tree"
      />
    </a-modal>
  </a-modal>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { Modal } from 'ant-design-vue'
import { useI18n } from 'vue-i18n'
import {
  batchSaveApiParamMappings,
  getApiParamMappings,
  getApiParamTree,
  importApiParamJson,
  parseApiParamJson,
} from '@/api/system/integration'
import type {
  ApiConfigItem,
  ApiParamConfigItem,
  ApiParamMappingItem,
  IntegrationDirection,
  ParamDirection,
} from '@/api/system/integration'
import type { ParamMappingRow } from '../types'

interface Props {
  open: boolean
  apiConfig?: ApiConfigItem
}

interface Emits {
  (e: 'update:open', value: boolean): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()
const { t } = useI18n({ useScope: 'global' })

const loading = ref(false)
const savingMappings = ref(false)
const parsingJson = ref(false)
const importingJson = ref(false)
const requestTree = ref<ApiParamConfigItem[]>([])
const responseTree = ref<ApiParamConfigItem[]>([])
const previewTree = ref<ApiParamConfigItem[]>([])
const jsonText = ref('')
const jsonDirection = ref<ParamDirection>('REQUEST')
const jsonDialogOpen = ref(false)
const mappingDirection = ref<IntegrationDirection>('INBOUND')
const mappingRows = ref<ParamMappingRow[]>([])

const visible = computed({
  get: () => props.open,
  set: (value: boolean) => emit('update:open', value),
})

const jsonDialogTitle = computed(() =>
  jsonDirection.value === 'REQUEST'
    ? t('integration.paramConfig.importRequestJson')
    : t('integration.paramConfig.importResponseJson')
)

const mappingColumns = computed(() => [
  { title: t('integration.mapping.sourcePath'), key: 'sourceFieldPath', width: 220 },
  { title: t('integration.mapping.targetPath'), key: 'targetFieldPath', width: 220 },
  { title: `${t('integration.mapping.sourceType')} / ${t('integration.mapping.targetType')}`, key: 'type', width: 160 },
  { title: t('integration.mapping.remark'), key: 'remark' },
  { title: t('common.action'), key: 'action', width: 80 },
])

const flatRequestFields = computed(() => flattenFields(requestTree.value))
const flatResponseFields = computed(() => flattenFields(responseTree.value))

const fieldOptions = computed(() => {
  const sourceFields = mappingDirection.value === 'INBOUND' ? flatRequestFields.value : flatResponseFields.value
  const targetFields = mappingDirection.value === 'INBOUND' ? flatResponseFields.value : flatRequestFields.value
  return {
    source: sourceFields.map(field => ({ label: `${field.fieldPath} (${field.fieldType || '-'})`, value: field.fieldPath })),
    target: targetFields.map(field => ({ label: `${field.fieldPath} (${field.fieldType || '-'})`, value: field.fieldPath })),
  }
})

const warnings = computed(() => {
  const result: string[] = []
  if (mappingRows.value.some(row => row.sourceType && row.targetType && !isTypeCompatible(row.sourceType, row.targetType))) {
    result.push(t('integration.common.typeMismatch'))
  }

  const targetMapped = new Set(mappingRows.value.map(row => row.targetFieldPath).filter(Boolean))
  const targetFields = mappingDirection.value === 'INBOUND' ? flatResponseFields.value : flatRequestFields.value
  if (targetFields.some(field => field.required === 1 && !targetMapped.has(field.fieldPath))) {
    result.push(t('integration.common.unmappedRequired'))
  }
  return result
})

watch(
  () => props.open,
  (open) => {
    if (open) {
      void reloadAll()
    }
  }
)

async function reloadAll() {
  if (!props.apiConfig?.id) {
    return
  }
  loading.value = true
  try {
    const [request, response] = await Promise.all([
      getApiParamTree(props.apiConfig.id, 'REQUEST'),
      getApiParamTree(props.apiConfig.id, 'RESPONSE'),
    ])
    requestTree.value = request || []
    responseTree.value = response || []
    await reloadMappings()
  } finally {
    loading.value = false
  }
}

async function reloadMappings() {
  if (!props.apiConfig?.id) {
    return
  }
  const rows = await getApiParamMappings({
    apiConfigId: props.apiConfig.id,
    direction: mappingDirection.value,
  })
  mappingRows.value = (rows || []).map(item => toMappingRow(item))
}

function handleClose() {
  emit('update:open', false)
}

function openJsonImport(direction: ParamDirection) {
  jsonDirection.value = direction
  jsonText.value = ''
  previewTree.value = []
  jsonDialogOpen.value = true
}

async function handleParsePreview() {
  parsingJson.value = true
  try {
    previewTree.value = await parseApiParamJson({ jsonString: jsonText.value })
  } finally {
    parsingJson.value = false
  }
}

async function handleImportPreview() {
  if (!props.apiConfig?.id) {
    return
  }
  const importTask = async () => {
    importingJson.value = true
    try {
      await importApiParamJson({
        apiConfigId: props.apiConfig!.id!,
        direction: jsonDirection.value,
        jsonString: jsonText.value,
      })
      jsonDialogOpen.value = false
      await reloadAll()
    } finally {
      importingJson.value = false
    }
  }

  if ((jsonDirection.value === 'REQUEST' ? requestTree.value : responseTree.value).length) {
    Modal.confirm({
      title: t('common.tip'),
      content: t('integration.paramConfig.resetTreeConfirm'),
      okText: t('common.confirm'),
      cancelText: t('common.cancel'),
      onOk: importTask,
    })
    return
  }

  await importTask()
}

function handleSelectField(side: 'source' | 'target', field: ApiParamConfigItem) {
  if (field.nodeType !== 'FIELD') {
    return
  }
  const last = mappingRows.value[mappingRows.value.length - 1]
  if (!last || (last.sourceFieldPath && last.targetFieldPath)) {
    mappingRows.value.push({
      sourceFieldPath: side === 'source' ? field.fieldPath : '',
      targetFieldPath: side === 'target' ? field.fieldPath : '',
    })
  } else if (side === 'source') {
    last.sourceFieldPath = field.fieldPath
  } else {
    last.targetFieldPath = field.fieldPath
  }
  syncMappingMeta(mappingRows.value.length - 1)
}

function handleAddMapping() {
  mappingRows.value.push({ sourceFieldPath: '', targetFieldPath: '' })
}

function handleAutoMatch() {
  const sourceFields = mappingDirection.value === 'INBOUND' ? flatRequestFields.value : flatResponseFields.value
  const targetFields = mappingDirection.value === 'INBOUND' ? flatResponseFields.value : flatRequestFields.value
  const existingTargets = new Set(mappingRows.value.map(row => row.targetFieldPath).filter(Boolean))
  const suggestions: ParamMappingRow[] = []
  sourceFields.forEach(source => {
    const sourceName = source.fieldPath.split('.').pop()
    const target = targetFields.find(item => item.fieldPath.split('.').pop() === sourceName && !existingTargets.has(item.fieldPath))
    if (target) {
      existingTargets.add(target.fieldPath)
      suggestions.push({
        sourceFieldPath: source.fieldPath,
        targetFieldPath: target.fieldPath,
        sourceType: source.fieldType,
        targetType: target.fieldType,
        required: target.required === 1,
      })
    }
  })
  mappingRows.value = [...mappingRows.value, ...suggestions]
}

function removeMapping(index: number) {
  mappingRows.value.splice(index, 1)
}

function syncMappingMeta(index: number) {
  const row = mappingRows.value[index]
  if (!row) {
    return
  }
  const sourceFields = mappingDirection.value === 'INBOUND' ? flatRequestFields.value : flatResponseFields.value
  const targetFields = mappingDirection.value === 'INBOUND' ? flatResponseFields.value : flatRequestFields.value
  const source = sourceFields.find(field => field.fieldPath === row.sourceFieldPath)
  const target = targetFields.find(field => field.fieldPath === row.targetFieldPath)
  row.sourceType = source?.fieldType
  row.targetType = target?.fieldType
  row.required = target?.required === 1
}

async function handleSaveMappings() {
  if (!props.apiConfig?.id) {
    return
  }
  savingMappings.value = true
  try {
    await batchSaveApiParamMappings({
      apiConfigId: props.apiConfig.id,
      direction: mappingDirection.value,
      mappings: mappingRows.value
        .filter(row => row.sourceFieldPath && row.targetFieldPath)
        .map(row => ({
          apiConfigId: props.apiConfig!.id!,
          direction: mappingDirection.value,
          sourceFieldPath: row.sourceFieldPath,
          targetFieldPath: row.targetFieldPath,
          remark: row.remark,
        })),
    })
    await reloadMappings()
  } finally {
    savingMappings.value = false
  }
}

function toMappingRow(item: ApiParamMappingItem): ParamMappingRow {
  return {
    id: item.id,
    sourceFieldPath: item.sourceFieldPath,
    targetFieldPath: item.targetFieldPath,
    remark: item.remark,
  }
}

function flattenFields(tree: ApiParamConfigItem[]): ApiParamConfigItem[] {
  const fields: ApiParamConfigItem[] = []
  const walk = (items: ApiParamConfigItem[]) => {
    items.forEach(item => {
      if (item.nodeType === 'FIELD') {
        fields.push(item)
      }
      if (item.children?.length) {
        walk(item.children)
      }
    })
  }
  walk(tree)
  return fields
}

function buildTreeData(tree: ApiParamConfigItem[]) {
  return tree.map(item => ({
    key: `${item.direction}-${item.fieldPath}-${item.id || item.fieldName}`,
    title: `${item.fieldName || item.fieldPath} (${item.fieldType || item.nodeType})`,
    dataRef: item,
    children: item.children?.length ? buildTreeData(item.children) : undefined,
  }))
}

function isTypeCompatible(sourceType?: string, targetType?: string) {
  if (!sourceType || !targetType) {
    return true
  }
  if (sourceType === targetType) {
    return true
  }
  const numeric = ['number', 'integer', 'long', 'double', 'float']
  return numeric.includes(sourceType.toLowerCase()) && numeric.includes(targetType.toLowerCase())
}
</script>

<style scoped>
.param-config-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(0, 1fr) minmax(420px, 1.2fr);
  gap: 16px;
  min-height: 560px;
}

.param-panel,
.mapping-panel {
  padding: 16px;
  border: 1px solid var(--fx-border-color, #e5e7eb);
  border-radius: 16px;
  background: var(--fx-bg-container, #fff);
  overflow: auto;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
}

.panel-header h3 {
  margin: 0 0 4px;
}

.panel-header p {
  margin: 0;
  color: var(--fx-text-secondary, #64748b);
}

.mapping-alert,
.mapping-toolbar,
.preview-toolbar,
.preview-tree {
  margin-bottom: 12px;
}

.mapping-table {
  margin-top: 12px;
}

@media (max-width: 1280px) {
  .param-config-layout {
    grid-template-columns: 1fr;
  }
}
</style>
