/**
 * Personal Homepage Translations - English
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
      subtitle: 'Frequently used entries and recently visited menus',
      empty: 'No common menus',
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
      title: 'My Messages',
      subtitle: 'Messages sent to me by users',
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

  // Messages
  message: {
    loadFailed: 'Failed to load homepage configuration',
    saveSuccess: 'Homepage configuration saved',
    saveFailed: 'Failed to save homepage configuration',
    resetSuccess: 'Reset to default layout',
    resetFailed: 'Failed to reset to default layout',
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
        afternoon: 'Keep the momentum — remember to pause and breathe now and then.',
        evening: "You've put in a solid day — unwind and recharge tonight.",
      },
      lineEnMale: 'Dear Mr. {name}, {lead} — {closing}',
      lineEnFemale: 'Dear Ms. {name}, {lead} — {closing}',
      lineEnNeutral: 'Dear {name}, {lead} — {closing}',
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
  },
}