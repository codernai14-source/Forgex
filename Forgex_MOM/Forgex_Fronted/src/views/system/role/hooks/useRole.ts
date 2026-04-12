import { ref, reactive } from 'vue'
import { message } from 'ant-design-vue'
import { getRoleList, deleteRole, batchDeleteRoles } from '@/api/system/role'
import type { Role } from '../types'

export const useRole = () => {
  const loading = ref(false)
  const roles = ref<Role[]>([])
  const selectedRowKeys = ref<number[]>([])

  // 鎼滅储琛ㄥ崟
  const search表单 = reactive({
    roleCode: '',
    roleName: '',
    status: undefined as number | undefined,
    tenantId: sessionStorage.getItem('tenantId') || undefined as string | undefined,
  })

  /**
   * 鍔犺浇瑙掕壊鍒楄〃
   */
  const loadRoles = async () => {
    loading.value = true
    const data = await getRoleList(search表单)
    roles.value = data || []
    loading.value = false
  }

  /**
   * 鎼滅储
   */
  const handleSearch = () => {
    loadRoles()
  }

  /**
   * 閲嶇疆鎼滅储
   */
  const handleReset = () => {
    search表单.roleCode = ''
    search表单.roleName = ''
    search表单.status = undefined
    loadRoles()
  }

  /**
   * 鍒犻櫎瑙掕壊
   */
  const handleDelete = async (id: number) => {
    await deleteRole(id)
    loadRoles()
  }

  /**
   * 鎵归噺鍒犻櫎
   */
  const handleBatchDelete = async () => {
    if (selectedRowKeys.value.length === 0) {
      message.warning('璇烽€夋嫨瑕佸垹闄ょ殑瑙掕壊')
      return
    }

    await batchDeleteRoles(selectedRowKeys.value)
    selectedRowKeys.value = []
    loadRoles()
  }

  /**
   * 琛岄€夋嫨鍙樺寲
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

