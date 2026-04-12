/**
 * 用户管理翻译 - 中文
 */
export default {
  // 页面标题
  title: '用户管理',
  userManagement: '用户管理',
  list: '用户列表',
  detail: '用户详情',

  // 表格列
  avatar: '头像',
  username: '用户名',
  realName: '真实姓名',
  email: '邮箱',
  phone: '手机号',
  gender: '性别',
  entryDate: '入职时间',
  department: '部门',
  position: '岗位',
  role: '角色',
  status: '状态',
  createTime: '创建时间',
  createBy: '创建人',
  updateTime: '更新时间',
  updateBy: '更新人',
  lastLoginTime: '最后登录时间',
  lastLoginIp: '最后登录 IP',
  lastLoginRegion: '最后登录地区',
  action: '操作',

  // 性别
  genderOptions: {
    unknown: '未知',
    male: '男',
    female: '女',
  },

  // 表单
  form: {
    addUser: '新增用户',
    editUser: '编辑用户',
    userDetail: '用户详情',
    username: '请输入用户名',
    realName: '请输入真实姓名',
    email: '请输入邮箱',
    phone: '请输入手机号',
    password: '请输入密码',
    confirmPassword: '请再次输入密码',
    gender: '请选择性别',
    entryDate: '请选择入职时间',
    department: '请选择部门',
    position: '请选择岗位',
    role: '请选择角色',
    status: '请选择状态',
  },

  // 标签页
  tabs: {
    basic: '基本信息',
    profile: '附属信息',
  },

  // 附属信息
  profile: {
    political状态: '政治面貌',
    homeAddress: '家庭住址',
    emergencyContact: '紧急联系人',
    emergencyPhone: '紧急联系人电话',
    referrer: '推荐人',
    education: '学历',
    workHistory: '工作经历',
    company: '公司名称',
    position: '岗位',
    startDate: '开始时间',
    endDate: '结束时间',
    description: '工作描述',
    addWorkHistory: '添加工作经历',
  },

  // 状态
  statusActive: '启用',
  statusInactive: '禁用',
  statusLocked: '锁定',

  // 操作
  add: '新增用户',
  edit: '编辑',
  delete: '删除',
  export: '导出',
  resetPassword: '重置密码',
  lockUser: '锁定用户',
  unlockUser: '解锁用户',
  assignRole: '分配角色',
  viewDetail: '查看详情',

  // 分配角色弹窗
  roleAssign: {
    searchPlaceholder: '搜索角色名称或编码',
    selectAll: '全选',
    clearAll: '清空',
    roleName: '角色名称',
    roleCode: '角色编码',
    assigned状态: '分配状态',
    assigned: '已分配',
    summary: '共 {total} 个角色，已选 {selected} 个',
    loadFailed: '加载角色数据失败',
    saveFailed: '保存失败',
    missingUserId: '缺少用户 ID',
  },

  // 消息
  message: {
    loadListFailed: '获取用户列表失败',
    deleteFailed: '删除失败',
    batchDeleteFailed: '批量删除失败',
    selectToDelete: '请选择要删除的用户',
    deleteConfirm: '确定要删除该用户吗？',
    batchDeleteConfirm: '确定要删除选中的 {count} 个用户吗？',
    deleteSuccess: '删除用户成功',
    addSuccess: '新增用户成功',
    editSuccess: '编辑用户成功',
    saveSuccess: '保存用户成功',
    resetPasswordSuccess: '重置密码成功',
    resetPasswordFailed: '重置密码失败',
    resetPasswordConfirm: '确定要重置该用户的密码吗？密码将重置为系统默认密码。',
    lockSuccess: '锁定用户成功',
    unlockSuccess: '解锁用户成功',
    exportSuccess: '导出成功',
    usernameExists: '用户名已存在',
    emailExists: '邮箱已存在',
    update状态Failed: '更新状态失败',
  },
}
