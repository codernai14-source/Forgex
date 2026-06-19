/**
 * 个人首页翻译 - 日文注释
 * 
 * @author Forgex Team
 * @version 1.0.0
 */
export default {
  loading: {
    thinking: 'Forgex が必要なコンテンツを読み込んでいます。しばらくお待ちください。',
  },

  // 头部区域
  hero: {
    eyebrow: 'パーソナルワークスペース',
    title: '個人ホーム',
    titleManage: '個人ホーム初期設定',
    desc: 'システム日志イン後の初期表示ページで、個人の好みに応じてコンポーネントのレイアウトを保存できます。',
    descManage: 'パブリックレベルとテナントレベルの初期ポータルレイアウトを一括管理し、すべてのユーザーがこれを基にシステムを利用します。',
    badge: {
      default: '初期エントリー',
      user: 'ユーザーレベル',
      tenant: 'テナントレベル',
      public: 'パブリックレベル',
    },
  },

  // 工具栏
  toolbar: {
    editMode: 'レイアウト編集',
    exitMode: '編集終了',
    refresh: '更新',
    resetDefault: '初期値にリセット',
    saveLayout: 'レイアウト保存',
    componentLibrary: 'コンポーネント庫',
    componentConfig: '個人コンポーネント設定',
    hint: {
      edit: 'コンポーネントをドラッグしてサイズ変更後、直接保存できます',
      view: '編集モードに切り替えてコンポーネントのレイアウトを調整できます',
    },
  },

  library: {
    title: 'ホームページコンポーネント庫',
    searchPlaceholder: 'コード、名称、用途で検索',
    scopeAll: 'すべて',
    scopePublic: '共通',
    scopeTenant: 'テナント',
    scopeUser: '個人',
    defaultGroup: '既定分類',
    empty: '選択可能なコンポーネントがありません',
    favorite: 'お気に入り',
    selected: '追加済み',
    removed: '削除済み',
    customPlaceholder: 'このコンポーネントはフロントエンド登録コンポーネントで描画され、一覧は選択可能範囲のみ管理します。',
  },

  // 设置面板
  panel: {
    title: 'ウィジェット設定',
    subtitle: '表示/非表示、件数、其他のオプション',
  },

  // 组件配置字段
  widget: {
    limit: '表示件数',
    showMore: 'もっと見るリンクを表示',
    more: 'もっと見る',
  },

  // 组件标题
  components: {
    commonMenus: {
      title: 'よく使うメニュー',
      subtitle: 'システム集計による固定 Top 6 メニュー',
      empty: 'よく使うメニューはありません',
    },
    myFavorites: {
      title: 'お気に入り',
      subtitle: '自分で登録したショートカット',
      empty: 'お気に入りメニューはありません',
      add: 'お気に入りに追加',
      remove: 'お気に入りを解除',
    },
    pendingApprovals: {
      title: '承認待ち',
      subtitle: '自分に割り当てられた承認タスク',
      empty: '承認待ちのタスクはありません',
    },
    calendar: {
      title: 'カレンダー',
      subtitle: 'ローカルカレンダービュー',
    },
    messages: {
      title: '自分の消息',
      subtitle: 'ユーザーから送信された站内消息',
      empty: '未読消息はありません',
      systemSender: '站内消息',
    },
    notices: {
      title: 'システム通知',
      subtitle: '承認およびシステム通知',
      empty: 'システム通知はありません',
      systemType: 'システム通知',
    },
    currentTime: {
      title: '現在の時刻',
      subtitle: '現在の日時',
    },
  },

  // 空状態
  empty: '有効なホームウィジェットはありません',

  // 消息
  message: {
    loadFailed: 'ホーム設定の読み込みに失敗しました',
    saveSuccess: 'ホーム設定を保存しました',
    saveFailed: 'ホーム設定の保存に失敗しました',
    resetSuccess: '初期レイアウトにリセットしました',
    resetFailed: '初期レイアウトへのリセットに失敗しました',
  },

  // お気に入り管理ページ
  share: {
    create: '共有コードを作成',
    import: 'レイアウトをインポート',
    shareTitle: 'レイアウト共有コード',
    importTitle: 'レイアウト共有コードをインポート',
    shareCode: '共有コード',
    inputPlaceholder: '共有コードを入力',
    copy: 'コピー',
    preview: 'プレビュー',
    apply: '現在の下書きに適用',
    moduleCode: 'モジュール',
    createTime: '作成日時',
    createSuccess: '共有コードを作成しました',
    createFailed: '共有コードの作成に失敗しました',
    copySuccess: '共有コードをコピーしました',
    previewFailed: '共有コードが無効、または現在のテナントに属していません',
    importApplied: '現在の下書きにインポートしました。保存後に反映されます。',
  },

  management: {
    title: 'お気に入り管理',
    desc: 'お気に入りメニューをまとめて管理し、表示順の調整や一括解除を行えます。',
    alert: '「お気に入り」カードはここで保存した順序で表示されます。「よく使うメニュー」はシステム集計の Top 6 を固定表示します。',
    empty: 'お気に入りメニューがありません。ホームまたはメニューから先に追加してください。',
    stats: {
      count: '{count} 件を登録済み',
    },
    table: {
      order: '順序',
      menu: 'メニュー',
      path: 'ルートパス',
      action: '操作',
    },
    action: {
      refresh: '再読み込み',
      batchCancel: '一括解除',
      saveSort: '順序を保存',
      moveUp: '上へ',
      moveDown: '下へ',
      open: '開く',
      remove: '解除',
    },
    confirm: {
      batchCancelTitle: '一括解除の確認',
      batchCancelContent: '選択した {count} 件のお気に入りを解除しますか？',
      singleCancelTitle: '解除の確認',
      singleCancelContent: '「{title}」をお気に入りから解除しますか？',
    },
    message: {
      loadFailed: 'お気に入り一覧の読み込みに失敗しました',
      batchCancelSuccess: '一括解除が完了しました',
      batchCancelFailed: '一括解除に失敗しました',
      singleCancelSuccess: 'お気に入りを解除しました',
      singleCancelFailed: 'お気に入りの解除に失敗しました',
      sortSaveSuccess: 'お気に入り順序を保存しました',
      sortSaveFailed: 'お気に入り順序の保存に失敗しました',
    },
  },

  // 摘要卡片
  componentConfig: {
    title: '個人ホームページコンポーネント設定',
    desc: '現在のユーザーが利用できるホームページコンポーネントを管理し、お気に入り、追加、削除を行えます。',
    empty: '設定できる個人ホームページコンポーネントがありません',
    addSuccess: '個人ホームページに追加しました',
    removeSuccess: '個人ホームページから削除しました',
    stats: {
      count: '{count} グループ',
    },
  },

  summary: {
    greeting: {
      honorificMale: '様',
      honorificFemale: '様',
      lead: {
        morning: 'おはようございます',
        afternoon: 'こんにちは',
        evening: 'こんばんは',
      },
      closing: {
        morning: '元気いっぱいで、生産性のある一日をスタートしましょう。',
        afternoon: '調子を維持しましょう。適度に休憩して深呼吸してください。',
        evening: '充実した一日でしたね。今夜はゆっくり休んで充電してください。',
      },
      lineJaMale: '{name} 様、{lead} — {closing}',
      lineJaFemale: '{name} 様、{lead} — {closing}',
      lineJaNeutral: '{name} さん、{lead} — {closing}',
    },
    weekday: {
      0: '日曜日',
      1: '月曜日',
      2: '火曜日',
      3: '水曜日',
      4: '木曜日',
      5: '金曜日',
      6: '土曜日',
    },
    todayLineJa: '今日は{month}月{day}日 {weekday}',
    onlineDuration: 'オンライン時間',
    zeroMinutes: '0?',
  },
  module: {
    mode: { editing: '編集中', view: '表示' },
    toolbar: { exitConfig: '設定を終了', hint: { edit: 'カードをドラッグまたはサイズ変更してから保存します。', view: '設定に入り、モジュールホームのウィジェットを調整できます。' } },
    empty: '表示できるウィジェットがありません',
    action: { enter: '開く' },
    panel: { width: '幅', height: '高さ' },
    message: { saveSuccess: 'モジュールホーム設定を保存しました', saveFailed: 'モジュールホーム設定の保存に失敗しました' },
    modules: {
      personal: { name: '個人ホーム', title: '個人ホーム既定設定', desc: '個人ホームの既定レイアウトを管理します。' },
      basic: { name: '基本情報', title: '基本情報ホーム', desc: '基本情報モジュールのマスターデータ入口と設定状態をまとめて表示します。' },
      approval: { name: '承認管理', title: '承認管理ホーム', desc: '承認タスク、保留中の入口、ワークフロー設定をまとめて表示します。' },
      sys: { name: 'システム管理', title: 'システム管理ホーム', desc: 'システム稼働状況、権限設定、システムパラメータ入口をまとめて提供します。' },
    },
    widgets: {
      supplierInfo: { title: 'サプライヤー情報', subtitle: 'サプライヤーマスターデータと登録管理', summary: 'サプライヤープロファイル、連絡先、資格、協業状態を管理します。' },
      encodeRuleInfo: { title: '採番ルール', subtitle: '統一コード生成ルール', summary: '基本情報モジュールの採番ルール、連番、テスト生成を管理します。' },
      systemOverview: { title: 'システム概要', subtitle: '組織、ユーザー、権限入口', summary: 'ユーザー、ロール、メニュー権限など主要なシステム機能へ素早くアクセスします。' },
      systemHealth: { title: '稼働状態', subtitle: 'システム稼働とセキュリティ状態', summary: 'オンラインユーザー、ログイン動作、システム稼働関連情報を確認します。' },
      systemLogs: { title: '操作ログ', subtitle: 'システム監査と追跡', summary: 'ログインログ、操作ログなどの監査画面を開きます。' },
      systemConfig: { title: 'システム設定', subtitle: 'プラットフォームパラメータと外観設定', summary: 'ポータル、テーマ、セキュリティ、メール、アップロード、既定ホームレイアウトを管理します。' },
      approvalStats: { title: '承認概要', subtitle: '承認稼働状況', summary: '保留、処理済み、承認実行の全体状況を確認します。' },
      approvalShortcuts: { title: '承認入口', subtitle: '承認開始とよく使うフロー', summary: '開始可能な承認タスクと業務フローへ素早く移動します。' },
      approvalPending: { title: '自分の保留タスク', subtitle: '現在処理待ちの承認', summary: '現在のユーザーが処理する必要のある承認インスタンスを確認します。' },
      approvalTaskConfig: { title: 'タスク設定', subtitle: '承認タスクとノードルール', summary: 'タスクフォーム、ノード承認者、ワークフロールールを設定します。' },
      custom: { subtitle: 'カスタムウィジェット', summary: '保存済みのモジュールホーム設定から取得したウィジェットです。' },
    },
    stats: {
      masterData: 'マスターデータ', supplierArchive: 'サプライヤープロファイル', approval: '承認', admissionChange: '登録/変更', rule: 'ルール', byModule: 'モジュール別', capability: '機能', testGenerate: 'テスト/生成', user: 'ユーザー', accountManage: 'アカウント管理', role: 'ロール', authConfig: '権限設定', status: '状態', onlineSession: 'オンライン/セッション', security: 'セキュリティ', loginAudit: 'ログイン監査', audit: '監査', operationLog: '操作ログ', trace: '追跡', loginRecord: 'ログイン記録', scope: '範囲', publicTenant: '共通/テナント', config: '設定', systemParams: 'システムパラメータ', pending: '保留', myTasks: '自分のタスク', processed: '処理済み', processRecord: '処理記録', entry: '入口', startApproval: '承認開始', flow: 'フロー', taskTemplate: 'タスクテンプレート', action: '操作', approveReject: '承認/却下', task: 'タスク', flowConfig: 'フロー設定', node: 'ノード', approvalRule: '承認ルール', type: 'タイプ', extension: '拡張', enabled: '有効', pendingApproval: '承認待ち',
    },
  }
}
