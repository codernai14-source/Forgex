/**
 * 表单校验翻译 - 中文注释
 * 
 * Contains form validation messages
 */
export default {
  // Required Validation
  required: 'This field is required',
  requiredField: '{field} is required',
  
  // 格式校验
  invalidEmail: 'Please enter a valid email address',
  invalidPhone: 'Please enter a valid phone number',
  invalidUrl: 'Please enter a valid URL',
  invalidNumber: 'Please enter a valid number',
  invalidInteger: 'Please enter an integer',
  invalidDate: 'Please enter a valid date',
  
  // 长度校验
  minLength: 'Length must be at least {min} characters',
  maxLength: 'Length must not exceed {max} characters',
  rangeLength: 'Length must be between {min} and {max} characters',
  
  // 数值校验
  minValue: 'Value must be at least {min}',
  maxValue: 'Value must not exceed {max}',
  rangeValue: 'Value must be between {min} and {max}',
  
  // 密码校验
  passwordMismatch: 'Passwords do not match',
  passwordWeak: 'Password is too weak',
  passwordRequirement: 'Password must contain uppercase, lowercase, numbers and special characters',
  
  // 其他校验
  duplicateValue: 'This value already exists',
  invalid表单at: 'Invalid format',
  selectAtLeastOne: 'Please select at least one item',
}
