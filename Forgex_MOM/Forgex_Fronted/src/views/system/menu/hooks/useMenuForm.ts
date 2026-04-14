/**
 * 菜单表单逻辑封装
 */

import { computed, reactive, ref, watch } from 'vue'
import { message } from 'ant-design-vue'
import type { FormInstance } from 'ant-design-vue'
import { addMenu, getMenuTree, updateMenu } from '@/api/system/menu'
import type { Menu, MenuTreeNode } from '../types'

export function useMenuForm(emit: any) {
  const formRef = ref<FormInstance>()
  const visible = ref(false)
  const mode = ref<'add' | 'edit'>('add')
  const submitLoading = ref(false)
  const menuTreeData = ref<MenuTreeNode[]>([
    {
      key: '0',
      title: '顶级菜单',
      value: '0',
      children: [],
    },
  ])
  const allMenus = ref<Menu[]>([])

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
    visible: true,
    status: true,
  })

  const formTitle = computed(() => (mode.value === 'add' ? '新增菜单' : '编辑菜单'))
  const showPath = computed(() => formData.type !== 'button')
  const showComponentKey = computed(() => formData.type === 'menu' && formData.menuMode === 'embedded')
  const showPermKey = computed(() => formData.type === 'button' || formData.type === 'menu')
  const showExternalUrl = computed(() => formData.menuMode === 'external')

  const rules = {
    moduleId: [{ required: true, message: '请选择所属模块', trigger: 'change' }],
    name: [
      { required: true, message: '请输入菜单名称', trigger: 'blur' },
      { max: 50, message: '菜单名称不能超过 50 个字符', trigger: 'blur' },
    ],
    type: [{ required: true, message: '请选择菜单类型', trigger: 'change' }],
    path: [{ required: true, message: '请输入路由路径', trigger: 'blur' }],
    permKey: [{ required: true, message: '请输入权限标识', trigger: 'blur' }],
    externalUrl: [
      { required: true, message: '请输入外链 URL', trigger: 'blur' },
      { type: 'url', message: 'URL 格式不正确', trigger: 'blur' },
    ],
  }

  function openAdd() {
    mode.value = 'add'
    resetForm()
    visible.value = true
    if (formData.moduleId) {
      loadMenuTreeData(formData.moduleId)
    }
  }

  function openEdit(record: Menu) {
    mode.value = 'edit'
    Object.assign(formData, record)
    visible.value = true
    if (formData.moduleId) {
      loadMenuTreeData(formData.moduleId)
    }
  }

  function handleCancel() {
    visible.value = false
    resetForm()
  }

  async function handleSubmit() {
    try {
      await formRef.value?.validate()
      submitLoading.value = true

      if (mode.value === 'add') {
        await addMenu(formData)
      } else {
        await updateMenu(formData)
      }

      visible.value = false
      resetForm()
      emit('success')
    } catch (error) {
      console.error('提交菜单表单失败:', error)
      if ((error as any)?.errorFields || error === false) {
        return
      }
    } finally {
      submitLoading.value = false
    }
  }

  function resetForm() {
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
      visible: true,
      status: true,
    })
  }

  function handleTypeChange() {
    if (formData.type === 'button') {
      formData.path = ''
      formData.componentKey = undefined
      formData.menuMode = 'embedded'
    }
  }

  function handleModeChange() {
    if (formData.menuMode === 'external') {
      formData.componentKey = undefined
    } else {
      formData.externalUrl = undefined
    }
  }

  async function loadMenuTreeData(moduleId: string) {
    if (!moduleId) return

    try {
      const tenantId = sessionStorage.getItem('tenantId')
      if (!tenantId) {
        message.warning('未获取到租户信息')
        return
      }

      const response = await getMenuTree({
        tenantId: Number(tenantId),
        moduleId: Number(moduleId),
      })
      allMenus.value = response || []
      buildParentMenuTree()
    } catch (error) {
      console.error('加载菜单树失败:', error)
      allMenus.value = []
      buildParentMenuTree()
    }
  }

  function buildTreeNodes(menus: Menu[], parentId: string): MenuTreeNode[] {
    return menus
      .filter((menu) => String(menu.parentId || '0') === String(parentId))
      .sort((a, b) => (a.orderNum || 0) - (b.orderNum || 0))
      .map((menu) => ({
        key: String(menu.id),
        title: menu.name,
        value: String(menu.id),
        children: buildTreeNodes(menus, String(menu.id)),
      }))
  }

  function filterCurrentMenuFromTree(menus: Menu[], currentId?: string): Menu[] {
    if (!currentId) return menus

    const excludeIds = new Set<string>()
    const collectChildIds = (id: string) => {
      excludeIds.add(id)
      menus
        .filter((menu) => String(menu.parentId) === String(id))
        .forEach((child) => collectChildIds(String(child.id)))
    }

    collectChildIds(currentId)
    return menus.filter((menu) => !excludeIds.has(String(menu.id)))
  }

  function buildParentMenuTree() {
    const rootNode: MenuTreeNode = {
      key: '0',
      title: '顶级菜单',
      value: '0',
      children: [],
    }

    const validMenus = allMenus.value.filter((menu) => menu.type !== 'button')
    const filteredMenus =
      mode.value === 'edit' ? filterCurrentMenuFromTree(validMenus, formData.id) : validMenus

    rootNode.children = buildTreeNodes(filteredMenus, '0')
    menuTreeData.value = [rootNode]
  }

  function calculateMenuLevel(parentId: string): number {
    if (!parentId || parentId === '0') {
      return 1
    }

    const parentMenu = allMenus.value.find((menu) => String(menu.id) === String(parentId))
    if (!parentMenu) {
      return 1
    }

    return (parentMenu.menuLevel || 1) + 1
  }

  watch(
    () => formData.parentId,
    (newParentId) => {
      if (newParentId !== undefined) {
        formData.menuLevel = calculateMenuLevel(newParentId)
      }
    },
  )

  watch(
    () => formData.moduleId,
    (newModuleId) => {
      if (newModuleId) {
        loadMenuTreeData(newModuleId)
      }
    },
  )

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
    menuTreeData,
    openAdd,
    openEdit,
    handleCancel,
    handleSubmit,
    handleTypeChange,
    handleModeChange,
  }
}
