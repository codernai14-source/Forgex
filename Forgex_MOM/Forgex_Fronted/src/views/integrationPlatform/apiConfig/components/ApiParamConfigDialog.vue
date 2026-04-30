<template>
  <component
    :is="pageMode ? 'div' : 'a-modal'"
    v-bind="pageMode ? {} : modalProps"
    class="api-param-config-shell"
  >
    <a-spin :spinning="loading">
      <div class="toolbar">
        <div class="toolbar__info">
          <h3>{{ props.apiConfig?.apiName || props.apiConfig?.apiCode || '-' }}</h3>
          <p>{{ treeModeLabel }}</p>
        </div>

        <a-space wrap>
          <a-select
            v-if="isOutbound && targetOptions.length"
            v-model:value="selectedTargetId"
            style="width: 260px"
            :placeholder="t('integration.paramConfig.selectTarget')"
            @change="reloadAll"
          >
            <a-select-option v-for="target in targetOptions" :key="target.id" :value="target.id">
              {{ target.targetName || target.targetCode || target.id }}
            </a-select-option>
          </a-select>
          <a-select v-model:value="mappingDirection" style="width: 180px" @change="reloadMappings">
            <a-select-option value="INBOUND">{{ t('integration.mapping.requestToResponse') }}</a-select-option>
            <a-select-option value="OUTBOUND">{{ t('integration.mapping.responseToRequest') }}</a-select-option>
          </a-select>
          <a-button @click="handleAutoMatch">{{ t('integration.mapping.autoMatch') }}</a-button>
          <a-button type="primary" :loading="savingAll" @click="handleSaveAll">{{ t('common.save') }}</a-button>
        </a-space>
      </div>

      <div class="param-config-layout">
        <section class="param-panel">
          <div class="panel-header">
            <div>
              <h3>{{ t('integration.paramConfig.requestFields') }}</h3>
              <p>{{ requestPanelDesc }}</p>
            </div>
            <a-space wrap>
              <a-button size="small" @click="appendChildNode(requestRoot, 'REQUEST')">{{ t('common.add') }}</a-button>
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
            :data-source="[requestRoot]"
            :row-key="getRowKey"
            :scroll="treeTableScroll"
            :indent-size="18"
            :children-column-name="'children'"
            class="tree-table"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'fieldName'">
                <span v-if="isRoot(record)" class="field-cell field-cell--root">
                  {{ record.fieldName }}
                </span>
                <a-input
                  v-else
                  v-model:value="record.fieldName"
                  :placeholder="t('integration.paramConfig.field')"
                  @blur="() => handleFieldNameBlur(record)"
                />
              </template>
              <template v-else-if="column.key === 'fieldLabel'">
                <a-input
                  v-model:value="record.fieldLabel"
                  :disabled="isRoot(record)"
                  :placeholder="t('integration.paramConfig.fieldName')"
                />
              </template>
              <template v-else-if="column.key === 'fieldType'">
                <a-select
                  v-model:value="record.fieldType"
                  :disabled="isRoot(record)"
                  :options="fieldTypeOptions"
                  style="width: 120px"
                />
              </template>
              <template v-else-if="column.key === 'required'">
                <a-switch
                  :checked="record.required === 1"
                  :disabled="isRoot(record)"
                  @change="checked => record.required = checked ? 1 : 0"
                />
              </template>
              <template v-else-if="column.key === 'remark'">
                <a-input
                  v-model:value="record.remark"
                  :disabled="isRoot(record)"
                  :placeholder="t('integration.paramConfig.remark')"
                />
              </template>
              <template v-else-if="column.key === 'action'">
                <a-space size="small">
                  <a @click="appendChildNode(record, 'REQUEST')">{{ t('common.add') }}</a>
                  <a v-if="!isRoot(record)" class="danger-link" @click="removeNode(requestRoot, record)">
                    {{ t('common.delete') }}
                  </a>
                </a-space>
              </template>
            </template>
          </a-table>
        </section>

        <section class="param-panel">
          <div class="panel-header">
            <div>
              <h3>{{ t('integration.paramConfig.responseFields') }}</h3>
              <p>{{ responsePanelDesc }}</p>
            </div>
            <a-space wrap>
              <a-button size="small" @click="appendChildNode(responseRoot, 'RESPONSE')">{{ t('common.add') }}</a-button>
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
            :data-source="[responseRoot]"
            :row-key="getRowKey"
            :scroll="treeTableScroll"
            :indent-size="18"
            :children-column-name="'children'"
            class="tree-table"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'fieldName'">
                <span v-if="isRoot(record)" class="field-cell field-cell--root">
                  {{ record.fieldName }}
                </span>
                <a-input
                  v-else
                  v-model:value="record.fieldName"
                  :placeholder="t('integration.paramConfig.field')"
                  @blur="() => handleFieldNameBlur(record)"
                />
              </template>
              <template v-else-if="column.key === 'fieldLabel'">
                <a-input
                  v-model:value="record.fieldLabel"
                  :disabled="isRoot(record)"
                  :placeholder="t('integration.paramConfig.fieldName')"
                />
              </template>
              <template v-else-if="column.key === 'fieldType'">
                <a-select
                  v-model:value="record.fieldType"
                  :disabled="isRoot(record)"
                  :options="fieldTypeOptions"
                  style="width: 120px"
                />
              </template>
              <template v-else-if="column.key === 'required'">
                <a-switch
                  :checked="record.required === 1"
                  :disabled="isRoot(record)"
                  @change="checked => record.required = checked ? 1 : 0"
                />
              </template>
              <template v-else-if="column.key === 'remark'">
                <a-input
                  v-model:value="record.remark"
                  :disabled="isRoot(record)"
                  :placeholder="t('integration.paramConfig.remark')"
                />
              </template>
              <template v-else-if="column.key === 'action'">
                <a-space size="small">
                  <a @click="appendChildNode(record, 'RESPONSE')">{{ t('common.add') }}</a>
                  <a v-if="!isRoot(record)" class="danger-link" @click="removeNode(responseRoot, record)">
                    {{ t('common.delete') }}
                  </a>
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
          <a-button @click="handleAddMapping">{{ t('integration.mapping.addMapping') }}</a-button>
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
          :scroll="mappingTableScroll"
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
import { message, Modal } from 'ant-design-vue'
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

type TreeNode = ApiParamConfigItem & { __rowKey: string }

const props = withDefaults(defineProps<Props>(), {
  pageMode: false,
})
const emit = defineEmits<Emits>()
const { t } = useI18n({ useScope: 'global' })

const selectedTargetId = ref<number | null>(null)
let localNodeSeed = 0

const isOutbound = computed(() => props.apiConfig?.direction === 'OUTBOUND')
const targetOptions = computed(() => props.apiConfig?.outboundTargets || [])
const activeOutboundTargetId = computed(() => (isOutbound.value ? selectedTargetId.value : null))

const loading = ref(false)
const savingAll = ref(false)
const parsingSource = ref(false)
const importingSource = ref(false)
const requestRoot = ref<TreeNode>(createRootNode('REQUEST'))
const responseRoot = ref<TreeNode>(createRootNode('RESPONSE'))
const previewTree = ref<ApiParamConfigItem[]>([])
const importText = ref('')
const importDirection = ref<ParamDirection>('REQUEST')
const currentImportType = ref<ParamSourceType>('JSON')
const importDialogOpen = ref(false)
const mappingDirection = ref<IntegrationDirection>('INBOUND')
const mappingRows = ref<ParamMappingRow[]>([])

const visible = computed({
  get: () => props.open,
  set: (value: boolean) => emit('update:open', value),
})

const treeModeLabel = computed(() =>
  isOutbound.value ? t('integration.paramConfig.outboundTargetMode') : t('integration.paramConfig.inboundMode'),
)
const requestPanelDesc = computed(() =>
  isOutbound.value ? t('integration.paramConfig.outboundRequestDesc') : t('integration.paramConfig.inboundRequestDesc'),
)
const responsePanelDesc = computed(() =>
  isOutbound.value ? t('integration.paramConfig.outboundResponseDesc') : t('integration.paramConfig.inboundResponseDesc'),
)

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
  { title: t('integration.paramConfig.required'), key: 'required', width: 100, align: 'center' },
  { title: t('integration.paramConfig.remark'), key: 'remark', width: 220 },
  { title: t('common.action'), key: 'action', width: 120, align: 'center' },
])

const treeTableScroll = { x: 940 }

const fieldTypeOptions = [
  { label: 'object', value: 'object' },
  { label: 'array', value: 'array' },
  { label: 'string', value: 'string' },
  { label: 'number', value: 'number' },
  { label: 'boolean', value: 'boolean' },
  { label: 'date', value: 'date' },
]

const mappingColumns = computed(() => [
  { title: t('integration.mapping.sourcePath'), key: 'sourceFieldPath', width: 280 },
  { title: t('integration.mapping.targetPath'), key: 'targetFieldPath', width: 280 },
  { title: `${t('integration.mapping.sourceType')} / ${t('integration.mapping.targetType')}`, key: 'type', width: 170 },
  { title: t('integration.mapping.remark'), key: 'remark', width: 360 },
  { title: t('common.action'), key: 'action', width: 90 },
])

const mappingTableScroll = { x: 1180, y: 220 }

const importDialogTitle = computed(() => {
  const sideTitle = importDirection.value === 'REQUEST'
    ? t('integration.paramConfig.requestFields')
    : t('integration.paramConfig.responseFields')
  const sourceTitle = currentImportType.value === 'JAVA'
    ? t('integration.paramConfig.importJavaEntity')
    : t('integration.common.importJson')
  return `${sourceTitle} - ${sideTitle}`
})

const flatRequestFields = computed(() => flattenFields(requestRoot.value))
const flatResponseFields = computed(() => flattenFields(responseRoot.value))

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
  open => {
    if (!open) {
      return
    }
    if (isOutbound.value && !selectedTargetId.value) {
      selectedTargetId.value = targetOptions.value[0]?.id ?? null
    }
    void reloadAll()
  },
  { immediate: true },
)

watch(
  () => props.apiConfig?.id,
  () => {
    if (isOutbound.value) {
      selectedTargetId.value = targetOptions.value[0]?.id ?? null
    } else {
      selectedTargetId.value = null
    }
  },
  { immediate: true },
)

async function reloadAll() {
  if (!props.apiConfig?.id) {
    return
  }
  if (isOutbound.value && !activeOutboundTargetId.value) {
    requestRoot.value = createRootNode('REQUEST')
    responseRoot.value = createRootNode('RESPONSE')
    mappingRows.value = []
    return
  }

  loading.value = true
  try {
    const [request, response] = await Promise.all([
      getApiParamTree(props.apiConfig.id, 'REQUEST', activeOutboundTargetId.value),
      getApiParamTree(props.apiConfig.id, 'RESPONSE', activeOutboundTargetId.value),
    ])
    requestRoot.value = ensureRoot(request || [], 'REQUEST')
    responseRoot.value = ensureRoot(response || [], 'RESPONSE')
    await reloadMappings()
  } finally {
    loading.value = false
  }
}

async function reloadMappings() {
  if (!props.apiConfig?.id) {
    return
  }
  if (isOutbound.value && !activeOutboundTargetId.value) {
    mappingRows.value = []
    return
  }
  const rows = await getApiParamMappings({
    apiConfigId: props.apiConfig.id,
    outboundTargetId: activeOutboundTargetId.value,
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
    const parsed = currentImportType.value === 'JAVA'
      ? await parseApiParamJava({ javaSource: importText.value })
      : await parseApiParamJson({ jsonString: importText.value })
    previewTree.value = (parsed || []).length ? parsed : []
  } finally {
    parsingSource.value = false
  }
}

async function handleImportConfirm() {
  if (!props.apiConfig?.id) {
    return
  }
  if (isOutbound.value && !activeOutboundTargetId.value) {
    message.warning(t('integration.paramConfig.selectTarget'))
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
          outboundTargetId: activeOutboundTargetId.value,
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

  const currentRoot = importDirection.value === 'REQUEST' ? requestRoot.value : responseRoot.value
  if (currentRoot.children && currentRoot.children.length) {
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
  const normalized = ensureRoot(tree, direction)
  if (direction === 'REQUEST') {
    requestRoot.value = normalized
  } else {
    responseRoot.value = normalized
  }
  syncAllMappingMeta()
}

function appendChildNode(record: TreeNode, direction: ParamDirection) {
  const child = createNode(direction)
  if (!record.children) {
    record.children = []
  }
  record.children.push(child)
  if (record.nodeType === 'FIELD') {
    record.nodeType = 'OBJECT'
    record.fieldType = 'object'
  }
  syncTreePaths(direction === 'REQUEST' ? requestRoot.value : responseRoot.value, 'root')
  syncAllMappingMeta()
}

function createRootNode(direction: ParamDirection): TreeNode {
  localNodeSeed += 1
  return {
    id: undefined,
    apiConfigId: props.apiConfig?.id || 0,
    outboundTargetId: activeOutboundTargetId.value,
    parentId: null,
    direction,
    nodeType: 'OBJECT',
    fieldName: 'root',
    fieldLabel: 'root',
    fieldType: 'object',
    fieldPath: 'root',
    required: 0,
    remark: '',
    children: [],
    __rowKey: `${direction}-root-${localNodeSeed}`,
  }
}

function createNode(direction: ParamDirection): TreeNode {
  localNodeSeed += 1
  return {
    id: undefined,
    apiConfigId: props.apiConfig?.id || 0,
    outboundTargetId: activeOutboundTargetId.value,
    parentId: null,
    direction,
    nodeType: 'FIELD',
    fieldName: `field${localNodeSeed}`,
    fieldLabel: `field${localNodeSeed}`,
    fieldPath: `root.field${localNodeSeed}`,
    fieldType: 'string',
    required: 0,
    remark: '',
    children: [],
    __rowKey: `${direction}-local-${localNodeSeed}`,
  }
}

function ensureRoot(tree: ApiParamConfigItem[], direction: ParamDirection): TreeNode {
  const first = tree?.[0]
  if (first && first.fieldName === 'root') {
    const rootNode = attachNode(first, direction, 'root')
    syncTreePaths(rootNode, 'root')
    return rootNode
  }

  const root = createRootNode(direction)
  root.children = (tree || []).map(item => attachNode(item, direction, `root.${item.fieldName || 'field'}`))
  syncTreePaths(root, 'root')
  return root
}

function attachNode(node: ApiParamConfigItem, direction: ParamDirection, fieldPath: string): TreeNode {
  localNodeSeed += 1
  const normalizedFieldName = (node.fieldName || fieldPath.split('.').pop() || 'field').trim()
  const nextFieldPath = normalizedFieldName === 'root' ? 'root' : fieldPath
  const children = (node.children || []).map(child => {
    const childName = (child.fieldName || child.fieldPath?.split('.').pop() || 'field').trim()
    return attachNode(child, direction, `${nextFieldPath}.${childName}`)
  })
  return {
    ...node,
    apiConfigId: props.apiConfig?.id || node.apiConfigId || 0,
    outboundTargetId: activeOutboundTargetId.value,
    direction,
    fieldName: normalizedFieldName,
    fieldLabel: node.fieldLabel || normalizedFieldName,
    fieldPath: node.fieldName === 'root' ? 'root' : nextFieldPath,
    __rowKey: `${direction}-${node.id || nextFieldPath}-${localNodeSeed}`,
    children,
  }
}

function isRoot(node: ApiParamConfigItem) {
  return node.fieldName === 'root' && node.fieldPath === 'root'
}

function handleFieldNameBlur(node: TreeNode) {
  if (isRoot(node)) {
    return
  }
  const oldPath = node.fieldPath
  const nextFieldName = sanitizeFieldName(node.fieldName, 'field')
  node.fieldName = nextFieldName
  if (!node.fieldLabel?.trim()) {
    node.fieldLabel = nextFieldName
  }
  const currentRoot = node.direction === 'REQUEST' ? requestRoot.value : responseRoot.value
  syncTreePaths(currentRoot, 'root')
  remapMappingPaths(oldPath, node.fieldPath)
  syncAllMappingMeta()
}

function removeNode(root: TreeNode, target: ApiParamConfigItem) {
  const removeRecursively = (items: ApiParamConfigItem[]) => {
    const index = items.findIndex(item => getRowKey(item) === getRowKey(target))
    if (index !== -1) {
      items.splice(index, 1)
      return true
    }
    return items.some(item => item.children?.length && removeRecursively(item.children))
  }
  removeRecursively(root.children || [])
}

function getRowKey(record: ApiParamConfigItem) {
  return String((record as TreeNode).__rowKey || record.id || record.fieldPath || record.fieldName)
}

function sanitizeFieldName(value?: string, fallback = 'field') {
  const normalized = (value || '').trim().replace(/\s+/g, '_').replace(/\./g, '_')
  return normalized || fallback
}

function syncTreePaths(node: TreeNode, fieldPath: string) {
  node.apiConfigId = props.apiConfig?.id || node.apiConfigId || 0
  node.outboundTargetId = activeOutboundTargetId.value
  node.fieldName = isRoot(node) ? 'root' : sanitizeFieldName(node.fieldName, 'field')
  node.fieldPath = isRoot(node) ? 'root' : fieldPath

  ;(node.children || []).forEach((child, index) => {
    const childNode = child as TreeNode
    childNode.parentId = node.id ?? null
    childNode.direction = node.direction
    childNode.apiConfigId = props.apiConfig?.id || childNode.apiConfigId || 0
    childNode.outboundTargetId = activeOutboundTargetId.value
    childNode.fieldName = sanitizeFieldName(childNode.fieldName, `field${index + 1}`)
    if (!childNode.fieldLabel?.trim()) {
      childNode.fieldLabel = childNode.fieldName
    }
    syncTreePaths(childNode, `${node.fieldPath}.${childNode.fieldName}`)
  })
}

function remapMappingPaths(oldPath?: string, newPath?: string) {
  if (!oldPath || !newPath || oldPath === newPath) {
    return
  }

  const replacePath = (fieldPath?: string) => {
    if (!fieldPath) {
      return fieldPath
    }
    if (fieldPath === oldPath) {
      return newPath
    }
    const prefix = `${oldPath}.`
    return fieldPath.startsWith(prefix) ? `${newPath}${fieldPath.slice(oldPath.length)}` : fieldPath
  }

  mappingRows.value = mappingRows.value.map(row => ({
    ...row,
    sourceFieldPath: replacePath(row.sourceFieldPath) || '',
    targetFieldPath: replacePath(row.targetFieldPath) || '',
  }))
}

function flattenFields(root: TreeNode): ApiParamConfigItem[] {
  const fields: ApiParamConfigItem[] = []
  const walk = (items: ApiParamConfigItem[]) => {
    items.forEach(item => {
      if (!isRoot(item) && isMappableNode(item)) {
        fields.push(item)
      }
      if (item.children?.length) {
        walk(item.children)
      }
    })
  }
  walk(root.children || [])
  return fields
}

function buildTreeData(tree: ApiParamConfigItem[]) {
  return tree.map(item => ({
    key: `${item.fieldPath}-${item.fieldName}`,
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
  const existingSources = new Set(mappingRows.value.map(row => row.sourceFieldPath).filter(Boolean))
  const existingTargets = new Set(mappingRows.value.map(row => row.targetFieldPath).filter(Boolean))
  const suggestions: ParamMappingRow[] = []

  sourceFields.forEach(source => {
    if (existingSources.has(source.fieldPath)) {
      return
    }
    const sourceName = source.fieldPath.split('.').pop()
    const target = targetFields.find(item =>
      item.fieldPath.split('.').pop() === sourceName && !existingTargets.has(item.fieldPath),
    )
    if (target) {
      existingSources.add(source.fieldPath)
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

  if (!suggestions.length) {
    message.info(t('integration.mapping.autoMatchNoResult'))
    return
  }
  mappingRows.value = [...mappingRows.value, ...suggestions]
  syncAllMappingMeta()
  message.success(t('integration.mapping.autoMatchApplied', { count: suggestions.length }))
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
  if (isOutbound.value && !activeOutboundTargetId.value) {
    message.warning(t('integration.paramConfig.selectTarget'))
    return
  }

  savingAll.value = true
  try {
    await Promise.all([
      replaceApiParamTree({
        apiConfigId: props.apiConfig.id,
        outboundTargetId: activeOutboundTargetId.value,
        direction: 'REQUEST',
        tree: [normalizeRootForSave(requestRoot.value)],
      }),
      replaceApiParamTree({
        apiConfigId: props.apiConfig.id,
        outboundTargetId: activeOutboundTargetId.value,
        direction: 'RESPONSE',
        tree: [normalizeRootForSave(responseRoot.value)],
      }),
    ])

    await batchSaveApiParamMappings({
      apiConfigId: props.apiConfig.id,
      outboundTargetId: activeOutboundTargetId.value,
      direction: mappingDirection.value,
      mappings: mappingRows.value
        .filter(row => row.sourceFieldPath && row.targetFieldPath)
        .map(row => ({
          apiConfigId: props.apiConfig!.id!,
          outboundTargetId: activeOutboundTargetId.value,
          direction: mappingDirection.value,
          sourceFieldPath: row.sourceFieldPath,
          targetFieldPath: row.targetFieldPath,
          remark: row.remark,
        })),
    })

    await reloadAll()
    message.success(t('common.saveSuccess'))
  } finally {
    savingAll.value = false
  }
}

function normalizeRootForSave(root: TreeNode): ApiParamConfigItem {
  return normalizeNodeForSave(root, null, root.direction, 'root')
}

function normalizeNodeForSave(node: ApiParamConfigItem, parentId: number | null, direction: ParamDirection, fieldPath: string): ApiParamConfigItem {
  const normalizedChildren = (node.children || []).map(child => {
    const childFieldName = sanitizeFieldName(child.fieldName || child.fieldPath.split('.').pop() || 'field', 'field')
    return normalizeNodeForSave(child, child.id ?? null, direction, `${fieldPath}.${childFieldName}`)
  })

  const normalizedFieldName = sanitizeFieldName(node.fieldName, 'field')

  return {
    id: node.id,
    apiConfigId: props.apiConfig?.id || 0,
    outboundTargetId: activeOutboundTargetId.value,
    parentId,
    direction,
    nodeType: isRoot(node) ? 'OBJECT' : normalizedChildren.length ? (node.nodeType === 'ARRAY' ? 'ARRAY' : 'OBJECT') : 'FIELD',
    fieldName: isRoot(node) ? 'root' : normalizedFieldName,
    fieldLabel: isRoot(node) ? 'root' : node.fieldLabel?.trim() || normalizedFieldName,
    fieldPath: isRoot(node) ? 'root' : fieldPath,
    fieldType: isRoot(node) ? 'object' : normalizedChildren.length ? (node.nodeType === 'ARRAY' ? 'array' : 'object') : node.fieldType,
    required: isRoot(node) ? 0 : node.required ?? 0,
    defaultValue: node.defaultValue,
    dictCode: node.dictCode,
    orderNum: node.orderNum,
    remark: node.remark,
    children: normalizedChildren,
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

function isMappableNode(node: ApiParamConfigItem) {
  if (node.nodeType === 'ARRAY') {
    return true
  }
  if (node.nodeType === 'FIELD') {
    return true
  }
  return !node.children?.length
}
</script>

<style scoped lang="less">
.api-param-config-shell {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
  overflow-x: hidden;
  overflow-y: auto;
  color: var(--fx-text-primary, #111827);
}

:deep(.ant-spin-nested-loading),
:deep(.ant-spin-container) {
  display: flex;
  flex: 1;
  min-height: 0;
  flex-direction: column;
}

.toolbar {
  flex-shrink: 0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;
  padding: 16px;
  border: 1px solid var(--fx-border-color, #e5e7eb);
  border-radius: 18px;
  background: var(--fx-bg-container, #ffffff);
}

.toolbar__info {
  h3 {
    margin: 0 0 4px;
    color: var(--fx-text-primary, #111827);
  }

  p {
    margin: 0;
    color: var(--fx-text-secondary, #64748b);
  }
}

.param-config-layout {
  flex: 1 1 340px;
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(0, 1fr);
  min-height: 300px;
  gap: 16px;
  margin-bottom: 16px;
}

.param-panel,
.mapping-panel {
  display: flex;
  flex-direction: column;
  min-height: 0;
  padding: 16px;
  border: 1px solid var(--fx-border-color, #e5e7eb);
  border-radius: 18px;
  background: var(--fx-bg-container, #ffffff);
  overflow: hidden;
}

.param-panel {
  max-height: 100%;
}

.mapping-panel {
  flex: 0 0 330px;
  min-height: 280px;
  max-height: 38vh;
  overflow-x: hidden;
  overflow-y: auto;
}

.panel-header {
  flex-shrink: 0;
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: flex-start;
  margin-bottom: 14px;

  h3 {
    margin: 0 0 4px;
    color: var(--fx-text-primary, #111827);
  }

  p {
    margin: 0;
    color: var(--fx-text-secondary, #64748b);
  }
}

.field-cell {
  display: inline-flex;
  align-items: center;
  min-height: 32px;
}

.field-cell--root {
  font-weight: 600;
  color: var(--fx-text-primary, #111827);
}

.mapping-alert,
.preview-toolbar,
.preview-tree {
  margin-bottom: 12px;
}

.danger-link {
  color: #ff4d4f;
}

.tree-table {
  flex: 1 1 auto;
  min-width: 0;
  min-height: 0;
  overflow: auto;
}

.mapping-table {
  flex: 1 1 auto;
  min-width: 0;
  min-height: 0;
  overflow: auto;
}

.tree-table :deep(.ant-spin-nested-loading),
.tree-table :deep(.ant-spin-container),
.mapping-table :deep(.ant-spin-nested-loading),
.mapping-table :deep(.ant-spin-container) {
  height: 100%;
  min-height: 0;
}

.tree-table :deep(.ant-table-content) {
  max-height: 100%;
  overflow: auto !important;
}

.mapping-table :deep(.ant-table-content) {
  width: 100%;
  overflow-x: auto !important;
}

.mapping-table :deep(.ant-table-body) {
  overflow: auto !important;
}

.mapping-table :deep(.ant-table table) {
  min-width: 1180px;
  table-layout: fixed !important;
}

:deep(.ant-table-wrapper),
:deep(.ant-table),
:deep(.ant-table-container),
:deep(.ant-table-content),
:deep(.ant-table-body) {
  background: transparent !important;
  color: var(--fx-text-primary, #111827) !important;
}

:deep(.ant-table-thead > tr > th) {
  background: var(--fx-bg-elevated, var(--fx-fill-secondary, #f8fafc)) !important;
  color: var(--fx-text-primary, #111827) !important;
  border-bottom: 1px solid var(--fx-border-color, #e5e7eb) !important;
}

:deep(.ant-table-tbody > tr > td) {
  background: transparent !important;
  color: var(--fx-text-primary, #111827) !important;
  border-bottom: 1px solid var(--fx-border-color, #e5e7eb) !important;
}

:deep(.ant-table-tbody > tr.ant-table-row:hover > td) {
  background: color-mix(in srgb, var(--fx-primary, #1677ff) 6%, var(--fx-bg-container, #ffffff)) !important;
}

:deep(.ant-input),
:deep(.ant-input-affix-wrapper),
:deep(.ant-input-textarea textarea),
:deep(.ant-select-selector) {
  background: var(--fx-bg-container, #ffffff) !important;
  color: var(--fx-text-primary, #111827) !important;
  border-color: var(--fx-border-color, #e5e7eb) !important;
}

:deep(.ant-input::placeholder),
:deep(.ant-input-textarea textarea::placeholder),
:deep(.ant-select-selection-placeholder) {
  color: var(--fx-text-tertiary, #9ca3af) !important;
}

:deep(.ant-select-selection-item),
:deep(.ant-select-arrow),
:deep(.ant-empty-description),
:deep(.ant-alert-message),
:deep(.ant-tree),
:deep(.ant-tree-node-content-wrapper) {
  color: var(--fx-text-primary, #111827) !important;
}

:deep(.ant-alert) {
  background: color-mix(in srgb, var(--fx-warning, #faad14) 10%, var(--fx-bg-container, #ffffff)) !important;
  border-color: color-mix(in srgb, var(--fx-warning, #faad14) 25%, var(--fx-border-color, #e5e7eb)) !important;
}

@media (max-width: 1280px) {
  .toolbar,
  .param-config-layout {
    grid-template-columns: 1fr;
    flex-direction: column;
    align-items: stretch;
  }
}
</style>
