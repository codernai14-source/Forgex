/**
 * Fallback page translations - Korean
 */
export default {
  actions: {
    home: '홈으로 돌아가기',
    back: '이전 페이지',
    retry: '다시 연결',
  },
  panel: {
    title: '상태 점검',
  },
  '403': {
    eyebrow: '권한 확인 실패',
    title: '접근이 거부되었습니다',
    description: '현재 계정에는 이 기능에 접근할 권한이 없습니다. 역할 권한, 테넌트 범위 또는 관리자에게 메뉴와 작업 권한 부여 여부를 확인하세요.',
    status: '접근 경로 차단됨',
    checks: {
      permission: '메뉴와 작업 권한이 부여되었는지 확인',
      role: '현재 역할에 대상 기능이 포함되는지 확인',
      tenant: '현재 테넌트에서 이 모듈에 접근 가능한지 확인',
    },
  },
  '404': {
    eyebrow: '라우트 불일치',
    title: '페이지를 찾을 수 없습니다',
    description: '요청한 페이지를 찾을 수 없습니다. 링크가 변경되었거나 메뉴가 게시되지 않았거나 모듈 라우트가 설정되지 않았을 수 있습니다.',
    status: '유효한 페이지 없음',
    checks: {
      route: '접속 주소가 올바른지 확인',
      menu: '메뉴가 게시되고 활성화되었는지 확인',
      link: '홈 또는 모듈 메뉴에서 다시 진입',
    },
  },
  offline: {
    eyebrow: '네트워크 연결 이상',
    title: '서비스에 연결할 수 없습니다',
    description: '현재 Forgex 서비스에 연결할 수 없습니다. 네트워크, 게이트웨이 또는 VPN 상태를 확인하고 복구 후 다시 연결하세요.',
    status: '연결 복구 대기',
    checks: {
      network: '로컬 네트워크와 사내망 연결 확인',
      gateway: 'API 게이트웨이 또는 백엔드 서비스 사용 가능 여부 확인',
      retry: '연결 복구 후 다시 연결하여 페이지 새로고침',
    },
  },
}
