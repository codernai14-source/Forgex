/**
 * 个人首页翻译 - 中文
 * 
 * @author Forgex Team
 * @version 1.0.0
 */
export default {
  // Hero 区
  hero: {
    eyebrow: '个人工作台',
    title: '个人首页',
    titleManage: '个人首页默认配置',
    desc: '进入系统后的唯一默认落地页，支持按个人习惯保存组件布局。',
    descManage: '统一维护公共级和租户级默认门户布局，所有用户进入系统后都会以这里为基础。',
    badge: {
      default: '统一默认入口',
      user: '用户级',
      tenant: '租户级',
      public: '公共级',
    },
  },

  // 工具栏
  toolbar: {
    editMode: '编辑布局',
    exitMode: '退出编辑',
    refresh: '刷新',
    resetDefault: '恢复默认',
    saveLayout: '保存布局',
    hint: {
      edit: '拖拽和缩放组件后可直接保存',
      view: '切换到编辑模式后可调整组件布局',
    },
  },

  // 配置面板
  panel: {
    title: '组件配置',
    subtitle: '显隐、条数与更多入口',
  },

  // 组件配置字段
  widget: {
    limit: '显示条数',
    showMore: '显示更多入口',
    more: '更多',
  },

  // 组件标题
  components: {
    commonMenus: {
      title: '常用菜单',
      subtitle: '系统自动统计的固定 Top 6 菜单',
      empty: '暂无常用菜单',
    },
    myFavorites: {
      title: '我的收藏',
      subtitle: '我主动收藏的快捷入口',
      empty: '暂无收藏菜单',
      add: '加入收藏',
      remove: '取消收藏',
    },
    pendingApprovals: {
      title: '我收到的审批',
      subtitle: '我收到的审批待办',
      empty: '暂无待处理审批',
    },
    calendar: {
      title: '日历',
      subtitle: '本地日历视图',
    },
    messages: {
      title: '我收到的消息',
      subtitle: '用户发给我的站内消息',
      empty: '暂无未读消息',
      systemSender: '站内消息',
    },
    notices: {
      title: '系统通知',
      subtitle: '审批与系统类通知',
      empty: '暂无系统通知',
      systemType: '系统通知',
    },
    currentTime: {
      title: '当前时间',
      subtitle: '当前日期与时间',
    },
  },

  // 空状态
  empty: '当前没有启用的首页组件',

  // 消息提示
  message: {
    loadFailed: '加载个人首页配置失败',
    saveSuccess: '个人首页配置已保存',
    saveFailed: '保存个人首页配置失败',
    resetSuccess: '已恢复为默认布局',
    resetFailed: '恢复默认布局失败',
  },

  // 收藏管理页面
  management: {
    title: '收藏管理',
    desc: '统一维护我的收藏菜单，支持调整首页展示顺序和批量取消收藏。',
    alert: '“我的收藏”卡片会按照这里保存的顺序展示；“常用菜单”固定保留系统自动统计的 Top 6。',
    empty: '暂无收藏菜单，请先在首页或菜单中添加收藏。',
    stats: {
      count: '已收藏 {count} 项',
    },
    table: {
      order: '排序',
      menu: '菜单',
      path: '路由路径',
      action: '操作',
    },
    action: {
      refresh: '刷新列表',
      batchCancel: '批量取消收藏',
      saveSort: '保存排序',
      moveUp: '上移',
      moveDown: '下移',
      open: '打开',
      remove: '取消收藏',
    },
    confirm: {
      batchCancelTitle: '确认批量取消收藏',
      batchCancelContent: '确认取消选中的 {count} 个收藏菜单吗？',
      singleCancelTitle: '确认取消收藏',
      singleCancelContent: '确认取消收藏“{title}”吗？',
    },
    message: {
      loadFailed: '加载收藏管理列表失败',
      batchCancelSuccess: '批量取消收藏成功',
      batchCancelFailed: '批量取消收藏失败',
      singleCancelSuccess: '取消收藏成功',
      singleCancelFailed: '取消收藏失败',
      sortSaveSuccess: '收藏排序已保存',
      sortSaveFailed: '保存收藏排序失败',
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
        morning: '愿您精神饱满，开启元气满满的一天',
        afternoon: '适当休息片刻，下午继续高效前行',
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
    onlineDuration: '在线时长',
  },
}