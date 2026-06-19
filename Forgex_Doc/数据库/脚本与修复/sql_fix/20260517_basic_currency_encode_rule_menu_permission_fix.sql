-- Forgex 基础信息菜单与权限修复脚本
-- 修复内容：
--   1. 编码规则管理改为基础信息模块下的单页面入口，不再展示旧子目录。
--   2. 补齐币种管理菜单与按钮权限，让已有 basic/currency 页面可见可授权。
--   3. 将本次修复涉及的基础信息菜单授权给 admin 角色。
-- 可重复执行：插入均带 NOT EXISTS 防重，更新按业务键限定。

SET NAMES utf8mb4;

USE `forgex_admin`;

START TRANSACTION;

SET @encode_menu_id := (
  SELECT MIN(m.id)
  FROM `sys_menu` m
  JOIN `sys_module` module
    ON module.id = m.module_id
   AND module.tenant_id = m.tenant_id
   AND module.code = 'basic'
  WHERE m.deleted = 0
    AND (
      m.component_key = 'BasicEncodeRule'
      OR (m.type = 'menu' AND m.path = 'encodeRule')
    )
);

SET @currency_menu_id := (
  SELECT MIN(m.id)
  FROM `sys_menu` m
  JOIN `sys_module` module
    ON module.id = m.module_id
   AND module.tenant_id = m.tenant_id
   AND module.code = 'basic'
  WHERE m.deleted = 0
    AND (
      m.component_key = 'BasicCurrency'
      OR (m.type = 'menu' AND m.path = 'currency')
    )
);

-- 编码规则管理：主入口必须是基础信息模块下的页面，而不是目录。
UPDATE `sys_menu` m
JOIN `sys_module` module
  ON module.id = m.module_id
 AND module.tenant_id = m.tenant_id
 AND module.code = 'basic'
SET m.parent_id = 0,
    m.type = 'menu',
    m.path = 'encodeRule',
    m.name = '编码规则管理',
    m.name_i18n_json = JSON_OBJECT(
      'zh-CN', '编码规则管理',
      'zh-TW', '編碼規則管理',
      'en-US', 'Encoding Rule Management',
      'ja-JP', '採番ルール管理',
      'ko-KR', '인코딩 규칙 관리'
    ),
    m.icon = 'CodeOutlined',
    m.component_key = 'BasicEncodeRule',
    m.perm_key = 'basic:encodeRule:query',
    m.order_num = 10,
    m.visible = 1,
    m.status = 1,
    m.deleted = 0,
    m.menu_level = 1,
    m.menu_mode = 'embedded',
    m.external_url = NULL,
    m.update_by = '20260517_basic_menu_fix',
    m.update_time = NOW()
WHERE m.id = @encode_menu_id;

-- 如果历史修复曾把查询按钮误改成编码规则主入口，只保留最早的主入口，其余重复入口隐藏。
UPDATE `sys_menu` duplicate_menu
JOIN `sys_menu` encode_menu
  ON encode_menu.id = @encode_menu_id
SET duplicate_menu.visible = 0,
    duplicate_menu.status = 0,
    duplicate_menu.deleted = 1,
    duplicate_menu.update_by = '20260517_basic_menu_fix',
    duplicate_menu.update_time = NOW()
WHERE duplicate_menu.id <> @encode_menu_id
  AND duplicate_menu.tenant_id = encode_menu.tenant_id
  AND duplicate_menu.module_id = encode_menu.module_id
  AND duplicate_menu.type = 'menu'
  AND duplicate_menu.component_key = 'BasicEncodeRule'
  AND duplicate_menu.perm_key = 'basic:encodeRule:query';

-- 隐藏历史上挂在编码规则下的旧子页面，保留主页面按钮权限。
UPDATE `sys_menu` child
JOIN `sys_menu` parent
  ON parent.id = child.parent_id
 AND parent.tenant_id = child.tenant_id
SET child.visible = 0,
    child.status = 0,
    child.deleted = 1,
    child.update_by = '20260517_basic_menu_fix',
    child.update_time = NOW()
WHERE parent.component_key = 'BasicEncodeRule'
  AND child.type = 'menu'
  AND (
    child.path IN ('/encode/rule', '/encode/example', '/encode/history', 'rule', 'example', 'history')
    OR child.perm_key LIKE 'menu:encode:%'
    OR child.component_key LIKE 'system/encode%'
  );

-- 确保编码规则按钮都直接挂在主页面下。
UPDATE `sys_menu` button_menu
JOIN `sys_menu` encode_menu
  ON encode_menu.tenant_id = button_menu.tenant_id
 AND encode_menu.component_key = 'BasicEncodeRule'
 AND encode_menu.deleted = 0
SET button_menu.module_id = encode_menu.module_id,
    button_menu.parent_id = encode_menu.id,
    button_menu.type = 'button',
    button_menu.visible = 1,
    button_menu.status = 1,
    button_menu.deleted = 0,
    button_menu.menu_level = 2,
    button_menu.update_by = '20260517_basic_menu_fix',
    button_menu.update_time = NOW()
WHERE button_menu.perm_key IN (
  'basic:encodeRule:add',
  'basic:encodeRule:edit',
  'basic:encodeRule:delete',
  'basic:encodeRule:query',
  'basic:encodeRule:test',
  'basic:encodeRule:generate'
)
  AND button_menu.type = 'button';

-- 历史数据中查询按钮可能与主菜单共用 basic:encodeRule:query，需要保持为按钮形态。
UPDATE `sys_menu` button_menu
JOIN `sys_menu` encode_menu
  ON encode_menu.tenant_id = button_menu.tenant_id
 AND encode_menu.component_key = 'BasicEncodeRule'
 AND encode_menu.type = 'menu'
 AND encode_menu.deleted = 0
SET button_menu.module_id = encode_menu.module_id,
    button_menu.parent_id = encode_menu.id,
    button_menu.type = 'button',
    button_menu.path = NULL,
    button_menu.name = '查询',
    button_menu.name_i18n_json = JSON_OBJECT(
      'zh-CN', '查询',
      'zh-TW', '查詢',
      'en-US', 'Query',
      'ja-JP', '検索',
      'ko-KR', '조회'
    ),
    button_menu.icon = NULL,
    button_menu.component_key = NULL,
    button_menu.perm_key = 'basic:encodeRule:query',
    button_menu.order_num = 4,
    button_menu.visible = 1,
    button_menu.status = 1,
    button_menu.deleted = 0,
    button_menu.menu_level = 2,
    button_menu.menu_mode = 'embedded',
    button_menu.external_url = NULL,
    button_menu.update_by = '20260517_basic_menu_fix',
    button_menu.update_time = NOW()
WHERE button_menu.perm_key = 'basic:encodeRule:query'
  AND button_menu.type = 'button';

INSERT INTO `sys_menu` (
  tenant_id, tenant_type, module_id, parent_id, type, path, name, name_i18n_json,
  icon, component_key, perm_key, order_num, visible, status,
  create_by, update_by, deleted, menu_level, menu_mode, external_url
)
SELECT menu.tenant_id, 'PUBLIC', menu.module_id, menu.id, 'button', NULL,
       '查询', JSON_OBJECT('zh-CN', '查询', 'zh-TW', '查詢', 'en-US', 'Query', 'ja-JP', '検索', 'ko-KR', '조회'),
       NULL, NULL, 'basic:encodeRule:query', 4, 1, 1,
       '20260517_basic_menu_fix', '20260517_basic_menu_fix', 0, 2, 'embedded', NULL
FROM `sys_menu` menu
WHERE menu.id = @encode_menu_id
  AND NOT EXISTS (
    SELECT 1
    FROM `sys_menu` b
    WHERE b.tenant_id = menu.tenant_id
      AND b.parent_id = menu.id
      AND b.type = 'button'
      AND b.perm_key = 'basic:encodeRule:query'
      AND b.deleted = 0
  );

-- 币种管理页面已存在于前端 src/views/basic/currency/index.vue，这里补菜单入口。
INSERT INTO `sys_menu` (
  tenant_id, tenant_type, module_id, parent_id, type, path, name, name_i18n_json,
  icon, component_key, perm_key, order_num, visible, status,
  create_by, update_by, deleted, menu_level, menu_mode, external_url
)
SELECT
  module.tenant_id,
  'PUBLIC',
  module.id,
  0,
  'menu',
  'currency',
  '币种管理',
  JSON_OBJECT(
    'zh-CN', '币种管理',
    'zh-TW', '幣種管理',
    'en-US', 'Currency Management',
    'ja-JP', '通貨管理',
    'ko-KR', '통화 관리'
  ),
  'DollarOutlined',
  'BasicCurrency',
  'basic:currency:query',
  55,
  1,
  1,
  '20260517_basic_menu_fix',
  '20260517_basic_menu_fix',
  0,
  1,
  'embedded',
  NULL
FROM `sys_module` module
WHERE module.code = 'basic'
  AND module.deleted = 0
  AND @currency_menu_id IS NULL
  AND NOT EXISTS (
    SELECT 1
    FROM `sys_menu` existing
    WHERE existing.tenant_id = module.tenant_id
      AND existing.module_id = module.id
      AND existing.deleted = 0
      AND (
        existing.component_key = 'BasicCurrency'
        OR existing.perm_key = 'basic:currency:query'
        OR existing.path = 'currency'
      )
  );

-- 修复已存在但被隐藏/禁用/挂错组件的币种菜单。
UPDATE `sys_menu` m
JOIN `sys_module` module
  ON module.id = m.module_id
 AND module.tenant_id = m.tenant_id
 AND module.code = 'basic'
SET m.parent_id = 0,
    m.type = 'menu',
    m.path = 'currency',
    m.name = '币种管理',
    m.name_i18n_json = JSON_OBJECT(
      'zh-CN', '币种管理',
      'zh-TW', '幣種管理',
      'en-US', 'Currency Management',
      'ja-JP', '通貨管理',
      'ko-KR', '통화 관리'
    ),
    m.icon = 'DollarOutlined',
    m.component_key = 'BasicCurrency',
    m.perm_key = 'basic:currency:query',
    m.order_num = 55,
    m.visible = 1,
    m.status = 1,
    m.deleted = 0,
    m.menu_level = 1,
    m.menu_mode = 'embedded',
    m.external_url = NULL,
    m.update_by = '20260517_basic_menu_fix',
    m.update_time = NOW()
WHERE m.id = COALESCE(@currency_menu_id, m.id)
  AND (
    m.component_key = 'BasicCurrency'
    OR m.path = 'currency'
  );

SET @currency_menu_id := (
  SELECT MIN(m.id)
  FROM `sys_menu` m
  JOIN `sys_module` module
    ON module.id = m.module_id
   AND module.tenant_id = m.tenant_id
   AND module.code = 'basic'
  WHERE m.deleted = 0
    AND (
      m.component_key = 'BasicCurrency'
      OR (m.type = 'menu' AND m.path = 'currency')
    )
);

-- 隐藏重复币种主入口以及挂在重复入口下的按钮，保留一个可导航页面。
UPDATE `sys_menu` duplicate_menu
JOIN `sys_menu` currency_menu
  ON currency_menu.id = @currency_menu_id
SET duplicate_menu.visible = 0,
    duplicate_menu.status = 0,
    duplicate_menu.deleted = 1,
    duplicate_menu.update_by = '20260517_basic_menu_fix',
    duplicate_menu.update_time = NOW()
WHERE duplicate_menu.id <> @currency_menu_id
  AND duplicate_menu.tenant_id = currency_menu.tenant_id
  AND duplicate_menu.module_id = currency_menu.module_id
  AND duplicate_menu.type = 'menu'
  AND duplicate_menu.component_key = 'BasicCurrency';

UPDATE `sys_menu` duplicate_child
LEFT JOIN `sys_menu` active_parent
  ON active_parent.id = duplicate_child.parent_id
 AND active_parent.id = @currency_menu_id
JOIN `sys_menu` currency_menu
  ON currency_menu.id = @currency_menu_id
SET duplicate_child.visible = 0,
    duplicate_child.status = 0,
    duplicate_child.deleted = 1,
    duplicate_child.update_by = '20260517_basic_menu_fix',
    duplicate_child.update_time = NOW()
WHERE duplicate_child.tenant_id = currency_menu.tenant_id
  AND duplicate_child.module_id = currency_menu.module_id
  AND duplicate_child.type = 'button'
  AND duplicate_child.parent_id <> @currency_menu_id
  AND duplicate_child.perm_key IN (
    'basic:currency:query',
    'basic:currency:add',
    'basic:currency:edit',
    'basic:currency:delete',
    'basic:currency:setBase',
    'basic:exchangeRate:add',
    'basic:exchangeRate:edit',
    'basic:exchangeRate:delete',
    'basic:exchangeRate:approval'
  );

-- 币种管理按钮权限。页面中实际使用 query/add/edit/delete/setBase 与汇率 add/edit/delete/approval。
INSERT INTO `sys_menu` (
  tenant_id, tenant_type, module_id, parent_id, type, path, name, name_i18n_json,
  icon, component_key, perm_key, order_num, visible, status,
  create_by, update_by, deleted, menu_level, menu_mode, external_url
)
SELECT menu.tenant_id, 'PUBLIC', menu.module_id, menu.id, 'button', NULL,
       '查询', JSON_OBJECT('zh-CN', '查询', 'zh-TW', '查詢', 'en-US', 'Query', 'ja-JP', '検索', 'ko-KR', '조회'),
       NULL, NULL, 'basic:currency:query', 1, 1, 1,
       '20260517_basic_menu_fix', '20260517_basic_menu_fix', 0, 2, 'embedded', NULL
FROM `sys_menu` menu
WHERE menu.id = @currency_menu_id
  AND menu.deleted = 0
  AND NOT EXISTS (
    SELECT 1 FROM `sys_menu` b
    WHERE b.tenant_id = menu.tenant_id
      AND b.parent_id = menu.id
      AND b.perm_key = 'basic:currency:query'
      AND b.deleted = 0
  );

INSERT INTO `sys_menu` (
  tenant_id, tenant_type, module_id, parent_id, type, path, name, name_i18n_json,
  icon, component_key, perm_key, order_num, visible, status,
  create_by, update_by, deleted, menu_level, menu_mode, external_url
)
SELECT menu.tenant_id, 'PUBLIC', menu.module_id, menu.id, 'button', NULL,
       '新增', JSON_OBJECT('zh-CN', '新增', 'zh-TW', '新增', 'en-US', 'Add', 'ja-JP', '追加', 'ko-KR', '추가'),
       NULL, NULL, 'basic:currency:add', 2, 1, 1,
       '20260517_basic_menu_fix', '20260517_basic_menu_fix', 0, 2, 'embedded', NULL
FROM `sys_menu` menu
WHERE menu.id = @currency_menu_id
  AND menu.deleted = 0
  AND NOT EXISTS (
    SELECT 1 FROM `sys_menu` b
    WHERE b.tenant_id = menu.tenant_id
      AND b.parent_id = menu.id
      AND b.perm_key = 'basic:currency:add'
      AND b.deleted = 0
  );

INSERT INTO `sys_menu` (
  tenant_id, tenant_type, module_id, parent_id, type, path, name, name_i18n_json,
  icon, component_key, perm_key, order_num, visible, status,
  create_by, update_by, deleted, menu_level, menu_mode, external_url
)
SELECT menu.tenant_id, 'PUBLIC', menu.module_id, menu.id, 'button', NULL,
       '编辑', JSON_OBJECT('zh-CN', '编辑', 'zh-TW', '編輯', 'en-US', 'Edit', 'ja-JP', '編集', 'ko-KR', '편집'),
       NULL, NULL, 'basic:currency:edit', 3, 1, 1,
       '20260517_basic_menu_fix', '20260517_basic_menu_fix', 0, 2, 'embedded', NULL
FROM `sys_menu` menu
WHERE menu.id = @currency_menu_id
  AND menu.deleted = 0
  AND NOT EXISTS (
    SELECT 1 FROM `sys_menu` b
    WHERE b.tenant_id = menu.tenant_id
      AND b.parent_id = menu.id
      AND b.perm_key = 'basic:currency:edit'
      AND b.deleted = 0
  );

INSERT INTO `sys_menu` (
  tenant_id, tenant_type, module_id, parent_id, type, path, name, name_i18n_json,
  icon, component_key, perm_key, order_num, visible, status,
  create_by, update_by, deleted, menu_level, menu_mode, external_url
)
SELECT menu.tenant_id, 'PUBLIC', menu.module_id, menu.id, 'button', NULL,
       '删除', JSON_OBJECT('zh-CN', '删除', 'zh-TW', '刪除', 'en-US', 'Delete', 'ja-JP', '削除', 'ko-KR', '삭제'),
       NULL, NULL, 'basic:currency:delete', 4, 1, 1,
       '20260517_basic_menu_fix', '20260517_basic_menu_fix', 0, 2, 'embedded', NULL
FROM `sys_menu` menu
WHERE menu.id = @currency_menu_id
  AND menu.deleted = 0
  AND NOT EXISTS (
    SELECT 1 FROM `sys_menu` b
    WHERE b.tenant_id = menu.tenant_id
      AND b.parent_id = menu.id
      AND b.perm_key = 'basic:currency:delete'
      AND b.deleted = 0
  );

INSERT INTO `sys_menu` (
  tenant_id, tenant_type, module_id, parent_id, type, path, name, name_i18n_json,
  icon, component_key, perm_key, order_num, visible, status,
  create_by, update_by, deleted, menu_level, menu_mode, external_url
)
SELECT menu.tenant_id, 'PUBLIC', menu.module_id, menu.id, 'button', NULL,
       '设为本位币', JSON_OBJECT('zh-CN', '设为本位币', 'zh-TW', '設為本位幣', 'en-US', 'Set Base Currency', 'ja-JP', '基準通貨に設定', 'ko-KR', '기준 통화 설정'),
       NULL, NULL, 'basic:currency:setBase', 5, 1, 1,
       '20260517_basic_menu_fix', '20260517_basic_menu_fix', 0, 2, 'embedded', NULL
FROM `sys_menu` menu
WHERE menu.id = @currency_menu_id
  AND menu.deleted = 0
  AND NOT EXISTS (
    SELECT 1 FROM `sys_menu` b
    WHERE b.tenant_id = menu.tenant_id
      AND b.parent_id = menu.id
      AND b.perm_key = 'basic:currency:setBase'
      AND b.deleted = 0
  );

INSERT INTO `sys_menu` (
  tenant_id, tenant_type, module_id, parent_id, type, path, name, name_i18n_json,
  icon, component_key, perm_key, order_num, visible, status,
  create_by, update_by, deleted, menu_level, menu_mode, external_url
)
SELECT menu.tenant_id, 'PUBLIC', menu.module_id, menu.id, 'button', NULL,
       '新增汇率', JSON_OBJECT('zh-CN', '新增汇率', 'zh-TW', '新增匯率', 'en-US', 'Add Exchange Rate', 'ja-JP', '為替レート追加', 'ko-KR', '환율 추가'),
       NULL, NULL, 'basic:exchangeRate:add', 6, 1, 1,
       '20260517_basic_menu_fix', '20260517_basic_menu_fix', 0, 2, 'embedded', NULL
FROM `sys_menu` menu
WHERE menu.id = @currency_menu_id
  AND menu.deleted = 0
  AND NOT EXISTS (
    SELECT 1 FROM `sys_menu` b
    WHERE b.tenant_id = menu.tenant_id
      AND b.parent_id = menu.id
      AND b.perm_key = 'basic:exchangeRate:add'
      AND b.deleted = 0
  );

INSERT INTO `sys_menu` (
  tenant_id, tenant_type, module_id, parent_id, type, path, name, name_i18n_json,
  icon, component_key, perm_key, order_num, visible, status,
  create_by, update_by, deleted, menu_level, menu_mode, external_url
)
SELECT menu.tenant_id, 'PUBLIC', menu.module_id, menu.id, 'button', NULL,
       '编辑汇率', JSON_OBJECT('zh-CN', '编辑汇率', 'zh-TW', '編輯匯率', 'en-US', 'Edit Exchange Rate', 'ja-JP', '為替レート編集', 'ko-KR', '환율 편집'),
       NULL, NULL, 'basic:exchangeRate:edit', 7, 1, 1,
       '20260517_basic_menu_fix', '20260517_basic_menu_fix', 0, 2, 'embedded', NULL
FROM `sys_menu` menu
WHERE menu.id = @currency_menu_id
  AND menu.deleted = 0
  AND NOT EXISTS (
    SELECT 1 FROM `sys_menu` b
    WHERE b.tenant_id = menu.tenant_id
      AND b.parent_id = menu.id
      AND b.perm_key = 'basic:exchangeRate:edit'
      AND b.deleted = 0
  );

INSERT INTO `sys_menu` (
  tenant_id, tenant_type, module_id, parent_id, type, path, name, name_i18n_json,
  icon, component_key, perm_key, order_num, visible, status,
  create_by, update_by, deleted, menu_level, menu_mode, external_url
)
SELECT menu.tenant_id, 'PUBLIC', menu.module_id, menu.id, 'button', NULL,
       '删除汇率', JSON_OBJECT('zh-CN', '删除汇率', 'zh-TW', '刪除匯率', 'en-US', 'Delete Exchange Rate', 'ja-JP', '為替レート削除', 'ko-KR', '환율 삭제'),
       NULL, NULL, 'basic:exchangeRate:delete', 8, 1, 1,
       '20260517_basic_menu_fix', '20260517_basic_menu_fix', 0, 2, 'embedded', NULL
FROM `sys_menu` menu
WHERE menu.id = @currency_menu_id
  AND menu.deleted = 0
  AND NOT EXISTS (
    SELECT 1 FROM `sys_menu` b
    WHERE b.tenant_id = menu.tenant_id
      AND b.parent_id = menu.id
      AND b.perm_key = 'basic:exchangeRate:delete'
      AND b.deleted = 0
  );

INSERT INTO `sys_menu` (
  tenant_id, tenant_type, module_id, parent_id, type, path, name, name_i18n_json,
  icon, component_key, perm_key, order_num, visible, status,
  create_by, update_by, deleted, menu_level, menu_mode, external_url
)
SELECT menu.tenant_id, 'PUBLIC', menu.module_id, menu.id, 'button', NULL,
       '发起汇率审批', JSON_OBJECT('zh-CN', '发起汇率审批', 'zh-TW', '發起匯率審批', 'en-US', 'Start Exchange Rate Approval', 'ja-JP', '為替レート承認開始', 'ko-KR', '환율 승인 시작'),
       NULL, NULL, 'basic:exchangeRate:approval', 9, 1, 1,
       '20260517_basic_menu_fix', '20260517_basic_menu_fix', 0, 2, 'embedded', NULL
FROM `sys_menu` menu
WHERE menu.id = @currency_menu_id
  AND menu.deleted = 0
  AND NOT EXISTS (
    SELECT 1 FROM `sys_menu` b
    WHERE b.tenant_id = menu.tenant_id
      AND b.parent_id = menu.id
      AND b.perm_key = 'basic:exchangeRate:approval'
      AND b.deleted = 0
  );

-- 修正已存在币种按钮的中文名称，处理旧脚本通过非 UTF-8 管道执行后产生的问号名称。
UPDATE `sys_menu` b
JOIN `sys_menu` currency_menu
  ON currency_menu.id = @currency_menu_id
SET b.name = CASE b.perm_key
    WHEN 'basic:currency:query' THEN '查询'
    WHEN 'basic:currency:add' THEN '新增'
    WHEN 'basic:currency:edit' THEN '编辑'
    WHEN 'basic:currency:delete' THEN '删除'
    WHEN 'basic:currency:setBase' THEN '设为本位币'
    WHEN 'basic:exchangeRate:add' THEN '新增汇率'
    WHEN 'basic:exchangeRate:edit' THEN '编辑汇率'
    WHEN 'basic:exchangeRate:delete' THEN '删除汇率'
    WHEN 'basic:exchangeRate:approval' THEN '发起汇率审批'
    ELSE b.name
  END,
  b.name_i18n_json = CASE b.perm_key
    WHEN 'basic:currency:query' THEN JSON_OBJECT('zh-CN', '查询', 'zh-TW', '查詢', 'en-US', 'Query', 'ja-JP', '検索', 'ko-KR', '조회')
    WHEN 'basic:currency:add' THEN JSON_OBJECT('zh-CN', '新增', 'zh-TW', '新增', 'en-US', 'Add', 'ja-JP', '追加', 'ko-KR', '추가')
    WHEN 'basic:currency:edit' THEN JSON_OBJECT('zh-CN', '编辑', 'zh-TW', '編輯', 'en-US', 'Edit', 'ja-JP', '編集', 'ko-KR', '편집')
    WHEN 'basic:currency:delete' THEN JSON_OBJECT('zh-CN', '删除', 'zh-TW', '刪除', 'en-US', 'Delete', 'ja-JP', '削除', 'ko-KR', '삭제')
    WHEN 'basic:currency:setBase' THEN JSON_OBJECT('zh-CN', '设为本位币', 'zh-TW', '設為本位幣', 'en-US', 'Set Base Currency', 'ja-JP', '基準通貨に設定', 'ko-KR', '기준 통화 설정')
    WHEN 'basic:exchangeRate:add' THEN JSON_OBJECT('zh-CN', '新增汇率', 'zh-TW', '新增匯率', 'en-US', 'Add Exchange Rate', 'ja-JP', '為替レート追加', 'ko-KR', '환율 추가')
    WHEN 'basic:exchangeRate:edit' THEN JSON_OBJECT('zh-CN', '编辑汇率', 'zh-TW', '編輯匯率', 'en-US', 'Edit Exchange Rate', 'ja-JP', '為替レート編集', 'ko-KR', '환율 편집')
    WHEN 'basic:exchangeRate:delete' THEN JSON_OBJECT('zh-CN', '删除汇率', 'zh-TW', '刪除匯率', 'en-US', 'Delete Exchange Rate', 'ja-JP', '為替レート削除', 'ko-KR', '환율 삭제')
    WHEN 'basic:exchangeRate:approval' THEN JSON_OBJECT('zh-CN', '发起汇率审批', 'zh-TW', '發起匯率審批', 'en-US', 'Start Exchange Rate Approval', 'ja-JP', '為替レート承認開始', 'ko-KR', '환율 승인 시작')
    ELSE b.name_i18n_json
  END,
  b.parent_id = currency_menu.id,
  b.module_id = currency_menu.module_id,
  b.type = 'button',
  b.path = NULL,
  b.icon = NULL,
  b.component_key = NULL,
  b.visible = 1,
  b.status = 1,
  b.deleted = 0,
  b.menu_level = 2,
  b.update_by = '20260517_basic_menu_fix',
  b.update_time = NOW()
WHERE b.parent_id = currency_menu.id
  AND b.perm_key IN (
    'basic:currency:query',
    'basic:currency:add',
    'basic:currency:edit',
    'basic:currency:delete',
    'basic:currency:setBase',
    'basic:exchangeRate:add',
    'basic:exchangeRate:edit',
    'basic:exchangeRate:delete',
    'basic:exchangeRate:approval'
  );

-- 将本次涉及的基础信息菜单补授权给 admin 角色，避免页面存在但菜单不可见。
INSERT INTO `sys_role_menu` (tenant_id, role_id, menu_id)
SELECT role.tenant_id, role.id, menu.id
FROM `sys_role` role
JOIN `sys_menu` menu
  ON menu.tenant_id = role.tenant_id
WHERE role.role_key = 'admin'
  AND role.deleted = 0
  AND menu.deleted = 0
  AND (
    menu.component_key IN ('BasicEncodeRule', 'BasicCurrency')
    OR menu.perm_key IN (
      'basic:encodeRule:add',
      'basic:encodeRule:edit',
      'basic:encodeRule:delete',
      'basic:encodeRule:query',
      'basic:encodeRule:test',
      'basic:encodeRule:generate',
      'basic:currency:query',
      'basic:currency:add',
      'basic:currency:edit',
      'basic:currency:delete',
      'basic:currency:setBase',
      'basic:exchangeRate:add',
      'basic:exchangeRate:edit',
      'basic:exchangeRate:delete',
      'basic:exchangeRate:approval'
    )
  )
  AND NOT EXISTS (
    SELECT 1
    FROM `sys_role_menu` rm
    WHERE rm.tenant_id = role.tenant_id
      AND rm.role_id = role.id
      AND rm.menu_id = menu.id
  );

COMMIT;

SELECT id, name, type, path, component_key, perm_key, parent_id, visible, status, deleted
FROM `sys_menu`
WHERE component_key IN ('BasicEncodeRule', 'BasicCurrency')
   OR perm_key LIKE 'basic:encodeRule:%'
   OR perm_key LIKE 'basic:currency:%'
   OR perm_key LIKE 'basic:exchangeRate:%'
ORDER BY module_id, parent_id, order_num, id;
