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
          :fallback-config="fallbackConfig"
          row-key="id"
        >
          <template #toolbar>
            <a-space>
              <a-button type="primary" @click="openAdd" v-permission="'sys:invite-code:add'">
                <template #icon><PlusOutlined /></template>
                新增邀请码
              </a-button>
            </a-space>
          </template>

          <template #status="{ record }">
            <a-tag :color="getStatusColor(record)">
              {{ getStatusText(record) }}
            </a-tag>
          </template>

          <template #action="{ record }">
            <a-space>
              <a-button type="link" size="small" @click="copyCode(record.inviteCode)">
                复制
              </a-button>
              <a-button type="link" size="small" @click="showRecords(record)">
                使用记录
              </a-button>
              <a-popconfirm
                title="确定停用该邀请码？"
                ok-text="确定"
                cancel-text="取消"
                @confirm="handleDisable(record.id)"
                v-if="record.status === true"
              >
                <a-button
                  type="link"
                  size="small"
                  danger
                  v-permission="'sys:invite-code:edit'"
                >
                  停用
                </a-button>
              </a-popconfirm>
              <a-popconfirm
                title="确定删除该邀请码？"
                ok-text="确定"
                cancel-text="取消"
                @confirm="handleDelete(record.id)"
              >
                <a-button
                  type="link"
                  size="small"
                  danger
                  v-permission="'sys:invite-code:delete'"
                >
                  删除
                </a-button>
              </a-popconfirm>
            </a-space>
          </template>
        </fx-dynamic-table>
      </div>
    </a-card>

    <!-- 新增邀请码弹窗 -->
    <BaseFormDialog
      v-model:open="addVisible"
      title="新增邀请码"
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
        <a-form-item label="归属部门" name="departmentId">
          <a-tree-select
            v-model:value="formData.departmentId"
            style="width: 100%"
            :dropdown-style="{ maxHeight: '400px', overflow: 'auto' }"
            :tree-data="treeData"
            placeholder="请选择归属部门"
            tree-default-expand-all
            :field-names="{
              children: 'children',
              label: 'deptName',
              value: 'id',
            }"
            allow-clear
          />
        </a-form-item>

        <a-form-item label="归属职位" name="positionId">
          <a-select
            v-model:value="formData.positionId"
            placeholder="请选择职位（可选）"
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

        <a-form-item label="过期时间" name="expireTime">
          <a-date-picker
            v-model:value="formData.expireTime"
            show-time
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
            placeholder="请选择过期时间"
            style="width: 100%"
          />
        </a-form-item>

        <a-form-item label="最大注册人数" name="maxRegisterCount">
          <a-input-number
            v-model:value="formData.maxRegisterCount"
            :min="1"
            placeholder="请输入最大注册人数"
            style="width: 100%"
          />
        </a-form-item>

        <a-form-item label="备注" name="remark">
          <a-textarea
            v-model:value="formData.remark"
            placeholder="请输入备注"
            :rows="3"
          />
        </a-form-item>
      </a-form>
    </BaseFormDialog>

    <!-- 创建成功展示邀请码弹窗 -->
    <a-modal
      v-model:open="codeVisible"
      title="邀请码已生成"
      :footer="null"
      @cancel="codeVisible = false"
    >
      <div style="text-align: center; padding: 24px 0;">
        <p style="font-size: 14px; color: #666;">邀请码：</p>
        <p style="font-size: 28px; font-weight: bold; letter-spacing: 4px; color: #1890ff;">
          {{ createdCode }}
        </p>
        <a-button type="primary" @click="copyCode(createdCode)">
          复制邀请码
        </a-button>
      </div>
    </a-modal>

    <!-- 使用记录弹窗 -->
    <a-modal
      v-model:open="recordVisible"
      title="邀请码使用记录"
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
import { onMounted, ref, watch } from 'vue'
import { message } from 'ant-design-vue'
import { PlusOutlined } from '@ant-design/icons-vue'
import BaseFormDialog from '@/components/common/BaseFormDialog.vue'
import { getDepartmentTree } from '@/api/system/department'
import { listPositions } from '@/api/system/position'
import {
  getInviteCodePage,
  createInviteCode,
  disableInviteCode,
  deleteInviteCode,
  getInviteRecordPage
} from '@/api/system/inviteCode'
import type { FxTableConfig } from '@/api/system/tableConfig'
import type { InviteCodeSaveParam, InviteRecord } from './types'

const currentTenantId = ref<string | null>(null)
const treeData = ref<any[]>([])
const positionList = ref<any[]>([])
const tableRef = ref()
const loading = ref(false)

const fallbackConfig: Partial<FxTableConfig> = {
  columns: [
    { field: 'inviteCode', title: '邀请码', width: 140, align: 'center' },
    { field: 'departmentName', title: '归属部门', width: 160, align: 'left' },
    { field: 'positionName', title: '归属职位', width: 140, align: 'left' },
    { field: 'expireTime', title: '过期时间', width: 180, align: 'center' },
    { field: 'maxRegisterCount', title: '最大人数', width: 100, align: 'center' },
    { field: 'usedCount', title: '已用人数', width: 100, align: 'center' },
    { field: 'remainCount', title: '剩余人数', width: 100, align: 'center' },
    { field: 'status', title: '状态', width: 110, align: 'center' },
    { field: 'createBy', title: '创建人', width: 120, align: 'center' },
    { field: 'createTime', title: '创建时间', width: 180, align: 'center' },
    { field: 'action', title: '操作', width: 220, align: 'center', fixed: 'right' }
  ],
  queryFields: [
    { field: 'inviteCode', label: '邀请码', queryType: 'input', queryOperator: 'like' },
    { field: 'status', label: '状态', queryType: 'select', queryOperator: 'eq', dictCode: 'status' }
  ]
}

// ==================== 表格数据请求 ====================
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
      ...payload.query
    }
    const data = await getInviteCodePage(params)
    const total = typeof data.total === 'number' ? data.total : parseInt(String(data.total) || '0', 10)
    return { records: data.records || [], total }
  } catch (e) {
    message.error('加载邀请码列表失败')
    return { records: [], total: 0 }
  } finally {
    loading.value = false
  }
}

// ==================== 状态展示 ====================
function getStatusColor(record: any): string {
  const label = record.statusLabel
  if (label === 'DISABLED') return 'default'
  if (label === 'EXPIRED') return 'orange'
  if (label === 'USED_UP') return 'red'
  return 'green'
}

function getStatusText(record: any): string {
  const label = record.statusLabel
  if (label === 'DISABLED') return '已停用'
  if (label === 'EXPIRED') return '已过期'
  if (label === 'USED_UP') return '已用尽'
  return '生效中'
}

// ==================== 新增邀请码 ====================
const addVisible = ref(false)
const formLoading = ref(false)
const formRef = ref()
const codeVisible = ref(false)
const createdCode = ref('')

const formData = ref<InviteCodeSaveParam>({
  departmentId: '',
  expireTime: '',
  maxRegisterCount: 10
})

const rules = {
  departmentId: [{ required: true, message: '请选择归属部门', trigger: 'change' }],
  expireTime: [{ required: true, message: '请选择过期时间', trigger: 'change' }],
  maxRegisterCount: [{ required: true, message: '请输入最大注册人数', trigger: 'blur' }]
}

function openAdd() {
  addVisible.value = true
  formData.value = {
    departmentId: '',
    expireTime: '',
    maxRegisterCount: 10
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
    const result = await createInviteCode(formData.value)
    addVisible.value = false
    // 展示生成的邀请码
    if (result && result.inviteCode) {
      createdCode.value = result.inviteCode
      codeVisible.value = true
    }
    await tableRef.value?.refresh?.()
  } catch (e: any) {
    if (e.errorFields) return
    message.error(e.message || '创建失败')
  } finally {
    formLoading.value = false
  }
}

// ==================== 停用 / 删除 ====================
async function handleDisable(id: string) {
  try {
    await disableInviteCode({ id })
    await tableRef.value?.refresh?.()
  } catch (e: any) {
    message.error(e.message || '停用失败')
  }
}

async function handleDelete(id: string) {
  try {
    await deleteInviteCode({ id })
    await tableRef.value?.refresh?.()
  } catch (e: any) {
    message.error(e.message || '删除失败')
  }
}

// ==================== 复制邀请码 ====================
function copyCode(code: string) {
  if (navigator.clipboard) {
    navigator.clipboard.writeText(code).then(() => {
      message.success('邀请码已复制到剪贴板')
    })
  } else {
    // fallback
    const input = document.createElement('input')
    input.value = code
    document.body.appendChild(input)
    input.select()
    document.execCommand('copy')
    document.body.removeChild(input)
    message.success('邀请码已复制到剪贴板')
  }
}

// ==================== 使用记录 ====================
const recordVisible = ref(false)
const recordLoading = ref(false)
const recordList = ref<InviteRecord[]>([])
const currentInviteId = ref<string>('')
const recordPagination = ref({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showTotal: (total: number) => `共 ${total} 条`
})

const recordColumns = [
  { title: '注册账号', dataIndex: 'account', width: 120 },
  { title: '用户名', dataIndex: 'username', width: 120 },
  { title: '注册IP', dataIndex: 'registerIp', width: 130 },
  { title: '注册地区', dataIndex: 'registerRegion', width: 130 },
  { title: '注册时间', dataIndex: 'registerTime', width: 180 },
]

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
      pageSize: recordPagination.value.pageSize
    })
    recordList.value = data.records || []
    recordPagination.value.total = typeof data.total === 'number' ? data.total : 0
  } catch (e) {
    message.error('加载使用记录失败')
  } finally {
    recordLoading.value = false
  }
}

function handleRecordPageChange(pagination: any) {
  recordPagination.value.current = pagination.current
  recordPagination.value.pageSize = pagination.pageSize
  loadRecords()
}

// ==================== 加载部门/职位数据 ====================
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

// 当选择部门时过滤职位
watch(() => formData.value.departmentId, (newDeptId) => {
  // 可以在这里按部门过滤职位，暂时加载全部
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
      loadPositions()
    ])
  }
})
</script>

<style scoped>
.page-wrap {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
  padding: 16px;
  box-sizing: border-box;
}

.content-card {
  display: flex;
  flex-direction: column;
  flex: 1 1 auto;
  min-height: 0;
}

.content-card :deep(.ant-card-body) {
  display: flex;
  flex-direction: column;
  flex: 1 1 auto;
  min-height: 0;
  height: 100%;
}

.table-area {
  flex: 1 1 auto;
  min-height: 0;
}
</style>

