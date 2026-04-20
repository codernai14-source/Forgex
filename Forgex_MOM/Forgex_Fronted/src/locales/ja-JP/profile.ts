/**
 * 个人资料页面
 *
 * @author Forgex
 * @version 1.0.0
 */
export default {
  title: 'プロフィール',
  tabs: {
    basic: '基本情報',
    org: '組織情報',
    security: 'セキュリティ',
    personalHomepage: '個人ホーム',
  },
  fields: {
    account: 'アカウント',
    username: 'ユーザー名',
    email: 'メール',
    phone: '電話番号',
    gender: '性別',
    department: '部門',
    position: '役職',
    entryDate: '入社日',
    status: '状態',
    oldPassword: '現在のパスワード',
    newPassword: '新しいパスワード',
    confirmPassword: 'パスワード確認',
  },
  gender: {
    male: '男性',
    female: '女性',
    unknown: '不明',
  },
  actions: {
    save: '保存',
    reset: 'リセット',
    changePassword: 'パスワード変更',
  },
  personalHomepage: {
    title: 'マイホーム',
    description: 'ここにホームレイアウトを保存します。既定に戻すとテナントまたは公開設定を継承します。',
  },
  validation: {
    oldPasswordRequired: '現在のパスワードを入力してください',
    newPasswordRequired: '新しいパスワードを入力してください',
    newPasswordMin: 'パスワードは6文字以上にしてください',
    confirmPasswordRequired: '新しいパスワードを再入力してください',
    passwordMismatch: 'パスワードが一致しません',
  },
  message: {
    loadFailed: 'ユーザー情報の読み込みに失敗しました',
    passwordChanged: 'パスワードを変更しました。再度日志インしてください。',
    passwordChangeFailed: 'パスワードの変更に失敗しました',
  },
}
