<template>
  <div class="dict-container">
    <FxDynamicTable
      ref="tableRef"
      table-code="DictTable"
      :request="handleRequest"
      :dict-options="dictOptions"
      :fallback-config="fallbackConfig"
      :default-expand-all-rows="true"
      :show-query-form="true"
      row-key="id"
    >
      <template #toolbar>
        <a-button type="primary" @click="handleAdd(null)">新增字典类型</a-button>
      </template>

      <template #moduleId="{ record }">
        <a-tag v-if="resolveModuleLabel(record)" color="blue">
          {{ resolveModuleLabel(record) }}
        </a-tag>
        <span v-else>-</span>
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
          <a v-if="!record.dictValue" @click="handleAdd(record)">新增子项</a>
          <a @click="handleEdit(record)">编辑</a>
          <a style="color: #ff4d4f" @click="handleDelete(record)">删除</a>
        </a-space>
      </template>
    </FxDynamicTable>

    <a-modal
      v-model:open="dialogVisible"
      :title="dialogTitle"
      width="600px"
      @ok="handleSubmit"
      @cancel="handleDialogClose"
    >
      <a-form :model="form" :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }">
        <a-form-item label="字典名称" required>
          <a-input v-model:value="form.dictName" placeholder="请输入字典名称" />
        </a-form-item>
        <a-form-item label="所属模块">
          <a-select
            v-model:value="form.moduleId"
            :options="moduleOptions"
            :disabled="isChildNode"
            allow-clear
            placeholder="请选择所属模块"
          />
        </a-form-item>
        <a-form-item v-if="!isChildNode" label="字典编码" required>
          <a-input v-model:value="form.dictCode" placeholder="请输入字典编码" />
        </a-form-item>
        <a-form-item v-else label="字典值" required>
          <a-input v-model:value="form.dictValue" placeholder="请输入字典值" />
        </a-form-item>
        <a-form-item v-if="isChildNode" label="多语言配置">
          <I18nInput v-model="form.dictValueI18nJson" mode="table" />
        </a-form-item>
        <a-form-item v-if="isChildNode" label="标签样式">
          <TagStyleConfig ref="tagStyleConfigRef" />
        </a-form-item>
        <a-form-item label="排序号">
          <a-input-number v-model:value="form.orderNum" :min="0" style="width: 100%" />
        </a-form-item>
        <a-form-item label="状态">
          <a-radio-group v-model:value="form.status">
            <a-radio v-for="option in statusOptions" :key="option.value" :value="option.value">
              {{ option.label }}
            </a-radio>
          </a-radio-group>
        </a-form-item>
        <a-form-item label="备注">
          <a-textarea v-model:value="form.remark" :rows="3" placeholder="请输入备注" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { Modal } from 'ant-design-vue'
import http from '@/api/http'
import { listModules } from '@/api/system/module'
import type { FxTableConfig } from '@/api/system/tableConfig'
import FxDynamicTable from '@/components/common/FxDynamicTable.vue'
import I18nInput from '@/components/common/I18nInput.vue'
import TagStyleConfig from '@/components/system/TagStyleConfig.vue'
import { useDict } from '@/hooks/useDict'

const { dictItems: statusOptions } = useDict('status')

const tableRef = ref()
const dialogVisible = ref(false)
const dialogTitle = ref('')
const tagStyleConfigRef = ref()
const moduleOptions = ref<Array<{ label: string; value: number }>>([])

const dictOptions = computed(() => ({
  status: statusOptions.value,
  moduleId: moduleOptions.value,
}))

const fallbackConfig = computed<Partial<FxTableConfig>>(() => ({
  tableCode: 'DictTable',
  tableName: '字典管理',
  tableType: 'NORMAL',
  rowKey: 'id',
  defaultPageSize: 20,
  columns: [
    { field: 'dictCode', title: '字典编码', width: 180, align: 'left' },
    { field: 'dictName', title: '字典名称', width: 180, align: 'left' },
    { field: 'moduleId', title: '模块', width: 140, align: 'center', dictCode: 'moduleId' },
    { field: 'dictValue', title: '字典值', width: 180, align: 'left' },
    { field: 'orderNum', title: '排序', width: 90, align: 'center' },
    { field: 'status', title: '状态', width: 100, align: 'center', dictCode: 'status' },
    { field: 'remark', title: '备注', width: 220, align: 'left' },
    { field: 'createTime', title: '创建时间', width: 180, align: 'center' },
    { field: 'action', title: '操作', width: 180, align: 'center', fixed: 'right' },
  ],
  queryFields: [
    { field: 'dictCode', label: '字典编码', queryType: 'input', queryOperator: 'like' },
    { field: 'dictName', label: '字典名称', queryType: 'input', queryOperator: 'like' },
    { field: 'moduleId', label: '模块', queryType: 'select', queryOperator: 'eq', dictCode: 'moduleId' },
  ],
  version: 1,
}))

const form = reactive({
  id: null as number | null,
  parentId: 0,
  moduleId: undefined as number | undefined,
  dictName: '',
  dictCode: '',
  dictValue: '',
  dictValueI18nJson: '',
  orderNum: 0,
  status: 1,
  remark: '',
})

const isChildNode = computed(() => !!(form.parentId && form.parentId > 0))

function mapDictTreeNodes(nodes: any[]): any[] {
  return (nodes || []).map((item: any) => ({
    ...item,
    children: Array.isArray(item.children) ? mapDictTreeNodes(item.children) : [],
  }))
}

function resolveModuleLabel(record: any) {
  if (record?.moduleName) {
    return record.moduleName
  }
  const option = moduleOptions.value.find((item) => String(item.value) === String(record?.moduleId))
  return option?.label || ''
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
    const res = await http.post('/sys/dict/page', {
      pageNum: payload.page.current,
      pageSize: payload.page.pageSize,
      dictCode: payload.query?.dictCode,
      dictName: payload.query?.dictName,
      moduleId: payload.query?.moduleId,
    })
    return {
      records: mapDictTreeNodes(res?.records || []),
      total: Number(res?.total || 0),
    }
  } catch (error) {
    console.error('加载字典分页数据失败', error)
    return {
      records: [],
      total: 0,
    }
  }
}

async function loadModules() {
  try {
    const modules: any[] = await listModules({})
    moduleOptions.value = (modules || []).map((item: any) => ({
      label: item.name,
      value: Number(item.id),
    }))
  } catch (error) {
    console.error('加载模块列表失败', error)
    moduleOptions.value = []
  }
}

function resetForm() {
  form.id = null
  form.parentId = 0
  form.moduleId = undefined
  form.dictName = ''
  form.dictCode = ''
  form.dictValue = ''
  form.dictValueI18nJson = ''
  form.orderNum = 0
  form.status = 1
  form.remark = ''
  tagStyleConfigRef.value?.setTagStyleJson('')
}

function handleAdd(row: any) {
  resetForm()
  dialogTitle.value = row ? '新增字典子项' : '新增字典类型'
  form.parentId = row ? Number(row.id) : 0
  form.moduleId = row?.moduleId != null ? Number(row.moduleId) : undefined
  dialogVisible.value = true
}

function handleEdit(row: any) {
  dialogTitle.value = '编辑字典'
  form.id = Number(row.id)
  form.parentId = Number(row.parentId || 0)
  form.moduleId = row?.moduleId != null ? Number(row.moduleId) : undefined
  form.dictName = row.dictName || ''
  form.dictCode = row.dictCode || ''
  form.dictValue = row.dictValue || ''
  form.dictValueI18nJson = row.dictValueI18nJson || ''
  form.orderNum = Number(row.orderNum || 0)
  form.status = row.status === 0 || row.status === '0' ? 0 : 1
  form.remark = row.remark || ''
  dialogVisible.value = true
  tagStyleConfigRef.value?.setTagStyleJson(row.tagStyleJson || '')
}

function handleDelete(row: any) {
  Modal.confirm({
    title: '提示',
    content: '确定要删除该字典吗？',
    okText: '确定',
    cancelText: '取消',
    onOk: async () => {
      await http.post('/sys/dict/delete', { id: row.id })
      await tableRef.value?.refresh?.()
    },
  })
}

async function handleSubmit() {
  const tagStyleJson = tagStyleConfigRef.value?.getTagStyleJson() || ''
  const url = form.id ? '/sys/dict/update' : '/sys/dict/create'
  await http.post(url, {
    ...form,
    moduleId: form.moduleId ?? null,
    tagStyleJson,
  })
  dialogVisible.value = false
  await tableRef.value?.refresh?.()
}

function handleDialogClose() {
  dialogVisible.value = false
  tagStyleConfigRef.value?.setTagStyleJson('')
}

onMounted(async () => {
  await loadModules()
  await tableRef.value?.refresh?.()
})
</script>

<style scoped lang="less">
.dict-container {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
  overflow: hidden;
}
</style>
