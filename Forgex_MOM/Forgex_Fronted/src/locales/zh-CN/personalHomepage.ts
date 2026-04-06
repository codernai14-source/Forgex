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
      subtitle: '常看入口和最近访问菜单',
      empty: '暂无常用菜单',
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

  // 摘要卡片
  summary: {
    greeting: {
      morning: '早上好，祝您工作愉快！',
      afternoon: '下午好，继续加油！',
      evening: '晚上好，注意休息！',
    },
    onlineDuration: '在线时长',
  },
}