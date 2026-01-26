import { ref, reactive } from 'vue';
import { message } from 'ant-design-vue';
import { getRoleList, deleteRole, batchDeleteRoles } from '@/api/system/role';
export const useRole = () => {
    const loading = ref(false);
    const roles = ref([]);
    const selectedRowKeys = ref([]);
    // 搜索表单
    const searchForm = reactive({
        roleCode: '',
        roleName: '',
        status: undefined
    });
    /**
     * 加载角色列表
     */
    const loadRoles = async () => {
        loading.value = true;
        try {
            // http拦截器已经返回了data字段，不需要再访问res.data
            const data = await getRoleList(searchForm);
            roles.value = data || [];
        }
        catch (error) {
            console.error('加载角色列表失败:', error);
            message.error('加载角色列表失败');
        }
        finally {
            loading.value = false;
        }
    };
    /**
     * 搜索
     */
    const handleSearch = () => {
        loadRoles();
    };
    /**
     * 重置搜索
     */
    const handleReset = () => {
        searchForm.roleCode = '';
        searchForm.roleName = '';
        searchForm.status = undefined;
        loadRoles();
    };
    /**
     * 删除角色
     */
    const handleDelete = async (id) => {
        try {
            await deleteRole(id);
            message.success('删除成功');
            loadRoles();
        }
        catch (error) {
            console.error('删除角色失败:', error);
            message.error('删除失败');
        }
    };
    /**
     * 批量删除
     */
    const handleBatchDelete = async () => {
        if (selectedRowKeys.value.length === 0) {
            message.warning('请选择要删除的角色');
            return;
        }
        try {
            await batchDeleteRoles(selectedRowKeys.value);
            message.success('批量删除成功');
            selectedRowKeys.value = [];
            loadRoles();
        }
        catch (error) {
            console.error('批量删除失败:', error);
            message.error('批量删除失败');
        }
    };
    /**
     * 行选择变化
     */
    const onSelectChange = (keys) => {
        selectedRowKeys.value = keys;
    };
    return {
        loading,
        roles,
        searchForm,
        selectedRowKeys,
        loadRoles,
        handleSearch,
        handleReset,
        handleDelete,
        handleBatchDelete,
        onSelectChange
    };
};
