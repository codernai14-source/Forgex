/**
 * 사용자 관리 번역 - 한국어
 */
export default {
  // 페이지 제목
  title: '사용자 관리',
  userManagement: '사용자 관리',
  list: '사용자 목록',
  detail: '사용자 상세',

  // 작업
  add: '사용자 추가',
  edit: '수정',
  delete: '삭제',
  export: '내보내기',
  resetPassword: '비밀번호 재설정',
  lockUser: '사용자 잠금',
  unlockUser: '사용자 잠금 해제',
  assignRole: '역할 할당',
  viewDetail: '상세 보기',

  // 역할 할당 팝업
  roleAssign: {
    searchPlaceholder: '역할명 또는 코드 검색',
    selectAll: '전체 선택',
    clearAll: '전체 해제',
    roleName: '역할명',
    roleCode: '역할 코드',
    assigned状态: '할당 상태',
    assigned: '할당됨',
    summary: '총 {total}개 역할, {selected}개 선택됨',
    loadFailed: '역할 데이터를 불러오지 못했습니다',
    saveFailed: '저장 실패',
    missingUserId: '사용자 ID가 없습니다',
  },
  statusActive: '활성',
  statusInactive: '비활성',
  statusLocked: '잠금',

  // 테이블 열
  avatar: '아바타',
  username: '사용자명',
  realName: '실명',
  email: '이메일',
  phone: '휴대폰 번호',
  gender: '성별',
  department: '부서',
  position: '직위',
  role: '역할',
  entryDate: '입사일',
  status: '상태',
  createTime: '생성 시간',
  createBy: '생성자',
  updateTime: '수정 시간',
  updateBy: '수정자',
  lastLoginTime: '마지막 로그인 시간',
  lastLoginIp: '마지막 로그인 IP',
  lastLoginRegion: '마지막 로그인 지역',
  action: '작업',

  // 성별
  genderOptions: {
    unknown: '알 수 없음',
    male: '남',
    female: '여',
  },

  // 폼
  form: {
    addUser: '사용자 추가',
    editUser: '사용자 수정',
    userDetail: '사용자 상세',
    username: '사용자명을 입력하세요',
    realName: '실명을 입력하세요',
    email: '이메일을 입력하세요',
    phone: '휴대폰 번호를 입력하세요',
    password: '비밀번호를 입력하세요',
    confirmPassword: '비밀번호를 다시 입력하세요',
    department: '부서를 선택하세요',
    role: '역할을 선택하세요',
    status: '상태를 선택하세요',
  },

  // 메시지
  message: {
    loadListFailed: '사용자 목록을 불러오지 못했습니다',
    deleteFailed: '삭제 실패',
    batchDeleteFailed: '일괄 삭제 실패',
    selectToDelete: '삭제할 사용자를 선택하세요',
    deleteConfirm: '이 사용자를 삭제하시겠습니까?',
    batchDeleteConfirm: '선택한 {count}명의 사용자를 삭제하시겠습니까?',
    deleteSuccess: '사용자를 삭제했습니다',
    saveSuccess: '사용자를 저장했습니다',
    resetPasswordSuccess: '비밀번호를 재설정했습니다',
    resetPasswordFailed: '비밀번호 재설정 실패',
    resetPasswordConfirm: '이 사용자의 비밀번호를 재설정하시겠습니까? 시스템 기본 비밀번호로 재설정됩니다.',
    lockSuccess: '사용자를 잠갔습니다',
    unlockSuccess: '사용자 잠금을 해제했습니다',
    usernameExists: '사용자명이 이미 존재합니다',
    emailExists: '이메일이 이미 존재합니다',
    update状态Failed: '상태 변경 실패',
  },
}
