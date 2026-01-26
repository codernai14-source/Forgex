/**
 * 菜单表单逻辑Hook
 */
import { ref, reactive, computed, watch } from 'vue';
import { message } from 'ant-design-vue';
import { addMenu, updateMenu, getMenuTree } from '@/api/system/menu';
export function useMenuForm(emit) {
    // 表单引用
    const formRef = ref();
    // 弹窗显示状态
    const visible = ref(false);
    // 表单模式：add-新增，edit-编辑
    const mode = ref('add');
    // 提交加载状态
    const submitLoading = ref(false);
    // 父菜单树数据
    const menuTreeData = ref([
        {
            key: '0',
            title: '根目录',
            value: '0',
            children: []
        }
    ]);
    // 当前模块的所有菜单（用于构建父菜单树）
    const allMenus = ref([]);
    // 表单数据
    const formData = reactive({
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
    });
    // 表单标题
    const formTitle = computed(() => {
        return mode.value === 'add' ? '新增菜单' : '编辑菜单';
    });
    // 是否显示路径字段
    const showPath = computed(() => {
        return formData.type !== 'button';
    });
    // 是否显示组件Key字段
    const showComponentKey = computed(() => {
        return formData.type === 'menu' && formData.menuMode === 'embedded';
    });
    // 是否显示权限标识字段
    const showPermKey = computed(() => {
        return formData.type === 'button' || formData.type === 'menu';
    });
    // 是否显示外联URL字段
    const showExternalUrl = computed(() => {
        return formData.menuMode === 'external';
    });
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
    };
    /**
     * 打开新增弹窗
     */
    const openAdd = () => {
        mode.value = 'add';
        resetForm();
        visible.value = true;
        // 加载菜单树数据
        if (formData.moduleId) {
            loadMenuTreeData(formData.moduleId);
        }
    };
    /**
     * 打开编辑弹窗
     */
    const openEdit = (record) => {
        mode.value = 'edit';
        Object.assign(formData, record);
        visible.value = true;
        // 加载菜单树数据
        if (formData.moduleId) {
            loadMenuTreeData(formData.moduleId);
        }
    };
    /**
     * 关闭弹窗
     */
    const handleCancel = () => {
        visible.value = false;
        resetForm();
    };
    /**
     * 提交表单
     */
    const handleSubmit = async () => {
        try {
            // 表单验证
            await formRef.value?.validate();
            submitLoading.value = true;
            if (mode.value === 'add') {
                await addMenu(formData);
                message.success('新增成功');
            }
            else {
                await updateMenu(formData);
                message.success('更新成功');
            }
            visible.value = false;
            resetForm();
            emit('success');
        }
        catch (error) {
            console.error('提交失败:', error);
            if (error !== false) { // 排除表单验证失败
                message.error('提交失败');
            }
        }
        finally {
            submitLoading.value = false;
        }
    };
    /**
     * 重置表单
     */
    const resetForm = () => {
        formRef.value?.resetFields();
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
        });
    };
    /**
     * 菜单类型变化
     */
    const handleTypeChange = () => {
        // 按钮类型不需要路径和组件Key
        if (formData.type === 'button') {
            formData.path = '';
            formData.componentKey = undefined;
            formData.menuMode = 'embedded';
        }
    };
    /**
     * 菜单模式变化
     */
    const handleModeChange = () => {
        // 外联模式不需要组件Key
        if (formData.menuMode === 'external') {
            formData.componentKey = undefined;
        }
        else {
            formData.externalUrl = undefined;
        }
    };
    /**
     * 加载菜单树数据
     * 用于构建父菜单选择器的数据源
     */
    const loadMenuTreeData = async (moduleId) => {
        if (!moduleId) {
            return;
        }
        try {
            const tenantId = sessionStorage.getItem('tenantId');
            if (!tenantId) {
                message.warning('租户信息缺失');
                return;
            }
            const response = await getMenuTree({
                tenantId: Number(tenantId),
                moduleId: Number(moduleId)
            });
            allMenus.value = response || [];
            buildParentMenuTree();
        }
        catch (error) {
            console.error('加载菜单树失败:', error);
            // 失败时使用默认的根目录树
            allMenus.value = [];
            buildParentMenuTree();
        }
    };
    /**
     * 递归构建树节点
     */
    const buildTreeNodes = (menus, parentId) => {
        return menus
            .filter(m => String(m.parentId || '0') === String(parentId))
            .sort((a, b) => (a.orderNum || 0) - (b.orderNum || 0))
            .map(menu => ({
            key: String(menu.id),
            title: menu.name,
            value: String(menu.id),
            children: buildTreeNodes(menus, String(menu.id))
        }));
    };
    /**
     * 过滤当前菜单及其子菜单
     * 防止循环引用
     */
    const filterCurrentMenuFromTree = (menus, currentId) => {
        if (!currentId)
            return menus;
        // 收集当前菜单及其所有子菜单的ID
        const excludeIds = new Set();
        const collectChildIds = (id) => {
            excludeIds.add(id);
            menus
                .filter(m => String(m.parentId) === String(id))
                .forEach(child => collectChildIds(String(child.id)));
        };
        collectChildIds(currentId);
        // 过滤掉这些ID
        return menus.filter(m => !excludeIds.has(String(m.id)));
    };
    /**
     * 构建父菜单树
     * 将扁平化的菜单列表转换为树形结构
     * 排除类型为 'button' 的菜单项
     */
    const buildParentMenuTree = () => {
        const rootNode = {
            key: '0',
            title: '根目录',
            value: '0',
            children: []
        };
        // 过滤掉按钮类型的菜单
        const validMenus = allMenus.value.filter(m => m.type !== 'button');
        // 如果是编辑模式，过滤掉当前菜单及其子菜单
        const filteredMenus = mode.value === 'edit'
            ? filterCurrentMenuFromTree(validMenus, formData.id)
            : validMenus;
        // 构建树形结构
        rootNode.children = buildTreeNodes(filteredMenus, '0');
        menuTreeData.value = [rootNode];
    };
    /**
     * 自动计算菜单层级
     * 根据父菜单ID计算当前菜单的层级
     */
    const calculateMenuLevel = (parentId) => {
        // 根目录下的菜单为一级菜单
        if (!parentId || parentId === '0') {
            return 1;
        }
        // 查找父菜单
        const parentMenu = allMenus.value.find(m => String(m.id) === String(parentId));
        if (!parentMenu) {
            return 1;
        }
        // 父菜单层级 + 1
        return (parentMenu.menuLevel || 1) + 1;
    };
    /**
     * 监听父菜单变化，自动计算层级
     */
    watch(() => formData.parentId, (newParentId) => {
        if (newParentId !== undefined) {
            formData.menuLevel = calculateMenuLevel(newParentId);
        }
    });
    /**
     * 监听模块变化，重新加载菜单树
     */
    watch(() => formData.moduleId, (newModuleId) => {
        if (newModuleId) {
            loadMenuTreeData(newModuleId);
        }
    });
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
    };
}
