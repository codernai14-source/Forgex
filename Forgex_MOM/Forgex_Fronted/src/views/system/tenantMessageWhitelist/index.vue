<template>
  <div class="tenant-message-whitelist-container">
    <FxDynamicTable
      ref="tableRef"
      table-code="TenantMessageWhitelistTable"
      :request="handleRequest"
      :dict-options="dictOptions"
      :row-selection="{
        selectedRowKeys,
        onChange: onSelectChange
      }"
      row-key="id"
    >
      <template #toolbar>
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
      </template>

      <template #senderTenantId="{ record }">
        <a-tag color="blue">
          {{ getTenantName(record.senderTenantId) || record.senderTenantName || record.senderTenantId }}
        </a-tag>
      </template>

      <template #receiverTenantId="{ record }">
        <a-tag color="green">
          {{ getTenantName(record.receiverTenantId) || record.receiverTenantName || record.receiverTenantId }}
        </a-tag>
      </template>

      <template #enabled="{ record }">
        <a-switch
          :checked="record.enabled"
          @change="(checked) => handleToggleEnabled(record, checked)"
          v-permission="'sys:tenant-message-whitelist:update'"
        />
      </template>

      <template #action="{ record }">
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
    </FxDynamicTable>

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
import { computed, ref, reactive, onMounted } from 'vue'
import { message, Modal } from 'ant-design-vue'
import {
  PlusOutlined,
  DeleteOutlined
} from '@ant-design/icons-vue'
import {
  pageTenantMessageWhitelist,
  saveTenantMessageWhitelist,
  deleteTenantMessageWhitelist,
  toggleEnabled
} from '@/api/tenantMessageWhitelist'
import { listTenant } from '@/api/system/tenant'
import FxDynamicTable from '@/components/common/FxDynamicTable.vue'

const tableRef = ref<any>()
const selectedRowKeys = ref<number[]>([])

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

const dictOptions = computed(() => {
  const tenants = (tenantList.value || []).map((t: any) => ({ label: t.tenantName, value: t.id }))
  return {
    senderTenantId: tenants,
    receiverTenantId: tenants,
    enabled: [
      { label: '启用', value: true },
      { label: '禁用', value: false },
    ],
  }
})

const getTenantName = (tenantId: any) => {
  const list: any[] = tenantList.value as any
  const found = list.find(t => String(t.id) === String(tenantId))
  return found?.tenantName
}

// 加载租户列表
const loadTenantList = async () => {
  try {
    const res = await listTenant({})
    tenantList.value = res || []
  } catch (error) {
    console.error('加载租户列表失败:', error)
  }
}

const handleRequest = async (payload: {
  page: { current: number; pageSize: number }
  query: Record<string, any>
  sorter?: { field?: string; order?: string }
}) => {
  const params: any = {
    current: payload.page.current,
    size: payload.page.pageSize,
    ...payload.query,
  }

  if (payload.sorter?.field) {
    params.sortField = payload.sorter.field
    params.sortOrder = payload.sorter.order
  }

  const res: any = await pageTenantMessageWhitelist(params)
  const total = typeof res.total === 'number' ? res.total : parseInt(String(res.total) || '0', 10)
  return { records: res.records || [], total }
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

  await saveTenantMessageWhitelist(formData)
  modalVisible.value = false
  await tableRef.value?.reload?.()
}

// 取消
const handleCancel = () => {
  modalVisible.value = false
}

// 删除
const handleDelete = async (record: any) => {
  await deleteTenantMessageWhitelist(record.id)
  await tableRef.value?.reload?.()
}

// 批量删除
const handleBatchDelete = () => {
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除选中的 ${selectedRowKeys.value.length} 条白名单配置吗？`,
    onOk: async () => {
      for (const id of selectedRowKeys.value) {
        await deleteTenantMessageWhitelist(id)
      }
      selectedRowKeys.value = []
      await tableRef.value?.reload?.()
    }
  })
}

// 启用/禁用
const handleToggleEnabled = async (record: any, checked: boolean) => {
  await toggleEnabled(record.id, checked)
  record.enabled = checked
}

// 初始化
onMounted(() => {
  loadTenantList()
})
</script>

<style scoped lang="less">
.tenant-message-whitelist-container {
  padding: 16px;
}
</style>
