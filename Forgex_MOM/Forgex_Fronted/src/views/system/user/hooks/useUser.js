/**
 * 用户列表逻辑
 */
import { ref, reactive } from 'vue';
import { message, Modal } from 'ant-design-vue';
import { useI18n } from 'vue-i18n';
import { userApi } from '@/api/system/user';
const { t } = useI18n();
export function useUser() {
    // 加载状态
    const loading = ref(false);
    // 用户列表
    const userList = ref([]);
    // 分页信息
    const pagination = reactive({
        current: 1,
        pageSize: 10,
        total: 0,
    });
    // 查询条件
    const queryForm = reactive({
        username: '',
        phone: '',
        departmentId: undefined,
        positionId: undefined,
        status: undefined,
    });
    // 选中的用户ID列表
    const selectedRowKeys = ref([]);
    /**
     * 获取用户列表
     */
    async function fetchUserList() {
        loading.value = true;
        try {
            const query = {
                ...queryForm,
                pageNum: pagination.current,
                pageSize: pagination.pageSize,
            };
            // http拦截器已经返回了data字段
            const data = await userApi.getUserList(query);
            userList.value = data.records || [];
            pagination.total = data.total || 0;
        }
        catch (error) {
            console.error('获取用户列表失败:', error);
            message.error('获取用户列表失败');
        }
        finally {
            loading.value = false;
        }
    }
    /**
     * 搜索
     */
    function handleSearch() {
        pagination.current = 1;
        fetchUserList();
    }
    /**
     * 重置搜索
     */
    function handleReset() {
        Object.assign(queryForm, {
            username: '',
            phone: '',
            departmentId: undefined,
            positionId: undefined,
            status: undefined,
        });
        pagination.current = 1;
        fetchUserList();
    }
    /**
     * 分页改变
     */
    function handlePageChange(page, pageSize) {
        pagination.current = page;
        pagination.pageSize = pageSize;
        fetchUserList();
    }
    /**
     * 删除用户
     */
    async function handleDelete(id) {
        Modal.confirm({
            title: '确认删除',
            content: '确定要删除该用户吗？',
            onOk: async () => {
                try {
                    await userApi.deleteUser(id);
                    fetchUserList();
                }
                catch (error) {
                    console.error('删除用户失败:', error);
                }
            },
        });
    }
    /**
     * 批量删除用户
     */
    async function handleBatchDelete() {
        if (selectedRowKeys.value.length === 0) {
            message.warning('请选择要删除的用户');
            return;
        }
        Modal.confirm({
            title: '确认删除',
            content: `确定要删除选中的 ${selectedRowKeys.value.length} 个用户吗？`,
            onOk: async () => {
                try {
                    await userApi.batchDeleteUsers(selectedRowKeys.value);
                    selectedRowKeys.value = [];
                    fetchUserList();
                }
                catch (error) {
                    console.error('批量删除失败:', error);
                }
            },
        });
    }
    /**
     * 重置密码
     */
    async function handleResetPassword(id) {
        Modal.confirm({
            title: '确认重置密码',
            content: '确定要重置该用户的密码吗？密码将重置为：123456',
            onOk: async () => {
                try {
                    await userApi.resetPassword(id);
                }
                catch (error) {
                    console.error('密码重置失败:', error);
                }
            },
        });
    }
    /**
     * 更新用户状态
     */
    async function handleUpdateStatus(id, status) {
        const statusText = status ? t('common.enabled') : t('common.disabled');
        Modal.confirm({
            title: `${t('common.confirm')}${statusText}`,
            content: `${t('common.confirm')}${statusText}${t('common.confirmDeleteMessage')}`,
            onOk: async () => {
                try {
                    await userApi.updateUserStatus(id, status);
                    fetchUserList();
                }
                catch (error) {
                    console.error(`${statusText}${t('common.failed')}:`, error);
                }
            },
        });
    }
    /**
     * 选择改变
     */
    function handleSelectionChange(keys) {
        selectedRowKeys.value = keys;
    }
    /**
     * 导出用户
     */
    async function handleExport() {
        try {
            loading.value = true;
            const resp = await userApi.exportUsers(queryForm);
            const blob = new Blob([resp.data], { type: resp.headers?.['content-type'] || 'application/octet-stream' });
            const url = window.URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            a.download = `sys-user-${Date.now()}.xlsx`;
            document.body.appendChild(a);
            a.click();
            document.body.removeChild(a);
            window.URL.revokeObjectURL(url);
        }
        catch (error) {
            console.error('导出失败:', error);
        }
        finally {
            loading.value = false;
        }
    }
    return {
        loading,
        userList,
        pagination,
        queryForm,
        selectedRowKeys,
        fetchUserList,
        handleSearch,
        handleReset,
        handlePageChange,
        handleDelete,
        handleBatchDelete,
        handleResetPassword,
        handleUpdateStatus,
        handleSelectionChange,
        handleExport,
    };
}
