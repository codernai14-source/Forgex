/**
 * 个人首页翻译 - 中文注释
 * 
 * @author Forgex Team
 * @version 1.0.0
 */
export default {
  loading: {
    thinking: 'Forgex is loading everything you need. Please wait.',
  },

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
    componentLibrary: 'Component Library',
    componentConfig: 'Personal Component Config',
    hint: {
      edit: 'Drag and resize components, then save directly',
      view: 'Switch to edit mode to adjust component layout',
    },
  },

  library: {
    title: 'Homepage Component Library',
    searchPlaceholder: 'Search by code, name, or purpose',
    scopeAll: 'All',
    scopePublic: 'Public',
    scopeTenant: 'Tenant',
    scopeUser: 'Personal',
    defaultGroup: 'Default Group',
    empty: 'No available components',
    favorite: 'Favorite',
    selected: 'Added',
    removed: 'Removed',
    customPlaceholder: 'This widget is rendered by a frontend registered component; the catalog only controls availability.',
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
  share: {
    create: 'Create Share Code',
    import: 'Import Layout',
    shareTitle: 'Layout Share Code',
    importTitle: 'Import Layout Share Code',
    shareCode: 'Share Code',
    inputPlaceholder: 'Enter a share code',
    copy: 'Copy',
    preview: 'Preview',
    apply: 'Apply to Draft',
    moduleCode: 'Module',
    createTime: 'Created At',
    createSuccess: 'Share code created',
    createFailed: 'Failed to create share code',
    copySuccess: 'Share code copied',
    previewFailed: 'Invalid share code or it does not belong to this tenant',
    importApplied: 'Imported into the current draft. Save the layout to make it effective.',
  },

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

  componentConfig: {
    title: 'Personal Homepage Component Config',
    desc: 'Manage the current user homepage components, including personal add, remove, and favorite actions.',
    empty: 'No configurable personal homepage components',
    addSuccess: 'Added to personal homepage',
    removeSuccess: 'Removed from personal homepage',
    stats: {
      count: '{count} groups',
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
        integration: { name: 'Integration Platform', title: 'Integration Dashboard', desc: 'Shows external API capabilities, call trends, and abnormal call status.' },
      },
    widgets: {
      supplierInfo: { title: 'Supplier Information', subtitle: 'Supplier master data and admission maintenance', summary: 'Manage supplier profiles, contacts, qualifications, and collaboration status.' },
      customerInfo: { title: 'Customer Information', subtitle: 'Customer master data and integration maintenance', summary: 'Manage customer profiles, contacts, invoice data, and third-party sync status.' },
      workCalendarInfo: { title: 'Work Calendar', subtitle: 'Workday and holiday maintenance', summary: 'Maintain workdays, holidays, and scheduling events for Basic Information.' },
      encodeRuleInfo: { title: 'Encoding Rules', subtitle: 'Unified code generation rules', summary: 'Maintain encoding rules, serial numbers, and test generation for basic information.' },
      systemOverview: { title: 'System Overview', subtitle: 'Organization, users, and permission entries', summary: 'Quick access to users, roles, menu grants, and other core system capabilities.' },
      systemHealth: { title: 'Runtime Status', subtitle: 'System runtime and security status', summary: 'View online users, login behavior, and system runtime information.' },
      systemLogs: { title: 'Operation Logs', subtitle: 'System audit and tracing', summary: 'Open login logs, operation logs, and other audit pages.' },
      systemConfig: { title: 'System Config', subtitle: 'Platform parameters and appearance settings', summary: 'Maintain portal, theme, security, mail, upload, and default homepage layout settings.' },
      approvalStats: { title: 'Approval Overview', subtitle: 'Approval operation summary', summary: 'View pending, processed, and overall approval execution status.' },
      approvalShortcuts: { title: 'Approval Entry', subtitle: 'Start approvals and common flows', summary: 'Quickly open approval tasks and business workflows that can be started.' },
        approvalPending: { title: 'My Pending Tasks', subtitle: 'Current pending approvals', summary: 'View approval instances that require the current user to process.' },
        approvalTaskConfig: { title: 'Task Config', subtitle: 'Approval tasks and node rules', summary: 'Configure task forms, node approvers, and workflow rules.' },
        integrationSummary: { title: 'API Capability Summary', subtitle: 'Third-party systems and external APIs', summary: 'Shows third-party systems, API directions, today calls, and success rate.' },
        integrationStatusComparison: { title: 'Success/Fail Comparison', subtitle: 'Call result comparison', summary: 'Compares successful and failed calls in the last 14 days.' },
        integrationStatusPie: { title: 'Call Status Share', subtitle: 'Status structure analysis', summary: 'Shows success, failure, and other call status proportions.' },
        integrationCallTrend: { title: 'Call Trend', subtitle: 'Last 14 days call curve', summary: 'Shows total, successful, and failed call trends over time.' },
        integrationTopApis: { title: 'Top APIs', subtitle: 'API call popularity', summary: 'Shows APIs with the most calls and their success rates.' },
        integrationRecentFailures: { title: 'Recent Failed Calls', subtitle: 'Abnormal call tracing', summary: 'Shows recent failed calls, error messages, and cost time.' },
        custom: { subtitle: 'Custom Widget', summary: 'This widget comes from a saved module homepage configuration.' },
      },
      stats: {
        masterData: 'Master Data', supplierArchive: 'Supplier Profiles', customerArchive: 'Customer Profiles', integration: 'Integration', thirdPartySync: 'Third-party Sync', calendar: 'Calendar', workdayMaintain: 'Workday Maintenance', event: 'Event', holidayShift: 'Holiday/Shift', approval: 'Approval', admissionChange: 'Admission/Change', rule: 'Rule', byModule: 'By Module', capability: 'Capability', externalApi: 'External APIs', testGenerate: 'Test/Generate', user: 'User', accountManage: 'Account Management', role: 'Role', authConfig: 'Authorization Config', status: 'Status', onlineSession: 'Online/Session', security: 'Security', loginAudit: 'Login Audit', audit: 'Audit', operationLog: 'Operation Logs', trace: 'Trace', loginRecord: 'Login Records', scope: 'Scope', publicTenant: 'Public/Tenant', config: 'Config', systemParams: 'System Parameters', pending: 'Pending', myTasks: 'My Tasks', processed: 'Processed', processRecord: 'Processing Records', entry: 'Entry', startApproval: 'Start Approval', flow: 'Flow', taskTemplate: 'Task Templates', action: 'Action', approveReject: 'Approve/Reject', task: 'Task', flowConfig: 'Flow Config', node: 'Node', approvalRule: 'Approval Rules', type: 'Type', extension: 'Extension', enabled: 'Enabled', pendingApproval: 'Pending Approval', success: 'Success', callSuccess: 'Call Success', fail: 'Fail', callFail: 'Call Fail', callStatus: 'Call Status', callTrace: 'Call Trace', trend: 'Trend', last14Days: 'Last 14 Days', call: 'Call', callCount: 'Call Count', api: 'API', hotApi: 'Hot APIs', successRate: 'Success Rate', quality: 'Quality', exception: 'Exception', failureTrace: 'Failure Trace', callAudit: 'Call Audit',
      },
  }
}
