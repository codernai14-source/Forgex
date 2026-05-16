/**
 * 部门管理 - 韩语界面文案
 */
export default {
  // 页面
  title: '부서 관리',
  list: '부서 목록',
  detail: '부서 상세',

  // 表格列
  deptName: '부서 이름',
  deptCode: '부서 코드',
  orgType: '조직 유형',
  orgLevel: '조직 단계',
  leader: '담당자',
  phone: '전화번호',
  email: '이메일',
  orderNum: '정렬 번호',
  status: '상태',
  createTime: '생성 시간',
  updateTime: '수정 시간',
  createBy: '생성자',
  updateBy: '수정자',

  // 表单
  form: {
    addDept: '부서 추가',
    editDept: '부서 편집',
    deptName: '부서 이름을 입력하세요',
    deptCode: '부서 코드를 입력하세요',
    orgType: '조직 유형을 선택하세요',
    orgLevel: '조직 단계를 입력하세요',
    leader: '담당자를 입력하세요',
    phone: '전화번호를 입력하세요',
    email: '이메일을 입력하세요',
    orderNum: '정렬 번호를 입력하세요',
    status: '상태를 선택하세요',
  },

  // 操作
  childDept: '하위 부서',
  addRootDept: '최상위 부서 추가',
  emptySelectTip: '상세를 보려면 좌측에서 부서를 선택하세요',

  // 消息
  message: {
    loadTreeFailed: '조직 트리를 불러오지 못했습니다',
    deleteConfirm: '이 부서를 삭제하시겠습니까?',
    deleteSuccess: '부서가 삭제되었습니다',
    saveSuccess: '부서가 저장되었습니다',
    deptCodeExists: '이미 존재하는 부서 코드입니다',
    hasChildren: '하위 부서가 있어 삭제할 수 없습니다',
  },
}
