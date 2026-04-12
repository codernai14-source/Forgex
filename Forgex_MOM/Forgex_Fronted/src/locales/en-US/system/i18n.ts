/**
 * 国际化配置翻译 - 中文注释
 */
export default {
  // Page Title
  languageType: 'Language Configuration',
  encodeRule: 'Encoding Rule Management',
  
  // 表格列
  langCode: 'Language Code',
  langName: 'Language Name',
  langNameEn: 'English Name',
  icon: 'Icon',
  orderNum: 'Order',
  enabled: 'Enabled',
  isDefault: 'Default Language',
  defaultLang: 'Default Language',
  ruleName: 'Rule Name',
  ruleCode: 'Rule Code',
  businessType: 'Business Type',
  description: 'Description',
  status: '状态',
  exampleCode: 'Example Code',
  createTime: 'Create Time',
  updateTime: 'Update Time',
  remark: 'Remark',
  ruleDetails: 'Rule Details',
  detailSegment: 'Segment',
  segmentType: 'Segment Type',
  segmentValue: 'Segment Value',
  date表单at: 'Date 表单at',
  seqStart: 'Start Value',
  seqLength: 'Length',
  seqResetType: 'Reset Type',
  
  // 表单占位提示
  langCodePlaceholder: 'Enter language code, e.g., zh-CN',
  langNamePlaceholder: 'Enter language name, e.g., Simplified Chinese',
  langNameEnPlaceholder: 'Enter English name, e.g., Simplified Chinese',
  iconPlaceholder: 'Enter icon name',
  ruleNamePlaceholder: 'Enter rule name',
  ruleCodePlaceholder: 'Enter rule code (letters and numbers)',
  businessTypePlaceholder: 'Enter business type (e.g., ORDER, INVOICE)',
  descriptionPlaceholder: 'Enter rule description',
  remarkPlaceholder: 'Enter remark',
  date表单atPlaceholder: 'Enter date format (e.g., YYYYMMDD)',
  
  // 表单校验
  langCodeRequired: 'Please enter language code',
  langNameRequired: 'Please enter language name',
  ruleNameRequired: 'Please enter rule name',
  ruleNameMaxLength: 'Rule name max 50 characters',
  ruleCodeRequired: 'Please enter rule code',
  ruleCodeMaxLength: 'Rule code max 50 characters',
  ruleCodePattern: 'Rule code can only contain letters, numbers and underscores',
  segmentTypeRequired: 'Please select segment type',
  segmentValueRequired: 'Please enter segment value',
  date表单atRequired: 'Please enter date format',
  seqStartRequired: 'Please enter start value',
  seqLengthRequired: 'Please enter sequence length',
  detailRequired: 'Please add at least one rule detail',
  detailOrderError: 'Rule detail order must be consecutive',
  
  // 状态
  statusActive: 'Active',
  statusInactive: 'Inactive',
  segmentTypeFixed: 'Fixed',
  segmentTypeDate: 'Date',
  segmentTypeSeq: 'Sequence',
  segmentTypeCustom: 'Custom',
  seqResetNever: 'Never',
  seqResetYearly: 'Yearly',
  seqResetMonthly: 'Monthly',
  seqResetDaily: 'Daily',
  
  // 操作
  add: 'Add',
  edit: 'Edit',
  delete: 'Delete',
  test: 'Test',
  generate: 'Generate Code',
  addDetail: 'Add Detail',
  setDefault: 'Set as Default',
  confirmSetDefault: 'Are you sure you want to set this language as default?',
  testNotImplemented: 'Test function not implemented',
  generateSuccess: 'Generate code success',
  generateFailed: 'Generate code failed',
  updateSuccess: 'Update success',
  addSuccess: 'Add success',
  loadDetailFailed: 'Load detail failed',
  
  // 消息
  message: {
    deleteConfirm: 'Are you sure you want to delete this encoding rule?',
    batchDeleteConfirm: 'Are you sure you want to batch delete selected encoding rules?',
  },
}
