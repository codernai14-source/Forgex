<template>
  <div class="page-wrap">
    <!-- 租户列表 -->
    <fx-dynamic-table
      ref="tableRef"
      :table-code="'TenantTable'"
      :request="handleRequest"
      :dict-options="dictOptions"
      row-key="id"
      :show-query-form="true"
    >
      <template #toolbar>
        <a-space>
          <a-button
            type="primary"
            @click="openAdd"
            v-permission="'sys:tenant:add'"
          >
            <template #icon><PlusOutlined /></template>
            {{ $t('system.tenant.form.addTenant') }}
          </a-button>
        </a-space>
      </template>
      
      <template #tenantType="{ record }">
        <a-tag :color="getTenantTypeColor(record.tenantType)">
          {{ TenantTypeLabels[record.tenantType] }}
        </a-tag>
      </template>
      
      <template #status="{ record }">
        <a-tag :color="record.status ? 'green' : 'red'">
          {{ record.status ? $t('common.enabled') : $t('common.disabled') }}
        </a-tag>
      </template>

      <template #parentTenantName="{ record }">
        <span v-if="record.parentTenantId" class="text-gray-600">
          {{ record.parentTenantName || '-' }}
        </span>
        <span v-else class="text-gray-400">
          {{ $t('system.tenant.noParentTenant') }}
        </span>
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
      :title="formData.id ? $t('system.tenant.form.editTenant') : $t('system.tenant.form.addTenant')"
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
        <a-form-item :label="$t('system.tenant.tenantName')" name="tenantName">
          <a-input
            v-model:value="formData.tenantName"
            :placeholder="$t('system.tenant.form.tenantName')"
          />
        </a-form-item>

        <a-form-item :label="$t('system.tenant.tenantCode')" name="tenantCode">
          <a-input
            v-model:value="formData.tenantCode"
            :placeholder="$t('system.tenant.form.tenantCode')"
          />
        </a-form-item>

        <a-form-item :label="$t('system.tenant.tenantType')" name="tenantType">
          <a-select
            v-model:value="formData.tenantType"
            :placeholder="$t('system.tenant.form.tenantType')"
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

        <a-form-item 
          v-if="!formData.id" 
          :label="$t('system.tenant.parentTenant')" 
          name="parentTenantId"
        >
          <a-select
            v-model:value="formData.parentTenantId"
            :placeholder="$t('system.tenant.form.parentTenant')"
            allow-clear
            show-search
            :filter-option="filterTenantOption"
          >
            <a-select-option
              v-for="tenant in tenantOptions"
              :key="tenant.id"
              :value="tenant.id"
            >
              {{ tenant.tenantName }} ({{ TenantTypeLabels[tenant.tenantType] }})
            </a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item :label="$t('system.tenant.description')" name="description">
          <a-textarea
            v-model:value="formData.description"
            :placeholder="$t('system.tenant.form.description')"
            :rows="3"
          />
        </a-form-item>

        <a-form-item label="Logo" name="logo">
          <div class="tenant-logo-upload">
            <AvatarUpload v-model="formData.logo" />
          </div>
        </a-form-item>

        <a-form-item :label="$t('common.status')" name="status">
          <a-radio-group v-model:value="formData.status">
            <a-radio :value="true">{{ $t('common.enabled') }}</a-radio>
            <a-radio :value="false">{{ $t('common.disabled') }}</a-radio>
          </a-radio-group>
        </a-form-item>
      </a-form>
    </BaseFormDialog>

    <!-- 租户初始化进度弹窗 -->
    <TenantInitProgress
      v-if="progressDialogVisible && currentTaskId"
      v-model:open="progressDialogVisible"
      :task-id="currentTaskId"
      @finish="handleInitProgressFinish"
    />
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
  getTenantPage,
  createTenant,
  updateTenant,
  deleteTenant,
  listTenantForSelect,
  TenantTypeEnum,
  TenantTypeLabels,
  type TenantDTO,
  type TenantQueryDTO,
  type TenantSaveParam
} from '@/api/system/tenant'
import { getTaskDetail, getTaskByTenantId } from '@/api/system/tenantInitTask'
import AvatarUpload from '@/components/AvatarUpload.vue'
import BaseFormDialog from '@/components/common/BaseFormDialog.vue'
import FxDynamicTable from '@/components/common/FxDynamicTable.vue'
import TenantInitProgress from '@/components/tenant/TenantInitProgress.vue'
import { useDict } from '@/hooks/useDict'

const { dictItems: statusOptions } = useDict('status')

const formRef = ref()
const tableRef = ref()

const loading = ref(false)

const dialogVisible = ref(false)
const saving = ref(false)

/**
 * 租户初始化进度弹窗显示状态
 */
const progressDialogVisible = ref(false)

/**
 * 当前租户初始化任务 ID
 */
const currentTaskId = ref<number>()

/**
 * 租户选项列表（用于父租户选择）
 */
const tenantOptions = ref<TenantDTO[]>([])

const formData = reactive<TenantSaveParam>({
  tenantName: '',
  tenantCode: '',
  tenantType: TenantTypeEnum.CUSTOMER_TENANT,
  status: true
})

const rules = {
  tenantName: [{ required: true, message: '请输入租户名称', trigger: 'blur' }],
  tenantCode: [{ required: true, message: '请输入租户编码', trigger: 'blur' }],
  tenantType: [{ required: true, message: '请选择租户类型', trigger: 'change' }]
}

/**
 * 过滤租户选项
 */
const filterTenantOption = (input: string, option: any) => {
  return option.children.toLowerCase().includes(input.toLowerCase())
}

/**
 * 加载租户选项列表
 */
const loadTenantOptions = async () => {
  try {
    const data = await listTenantForSelect()
    // 排除已删除的租户
    tenantOptions.value = data.filter(t => t.status !== false)
  } catch (e: any) {
    console.error('加载租户选项失败:', e)
  }
}

// 字典配置
const dictOptions = computed(() => ({
  status: statusOptions.value,
  tenantType: Object.entries(TenantTypeLabels).map(([value, label]) => ({ label, value })),
}))

// 处理表格数据请求
const handleRequest = async (payload: {
  page: { current: number; pageSize: number }; 
  query: Record<string, any>; 
  sorter?: { field?: string; order?: string } 
}) => {
  try {
    loading.value = true
    const params: any = {
      pageNum: payload.page.current,
      pageSize: payload.page.pageSize,
      ...payload.query
    }
    
    // 处理排序
    if (payload.sorter) {
      params.sortField = payload.sorter.field
      params.sortOrder = payload.sorter.order
    }
    
    const data = await getTenantPage(params)
    // 确保 total 是数字类型
    const total = typeof data.total === 'number' ? data.total : parseInt(String(data.total) || '0', 10)
    return { records: data.records || [], total: total }
  } catch (e: any) {
    message.error(e.message || '加载租户列表失败')
    return { records: [], total: 0 }
  } finally {
    loading.value = false
  }
}

function getTenantTypeColor(type: TenantTypeEnum): string {
  const colorMap: Record<TenantTypeEnum, string> = {
    [TenantTypeEnum.MAIN_TENANT]: 'blue',
    [TenantTypeEnum.CUSTOMER_TENANT]: 'green',
    [TenantTypeEnum.SUPPLIER_TENANT]: 'orange',
    [TenantTypeEnum.PARTNER_TENANT]: 'cyan'
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

function openAdd() {
  dialogVisible.value = true
  Object.assign(formData, {
    tenantName: '',
    tenantCode: '',
    tenantType: TenantTypeEnum.CUSTOMER_TENANT,
    description: undefined,
    logo: undefined,
    status: true,
    parentTenantId: undefined
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
    status: record.status,
    // 编辑时不支持修改父租户
    parentTenantId: undefined
  })
  
  formRef.value?.resetFields()
}

async function handleSave() {
  try {
    await formRef.value?.validate()
    saving.value = true

    if (formData.id) {
      await updateTenant(formData)
      // 成功提示由后端返回，在 http 拦截器中统一处理
    } else {
      // 新增租户
      const tenantId = await createTenant(formData)
      // 成功提示由后端返回，在 http 拦截器中统一处理

      // 查询任务详情，获取任务 ID
      try {
        // 延迟一点，等待异步任务创建
        await new Promise(resolve => setTimeout(resolve, 500))
        
        // 通过租户 ID 查询任务详情（任务表中租户 ID 是唯一的）
        // 这里需要后端提供一个通过租户 ID 查询任务的接口
        // 暂时假设后端会返回任务 ID，或者前端轮询查询
        const taskId = await queryTaskByTenantId(tenantId)
        
        if (taskId) {
          currentTaskId.value = taskId
          progressDialogVisible.value = true
        }
      } catch (e) {
        console.error('查询初始化任务失败:', e)
      }
    }

    dialogVisible.value = false
    tableRef.value?.refresh?.()
  } catch (e: any) {
    if (e.errorFields) {
      return
    }
    // 错误信息由后端返回，在 http 拦截器中统一处理
  } finally {
    saving.value = false
  }
}

/**
 * 通过租户 ID 查询任务 ID
 * 轮询查询任务表，最多尝试 10 次
 */
const queryTaskByTenantId = async (tenantId: number): Promise<number | null> => {
  const maxRetries = 10
  const retryDelay = 500 // 毫秒

  for (let i = 0; i < maxRetries; i++) {
    try {
      const task = await getTaskByTenantId(tenantId)
      
      if (task && task.id) {
        console.log('查询到任务:', task)
        return task.id
      }
      
      // 任务还未创建，等待后重试
      await new Promise(resolve => setTimeout(resolve, retryDelay))
      console.log('等待任务创建...', i + 1)
    } catch (e) {
      console.error('查询任务失败:', e)
      // 失败也等待后重试
      await new Promise(resolve => setTimeout(resolve, retryDelay))
    }
  }
  
  return null
}

/**
 * 初始化进度完成回调
 */
const handleInitProgressFinish = () => {
  progressDialogVisible.value = false
  currentTaskId.value = undefined
  // 成功信息由后端返回，在 http 拦截器中统一处理
  tableRef.value?.refresh?.()
}

async function handleDelete(record: TenantDTO) {
  await deleteTenant({ id: record.id })
  await tableRef.value?.refresh?.()
}

onMounted(() => {
  tableRef.value?.refresh?.()
  loadTenantOptions()
})
</script>

<style scoped>
.page-wrap {
  padding: 16px;
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
  overflow: hidden;
  box-sizing: border-box;
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
