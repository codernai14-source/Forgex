/**
 * 菜单表单逻辑封装
 */

import { ref, reactive, computed, watch } from 'vue'
import { message } from 'ant-design-vue'
import { addMenu, updateMenu, getMenuTree } from '@/api/system/menu'
import type { Menu, MenuTreeNode } from '../types'
import type { 表单Instance } from 'ant-design-vue'

export function useMenu表单(emit: any) {
  // 琛ㄥ崟寮曠敤
  const formRef = ref<表单Instance>()
  
  // 寮圭獥鏄剧ず鐘舵€?
  const visible = ref(false)
  
  // 琛ㄥ崟妯″紡锛歛dd-鏂板锛宔dit-缂栬緫
  const mode = ref<'add' | 'edit'>('add')
  
  // 鎻愪氦鍔犺浇鐘舵€?
  const submitLoading = ref(false)
  
  // 鐖惰彍鍗曟爲鏁版嵁
  const menuTreeData = ref<MenuTreeNode[]>([
    {
      key: '0',
      title: '鏍圭洰褰?,
      value: '0',
      children: []
    }
  ])
  
  // 褰撳墠妯″潡鐨勬墍鏈夎彍鍗曪紙鐢ㄤ簬鏋勫缓鐖惰彍鍗曟爲锛?
  const allMenus = ref<Menu[]>([])
  
  // 琛ㄥ崟鏁版嵁
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
    status: true
  })
  
  // 琛ㄥ崟鏍囬
  const formTitle = computed(() => {
    return mode.value === 'add' ? '鏂板鑿滃崟' : '缂栬緫鑿滃崟'
  })
  
  // 鏄惁鏄剧ず璺緞瀛楁
  const showPath = computed(() => {
    return formData.type !== 'button'
  })
  
  // 鏄惁鏄剧ず缁勪欢Key瀛楁
  const showComponentKey = computed(() => {
    return formData.type === 'menu' && formData.menuMode === 'embedded'
  })
  
  // 鏄惁鏄剧ず鏉冮檺鏍囪瘑瀛楁
  const showPermKey = computed(() => {
    return formData.type === 'button' || formData.type === 'menu'
  })
  
  // 鏄惁鏄剧ず澶栬仈URL瀛楁
  const showExternalUrl = computed(() => {
    return formData.menuMode === 'external'
  })
  
  // 琛ㄥ崟楠岃瘉瑙勫垯
  const rules = {
    moduleId: [
      { required: true, message: '璇烽€夋嫨鎵€灞炴ā鍧?, trigger: 'change' }
    ],
    name: [
      { required: true, message: '璇疯緭鍏ヨ彍鍗曞悕绉?, trigger: 'blur' },
      { max: 50, message: '鑿滃崟鍚嶇О涓嶈兘瓒呰繃50涓瓧绗?, trigger: 'blur' }
    ],
    type: [
      { required: true, message: '璇烽€夋嫨鑿滃崟绫诲瀷', trigger: 'change' }
    ],
    path: [
      { required: true, message: '璇疯緭鍏ヨ彍鍗曡矾寰?, trigger: 'blur' }
    ],
    permKey: [
      { required: true, message: '璇疯緭鍏ユ潈闄愭爣璇?, trigger: 'blur' }
    ],
    externalUrl: [
      { required: true, message: '璇疯緭鍏ュ鑱擴RL', trigger: 'blur' },
      { type: 'url', message: 'URL鏍煎紡涓嶆纭?, trigger: 'blur' }
    ]
  }
  
  /**
   * 鎵撳紑鏂板寮圭獥
   */
  const openAdd = () => {
    mode.value = 'add'
    reset表单()
    visible.value = true
    // 鍔犺浇鑿滃崟鏍戞暟鎹?
    if (formData.moduleId) {
      loadMenuTreeData(formData.moduleId)
    }
  }
  
  /**
   * 鎵撳紑缂栬緫寮圭獥
   */
  const openEdit = (record: Menu) => {
    mode.value = 'edit'
    Object.assign(formData, record)
    visible.value = true
    // 鍔犺浇鑿滃崟鏍戞暟鎹?
    if (formData.moduleId) {
      loadMenuTreeData(formData.moduleId)
    }
  }
  
  /**
   * 鍏抽棴寮圭獥
   */
  const handleCancel = () => {
    visible.value = false
    reset表单()
  }
  
  /**
   * 鎻愪氦琛ㄥ崟
   */
  const handleSubmit = async () => {
    try {
      // 琛ㄥ崟楠岃瘉
      await formRef.value?.validate()
      
      submitLoading.value = true
      
      if (mode.value === 'add') {
        await addMenu(formData)
      } else {
        await updateMenu(formData)
      }
      
      visible.value = false
      reset表单()
      emit('success')
    } catch (error) {
      console.error('鎻愪氦澶辫触:', error)
      if ((error as any)?.errorFields || error === false) {
        return
      }
    } finally {
      submitLoading.value = false
    }
  }
  
  /**
   * 閲嶇疆琛ㄥ崟
   */
  const reset表单 = () => {
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
      status: true
    })
  }
  
  /**
   * 鑿滃崟绫诲瀷鍙樺寲
   */
  const handleTypeChange = () => {
    // 鎸夐挳绫诲瀷涓嶉渶瑕佽矾寰勫拰缁勪欢Key
    if (formData.type === 'button') {
      formData.path = ''
      formData.componentKey = undefined
      formData.menuMode = 'embedded'
    }
  }
  
  /**
   * 鑿滃崟妯″紡鍙樺寲
   */
  const handleModeChange = () => {
    // 澶栬仈妯″紡涓嶉渶瑕佺粍浠禟ey
    if (formData.menuMode === 'external') {
      formData.componentKey = undefined
    } else {
      formData.externalUrl = undefined
    }
  }
  
  /**
   * 鍔犺浇鑿滃崟鏍戞暟鎹?
   * 鐢ㄤ簬鏋勫缓鐖惰彍鍗曢€夋嫨鍣ㄧ殑鏁版嵁婧?
   */
  const loadMenuTreeData = async (moduleId: string) => {
    if (!moduleId) {
      return
    }
    
    try {
      const tenantId = sessionStorage.getItem('tenantId')
      if (!tenantId) {
        message.warning('绉熸埛淇℃伅缂哄け')
        return
      }
      
      const response = await getMenuTree({ 
        tenantId: Number(tenantId), 
        moduleId: Number(moduleId) 
      })
      allMenus.value = response || []
      buildParentMenuTree()
    } catch (error) {
      console.error('鍔犺浇鑿滃崟鏍戝け璐?', error)
      // 澶辫触鏃朵娇鐢ㄩ粯璁ょ殑鏍圭洰褰曟爲
      allMenus.value = []
      buildParentMenuTree()
    }
  }
  
  /**
   * 閫掑綊鏋勫缓鏍戣妭鐐?
   */
  const buildTreeNodes = (menus: Menu[], parentId: string): MenuTreeNode[] => {
    return menus
      .filter(m => String(m.parentId || '0') === String(parentId))
      .sort((a, b) => (a.orderNum || 0) - (b.orderNum || 0))
      .map(menu => ({
        key: String(menu.id),
        title: menu.name,
        value: String(menu.id),
        children: buildTreeNodes(menus, String(menu.id))
      }))
  }
  
  /**
   * 杩囨护褰撳墠鑿滃崟鍙婂叾瀛愯彍鍗?
   * 闃叉寰幆寮曠敤
   */
  const filterCurrentMenuFromTree = (menus: Menu[], currentId?: string): Menu[] => {
    if (!currentId) return menus
    
    // 鏀堕泦褰撳墠鑿滃崟鍙婂叾鎵€鏈夊瓙鑿滃崟鐨処D
    const excludeIds = new Set<string>()
    const collectChildIds = (id: string) => {
      excludeIds.add(id)
      menus
        .filter(m => String(m.parentId) === String(id))
        .forEach(child => collectChildIds(String(child.id)))
    }
    
    collectChildIds(currentId)
    
    // 杩囨护鎺夎繖浜汭D
    return menus.filter(m => !excludeIds.has(String(m.id)))
  }
  
  /**
   * 鏋勫缓鐖惰彍鍗曟爲
   * 灏嗘墎骞冲寲鐨勮彍鍗曞垪琛ㄨ浆鎹负鏍戝舰缁撴瀯
   * 鎺掗櫎绫诲瀷涓?'button' 鐨勮彍鍗曢」
   */
  const buildParentMenuTree = () => {
    const rootNode: MenuTreeNode = {
      key: '0',
      title: '鏍圭洰褰?,
      value: '0',
      children: []
    }
    
    // 杩囨护鎺夋寜閽被鍨嬬殑鑿滃崟
    const validMenus = allMenus.value.filter(m => m.type !== 'button')
    
    // 濡傛灉鏄紪杈戞ā寮忥紝杩囨护鎺夊綋鍓嶈彍鍗曞強鍏跺瓙鑿滃崟
    const filteredMenus = mode.value === 'edit' 
      ? filterCurrentMenuFromTree(validMenus, formData.id)
      : validMenus
    
    // 鏋勫缓鏍戝舰缁撴瀯
    rootNode.children = buildTreeNodes(filteredMenus, '0')
    menuTreeData.value = [rootNode]
  }
  
  /**
   * 鑷姩璁＄畻鑿滃崟灞傜骇
   * 鏍规嵁鐖惰彍鍗旾D璁＄畻褰撳墠鑿滃崟鐨勫眰绾?
   */
  const calculateMenuLevel = (parentId: string): number => {
    // 鏍圭洰褰曚笅鐨勮彍鍗曚负涓€绾ц彍鍗?
    if (!parentId || parentId === '0') {
      return 1
    }
    
    // 鏌ユ壘鐖惰彍鍗?
    const parentMenu = allMenus.value.find(m => String(m.id) === String(parentId))
    if (!parentMenu) {
      return 1
    }
    
    // 鐖惰彍鍗曞眰绾?+ 1
    return (parentMenu.menuLevel || 1) + 1
  }
  
  /**
   * 鐩戝惉鐖惰彍鍗曞彉鍖栵紝鑷姩璁＄畻灞傜骇
   */
  watch(() => formData.parentId, (newParentId) => {
    if (newParentId !== undefined) {
      formData.menuLevel = calculateMenuLevel(newParentId)
    }
  })
  
  /**
   * 鐩戝惉妯″潡鍙樺寲锛岄噸鏂板姞杞借彍鍗曟爲
   */
  watch(() => formData.moduleId, (newModuleId) => {
    if (newModuleId) {
      loadMenuTreeData(newModuleId)
    }
  })
  
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
    handleModeChange
  }
}
