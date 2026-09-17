export default {
  action: {
    kickout: '강제 로그아웃',
    batchKickout: '일괄 강제 로그아웃',
    detail: '상세',
  },
  terminal: {
    all: '전체',
    b: 'B단',
    c: 'C단',
    thirdParty: '서드파티',
  },
  ttl: {
    longTerm: '장기',
    second: '{count}s',
    minuteSecond: '{minute}m {second}s',
  },
  confirm: {
    kickoutTitle: '강제 로그아웃 확인',
    kickoutContent: '이 작업을 수행하면 해당 세션이 즉시 무효화됩니다.',
    batchKickoutTitle: '일괄 강제 로그아웃 확인',
    batchKickoutContent: '선택한 {count}개의 세션에 대해 강제 로그아웃을 수행합니다.',
    onlyKickout: '로그아웃만 수행 (사용자 활성 유지)',
    kickoutAndDisable: '로그아웃 후 사용자 비활성화',
    batchTarget: '선택한 {count}명의 사용자',
  },
  detail: {
    title: '온라인 세션 상세',
    account: '계정',
    username: '사용자 이름',
    tenant: '테넌트',
    sessionCount: '활성 세션 수',
    token: 'Token',
    terminal: '로그인 단말',
    region: '로그인 지역',
    ttl: '남은 세션 시간',
  },
}
