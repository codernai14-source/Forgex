<template>
  <div class="third-system-page">
    <fx-dynamic-table
      ref="tableRef"
      :table-code="'ThirdSystemTable'"
      :show-query-form="true"
      :request="handleRequest"
      :row-selection="{
        selectedRowKeys: selectedRowKeys,
        onChange: handleSelectionChange
      }"
    >
      <!-- 工具栏按钮 -->
      <template #toolbar>
        <a-button v-permission="'integration:third-system:add'" type="primary" @click="openAddDialog">
          新增系统
        </a-button>
        <a-button 
          v-permission="'integration:third-system:batch-delete'" 
          danger 
          :disabled="selectedRowKeys.length === 0"
          @click="handleBatchDelete"
        >
          批量删除
        </a-button>
      </template>

      <!-- 状态列自定义渲染 -->
      <template #status="{ record }">
        <a-switch
          v-model:checked="record.status"
          :checked-value="1"
          :unchecked-value="0"
          @change="handleStatusChange(record)"
        >
          <template #checkedChildren>启用</template>
          <template #unCheckedChildren>禁用</template>
        </a-switch>
      </template>

      <!-- 操作列 -->
      <template #action="{ record }">
        <a-button type="link" v-permission="'integration:third-system:edit'" @click="openEditDialog(record)">
          编辑
        </a-button>
        <a-button type="link" v-permission="'integration:third-system:auth'" @click="openAuthDialog(record)">
          授权
        </a-button>
        <a-popconfirm
          title="确定要删除该第三方系统吗？"
          ok-text="确定"
          cancel-text="取消"
          @confirm="handleDelete(record.id)"
        >
          <a-button type="link" danger v-permission="'integration:third-system:delete'">
            删除
          </a-button>
        </a-popconfirm>
      </template>
    </fx-dynamic-table>

    <!-- 新增/编辑弹窗 -->
    <third-system-form-dialog
      v-model:open="formVisible"
      :form-data="formData"
      :is-edit="isEdit"
      @submit="handleSubmit"
    />

    <!-- 授权弹窗 -->
    <third-system-auth-dialog
      v-model:open="authVisible"
      :system-id="currentSystemId"
      @submit="handleAuthSubmit"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { message } from 'ant-design-vue'
import { integrationApi, type ThirdSystemQuery, type ThirdSystemSubmit } from '@/api/system/integration'
import ThirdSystemFormDialog from './components/ThirdSystemFormDialog.vue'
import ThirdSystemAuthDialog from './components/ThirdSystemAuthDialog.vue'

defineOptions({
  name: 'ThirdSystemPage'
})

const tableRef = ref()
const selectedRowKeys = ref<number[]>([])
const formVisible = ref(false)
const authVisible = ref(false)
const isEdit = ref(false)
const formData = reactive<ThirdSystemSubmit>({
  systemCode: '',
  systemName: '',
  ipAddress: '',
  contactInfo: '',
  remark: '',
  status: 1
})
const currentSystemId = ref<number>()

/**
 * 表格数据请求
 */
const handleRequest = async (payload: { 
  page: { current: number; pageSize: number }
  query: Record<string, any>
}) => {
  const params: ThirdSystemQuery = {
    pageNum: payload.page.current,
    pageSize: payload.pageSize,
    ...payload.query
  }
  
  const data = await integrationApi.getThirdSystemList(params)
  return { 
    records: data.records || [], 
    total: data.total 
  }
}

/**
 * 打开新增弹窗
 */
const openAddDialog = () => {
  isEdit.value = false
  Object.assign(formData, {
    systemCode: '',
    systemName: '',
    ipAddress: '',
    contactInfo: '',
    remark: '',
    status: 1
  })
  formVisible.value = true
}

/**
 * 打开编辑弹窗
 */
const openEditDialog = async (record: any) => {
  isEdit.value = true
  const detail = await integrationApi.getThirdSystemDetail(record.id)
  Object.assign(formData, detail)
  formVisible.value = true
}

/**
 * 打开授权弹窗
 */
const openAuthDialog = (record: any) => {
  currentSystemId.value = record.id
  authVisible.value = true
}

/**
 * 提交表单
 */
const handleSubmit = async () => {
  try {
    if (isEdit.value) {
      await integrationApi.updateThirdSystem(formData)
      message.success('更新成功')
    } else {
      await integrationApi.addThirdSystem(formData)
      message.success('创建成功')
    }
    formVisible.value = false
    tableRef.value?.refresh()
  } catch (error: any) {
    message.error(error.message || '操作失败')
  }
}

/**
 * 提交授权
 */
const handleAuthSubmit = async () => {
  authVisible.value = false
  message.success('授权成功')
  tableRef.value?.refresh()
}

/**
 * 删除
 */
const handleDelete = async (id: number) => {
  try {
    await integrationApi.deleteThirdSystem(id)
    message.success('删除成功')
    tableRef.value?.refresh()
  } catch (error: any) {
    message.error(error.message || '删除失败')
  }
}

/**
 * 批量删除
 */
const handleBatchDelete = async () => {
  if (selectedRowKeys.value.length === 0) {
    message.warning('请选择要删除的数据')
    return
  }
  
  try {
    await integrationApi.batchDeleteThirdSystems(selectedRowKeys.value)
    message.success('批量删除成功')
    selectedRowKeys.value = []
    tableRef.value?.refresh()
  } catch (error: any) {
    message.error(error.message || '批量删除失败')
  }
}

/**
 * 状态变更
 */
const handleStatusChange = async (record: any) => {
  try {
    await integrationApi.updateThirdSystem({
      id: record.id,
      systemCode: record.systemCode,
      systemName: record.systemName,
      status: record.status
    })
    message.success(record.status === 1 ? '启用成功' : '禁用成功')
  } catch (error: any) {
    message.error(error.message || '状态更新失败')
    record.status = record.status === 1 ? 0 : 1
  }
}

/**
 * 选择变更
 */
const handleSelectionChange = (keys: any[]) => {
  selectedRowKeys.value = keys as number[]
}
</script>

<style scoped lang="less">
.third-system-page {
  padding: 24px;
  background: var(--fx-bg-base);
}
</style>
