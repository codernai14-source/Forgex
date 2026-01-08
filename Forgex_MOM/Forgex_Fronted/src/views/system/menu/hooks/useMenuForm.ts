/**
 * 菜单表单逻辑Hook
 */

import { ref, reactive, computed } from 'vue'
import { message } from 'ant-design-vue'
import { addMenu, updateMenu } from '@/api/system/menu'
import type { Menu } from '../types'
import type { FormInstance } from 'ant-design-vue'

export function useMenuForm(emit: any) {
  // 表单引用
  const formRef = ref<FormInstance>()
  
  // 弹窗显示状态
  const visible = ref(false)
  
  // 表单模式：add-新增，edit-编辑
  const mode = ref<'add' | 'edit'>('add')
  
  // 提交加载状态
  const submitLoading = ref(false)
  
  // 表单数据
  const formData = reactive<Menu>({
    id: undefined,
    moduleId: '',
    parentId: '0',
    type: 'menu',
    menuLevel: 1,
    path: '',
    name: '',
    icon: undefined,
    componentKey: undefined,
    permKey: undefined,
    menuMode: 'embedded',
    externalUrl: undefined,
    orderNum: 0,
    visible: 1,
    status: 1
  })
  
  // 表单标题
  const formTitle = computed(() => {
    return mode.value === 'add' ? '新增菜单' : '编辑菜单'
  })
  
  // 是否显示路径字段
  const showPath = computed(() => {
    return formData.type !== 'button'
  })
  
  // 是否显示组件Key字段
  const showComponentKey = computed(() => {
    return formData.type === 'menu' && formData.menuMode === 'embedded'
  })
  
  // 是否显示权限标识字段
  const showPermKey = computed(() => {
    return formData.type === 'button' || formData.type === 'menu'
  })
  
  // 是否显示外联URL字段
  const showExternalUrl = computed(() => {
    return formData.menuMode === 'external'
  })
  
  // 表单验证规则
  const rules = {
    moduleId: [
      { required: true, message: '请选择所属模块', trigger: 'change' }
    ],
    name: [
      { required: true, message: '请输入菜单名称', trigger: 'blur' },
      { max: 50, message: '菜单名称不能超过50个字符', trigger: 'blur' }
    ],
    type: [
      { required: true, message: '请选择菜单类型', trigger: 'change' }
    ],
    path: [
      { required: true, message: '请输入菜单路径', trigger: 'blur' }
    ],
    permKey: [
      { required: true, message: '请输入权限标识', trigger: 'blur' }
    ],
    externalUrl: [
      { required: true, message: '请输入外联URL', trigger: 'blur' },
      { type: 'url', message: 'URL格式不正确', trigger: 'blur' }
    ]
  }
  
  /**
   * 打开新增弹窗
   */
  const openAdd = () => {
    mode.value = 'add'
    resetForm()
    visible.value = true
  }
  
  /**
   * 打开编辑弹窗
   */
  const openEdit = (record: Menu) => {
    mode.value = 'edit'
    Object.assign(formData, record)
    visible.value = true
  }
  
  /**
   * 关闭弹窗
   */
  const handleCancel = () => {
    visible.value = false
    resetForm()
  }
  
  /**
   * 提交表单
   */
  const handleSubmit = async () => {
    try {
      // 表单验证
      await formRef.value?.validate()
      
      submitLoading.value = true
      
      if (mode.value === 'add') {
        await addMenu(formData)
        message.success('新增成功')
      } else {
        await updateMenu(formData)
        message.success('更新成功')
      }
      
      visible.value = false
      resetForm()
      emit('success')
    } catch (error) {
      console.error('提交失败:', error)
      if (error !== false) { // 排除表单验证失败
        message.error('提交失败')
      }
    } finally {
      submitLoading.value = false
    }
  }
  
  /**
   * 重置表单
   */
  const resetForm = () => {
    formRef.value?.resetFields()
    Object.assign(formData, {
      id: undefined,
      moduleId: '',
      parentId: '0',
      type: 'menu',
      menuLevel: 1,
      path: '',
      name: '',
      icon: undefined,
      componentKey: undefined,
      permKey: undefined,
      menuMode: 'embedded',
      externalUrl: undefined,
      orderNum: 0,
      visible: 1,
      status: 1
    })
  }
  
  /**
   * 菜单类型变化
   */
  const handleTypeChange = () => {
    // 按钮类型不需要路径和组件Key
    if (formData.type === 'button') {
      formData.path = ''
      formData.componentKey = undefined
      formData.menuMode = 'embedded'
    }
  }
  
  /**
   * 菜单模式变化
   */
  const handleModeChange = () => {
    // 外联模式不需要组件Key
    if (formData.menuMode === 'external') {
      formData.componentKey = undefined
    } else {
      formData.externalUrl = undefined
    }
  }
  
  return {
    formRef,
    visible,
    mode,
    submitLoading,
    formData,
    formTitle,
    showPath,
    showComponentKey,
    showPermKey,
    showExternalUrl,
    rules,
    openAdd,
    openEdit,
    handleCancel,
    handleSubmit,
    handleTypeChange,
    handleModeChange
  }
}
