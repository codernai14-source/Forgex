import { ref, reactive } from 'vue'
import { message } from 'ant-design-vue'
import { getRoleList, deleteRole, batchDeleteRoles } from '@/api/system/role'
import type { Role } from '../types'

export const useRole = () => {
  const loading = ref(false)
  const roles = ref<Role[]>([])
  const selectedRowKeys = ref<number[]>([])

  // 搜索表单
  const searchForm = reactive({
    roleCode: '',
    roleName: '',
    status: undefined as number | undefined
  })

  /**
   * 加载角色列表
   */
  const loadRoles = async () => {
    loading.value = true
    try {
      // http拦截器已经返回了data字段，不需要再访问res.data
      const data = await getRoleList(searchForm)
      roles.value = data || []
    } catch (error) {
      console.error('加载角色列表失败:', error)
      message.error('加载角色列表失败')
    } finally {
      loading.value = false
    }
  }

  /**
   * 搜索
   */
  const handleSearch = () => {
    loadRoles()
  }

  /**
   * 重置搜索
   */
  const handleReset = () => {
    searchForm.roleCode = ''
    searchForm.roleName = ''
    searchForm.status = undefined
    loadRoles()
  }

  /**
   * 删除角色
   */
  const handleDelete = async (id: number) => {
    try {
      await deleteRole(id)
      // 成功提示由后端返回，在 http 拦截器中统一处理
      loadRoles()
    } catch (error) {
      console.error('删除角色失败:', error)
      message.error('删除失败')
    }
  }

  /**
   * 批量删除
   */
  const handleBatchDelete = async () => {
    if (selectedRowKeys.value.length === 0) {
      message.warning('请选择要删除的角色')
      return
    }

    try {
      await batchDeleteRoles(selectedRowKeys.value)
      // 成功提示由后端返回，在 http 拦截器中统一处理
      selectedRowKeys.value = []
      loadRoles()
    } catch (error) {
      console.error('批量删除失败:', error)
      message.error('批量删除失败')
    }
  }

  /**
   * 行选择变化
   */
  const onSelectChange = (keys: number[]) => {
    selectedRowKeys.value = keys
  }

  return {
    loading,
    roles,
    searchForm,
    selectedRowKeys,
    loadRoles,
    handleSearch,
    handleReset,
    handleDelete,
    handleBatchDelete,
    onSelectChange
  }
}
