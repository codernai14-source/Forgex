<template>
  <div class="page-wrap">
    <a-card class="content-card" :bordered="false">
      <!-- 查询表单 -->
      <a-form
        :model="queryForm"
        layout="inline"
        style="margin-bottom: 16px"
      >
        <a-form-item label="租户名称">
          <a-input
            v-model:value="queryForm.tenantName"
            placeholder="请输入租户名称"
            allow-clear
            style="width: 200px"
            @pressEnter="handleQuery"
          />
        </a-form-item>

        <a-form-item label="租户编码">
          <a-input
            v-model:value="queryForm.tenantCode"
            placeholder="请输入租户编码"
            allow-clear
            style="width: 200px"
            @pressEnter="handleQuery"
          />
        </a-form-item>

        <a-form-item label="租户类别">
          <a-select
            v-model:value="queryForm.tenantType"
            placeholder="请选择租户类别"
            allow-clear
            style="width: 150px"
          >
            <a-select-option
              v-for="(label, value) in TenantTypeLabels"
              :key="value"
              :value="value"
            >
              {{ label }}
            </a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item label="状态">
          <a-select
            v-model:value="queryForm.status"
            placeholder="请选择状态"
            allow-clear
            style="width: 120px"
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

      <!-- 操作按钮 -->
      <div style="margin-bottom: 16px">
        <a-space>
          <a-button
            type="primary"
            @click="openAdd"
            v-permission="'sys:tenant:create'"
          >
            <template #icon><PlusOutlined /></template>
            新增租户
          </a-button>
        </a-space>
      </div>

      <!-- 租户列表 -->
      <fx-dynamic-table
        ref="tableRef"
        :table-code="'TenantTable'"
        :request="handleRequest"
        :fallback-config="fallbackConfig"
        :dict-options="dictOptions"
        :loading="loading"
        :pagination="false"
        row-key="id"
        :scroll="{ y: tableHeight }"
      >
        <template #tenantType="{ record }">
          <a-tag :color="getTenantTypeColor(record.tenantType)">
            {{ TenantTypeLabels[record.tenantType] }}
          </a-tag>
        </template>

        <template #status="{ record }">
          <a-tag :color="record.status ? 'green' : 'red'">
            {{ record.status ? '启用' : '禁用' }}
          </a-tag>
        </template>

        <template #logo="{ record }">
          <a-image
            v-if="record.logo"
            :src="formatLogoUrl(record.logo)"
            :width="40"
            :height="40"
            style="border-radius: 4px"
          />
          <span v-else>-</span>
        </template>

        <template #action="{ record }">
          <a-space>
            <a-button
              type="link"
              size="small"
              @click="openEdit(record)"
              v-permission="'sys:tenant:edit'"
            >
              <template #icon><EditOutlined /></template>
              编辑
            </a-button>
            <a-popconfirm
              title="确定要删除这个租户吗？"
              :ok-text="$t('common.confirm')"
              :cancel-text="$t('common.cancel')"
              @confirm="handleDelete(record)"
            >
              <a-button
                type="link"
                size="small"
                danger
                v-permission="'sys:tenant:delete'"
                :disabled="record.tenantType === TenantTypeEnum.MAIN_TENANT"
              >
                <template #icon><DeleteOutlined /></template>
                删除
              </a-button>
            </a-popconfirm>
          </a-space>
        </template>
      </fx-dynamic-table>

      <!-- 新增/编辑表单：使用通用弹窗组件，支持弹窗/抽屉模式 -->
      <BaseFormDialog
        v-model:open="dialogVisible"
        :title="formData.id ? '编辑租户' : '新增租户'"
        :loading="saving"
        :width="600"
        @submit="handleSave"
        @cancel="handleCancel"
      >
        <a-form
          ref="formRef"
          :model="formData"
          :rules="rules"
          :label-col="{ span: 6 }"
          :wrapper-col="{ span: 16 }"
        >
          <a-form-item label="租户名称" name="tenantName">
            <a-input
              v-model:value="formData.tenantName"
              placeholder="请输入租户名称"
            />
          </a-form-item>

          <a-form-item label="租户编码" name="tenantCode">
            <a-input
              v-model:value="formData.tenantCode"
              placeholder="请输入租户编码"
            />
          </a-form-item>

          <a-form-item label="租户类别" name="tenantType">
            <a-select
              v-model:value="formData.tenantType"
              placeholder="请选择租户类别"
              :disabled="formData.id && formData.tenantType === TenantTypeEnum.MAIN_TENANT"
            >
              <a-select-option
                v-for="(label, value) in TenantTypeLabels"
                :key="value"
                :value="value"
              >
                {{ label }}
              </a-select-option>
            </a-select>
          </a-form-item>

          <a-form-item label="描述" name="description">
            <a-textarea
              v-model:value="formData.description"
              placeholder="请输入描述"
              :rows="3"
            />
          </a-form-item>

          <a-form-item label="Logo" name="logo">
            <div class="tenant-logo-upload">
              <AvatarUpload v-model="formData.logo" />
            </div>
          </a-form-item>

          <a-form-item label="状态" name="status">
            <a-radio-group v-model:value="formData.status">
              <a-radio :value="true">启用</a-radio>
              <a-radio :value="false">禁用</a-radio>
            </a-radio-group>
          </a-form-item>
        </a-form>
      </BaseFormDialog>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  SearchOutlined,
  ReloadOutlined
} from '@ant-design/icons-vue'
import {
  listTenant,
  createTenant,
  updateTenant,
  deleteTenant,
  TenantTypeEnum,
  TenantTypeLabels,
  type TenantDTO,
  type TenantQueryDTO,
  type TenantSaveParam
} from '@/api/system/tenant'
import AvatarUpload from '@/components/AvatarUpload.vue'
import BaseFormDialog from '@/components/common/BaseFormDialog.vue'

const formRef = ref()
const tableRef = ref()

const queryForm = reactive<TenantQueryDTO>({
  tenantName: undefined,
  tenantCode: undefined,
  tenantType: undefined,
  status: undefined
})

const loading = ref(false)
const tableHeight = ref(500)

const dialogVisible = ref(false)
const saving = ref(false)

const formData = reactive<TenantSaveParam>({
  tenantName: '',
  tenantCode: '',
  tenantType: TenantTypeEnum.CUSTOMER_TENANT,
  status: true
})

const rules = {
  tenantName: [{ required: true, message: '请输入租户名称', trigger: 'blur' }],
  tenantCode: [{ required: true, message: '请输入租户编码', trigger: 'blur' }],
  tenantType: [{ required: true, message: '请选择租户类别', trigger: 'change' }]
}

// fallback配置
const fallbackConfig = ref({
  columns: [
    {
      title: '租户ID',
      dataIndex: 'id',
      key: 'id',
      width: 100
    },
    {
      title: '租户名称',
      dataIndex: 'tenantName',
      key: 'tenantName',
      width: 150
    },
    {
      title: '租户编码',
      dataIndex: 'tenantCode',
      key: 'tenantCode',
      width: 150
    },
    {
      title: '租户类别',
      dataIndex: 'tenantType',
      key: 'tenantType',
      width: 120
    },
    {
      title: 'Logo',
      dataIndex: 'logo',
      key: 'logo',
      width: 80
    },
    {
      title: '描述',
      dataIndex: 'description',
      key: 'description',
      width: 200,
      ellipsis: true
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 80
    },
    {
      title: '创建时间',
      dataIndex: 'createTime',
      key: 'createTime',
      width: 160
    },
    {
      title: '操作',
      key: 'action',
      width: 150,
      fixed: 'right'
    }
  ]
})

// 字典配置
const dictOptions = ref({
  status: {
    true: { text: '启用', color: 'green' },
    false: { text: '禁用', color: 'red' }
  },
  tenantType: {
    [TenantTypeEnum.MAIN_TENANT]: { text: TenantTypeLabels[TenantTypeEnum.MAIN_TENANT], color: 'blue' },
    [TenantTypeEnum.CUSTOMER_TENANT]: { text: TenantTypeLabels[TenantTypeEnum.CUSTOMER_TENANT], color: 'green' },
    [TenantTypeEnum.SUPPLIER_TENANT]: { text: TenantTypeLabels[TenantTypeEnum.SUPPLIER_TENANT], color: 'orange' }
  }
})

// 处理表格数据请求
const handleRequest = async (params: any) => {
  try {
    loading.value = true
    const data = await listTenant({ ...queryForm, ...params })
    return {
      success: true,
      data: data || [],
      total: data?.length || 0
    }
  } catch (e: any) {
    message.error(e.message || '加载租户列表失败')
    return {
      success: false,
      data: [],
      total: 0
    }
  } finally {
    loading.value = false
  }
}

function getTenantTypeColor(type: TenantTypeEnum): string {
  const colorMap: Record<TenantTypeEnum, string> = {
    [TenantTypeEnum.MAIN_TENANT]: 'blue',
    [TenantTypeEnum.CUSTOMER_TENANT]: 'green',
    [TenantTypeEnum.SUPPLIER_TENANT]: 'orange'
  }
  return colorMap[type] || 'default'
}

function formatLogoUrl(url: string): string {
  if (!url) return ''
  if (url.startsWith('http://') || url.startsWith('https://')) {
    return url
  }
  if (url.startsWith('/')) {
    return '/api' + url
  }
  return '/api/' + url
}

async function handleQuery() {
  await tableRef.value?.refresh?.()
}

function handleReset() {
  queryForm.tenantName = undefined
  queryForm.tenantCode = undefined
  queryForm.tenantType = undefined
  queryForm.status = undefined
  tableRef.value?.refresh?.()
}

function openAdd() {
  dialogVisible.value = true
  Object.assign(formData, {
    tenantName: '',
    tenantCode: '',
    tenantType: TenantTypeEnum.CUSTOMER_TENANT,
    description: undefined,
    logo: undefined,
    status: true
  })
  formRef.value?.resetFields()
}

function openEdit(record: TenantDTO) {
  dialogVisible.value = true
  Object.assign(formData, {
    id: record.id,
    tenantName: record.tenantName,
    tenantCode: record.tenantCode,
    tenantType: record.tenantType,
    description: record.description,
    logo: record.logo,
    status: record.status
  })
  
  formRef.value?.resetFields()
}

async function handleSave() {
  try {
    await formRef.value?.validate()
    saving.value = true

    if (formData.id) {
      await updateTenant(formData)
      message.success('更新成功')
    } else {
      await createTenant(formData)
      message.success('新增成功')
    }

    dialogVisible.value = false
    await tableRef.value?.refresh?.()
  } catch (e: any) {
    if (e.errorFields) {
      return
    }
    message.error(e.message || '保存失败')
  } finally {
    saving.value = false
  }
}

function handleCancel() {
  dialogVisible.value = false
  formRef.value?.resetFields()
}

async function handleDelete(record: TenantDTO) {
  try {
    await deleteTenant({ id: record.id })
    message.success('删除成功')
    await tableRef.value?.refresh?.()
  } catch (e: any) {
    message.error(e.message || '删除失败')
  }
}

onMounted(() => {
  tableRef.value?.refresh?.()
  tableHeight.value = window.innerHeight - 300
})
</script>

<style scoped>
.page-wrap {
  padding: 16px;
  height: calc(100vh - 84px);
  overflow: hidden;
}

.content-card {
  height: 100%;
  display: flex;
  flex-direction: column;
}

:deep(.ant-card-body) {
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

:deep(.ant-table-wrapper) {
  flex: 1;
  overflow: hidden;
}

:deep(.ant-table) {
  height: 100%;
}

.tenant-logo-upload {
  display: inline-block;
}

.tenant-logo-upload :deep(.avatar-upload) {
  align-items: flex-start;
}

.tenant-logo-upload :deep(.avatar-upload .avatar-uploader .ant-upload) {
  width: 160px;
  height: 80px;
  border-radius: 8px;
  overflow: hidden;
}

.tenant-logo-upload :deep(.avatar-upload .avatar-image) {
  width: 100%;
  height: 100%;
  object-fit: contain;
  background-color: #fafafa;
}

.tenant-logo-upload :deep(.avatar-upload .upload-tips) {
  margin-top: 8px;
  text-align: left;
}
</style>
