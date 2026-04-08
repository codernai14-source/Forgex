<template>
  <div class="dict-container">
    <a-card :bordered="false" class="dict-table-card">
      <fx-dynamic-table
        ref="tableRef"
        :table-code="'DictTable'"
        :request="handleRequest"
        :dict-options="dictOptions"
        :fallback-config="fallbackConfig"
        :default-expand-all-rows="true"
        :show-query-form="false"
        row-key="id"
      >
        <template #toolbar>
          <a-button type="primary" @click="handleAdd(null)">新增字典类型</a-button>
        </template>

        <template #action="{ record }">
          <a-space>
            <a v-if="!record.dictValue" @click="handleAdd(record)">新增子项</a>
            <a @click="handleEdit(record)">编辑</a>
            <a style="color: #ff4d4f" @click="handleDelete(record)">删除</a>
          </a-space>
        </template>
      </fx-dynamic-table>
    </a-card>

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
        <a-form-item label="字典编码" v-if="!form.parentId || form.parentId === 0" required>
          <a-input v-model:value="form.dictCode" placeholder="请输入字典编码" />
        </a-form-item>
        <a-form-item label="字典值" v-if="form.parentId && form.parentId > 0" required>
          <a-input v-model:value="form.dictValue" placeholder="请输入字典值" />
        </a-form-item>
        <a-form-item label="多语言配置" v-if="form.parentId && form.parentId > 0">
          <I18nInput v-model="form.dictValueI18nJson" mode="table" />
        </a-form-item>
        <a-form-item label="标签样式" v-if="form.parentId && form.parentId > 0">
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
import { onMounted, reactive, ref } from 'vue'
import { Modal } from 'ant-design-vue'
import http from '@/api/http'
import { useDict } from '@/hooks/useDict'
import type { FxTableConfig } from '@/api/system/tableConfig'
import I18nInput from '@/components/common/I18nInput.vue'
import TagStyleConfig from '@/components/system/TagStyleConfig.vue'

const { dictItems: statusOptions } = useDict('status')

const tableRef = ref()
const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const tagStyleConfigRef = ref()

const dictOptions = ref({
  status: statusOptions
})

const fallbackConfig: Partial<FxTableConfig> = {
  columns: [
    { field: 'dictCode', title: '字典编码', width: 180, align: 'left' },
    { field: 'dictName', title: '字典名称', width: 180, align: 'left' },
    { field: 'dictValue', title: '字典值', width: 180, align: 'left' },
    { field: 'orderNum', title: '排序', width: 90, align: 'center' },
    { field: 'status', title: '状态', width: 100, align: 'center', dictCode: 'status' },
    { field: 'remark', title: '备注', width: 220, align: 'left' },
    { field: 'createTime', title: '创建时间', width: 180, align: 'center' },
    { field: 'action', title: '操作', width: 180, align: 'center', fixed: 'right' }
  ],
  queryFields: []
}

const form = reactive({
  id: null as number | null,
  parentId: 0,
  dictName: '',
  dictCode: '',
  dictValue: '',
  dictValueI18nJson: '',
  orderNum: 0,
  status: 1,
  remark: ''
})

function mapDictTreeNodes(nodes: any[]): any[] {
  return (nodes || []).map((item: any) => ({
    ...item,
    children: Array.isArray(item.children) ? mapDictTreeNodes(item.children) : []
  }))
}

const handleRequest = async (payload: {
  page: { current: number; pageSize: number }
}) => {
  loading.value = true
  try {
    const res = await http.post('/sys/dict/page', {
      pageNum: payload.page.current,
      pageSize: payload.page.pageSize
    })
    return {
      records: mapDictTreeNodes(res?.records || []),
      total: Number(res?.total || 0)
    }
  } catch (error) {
    console.error('加载字典分页数据失败', error)
    return {
      records: [],
      total: 0
    }
  } finally {
    loading.value = false
  }
}

const handleAdd = (row: any) => {
  dialogTitle.value = row ? '新增字典项' : '新增字典类型'
  form.id = null
  form.parentId = row ? row.id : 0
  form.dictName = ''
  form.dictCode = ''
  form.dictValue = ''
  form.dictValueI18nJson = ''
  form.orderNum = 0
  form.status = 1
  form.remark = ''
  dialogVisible.value = true
  tagStyleConfigRef.value?.setTagStyleJson('')
}

const handleEdit = (row: any) => {
  dialogTitle.value = '编辑字典'
  form.id = row.id
  form.parentId = row.parentId
  form.dictName = row.dictName
  form.dictCode = row.dictCode || ''
  form.dictValue = row.dictValue || ''
  form.dictValueI18nJson = row.dictValueI18nJson || ''
  form.orderNum = row.orderNum
  form.status = row.status
  form.remark = row.remark || ''
  dialogVisible.value = true
  tagStyleConfigRef.value?.setTagStyleJson(row.tagStyleJson || '')
}

const handleDelete = async (row: any) => {
  Modal.confirm({
    title: '提示',
    content: '确定要删除该字典吗？',
    okText: '确定',
    cancelText: '取消',
    onOk: async () => {
      try {
        await http.post('/sys/dict/delete', { id: row.id })
        await tableRef.value?.refresh?.()
      } catch (error) {
        console.error('删除字典失败', error)
      }
    }
  })
}

const handleSubmit = async () => {
  try {
    const tagStyleJson = tagStyleConfigRef.value?.getTagStyleJson() || ''
    const url = form.id ? '/sys/dict/update' : '/sys/dict/create'
    await http.post(url, {
      ...form,
      tagStyleJson
    })
    dialogVisible.value = false
    await tableRef.value?.refresh?.()
  } catch (error) {
    console.error('提交字典失败', error)
  }
}

const handleDialogClose = () => {
  tagStyleConfigRef.value?.setTagStyleJson('')
}

onMounted(async () => {
  await tableRef.value?.refresh?.()
})
</script>

<style scoped lang="less">
.dict-container {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
  padding: 20px;
  box-sizing: border-box;
}

.dict-table-card {
  display: flex;
  flex-direction: column;
  flex: 1 1 auto;
  min-height: 0;
}

.dict-table-card :deep(.ant-card-body) {
  display: flex;
  flex-direction: column;
  flex: 1 1 auto;
  min-height: 0;
  height: 100%;
}

.dict-table-card :deep(.fx-dynamic-table) {
  flex: 1 1 auto;
  min-height: 0;
}
</style>
