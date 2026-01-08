/**
 * 用户信息 Store
 * 
 * @description 管理用户登录状态、用户信息等
 */

import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export interface UserInfo {
  account: string
  username: string
  email?: string
  phone?: string
  avatar?: string
  tenantId: string
  tenantName?: string
}

export const useUserStore = defineStore('user', () => {
  // ============ State ============
  
  /**
   * 用户信息
   */
  const userInfo = ref<UserInfo | null>(null)
  
  /**
   * 是否已登录
   */
  const isLoggedIn = computed(() => !!userInfo.value)
  
  /**
   * 当前账号
   */
  const account = computed(() => userInfo.value?.account || '')
  
  /**
   * 当前租户ID
   */
  const tenantId = computed(() => userInfo.value?.tenantId || '')
  
  // ============ Actions ============
  
  /**
   * 设置用户信息
   */
  function setUserInfo(info: UserInfo) {
    userInfo.value = info
    
    // 同步到 sessionStorage（兼容旧代码）
    sessionStorage.setItem('account', info.account)
    sessionStorage.setItem('tenantId', info.tenantId)
  }
  
  /**
   * 更新用户信息
   */
  function updateUserInfo(info: Partial<UserInfo>) {
    if (userInfo.value) {
      userInfo.value = { ...userInfo.value, ...info }
    }
  }
  
  /**
   * 清除用户信息
   */
  function clearUserInfo() {
    userInfo.value = null
    
    // 清除 sessionStorage
    sessionStorage.removeItem('account')
    sessionStorage.removeItem('tenantId')
  }
  
  /**
   * 从 sessionStorage 恢复用户信息（用于页面刷新）
   */
  function restoreFromSession() {
    const account = sessionStorage.getItem('account')
    const tenantId = sessionStorage.getItem('tenantId')
    
    if (account && tenantId) {
      userInfo.value = {
        account,
        username: account, // 默认使用 account 作为 username
        tenantId
      }
    }
  }
  
  // ============ 初始化 ============
  
  // 页面加载时尝试从 sessionStorage 恢复
  restoreFromSession()
  
  return {
    // State
    userInfo,
    isLoggedIn,
    account,
    tenantId,
    
    // Actions
    setUserInfo,
    updateUserInfo,
    clearUserInfo,
    restoreFromSession
  }
}, {
  // 持久化配置（可选）
  // persist: {
  //   key: 'forgex-user',
  //   storage: sessionStorage,
  //   paths: ['userInfo']
  // }
})
