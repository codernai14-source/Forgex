import { ref, reactive } from 'vue'
import { message } from 'ant-design-vue'
import { getModulePage, deleteModule, batchDeleteModules } from '@/api/system/module'
import type { Module, ModuleQuery } from '../types'

/**
 * 模块列表Hook
 */
export function useModule() {
  const loading = ref(false)
  const dataSource = ref<Module[]>([])
  const total = ref(0)
  const selectedRowKeys = ref<string[]>([])

  // 查询参数
  const queryParams = reactive<ModuleQuery>({
    code: undefined,
    name: undefined,
    status: undefined,
    pageNum: 1,
    pageSize: 10
  })

  /**
   * 加载模块列表
   */
  const loadData = async () => {
    loading.value = true
    try {
      const response = await getModulePage(queryParams)
      const records = response.records || []
      dataSource.value = records.map((item: any) => ({
        ...item,
        visible: item.visible ? 1 : 0,
        status: item.status ? 1 : 0
      }))
      total.value = response.total || 0
    } catch (error) {
      console.error('加载模块列表失败:', error)
      message.error('加载模块列表失败')
    } finally {
      loading.value = false
    }
  }

  /**
   * 搜索
   */
  const handleSearch = () => {
    queryParams.pageNum = 1
    loadData()
  }

  /**
   * 重置搜索
   */
  const handleReset = () => {
    queryParams.code = undefined
    queryParams.name = undefined
    queryParams.status = undefined
    queryParams.pageNum = 1
    loadData()
  }

  /**
   * 分页变化
   */
  const handlePageChange = (page: number, pageSize: number) => {
    queryParams.pageNum = page
    queryParams.pageSize = pageSize
    loadData()
  }

  /**
   * 删除模块
   */
  const handleDelete = async (id: string) => {
    try {
      await deleteModule(id)
      message.success('删除成功')
      loadData()
    } catch (error) {
      console.error('删除模块失败:', error)
      message.error('删除模块失败')
    }
  }

  /**
   * 批量删除
   */
  const handleBatchDelete = async () => {
    if (selectedRowKeys.value.length === 0) {
      message.warning('请选择要删除的模块')
      return
    }

    try {
      await batchDeleteModules(selectedRowKeys.value)
      message.success('批量删除成功')
      selectedRowKeys.value = []
      loadData()
    } catch (error) {
      console.error('批量删除失败:', error)
      message.error('批量删除失败')
    }
  }

  /**
   * 选择变化
   */
  const handleSelectionChange = (keys: string[]) => {
    selectedRowKeys.value = keys
  }

  return {
    loading,
    dataSource,
    total,
    selectedRowKeys,
    queryParams,
    loadData,
    handleSearch,
    handleReset,
    handlePageChange,
    handleDelete,
    handleBatchDelete,
    handleSelectionChange
  }
}
