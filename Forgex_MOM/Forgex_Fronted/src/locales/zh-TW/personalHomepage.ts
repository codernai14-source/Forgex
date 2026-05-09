/**
 * 个人首页翻译 - 繁体中文注释
 * 
 * @author Forgex Team
 * @version 1.0.0
 */
export default {
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
    hint: {
      edit: '拖拽和縮放組件後可直接保存',
      view: '切換到編輯模式後可調整組件佈局',
    },
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
    mode: { editing: '\u7f16\u8f91\u4e2d', view: '\u6d4f\u89c8' },
    toolbar: { exitConfig: '\u9000\u51fa\u914d\u7f6e', hint: { edit: '\u62d6\u62fd\u5361\u7247\u6216\u8c03\u6574\u5c3a\u5bf8\u540e\u4fdd\u5b58\u3002', view: '\u8fdb\u5165\u914d\u7f6e\u540e\u53ef\u4ee5\u8c03\u6574\u6a21\u5757\u9996\u9875\u7ec4\u4ef6\u3002' } },
    empty: '\u6682\u65e0\u53ef\u663e\u793a\u7ec4\u4ef6',
    action: { enter: '\u8fdb\u5165' },
    panel: { width: '\u5bbd\u5ea6', height: '\u9ad8\u5ea6' },
    message: { saveSuccess: '\u6a21\u5757\u9996\u9875\u914d\u7f6e\u5df2\u4fdd\u5b58', saveFailed: '\u4fdd\u5b58\u6a21\u5757\u9996\u9875\u914d\u7f6e\u5931\u8d25' },
    modules: {
      personal: { name: '\u4e2a\u4eba\u9996\u9875', title: '\u4e2a\u4eba\u9996\u9875\u9ed8\u8ba4\u914d\u7f6e', desc: '\u7ef4\u62a4\u4e2a\u4eba\u9996\u9875\u9ed8\u8ba4\u5e03\u5c40\u3002' },
      basic: { name: '\u57fa\u7840\u4fe1\u606f', title: '\u57fa\u7840\u4fe1\u606f\u4e3b\u9875', desc: '\u96c6\u4e2d\u5c55\u793a\u57fa\u7840\u4fe1\u606f\u6a21\u5757\u7684\u4e3b\u6570\u636e\u5165\u53e3\u548c\u914d\u7f6e\u72b6\u6001\u3002' },
      approval: { name: '\u5ba1\u6279\u7ba1\u7406', title: '\u5ba1\u6279\u7ba1\u7406\u4e3b\u9875', desc: '\u96c6\u4e2d\u5c55\u793a\u5ba1\u6279\u4efb\u52a1\u3001\u5f85\u529e\u5165\u53e3\u548c\u6d41\u7a0b\u914d\u7f6e\u3002' },
      sys: { name: '\u7cfb\u7edf\u7ba1\u7406', title: '\u7cfb\u7edf\u7ba1\u7406\u4e3b\u9875', desc: '\u96c6\u4e2d\u5c55\u793a\u7cfb\u7edf\u8fd0\u884c\u3001\u6743\u9650\u914d\u7f6e\u548c\u7cfb\u7edf\u53c2\u6570\u5165\u53e3\u3002' },
    },
    widgets: {
      supplierInfo: { title: '\u4f9b\u5e94\u5546\u4fe1\u606f', subtitle: '\u4f9b\u5e94\u5546\u4e3b\u6570\u636e\u4e0e\u51c6\u5165\u7ef4\u62a4', summary: '\u7ba1\u7406\u4f9b\u5e94\u5546\u6863\u6848\u3001\u8054\u7cfb\u4eba\u3001\u8d44\u8d28\u4e0e\u534f\u540c\u72b6\u6001\u3002' },
      encodeRuleInfo: { title: '\u7f16\u7801\u89c4\u5219\u4fe1\u606f', subtitle: '\u7edf\u4e00\u7f16\u7801\u751f\u6210\u89c4\u5219', summary: '\u7ef4\u62a4\u57fa\u7840\u4fe1\u606f\u6a21\u5757\u7684\u7f16\u7801\u89c4\u5219\u3001\u6d41\u6c34\u53f7\u548c\u6d4b\u8bd5\u751f\u6210\u3002' },
      systemOverview: { title: '\u7cfb\u7edf\u6982\u89c8', subtitle: '\u7ec4\u7ec7\u3001\u7528\u6237\u4e0e\u6743\u9650\u5165\u53e3', summary: '\u5feb\u901f\u8fdb\u5165\u7528\u6237\u3001\u89d2\u8272\u548c\u83dc\u5355\u6388\u6743\u7b49\u7cfb\u7edf\u6838\u5fc3\u80fd\u529b\u3002' },
      systemHealth: { title: '\u8fd0\u884c\u72b6\u6001', subtitle: '\u7cfb\u7edf\u8fd0\u884c\u4e0e\u5b89\u5168\u72b6\u6001', summary: '\u67e5\u770b\u5728\u7ebf\u7528\u6237\u3001\u767b\u5f55\u884c\u4e3a\u548c\u7cfb\u7edf\u8fd0\u884c\u76f8\u5173\u4fe1\u606f\u3002' },
      systemLogs: { title: '\u64cd\u4f5c\u65e5\u5fd7', subtitle: '\u7cfb\u7edf\u5ba1\u8ba1\u4e0e\u8ffd\u8e2a', summary: '\u8fdb\u5165\u767b\u5f55\u65e5\u5fd7\u3001\u64cd\u4f5c\u65e5\u5fd7\u7b49\u5ba1\u8ba1\u9875\u9762\u3002' },
      systemConfig: { title: '\u7cfb\u7edf\u914d\u7f6e', subtitle: '\u5e73\u53f0\u53c2\u6570\u4e0e\u5916\u89c2\u914d\u7f6e', summary: '\u7ef4\u62a4\u95e8\u6237\u3001\u4e3b\u9898\u3001\u5b89\u5168\u3001\u90ae\u4ef6\u3001\u4e0a\u4f20\u548c\u9996\u9875\u9ed8\u8ba4\u5e03\u5c40\u3002' },
      approvalStats: { title: '\u5ba1\u6279\u6982\u89c8', subtitle: '\u5ba1\u6279\u8fd0\u884c\u6982\u51b5', summary: '\u67e5\u770b\u5f85\u529e\u3001\u5df2\u529e\u548c\u5ba1\u6279\u6267\u884c\u7684\u6574\u4f53\u60c5\u51b5\u3002' },
      approvalShortcuts: { title: '\u5ba1\u6279\u5165\u53e3', subtitle: '\u53d1\u8d77\u5ba1\u6279\u4e0e\u5e38\u7528\u6d41\u7a0b', summary: '\u5feb\u901f\u8fdb\u5165\u53ef\u53d1\u8d77\u7684\u5ba1\u6279\u4efb\u52a1\u548c\u4e1a\u52a1\u6d41\u7a0b\u3002' },
      approvalPending: { title: '\u6211\u7684\u5f85\u529e', subtitle: '\u5f53\u524d\u5f85\u5904\u7406\u5ba1\u6279', summary: '\u67e5\u770b\u9700\u8981\u5f53\u524d\u7528\u6237\u5904\u7406\u7684\u5ba1\u6279\u5b9e\u4f8b\u3002' },
      approvalTaskConfig: { title: '\u4efb\u52a1\u914d\u7f6e', subtitle: '\u5ba1\u6279\u4efb\u52a1\u4e0e\u8282\u70b9\u89c4\u5219', summary: '\u914d\u7f6e\u4efb\u52a1\u8868\u5355\u3001\u8282\u70b9\u5ba1\u6279\u4eba\u548c\u6d41\u7a0b\u89c4\u5219\u3002' },
      custom: { subtitle: '\u81ea\u5b9a\u4e49\u7ec4\u4ef6', summary: '\u8be5\u7ec4\u4ef6\u6765\u81ea\u5df2\u4fdd\u5b58\u7684\u6a21\u5757\u9996\u9875\u914d\u7f6e\u3002' },
    },
    stats: {
      masterData: '\u4e3b\u6570\u636e', supplierArchive: '\u4f9b\u5e94\u5546\u6863\u6848', approval: '\u5ba1\u6279', admissionChange: '\u51c6\u5165/\u53d8\u66f4', rule: '\u89c4\u5219', byModule: '\u6309\u6a21\u5757\u7ef4\u62a4', capability: '\u80fd\u529b', testGenerate: '\u6d4b\u8bd5/\u751f\u6210', user: '\u7528\u6237', accountManage: '\u8d26\u53f7\u7ba1\u7406', role: '\u89d2\u8272', authConfig: '\u6388\u6743\u914d\u7f6e', status: '\u72b6\u6001', onlineSession: '\u5728\u7ebf/\u4f1a\u8bdd', security: '\u5b89\u5168', loginAudit: '\u767b\u5f55\u5ba1\u8ba1', audit: '\u5ba1\u8ba1', operationLog: '\u64cd\u4f5c\u65e5\u5fd7', trace: '\u8ffd\u8e2a', loginRecord: '\u767b\u5f55\u8bb0\u5f55', scope: '\u8303\u56f4', publicTenant: '\u516c\u5171/\u79df\u6237', config: '\u914d\u7f6e', systemParams: '\u7cfb\u7edf\u53c2\u6570', pending: '\u5f85\u529e', myTasks: '\u6211\u7684\u4efb\u52a1', processed: '\u5df2\u529e', processRecord: '\u5904\u7406\u8bb0\u5f55', entry: '\u5165\u53e3', startApproval: '\u53d1\u8d77\u5ba1\u6279', flow: '\u6d41\u7a0b', taskTemplate: '\u4efb\u52a1\u6a21\u677f', action: '\u52a8\u4f5c', approveReject: '\u540c\u610f/\u9a73\u56de', task: '\u4efb\u52a1', flowConfig: '\u6d41\u7a0b\u914d\u7f6e', node: '\u8282\u70b9', approvalRule: '\u5ba1\u6279\u89c4\u5219', type: '\u7c7b\u578b', extension: '\u6269\u5c55', enabled: '\u5df2\u542f\u7528', pendingApproval: '\u5f85\u5ba1\u6279',
    },
  }
}
