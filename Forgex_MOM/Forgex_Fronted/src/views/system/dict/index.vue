<template>
  <div class="dict-container">
    <a-card :bordered="false">
      <!-- 工具栏 -->
      <div class="toolbar">
        <a-button type="primary" @click="handleAdd(null)">新增字典类型</a-button>
      </div>

      <!-- 树形表格 -->
      <a-table
        :columns="columns"
        :data-source="tableData"
        :loading="loading"
        row-key="id"
        :pagination="false"
        :default-expand-all-rows="true"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="record.status === 1 ? 'success' : 'error'">
              {{ record.status === 1 ? '启用' : '禁用' }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a v-if="!record.dictValue" @click="handleAdd(record)">新增子项</a>
              <a @click="handleEdit(record)">编辑</a>
              <a style="color: #ff4d4f" @click="handleDelete(record)">删除</a>
            </a-space>
          </template>
        </template>
      </a-table>
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
        <a-form-item label="排序号">
          <a-input-number v-model:value="form.orderNum" :min="0" style="width: 100%" />
        </a-form-item>
        <a-form-item label="状态">
          <a-radio-group v-model:value="form.status">
            <a-radio :value="1">启用</a-radio>
            <a-radio :value="0">禁用</a-radio>
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

const userStore = useUserStore()

// 表格数据
const tableData = ref<any[]>([])
const loading = ref(false)

// 表格列定义
const columns = [
  { title: '字典名称', dataIndex: 'dictName', key: 'dictName', width: 200 },
  { title: '字典编码', dataIndex: 'dictCode', key: 'dictCode', width: 150 },
  { title: '字典值', dataIndex: 'dictValue', key: 'dictValue', width: 120 },
  { title: '排序', dataIndex: 'orderNum', key: 'orderNum', width: 80, align: 'center' },
  { title: '状态', key: 'status', width: 80, align: 'center' },
  { title: '备注', dataIndex: 'remark', key: 'remark', ellipsis: true },
  { title: '操作', key: 'action', width: 200, fixed: 'right' }
]

// 对话框
const dialogVisible = ref(false)
const dialogTitle = ref('')

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

// 加载字典树
const loadDictTree = async () => {
  loading.value = true
  try {
    const res = await http.post('/sys/dict/tree', { tenantId: userStore.tenantId || 1 })
    tableData.value = res
  } catch (error) {
    console.error('加载字典树失败', error)
  } finally {
    loading.value = false
  }
}

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
        loadDictTree()
      } catch (error) {
        console.error('删除失败', error)
      }
    }
  })
}

// 提交
const handleSubmit = async () => {
  try {
    const url = form.id ? '/sys/dict/update' : '/sys/dict/create'
    await http.post(url, form)
    message.success(form.id ? '更新成功' : '新增成功')
    dialogVisible.value = false
    loadDictTree()
  } catch (error) {
    console.error('提交失败', error)
  }
}

// 对话框关闭
const handleDialogClose = () => {
  // 重置表单
}

// 初始化
onMounted(() => {
  loadDictTree()
})
</script>

<style scoped lang="less">
.dict-container {
  padding: 20px;
}

.toolbar {
  margin-bottom: 20px;
}
</style>
