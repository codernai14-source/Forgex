<template>
  <div class="basic-master-page">
    <div class="basic-master-header">
      <div>
        <a-tag color="blue">Basic MDM</a-tag>
        <h1>车间管理</h1>
        <p>维护车间编码、所属工厂和启用状态，为班组归属提供统一主数据。</p>
      </div>
      <a-space wrap>
        <a-button v-permission="'basic:workshop:add'" type="primary" @click="openEditor()">新增车间</a-button>
      </a-space>
    </div>

    <FxDynamicTable ref="tableRef" table-code="BasicWorkshopTable" :request="handleRequest" :row-selection="rowSelection" row-key="id">
      <template #toolbar>
        <a-button
          v-permission="'basic:workshop:batchDelete'"
          danger
          :disabled="!selectedCount"
          @click="handleBatchDelete"
        >
          {{ t('common.batchDelete') }}
        </a-button>
      </template>
      <template #workshopName="{ record }">
        <div class="master-name">
          <strong>{{ record.workshopName }}</strong>
          <span>{{ record.workshopCode }}</span>
        </div>
      </template>
      <template #factoryName="{ record }">{{ record.factoryName || '-' }}</template>
      <template #status="{ record }">
        <a-tag :color="record.status ? 'green' : 'red'">{{ record.status ? '启用' : '禁用' }}</a-tag>
      </template>
      <template #action="{ record }">
        <a-space>
          <a v-permission="'basic:workshop:edit'" @click="openEditor(record)">编辑</a>
          <a v-permission="'basic:workshop:delete'" class="danger-link" @click="handleDelete(record)">删除</a>
        </a-space>
      </template>
    </FxDynamicTable>

    <BaseFormDialog v-model:open="visible" :title="form.id ? '编辑车间' : '新增车间'" width="720px" :loading="saving" @submit="handleSave">
      <a-form layout="vertical" :model="form">
        <a-row :gutter="16">
          <a-col :span="12"><a-form-item label="车间编码" required><a-input v-model:value="form.workshopCode" :disabled="!!form.id" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="车间名称" required><a-input v-model:value="form.workshopName" /></a-form-item></a-col>
          <a-col :span="12">
            <a-form-item label="所属工厂">
              <a-select v-model:value="form.factoryId" allow-clear show-search option-filter-prop="label" :options="factoryOptions" />
            </a-form-item>
          </a-col>
          <a-col :span="12"><a-form-item label="是否启用"><a-switch v-model:checked="form.status" /></a-form-item></a-col>
          <a-col :span="24"><a-form-item label="备注"><a-textarea v-model:value="form.remark" :rows="3" /></a-form-item></a-col>
        </a-row>
      </a-form>
    </BaseFormDialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { Modal } from 'ant-design-vue'
import BaseFormDialog from '@/components/common/BaseFormDialog.vue'
import FxDynamicTable from '@/components/common/FxDynamicTable.vue'
import { useBatchTableSelection } from '@/hooks/useBatchTableSelection'
import { factoryApi } from '@/api/basic/factory'
import { workshopApi, type Workshop } from '@/api/basic/workshop'

const { t } = useI18n()
const tableRef = ref()
const { selectedRowKeys, selectedCount, rowSelection, clearSelection } = useBatchTableSelection<number>()
const visible = ref(false)
const saving = ref(false)
const form = ref<Workshop>(emptyForm())
const factoryOptions = ref<{ label: string; value: number }[]>([])

function emptyForm(): Workshop {
  return { workshopCode: '', workshopName: '', status: true }
}

async function handleRequest(payload: any) {
  const result = await workshopApi.page({ pageNum: payload.page.current, pageSize: payload.page.pageSize, ...payload.query })
  return { records: result.records || [], total: Number(result.total || 0) }
}

async function loadFactories() {
  const list = await factoryApi.list({ status: 1 })
  factoryOptions.value = Array.isArray(list) ? list.map((item: any) => ({ label: `${item.factoryName} (${item.factoryCode})`, value: item.id })) : []
}

function openEditor(record?: Workshop) {
  form.value = record ? { ...record } : emptyForm()
  visible.value = true
}

async function handleSave() {
  saving.value = true
  try {
    form.value.id ? await workshopApi.update(form.value) : await workshopApi.create(form.value)
    visible.value = false
    await tableRef.value?.refresh?.()
  } finally {
    saving.value = false
  }
}

function handleDelete(record: Workshop) {
  Modal.confirm({
    title: '确认删除该车间？',
    async onOk() {
      await workshopApi.delete(record.id!)
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
      await workshopApi.batchDelete(selectedRowKeys.value)
      clearSelection()
      await tableRef.value?.refresh?.()
    },
  })
}

onMounted(loadFactories)
</script>

<style scoped lang="less" src="@/styles/views/basic/masterData/index.less"></style>
