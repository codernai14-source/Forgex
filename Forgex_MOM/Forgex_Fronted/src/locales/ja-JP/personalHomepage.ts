/**
 * 个人首页翻译 - 日文注释
 * 
 * @author Forgex Team
 * @version 1.0.0
 */
export default {
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
    hint: {
      edit: 'コンポーネントをドラッグしてサイズ変更後、直接保存できます',
      view: '編集モードに切り替えてコンポーネントのレイアウトを調整できます',
    },
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
    mode: { editing: '\u7de8\u96c6\u4e2d', view: '\u8868\u793a' },
    toolbar: { exitConfig: '\u8a2d\u5b9a\u3092\u7d42\u4e86', hint: { edit: 'Drag cards or resize them, then save.', view: 'Enter configuration to adjust module homepage widgets.' } },
    empty: '\u8868\u793a\u3067\u304d\u308b\u30a6\u30a3\u30b8\u30a7\u30c3\u30c8\u304c\u3042\u308a\u307e\u305b\u3093',
    action: { enter: '\u958b\u304f' },
    panel: { width: '\u5e45', height: '\u9ad8\u3055' },
    message: { saveSuccess: 'Module homepage configuration saved', saveFailed: 'Failed to save module homepage configuration' },
    modules: {
      personal: { name: 'Personal Homepage', title: 'Personal Homepage Default Config', desc: 'Maintain the default personal homepage layout.' },
      basic: { name: 'Basic Information', title: 'Basic Information Homepage', desc: 'Central entry for master data and configuration status in Basic Information.' },
      approval: { name: 'Approval', title: 'Approval Homepage', desc: 'Central view for approval tasks, pending entries, and workflow configuration.' },
      sys: { name: 'System Management', title: 'System Management Homepage', desc: 'Central entry for system runtime, permissions, and system parameters.' },
    },
    widgets: {
      supplierInfo: { title: 'Supplier Information', subtitle: 'Supplier master data and admission maintenance', summary: 'Manage supplier profiles, contacts, qualifications, and collaboration status.' },
      encodeRuleInfo: { title: 'Encoding Rules', subtitle: 'Unified code generation rules', summary: 'Maintain encoding rules, serial numbers, and test generation for basic information.' },
      systemOverview: { title: 'System Overview', subtitle: 'Organization, users, and permission entries', summary: 'Quick access to users, roles, menu grants, and other core system capabilities.' },
      systemHealth: { title: 'Runtime Status', subtitle: 'System runtime and security status', summary: 'View online users, login behavior, and system runtime information.' },
      systemLogs: { title: 'Operation Logs', subtitle: 'System audit and tracing', summary: 'Open login logs, operation logs, and other audit pages.' },
      systemConfig: { title: 'System Config', subtitle: 'Platform parameters and appearance settings', summary: 'Maintain portal, theme, security, mail, upload, and default homepage layout settings.' },
      approvalStats: { title: 'Approval Overview', subtitle: 'Approval operation summary', summary: 'View pending, processed, and overall approval execution status.' },
      approvalShortcuts: { title: 'Approval Entry', subtitle: 'Start approvals and common flows', summary: 'Quickly open approval tasks and business workflows that can be started.' },
      approvalPending: { title: 'My Pending Tasks', subtitle: 'Current pending approvals', summary: 'View approval instances that require the current user to process.' },
      approvalTaskConfig: { title: 'Task Config', subtitle: 'Approval tasks and node rules', summary: 'Configure task forms, node approvers, and workflow rules.' },
      custom: { subtitle: 'Custom Widget', summary: 'This widget comes from a saved module homepage configuration.' },
    },
    stats: {
      masterData: 'Master Data', supplierArchive: 'Supplier Profiles', approval: 'Approval', admissionChange: 'Admission/Change', rule: 'Rule', byModule: 'By Module', capability: 'Capability', testGenerate: 'Test/Generate', user: 'User', accountManage: 'Account Management', role: 'Role', authConfig: 'Authorization Config', status: 'Status', onlineSession: 'Online/Session', security: 'Security', loginAudit: 'Login Audit', audit: 'Audit', operationLog: 'Operation Logs', trace: 'Trace', loginRecord: 'Login Records', scope: 'Scope', publicTenant: 'Public/Tenant', config: 'Config', systemParams: 'System Parameters', pending: 'Pending', myTasks: 'My Tasks', processed: 'Processed', processRecord: 'Processing Records', entry: 'Entry', startApproval: 'Start Approval', flow: 'Flow', taskTemplate: 'Task Templates', action: 'Action', approveReject: 'Approve/Reject', task: 'Task', flowConfig: 'Flow Config', node: 'Node', approvalRule: 'Approval Rules', type: 'Type', extension: 'Extension', enabled: 'Enabled', pendingApproval: 'Pending Approval',
    },
  }
}
