/**
 * 缺省頁面翻譯 - 繁體中文
 */
export default {
  actions: {
    home: '返回首頁',
    back: '返回上一頁',
    retry: '重新連線',
  },
  panel: {
    title: '狀態檢查',
  },
  '403': {
    eyebrow: '權限驗證未通過',
    title: '存取被拒絕',
    description: '目前帳號沒有存取此功能的權限。請確認角色授權、租戶範圍，或聯絡管理員開通對應選單與按鈕權限。',
    status: '存取鏈路已攔截',
    checks: {
      permission: '檢查選單與按鈕權限是否已授權',
      role: '確認目前角色是否包含目標功能',
      tenant: '確認目前租戶是否允許存取此模組',
    },
  },
  '404': {
    eyebrow: '路由未匹配',
    title: '頁面不存在',
    description: '找不到您要存取的頁面。連結可能已變更、選單尚未發佈，或目前模組路由尚未完成設定。',
    status: '未發現有效頁面',
    checks: {
      route: '檢查存取位址是否輸入正確',
      menu: '確認選單設定是否已發佈並啟用',
      link: '從首頁或模組選單重新進入目標頁面',
    },
  },
  offline: {
    eyebrow: '網路連線異常',
    title: '暫時無法連線服務',
    description: '目前用戶端無法連線到 Forgex 服務。請檢查網路、閘道或 VPN 狀態，恢復後重新連線即可繼續操作。',
    status: '連線等待恢復',
    checks: {
      network: '檢查本機網路與企業內網連線',
      gateway: '確認介面閘道或後端服務是否可用',
      retry: '恢復連線後點選重新連線重新整理頁面',
    },
  },
}
