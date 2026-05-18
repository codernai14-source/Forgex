<template>
  <div class="basic-master-page">
    <div class="basic-master-header">
      <div>
        <a-tag color="blue">Basic MDM</a-tag>
        <h1>人员管理</h1>
        <p>维护工号、姓名和组织信息，可按工号幂等同步为系统登录用户。</p>
      </div>
      <a-space wrap>
        <a-button v-permission="'basic:employee:pullThirdParty'" @click="handlePullThirdParty">从第三方拉取</a-button>
        <a-button v-permission="'basic:employee:sync'" @click="handleSyncThirdParty">同步第三方</a-button>
        <a-button v-permission="'basic:employee:add'" type="primary" @click="openEditor()">新增人员</a-button>
      </a-space>
    </div>

    <FxDynamicTable ref="tableRef" table-code="BasicEmployeeTable" :request="handleRequest" :row-selection="rowSelection" row-key="id">
      <template #toolbar>
        <a-button
          v-permission="'basic:employee:batchDelete'"
          danger
          :disabled="!selectedCount"
          @click="handleBatchDelete"
        >
          {{ t('common.batchDelete') }}
        </a-button>
      </template>
      <template #employeeName="{ record }">
        <div class="master-name">
          <strong>{{ record.employeeName }}</strong>
          <span>{{ record.employeeNo }}</span>
        </div>
      </template>
      <template #gender="{ record }">{{ genderLabel(record.gender) }}</template>
      <template #departmentName="{ record }">{{ record.departmentName || '-' }}</template>
      <template #positionName="{ record }">{{ record.positionName || '-' }}</template>
      <template #status="{ record }">
        <a-tag :color="record.status ? 'green' : 'red'">{{ record.status ? '启用' : '禁用' }}</a-tag>
      </template>
      <template #userId="{ record }">
        <a-tag :color="record.userId ? 'green' : 'default'">{{ record.userId ? '已同步' : '未同步' }}</a-tag>
      </template>
      <template #action="{ record }">
        <a-space>
          <a v-permission="'basic:employee:syncUser'" @click="handleSyncUser(record)">同步用户</a>
          <a v-permission="'basic:employee:edit'" @click="openEditor(record)">编辑</a>
          <a v-permission="'basic:employee:delete'" class="danger-link" @click="handleDelete(record)">删除</a>
        </a-space>
      </template>
    </FxDynamicTable>

    <BaseFormDialog v-model:open="visible" :title="form.id ? '编辑人员' : '新增人员'" width="820px" :loading="saving" @submit="handleSave">
      <a-form layout="vertical" :model="form">
        <a-row :gutter="16">
          <a-col :span="8"><a-form-item label="工号" required><a-input v-model:value="form.employeeNo" :disabled="!!form.id" /></a-form-item></a-col>
          <a-col :span="8"><a-form-item label="姓名" required><a-input v-model:value="form.employeeName" /></a-form-item></a-col>
          <a-col :span="8"><a-form-item label="性别"><a-select v-model:value="form.gender" :options="genderOptions" allow-clear /></a-form-item></a-col>
          <a-col :span="8"><a-form-item label="手机"><a-input v-model:value="form.phone" /></a-form-item></a-col>
          <a-col :span="8"><a-form-item label="邮箱"><a-input v-model:value="form.email" /></a-form-item></a-col>
          <a-col :span="8"><a-form-item label="入职日期"><a-date-picker v-model:value="form.entryDate" value-format="YYYY-MM-DD" class="full-width" /></a-form-item></a-col>
          <a-col :span="8"><a-form-item label="部门"><a-select v-model:value="form.departmentId" :options="departmentOptions" allow-clear show-search option-filter-prop="label" /></a-form-item></a-col>
          <a-col :span="8"><a-form-item label="岗位"><a-select v-model:value="form.positionId" :options="positionOptions" allow-clear show-search option-filter-prop="label" /></a-form-item></a-col>
          <a-col :span="8"><a-form-item label="是否启用"><a-switch v-model:checked="form.status" /></a-form-item></a-col>
          <a-col :span="24"><a-form-item label="头像地址"><a-input v-model:value="form.avatar" /></a-form-item></a-col>
          <a-col :span="24"><a-form-item label="备注"><a-textarea v-model:value="form.remark" :rows="3" /></a-form-item></a-col>
        </a-row>
      </a-form>
    </BaseFormDialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { Modal, message } from 'ant-design-vue'
import BaseFormDialog from '@/components/common/BaseFormDialog.vue'
import FxDynamicTable from '@/components/common/FxDynamicTable.vue'
import { useBatchTableSelection } from '@/hooks/useBatchTableSelection'
import { listDepartments } from '@/api/system/department'
import { listPositions } from '@/api/system/position'
import { employeeApi, type Employee } from '@/api/basic/employee'

const { t } = useI18n()
const tableRef = ref()
const { selectedRowKeys, selectedCount, rowSelection, clearSelection } = useBatchTableSelection<number>()
const visible = ref(false)
const saving = ref(false)
const form = ref<Employee>(emptyForm())
const departmentOptions = ref<{ label: string; value: number }[]>([])
const positionOptions = ref<{ label: string; value: number }[]>([])
const genderOptions = [{ label: '未知', value: 0 }, { label: '男', value: 1 }, { label: '女', value: 2 }]

function emptyForm(): Employee {
  return { employeeNo: '', employeeName: '', gender: 0, status: true }
}

async function handleRequest(payload: any) {
  const result = await employeeApi.page({ pageNum: payload.page.current, pageSize: payload.page.pageSize, ...payload.query })
  return { records: result.records || [], total: Number(result.total || 0) }
}

function genderLabel(value?: number) {
  return genderOptions.find(item => item.value === value)?.label || '-'
}

async function loadOptions() {
  const [departments, positions] = await Promise.all([listDepartments({}), listPositions({})])
  departmentOptions.value = Array.isArray(departments) ? departments.map((item: any) => ({ label: item.deptName, value: item.id })) : []
  positionOptions.value = Array.isArray(positions) ? positions.map((item: any) => ({ label: item.positionName, value: item.id })) : []
}

function openEditor(record?: Employee) {
  form.value = record ? { ...record } : emptyForm()
  visible.value = true
}

async function handleSave() {
  saving.value = true
  try {
    form.value.id ? await employeeApi.update(form.value) : await employeeApi.create(form.value)
    visible.value = false
    await tableRef.value?.refresh?.()
  } finally {
    saving.value = false
  }
}

async function handleSyncUser(record: Employee) {
  const result = await employeeApi.syncUser(record.id!)
  message.success(`同步完成：新增 ${result.createdCount}，更新 ${result.updatedCount}`)
  await tableRef.value?.refresh?.()
}

async function handleSyncThirdParty() {
  const result = await employeeApi.syncThirdParty()
  message.success(`同步第三方完成：共 ${result.totalCount} 条`)
}

async function handlePullThirdParty() {
  const result = await employeeApi.pullFromThirdParty()
  message.success(`第三方拉取完成：共 ${result.totalCount} 条`)
  await tableRef.value?.refresh?.()
}

function handleDelete(record: Employee) {
  Modal.confirm({
    title: '确认删除该人员？',
    async onOk() {
      await employeeApi.delete(record.id!)
      await tableRef.value?.refresh?.()
    },
  })
}

function handleBatchDelete() {
  if (!selectedCount.value) return
  Modal.confirm({
    title: t('common.confirmBatchDelete'),
    content: t('common.confirmBatchDeleteMessage', { count: selectedCount.value }),
    async onOk() {
      await employeeApi.batchDelete(selectedRowKeys.value)
      clearSelection()
      await tableRef.value?.refresh?.()
    },
  })
}

onMounted(loadOptions)
</script>

<style scoped lang="less" src="@/styles/views/basic/masterData/index.less"></style>
