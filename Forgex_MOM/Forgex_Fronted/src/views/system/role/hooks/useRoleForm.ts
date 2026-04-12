import { ref, reactive } from 'vue'
import { message } from 'ant-design-vue'
import type { 表单Instance, Rule } from 'ant-design-vue/es/form'
import { addRole, updateRole } from '@/api/system/role'
import type { Role } from '../types'

export const useRole表单 = (onSuccess: () => void) => {
  const formRef = ref<表单Instance>()
  const visible = ref(false)
  const loading = ref(false)
  const isEdit = ref(false)

  // 琛ㄥ崟鏁版嵁
  const formData = reactive<Partial<Role>>({
    id: undefined,
    roleCode: '',
    roleName: '',
    description: '',
    status: 1
  })

  // 琛ㄥ崟楠岃瘉瑙勫垯
  const rules: Record<string, Rule[]> = {
    roleCode: [
      { required: true, message: '璇疯緭鍏ヨ鑹茬紪鐮?, trigger: 'blur' },
      { pattern: /^[a-zA-Z][a-zA-Z0-9_]*$/, message: '瑙掕壊缂栫爜鍙兘鍖呭惈瀛楁瘝銆佹暟瀛楀拰涓嬪垝绾匡紝涓斾互瀛楁瘝寮€澶?, trigger: 'blur' }
    ],
    roleName: [
      { required: true, message: '璇疯緭鍏ヨ鑹插悕绉?, trigger: 'blur' },
      { max: 50, message: '瑙掕壊鍚嶇О涓嶈兘瓒呰繃50涓瓧绗?, trigger: 'blur' }
    ],
    description: [
      { max: 200, message: '鎻忚堪涓嶈兘瓒呰繃200涓瓧绗?, trigger: 'blur' }
    ],
    status: [
      { required: true, message: '璇烽€夋嫨鐘舵€?, trigger: 'change' }
    ]
  }

  /**
   * 鎵撳紑鏂板寮圭獥
   */
  const openAdd = () => {
    isEdit.value = false
    visible.value = true
    reset表单()
  }

  /**
   * 鎵撳紑缂栬緫寮圭獥
   */
  const openEdit = (record: Role) => {
    isEdit.value = true
    visible.value = true
    Object.assign(formData, record)
  }

  /**
   * 閲嶇疆琛ㄥ崟
   */
  const reset表单 = () => {
    formData.id = undefined
    formData.roleCode = ''
    formData.roleName = ''
    formData.description = ''
    formData.status = 1
    formRef.value?.clearValidate()
  }

  /**
   * 鎻愪氦琛ㄥ崟
   */
  const handleSubmit = async () => {
    try {
      await formRef.value?.validate()
      loading.value = true

      const apiFunc = isEdit.value ? updateRole : addRole
      await apiFunc(formData)

      // 鎴愬姛鎻愮ず鐢卞悗绔繑鍥烇紝鍦?http 鎷︽埅鍣ㄤ腑缁熶竴澶勭悊
      visible.value = false
      onSuccess()
    } catch (error) {
      console.error('琛ㄥ崟楠岃瘉澶辫触:', error)
    } finally {
      loading.value = false
    }
  }

  /**
   * 鍙栨秷
   */
  const handleCancel = () => {
    visible.value = false
    reset表单()
  }

  return {
    formRef,
    visible,
    loading,
    isEdit,
    formData,
    rules,
    openAdd,
    openEdit,
    handleSubmit,
    handleCancel
  }
}
