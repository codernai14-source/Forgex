<template>
  <div class="basic-master-page">
    <div class="basic-master-header">
      <div>
        <a-tag color="blue">Basic MDM</a-tag>
        <h1>班组管理</h1>
        <p>维护班组负责人、当前班次、所属车间和班组人员清单。</p>
      </div>
      <a-space wrap>
        <a-button v-permission="'basic:team:add'" type="primary" @click="openEditor()">新增班组</a-button>
      </a-space>
    </div>

    <FxDynamicTable ref="tableRef" table-code="BasicTeamTable" :request="handleRequest" :row-selection="rowSelection" row-key="id">
      <template #toolbar>
        <a-button
          v-permission="'basic:team:batchDelete'"
          danger
          :disabled="!selectedCount"
          @click="handleBatchDelete"
        >
          {{ t('common.batchDelete') }}
        </a-button>
      </template>
      <template #teamName="{ record }">
        <div class="master-name">
          <strong>{{ record.teamName }}</strong>
          <span>{{ record.teamCode }}</span>
        </div>
      </template>
      <template #leaderEmployeeName="{ record }">{{ record.leaderEmployeeName || '-' }}</template>
      <template #currentShiftName="{ record }">{{ record.currentShiftName || '-' }}</template>
      <template #workshopName="{ record }">{{ record.workshopName || '-' }}</template>
      <template #status="{ record }">
        <a-tag :color="record.status ? 'green' : 'red'">{{ record.status ? '启用' : '禁用' }}</a-tag>
      </template>
      <template #action="{ record }">
        <a-space>
          <a v-permission="'basic:team:query'" @click="openDetail(record)">详情</a>
          <a v-permission="'basic:team:edit'" @click="openEditor(record)">编辑</a>
          <a v-permission="'basic:team:delete'" class="danger-link" @click="handleDelete(record)">删除</a>
        </a-space>
      </template>
    </FxDynamicTable>

    <BaseFormDialog v-model:open="visible" :title="dialogTitle" width="980px" :loading="saving" @submit="handleSave">
      <a-form layout="vertical" :model="form">
        <a-row :gutter="16">
          <a-col :span="8"><a-form-item label="班组编码" required><a-input v-model:value="form.teamCode" :disabled="!!form.id || readonly" /></a-form-item></a-col>
          <a-col :span="8"><a-form-item label="班组名称" required><a-input v-model:value="form.teamName" :disabled="readonly" /></a-form-item></a-col>
          <a-col :span="8"><a-form-item label="是否启用"><a-switch v-model:checked="form.status" :disabled="readonly" /></a-form-item></a-col>
          <a-col :span="8"><a-form-item label="负责人"><a-select v-model:value="form.leaderEmployeeId" :disabled="readonly" :options="employeeOptions" allow-clear show-search option-filter-prop="label" /></a-form-item></a-col>
          <a-col :span="8"><a-form-item label="当前班次"><a-select v-model:value="form.currentShiftId" :disabled="readonly" :options="shiftOptions" allow-clear show-search option-filter-prop="label" /></a-form-item></a-col>
          <a-col :span="8"><a-form-item label="所属车间"><a-select v-model:value="form.workshopId" :disabled="readonly" :options="workshopOptions" allow-clear show-search option-filter-prop="label" /></a-form-item></a-col>
          <a-col :span="24"><a-form-item label="备注"><a-textarea v-model:value="form.remark" :rows="2" :disabled="readonly" /></a-form-item></a-col>
        </a-row>
      </a-form>

      <div class="inline-table-actions">
        <a-button :disabled="readonly" @click="addEmployee">新增人员</a-button>
      </div>
      <a-table :data-source="form.employeeList" :pagination="false" row-key="employeeId" size="small">
        <a-table-column title="人员" data-index="employeeId">
          <template #default="{ record }">
            <a-select
              v-model:value="record.employeeId"
              :disabled="readonly"
              :options="employeeOptions"
              allow-clear
              show-search
              option-filter-prop="label"
              class="full-width"
              @change="syncEmployeeLabel(record)"
            />
          </template>
        </a-table-column>
        <a-table-column title="工号" data-index="employeeNo" width="180" />
        <a-table-column title="姓名" data-index="employeeName" width="180" />
        <a-table-column title="操作" width="100">
          <template #default="{ index }">
            <a class="danger-link" :class="{ disabled: readonly }" @click="removeEmployee(index)">删除</a>
          </template>
        </a-table-column>
      </a-table>
    </BaseFormDialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { Modal } from 'ant-design-vue'
import BaseFormDialog from '@/components/common/BaseFormDialog.vue'
import FxDynamicTable from '@/components/common/FxDynamicTable.vue'
import { useBatchTableSelection } from '@/hooks/useBatchTableSelection'
import { employeeApi, type Employee } from '@/api/basic/employee'
import { shiftApi } from '@/api/basic/shift'
import { teamApi, type Team, type TeamEmployee } from '@/api/basic/team'
import { workshopApi } from '@/api/basic/workshop'

const { t } = useI18n()
const tableRef = ref()
const { selectedRowKeys, selectedCount, rowSelection, clearSelection } = useBatchTableSelection<number>()
const visible = ref(false)
const saving = ref(false)
const readonly = ref(false)
const form = ref<Team>(emptyForm())
const employees = ref<Employee[]>([])
const employeeOptions = ref<{ label: string; value: number }[]>([])
const shiftOptions = ref<{ label: string; value: number }[]>([])
const workshopOptions = ref<{ label: string; value: number }[]>([])
const dialogTitle = computed(() => readonly.value ? '班组详情' : form.value.id ? '编辑班组' : '新增班组')

function emptyForm(): Team {
  return { teamCode: '', teamName: '', status: true, employeeList: [] }
}

async function handleRequest(payload: any) {
  const result = await teamApi.page({ pageNum: payload.page.current, pageSize: payload.page.pageSize, ...payload.query })
  return { records: result.records || [], total: Number(result.total || 0) }
}

async function loadOptions() {
  const [employeeList, shiftList, workshopList] = await Promise.all([
    employeeApi.list({ status: true }),
    shiftApi.list({ status: true }),
    workshopApi.list({ status: true }),
  ])
  employees.value = Array.isArray(employeeList) ? employeeList : []
  employeeOptions.value = employees.value.map(item => ({ label: `${item.employeeName} (${item.employeeNo})`, value: item.id! }))
  shiftOptions.value = Array.isArray(shiftList) ? shiftList.map(item => ({ label: `${item.shiftName} (${item.shiftCode})`, value: item.id! })) : []
  workshopOptions.value = Array.isArray(workshopList) ? workshopList.map(item => ({ label: `${item.workshopName} (${item.workshopCode})`, value: item.id! })) : []
}

async function openEditor(record?: Team) {
  readonly.value = false
  form.value = record?.id ? await teamApi.detail(record.id) : emptyForm()
  form.value.employeeList ||= []
  visible.value = true
}

async function openDetail(record: Team) {
  readonly.value = true
  form.value = await teamApi.detail(record.id!)
  form.value.employeeList ||= []
  visible.value = true
}

function addEmployee() {
  form.value.employeeList = [...(form.value.employeeList || []), {}]
}

function removeEmployee(index: number) {
  if (readonly.value) return
  form.value.employeeList?.splice(index, 1)
}

function syncEmployeeLabel(record: TeamEmployee) {
  const employee = employees.value.find(item => item.id === record.employeeId)
  record.employeeNo = employee?.employeeNo
  record.employeeName = employee?.employeeName
}

async function handleSave() {
  if (readonly.value) {
    visible.value = false
    return
  }
  saving.value = true
  try {
    form.value.id ? await teamApi.update(form.value) : await teamApi.create(form.value)
    visible.value = false
    await tableRef.value?.refresh?.()
  } finally {
    saving.value = false
  }
}

function handleDelete(record: Team) {
  Modal.confirm({
    title: '确认删除该班组？',
    async onOk() {
      await teamApi.delete(record.id!)
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
      await teamApi.batchDelete(selectedRowKeys.value)
      clearSelection()
      await tableRef.value?.refresh?.()
    },
  })
}

onMounted(loadOptions)
</script>

<style scoped lang="less" src="@/styles/views/basic/masterData/index.less"></style>
