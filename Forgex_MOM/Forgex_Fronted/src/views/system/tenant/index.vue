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
            data-guide-id="sys-tenant-add"
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
          {{ getTenantTypeLabel(record.tenantType) }}
        </a-tag>
      </template>
      
      <template #status="{ record }">
        <a-tag :color="record.status ? 'green' : 'red'">
          {{ record.status ? $t('common.enabled') : $t('common.disabled') }}
        </a-tag>
      </template>

      <template #parentTenantName="{ record }">
        <span v-if="record.parentTenantId" class="tenant-parent-name">
          {{ record.parentTenantName || '-' }}
        </span>
        <span v-else class="tenant-parent-empty">
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
            {{ $t('common.edit') }}
          </a-button>
          <a-popconfirm
            :title="$t('system.tenant.message.deleteConfirm')"
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
              {{ $t('common.delete') }}
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
      :width="'min(720px, 100vw)'"
      :body-style="{ padding: '20px 24px 8px' }"
      wrap-class-name="tenant-form-dialog"
      @submit="handleSave"
      @cancel="handleCancel"
    >
      <a-form
        ref="formRef"
        :model="formData"
        :rules="rules"
        layout="vertical"
        class="tenant-form"
      >
        <section class="form-section">
          <div class="form-section__title">
            <BankOutlined />
            <span>{{ $t('system.tenant.detail') }}</span>
          </div>
          <div class="form-grid form-grid--two">
            <a-form-item :label="$t('system.tenant.tenantName')" name="tenantName">
              <a-input v-model:value="formData.tenantName" :placeholder="$t('system.tenant.form.tenantName')" />
            </a-form-item>
            <a-form-item :label="$t('system.tenant.tenantCode')" name="tenantCode">
              <a-input v-model:value="formData.tenantCode" :placeholder="$t('system.tenant.form.tenantCode')" />
            </a-form-item>
          </div>
          <a-form-item :label="$t('system.tenant.description')" name="description">
            <a-textarea v-model:value="formData.description" :placeholder="$t('system.tenant.form.description')" :rows="3" />
          </a-form-item>
        </section>

        <section class="form-section">
          <div class="form-section__title">
            <ApartmentOutlined />
            <span>{{ $t('system.tenant.parentTenant') }}</span>
          </div>
          <div class="form-grid form-grid--two">
            <a-form-item :label="$t('system.tenant.tenantType')" name="tenantType">
              <a-select
                v-model:value="formData.tenantType"
                :placeholder="$t('system.tenant.form.tenantType')"
                :disabled="Boolean(formData.id)"
              >
                <a-select-option v-for="option in tenantTypeOptions" :key="option.value" :value="option.value">
                  {{ option.label }}
                </a-select-option>
              </a-select>
            </a-form-item>
            <a-form-item
              v-if="formData.tenantType !== TenantTypeEnum.MAIN_TENANT"
              :label="$t('system.tenant.parentTenant')"
              name="parentTenantId"
            >
              <a-select
                v-model:value="formData.parentTenantId"
                :placeholder="$t('system.tenant.form.parentTenant')"
                show-search
                :disabled="Boolean(formData.id)"
                :filter-option="filterTenantOption"
              >
                <a-select-option v-for="tenant in tenantOptions" :key="tenant.id" :value="tenant.id">
                  {{ tenant.tenantName }} ({{ tenant.tenantCode }})
                </a-select-option>
              </a-select>
            </a-form-item>
          </div>
        </section>

        <section class="form-section form-section--last">
          <div class="form-section__title">
            <SettingOutlined />
            <span>{{ $t('common.status') }}</span>
          </div>
          <div class="form-grid form-grid--brand">
            <a-form-item label="Logo" name="logo">
              <div class="tenant-logo-upload">
                <AvatarUpload v-model="formData.logo" module-code="sys_tenant_logo" :module-name="$t('system.tenant.logoModuleName')" />
              </div>
            </a-form-item>
            <a-form-item :label="$t('common.status')" name="status" class="status-field">
              <a-radio-group v-model:value="formData.status" button-style="solid">
                <a-radio-button :value="true">{{ $t('common.enabled') }}</a-radio-button>
                <a-radio-button :value="false">{{ $t('common.disabled') }}</a-radio-button>
              </a-radio-group>
            </a-form-item>
          </div>
        </section>
      </a-form>
    </BaseFormDialog>

  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { message } from 'ant-design-vue'
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  BankOutlined,
  ApartmentOutlined,
  SettingOutlined
} from '@ant-design/icons-vue'
import {
  getTenantPage,
  createTenant,
  updateTenant,
  deleteTenant,
  listTenantForSelect,
  TenantTypeEnum,
  type TenantDTO,
  type TenantSaveParam
} from '@/api/system/tenant'
import AvatarUpload from '@/components/AvatarUpload.vue'
import BaseFormDialog from '@/components/common/BaseFormDialog.vue'
import FxDynamicTable from '@/components/common/FxDynamicTable.vue'
import { useDict } from '@/hooks/useDict'
import { normalizeMediaUrl } from '@/utils/media'

const { dictItems: statusOptions } = useDict('status')
const { t } = useI18n()

const formRef = ref()
const tableRef = ref()

const loading = ref(false)

const dialogVisible = ref(false)
const saving = ref(false)

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

const rules = computed(() => ({
  tenantName: [{ required: true, message: t('system.tenant.form.tenantName'), trigger: 'blur' }],
  tenantCode: [{ required: true, message: t('system.tenant.form.tenantCode'), trigger: 'blur' }],
  tenantType: [{ required: true, message: t('system.tenant.form.tenantType'), trigger: 'change' }],
  parentTenantId: formData.tenantType === TenantTypeEnum.MAIN_TENANT
    ? []
    : [{ required: true, message: t('system.tenant.form.parentTenant'), trigger: 'change' }]
}))

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
    const data = await listTenantForSelect({ tenantType: TenantTypeEnum.MAIN_TENANT })
    tenantOptions.value = data.filter(t => t.status !== false)
    if (!formData.id && formData.tenantType !== TenantTypeEnum.MAIN_TENANT && !formData.parentTenantId) {
      formData.parentTenantId = tenantOptions.value[0]?.id
    }
  } catch (e: any) {
    console.error('加载租户选项失败:', e)
  }
}

// 字典配置
const dictOptions = computed(() => ({
  status: statusOptions.value,
  tenantType: tenantTypeOptions.value,
}))

const tenantTypeOptions = computed(() => [
  { value: TenantTypeEnum.MAIN_TENANT, label: t('system.tenant.type.main') },
  { value: TenantTypeEnum.CUSTOMER_TENANT, label: t('system.tenant.type.customer') },
  { value: TenantTypeEnum.SUPPLIER_TENANT, label: t('system.tenant.type.supplier') },
  { value: TenantTypeEnum.PARTNER_TENANT, label: t('system.tenant.type.partner') },
])

function getTenantTypeLabel(type: TenantTypeEnum) {
  return tenantTypeOptions.value.find(item => item.value === type)?.label || type
}

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
    message.error(e.message || t('system.tenant.message.loadListFailed'))
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
  return normalizeMediaUrl(url)
}

function openAdd() {
  dialogVisible.value = true
  Object.assign(formData, {
    id: undefined,
    tenantName: '',
    tenantCode: '',
    tenantType: TenantTypeEnum.CUSTOMER_TENANT,
    description: undefined,
    logo: undefined,
    status: true,
    parentTenantId: tenantOptions.value[0]?.id
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
    parentTenantId: record.parentTenantId
  })
  
  formRef.value?.resetFields()
}

function handleCancel() {
  dialogVisible.value = false
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
      await createTenant(formData)
      // 成功提示由后端返回，在 http 拦截器中统一处理
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

async function handleDelete(record: TenantDTO) {
  await deleteTenant({ id: record.id })
  await tableRef.value?.refresh?.()
}

onMounted(() => {
  tableRef.value?.refresh?.()
  loadTenantOptions()
})

watch(() => formData.tenantType, tenantType => {
  if (tenantType === TenantTypeEnum.MAIN_TENANT) {
    formData.parentTenantId = undefined
    formRef.value?.clearValidate?.(['parentTenantId'])
  } else if (!formData.parentTenantId && tenantOptions.value.length > 0) {
    formData.parentTenantId = tenantOptions.value[0].id
  }
})
</script>

<style scoped lang="less" src="@/styles/views/system/tenant/index.less"></style>
