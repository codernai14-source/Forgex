/*
 * Copyright 2026 coder_nai@163.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

export default {
  title: '테넌트 메시지 화이트리스트',
  guide: {
    title: '용도 안내',
    description: '테넌트 메시지 화이트리스트는 테넌트 간 메시지 발송 권한을 제어합니다. 발신 테넌트가 수신 테넌트로 메시지를 보낼 수 있도록 허용된 경우에만 교차 테넌트 메시지 전송이 가능합니다. 동일 테넌트 메시지와 플랫폼 테넌트(tenantId=0)는 제한되지 않습니다.',
  },
  modal: {
    addTitle: '화이트리스트 추가',
    editTitle: '화이트리스트 수정',
  },
  form: {
    senderTenant: '발신 테넌트',
    senderTenantPlaceholder: '발신 테넌트를 선택하세요',
    receiverTenant: '수신 테넌트',
    receiverTenantPlaceholder: '수신 테넌트를 선택하세요',
    remarkPlaceholder: '비고를 입력하세요',
  },
  confirm: {
    deleteContent: '이 화이트리스트 설정을 삭제하시겠습니까?',
    batchDeleteTitle: '삭제 확인',
    batchDeleteContent: '선택한 {count}개의 화이트리스트 설정을 삭제하시겠습니까?',
  },
  message: {
    selectSenderTenant: '발신 테넌트를 선택하세요',
    selectReceiverTenant: '수신 테넌트를 선택하세요',
    sameTenantNotAllowed: '발신 테넌트와 수신 테넌트는 같을 수 없습니다',
    saveSuccess: '화이트리스트가 저장되었습니다',
    deleteSuccess: '화이트리스트가 삭제되었습니다',
  },
}

