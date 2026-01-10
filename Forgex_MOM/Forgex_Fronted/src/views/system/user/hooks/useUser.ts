/**
 * 用户列表逻辑
 */
import { ref, reactive } from 'vue'
import { message, Modal } from 'ant-design-vue'
import { userApi } from '@/api/system/user'
import type { User, UserQuery } from '../types'

export function useUser() {
  // 加载状态
  const loading = ref(false)
  
  // 用户列表
  const userList = ref<User[]>([])
  
  // 分页信息
  const pagination = reactive({
    current: 1,
    pageSize: 10,
    total: 0,
  })
  
  // 查询条件
  const queryForm = reactive<Partial<UserQuery>>({
    username: '',
    phone: '',
    departmentId: undefined,
    positionId: undefined,
    status: undefined,
  })
  
  // 选中的用户ID列表
  const selectedRowKeys = ref<string[]>([])
  
  /**
   * 获取用户列表
   */
  async function fetchUserList() {
    loading.value = true
    try {
      const query: UserQuery = {
        ...queryForm,
        pageNum: pagination.current,
        pageSize: pagination.pageSize,
      }
      
      // http拦截器已经返回了data字段
      const data = await userApi.getUserList(query)
      userList.value = data.records || []
      pagination.total = data.total || 0
    } catch (error) {
      console.error('获取用户列表失败:', error)
      message.error('获取用户列表失败')
    } finally {
      loading.value = false
    }
  }
  
  /**
   * 搜索
   */
  function handleSearch() {
    pagination.current = 1
    fetchUserList()
  }
  
  /**
   * 重置搜索
   */
  function handleReset() {
    Object.assign(queryForm, {
      username: '',
      phone: '',
      departmentId: undefined,
      positionId: undefined,
      status: undefined,
    })
    pagination.current = 1
    fetchUserList()
  }
  
  /**
   * 分页改变
   */
  function handlePageChange(page: number, pageSize: number) {
    pagination.current = page
    pagination.pageSize = pageSize
    fetchUserList()
  }
  
  /**
   * 删除用户
   */
  async function handleDelete(id: string) {
    Modal.confirm({
      title: '确认删除',
      content: '确定要删除该用户吗？',
      onOk: async () => {
        try {
          await userApi.deleteUser(id)
          message.success('删除成功')
          fetchUserList()
        } catch (error) {
          console.error('删除用户失败:', error)
          message.error('删除失败')
        }
      },
    })
  }
  
  /**
   * 批量删除用户
   */
  async function handleBatchDelete() {
    if (selectedRowKeys.value.length === 0) {
      message.warning('请选择要删除的用户')
      return
    }
    
    Modal.confirm({
      title: '确认删除',
      content: `确定要删除选中的 ${selectedRowKeys.value.length} 个用户吗？`,
      onOk: async () => {
        try {
          await userApi.batchDeleteUsers(selectedRowKeys.value)
          message.success('批量删除成功')
          selectedRowKeys.value = []
          fetchUserList()
        } catch (error) {
          console.error('批量删除失败:', error)
          message.error('批量删除失败')
        }
      },
    })
  }
  
  /**
   * 重置密码
   */
  async function handleResetPassword(id: string) {
    Modal.confirm({
      title: '确认重置密码',
      content: '确定要重置该用户的密码吗？密码将重置为：123456',
      onOk: async () => {
        try {
          await userApi.resetPassword(id)
          message.success('密码重置成功')
        } catch (error) {
          console.error('密码重置失败:', error)
          message.error('密码重置失败')
        }
      },
    })
  }
  
  /**
   * 更新用户状态
   */
  async function handleUpdateStatus(id: string, status: boolean) {
    const statusText = status ? '启用' : '禁用'
    Modal.confirm({
      title: `确认${statusText}`,
      content: `确定要${statusText}该用户吗？`,
      onOk: async () => {
        try {
          await userApi.updateUserStatus(id, status)
          message.success(`${statusText}成功`)
          fetchUserList()
        } catch (error) {
          console.error(`${statusText}失败:`, error)
          message.error(`${statusText}失败`)
        }
      },
    })
  }
  
  /**
   * 选择改变
   */
  function handleSelectionChange(keys: string[]) {
    selectedRowKeys.value = keys
  }
  
  return {
    loading,
    userList,
    pagination,
    queryForm,
    selectedRowKeys,
    fetchUserList,
    handleSearch,
    handleReset,
    handlePageChange,
    handleDelete,
    handleBatchDelete,
    handleResetPassword,
    handleUpdateStatus,
    handleSelectionChange,
  }
}
