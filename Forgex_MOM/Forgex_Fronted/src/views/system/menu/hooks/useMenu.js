/**
 * 菜单列表逻辑Hook
 */
import { ref, reactive } from 'vue';
import { message, Modal } from 'ant-design-vue';
import { getMenuTree, deleteMenu, batchDeleteMenus } from '@/api/system/menu';
/**
 * 构建树形结构
 */
function buildTree(list, parentId = '0') {
    const result = [];
    for (const item of list) {
        if (String(item.parentId || '0') === String(parentId)) {
            const children = buildTree(list, item.id);
            if (children.length > 0) {
                item.children = children;
            }
            result.push(item);
        }
    }
    return result.sort((a, b) => (a.orderNum || 0) - (b.orderNum || 0));
}
export function useMenu() {
    // 加载状态
    const loading = ref(false);
    // 菜单列表（树形结构）
    const menuList = ref([]);
    // 查询参数
    const queryParams = reactive({
        moduleId: undefined,
        name: undefined,
        status: undefined
    });
    // 选中的菜单ID列表
    const selectedRowKeys = ref([]);
    /**
     * 加载菜单列表
     */
    const loadMenuList = async () => {
        try {
            loading.value = true;
            const tenantId = sessionStorage.getItem('tenantId');
            if (!tenantId) {
                message.error('租户信息缺失');
                return;
            }
            const params = {
                tenantId,
                moduleId: queryParams.moduleId
            };
            if (queryParams.name) {
                params.name = queryParams.name;
            }
            if (queryParams.status !== undefined) {
                params.status = queryParams.status;
            }
            const response = await getMenuTree(params);
            const flatList = response || [];
            // 构建树形结构
            menuList.value = buildTree(flatList);
        }
        catch (error) {
            console.error('加载菜单列表失败:', error);
            message.error('加载菜单列表失败');
        }
        finally {
            loading.value = false;
        }
    };
    /**
     * 搜索
     */
    const handleSearch = () => {
        loadMenuList();
    };
    /**
     * 重置搜索
     */
    const handleReset = () => {
        queryParams.moduleId = undefined;
        queryParams.name = undefined;
        queryParams.status = undefined;
        loadMenuList();
    };
    /**
     * 删除菜单
     */
    const handleDelete = async (id) => {
        Modal.confirm({
            title: '确认删除',
            content: '确定要删除该菜单吗？删除后将同时删除其子菜单和按钮权限。',
            okText: '确定',
            cancelText: '取消',
            onOk: async () => {
                try {
                    await deleteMenu(id);
                    message.success('删除成功');
                    loadMenuList();
                }
                catch (error) {
                    console.error('删除菜单失败:', error);
                    message.error('删除菜单失败');
                }
            }
        });
    };
    /**
     * 批量删除
     */
    const handleBatchDelete = () => {
        if (selectedRowKeys.value.length === 0) {
            message.warning('请选择要删除的菜单');
            return;
        }
        Modal.confirm({
            title: '确认删除',
            content: `确定要删除选中的 ${selectedRowKeys.value.length} 个菜单吗？`,
            okText: '确定',
            cancelText: '取消',
            onOk: async () => {
                try {
                    await batchDeleteMenus(selectedRowKeys.value);
                    message.success('批量删除成功');
                    selectedRowKeys.value = [];
                    loadMenuList();
                }
                catch (error) {
                    console.error('批量删除失败:', error);
                    message.error('批量删除失败');
                }
            }
        });
    };
    /**
     * 选择变化
     */
    const handleSelectionChange = (keys) => {
        selectedRowKeys.value = keys;
    };
    return {
        loading,
        menuList,
        queryParams,
        selectedRowKeys,
        loadMenuList,
        handleSearch,
        handleReset,
        handleDelete,
        handleBatchDelete,
        handleSelectionChange
    };
}
