/**
 * 用户附属信息逻辑
 */
import { ref, reactive } from 'vue'
import type { UserProfile, WorkHistory } from '../types'

export function useUserProfile() {
  // 附属信息数据
  const profileData = reactive<Partial<UserProfile>>({
    political状态: '',
    homeAddress: '',
    emergencyContact: '',
    emergencyPhone: '',
    referrer: '',
    education: '',
    workHistory: [],
  })
  
  /**
   * 添加工作经历
   */
  function addWorkHistory() {
    if (!profileData.workHistory) {
      profileData.workHistory = []
    }
    profileData.workHistory.push({
      company: '',
      position: '',
      startDate: '',
      endDate: '',
      description: '',
    })
  }
  
  /**
   * 删除工作经历
   */
  function removeWorkHistory(index: number) {
    profileData.workHistory?.splice(index, 1)
  }
  
  /**
   * 重置附属信息
   */
  function resetProfile() {
    Object.assign(profileData, {
      political状态: '',
      homeAddress: '',
      emergencyContact: '',
      emergencyPhone: '',
      referrer: '',
      education: '',
      workHistory: [],
    })
  }
  
  /**
   * 设置附属信息
   */
  function setProfile(profile: Partial<UserProfile>) {
    Object.assign(profileData, profile)
  }
  
  return {
    profileData,
    addWorkHistory,
    removeWorkHistory,
    resetProfile,
    setProfile,
  }
}
