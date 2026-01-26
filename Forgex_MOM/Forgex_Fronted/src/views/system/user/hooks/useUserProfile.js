/**
 * 用户附属信息逻辑
 */
import { reactive } from 'vue';
export function useUserProfile() {
    // 附属信息数据
    const profileData = reactive({
        politicalStatus: '',
        homeAddress: '',
        emergencyContact: '',
        emergencyPhone: '',
        referrer: '',
        education: '',
        workHistory: [],
    });
    /**
     * 添加工作经历
     */
    function addWorkHistory() {
        if (!profileData.workHistory) {
            profileData.workHistory = [];
        }
        profileData.workHistory.push({
            company: '',
            position: '',
            startDate: '',
            endDate: '',
            description: '',
        });
    }
    /**
     * 删除工作经历
     */
    function removeWorkHistory(index) {
        profileData.workHistory?.splice(index, 1);
    }
    /**
     * 重置附属信息
     */
    function resetProfile() {
        Object.assign(profileData, {
            politicalStatus: '',
            homeAddress: '',
            emergencyContact: '',
            emergencyPhone: '',
            referrer: '',
            education: '',
            workHistory: [],
        });
    }
    /**
     * 设置附属信息
     */
    function setProfile(profile) {
        Object.assign(profileData, profile);
    }
    return {
        profileData,
        addWorkHistory,
        removeWorkHistory,
        resetProfile,
        setProfile,
    };
}
