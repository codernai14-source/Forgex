/**
 * ユーザー管理翻訳 - 日本語
 */
export default {
  // ページタイトル
  title: 'ユーザー管理',
  userManagement: 'ユーザー管理',
  list: 'ユーザー一覧',
  detail: 'ユーザー詳細',
  
  // アクション
  add: 'ユーザー追加',
  edit: '編集',
  delete: '削除',
  export: 'エクスポート',
  resetPassword: 'パスワードリセット',
  lockUser: 'ユーザーロック',
  unlockUser: 'ユーザーロック解除',
  assignRole: 'ロール割り当て',
  viewDetail: '詳細表示',

  // ロール割り当てダイアログ
  roleAssign: {
    searchPlaceholder: 'ロール名またはコードで検索',
    selectAll: '全選択',
    clearAll: 'クリア',
    roleName: 'ロール名',
    roleCode: 'ロールコード',
    assignedStatus: '割り当て状態',
    assigned: '割り当て済み',
    summary: '合計 {total} ロール、{selected} 件選択中',
    loadFailed: 'ロールデータの読み込みに失敗しました',
    saveFailed: '保存に失敗しました',
    missingUserId: 'ユーザーIDが不足しています',
  },
  statusActive: '有効',
  statusInactive: '無効',
  statusLocked: 'ロック',
  
  // テーブル列
  avatar: 'アバター',
  username: 'ユーザー名',
  realName: '氏名',
  email: 'メール',
  phone: '電話番号',
  gender: '性別',
  department: '部門',
  position: '役職',
  role: 'ロール',
  entryDate: '入社日',
  status: '状態',
  createTime: '作成日時',
  createBy: '作成者',
  updateTime: '更新日時',
  updateBy: '更新者',
  lastLoginTime: '最終ログイン日時',
  lastLoginIp: '最終ログイン IP',
  lastLoginRegion: '最終ログイン地域',
  action: '操作',
  
  // 性別
  genderOptions: {
    unknown: '不明',
    male: '男性',
    female: '女性',
  },
  
  // フォーム
  form: {
    addUser: 'ユーザー追加',
    editUser: 'ユーザー編集',
    userDetail: 'ユーザー詳細',
    username: 'ユーザー名を入力してください',
    realName: '氏名を入力してください',
    email: 'メールを入力してください',
    phone: '電話番号を入力してください',
    password: 'パスワードを入力してください',
    confirmPassword: 'パスワードを確認してください',
    department: '部門を選択してください',
    role: 'ロールを選択してください',
    status: '状態を選択してください',
  },
  
  // メッセージ
  message: {
    loadListFailed: 'ユーザー一覧の読み込みに失敗しました',
    deleteFailed: '削除に失敗しました',
    batchDeleteFailed: '一括削除に失敗しました',
    selectToDelete: '削除するユーザーを選択してください',
    deleteConfirm: 'このユーザーを削除してもよろしいですか？',
    batchDeleteConfirm: '選択した{count}人のユーザーを削除してもよろしいですか？',
    deleteSuccess: 'ユーザーを削除しました',
    saveSuccess: 'ユーザーを保存しました',
    resetPasswordSuccess: 'パスワードをリセットしました',
    resetPasswordFailed: 'パスワードのリセットに失敗しました',
    resetPasswordConfirm: 'このパスワードをリセットしてもよろしいですか？システムデフォルトのパスワードにリセットされます。',
    lockSuccess: 'ユーザーをロックしました',
    unlockSuccess: 'ユーザーのロックを解除しました',
    usernameExists: 'ユーザー名は既に存在します',
    emailExists: 'メールは既に存在します',
    updateStatusFailed: '状態の更新に失敗しました',
  },
}
