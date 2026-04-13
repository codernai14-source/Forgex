<template>
  <div class="report-container">
    <FxDynamicTable
      ref="tableRef"
      table-code="ReportTemplateTable"
      :request="handleRequest"
      :dict-options="dictOptions"
      :降级方案-config="降级方案Config"
      :show-query-form="true"
      row-key="id"
    >
      <template #toolbar>
        <a-button 
          v-permission="'report:template:add'"
          type="primary" 
          @click="handleAdd"
        >
          <template #icon><PlusOutlined /></template>
          新增
        </a-button>
      </template>

      <template #engineType="{ record }">
        <a-tag :color="record.engineType === 'UREPORT' ? 'blue' : 'green'">
          {{ resolveEngineTypeLabel(record.engineType) }}
        </a-tag>
      </template>

      <template #status="{ record }">
        <a-tag
          v-if="resolve状态Tag(record.status)"
          :color="resolve状态Tag(record.status)?.color"
          :style="resolve状态Tag(record.status)?.style"
        >
          {{ resolve状态Tag(record.status)?.label }}
        </a-tag>
        <span v-else>{{ record.status ?? '-' }}</span>
      </template>

      <template #action="{ record }">
        <a-space>
          <a 
            v-permission="'report:template:edit'"
            @click="handleEdit(record)"
          >
            编辑
          </a>
          <a 
            v-permission="'report:template:design'"
            @click="handleDesigner(record)"
          >
            设计器
          </a>
          <a 
            v-permission="'report:template:preview'"
            @click="handlePreview(record)"
          >
            预览
          </a>
          <a 
            v-permission="'report:template:delete'"
            style="color: #ff4d4f" 
            @click="handleDelete(record)"
          >
            删除
          </a>
        </a-space>
      </template>
    </FxDynamicTable>

    <!-- 鎶ヨ〃琛ㄥ崟寮圭獥 -->
    <ReportForm
      v-model:open="formVisible"
      :form-data="current表单Data"
      :category-options="categoryOptions"
      :datasource-options="datasourceOptions"
      @ok="handle表单Ok"
    />

    <!-- 鎶ヨ〃璁捐鍣ㄥ脊绐?-->
    <ReportDesigner
      v-model:open="designerVisible"
      :report-code="currentDesignerCode"
      :engine-type="currentEngineType"
      @ok="handleDesignerOk"
    />

    <!-- 鎶ヨ〃棰勮寮圭獥 -->
    <ReportPreview
      v-model:open="previewVisible"
      :report-code="currentPreviewCode"
      :engine-type="currentPreviewEngineType"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { Modal } from 'ant-design-vue'
import { PlusOutlined } from '@ant-design/icons-vue'
import FxDynamicTable from '@/components/common/FxDynamicTable.vue'
import { useDict } from '@/hooks/useDict'
import type { FxTableConfig } from '@/api/system/tableConfig'
import type { ReportTemplate, ReportTemplateParam, ReportCategory, ReportDatasource } from '@/api/report/types'
import {
  page,
  remove,
  getDesignerUrl,
  getPreviewUrl,
  getCategoryTree,
  getAvailableDatasources,
} from '@/api/report'
import ReportForm from './components/ReportForm.vue'
import ReportDesigner from './components/ReportDesigner.vue'
import ReportPreview from './components/ReportPreview.vue'

const { dictItems: statusOptions } = useDict('status')

const tableRef = ref()
const formVisible = ref(false)
const designerVisible = ref(false)
const previewVisible = ref(false)
const current表单Data = ref<Partial<ReportTemplate>>({})
const currentDesignerCode = ref('')
const currentEngineType = ref<'UREPORT' | 'JIMU'>('UREPORT')
const currentPreviewCode = ref('')
const currentPreviewEngineType = ref<'UREPORT' | 'JIMU'>('UREPORT')
const categoryOptions = ref<Array<{ label: string; value: number }>>([])
const datasourceOptions = ref<Array<{ label: string; value: number }>>([])

const dictOptions = computed(() => ({
  status: statusOptions.value,
  engineType: [
    { label: 'UReport2', value: 'UREPORT', color: 'blue' },
    { label: 'JimuReport', value: 'JIMU', color: 'green' },
  ],
}))

const 降级方案Config = computed<Partial<FxTableConfig>>(() => ({
  tableCode: 'ReportTemplateTable',
  tableName: '报表模板管理',
  tableType: 'NORMAL',
  rowKey: 'id',
  defaultPageSize: 10,
  columns: [
    { field: 'id', title: '报表 ID', width: 100, align: 'center' },
    { field: 'name', title: '报表名称', width: 200, align: 'left' },
    { field: 'code', title: '报表编码', width: 150, align: 'left' },
    { field: 'engineType', title: '引擎类型', width: 120, align: 'center', dictCode: 'engineType' },
    { field: 'categoryName', title: '分类', width: 120, align: 'center' },
    { field: 'datasourceName', title: '数据源', width: 120, align: 'center' },
    { field: 'status', title: '状态', width: 100, align: 'center', dictCode: 'status' },
    { field: 'remark', title: '备注', width: 200, align: 'left' },
    { field: 'createTime', title: '创建时间', width: 180, align: 'center' },
    { field: 'action', title: '操作', width: 280, align: 'center', fixed: 'right' },
  ],
  queryFields: [
    { field: 'name', label: '报表名称', queryType: 'input', queryOperator: 'like' },
    { field: 'code', label: '报表编码', queryType: 'input', queryOperator: 'like' },
    { field: 'engineType', label: '引擎类型', queryType: 'select', queryOperator: 'eq', dictCode: 'engineType' },
    { field: 'status', label: '状态', queryType: 'select', queryOperator: 'eq', dictCode: 'status' },
  ],
  version: 1,
}))

function resolveEngineTypeLabel(value: string) {
  if (value === 'UREPORT') {
    return 'UReport2'
  } else if (value === 'JIMU') {
    return 'JimuReport'
  }
  return value
}

function resolve状态Tag(value: unknown) {
  const normalizedValue = value === true || value === 1 || value === '1' ? 1 : 0
  const dictItem = statusOptions.value.find((item) => String(item?.value) === String(normalizedValue))
  if (!dictItem) {
    return null
  }
  const style =
    dictItem.tagStyle?.borderColor || dictItem.tagStyle?.backgroundColor
      ? {
          borderColor: dictItem.tagStyle?.borderColor,
          backgroundColor: dictItem.tagStyle?.backgroundColor,
        }
      : undefined

  return {
    label: dictItem.label,
    color: dictItem.tagStyle?.color || dictItem.color || 'blue',
    style,
  }
}

const handleRequest = async (payload: {
  page: { current: number; pageSize: number }
  query: Record<string, any>
}) => {
  try {
    const res = await page({
      pageNum: payload.page.current,
      pageSize: payload.page.pageSize,
      name: payload.query?.name,
      code: payload.query?.code,
      engineType: payload.query?.engineType,
      status: payload.query?.status,
    } as ReportTemplateParam)
    return {
      records: res.records || [],
      total: Number(res.total || 0),
    }
  } catch (error) {
    console.error('加载报表分页数据失败', error)
    return {
      records: [],
      total: 0,
    }
  }
}

async function loadCategories() {
  try {
    const categories: ReportCategory[] = await getCategoryTree({})
    categoryOptions.value = flattenTreeToOptions(categories)
  } catch (error) {
    console.error('加载分类树失败', error)
    categoryOptions.value = []
  }
}

async function loadDatasources() {
  try {
    const datasources: ReportDatasource[] = await getAvailableDatasources()
    datasourceOptions.value = (datasources || []).map((item) => ({
      label: item.name,
      value: item.id,
    }))
  } catch (error) {
    console.error('加载数据源列表失败', error)
    datasourceOptions.value = []
  }
}

function flattenTreeToOptions(tree: ReportCategory[]): Array<{ label: string; value: number }> {
  const result: Array<{ label: string; value: number }> = []
  function traverse(nodes: ReportCategory[], prefix = '') {
    nodes.forEach((node) => {
      const label = prefix ? `${prefix} / ${node.name}` : node.name
      result.push({
        label,
        value: node.id,
      })
      if (node.children && node.children.length > 0) {
        traverse(node.children, label)
      }
    })
  }
  traverse(tree)
  return result
}

function handleAdd() {
  current表单Data.value = {}
  formVisible.value = true
}

function handleEdit(record: ReportTemplate) {
  current表单Data.value = { ...record }
  formVisible.value = true
}

function handleDelete(record: ReportTemplate) {
  Modal.confirm({
    title: '提示',
    content: `确定要删除报表“${record.name}”吗？`,
    okText: '确定',
    cancelText: '取消',
    onOk: async () => {
      try {
        await remove(record.id)
        await tableRef.value?.refresh?.()
      } catch (error) {
        console.error('删除失败', error)
      }
    },
  })
}

function handleDesigner(record: ReportTemplate) {
  currentDesignerCode.value = record.code
  currentEngineType.value = (record.engineType as 'UREPORT' | 'JIMU') || 'UREPORT'
  designerVisible.value = true
}

function handlePreview(record: ReportTemplate) {
  currentPreviewCode.value = record.code
  currentPreviewEngineType.value = (record.engineType as 'UREPORT' | 'JIMU') || 'UREPORT'
  previewVisible.value = true
}

async function handle表单Ok() {
  formVisible.value = false
  await tableRef.value?.refresh?.()
}

async function handleDesignerOk() {
  designerVisible.value = false
  await tableRef.value?.refresh?.()
}

onMounted(async () => {
  await loadCategories()
  await loadDatasources()
  await tableRef.value?.refresh?.()
})
</script>

<style scoped lang="less">
.report-container {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
  overflow: hidden;
  padding: 16px;
  background-color: #fff;
}
</style>
