/**
 * 部门管理 - 日语界面文案
 */
export default {
  title: '部門管理',
  list: '部門一覧',
  detail: '部門詳細',

  deptName: '部門名',
  deptCode: '部門コード',
  orgType: '組織種別',
  orgLevel: '組織階層',
  leader: '責任者',
  phone: '電話番号',
  email: 'メール',
  orderNum: '表示順',
  status: '状態',
  createTime: '作成日時',
  updateTime: '更新日時',
  createBy: '作成者',
  updateBy: '更新者',

  form: {
    addDept: '部門を追加',
    editDept: '部門を編集',
    deptName: '部門名を入力してください',
    deptCode: '部門コードを入力してください',
    orgType: '組織種別を選択してください',
    orgLevel: '組織階層を入力してください',
    leader: '責任者を入力してください',
    phone: '電話番号を入力してください',
    email: 'メールを入力してください',
    orderNum: '表示順を入力してください',
    status: '状態を選択してください',
  },

  childDept: '子部門',
  addRootDept: 'ルート部門を追加',
  emptySelectTip: '詳細を表示するには左側で部門を選択してください',

  message: {
    loadTreeFailed: '組織ツリーの読み込みに失敗しました',
    deleteConfirm: 'この部門を削除してもよろしいですか？',
    deleteSuccess: '部門を削除しました',
    saveSuccess: '部門を保存しました',
    deptCodeExists: 'この部門コードは既に存在します',
    hasChildren: '子部門があるため削除できません',
  },
}
