<template>
  <div class="tenant-message-whitelist-container">
    <!-- 查询表单 -->
    <a-card :bordered="false" style="margin-bottom: 16px">
      <a-form layout="inline" :model="queryForm">
        <a-form-item label="发送方租户">
          <a-select
            v-model:value="queryForm.senderTenantId"
            placeholder="请选择发送方租户"
            style="width: 200px"
            allowClear
          >
            <a-select-option v-for="tenant in tenantList" :key="tenant.id" :value="tenant.id">
              {{ tenant.tenantName }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="接收方租户">
          <a-select
            v-model:value="queryForm.receiverTenantId"
            placeholder="请选择接收方租户"
            style="width: 200px"
            allowClear
          >
            <a-select-option v-for="tenant in tenantList" :key="tenant.id" :value="tenant.id">
              {{ tenant.tenantName }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="状态">
          <a-select
            v-model:value="queryForm.enabled"
            placeholder="请选择状态"
            style="width: 120px"
            allowClear
          >
            <a-select-option :value="true">启用</a-select-option>
            <a-select-option :value="false">禁用</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="handleQuery">
              <template #icon><SearchOutlined /></template>
              查询
            </a-button>
            <a-button @click="handleReset">
              <template #icon><ReloadOutlined /></template>
              重置
            </a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <!-- 操作按钮和表格 -->
    <a-card :bordered="false">
      <div style="margin-bottom: 16px">
        <a-space>
          <a-button type="primary" @click="handleAdd" v-permission="'sys:tenant-message-whitelist:create'">
            <template #icon><PlusOutlined /></template>
            新增
          </a-button>
          <a-button
            danger
            :disabled="selectedRowKeys.length === 0"
            @click="handleBatchDelete"
            v-permission="'sys:tenant-message-whitelist:delete'"
          >
            <template #icon><DeleteOutlined /></template>
            批量删除
          </a-button>
        </a-space>
      </div>

      <a-table
        :columns="columns"
        :data-source="dataSource"
        :loading="loading"
        :pagination="pagination"
        :row-selection="{ selectedRowKeys, onChange: onSelectChange }"
        @change="handleTableChange"
        row-key="id"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'senderTenantName'">
            <a-tag color="blue">{{ record.senderTenantName }}</a-tag>
          </template>
          <template v-if="column.key === 'receiverTenantName'">
            <a-tag color="green">{{ record.receiverTenantName }}</a-tag>
          </template>
          <template v-if="column.key === 'enabled'">
            <a-switch
              :checked="record.enabled"
              @change="(checked) => handleToggleEnabled(record, checked)"
              v-permission="'sys:tenant-message-whitelist:update'"
            />
          </template>
          <template v-if="column.key === 'action'">
            <a-space>
              <a-button
                type="link"
                size="small"
                @click="handleEdit(record)"
                v-permission="'sys:tenant-message-whitelist:update'"
              >
                编辑
              </a-button>
              <a-popconfirm
                title="确定要删除这条白名单配置吗？"
                @confirm="handleDelete(record)"
                ok-text="确定"
                cancel-text="取消"
              >
                <a-button
                  type="link"
                  size="small"
                  danger
                  v-permission="'sys:tenant-message-whitelist:delete'"
                >
                  删除
                </a-button>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 新增/编辑弹窗 -->
    <a-modal
      v-model:open="modalVisible"
      :title="modalTitle"
      :width="600"
      @ok="handleSubmit"
      @cancel="handleCancel"
    >
      <a-form :model="formData" :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }">
        <a-form-item label="发送方租户" required>
          <a-select
            v-model:value="formData.senderTenantId"
            placeholder="请选择发送方租户"
            :disabled="isEdit"
          >
            <a-select-option v-for="tenant in tenantList" :key="tenant.id" :value="tenant.id">
              {{ tenant.tenantName }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="接收方租户" required>
          <a-select
            v-model:value="formData.receiverTenantId"
            placeholder="请选择接收方租户"
            :disabled="isEdit"
          >
            <a-select-option v-for="tenant in tenantList" :key="tenant.id" :value="tenant.id">
              {{ tenant.tenantName }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="状态">
          <a-switch v-model:checked="formData.enabled" checked-children="启用" un-checked-children="禁用" />
        </a-form-item>
        <a-form-item label="备注">
          <a-textarea
            v-model:value="formData.remark"
            placeholder="请输入备注说明"
            :rows="4"
            :maxlength="500"
            show-count
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { message, Modal } from 'ant-design-vue'
import {
  SearchOutlined,
  ReloadOutlined,
  PlusOutlined,
  DeleteOutlined
} from '@ant-design/icons-vue'
import {
  pageTenantMessageWhitelist,
  saveTenantMessageWhitelist,
  deleteTenantMessageWhitelist,
  batchDeleteTenantMessageWhitelist,
  toggleEnabled
} from '@/api/tenantMessageWhitelist'
import { pageTenant } from '@/api/tenant'

// 查询表单
const queryForm = reactive({
  senderTenantId: undefined,
  receiverTenantId: undefined,
  enabled: undefined
})

// 表格数据
const dataSource = ref([])
const loading = ref(false)
const selectedRowKeys = ref<number[]>([])

// 分页配置
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total: number) => `共 ${total} 条`
})

// 表格列配置
const columns = [
  {
    title: '发送方租户',
    dataIndex: 'senderTenantName',
    key: 'senderTenantName',
    width: 200
  },
  {
    title: '接收方租户',
    dataIndex: 'receiverTenantName',
    key: 'receiverTenantName',
    width: 200
  },
  {
    title: '状态',
    dataIndex: 'enabled',
    key: 'enabled',
    width: 100,
    align: 'center'
  },
  {
    title: '备注',
    dataIndex: 'remark',
    key: 'remark',
    ellipsis: true
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    key: 'createTime',
    width: 180
  },
  {
    title: '操作',
    key: 'action',
    width: 150,
    align: 'center',
    fixed: 'right'
  }
]

// 弹窗相关
const modalVisible = ref(false)
const modalTitle = ref('新增白名单')
const isEdit = ref(false)
const formData = reactive({
  id: undefined,
  senderTenantId: undefined,
  receiverTenantId: undefined,
  enabled: true,
  remark: ''
})

// 租户列表
const tenantList = ref([])

// 加载租户列表
const loadTenantList = async () => {
  try {
    const res = await pageTenant({ current: 1, size: 1000 })
    if (res.code === 200) {
      tenantList.value = res.data.records || []
    }
  } catch (error) {
    console.error('加载租户列表失败:', error)
  }
}

// 查询数据
const loadData = async () => {
  loading.value = true
  try {
    const params = {
      current: pagination.current,
      size: pagination.pageSize,
      ...queryForm
    }
    const res = await pageTenantMessageWhitelist(params)
    if (res.code === 200) {
      dataSource.value = res.data.records || []
      pagination.total = res.data.total || 0
    } else {
      message.error(res.message || '查询失败')
    }
  } catch (error) {
    message.error('查询失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

// 查询
const handleQuery = () => {
  pagination.current = 1
  loadData()
}

// 重置
const handleReset = () => {
  queryForm.senderTenantId = undefined
  queryForm.receiverTenantId = undefined
  queryForm.enabled = undefined
  handleQuery()
}

// 表格变化
const handleTableChange = (pag: any) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  loadData()
}

// 行选择
const onSelectChange = (keys: number[]) => {
  selectedRowKeys.value = keys
}

// 新增
const handleAdd = () => {
  modalTitle.value = '新增白名单'
  isEdit.value = false
  formData.id = undefined
  formData.senderTenantId = undefined
  formData.receiverTenantId = undefined
  formData.enabled = true
  formData.remark = ''
  modalVisible.value = true
}

// 编辑
const handleEdit = (record: any) => {
  modalTitle.value = '编辑白名单'
  isEdit.value = true
  formData.id = record.id
  formData.senderTenantId = record.senderTenantId
  formData.receiverTenantId = record.receiverTenantId
  formData.enabled = record.enabled
  formData.remark = record.remark
  modalVisible.value = true
}

// 提交
const handleSubmit = async () => {
  // 表单验证
  if (!formData.senderTenantId) {
    message.warning('请选择发送方租户')
    return
  }
  if (!formData.receiverTenantId) {
    message.warning('请选择接收方租户')
    return
  }
  if (formData.senderTenantId === formData.receiverTenantId) {
    message.warning('发送方租户和接收方租户不能相同')
    return
  }

  try {
    const res = await saveTenantMessageWhitelist(formData)
    if (res.code === 200) {
      message.success(isEdit.value ? '修改成功' : '新增成功')
      modalVisible.value = false
      loadData()
    } else {
      message.error(res.message || '操作失败')
    }
  } catch (error) {
    message.error('操作失败')
    console.error(error)
  }
}

// 取消
const handleCancel = () => {
  modalVisible.value = false
}

// 删除
const handleDelete = async (record: any) => {
  try {
    const res = await deleteTenantMessageWhitelist(record.id)
    if (res.code === 200) {
      message.success('删除成功')
      loadData()
    } else {
      message.error(res.message || '删除失败')
    }
  } catch (error) {
    message.error('删除失败')
    console.error(error)
  }
}

// 批量删除
const handleBatchDelete = () => {
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除选中的 ${selectedRowKeys.value.length} 条白名单配置吗？`,
    onOk: async () => {
      try {
        const res = await batchDeleteTenantMessageWhitelist(selectedRowKeys.value)
        if (res.code === 200) {
          message.success('删除成功')
          selectedRowKeys.value = []
          loadData()
        } else {
          message.error(res.message || '删除失败')
        }
      } catch (error) {
        message.error('删除失败')
        console.error(error)
      }
    }
  })
}

// 启用/禁用
const handleToggleEnabled = async (record: any, checked: boolean) => {
  try {
    const res = await toggleEnabled(record.id, checked)
    if (res.code === 200) {
      message.success(checked ? '启用成功' : '禁用成功')
      record.enabled = checked
    } else {
      message.error(res.message || '操作失败')
    }
  } catch (error) {
    message.error('操作失败')
    console.error(error)
  }
}

// 初始化
onMounted(() => {
  loadTenantList()
  loadData()
})
</script>

<style scoped lang="less">
.tenant-message-whitelist-container {
  padding: 16px;
}
</style>

