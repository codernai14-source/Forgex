/**
 * 表单校验翻译 - 中文
 *
 * 包含表单校验相关的提示信息
 */
export default {
  // 必填校验
  required: '此项为必填项',
  requiredField: '{field}不能为空',

  // 格式校验
  invalidEmail: '请输入有效的邮箱地址',
  invalidPhone: '请输入有效的手机号',
  invalidUrl: '请输入有效的网址',
  invalidNumber: '请输入有效的数字',
  invalidInteger: '请输入整数',
  invalidDate: '请输入有效的日期',

  // 长度校验
  minLength: '长度不能少于{min}个字符',
  maxLength: '长度不能超过{max}个字符',
  rangeLength: '长度必须在{min}到{max}个字符之间',

  // 数值校验
  minValue: '值不能小于{min}',
  maxValue: '值不能大于{max}',
  rangeValue: '值必须在{min}到{max}之间',

  // 密码校验
  passwordMismatch: '两次输入的密码不一致',
  passwordWeak: '密码强度太弱',
  passwordRequirement: '密码必须包含大小写字母、数字和特殊字符',

  // 其他校验
  duplicateValue: '该值已存在',
  invalid表单at: '格式不正确',
  selectAtLeastOne: '请至少选择一项',
}
