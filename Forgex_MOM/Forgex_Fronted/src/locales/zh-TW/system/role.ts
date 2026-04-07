/**
 * 角色管理翻譯 - 繁體中文
 */
export default {
  // 頁面標題
  title: '角色管理',
  list: '角色列表',
  detail: '角色詳情',
  
  // 表格列
  roleName: '角色名稱',
  roleCode: '角色編碼',
  description: '描述',
  status: '狀態',
  createTime: '創建時間',
  updateTime: '更新時間',
  
  // 表單
  form: {
    addRole: '新增角色',
    editRole: '編輯角色',
    roleDetail: '角色詳情',
    roleName: '請輸入角色名稱',
    roleCode: '請輸入角色編碼',
    fieldLengthRange: '長度在 1 到 50 個字元',
    description: '請輸入描述',
    status: '請選擇狀態',
  },
  
  // 權限
  permission: {
    title: '權限配置',
    menu: '菜單權限',
    button: '按鈕權限',
    data: '數據權限',
    api: '接口權限',
    selectAll: '全選',
    expandAll: '展開全部',
    collapseAll: '收起全部',
    selectMenu: '請先選擇菜單',
  },
  
  // 狀態
  statusActive: '啟用',
  statusInactive: '禁用',
  
  // 操作
  assignPermission: '分配權限',
  copyRole: '複製角色',
  menuAuth: '菜單授權',
  userAuth: '授權人員',
  
  // 菜單授權
  menuGrant: '菜單授權',
  menuGrantDesc: '為角色分配菜單和按鈕權限',
  moduleFilter: '模塊篩選',
  
  // 人員授權
  userGrant: '人員授權',
  userGrantDesc: '為角色分配用戶、部門、職位權限',
  selectGrantObject: '選擇授權對象',
  selectUser: '選擇用戶',
  selectDepartment: '選擇部門',
  selectPosition: '選擇職位',
  grantedList: '已授權列表',
  grantType: '授權類型',
  grantTypeUser: '用戶',
  grantTypeDepartment: '部門',
  grantTypePosition: '職位',
  grantObject: '授權對象',
  grantTime: '授權時間',
  grantBy: '授權人',
  addToGranted: '添加到已授權',
  batchRevoke: '批量移除',
  revoke: '移除',
  searchMenu: '搜索菜單名稱',
  searchUser: '搜索用戶名/賬號',
  selectAll: '全選',
  selectInvert: '反選',
  clearAll: '清空',
  saveGrant: '保存授權',
  
  // 消息
  message: {
    loadListFailed: '獲取角色列表失敗',
    deleteFailed: '刪除失敗',
    batchDeleteFailed: '批量刪除失敗',
    selectToDelete: '請選擇要刪除的角色',
    deleteConfirm: '確定要刪除該角色嗎？',
    deleteSuccess: '刪除角色成功',
    saveSuccess: '保存角色成功',
    roleCodeExists: '角色編碼已存在',
    assignPermissionSuccess: '分配權限成功',
    missingRoleInfo: '角色信息缺失',
    saveGrantSuccess: '保存授權成功',
    saveGrantFailed: '保存授權失敗',
    loadGrantedFailed: '加載已授權列表失敗',
    selectToGrant: '請選擇要授權的對象',
    grantSuccess: '授權成功',
    grantFailed: '授權失敗',
    selectToRevoke: '請選擇要移除的授權',
    revokeSuccess: '移除授權成功',
    revokeFailed: '移除授權失敗',
  },
}