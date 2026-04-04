import { ref, reactive } from 'vue'
import { message } from 'ant-design-vue'
import type { FormInstance, Rule } from 'ant-design-vue/es/form'
import { addRole, updateRole } from '@/api/system/role'
import type { Role } from '../types'

export const useRoleForm = (onSuccess: () => void) => {
  const formRef = ref<FormInstance>()
  const visible = ref(false)
  const loading = ref(false)
  const isEdit = ref(false)

  // 表单数据
  const formData = reactive<Partial<Role>>({
    id: undefined,
    roleCode: '',
    roleName: '',
    description: '',
    status: 1
  })

  // 表单验证规则
  const rules: Record<string, Rule[]> = {
    roleCode: [
      { required: true, message: '请输入角色编码', trigger: 'blur' },
      { pattern: /^[a-zA-Z][a-zA-Z0-9_]*$/, message: '角色编码只能包含字母、数字和下划线，且以字母开头', trigger: 'blur' }
    ],
    roleName: [
      { required: true, message: '请输入角色名称', trigger: 'blur' },
      { max: 50, message: '角色名称不能超过50个字符', trigger: 'blur' }
    ],
    description: [
      { max: 200, message: '描述不能超过200个字符', trigger: 'blur' }
    ],
    status: [
      { required: true, message: '请选择状态', trigger: 'change' }
    ]
  }

  /**
   * 打开新增弹窗
   */
  const openAdd = () => {
    isEdit.value = false
    visible.value = true
    resetForm()
  }

  /**
   * 打开编辑弹窗
   */
  const openEdit = (record: Role) => {
    isEdit.value = true
    visible.value = true
    Object.assign(formData, record)
  }

  /**
   * 重置表单
   */
  const resetForm = () => {
    formData.id = undefined
    formData.roleCode = ''
    formData.roleName = ''
    formData.description = ''
    formData.status = 1
    formRef.value?.clearValidate()
  }

  /**
   * 提交表单
   */
  const handleSubmit = async () => {
    try {
      await formRef.value?.validate()
      loading.value = true

      const apiFunc = isEdit.value ? updateRole : addRole
      await apiFunc(formData)

      // 成功提示由后端返回，在 http 拦截器中统一处理
      visible.value = false
      onSuccess()
    } catch (error) {
      console.error('表单验证失败:', error)
    } finally {
      loading.value = false
    }
  }

  /**
   * 取消
   */
  const handleCancel = () => {
    visible.value = false
    resetForm()
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
