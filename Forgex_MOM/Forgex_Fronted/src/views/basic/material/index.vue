<template>
  <div class="material-page">
    <div class="page-header">
      <div>
        <a-tag color="blue">基础数据</a-tag>
        <h1>物料管理</h1>
        <p>统一维护全部物料，并按物料类型快速筛选。</p>
      </div>
      <a-space>
        <a-button v-permission="'basic:material:add'" type="primary" @click="openCreate">
          <PlusOutlined /> 新增物料
        </a-button>
      </a-space>
    </div>

    <a-tabs v-model:active-key="activeTab" class="material-tabs" @change="handleTabChange">
      <a-tab-pane v-for="tab in materialTabs" :key="tab.value" :tab="tab.label" />
    </a-tabs>

    <FxDynamicTable
      ref="tableRef"
      table-code="MaterialTable"
      :request="handleRequest"
      :dict-options="dictOptions"
      :fallback-config="tableFallbackConfig"
      row-key="id"
    >
      <template #materialType="{ record }">
        <a-tag :color="materialTypeColor(record.materialType)">
          {{ labelOf(materialTypeOptions, record.materialType) }}
        </a-tag>
      </template>

      <template #status="{ record }">
        <a-tag :color="record.status === 1 ? 'success' : 'default'">
          {{ record.status === 1 ? '启用' : '停用' }}
        </a-tag>
      </template>

      <template #approvalStatus="{ record }">
        <a-tag :color="approvalStatusColor(record.approvalStatus)">
          {{ labelOf(approvalStatusOptions, record.approvalStatus) }}
        </a-tag>
      </template>

      <template #action="{ record }">
        <a-space>
          <a v-permission="'basic:material:query'" @click="openDetail(record)">详情</a>
          <a v-permission="'basic:material:edit'" @click="openEdit(record)">编辑</a>
          <a v-permission="'basic:material:delete'" class="danger-link" @click="handleDelete(record)">删除</a>
        </a-space>
      </template>
    </FxDynamicTable>

    <a-modal v-model:open="detailVisible" title="物料详情" width="800px" :footer="null">
      <a-descriptions bordered :column="2" size="small">
        <a-descriptions-item label="物料编码">{{ detailData.materialCode || '-' }}</a-descriptions-item>
        <a-descriptions-item label="物料名称">{{ detailData.materialName || '-' }}</a-descriptions-item>
        <a-descriptions-item label="物料类型">{{ labelOf(materialTypeOptions, detailData.materialType) }}</a-descriptions-item>
        <a-descriptions-item label="物料分类">{{ detailData.materialCategory || '-' }}</a-descriptions-item>
        <a-descriptions-item label="规格型号">{{ detailData.specification || '-' }}</a-descriptions-item>
        <a-descriptions-item label="计量单位">{{ detailData.unit || '-' }}</a-descriptions-item>
        <a-descriptions-item label="品牌">{{ detailData.brand || '-' }}</a-descriptions-item>
        <a-descriptions-item label="状态">
          <a-tag :color="detailData.status === 1 ? 'success' : 'default'">
            {{ detailData.status === 1 ? '启用' : '停用' }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="审批状态">
          <a-tag :color="approvalStatusColor(detailData.approvalStatus)">
            {{ labelOf(approvalStatusOptions, detailData.approvalStatus) }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="备注" :span="2">{{ detailData.remark || '-' }}</a-descriptions-item>
        <a-descriptions-item label="详细描述" :span="2">{{ detailData.description || '-' }}</a-descriptions-item>
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
            <a-form-item label="物料编码" required>
              <a-input v-model:value="form.materialCode" placeholder="请输入物料编码" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="物料名称" required>
              <a-input v-model:value="form.materialName" placeholder="请输入物料名称" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="物料类型" required>
              <a-select
                v-model:value="form.materialType"
                :disabled="activeTab !== ALL_TAB"
                :options="materialTypeOptions"
                placeholder="请选择物料类型"
              />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="物料分类">
              <a-input v-model:value="form.materialCategory" placeholder="请输入物料分类" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="规格型号">
              <a-input v-model:value="form.specification" placeholder="请输入规格型号" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="计量单位">
              <a-select v-model:value="form.unit" placeholder="请选择计量单位" allow-clear show-search>
                <a-select-option v-for="item in unitOptions" :key="item.id" :value="item.unitName">
                  {{ item.unitName }} ({{ item.unitSymbol }})
                </a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="品牌">
              <a-input v-model:value="form.brand" placeholder="请输入品牌" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="状态">
              <a-select v-model:value="form.status" :options="statusOptions" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item label="备注">
          <a-textarea v-model:value="form.remark" :rows="2" placeholder="请输入备注" />
        </a-form-item>
        <a-form-item label="详细描述">
          <a-textarea v-model:value="form.description" :rows="3" placeholder="请输入详细描述" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, reactive, ref } from 'vue'
import { message, Modal } from 'ant-design-vue'
import { PlusOutlined } from '@ant-design/icons-vue'
import { materialApi, type MaterialPageParam } from '@/api/basic/material'
import { getAllUnits } from '@/api/basic/unit'
import FxDynamicTable from '@/components/common/FxDynamicTable.vue'

const ALL_TAB = 'ALL'
const DEFAULT_MATERIAL_TYPE = 'RAW_MATERIAL'

const tableRef = ref()
const activeTab = ref(ALL_TAB)
const dialogVisible = ref(false)
const detailVisible = ref(false)
const saving = ref(false)
const isEdit = ref(false)
const detailData = ref<any>({})
const unitOptions = ref<any[]>([])

const materialTabs = [
  { value: ALL_TAB, label: '全部' },
  { value: 'RAW_MATERIAL', label: '原材料' },
  { value: 'SEMI_FINISHED', label: '半成品' },
  { value: 'FINISHED_GOODS', label: '成品' },
]

const materialTypeOptions = [
  { value: 'RAW_MATERIAL', label: '原材料' },
  { value: 'SEMI_FINISHED', label: '半成品' },
  { value: 'FINISHED_GOODS', label: '成品' },
  { value: 'OTHER', label: '其它' },
]

const approvalStatusOptions = [
  { value: 'NO_APPROVAL_REQUIRED', label: '无需审批' },
  { value: 'PENDING', label: '待审批' },
  { value: 'APPROVED', label: '已审批' },
  { value: 'REJECTED', label: '已驳回' },
]

const statusOptions = [
  { value: 1, label: '启用' },
  { value: 0, label: '停用' },
]

const dictOptions = computed(() => ({
  materialType: materialTypeOptions,
  material_type: materialTypeOptions,
  status: statusOptions,
  common_status: statusOptions,
  approvalStatus: approvalStatusOptions,
  material_approval_status: approvalStatusOptions,
}))

const form = reactive<any>({
  id: null,
  materialCode: '',
  materialName: '',
  materialType: DEFAULT_MATERIAL_TYPE,
  materialCategory: '',
  specification: '',
  unit: '',
  brand: '',
  status: 1,
  remark: '',
  description: '',
})

const tableFallbackConfig = {
  tableCode: 'MaterialTable',
  tableName: '物料管理',
  tableType: 'BUSINESS',
  rowKey: 'id',
  defaultPageSize: 10,
  columns: [
    { field: 'materialCode', title: '物料编码', width: 140, visible: true, order: 1 },
    { field: 'materialName', title: '物料名称', width: 180, visible: true, order: 2 },
    { field: 'materialType', title: '物料类型', width: 120, visible: true, order: 3 },
    { field: 'materialCategory', title: '物料分类', width: 140, visible: true, order: 4 },
    { field: 'specification', title: '规格型号', width: 160, visible: true, ellipsis: true, order: 5 },
    { field: 'unit', title: '计量单位', width: 100, visible: true, order: 6 },
    { field: 'brand', title: '品牌', width: 120, visible: true, order: 7 },
    { field: 'status', title: '状态', width: 90, visible: true, order: 8 },
    { field: 'approvalStatus', title: '审批状态', width: 120, visible: true, order: 9 },
    { field: 'createTime', title: '创建时间', width: 180, visible: true, order: 10 },
    { field: 'action', title: '操作', width: 180, fixed: 'right', visible: true, order: 99 },
  ],
  queryFields: [
    { field: 'materialCode', label: '物料编码', queryType: 'input', queryOperator: 'like' },
    { field: 'materialName', label: '物料名称', queryType: 'input', queryOperator: 'like' },
    { field: 'materialCategory', label: '物料分类', queryType: 'input', queryOperator: 'like' },
  ],
}

const dialogTitle = computed(() => (isEdit.value ? '编辑物料' : '新增物料'))

async function handleRequest(payload: {
  page: { current: number; pageSize: number }
  query: Record<string, any>
  sorter?: { field?: string; order?: string }
}) {
  const params: MaterialPageParam = {
    pageNum: payload.page.current,
    pageSize: payload.page.pageSize,
    ...payload.query,
  }

  if (activeTab.value !== ALL_TAB) {
    params.materialType = activeTab.value
  }

  const result: any = await materialApi.page(params)
  return { records: result?.records || [], total: Number(result?.total || 0) }
}

function handleTabChange() {
  nextTick(() => tableRef.value?.refresh?.())
}

function currentCreateType() {
  return activeTab.value === ALL_TAB ? DEFAULT_MATERIAL_TYPE : activeTab.value
}

function resetForm() {
  form.id = null
  form.materialCode = ''
  form.materialName = ''
  form.materialType = currentCreateType()
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
  if (!form.materialCode || !form.materialName || !form.materialType) {
    message.warning('请填写必填项')
    return
  }
  saving.value = true
  try {
    if (isEdit.value) {
      await materialApi.update(form)
      message.success('更新成功')
    } else {
      await materialApi.create(form)
      message.success('创建成功')
    }
    dialogVisible.value = false
    tableRef.value?.refresh?.()
  } finally {
    saving.value = false
  }
}

function handleDelete(record: any) {
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除物料 "${record.materialName}" 吗？`,
    onOk: async () => {
      await materialApi.delete({ id: record.id })
      message.success('删除成功')
      tableRef.value?.refresh?.()
    },
  })
}

function labelOf(options: any[], value: any) {
  const item = options.find(option => option.value === value)
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

function materialTypeColor(type: string) {
  const colorMap: Record<string, string> = {
    RAW_MATERIAL: 'blue',
    SEMI_FINISHED: 'gold',
    FINISHED_GOODS: 'green',
    OTHER: 'default',
  }
  return colorMap[type] || 'default'
}

onMounted(async () => {
  try {
    const res: any = await getAllUnits()
    unitOptions.value = Array.isArray(res) ? res : (res?.data || [])
  } catch (e) {
    unitOptions.value = []
  }
})
</script>

<style scoped>
.material-page {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
  padding: 24px;
  overflow: hidden;
  box-sizing: border-box;
}

.page-header {
  flex-shrink: 0;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 16px;
}

.page-header h1 {
  margin: 8px 0 4px;
  font-size: 20px;
  font-weight: 600;
}

.page-header p {
  margin: 0;
  color: #666;
  font-size: 14px;
}

.material-tabs {
  flex-shrink: 0;
  margin-bottom: 12px;
}

.material-page :deep(.fx-dynamic-table) {
  flex: 1 1 auto;
  min-height: 0;
}

.danger-link {
  color: #ff4d4f;
}

@media (max-width: 768px) {
  .material-page {
    padding: 16px;
  }

  .page-header {
    flex-direction: column;
  }
}
</style>
