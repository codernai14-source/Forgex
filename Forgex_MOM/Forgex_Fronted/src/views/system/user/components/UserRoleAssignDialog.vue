<template>
  <a-modal
    v-model:open="visible"
    title="分配角色"
    :confirm-loading="loading"
    width="520px"
    @ok="handleOk"
    @cancel="handleCancel"
  >
    <a-form :label-col="{ span: 5 }" :wrapper-col="{ span: 18 }">
      <a-form-item label="用户ID">
        <a-input v-model:value="innerUserId" disabled />
      </a-form-item>
      <a-form-item label="角色列表">
        <a-select
          v-model:value="selectedRoleIds"
          mode="multiple"
          style="width: 100%;"
          placeholder="请选择角色"
          :options="roleOptions"
          :max-tag-count="5"
        />
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup lang="ts">
/**
 * 用户分配角色弹窗。
 *
 * 功能说明：
 * 1. 打开弹窗时加载当前租户下的角色列表
 * 2. 加载该用户在当前租户下已分配的角色ID
 * 3. 勾选保存后提交到后端，后端以“先删后插”方式保存 sys_user_role
 *
 * @author Forgex
 * @version 1.0.0
 */
import { ref, watch, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { userApi } from '@/api/system/user'
import { getRoleList } from '@/api/system/role'
import { useUserStore } from '@/stores/user'

interface Props {
  /** 对话框是否打开，用于控制组件的显示/隐藏状态 */
  open: boolean
  /** 用户 ID，必填，用于指定要分配角色的用户 */
  userId?: string
}

const props = withDefaults(defineProps<Props>(), {
  open: false,
  userId: undefined,
})

const emit = defineEmits<{
  /**
   * 更新对话框打开状态
   * @param value 新的打开状态
   */
  'update:open': [value: boolean]
  /**
   * 操作成功事件
   * 触发时机：角色分配成功保存后触发
   */
  'success': []
}>()

const visible = ref(props.open)
const loading = ref(false)
const innerUserId = ref<string>()
const selectedRoleIds = ref<number[]>([])
const roleOptions = ref<{ label: string; value: number }[]>([])
const userStore = useUserStore()

watch(() => props.open, (val) => {
  visible.value = val
  if (val) {
    innerUserId.value = props.userId
    initData()
  } else {
    resetState()
  }
})

watch(visible, (val) => emit('update:open', val))

async function initData() {
  if (!innerUserId.value) return
  loading.value = true
  try {
    const [roleList, assigned] = await Promise.all([
      getRoleList({ tenantId: userStore.tenantId }),
      userApi.getUserAssignedRoles(innerUserId.value),
    ])
    roleOptions.value = (roleList || []).map((r: any) => ({
      label: r.roleCode ? `${r.roleName}（${r.roleCode}）` : r.roleName,
      value: Number(r.id),
    }))
    selectedRoleIds.value = (assigned?.assignedRoleIds || []).map((id: any) => Number(id))
  } catch (e) {
    console.error(e)
    message.error('加载角色数据失败')
  } finally {
    loading.value = false
  }
}

function resetState() {
  innerUserId.value = undefined
  selectedRoleIds.value = []
  roleOptions.value = []
}

/**
 * 点击“确定”保存分配结果。
 */
async function handleOk() {
  if (!innerUserId.value) {
    message.warning('缺少用户ID')
    return
  }
  loading.value = true
  try {
    await userApi.saveUserRoles(innerUserId.value, selectedRoleIds.value)
    message.success('保存成功')
    visible.value = false
    emit('success')
  } catch (e) {
    console.error(e)
    message.error('保存失败')
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
