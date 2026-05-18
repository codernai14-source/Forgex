<template>
  <div class="report-container">
    <FxDynamicTable
      ref="tableRef"
      table-code="ReportTemplateTable"
      :request="handleRequest"
      :dict-options="dictOptions"
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
          {{ t('common.add') }}
        </a-button>
      </template>

      <template #engineType="{ record }">
        <a-tag :color="record.engineType === 'UREPORT' ? 'blue' : 'green'">
          {{ resolveEngineTypeLabel(record.engineType) }}
        </a-tag>
      </template>

      <template #status="{ record }">
        <a-tag
          v-if="resolveStatusTag(record.status)"
          :color="resolveStatusTag(record.status)?.color"
          :style="resolveStatusTag(record.status)?.style"
        >
          {{ resolveStatusTag(record.status)?.label }}
        </a-tag>
        <span v-else>{{ record.status ?? '-' }}</span>
      </template>

      <template #action="{ record }">
        <a-space>
          <a
            v-permission="'report:template:edit'"
            @click="handleEdit(record)"
          >
            {{ t('common.edit') }}
          </a>
          <a
            v-permission="'report:template:design'"
            @click="handleDesigner(record)"
          >
            {{ t('report.actions.designer') }}
          </a>
          <a
            v-permission="'report:template:preview'"
            @click="handlePreview(record)"
          >
            {{ t('report.actions.preview') }}
          </a>
          <a
            v-permission="'report:template:delete'"
            style="color: #ff4d4f"
            @click="handleDelete(record)"
          >
            {{ t('common.delete') }}
          </a>
        </a-space>
      </template>
    </FxDynamicTable>

    <ReportForm
      v-model:open="formVisible"
      :form-data="currentFormData"
      :category-options="categoryOptions"
      :datasource-options="datasourceOptions"
      @ok="handleFormOk"
    />

    <ReportDesigner
      v-model:open="designerVisible"
      :report-code="currentDesignerCode"
      :engine-type="currentEngineType"
      @ok="handleDesignerOk"
    />

    <ReportPreview
      v-model:open="previewVisible"
      :report-code="currentPreviewCode"
      :engine-type="currentPreviewEngineType"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { Modal } from 'ant-design-vue'
import { PlusOutlined } from '@ant-design/icons-vue'
import { useI18n } from 'vue-i18n'
import FxDynamicTable from '@/components/common/FxDynamicTable.vue'
import type {
  ReportCategory,
  ReportDatasource,
  ReportTemplate,
  ReportTemplateParam,
} from '@/report/types'
import {
  getAvailableDatasources,
  getCategoryTree,
  page,
  remove,
} from '@/api/report'
import { useDict } from '@/hooks/useDict'
import ReportDesigner from './components/ReportDesigner.vue'
import ReportForm from './components/ReportForm.vue'
import ReportPreview from './components/ReportPreview.vue'

const { dictItems: statusOptions } = useDict('status')
const { t } = useI18n()

const tableRef = ref()
const formVisible = ref(false)
const designerVisible = ref(false)
const previewVisible = ref(false)
const currentFormData = ref<Partial<ReportTemplate>>({})
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


function resolveEngineTypeLabel(value: string) {
  if (value === 'UREPORT') return 'UReport2'
  if (value === 'JIMU') return 'JimuReport'
  return value
}

function resolveStatusTag(value: unknown) {
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
    console.error('[Report] Failed to load report page:', error)
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
    console.error('[Report] Failed to load category tree:', error)
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
    console.error('[Report] Failed to load datasource list:', error)
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
  currentFormData.value = {}
  formVisible.value = true
}

function handleEdit(record: ReportTemplate) {
  currentFormData.value = { ...record }
  formVisible.value = true
}

function handleDelete(record: ReportTemplate) {
  Modal.confirm({
    title: t('common.tip'),
    content: t('report.messages.deleteConfirm', { name: record.name }),
    okText: t('common.confirm'),
    cancelText: t('common.cancel'),
    onOk: async () => {
      try {
        await remove(record.id)
        await tableRef.value?.refresh?.()
      } catch (error) {
        console.error('[Report] Failed to delete report:', error)
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

async function handleFormOk() {
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

<style scoped lang="less" src="@/styles/views/report/index.less"></style>
