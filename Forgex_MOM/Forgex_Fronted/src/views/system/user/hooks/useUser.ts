/**
 * 鐢ㄦ埛鍒楄〃閫昏緫灏佽
 * 
 * 灏佽鐢ㄦ埛绠＄悊椤甸潰鐨勬牳蹇冧笟鍔￠€昏緫锛屽寘鎷垪琛ㄦ煡璇€佸垎椤点€佸垹闄ゃ€佸鍑虹瓑鍔熻兘銆?
 *
 * @author Forgex
 * @version 1.0.0
 */
import { ref, reactive } from 'vue'
import { message, Modal } from 'ant-design-vue'
import { useI18n } from 'vue-i18n'
// @ts-ignore
import { userApi } from '@/api/system/user'
import type { User, UserQuery } from '../types'

const { t } = useI18n()

/**
 * 鐢ㄦ埛绠＄悊閫昏緫灏佽
 * 
 * @returns 鍖呭惈鐢ㄦ埛鍒楄〃鐘舵€佸拰鎿嶄綔鏂规硶鐨勫璞?
 */
export function useUser() {
  // 鍔犺浇鐘舵€?
  const loading = ref(false)
  
  // 鐢ㄦ埛鍒楄〃
  const userList = ref<User[]>([])
  
  // 鍒嗛〉淇℃伅
  const pagination = reactive({
    current: 1,
    pageSize: 10,
    total: 0,
  })
  
  // 鏌ヨ鏉′欢
  const query琛ㄥ崟 = reactive<Partial<UserQuery>>({
    username: '',
    phone: '',
    departmentId: undefined,
    positionId: undefined,
    status: undefined,
  })
  
  // 閫変腑鐨勭敤鎴?ID 鍒楄〃
  const selectedRowKeys = ref<string[]>([])
  
  /**
   * 鑾峰彇鐢ㄦ埛鍒楄〃
   *
   * 鎵ц姝ラ锛?
   * 1. 璁剧疆鍔犺浇鐘舵€佷负 true
   * 2. 鏋勫缓鏌ヨ鍙傛暟锛堝悎骞舵煡璇㈡潯浠跺拰鍒嗛〉淇℃伅锛?
   * 3. 璋冪敤 userApi.getUserList 鎺ュ彛
   * 4. 鏇存柊鐢ㄦ埛鍒楄〃鍜屽垎椤垫€绘暟
   * 5. 閲嶇疆鍔犺浇鐘舵€?
   *
   * @throws 鏌ヨ澶辫触鏃舵樉绀洪敊璇彁绀?
   */
  async function fetchUserList() {
    loading.value = true
    const data = await userApi.getUserList(query)
    userList.value = data.records || []
    pagination.total = data.total || 0
    loading.value = false
  }
  
  /**
   * 鎼滅储
   *
   * 鎵ц姝ラ锛?
   * 1. 閲嶇疆鍒嗛〉鍒扮涓€椤?
   * 2. 璋冪敤 fetchUserList 閲嶆柊鏌ヨ
   */
  function handleSearch() {
    pagination.current = 1
    fetchUserList()
  }
  
  /**
   * 閲嶇疆鎼滅储
   *
   * 鎵ц姝ラ锛?
   * 1. 娓呯┖鏌ヨ鏉′欢
   * 2. 閲嶇疆鍒嗛〉鍒扮涓€椤?
   * 3. 璋冪敤 fetchUserList 閲嶆柊鏌ヨ
   */
  function handleReset() {
    Object.assign(query琛ㄥ崟, {
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
   * 鍒嗛〉鏀瑰彉
   *
   * 鎵ц姝ラ锛?
   * 1. 鏇存柊褰撳墠椤电爜鍜屾瘡椤垫潯鏁?
   * 2. 璋冪敤 fetchUserList 閲嶆柊鏌ヨ
   *
   * @param page 鏂扮殑椤电爜
   * @param pageSize 鏂扮殑姣忛〉鏉℃暟
   */
  function handlePageChange(page: number, pageSize: number) {
    pagination.current = page
    pagination.pageSize = pageSize
    fetchUserList()
  }
  
  /**
   * 鍒犻櫎鐢ㄦ埛
   *
   * 鎵ц姝ラ锛?
   * 1. 鏄剧ず纭瀵硅瘽妗?
   * 2. 鐢ㄦ埛纭鍚庤皟鐢?deleteUser 鎺ュ彛
   * 3. 鍒犻櫎鎴愬姛鍚庡埛鏂板垪琛?
   *
   * @param id 鐢ㄦ埛 ID
   * @throws 鍒犻櫎澶辫触鏃跺湪鎺у埗鍙拌緭鍑洪敊璇?
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
   * 鎵归噺鍒犻櫎鐢ㄦ埛
   *
   * 鎵ц姝ラ锛?
   * 1. 妫€鏌ユ槸鍚︽湁閫変腑鐨勭敤鎴?
   * 2. 鏄剧ず纭瀵硅瘽妗?
   * 3. 鐢ㄦ埛纭鍚庤皟鐢?batchDeleteUsers 鎺ュ彛
   * 4. 鍒犻櫎鎴愬姛鍚庢竻绌洪€変腑鐘舵€佸苟鍒锋柊鍒楄〃
   *
   * @throws 鍒犻櫎澶辫触鏃跺湪鎺у埗鍙拌緭鍑洪敊璇?
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
   * 閲嶇疆瀵嗙爜
   *
   * 鎵ц姝ラ锛?
   * 1. 鏄剧ず纭瀵硅瘽妗嗭紙鎻愮ず榛樿瀵嗙爜锛?
   * 2. 鐢ㄦ埛纭鍚庤皟鐢?resetPassword 鎺ュ彛
   *
   * @param id 鐢ㄦ埛 ID
   * @throws 閲嶇疆澶辫触鏃跺湪鎺у埗鍙拌緭鍑洪敊璇?
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
   * 鏇存柊鐢ㄦ埛鐘舵€?
   *
   * 鎵ц姝ラ锛?
   * 1. 鏄剧ず纭瀵硅瘽妗?
   * 2. 鐢ㄦ埛纭鍚庤皟鐢?updateUser鐘舵€?鎺ュ彛
   * 3. 鏇存柊鎴愬姛鍚庡埛鏂板垪琛?
   *
   * @param id 鐢ㄦ埛 ID
   * @param status 鏂扮姸鎬侊紙true=鍚敤锛宖alse=绂佺敤锛?
   * @throws 鏇存柊澶辫触鏃跺湪鎺у埗鍙拌緭鍑洪敊璇?
   */
  async function handleUpdate鐘舵€?id: string, status: boolean) {
    const actionText = status ? t('system.user.statusActive') : t('system.user.statusInactive')
    Modal.confirm({
      title: t('common.confirm'),
      content: `${t('common.confirm')}${actionText}${t('common.confirmDeleteMessage')}`,
      onOk: async () => {
        try {
          await userApi.updateUser鐘舵€?id, status)
          fetchUserList()
        } catch (error) {
          console.error(t('system.user.message.updateStatusFailed'), error)
        }
      },
    })
  }
  
  /**
   * 閫夋嫨鏀瑰彉
   *
   * 鎵ц姝ラ锛?
   * 1. 鏇存柊閫変腑鐨勭敤鎴?ID 鍒楄〃
   *
   * @param keys 閫変腑鐨勭敤鎴?ID 鍒楄〃
   */
  function handleSelectionChange(keys: string[]) {
    selectedRowKeys.value = keys
  }
  
  /**
   * 瀵煎嚭鐢ㄦ埛
   *
   * 鎵ц姝ラ锛?
   * 1. 璁剧疆鍔犺浇鐘舵€佷负 true
   * 2. 璋冪敤 exportUsers 瀵煎嚭鎺ュ彛
   * 3. 鍒涘缓 Blob 瀵硅薄
   * 4. 鍒涘缓涓存椂涓嬭浇閾炬帴
   * 5. 瑙﹀彂涓嬭浇
   * 6. 娓呯悊涓存椂瀵硅薄
   * 7. 閲嶇疆鍔犺浇鐘舵€?
   *
   * @throws 瀵煎嚭澶辫触鏃跺湪鎺у埗鍙拌緭鍑洪敊璇?
   */
  async function handleExport() {
    try {
      loading.value = true
      const resp: any = await userApi.exportUsers(query琛ㄥ崟)
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
    query琛ㄥ崟,
    selectedRowKeys,
    fetchUserList,
    handleSearch,
    handleReset,
    handlePageChange,
    handleDelete,
    handleBatchDelete,
    handleResetPassword,
    handleUpdate鐘舵€?
    handleSelectionChange,
    handleExport,
  }
}
