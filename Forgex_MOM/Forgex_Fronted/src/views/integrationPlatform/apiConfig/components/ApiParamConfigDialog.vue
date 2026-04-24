<template>
  <component
    :is="pageMode ? 'div' : 'a-modal'"
    v-bind="pageMode ? {} : modalProps"
    class="api-param-config-shell"
  >
    <a-spin :spinning="loading">
      <div class="param-config-layout">
        <section class="param-panel">
          <div class="panel-header">
            <div>
              <h3>{{ senderTitle }}</h3>
              <p>{{ senderDesc }}</p>
            </div>
            <a-space wrap>
              <a-button size="small" @click="appendRootNode('REQUEST')">{{ t('common.add') }}</a-button>
              <a-button size="small" @click="openImportDialog('REQUEST', 'JSON')">
                {{ t('integration.paramConfig.importRequestJson') }}
              </a-button>
              <a-button size="small" @click="openImportDialog('REQUEST', 'JAVA')">
                {{ t('integration.paramConfig.importJavaEntity') }}
              </a-button>
            </a-space>
          </div>

          <a-table
            size="small"
            :pagination="false"
            :columns="treeColumns"
            :data-source="requestTree"
            :row-key="getRowKey"
            :indent-size="18"
            :scroll="{ x: 820 }"
            class="tree-table"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'fieldName'">
                <a-input v-model:value="record.fieldName" :placeholder="t('integration.paramConfig.field')" />
              </template>
              <template v-else-if="column.key === 'fieldLabel'">
                <a-input v-model:value="record.fieldLabel" :placeholder="t('integration.paramConfig.fieldName')" />
              </template>
              <template v-else-if="column.key === 'fieldType'">
                <a-select v-model:value="record.fieldType" :options="fieldTypeOptions" style="width: 120px" />
              </template>
              <template v-else-if="column.key === 'remark'">
                <a-input v-model:value="record.remark" :placeholder="t('integration.paramConfig.remark')" />
              </template>
              <template v-else-if="column.key === 'required'">
                <a-switch :checked="record.required === 1" @change="checked => record.required = checked ? 1 : 0" />
              </template>
              <template v-else-if="column.key === 'action'">
                <a-space size="small">
                  <a @click="appendChildNode(record, 'REQUEST')">{{ t('common.add') }}</a>
                  <a class="danger-link" @click="removeNode(requestTree, record)">{{ t('common.delete') }}</a>
                </a-space>
              </template>
            </template>
          </a-table>
        </section>

        <section class="param-panel">
          <div class="panel-header">
            <div>
              <h3>{{ receiverTitle }}</h3>
              <p>{{ receiverDesc }}</p>
            </div>
            <a-space wrap>
              <a-button size="small" @click="appendRootNode('RESPONSE')">{{ t('common.add') }}</a-button>
              <a-button size="small" @click="openImportDialog('RESPONSE', 'JSON')">
                {{ t('integration.paramConfig.importResponseJson') }}
              </a-button>
              <a-button size="small" @click="openImportDialog('RESPONSE', 'JAVA')">
                {{ t('integration.paramConfig.importJavaEntity') }}
              </a-button>
            </a-space>
          </div>

          <a-table
            size="small"
            :pagination="false"
            :columns="treeColumns"
            :data-source="responseTree"
            :row-key="getRowKey"
            :indent-size="18"
            :scroll="{ x: 820 }"
            class="tree-table"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'fieldName'">
                <a-input v-model:value="record.fieldName" :placeholder="t('integration.paramConfig.field')" />
              </template>
              <template v-else-if="column.key === 'fieldLabel'">
                <a-input v-model:value="record.fieldLabel" :placeholder="t('integration.paramConfig.fieldName')" />
              </template>
              <template v-else-if="column.key === 'fieldType'">
                <a-select v-model:value="record.fieldType" :options="fieldTypeOptions" style="width: 120px" />
              </template>
              <template v-else-if="column.key === 'remark'">
                <a-input v-model:value="record.remark" :placeholder="t('integration.paramConfig.remark')" />
              </template>
              <template v-else-if="column.key === 'required'">
                <a-switch :checked="record.required === 1" @change="checked => record.required = checked ? 1 : 0" />
              </template>
              <template v-else-if="column.key === 'action'">
                <a-space size="small">
                  <a @click="appendChildNode(record, 'RESPONSE')">{{ t('common.add') }}</a>
                  <a class="danger-link" @click="removeNode(responseTree, record)">{{ t('common.delete') }}</a>
                </a-space>
              </template>
            </template>
          </a-table>
        </section>
      </div>

      <section class="mapping-panel">
        <div class="panel-header">
          <div>
            <h3>{{ t('integration.mapping.title') }}</h3>
            <p>{{ t('integration.mapping.direction') }}</p>
          </div>
          <a-space wrap>
            <a-select v-model:value="mappingDirection" style="width: 180px" @change="reloadMappings">
              <a-select-option value="INBOUND">{{ t('integration.mapping.requestToResponse') }}</a-select-option>
              <a-select-option value="OUTBOUND">{{ t('integration.mapping.responseToRequest') }}</a-select-option>
            </a-select>
            <a-button @click="handleAddMapping">{{ t('integration.mapping.addMapping') }}</a-button>
            <a-button @click="handleAutoMatch">{{ t('integration.mapping.autoMatch') }}</a-button>
            <a-button type="primary" :loading="savingAll" @click="handleSaveAll">
              {{ t('common.save') }}
            </a-button>
          </a-space>
        </div>

        <a-alert
          v-if="warnings.length"
          type="warning"
          show-icon
          class="mapping-alert"
          :message="warnings.join(' / ')"
        />

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
    </a-spin>

    <a-modal
      v-model:open="importDialogOpen"
      :title="importDialogTitle"
      width="820px"
      :confirm-loading="parsingSource || importingSource"
      @ok="handleImportConfirm"
    >
      <a-form layout="vertical">
        <a-form-item :label="currentImportType === 'JAVA' ? t('integration.paramConfig.javaInputLabel') : t('integration.paramConfig.jsonInputLabel')">
          <a-textarea
            v-model:value="importText"
            :rows="12"
            :placeholder="currentImportType === 'JAVA' ? t('integration.paramConfig.javaInputPlaceholder') : t('integration.paramConfig.jsonInputPlaceholder')"
          />
        </a-form-item>
      </a-form>

      <a-space class="preview-toolbar">
        <a-button :loading="parsingSource" @click="handleParsePreview">
          {{ currentImportType === 'JAVA' ? t('integration.paramConfig.parseJava') : t('integration.common.parseJson') }}
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
  </component>
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
  parseApiParamJava,
  parseApiParamJson,
  replaceApiParamTree,
} from '@/api/system/integration'
import type {
  ApiConfigItem,
  ApiParamConfigItem,
  ApiParamMappingItem,
  IntegrationDirection,
  ParamDirection,
  ParamSourceType,
} from '@/api/system/integration'
import type { ParamMappingRow } from '../types'

interface Props {
  open: boolean
  apiConfig?: ApiConfigItem
  pageMode?: boolean
}

interface Emits {
  (e: 'update:open', value: boolean): void
}

const props = withDefaults(defineProps<Props>(), {
  pageMode: false,
})
const emit = defineEmits<Emits>()
const { t } = useI18n({ useScope: 'global' })

const loading = ref(false)
const savingAll = ref(false)
const parsingSource = ref(false)
const importingSource = ref(false)
const requestTree = ref<ApiParamConfigItem[]>([])
const responseTree = ref<ApiParamConfigItem[]>([])
const previewTree = ref<ApiParamConfigItem[]>([])
const importText = ref('')
const importDirection = ref<ParamDirection>('REQUEST')
const currentImportType = ref<ParamSourceType>('JSON')
const importDialogOpen = ref(false)
const mappingDirection = ref<IntegrationDirection>('INBOUND')
const mappingRows = ref<ParamMappingRow[]>([])
let localNodeSeed = 0

const visible = computed({
  get: () => props.open,
  set: (value: boolean) => emit('update:open', value),
})

const modalProps = computed(() => ({
  open: visible.value,
  title: `${t('integration.paramConfig.title')} - ${props.apiConfig?.apiName || props.apiConfig?.apiCode || ''}`,
  width: '1440px',
  footer: null,
  destroyOnClose: true,
  onCancel: handleClose,
}))

const treeColumns = computed(() => [
  { title: t('integration.paramConfig.field'), key: 'fieldName', width: 190 },
  { title: t('integration.paramConfig.fieldName'), key: 'fieldLabel', width: 180 },
  { title: t('integration.paramConfig.fieldType'), key: 'fieldType', width: 130 },
  { title: t('integration.paramConfig.remark'), key: 'remark' },
  { title: t('integration.paramConfig.required'), key: 'required', width: 100, align: 'center' },
  { title: t('common.action'), key: 'action', width: 120, align: 'center' },
])

const fieldTypeOptions = [
  { label: 'string', value: 'string' },
  { label: 'number', value: 'number' },
  { label: 'boolean', value: 'boolean' },
  { label: 'object', value: 'object' },
  { label: 'array', value: 'array' },
  { label: 'date', value: 'date' },
]

const mappingColumns = computed(() => [
  { title: t('integration.mapping.sourcePath'), key: 'sourceFieldPath', width: 260 },
  { title: t('integration.mapping.targetPath'), key: 'targetFieldPath', width: 260 },
  { title: `${t('integration.mapping.sourceType')} / ${t('integration.mapping.targetType')}`, key: 'type', width: 180 },
  { title: t('integration.mapping.remark'), key: 'remark' },
  { title: t('common.action'), key: 'action', width: 80 },
])

const importDialogTitle = computed(() => {
  const sideTitle = importDirection.value === 'REQUEST'
    ? t('integration.paramConfig.requestFields')
    : t('integration.paramConfig.responseFields')
  const sourceTitle = currentImportType.value === 'JAVA'
    ? t('integration.paramConfig.importJavaEntity')
    : t('integration.common.importJson')
  return `${sourceTitle} - ${sideTitle}`
})

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

const senderTitle = computed(() =>
  mappingDirection.value === 'INBOUND'
    ? t('integration.paramConfig.requestFields')
    : t('integration.paramConfig.responseFields')
)

const senderDesc = computed(() =>
  mappingDirection.value === 'INBOUND'
    ? t('integration.paramConfig.requestStructure')
    : t('integration.paramConfig.responseStructure')
)

const receiverTitle = computed(() =>
  mappingDirection.value === 'INBOUND'
    ? t('integration.paramConfig.responseFields')
    : t('integration.paramConfig.requestFields')
)

const receiverDesc = computed(() =>
  mappingDirection.value === 'INBOUND'
    ? t('integration.paramConfig.responseStructure')
    : t('integration.paramConfig.requestStructure')
)

watch(
  () => props.open,
  open => {
    if (open) {
      void reloadAll()
    }
  },
  { immediate: true },
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
    requestTree.value = attachLocalKeys(request || [], 'REQUEST')
    responseTree.value = attachLocalKeys(response || [], 'RESPONSE')
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
  syncAllMappingMeta()
}

function handleClose() {
  emit('update:open', false)
}

function openImportDialog(direction: ParamDirection, sourceType: ParamSourceType) {
  importDirection.value = direction
  currentImportType.value = sourceType
  importText.value = ''
  previewTree.value = []
  importDialogOpen.value = true
}

async function handleParsePreview() {
  parsingSource.value = true
  try {
    previewTree.value = currentImportType.value === 'JAVA'
      ? attachLocalKeys(await parseApiParamJava({ javaSource: importText.value }), importDirection.value)
      : attachLocalKeys(await parseApiParamJson({ jsonString: importText.value }), importDirection.value)
  } finally {
    parsingSource.value = false
  }
}

async function handleImportConfirm() {
  if (!props.apiConfig?.id) {
    return
  }

  const applyImport = async () => {
    importingSource.value = true
    try {
      if (currentImportType.value === 'JAVA') {
        const parsed = await parseApiParamJava({ javaSource: importText.value })
        applyImportedTree(importDirection.value, parsed || [])
      } else {
        await importApiParamJson({
          apiConfigId: props.apiConfig.id,
          direction: importDirection.value,
          jsonString: importText.value,
        })
        await reloadAll()
      }
      importDialogOpen.value = false
    } finally {
      importingSource.value = false
    }
  }

  const currentTree = importDirection.value === 'REQUEST' ? requestTree.value : responseTree.value
  if (currentTree.length) {
    Modal.confirm({
      title: t('common.tip'),
      content: t('integration.paramConfig.resetTreeConfirm'),
      okText: t('common.confirm'),
      cancelText: t('common.cancel'),
      onOk: applyImport,
    })
    return
  }

  await applyImport()
}

function applyImportedTree(direction: ParamDirection, tree: ApiParamConfigItem[]) {
  const normalized = attachLocalKeys(tree, direction)
  if (direction === 'REQUEST') {
    requestTree.value = normalized
  } else {
    responseTree.value = normalized
  }
  syncAllMappingMeta()
}

function appendRootNode(direction: ParamDirection) {
  const target = direction === 'REQUEST' ? requestTree.value : responseTree.value
  target.push(createNode(direction))
}

function appendChildNode(record: ApiParamConfigItem, direction: ParamDirection) {
  const child = createNode(direction)
  if (!record.children) {
    record.children = []
  }
  record.children.push(child)
  if (record.nodeType === 'FIELD') {
    record.nodeType = 'OBJECT'
    record.fieldType = 'object'
  }
}

function createNode(direction: ParamDirection): ApiParamConfigItem {
  localNodeSeed += 1
  return {
    id: undefined,
    apiConfigId: props.apiConfig?.id || 0,
    direction,
    nodeType: 'FIELD',
    fieldName: `field${localNodeSeed}`,
    fieldLabel: `field${localNodeSeed}`,
    fieldPath: `field${localNodeSeed}`,
    fieldType: 'string',
    required: 0,
    remark: '',
    children: [],
    __rowKey: `${direction}-local-${localNodeSeed}`,
  } as ApiParamConfigItem & { __rowKey: string }
}

function removeNode(tree: ApiParamConfigItem[], target: ApiParamConfigItem) {
  const removeRecursively = (items: ApiParamConfigItem[]) => {
    const index = items.findIndex(item => getRowKey(item) === getRowKey(target))
    if (index !== -1) {
      items.splice(index, 1)
      return true
    }
    return items.some(item => item.children?.length && removeRecursively(item.children))
  }
  removeRecursively(tree)
}

function getRowKey(record: ApiParamConfigItem) {
  return String((record as ApiParamConfigItem & { __rowKey?: string }).__rowKey || record.id || record.fieldPath || record.fieldName)
}

function attachLocalKeys(tree: ApiParamConfigItem[], direction: ParamDirection): ApiParamConfigItem[] {
  return (tree || []).map(item => {
    localNodeSeed += 1
    const normalizedItem = normalizeNode(item)
    return {
      ...normalizedItem,
      direction,
      __rowKey: `${direction}-${normalizedItem.id || normalizedItem.fieldPath || normalizedItem.fieldName}-${localNodeSeed}`,
      children: normalizedItem.children?.length ? attachLocalKeys(normalizedItem.children, direction) : [],
    } as ApiParamConfigItem & { __rowKey: string }
  })
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
    key: getRowKey(item),
    title: `${item.fieldLabel || item.fieldName || item.fieldPath} (${item.fieldType || item.nodeType})`,
    children: item.children?.length ? buildTreeData(item.children) : undefined,
  }))
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

function syncAllMappingMeta() {
  mappingRows.value.forEach((_, index) => syncMappingMeta(index))
}

async function handleSaveAll() {
  if (!props.apiConfig?.id) {
    return
  }

  savingAll.value = true
  try {
    await Promise.all([
      replaceApiParamTree({
        apiConfigId: props.apiConfig.id,
        direction: 'REQUEST',
        tree: normalizeTreeForSave(requestTree.value, props.apiConfig.id, 'REQUEST'),
      }),
      replaceApiParamTree({
        apiConfigId: props.apiConfig.id,
        direction: 'RESPONSE',
        tree: normalizeTreeForSave(responseTree.value, props.apiConfig.id, 'RESPONSE'),
      }),
    ])

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

    await reloadAll()
  } finally {
    savingAll.value = false
  }
}

function normalizeTreeForSave(tree: ApiParamConfigItem[], apiConfigId: number, direction: ParamDirection): ApiParamConfigItem[] {
  return tree.map(rawItem => {
    const item = normalizeNode(rawItem)
    return {
      id: item.id,
      apiConfigId,
      direction,
      parentId: null,
      nodeType: item.children?.length ? (item.nodeType === 'ARRAY' ? 'ARRAY' : 'OBJECT') : (item.nodeType || 'FIELD'),
      fieldName: item.fieldName,
      fieldLabel: item.fieldLabel,
      fieldPath: item.fieldPath || item.fieldName,
      fieldType: item.children?.length ? (item.nodeType === 'ARRAY' ? 'array' : 'object') : item.fieldType,
      required: item.required ?? 0,
      defaultValue: item.defaultValue,
      dictCode: item.dictCode,
      orderNum: item.orderNum,
      remark: item.remark,
      children: item.children?.length ? normalizeTreeForSave(item.children, apiConfigId, direction) : [],
    }
  })
}

function normalizeNode(item: ApiParamConfigItem): ApiParamConfigItem {
  const fieldName = (item.fieldName || item.fieldPath || '').trim()
  const fieldLabel = (item.fieldLabel || item.fieldName || item.fieldPath || '').trim()
  return {
    ...item,
    fieldName,
    fieldLabel,
    fieldPath: item.fieldPath || fieldName,
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

<style scoped lang="less">
.api-param-config-shell {
  min-height: 0;
}

.param-config-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(0, 1fr);
  gap: 16px;
  margin-bottom: 16px;
}

.param-panel,
.mapping-panel {
  padding: 16px;
  border: 1px solid var(--fx-border-color, #e5e7eb);
  border-radius: 18px;
  background: var(--fx-bg-container, #fff);
  overflow: auto;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: flex-start;
  margin-bottom: 14px;

  h3 {
    margin: 0 0 4px;
  }

  p {
    margin: 0;
    color: var(--fx-text-secondary, #64748b);
  }
}

.tree-table,
.mapping-table {
  margin-top: 8px;
}

.mapping-alert,
.preview-toolbar,
.preview-tree {
  margin-bottom: 12px;
}

.danger-link {
  color: #ff4d4f;
}

@media (max-width: 1280px) {
  .param-config-layout {
    grid-template-columns: 1fr;
  }
}
</style>
