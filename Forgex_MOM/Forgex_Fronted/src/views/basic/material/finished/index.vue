<template>
  <div class="material-page">
    <div class="page-header">
      <div>
        <a-tag color="blue">基础数据</a-tag>
        <h1>成品管理</h1>
        <p>管理成品物料信息，支持新增、编辑、删除和审批操作。</p>
      </div>
      <a-space>
        <a-button v-permission="'basic:material:add'" type="primary" @click="openCreate">
          <PlusOutlined /> 新增成品
        </a-button>
      </a-space>
    </div>

    <FxDynamicTable
      ref="tableRef"
      table-code="FinishedGoodsMaterialTable"
      :request="handleRequest"
      :fallback-config="tableFallbackConfig"
      row-key="id"
    >
      <template #status="{ record }">
        <a-tag :color="record.status === 1 ? 'success' : 'default'">
          {{ record.status === 1 ? '启用' : '禁用' }}
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

    <a-modal
      v-model:open="detailVisible"
      title="成品详情"
      width="800px"
      :footer="null"
    >
      <a-descriptions bordered :column="2" size="small">
        <a-descriptions-item label="物料编码">{{ detailData.materialCode }}</a-descriptions-item>
        <a-descriptions-item label="物料名称">{{ detailData.materialName }}</a-descriptions-item>
        <a-descriptions-item label="物料类型">{{ labelOf(materialTypeOptions, detailData.materialType) }}</a-descriptions-item>
        <a-descriptions-item label="物料分类">{{ detailData.materialCategory }}</a-descriptions-item>
        <a-descriptions-item label="规格型号">{{ detailData.specification || '-' }}</a-descriptions-item>
        <a-descriptions-item label="计量单位">{{ detailData.unit }}</a-descriptions-item>
        <a-descriptions-item label="品牌">{{ detailData.brand || '-' }}</a-descriptions-item>
        <a-descriptions-item label="状态">
          <a-tag :color="detailData.status === 1 ? 'success' : 'default'">
            {{ detailData.status === 1 ? '启用' : '禁用' }}
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
            <a-form-item label="物料分类">
              <a-input v-model:value="form.materialCategory" placeholder="请输入物料分类" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="规格型号">
              <a-input v-model:value="form.specification" placeholder="请输入规格型号" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="计量单位">
              <a-select v-model:value="form.unit" placeholder="请选择计量单位" allow-clear show-search>
                <a-select-option v-for="item in unitOptions" :key="item.id" :value="item.unitName">
                  {{ item.unitName }} ({{ item.unitSymbol }})
                </a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="品牌">
              <a-input v-model:value="form.brand" placeholder="请输入品牌" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="状态">
              <a-select v-model:value="form.status" style="width: 100%">
                <a-select-option :value="1">启用</a-select-option>
                <a-select-option :value="0">禁用</a-select-option>
              </a-select>
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
import { ref, reactive, onMounted } from 'vue';
import { message, Modal } from 'ant-design-vue';
import { PlusOutlined } from '@ant-design/icons-vue';
import { materialApi } from '@/api/basic/material';
import { getAllUnits } from '@/api/basic/unit';
import FxDynamicTable from '@/components/common/FxDynamicTable.vue';

const tableRef = ref();
const dialogVisible = ref(false);
const detailVisible = ref(false);
const saving = ref(false);
const isEdit = ref(false);
const detailData = ref<any>({});
const unitOptions = ref<any[]>([]);

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
});

const materialTypeOptions = [
  { value: 'RAW_MATERIAL', label: '原材料' },
  { value: 'SEMI_FINISHED', label: '半成品' },
  { value: 'FINISHED_GOODS', label: '成品' },
  { value: 'OTHER', label: '其它' },
];

const approvalStatusOptions = [
  { value: 'NO_APPROVAL_REQUIRED', label: '无需审批' },
  { value: 'PENDING', label: '未审批' },
  { value: 'APPROVED', label: '已审批' },
  { value: 'REJECTED', label: '已驳回' },
];

const tableFallbackConfig = ref({
  columns: [
    { title: '物料编码', dataIndex: 'materialCode', key: 'materialCode', width: 120 },
    { title: '物料名称', dataIndex: 'materialName', key: 'materialName', width: 150 },
    { title: '规格型号', dataIndex: 'specification', key: 'specification', width: 150, ellipsis: true },
    { title: '计量单位', dataIndex: 'unit', key: 'unit', width: 80 },
    { title: '品牌', dataIndex: 'brand', key: 'brand', width: 100 },
    { title: '状态', dataIndex: 'status', key: 'status', slotName: 'status', width: 80 },
    { title: '审批状态', dataIndex: 'approvalStatus', key: 'approvalStatus', slotName: 'approvalStatus', width: 100 },
    { title: '备注', dataIndex: 'remark', key: 'remark', width: 150, ellipsis: true },
    { title: '操作', key: 'action', slotName: 'action', width: 180, fixed: 'right' },
  ],
});

function handleRequest(params: any) {
  return materialApi.page({
    pageNum: params.pageNum || 1,
    pageSize: params.pageSize || 10,
    materialType: 'FINISHED_GOODS',
    ...params,
  });
}

function labelOf(options: any[], value: any) {
  const item = options.find(o => o.value === value);
  return item ? item.label : (value || '-');
}

function approvalStatusColor(status: string) {
  const colorMap: Record<string, string> = {
    'NO_APPROVAL_REQUIRED': 'default',
    'PENDING': 'warning',
    'APPROVED': 'success',
    'REJECTED': 'error',
  };
  return colorMap[status] || 'default';
}

function resetForm() {
  form.id = null;
  form.materialCode = '';
  form.materialName = '';
  form.materialType = 'FINISHED_GOODS';
  form.materialCategory = '';
  form.specification = '';
  form.unit = '';
  form.brand = '';
  form.status = 1;
  form.remark = '';
  form.description = '';
}

const dialogTitle = ref('');

function openCreate() {
  resetForm();
  isEdit.value = false;
  dialogTitle.value = '新增成品';
  dialogVisible.value = true;
}

function openEdit(record: any) {
  resetForm();
  Object.assign(form, record);
  isEdit.value = true;
  dialogTitle.value = '编辑成品';
  dialogVisible.value = true;
}

function openDetail(record: any) {
  detailData.value = record;
  detailVisible.value = true;
}

async function handleSave() {
  if (!form.materialCode || !form.materialName) {
    message.warning('请填写必填项');
    return;
  }
  saving.value = true;
  try {
    if (isEdit.value) {
      await materialApi.update(form);
      message.success('更新成功');
    } else {
      await materialApi.create(form);
      message.success('创建成功');
    }
    dialogVisible.value = false;
    tableRef.value?.refresh?.();
  } finally {
    saving.value = false;
  }
}

function handleDelete(record: any) {
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除成品"${record.materialName}"吗？`,
    onOk: async () => {
      await materialApi.delete({ id: record.id });
      message.success('删除成功');
      tableRef.value?.refresh?.();
    },
  });
}

onMounted(async () => {
  try {
    const res = await getAllUnits();
    unitOptions.value = res.data || [];
  } catch (e) {
    unitOptions.value = [];
  }
});
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
  margin-bottom: 24px;
}
.page-header h1 {
  margin: 8px 0 4px;
  font-size: 20px;
  font-weight: 600;
}
.page-header p {
  color: #666;
  font-size: 14px;
}
.danger-link {
  color: #ff4d4f;
}
.material-page :deep(.fx-dynamic-table) {
  flex: 1 1 auto;
  min-height: 0;
}
</style>
