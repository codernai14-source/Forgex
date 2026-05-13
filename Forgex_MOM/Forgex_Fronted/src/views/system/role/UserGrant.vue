/**
 * 角色人员授权页面
 * 
 * 閸旂喕鍏橀敍?
 * 1. 閺€顖涘瘮閻劍鍩涢妴渚€鍎撮梻銊ｂ偓浣戒捍娴ｅ秳绗佺粔宥嗗房閺夊啰琚崹?
 * 2. 瀹革缚鏅堕柅澶嬪閸ｎ煉绱欓悽銊﹀煕閸掓銆?闁劑妫弽?閼卞奔缍呴弽鎴礆
 * 3. 閸欏厖鏅跺鍙夊房閺夊啫鍨悰銊ㄣ€冮弽?
 * 4. 閺€顖涘瘮閹靛綊鍣哄ǎ璇插閵嗕胶些闂勩倖宸块弶?
 * 
 * @author Forgex
 * @version 1.0.0
 */
<template>
  <div class="role-grant-page">
    <!-- 头部面板 -->
    <section class="hero-panel">
      <div>
        <p class="hero-panel__eyebrow">{{ $t('system.role.userGrant') }}</p>
        <h2 class="hero-panel__title">{{ roleName }}</h2>
        <p class="hero-panel__desc">{{ $t('system.role.userGrantDesc') }}</p>
      </div>
    </section>

    <!-- 主体区域 -->
    <section class="board">
      <!-- 侧边栏： 闁瀚ㄩ崳?-->
      <aside class="sidebar" data-guide-id="sys-role-user-grant-object-panel">
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
          <a-button data-guide-id="sys-role-user-grant-add" type="primary" block @click="handleAddToGranted">
            <template #icon><PlusOutlined /></template>
            {{ $t('system.role.addToGranted') }}
          </a-button>
          <a-button data-guide-id="sys-role-user-grant-select-all" block @click="handleSelectAll">{{ $t('system.role.selectAll') }}</a-button>
          <a-button data-guide-id="sys-role-user-grant-clear" block @click="handleClearAll">{{ $t('system.role.clearAll') }}</a-button>
        </div>
      </aside>

      <!-- 内容区域： 瀹稿弶宸块弶鍐ㄥ灙鐞?-->
      <section class="content-panel">
        <div class="toolbar">
          <div class="toolbar__title">{{ $t('system.role.grantedList') }}</div>
          <a-space>
            <a-button data-guide-id="sys-role-user-grant-batch-revoke" danger @click="handleBatchRevoke">
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
              data-guide-id="sys-role-user-grant-row-revoke"
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
import { useRoute } from 'vue-router'
import { message } from 'ant-design-vue'
import { DeleteOutlined, PlusOutlined } from '@ant-design/icons-vue'
import { useI18n } from 'vue-i18n'
import FxDynamicTable from '@/components/common/FxDynamicTable.vue'
import { getUserList } from '@/api/system/user'
import { getDepartmentTree } from '@/api/system/department'
import { getPositionTree } from '@/api/system/position'
import { getRoleById, getGrantedUserList, grantBatch, revokeRoleUsers } from '@/api/system/role'
import { useUserStore } from '@/stores/user'
import type { RoleGrantVO } from './types'

interface RoleUserGrantProps {
  roleId?: string | number
  roleName?: string
  tenantId?: string
  embedded?: boolean
}

const props = defineProps<RoleUserGrantProps>()
defineEmits<{
  (e: 'back'): void
}>()

const { t } = useI18n()
const route = useRoute()
const userStore = useUserStore()

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


/**
 * 鐠侊紕鐣荤仦鐐粹偓褝绱版潻鍥ㄦ姢閸氬海娈戦悽銊﹀煕閸掓銆?
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
      roleId: roleId.value,
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
 * 婢跺嫮鎮婄悰宀勨偓澶嬪閸欐ê瀵?
 */
function handleSelectionChange(keys: Array<string | number>) {
  selectedRowKeys.value = keys.map(String)
}

/**
 * 閹兼粎鍌ㄩ悽銊﹀煕
 */
function handleSearchUsers() {
  // 閸撳秶顏潻鍥ㄦ姢閿涘本妫ら棁鈧拠閿嬬湴閸氬海顏?
}

/**
 * 閸旂姾娴囬幍鈧張澶屾暏閹?
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
 * 閸旂姾娴囬柈銊╂，閺?
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
 * 閸旂姾娴囬懕灞肩秴閺?
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
 * 閸旂姾娴囩憴鎺曞娣団剝浼?
 */
async function loadRoleInfo() {
  if (props.roleName) {
    roleName.value = props.roleName
    return
  }
  if (!roleId.value) {
    return
  }
  try {
    const role = await getRoleById(roleId.value)
    roleName.value = role?.roleName || ''
  } catch (error) {
    console.error('load role info failed:', error)
  }
}

/**
 * 閸忋劑鈧?
 */
function handleSelectAll() {
  if (activeTab.value === 'user') {
    selectedUserIds.value = filteredUsers.value.map(user => user.id)
  }
}

/**
 * 濞撳懐鈹?
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
 * 濞ｈ濮為崚鏉垮嚒閹哄牊娼?
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
      roleId: roleId.value,
      tenantId: currentTenantId.value,
      grantType: activeTab.value.toUpperCase(),
      userIds: userIds.length > 0 ? userIds : undefined,
      departmentIds: departmentIds.length > 0 ? departmentIds : undefined,
      positionIds: positionIds.length > 0 ? positionIds : undefined,
    })

    selectedUserIds.value = []
    selectedDepartmentIds.value = []
    selectedPositionIds.value = []
    await tableRef.value?.refresh?.()
  } catch (error: any) {
    console.error('grant failed:', error)
  }
}

/**
 * 缁夊娅庨崡鏇氶嚋閹哄牊娼?
 */
async function handleRevoke(id: number) {
  if (!currentTenantId.value || !roleId.value) {
    return
  }

  try {
    await revokeRoleUsers({
      roleId: roleId.value,
      tenantId: currentTenantId.value,
      userIds: [id],
    })

    await tableRef.value?.refresh?.()
  } catch (error: any) {
    console.error('revoke failed:', error)
  }
}

/**
 * 閹靛綊鍣虹粔濠氭珟閹哄牊娼?
 */
async function handleBatchRevoke() {
  if (selectedRowKeys.value.length === 0) {
    message.warning(t('system.role.message.selectToRevoke'))
    return
  }

  try {
    await revokeRoleUsers({
      roleId: roleId.value,
      tenantId: currentTenantId.value,
      userIds: selectedRowKeys.value.map(id => Number(id)),
    })

    selectedRowKeys.value = []
    await tableRef.value?.refresh?.()
  } catch (error: any) {
    console.error('batch revoke failed:', error)
  }
}

onMounted(async () => {
  const tid = props.tenantId || userStore.tenantId || sessionStorage.getItem('tenantId')
  if (tid) {
    currentTenantId.value = tid
  }

  roleId.value = String(props.roleId ?? route.params.roleId ?? '')
  if (props.roleName) {
    roleName.value = props.roleName
  }

  await Promise.all([
    loadRoleInfo(),
    loadAllUsers(),
    loadDepartmentTree(),
    loadPositionTree(),
  ])
})
</script>

<style scoped lang="less" src="@/styles/views/system/role/user-grant.less"></style>
