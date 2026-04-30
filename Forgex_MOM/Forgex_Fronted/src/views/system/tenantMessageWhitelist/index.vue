<template>
  <div class="tenant-message-whitelist-container">
    <a-alert
      :message="t('system.tenantMessageWhitelist.guide.title')"
      :description="t('system.tenantMessageWhitelist.guide.description')"
      type="info"
      show-icon
    />

    <FxDynamicTable
      ref="tableRef"
      table-code="TenantMessageWhitelistTable"
      :show-query-form="true"
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
        <a-button data-guide-id="sys-tenant-message-whitelist-add" type="primary" @click="handleAdd" v-permission="'sys:tenant-message-whitelist:create'">
            <template #icon><PlusOutlined /></template>
            {{ t('common.add') }}
          </a-button>
        <a-button
          data-guide-id="sys-tenant-message-whitelist-batch-delete"
          danger
            :disabled="selectedRowKeys.length === 0"
            @click="handleBatchDelete"
            v-permission="'sys:tenant-message-whitelist:delete'"
          >
            <template #icon><DeleteOutlined /></template>
            {{ t('common.batchDelete') }}
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
          @change="handleToggleEnabled(record, $event)"
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
            {{ t('common.edit') }}
          </a-button>
          <a-popconfirm
            :title="t('system.tenantMessageWhitelist.confirm.deleteContent')"
            @confirm="handleDelete(record)"
            :ok-text="t('common.confirm')"
            :cancel-text="t('common.cancel')"
          >
            <a-button
              type="link"
              size="small"
              danger
              v-permission="'sys:tenant-message-whitelist:delete'"
            >
              {{ t('common.delete') }}
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
        <a-form-item :label="t('system.tenantMessageWhitelist.form.senderTenant')" required>
          <a-select
            v-model:value="formData.senderTenantId"
            :placeholder="t('system.tenantMessageWhitelist.form.senderTenantPlaceholder')"
            :disabled="isEdit"
          >
            <a-select-option v-for="tenant in tenantList" :key="tenant.id" :value="tenant.id">
              {{ tenant.tenantName }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item :label="t('system.tenantMessageWhitelist.form.receiverTenant')" required>
          <a-select
            v-model:value="formData.receiverTenantId"
            :placeholder="t('system.tenantMessageWhitelist.form.receiverTenantPlaceholder')"
            :disabled="isEdit"
          >
            <a-select-option v-for="tenant in tenantList" :key="tenant.id" :value="tenant.id">
              {{ tenant.tenantName }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item :label="t('common.status')">
          <a-switch
            v-model:checked="formData.enabled"
            :checked-children="t('common.enabled')"
            :un-checked-children="t('common.disabled')"
          />
        </a-form-item>
        <a-form-item :label="t('common.remark')">
          <a-textarea
            v-model:value="formData.remark"
            :placeholder="t('system.tenantMessageWhitelist.form.remarkPlaceholder')"
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
import { useI18n } from 'vue-i18n'
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

interface TenantOption {
  id: number | string
  tenantName: string
}

const { t } = useI18n()

const tableRef = ref<any>()
const selectedRowKeys = ref<number[]>([])

// 弹窗相关
const modalVisible = ref(false)
const isEdit = ref(false)
const modalTitle = computed(() => (
  isEdit.value
    ? t('system.tenantMessageWhitelist.modal.editTitle')
    : t('system.tenantMessageWhitelist.modal.addTitle')
))
const formData = reactive({
  id: undefined,
  senderTenantId: undefined,
  receiverTenantId: undefined,
  enabled: true,
  remark: ''
})

// 租户列表
const tenantList = ref<TenantOption[]>([])

const dictOptions = computed(() => {
  const tenants = (tenantList.value || []).map((t: any) => ({ label: t.tenantName, value: t.id }))
  return {
    senderTenantId: tenants,
    receiverTenantId: tenants,
    enabled: [
      { label: t('common.enabled'), value: true },
      { label: t('common.disabled'), value: false },
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
    message.warning(t('system.tenantMessageWhitelist.message.selectSenderTenant'))
    return
  }
  if (!formData.receiverTenantId) {
    message.warning(t('system.tenantMessageWhitelist.message.selectReceiverTenant'))
    return
  }
  if (formData.senderTenantId === formData.receiverTenantId) {
    message.warning(t('system.tenantMessageWhitelist.message.sameTenantNotAllowed'))
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
    title: t('system.tenantMessageWhitelist.confirm.batchDeleteTitle'),
    content: t('system.tenantMessageWhitelist.confirm.batchDeleteContent', { count: selectedRowKeys.value.length }),
    onOk: async () => {
      for (const id of selectedRowKeys.value) {
        await deleteTenantMessageWhitelist(id, { showSuccessMessage: false })
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
  display: flex;
  flex-direction: column;
  gap: 16px;
}
</style>
