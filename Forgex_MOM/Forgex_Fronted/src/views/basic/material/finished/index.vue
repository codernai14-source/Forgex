<template>
  <div class="material-page">
    <div class="page-header">
      <div>
        <a-tag color="blue">{{ t('basic.material.sectionTag') }}</a-tag>
        <h1>{{ t('basic.material.pages.finished.title') }}</h1>
        <p>{{ t('basic.material.pages.finished.description') }}</p>
      </div>
      <a-space>
        <a-button v-permission="'basic:material:add'" type="primary" @click="openCreate">
          <PlusOutlined /> {{ t('basic.material.pages.finished.add') }}
        </a-button>
      </a-space>
    </div>

    <FxDynamicTable ref="tableRef" table-code="FinishedGoodsMaterialTable" :request="handleRequest" row-key="id">
      <template #status="{ record }">
        <a-tag :color="record.status === 1 ? 'success' : 'default'">
          {{ record.status === 1 ? t('common.enable') : t('common.disable') }}
        </a-tag>
      </template>

      <template #approvalStatus="{ record }">
        <a-tag :color="approvalStatusColor(record.approvalStatus)">
          {{ labelOf(approvalStatusOptions, record.approvalStatus) }}
        </a-tag>
      </template>

      <template #action="{ record }">
        <a-space>
          <a v-permission="'basic:material:query'" @click="openDetail(record)">{{ t('common.detail') }}</a>
          <a v-permission="'basic:material:edit'" @click="openEdit(record)">{{ t('common.edit') }}</a>
          <a v-permission="'basic:material:delete'" class="danger-link" @click="handleDelete(record)">{{ t('common.delete') }}</a>
        </a-space>
      </template>
    </FxDynamicTable>

    <a-modal v-model:open="detailVisible" :title="t('basic.material.pages.finished.detail')" width="800px" :footer="null">
      <a-descriptions bordered :column="2" size="small">
        <a-descriptions-item :label="t('basic.material.fields.code')">{{ detailData.materialCode || '-' }}</a-descriptions-item>
        <a-descriptions-item :label="t('basic.material.fields.name')">{{ detailData.materialName || '-' }}</a-descriptions-item>
        <a-descriptions-item :label="t('basic.material.fields.type')">{{ labelOf(materialTypeOptions, detailData.materialType) }}</a-descriptions-item>
        <a-descriptions-item :label="t('basic.material.fields.category')">{{ detailData.materialCategory || '-' }}</a-descriptions-item>
        <a-descriptions-item :label="t('basic.material.fields.specification')">{{ detailData.specification || '-' }}</a-descriptions-item>
        <a-descriptions-item :label="t('basic.material.fields.unit')">{{ detailData.unit || '-' }}</a-descriptions-item>
        <a-descriptions-item :label="t('basic.material.fields.brand')">{{ detailData.brand || '-' }}</a-descriptions-item>
        <a-descriptions-item :label="t('basic.material.fields.status')">
          <a-tag :color="detailData.status === 1 ? 'success' : 'default'">
            {{ detailData.status === 1 ? t('common.enable') : t('common.disable') }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item :label="t('basic.material.fields.approvalStatus')">
          <a-tag :color="approvalStatusColor(detailData.approvalStatus)">
            {{ labelOf(approvalStatusOptions, detailData.approvalStatus) }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item :label="t('basic.material.fields.remark')" :span="2">{{ detailData.remark || '-' }}</a-descriptions-item>
        <a-descriptions-item :label="t('basic.material.fields.description')" :span="2">{{ detailData.description || '-' }}</a-descriptions-item>
      </a-descriptions>
    </a-modal>

    <a-modal
      v-model:open="dialogVisible"
      :title="dialogTitle"
      width="800px"
      :confirm-loading="saving"
      @ok="handleSave"
      @cancel="dialogVisible = false"
    >
      <a-form :model="form" layout="vertical">
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item :label="t('basic.material.fields.code')" required>
              <a-input v-model:value="form.materialCode" :placeholder="t('basic.material.placeholder.code')" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item :label="t('basic.material.fields.name')" required>
              <a-input v-model:value="form.materialName" :placeholder="t('basic.material.placeholder.name')" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item :label="t('basic.material.fields.category')">
              <a-input v-model:value="form.materialCategory" :placeholder="t('basic.material.placeholder.category')" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item :label="t('basic.material.fields.specification')">
              <a-input v-model:value="form.specification" :placeholder="t('basic.material.placeholder.specification')" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item :label="t('basic.material.fields.unit')">
              <a-select v-model:value="form.unit" :placeholder="t('basic.material.placeholder.unit')" allow-clear show-search>
                <a-select-option v-for="item in unitOptions" :key="item.id" :value="item.unitName">
                  {{ item.unitName }} ({{ item.unitSymbol }})
                </a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item :label="t('basic.material.fields.brand')">
              <a-input v-model:value="form.brand" :placeholder="t('basic.material.placeholder.brand')" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item :label="t('basic.material.fields.status')">
              <a-select v-model:value="form.status" :options="statusOptions" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item :label="t('basic.material.fields.remark')">
          <a-textarea v-model:value="form.remark" :rows="2" :placeholder="t('basic.material.placeholder.remark')" />
        </a-form-item>
        <a-form-item :label="t('basic.material.fields.description')">
          <a-textarea v-model:value="form.description" :rows="3" :placeholder="t('basic.material.placeholder.description')" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { message, Modal } from 'ant-design-vue'
import { PlusOutlined } from '@ant-design/icons-vue'
import { useI18n } from 'vue-i18n'
import { materialApi } from '@/api/basic/material'
import { getAllUnits } from '@/api/basic/unit'
import FxDynamicTable from '@/components/common/FxDynamicTable.vue'

const { t } = useI18n()

const tableRef = ref()
const dialogVisible = ref(false)
const detailVisible = ref(false)
const saving = ref(false)
const isEdit = ref(false)
const detailData = ref<any>({})
const unitOptions = ref<any[]>([])

const form = reactive<any>({
  id: null,
  materialCode: '',
  materialName: '',
  materialType: 'FINISHED_GOODS',
  materialCategory: '',
  specification: '',
  unit: '',
  brand: '',
  status: 1,
  remark: '',
  description: '',
})

const materialTypeOptions = computed(() => [
  { value: 'RAW_MATERIAL', label: t('basic.material.types.raw') },
  { value: 'SEMI_FINISHED', label: t('basic.material.types.semiFinished') },
  { value: 'FINISHED_GOODS', label: t('basic.material.types.finished') },
  { value: 'OTHER', label: t('basic.material.types.other') },
])

const approvalStatusOptions = computed(() => [
  { value: 'NO_APPROVAL_REQUIRED', label: t('basic.material.approval.noApprovalRequired') },
  { value: 'PENDING', label: t('basic.material.approval.pending') },
  { value: 'APPROVED', label: t('basic.material.approval.approved') },
  { value: 'REJECTED', label: t('basic.material.approval.rejected') },
])

const statusOptions = computed(() => [
  { value: 1, label: t('common.enable') },
  { value: 0, label: t('common.disable') },
])

const dialogTitle = computed(() => (isEdit.value ? t('basic.material.pages.finished.edit') : t('basic.material.pages.finished.add')))

function handleRequest(params: any) {
  return materialApi.page({
    pageNum: params.pageNum || 1,
    pageSize: params.pageSize || 10,
    materialType: 'FINISHED_GOODS',
    ...params,
  })
}

function labelOf(options: Array<{ value: string; label: string }>, value: any) {
  const item = options.find((option) => option.value === value)
  return item ? item.label : (value || '-')
}

function approvalStatusColor(status: string) {
  const colorMap: Record<string, string> = {
    NO_APPROVAL_REQUIRED: 'default',
    PENDING: 'warning',
    APPROVED: 'success',
    REJECTED: 'error',
  }
  return colorMap[status] || 'default'
}

function resetForm() {
  form.id = null
  form.materialCode = ''
  form.materialName = ''
  form.materialType = 'FINISHED_GOODS'
  form.materialCategory = ''
  form.specification = ''
  form.unit = ''
  form.brand = ''
  form.status = 1
  form.remark = ''
  form.description = ''
}

function openCreate() {
  resetForm()
  isEdit.value = false
  dialogVisible.value = true
}

function openEdit(record: any) {
  resetForm()
  Object.assign(form, record)
  isEdit.value = true
  dialogVisible.value = true
}

function openDetail(record: any) {
  detailData.value = record
  detailVisible.value = true
}

async function handleSave() {
  if (!form.materialCode || !form.materialName) {
    message.warning(t('validation.required'))
    return
  }
  saving.value = true
  try {
    if (isEdit.value) {
      await materialApi.update(form)
      message.success(t('common.updateSuccess'))
    } else {
      await materialApi.create(form)
      message.success(t('common.createSuccess'))
    }
    dialogVisible.value = false
    tableRef.value?.refresh?.()
  } finally {
    saving.value = false
  }
}

function handleDelete(record: any) {
  Modal.confirm({
    title: t('common.delete'),
    content: t('basic.material.pages.finished.confirmDelete', { name: record.materialName || '' }),
    onOk: async () => {
      await materialApi.delete({ id: record.id })
      message.success(t('common.deleteSuccess'))
      tableRef.value?.refresh?.()
    },
  })
}

onMounted(async () => {
  try {
    const res: any = await getAllUnits()
    unitOptions.value = Array.isArray(res) ? res : (res?.data || [])
  } catch {
    unitOptions.value = []
  }
})
</script>

<style scoped lang="less" src="@/styles/views/basic/material/finished/index.less"></style>
