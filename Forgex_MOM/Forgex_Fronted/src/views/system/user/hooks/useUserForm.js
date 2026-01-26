/**
 * 用户表单逻辑
 */
import { ref, reactive } from 'vue';
import { userApi } from '@/api/system/user';
export function useUserForm(onSuccess) {
    // 弹窗显示状态
    const dialogVisible = ref(false);
    // 加载状态
    const loading = ref(false);
    // 是否编辑模式
    const isEdit = ref(false);
    // 表单数据
    const formData = reactive({
        username: '',
        email: '',
        phone: '',
        gender: 0,
        entryDate: '',
        departmentId: '',
        positionId: '',
        status: true,
    });
    // 部门列表
    const departmentList = ref([]);
    // 职位列表
    const positionList = ref([]);
    /**
     * 获取部门列表
     */
    async function fetchDepartmentList() {
        try {
            const data = await userApi.getDepartmentList();
            departmentList.value = data || [];
        }
        catch (error) {
            console.error('获取部门列表失败:', error);
        }
    }
    /**
     * 获取职位列表
     */
    async function fetchPositionList() {
        try {
            const data = await userApi.getPositionList();
            positionList.value = data || [];
        }
        catch (error) {
            console.error('获取职位列表失败:', error);
        }
    }
    /**
     * 打开新增弹窗
     */
    function openAddDialog() {
        isEdit.value = false;
        Object.assign(formData, {
            username: '',
            email: '',
            phone: '',
            gender: 0,
            entryDate: '',
            departmentId: '',
            positionId: '',
            status: true,
        });
        dialogVisible.value = true;
        fetchDepartmentList();
        fetchPositionList();
    }
    /**
     * 打开编辑弹窗
     */
    async function openEditDialog(user) {
        isEdit.value = true;
        Object.assign(formData, user);
        dialogVisible.value = true;
        fetchDepartmentList();
        fetchPositionList();
    }
    /**
     * 提交表单
     */
    async function handleSubmit() {
        loading.value = true;
        try {
            await (isEdit.value
                ? userApi.updateUser(formData)
                : userApi.addUser(formData));
            dialogVisible.value = false;
            onSuccess();
        }
        catch (error) {
            console.error('提交失败:', error);
        }
        finally {
            loading.value = false;
        }
    }
    /**
     * 取消
     */
    function handleCancel() {
        dialogVisible.value = false;
    }
    return {
        dialogVisible,
        loading,
        isEdit,
        formData,
        departmentList,
        positionList,
        openAddDialog,
        openEditDialog,
        handleSubmit,
        handleCancel,
    };
}
