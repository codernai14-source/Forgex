import { readFileSync, writeFileSync } from 'node:fs'
import { join } from 'node:path'
import { fileURLToPath } from 'node:url'

const root = join(fileURLToPath(new URL('.', import.meta.url)), '..')
const file = join(root, 'src/locales/staticViewText.ts')
const source = readFileSync(file, 'utf8')

const entries = []
const entryRe = /  '((?:\\'|[^'])+)': \{\n    'en-US': '((?:\\'|[^'])*)',\n    'zh-TW': '((?:\\'|[^'])*)',\n    'ja-JP': '((?:\\'|[^'])*)',\n    'ko-KR': '((?:\\'|[^'])*)',\n  \}/g
let match
while ((match = entryRe.exec(source))) {
  entries.push({
    key: match[1].replace(/\\'/g, "'"),
    vals: [match[2], match[3], match[4], match[5]],
  })
}

const map = new Map()
function add(zh, en, tw, ja, ko) {
  map.set(zh, [en, tw, ja, ko])
}

add('政治面貌', 'Political Status', '政治面貌', '政治的立場', '정치 성향')
add('绑定维度（智能匹配使用）', 'Binding Dimension (for Smart Matching)', '綁定維度（智慧匹配使用）', 'バインド軸（スマート照合用）', '바인딩 차원(스마트 매칭용)')
add('包装编码', 'Packaging Code', '包裝編碼', '包装コード', '포장 코드')
add('包装材料', 'Packaging Material', '包裝材料', '包装材料', '포장재')
add('包装名称', 'Packaging Name', '包裝名稱', '包装名', '포장명')
add('报表编码', 'Report Code', '報表編碼', 'レポートコード', '보고서 코드')
add('报表分类', 'Report Category', '報表分類', 'レポートカテゴリ', '보고서 분류')
add('报表名称', 'Report Name', '報表名稱', 'レポート名', '보고서명')
add('报表状态', 'Report Status', '報表狀態', 'レポート状態', '보고서 상태')
add('币种', 'Currency', '幣別', '通貨', '통화')
add('币种主数据', 'Currency Master Data', '幣別主資料', '通貨マスターデータ', '통화 마스터 데이터')
add('编码', 'Code', '編碼', 'コード', '코드')
add('补偿中心', 'Compensation Center', '補償中心', '補償センター', '보상 센터')
add('不选则自动匹配', 'Leave blank to auto match', '不選則自動匹配', '未選択の場合は自動照合', '선택하지 않으면 자동 매칭')
add('操作模块', 'Operation Module', '操作模組', '操作モジュール', '작업 모듈')
add('操作内容', 'Operation Content', '操作內容', '操作内容', '작업 내용')
add('初始密码', 'Initial Password', '初始密碼', '初期パスワード', '초기 비밀번호')
add('打印', 'Print', '打印', '印刷', '인쇄')
add('打印数据 JSON 格式', 'Print data JSON format', '打印資料 JSON 格式', '印刷データ JSON 形式', '인쇄 데이터 JSON 형식')
add('打印预览', 'Print Preview', '打印預覽', '印刷プレビュー', '인쇄 미리보기')
add('待审供应商', 'Supplier Pending Review', '待審供應商', '審査待ちサプライヤー', '심사 대기 공급업체')
add('待审批', 'Pending Approval', '待審批', '承認待ち', '승인 대기')
add('单位成本', 'Unit Cost', '單位成本', '単位原価', '단위 원가')
add('登出', 'Logout', '登出', 'ログアウト', '로그아웃')
add('登录', 'Login', '登入', 'ログイン', '로그인')
add('登录地点', 'Login Location', '登入地點', 'ログイン場所', '로그인 위치')
add('低', 'Low', '低', '低', '낮음')
add('第三方', 'Third Party', '第三方', 'サードパーティ', '타사')
add('调用模式', 'Call Mode', '調用模式', '呼び出しモード', '호출 모드')
add('非默认', 'Non-default', '非預設', '非デフォルト', '기본 아님')
add('分类', 'Category', '分類', '分類', '분류')
add('风险等级', 'Risk Level', '風險等級', 'リスクランク', '위험 등급')
add('附属信息（可选）', 'Additional Information (Optional)', '附屬資訊（可選）', '付属情報（任意）', '부가 정보(선택)')
add('复制组装后参数', 'Copy Assembled Parameters', '複製組裝後參數', '組み立て後パラメータをコピー', '조립 후 매개변수 복사')
add('复制组装前参数', 'Copy Raw Parameters', '複製組裝前參數', '組み立て前パラメータをコピー', '조립 전 매개변수 복사')
add('岗位级别', 'Position Level', '崗位級別', '職位レベル', '직무 등급')
add('高', 'High', '高', '高', '높음')
add('告警', 'Alert', '告警', 'アラート', '경고')
add('个人简介', 'Personal Bio', '個人簡介', 'プロフィール', '개인 소개')
add('个人首页', 'Personal Home', '個人首頁', '個人ホーム', '개인 홈')
add('工厂', 'Factory', '工廠', '工場', '공장')
add('工厂名称', 'Factory Name', '工廠名稱', '工場名', '공장명')
add('工程卡包装标签', 'Process Card Packaging Label', '工程卡包裝標籤', '工程カード包装ラベル', '공정 카드 포장 라벨')
add('工程卡号', 'Process Card No.', '工程卡號', '工程カード番号', '공정 카드 번호')
add('工位标签', 'Workstation Label', '工位標籤', '作業ステーションラベル', '작업장 라벨')
add('工位标识标签', 'Workstation ID Label', '工位標識標籤', '作業ステーションIDラベル', '작업장 식별 라벨')
add('公共配置', 'Public Config', '公共設定', '共通設定', '공통 설정')
add('供应商', 'Supplier', '供應商', 'サプライヤー', '공급업체')
add('供应商标签', 'Supplier Label', '供應商標籤', 'サプライヤーラベル', '공급업체 라벨')
add('供应商ID', 'Supplier ID', '供應商 ID', 'サプライヤーID', '공급업체 ID')
add('关闭', 'Close', '關閉', '閉じる', '닫기')
add('滑块验证码', 'Slider Captcha', '滑塊驗證碼', 'スライダー認証', '슬라이더 인증코드')
add('会话剩余时长', 'Session Remaining Time', '會話剩餘時長', 'セッション残り時間', '세션 남은 시간')
add('基本信息', 'Basic Info', '基本資訊', '基本情報', '기본 정보')
add('集中处理未激活待办实例与超时待办实例，作为审批流三期治理能力的运营入口。', 'Centralized handling for inactive and timed-out pending instances, serving as the operation entry for workflow governance phase 3.', '集中處理未啟用待辦實例與逾時待辦實例，作為審批流三期治理能力的營運入口。', '未アクティブの保留インスタンスとタイムアウトした保留インスタンスを集中的に処理し、承認フロー第3期ガバナンス機能の運用入口とします。', '비활성 대기 인스턴스와 시간 초과 대기 인스턴스를 집중 처리하며, 승인 흐름 3단계 거버넌스 운영 진입점입니다.')
add('籍贯', 'Native Place', '籍貫', '本籍', '본적')
add('简介', 'Introduction', '簡介', '概要', '소개')
add('结果类型', 'Result Type', '結果類型', '結果タイプ', '결과 유형')
add('金牌', 'Gold', '金牌', 'ゴールド', '골드')
add('仅提醒', 'Reminder Only', '僅提醒', '通知のみ', '알림만')
add('开发者', 'Developer', '開發者', '開発者', '개발자')
add('开启验证码', 'Enable Captcha', '開啟驗證碼', '認証コードを有効化', '인증 코드 활성화')
add('客户账号默认为 custom，不支持在此创建更多客户用户', 'The customer account defaults to custom. Creating more customer users here is not supported.', '客戶帳號預設為 custom，不支援在此建立更多客戶使用者', '顧客アカウントは既定で custom です。ここで追加の顧客ユーザーを作成することはできません。', '고객 계정은 기본적으로 custom이며 여기서 더 많은 고객 사용자를 만들 수 없습니다.')
add('库位标签', 'Location Label', '庫位標籤', 'ロケーションラベル', '창고 위치 라벨')
add('拉取公共配置', 'Pull Public Config', '拉取公共設定', '共通設定を取得', '공통 설정 가져오기')
add('连接地址 URL', 'Connection URL', '連線地址 URL', '接続URL', '연결 URL')
add('链路ID', 'Trace ID', '鏈路ID', 'トレースID', '추적 ID')
add('浏览器 UA', 'Browser UA', '瀏覽器 UA', 'ブラウザーUA', '브라우저 UA')
add('流水位数', 'Serial Digits', '流水位數', '連番桁数', '일련번호 자릿수')
add('密码加密方式', 'Password Encryption Method', '密碼加密方式', 'パスワード暗号化方式', '비밀번호 암호화 방식')
add('密码强度', 'Password Strength', '密碼強度', 'パスワード強度', '비밀번호 강도')
add('民企', 'Private Enterprise', '民企', '民営企業', '민영 기업')
add('名称', 'Name', '名稱', '名称', '이름')
add('明细信息', 'Details', '明細資訊', '詳細情報', '상세 정보')
add('模板内容设计', 'Template Content Design', '模板內容設計', 'テンプレート内容設計', '템플릿 내용 설계')
add('配置版本', 'Config Version', '設定版本', '設定バージョン', '설정 버전')
add('配置状态', 'Config Status', '設定狀態', '設定状態', '설정 상태')
add('批量强制下线', 'Batch Force Offline', '批量強制下線', '一括強制オフライン', '일괄 강제 오프라인')
add('普通客户', 'Normal Customer', '普通客戶', '一般顧客', '일반 고객')
add('起始值', 'Start Value', '起始值', '開始値', '시작값')
add('手机号', 'Mobile Number', '手機號', '携帯番号', '휴대폰 번호')
add('邮箱', 'Email', '電子郵件', 'メール', '이메일')
add('账号', 'Account', '帳號', 'アカウント', '계정')
add('标签类型', 'Label Type', '標籤類型', 'ラベルタイプ', '라벨 유형')
add('部门', 'Department', '部門', '部門', '부서')
add('模板', 'Template', '模板', 'テンプレート', '템플릿')
add('模板编码', 'Template Code', '模板編碼', 'テンプレートコード', '템플릿 코드')
add('模板描述', 'Template Description', '模板描述', 'テンプレート説明', '템플릿 설명')
add('模板名称', 'Template Name', '模板名稱', 'テンプレート名', '템플릿명')
add('模板类型', 'Template Type', '模板類型', 'テンプレートタイプ', '템플릿 유형')
add('确定删除该版本吗？', 'Delete this version?', '確定刪除此版本嗎？', 'このバージョンを削除しますか？', '이 버전을 삭제하시겠습니까?')
add('确认批量强制下线', 'Confirm Batch Force Offline', '確認批量強制下線', '一括強制オフラインの確認', '일괄 강제 오프라인 확인')
add('确认批量删除', 'Confirm Batch Delete', '確認批量刪除', '一括削除の確認', '일괄 삭제 확인')
add('确认强制下线', 'Confirm Force Offline', '確認強制下線', '強制オフラインの確認', '강제 오프라인 확인')
add('确认设置', 'Confirm Setting', '確認設定', '設定確認', '설정 확인')
add('确认执行补偿激活', 'Confirm compensation activation', '確認執行補償啟用', '補償アクティブ化を実行しますか', '보상 활성화를 실행하시겠습니까')
add('确认执行超时重试', 'Confirm timeout retry', '確認執行逾時重試', 'タイムアウト再試行を実行しますか', '시간 초과 재시도를 실행하시겠습니까')
add('确认重置', 'Confirm Reset', '確認重設', 'リセット確認', '초기화 확인')
add('任务ID', 'Task ID', '任務ID', 'タスクID', '작업 ID')
add('设备标签', 'Equipment Label', '設備標籤', '設備ラベル', '설비 라벨')
add('设备标识标签', 'Equipment ID Label', '設備標識標籤', '設備IDラベル', '설비 식별 라벨')
add('审批管理', 'Approval Management', '審批管理', '承認管理', '승인 관리')
add('事业单位', 'Public Institution', '事業單位', '公共機関', '공공기관')
add('是否默认', 'Is Default', '是否預設', 'デフォルトか', '기본 여부')
add('输入工程卡号自动填充', 'Enter process card no. to auto fill', '輸入工程卡號自動填充', '工程カード番号を入力すると自動入力', '공정 카드 번호를 입력하면 자동 입력')
add('数量', 'Quantity', '數量', '数量', '수량')
add('说明', 'Description', '說明', '説明', '설명')
add('添加 客户 租户', 'Add Customer Tenant', '新增 客戶 租戶', '顧客テナントを追加', '고객 테넌트 추가')
add('添加 Forgex 租户', 'Add Forgex Tenant', '新增 Forgex 租戶', 'Forgexテナントを追加', 'Forgex 테넌트 추가')
add('通知', 'Notification', '通知', '通知', '알림')
add('统一维护供应商共享主数据，并为租户创建、接口同步和资质审查提供数据来源。', 'Maintain shared supplier master data and provide data sources for tenant creation, API synchronization, and qualification review.', '統一維護供應商共享主資料，並為租戶建立、介面同步和資質審查提供資料來源。', 'サプライヤー共有マスターデータを一元管理し、テナント作成、API同期、資格審査のデータソースを提供します。', '공급업체 공유 마스터 데이터를 통합 관리하고 테넌트 생성, API 동기화, 자격 심사에 필요한 데이터 소스를 제공합니다.')
add('图片验证码', 'Image Captcha', '圖片驗證碼', '画像認証', '이미지 인증코드')
add('未读', 'Unread', '未讀', '未読', '읽지 않음')
add('未提交', 'Not Submitted', '未提交', '未提出', '미제출')
add('无', 'None', '無', 'なし', '없음')
add('无党派人士', 'Non-party Person', '無黨派人士', '無党派', '무당파 인사')
add('物料', 'Material', '物料', '品目', '자재')
add('系统管理', 'System Management', '系統管理', 'システム管理', '시스템 관리')
add('系统管理员', 'System Administrator', '系統管理員', 'システム管理者', '시스템 관리자')
add('响应编码', 'Response Code', '回應編碼', 'レスポンスコード', '응답 코드')
add('消息类型', 'Message Type', '消息類型', 'メッセージタイプ', '메시지 유형')
add('小微客户', 'Micro Customer', '小微客戶', '小規模顧客', '소규모 고객')
add('选择用户与角色，配置租户与关联关系', 'Select users and roles, then configure tenants and relationships.', '選擇使用者與角色，設定租戶與關聯關係', 'ユーザーとロールを選択し、テナントと関連付けを設定します', '사용자와 역할을 선택하고 테넌트 및 연결 관계를 설정합니다')
add('验证码', 'Captcha', '驗證碼', '認証コード', '인증 코드')
add('移动', 'Mobile', '移動', 'モバイル', '모바일')
add('已读', 'Read', '已讀', '既読', '읽음')
add('已通过', 'Passed', '已通過', '合格', '통과')
add('以下为报表预览效果，可通过 URL 参数传递查询条件。', 'Below is the report preview. Query conditions can be passed through URL parameters.', '以下為報表預覽效果，可透過 URL 參數傳遞查詢條件。', '以下はレポートプレビューです。URLパラメータで検索条件を渡せます。', '아래는 보고서 미리보기입니다. URL 매개변수로 조회 조건을 전달할 수 있습니다.')
add('银牌', 'Silver', '銀牌', 'シルバー', '실버')
add('汇率', 'Exchange Rate', '匯率', '為替レート', '환율')
add('汇率类型', 'Exchange Rate Type', '匯率類型', '為替レートタイプ', '환율 유형')
add('用户名', 'User Name', '使用者名稱', 'ユーザー名', '사용자 이름')
add('用户、角色、租户初始化', 'User, Role, and Tenant Initialization', '使用者、角色、租戶初始化', 'ユーザー、ロール、テナント初期化', '사용자, 역할, 테넌트 초기화')
add('用户分页大小', 'User Page Size', '使用者分頁大小', 'ユーザーページサイズ', '사용자 페이지 크기')
add('预警', 'Warning', '預警', '警告', '경고')
add('在报表设计器中完成设计后，请点击保存按钮。关闭此窗口将自动刷新列表。', 'After finishing the design in the report designer, click Save. Closing this window will refresh the list automatically.', '在報表設計器中完成設計後，請點擊保存按鈕。關閉此視窗將自動刷新列表。', 'レポートデザイナーで設計を完了したら保存ボタンをクリックしてください。このウィンドウを閉じると一覧が自動更新されます。', '보고서 디자이너에서 설계를 완료한 후 저장 버튼을 클릭하세요. 이 창을 닫으면 목록이 자동으로 새로 고침됩니다.')
add('暂无资质', 'No Qualifications', '暫無資質', '資格なし', '자격 없음')
add('战略客户', 'Strategic Customer', '戰略客戶', '戦略顧客', '전략 고객')
add('政府机构', 'Government Agency', '政府機構', '政府機関', '정부 기관')
add('中共党员', 'CPC Member', '中共黨員', '中国共産党員', '중국공산당원')
add('中专', 'Technical Secondary School', '中專', '専門学校', '중등전문학교')
add('重点客户', 'Key Customer', '重點客戶', '重要顧客', '중점 고객')
add('重置', 'Reset', '重設', 'リセット', '초기화')
add('取消', 'Cancel', '取消', 'キャンセル', '취소')
add('全部', 'All', '全部', 'すべて', '전체')
add('主信息', 'Main Info', '主資訊', '主情報', '주요 정보')
add('资质摘要', 'Qualification Summary', '資質摘要', '資格サマリー', '자격 요약')
add('字段值', 'Field Value', '欄位值', 'フィールド値', '필드 값')
add('自动通过', 'Auto Approve', '自動通過', '自動承認', '자동 통과')
add('自动转交', 'Auto Transfer', '自動轉交', '自動転送', '자동 전달')
add('租户 ID', 'Tenant ID', '租戶 ID', 'テナントID', '테넌트 ID')
add('租户标识', 'Tenant Key', '租戶標識', 'テナント識別子', '테넌트 식별자')
add('租户配置', 'Tenant Config', '租戶設定', 'テナント設定', '테넌트 설정')
add('组装后参数', 'Assembled Parameters', '組裝後參數', '組み立て後パラメータ', '조립 후 매개변수')
add('组装前参数', 'Raw Parameters', '組裝前參數', '組み立て前パラメータ', '조립 전 매개변수')
add('最后登录 IP', 'Last Login IP', '最後登入 IP', '最終ログインIP', '마지막 로그인 IP')
add('最后更新时间', 'Last Updated Time', '最後更新時間', '最終更新日時', '마지막 수정 시간')
add('最后使用时间', 'Last Used Time', '最後使用時間', '最終使用日時', '마지막 사용 시간')

function infer(key, current) {
  if (map.has(key)) return map.get(key)
  const enter = '请输入'
  const select = '请选择'
  const confirmDelete = '确认删除'
  if (key.startsWith(enter)) {
    const target = key.slice(enter.length)
    const translated = map.get(target)
    if (translated) {
      return [
        `Please enter ${translated[0]}`,
        `請輸入${translated[1]}`,
        `${translated[2]}を入力してください`,
        `${translated[3]}을(를) 입력하세요`,
      ]
    }
    if (target === '报表编码，首字母必须为字母，可包含字母、数字和下划线') {
      return [
        'Please enter report code. It must start with a letter and can contain letters, numbers, and underscores.',
        '請輸入報表編碼，首字母必須為字母，可包含字母、數字和底線',
        'レポートコードを入力してください。先頭は文字で、文字、数字、アンダースコアを使用できます。',
        '보고서 코드를 입력하세요. 첫 글자는 문자여야 하며 문자, 숫자, 밑줄을 포함할 수 있습니다.',
      ]
    }
  }
  if (key.startsWith(select)) {
    const target = key.slice(select.length)
    const translated = map.get(target)
    if (translated) {
      return [
        `Please select ${translated[0]}`,
        `請選擇${translated[1]}`,
        `${translated[2]}を選択してください`,
        `${translated[3]}을(를) 선택하세요`,
      ]
    }
    if (target === '密码强度、初始密码、验证码与加密方式') {
      return [
        'Select password strength, initial password, captcha, and encryption method.',
        '請選擇密碼強度、初始密碼、驗證碼與加密方式',
        'パスワード強度、初期パスワード、認証コード、暗号化方式を選択してください',
        '비밀번호 강도, 초기 비밀번호, 인증 코드 및 암호화 방식을 선택하세요',
      ]
    }
  }
  if (key.startsWith(confirmDelete)) {
    const target = key.slice(confirmDelete.length).replace(/[？?]$/, '')
    const translated = map.get(target)
    if (translated) {
      return [
        `Delete ${translated[0]}?`,
        `確認刪除${translated[1]}？`,
        `${translated[2]}を削除しますか？`,
        `${translated[3]}을(를) 삭제하시겠습니까?`,
      ]
    }
  }
  return current
}

function quote(value) {
  return value.replace(/\\/g, '\\\\').replace(/'/g, "\\'")
}

const next = entries.map(entry => ({ ...entry, vals: infer(entry.key, entry.vals) }))
const body = next.map(({ key, vals }) => (
  `  '${quote(key)}': {\n` +
  `    'en-US': '${quote(vals[0])}',\n` +
  `    'zh-TW': '${quote(vals[1])}',\n` +
  `    'ja-JP': '${quote(vals[2])}',\n` +
  `    'ko-KR': '${quote(vals[3])}',\n` +
  `  }`
)).join(',\n')

writeFileSync(file, `import type { LocaleCode } from './index'\n\n` +
  `type StaticTextEntry = Partial<Record<LocaleCode, string>>\n\n` +
  `const staticViewTextMessages: Record<string, StaticTextEntry> = {\n${body}\n}\n\n` +
  `export default staticViewTextMessages\n`, 'utf8')

const bad = next
  .filter(({ vals }) => /[\u4e00-\u9fff]/.test(vals[0]) || /[\u4e00-\u9fff]/.test(vals[3]))
  .map(({ key, vals }) => `${key}\ten=${vals[0]}\tko=${vals[3]}`)
writeFileSync(join(root, 'audit-reports/static-view-en-ko-unresolved.tsv'), bad.join('\n'), 'utf8')

console.log(JSON.stringify({ entries: next.length, unresolved: bad.length }, null, 2))
