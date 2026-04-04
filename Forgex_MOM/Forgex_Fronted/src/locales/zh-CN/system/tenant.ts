/**
 * 租户管理翻译 - 中文
 */
export default {
  title: '租户管理',
  list: '租户列表',
  detail: '租户详情',
  
  // 表格列
  tenantId: '租户ID',
  tenantName: '租户名称',
  tenantCode: '租户编码',
  tenantType: '租户类别',
  logo: 'Logo',
  description: '描述',
  status: '状态',
  createTime: '创建时间',
  
  // 表单
  form: {
    addTenant: '新增租户',
    editTenant: '编辑租户',
    tenantName: '请输入租户名称',
    tenantCode: '请输入租户编码',
    tenantType: '请选择租户类别',
    description: '请输入描述',
    status: '请选择状态',
    parentTenant: '请选择父租户',
  },
  
  // 租户类型
  type: {
    main: '主租户',
    customer: '客户租户',
  },
  
  // 父租户
  parentTenant: '父租户',
  parentTenantPlaceholder: '请选择父租户（可选）',
  noParentTenant: '无父租户',
  
  // 消息
  message: {
    deleteConfirm: '确定要删除该租户吗？',
    deleteSuccess: '删除租户成功',
    saveSuccess: '保存租户成功',
    tenantCodeExists: '租户编码已存在',
  },
}
