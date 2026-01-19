import { ref, reactive } from 'vue'
import { message } from 'ant-design-vue'
import type { FormInstance } from 'ant-design-vue'
import { addModule, updateModule, getModuleById } from '@/api/system/module'
import type { Module } from '../types'

/**
 * 模块表单Hook
 */
export function useModuleForm() {
  const formRef = ref<FormInstance>()
  const dialogVisible = ref(false)
  const dialogTitle = ref('新增模块')
  const loading = ref(false)
  const isEdit = ref(false)

  // 表单数据
  const formData = reactive<Module>({
    id: undefined,
    code: '',
    name: '',
    icon: undefined,
    orderNum: 0,
    visible: 1,
    status: 1
  })

  // 表单验证规则
  const rules = {
    code: [
      { required: true, message: '请输入模块编码', trigger: 'blur' },
      { pattern: /^[a-zA-Z0-9_]{2,50}$/, message: '只能包含字母、数字、下划线，长度2-50', trigger: 'blur' }
    ],
    name: [
      { required: true, message: '请输入模块名称', trigger: 'blur' },
      { max: 50, message: '模块名称不能超过50个字符', trigger: 'blur' }
    ],
    orderNum: [
      { required: true, message: '请输入排序号', trigger: 'blur' },
      { type: 'number', message: '排序号必须是数字', trigger: 'blur' }
    ],
    status: [
      { required: true, message: '请选择状态', trigger: 'change' }
    ]
  }

  /**
   * 打开新增对话框
   */
  const openAddDialog = () => {
    isEdit.value = false
    dialogTitle.value = '新增模块'
    resetForm()
    dialogVisible.value = true
  }

  /**
   * 打开编辑对话框
   */
  const openEditDialog = async (id: string) => {
    isEdit.value = true
    dialogTitle.value = '编辑模块'
    dialogVisible.value = true

    try {
      const response = await getModuleById(id)
      Object.assign(formData, response.data)
    } catch (error) {
      console.error('加载模块详情失败:', error)
      message.error('加载模块详情失败')
      dialogVisible.value = false
    }
  }

  /**
   * 提交表单
   */
  const handleSubmit = async () => {
    try {
      await formRef.value?.validate()
      loading.value = true

      if (isEdit.value) {
        await updateModule(formData)
        message.success('更新成功')
      } else {
        await addModule(formData)
        message.success('新增成功')
      }

      dialogVisible.value = false
      return true
    } catch (error) {
      console.error('提交失败:', error)
      if (error instanceof Error) {
        message.error(error.message || '操作失败')
      }
      return false
    } finally {
      loading.value = false
    }
  }

  /**
   * 取消
   */
  const handleCancel = () => {
    dialogVisible.value = false
    resetForm()
  }

  /**
   * 重置表单
   */
  const resetForm = () => {
    formRef.value?.resetFields()
    Object.assign(formData, {
      id: undefined,
      code: '',
      name: '',
      icon: undefined,
      orderNum: 0,
      visible: 1,
      status: 1
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
    resetForm
  }
}
