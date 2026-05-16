/**
 * 个人首页翻译 - 中文
 * 
 * @author Forgex Team
 * @version 1.0.0
 */
export default {
  loading: {
    thinking: 'Forgex正在加载您所需的任何内容，请稍等',
  },

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
    componentLibrary: '组件库',
    componentConfig: '个人组件配置',
    hint: {
      edit: '拖拽和缩放组件后可直接保存',
      view: '切换到编辑模式后可调整组件布局',
    },
  },

  library: {
    title: '首页组件库',
    searchPlaceholder: '按编码、名称、作用搜索',
    scopeAll: '全部',
    scopePublic: '公共',
    scopeTenant: '租户',
    scopeUser: '个人',
    defaultGroup: '默认分类',
    empty: '暂无可选组件',
    favorite: '已收藏',
    selected: '已添加',
    removed: '已移除',
    customPlaceholder: '该组件由前端注册组件承载，目录仅维护可选范围。',
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

  componentConfig: {
    title: '个人首页组件配置',
    desc: '统一维护当前用户可见的首页组件，支持收藏、添加和移除个人组件。',
    empty: '当前没有可配置的个人首页组件',
    addSuccess: '已添加到个人首页',
    removeSuccess: '已从个人首页移除',
    stats: {
      count: '共 {count} 个分组',
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
