/**
 * 用户列表逻辑 Hook
 * 
 * 封装用户管理页面的核心业务逻辑，包括列表查询、分页、删除、导出等功能。
 * 
 * @author Forgex
 * @version 1.0.0
 */
import { ref, reactive } from 'vue'
import { message, Modal } from 'ant-design-vue'
import { useI18n } from 'vue-i18n'
import { userApi } from '@/api/system/user'
import type { User, UserQuery } from '../types'

const { t } = useI18n()

/**
 * 用户管理 Hook
 * 
 * @returns 包含用户列表状态和操作方法的对象
 */
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
  
  // 选中的用户 ID 列表
  const selectedRowKeys = ref<string[]>([])
  
  /**
   * 获取用户列表
   * 
   * 执行步骤：
   * 1. 设置加载状态为 true
   * 2. 构建查询参数（合并查询条件和分页信息）
   * 3. 调用 userApi.getUserList 接口
   * 4. 更新用户列表和分页总数
   * 5. 重置加载状态
   * 
   * @throws 查询失败时显示错误提示
   */
  async function fetchUserList() {
    loading.value = true
    const data = await userApi.getUserList(query)
    userList.value = data.records || []
    pagination.total = data.total || 0
    loading.value = false
  }
  
  /**
   * 搜索
   * 
   * 执行步骤：
   * 1. 重置分页到第一页
   * 2. 调用 fetchUserList 重新查询
   */
  function handleSearch() {
    pagination.current = 1
    fetchUserList()
  }
  
  /**
   * 重置搜索
   * 
   * 执行步骤：
   * 1. 清空查询条件
   * 2. 重置分页到第一页
   * 3. 调用 fetchUserList 重新查询
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
   * 
   * 执行步骤：
   * 1. 更新当前页码和每页条数
   * 2. 调用 fetchUserList 重新查询
   * 
   * @param page 新的页码
   * @param pageSize 新的每页条数
   */
  function handlePageChange(page: number, pageSize: number) {
    pagination.current = page
    pagination.pageSize = pageSize
    fetchUserList()
  }
  
  /**
   * 删除用户
   * 
   * 执行步骤：
   * 1. 显示确认对话框
   * 2. 用户确认后调用 deleteUser 接口
   * 3. 删除成功后刷新列表
   * 
   * @param id 用户 ID
   * @throws 删除失败时在控制台输出错误
   */
  async function handleDelete(id: string) {
    Modal.confirm({
      title: t('common.confirm'),
      content: t('system.user.message.deleteConfirm'),
      onOk: async () => {
        try {
          await userApi.deleteUser(id)
          fetchUserList()
        } catch (error) {
          console.error(t('system.user.message.deleteFailed'), error)
        }
      },
    })
  }
  
  /**
   * 批量删除用户
   * 
   * 执行步骤：
   * 1. 检查是否有选中的用户
   * 2. 显示确认对话框
   * 3. 用户确认后调用 batchDeleteUsers 接口
   * 4. 删除成功后清空选中状态并刷新列表
   * 
   * @throws 删除失败时在控制台输出错误
   */
  async function handleBatchDelete() {
    if (selectedRowKeys.value.length === 0) {
      message.warning(t('system.user.message.selectToDelete'))
      return
    }
    
    Modal.confirm({
      title: t('common.confirm'),
      content: t('system.user.message.batchDeleteConfirm', { count: selectedRowKeys.value.length }),
      onOk: async () => {
        try {
          await userApi.batchDeleteUsers(selectedRowKeys.value)
          selectedRowKeys.value = []
          fetchUserList()
        } catch (error) {
          console.error(t('system.user.message.batchDeleteFailed'), error)
        }
      },
    })
  }
  
  /**
   * 重置密码
   * 
   * 执行步骤：
   * 1. 显示确认对话框（提示默认密码）
   * 2. 用户确认后调用 resetPassword 接口
   * 
   * @param id 用户 ID
   * @throws 重置失败时在控制台输出错误
   */
  async function handleResetPassword(id: string) {
    Modal.confirm({
      title: t('system.user.resetPassword'),
      content: t('system.user.message.resetPasswordConfirm'),
      onOk: async () => {
        await userApi.resetPassword(id)
      },
    })
  }
  
  /**
   * 更新用户状态
   * 
   * 执行步骤：
   * 1. 显示确认对话框
   * 2. 用户确认后调用 updateUserStatus 接口
   * 3. 更新成功后刷新列表
   * 
   * @param id 用户 ID
   * @param status 新状态（true=启用，false=禁用）
   * @throws 更新失败时在控制台输出错误
   */
  async function handleUpdateStatus(id: string, status: boolean) {
    const actionText = status ? t('system.user.statusActive') : t('system.user.statusInactive')
    Modal.confirm({
      title: t('common.confirm'),
      content: `${t('common.confirm')}${actionText}${t('common.confirmDeleteMessage')}`,
      onOk: async () => {
        try {
          await userApi.updateUserStatus(id, status)
          fetchUserList()
        } catch (error) {
          console.error(t('system.user.message.updateStatusFailed'), error)
        }
      },
    })
  }
  
  /**
   * 选择改变
   * 
   * 执行步骤：
   * 1. 更新选中的用户 ID 列表
   * 
   * @param keys 选中的用户 ID 列表
   */
  function handleSelectionChange(keys: string[]) {
    selectedRowKeys.value = keys
  }
  
  /**
   * 导出用户
   * 
   * 执行步骤：
   * 1. 设置加载状态为 true
   * 2. 调用 exportUsers 导出接口
   * 3. 创建 Blob 对象
   * 4. 创建临时下载链接
   * 5. 触发下载
   * 6. 清理临时对象
   * 7. 重置加载状态
   * 
   * @throws 导出失败时在控制台输出错误
   */
  async function handleExport() {
    try {
      loading.value = true
      const resp: any = await userApi.exportUsers(queryForm)
      const blob = new Blob([resp.data], { type: resp.headers?.['content-type'] || 'application/octet-stream' })
      const url = window.URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      a.download = `sys-user-${Date.now()}.xlsx`
      document.body.appendChild(a)
      a.click()
      document.body.removeChild(a)
      window.URL.revokeObjectURL(url)
    } catch (error) {
      console.error(t('common.exportFailed'), error)
    } finally {
      loading.value = false
    }
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
    handleExport,
  }
}
