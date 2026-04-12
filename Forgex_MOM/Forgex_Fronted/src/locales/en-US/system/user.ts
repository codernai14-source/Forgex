/**
 * 用户管理翻译 - 中文注释
 */
export default {
  // 页面标题
  title: 'User Management',
  userManagement: 'User Management',
  list: 'User List',
  detail: 'User Detail',
  
  // 操作
  add: 'Add User',
  edit: 'Edit',
  delete: 'Delete',
  export: 'Export',
  resetPassword: 'Reset Password',
  lockUser: 'Lock User',
  unlockUser: 'Unlock User',
  assignRole: 'Assign Role',
  viewDetail: 'View Detail',

  // Role Assignment Dialog
  roleAssign: {
    searchPlaceholder: 'Search role name or code',
    selectAll: 'Select All',
    clearAll: 'Clear All',
    roleName: 'Role Name',
    roleCode: 'Role Code',
    assigned状态: 'Assignment',
    assigned: 'Assigned',
    summary: '{total} roles in total, {selected} selected',
    loadFailed: 'Failed to load role data',
    saveFailed: 'Save failed',
    missingUserId: 'User ID is missing',
  },

  // 表格列
  avatar: 'Avatar',
  username: 'Username',
  realName: 'Real Name',
  email: 'Email',
  phone: 'Phone',
  gender: 'Gender',
  department: 'Department',
  position: 'Position',
  role: 'Role',
  entryDate: 'Entry Date',
  status: '状态',
  createTime: 'Create Time',
  createBy: 'Created By',
  updateTime: 'Update Time',
  updateBy: 'Updated By',
  lastLoginTime: 'Last Login Time',
  lastLoginIp: 'Last Login IP',
  lastLoginRegion: 'Last Login Region',
  action: '操作',
  
  // 表单
  form: {
    addUser: 'Add User',
    editUser: 'Edit User',
    userDetail: 'User Detail',
    username: 'Please enter username',
    realName: 'Please enter real name',
    email: 'Please enter email',
    phone: 'Please enter phone number',
    password: 'Please enter password',
    confirmPassword: 'Please confirm password',
    department: 'Please select department',
    role: 'Please select role',
    status: 'Please select status',
  },
  
  // 状态
  statusActive: 'Active',
  statusInactive: 'Inactive',
  statusLocked: 'Locked',
  
  // 消息
  message: {
    loadListFailed: 'Failed to load user list',
    deleteFailed: 'Delete failed',
    batchDeleteFailed: 'Batch delete failed',
    selectToDelete: 'Please select users to delete',
    deleteConfirm: 'Are you sure you want to delete this user?',
    batchDeleteConfirm: 'Are you sure you want to delete {count} selected users?',
    deleteSuccess: 'User deleted successfully',
    saveSuccess: 'User saved successfully',
    resetPasswordSuccess: 'Password reset successfully',
    resetPasswordFailed: 'Password reset failed',
    resetPasswordConfirm: 'Are you sure you want to reset this password? It will be reset to the system default password.',
    lockSuccess: 'User locked successfully',
    unlockSuccess: 'User unlocked successfully',
    usernameExists: 'Username already exists',
    emailExists: 'Email already exists',
    update状态Failed: 'Update status failed',
  },
}
