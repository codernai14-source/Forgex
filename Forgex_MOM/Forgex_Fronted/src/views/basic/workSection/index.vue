<template>
  <div class="basic-master-page">
    <div class="basic-master-header">
      <div>
        <a-tag color="blue">Basic MDM</a-tag>
        <h1>{{ t('basic.workSection.title') }}</h1>
        <p>{{ t('basic.workSection.subtitle') }}</p>
      </div>
      <a-space wrap>
        <a-button v-permission="'basic:workSection:add'" type="primary" @click="openEditor()">{{ t('basic.workSection.addWorkSection') }}</a-button>
      </a-space>
    </div>

    <FxDynamicTable
      ref="tableRef"
      table-code="BasicWorkSectionTable"
      :request="handleRequest"
      :row-selection="rowSelection"
      row-key="id"
    >
      <template #toolbar>
        <a-button
          v-permission="'basic:workSection:batchDelete'"
          danger
          :disabled="!selectedCount"
          @click="handleBatchDelete"
        >
          {{ t('common.batchDelete') }}
        </a-button>
      </template>
      <template #workSectionName="{ record }">
        <div class="master-name">
          <strong>{{ record.workSectionName }}</strong>
          <span>{{ record.workSectionCode }}</span>
        </div>
      </template>
      <template #workshopName="{ record }">
        <span>{{ record.workshopName || '-' }}</span>
      </template>
      <template #productionLineName="{ record }">
        <span>{{ record.productionLineName || '-' }}</span>
      </template>
      <template #status="{ record }">
        <a-tag :color="Number(record.status) === 1 ? 'green' : 'red'">{{ Number(record.status) === 1 ? t('common.enabled') : t('common.disabled') }}</a-tag>
      </template>
      <template #action="{ record }">
        <a-space>
          <a v-permission="'basic:workSection:edit'" @click="openEditor(record)">{{ t('common.edit') }}</a>
          <a v-permission="'basic:workSection:delete'" class="danger-link" @click="handleDelete(record)">{{ t('common.delete') }}</a>
        </a-space>
      </template>
    </FxDynamicTable>

    <BaseFormDialog
      v-model:open="visible"
      :title="form.id ? t('basic.workSection.editWorkSection') : t('basic.workSection.addWorkSection')"
      width="720px"
      :loading="saving"
      @submit="handleSave"
    >
      <a-form layout="vertical" :model="form">
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item :label="t('basic.workSection.fields.code')" required>
              <a-input v-model:value="form.workSectionCode" :disabled="!!form.id" :placeholder="t('basic.workSection.placeholder.code')" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item :label="t('basic.workSection.fields.name')" required>
              <a-input v-model:value="form.workSectionName" :placeholder="t('basic.workSection.placeholder.name')" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item :label="t('basic.workSection.fields.workshop')">
              <a-select
                v-model:value="form.workshopId"
                allow-clear
                show-search
                option-filter-prop="label"
                :options="workshopOptions"
                :placeholder="t('basic.workSection.placeholder.workshop')"
                @change="onWorkshopChange"
              />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item :label="t('basic.workSection.fields.productionLine')">
              <a-select
                v-model:value="form.productionLineId"
                allow-clear
                show-search
                option-filter-prop="label"
                :options="productionLineOptions"
                :placeholder="t('basic.workSection.placeholder.productionLine')"
                @change="onProductionLineChange"
              />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item :label="t('basic.workSection.fields.sortOrder')">
              <a-input-number v-model:value="form.sortOrder" class="full-width" :min="0" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item :label="t('basic.workSection.fields.status')">
              <a-switch
                :checked="Number(form.status) === 1"
                @change="(val: boolean | string | number) => (form.status = val ? 1 : 0)"
              />
            </a-form-item>
          </a-col>
          <a-col :span="24">
            <a-form-item :label="t('basic.workSection.fields.remark')">
              <a-textarea v-model:value="form.remark" :rows="3" :placeholder="t('basic.workSection.placeholder.remark')" />
            </a-form-item>
          </a-col>
        </a-row>
      </a-form>
    </BaseFormDialog>
  </div>
</template>

<script setup lang="ts">
/**
 * 工段管理页面。
 * <p>
 * 对应 {@code /basic/workSection/*} 接口，提供工段主数据的列表展示、新增、编辑、删除（单条 / 批量）能力。
 * 表单支持所属车间下拉（{@code workshopApi.list}）、所属产线下拉（按车间联动
 * {@code productionLineApi.listByWorkshop}），并按权限按钮显示操作。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0
 * @since 2026-06-19
 * @see workSectionApi
 * @see productionLineApi
 * @see workshopApi
 */
import { onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { Modal, message } from 'ant-design-vue'
import BaseFormDialog from '@/components/common/BaseFormDialog.vue'
import FxDynamicTable from '@/components/common/FxDynamicTable.vue'
import { useBatchTableSelection } from '@/hooks/useBatchTableSelection'
import { workSectionApi, type WorkSection } from '@/api/basic/workSection'
import { productionLineApi, type ProductionLine } from '@/api/basic/productionLine'
import { workshopApi, type Workshop } from '@/api/basic/workshop'

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
/** 工段表单（同时用于新增 / 编辑） */
const form = ref<WorkSection>(emptyForm())

/** 车间下拉选项 */
const workshopOptions = ref<{ label: string; value: number }[]>([])
/** 车间原始数据备份，用于同步车间编码 / 名称快照 */
const workshopListCache = ref<Workshop[]>([])

/** 产线下拉选项（按所选车间动态加载） */
const productionLineOptions = ref<{ label: string; value: number }[]>([])
/** 产线原始数据备份，用于同步产线编码 / 名称快照 */
const productionLineListCache = ref<ProductionLine[]>([])

/**
 * 构造空表单。
 *
 * @returns 工段空对象（含默认值）
 */
function emptyForm(): WorkSection {
  return { workSectionCode: '', workSectionName: '', status: 1, sortOrder: 0 }
}

/**
 * 处理 FxDynamicTable 的分页查询请求。
 *
 * @param payload 分页与查询条件
 * @returns 分页结果（{@code records + total}）
 */
async function handleRequest(payload: { page: { current: number; pageSize: number }; query: Record<string, any> }) {
  const result = await workSectionApi.page({
    pageNum: payload.page.current,
    pageSize: payload.page.pageSize,
    ...payload.query,
  })
  return { records: result.records || [], total: Number(result.total || 0) }
}

/**
 * 加载车间下拉（仅启用状态）。
 */
async function loadWorkshops() {
  const list = await workshopApi.list({ status: true })
  const workshops = Array.isArray(list) ? list : []
  workshopListCache.value = workshops
  workshopOptions.value = workshops.map(item => ({ label: `${item.workshopName} (${item.workshopCode})`, value: item.id! }))
}

/**
 * 按车间 ID 加载产线下拉（仅启用状态）。
 *
 * @param workshopId 所属车间 ID
 */
async function loadProductionLines(workshopId?: number) {
  if (!workshopId) {
    productionLineOptions.value = []
    productionLineListCache.value = []
    return
  }
  const list = await productionLineApi.listByWorkshop(workshopId)
  const lines = Array.isArray(list) ? list : []
  productionLineListCache.value = lines
  productionLineOptions.value = lines.map(item => ({ label: `${item.productionLineName} (${item.productionLineCode})`, value: item.id! }))
}

/**
 * 车间变化时同步车间编码 / 名称快照，并联动清空产线 / 重新加载产线。
 *
 * @param value 选中的车间 ID
 */
function onWorkshopChange(value: number | undefined) {
  const matched = value ? workshopListCache.value.find(item => item.id === value) : undefined
  form.value.workshopId = value
  form.value.workshopCode = matched?.workshopCode
  form.value.workshopName = matched?.workshopName

  // 切换车间时重置产线，避免与新车间不匹配
  form.value.productionLineId = undefined
  form.value.productionLineCode = undefined
  form.value.productionLineName = undefined
  loadProductionLines(value)
}

/**
 * 产线变化时同步产线编码 / 名称快照。
 *
 * @param value 选中的产线 ID
 */
function onProductionLineChange(value: number | undefined) {
  const matched = value ? productionLineListCache.value.find(item => item.id === value) : undefined
  form.value.productionLineId = value
  form.value.productionLineCode = matched?.productionLineCode
  form.value.productionLineName = matched?.productionLineName
}

/**
 * 打开编辑 / 新增弹窗。
 *
 * @param record 编辑记录；为空时表示新增
 */
async function openEditor(record?: WorkSection) {
  if (record) {
    form.value = {
      ...record,
      status: Number(record.status) === 1 ? 1 : 0,
    }
    // 编辑时按所属车间回填产线下拉
    if (record.workshopId) {
      await loadProductionLines(record.workshopId)
    } else {
      productionLineOptions.value = []
      productionLineListCache.value = []
    }
  } else {
    form.value = emptyForm()
    productionLineOptions.value = []
    productionLineListCache.value = []
  }
  visible.value = true
}

/**
 * 保存（新增或更新）。
 */
async function handleSave() {
  if (!form.value.workSectionCode?.trim()) {
    message.warning(t('basic.workSection.codeRequired'))
    return
  }
  if (!form.value.workSectionName?.trim()) {
    message.warning(t('basic.workSection.nameRequired'))
    return
  }
  saving.value = true
  try {
    if (form.value.id) {
      await workSectionApi.update(form.value)
    } else {
      await workSectionApi.create(form.value)
    }
    visible.value = false
    await tableRef.value?.refresh?.()
  } finally {
    saving.value = false
  }
}

/**
 * 删除单条工段。
 *
 * @param record 待删除记录
 */
function handleDelete(record: WorkSection) {
  Modal.confirm({
    title: t('basic.workSection.confirmDelete'),
    async onOk() {
      await workSectionApi.delete(record.id!)
      await tableRef.value?.refresh?.()
    },
  })
}

/**
 * 批量删除工段。
 */
function handleBatchDelete() {
  if (!selectedCount.value) return
  Modal.confirm({
    title: t('common.confirmBatchDelete'),
    content: t('common.confirmBatchDeleteMessage', { count: selectedCount.value }),
    async onOk() {
      await workSectionApi.batchDelete(selectedRowKeys.value)
      clearSelection()
      await tableRef.value?.refresh?.()
    },
  })
}

onMounted(loadWorkshops)
</script>

<style scoped lang="less" src="@/styles/views/basic/masterData/index.less"></style>
