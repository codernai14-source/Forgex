/**
 * 表格配置管理 - 日文注释
 */
export default {
  tableCode: 'テーブルコード',
  tableName: 'テーブル名',
  tableType: 'テーブルタイプ',
  rowKey: '行キー',
  defaultPageSize: 'デフォルトページサイズ',
  enabled: '有効',
  tableTypeNormal: '通常テーブル',
  tableTypeLazy: '遅延ロードテーブル',
  tableTypeTree: 'ツリーテーブル',
  basicInfo: '基本情報',
  columnConfig: '列設定',
  field: 'フィールド名',
  title: '列タイトル',
  align: '配置',
  width: '列幅',
  fixed: '固定列',
  ellipsis: '省略',
  sortable: 'ソート可能',
  queryable: '検索可能',
  orderNum: '並び順',
  add: '設定追加',
  edit: '設定編集',
  addColumn: '列追加',
  columnRequired: '少なくとも 1 つの列設定を追加してください',
  loadDetailFailed: '設定詳細の読み込みに失敗しました',
  userColumnSetting: {
    title: 'ユーザー列設定を編集',
    descriptionTitle: '説明',
    description: '現在のユーザーの列表示設定を管理します。ページやボタンの表示はロール権限に従います。',
    configured: '設定済み',
    notConfigured: '未設定',
    pageSize: 'ページサイズ',
    userPageSize: 'ユーザーページサイズ',
    configStatus: '設定状態',
    version: '設定バージョン',
    updateTime: '最終更新日時',
    visible: '表示',
    order: '並び順',
    move: '移動',
  },
  
  /**
   * 列设置相关
   */
  columnSetting: {
    title: '列設定',
    reset: 'リセット',
    resetSuccess: 'デフォルト設定にリセットしました',
    hint: '表示列をチェックし、ドラッグして順序を調整',
    dragSort: 'ドラッグして並び替え',
    dragResize: 'ドラッグして列幅を調整',
    fixedUnset: '固定しない',
    fixedLeft: '左に固定',
    fixedRight: '右に固定'
  },
  
  form: {
    tableCode: 'テーブルコードを入力してください',
    tableName: 'テーブル名を入力してください',
    tableType: 'テーブルタイプを選択してください',
    rowKey: '行キーを入力してください',
    defaultPageSize: 'デフォルトページサイズを入力してください',
    enabled: '有効状態を選択してください',
    field: 'フィールド名を入力してください',
    title: '列タイトルを入力してください'
  }
}
