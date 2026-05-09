/**
 * 个人首页翻译 - 韩文注释
 * 
 * @author Forgex Team
 * @version 1.0.0
 */
export default {
  // 头部区域
  hero: {
    eyebrow: '개인 워크스페이스',
    title: '개인 홈',
    titleManage: '개인 홈 초기 설정',
    desc: '시스템 日志인 후 초기 표시 페이지로, 개인의 취향에 따라 컴포넌트 레이아웃을 저장할 수 있습니다.',
    descManage: '퍼블릭 레벨과 테넌트 레벨의 초기 포털 레이아웃을 일괄 관리하며, 모든 사용자가 이를 기반으로 시스템을 이용합니다.',
    badge: {
      default: '초기 진입',
      user: '사용자 레벨',
      tenant: '테넌트 레벨',
      public: '퍼블릭 레벨',
    },
  },

  // 工具栏
  toolbar: {
    editMode: '레이아웃 편집',
    exitMode: '편집 종료',
    refresh: '새로고침',
    resetDefault: '초기값으로 리셋',
    saveLayout: '레이아웃 저장',
    hint: {
      edit: '컴포넌트를 드래그하여 크기 변경 후 직접 저장하세요',
      view: '편집 모드로 전환하여 컴포넌트 레이아웃을 조정하세요',
    },
  },

  // 设置面板
  panel: {
    title: '위젯 설정',
    subtitle: '표시/비표시, 건수 및 其他 옵션',
  },

  // 组件配置字段
  widget: {
    limit: '표시 건수',
    showMore: '더 보기 링크 표시',
    more: '더 보기',
  },

  // 组件标题
  components: {
    commonMenus: {
      title: '자주 쓰는 메뉴',
      subtitle: '시스템이 집계한 고정 Top 6 메뉴',
      empty: '자주 쓰는 메뉴가 없습니다',
    },
    myFavorites: {
      title: '내 즐겨찾기',
      subtitle: '내가 직접 고정한 바로가기',
      empty: '즐겨찾기 메뉴가 없습니다',
      add: '즐겨찾기에 추가',
      remove: '즐겨찾기 해제',
    },
    pendingApprovals: {
      title: '승인 대기',
      subtitle: '나에게 할당된 승인 작업',
      empty: '승인 대기 작업이 없습니다',
    },
    calendar: {
      title: '캘린더',
      subtitle: '로컬 캘린더 뷰',
    },
    messages: {
      title: '나의 메시지',
      subtitle: '사용자로부터 수신된站内 메시지',
      empty: '읽지 않은 메시지가 없습니다',
      systemSender: '站内 메시지',
    },
    notices: {
      title: '시스템 알림',
      subtitle: '승인 및 시스템 알림',
      empty: '시스템 알림이 없습니다',
      systemType: '시스템 알림',
    },
    currentTime: {
      title: '현재 시각',
      subtitle: '현재 날짜 및 时间',
    },
  },

  // 空状态
  empty: '활성화된 홈 위젯이 없습니다',

  // 消息
  message: {
    loadFailed: '홈 설정을 불러오지 못했습니다',
    saveSuccess: '홈 설정을 저장했습니다',
    saveFailed: '홈 설정 저장에 실패했습니다',
    resetSuccess: '초기 레이아웃으로 리셋했습니다',
    resetFailed: '초기 레이아웃으로 리셋하는 데 실패했습니다',
  },

  // 즐겨찾기 관리 페이지
  management: {
    title: '즐겨찾기 관리',
    desc: '즐겨찾기 메뉴를 한 곳에서 관리하고, 홈 표시 순서 조정과 일괄 해제를 지원합니다.',
    alert: '“내 즐겨찾기” 카드는 여기서 저장한 순서대로 표시됩니다. “자주 쓰는 메뉴”는 시스템이 집계한 Top 6만 고정 표시됩니다.',
    empty: '즐겨찾기 메뉴가 없습니다. 먼저 홈이나 메뉴에서 즐겨찾기를 추가하세요.',
    stats: {
      count: '{count}개 즐겨찾기',
    },
    table: {
      order: '순서',
      menu: '메뉴',
      path: '라우트 경로',
      action: '작업',
    },
    action: {
      refresh: '새로고침',
      batchCancel: '일괄 해제',
      saveSort: '정렬 저장',
      moveUp: '위로',
      moveDown: '아래로',
      open: '열기',
      remove: '해제',
    },
    confirm: {
      batchCancelTitle: '일괄 해제 확인',
      batchCancelContent: '선택한 {count}개의 즐겨찾기를 해제하시겠습니까?',
      singleCancelTitle: '해제 확인',
      singleCancelContent: '“{title}” 즐겨찾기를 해제하시겠습니까?',
    },
    message: {
      loadFailed: '즐겨찾기 목록을 불러오지 못했습니다',
      batchCancelSuccess: '선택한 즐겨찾기를 해제했습니다',
      batchCancelFailed: '일괄 해제에 실패했습니다',
      singleCancelSuccess: '즐겨찾기를 해제했습니다',
      singleCancelFailed: '즐겨찾기 해제에 실패했습니다',
      sortSaveSuccess: '즐겨찾기 순서를 저장했습니다',
      sortSaveFailed: '즐겨찾기 순서 저장에 실패했습니다',
    },
  },

  // 摘要卡片
  summary: {
    greeting: {
      honorificMale: '님',
      honorificFemale: '님',
      lead: {
        morning: '좋은 아침입니다',
        afternoon: '안녕하세요',
        evening: '좋은 저녁입니다',
      },
      closing: {
        morning: '활기차고 생산적인 하루를 시작하세요.',
        afternoon: '리듬을 유지하세요. 가끔은 멈춰서 심호흡을 하세요.',
        evening: '충실한 하루였습니다. 오늘 밤은 편안하게 쉬세요.',
      },
      lineKoMale: '{name} {honorific}, {lead} — {closing}',
      lineKoFemale: '{name} {honorific}, {lead} — {closing}',
      lineKoNeutral: '{name} {honorific}, {lead} — {closing}',
    },
    weekday: {
      0: '일요일',
      1: '월요일',
      2: '화요일',
      3: '수요일',
      4: '목요일',
      5: '금요일',
      6: '토요일',
    },
    todayLineKo: '오늘은 {month}월{day}일 {weekday}',
    onlineDuration: '온라인 时间',
    zeroMinutes: '0?',
  },
  module: {
    mode: { editing: '\ud3b8\uc9d1 \uc911', view: '\ubcf4\uae30' },
    toolbar: { exitConfig: '\uc124\uc815 \uc885\ub8cc', hint: { edit: 'Drag cards or resize them, then save.', view: 'Enter configuration to adjust module homepage widgets.' } },
    empty: '\ud45c\uc2dc\ud560 \uc704\uc82f\uc774 \uc5c6\uc2b5\ub2c8\ub2e4',
    action: { enter: '\uc5f4\uae30' },
    panel: { width: '\ub108\ube44', height: '\ub192\uc774' },
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
