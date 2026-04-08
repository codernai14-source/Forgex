/**
 * 用户信息 Store
 * 
 * @description 管理用户登录状态、用户信息等
 */

import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getCurrentUserInfo } from '@/api/profile'
import { logout as logoutApi } from '@/api/auth/login'

export interface UserInfo {
  id?: number
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
    if (!userInfo.value) {
      // 如果为空，尝试使用 sessionStorage 中的基本信息初始化
      const account = sessionStorage.getItem('account') || ''
      const tenantId = sessionStorage.getItem('tenantId') || ''
      if (account && tenantId) {
        userInfo.value = {
          account,
          username: account,
          tenantId,
          ...info
        } as UserInfo
      }
    } else {
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
  async function restoreFromSession() {
    const account = sessionStorage.getItem('account')
    const tenantId = sessionStorage.getItem('tenantId')
    
    if (account && tenantId) {
      // 先恢复基本信息，避免页面闪烁
      userInfo.value = {
        account,
        username: account, // 默认使用 account 作为 username
        tenantId
      }
      
      // 异步获取完整用户信息（包含头像等）
      try {
        const res = await getCurrentUserInfo()
        if (res) {
          userInfo.value = {
            ...userInfo.value,
            ...res,
            username: res.username || res.account || account
          }
        }
      } catch (error) {
        console.error('获取用户信息失败:', error)
      }
    }
  }
  
  /**
   * 用户登出
   */
  async function logout() {
    try {
      // 调用后端登出接口
      await logoutApi()
    } catch (error) {
      console.error('登出接口调用失败:', error)
    } finally {
      // 清除用户信息
      clearUserInfo()
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
    restoreFromSession,
    logout
  }
}, {
  // 持久化配置（可选）
  // persist: {
  //   key: 'forgex-user',
  //   storage: sessionStorage,
  //   paths: ['userInfo']
  // }
})
