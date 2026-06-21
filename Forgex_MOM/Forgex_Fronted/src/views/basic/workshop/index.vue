<template>
  <div class="basic-master-page">
    <div class="basic-master-header">
      <div>
        <a-tag color="blue">Basic MDM</a-tag>
        <h1>车间管理</h1>
        <p>维护车间编码、所属工厂、车间类型、负责人和启用状态，为班组归属提供统一主数据。</p>
      </div>
      <a-space wrap>
        <a-button v-permission="'basic:workshop:add'" type="primary" @click="openEditor()">新增车间</a-button>
      </a-space>
    </div>

    <FxDynamicTable
      ref="tableRef"
      table-code="BasicWorkshopTable"
      :request="handleRequest"
      :row-selection="rowSelection"
      :dict-options="dictOptions"
      row-key="id"
    >
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
      <template #workshopType="{ record }">
        <a-tag>{{ labelOf(workshopTypeOptions, record.workshopType) }}</a-tag>
      </template>
      <template #workshopManagerName="{ record }">
        <span>{{ record.workshopManagerName || '-' }}</span>
      </template>
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

    <BaseFormDialog
      v-model:open="visible"
      :title="form.id ? '编辑车间' : '新增车间'"
      width="720px"
      :loading="saving"
      @submit="handleSave"
    >
      <a-form layout="vertical" :model="form">
        <a-row :gutter="16">
          <a-col :span="12"><a-form-item label="车间编码" required><a-input v-model:value="form.workshopCode" :disabled="!!form.id" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="车间名称" required><a-input v-model:value="form.workshopName" /></a-form-item></a-col>
          <a-col :span="12">
            <a-form-item label="所属工厂">
              <a-select v-model:value="form.factoryId" allow-clear show-search option-filter-prop="label" :options="factoryOptions" @change="onFactoryChange" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="车间类型">
              <a-select v-model:value="form.workshopType" allow-clear :options="workshopTypeOptions" placeholder="请选择车间类型" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="负责人">
              <a-select
                v-model:value="form.workshopManagerId"
                allow-clear
                show-search
                option-filter-prop="label"
                :options="employeeOptions"
                placeholder="请选择负责人"
                @change="onManagerChange"
              />
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
/**
 * 车间管理页面。
 * <p>
 * 对应 {@code /basic/workshop/*} 接口，提供车间主数据的列表展示、新增、编辑、删除（单条 / 批量）能力。
 * 工厂建模升级后，车间实体新增 {@code workshopType}（字典 workshop_type）、
 * {@code workshopManagerId / workshopManagerName}（关联 basic_employee 员工主数据）字段，
 * 本页面同步展示 / 编辑 / 回显这些字段。
 * </p>
 *
 * @author Forgex Team
 * @version 1.1
 * @since 2026-06-19
 * @see workshopApi
 * @see factoryApi
 * @see employeeApi
 */
import { computed, onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { Modal } from 'ant-design-vue'
import BaseFormDialog from '@/components/common/BaseFormDialog.vue'
import FxDynamicTable from '@/components/common/FxDynamicTable.vue'
import { useBatchTableSelection } from '@/hooks/useBatchTableSelection'
import { useDict, type DictItemOption } from '@/hooks/useDict'
import { factoryApi } from '@/api/basic/factory'
import { workshopApi, type Workshop } from '@/api/basic/workshop'
import { employeeApi, type Employee } from '@/api/basic/employee'

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
/** 车间表单（同时用于新增 / 编辑） */
const form = ref<Workshop>(emptyForm())

/** 工厂下拉选项 */
const factoryOptions = ref<{ label: string; value: number }[]>([])
/** 工厂原始数据备份，用于同步工厂编码 / 名称快照 */
const factoryListCache = ref<any[]>([])

/** 员工下拉选项（仅启用状态） */
const employeeOptions = ref<{ label: string; value: number }[]>([])
/** 员工原始数据备份，用于同步负责人姓名快照 */
const employeeListCache = ref<Employee[]>([])

/** 字典：车间类型（workshop_type） */
const { dictItems: workshopTypeDictItems } = useDict('workshop_type')

/** 车间类型下拉选项 */
const workshopTypeOptions = computed<{ label: string; value: string }[]>(() =>
  workshopTypeDictItems.value.map((item: DictItemOption) => ({ label: item.label, value: String(item.value) })),
)

/** 表格列使用的字典选项（FxDynamicTable 渲染 tag / 字典列时使用） */
const dictOptions = computed<Record<string, any[]>>(() => ({
  workshopType: workshopTypeOptions.value,
  workshop_type: workshopTypeOptions.value,
}))

/**
 * 构造空表单。
 *
 * @returns 车间空对象（含默认值）
 */
function emptyForm(): Workshop {
  return { workshopCode: '', workshopName: '', status: true }
}

/**
 * 处理 FxDynamicTable 的分页查询请求。
 *
 * @param payload 分页与查询条件
 * @returns 分页结果（{@code records + total}）
 */
async function handleRequest(payload: any) {
  const result = await workshopApi.page({ pageNum: payload.page.current, pageSize: payload.page.pageSize, ...payload.query })
  return { records: result.records || [], total: Number(result.total || 0) }
}

/**
 * 加载工厂下拉、员工下拉。
 */
async function loadOptions() {
  const [factoryList, employeeList] = await Promise.all([
    factoryApi.list({ status: 1 }),
    employeeApi.list({ status: true }),
  ])
  const factories = Array.isArray(factoryList) ? factoryList : []
  factoryListCache.value = factories
  factoryOptions.value = factories.map((item: any) => ({ label: `${item.factoryName} (${item.factoryCode})`, value: item.id }))

  const employees = Array.isArray(employeeList) ? employeeList : []
  employeeListCache.value = employees
  employeeOptions.value = employees.map(item => ({ label: `${item.employeeName} (${item.employeeNo})`, value: item.id! }))
}

/**
 * 工厂变化时同步工厂编码 / 名称快照。
 *
 * @param value 选中的工厂 ID
 */
function onFactoryChange(value: number | undefined) {
  const matched = value ? factoryListCache.value.find((item: any) => item.id === value) : undefined
  form.value.factoryId = value
  form.value.factoryCode = matched?.factoryCode
  form.value.factoryName = matched?.factoryName
}

/**
 * 负责人变化时同步负责人姓名快照。
 *
 * @param value 选中的员工 ID
 */
function onManagerChange(value: number | undefined) {
  const matched = value ? employeeListCache.value.find(item => item.id === value) : undefined
  form.value.workshopManagerId = value
  form.value.workshopManagerName = matched?.employeeName
}

/**
 * 打开编辑 / 新增弹窗。
 *
 * @param record 编辑记录；为空时表示新增
 */
function openEditor(record?: Workshop) {
  form.value = record ? { ...record } : emptyForm()
  visible.value = true
}

/**
 * 保存（新增或更新）。
 */
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

/**
 * 删除单条车间。
 *
 * @param record 待删除记录
 */
function handleDelete(record: Workshop) {
  Modal.confirm({
    title: '确认删除该车间？',
    async onOk() {
      await workshopApi.delete(record.id!)
      await tableRef.value?.refresh?.()
    },
  })
}

/**
 * 批量删除车间。
 */
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

onMounted(loadOptions)
</script>

<style scoped lang="less" src="@/styles/views/basic/masterData/index.less"></style>
