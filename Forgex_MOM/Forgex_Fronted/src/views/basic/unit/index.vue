<template>
  <div class="unit-page">
    <div class="page-header">
      <div>
        <a-tag color="blue">基础数据</a-tag>
        <h1>计量单位管理</h1>
        <p>统一管理物料的计量单位，支持多分类和换算比率配置。</p>
      </div>
      <a-space>
        <a-button v-permission="'basic:unit:add'" type="primary" @click="openCreate">
          <PlusOutlined /> 新增单位
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
      <template #status="{ record }">
        <a-tag :color="record.status === 1 ? 'success' : 'default'">
          {{ record.status === 1 ? '启用' : '禁用' }}
        </a-tag>
      </template>

      <template #action="{ record }">
        <a-space>
          <a v-permission="'basic:unit:edit'" @click="openEdit(record)">编辑</a>
          <a v-permission="'basic:unit:delete'" class="danger-link" @click="handleDelete(record)">删除</a>
        </a-space>
      </template>
    </FxDynamicTable>

    <a-modal
      v-model:open="dialogVisible"
      :title="dialogTitle"
      width="600px"
      :confirm-loading="saving"
      @ok="handleSave"
      @cancel="dialogVisible = false"
    >
      <a-form :model="form" layout="vertical">
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="单位编码" required>
              <a-input v-model:value="form.unitCode" placeholder="请输入单位编码" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="单位名称" required>
              <a-input v-model:value="form.unitName" placeholder="请输入单位名称" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="单位符号">
              <a-input v-model:value="form.unitSymbol" placeholder="如: kg, m, L" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="单位分类">
              <a-select v-model:value="form.unitCategory" placeholder="请选择分类" allow-clear>
                <a-select-option value="length">长度</a-select-option>
                <a-select-option value="weight">重量</a-select-option>
                <a-select-option value="volume">体积</a-select-option>
                <a-select-option value="area">面积</a-select-option>
                <a-select-option value="quantity">数量</a-select-option>
                <a-select-option value="time">时间</a-select-option>
                <a-select-option value="other">其它</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="换算比率">
              <a-input-number v-model:value="form.conversionRate" :min="0" :precision="6" style="width: 100%" placeholder="相对于标准单位的比率" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="排序号">
              <a-input-number v-model:value="form.sortOrder" :min="0" style="width: 100%" />
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
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue';
import { message, Modal } from 'ant-design-vue';
import { PlusOutlined } from '@ant-design/icons-vue';
import { createUnit, updateUnit, deleteUnit, getUnitPage } from '@/api/basic/unit';
import FxDynamicTable from '@/components/FxDynamicTable/index.vue';

const tableRef = ref();
const dialogVisible = ref(false);
const saving = ref(false);
const isEdit = ref(false);

const form = reactive<any>({
  id: null,
  unitCode: '',
  unitName: '',
  unitSymbol: '',
  unitCategory: '',
  conversionRate: 1,
  status: 1,
  sortOrder: 0,
  remark: '',
});

const tableFallbackConfig = ref({
  columns: [
    { title: '单位编码', dataIndex: 'unitCode', key: 'unitCode', width: 120 },
    { title: '单位名称', dataIndex: 'unitName', key: 'unitName', width: 120 },
    { title: '单位符号', dataIndex: 'unitSymbol', key: 'unitSymbol', width: 100 },
    { title: '单位分类', dataIndex: 'unitCategory', key: 'unitCategory', width: 100 },
    { title: '换算比率', dataIndex: 'conversionRate', key: 'conversionRate', width: 100 },
    { title: '排序', dataIndex: 'sortOrder', key: 'sortOrder', width: 80 },
    { title: '状态', dataIndex: 'status', key: 'status', slotName: 'status', width: 80 },
    { title: '备注', dataIndex: 'remark', key: 'remark', ellipsis: true },
    { title: '操作', key: 'action', slotName: 'action', width: 150, fixed: 'right' },
  ],
});

function handleRequest(params: any) {
  return getUnitPage({
    pageNum: params.pageNum || 1,
    pageSize: params.pageSize || 10,
    ...params,
  });
}

function resetForm() {
  form.id = null;
  form.unitCode = '';
  form.unitName = '';
  form.unitSymbol = '';
  form.unitCategory = '';
  form.conversionRate = 1;
  form.status = 1;
  form.sortOrder = 0;
  form.remark = '';
}

const dialogTitle = ref('');

function openCreate() {
  resetForm();
  isEdit.value = false;
  dialogTitle.value = '新增计量单位';
  dialogVisible.value = true;
}

function openEdit(record: any) {
  resetForm();
  Object.assign(form, record);
  isEdit.value = true;
  dialogTitle.value = '编辑计量单位';
  dialogVisible.value = true;
}

async function handleSave() {
  if (!form.unitCode || !form.unitName) {
    message.warning('请填写必填项');
    return;
  }
  saving.value = true;
  try {
    if (isEdit.value) {
      await updateUnit(form);
      message.success('更新成功');
    } else {
      await createUnit(form);
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
    content: `确定要删除计量单位"${record.unitName}"吗？`,
    onOk: async () => {
      await deleteUnit([record.id]);
      message.success('删除成功');
      tableRef.value?.refresh?.();
    },
  });
}
</script>

<style scoped>
.unit-page {
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
