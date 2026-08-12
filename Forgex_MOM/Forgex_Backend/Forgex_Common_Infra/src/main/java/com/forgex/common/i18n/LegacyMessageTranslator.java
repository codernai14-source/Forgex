package com.forgex.common.i18n;

import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 存量硬编码提示翻译器。
 * <p>
 * 新增业务提示仍应优先使用 {@link I18nPrompt} 与 {@code fx_i18n_message}。该工具只用于兜底翻译历史
 * 注解校验消息、默认模板和少量动态错误文本，避免接口在非中文语言下直接漏出中文提示。
 * </p>
 *
 * @deprecated 仅用于历史兼容兜底，禁止新增业务调用、禁止新增词条；新增文案必须定义 {@link I18nPrompt}
 * 并进入 {@code fx_i18n_message} 配置。
 * @author Forgex
 * @version 1.0.0
 */
@Deprecated
public final class LegacyMessageTranslator {

    private static final String ZH_CN = "zh-CN";
    private static final String ZH_TW = "zh-TW";
    private static final String EN_US = "en-US";
    private static final String JA_JP = "ja-JP";
    private static final String KO_KR = "ko-KR";

    private static final Map<String, Map<String, String>> DICTIONARY = new LinkedHashMap<>();
    private static final Map<String, String> FIELD_LABELS = new LinkedHashMap<>();
    private static final Pattern FIELD_NOT_EMPTY_PATTERN = Pattern.compile("^(.+?)(不能为空|不能為空)$");
    private static final Pattern BAD_REQUEST_FIELD_PATTERN = Pattern.compile("([^:;\\s]+):([^;]+)");

    static {
        add("保存成功：{0}", "Save successful: {0}", "保存成功：{0}", "保存しました: {0}", "저장 성공: {0}");
        add("新增成功：{0}", "Created successfully: {0}", "新增成功：{0}", "追加しました: {0}", "추가 성공: {0}");
        add("添加成功：{0}", "Added successfully: {0}", "添加成功：{0}", "追加しました: {0}", "추가 성공: {0}");
        add("修改成功：{0}", "Updated successfully: {0}", "修改成功：{0}", "更新しました: {0}", "수정 성공: {0}");
        add("删除成功：{0}", "Deleted successfully: {0}", "刪除成功：{0}", "削除しました: {0}", "삭제 성공: {0}");
        add("上传成功", "Upload successful", "上傳成功", "アップロードしました", "업로드 성공");
        add("导入成功", "Import successful", "匯入成功", "インポートしました", "가져오기 성공");
        add("导出成功", "Export successful", "匯出成功", "エクスポートしました", "내보내기 성공");
        add("提交成功", "Submitted successfully", "提交成功", "送信しました", "제출 성공");
        add("审批成功", "Approved successfully", "審批成功", "承認しました", "승인 성공");
        add("驳回成功", "Rejected successfully", "駁回成功", "却下しました", "반려 성공");
        add("取消成功", "Canceled successfully", "取消成功", "キャンセルしました", "취소 성공");
        add("启用成功", "Enabled successfully", "啟用成功", "有効にしました", "활성화 성공");
        add("禁用成功", "Disabled successfully", "停用成功", "無効にしました", "비활성화 성공");
        add("重置成功", "Reset successful", "重置成功", "リセットしました", "초기화 성공");
        add("复制成功", "Copied successfully", "複製成功", "コピーしました", "복사 성공");
        add("移动成功", "Moved successfully", "移動成功", "移動しました", "이동 성공");
        add("同步成功", "Sync successful", "同步成功", "同期しました", "동기화 성공");
        add("备份成功", "Backup successful", "備份成功", "バックアップしました", "백업 성공");
        add("恢复成功", "Restore successful", "恢復成功", "復元しました", "복원 성공");
        add("发布成功", "Published successfully", "發布成功", "公開しました", "게시 성공");
        add("取消发布成功", "Unpublished successfully", "取消發布成功", "公開を取り消しました", "게시 취소 성공");
        add("绑定成功", "Bound successfully", "綁定成功", "バインドしました", "바인딩 성공");
        add("解绑成功", "Unbound successfully", "解綁成功", "バインドを解除しました", "바인딩 해제 성공");
        add("分配成功", "Assigned successfully", "分配成功", "割り当てました", "할당 성공");
        add("撤销成功", "Revoked successfully", "撤銷成功", "取り消しました", "철회 성공");
        add("授权成功", "Authorized successfully", "授權成功", "権限を付与しました", "권한 부여 성공");
        add("取消授权成功", "Authorization canceled successfully", "取消授權成功", "権限付与を取り消しました", "권한 취소 성공");
        add("安装成功", "Installed successfully", "安裝成功", "インストールしました", "설치 성공");
        add("卸载成功", "Uninstalled successfully", "卸載成功", "アンインストールしました", "제거 성공");
        add("启动成功", "Started successfully", "啟動成功", "起動しました", "시작 성공");
        add("停止成功", "Stopped successfully", "停止成功", "停止しました", "중지 성공");
        add("重启成功", "Restarted successfully", "重啟成功", "再起動しました", "재시작 성공");
        add("发送成功", "Sent successfully", "發送成功", "送信しました", "전송 성공");
        add("操作成功", "Operation successful", "操作成功", "操作に成功しました", "작업 성공");
        add("操作失败", "Operation failed", "操作失敗", "操作に失敗しました", "작업 실패");

        add("未登录或登录过期", "Not logged in or session expired", "未登入或登入過期", "未ログインまたはセッション期限切れです", "로그인하지 않았거나 세션이 만료되었습니다");
        add("无权限", "No permission", "無權限", "権限がありません", "권한이 없습니다");
        add("请求参数错误：{0}", "Invalid request parameters: {0}", "請求參數錯誤：{0}", "リクエストパラメータが不正です: {0}", "요청 매개변수 오류: {0}");
        add("数据库连接失败，请检查数据库服务与配置", "Database connection failed. Please check the database service and configuration.", "資料庫連線失敗，請檢查資料庫服務與配置", "データベース接続に失敗しました。サービスと設定を確認してください。", "데이터베이스 연결 실패. 서비스와 설정을 확인하세요.");
        add("数据访问异常", "Data access error", "資料存取異常", "データアクセスエラー", "데이터 접근 오류");
        add("服务器内部错误：{0}", "Internal server error: {0}", "伺服器內部錯誤：{0}", "サーバー内部エラー: {0}", "서버 내부 오류: {0}");
        add("参数不能为空", "Parameters cannot be empty", "參數不能為空", "パラメータは空にできません", "매개변수는 비워둘 수 없습니다");
        add("ID不能为空", "ID cannot be empty", "ID 不能為空", "ID は空にできません", "ID는 비워둘 수 없습니다");
        add("ID 不能为空", "ID cannot be empty", "ID 不能為空", "ID は空にできません", "ID는 비워둘 수 없습니다");
        add("ID格式不正确", "Invalid ID format", "ID 格式不正確", "ID 形式が正しくありません", "ID 형식이 올바르지 않습니다");
        add("数据不存在", "Data does not exist", "資料不存在", "データが存在しません", "데이터가 없습니다");
        add("数据已存在", "Data already exists", "資料已存在", "データは既に存在します", "데이터가 이미 존재합니다");
        add("租户ID不能为空", "Tenant ID cannot be empty", "租戶 ID 不能為空", "テナント ID は空にできません", "테넌트 ID는 비워둘 수 없습니다");
        add("租户 ID 不能为空", "Tenant ID cannot be empty", "租戶 ID 不能為空", "テナント ID は空にできません", "테넌트 ID는 비워둘 수 없습니다");
        add("职位不存在", "Position does not exist", "職位不存在", "職位が存在しません", "직위가 없습니다");
        add("部门不存在", "Department does not exist", "部門不存在", "部門が存在しません", "부서가 없습니다");
        add("上传文件不能为空", "Upload file cannot be empty", "上傳檔案不能為空", "アップロードファイルは空にできません", "업로드 파일은 비워둘 수 없습니다");
        add("文件上传失败", "File upload failed", "檔案上傳失敗", "ファイルアップロードに失敗しました", "파일 업로드 실패");
        add("语言代码不能为空", "Language code cannot be empty", "語言代碼不能為空", "言語コードは空にできません", "언어 코드는 비워둘 수 없습니다");
        add("语言名称不能为空", "Language name cannot be empty", "語言名稱不能為空", "言語名は空にできません", "언어 이름은 비워둘 수 없습니다");
        add("语言类型不存在", "Language type does not exist", "語言類型不存在", "言語タイプが存在しません", "언어 유형이 없습니다");
        add("默认语言类型不存在", "Default language type does not exist", "預設語言類型不存在", "既定の言語タイプが存在しません", "기본 언어 유형이 없습니다");
        add("admin用户不存在", "Admin user does not exist", "admin 使用者不存在", "admin ユーザーが存在しません", "admin 사용자가 없습니다");
        add("账号或密码不能为空", "Account or password cannot be empty", "帳號或密碼不能為空", "アカウントまたはパスワードは空にできません", "계정 또는 비밀번호는 비워둘 수 없습니다");
        add("账号不能为空", "Account cannot be empty", "帳號不能為空", "アカウントは空にできません", "계정은 비워둘 수 없습니다");
        add("密码不能为空", "Password cannot be empty", "密碼不能為空", "パスワードは空にできません", "비밀번호는 비워둘 수 없습니다");
        add("旧密码不能为空", "Old password cannot be empty", "舊密碼不能為空", "旧パスワードは空にできません", "기존 비밀번호는 비워둘 수 없습니다");
        add("新密码不能为空", "New password cannot be empty", "新密碼不能為空", "新パスワードは空にできません", "새 비밀번호는 비워둘 수 없습니다");
        add("密码长度不能少于6位", "Password must be at least 6 characters", "密碼長度不能少於 6 位", "パスワードは 6 文字以上で入力してください", "비밀번호는 6자 이상이어야 합니다");
        add("默认密码不符合密码策略要求", "Default password does not meet password policy requirements", "預設密碼不符合密碼策略要求", "既定パスワードがパスワードポリシーを満たしていません", "기본 비밀번호가 비밀번호 정책을 충족하지 않습니다");
        add("密码不正确", "Incorrect password", "密碼不正確", "パスワードが正しくありません", "비밀번호가 올바르지 않습니다");
        add("账号已被锁定，请{0}分钟后再试", "Account is locked. Please try again in {0} minutes.", "帳號已被鎖定，請 {0} 分鐘後再試", "アカウントがロックされています。{0} 分後に再試行してください。", "계정이 잠겼습니다. {0}분 후 다시 시도하세요.");
        add("验证码不能为空", "Verification code cannot be empty", "驗證碼不能為空", "認証コードは空にできません", "인증코드는 비워둘 수 없습니다");
        add("验证码不正确", "Incorrect verification code", "驗證碼不正確", "認証コードが正しくありません", "인증코드가 올바르지 않습니다");
        add("未配置公钥", "Public key is not configured", "未配置公鑰", "公開鍵が設定されていません", "공개키가 설정되지 않았습니다");
        add("用户不存在", "User does not exist", "使用者不存在", "ユーザーが存在しません", "사용자가 없습니다");
        add("未绑定该租户", "User is not bound to this tenant", "未綁定該租戶", "このテナントに紐づいていません", "해당 테넌트에 연결되지 않았습니다");
        add("用户ID不能为空", "User ID cannot be empty", "使用者 ID 不能為空", "ユーザー ID は空にできません", "사용자 ID는 비워둘 수 없습니다");
        add("用户 ID 不能为空", "User ID cannot be empty", "使用者 ID 不能為空", "ユーザー ID は空にできません", "사용자 ID는 비워둘 수 없습니다");
        add("字典编码不能为空", "Dictionary code cannot be empty", "字典編碼不能為空", "辞書コードは空にできません", "사전 코드는 비워둘 수 없습니다");
        add("字典ID不能为空", "Dictionary ID cannot be empty", "字典 ID 不能為空", "辞書 ID は空にできません", "사전 ID는 비워둘 수 없습니다");
        add("数据查询失败：{0}", "Data query failed: {0}", "資料查詢失敗：{0}", "データ照会に失敗しました: {0}", "데이터 조회 실패: {0}");
        add("重置失败", "Reset failed", "重置失敗", "リセットに失敗しました", "초기화 실패");
        add("退出失败", "Logout failed", "登出失敗", "ログアウトに失敗しました", "로그아웃 실패");
        add("语言设置失败", "Failed to set language", "語言設定失敗", "言語設定に失敗しました", "언어 설정 실패");
        add("正在初始化中，请稍后再试", "Initializing. Please try again later.", "正在初始化中，請稍後再試", "初期化中です。しばらくしてから再試行してください。", "초기화 중입니다. 잠시 후 다시 시도하세요.");
        add("初始化密码不符合安全策略要求", "Initial password does not meet security policy requirements", "初始化密碼不符合安全策略要求", "初期パスワードがセキュリティポリシーを満たしていません", "초기 비밀번호가 보안 정책을 충족하지 않습니다");
        add("{0}服务未启动", "{0} service is not running", "{0} 服務未啟動", "{0} サービスが起動していません", "{0} 서비스가 시작되지 않았습니다");
        add("系统未授权，请先导入有效授权", "System is not licensed. Please import a valid license first.", "系統未授權，請先匯入有效授權", "システムが未認可です。有効なライセンスを先にインポートしてください。", "시스템이 라이선스되지 않았습니다. 유효한 라이선스를 먼저 가져오세요.");
        add("系统授权无效：{0}", "Invalid system license: {0}", "系統授權無效：{0}", "システムライセンスが無効です: {0}", "시스템 라이선스가 유효하지 않습니다: {0}");
        add("系统授权已过期", "System license has expired", "系統授權已過期", "システムライセンスの期限が切れています", "시스템 라이선스가 만료되었습니다");
        add("系统授权即将到期", "System license will expire soon", "系統授權即將到期", "システムライセンスの期限が近づいています", "시스템 라이선스가 곧 만료됩니다");
        add("系统授权已过期，当前处于宽限期", "System license has expired and is currently in the grace period", "系統授權已過期，目前處於寬限期", "システムライセンスは期限切れで、現在猶予期間中です", "시스템 라이선스가 만료되었으며 현재 유예 기간입니다");
        add("授权机器码不匹配", "License machine code does not match", "授權機器碼不匹配", "ライセンスのマシンコードが一致しません", "라이선스 머신 코드가 일치하지 않습니다");
        add("授权客户码不匹配", "License customer code does not match", "授權客戶碼不匹配", "ライセンスの顧客コードが一致しません", "라이선스 고객 코드가 일치하지 않습니다");
        add("接口不存在", "API does not exist", "介面不存在", "API が存在しません", "API가 없습니다");
        add("网关错误", "Gateway error", "閘道錯誤", "ゲートウェイエラー", "게이트웨이 오류");
        add("邀请码不存在", "Invitation code does not exist", "邀請碼不存在", "招待コードが存在しません", "초대 코드가 없습니다");
        add("邀请码已过期", "Invitation code has expired", "邀請碼已過期", "招待コードの期限が切れています", "초대 코드가 만료되었습니다");
        add("邀请码已停用", "Invitation code is disabled", "邀請碼已停用", "招待コードは無効です", "초대 코드가 비활성화되었습니다");
        add("邀请码已用尽", "Invitation code usage limit reached", "邀請碼已用盡", "招待コードは使用上限に達しました", "초대 코드가 모두 사용되었습니다");
        add("邀请码无效", "Invalid invitation code", "邀請碼無效", "招待コードが無効です", "초대 코드가 유효하지 않습니다");
        add("注册成功", "Registration successful", "註冊成功", "登録しました", "등록 성공");
        add("账号已存在", "Account already exists", "帳號已存在", "アカウントは既に存在します", "계정이 이미 존재합니다");
        add("邀请码不能为空", "Invitation code cannot be empty", "邀請碼不能為空", "招待コードは空にできません", "초대 코드는 비워둘 수 없습니다");

        add("物料不存在", "Material does not exist", "物料不存在", "品目が存在しません", "자재가 없습니다");
        add("物料编码已存在", "Material code already exists", "物料編碼已存在", "品目コードは既に存在します", "자재 코드가 이미 존재합니다");
        add("扩展信息不存在", "Extended information does not exist", "擴展資訊不存在", "拡張情報が存在しません", "확장 정보가 없습니다");
        add("模块编码不能为空", "Module code cannot be empty", "模組編碼不能為空", "モジュールコードは空にできません", "모듈 코드는 비워둘 수 없습니다");
        add("扩展配置不存在", "Extended configuration does not exist", "擴展配置不存在", "拡張設定が存在しません", "확장 설정이 없습니다");
        add("模板不存在", "Template does not exist", "模板不存在", "テンプレートが存在しません", "템플릿이 없습니다");
        add("模板编码已存在：{0}", "Template code already exists: {0}", "模板編碼已存在：{0}", "テンプレートコードは既に存在します: {0}", "템플릿 코드가 이미 존재합니다: {0}");
        add("模板类型不匹配", "Template type does not match", "模板類型不匹配", "テンプレートタイプが一致しません", "템플릿 유형이 일치하지 않습니다");
        add("模板已禁用，无法打印", "Template is disabled and cannot be printed", "模板已停用，無法列印", "テンプレートは無効のため印刷できません", "템플릿이 비활성화되어 인쇄할 수 없습니다");
        add("未找到可用模板", "No available template found", "未找到可用模板", "利用可能なテンプレートが見つかりません", "사용 가능한 템플릿을 찾을 수 없습니다");
        add("该绑定关系已存在", "This binding relation already exists", "該綁定關係已存在", "このバインド関係は既に存在します", "해당 바인딩 관계가 이미 존재합니다");
        add("绑定关系不存在", "Binding relation does not exist", "綁定關係不存在", "バインド関係が存在しません", "바인딩 관계가 없습니다");
        add("预览最多支持 10 张", "Preview supports up to 10 sheets", "預覽最多支援 10 張", "プレビューは最大 10 枚まで対応します", "미리보기는 최대 10장까지 지원합니다");
        add("打印张数必须大于 0", "Print count must be greater than 0", "列印張數必須大於 0", "印刷枚数は 0 より大きい必要があります", "인쇄 매수는 0보다 커야 합니다");
        add("单次打印不能超过 1000 张", "A single print job cannot exceed 1000 sheets", "單次列印不能超過 1000 張", "1 回の印刷は 1000 枚を超えられません", "한 번에 1000장을 초과해 인쇄할 수 없습니다");
        add("打印记录不存在", "Print record does not exist", "列印記錄不存在", "印刷記録が存在しません", "인쇄 기록이 없습니다");
        add("供应商编码已存在", "Supplier code already exists", "供應商編碼已存在", "サプライヤーコードは既に存在します", "공급업체 코드가 이미 존재합니다");
        add("供应商不存在", "Supplier does not exist", "供應商不存在", "サプライヤーが存在しません", "공급업체가 없습니다");
        add("供应商编码创建后不可修改", "Supplier code cannot be changed after creation", "供應商編碼建立後不可修改", "サプライヤーコードは作成後に変更できません", "공급업체 코드는 생성 후 수정할 수 없습니다");
        add("已关联租户的供应商不允许删除", "Suppliers linked to tenants cannot be deleted", "已關聯租戶的供應商不允許刪除", "テナントに連携済みのサプライヤーは削除できません", "테넌트에 연결된 공급업체는 삭제할 수 없습니다");
        add("创建供应商租户失败", "Failed to create supplier tenant", "建立供應商租戶失敗", "サプライヤーテナントの作成に失敗しました", "공급업체 테넌트 생성 실패");
        add("供应商 ID 不能为空", "Supplier ID cannot be empty", "供應商 ID 不能為空", "サプライヤー ID は空にできません", "공급업체 ID는 비워둘 수 없습니다");
        add("仅未审查供应商允许发起审查", "Only unreviewed suppliers can start a review", "僅未審查供應商允許發起審查", "未審査のサプライヤーのみ審査を開始できます", "미심사 공급업체만 심사를 시작할 수 있습니다");
        add("发起供应商资质审查失败", "Failed to start supplier qualification review", "發起供應商資質審查失敗", "サプライヤー資格審査の開始に失敗しました", "공급업체 자격 심사 시작 실패");
        add("导入文件不能为空", "Import file cannot be empty", "匯入檔案不能為空", "インポートファイルは空にできません", "가져오기 파일은 비워둘 수 없습니다");
        add("供应商参数不能为空", "Supplier parameters cannot be empty", "供應商參數不能為空", "サプライヤーパラメータは空にできません", "공급업체 매개변수는 비워둘 수 없습니다");
        add("供应商编码不能为空", "Supplier code cannot be empty", "供應商編碼不能為空", "サプライヤーコードは空にできません", "공급업체 코드는 비워둘 수 없습니다");
        add("供应商全称不能为空", "Supplier full name cannot be empty", "供應商全稱不能為空", "サプライヤー正式名称は空にできません", "공급업체 전체 이름은 비워둘 수 없습니다");
        add("供应商数据不能为空", "Supplier data cannot be empty", "供應商資料不能為空", "サプライヤーデータは空にできません", "공급업체 데이터는 비워둘 수 없습니다");
        add("客户编码已存在", "Customer code already exists", "客戶編碼已存在", "顧客コードは既に存在します", "고객 코드가 이미 존재합니다");
        add("客户不存在", "Customer does not exist", "客戶不存在", "顧客が存在しません", "고객이 없습니다");
        add("客户编码创建后不可修改", "Customer code cannot be changed after creation", "客戶編碼建立後不可修改", "顧客コードは作成後に変更できません", "고객 코드는 생성 후 수정할 수 없습니다");
        add("已关联租户的客户不允许删除", "Customers linked to tenants cannot be deleted", "已關聯租戶的客戶不允許刪除", "テナントに連携済みの顧客は削除できません", "테넌트에 연결된 고객은 삭제할 수 없습니다");
        add("创建客户租户失败", "Failed to create customer tenant", "建立客戶租戶失敗", "顧客テナントの作成に失敗しました", "고객 테넌트 생성 실패");
        add("客户 ID 不能为空", "Customer ID cannot be empty", "客戶 ID 不能為空", "顧客 ID は空にできません", "고객 ID는 비워둘 수 없습니다");
        add("客户参数不能为空", "Customer parameters cannot be empty", "客戶參數不能為空", "顧客パラメータは空にできません", "고객 매개변수는 비워둘 수 없습니다");
        add("客户编码不能为空", "Customer code cannot be empty", "客戶編碼不能為空", "顧客コードは空にできません", "고객 코드는 비워둘 수 없습니다");
        add("客户全称不能为空", "Customer full name cannot be empty", "客戶全稱不能為空", "顧客正式名称は空にできません", "고객 전체 이름은 비워둘 수 없습니다");
        add("发起客户审批失败", "Failed to start customer approval", "發起客戶審批失敗", "顧客承認の開始に失敗しました", "고객 승인 시작 실패");
        add("币种编码已存在", "Currency code already exists", "幣別編碼已存在", "通貨コードは既に存在します", "통화 코드가 이미 존재합니다");
        add("币种不存在", "Currency does not exist", "幣別不存在", "通貨が存在しません", "통화가 없습니다");
        add("币种编码创建后不可修改", "Currency code cannot be changed after creation", "幣別編碼建立後不可修改", "通貨コードは作成後に変更できません", "통화 코드는 생성 후 수정할 수 없습니다");
        add("本位币仅允许设置一个", "Only one base currency can be set", "本位幣僅允許設定一個", "基準通貨は 1 つだけ設定できます", "기준 통화는 하나만 설정할 수 있습니다");
        add("禁用币种不能维护汇率", "Exchange rates cannot be maintained for disabled currencies", "停用幣別不能維護匯率", "無効な通貨の為替レートは管理できません", "비활성화된 통화는 환율을 관리할 수 없습니다");
        add("汇率类型编码已存在", "Rate type code already exists", "匯率類型編碼已存在", "レートタイプコードは既に存在します", "환율 유형 코드가 이미 존재합니다");
        add("汇率类型不存在", "Rate type does not exist", "匯率類型不存在", "レートタイプが存在しません", "환율 유형이 없습니다");
        add("汇率记录不存在", "Exchange rate record does not exist", "匯率記錄不存在", "為替レート記録が存在しません", "환율 기록이 없습니다");
        add("源币种和目标币种不能相同", "Source currency and target currency cannot be the same", "來源幣別和目標幣別不能相同", "元通貨と対象通貨は同じにできません", "원본 통화와 대상 통화는 같을 수 없습니다");
        add("汇率失效日期不能早于生效日期", "Exchange rate expiry date cannot be earlier than effective date", "匯率失效日期不能早於生效日期", "為替レートの失効日は有効日より前にできません", "환율 만료일은 유효일보다 빠를 수 없습니다");
        add("审批中的汇率不允许再次发起审批", "Exchange rates under approval cannot be submitted again", "審批中的匯率不允許再次發起審批", "承認中の為替レートは再申請できません", "승인 중인 환율은 다시 승인 요청할 수 없습니다");
        add("发起汇率审批失败", "Failed to start exchange rate approval", "發起匯率審批失敗", "為替レート承認の開始に失敗しました", "환율 승인 시작 실패");

        add("任务编码不能为空", "Task code cannot be empty", "任務編碼不能為空", "タスクコードは空にできません", "작업 코드는 비워둘 수 없습니다");
        add("表单内容不能为空", "Form content cannot be empty", "表單內容不能為空", "フォーム内容は空にできません", "양식 내용은 비워둘 수 없습니다");
        add("任务名称不能为空", "Task name cannot be empty", "任務名稱不能為空", "タスク名は空にできません", "작업 이름은 비워둘 수 없습니다");
        add("组织类型不能为空", "Organization type cannot be empty", "組織類型不能為空", "組織タイプは空にできません", "조직 유형은 비워둘 수 없습니다");
        add("组织层级不能为空", "Organization level cannot be empty", "組織層級不能為空", "組織階層は空にできません", "조직 레벨은 비워둘 수 없습니다");
        add("部门名称不能为空", "Department name cannot be empty", "部門名稱不能為空", "部門名は空にできません", "부서 이름은 비워둘 수 없습니다");
        add("部门编码不能为空", "Department code cannot be empty", "部門編碼不能為空", "部門コードは空にできません", "부서 코드는 비워둘 수 없습니다");
        add("职位名称不能为空", "Position name cannot be empty", "職位名稱不能為空", "職位名は空にできません", "직위 이름은 비워둘 수 없습니다");
        add("职位编码不能为空", "Position code cannot be empty", "職位編碼不能為空", "職位コードは空にできません", "직위 코드는 비워둘 수 없습니다");
        add("租户名称不能为空", "Tenant name cannot be empty", "租戶名稱不能為空", "テナント名は空にできません", "테넌트 이름은 비워둘 수 없습니다");
        add("租户编码不能为空", "Tenant code cannot be empty", "租戶編碼不能為空", "テナントコードは空にできません", "테넌트 코드는 비워둘 수 없습니다");
        add("租户类型不能为空", "Tenant type cannot be empty", "租戶類型不能為空", "テナントタイプは空にできません", "테넌트 유형은 비워둘 수 없습니다");
        add("租户类别不能为空", "Tenant category cannot be empty", "租戶類別不能為空", "テナントカテゴリは空にできません", "테넌트 카테고리는 비워둘 수 없습니다");
        add("部门ID不能为空", "Department ID cannot be empty", "部門 ID 不能為空", "部門 ID は空にできません", "부서 ID는 비워둘 수 없습니다");
        add("失效时间不能为空", "Expiry time cannot be empty", "失效時間不能為空", "失効日時は空にできません", "만료 시간은 비워둘 수 없습니다");
        add("最大注册人数不能为空", "Maximum registrations cannot be empty", "最大註冊人數不能為空", "最大登録人数は空にできません", "최대 등록 인원은 비워둘 수 없습니다");
        add("执行ID不能为空", "Execution ID cannot be empty", "執行 ID 不能為空", "実行 ID は空にできません", "실행 ID는 비워둘 수 없습니다");
        add("审批实例ID不能为空", "Approval instance ID cannot be empty", "審批實例 ID 不能為空", "承認インスタンス ID は空にできません", "승인 인스턴스 ID는 비워둘 수 없습니다");
        add("加签审批人不能为空", "Additional approver cannot be empty", "加簽審批人不能為空", "追加承認者は空にできません", "추가 결재자는 비워둘 수 없습니다");
        add("审批状态不能为空", "Approval status cannot be empty", "審批狀態不能為空", "承認状態は空にできません", "승인 상태는 비워둘 수 없습니다");
        add("目标审批人不能为空", "Target approver cannot be empty", "目標審批人不能為空", "対象承認者は空にできません", "대상 결재자는 비워둘 수 없습니다");
        add("委托人不能为空", "Delegator cannot be empty", "委託人不能為空", "委任者は空にできません", "위임자는 비워둘 수 없습니다");
        add("受托人不能为空", "Delegatee cannot be empty", "受託人不能為空", "受任者は空にできません", "수임자는 비워둘 수 없습니다");
        add("表单类型不能为空", "Form type cannot be empty", "表單類型不能為空", "フォームタイプは空にできません", "양식 유형은 비워둘 수 없습니다");
        add("草稿ID不能为空", "Draft ID cannot be empty", "草稿 ID 不能為空", "ドラフト ID は空にできません", "초안 ID는 비워둘 수 없습니다");
        add("工厂 ID 不能为空", "Factory ID cannot be empty", "工廠 ID 不能為空", "工場 ID は空にできません", "공장 ID는 비워둘 수 없습니다");
        add("模板 ID 不能为空", "Template ID cannot be empty", "模板 ID 不能為空", "テンプレート ID は空にできません", "템플릿 ID는 비워둘 수 없습니다");
        add("打印数据不能为空", "Print data cannot be empty", "列印資料不能為空", "印刷データは空にできません", "인쇄 데이터는 비워둘 수 없습니다");
        add("打印张数不能为空", "Print count cannot be empty", "列印張數不能為空", "印刷枚数は空にできません", "인쇄 매수는 비워둘 수 없습니다");
        add("打印记录 ID 不能为空", "Print record ID cannot be empty", "列印記錄 ID 不能為空", "印刷記録 ID は空にできません", "인쇄 기록 ID는 비워둘 수 없습니다");
        add("物料 ID 不能为空", "Material ID cannot be empty", "物料 ID 不能為空", "品目 ID は空にできません", "자재 ID는 비워둘 수 없습니다");
        add("物料编码不能为空", "Material code cannot be empty", "物料編碼不能為空", "品目コードは空にできません", "자재 코드는 비워둘 수 없습니다");
        add("物料名称不能为空", "Material name cannot be empty", "物料名稱不能為空", "品目名は空にできません", "자재 이름은 비워둘 수 없습니다");
        add("物料类型不能为空", "Material type cannot be empty", "物料類型不能為空", "品目タイプは空にできません", "자재 유형은 비워둘 수 없습니다");
        add("状态不能为空", "Status cannot be empty", "狀態不能為空", "状態は空にできません", "상태는 비워둘 수 없습니다");
        add("绑定类型不能为空", "Binding type cannot be empty", "綁定類型不能為空", "バインドタイプは空にできません", "바인딩 유형은 비워둘 수 없습니다");
        add("绑定值不能为空", "Binding value cannot be empty", "綁定值不能為空", "バインド値は空にできません", "바인딩 값은 비워둘 수 없습니다");
        add("模板编码不能为空", "Template code cannot be empty", "模板編碼不能為空", "テンプレートコードは空にできません", "템플릿 코드는 비워둘 수 없습니다");
        add("模板名称不能为空", "Template name cannot be empty", "模板名稱不能為空", "テンプレート名は空にできません", "템플릿 이름은 비워둘 수 없습니다");
        add("模板类型不能为空", "Template type cannot be empty", "模板類型不能為空", "テンプレートタイプは空にできません", "템플릿 유형은 비워둘 수 없습니다");
        add("模板内容不能为空", "Template content cannot be empty", "模板內容不能為空", "テンプレート内容は空にできません", "템플릿 내용은 비워둘 수 없습니다");

        field("id", "ID", "ID", "ID", "ID");
        field("userId", "User ID", "使用者 ID", "ユーザー ID", "사용자 ID");
        field("tenantId", "Tenant ID", "租戶 ID", "テナント ID", "테넌트 ID");
        field("departmentId", "Department ID", "部門 ID", "部門 ID", "부서 ID");
        field("positionId", "Position ID", "職位 ID", "職位 ID", "직위 ID");
        field("taskCode", "Task Code", "任務編碼", "タスクコード", "작업 코드");
        field("formData", "Form Content", "表單內容", "フォーム内容", "양식 내용");
        field("templateId", "Template ID", "模板 ID", "テンプレート ID", "템플릿 ID");
        field("templateCode", "Template Code", "模板編碼", "テンプレートコード", "템플릿 코드");
        field("templateName", "Template Name", "模板名稱", "テンプレート名", "템플릿 이름");
        field("templateType", "Template Type", "模板類型", "テンプレートタイプ", "템플릿 유형");
        field("bindingType", "Binding Type", "綁定類型", "バインドタイプ", "바인딩 유형");
        field("bindingValue", "Binding Value", "綁定值", "バインド値", "바인딩 값");
        field("materialCode", "Material Code", "物料編碼", "品目コード", "자재 코드");
        field("materialName", "Material Name", "物料名稱", "品目名", "자재 이름");
        field("materialType", "Material Type", "物料類型", "品目タイプ", "자재 유형");
        field("supplierCode", "Supplier Code", "供應商編碼", "サプライヤーコード", "공급업체 코드");
        field("fullName", "Full Name", "全稱", "正式名称", "전체 이름");
        field("status", "Status", "狀態", "状態", "상태");
    }

    private LegacyMessageTranslator() {
    }

    /**
     * 按当前语言翻译文本。
     *
     * @param text 原始文本
     * @return 翻译后的文本；中文或无匹配时返回原文
     */
    public static String translate(String text) {
        return translate(text, LangContext.get());
    }

    /**
     * 按指定语言翻译文本。
     *
     * @param text 原始文本
     * @param lang 语言编码
     * @return 翻译后的文本；中文或无匹配时返回原文
     */
    public static String translate(String text, String lang) {
        if (!StringUtils.hasText(text)) {
            return text;
        }
        String normalizedLang = normalizeLang(lang);
        if (ZH_CN.equals(normalizedLang)) {
            return text;
        }

        String direct = resolveDirect(text.trim(), normalizedLang);
        if (StringUtils.hasText(direct)) {
            return preserveWhitespace(text, direct);
        }

        String fieldTranslated = translateFieldNotEmpty(text.trim(), normalizedLang);
        if (StringUtils.hasText(fieldTranslated)) {
            return preserveWhitespace(text, fieldTranslated);
        }

        String badRequestTranslated = translateBadRequestMessage(text, normalizedLang);
        if (StringUtils.hasText(badRequestTranslated)) {
            return badRequestTranslated;
        }

        return translateFragments(text, normalizedLang);
    }

    private static String resolveDirect(String text, String lang) {
        Map<String, String> row = DICTIONARY.get(text);
        if (row == null) {
            return null;
        }
        return row.getOrDefault(lang, row.get(EN_US));
    }

    private static String translateFieldNotEmpty(String text, String lang) {
        Matcher matcher = FIELD_NOT_EMPTY_PATTERN.matcher(text);
        if (!matcher.matches()) {
            return null;
        }
        String field = translateFieldLabel(matcher.group(1), lang);
        return switch (lang) {
            case ZH_TW -> field + "不能為空";
            case JA_JP -> field + "は空にできません";
            case KO_KR -> field + "는 비워둘 수 없습니다";
            default -> field + " cannot be empty";
        };
    }

    private static String translateBadRequestMessage(String text, String lang) {
        if (!text.contains(":") && !text.contains(";")) {
            return null;
        }
        Matcher matcher = BAD_REQUEST_FIELD_PATTERN.matcher(text);
        StringBuffer sb = new StringBuffer();
        boolean matched = false;
        while (matcher.find()) {
            matched = true;
            String field = translateFieldLabel(matcher.group(1), lang);
            String message = translate(matcher.group(2).trim(), lang);
            matcher.appendReplacement(sb, Matcher.quoteReplacement(field + ": " + message));
        }
        if (!matched) {
            return null;
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private static String translateFragments(String text, String lang) {
        String result = text;
        for (Map.Entry<String, Map<String, String>> entry : DICTIONARY.entrySet()) {
            String source = entry.getKey();
            if (result.contains(source)) {
                String translated = entry.getValue().getOrDefault(lang, entry.getValue().get(EN_US));
                if (StringUtils.hasText(translated)) {
                    result = result.replace(source, translated);
                }
            }
        }
        return result;
    }

    private static String translateFieldLabel(String field, String lang) {
        String key = field.trim();
        String labelKey = "field:" + key;
        Map<String, String> row = DICTIONARY.get(labelKey);
        if (row != null) {
            return row.getOrDefault(lang, row.get(EN_US));
        }
        String readable = FIELD_LABELS.getOrDefault(key, splitCamelCase(key));
        return readable;
    }

    private static String splitCamelCase(String text) {
        if (!StringUtils.hasText(text)) {
            return text;
        }
        String spaced = text.replaceAll("([a-z])([A-Z])", "$1 $2")
                .replace('_', ' ')
                .replace('-', ' ');
        return spaced.substring(0, 1).toUpperCase(Locale.ROOT) + spaced.substring(1);
    }

    private static String normalizeLang(String lang) {
        if (!StringUtils.hasText(lang)) {
            return ZH_CN;
        }
        String lower = lang.toLowerCase(Locale.ROOT).replace('_', '-');
        if (lower.startsWith("en")) {
            return EN_US;
        }
        if (lower.startsWith("zh-tw") || lower.startsWith("zh-hk") || lower.startsWith("zh-mo")) {
            return ZH_TW;
        }
        if (lower.startsWith("ja")) {
            return JA_JP;
        }
        if (lower.startsWith("ko")) {
            return KO_KR;
        }
        return ZH_CN;
    }

    private static String preserveWhitespace(String original, String translated) {
        int start = 0;
        int end = original.length();
        while (start < end && Character.isWhitespace(original.charAt(start))) {
            start++;
        }
        while (end > start && Character.isWhitespace(original.charAt(end - 1))) {
            end--;
        }
        return original.substring(0, start) + translated + original.substring(end);
    }

    private static void add(String zhCn, String enUs, String zhTw, String jaJp, String koKr) {
        Map<String, String> row = new LinkedHashMap<>();
        row.put(EN_US, enUs);
        row.put(ZH_TW, zhTw);
        row.put(JA_JP, jaJp);
        row.put(KO_KR, koKr);
        DICTIONARY.put(zhCn, row);
    }

    private static void field(String key, String enUs, String zhTw, String jaJp, String koKr) {
        FIELD_LABELS.put(key, enUs);
        add("field:" + key, enUs, zhTw, jaJp, koKr);
    }
}
