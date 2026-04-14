/**
 * 角色管理翻译 - 中文注释
 */
export default {
  // 页面标题
  title: 'Role Management',
  list: 'Role List',
  detail: 'Role Detail',
  
  // 表格列
  roleName: 'Role Name',
  roleCode: 'Role Code',
  description: 'Description',
  status: '状态',
  createTime: 'Create Time',
  updateTime: 'Update Time',
  
  // 表单
  form: {
    addRole: 'Add Role',
    editRole: 'Edit Role',
    roleDetail: 'Role Detail',
    roleName: 'Please enter role name',
    roleCode: 'Please enter role code',
    fieldLengthRange: 'Length must be between 1 and 50 characters',
    description: 'Please enter description',
    status: 'Please select status',
  },
  
  // 权限
  permission: {
    title: '权限 Configuration',
    menu: 'Menu 权限',
    data: 'Data 权限',
    api: 'API 权限',
    selectAll: 'Select All',
    expandAll: 'Expand All',
    collapseAll: 'Collapse All',
  },
  
  // 状态
  statusActive: 'Active',
  statusInactive: 'Inactive',
  
  // 操作
  assign权限: 'Assign 权限',
  copyRole: 'Copy Role',
  menuAuth: 'Menu Authorization',
  userAuth: 'User Authorization',
  
  // 菜单授权
  menuGrant: 'Menu Authorization',
  menuGrantDesc: 'Assign menu and button permissions to role',
  moduleFilter: 'Module Filter',
  
  // 用户授权
  userGrant: 'User Authorization',
  userGrantDesc: 'Assign users, departments, positions to role',
  selectGrantObject: 'Select Grant Object',
  selectUser: 'Select User',
  selectDepartment: 'Select Department',
  selectPosition: 'Select Position',
  grantedList: 'Granted List',
  grantType: 'Grant Type',
  grantTypeUser: 'User',
  grantTypeDepartment: 'Department',
  grantTypePosition: 'Position',
  grantObject: 'Grant Object',
  grantTime: 'Grant Time',
  grantBy: 'Grant By',
  addToGranted: 'Add to Granted',
  batchRevoke: 'Batch Revoke',
  revoke: 'Revoke',
  searchMenu: 'Search Menu Name',
  searchUser: 'Search Username/Account',
  selectAll: 'Select All',
  selectInvert: 'Invert Selection',
  clearAll: 'Clear All',
  saveGrant: 'Save Authorization',
  
  // 消息
  message: {
    loadListFailed: 'Failed to load role list',
    deleteFailed: 'Delete failed',
    batchDeleteFailed: 'Batch delete failed',
    selectToDelete: 'Please select roles to delete',
    deleteConfirm: 'Are you sure you want to delete this role?',
    deleteSuccess: 'Role deleted successfully',
    saveSuccess: 'Role saved successfully',
    roleCodeExists: 'Role code already exists',
    assign权限Success: '权限 assigned successfully',
    missingRoleInfo: 'Role information is missing',
    saveGrantSuccess: 'Authorization saved successfully',
    saveGrantFailed: 'Failed to save authorization',
    loadGrantedFailed: 'Failed to load granted list',
    selectToGrant: 'Please select objects to grant',
    grantSuccess: 'Grant successful',
    grantFailed: 'Grant failed',
    selectToRevoke: 'Please select grants to revoke',
    revokeSuccess: 'Grant revoked successfully',
    revokeFailed: 'Failed to revoke grant',
  },
}
