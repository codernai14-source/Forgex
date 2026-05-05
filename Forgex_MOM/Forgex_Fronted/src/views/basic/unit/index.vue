<template>
  <div class="unit-page">
    <section class="unit-type-panel">
      <div class="unit-type-toolbar">
        <div>
          <h2>{{ t('basic.unit.typeTree') }}</h2>
          <span>{{ t('basic.unit.typeTreeSub') }}</span>
        </div>
        <a-space>
          <a-button type="text" @click="loadTypeTree">
            <ReloadOutlined />
          </a-button>
          <a-button v-permission="'basic:unit:add'" type="primary" @click="openTypeCreate">
            <PlusOutlined />
          </a-button>
        </a-space>
      </div>

      <a-tree
        v-if="treeData.length"
        v-model:selectedKeys="selectedTypeKeys"
        :tree-data="treeData"
        :field-names="{ title: 'title', key: 'key', children: 'children' }"
        block-node
        default-expand-all
        @select="handleTypeSelect"
      >
        <template #title="{ dataRef }">
          <div class="type-tree-node">
            <span>{{ dataRef.title }}</span>
            <a-dropdown :trigger="['click']">
              <MoreOutlined class="type-node-more" @click.stop />
              <template #overlay>
                <a-menu @click="info => handleTypeMenu(String(info.key), dataRef.raw)">
                  <a-menu-item key="add">{{ t('common.add') }}</a-menu-item>
                  <a-menu-item key="edit">{{ t('common.edit') }}</a-menu-item>
                  <a-menu-item key="delete" class="danger-menu-item">{{ t('common.delete') }}</a-menu-item>
                </a-menu>
              </template>
            </a-dropdown>
          </div>
        </template>
      </a-tree>
      <a-empty v-else :description="t('common.noData')" />
    </section>

    <section class="unit-main-panel">
      <div class="unit-main-header">
        <div>
          <a-tag color="blue">Master Data</a-tag>
          <h1>{{ t('basic.unit.title') }}</h1>
          <p>{{ selectedTypeName || t('basic.unit.selectTypeHint') }}</p>
        </div>
        <a-space wrap>
          <a-button @click="loadTypeTree">
            <ReloadOutlined />
            {{ t('common.refresh') }}
          </a-button>
          <a-button v-permission="'basic:unit:add'" type="primary" :disabled="!selectedTypeId" @click="openUnitCreate">
            <PlusOutlined />
            {{ t('basic.unit.addUnit') }}
          </a-button>
        </a-space>
      </div>

      <FxDynamicTable
        ref="tableRef"
        table-code="BasicUnitTable"
        :request="handleRequest"
        :fallback-config="tableFallbackConfig"
        row-key="id"
      >
        <template #action="{ record }">
          <a-space>
            <a v-permission="'basic:unit:edit'" @click="openUnitEdit(record)">{{ t('common.edit') }}</a>
            <a v-permission="'basic:unit:edit'" @click="openConversion(record)">{{ t('basic.unit.conversionConfig') }}</a>
            <a v-permission="'basic:unit:delete'" class="danger-link" @click="handleUnitDelete(record)">{{ t('common.delete') }}</a>
          </a-space>
        </template>
      </FxDynamicTable>
    </section>

    <BaseFormDialog
      v-model:open="typeDialogVisible"
      :title="typeForm.id ? t('basic.unit.editType') : t('basic.unit.addType')"
      width="560px"
      :loading="saving"
      @submit="saveType"
    >
      <a-form layout="vertical" :model="typeForm">
        <a-form-item :label="t('basic.unit.parentType')">
          <a-tree-select
            v-model:value="typeForm.parentId"
            :tree-data="typeSelectData"
            allow-clear
            tree-default-expand-all
            :placeholder="t('basic.unit.rootType')"
          />
        </a-form-item>
        <a-form-item :label="t('basic.unit.typeCode')" required>
          <a-input v-model:value="typeForm.unitTypeCode" />
        </a-form-item>
        <a-form-item :label="t('basic.unit.typeName')" required>
          <a-input v-model:value="typeForm.unitTypeName" />
        </a-form-item>
      </a-form>
    </BaseFormDialog>

    <BaseFormDialog
      v-model:open="unitDialogVisible"
      :title="unitForm.id ? t('basic.unit.editUnit') : t('basic.unit.addUnit')"
      width="640px"
      :loading="saving"
      @submit="saveUnit"
    >
      <a-form layout="vertical" :model="unitForm">
        <a-form-item :label="t('basic.unit.typeName')" required>
          <a-tree-select
            v-model:value="unitForm.unitTypeId"
            :tree-data="typeSelectData"
            tree-default-expand-all
            :placeholder="t('basic.unit.selectType')"
          />
        </a-form-item>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item :label="t('basic.unit.unitCode')" required>
              <a-input v-model:value="unitForm.unitCode" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item :label="t('basic.unit.unitName')" required>
              <a-input v-model:value="unitForm.unitName" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item :label="t('common.remark')">
          <a-textarea v-model:value="unitForm.remark" :rows="3" />
        </a-form-item>
      </a-form>
    </BaseFormDialog>

    <a-modal
      v-model:open="conversionVisible"
      :title="conversionTitle"
      width="760px"
      :confirm-loading="saving"
      @ok="saveConversions"
    >
      <div class="conversion-toolbar">
        <span>{{ t('basic.unit.conversionFormula', { unit: currentUnit?.unitName || '-' }) }}</span>
        <a-button type="dashed" @click="addConversionRow">
          <PlusOutlined />
          {{ t('basic.unit.addConversion') }}
        </a-button>
      </div>
      <a-table
        :data-source="conversionRows"
        :pagination="false"
        row-key="rowKey"
        size="small"
      >
        <a-table-column :title="t('basic.unit.targetUnit')" data-index="targetUnitId">
          <template #default="{ record }">
            <a-select
              v-model:value="record.targetUnitId"
              :options="targetUnitOptions"
              show-search
              :filter-option="filterUnitOption"
              class="full-width"
            />
          </template>
        </a-table-column>
        <a-table-column :title="t('basic.unit.conversionValue')" data-index="conversionValue" width="220">
          <template #default="{ record }">
            <a-input-number
              v-model:value="record.conversionValue"
              :min="0"
              :precision="12"
              class="full-width"
            />
          </template>
        </a-table-column>
        <a-table-column :title="t('common.action')" width="90">
          <template #default="{ index }">
            <a class="danger-link" @click="removeConversionRow(index)">{{ t('common.remove') }}</a>
          </template>
        </a-table-column>
      </a-table>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { message, Modal } from 'ant-design-vue'
import { MoreOutlined, PlusOutlined, ReloadOutlined } from '@ant-design/icons-vue'
import BaseFormDialog from '@/components/common/BaseFormDialog.vue'
import FxDynamicTable from '@/components/common/FxDynamicTable.vue'
import { type UnitConversion, type UnitMaster, type UnitTypeNode, unitApi } from '@/api/basic/unit'

type TreeNode = {
  key: number
  value: number
  title: string
  raw: UnitTypeNode
  children?: TreeNode[]
}

type ConversionRow = UnitConversion & {
  rowKey: string
}

const ROOT_PARENT_ID = 0

const { t } = useI18n()
const tableRef = ref()
const saving = ref(false)
const typeTree = ref<UnitTypeNode[]>([])
const treeData = ref<TreeNode[]>([])
const selectedTypeKeys = ref<number[]>([])
const selectedTypeId = ref<number>()
const selectedTypeName = ref('')
const typeDialogVisible = ref(false)
const unitDialogVisible = ref(false)
const conversionVisible = ref(false)
const currentUnit = ref<UnitMaster>()
const typeForm = reactive<UnitTypeNode>({
  id: undefined,
  unitTypeCode: '',
  unitTypeName: '',
  parentId: ROOT_PARENT_ID,
})
const unitForm = reactive<UnitMaster>({
  id: undefined,
  unitTypeId: undefined,
  unitCode: '',
  unitName: '',
  remark: '',
})
const conversionRows = ref<ConversionRow[]>([])
const unitOptions = ref<UnitMaster[]>([])

const tableFallbackConfig = {
  tableCode: 'BasicUnitTable',
  tableName: '计量单位主数据',
  tableType: 'BUSINESS',
  rowKey: 'id',
  defaultPageSize: 10,
  version: 1,
  columns: [
    { field: 'unitCode', title: t('basic.unit.unitCode'), width: 140, visible: true, order: 1, queryable: true, queryType: 'input', queryOperator: 'like' },
    { field: 'unitName', title: t('basic.unit.unitName'), width: 160, visible: true, order: 2, queryable: true, queryType: 'input', queryOperator: 'like' },
    { field: 'unitTypeName', title: t('basic.unit.typeName'), width: 160, visible: true, order: 3 },
    { field: 'remark', title: t('common.remark'), width: 220, visible: true, order: 4, ellipsis: true },
    { field: 'createTime', title: t('common.createTime'), width: 180, visible: true, order: 5 },
    { field: 'action', title: t('common.action'), width: 220, fixed: 'right', visible: true, order: 99 },
  ],
  queryFields: [
    { field: 'unitCode', label: t('basic.unit.unitCode'), queryType: 'input', queryOperator: 'like' },
    { field: 'unitName', label: t('basic.unit.unitName'), queryType: 'input', queryOperator: 'like' },
  ],
}

const typeSelectData = computed(() => [
  {
    title: t('basic.unit.rootType'),
    value: ROOT_PARENT_ID,
    key: ROOT_PARENT_ID,
    children: treeData.value,
  },
])

const conversionTitle = computed(() => (
  currentUnit.value
    ? t('basic.unit.conversionTitle', { unit: currentUnit.value.unitName })
    : t('basic.unit.conversionConfig')
))

const targetUnitOptions = computed(() => unitOptions.value
  .filter(item => item.id !== currentUnit.value?.id && item.unitTypeId === currentUnit.value?.unitTypeId)
  .map(item => ({
    label: `${item.unitName} (${item.unitCode})`,
    value: item.id,
  })))

function toTreeNodes(nodes: UnitTypeNode[]): TreeNode[] {
  return (nodes || []).map(node => ({
    key: node.id ?? ROOT_PARENT_ID,
    value: node.id ?? ROOT_PARENT_ID,
    title: `${node.unitTypeName} (${node.unitTypeCode})`,
    raw: node,
    children: node.children?.length ? toTreeNodes(node.children) : undefined,
  }))
}

function flattenTypes(nodes: UnitTypeNode[], out: UnitTypeNode[] = []) {
  nodes.forEach(node => {
    out.push(node)
    if (node.children?.length) {
      flattenTypes(node.children, out)
    }
  })
  return out
}

async function loadTypeTree() {
  const data = await unitApi.typeTree()
  typeTree.value = data || []
  treeData.value = toTreeNodes(typeTree.value)
  const flatTypes = flattenTypes(typeTree.value)
  const firstType = flatTypes.find(item => item.id != null)
  if (!selectedTypeId.value && firstType?.id != null) {
    selectedTypeId.value = firstType.id
    selectedTypeName.value = firstType.unitTypeName
    selectedTypeKeys.value = [firstType.id]
  } else if (selectedTypeId.value) {
    const selected = flatTypes.find(item => item.id === selectedTypeId.value)
    if (selected) {
      selectedTypeName.value = selected.unitTypeName
    }
  }
  await tableRef.value?.refresh?.()
}

function handleTypeSelect(keys: any[], info: any) {
  const node = info?.node?.dataRef?.raw as UnitTypeNode | undefined
  selectedTypeId.value = node?.id
  selectedTypeName.value = node?.unitTypeName || ''
  selectedTypeKeys.value = node?.id ? [node.id] : []
  tableRef.value?.refresh?.()
}

function handleTypeMenu(key: string, node: UnitTypeNode) {
  if (key === 'add') {
    openTypeCreate(node)
  } else if (key === 'edit') {
    openTypeEdit(node)
  } else if (key === 'delete') {
    handleTypeDelete(node)
  }
}

function resetTypeForm(parentId = ROOT_PARENT_ID) {
  typeForm.id = undefined
  typeForm.unitTypeCode = ''
  typeForm.unitTypeName = ''
  typeForm.parentId = parentId
}

function openTypeCreate(parent?: UnitTypeNode) {
  resetTypeForm(parent?.id ?? selectedTypeId.value ?? ROOT_PARENT_ID)
  typeDialogVisible.value = true
}

function openTypeEdit(node: UnitTypeNode) {
  typeForm.id = node.id
  typeForm.unitTypeCode = node.unitTypeCode
  typeForm.unitTypeName = node.unitTypeName
  typeForm.parentId = node.parentId ?? ROOT_PARENT_ID
  typeDialogVisible.value = true
}

async function saveType() {
  if (!typeForm.unitTypeCode || !typeForm.unitTypeName) {
    message.warning(t('basic.unit.requiredHint'))
    return
  }
  saving.value = true
  try {
    const payload = {
      id: typeForm.id,
      unitTypeCode: typeForm.unitTypeCode,
      unitTypeName: typeForm.unitTypeName,
      parentId: typeForm.parentId ?? ROOT_PARENT_ID,
    }
    if (typeForm.id) {
      await unitApi.updateType(payload)
    } else {
      await unitApi.createType(payload)
    }
    typeDialogVisible.value = false
    await loadTypeTree()
  } finally {
    saving.value = false
  }
}

function handleTypeDelete(node: UnitTypeNode) {
  Modal.confirm({
    title: t('basic.unit.deleteTypeConfirm'),
    content: node.unitTypeName,
    async onOk() {
      if (!node.id) {
        return
      }
      await unitApi.deleteType(node.id)
      if (selectedTypeId.value === node.id) {
        selectedTypeId.value = undefined
        selectedTypeName.value = ''
        selectedTypeKeys.value = []
      }
      await loadTypeTree()
    },
  })
}

async function handleRequest(payload: {
  page: { current: number; pageSize: number }
  query: Record<string, any>
}) {
  const result = await unitApi.page({
    pageNum: payload.page.current,
    pageSize: payload.page.pageSize,
    unitTypeId: selectedTypeId.value,
    ...payload.query,
  })
  return { records: result?.records || [], total: Number(result?.total || 0) }
}

function resetUnitForm() {
  unitForm.id = undefined
  unitForm.unitTypeId = selectedTypeId.value
  unitForm.unitCode = ''
  unitForm.unitName = ''
  unitForm.remark = ''
}

function openUnitCreate() {
  resetUnitForm()
  unitDialogVisible.value = true
}

function openUnitEdit(record: UnitMaster) {
  unitForm.id = record.id
  unitForm.unitTypeId = record.unitTypeId
  unitForm.unitCode = record.unitCode
  unitForm.unitName = record.unitName
  unitForm.remark = record.remark
  unitDialogVisible.value = true
}

async function saveUnit() {
  if (!unitForm.unitTypeId || !unitForm.unitCode || !unitForm.unitName) {
    message.warning(t('basic.unit.requiredHint'))
    return
  }
  saving.value = true
  try {
    if (unitForm.id) {
      await unitApi.update({ ...unitForm })
    } else {
      await unitApi.create({ ...unitForm })
    }
    unitDialogVisible.value = false
    await reloadUnits()
    await tableRef.value?.refresh?.()
  } finally {
    saving.value = false
  }
}

function handleUnitDelete(record: UnitMaster) {
  Modal.confirm({
    title: t('basic.unit.deleteUnitConfirm'),
    content: record.unitName,
    async onOk() {
      await unitApi.delete(record.id!)
      await reloadUnits()
      await tableRef.value?.refresh?.()
    },
  })
}

async function reloadUnits() {
  unitOptions.value = await unitApi.list({})
}

async function openConversion(record: UnitMaster) {
  currentUnit.value = record
  await reloadUnits()
  const rows = await unitApi.listConversions(record.id!)
  conversionRows.value = (rows || []).map(row => ({
    ...row,
    rowKey: String(row.id ?? `${Date.now()}-${Math.random()}`),
  }))
  conversionVisible.value = true
}

function addConversionRow() {
  conversionRows.value.push({
    rowKey: `${Date.now()}-${Math.random()}`,
    unitId: currentUnit.value?.id,
    targetUnitId: undefined,
    conversionValue: undefined,
  })
}

function removeConversionRow(index: number) {
  conversionRows.value.splice(index, 1)
}

async function saveConversions() {
  if (!currentUnit.value?.id) {
    return
  }
  for (const row of conversionRows.value) {
    if (!row.targetUnitId || row.conversionValue === undefined || row.conversionValue === null || Number(row.conversionValue) <= 0) {
      message.warning(t('basic.unit.conversionRequiredHint'))
      return
    }
  }
  saving.value = true
  try {
    await unitApi.saveConversions({
      unitId: currentUnit.value.id,
      conversions: conversionRows.value.map(row => ({
        id: row.id,
        targetUnitId: row.targetUnitId,
        conversionValue: row.conversionValue,
      })),
    })
    conversionVisible.value = false
  } finally {
    saving.value = false
  }
}

function filterUnitOption(input: string, option: any) {
  return String(option?.label || '').toLowerCase().includes(input.toLowerCase())
}

onMounted(async () => {
  await loadTypeTree()
  await reloadUnits()
})
</script>

<style scoped lang="less">
.unit-page {
  display: grid;
  grid-template-columns: 300px minmax(0, 1fr);
  gap: 16px;
  height: 100%;
  min-height: 0;
  padding: 20px;
  box-sizing: border-box;
  overflow: hidden;
  background: var(--fx-bg-layout, #f8fafc);
}

.unit-type-panel,
.unit-main-panel {
  min-height: 0;
  border: 1px solid var(--fx-border-color, #e5e7eb);
  border-radius: 8px;
  background: var(--fx-bg-container, #fff);
}

.unit-type-panel {
  display: flex;
  flex-direction: column;
  padding: 14px;
  overflow: hidden;
}

.unit-type-toolbar {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}

.unit-type-toolbar h2 {
  margin: 0 0 4px;
  font-size: 16px;
}

.unit-type-toolbar span {
  color: var(--fx-text-secondary, #64748b);
  font-size: 12px;
}

.unit-type-panel :deep(.ant-tree) {
  flex: 1 1 auto;
  overflow: auto;
  background: transparent;
}

.type-tree-node {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  width: 100%;
}

.type-node-more {
  color: var(--fx-text-secondary, #64748b);
  opacity: 0;
}

.type-tree-node:hover .type-node-more {
  opacity: 1;
}

.unit-main-panel {
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.unit-main-header {
  flex-shrink: 0;
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 20px;
  padding: 20px 24px 12px;
}

.unit-main-header h1 {
  margin: 10px 0 6px;
  color: var(--fx-text-primary, #111827);
  font-size: 24px;
}

.unit-main-header p {
  margin: 0;
  color: var(--fx-text-secondary, #64748b);
}

.unit-main-panel :deep(.fx-dynamic-table) {
  flex: 1 1 auto;
  min-height: 0;
}

.danger-link {
  color: #ff4d4f;
}

.danger-menu-item {
  color: #ff4d4f;
}

.conversion-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}

.full-width {
  width: 100%;
}

@media (max-width: 960px) {
  .unit-page {
    grid-template-columns: 1fr;
    overflow: auto;
  }

  .unit-type-panel {
    min-height: 260px;
  }

  .unit-main-header {
    flex-direction: column;
  }
}
</style>
