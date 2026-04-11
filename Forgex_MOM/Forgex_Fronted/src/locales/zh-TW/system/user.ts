/**
 * 用戶管理翻譯 - 繁體中文
 */
export default {
  // 頁面標題
  title: '用戶管理',
  userManagement: '用戶管理',
  list: '用戶列表',
  detail: '用戶詳情',
  
  // 表格列
  avatar: '頭像',
  username: '用戶名',
  realName: '真實姓名',
  email: '郵箱',
  phone: '手機號',
  gender: '性別',
  entryDate: '入職時間',
  department: '部門',
  position: '職位',
  role: '角色',
  status: '狀態',
  createTime: '創建時間',
  createBy: '創建人',
  updateTime: '更新時間',
  updateBy: '修改人',
  lastLoginTime: '最後登入時間',
  lastLoginIp: '最後登入IP',
  lastLoginRegion: '最後登入地區',
  action: '操作',
  
  // 性別
  genderOptions: {
    unknown: '未知',
    male: '男',
    female: '女',
  },
  
  // 表單
  form: {
    addUser: '新增用戶',
    editUser: '編輯用戶',
    userDetail: '用戶詳情',
    username: '請輸入用戶名',
    realName: '請輸入真實姓名',
    email: '請輸入郵箱',
    phone: '請輸入手機號',
    password: '請輸入密碼',
    confirmPassword: '請再次輸入密碼',
    gender: '請選擇性別',
    entryDate: '請選擇入職時間',
    department: '請選擇部門',
    position: '請選擇職位',
    role: '請選擇角色',
    status: '請選擇狀態',
  },
  
  // 標籤頁
  tabs: {
    basic: '基本信息',
    profile: '附屬信息',
  },
  
  // 附屬信息
  profile: {
    politicalStatus: '政治面貌',
    homeAddress: '家庭住址',
    emergencyContact: '緊急聯繫人',
    emergencyPhone: '緊急聯繫人電話',
    referrer: '引荐人',
    education: '學歷',
    workHistory: '工作經歷',
    company: '公司名稱',
    position: '職位',
    startDate: '開始時間',
    endDate: '結束時間',
    description: '工作描述',
    addWorkHistory: '添加工作經歷',
  },
  
  // 狀態
  statusActive: '啟用',
  statusInactive: '禁用',
  statusLocked: '鎖定',
  
  // 操作
  add: '新增用戶',
  edit: '編輯',
  delete: '刪除',
  export: '導出',
  resetPassword: '重置密碼',
  lockUser: '鎖定用戶',
  unlockUser: '解鎖用戶',
  assignRole: '分配角色',
  viewDetail: '查看詳情',
  
  // 分配角色彈窗
  roleAssign: {
    searchPlaceholder: '搜尋角色名稱或編碼',
    selectAll: '全選',
    clearAll: '清空',
    roleName: '角色名稱',
    roleCode: '角色編碼',
    assignedStatus: '分配狀態',
    assigned: '已分配',
    summary: '共 {total} 個角色，已選 {selected} 個',
    loadFailed: '載入角色資料失敗',
    saveFailed: '保存失敗',
    missingUserId: '缺少用戶ID',
  },

  // 消息
  message: {
    loadListFailed: '獲取用戶列表失敗',
    deleteFailed: '刪除失敗',
    batchDeleteFailed: '批量刪除失敗',
    selectToDelete: '請選擇要刪除的用戶',
    deleteConfirm: '確定要刪除該用戶嗎？',
    batchDeleteConfirm: '確定要刪除選中的 {count} 個用戶嗎？',
    deleteSuccess: '刪除用戶成功',
    addSuccess: '新增用戶成功',
    editSuccess: '編輯用戶成功',
    saveSuccess: '保存用戶成功',
    resetPasswordSuccess: '重置密碼成功',
    resetPasswordFailed: '重置密碼失敗',
    resetPasswordConfirm: '確定要重置該用戶的密碼嗎？密碼將重置為系統配置中的默認密碼。',
    lockSuccess: '鎖定用戶成功',
    unlockSuccess: '解鎖用戶成功',
    exportSuccess: '導出成功',
    usernameExists: '用戶名已存在',
    emailExists: '郵箱已存在',
    updateStatusFailed: '更新狀態失敗',
  },
}