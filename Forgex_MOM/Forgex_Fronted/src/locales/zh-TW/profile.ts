/**
 * 個人中心頁面文案
 *
 * @author Forgex
 * @version 1.0.0
 */
export default {
  title: '個人信息',
  tabs: {
    basic: '基本信息',
    org: '組織信息',
    security: '安全設置',
    personalHomepage: '個人首頁',
  },
  fields: {
    account: '帳號',
    username: '用戶名',
    email: '郵箱',
    phone: '手機號',
    gender: '性別',
    department: '所屬部門',
    position: '職位',
    entryDate: '入職時間',
    status: '狀態',
    oldPassword: '舊密碼',
    newPassword: '新密碼',
    confirmPassword: '確認密碼',
  },
  gender: {
    male: '男',
    female: '女',
    unknown: '未知',
  },
  actions: {
    save: '保存',
    reset: '重置',
    changePassword: '修改密碼',
  },
  personalHomepage: {
    title: '我的個人首頁',
    description: '這裡保存你自己的首頁佈局，恢復預設後會重新繼承租戶級或公共級配置。',
  },
  validation: {
    oldPasswordRequired: '請輸入舊密碼',
    newPasswordRequired: '請輸入新密碼',
    newPasswordMin: '密碼長度不能少於6位',
    confirmPasswordRequired: '請確認新密碼',
    passwordMismatch: '兩次輸入的密碼不一致',
  },
  message: {
    loadFailed: '加載用戶信息失敗',
    passwordChanged: '密碼修改成功，請重新登入',
    passwordChangeFailed: '密碼修改失敗',
  },
}
