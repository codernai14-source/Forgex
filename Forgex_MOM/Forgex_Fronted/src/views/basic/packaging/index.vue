<template>
  <div class="packaging-page">
    <div class="page-header">
      <div>
        <a-tag color="blue">基础数据</a-tag>
        <h1>包装方式管理</h1>
        <p>统一维护物料的包装方式信息，支持多种包装规格和材质配置。</p>
      </div>
      <a-space>
        <a-button v-permission="'basic:packaging:add'" type="primary" @click="openCreate">
          <PlusOutlined /> 新增包装
        </a-button>
      </a-space>
    </div>

    <FxDynamicTable
      ref="tableRef"
      table-code="BasicPackagingTypeTable"
      :request="handleRequest"
      :fallback-config="tableFallbackConfig"
      row-key="id"
    >
      <template #status="{ record }">
        <a-tag :color="record.status === 1 ? 'success' : 'default'">
          {{ record.status === 1 ? '启用' : '禁用' }}
        </a-tag>
      </template>

      <template #action="{ record }">
        <a-space>
          <a v-permission="'basic:packaging:edit'" @click="openEdit(record)">编辑</a>
          <a v-permission="'basic:packaging:delete'" class="danger-link" @click="handleDelete(record)">删除</a>
        </a-space>
      </template>
    </FxDynamicTable>

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
            <a-form-item label="包装方式编码" required>
              <a-input v-model:value="form.packagingCode" placeholder="请输入包装方式编码" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="包装方式名称" required>
              <a-input v-model:value="form.packagingName" placeholder="请输入包装方式名称" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="包装材料">
              <a-select v-model:value="form.packagingMaterial" placeholder="请选择包装材料" allow-clear>
                <a-select-option value="carton">纸箱</a-select-option>
                <a-select-option value="wooden_box">木箱</a-select-option>
                <a-select-option value="pallet">托盘</a-select-option>
                <a-select-option value="iron_drum">铁桶</a-select-option>
                <a-select-option value="plastic_bag">塑料袋</a-select-option>
                <a-select-option value="bubble_wrap">气泡膜</a-select-option>
                <a-select-option value="other">其它</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="单位成本">
              <a-input-number v-model:value="form.unitCost" :min="0" :precision="2" style="width: 100%" placeholder="单位成本" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="8">
            <a-form-item label="长度(mm)">
              <a-input-number v-model:value="form.lengthMm" :min="0" :precision="2" style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="宽度(mm)">
              <a-input-number v-model:value="form.widthMm" :min="0" :precision="2" style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="高度(mm)">
              <a-input-number v-model:value="form.heightMm" :min="0" :precision="2" style="width: 100%" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="包装自重(kg)">
              <a-input-number v-model:value="form.weightKg" :min="0" :precision="2" style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="最大承重(kg)">
              <a-input-number v-model:value="form.maxLoadKg" :min="0" :precision="2" style="width: 100%" />
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
          <a-col :span="12">
            <a-form-item label="排序号">
              <a-input-number v-model:value="form.sortOrder" :min="0" style="width: 100%" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item label="备注">
          <a-textarea v-model:value="form.remark" :rows="2" placeholder="请输入备注" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue';
import { message, Modal } from 'ant-design-vue';
import { PlusOutlined } from '@ant-design/icons-vue';
import { createPackagingType, updatePackagingType, deletePackagingType, getPackagingTypePage } from '@/api/basic/packaging';
import FxDynamicTable from '@/components/FxDynamicTable/index.vue';

const tableRef = ref();
const dialogVisible = ref(false);
const saving = ref(false);
const isEdit = ref(false);

const form = reactive<any>({
  id: null,
  packagingCode: '',
  packagingName: '',
  packagingMaterial: '',
  lengthMm: null,
  widthMm: null,
  heightMm: null,
  weightKg: null,
  maxLoadKg: null,
  unitCost: null,
  status: 1,
  sortOrder: 0,
  remark: '',
});

const tableFallbackConfig = ref({
  columns: [
    { title: '包装编码', dataIndex: 'packagingCode', key: 'packagingCode', width: 120 },
    { title: '包装名称', dataIndex: 'packagingName', key: 'packagingName', width: 120 },
    { title: '包装材料', dataIndex: 'packagingMaterial', key: 'packagingMaterial', width: 100 },
    { title: '尺寸(mm)', key: 'dimensions', width: 150, customRender: ({ record }: any) => {
      const parts = [record.lengthMm, record.widthMm, record.heightMm].filter(Boolean);
      return parts.length ? parts.join(' × ') : '-';
    }},
    { title: '自重(kg)', dataIndex: 'weightKg', key: 'weightKg', width: 90 },
    { title: '承重(kg)', dataIndex: 'maxLoadKg', key: 'maxLoadKg', width: 90 },
    { title: '单位成本', dataIndex: 'unitCost', key: 'unitCost', width: 100 },
    { title: '排序', dataIndex: 'sortOrder', key: 'sortOrder', width: 80 },
    { title: '状态', dataIndex: 'status', key: 'status', slotName: 'status', width: 80 },
    { title: '备注', dataIndex: 'remark', key: 'remark', ellipsis: true },
    { title: '操作', key: 'action', slotName: 'action', width: 150, fixed: 'right' },
  ],
});

function handleRequest(params: any) {
  return getPackagingTypePage({
    pageNum: params.pageNum || 1,
    pageSize: params.pageSize || 10,
    ...params,
  });
}

function resetForm() {
  form.id = null;
  form.packagingCode = '';
  form.packagingName = '';
  form.packagingMaterial = '';
  form.lengthMm = null;
  form.widthMm = null;
  form.heightMm = null;
  form.weightKg = null;
  form.maxLoadKg = null;
  form.unitCost = null;
  form.status = 1;
  form.sortOrder = 0;
  form.remark = '';
}

const dialogTitle = ref('');

function openCreate() {
  resetForm();
  isEdit.value = false;
  dialogTitle.value = '新增包装方式';
  dialogVisible.value = true;
}

function openEdit(record: any) {
  resetForm();
  Object.assign(form, record);
  isEdit.value = true;
  dialogTitle.value = '编辑包装方式';
  dialogVisible.value = true;
}

async function handleSave() {
  if (!form.packagingCode || !form.packagingName) {
    message.warning('请填写必填项');
    return;
  }
  saving.value = true;
  try {
    if (isEdit.value) {
      await updatePackagingType(form);
      message.success('更新成功');
    } else {
      await createPackagingType(form);
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
    content: `确定要删除包装方式"${record.packagingName}"吗？`,
    onOk: async () => {
      await deletePackagingType([record.id]);
      message.success('删除成功');
      tableRef.value?.refresh?.();
    },
  });
}
</script>

<style scoped>
.packaging-page {
  padding: 24px;
}
.page-header {
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
</style>
