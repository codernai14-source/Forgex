/**
 * 角色管理翻译 - 中文
 */
export default {
  // 页面标题
  title: '角色管理',
  list: '角色列表',
  detail: '角色详情',
  
  // 表格列
  roleName: '角色名称',
  roleCode: '角色编码',
  description: '描述',
  status: '状态',
  createTime: '创建时间',
  updateTime: '更新时间',
  
  // 表单
  form: {
    addRole: '新增角色',
    editRole: '编辑角色',
    roleDetail: '角色详情',
    roleName: '请输入角色名称',
    roleCode: '请输入角色编码',
    fieldLengthRange: '长度在 1 到 50 个字符',
    description: '请输入描述',
    status: '请选择状态',
  },
  
  // 权限
  permission: {
    title: '权限配置',
    menu: '菜单权限',
    button: '按钮权限',
    data: '数据权限',
    api: '接口权限',
    selectAll: '全选',
    expandAll: '展开全部',
    collapseAll: '收起全部',
    selectMenu: '请先选择菜单',
  },
  
  // 状态
  statusActive: '启用',
  statusInactive: '禁用',
  
  // 操作
  assignPermission: '分配权限',
  copyRole: '复制角色',
  menuAuth: '菜单授权',
  userAuth: '授权人员',
  
  // 菜单授权
  menuGrant: '菜单授权',
  menuGrantDesc: '为角色分配菜单和按钮权限',
  moduleFilter: '模块筛选',
  
  // 人员授权
  userGrant: '人员授权',
  userGrantDesc: '为角色分配用户、部门、职位权限',
  selectGrantObject: '选择授权对象',
  selectUser: '选择用户',
  selectDepartment: '选择部门',
  selectPosition: '选择职位',
  grantedList: '已授权列表',
  grantType: '授权类型',
  grantTypeUser: '用户',
  grantTypeDepartment: '部门',
  grantTypePosition: '职位',
  grantObject: '授权对象',
  grantTime: '授权时间',
  grantBy: '授权人',
  addToGranted: '添加到已授权',
  batchRevoke: '批量移除',
  revoke: '移除',
  searchMenu: '搜索菜单名称',
  searchUser: '搜索用户名/账号',
  selectAll: '全选',
  selectInvert: '反选',
  clearAll: '清空',
  saveGrant: '保存授权',
  
  // 消息
  message: {
    loadListFailed: '获取角色列表失败',
    deleteFailed: '删除失败',
    batchDeleteFailed: '批量删除失败',
    selectToDelete: '请选择要删除的角色',
    deleteConfirm: '确定要删除该角色吗？',
    deleteSuccess: '删除角色成功',
    saveSuccess: '保存角色成功',
    roleCodeExists: '角色编码已存在',
    assignPermissionSuccess: '分配权限成功',
    missingRoleInfo: '角色信息缺失',
    saveGrantSuccess: '保存授权成功',
    saveGrantFailed: '保存授权失败',
    loadGrantedFailed: '加载已授权列表失败',
    selectToGrant: '请选择要授权的对象',
    grantSuccess: '授权成功',
    grantFailed: '授权失败',
    selectToRevoke: '请选择要移除的授权',
    revokeSuccess: '移除授权成功',
    revokeFailed: '移除授权失败',
  },
}
