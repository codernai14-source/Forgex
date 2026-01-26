<template>
  <div class="dict-container">
    <a-card :bordered="false" class="dict-table-card">
      <!-- 工具栏 -->
      <div class="toolbar">
        <a-button type="primary" @click="handleAdd(null)">新增字典类型</a-button>
      </div>

      <!-- 树形表格 -->
      <fx-dynamic-table
        ref="tableRef"
        :table-code="'DictTable'"
        :request="handleRequest"
        :fallback-config="fallbackConfig"
        :dict-options="dictOptions"
        row-key="id"
        :pagination="false"
        :default-expand-all-rows="true"
      >
        <template #action="{ record }">
          <a-space>
            <a v-if="!record.dictValue" @click="handleAdd(record)">新增子项</a>
            <a @click="handleEdit(record)">编辑</a>
            <a style="color: #ff4d4f" @click="handleDelete(record)">删除</a>
          </a-space>
        </template>
      </fx-dynamic-table>
    </a-card>

    <!-- 新增/编辑对话框 -->
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
import { ref, reactive, onMounted } from 'vue'
import { message, Modal } from 'ant-design-vue'
import http from '@/api/http'
import { useUserStore } from '@/stores/user'
import { useDict } from '@/hooks/useDict'
import TagStyleConfig from '@/components/system/TagStyleConfig.vue'

const userStore = useUserStore()
const { dictItems: statusOptions } = useDict('status')

// 表格相关
const tableRef = ref()
const loading = ref(false)

// fallback配置
const fallbackConfig = ref({
  columns: [
    { title: '字典名称', dataIndex: 'dictName', key: 'dictName', width: 200 },
    { title: '字典编码', dataIndex: 'dictCode', key: 'dictCode', width: 150 },
    { title: '字典值', dataIndex: 'dictValue', key: 'dictValue', width: 120 },
    { title: '排序', dataIndex: 'orderNum', key: 'orderNum', width: 80, align: 'center' },
    { title: '状态', key: 'status', width: 80, align: 'center', dictCode: 'status' },
    { title: '备注', dataIndex: 'remark', key: 'remark', ellipsis: true },
    { title: '操作', key: 'action', width: 200, fixed: 'right' }
  ]
})

// 字典配置
const dictOptions = ref({
  status: statusOptions
})

// 处理表格数据请求
const handleRequest = async (params: any) => {
  loading.value = true
  try {
    const res = await http.post('/sys/dict/tree', { tenantId: userStore.tenantId || 1, ...params })
    return {
      success: true,
      data: res || [],
      total: res?.length || 0
    }
  } catch (error) {
    console.error('加载字典树失败', error)
    return {
      success: false,
      data: [],
      total: 0
    }
  } finally {
    loading.value = false
  }
}

// 加载字典树
const loadDictTree = async () => {
  await tableRef.value?.refresh?.()
}

// 对话框
const dialogVisible = ref(false)
const dialogTitle = ref('')
const tagStyleConfigRef = ref()

// 表单
const form = reactive({
  id: null as number | null,
  parentId: 0,
  dictName: '',
  dictCode: '',
  dictValue: '',
  orderNum: 0,
  status: 1,
  remark: ''
})

// 新增
const handleAdd = (row: any) => {
  dialogTitle.value = row ? '新增字典项' : '新增字典类型'
  form.id = null
  form.parentId = row ? row.id : 0
  form.dictName = ''
  form.dictCode = ''
  form.dictValue = ''
  form.orderNum = 0
  form.status = 1
  form.remark = ''
  dialogVisible.value = true
  // 重置标签样式配置
  tagStyleConfigRef.value?.setTagStyleJson('')
}

// 编辑
const handleEdit = (row: any) => {
  dialogTitle.value = '编辑字典'
  form.id = row.id
  form.parentId = row.parentId
  form.dictName = row.dictName
  form.dictCode = row.dictCode || ''
  form.dictValue = row.dictValue || ''
  form.orderNum = row.orderNum
  form.status = row.status
  form.remark = row.remark || ''
  dialogVisible.value = true
  // 加载标签样式配置
  tagStyleConfigRef.value?.setTagStyleJson(row.tagStyleJson || '')
}

// 删除
const handleDelete = async (row: any) => {
  Modal.confirm({
    title: '提示',
    content: '确定要删除该字典吗？',
    okText: '确定',
    cancelText: '取消',
    onOk: async () => {
      try {
        await http.post('/sys/dict/delete', { id: row.id })
        message.success('删除成功')
        await tableRef.value?.refresh?.()
      } catch (error) {
        console.error('删除失败', error)
      }
    }
  })
}

// 提交
const handleSubmit = async () => {
  try {
    // 获取标签样式配置JSON
    const tagStyleJson = tagStyleConfigRef.value?.getTagStyleJson() || ''
    const submitData = {
      ...form,
      tagStyleJson
    }
    const url = form.id ? '/sys/dict/update' : '/sys/dict/create'
    await http.post(url, submitData)
    message.success(form.id ? '更新成功' : '新增成功')
    dialogVisible.value = false
    await tableRef.value?.refresh?.()
  } catch (error) {
    console.error('提交失败', error)
  }
}

// 对话框关闭
const handleDialogClose = () => {
  // 重置表单
}

// 初始化
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

.toolbar {
  margin-bottom: 20px;
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
