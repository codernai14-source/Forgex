<template>
  <a-modal
    v-model:open="visible"
    :title="t('system.user.assignRole')"
    :confirm-loading="loading"
    width="720px"
    @ok="handleOk"
    @cancel="handleCancel"
  >
    <!-- 用户信息区域 -->
    <div class="user-info-header">
      <a-descriptions :column="2" size="small" bordered>
        <a-descriptions-item :label="t('system.user.username')">
          {{ userName || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="Account">
          {{ userAccount || '-' }}
        </a-descriptions-item>
      </a-descriptions>
    </div>

    <!-- 搜索与操作栏 -->
    <div class="role-toolbar">
      <a-input-search
        v-model:value="searchKeyword"
        :placeholder="t('system.user.roleAssign.searchPlaceholder')"
        style="width: 280px;"
        allow-clear
      />
      <a-space>
        <a-button size="small" @click="handleSelectAll">
          {{ t('system.user.roleAssign.selectAll') }}
        </a-button>
        <a-button size="small" @click="handleClearAll">
          {{ t('system.user.roleAssign.clearAll') }}
        </a-button>
      </a-space>
    </div>

    <!-- 角色表格 -->
    <a-table
      :columns="columns"
      :data-source="filteredRoleList"
      :row-selection="{
        selectedRowKeys: selectedRoleIds,
        onChange: handleSelectionChange,
      }"
      row-key="id"
      :pagination="false"
      :scroll="{ y: 360 }"
      size="small"
      :loading="loading"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'roleName'">
          <span class="role-name">{{ record.roleName }}</span>
        </template>
        <template v-else-if="column.dataIndex === 'roleCode'">
          <a-tag>{{ record.roleCode }}</a-tag>
        </template>
        <template v-else-if="column.dataIndex === 'status'">
          <a-badge
            :status="record.status ? 'success' : 'error'"
            :text="record.status ? t('common.enabled') : t('common.disabled')"
          />
        </template>
        <template v-else-if="column.dataIndex === 'assigned'">
          <a-tag v-if="initialAssignedIds.has(record.id)" color="blue">
            {{ t('system.user.roleAssign.assigned') }}
          </a-tag>
        </template>
      </template>
    </a-table>

    <!-- 底部统计信息 -->
    <div class="role-summary">
      {{ t('system.user.roleAssign.summary', { total: allRoleList.length, selected: selectedRoleIds.length }) }}
    </div>
  </a-modal>
</template>

<script setup lang="ts">
/**
 * 用户分配角色弹窗（增强版）。
 *
 * 功能说明：
 * 1. 打开弹窗时加载当前租户下的角色列表
 * 2. 加载该用户在当前租户下已分配的角色ID
 * 3. 表格形式展示所有角色，勾选框标记已分配角色
 * 4. 支持搜索、全选、清空操作
 * 5. 保存后提交到后端，后端以"先删后插"方式保存 sys_user_role
 *
 * @author Forgex
 * @version 2.0.0
 */
import { ref, computed, watch, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { message } from 'ant-design-vue'
import { userApi } from '@/api/system/user'
import { getRoleList } from '@/api/system/role'
import { useUserStore } from '@/stores/user'

interface Props {
  /** 对话框是否打开 */
  open: boolean
  /** 用户 ID */
  userId?: string
  /** 用户名 */
  userName?: string
  /** 用户账号 */
  userAccount?: string
}

const props = withDefaults(defineProps<Props>(), {
  open: false,
  userId: undefined,
  userName: undefined,
  userAccount: undefined,
})

const emit = defineEmits<{
  'update:open': [value: boolean]
  'success': []
}>()

const { t } = useI18n()
const visible = ref(props.open)
const loading = ref(false)
const searchKeyword = ref('')
const selectedRoleIds = ref<string[]>([])
const allRoleList = ref<RoleRecord[]>([])
const initialAssignedIds = ref<Set<string>>(new Set())
const userStore = useUserStore()

interface RoleRecord {
  id: string
  roleName: string
  roleCode: string
  status: boolean | number
  description?: string
}

/**
 * 表格列定义
 */
const columns = computed(() => [
  {
    title: t('system.user.roleAssign.roleName'),
    dataIndex: 'roleName',
    width: 180,
    ellipsis: true,
  },
  {
    title: t('system.user.roleAssign.roleCode'),
    dataIndex: 'roleCode',
    width: 160,
    ellipsis: true,
  },
  {
    title: t('common.status'),
    dataIndex: 'status',
    width: 100,
  },
  {
    title: t('system.user.roleAssign.assignedStatus'),
    dataIndex: 'assigned',
    width: 100,
  },
])

/**
 * 将角色 ID 转为字符串（避免 snowflake ID 转 Number 时精度丢失）
 */
function toValidRoleId(value: unknown): string | null {
  if (value == null) return null
  const str = String(value).trim()
  return str.length > 0 && str !== '0' ? str : null
}

/**
 * 规范化角色列表
 */
function normalizeRoleList(roleList: any[]): RoleRecord[] {
  const uniqueIds = new Set<string>()
  return (roleList || []).reduce((result: RoleRecord[], role: any) => {
    const roleId = toValidRoleId(role?.id)
    if (roleId === null || uniqueIds.has(roleId)) {
      return result
    }
    uniqueIds.add(roleId)
    const status = role?.status
    result.push({
      id: roleId,
      roleName: role?.roleName || '',
      roleCode: role?.roleCode || role?.roleKey || '',
      status: status === true || status === 1 || status === '1',
      description: role?.description || '',
    })
    return result
  }, [])
}

/**
 * 规范化已分配角色 ID
 */
function normalizeSelectedRoleIds(roleIds: unknown[]): string[] {
  const uniqueIds = new Set<string>()
  return (roleIds || []).reduce((result: string[], roleId: unknown) => {
    const normalizedId = toValidRoleId(roleId)
    if (normalizedId === null || uniqueIds.has(normalizedId)) {
      return result
    }
    uniqueIds.add(normalizedId)
    result.push(normalizedId)
    return result
  }, [])
}

/**
 * 过滤后的角色列表（搜索）
 */
const filteredRoleList = computed(() => {
  if (!searchKeyword.value) {
    return allRoleList.value
  }
  const keyword = searchKeyword.value.toLowerCase().trim()
  return allRoleList.value.filter(
    (role) =>
      role.roleName.toLowerCase().includes(keyword) ||
      role.roleCode.toLowerCase().includes(keyword),
  )
})

watch(
  () => props.open,
  (val) => {
    visible.value = val
    if (val) {
      searchKeyword.value = ''
      initData()
    } else {
      resetState()
    }
  },
)

watch(visible, (val) => emit('update:open', val))

async function initData() {
  if (!props.userId) return
  loading.value = true
  try {
    const tenantId = userStore.tenantId || sessionStorage.getItem('tenantId') || undefined
    const [roleList, assigned] = await Promise.all([
      getRoleList(tenantId ? { tenantId } : {}),
      userApi.getUserAssignedRoles(props.userId),
    ])
    allRoleList.value = normalizeRoleList(roleList || [])
    const assignedIds = normalizeSelectedRoleIds(assigned?.assignedRoleIds || [])
    selectedRoleIds.value = assignedIds
    initialAssignedIds.value = new Set(assignedIds)
  } catch (e) {
    console.error(e)
    message.error(t('system.user.roleAssign.loadFailed'))
  } finally {
    loading.value = false
  }
}

function resetState() {
  selectedRoleIds.value = []
  allRoleList.value = []
  initialAssignedIds.value = new Set()
  searchKeyword.value = ''
}

function handleSelectionChange(keys: string[]) {
  selectedRoleIds.value = keys
}

function handleSelectAll() {
  selectedRoleIds.value = allRoleList.value.map((r) => r.id)
}

function handleClearAll() {
  selectedRoleIds.value = []
}

/**
 * 点击"确定"保存分配结果
 */
async function handleOk() {
  if (!props.userId) {
    message.warning(t('system.user.roleAssign.missingUserId'))
    return
  }
  loading.value = true
  try {
    await userApi.saveUserRoles(props.userId, selectedRoleIds.value)
    visible.value = false
    emit('success')
  } catch (e) {
    console.error(e)
    message.error(t('system.user.roleAssign.saveFailed'))
  } finally {
    loading.value = false
  }
}

function handleCancel() {
  visible.value = false
}

onMounted(() => {
  if (visible.value) initData()
})
</script>

<style scoped lang="less">
.user-info-header {
  margin-bottom: 16px;
}

.role-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.role-name {
  font-weight: 500;
}

.role-summary {
  margin-top: 12px;
  font-size: 13px;
  color: var(--fx-text-secondary, #6b7280);
  text-align: right;
}
</style>
