/**
 * 角色人员授权页面
 * 
 * 功能：
 * 1. 支持用户、部门、职位三种授权类型
 * 2. 左侧选择器（用户列表/部门树/职位树）
 * 3. 右侧已授权列表表格
 * 4. 支持批量添加、移除授权
 * 
 * @author Forgex
 * @version 1.0.0
 */
<template>
  <div class="role-grant-page">
    <!-- Hero Panel -->
    <section class="hero-panel">
      <div>
        <p class="hero-panel__eyebrow">{{ $t('system.role.userGrant') }}</p>
        <h2 class="hero-panel__title">{{ roleName }}</h2>
        <p class="hero-panel__desc">{{ $t('system.role.userGrantDesc') }}</p>
      </div>
    </section>

    <!-- Board -->
    <section class="board">
      <!-- Sidebar: 选择器 -->
      <aside class="sidebar">
        <div class="panel">
          <div class="panel__title">{{ $t('system.role.selectGrantObject') }}</div>
          <a-tabs v-model:activeKey="activeTab" tab-position="left">
            <a-tab-pane key="user" :tab="$t('system.role.selectUser')">
              <a-input-search
                v-model:value="userSearchKeyword"
                :placeholder="$t('system.role.searchUser')"
                @search="handleSearchUsers"
              />
              <div class="user-list">
                <a-checkbox-group v-model:value="selectedUserIds">
                  <div v-for="user in filteredUsers" :key="user.id" class="user-item">
                    <a-checkbox :value="user.id">
                      <div class="user-info">
                        <span class="user-name">{{ user.username }}</span>
                        <span class="user-dept">{{ user.departmentName || '-' }}</span>
                      </div>
                    </a-checkbox>
                  </div>
                </a-checkbox-group>
              </div>
            </a-tab-pane>
            <a-tab-pane key="department" :tab="$t('system.role.selectDepartment')">
              <a-tree
                checkable
                v-model:checkedKeys="selectedDepartmentIds"
                :tree-data="departmentTreeData"
                :field-names="departmentTreeFieldNames"
                :default-expand-all="true"
              />
            </a-tab-pane>
            <a-tab-pane key="position" :tab="$t('system.role.selectPosition')">
              <a-tree
                checkable
                v-model:checkedKeys="selectedPositionIds"
                :tree-data="positionTreeData"
                :field-names="positionTreeFieldNames"
                :default-expand-all="true"
              />
            </a-tab-pane>
          </a-tabs>
        </div>
        <div class="panel-actions">
          <a-button type="primary" block @click="handleAddToGranted">
            <template #icon><PlusOutlined /></template>
            {{ $t('system.role.addToGranted') }}
          </a-button>
          <a-button block @click="handleSelectAll">{{ $t('system.role.selectAll') }}</a-button>
          <a-button block @click="handleClearAll">{{ $t('system.role.clearAll') }}</a-button>
        </div>
      </aside>

      <!-- Content Panel: 已授权列表 -->
      <section class="content-panel">
        <div class="toolbar">
          <div class="toolbar__title">{{ $t('system.role.grantedList') }}</div>
          <a-space>
            <a-button danger @click="handleBatchRevoke">
              <template #icon><DeleteOutlined /></template>
              {{ $t('system.role.batchRevoke') }}
            </a-button>
          </a-space>
        </div>

        <fx-dynamic-table
          ref="tableRef"
          table-code="RoleUserGrantTable"
          :request="handleRequest"
          :row-selection="{
            selectedRowKeys,
            onChange: handleSelectionChange
          }"
          row-key="id"
        >
          <template #grantType="{ record }">
            <a-tag v-if="record.grantType === 'USER'">{{ $t('system.role.grantTypeUser') }}</a-tag>
            <a-tag v-else-if="record.grantType === 'DEPARTMENT'" color="blue">{{ $t('system.role.grantTypeDepartment') }}</a-tag>
            <a-tag v-else-if="record.grantType === 'POSITION'" color="green">{{ $t('system.role.grantTypePosition') }}</a-tag>
          </template>
          <template #action="{ record }">
            <a-button
              type="link"
              size="small"
              danger
              @click="handleRevoke(record.id)"
            >
              {{ $t('system.role.revoke') }}
            </a-button>
          </template>
        </fx-dynamic-table>
      </section>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { DeleteOutlined, PlusOutlined } from '@ant-design/icons-vue'
import { useI18n } from 'vue-i18n'
import FxDynamicTable from '@/components/common/FxDynamicTable.vue'
import { getUserList } from '@/api/system/user'
import { getDepartmentTree } from '@/api/system/department'
import { getPositionTree } from '@/api/system/position'
import { getRoleById, getGrantedUserList, grantBatch, revokeRoleUsers } from '@/api/system/role'
import type { RoleGrantVO } from './types'

const { t } = useI18n()
const route = useRoute()
const router = useRouter()

const roleId = ref<string>('')
const roleName = ref<string>('')
const tableRef = ref()
const activeTab = ref('user')
const userSearchKeyword = ref('')
const allUsers = ref<any[]>([])
const selectedUserIds = ref<number[]>([])
const selectedDepartmentIds = ref<string[]>([])
const selectedPositionIds = ref<string[]>([])
const selectedRowKeys = ref<string[]>([])
const currentTenantId = ref<string>('')
const departmentTreeData = ref<any[]>([])
const positionTreeData = ref<any[]>([])

const departmentTreeFieldNames = {
  key: 'id',
  title: 'deptName',
  children: 'children',
}

const positionTreeFieldNames = {
  key: 'id',
  title: 'positionName',
  children: 'children',
}

const fallbackConfig = computed(() => ({
  tableCode: 'RoleUserGrantTable',
  tableName: t('system.role.userGrant'),
  tableType: 'NORMAL',
  rowKey: 'id',
  defaultPageSize: 20,
  columns: [
    { field: 'grantType', title: t('system.role.grantType'), width: 100 },
    { field: 'grantObject', title: t('system.role.grantObject'), minWidth: 150 },
    { field: 'createTime', title: t('system.role.grantTime'), width: 180 },
    { field: 'createBy', title: t('system.role.grantBy'), width: 120 },
    { field: 'action', title: t('common.action'), width: 100 },
  ],
  version: 1,
}))

/**
 * 计算属性：过滤后的用户列表
 */
const filteredUsers = computed(() => {
  if (!userSearchKeyword.value) {
    return allUsers.value
  }
  const keyword = userSearchKeyword.value.toLowerCase()
  return allUsers.value.filter(user =>
    user.username?.toLowerCase().includes(keyword) ||
    user.account?.toLowerCase().includes(keyword) ||
    user.email?.toLowerCase().includes(keyword)
  )
})

/**
 * 处理表格数据请求
 */
async function handleRequest(params: any) {
  if (!currentTenantId.value || !roleId.value) {
    return { records: [], total: 0 }
  }

  try {
    const result = await getGrantedUserList({
      roleId: Number(roleId.value),
      tenantId: currentTenantId.value,
      pageNum: params.page.current,
      pageSize: params.page.pageSize,
      ...params.query,
    })

    return {
      records: result.records || [],
      total: result.total || 0,
    }
  } catch (error) {
    console.error('load granted list failed:', error)
    message.error(t('system.role.message.loadGrantedFailed'))
    return { records: [], total: 0 }
  }
}

/**
 * 处理行选择变化
 */
function handleSelectionChange(keys: Array<string | number>) {
  selectedRowKeys.value = keys.map(String)
}

/**
 * 搜索用户
 */
function handleSearchUsers() {
  // 前端过滤，无需请求后端
}

/**
 * 加载所有用户
 */
async function loadAllUsers() {
  if (!currentTenantId.value) {
    return
  }
  try {
    const result = await getUserList({
      tenantId: currentTenantId.value,
      pageNum: 1,
      pageSize: 1000,
    })
    allUsers.value = result.records || []
  } catch (error) {
    console.error('load user list failed:', error)
    message.error(t('system.user.message.loadListFailed'))
  }
}

/**
 * 加载部门树
 */
async function loadDepartmentTree() {
  if (!currentTenantId.value) {
    return
  }
  try {
    const result = await getDepartmentTree({ tenantId: currentTenantId.value })
    departmentTreeData.value = result || []
  } catch (error) {
    console.error('load department tree failed:', error)
  }
}

/**
 * 加载职位树
 */
async function loadPositionTree() {
  if (!currentTenantId.value) {
    return
  }
  try {
    const result = await getPositionTree({ tenantId: currentTenantId.value })
    positionTreeData.value = result || []
  } catch (error) {
    console.error('load position tree failed:', error)
  }
}

/**
 * 加载角色信息
 */
async function loadRoleInfo() {
  if (!roleId.value) {
    return
  }
  try {
    const role = await getRoleById(Number(roleId.value))
    roleName.value = role?.roleName || ''
  } catch (error) {
    console.error('load role info failed:', error)
  }
}

/**
 * 全选
 */
function handleSelectAll() {
  if (activeTab.value === 'user') {
    selectedUserIds.value = filteredUsers.value.map(user => user.id)
  }
}

/**
 * 清空
 */
function handleClearAll() {
  if (activeTab.value === 'user') {
    selectedUserIds.value = []
  } else if (activeTab.value === 'department') {
    selectedDepartmentIds.value = []
  } else if (activeTab.value === 'position') {
    selectedPositionIds.value = []
  }
}

/**
 * 添加到已授权
 */
async function handleAddToGranted() {
  if (!currentTenantId.value || !roleId.value) {
    message.warning(t('system.role.message.missingRoleInfo'))
    return
  }

  let userIds: number[] = []
  let departmentIds: string[] = []
  let positionIds: string[] = []

  if (activeTab.value === 'user') {
    userIds = selectedUserIds.value
  } else if (activeTab.value === 'department') {
    departmentIds = selectedDepartmentIds.value
  } else if (activeTab.value === 'position') {
    positionIds = selectedPositionIds.value
  }

  if (userIds.length === 0 && departmentIds.length === 0 && positionIds.length === 0) {
    message.warning(t('system.role.message.selectToGrant'))
    return
  }

  try {
    await grantBatch({
      roleId: Number(roleId.value),
      tenantId: currentTenantId.value,
      grantType: activeTab.value.toUpperCase(),
      userIds: userIds.length > 0 ? userIds : undefined,
      departmentIds: departmentIds.length > 0 ? departmentIds : undefined,
      positionIds: positionIds.length > 0 ? positionIds : undefined,
    })

    message.success(t('system.role.message.grantSuccess'))
    selectedUserIds.value = []
    selectedDepartmentIds.value = []
    selectedPositionIds.value = []
    await tableRef.value?.refresh?.()
  } catch (error: any) {
    console.error('grant failed:', error)
    if (!error?.code) {
      message.error(t('system.role.message.grantFailed'))
    }
  }
}

/**
 * 移除单个授权
 */
async function handleRevoke(id: number) {
  if (!currentTenantId.value || !roleId.value) {
    return
  }

  try {
    await revokeRoleUsers({
      roleId: Number(roleId.value),
      tenantId: currentTenantId.value,
      userIds: [id],
    })

    message.success(t('system.role.message.revokeSuccess'))
    await tableRef.value?.refresh?.()
  } catch (error: any) {
    console.error('revoke failed:', error)
    if (!error?.code) {
      message.error(t('system.role.message.revokeFailed'))
    }
  }
}

/**
 * 批量移除授权
 */
async function handleBatchRevoke() {
  if (selectedRowKeys.value.length === 0) {
    message.warning(t('system.role.message.selectToRevoke'))
    return
  }

  try {
    await revokeRoleUsers({
      roleId: Number(roleId.value),
      tenantId: currentTenantId.value,
      userIds: selectedRowKeys.value.map(id => Number(id)),
    })

    message.success(t('system.role.message.revokeSuccess'))
    selectedRowKeys.value = []
    await tableRef.value?.refresh?.()
  } catch (error: any) {
    console.error('batch revoke failed:', error)
    if (!error?.code) {
      message.error(t('system.role.message.revokeFailed'))
    }
  }
}

onMounted(async () => {
  const tid = sessionStorage.getItem('tenantId')
  if (tid) {
    currentTenantId.value = tid
  }

  roleId.value = String(route.params.roleId || '')

  await Promise.all([
    loadRoleInfo(),
    loadAllUsers(),
    loadDepartmentTree(),
    loadPositionTree(),
  ])
})
</script>

<style scoped lang="less">
.role-grant-page {
  display: flex;
  flex-direction: column;
  height: 100%;
  padding: 16px;
  background: var(--bg-color);

  .hero-panel {
    margin-bottom: 24px;
    padding: 24px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-radius: 8px;
    color: #fff;

    &__eyebrow {
      font-size: 14px;
      opacity: 0.9;
      margin-bottom: 8px;
    }

    &__title {
      font-size: 28px;
      font-weight: 600;
      margin: 0 0 8px 0;
    }

    &__desc {
      font-size: 14px;
      opacity: 0.8;
      margin: 0;
    }
  }

  .board {
    display: flex;
    flex: 1;
    min-height: 0;
    background: #fff;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

    .sidebar {
      width: 320px;
      border-right: 1px solid var(--border-color);
      padding: 16px;
      display: flex;
      flex-direction: column;

      .panel {
        flex: 1;
        overflow-y: auto;

        &__title {
          font-size: 16px;
          font-weight: 600;
          margin-bottom: 16px;
          color: var(--text-color);
        }
      }

      .panel-actions {
        margin-top: 16px;
        display: flex;
        flex-direction: column;
        gap: 8px;
      }

      .user-list {
        margin-top: 12px;
        max-height: 500px;
        overflow-y: auto;
      }

      .user-item {
        padding: 8px;
        border-bottom: 1px solid var(--border-color-split);
        display: flex;
        align-items: center;
        justify-content: space-between;

        &:hover {
          background: var(--hover-bg-color);
        }

        .user-info {
          display: flex;
          flex-direction: column;
          gap: 4px;

          .user-name {
            font-weight: 500;
            color: var(--text-color);
          }

          .user-dept {
            font-size: 12px;
            color: var(--text-color-secondary);
          }
        }
      }

      :deep(.ant-tabs-left) {
        .ant-tabs-nav {
          min-width: 100px;
        }

        .ant-tabs-content-holder {
          min-width: 0;
        }
      }

      :deep(.ant-tree) {
        max-height: 500px;
        overflow-y: auto;
      }
    }

    .content-panel {
      flex: 1;
      padding: 16px;
      display: flex;
      flex-direction: column;
      min-width: 0;

      .toolbar {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 16px;

        &__title {
          font-size: 16px;
          font-weight: 600;
          color: var(--text-color);
        }
      }

      :deep(.fx-dynamic-table) {
        flex: 1;
        min-height: 0;
      }
    }
  }
}
</style>
