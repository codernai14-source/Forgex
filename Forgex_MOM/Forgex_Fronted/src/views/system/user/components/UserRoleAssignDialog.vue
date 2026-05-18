<template>
  <a-modal
    v-model:open="visible"
    :title="t('system.user.assignRole')"
    :confirm-loading="loading"
    width="720px"
    @ok="handleOk"
    @cancel="handleCancel"
  >
    <!-- 鐢ㄦ埛淇℃伅鍖哄煙 -->
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

    <!-- 鎼滅储涓庢搷浣滄爮 -->
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

    <!-- 瑙掕壊琛ㄦ牸 -->
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

    <!-- 搴曢儴缁熻淇℃伅 -->
    <div class="role-summary">
      {{ t('system.user.roleAssign.summary', { total: allRoleList.length, selected: selectedRoleIds.length }) }}
    </div>
  </a-modal>
</template>

<script setup lang="ts">
/**
 * 鐢ㄦ埛鍒嗛厤瑙掕壊寮圭獥锛堝寮虹増锛?
 *
 * 鍔熻兘璇存槑锛?
 * 1. 鎵撳紑寮圭獥鏃跺姞杞藉綋鍓嶇鎴蜂笅鐨勮鑹插垪琛?
 * 2. 鍔犺浇璇ョ敤鎴峰湪褰撳墠绉熸埛涓嬪凡鍒嗛厤鐨勮鑹睮D
 * 3. 琛ㄦ牸褰㈠紡灞曠ず鎵€鏈夎鑹诧紝鍕鹃€夋鏍囪宸插垎閰嶈鑹?
 * 4. 鏀寔鎼滅储銆佸叏閫夈€佹竻绌烘搷浣?
 * 5. 淇濆瓨鍚庢彁浜ゅ埌鍚庣锛屽悗绔互"鍏堝垹鍚庢彃"鏂瑰紡淇濆瓨 sys_user_role
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
  /** 瀵硅瘽妗嗘槸鍚︽墦寮€ */
  open: boolean
  /** 鐢ㄦ埛 ID */
  userId?: string
  /** 鐢ㄦ埛鍚?*/
  userName?: string
  /** 鐢ㄦ埛璐﹀彿 */
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
 * 琛ㄦ牸鍒楀畾涔?
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
 * 灏嗚鑹?ID 杞负瀛楃涓诧紙閬垮厤 snowflake ID 杞?Number 鏃剁簿搴︿涪澶憋級
 */
function toValidRoleId(value: unknown): string | null {
  if (value == null) return null
  const str = String(value).trim()
  return str.length > 0 && str !== '0' ? str : null
}

/**
 * 瑙勮寖鍖栬鑹插垪琛?
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
 * 瑙勮寖鍖栧凡鍒嗛厤瑙掕壊 ID
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
 * 杩囨护鍚庣殑瑙掕壊鍒楄〃锛堟悳绱級
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
 * 鐐瑰嚮"纭畾"淇濆瓨鍒嗛厤缁撴灉
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

<style scoped lang="less" src="@/styles/views/system/user/components/user-role-assign-dialog.less"></style>
