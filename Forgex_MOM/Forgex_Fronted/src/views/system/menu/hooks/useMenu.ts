/**
 * 菜单列表逻辑 Hook
 * 
 * 封装菜单管理页面的核心业务逻辑，包括树形列表查询、搜索、删除等功能。
 * 
 * @author Forgex
 * @version 1.0.0
 */

import { ref, reactive } from 'vue'
import { message, Modal } from 'ant-design-vue'
import { getMenuTree, deleteMenu, batchDeleteMenus } from '@/api/system/menu'
import type { Menu, MenuQuery } from '../types'

/**
 * 构建菜单树形结构
 * 
 * 执行步骤：
 * 1. 遍历菜单列表
 * 2. 找出所有父级 ID 等于指定值的菜单项
 * 3. 递归构建子菜单
 * 4. 按排序号排序
 * 
 * @param list 菜单列表（扁平结构）
 * @param parentId 父级菜单 ID，默认 '0'（顶级菜单）
 * @returns 树形结构的菜单列表
 */
function buildTree(list: any[], parentId: string | number = '0'): any[] {
  const result: any[] = []
  
  for (const item of list) {
    if (String(item.parentId || '0') === String(parentId)) {
      const children = buildTree(list, item.id)
      if (children.length > 0) {
        item.children = children
      }
      result.push(item)
    }
  }
  
  return result.sort((a, b) => (a.orderNum || 0) - (b.orderNum || 0))
}

/**
 * 菜单管理 Hook
 * 
 * @returns 包含菜单列表状态和操作方法的对象
 */
export function useMenu() {
  // 加载状态
  const loading = ref(false)
  
  // 菜单列表（树形结构）
  const menuList = ref<Menu[]>([])
  
  // 查询参数
  const queryParams = reactive<MenuQuery>({
    moduleId: undefined,
    name: undefined,
    status: undefined
  })
  
  // 选中的菜单 ID 列表
  const selectedRowKeys = ref<string[]>([])
  
  /**
   * 加载菜单列表
   * 
   * 执行步骤：
   * 1. 设置加载状态为 true
   * 2. 从 sessionStorage 获取租户 ID
   * 3. 构建查询参数（模块 ID、名称、状态等）
   * 4. 调用 getMenuTree 接口获取菜单树
   * 5. 使用 buildTree 构建完整的树形结构
   * 6. 重置加载状态
   * 
   * @throws 加载失败时显示错误提示
   */
  const loadMenuList = async () => {
    try {
      loading.value = true
      const tenantId = sessionStorage.getItem('tenantId')
      if (!tenantId) {
        message.error('租户信息缺失')
        return
      }
      
      const params: any = {
        tenantId,
        moduleId: queryParams.moduleId
      }
      
      if (queryParams.name) {
        params.name = queryParams.name
      }
      
      if (queryParams.status !== undefined) {
        params.status = queryParams.status
      }
      
      const response = await getMenuTree(params)
      const flatList = response || []
      
      // 构建树形结构
      menuList.value = buildTree(flatList)
    } catch (error) {
      console.error('加载菜单列表失败:', error)
      message.error('加载菜单列表失败')
    } finally {
      loading.value = false
    }
  }
  
  /**
   * 搜索
   * 
   * 执行步骤：
   * 1. 调用 loadMenuList 重新加载菜单树
   */
  const handleSearch = () => {
    loadMenuList()
  }
  
  /**
   * 重置搜索
   * 
   * 执行步骤：
   * 1. 清空所有查询条件
   * 2. 调用 loadMenuList 重新加载菜单树
   */
  const handleReset = () => {
    queryParams.moduleId = undefined
    queryParams.name = undefined
    queryParams.status = undefined
    loadMenuList()
  }
  
  /**
   * 删除菜单
   * 
   * 执行步骤：
   * 1. 显示确认对话框（提示删除子菜单和权限）
   * 2. 用户确认后调用 deleteMenu 接口
   * 3. 删除成功后刷新列表
   * 
   * @param id 菜单 ID
   * @throws 删除失败时显示错误提示
   */
  const handleDelete = async (id: string) => {
    Modal.confirm({
      title: '确认删除',
      content: '确定要删除该菜单吗？删除后将同时删除其子菜单和按钮权限。',
      okText: '确定',
      cancelText: '取消',
      onOk: async () => {
        try {
          await deleteMenu(id)
          message.success('删除成功')
          loadMenuList()
        } catch (error) {
          console.error('删除菜单失败:', error)
          message.error('删除菜单失败')
        }
      }
    })
  }
  
  /**
   * 批量删除
   * 
   * 执行步骤：
   * 1. 检查是否有选中的菜单
   * 2. 显示确认对话框
   * 3. 用户确认后调用 batchDeleteMenus 接口
   * 4. 删除成功后清空选中状态并刷新列表
   * 
   * @throws 删除失败时显示错误提示
   */
  const handleBatchDelete = () => {
    if (selectedRowKeys.value.length === 0) {
      message.warning('请选择要删除的菜单')
      return
    }
    
    Modal.confirm({
      title: '确认删除',
      content: `确定要删除选中的 ${selectedRowKeys.value.length} 个菜单吗？`,
      okText: '确定',
      cancelText: '取消',
      onOk: async () => {
        try {
          await batchDeleteMenus(selectedRowKeys.value)
          message.success('批量删除成功')
          selectedRowKeys.value = []
          loadMenuList()
        } catch (error) {
          console.error('批量删除失败:', error)
          message.error('批量删除失败')
        }
      }
    })
  }
  
  /**
   * 选择变化
   * 
   * 执行步骤：
   * 1. 更新选中的菜单 ID 列表
   * 
   * @param keys 选中的菜单 ID 列表
   */
  const handleSelectionChange = (keys: string[]) => {
    selectedRowKeys.value = keys
  }
  
  return {
    loading,
    menuList,
    queryParams,
    selectedRowKeys,
    loadMenuList,
    handleSearch,
    handleReset,
    handleDelete,
    handleBatchDelete,
    handleSelectionChange
  }
}
