import { ref, reactive } from 'vue'
import { message } from 'ant-design-vue'
import { getRoleList, deleteRole, batchDeleteRoles } from '@/api/system/role'
import type { Role } from '../types'

export const useRole = () => {
  const loading = ref(false)
  const roles = ref<Role[]>([])
  const selectedRowKeys = ref<number[]>([])

  // 搜索表单
  const search表单 = reactive({
    roleCode: '',
    roleName: '',
    status: undefined as number | undefined,
    tenantId: sessionStorage.getItem('tenantId') || undefined as string | undefined,
  })

  /**
   * 加载角色列表。
   */
  const loadRoles = async () => {
    loading.value = true
    const data = await getRoleList(search表单)
    roles.value = data || []
    loading.value = false
  }

  /**
   * 搜索。
   */
  const handleSearch = () => {
    loadRoles()
  }

  /**
   * 重置搜索。
   */
  const handleReset = () => {
    search表单.roleCode = ''
    search表单.roleName = ''
    search表单.status = undefined
    loadRoles()
  }

  /**
   * 删除角色。
   */
  const handleDelete = async (id: number) => {
    await deleteRole(id)
    loadRoles()
  }

  /**
   * 批量删除。
   */
  const handleBatchDelete = async () => {
    if (selectedRowKeys.value.length === 0) {
      message.warning('请选择要删除的角色')
      return
    }

    await batchDeleteRoles(selectedRowKeys.value)
    selectedRowKeys.value = []
    loadRoles()
  }

  /**
   * 行选择变化。
   */
  const onSelectChange = (keys: number[]) => {
    selectedRowKeys.value = keys
  }

  return {
    loading,
    roles,
    search表单,
    selectedRowKeys,
    loadRoles,
    handleSearch,
    handleReset,
    handleDelete,
    handleBatchDelete,
    onSelectChange
  }
}
