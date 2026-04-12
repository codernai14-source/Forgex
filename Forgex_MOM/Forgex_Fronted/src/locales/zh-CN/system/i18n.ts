/**
 * 国际化配置翻译 - 中文
 */
export default {
  // 页面标题
  languageType: '语言配置',
  encodeRule: '编码规则管理',

  // 表格列
  langCode: '语言代码',
  langName: '语言名称',
  langNameEn: '英文名称',
  icon: '图标',
  orderNum: '排序号',
  enabled: '是否启用',
  isDefault: '默认语言',
  defaultLang: '默认语言',
  ruleName: '规则名称',
  ruleCode: '规则编码',
  businessType: '业务类型',
  description: '规则描述',
  status: '状态',
  exampleCode: '示例编码',
  createTime: '创建时间',
  updateTime: '更新时间',
  remark: '备注',
  ruleDetails: '规则明细',
  detailSegment: '段',
  segmentType: '段类型',
  segmentValue: '段值',
  date表单at: '日期格式',
  seqStart: '起始值',
  seqLength: '长度',
  seqResetType: '重置方式',

  // 表单占位提示
  langCodePlaceholder: '请输入语言代码，例如：zh-CN',
  langNamePlaceholder: '请输入语言名称，例如：简体中文',
  langNameEnPlaceholder: '请输入英文名称，例如：Simplified Chinese',
  iconPlaceholder: '请输入图标名称',
  ruleNamePlaceholder: '请输入规则名称',
  ruleCodePlaceholder: '请输入规则编码（字母和数字）',
  businessTypePlaceholder: '请输入业务类型（例如：ORDER、INVOICE）',
  descriptionPlaceholder: '请输入规则描述',
  remarkPlaceholder: '请输入备注',
  date表单atPlaceholder: '请输入日期格式（例如：YYYYMMDD）',

  // 表单校验
  langCodeRequired: '请输入语言代码',
  langNameRequired: '请输入语言名称',
  ruleNameRequired: '请输入规则名称',
  ruleNameMaxLength: '规则名称最多 50 个字符',
  ruleCodeRequired: '请输入规则编码',
  ruleCodeMaxLength: '规则编码最多 50 个字符',
  ruleCodePattern: '规则编码只能包含字母、数字和下划线',
  segmentTypeRequired: '请选择段类型',
  segmentValueRequired: '请输入段值',
  date表单atRequired: '请输入日期格式',
  seqStartRequired: '请输入起始值',
  seqLengthRequired: '请输入序列长度',
  detailRequired: '请至少添加一条规则明细',
  detailOrderError: '规则明细序号必须连续',

  // 状态
  statusActive: '启用',
  statusInactive: '禁用',
  segmentTypeFixed: '固定值',
  segmentTypeDate: '日期',
  segmentTypeSeq: '序列号',
  segmentTypeCustom: '自定义',
  seqResetNever: '不重置',
  seqResetYearly: '按年重置',
  seqResetMonthly: '按月重置',
  seqResetDaily: '按日重置',

  // 操作
  add: '新增',
  edit: '编辑',
  delete: '删除',
  test: '测试',
  generate: '生成编码',
  addDetail: '添加明细',
  setDefault: '设为默认',
  confirmSetDefault: '确定要将该语言设为默认语言吗？',
  testNotImplemented: '测试功能暂未实现',
  generateSuccess: '生成编码成功',
  generateFailed: '生成编码失败',
  updateSuccess: '更新成功',
  addSuccess: '新增成功',
  loadDetailFailed: '加载详情失败',

  // 消息提示
  message: {
    deleteConfirm: '确定要删除该编码规则吗？',
    batchDeleteConfirm: '确定要批量删除选中的编码规则吗？',
  },
}
