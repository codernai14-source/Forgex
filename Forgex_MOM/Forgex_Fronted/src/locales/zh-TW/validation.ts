/**
 * 表單校驗翻譯 - 繁體中文
 *
 * 包含表單校驗相關的提示資訊
 */
export default {
  // 必填校驗
  required: '此欄為必填項',
  requiredField: '{field}不能為空',

  // 格式校驗
  invalidEmail: '請輸入有效的電子郵件地址',
  invalidPhone: '請輸入有效的手機號碼',
  invalidUrl: '請輸入有效的網址',
  invalidNumber: '請輸入有效的數字',
  invalidInteger: '請輸入整數',
  invalidDate: '請輸入有效的日期',

  // 長度校驗
  minLength: '長度不能少於{min}個字元',
  maxLength: '長度不能超過{max}個字元',
  rangeLength: '長度必須介於{min}到{max}個字元之間',

  // 數值校驗
  minValue: '值不能小於{min}',
  maxValue: '值不能大於{max}',
  rangeValue: '值必須介於{min}到{max}之間',

  // 密碼校驗
  passwordMismatch: '兩次輸入的密碼不一致',
  passwordWeak: '密碼強度太弱',
  passwordRequirement: '密碼必須包含大小寫字母、數字和特殊字元',

  // 其他校驗
  duplicateValue: '該值已存在',
  invalid表单at: '格式不正確',
  selectAtLeastOne: '請至少選擇一項',
}