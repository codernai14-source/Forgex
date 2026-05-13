<!--
  - Copyright 2026 coder_nai@163.com
  -
  - Licensed under the Apache License, Version 2.0 (the "License");
  - you may not use this file except in compliance with the License.
  - You may obtain a copy of the License at
  -
  - http://www.apache.org/licenses/LICENSE-2.0
  -
  - Unless required by applicable law or agreed to in writing, software
  - distributed under the License is distributed on an "AS IS" BASIS,
  - WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  - See the License for the specific language governing permissions and
  - limitations under the License.
  -->

<template>
  <div class="page-wrap">
    <a-card class="content-card" :bordered="false">
      <div class="table-area">
        <fx-dynamic-table
          ref="tableRef"
          :table-code="'InviteCodeTable'"
          :request="handleRequest"
          :dict-options="dictOptions"
          row-key="id"
        >
          <template #toolbar>
            <a-space>
              <a-button data-guide-id="sys-invite-add" type="primary" @click="openAdd" v-permission="'sys:invite-code:add'">
                <template #icon><PlusOutlined /></template>
                {{ t('system.inviteCode.add') }}
              </a-button>
            </a-space>
          </template>

          <template #status="{ record }">
            <a-tag :color="getStatusColor(record)">
              {{ getStatusText(record) }}
            </a-tag>
          </template>

          <template #action="{ record }">
            <a-space wrap>
              <a-button type="link" size="small" @click="copyCode(record.inviteCode)">
                {{ t('common.copy') }}
              </a-button>
              <a-button type="link" size="small" @click="showRecords(record)">
                {{ t('common.usageRecord') }}
              </a-button>
              <a-popconfirm
                :title="t('system.inviteCode.confirmDisable')"
                :ok-text="t('common.confirm')"
                :cancel-text="t('common.cancel')"
                @confirm="handleDisable(record.id)"
                :disabled="record.status !== true"
              >
                <a-button
                  type="link"
                  size="small"
                  danger
                  v-permission="'sys:invite-code:edit'"
                  :disabled="record.status !== true"
                >
                  {{ t('common.disable') }}
                </a-button>
              </a-popconfirm>
              <a-popconfirm
                :title="t('system.inviteCode.confirmDelete')"
                :ok-text="t('common.confirm')"
                :cancel-text="t('common.cancel')"
                @confirm="handleDelete(record.id)"
              >
                <a-button
                  type="link"
                  size="small"
                  danger
                  v-permission="'sys:invite-code:delete'"
                >
                  {{ t('common.delete') }}
                </a-button>
              </a-popconfirm>
            </a-space>
          </template>
        </fx-dynamic-table>
      </div>
    </a-card>

    <BaseFormDialog
      v-model:open="addVisible"
      :title="t('system.inviteCode.add')"
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
        <a-form-item :label="t('system.inviteCode.department')" name="departmentId">
          <a-tree-select
            v-model:value="formData.departmentId"
            style="width: 100%"
            :dropdown-style="{ maxHeight: '400px', overflow: 'auto' }"
            :tree-data="treeData"
            :placeholder="t('system.inviteCode.form.department')"
            tree-default-expand-all
            :field-names="{
              children: 'children',
              label: 'deptName',
              value: 'id',
            }"
            allow-clear
          />
        </a-form-item>

        <a-form-item :label="t('system.inviteCode.position')" name="positionId">
          <a-select
            v-model:value="formData.positionId"
            :placeholder="t('system.inviteCode.form.position')"
            allow-clear
            style="width: 100%"
          >
            <a-select-option
              v-for="pos in positionList"
              :key="pos.id"
              :value="pos.id"
            >
              {{ pos.positionName }}
            </a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item :label="t('system.inviteCode.role')" name="roleId">
          <a-select
            v-model:value="formData.roleId"
            :placeholder="t('system.inviteCode.form.role')"
            allow-clear
            show-search
            option-label-prop="label"
            :filter-option="filterRoleOption"
            style="width: 100%"
          >
            <a-select-option
              v-for="role in roleList"
              :key="role.id"
              :value="role.id"
              :label="role.roleName"
            >
              {{ role.roleName }}
            </a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item :label="t('system.inviteCode.expireTime')" name="expireTime">
          <a-date-picker
            v-model:value="formData.expireTime"
            show-time
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
            :placeholder="t('system.inviteCode.form.expireTime')"
            style="width: 100%"
          />
        </a-form-item>

        <a-form-item :label="t('system.inviteCode.maxRegisterCount')" name="maxRegisterCount">
          <a-input-number
            v-model:value="formData.maxRegisterCount"
            :min="1"
            :placeholder="t('system.inviteCode.form.maxRegisterCount')"
            style="width: 100%"
          />
        </a-form-item>

        <a-form-item :label="t('common.remark')" name="remark">
          <a-textarea
            v-model:value="formData.remark"
            :placeholder="t('common.pleaseInput')"
            :rows="3"
          />
        </a-form-item>
      </a-form>
    </BaseFormDialog>

    <a-modal
      v-model:open="codeVisible"
      :title="t('system.inviteCode.generatedTitle')"
      :footer="null"
      @cancel="codeVisible = false"
    >
      <div style="text-align: center; padding: 24px 0;">
        <p style="font-size: 14px; color: #666;">{{ t('system.inviteCode.inviteCode') }}</p>
        <p style="font-size: 28px; font-weight: bold; letter-spacing: 4px; color: #1890ff;">
          {{ createdCode }}
        </p>
        <a-button type="primary" @click="copyCode(createdCode)">
          {{ t('system.inviteCode.copyInviteCode') }}
        </a-button>
      </div>
    </a-modal>

    <a-modal
      v-model:open="recordVisible"
      :title="t('system.inviteCode.recordTitle')"
      width="800px"
      :footer="null"
      @cancel="recordVisible = false"
    >
      <a-table
        :columns="recordColumns"
        :data-source="recordList"
        :loading="recordLoading"
        :pagination="recordPagination"
        @change="handleRecordPageChange"
        row-key="id"
        size="small"
      />
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { message } from 'ant-design-vue'
import { PlusOutlined } from '@ant-design/icons-vue'
import BaseFormDialog from '@/components/common/BaseFormDialog.vue'
import { getDepartmentTree } from '@/api/system/department'
import { listPositions } from '@/api/system/position'
import { getRoleList } from '@/api/system/role'
import {
  getInviteCodePage,
  createInviteCode,
  disableInviteCode,
  deleteInviteCode,
  getInviteRecordPage,
} from '@/api/system/inviteCode'

import type { InviteCodeSaveParam, InviteRecord } from './types'

const currentTenantId = ref<string | null>(null)
const { t } = useI18n()
const treeData = ref<any[]>([])
const positionList = ref<any[]>([])
const roleList = ref<any[]>([])
const tableRef = ref()
const loading = ref(false)
const dictOptions = reactive<Record<string, any[]>>({
  status: [
    { label: t('system.inviteCode.status.active'), value: true },
    { label: t('system.inviteCode.status.disabled'), value: false },
  ],
  role: [],
})


const handleRequest = async (payload: {
  page: { current: number; pageSize: number }
  query: Record<string, any>
  sorter?: { field?: string; order?: string }
}) => {
  if (!currentTenantId.value) {
    return { records: [], total: 0 }
  }
  loading.value = true
  try {
    const params: any = {
      pageNum: payload.page.current,
      pageSize: payload.page.pageSize,
      tenantId: currentTenantId.value,
      ...payload.query,
    }
    const data = await getInviteCodePage(params)
    const total = typeof data.total === 'number' ? data.total : parseInt(String(data.total) || '0', 10)
    return { records: data.records || [], total }
  } catch (e) {
    message.error(t('system.inviteCode.loadListFailed'))
    return { records: [], total: 0 }
  } finally {
    loading.value = false
  }
}

function getStatusColor(record: any): string {
  const label = record.statusLabel
  if (label === 'DISABLED') return 'default'
  if (label === 'EXPIRED') return 'orange'
  if (label === 'USED_UP') return 'red'
  return 'green'
}

function getStatusText(record: any): string {
  const label = record.statusLabel
  if (label === 'DISABLED') return t('system.inviteCode.status.disabled')
  if (label === 'EXPIRED') return t('system.inviteCode.status.expired')
  if (label === 'USED_UP') return t('system.inviteCode.status.usedUp')
  return t('system.inviteCode.status.active')
}

const addVisible = ref(false)
const formLoading = ref(false)
const formRef = ref()
const codeVisible = ref(false)
const createdCode = ref('')

const formData = ref<InviteCodeSaveParam>({
  departmentId: '',
  roleId: '',
  expireTime: '',
  maxRegisterCount: 10,
})

const rules = computed(() => ({
  departmentId: [{ required: true, message: t('system.inviteCode.form.department'), trigger: 'change' }],
  roleId: [{ required: true, message: t('system.inviteCode.form.role'), trigger: 'change' }],
  expireTime: [{ required: true, message: t('system.inviteCode.form.expireTime'), trigger: 'change' }],
  maxRegisterCount: [{ required: true, message: t('system.inviteCode.form.maxRegisterCount'), trigger: 'blur' }],
}))

function openAdd() {
  addVisible.value = true
  formData.value = {
    departmentId: '',
    roleId: '',
    expireTime: '',
    maxRegisterCount: 10,
  }
}

function handleCancel() {
  addVisible.value = false
  formRef.value?.resetFields()
}

async function handleSubmit() {
  try {
    await formRef.value?.validate()
    formLoading.value = true
    const result = await createInviteCode({
      ...formData.value,
      tenantId: currentTenantId.value || undefined,
    })
    addVisible.value = false
    if (result && result.inviteCode) {
      createdCode.value = result.inviteCode
      codeVisible.value = true
    }
    await tableRef.value?.refresh?.()
  } catch (e: any) {
    if (e.errorFields) return
    message.error(e.message || t('common.saveFailed'))
  } finally {
    formLoading.value = false
  }
}

async function handleDisable(id: string) {
  try {
    await disableInviteCode({ id })
    await tableRef.value?.refresh?.()
  } catch (e: any) {
    message.error(e.message || t('system.inviteCode.disableFailed'))
  }
}

async function handleDelete(id: string) {
  try {
    await deleteInviteCode({ id })
    await tableRef.value?.refresh?.()
  } catch (e: any) {
    message.error(e.message || t('system.inviteCode.deleteFailed'))
  }
}

function copyCode(code: string) {
  if (navigator.clipboard) {
    navigator.clipboard.writeText(code).then(() => {
      message.success(t('system.inviteCode.copySuccess'))
    })
  } else {
    const input = document.createElement('input')
    input.value = code
    document.body.appendChild(input)
    input.select()
    document.execCommand('copy')
    document.body.removeChild(input)
    message.success(t('system.inviteCode.copySuccess'))
  }
}

const recordVisible = ref(false)
const recordLoading = ref(false)
const recordList = ref<InviteRecord[]>([])
const currentInviteId = ref<string>('')
const recordPagination = ref({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showTotal: (total: number) => t('common.total', { total }),
})

const recordColumns = computed(() => [
  { title: t('system.inviteCode.record.account'), dataIndex: 'account', width: 120 },
  { title: t('system.user.username'), dataIndex: 'username', width: 120 },
  { title: t('system.inviteCode.role'), dataIndex: 'roleName', width: 160 },
  { title: t('system.inviteCode.record.registerIp'), dataIndex: 'registerIp', width: 130 },
  { title: t('system.inviteCode.record.registerRegion'), dataIndex: 'registerRegion', width: 130 },
  { title: t('system.inviteCode.record.registerTime'), dataIndex: 'registerTime', width: 180 },
])

async function showRecords(record: any) {
  currentInviteId.value = record.id
  recordPagination.value.current = 1
  recordVisible.value = true
  await loadRecords()
}

async function loadRecords() {
  recordLoading.value = true
  try {
    const data = await getInviteRecordPage({
      inviteId: currentInviteId.value,
      pageNum: recordPagination.value.current,
      pageSize: recordPagination.value.pageSize,
    })
    recordList.value = data.records || []
    recordPagination.value.total = typeof data.total === 'number' ? data.total : 0
  } catch (e) {
    message.error(t('system.inviteCode.loadRecordsFailed'))
  } finally {
    recordLoading.value = false
  }
}

function handleRecordPageChange(pagination: any) {
  recordPagination.value.current = pagination.current
  recordPagination.value.pageSize = pagination.pageSize
  loadRecords()
}

async function loadDeptTree() {
  if (!currentTenantId.value) return
  try {
    const data = await getDepartmentTree({ tenantId: currentTenantId.value })
    treeData.value = data || []
  } catch (e) {
    console.error(e)
  }
}

async function loadPositions() {
  if (!currentTenantId.value) return
  try {
    const data = await listPositions({ tenantId: currentTenantId.value })
    positionList.value = Array.isArray(data) ? data : []
  } catch (e) {
    console.error(e)
  }
}

async function loadRoles() {
  if (!currentTenantId.value) return
  try {
    const data = await getRoleList({ tenantId: currentTenantId.value, status: true })
    roleList.value = (Array.isArray(data) ? data : []).map((role: any) => ({
      ...role,
      id: String(role.id),
    }))
    dictOptions.role = roleList.value.map((role: any) => ({
      label: role.roleName,
      value: role.id,
    }))
  } catch (e) {
    console.error(e)
  }
}

function filterRoleOption(input: string, option: any) {
  const label = String(option?.label || '')
  return label.toLowerCase().includes(input.toLowerCase())
}

watch(() => formData.value.departmentId, (newDeptId) => {
  if (newDeptId) {
    loadPositions()
  }
})

onMounted(async () => {
  const tid = sessionStorage.getItem('tenantId')
  if (tid) {
    currentTenantId.value = tid
    await Promise.all([
      tableRef.value?.refresh?.(),
      loadDeptTree(),
      loadPositions(),
      loadRoles(),
    ])
  }
})
</script>

<style scoped lang="less" src="@/styles/views/system/inviteCode/index.less"></style>
