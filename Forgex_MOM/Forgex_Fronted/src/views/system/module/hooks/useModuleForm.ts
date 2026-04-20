import { reactive, ref } from 'vue'
import type { FormInstance } from 'ant-design-vue'
import { addModule, getModuleById, updateModule } from '@/api/system/module'
import type { Module } from '../types'

/**
 * 模块表单逻辑封装
 */
export function useModuleForm() {
  const formRef = ref<FormInstance>()
  const dialogVisible = ref(false)
  const dialogTitle = ref('新增模块')
  const loading = ref(false)
  const isEdit = ref(false)

  const formData = reactive<Module>({
    id: undefined,
    code: '',
    name: '',
    nameI18nJson: '',
    icon: undefined,
    orderNum: 0,
    visible: 1,
    status: 1,
  })

  const rules = {
    code: [
      { required: true, message: '请输入模块编码', trigger: 'blur' },
      { pattern: /^[a-zA-Z0-9_]{2,50}$/, message: '只能包含字母、数字和下划线，长度 2-50', trigger: 'blur' },
    ],
    nameI18nJson: [
      {
        required: true,
        message: '请输入模块名称',
        trigger: 'change',
        validator: (_rule: any, value: string) => {
          if (!value || value === '{}' || value === '') {
            return Promise.reject('请至少配置一种语言的模块名称')
          }
          return Promise.resolve()
        },
      },
    ],
    orderNum: [
      { required: true, message: '请输入排序号', trigger: 'blur' },
      { type: 'number', message: '排序号必须是数字', trigger: 'blur' },
    ],
    status: [{ required: true, message: '请选择状态', trigger: 'change' }],
  }

  function openAddDialog() {
    isEdit.value = false
    dialogTitle.value = '新增模块'
    resetForm()
    dialogVisible.value = true
  }

  async function openEditDialog(id: string) {
    isEdit.value = true
    dialogTitle.value = '编辑模块'
    dialogVisible.value = true

    try {
      const response = await getModuleById(id)
      Object.assign(formData, response.data)
    } catch (error) {
      console.error('加载模块详情失败:', error)
      dialogVisible.value = false
    }
  }

  async function handleSubmit() {
    try {
      await formRef.value?.validate()
      loading.value = true

      if (isEdit.value) {
        await updateModule(formData)
      } else {
        await addModule(formData)
      }

      dialogVisible.value = false
      return true
    } catch (error) {
      console.error('提交模块表单失败:', error)
      if ((error as any)?.errorFields) {
        return false
      }
      return false
    } finally {
      loading.value = false
    }
  }

  function handleCancel() {
    dialogVisible.value = false
    resetForm()
  }

  function resetForm() {
    formRef.value?.resetFields()
    Object.assign(formData, {
      id: undefined,
      code: '',
      name: '',
      nameI18nJson: '',
      icon: undefined,
      orderNum: 0,
      visible: 1,
      status: 1,
    })
  }

  return {
    formRef,
    dialogVisible,
    dialogTitle,
    loading,
    isEdit,
    formData,
    rules,
    openAddDialog,
    openEditDialog,
    handleSubmit,
    handleCancel,
    resetForm,
  }
}
