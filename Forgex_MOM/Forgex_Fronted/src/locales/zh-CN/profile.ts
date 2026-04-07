/**
 * 个人中心页面文案
 *
 * @author Forgex
 * @version 1.0.0
 */
export default {
  title: '个人信息',
  tabs: {
    basic: '基本信息',
    org: '组织信息',
    security: '安全设置',
    personalHomepage: '个人首页',
  },
  fields: {
    account: '账号',
    username: '用户名',
    email: '邮箱',
    phone: '手机号',
    gender: '性别',
    department: '所属部门',
    position: '职位',
    entryDate: '入职时间',
    status: '状态',
    oldPassword: '旧密码',
    newPassword: '新密码',
    confirmPassword: '确认密码',
  },
  gender: {
    male: '男',
    female: '女',
    unknown: '未知',
  },
  actions: {
    save: '保存',
    reset: '重置',
    changePassword: '修改密码',
  },
  personalHomepage: {
    title: '我的个人首页',
    description: '这里保存你自己的首页布局，恢复默认后会重新继承租户级或公共级配置。',
  },
  validation: {
    oldPasswordRequired: '请输入旧密码',
    newPasswordRequired: '请输入新密码',
    newPasswordMin: '密码长度不能少于6位',
    confirmPasswordRequired: '请确认新密码',
    passwordMismatch: '两次输入的密码不一致',
  },
  message: {
    loadFailed: '加载用户信息失败',
    passwordChanged: '密码修改成功，请重新登录',
    passwordChangeFailed: '密码修改失败',
  },
}
