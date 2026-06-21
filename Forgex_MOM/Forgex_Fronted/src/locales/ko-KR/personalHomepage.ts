/**
 * 个人首页翻译 - 韩文注释
 * 
 * @author Forgex Team
 * @version 1.0.0
 */
export default {
  loading: {
    thinking: 'Forgex가 필요한 모든 내용을 불러오는 중입니다. 잠시만 기다려 주세요.',
  },

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
    componentLibrary: '컴포넌트 라이브러리',
    componentConfig: '개인 컴포넌트 설정',
    hint: {
      edit: '컴포넌트를 드래그하여 크기 변경 후 직접 저장하세요',
      view: '편집 모드로 전환하여 컴포넌트 레이아웃을 조정하세요',
    },
  },

  library: {
    title: '홈페이지 컴포넌트 라이브러리',
    searchPlaceholder: '코드, 이름, 용도로 검색',
    scopeAll: '전체',
    scopePublic: '공통',
    scopeTenant: '테넌트',
    scopeUser: '개인',
    defaultGroup: '기본 분류',
    empty: '선택 가능한 컴포넌트가 없습니다',
    favorite: '즐겨찾기',
    selected: '추가됨',
    removed: '제거됨',
    customPlaceholder: '이 컴포넌트는 프론트엔드 등록 컴포넌트로 렌더링되며, 목록은 선택 가능 범위만 관리합니다.',
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
  share: {
    create: '공유 코드 생성',
    import: '레이아웃 가져오기',
    shareTitle: '레이아웃 공유 코드',
    importTitle: '레이아웃 공유 코드 가져오기',
    shareCode: '공유 코드',
    inputPlaceholder: '공유 코드를 입력하세요',
    copy: '복사',
    preview: '미리보기',
    apply: '현재 초안에 적용',
    moduleCode: '모듈',
    createTime: '생성 시각',
    createSuccess: '공유 코드가 생성되었습니다',
    createFailed: '공유 코드 생성에 실패했습니다',
    copySuccess: '공유 코드가 복사되었습니다',
    previewFailed: '유효하지 않은 공유 코드이거나 현재 테넌트에 속하지 않습니다',
    importApplied: '현재 초안에 가져왔습니다. 레이아웃을 저장해야 반영됩니다.',
  },

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
  componentConfig: {
    title: '개인 홈페이지 컴포넌트 설정',
    desc: '현재 사용자의 홈페이지 컴포넌트를 관리하고 즐겨찾기, 추가, 제거를 수행합니다.',
    empty: '설정 가능한 개인 홈페이지 컴포넌트가 없습니다',
    addSuccess: '개인 홈페이지에 추가했습니다',
    removeSuccess: '개인 홈페이지에서 제거했습니다',
    stats: {
      count: '{count}개 그룹',
    },
  },

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
    mode: { editing: '편집 중', view: '보기' },
    toolbar: { exitConfig: '설정 종료', hint: { edit: '카드를 드래그하거나 크기를 조정한 후 저장하세요.', view: '설정에 들어가 모듈 홈 위젯을 조정할 수 있습니다.' } },
    empty: '표시할 위젯이 없습니다',
    action: { enter: '열기' },
    panel: { width: '너비', height: '높이' },
    message: { saveSuccess: '모듈 홈 설정을 저장했습니다', saveFailed: '모듈 홈 설정 저장에 실패했습니다' },
    modules: {
      personal: { name: '개인 홈', title: '개인 홈 기본 설정', desc: '개인 홈의 기본 레이아웃을 관리합니다.' },
      basic: { name: '기본 정보', title: '기본 정보 홈', desc: '기본 정보 모듈의 마스터 데이터 진입점과 설정 상태를 한곳에서 확인합니다.' },
      approval: { name: '승인 관리', title: '승인 관리 홈', desc: '승인 작업, 대기 항목, 워크플로 설정을 한곳에서 확인합니다.' },
      sys: { name: '시스템 관리', title: '시스템 관리 홈', desc: '시스템 실행 상태, 권한 설정, 시스템 파라미터 진입점을 한곳에서 제공합니다.' },
      integration: { name: 'Integration Platform', title: 'Integration Platform Home', desc: 'Shows external system access, API capabilities, call quality and recent exceptions.' },
    },
    widgets: {
      supplierInfo: { title: '공급업체 정보', subtitle: '공급업체 마스터 데이터 및 등록 관리', summary: '공급업체 프로필, 연락처, 자격 및 협업 상태를 관리합니다.' },
      customerInfo: { title: '고객 정보', subtitle: '고객 마스터 데이터 및 연동 관리', summary: '고객 프로필, 연락처, 세금계산서 정보 및 타사 동기화 상태를 관리합니다.' },
      workCalendarInfo: { title: '근무 캘린더', subtitle: '근무일 및 휴일 관리', summary: '기본 정보 모듈의 근무일, 휴일 및 일정 이벤트를 관리합니다.' },
      encodeRuleInfo: { title: '인코딩 규칙', subtitle: '통합 코드 생성 규칙', summary: '기본 정보 모듈의 인코딩 규칙, 일련번호 및 테스트 생성을 관리합니다.' },
      systemOverview: { title: '시스템 개요', subtitle: '조직, 사용자 및 권한 진입점', summary: '사용자, 역할, 메뉴 권한 등 핵심 시스템 기능에 빠르게 접근합니다.' },
      systemHealth: { title: '실행 상태', subtitle: '시스템 실행 및 보안 상태', summary: '온라인 사용자, 로그인 동작, 시스템 실행 관련 정보를 확인합니다.' },
      systemLogs: { title: '작업 로그', subtitle: '시스템 감사 및 추적', summary: '로그인 로그, 작업 로그 등 감사 화면으로 이동합니다.' },
      systemConfig: { title: '시스템 설정', subtitle: '플랫폼 파라미터 및 화면 설정', summary: '포털, 테마, 보안, 메일, 업로드 및 기본 홈 레이아웃을 관리합니다.' },
      approvalStats: { title: '승인 개요', subtitle: '승인 실행 현황', summary: '대기, 처리 완료, 승인 실행의 전체 현황을 확인합니다.' },
      approvalShortcuts: { title: '승인 진입점', subtitle: '승인 시작 및 자주 쓰는 흐름', summary: '시작 가능한 승인 작업과 업무 흐름으로 빠르게 이동합니다.' },
      approvalPending: { title: '내 대기 작업', subtitle: '현재 처리 대기 승인', summary: '현재 사용자가 처리해야 하는 승인 인스턴스를 확인합니다.' },
      approvalTaskConfig: { title: '작업 설정', subtitle: '승인 작업 및 노드 규칙', summary: '작업 양식, 노드 승인자, 워크플로 규칙을 설정합니다.' },
      integrationSummary: { title: 'Integration Overview', subtitle: 'External systems and API capability', summary: 'Shows third-party systems, external APIs, daily calls and success rate.' },
      integrationStatusComparison: { title: 'Success/Failure Comparison', subtitle: 'Successful and failed API calls', summary: 'Compares successful and failed calls in the current statistical window.' },
      integrationStatusPie: { title: 'Call Status Share', subtitle: 'Call result distribution', summary: 'Shows the share of call statuses for API monitoring.' },
      integrationCallTrend: { title: 'Call Trend', subtitle: 'Call volume over the last 14 days', summary: 'Tracks total, successful and failed calls by date.' },
      integrationTopApis: { title: 'Top APIs', subtitle: 'Most frequently called APIs', summary: 'Lists high-frequency external APIs and their call quality.' },
      integrationRecentFailures: { title: 'Recent Failures', subtitle: 'Latest failed calls', summary: 'Shows recent failed calls for troubleshooting and audit.' },
      custom: { subtitle: '사용자 정의 위젯', summary: '저장된 모듈 홈 설정에서 가져온 위젯입니다.' },
    },
    stats: {      masterData: '마스터 데이터', customerArchive: '고객 프로필', integration: '연동', thirdPartySync: '타사 동기화', calendar: '캘린더', workdayMaintain: '근무일 관리', event: '이벤트', holidayShift: '휴일 대체', supplierArchive: '공급업체 프로필', approval: '승인', admissionChange: '등록/변경', rule: '규칙', byModule: '모듈별', capability: '기능', testGenerate: '테스트/생성', user: '사용자', accountManage: '계정 관리', role: '역할', authConfig: '권한 설정', status: '상태', onlineSession: '온라인/세션', security: '보안', loginAudit: '로그인 감사', audit: '감사', operationLog: '작업 로그', trace: '추적', loginRecord: '로그인 기록', scope: '범위', publicTenant: '공용/테넌트', config: '설정', systemParams: '시스템 파라미터', pending: '대기', myTasks: '내 작업', processed: '처리 완료', processRecord: '처리 기록', entry: '진입점', startApproval: '승인 시작', flow: '흐름', taskTemplate: '작업 템플릿', action: '작업', approveReject: '승인/반려', task: '작업', flowConfig: '흐름 설정', node: '노드', approvalRule: '승인 규칙', type: '유형', extension: '확장', enabled: '사용', pendingApproval: '승인 대기', externalApi: 'External APIs', success: 'Success', callSuccess: 'Call success', fail: 'Failure', callFail: 'Call failure', callStatus: 'Call status', callTrace: 'Call trace', trend: 'Trend', last14Days: 'Last 14 days', call: 'Call', callCount: 'Call count', api: 'API', hotApi: 'Hot API', successRate: 'Success rate', quality: 'Quality', exception: 'Exception', failureTrace: 'Failure trace', callAudit: 'Call audit',
    },
  }
}
