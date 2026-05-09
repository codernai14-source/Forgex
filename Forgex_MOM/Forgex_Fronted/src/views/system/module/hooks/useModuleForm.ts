import { reactive, ref } from 'vue'
import type { FormInstance } from 'ant-design-vue'
import { addModule, getModuleById, updateModule } from '@/api/system/module'
import type { Module } from '../types'
import i18n from '@/locales'

/**
 * 模块表单逻辑封装
 */
export function useModuleForm() {
  const t = i18n.global.t
  const formRef = ref<FormInstance>()
  const dialogVisible = ref(false)
  const dialogTitle = ref(t('system.module.form.addModule'))
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
      { required: true, message: t('system.module.form.moduleCode'), trigger: 'blur' },
      { pattern: /^[a-zA-Z0-9_]{2,50}$/, message: t('system.module.form.moduleCodePattern'), trigger: 'blur' },
    ],
    nameI18nJson: [
      {
        required: true,
        message: t('system.module.form.moduleName'),
        trigger: 'change',
        validator: (_rule: any, value: string) => {
          if (!value || value === '{}' || value === '') {
            return Promise.reject(t('system.module.form.moduleNameI18nRequired'))
          }
          return Promise.resolve()
        },
      },
    ],
    orderNum: [
      { required: true, message: t('system.module.form.orderNum'), trigger: 'blur' },
      { type: 'number', message: t('system.module.form.orderNumNumber'), trigger: 'blur' },
    ],
    status: [{ required: true, message: t('system.module.form.status'), trigger: 'change' }],
  }

  function openAddDialog() {
    isEdit.value = false
    dialogTitle.value = t('system.module.form.addModule')
    resetForm()
    dialogVisible.value = true
  }

  async function openEditDialog(id: string) {
    isEdit.value = true
    dialogTitle.value = t('system.module.form.editModule')
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
