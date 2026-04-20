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
  },
}