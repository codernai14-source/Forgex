-- Forgex dynamic table backend config repair
-- Target database: forgex_common
-- Purpose: after removing FxDynamicTable frontend fallback configs, make all frontend table-code values resolve from backend config.
-- Safe to rerun: inserts are guarded by NOT EXISTS.

USE forgex_common;

DROP TEMPORARY TABLE IF EXISTS tmp_fx_table_cfg_20260508;
CREATE TEMPORARY TABLE tmp_fx_table_cfg_20260508 (
  table_code VARCHAR(128) PRIMARY KEY,
  zh_cn VARCHAR(255) NOT NULL,
  en_us VARCHAR(255) NOT NULL,
  zh_tw VARCHAR(255) NOT NULL,
  ja_jp VARCHAR(255) NOT NULL,
  ko_kr VARCHAR(255) NOT NULL,
  table_type VARCHAR(20) NOT NULL DEFAULT 'BUSINESS',
  row_key VARCHAR(64) NOT NULL DEFAULT 'id',
  page_size INT NOT NULL DEFAULT 10
);

INSERT INTO tmp_fx_table_cfg_20260508(table_code, zh_cn, en_us, zh_tw, ja_jp, ko_kr) VALUES
('CurrencyMasterTable', '币种主数据', 'Currency Master Data', '幣別主資料', '通貨マスターデータ', '통화 마스터 데이터'),
('ExchangeRateTypeTable', '汇率类型', 'Exchange Rate Type', '匯率類型', '為替レートタイプ', '환율 유형'),
('CurrencyExchangeRateTable', '汇率明细', 'Exchange Rate Details', '匯率明細', '為替レート明細', '환율 상세'),
('ExchangeRateLogTable', '汇率日志', 'Exchange Rate Logs', '匯率日誌', '為替レートログ', '환율 로그'),
('RawMaterialTable', '原材料管理', 'Raw Material Management', '原材料管理', '原材料管理', '원자재 관리'),
('SemiFinishedMaterialTable', '半成品管理', 'Semi-finished Goods Management', '半成品管理', '半製品管理', '반제품 관리'),
('FinishedGoodsMaterialTable', '成品管理', 'Finished Goods Management', '成品管理', '完成品管理', '완제품 관리'),
('BasicPackagingTypeTable', '包装方式管理', 'Packaging Method Management', '包裝方式管理', '包装方式管理', '포장 방식 관리'),
('LabelBindingTable', '标签绑定', 'Label Binding', '標籤綁定', 'ラベルバインド', '라벨 바인딩'),
('LabelPrintRecordTable', '标签打印记录', 'Label Print Records', '標籤打印記錄', 'ラベル印刷記録', '라벨 인쇄 기록'),
('LabelTemplateTable', '标签模板', 'Label Template', '標籤模板', 'ラベルテンプレート', '라벨 템플릿'),
('ReportDatasourceTable', '报表数据源', 'Report Datasource', '報表資料來源', 'レポートデータソース', '보고서 데이터 소스'),
('ReportTemplateTable', '报表模板管理', 'Report Template Management', '報表模板管理', 'レポートテンプレート管理', '보고서 템플릿 관리'),
('WfCompensationCenterTable', '补偿中心', 'Compensation Center', '補償中心', '補償センター', '보상 센터');

INSERT INTO fx_table_config (
  tenant_id, table_code, table_name_i18n_json, table_type, row_key, default_page_size,
  enabled, version, create_by, update_by, deleted
)
SELECT
  0,
  c.table_code,
  JSON_OBJECT('zh-CN', c.zh_cn, 'en-US', c.en_us, 'zh-TW', c.zh_tw, 'ja-JP', c.ja_jp, 'ko-KR', c.ko_kr),
  c.table_type,
  c.row_key,
  c.page_size,
  1,
  1,
  'i18n_repair',
  'i18n_repair',
  0
FROM tmp_fx_table_cfg_20260508 c
WHERE NOT EXISTS (
  SELECT 1 FROM fx_table_config t
  WHERE t.tenant_id = 0 AND t.table_code = c.table_code AND t.deleted = 0
);

INSERT INTO fx_table_config (
  tenant_id, table_code, table_name_i18n_json, table_type, row_key, default_page_size,
  enabled, version, create_by, update_by, deleted
)
SELECT
  0,
  'basic_supplier',
  JSON_OBJECT('zh-CN', '供应商导入', 'en-US', 'Supplier Import', 'zh-TW', '供應商匯入', 'ja-JP', 'サプライヤーインポート', 'ko-KR', '공급업체 가져오기'),
  table_type,
  row_key,
  default_page_size,
  enabled,
  version,
  'i18n_repair',
  'i18n_repair',
  0
FROM fx_table_config s
WHERE s.tenant_id = 0 AND s.table_code = 'SupplierMasterTable' AND s.deleted = 0
  AND NOT EXISTS (SELECT 1 FROM fx_table_config t WHERE t.tenant_id = 0 AND t.table_code = 'basic_supplier' AND t.deleted = 0)
LIMIT 1;

INSERT INTO fx_table_config (
  tenant_id, table_code, table_name_i18n_json, table_type, row_key, default_page_size,
  enabled, version, create_by, update_by, deleted
)
SELECT
  0,
  'sys_user',
  JSON_OBJECT('zh-CN', '用户导入', 'en-US', 'User Import', 'zh-TW', '使用者匯入', 'ja-JP', 'ユーザーインポート', 'ko-KR', '사용자 가져오기'),
  table_type,
  row_key,
  default_page_size,
  enabled,
  version,
  'i18n_repair',
  'i18n_repair',
  0
FROM fx_table_config s
WHERE s.tenant_id = 0 AND s.table_code = 'UserTable' AND s.deleted = 0
  AND NOT EXISTS (SELECT 1 FROM fx_table_config t WHERE t.tenant_id = 0 AND t.table_code = 'sys_user' AND t.deleted = 0)
LIMIT 1;

DROP TEMPORARY TABLE IF EXISTS tmp_fx_table_col_20260508;
CREATE TEMPORARY TABLE tmp_fx_table_col_20260508 (
  table_code VARCHAR(128) NOT NULL,
  field_name VARCHAR(128) NOT NULL,
  zh_cn VARCHAR(255) NOT NULL,
  en_us VARCHAR(255) NOT NULL,
  zh_tw VARCHAR(255) NOT NULL,
  ja_jp VARCHAR(255) NOT NULL,
  ko_kr VARCHAR(255) NOT NULL,
  width_val INT NULL,
  fixed_val VARCHAR(16) NULL,
  order_num_val INT NOT NULL,
  queryable_val TINYINT NOT NULL DEFAULT 0,
  query_type_val VARCHAR(32) NULL,
  query_operator_val VARCHAR(32) NULL,
  dict_code_val VARCHAR(100) NULL,
  ellipsis_val TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY(table_code, field_name)
);

INSERT INTO tmp_fx_table_col_20260508 VALUES
('CurrencyMasterTable','currencyCode','币种编码','Currency Code','幣別編碼','通貨コード','통화 코드',120,NULL,1,1,'input','like',NULL,0),
('CurrencyMasterTable','currencyNameCn','中文名称','Chinese Name','中文名稱','中国語名','중문명',160,NULL,2,1,'input','like',NULL,0),
('CurrencyMasterTable','currencyNameEn','英文名称','English Name','英文名稱','英語名','영문명',180,NULL,3,1,'input','like',NULL,0),
('CurrencyMasterTable','currencySymbol','符号','Symbol','符號','記号','기호',80,NULL,4,0,NULL,NULL,NULL,0),
('CurrencyMasterTable','decimalDigits','小数位','Decimal Digits','小數位','小数桁数','소수 자릿수',90,NULL,5,0,NULL,NULL,NULL,0),
('CurrencyMasterTable','isBaseCurrency','本位币','Base Currency','本位幣','基準通貨','기준 통화',100,NULL,6,1,'select','eq',NULL,0),
('CurrencyMasterTable','status','状态','Status','狀態','状態','상태',90,NULL,7,1,'select','eq','status',0),
('CurrencyMasterTable','action','操作','Action','操作','操作','작업',260,'right',99,0,NULL,NULL,NULL,0),

('ExchangeRateTypeTable','rateTypeCode','类型编码','Type Code','類型編碼','タイプコード','유형 코드',140,NULL,1,1,'input','like',NULL,0),
('ExchangeRateTypeTable','rateTypeName','类型名称','Type Name','類型名稱','タイプ名','유형명',160,NULL,2,1,'input','like',NULL,0),
('ExchangeRateTypeTable','businessScene','业务场景','Business Scenario','業務場景','業務シナリオ','업무 시나리오',260,NULL,3,0,NULL,NULL,NULL,1),
('ExchangeRateTypeTable','isDefault','默认','Default','預設','デフォルト','기본',90,NULL,4,1,'select','eq',NULL,0),
('ExchangeRateTypeTable','status','状态','Status','狀態','状態','상태',90,NULL,5,1,'select','eq','status',0),
('ExchangeRateTypeTable','action','操作','Action','操作','操作','작업',220,'right',99,0,NULL,NULL,NULL,0),

('CurrencyExchangeRateTable','pair','币种对','Currency Pair','幣別對','通貨ペア','통화 쌍',140,NULL,1,0,NULL,NULL,NULL,0),
('CurrencyExchangeRateTable','rateTypeCode','汇率类型','Exchange Rate Type','匯率類型','為替レートタイプ','환율 유형',130,NULL,2,1,'input','like',NULL,0),
('CurrencyExchangeRateTable','effectiveDate','生效日期','Effective Date','生效日期','有効開始日','적용일',120,NULL,3,1,'date','eq',NULL,0),
('CurrencyExchangeRateTable','expireDate','失效日期','Expire Date','失效日期','失効日','만료일',120,NULL,4,0,NULL,NULL,NULL,0),
('CurrencyExchangeRateTable','exchangeRate','汇率值','Exchange Rate','匯率值','為替レート','환율 값',130,NULL,5,0,NULL,NULL,NULL,0),
('CurrencyExchangeRateTable','approveStatus','审批状态','Approval Status','審批狀態','承認状態','승인 상태',120,NULL,6,1,'select','eq',NULL,0),
('CurrencyExchangeRateTable','action','操作','Action','操作','操作','작업',240,'right',99,0,NULL,NULL,NULL,0),

('ExchangeRateLogTable','rateId','汇率ID','Exchange Rate ID','匯率ID','為替レートID','환율 ID',100,NULL,1,1,'input','eq',NULL,0),
('ExchangeRateLogTable','operationType','操作类型','Operation Type','操作類型','操作タイプ','작업 유형',120,NULL,2,1,'input','like',NULL,0),
('ExchangeRateLogTable','operationContent','操作内容','Operation Content','操作內容','操作内容','작업 내용',260,NULL,3,0,NULL,NULL,NULL,1),
('ExchangeRateLogTable','createTime','操作时间','Operation Time','操作時間','操作日時','작업 시간',180,NULL,4,1,'dateRange','between',NULL,0);

INSERT INTO tmp_fx_table_col_20260508 VALUES
('RawMaterialTable','materialCode','物料编码','Material Code','物料編碼','品目コード','자재 코드',120,NULL,1,1,'input','like',NULL,0),
('RawMaterialTable','materialName','物料名称','Material Name','物料名稱','品目名','자재명',150,NULL,2,1,'input','like',NULL,0),
('RawMaterialTable','materialCategory','物料分类','Material Category','物料分類','品目カテゴリ','자재 분류',120,NULL,3,1,'input','like',NULL,0),
('RawMaterialTable','specification','规格型号','Specification','規格型號','仕様型番','규격 모델',150,NULL,4,0,NULL,NULL,NULL,1),
('RawMaterialTable','unit','计量单位','Unit','計量單位','単位','단위',80,NULL,5,0,NULL,NULL,NULL,0),
('RawMaterialTable','brand','品牌','Brand','品牌','ブランド','브랜드',100,NULL,6,0,NULL,NULL,NULL,0),
('RawMaterialTable','status','状态','Status','狀態','状態','상태',80,NULL,7,1,'select','eq','status',0),
('RawMaterialTable','approvalStatus','审批状态','Approval Status','審批狀態','承認状態','승인 상태',100,NULL,8,1,'select','eq',NULL,0),
('RawMaterialTable','remark','备注','Remark','備註','備考','비고',150,NULL,9,0,NULL,NULL,NULL,1),
('RawMaterialTable','action','操作','Action','操作','操作','작업',180,'right',99,0,NULL,NULL,NULL,0),

('SemiFinishedMaterialTable','materialCode','物料编码','Material Code','物料編碼','品目コード','자재 코드',120,NULL,1,1,'input','like',NULL,0),
('SemiFinishedMaterialTable','materialName','物料名称','Material Name','物料名稱','品目名','자재명',150,NULL,2,1,'input','like',NULL,0),
('SemiFinishedMaterialTable','materialCategory','物料分类','Material Category','物料分類','品目カテゴリ','자재 분류',120,NULL,3,1,'input','like',NULL,0),
('SemiFinishedMaterialTable','specification','规格型号','Specification','規格型號','仕様型番','규격 모델',150,NULL,4,0,NULL,NULL,NULL,1),
('SemiFinishedMaterialTable','unit','计量单位','Unit','計量單位','単位','단위',80,NULL,5,0,NULL,NULL,NULL,0),
('SemiFinishedMaterialTable','brand','品牌','Brand','品牌','ブランド','브랜드',100,NULL,6,0,NULL,NULL,NULL,0),
('SemiFinishedMaterialTable','status','状态','Status','狀態','状態','상태',80,NULL,7,1,'select','eq','status',0),
('SemiFinishedMaterialTable','approvalStatus','审批状态','Approval Status','審批狀態','承認状態','승인 상태',100,NULL,8,1,'select','eq',NULL,0),
('SemiFinishedMaterialTable','remark','备注','Remark','備註','備考','비고',150,NULL,9,0,NULL,NULL,NULL,1),
('SemiFinishedMaterialTable','action','操作','Action','操作','操作','작업',180,'right',99,0,NULL,NULL,NULL,0),

('FinishedGoodsMaterialTable','materialCode','物料编码','Material Code','物料編碼','品目コード','자재 코드',120,NULL,1,1,'input','like',NULL,0),
('FinishedGoodsMaterialTable','materialName','物料名称','Material Name','物料名稱','品目名','자재명',150,NULL,2,1,'input','like',NULL,0),
('FinishedGoodsMaterialTable','materialCategory','物料分类','Material Category','物料分類','品目カテゴリ','자재 분류',120,NULL,3,1,'input','like',NULL,0),
('FinishedGoodsMaterialTable','specification','规格型号','Specification','規格型號','仕様型番','규격 모델',150,NULL,4,0,NULL,NULL,NULL,1),
('FinishedGoodsMaterialTable','unit','计量单位','Unit','計量單位','単位','단위',80,NULL,5,0,NULL,NULL,NULL,0),
('FinishedGoodsMaterialTable','brand','品牌','Brand','品牌','ブランド','브랜드',100,NULL,6,0,NULL,NULL,NULL,0),
('FinishedGoodsMaterialTable','status','状态','Status','狀態','状態','상태',80,NULL,7,1,'select','eq','status',0),
('FinishedGoodsMaterialTable','approvalStatus','审批状态','Approval Status','審批狀態','承認状態','승인 상태',100,NULL,8,1,'select','eq',NULL,0),
('FinishedGoodsMaterialTable','remark','备注','Remark','備註','備考','비고',150,NULL,9,0,NULL,NULL,NULL,1),
('FinishedGoodsMaterialTable','action','操作','Action','操作','操作','작업',180,'right',99,0,NULL,NULL,NULL,0);

INSERT INTO tmp_fx_table_col_20260508 VALUES
('BasicPackagingTypeTable','packagingCode','包装编码','Packaging Code','包裝編碼','包装コード','포장 코드',130,NULL,1,1,'input','like',NULL,0),
('BasicPackagingTypeTable','packagingName','包装名称','Packaging Name','包裝名稱','包装名','포장명',160,NULL,2,1,'input','like',NULL,0),
('BasicPackagingTypeTable','packagingMaterial','包装材料','Packaging Material','包裝材料','包装材料','포장재',140,NULL,3,1,'input','like',NULL,0),
('BasicPackagingTypeTable','lengthMm','长度(mm)','Length (mm)','長度(mm)','長さ(mm)','길이(mm)',110,NULL,4,0,NULL,NULL,NULL,0),
('BasicPackagingTypeTable','widthMm','宽度(mm)','Width (mm)','寬度(mm)','幅(mm)','너비(mm)',110,NULL,5,0,NULL,NULL,NULL,0),
('BasicPackagingTypeTable','heightMm','高度(mm)','Height (mm)','高度(mm)','高さ(mm)','높이(mm)',110,NULL,6,0,NULL,NULL,NULL,0),
('BasicPackagingTypeTable','weightKg','重量(kg)','Weight (kg)','重量(kg)','重量(kg)','중량(kg)',110,NULL,7,0,NULL,NULL,NULL,0),
('BasicPackagingTypeTable','maxLoadKg','最大载重(kg)','Max Load (kg)','最大載重(kg)','最大積載量(kg)','최대 하중(kg)',130,NULL,8,0,NULL,NULL,NULL,0),
('BasicPackagingTypeTable','unitCost','单位成本','Unit Cost','單位成本','単位原価','단위 원가',110,NULL,9,0,NULL,NULL,NULL,0),
('BasicPackagingTypeTable','status','状态','Status','狀態','状態','상태',90,NULL,10,1,'select','eq','status',0),
('BasicPackagingTypeTable','sortOrder','排序','Sort Order','排序','並び順','정렬',90,NULL,11,0,NULL,NULL,NULL,0),
('BasicPackagingTypeTable','remark','备注','Remark','備註','備考','비고',160,NULL,12,0,NULL,NULL,NULL,1),
('BasicPackagingTypeTable','action','操作','Action','操作','操作','작업',180,'right',99,0,NULL,NULL,NULL,0);

INSERT INTO tmp_fx_table_col_20260508 VALUES
('LabelBindingTable','bindingType','绑定类型','Binding Type','綁定類型','バインドタイプ','바인딩 유형',130,NULL,1,1,'select','eq',NULL,0),
('LabelBindingTable','bindingValue','绑定值','Binding Value','綁定值','バインド値','바인딩 값',180,NULL,2,1,'input','like',NULL,0),
('LabelBindingTable','templateCode','模板编码','Template Code','模板編碼','テンプレートコード','템플릿 코드',160,NULL,3,1,'input','like',NULL,0),
('LabelBindingTable','templateName','模板名称','Template Name','模板名稱','テンプレート名','템플릿명',180,NULL,4,1,'input','like',NULL,0),
('LabelBindingTable','priority','优先级','Priority','優先級','優先度','우선순위',90,NULL,5,1,'select','eq',NULL,0),
('LabelBindingTable','status','状态','Status','狀態','状態','상태',90,NULL,6,1,'select','eq','status',0),
('LabelBindingTable','createTime','创建时间','Create Time','建立時間','作成日時','생성 시간',180,NULL,7,0,NULL,NULL,NULL,0),
('LabelBindingTable','action','操作','Action','操作','操作','작업',180,'right',99,0,NULL,NULL,NULL,0),

('LabelPrintRecordTable','printNo','打印流水号','Print Serial No.','打印流水號','印刷シリアル番号','인쇄 일련번호',180,NULL,1,1,'input','like',NULL,0),
('LabelPrintRecordTable','templateType','模板类型','Template Type','模板類型','テンプレートタイプ','템플릿 유형',130,NULL,2,1,'select','eq',NULL,0),
('LabelPrintRecordTable','templateName','模板名称','Template Name','模板名稱','テンプレート名','템플릿명',180,NULL,3,1,'input','like',NULL,0),
('LabelPrintRecordTable','barcodeNo','条码号','Barcode No.','條碼號','バーコード番号','바코드 번호',160,NULL,4,1,'input','like',NULL,0),
('LabelPrintRecordTable','lotNo','LOT号','LOT No.','LOT號','LOT番号','LOT 번호',130,NULL,5,1,'input','like',NULL,0),
('LabelPrintRecordTable','materialCode','物料编码','Material Code','物料編碼','品目コード','자재 코드',140,NULL,6,1,'input','like',NULL,0),
('LabelPrintRecordTable','materialName','物料名称','Material Name','物料名稱','品目名','자재명',160,NULL,7,1,'input','like',NULL,0),
('LabelPrintRecordTable','printCount','打印张数','Print Count','打印張數','印刷枚数','인쇄 매수',100,NULL,8,0,NULL,NULL,NULL,0),
('LabelPrintRecordTable','printType','打印类型','Print Type','打印類型','印刷タイプ','인쇄 유형',120,NULL,9,1,'select','eq',NULL,0),
('LabelPrintRecordTable','operatorName','操作人','Operator','操作人','操作者','작업자',120,NULL,10,1,'input','like',NULL,0),
('LabelPrintRecordTable','printTime','打印时间','Print Time','打印時間','印刷日時','인쇄 시간',180,NULL,11,1,'dateRange','between',NULL,0),
('LabelPrintRecordTable','isReprint','是否补打','Is Reprint','是否補打','再印刷か','재인쇄 여부',110,NULL,12,1,'select','eq',NULL,0),
('LabelPrintRecordTable','action','操作','Action','操作','操作','작업',160,'right',99,0,NULL,NULL,NULL,0),

('LabelTemplateTable','templateCode','模板编码','Template Code','模板編碼','テンプレートコード','템플릿 코드',160,NULL,1,1,'input','like',NULL,0),
('LabelTemplateTable','templateName','模板名称','Template Name','模板名稱','テンプレート名','템플릿명',180,NULL,2,1,'input','like',NULL,0),
('LabelTemplateTable','templateType','模板类型','Template Type','模板類型','テンプレートタイプ','템플릿 유형',130,NULL,3,1,'select','eq','template_type',0),
('LabelTemplateTable','isDefault','是否默认','Is Default','是否預設','デフォルトか','기본 여부',110,NULL,4,1,'select','eq',NULL,0),
('LabelTemplateTable','status','状态','Status','狀態','状態','상태',90,NULL,5,1,'select','eq','common_status',0),
('LabelTemplateTable','version','版本','Version','版本','バージョン','버전',90,NULL,6,0,NULL,NULL,NULL,0),
('LabelTemplateTable','remark','备注','Remark','備註','備考','비고',180,NULL,7,0,NULL,NULL,NULL,1),
('LabelTemplateTable','createTime','创建时间','Create Time','建立時間','作成日時','생성 시간',180,NULL,8,0,NULL,NULL,NULL,0),
('LabelTemplateTable','action','操作','Action','操作','操作','작업',240,'right',99,0,NULL,NULL,NULL,0);

INSERT INTO tmp_fx_table_col_20260508 VALUES
('ReportDatasourceTable','name','数据源名称','Datasource Name','資料來源名稱','データソース名','데이터 소스명',180,NULL,1,1,'input','like',NULL,0),
('ReportDatasourceTable','code','数据源编码','Datasource Code','資料來源編碼','データソースコード','데이터 소스 코드',150,NULL,2,1,'input','like',NULL,0),
('ReportDatasourceTable','type','数据库类型','Database Type','資料庫類型','データベースタイプ','데이터베이스 유형',130,NULL,3,1,'select','eq','dbType',0),
('ReportDatasourceTable','url','连接地址 URL','Connection URL','連線地址 URL','接続URL','연결 URL',260,NULL,4,0,NULL,NULL,NULL,1),
('ReportDatasourceTable','username','用户名','User Name','使用者名稱','ユーザー名','사용자 이름',130,NULL,5,1,'input','like',NULL,0),
('ReportDatasourceTable','status','状态','Status','狀態','状態','상태',90,NULL,6,1,'select','eq','status',0),
('ReportDatasourceTable','remark','备注','Remark','備註','備考','비고',180,NULL,7,0,NULL,NULL,NULL,1),
('ReportDatasourceTable','createTime','创建时间','Create Time','建立時間','作成日時','생성 시간',180,NULL,8,0,NULL,NULL,NULL,0),
('ReportDatasourceTable','action','操作','Action','操作','操作','작업',220,'right',99,0,NULL,NULL,NULL,0),

('ReportTemplateTable','name','报表名称','Report Name','報表名稱','レポート名','보고서명',200,NULL,1,1,'input','like',NULL,0),
('ReportTemplateTable','code','报表编码','Report Code','報表編碼','レポートコード','보고서 코드',150,NULL,2,1,'input','like',NULL,0),
('ReportTemplateTable','engineType','引擎类型','Engine Type','引擎類型','エンジンタイプ','엔진 유형',120,NULL,3,1,'select','eq','engineType',0),
('ReportTemplateTable','categoryName','分类','Category','分類','分類','분류',120,NULL,4,0,NULL,NULL,NULL,0),
('ReportTemplateTable','datasourceName','数据源','Datasource','資料來源','データソース','데이터 소스',140,NULL,5,0,NULL,NULL,NULL,0),
('ReportTemplateTable','status','状态','Status','狀態','状態','상태',100,NULL,6,1,'select','eq','status',0),
('ReportTemplateTable','remark','备注','Remark','備註','備考','비고',200,NULL,7,0,NULL,NULL,NULL,1),
('ReportTemplateTable','createTime','创建时间','Create Time','建立時間','作成日時','생성 시간',180,NULL,8,0,NULL,NULL,NULL,0),
('ReportTemplateTable','action','操作','Action','操作','操作','작업',280,'right',99,0,NULL,NULL,NULL,0),

('WfCompensationCenterTable','taskName','任务名称','Task Name','任務名稱','タスク名','작업명',180,NULL,1,1,'input','like',NULL,0),
('WfCompensationCenterTable','taskCode','任务编码','Task Code','任務編碼','タスクコード','작업 코드',150,NULL,2,1,'input','like',NULL,0),
('WfCompensationCenterTable','initiatorName','发起人','Initiator','發起人','申請者','기안자',120,NULL,3,1,'input','like',NULL,0),
('WfCompensationCenterTable','currentNodeName','当前节点','Current Node','目前節點','現在ノード','현재 노드',150,NULL,4,0,NULL,NULL,NULL,0),
('WfCompensationCenterTable','status','状态','Status','狀態','状態','상태',110,NULL,5,1,'select','eq','wf_execution_status',0),
('WfCompensationCenterTable','governanceTag','治理标记','Governance Tag','治理標記','ガバナンスタグ','거버넌스 태그',220,NULL,6,0,NULL,NULL,NULL,0),
('WfCompensationCenterTable','startTime','发起时间','Start Time','發起時間','開始日時','시작 시간',180,NULL,7,1,'dateRange','between',NULL,0),
('WfCompensationCenterTable','activeInstanceCount','激活待办数','Active Pending Count','啟用待辦數','アクティブ保留数','활성 대기 수',130,NULL,8,0,NULL,NULL,NULL,0),
('WfCompensationCenterTable','action','操作','Action','操作','操作','작업',260,'right',99,0,NULL,NULL,NULL,0);

INSERT INTO fx_table_column_config (
  tenant_id, table_code, field, title_i18n_json, align, width, fixed, ellipsis, sortable,
  queryable, query_type, query_operator, dict_code, order_num, enabled, create_by, update_by, deleted
)
SELECT
  0,
  c.table_code,
  c.field_name,
  JSON_OBJECT('zh-CN', c.zh_cn, 'en-US', c.en_us, 'zh-TW', c.zh_tw, 'ja-JP', c.ja_jp, 'ko-KR', c.ko_kr),
  'center',
  c.width_val,
  c.fixed_val,
  c.ellipsis_val,
  0,
  c.queryable_val,
  c.query_type_val,
  c.query_operator_val,
  c.dict_code_val,
  c.order_num_val,
  1,
  'i18n_repair',
  'i18n_repair',
  0
FROM tmp_fx_table_col_20260508 c
WHERE NOT EXISTS (
  SELECT 1 FROM fx_table_column_config t
  WHERE t.tenant_id = 0 AND t.table_code = c.table_code AND t.field = c.field_name AND t.deleted = 0
);

INSERT INTO fx_table_column_config (
  tenant_id, table_code, field, title_i18n_json, align, width, fixed, ellipsis, sortable,
  sorter_field, queryable, query_type, query_operator, dict_code, render_type, perm_key,
  order_num, enabled, create_by, update_by, deleted
)
SELECT
  0,
  'basic_supplier',
  s.field,
  s.title_i18n_json,
  s.align,
  s.width,
  s.fixed,
  s.ellipsis,
  s.sortable,
  s.sorter_field,
  s.queryable,
  s.query_type,
  s.query_operator,
  s.dict_code,
  s.render_type,
  s.perm_key,
  s.order_num,
  s.enabled,
  'i18n_repair',
  'i18n_repair',
  0
FROM fx_table_column_config s
WHERE s.tenant_id = 0 AND s.table_code = 'SupplierMasterTable' AND s.deleted = 0
  AND NOT EXISTS (
    SELECT 1 FROM fx_table_column_config t
    WHERE t.tenant_id = 0 AND t.table_code = 'basic_supplier' AND t.field = s.field AND t.deleted = 0
  );

INSERT INTO fx_table_column_config (
  tenant_id, table_code, field, title_i18n_json, align, width, fixed, ellipsis, sortable,
  sorter_field, queryable, query_type, query_operator, dict_code, render_type, perm_key,
  order_num, enabled, create_by, update_by, deleted
)
SELECT
  0,
  'sys_user',
  s.field,
  s.title_i18n_json,
  s.align,
  s.width,
  s.fixed,
  s.ellipsis,
  s.sortable,
  s.sorter_field,
  s.queryable,
  s.query_type,
  s.query_operator,
  s.dict_code,
  s.render_type,
  s.perm_key,
  s.order_num,
  s.enabled,
  'i18n_repair',
  'i18n_repair',
  0
FROM fx_table_column_config s
WHERE s.tenant_id = 0 AND s.table_code = 'UserTable' AND s.deleted = 0
  AND NOT EXISTS (
    SELECT 1 FROM fx_table_column_config t
    WHERE t.tenant_id = 0 AND t.table_code = 'sys_user' AND t.field = s.field AND t.deleted = 0
  );

DROP TEMPORARY TABLE IF EXISTS tmp_fx_table_col_20260508;
DROP TEMPORARY TABLE IF EXISTS tmp_fx_table_cfg_20260508;
