<template>
  <div class="page-wrap">
    <template v-if="activeView === 'list'">
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

      <BaseFormDialog
        v-model:open="visible"
        :title="isEdit ? $t('system.role.form.editRole') : $t('system.role.form.addRole')"
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
          <a-form-item :label="$t('system.role.roleCode')" name="roleCode">
            <a-input
              v-model:value="formData.roleCode"
              :placeholder="$t('system.role.form.roleCode')"
              :disabled="isEdit"
            />
          </a-form-item>
          <a-form-item :label="$t('system.role.roleName')" name="roleName">
            <a-input
              v-model:value="formData.roleName"
              :placeholder="$t('system.role.form.roleName')"
            />
          </a-form-item>
          <a-form-item :label="$t('system.role.description')" name="description">
            <a-textarea
              v-model:value="formData.description"
              :placeholder="$t('system.role.form.description')"
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
    </template>

    <template v-else>
      <div class="grant-header">
        <a-space>
          <a-button @click="returnToRoleList">
            <template #icon><ArrowLeftOutlined /></template>
            {{ $t('common.back') }}
          </a-button>
          <span class="grant-header__title">
            {{ grantViewTitle }} · {{ currentRoleName || '-' }}
          </span>
        </a-space>
      </div>

      <div class="grant-content">
        <MenuGrant
          v-if="activeView === 'menuGrant'"
          :key="`menu-${currentRoleId}`"
          :role-id="currentRoleId"
          :role-name="currentRoleName"
          :tenant-id="currentTenantId"
          :embedded="true"
          @saved="handleGrantSaved"
          @back="returnToRoleList"
        />
        <UserGrant
          v-else
          :key="`user-${currentRoleId}`"
          :role-id="currentRoleId"
          :role-name="currentRoleName"
          :tenant-id="currentTenantId"
          :embedded="true"
          @back="returnToRoleList"
        />
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import BaseFormDialog from '@/components/common/BaseFormDialog.vue'
import MenuGrant from './MenuGrant.vue'
import UserGrant from './UserGrant.vue'
import { computed, onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { message } from 'ant-design-vue'
import {
  PlusOutlined,
  DeleteOutlined,
  ArrowLeftOutlined,
} from '@ant-design/icons-vue'
import { getRolePage, deleteRole, batchDeleteRoles } from '@/api/system/role'
import { useDict } from '@/hooks/useDict'
import type { Role } from './types'

const { t } = useI18n()
const { dictItems: statusOptions } = useDict('status')
const dictOptions = computed(() => ({ status: statusOptions.value }))

const selectedRowKeys = ref<string[]>([])
const tableRef = ref()
const loading = ref(false)
const currentTenantId = ref<string>('')
const activeView = ref<'list' | 'menuGrant' | 'userGrant'>('list')
const currentRoleId = ref<string>('')
const currentRoleName = ref<string>('')

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

function normalizeBoolean(value: unknown): number {
  if (value === true || value === 1 || value === '1') {
    return 1
  }
  if (value === false || value === 0 || value === '0') {
    return 0
  }
  return value ? 1 : 0
}

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
    message.error(t('system.role.message.loadListFailed'))
    return {
      success: false,
      data: [],
      total: 0
    }
  } finally {
    loading.value = false
  }
}

const onSelectChange = (keys: string[]) => {
  selectedRowKeys.value = keys
}

const handleDelete = async (id: string) => {
  try {
    await deleteRole(id)
    await tableRef.value?.refresh?.()
  } catch (error) {
    message.error(t('system.role.message.deleteFailed'))
  }
}

const handleBatchDelete = async () => {
  if (selectedRowKeys.value.length === 0) {
    message.warning(t('system.role.message.selectToDelete'))
    return
  }
  try {
    await batchDeleteRoles(selectedRowKeys.value)
    await tableRef.value?.refresh?.()
    selectedRowKeys.value = []
  } catch (error) {
    message.error(t('system.role.message.batchDeleteFailed'))
  }
}

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

const rules = computed(() => ({
  roleCode: [
    { required: true, message: t('system.role.form.roleCode'), trigger: 'blur' },
    { min: 1, max: 50, message: t('system.role.form.fieldLengthRange'), trigger: 'blur' },
  ],
  roleName: [
    { required: true, message: t('system.role.form.roleName'), trigger: 'blur' },
    { min: 1, max: 50, message: t('system.role.form.fieldLengthRange'), trigger: 'blur' },
  ],
}))

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

const openEdit = (record: Role) => {
  isEdit.value = true
  formData.value = { ...record }
  visible.value = true
}

const handleSubmit = async () => {
  await tableRef.value?.refresh?.()
  visible.value = false
}

const handleCancel = () => {
  visible.value = false
}

function returnToRoleList() {
  activeView.value = 'list'
}

const grantViewTitle = computed(() =>
  activeView.value === 'menuGrant' ? t('system.role.menuAuth') : t('system.role.userAuth'),
)

function openGrantView(view: 'menuGrant' | 'userGrant', role: Role) {
  if (!role?.id) {
    message.warning(t('system.role.message.missingRoleInfo'))
    return
  }
  currentRoleId.value = String(role.id)
  currentRoleName.value = role.roleName || ''
  visible.value = false
  activeView.value = view
}

function navigateToMenuGrant(role: Role) {
  openGrantView('menuGrant', role)
}

function navigateToUserGrant(role: Role) {
  openGrantView('userGrant', role)
}

function handleGrantSaved() {
  // keep current embedded page; user can click back manually
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

.grant-header {
  margin-bottom: 12px;
}

.grant-header__title {
  color: var(--fx-text-primary, #111827);
  font-size: 14px;
  font-weight: 600;
}

.grant-content {
  flex: 1;
  min-height: 0;
}
</style>
