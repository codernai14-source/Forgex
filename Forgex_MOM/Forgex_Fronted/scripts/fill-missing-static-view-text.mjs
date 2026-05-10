#!/usr/bin/env node
import { readFileSync, writeFileSync } from 'node:fs'

const file = 'src/locales/staticViewText.ts'
const source = readFileSync(file, 'utf8')

const additions = {
  'Argon2：内存硬耗型不可逆哈希（Argon2id）': ['Argon2: memory-hard irreversible hash (Argon2id)', 'Argon2：記憶體硬耗型不可逆雜湊（Argon2id）', 'Argon2: メモリハードな不可逆ハッシュ（Argon2id）', 'Argon2: 메모리 하드형 비가역 해시(Argon2id)'],
  'BCrypt：不可逆哈希，安全与性能平衡': ['BCrypt: irreversible hash balancing security and performance', 'BCrypt：不可逆雜湊，兼顧安全與效能', 'BCrypt: 安全性と性能のバランスが取れた不可逆ハッシュ', 'BCrypt: 보안과 성능의 균형을 맞춘 비가역 해시'],
  'PBKDF2：迭代不可逆哈希（HmacSHA256）': ['PBKDF2: iterative irreversible hash (HmacSHA256)', 'PBKDF2：迭代不可逆雜湊（HmacSHA256）', 'PBKDF2: 反復不可逆ハッシュ（HmacSHA256）', 'PBKDF2: 반복 비가역 해시(HmacSHA256)'],
  'scrypt：抗GPU/ASIC的不可逆哈希': ['scrypt: irreversible hash resistant to GPU/ASIC attacks', 'scrypt：抗 GPU/ASIC 的不可逆雜湊', 'scrypt: GPU/ASIC に耐性のある不可逆ハッシュ', 'scrypt: GPU/ASIC 공격에 강한 비가역 해시'],
  '上传完成': ['Upload completed', '上傳完成', 'アップロード完了', '업로드 완료'],
  '下载链接已复制': ['Download link copied', '下載連結已複製', 'ダウンロードリンクをコピーしました', '다운로드 링크가 복사되었습니다'],
  '低：仅数字': ['Low: digits only', '低：僅數字', '低: 数字のみ', '낮음: 숫자만'],
  '供应商全称不能为空': ['Supplier full name is required', '供應商全稱不能為空', 'サプライヤー正式名称は必須です', '공급업체 전체 이름은 필수입니다'],
  '供应商编码不能为空': ['Supplier code is required', '供應商編碼不能為空', 'サプライヤーコードは必須です', '공급업체 코드는 필수입니다'],
  '供应商详情': ['Supplier Details', '供應商詳情', 'サプライヤー詳細', '공급업체 상세'],
  '供应商资质审查': ['Supplier Qualification Review', '供應商資質審查', 'サプライヤー資格審査', '공급업체 자격 심사'],
  '保存修改': ['Save Changes', '保存修改', '変更を保存', '변경 저장'],
  '列配置不能为空': ['Column configuration cannot be empty', '欄位設定不能為空', '列設定は空にできません', '열 설정은 비워둘 수 없습니다'],
  '创建失败': ['Create failed', '建立失敗', '作成に失敗しました', '생성 실패'],
  '创建成功': ['Created successfully', '建立成功', '作成しました', '생성 성공'],
  '创建模板': ['Create Template', '建立模板', 'テンプレートを作成', '템플릿 생성'],
  '初始化上传任务': ['Initialize Upload Task', '初始化上傳任務', 'アップロードタスクを初期化', '업로드 작업 초기화'],
  '删除后将无法恢复。': ['It cannot be restored after deletion.', '刪除後將無法復原。', '削除後は復元できません。', '삭제 후 복구할 수 없습니다.'],
  '删除成功': ['Deleted successfully', '刪除成功', '削除しました', '삭제 성공'],
  '加载用户列设置失败': ['Failed to load user column settings', '載入使用者欄位設定失敗', 'ユーザー列設定の読み込みに失敗しました', '사용자 열 설정 로드 실패'],
  '加载用户数据失败': ['Failed to load user data', '載入使用者資料失敗', 'ユーザーデータの読み込みに失敗しました', '사용자 데이터 로드 실패'],
  '加载用户详情失败': ['Failed to load user details', '載入使用者詳情失敗', 'ユーザー詳細の読み込みに失敗しました', '사용자 상세 로드 실패'],
  '加载统计数据失败': ['Failed to load statistics', '載入統計資料失敗', '統計データの読み込みに失敗しました', '통계 데이터 로드 실패'],
  '只能上传 APK 文件': ['Only APK files can be uploaded', '只能上傳 APK 檔案', 'APK ファイルのみアップロードできます', 'APK 파일만 업로드할 수 있습니다'],
  '合并安装包': ['Merge Package', '合併安裝包', 'インストールパッケージを統合', '설치 패키지 병합'],
  '图片验证码：拦截暴力尝试': ['Image captcha: blocks brute-force attempts', '圖片驗證碼：攔截暴力嘗試', '画像認証: ブルートフォース試行を防止', '이미지 인증 코드: 무차별 대입 시도 차단'],
  '处理成功': ['Processed successfully', '處理成功', '処理しました', '처리 성공'],
  '复制功能待实现': ['Copy feature is pending implementation', '複製功能待實作', 'コピー機能は未実装です', '복사 기능은 구현 예정입니다'],
  '复制失败': ['Copy failed', '複製失敗', 'コピーに失敗しました', '복사 실패'],
  '复制成功': ['Copied successfully', '複製成功', 'コピーしました', '복사 성공'],
  '如: v1.0': ['Example: v1.0', '如：v1.0', '例: v1.0', '예: v1.0'],
  '安卓版本上传成功': ['Android version uploaded successfully', '安卓版本上傳成功', 'Android バージョンをアップロードしました', 'Android 버전 업로드 성공'],
  '客户全称不能为空': ['Customer full name is required', '客戶全稱不能為空', '顧客正式名称は必須です', '고객 전체 이름은 필수입니다'],
  '客户编码不能为空': ['Customer code is required', '客戶編碼不能為空', '顧客コードは必須です', '고객 코드는 필수입니다'],
  '工厂选择功能开发中': ['Factory selection is under development', '工廠選擇功能開發中', '工場選択機能は開発中です', '공장 선택 기능 개발 중'],
  '工程卡自动填充功能待实现': ['Process card auto-fill is pending implementation', '工程卡自動填充功能待實作', '工程カード自動入力機能は未実装です', '공정 카드 자동 입력 기능은 구현 예정입니다'],
  '已取消': ['Cancelled', '已取消', 'キャンセルしました', '취소됨'],
  '当前加签数据不完整，请关闭后重试': ['Current add-sign data is incomplete. Close and try again.', '目前加簽資料不完整，請關閉後重試', '現在の追加承認データが不完全です。閉じて再試行してください', '현재 추가 승인 데이터가 불완전합니다. 닫은 후 다시 시도하세요'],
  '当前待办未找到可加签的审批实例，请刷新后重试': ['No approval instance available for add-sign was found. Refresh and try again.', '目前待辦未找到可加簽的審批實例，請刷新後重試', '追加承認可能な承認インスタンスが見つかりません。更新して再試行してください', '추가 승인 가능한 승인 인스턴스를 찾지 못했습니다. 새로고침 후 다시 시도하세요'],
  '当前待办未找到可转交的审批实例，请刷新后重试': ['No approval instance available for transfer was found. Refresh and try again.', '目前待辦未找到可轉交的審批實例，請刷新後重試', '転送可能な承認インスタンスが見つかりません。更新して再試行してください', '전달 가능한 승인 인스턴스를 찾지 못했습니다. 새로고침 후 다시 시도하세요'],
  '当前记录缺少配置ID，无法加载编辑详情，请先刷新列表或确认后端列表接口已返回id': ['The current record lacks a config ID. Refresh the list or confirm the backend list API returns id.', '目前記錄缺少設定 ID，無法載入編輯詳情，請先刷新列表或確認後端列表介面已返回 id', '現在のレコードに設定IDがないため編集詳細を読み込めません。一覧を更新するか、バックエンド一覧APIがidを返すことを確認してください', '현재 기록에 설정 ID가 없어 편집 상세를 불러올 수 없습니다. 목록을 새로고침하거나 백엔드 목록 API가 id를 반환하는지 확인하세요'],
  '当前转交数据不完整，请关闭后重试': ['Current transfer data is incomplete. Close and try again.', '目前轉交資料不完整，請關閉後重試', '現在の転送データが不完全です。閉じて再試行してください', '현재 전달 데이터가 불완전합니다. 닫은 후 다시 시도하세요'],
  '批量催办已发送': ['Batch reminders sent', '批量催辦已發送', '一括督促を送信しました', '일괄 독촉이 전송되었습니다'],
  '批量同意成功': ['Batch approval succeeded', '批量同意成功', '一括承認しました', '일괄 동의 성공'],
  '操作失败': ['Operation failed', '操作失敗', '操作に失敗しました', '작업 실패'],
  '新增包装方式': ['Add Packaging Method', '新增包裝方式', '包装方式を追加', '포장 방식 추가'],
  '新增多语言消息': ['Add I18n Message', '新增多語言消息', '多言語メッセージを追加', '다국어 메시지 추가'],
  '新增字典子项': ['Add Dictionary Item', '新增字典子項', '辞書項目を追加', '사전 항목 추가'],
  '新增字典类型': ['Add Dictionary Type', '新增字典類型', '辞書タイプを追加', '사전 유형 추가'],
  '新增报表': ['Add Report', '新增報表', 'レポートを追加', '보고서 추가'],
  '新增标签模板': ['Add Label Template', '新增標籤模板', 'ラベルテンプレートを追加', '라벨 템플릿 추가'],
  '新增用户': ['Add User', '新增使用者', 'ユーザーを追加', '사용자 추가'],
  '新增绑定关系': ['Add Binding Relation', '新增綁定關係', 'バインド関係を追加', '바인딩 관계 추가'],
  '无效': ['Invalid', '無效', '無効', '무효'],
  '普通：必须包含数字和字母': ['Normal: must contain digits and letters', '普通：必須包含數字和字母', '通常: 数字と英字を含む必要があります', '보통: 숫자와 문자를 포함해야 합니다'],
  '暂无快照数据': ['No snapshot data', '暫無快照資料', 'スナップショットデータがありません', '스냅샷 데이터 없음'],
  '暂无数据': ['No data', '暫無資料', 'データがありません', '데이터 없음'],
  '更新失败': ['Update failed', '更新失敗', '更新に失敗しました', '업데이트 실패'],
  '更新成功': ['Updated successfully', '更新成功', '更新しました', '업데이트 성공'],
  '有效': ['Valid', '有效', '有効', '유효'],
  '未找到对应的基础表格配置': ['No corresponding base table configuration found', '未找到對應的基礎表格設定', '対応する基本テーブル設定が見つかりません', '해당 기본 테이블 설정을 찾을 수 없습니다'],
  '模板选择功能开发中': ['Template selection is under development', '模板選擇功能開發中', 'テンプレート選択機能は開発中です', '템플릿 선택 기능 개발 중'],
  '滑块验证码：提升人机识别，降低误拦': ['Slider captcha: improves human verification and reduces false blocks', '滑塊驗證碼：提升人機識別，降低誤攔', 'スライダー認証: 人間判定を向上し誤ブロックを低減', '슬라이더 인증 코드: 사용자 식별을 높이고 오차단을 줄입니다'],
  '确定': ['OK', '確定', '確定', '확인'],
  '确定要删除该字典吗？': ['Delete this dictionary?', '確定刪除該字典嗎？', 'この辞書を削除しますか？', '이 사전을 삭제하시겠습니까?'],
  '确定要删除该模块吗？': ['Delete this module?', '確定刪除該模組嗎？', 'このモジュールを削除しますか？', '이 모듈을 삭제하시겠습니까?'],
  '确认': ['Confirm', '確認', '確認', '확인'],
  '确认删除该多语言消息吗？': ['Delete this i18n message?', '確認刪除該多語言消息嗎？', 'この多言語メッセージを削除しますか？', '이 다국어 메시지를 삭제하시겠습니까?'],
  '租户信息缺失': ['Tenant information is missing', '租戶資訊缺失', 'テナント情報が不足しています', '테넌트 정보가 누락되었습니다'],
  '等待上传': ['Waiting for upload', '等待上傳', 'アップロード待ち', '업로드 대기'],
  '编辑供应商': ['Edit Supplier', '編輯供應商', 'サプライヤーを編集', '공급업체 편집'],
  '编辑包装方式': ['Edit Packaging Method', '編輯包裝方式', '包装方式を編集', '포장 방식 편집'],
  '编辑半成品': ['Edit Semi-finished Goods', '編輯半成品', '半製品を編集', '반제품 편집'],
  '编辑原材料': ['Edit Raw Material', '編輯原材料', '原材料を編集', '원자재 편집'],
  '编辑多语言消息': ['Edit I18n Message', '編輯多語言消息', '多言語メッセージを編集', '다국어 메시지 편집'],
  '编辑字典': ['Edit Dictionary', '編輯字典', '辞書を編集', '사전 편집'],
  '编辑客户': ['Edit Customer', '編輯客戶', '顧客を編集', '고객 편집'],
  '编辑成品': ['Edit Finished Goods', '編輯成品', '完成品を編集', '완제품 편집'],
  '编辑报表': ['Edit Report', '編輯報表', 'レポートを編集', '보고서 편집'],
  '编辑标签模板': ['Edit Label Template', '編輯標籤模板', 'ラベルテンプレートを編集', '라벨 템플릿 편집'],
  '编辑物料': ['Edit Material', '編輯物料', '品目を編集', '자재 편집'],
  '编辑用户': ['Edit User', '編輯使用者', 'ユーザーを編集', '사용자 편집'],
  '编辑绑定关系': ['Edit Binding Relation', '編輯綁定關係', 'バインド関係を編集', '바인딩 관계 편집'],
  '获取模板详情失败': ['Failed to get template details', '取得模板詳情失敗', 'テンプレート詳細の取得に失敗しました', '템플릿 상세 조회 실패'],
  '补偿激活已执行': ['Compensation activation executed', '補償啟用已執行', '補償アクティブ化を実行しました', '보상 활성화가 실행되었습니다'],
  '表单数据为空': ['Form data is empty', '表單資料為空', 'フォームデータが空です', '양식 데이터가 비어 있습니다'],
  '表格编码不能为空': ['Table code is required', '表格編碼不能為空', 'テーブルコードは必須です', '테이블 코드는 필수입니다'],
  '该操作会使对应会话立即失效。': ['This operation will immediately invalidate the session.', '該操作會使對應會話立即失效。', 'この操作により該当セッションは直ちに無効になります。', '이 작업은 해당 세션을 즉시 무효화합니다.'],
  '请先选择 APK 文件': ['Please select an APK file first', '請先選擇 APK 檔案', '先に APK ファイルを選択してください', '먼저 APK 파일을 선택하세요'],
  '请先选择待办': ['Please select pending tasks first', '請先選擇待辦', '先に保留タスクを選択してください', '먼저 대기 작업을 선택하세요'],
  '请完善基础信息': ['Please complete basic information', '請完善基本資訊', '基本情報を入力してください', '기본 정보를 완성하세요'],
  '请输入绑定值': ['Please enter binding value', '請輸入綁定值', 'バインド値を入力してください', '바인딩 값을 입력하세요'],
  '请选择优先级': ['Please select priority', '請選擇優先級', '優先度を選択してください', '우선순위를 선택하세요'],
  '请选择打印类型': ['Please select print type', '請選擇打印類型', '印刷タイプを選択してください', '인쇄 유형을 선택하세요'],
  '请选择接收人': ['Please select recipients', '請選擇接收人', '受信者を選択してください', '수신자를 선택하세요'],
  '请选择绑定类型': ['Please select binding type', '請選擇綁定類型', 'バインドタイプを選択してください', '바인딩 유형을 선택하세요'],
  '请选择至少一位审批人': ['Please select at least one approver', '請選擇至少一位審批人', '少なくとも1人の承認者を選択してください', '승인자를 한 명 이상 선택하세요'],
  '超时重试已执行': ['Timeout retry executed', '逾時重試已執行', 'タイムアウト再試行を実行しました', '시간 초과 재시도가 실행되었습니다'],
  '长期': ['Long-term', '長期', '長期', '장기'],
  '验证码已关闭': ['Captcha is disabled', '驗證碼已關閉', '認証コードは無効です', '인증 코드가 꺼져 있습니다'],
  '高：必须包含字母、数字、符号': ['High: must contain letters, digits, and symbols', '高：必須包含字母、數字、符號', '高: 英字、数字、記号を含む必要があります', '높음: 문자, 숫자, 기호를 포함해야 합니다'],
}

function quote(value) {
  return value.replace(/\\/g, '\\\\').replace(/'/g, "\\'")
}

const existing = new Set([...source.matchAll(/^  '((?:\\'|[^'])+)': \{/gm)].map(match => match[1].replace(/\\'/g, "'")))
const blocks = Object.entries(additions)
  .filter(([key]) => !existing.has(key))
  .map(([key, vals]) => (
    `  '${quote(key)}': {\n` +
    `    'en-US': '${quote(vals[0])}',\n` +
    `    'zh-TW': '${quote(vals[1])}',\n` +
    `    'ja-JP': '${quote(vals[2])}',\n` +
    `    'ko-KR': '${quote(vals[3])}',\n` +
    `  }`
  ))

if (blocks.length) {
  const next = source.replace(/\n}\n\nexport default staticViewTextMessages\n$/, `,\n${blocks.join(',\n')}\n}\n\nexport default staticViewTextMessages\n`)
  writeFileSync(file, next, 'utf8')
}

console.log(JSON.stringify({ added: blocks.length }, null, 2))
