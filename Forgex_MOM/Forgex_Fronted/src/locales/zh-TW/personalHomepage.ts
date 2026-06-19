/**
 * 个人首页翻译 - 繁体中文注释
 * 
 * @author Forgex Team
 * @version 1.0.0
 */
export default {
  loading: {
    thinking: 'Forgex正在載入您所需的任何內容，請稍候',
  },

  // Hero 区
  hero: {
    eyebrow: '個人工作台',
    title: '個人首頁',
    titleManage: '個人首頁默認配置',
    desc: '進入系統後的唯一默認落地頁，支持按個人習慣保存組件佈局。',
    descManage: '統一維護公共級和租戶級默認門戶佈局，所有用戶進入系統後都會以這裡為基礎。',
    badge: {
      default: '統一默認入口',
      user: '用戶級',
      tenant: '租戶級',
      public: '公共級',
    },
  },

  // 工具栏
  toolbar: {
    editMode: '編輯佈局',
    exitMode: '退出編輯',
    refresh: '刷新',
    resetDefault: '恢復默認',
    saveLayout: '保存佈局',
    componentLibrary: '組件庫',
    componentConfig: '個人組件配置',
    hint: {
      edit: '拖拽和縮放組件後可直接保存',
      view: '切換到編輯模式後可調整組件佈局',
    },
  },

  library: {
    title: '首頁組件庫',
    searchPlaceholder: '按編碼、名稱、作用搜尋',
    scopeAll: '全部',
    scopePublic: '公共',
    scopeTenant: '租戶',
    scopeUser: '個人',
    defaultGroup: '預設分類',
    empty: '暫無可選組件',
    favorite: '已收藏',
    selected: '已新增',
    removed: '已移除',
    customPlaceholder: '該組件由前端註冊組件承載，目錄僅維護可選範圍。',
  },

  // 配置面板
  panel: {
    title: '組件配置',
    subtitle: '顯隱、條數與更多入口',
  },

  // 组件配置字段
  widget: {
    limit: '顯示條數',
    showMore: '顯示更多入口',
    more: '更多',
  },

  // 组件标题
  components: {
    commonMenus: {
      title: '常用菜單',
      subtitle: '系統自動統計的固定 Top 6 菜單',
      empty: '暫無常用菜單',
    },
    myFavorites: {
      title: '我的收藏',
      subtitle: '我主動收藏的快捷入口',
      empty: '暫無收藏菜單',
      add: '加入收藏',
      remove: '取消收藏',
    },
    pendingApprovals: {
      title: '我收到的審批',
      subtitle: '我收到的審批待辦',
      empty: '暫無待處理審批',
    },
    calendar: {
      title: '日曆',
      subtitle: '本地日曆視圖',
    },
    messages: {
      title: '我收到的消息',
      subtitle: '用戶發給我的站內消息',
      empty: '暫無未讀消息',
      systemSender: '站內消息',
    },
    notices: {
      title: '系統通知',
      subtitle: '審批與系統類通知',
      empty: '暫無系統通知',
      systemType: '系統通知',
    },
    currentTime: {
      title: '當前時間',
      subtitle: '當前日期與時間',
    },
  },

  // 空状态
  empty: '當前沒有啟用的首頁組件',

  // 消息提示
  message: {
    loadFailed: '加載個人首頁配置失敗',
    saveSuccess: '個人首頁配置已保存',
    saveFailed: '保存個人首頁配置失敗',
    resetSuccess: '已恢復為默認佈局',
    resetFailed: '恢復默認佈局失敗',
  },

  // 收藏管理頁面
  share: {
    create: '\u751f\u6210\u5206\u4eab\u78bc',
    import: '\u532f\u5165\u4f48\u5c40',
    shareTitle: '\u4f48\u5c40\u5206\u4eab\u78bc',
    importTitle: '\u532f\u5165\u4f48\u5c40\u5206\u4eab\u78bc',
    shareCode: '\u5206\u4eab\u78bc',
    inputPlaceholder: '\u8acb\u8f38\u5165\u5206\u4eab\u78bc',
    copy: '\u8907\u88fd',
    preview: '\u9810\u89bd',
    apply: '\u5957\u7528\u5230\u76ee\u524d\u8349\u7a3f',
    moduleCode: '\u6a21\u7d44',
    createTime: '\u7522\u751f\u6642\u9593',
    createSuccess: '\u5206\u4eab\u78bc\u5df2\u7522\u751f',
    createFailed: '\u7522\u751f\u5206\u4eab\u78bc\u5931\u6557',
    copySuccess: '\u5206\u4eab\u78bc\u5df2\u8907\u88fd',
    previewFailed: '\u5206\u4eab\u78bc\u7121\u6548\u6216\u4e0d\u5c6c\u65bc\u76ee\u524d\u79df\u6236',
    importApplied: '\u5df2\u532f\u5165\u5230\u76ee\u524d\u8349\u7a3f\uff0c\u8acb\u5132\u5b58\u4f48\u5c40\u5f8c\u751f\u6548',
  },

  management: {
    title: '收藏管理',
    desc: '統一維護我的收藏菜單，支持調整首頁展示順序與批量取消收藏。',
    alert: '「我的收藏」卡片會按照這裡保存的順序展示；「常用菜單」固定保留系統自動統計的 Top 6。',
    empty: '暫無收藏菜單，請先在首頁或菜單中添加收藏。',
    stats: {
      count: '已收藏 {count} 項',
    },
    table: {
      order: '排序',
      menu: '菜單',
      path: '路由路徑',
      action: '操作',
    },
    action: {
      refresh: '刷新列表',
      batchCancel: '批量取消收藏',
      saveSort: '保存排序',
      moveUp: '上移',
      moveDown: '下移',
      open: '打開',
      remove: '取消收藏',
    },
    confirm: {
      batchCancelTitle: '確認批量取消收藏',
      batchCancelContent: '確認取消選中的 {count} 個收藏菜單嗎？',
      singleCancelTitle: '確認取消收藏',
      singleCancelContent: '確認取消收藏「{title}」嗎？',
    },
    message: {
      loadFailed: '加載收藏管理列表失敗',
      batchCancelSuccess: '批量取消收藏成功',
      batchCancelFailed: '批量取消收藏失敗',
      singleCancelSuccess: '取消收藏成功',
      singleCancelFailed: '取消收藏失敗',
      sortSaveSuccess: '收藏排序已保存',
      sortSaveFailed: '保存收藏排序失敗',
    },
  },

  // 摘要卡片
  componentConfig: {
    title: '個人首頁組件配置',
    desc: '維護目前使用者可見的首頁組件，支援收藏、新增和移除個人組件。',
    empty: '目前沒有可配置的個人首頁組件',
    addSuccess: '已新增到個人首頁',
    removeSuccess: '已從個人首頁移除',
    stats: {
      count: '共 {count} 個分組',
    },
  },

  summary: {
    greeting: {
      honorificMale: '先生',
      honorificFemale: '女士',
      lead: {
        morning: '早上好',
        afternoon: '下午好',
        evening: '晚上好',
      },
      closing: {
        morning: '願您精神飽滿，開啟元氣滿滿的一天',
        afternoon: '適當休息片刻，下午繼續高效前行',
        evening: '工作很辛苦吧，注意休息哦',
      },
      lineZh: '尊敬的{name}{honorific}，{lead}，{closing}',
    },
    weekday: {
      0: '星期日',
      1: '星期一',
      2: '星期二',
      3: '星期三',
      4: '星期四',
      5: '星期五',
      6: '星期六',
    },
    todayLineZh: '今天是{month}月{day}日 {weekday}',
    onlineDuration: '在線時長',
    zeroMinutes: '0??',
  },
  module: {
    mode: { editing: '編輯中', view: '瀏覽' },
    toolbar: { exitConfig: '退出配置', hint: { edit: '拖拽卡片或調整尺寸後保存。', view: '進入配置後可以調整模塊首頁組件。' } },
    empty: '暫無可顯示組件',
    action: { enter: '進入' },
    panel: { width: '寬度', height: '高度' },
    message: { saveSuccess: '模塊首頁配置已保存', saveFailed: '保存模塊首頁配置失敗' },
    modules: {
      personal: { name: '個人首頁', title: '個人首頁默認配置', desc: '維護個人首頁默認佈局。' },
      basic: { name: '基本信息', title: '基本信息主頁', desc: '集中展示基本信息模塊的主數據入口和配置狀態。' },
      approval: { name: '審批管理', title: '審批管理主頁', desc: '集中展示審批任務、待辦入口和流程配置。' },
      sys: { name: '系統管理', title: '系統管理主頁', desc: '集中展示系統運行、權限配置和系統參數入口。' },
    },
    widgets: {
      supplierInfo: { title: '供應商信息', subtitle: '供應商主數據與准入維護', summary: '管理供應商檔案、聯繫人、資質與協同狀態。' },
      encodeRuleInfo: { title: '編碼規則信息', subtitle: '統一編碼生成規則', summary: '維護基本信息模塊的編碼規則、流水號和測試生成。' },
      systemOverview: { title: '系統概覽', subtitle: '組織、用戶與權限入口', summary: '快速進入用戶、角色和菜單授權等系統核心能力。' },
      systemHealth: { title: '運行狀態', subtitle: '系統運行與安全狀態', summary: '查看在線用戶、登錄行為和系統運行相關信息。' },
      systemLogs: { title: '操作日誌', subtitle: '系統審計與追蹤', summary: '進入登錄日誌、操作日誌等審計頁面。' },
      systemConfig: { title: '系統配置', subtitle: '平台參數與外觀配置', summary: '維護門戶、主題、安全、郵件、上傳和首頁默認佈局。' },
      approvalStats: { title: '審批概覽', subtitle: '審批運行概況', summary: '查看待辦、已辦和審批執行的整體情況。' },
      approvalShortcuts: { title: '審批入口', subtitle: '發起審批與常用流程', summary: '快速進入可發起的審批任務和業務流程。' },
      approvalPending: { title: '我的待辦', subtitle: '當前待處理審批', summary: '查看需要當前用戶處理的審批實例。' },
      approvalTaskConfig: { title: '任務配置', subtitle: '審批任務與節點規則', summary: '配置任務表單、節點審批人和流程規則。' },
      custom: { subtitle: '自定義組件', summary: '該組件來自已保存的模塊首頁配置。' },
    },
    stats: {
      masterData: '主數據', supplierArchive: '供應商檔案', approval: '審批', admissionChange: '准入/變更', rule: '規則', byModule: '按模塊維護', capability: '能力', testGenerate: '測試/生成', user: '用戶', accountManage: '賬號管理', role: '角色', authConfig: '授權配置', status: '狀態', onlineSession: '在線/會話', security: '安全', loginAudit: '登錄審計', audit: '審計', operationLog: '操作日誌', trace: '追蹤', loginRecord: '登錄記錄', scope: '範圍', publicTenant: '公共/租戶', config: '配置', systemParams: '系統參數', pending: '待辦', myTasks: '我的任務', processed: '已辦', processRecord: '處理記錄', entry: '入口', startApproval: '發起審批', flow: '流程', taskTemplate: '任務模板', action: '動作', approveReject: '同意/駁回', task: '任務', flowConfig: '流程配置', node: '節點', approvalRule: '審批規則', type: '類型', extension: '擴展', enabled: '已啟用', pendingApproval: '待審批',
    },
  }
}
