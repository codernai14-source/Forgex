/**
 * 개인 홈 번역 - 한국어
 * 
 * @author Forgex Team
 * @version 1.0.0
 */
export default {
  // Hero 섹션
  hero: {
    eyebrow: '개인 워크스페이스',
    title: '개인 홈',
    titleManage: '개인 홈 초기 설정',
    desc: '시스템 로그인 후 초기 표시 페이지로, 개인의 취향에 따라 컴포넌트 레이아웃을 저장할 수 있습니다.',
    descManage: '퍼블릭 레벨과 테넌트 레벨의 초기 포털 레이아웃을 일괄 관리하며, 모든 사용자가 이를 기반으로 시스템을 이용합니다.',
    badge: {
      default: '초기 진입',
      user: '사용자 레벨',
      tenant: '테넌트 레벨',
      public: '퍼블릭 레벨',
    },
  },

  // 툴바
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

  // 설정 패널
  panel: {
    title: '위젯 설정',
    subtitle: '표시/비표시, 건수 및 기타 옵션',
  },

  // 위젯 설정 필드
  widget: {
    limit: '표시 건수',
    showMore: '더 보기 링크 표시',
    more: '더 보기',
  },

  // 위젯 제목
  components: {
    commonMenus: {
      title: '자주 쓰는 메뉴',
      subtitle: '자주 사용하는 진입 및 최근 방문한 메뉴',
      empty: '자주 쓰는 메뉴가 없습니다',
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
      subtitle: '현재 날짜 및 시간',
    },
  },

  // 빈 상태
  empty: '활성화된 홈 위젯이 없습니다',

  // 메시지
  message: {
    loadFailed: '홈 설정을 불러오지 못했습니다',
    saveSuccess: '홈 설정을 저장했습니다',
    saveFailed: '홈 설정 저장에 실패했습니다',
    resetSuccess: '초기 레이아웃으로 리셋했습니다',
    resetFailed: '초기 레이아웃으로 리셋하는 데 실패했습니다',
  },

  // 요약 카드
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
    onlineDuration: '온라인 시간',
  },
}