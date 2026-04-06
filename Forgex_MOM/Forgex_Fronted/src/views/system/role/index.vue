<template>
  <div class="page-wrap">
    <!-- 操作按钮和表格 -->
    <fx-dynamic-table
      ref="tableRef"
      class="role-table"
      :table-code="'RoleTable'"
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
          <a-button type="primary" @click="openAdd" v-permission="'sys:role:add'">
            <template #icon><PlusOutlined /></template>
            {{ $t('common.add') }}{{ $t('system.role.roleName') }}
          </a-button>
          <a-button
            danger
            :disabled="selectedRowKeys.length === 0"
            @click="handleBatchDelete"
            v-permission="'sys:role:delete'"
          >
            <template #icon><DeleteOutlined /></template>
            {{ $t('common.batchDelete') }}
          </a-button>
        </a-space>
      </template>

      <template #action="{ record }">
        <a-space>
          <a-button
            type="link"
            size="small"
            @click="openEdit(record)"
            v-permission="'sys:role:edit'"
          >
            {{ $t('common.edit') }}
          </a-button>
          <a-button
            type="link"
            size="small"
            @click="navigateToMenuGrant(record)"
            v-permission="'sys:role:authMenu'"
          >
            {{ $t('system.role.menuAuth') }}
          </a-button>
          <a-button
            type="link"
            size="small"
            @click="navigateToUserGrant(record)"
            v-permission="'sys:role:authUser'"
          >
            {{ $t('system.role.userAuth') }}
          </a-button>
          <a-popconfirm
            :title="$t('system.role.message.deleteConfirm')"
            :ok-text="$t('common.confirm')"
            :cancel-text="$t('common.cancel')"
            @confirm="handleDelete(record.id)"
          >
            <a-button
              type="link"
              size="small"
              danger
              v-permission="'sys:role:delete'"
            >
              {{ $t('common.delete') }}
            </a-button>
          </a-popconfirm>
        </a-space>
      </template>

      <template #status="{ record }">
        <a-tag
          v-if="resolveStatusTag(record.status)"
          :color="resolveStatusTag(record.status)?.color"
          :style="resolveStatusTag(record.status)?.style"
        >
          {{ resolveStatusTag(record.status)?.label }}
        </a-tag>
        <span v-else>{{ record.status ?? '-' }}</span>
      </template>
    </fx-dynamic-table>

    <!-- 新增/编辑弹窗 -->
    <BaseFormDialog
      v-model:open="visible"
      :title="isEdit ? '编辑角色' : '新增角色'"
      :confirm-loading="formLoading"
      @ok="handleSubmit"
      @cancel="handleCancel"
    >
      <a-form
        ref="formRef"
        :model="formData"
        :rules="rules"
        :label-col="{ span: 6 }"
        :wrapper-col="{ span: 16 }"
      >
        <a-form-item label="角色编码" name="roleCode">
          <a-input
            v-model:value="formData.roleCode"
            placeholder="请输入角色编码"
            :disabled="isEdit"
          />
        </a-form-item>
        <a-form-item label="角色名称" name="roleName">
          <a-input
            v-model:value="formData.roleName"
            placeholder="请输入角色名称"
          />
        </a-form-item>
        <a-form-item label="描述" name="description">
          <a-textarea
            v-model:value="formData.description"
            placeholder="请输入描述"
            :rows="4"
          />
        </a-form-item>
        <a-form-item :label="$t('common.status')" name="status">
          <a-radio-group v-model:value="formData.status">
            <a-radio :value="true">{{ $t('common.enabled') }}</a-radio>
            <a-radio :value="false">{{ $t('common.disabled') }}</a-radio>
          </a-radio-group>
        </a-form-item>
      </a-form>
    </BaseFormDialog>
  </div>
</template>

<script setup lang="ts">
/**
 * 角色管理页面
 * 
 * 功能：
 * 1. 角色列表查询（分页、搜索）
 * 2. 新增、编辑、删除角色
 * 3. 跳转到菜单授权和人员授权页面
 * 
 * @author Forgex
 * @version 1.0.0
 */
import BaseFormDialog from '@/components/common/BaseFormDialog.vue'
import { computed, onMounted, ref } from 'vue'
import { message } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import {
  PlusOutlined,
  DeleteOutlined
} from '@ant-design/icons-vue'
import { getRolePage, deleteRole, batchDeleteRoles } from '@/api/system/role'
import { useDict } from '@/hooks/useDict'
import type { Role } from './types'

const { dictItems: statusOptions } = useDict('status')
const dictOptions = computed(() => ({ status: statusOptions.value }))

const router = useRouter()

// 选中的行
const selectedRowKeys = ref<string[]>([])

// 表格相关
const tableRef = ref()
const loading = ref(false)

// 租户 ID
const currentTenantId = ref<string>('')

/**
 * 将接口返回的 status（布尔或数字）规范为与字典 {@code status} 一致的 0/1，便于 FxDynamicTable 字典列渲染。
 */
function normalizeRoleStatusRecord(row: any) {
  const s = row?.status
  let num: number
  if (typeof s === 'boolean') {
    num = s ? 1 : 0
  } else if (s === 1 || s === '1' || s === true) {
    num = 1
  } else {
    num = 0
  }
  return { ...row, status: num }
}

/**
 * 将布尔值或数字转换为 0/1
 * @param value 布尔值或数字
 * @returns 0 或 1
 */
function normalizeBoolean(value: unknown): number {
  if (value === true || value === 1 || value === '1') {
    return 1
  }
  if (value === false || value === 0 || value === '0') {
    return 0
  }
  return value ? 1 : 0
}

/**
 * 根据状态值从字典中获取对应的标签配置
 * @param value 状态值（布尔或数字）
 * @returns 标签配置对象，包含 label、color、style，未找到返回 null
 */
function resolveStatusTag(value: unknown) {
  const normalizedValue = normalizeBoolean(value)
  const dictItem = statusOptions.value.find((item) => String(item?.value) === String(normalizedValue))
  if (!dictItem) {
    return null
  }

  const style =
    dictItem.tagStyle?.borderColor || dictItem.tagStyle?.backgroundColor
      ? {
          borderColor: dictItem.tagStyle?.borderColor,
          backgroundColor: dictItem.tagStyle?.backgroundColor,
        }
      : undefined

  return {
    label: dictItem.label,
    color: dictItem.tagStyle?.color || dictItem.color || 'blue',
    style,
  }
}

/**
 * 处理表格数据请求
 */
const handleRequest = async (params: any) => {
  loading.value = true
  try {
    const res = await getRolePage({
      ...params.query,
      ...params.page
    })
    const records = (res.records || []).map((r: any) => normalizeRoleStatusRecord(r))
    return {
      success: true,
      data: records,
      total: res.total
    }
  } catch (error) {
    message.error('获取角色列表失败')
    return {
      success: false,
      data: [],
      total: 0
    }
  } finally {
    loading.value = false
  }
}

/**
 * 行选择变化
 */
const onSelectChange = (keys: string[]) => {
  selectedRowKeys.value = keys
}

/**
 * 删除角色
 */
const handleDelete = async (id: string) => {
  try {
    await deleteRole(id)
    // 成功提示由后端返回，在 http 拦截器中统一处理
    await tableRef.value?.refresh?.()
  } catch (error) {
    message.error('删除失败')
  }
}

/**
 * 批量删除角色
 */
const handleBatchDelete = async () => {
  if (selectedRowKeys.value.length === 0) {
    message.warning('请选择要删除的角色')
    return
  }
  try {
    await batchDeleteRoles(selectedRowKeys.value)
    // 成功提示由后端返回，在 http 拦截器中统一处理
    await tableRef.value?.refresh?.()
    selectedRowKeys.value = []
  } catch (error) {
    message.error('批量删除失败')
  }
}

// 表单相关
const formRef = ref()
const visible = ref(false)
const formLoading = ref(false)
const isEdit = ref(false)
const formData = ref({
  roleCode: '',
  roleName: '',
  description: '',
  status: true
})

const rules = ref({
  roleCode: [
    { required: true, message: '请输入角色编码', trigger: 'blur' },
    { min: 1, max: 50, message: '长度在 1 到 50 个字符', trigger: 'blur' }
  ],
  roleName: [
    { required: true, message: '请输入角色名称', trigger: 'blur' },
    { min: 1, max: 50, message: '长度在 1 到 50 个字符', trigger: 'blur' }
  ]
})

/**
 * 打开新增弹窗
 */
const openAdd = () => {
  isEdit.value = false
  formData.value = {
    roleCode: '',
    roleName: '',
    description: '',
    status: true
  }
  visible.value = true
}

/**
 * 打开编辑弹窗
 */
const openEdit = (record: Role) => {
  isEdit.value = true
  formData.value = { ...record }
  visible.value = true
}

/**
 * 提交表单
 */
const handleSubmit = async () => {
  // 这里需要根据实际情况实现表单提交逻辑
  await tableRef.value?.refresh?.()
  visible.value = false
}

/**
 * 取消表单
 */
const handleCancel = () => {
  visible.value = false
}

/**
 * 跳转到菜单授权页面
 */
function navigateToMenuGrant(role: Role) {
  router.push({
    path: `/system/role/menu-grant/${role.id}`,
  })
}

/**
 * 跳转到人员授权页面
 */
function navigateToUserGrant(role: Role) {
  router.push({
    path: `/system/role/user-grant/${role.id}`,
  })
}

onMounted(async () => {
  const tid = sessionStorage.getItem('tenantId')
  if (tid) {
    currentTenantId.value = tid
  }
})
</script>

<style scoped>
.page-wrap {
  padding: 16px;
  height: 100%;
  min-height: 0;
  display: flex;
  flex-direction: column;
}

.role-table {
  flex: 1;
  min-height: 0;
}
</style>
