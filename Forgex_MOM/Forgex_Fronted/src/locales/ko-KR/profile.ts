/**
 * 个人资料页面
 *
 * @author Forgex
 * @version 1.0.0
 */
export default {
  title: '개인 정보',
  tabs: {
    basic: '기본 정보',
    org: '조직 정보',
    security: '보안',
    personalHomepage: '개인 홈',
  },
  fields: {
    account: '계정',
    username: '사용자 이름',
    email: '이메일',
    phone: '휴대전화',
    gender: '성별',
    department: '부서',
    position: '직위',
    entryDate: '입사일',
    status: '状态',
    oldPassword: '기존 비밀번호',
    newPassword: '새 비밀번호',
    confirmPassword: '비밀번호 확인',
  },
  gender: {
    male: '남',
    female: '여',
    unknown: '알 수 없음',
  },
  actions: {
    save: '저장',
    reset: '초기화',
    changePassword: '비밀번호 변경',
  },
  personalHomepage: {
    title: '내 개인 홈',
    description: '여기에 홈 레이아웃이 저장됩니다. 기본값으로 복원하면 테넌트 또는 공용 설정을 다시 상속합니다.',
  },
  validation: {
    oldPasswordRequired: '기존 비밀번호를 입력하세요',
    newPasswordRequired: '새 비밀번호를 입력하세요',
    newPasswordMin: '비밀번호는 6자 이상이어야 합니다',
    confirmPasswordRequired: '새 비밀번호를 확인하세요',
    passwordMismatch: '비밀번호가 일치하지 않습니다',
  },
  message: {
    loadFailed: '사용자 정보를 불러오지 못했습니다',
    passwordChanged: '비밀번호가 변경되었습니다. 다시 日志인하세요.',
    passwordChangeFailed: '비밀번호 변경에 실패했습니다',
  },
}
