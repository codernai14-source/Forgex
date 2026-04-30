/**
 * 用户信息 Store
 * 
 * @description 绠＄悊鐢ㄦ埛鐧诲綍鐘舵€併€佺敤鎴蜂俊鎭瓑
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
   * 鏄惁宸茬櫥褰?
   */
  const isLoggedIn = computed(() => !!userInfo.value)
  
  /**
   * 褰撳墠璐﹀彿
   */
  const account = computed(() => userInfo.value?.account || '')
  
  /**
   * 褰撳墠绉熸埛ID
   */
  const tenantId = computed(() => userInfo.value?.tenantId || '')
  
  // ============ 操作 ============
  
  /**
   * 璁剧疆鐢ㄦ埛淇℃伅
   */
  function setUserInfo(info: UserInfo) {
    userInfo.value = info
    
    // 鍚屾鍒?sessionStorage锛堝吋瀹规棫浠ｇ爜锛?
    sessionStorage.setItem('account', info.account)
    sessionStorage.setItem('tenantId', info.tenantId)
  }
  
  /**
   * 更新用户信息
   */
  function updateUserInfo(info: Partial<UserInfo>) {
    if (!userInfo.value) {
      // 濡傛灉涓虹┖锛屽皾璇曚娇鐢?sessionStorage 涓殑鍩烘湰淇℃伅鍒濆鍖?
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
   * 娓呴櫎鐢ㄦ埛淇℃伅
   */
  function clearUserInfo() {
    userInfo.value = null
    
    // 娓呴櫎 sessionStorage
    sessionStorage.removeItem('account')
    sessionStorage.removeItem('tenantId')
  }
  
  /**
   * 浠?sessionStorage 鎭㈠鐢ㄦ埛淇℃伅锛堢敤浜庨〉闈㈠埛鏂帮級
   */
  async function restoreFromSession() {
    const account = sessionStorage.getItem('account')
    const tenantId = sessionStorage.getItem('tenantId')
    
    if (account && tenantId) {
      // 鍏堟仮澶嶅熀鏈俊鎭紝閬垮厤椤甸潰闂儊
      userInfo.value = {
        account,
        username: account, // 榛樿浣跨敤 account 浣滀负 username
        tenantId
      }
      
      // 寮傛鑾峰彇瀹屾暣鐢ㄦ埛淇℃伅锛堝寘鍚ご鍍忕瓑锛?
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
      // 璋冪敤鍚庣鐧诲嚭鎺ュ彛
      await logoutApi()
    } catch (error) {
      console.error('登出接口调用失败:', error)
    } finally {
      // 娓呴櫎鐢ㄦ埛淇℃伅
      clearUserInfo()
    }
  }
  
  // ============ 鍒濆鍖?============
  
  // 页面加载时尝试从 sessionStorage 鎭㈠
  restoreFromSession()
  
  return {
    // State
    userInfo,
    isLoggedIn,
    account,
    tenantId,
    
    // 操作
    setUserInfo,
    updateUserInfo,
    clearUserInfo,
    restoreFromSession,
    logout
  }
}, {
  // 鎸佷箙鍖栭厤缃紙鍙€夛級
  // persist: {
  //   key: 'forgex-user',
  //   storage: sessionStorage,
  //   paths: ['userInfo']
  // }
})
