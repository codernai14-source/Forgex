<template>
  <div class="basic-master-page">
    <div class="basic-master-header">
      <div>
        <a-tag color="blue">Basic MDM</a-tag>
        <h1>{{ t('basic.process.title') }}</h1>
        <p>{{ t('basic.process.subtitle') }}</p>
      </div>
      <a-space wrap>
        <a-button v-permission="'basic:process:add'" type="primary" @click="openEditor()">{{ t('basic.process.addProcess') }}</a-button>
      </a-space>
    </div>

    <FxDynamicTable
      ref="tableRef"
      table-code="BasicProcessTable"
      :request="handleRequest"
      :row-selection="rowSelection"
      :dict-options="dictOptions"
      row-key="id"
    >
      <template #toolbar>
        <a-button
          v-permission="'basic:process:batchDelete'"
          danger
          :disabled="!selectedCount"
          @click="handleBatchDelete"
        >
          {{ t('common.batchDelete') }}
        </a-button>
      </template>
      <template #processName="{ record }">
        <div class="master-name">
          <strong>{{ record.processName }}</strong>
          <span>{{ record.processCode }}</span>
        </div>
      </template>
      <template #workSectionName="{ record }">
        <span>{{ record.workSectionName || '-' }}</span>
      </template>
      <template #processType="{ record }">
        <a-tag>{{ labelOf(processTypeOptions, record.processType) }}</a-tag>
      </template>
      <template #reportType="{ record }">
        <a-tag>{{ labelOf(reportTypeOptions, record.reportType) }}</a-tag>
      </template>
      <template #qcTriggerPoint="{ record }">
        <a-tag>{{ labelOf(qcTriggerPointOptions, record.qcTriggerPoint) }}</a-tag>
      </template>
      <template #status="{ record }">
        <a-tag :color="Number(record.status) === 1 ? 'green' : 'red'">{{ Number(record.status) === 1 ? t('common.enabled') : t('common.disabled') }}</a-tag>
      </template>
      <template #action="{ record }">
        <a-space>
          <a v-permission="'basic:process:edit'" @click="openEditor(record)">{{ t('common.edit') }}</a>
          <a v-permission="'basic:process:delete'" class="danger-link" @click="handleDelete(record)">{{ t('common.delete') }}</a>
        </a-space>
      </template>
    </FxDynamicTable>

    <BaseFormDialog
      v-model:open="visible"
      :title="form.id ? t('basic.process.editProcess') : t('basic.process.addProcess')"
      width="720px"
      :loading="saving"
      @submit="handleSave"
    >
      <a-form layout="vertical" :model="form">
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item :label="t('basic.process.fields.code')" required>
              <a-input v-model:value="form.processCode" :disabled="!!form.id" :placeholder="t('basic.process.placeholder.code')" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item :label="t('basic.process.fields.name')" required>
              <a-input v-model:value="form.processName" :placeholder="t('basic.process.placeholder.name')" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item :label="t('basic.process.fields.workSection')">
              <a-select
                v-model:value="form.workSectionId"
                allow-clear
                show-search
                option-filter-prop="label"
                :options="workSectionOptions"
                :placeholder="t('basic.process.placeholder.workSection')"
                @change="onWorkSectionChange"
              />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item :label="t('basic.process.fields.processType')">
              <a-select
                v-model:value="form.processType"
                allow-clear
                :options="processTypeOptions"
                :placeholder="t('basic.process.placeholder.processType')"
              />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item :label="t('basic.process.fields.reportType')">
              <a-select
                v-model:value="form.reportType"
                allow-clear
                :options="reportTypeOptions"
                :placeholder="t('basic.process.placeholder.reportType')"
              />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item :label="t('basic.process.fields.qcTriggerPoint')">
              <a-select
                v-model:value="form.qcTriggerPoint"
                allow-clear
                :options="qcTriggerPointOptions"
                :placeholder="t('basic.process.placeholder.qcTriggerPoint')"
              />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item :label="t('basic.process.fields.sortOrder')">
              <a-input-number v-model:value="form.sortOrder" class="full-width" :min="0" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item :label="t('basic.process.fields.status')">
              <a-switch
                :checked="Number(form.status) === 1"
                @change="(val: boolean | string | number) => (form.status = val ? 1 : 0)"
              />
            </a-form-item>
          </a-col>
          <a-col :span="24">
            <a-form-item :label="t('basic.process.fields.remark')">
              <a-textarea v-model:value="form.remark" :rows="3" :placeholder="t('basic.process.placeholder.remark')" />
            </a-form-item>
          </a-col>
        </a-row>
      </a-form>
    </BaseFormDialog>
  </div>
</template>

<script setup lang="ts">
/**
 * 工序管理页面。
 * <p>
 * 对应 {@code /basic/process/*} 接口，提供工序主数据的列表展示、新增、编辑、删除（单条 / 批量）能力。
 * 工序类型（{@code process_type}）、报工方式（{@code report_type}）、质检触发点
 * （{@code qc_trigger_point}）均使用数据字典渲染，下拉选项来源于
 * {@code useDict} hook。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0
 * @since 2026-06-19
 * @see processApi
 * @see workSectionApi
 */
import { computed, onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { Modal, message } from 'ant-design-vue'
import BaseFormDialog from '@/components/common/BaseFormDialog.vue'
import FxDynamicTable from '@/components/common/FxDynamicTable.vue'
import { useBatchTableSelection } from '@/hooks/useBatchTableSelection'
import { useDict, type DictItemOption } from '@/hooks/useDict'
import { processApi, type Process } from '@/api/basic/process'
import { workSectionApi, type WorkSection } from '@/api/basic/workSection'

/** 通用 i18n 函数 */
const { t } = useI18n()

/** 表格引用，用于刷新列表 */
const tableRef = ref()
/** 批量选择 hooks */
const { selectedRowKeys, selectedCount, rowSelection, clearSelection } = useBatchTableSelection<number>()

/** 弹窗可见状态 */
const visible = ref(false)
/** 保存提交加载状态 */
const saving = ref(false)
/** 工序表单（同时用于新增 / 编辑） */
const form = ref<Process>(emptyForm())

/** 工段下拉选项 */
const workSectionOptions = ref<{ label: string; value: number }[]>([])
/** 工段原始数据备份，用于同步工段 / 产线 / 车间名称快照 */
const workSectionListCache = ref<WorkSection[]>([])

/** 字典：工序类型 / 报工方式 / 质检触发点 */
const { dictItems: processTypeDictItems } = useDict('process_type')
const { dictItems: reportTypeDictItems } = useDict('report_type')
const { dictItems: qcTriggerPointDictItems } = useDict('qc_trigger_point')

/** 字典下拉选项 */
const processTypeOptions = computed<{ label: string; value: string }[]>(() =>
  processTypeDictItems.value.map((item: DictItemOption) => ({ label: item.label, value: String(item.value) })),
)
const reportTypeOptions = computed<{ label: string; value: string }[]>(() =>
  reportTypeDictItems.value.map((item: DictItemOption) => ({ label: item.label, value: String(item.value) })),
)
const qcTriggerPointOptions = computed<{ label: string; value: string }[]>(() =>
  qcTriggerPointDictItems.value.map((item: DictItemOption) => ({ label: item.label, value: String(item.value) })),
)

/** 表格列使用的字典选项（FxDynamicTable 渲染 tag / 字典列时使用） */
const dictOptions = computed<Record<string, any[]>>(() => ({
  processType: processTypeOptions.value,
  process_type: processTypeOptions.value,
  reportType: reportTypeOptions.value,
  report_type: reportTypeOptions.value,
  qcTriggerPoint: qcTriggerPointOptions.value,
  qc_trigger_point: qcTriggerPointOptions.value,
}))

/**
 * 构造空表单。
 *
 * @returns 工序空对象（含默认值）
 */
function emptyForm(): Process {
  return { processCode: '', processName: '', status: 1, sortOrder: 0 }
}

/**
 * 处理 FxDynamicTable 的分页查询请求。
 *
 * @param payload 分页与查询条件
 * @returns 分页结果（{@code records + total}）
 */
async function handleRequest(payload: { page: { current: number; pageSize: number }; query: Record<string, any> }) {
  const result = await processApi.page({
    pageNum: payload.page.current,
    pageSize: payload.page.pageSize,
    ...payload.query,
  })
  return { records: result.records || [], total: Number(result.total || 0) }
}

/**
 * 加载工段下拉（仅启用状态）。
 */
async function loadWorkSections() {
  const list = await workSectionApi.list({ status: 1 })
  const sections = Array.isArray(list) ? list : []
  workSectionListCache.value = sections
  workSectionOptions.value = sections.map(item => ({ label: `${item.workSectionName} (${item.workSectionCode})`, value: item.id! }))
}

/**
 * 工段变化时同步工段 / 产线 / 车间名称快照。
 *
 * @param value 选中的工段 ID
 */
function onWorkSectionChange(value: number | undefined) {
  const matched = value ? workSectionListCache.value.find(item => item.id === value) : undefined
  form.value.workSectionId = value
  form.value.workSectionCode = matched?.workSectionCode
  form.value.workSectionName = matched?.workSectionName
  // 同步冗余快照
  form.value.productionLineId = matched?.productionLineId
  form.value.productionLineCode = matched?.productionLineCode
  form.value.productionLineName = matched?.productionLineName
  form.value.workshopId = matched?.workshopId
  form.value.workshopCode = matched?.workshopCode
  form.value.workshopName = matched?.workshopName
}

/**
 * 打开编辑 / 新增弹窗。
 *
 * @param record 编辑记录；为空时表示新增
 */
function openEditor(record?: Process) {
  if (record) {
    form.value = {
      ...record,
      status: Number(record.status) === 1 ? 1 : 0,
    }
  } else {
    form.value = emptyForm()
  }
  visible.value = true
}

/**
 * 保存（新增或更新）。
 */
async function handleSave() {
  if (!form.value.processCode?.trim()) {
    message.warning(t('basic.process.codeRequired'))
    return
  }
  if (!form.value.processName?.trim()) {
    message.warning(t('basic.process.nameRequired'))
    return
  }
  saving.value = true
  try {
    if (form.value.id) {
      await processApi.update(form.value)
    } else {
      await processApi.create(form.value)
    }
    visible.value = false
    await tableRef.value?.refresh?.()
  } finally {
    saving.value = false
  }
}

/**
 * 删除单条工序。
 *
 * @param record 待删除记录
 */
function handleDelete(record: Process) {
  Modal.confirm({
    title: t('basic.process.confirmDelete'),
    async onOk() {
      await processApi.delete(record.id!)
      await tableRef.value?.refresh?.()
    },
  })
}

/**
 * 批量删除工序。
 */
function handleBatchDelete() {
  if (!selectedCount.value) return
  Modal.confirm({
    title: t('common.confirmBatchDelete'),
    content: t('common.confirmBatchDeleteMessage', { count: selectedCount.value }),
    async onOk() {
      await processApi.batchDelete(selectedRowKeys.value)
      clearSelection()
      await tableRef.value?.refresh?.()
    },
  })
}

/**
 * 在下拉选项中按 value 查找 label。
 *
 * @param options 下拉选项
 * @param value 值
 * @returns 对应 label 或原 value
 */
function labelOf(options: { label: string; value: any }[], value: any) {
  const matched = options.find(item => String(item.value) === String(value ?? ''))
  return matched?.label || value || '-'
}

onMounted(loadWorkSections)
</script>

<style scoped lang="less" src="@/styles/views/basic/masterData/index.less"></style>
