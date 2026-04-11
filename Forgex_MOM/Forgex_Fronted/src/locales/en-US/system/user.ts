/**
 * User Management Translations - English
 */
export default {
  // Page Titles
  title: 'User Management',
  userManagement: 'User Management',
  list: 'User List',
  detail: 'User Detail',
  
  // Actions
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
    assignedStatus: 'Assignment',
    assigned: 'Assigned',
    summary: '{total} roles in total, {selected} selected',
    loadFailed: 'Failed to load role data',
    saveFailed: 'Save failed',
    missingUserId: 'User ID is missing',
  },

  // Table Columns
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
  status: 'Status',
  createTime: 'Create Time',
  createBy: 'Created By',
  updateTime: 'Update Time',
  updateBy: 'Updated By',
  lastLoginTime: 'Last Login Time',
  lastLoginIp: 'Last Login IP',
  lastLoginRegion: 'Last Login Region',
  action: 'Actions',
  
  // Form
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
  
  // Status
  statusActive: 'Active',
  statusInactive: 'Inactive',
  statusLocked: 'Locked',
  
  // Messages
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
    updateStatusFailed: 'Update status failed',
  },
}
