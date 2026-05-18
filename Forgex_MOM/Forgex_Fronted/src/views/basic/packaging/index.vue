<template>
  <div class="packaging-page">
    <div class="page-header">
      <div>
        <a-tag color="blue">{{ t('basic.packaging.sectionTag') }}</a-tag>
        <h1>{{ t('basic.packaging.title') }}</h1>
        <p>{{ t('basic.packaging.description') }}</p>
      </div>
    </div>

    <FxDynamicTable
      ref="tableRef"
      table-code="BasicPackagingTypeTable"
      :request="handleRequest"
      :dict-options="dictOptions"
      :row-selection="rowSelection"
      :query-first-row-count="3"
      row-key="id"
    >
      <template #toolbar>
        <a-space wrap>
          <a-button v-permission="'basic:packaging:add'" type="primary" @click="openCreate">
            <PlusOutlined /> {{ t('basic.packaging.add') }}
          </a-button>
          <a-button
            v-permission="'basic:packaging:delete'"
            danger
            :disabled="!selectedRowKeys.length"
            @click="handleBatchDelete"
          >
            {{ t('common.batchDelete') }}
          </a-button>
          <span v-if="selectedRowKeys.length" class="selection-summary">
            {{ t('common.selectedCount', { count: selectedRowKeys.length }) }}
          </span>
        </a-space>
      </template>

      <template #packagingSpecType="{ record }">
        <a-tag>{{ labelOf(packagingSpecTypeOptions, record.packagingSpecType) }}</a-tag>
      </template>

      <template #size="{ record }">
        {{ formatSize(record) }}
      </template>

      <template #volume="{ record }">
        {{ formatValueWithUnit(record.volumeValue, record.volumeUnitName) }}
      </template>

      <template #weight="{ record }">
        {{ formatValueWithUnit(record.weightValue, record.weightUnitName) }}
      </template>

      <template #status="{ record }">
        <a-tag :color="record.status === 1 ? 'success' : 'default'">
          {{ record.status === 1 ? t('common.enable') : t('common.disable') }}
        </a-tag>
      </template>

      <template #action="{ record }">
        <a-space>
          <a v-permission="'basic:packaging:query'" @click="openRelationDialog(record)">
            {{ t('basic.packaging.relation.entry') }}
          </a>
          <a v-permission="'basic:packaging:edit'" @click="openEdit(record)">{{ t('common.edit') }}</a>
          <a v-permission="'basic:packaging:delete'" class="danger-link" @click="handleDelete(record)">{{ t('common.delete') }}</a>
        </a-space>
      </template>
    </FxDynamicTable>

    <BaseFormDialog
      v-model:open="dialogVisible"
      mode="modal"
      :title="dialogTitle"
      width="860px"
      :loading="saving"
      @submit="handleSave"
      @cancel="dialogVisible = false"
    >
      <a-form :model="form" layout="vertical">
        <a-row :gutter="16">
          <a-col :xs="24" :md="12">
            <a-form-item :label="t('basic.packaging.fields.code')" required>
              <a-input v-model:value="form.packagingCode" :placeholder="t('basic.packaging.placeholder.code')" />
            </a-form-item>
          </a-col>
          <a-col :xs="24" :md="12">
            <a-form-item :label="t('basic.packaging.fields.name')" required>
              <a-input v-model:value="form.packagingName" :placeholder="t('basic.packaging.placeholder.name')" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :xs="24" :md="12">
            <a-form-item :label="t('basic.packaging.fields.specType')" required>
              <a-select
                v-model:value="form.packagingSpecType"
                :options="packagingSpecTypeOptions"
                :placeholder="t('basic.packaging.placeholder.specType')"
                allow-clear
              />
            </a-form-item>
          </a-col>
          <a-col :xs="24" :md="12">
            <a-form-item :label="t('basic.packaging.fields.sizeUnit')">
              <a-select
                v-model:value="form.sizeUnitId"
                :options="unitSelectOptions"
                :placeholder="t('basic.packaging.placeholder.unit')"
                show-search
                allow-clear
                :filter-option="filterOption"
              />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :xs="24" :md="8">
            <a-form-item :label="t('basic.packaging.fields.length')">
              <a-input-number v-model:value="form.lengthValue" :min="0" :precision="4" class="full-control" />
            </a-form-item>
          </a-col>
          <a-col :xs="24" :md="8">
            <a-form-item :label="t('basic.packaging.fields.width')">
              <a-input-number v-model:value="form.widthValue" :min="0" :precision="4" class="full-control" />
            </a-form-item>
          </a-col>
          <a-col :xs="24" :md="8">
            <a-form-item :label="t('basic.packaging.fields.height')">
              <a-input-number v-model:value="form.heightValue" :min="0" :precision="4" class="full-control" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :xs="24" :md="12">
            <a-form-item :label="t('basic.packaging.fields.volume')">
              <a-input-number v-model:value="form.volumeValue" :min="0" :precision="4" class="full-control" />
            </a-form-item>
          </a-col>
          <a-col :xs="24" :md="12">
            <a-form-item :label="t('basic.packaging.fields.volumeUnit')">
              <a-select
                v-model:value="form.volumeUnitId"
                :options="unitSelectOptions"
                :placeholder="t('basic.packaging.placeholder.unit')"
                show-search
                allow-clear
                :filter-option="filterOption"
              />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :xs="24" :md="12">
            <a-form-item :label="t('basic.packaging.fields.weight')">
              <a-input-number v-model:value="form.weightValue" :min="0" :precision="4" class="full-control" />
            </a-form-item>
          </a-col>
          <a-col :xs="24" :md="12">
            <a-form-item :label="t('basic.packaging.fields.weightUnit')">
              <a-select
                v-model:value="form.weightUnitId"
                :options="unitSelectOptions"
                :placeholder="t('basic.packaging.placeholder.unit')"
                show-search
                allow-clear
                :filter-option="filterOption"
              />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :xs="24" :md="12">
            <a-form-item :label="t('basic.packaging.fields.status')">
              <a-select v-model:value="form.status" :options="statusOptions" />
            </a-form-item>
          </a-col>
          <a-col :xs="24" :md="12">
            <a-form-item :label="t('basic.packaging.fields.sortOrder')">
              <a-input-number v-model:value="form.sortOrder" :min="0" class="full-control" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item :label="t('basic.packaging.fields.remark')">
          <a-textarea v-model:value="form.remark" :rows="2" :placeholder="t('basic.packaging.placeholder.remark')" />
        </a-form-item>
      </a-form>
    </BaseFormDialog>

    <BaseFormDialog
      v-model:open="relationVisible"
      mode="drawer"
      :title="relationTitle"
      width="760px"
      :loading="relationSaving"
      :footer="false"
      :body-style="{ maxHeight: 'calc(100vh - 120px)', overflowY: 'auto' }"
      @cancel="relationVisible = false"
    >
      <a-spin :spinning="relationLoading">
        <div class="relation-toolbar">
          <a-select
            v-model:value="relationForm.materialId"
            :options="materialSelectOptions"
            :placeholder="t('basic.packaging.relation.materialPlaceholder')"
            show-search
            allow-clear
            :filter-option="filterOption"
          />
          <a-select
            v-model:value="relationForm.packagingSlot"
            :options="packagingSlotOptions"
            class="slot-select"
          />
          <a-button v-permission="'basic:packaging:edit'" type="primary" :loading="relationSaving" @click="handleAddRelation">
            <PlusOutlined /> {{ t('basic.packaging.relation.add') }}
          </a-button>
        </div>

        <a-table
          size="small"
          :columns="relationColumns"
          :data-source="relationList"
          :pagination="false"
          row-key="id"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'slot'">
              <a-tag>{{ labelOf(packagingSlotOptions, record.packagingSlot) }}</a-tag>
            </template>
            <template v-else-if="column.key === 'action'">
              <a v-permission="'basic:packaging:edit'" class="danger-link" @click="handleDeleteRelation(record)">
                {{ t('common.delete') }}
              </a>
            </template>
          </template>
        </a-table>
      </a-spin>
    </BaseFormDialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { message, Modal } from 'ant-design-vue'
import type { TableProps } from 'ant-design-vue'
import { PlusOutlined } from '@ant-design/icons-vue'
import { useI18n } from 'vue-i18n'
import {
  batchDeletePackagingType,
  createPackagingType,
  deletePackagingRelation,
  deletePackagingType,
  getPackagingTypePage,
  listPackagingByPackaging,
  savePackagingSlot,
  updatePackagingType,
  type MaterialPackagingRelation,
  type PackagingType,
} from '@/api/basic/packaging'
import { materialApi } from '@/api/basic/material'
import { getAllUnits } from '@/api/basic/unit'
import { useDict } from '@/hooks/useDict'
import FxDynamicTable from '@/components/common/FxDynamicTable.vue'
import BaseFormDialog from '@/components/common/BaseFormDialog.vue'

const { t } = useI18n()
const { dictItems: packagingSpecDictItems } = useDict('packaging_spec_type')

type PackagingSlot = 'SMALL' | 'MEDIUM' | 'LARGE'
type SelectOption = { value?: string | number; label?: string }

const tableRef = ref()
const dialogVisible = ref(false)
const relationVisible = ref(false)
const saving = ref(false)
const relationSaving = ref(false)
const relationLoading = ref(false)
const isEdit = ref(false)
const selectedRowKeys = ref<number[]>([])
const selectedRows = ref<PackagingType[]>([])
const unitOptions = ref<any[]>([])
const materialOptions = ref<any[]>([])
const relationList = ref<MaterialPackagingRelation[]>([])
const currentPackaging = ref<PackagingType | null>(null)

const form = reactive<PackagingType>({
  id: undefined,
  packagingCode: '',
  packagingName: '',
  packagingSpecType: undefined,
  lengthValue: null,
  widthValue: null,
  heightValue: null,
  sizeUnitId: null,
  volumeValue: null,
  volumeUnitId: null,
  weightValue: null,
  weightUnitId: null,
  status: 1,
  sortOrder: 0,
  remark: '',
})

const relationForm = reactive({
  materialId: undefined as number | undefined,
  packagingSlot: 'SMALL' as PackagingSlot,
})

const fallbackPackagingSpecOptions = [
  { value: 'BOX', label: '箱' },
  { value: 'BUCKET', label: '桶' },
  { value: 'ROLL', label: '卷' },
  { value: 'CASE', label: '盒' },
  { value: 'BAG', label: '袋' },
]

const packagingSpecTypeOptions = computed(() => {
  return packagingSpecDictItems.value.length ? packagingSpecDictItems.value : fallbackPackagingSpecOptions
})

const packagingSlotOptions = computed(() => [
  { value: 'SMALL', label: t('basic.packaging.relation.small') },
  { value: 'MEDIUM', label: t('basic.packaging.relation.medium') },
  { value: 'LARGE', label: t('basic.packaging.relation.large') },
])

const statusOptions = computed(() => [
  { value: 1, label: t('common.enable') },
  { value: 0, label: t('common.disable') },
])

const dictOptions = computed(() => ({
  packaging_spec_type: packagingSpecTypeOptions.value,
  packagingSpecType: packagingSpecTypeOptions.value,
  status: statusOptions.value,
}))

const unitSelectOptions = computed(() => unitOptions.value.map((item) => ({
  value: item.id,
  label: `${item.unitName}${item.unitCode ? ` (${item.unitCode})` : ''}`,
})))

const materialSelectOptions = computed(() => materialOptions.value.map((item) => ({
  value: item.id,
  label: `${item.materialCode || ''} ${item.materialName || ''}`.trim(),
})))

const dialogTitle = computed(() => (isEdit.value ? t('basic.packaging.dialog.edit') : t('basic.packaging.dialog.create')))
const relationTitle = computed(() => t('basic.packaging.relation.title', { name: currentPackaging.value?.packagingName || '' }))

const relationColumns = computed(() => [
  { title: t('basic.material.fields.code'), dataIndex: 'materialCode', key: 'materialCode', width: 150 },
  { title: t('basic.material.fields.name'), dataIndex: 'materialName', key: 'materialName' },
  { title: t('basic.packaging.relation.slot'), dataIndex: 'packagingSlot', key: 'slot', width: 120 },
  { title: t('common.action'), key: 'action', width: 90 },
])

const rowSelection = computed<TableProps['rowSelection']>(() => ({
  selectedRowKeys: selectedRowKeys.value,
  preserveSelectedRowKeys: false,
  onChange: (keys, rows) => {
    selectedRowKeys.value = keys.map(key => Number(key)).filter(key => Number.isFinite(key))
    selectedRows.value = rows as PackagingType[]
  },
}))

function clearSelection() {
  selectedRowKeys.value = []
  selectedRows.value = []
}

function handleRequest(payload: {
  page: { current: number; pageSize: number }
  query: Record<string, any>
}) {
  return getPackagingTypePage({
    pageNum: payload.page.current,
    pageSize: payload.page.pageSize,
    ...payload.query,
  }).then((result: any) => ({
    records: result?.records || [],
    total: Number(result?.total || 0),
  }))
}

function resetForm() {
  Object.assign(form, {
    id: undefined,
    packagingCode: '',
    packagingName: '',
    packagingSpecType: undefined,
    lengthValue: null,
    widthValue: null,
    heightValue: null,
    sizeUnitId: null,
    volumeValue: null,
    volumeUnitId: null,
    weightValue: null,
    weightUnitId: null,
    status: 1,
    sortOrder: 0,
    remark: '',
  })
}

function openCreate() {
  resetForm()
  isEdit.value = false
  dialogVisible.value = true
}

function openEdit(record: PackagingType) {
  resetForm()
  Object.assign(form, record)
  isEdit.value = true
  dialogVisible.value = true
}

async function handleSave() {
  if (!form.packagingCode || !form.packagingName || !form.packagingSpecType) {
    message.warning(t('validation.required'))
    return
  }
  saving.value = true
  try {
    const payload = {
      ...form,
      packagingCode: form.packagingCode?.trim(),
      packagingName: form.packagingName?.trim(),
    }
    if (isEdit.value) {
      await updatePackagingType(payload)
      message.success(t('common.updateSuccess'))
    } else {
      await createPackagingType(payload)
      message.success(t('common.createSuccess'))
    }
    dialogVisible.value = false
    clearSelection()
    tableRef.value?.refresh?.()
  } finally {
    saving.value = false
  }
}

function handleDelete(record: PackagingType) {
  Modal.confirm({
    title: t('common.delete'),
    content: t('basic.packaging.confirmDelete', { name: record.packagingName || '' }),
    onOk: async () => {
      await deletePackagingType(record.id!)
      message.success(t('common.deleteSuccess'))
      clearSelection()
      tableRef.value?.refresh?.()
    },
  })
}

function handleBatchDelete() {
  const ids = selectedRowKeys.value
  if (!ids.length) {
    return
  }
  Modal.confirm({
    title: t('common.confirmBatchDelete'),
    content: t('common.confirmBatchDeleteMessage', { count: ids.length }),
    onOk: async () => {
      await batchDeletePackagingType(ids)
      message.success(t('common.deleteSuccess'))
      clearSelection()
      tableRef.value?.refresh?.()
    },
  })
}

async function openRelationDialog(record: PackagingType) {
  currentPackaging.value = record
  relationForm.materialId = undefined
  relationForm.packagingSlot = 'SMALL'
  relationVisible.value = true
  await Promise.all([loadMaterials(), loadRelations()])
}

async function loadRelations() {
  if (!currentPackaging.value?.id) return
  relationLoading.value = true
  try {
    const result: any = await listPackagingByPackaging(currentPackaging.value.id)
    relationList.value = Array.isArray(result) ? result : []
  } finally {
    relationLoading.value = false
  }
}

async function loadMaterials() {
  if (materialOptions.value.length) return
  const result: any = await materialApi.list({ status: 1 })
  materialOptions.value = Array.isArray(result) ? result : (result?.records || [])
}

async function handleAddRelation() {
  if (!relationForm.materialId || !currentPackaging.value?.id) {
    message.warning(t('validation.required'))
    return
  }
  relationSaving.value = true
  try {
    await savePackagingSlot({
      materialId: relationForm.materialId,
      packagingTypeId: currentPackaging.value.id,
      packagingSlot: relationForm.packagingSlot,
    })
    message.success(t('common.saveSuccess'))
    relationForm.materialId = undefined
    await loadRelations()
  } finally {
    relationSaving.value = false
  }
}

function handleDeleteRelation(record: MaterialPackagingRelation) {
  Modal.confirm({
    title: t('common.delete'),
    content: t('basic.packaging.relation.confirmDelete'),
    onOk: async () => {
      await deletePackagingRelation(record.id!)
      message.success(t('common.deleteSuccess'))
      await loadRelations()
    },
  })
}

function formatSize(record: PackagingType) {
  const values = [record.lengthValue, record.widthValue, record.heightValue].filter((item) => item !== null && item !== undefined && item !== '')
  return values.length ? `${values.join(' x ')} ${record.sizeUnitName || ''}`.trim() : '-'
}

function formatValueWithUnit(value: any, unitName?: string) {
  return value === null || value === undefined || value === '' ? '-' : `${value} ${unitName || ''}`.trim()
}

function labelOf(options: SelectOption[], value: any) {
  return options.find((item) => String(item.value) === String(value))?.label || value || '-'
}

function filterOption(input: string, option: any) {
  return String(option?.label || '').toLowerCase().includes(input.toLowerCase())
}

onMounted(async () => {
  try {
    const result: any = await getAllUnits()
    unitOptions.value = Array.isArray(result) ? result : (result?.records || [])
  } catch {
    unitOptions.value = []
  }
})
</script>

<style scoped lang="less" src="@/styles/views/basic/packaging/index.less"></style>
