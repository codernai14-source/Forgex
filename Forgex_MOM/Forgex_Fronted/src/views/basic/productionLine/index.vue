<template>
  <div class="basic-master-page">
    <div class="basic-master-header">
      <div>
        <a-tag color="blue">Basic MDM</a-tag>
        <h1>{{ t('basic.productionLine.title') }}</h1>
        <p>{{ t('basic.productionLine.subtitle') }}</p>
      </div>
      <a-space wrap>
        <a-button v-permission="'basic:productionLine:add'" type="primary" @click="openEditor()">{{ t('basic.productionLine.addProductionLine') }}</a-button>
      </a-space>
    </div>

    <FxDynamicTable
      ref="tableRef"
      table-code="BasicProductionLineTable"
      :request="handleRequest"
      :row-selection="rowSelection"
      :dict-options="dictOptions"
      row-key="id"
    >
      <template #toolbar>
        <a-button
          v-permission="'basic:productionLine:batchDelete'"
          danger
          :disabled="!selectedCount"
          @click="handleBatchDelete"
        >
          {{ t('common.batchDelete') }}
        </a-button>
      </template>
      <template #productionLineName="{ record }">
        <div class="master-name">
          <strong>{{ record.productionLineName }}</strong>
          <span>{{ record.productionLineCode }}</span>
        </div>
      </template>
      <template #workshopName="{ record }">
        <span>{{ record.workshopName || '-' }}</span>
      </template>
      <template #productionLineType="{ record }">
        <a-tag>{{ labelOf(productionLineTypeOptions, record.productionLineType) }}</a-tag>
      </template>
      <template #managerEmployeeName="{ record }">
        <span>{{ record.managerEmployeeName || '-' }}</span>
      </template>
      <template #status="{ record }">
        <a-tag :color="Number(record.status) === 1 ? 'green' : 'red'">{{ Number(record.status) === 1 ? t('common.enabled') : t('common.disabled') }}</a-tag>
      </template>
      <template #action="{ record }">
        <a-space>
          <a v-permission="'basic:productionLine:edit'" @click="openEditor(record)">{{ t('common.edit') }}</a>
          <a v-permission="'basic:productionLine:delete'" class="danger-link" @click="handleDelete(record)">{{ t('common.delete') }}</a>
        </a-space>
      </template>
    </FxDynamicTable>

    <BaseFormDialog
      v-model:open="visible"
      :title="form.id ? t('basic.productionLine.editProductionLine') : t('basic.productionLine.addProductionLine')"
      width="720px"
      :loading="saving"
      @submit="handleSave"
    >
      <a-form layout="vertical" :model="form">
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item :label="t('basic.productionLine.fields.code')" required>
              <a-input v-model:value="form.productionLineCode" :disabled="!!form.id" :placeholder="t('basic.productionLine.placeholder.code')" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item :label="t('basic.productionLine.fields.name')" required>
              <a-input v-model:value="form.productionLineName" :placeholder="t('basic.productionLine.placeholder.name')" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item :label="t('basic.productionLine.fields.workshop')">
              <a-select
                v-model:value="form.workshopId"
                allow-clear
                show-search
                option-filter-prop="label"
                :options="workshopOptions"
                :placeholder="t('basic.productionLine.placeholder.workshop')"
                @change="onWorkshopChange"
              />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item :label="t('basic.productionLine.fields.type')">
              <a-select
                v-model:value="form.productionLineType"
                allow-clear
                :options="productionLineTypeOptions"
                :placeholder="t('basic.productionLine.placeholder.type')"
              />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item :label="t('basic.productionLine.fields.manager')">
              <a-select
                v-model:value="form.managerEmployeeId"
                allow-clear
                show-search
                option-filter-prop="label"
                :options="employeeOptions"
                :placeholder="t('basic.productionLine.placeholder.manager')"
                @change="onManagerChange"
              />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item :label="t('basic.productionLine.fields.sortOrder')">
              <a-input-number v-model:value="form.sortOrder" class="full-width" :min="0" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item :label="t('basic.productionLine.fields.status')">
              <a-switch
                :checked="Number(form.status) === 1"
                @change="(val: boolean | string | number) => (form.status = val ? 1 : 0)"
              />
            </a-form-item>
          </a-col>
          <a-col :span="24">
            <a-form-item :label="t('basic.productionLine.fields.remark')">
              <a-textarea v-model:value="form.remark" :rows="3" :placeholder="t('basic.productionLine.placeholder.remark')" />
            </a-form-item>
          </a-col>
        </a-row>
      </a-form>
    </BaseFormDialog>
  </div>
</template>

<script setup lang="ts">
/**
 * 产线管理页面。
 * <p>
 * 对应 {@code /basic/productionLine/*} 接口，提供产线主数据的列表展示、新增、编辑、删除（单条 / 批量）能力。
 * 表单支持车间下拉（{@code workshopApi.list} 全量加载启用车间）、产线类型字典
 * （{@code prod_line_type}）、负责人下拉（{@code employeeApi.list}），并按权限按钮显示操作。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0
 * @since 2026-06-19
 * @see productionLineApi
 * @see workshopApi
 * @see employeeApi
 */
import { computed, onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { Modal, message } from 'ant-design-vue'
import BaseFormDialog from '@/components/common/BaseFormDialog.vue'
import FxDynamicTable from '@/components/common/FxDynamicTable.vue'
import { useBatchTableSelection } from '@/hooks/useBatchTableSelection'
import { useDict, type DictItemOption } from '@/hooks/useDict'
import { productionLineApi, type ProductionLine } from '@/api/basic/productionLine'
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
/** 产线表单（同时用于新增 / 编辑） */
const form = ref<ProductionLine>(emptyForm())

/** 车间下拉选项（全部启用车间） */
const workshopOptions = ref<{ label: string; value: number }[]>([])
/** 车间原始数据备份，用于同步车间编码 / 名称快照 */
const workshopListCache = ref<Workshop[]>([])

/** 员工下拉选项（仅启用状态） */
const employeeOptions = ref<{ label: string; value: number }[]>([])
/** 员工原始数据备份，用于同步工号 / 姓名快照 */
const employeeListCache = ref<Employee[]>([])

/** 字典：产线类型（prod_line_type） */
const { dictItems: productionLineTypeDictItems } = useDict('prod_line_type')

/** 产线类型下拉选项 */
const productionLineTypeOptions = computed<{ label: string; value: string }[]>(() =>
  productionLineTypeDictItems.value.map((item: DictItemOption) => ({ label: item.label, value: String(item.value) })),
)

/** 表格列使用的字典选项（FxDynamicTable 渲染 tag / 字典列时使用） */
const dictOptions = computed<Record<string, any[]>>(() => ({
  productionLineType: productionLineTypeOptions.value,
  prod_line_type: productionLineTypeOptions.value,
}))

/**
 * 构造空表单。
 *
 * @returns 产线空对象（含默认值）
 */
function emptyForm(): ProductionLine {
  return { productionLineCode: '', productionLineName: '', status: 1, sortOrder: 0 }
}

/**
 * 处理 FxDynamicTable 的分页查询请求。
 *
 * @param payload 分页与查询条件
 * @returns 分页结果（{@code records + total}）
 */
async function handleRequest(payload: { page: { current: number; pageSize: number }; query: Record<string, any> }) {
  const result = await productionLineApi.page({
    pageNum: payload.page.current,
    pageSize: payload.page.pageSize,
    ...payload.query,
  })
  return { records: result.records || [], total: Number(result.total || 0) }
}

/**
 * 加载车间下拉（仅启用状态）以及员工下拉。
 */
async function loadOptions() {
  const [workshopList, employeeList] = await Promise.all([
    workshopApi.list({ status: true }),
    employeeApi.list({ status: true }),
  ])
  const workshops = Array.isArray(workshopList) ? workshopList : []
  workshopListCache.value = workshops
  workshopOptions.value = workshops.map(item => ({ label: `${item.workshopName} (${item.workshopCode})`, value: item.id! }))

  const employees = Array.isArray(employeeList) ? employeeList : []
  employeeListCache.value = employees
  employeeOptions.value = employees.map(item => ({ label: `${item.employeeName} (${item.employeeNo})`, value: item.id! }))
}

/**
 * 车间变化时同步车间编码 / 名称快照。
 *
 * @param value 选中的车间 ID
 */
function onWorkshopChange(value: number | undefined) {
  if (!value) {
    form.value.workshopId = undefined
    form.value.workshopCode = undefined
    form.value.workshopName = undefined
    return
  }
  const matched = workshopListCache.value.find(item => item.id === value)
  form.value.workshopId = value
  form.value.workshopCode = matched?.workshopCode
  form.value.workshopName = matched?.workshopName
}

/**
 * 负责人变化时同步负责人姓名 / 工号快照。
 *
 * @param value 选中的员工 ID
 */
function onManagerChange(value: number | undefined) {
  if (!value) {
    form.value.managerEmployeeId = undefined
    form.value.managerEmployeeNo = undefined
    form.value.managerEmployeeName = undefined
    return
  }
  const matched = employeeListCache.value.find(item => item.id === value)
  form.value.managerEmployeeId = value
  form.value.managerEmployeeNo = matched?.employeeNo
  form.value.managerEmployeeName = matched?.employeeName
}

/**
 * 打开编辑 / 新增弹窗。
 *
 * @param record 编辑记录；为空时表示新增
 */
function openEditor(record?: ProductionLine) {
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
  if (!form.value.productionLineCode?.trim()) {
    message.warning(t('basic.productionLine.codeRequired'))
    return
  }
  if (!form.value.productionLineName?.trim()) {
    message.warning(t('basic.productionLine.nameRequired'))
    return
  }
  saving.value = true
  try {
    if (form.value.id) {
      await productionLineApi.update(form.value)
    } else {
      await productionLineApi.create(form.value)
    }
    visible.value = false
    await tableRef.value?.refresh?.()
  } finally {
    saving.value = false
  }
}

/**
 * 删除单条产线。
 *
 * @param record 待删除记录
 */
function handleDelete(record: ProductionLine) {
  Modal.confirm({
    title: t('basic.productionLine.confirmDelete'),
    async onOk() {
      await productionLineApi.delete(record.id!)
      await tableRef.value?.refresh?.()
    },
  })
}

/**
 * 批量删除产线。
 */
function handleBatchDelete() {
  if (!selectedCount.value) return
  Modal.confirm({
    title: t('common.confirmBatchDelete'),
    content: t('common.confirmBatchDeleteMessage', { count: selectedCount.value }),
    async onOk() {
      await productionLineApi.batchDelete(selectedRowKeys.value)
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
