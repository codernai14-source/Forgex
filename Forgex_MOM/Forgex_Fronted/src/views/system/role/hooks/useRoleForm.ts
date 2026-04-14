import { reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import type { FormInstance, Rule } from 'ant-design-vue/es/form'
import { addRole, updateRole } from '@/api/system/role'
import type { Role } from '../types'

export const useRoleForm = (onSuccess: () => void) => {
  const formRef = ref<FormInstance>()
  const visible = ref(false)
  const loading = ref(false)
  const isEdit = ref(false)

  const formData = reactive<Partial<Role>>({
    id: undefined,
    roleCode: '',
    roleName: '',
    description: '',
    status: 1,
  })

  const rules: Record<string, Rule[]> = {
    roleCode: [
      { required: true, message: '请输入角色编码', trigger: 'blur' },
      { pattern: /^[a-zA-Z][a-zA-Z0-9_]*$/, message: '角色编码必须以字母开头，且只能包含字母、数字和下划线', trigger: 'blur' },
    ],
    roleName: [
      { required: true, message: '请输入角色名称', trigger: 'blur' },
      { max: 50, message: '角色名称长度不能超过 50 个字符', trigger: 'blur' },
    ],
    description: [{ max: 200, message: '描述长度不能超过 200 个字符', trigger: 'blur' }],
    status: [{ required: true, message: '请选择状态', trigger: 'change' }],
  }

  function openAdd() {
    isEdit.value = false
    visible.value = true
    resetForm()
  }

  function openEdit(record: Role) {
    isEdit.value = true
    visible.value = true
    Object.assign(formData, record)
  }

  function resetForm() {
    formData.id = undefined
    formData.roleCode = ''
    formData.roleName = ''
    formData.description = ''
    formData.status = 1
    formRef.value?.clearValidate()
  }

  async function handleSubmit() {
    try {
      await formRef.value?.validate()
      loading.value = true

      const apiFunc = isEdit.value ? updateRole : addRole
      await apiFunc(formData)

      visible.value = false
      onSuccess()
    } catch (error) {
      console.error('角色表单提交失败:', error)
      if (!(error as any)?.errorFields) {
        message.error('提交角色信息失败')
      }
    } finally {
      loading.value = false
    }
  }

  function handleCancel() {
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
    handleCancel,
  }
}
