/**
 * 个人首页翻译 - 中文注释
 * 
 * @author Forgex Team
 * @version 1.0.0
 */
export default {
  // Hero Section
  hero: {
    eyebrow: 'Personal Workspace',
    title: 'Personal Homepage',
    titleManage: 'Personal Homepage Default Config',
    desc: 'The default landing page after entering the system, supports saving component layouts according to personal preferences.',
    descManage: 'Unified maintenance of public-level and tenant-level default portal layouts, all users will use this as the basis after entering the system.',
    badge: {
      default: 'Default Entry',
      user: 'User Level',
      tenant: 'Tenant Level',
      public: 'Public Level',
    },
  },

  // Toolbar
  toolbar: {
    editMode: 'Edit Layout',
    exitMode: 'Exit Edit',
    refresh: 'Refresh',
    resetDefault: 'Reset to Default',
    saveLayout: 'Save Layout',
    hint: {
      edit: 'Drag and resize components, then save directly',
      view: 'Switch to edit mode to adjust component layout',
    },
  },

  // Configuration Panel
  panel: {
    title: 'Widget Configuration',
    subtitle: 'Visibility, count and more options',
  },

  // Widget Configuration Fields
  widget: {
    limit: 'Display Count',
    showMore: 'Show More Link',
    more: 'More',
  },

  // Widget Titles
  components: {
    commonMenus: {
      title: 'Common Menus',
      subtitle: 'System-ranked fixed Top 6 menus',
      empty: 'No common menus',
    },
    myFavorites: {
      title: 'My Favorites',
      subtitle: 'Shortcuts I pinned myself',
      empty: 'No favorite menus',
      add: 'Add to favorites',
      remove: 'Remove from favorites',
    },
    pendingApprovals: {
      title: 'Pending Approvals',
      subtitle: 'Approval tasks assigned to me',
      empty: 'No pending approvals',
    },
    calendar: {
      title: 'Calendar',
      subtitle: 'Local calendar view',
    },
    messages: {
      title: 'My 消息',
      subtitle: '消息 sent to me by users',
      empty: 'No unread messages',
      systemSender: 'System Message',
    },
    notices: {
      title: 'System Notices',
      subtitle: 'Approval and system notifications',
      empty: 'No system notices',
      systemType: 'System Notice',
    },
    currentTime: {
      title: 'Current Time',
      subtitle: 'Current date and time',
    },
  },

  // Empty State
  empty: 'No enabled homepage widgets',

  // 消息
  message: {
    loadFailed: 'Failed to load homepage configuration',
    saveSuccess: 'Homepage configuration saved',
    saveFailed: 'Failed to save homepage configuration',
    resetSuccess: 'Reset to default layout',
    resetFailed: 'Failed to reset to default layout',
  },

  // Favorite Management Page
  management: {
    title: 'Favorite Management',
    desc: 'Manage all favorite menus in one place, including homepage order and batch removal.',
    alert: 'The “My Favorites” card follows the order saved here. “Common Menus” always keeps the system-ranked Top 6.',
    empty: 'No favorite menus yet. Add favorites from the homepage or menu first.',
    stats: {
      count: '{count} favorites',
    },
    table: {
      order: 'Order',
      menu: 'Menu',
      path: 'Route Path',
      action: 'Action',
    },
    action: {
      refresh: 'Refresh',
      batchCancel: 'Batch Remove',
      saveSort: 'Save Order',
      moveUp: 'Move Up',
      moveDown: 'Move Down',
      open: 'Open',
      remove: 'Remove',
    },
    confirm: {
      batchCancelTitle: 'Confirm Batch Remove',
      batchCancelContent: 'Remove the selected {count} favorite menus?',
      singleCancelTitle: 'Confirm Remove',
      singleCancelContent: 'Remove “{title}” from favorites?',
    },
    message: {
      loadFailed: 'Failed to load favorite menus',
      batchCancelSuccess: 'Favorites removed successfully',
      batchCancelFailed: 'Failed to remove selected favorites',
      singleCancelSuccess: 'Favorite removed successfully',
      singleCancelFailed: 'Failed to remove favorite',
      sortSaveSuccess: 'Favorite order saved',
      sortSaveFailed: 'Failed to save favorite order',
    },
  },

  // Summary Card
  summary: {
    greeting: {
      honorificMale: 'Mr.',
      honorificFemale: 'Ms.',
      lead: {
        morning: 'Good morning',
        afternoon: 'Good afternoon',
        evening: 'Good evening',
      },
      closing: {
        morning: "Here's to a fresh start and a focused, productive day ahead.",
        afternoon: 'Keep the momentum 鈥?remember to pause and breathe now and then.',
        evening: "You've put in a solid day 鈥?unwind and recharge tonight.",
      },
      lineEnMale: 'Dear Mr. {name}, {lead} 鈥?{closing}',
      lineEnFemale: 'Dear Ms. {name}, {lead} 鈥?{closing}',
      lineEnNeutral: 'Dear {name}, {lead} 鈥?{closing}',
    },
    weekday: {
      0: 'Sunday',
      1: 'Monday',
      2: 'Tuesday',
      3: 'Wednesday',
      4: 'Thursday',
      5: 'Friday',
      6: 'Saturday',
    },
    todayLineEn: 'Today is {weekday}, {month}/{day}',
    onlineDuration: 'Online Duration',
    zeroMinutes: '0 minutes',
  },
  module: {
    mode: { editing: 'Editing', view: 'View' },
    toolbar: { exitConfig: 'Exit Config', hint: { edit: 'Drag cards or resize them, then save.', view: 'Enter configuration to adjust module homepage widgets.' } },
    empty: 'No visible widgets',
    action: { enter: 'Enter' },
    panel: { width: 'Width', height: 'Height' },
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
