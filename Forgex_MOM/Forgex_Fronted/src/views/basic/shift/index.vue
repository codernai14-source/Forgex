<template>
  <div class="basic-master-page">
    <div class="basic-master-header">
      <div>
        <a-tag color="blue">Basic MDM</a-tag>
        <h1>班次管理</h1>
        <p>维护班次主数据和工作/休息时段，结束时间早于开始时间时按跨天理解。</p>
      </div>
      <a-space wrap>
        <a-button v-permission="'basic:shift:add'" type="primary" @click="openEditor()">新增班次</a-button>
      </a-space>
    </div>

    <FxDynamicTable ref="tableRef" table-code="BasicShiftTable" :request="handleRequest" row-key="id">
      <template #shiftName="{ record }">
        <div class="master-name">
          <strong>{{ record.shiftName }}</strong>
          <span>{{ record.shiftCode }}</span>
        </div>
      </template>
      <template #status="{ record }">
        <a-tag :color="record.status ? 'green' : 'red'">{{ record.status ? '启用' : '禁用' }}</a-tag>
      </template>
      <template #action="{ record }">
        <a-space>
          <a v-permission="'basic:shift:query'" @click="openDetail(record)">详情</a>
          <a v-permission="'basic:shift:edit'" @click="openEditor(record)">编辑</a>
          <a v-permission="'basic:shift:delete'" class="danger-link" @click="handleDelete(record)">删除</a>
        </a-space>
      </template>
    </FxDynamicTable>

    <BaseFormDialog v-model:open="visible" :title="dialogTitle" width="900px" :loading="saving" @submit="handleSave">
      <a-form layout="vertical" :model="form">
        <a-row :gutter="16">
          <a-col :span="8"><a-form-item label="班次编码" required><a-input v-model:value="form.shiftCode" :disabled="!!form.id || readonly" /></a-form-item></a-col>
          <a-col :span="8"><a-form-item label="班次名称" required><a-input v-model:value="form.shiftName" :disabled="readonly" /></a-form-item></a-col>
          <a-col :span="8"><a-form-item label="是否启用"><a-switch v-model:checked="form.status" :disabled="readonly" /></a-form-item></a-col>
          <a-col :span="24"><a-form-item label="备注"><a-textarea v-model:value="form.remark" :rows="2" :disabled="readonly" /></a-form-item></a-col>
        </a-row>
      </a-form>

      <div class="inline-table-actions">
        <a-button :disabled="readonly" @click="addPeriod">新增时段</a-button>
      </div>
      <a-table :data-source="form.periodList" :pagination="false" row-key="sortOrder" size="small">
        <a-table-column title="类型" data-index="timeType" width="180">
          <template #default="{ record }">
            <a-select v-model:value="record.timeType" :disabled="readonly" :options="timeTypeOptions" />
          </template>
        </a-table-column>
        <a-table-column title="开始时间" data-index="startTime" width="200">
          <template #default="{ record }">
            <a-time-picker v-model:value="record.startTime" value-format="HH:mm:ss" format="HH:mm" :disabled="readonly" class="full-width" />
          </template>
        </a-table-column>
        <a-table-column title="结束时间" data-index="endTime" width="200">
          <template #default="{ record }">
            <a-time-picker v-model:value="record.endTime" value-format="HH:mm:ss" format="HH:mm" :disabled="readonly" class="full-width" />
          </template>
        </a-table-column>
        <a-table-column title="排序" data-index="sortOrder" width="120">
          <template #default="{ record }">
            <a-input-number v-model:value="record.sortOrder" :disabled="readonly" class="full-width" />
          </template>
        </a-table-column>
        <a-table-column title="操作" width="100">
          <template #default="{ index }">
            <a class="danger-link" :class="{ disabled: readonly }" @click="removePeriod(index)">删除</a>
          </template>
        </a-table-column>
      </a-table>
    </BaseFormDialog>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { Modal } from 'ant-design-vue'
import BaseFormDialog from '@/components/common/BaseFormDialog.vue'
import FxDynamicTable from '@/components/common/FxDynamicTable.vue'
import { shiftApi, type Shift, type ShiftPeriod } from '@/api/basic/shift'

const tableRef = ref()
const visible = ref(false)
const saving = ref(false)
const readonly = ref(false)
const form = ref<Shift>(emptyForm())
const timeTypeOptions = [{ label: '工作', value: 'WORK' }, { label: '休息', value: 'REST' }]
const dialogTitle = computed(() => readonly.value ? '班次详情' : form.value.id ? '编辑班次' : '新增班次')

function emptyForm(): Shift {
  return { shiftCode: '', shiftName: '', status: true, periodList: [] }
}

async function handleRequest(payload: any) {
  const result = await shiftApi.page({ pageNum: payload.page.current, pageSize: payload.page.pageSize, ...payload.query })
  return { records: result.records || [], total: Number(result.total || 0) }
}

async function openEditor(record?: Shift) {
  readonly.value = false
  form.value = record?.id ? await shiftApi.detail(record.id) : emptyForm()
  form.value.periodList ||= []
  visible.value = true
}

async function openDetail(record: Shift) {
  readonly.value = true
  form.value = await shiftApi.detail(record.id!)
  form.value.periodList ||= []
  visible.value = true
}

function addPeriod() {
  const next = (form.value.periodList?.length || 0) + 1
  form.value.periodList = [...(form.value.periodList || []), { timeType: 'WORK', startTime: '08:00:00', endTime: '17:00:00', sortOrder: next }]
}

function removePeriod(index: number) {
  if (readonly.value) return
  form.value.periodList?.splice(index, 1)
}

async function handleSave() {
  if (readonly.value) {
    visible.value = false
    return
  }
  saving.value = true
  try {
    form.value.id ? await shiftApi.update(form.value) : await shiftApi.create(form.value)
    visible.value = false
    await tableRef.value?.refresh?.()
  } finally {
    saving.value = false
  }
}

function handleDelete(record: Shift) {
  Modal.confirm({
    title: '确认删除该班次？',
    async onOk() {
      await shiftApi.delete(record.id!)
      await tableRef.value?.refresh?.()
    },
  })
}
</script>

<style scoped lang="less" src="@/styles/views/basic/masterData/index.less"></style>
