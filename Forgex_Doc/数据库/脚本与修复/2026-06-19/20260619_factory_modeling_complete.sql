-- ============================================================
-- 工厂建模模块 - 完整数据初始化脚本
-- 创建日期：2026-06-19
-- 作者：ForGexTeam
-- 说明：基础数据 → 工厂建模 → 车间/产线/工段/工序 全套数据
-- ============================================================

SET NAMES utf8mb4;
SET @script_user := 0;
SET @script_tenant := 1;

USE forgex_admin;

-- ============================================================
-- 一、建表（3 张新表 + basic_workshop 扩展）
-- ============================================================

-- 1.1 产线主数据表
CREATE TABLE IF NOT EXISTS basic_production_line (
    id                BIGINT       NOT NULL AUTO_INCREMENT COMMENT '产线 ID（雪花算法）',
    tenant_id         BIGINT       NOT NULL DEFAULT 0   COMMENT '租户 ID',
    line_code         VARCHAR(64)  NOT NULL              COMMENT '产线编码',
    line_name         VARCHAR(128) NOT NULL              COMMENT '产线名称',
    workshop_id       BIGINT       NOT NULL              COMMENT '所属车间 ID',
    prod_line_type    VARCHAR(50)  DEFAULT NULL          COMMENT '产线类型（字典）',
    capability        VARCHAR(500) DEFAULT NULL          COMMENT '产线产能描述',
    status            TINYINT(1)   NOT NULL DEFAULT 1    COMMENT '状态：0-禁用 1-启用',
    remark            VARCHAR(500) DEFAULT NULL          COMMENT '备注',
    create_by         BIGINT       DEFAULT NULL          COMMENT '创建人',
    create_time       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by         BIGINT       DEFAULT NULL          COMMENT '更新人',
    update_time       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted           TINYINT(1)   NOT NULL DEFAULT 0    COMMENT '逻辑删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_line_tenant_code (tenant_id, line_code, deleted),
    KEY idx_line_workshop (workshop_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='产线主数据表';

-- 1.2 工段主数据表
CREATE TABLE IF NOT EXISTS basic_work_section (
    id                BIGINT       NOT NULL AUTO_INCREMENT COMMENT '工段 ID',
    tenant_id         BIGINT       NOT NULL DEFAULT 0   COMMENT '租户 ID',
    section_code      VARCHAR(64)  NOT NULL              COMMENT '工段编码',
    section_name      VARCHAR(128) NOT NULL              COMMENT '工段名称',
    production_line_id BIGINT      NOT NULL              COMMENT '所属产线 ID',
    workshop_id       BIGINT       NOT NULL              COMMENT '所属车间 ID（冗余）',
    section_type      VARCHAR(50)  DEFAULT NULL          COMMENT '工段类型（字典）',
    report_type       VARCHAR(50)  DEFAULT NULL          COMMENT '报工方式（字典）',
    qc_trigger_point  VARCHAR(50)  DEFAULT NULL          COMMENT '质检触发点（字典）',
    status            TINYINT(1)   NOT NULL DEFAULT 1    COMMENT '状态',
    remark            VARCHAR(500) DEFAULT NULL          COMMENT '备注',
    create_by         BIGINT       DEFAULT NULL          COMMENT '创建人',
    create_time       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by         BIGINT       DEFAULT NULL          COMMENT '更新人',
    update_time       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted           TINYINT(1)   NOT NULL DEFAULT 0    COMMENT '逻辑删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_section_tenant_code (tenant_id, section_code, deleted),
    KEY idx_section_line (production_line_id),
    KEY idx_section_workshop (workshop_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工段主数据表';

-- 1.3 工序主数据表
CREATE TABLE IF NOT EXISTS basic_process (
    id                BIGINT       NOT NULL AUTO_INCREMENT COMMENT '工序 ID',
    tenant_id         BIGINT       NOT NULL DEFAULT 0   COMMENT '租户 ID',
    process_code      VARCHAR(64)  NOT NULL              COMMENT '工序编码',
    process_name      VARCHAR(128) NOT NULL              COMMENT '工序名称',
    work_section_id   BIGINT       NOT NULL              COMMENT '所属工段 ID',
    production_line_id BIGINT      NOT NULL              COMMENT '所属产线 ID（冗余）',
    workshop_id       BIGINT       NOT NULL              COMMENT '所属车间 ID（冗余）',
    process_type      VARCHAR(50)  DEFAULT NULL          COMMENT '工序类型（字典）',
    standard_time     DECIMAL(10,2) DEFAULT NULL         COMMENT '标准工时（分钟）',
    sort_order        INT          NOT NULL DEFAULT 0    COMMENT '工序顺序',
    status            TINYINT(1)   NOT NULL DEFAULT 1    COMMENT '状态',
    remark            VARCHAR(500) DEFAULT NULL          COMMENT '备注',
    create_by         BIGINT       DEFAULT NULL          COMMENT '创建人',
    create_time       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by         BIGINT       DEFAULT NULL          COMMENT '更新人',
    update_time       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted           TINYINT(1)   NOT NULL DEFAULT 0    COMMENT '逻辑删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_process_tenant_code (tenant_id, process_code, deleted),
    KEY idx_process_section (work_section_id),
    KEY idx_process_line (production_line_id),
    KEY idx_process_workshop (workshop_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工序主数据表';

-- 1.4 扩展 basic_workshop 字段
ALTER TABLE basic_workshop
    ADD COLUMN IF NOT EXISTS workshop_type      VARCHAR(50)  DEFAULT NULL COMMENT '车间类型（字典）',
    ADD COLUMN IF NOT EXISTS workshop_manager_id BIGINT      DEFAULT NULL COMMENT '车间负责人 ID',
    ADD COLUMN IF NOT EXISTS workshop_manager_name VARCHAR(64) DEFAULT NULL COMMENT '车间负责人姓名';

-- ============================================================
-- 二、字典（5 个根字典 + 24 个字典项）
-- ============================================================

-- 2.1 产线类型
INSERT INTO sys_dict (parent_id, dict_name, dict_code, level, node_path, order_num, status, tenant_id, create_by)
SELECT 0, '产线类型', 'prod_line_type', 1, '/prod_line_type', 1, 1, @script_tenant, @script_user
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict WHERE dict_code='prod_line_type');

INSERT INTO sys_dict (parent_id, dict_name, dict_code, dict_value, level, node_path, order_num, status, tenant_id, create_by)
SELECT p.id, v.dn, v.dv, v.dv, 2, CONCAT(p.node_path,'/',v.dv), v.ord, 1, @script_tenant, @script_user
FROM sys_dict p,
(SELECT 1 ord, '装配线' dn, 'ASSEMBLY' dv UNION ALL
 SELECT 2, '加工线', 'PROCESSING' UNION ALL
 SELECT 3, '包装线', 'PACKAGING' UNION ALL
 SELECT 4, '测试线', 'TESTING' UNION ALL
 SELECT 5, '混合线', 'MIXED') v
WHERE p.dict_code='prod_line_type'
  AND NOT EXISTS (SELECT 1 FROM sys_dict c WHERE c.parent_id=p.id AND c.dict_code=v.dv);

-- 2.2 车间类型
INSERT INTO sys_dict (parent_id, dict_name, dict_code, level, node_path, order_num, status, tenant_id, create_by)
SELECT 0, '车间类型', 'workshop_type', 1, '/workshop_type', 2, 1, @script_tenant, @script_user
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict WHERE dict_code='workshop_type');

INSERT INTO sys_dict (parent_id, dict_name, dict_code, dict_value, level, node_path, order_num, status, tenant_id, create_by)
SELECT p.id, v.dn, v.dv, v.dv, 2, CONCAT(p.node_path,'/',v.dv), v.ord, 1, @script_tenant, @script_user
FROM sys_dict p,
(SELECT 1 ord, '备料车间' dn, 'PREPARATION' dv UNION ALL
 SELECT 2, '加工车间', 'MACHINING' UNION ALL
 SELECT 3, '装配车间', 'ASSEMBLY' UNION ALL
 SELECT 4, '包装车间', 'PACKAGING' UNION ALL
 SELECT 5, '综合车间', 'COMPREHENSIVE') v
WHERE p.dict_code='workshop_type'
  AND NOT EXISTS (SELECT 1 FROM sys_dict c WHERE c.parent_id=p.id AND c.dict_code=v.dv);

-- 2.3 工序类型
INSERT INTO sys_dict (parent_id, dict_name, dict_code, level, node_path, order_num, status, tenant_id, create_by)
SELECT 0, '工序类型', 'process_type', 1, '/process_type', 3, 1, @script_tenant, @script_user
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict WHERE dict_code='process_type');

INSERT INTO sys_dict (parent_id, dict_name, dict_code, dict_value, level, node_path, order_num, status, tenant_id, create_by)
SELECT p.id, v.dn, v.dv, v.dv, 2, CONCAT(p.node_path,'/',v.dv), v.ord, 1, @script_tenant, @script_user
FROM sys_dict p,
(SELECT 1 ord, '机加工序' dn, 'MACHINING' dv UNION ALL
 SELECT 2, '装配工序', 'ASSEMBLY' UNION ALL
 SELECT 3, '检测工序', 'INSPECTION' UNION ALL
 SELECT 4, '包装工序', 'PACKAGING' UNION ALL
 SELECT 5, '物流工序', 'LOGISTICS') v
WHERE p.dict_code='process_type'
  AND NOT EXISTS (SELECT 1 FROM sys_dict c WHERE c.parent_id=p.id AND c.dict_code=v.dv);

-- 2.4 报工方式
INSERT INTO sys_dict (parent_id, dict_name, dict_code, level, node_path, order_num, status, tenant_id, create_by)
SELECT 0, '报工方式', 'report_type', 1, '/report_type', 4, 1, @script_tenant, @script_user
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict WHERE dict_code='report_type');

INSERT INTO sys_dict (parent_id, dict_name, dict_code, dict_value, level, node_path, order_num, status, tenant_id, create_by)
SELECT p.id, v.dn, v.dv, v.dv, 2, CONCAT(p.node_path,'/',v.dv), v.ord, 1, @script_tenant, @script_user
FROM sys_dict p,
(SELECT 1 ord, '按工序报工' dn, 'BY_PROCESS' dv UNION ALL
 SELECT 2, '按工段报工', 'BY_SECTION' UNION ALL
 SELECT 3, '按产线报工', 'BY_LINE' UNION ALL
 SELECT 4, '按批次报工', 'BY_BATCH') v
WHERE p.dict_code='report_type'
  AND NOT EXISTS (SELECT 1 FROM sys_dict c WHERE c.parent_id=p.id AND c.dict_code=v.dv);

-- 2.5 质检触发点
INSERT INTO sys_dict (parent_id, dict_name, dict_code, level, node_path, order_num, status, tenant_id, create_by)
SELECT 0, '质检触发点', 'qc_trigger_point', 1, '/qc_trigger_point', 5, 1, @script_tenant, @script_user
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict WHERE dict_code='qc_trigger_point');

INSERT INTO sys_dict (parent_id, dict_name, dict_code, dict_value, level, node_path, order_num, status, tenant_id, create_by)
SELECT p.id, v.dn, v.dv, v.dv, 2, CONCAT(p.node_path,'/',v.dv), v.ord, 1, @script_tenant, @script_user
FROM sys_dict p,
(SELECT 1 ord, '工序完成' dn, 'AFTER_PROCESS' dv UNION ALL
 SELECT 2, '工段完成', 'AFTER_SECTION' UNION ALL
 SELECT 3, '产线完成', 'AFTER_LINE' UNION ALL
 SELECT 4, '入库前', 'BEFORE_STOCK' UNION ALL
 SELECT 5, '出厂前', 'BEFORE_SHIPMENT') v
WHERE p.dict_code='qc_trigger_point'
  AND NOT EXISTS (SELECT 1 FROM sys_dict c WHERE c.parent_id=p.id AND c.dict_code=v.dv);

-- ============================================================
-- 三、菜单结构：基础数据(504) → 工厂建模(505) → 4 子菜单
-- ============================================================

-- 3.1 一级菜单：基础数据
INSERT INTO sys_menu (id, tenant_id, parent_id, type, name, component_key, perm_key, order_num, visible, status, menu_level)
SELECT 504, @script_tenant, 0, 'menu', '基础数据', 'BasicDataRoot', 'basic:query', 50, 1, 1, 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id=504);

-- 3.2 二级菜单：工厂建模
INSERT INTO sys_menu (id, tenant_id, parent_id, type, name, component_key, perm_key, order_num, visible, status, menu_level)
SELECT 505, @script_tenant, 504, 'menu', '工厂建模', 'FactoryModeling', 'basic:factoryModeling:query', 1, 1, 1, 2
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id=505);

-- 3.3 把车间管理（id=456）移到工厂建模下
UPDATE sys_menu SET parent_id=505, order_num=1 WHERE id=456 AND parent_id != 505;

-- 3.4 车间管理子菜单列表
INSERT INTO sys_menu (id, tenant_id, parent_id, type, name, component_key, perm_key, order_num, visible, status, menu_level)
SELECT 456, @script_tenant, 505, 'menu', '车间管理', 'BasicWorkshop', 'basic:workshop:query', 1, 1, 1, 3
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id=456);

INSERT INTO sys_menu (id, tenant_id, parent_id, type, name, perm_key, order_num, visible, status, menu_level)
SELECT 481, @script_tenant, 456, 'button', '车间查询', 'basic:workshop:query', 1, 1, 1, 4
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id=481);

INSERT INTO sys_menu (id, tenant_id, parent_id, type, name, perm_key, order_num, visible, status, menu_level)
SELECT 463, @script_tenant, 456, 'button', '车间新增', 'basic:workshop:add', 2, 1, 1, 4
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id=463);

INSERT INTO sys_menu (id, tenant_id, parent_id, type, name, perm_key, order_num, visible, status, menu_level)
SELECT 465, @script_tenant, 456, 'button', '车间编辑', 'basic:workshop:edit', 3, 1, 1, 4
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id=465);

INSERT INTO sys_menu (id, tenant_id, parent_id, type, name, perm_key, order_num, visible, status, menu_level)
SELECT 464, @script_tenant, 456, 'button', '车间删除', 'basic:workshop:delete', 4, 1, 1, 4
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id=464);

INSERT INTO sys_menu (id, tenant_id, parent_id, type, name, component_key, perm_key, order_num, visible, status, menu_level)
SELECT 530, @script_tenant, 456, 'button', '按工厂查询车间', 'BasicWorkshop', 'basic:workshop:listByFactory', 6, 1, 1, 4
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id=530);

-- 3.5 产线管理 (506) + 5 按钮
INSERT INTO sys_menu (id, tenant_id, parent_id, type, name, component_key, perm_key, order_num, visible, status, menu_level)
SELECT 506, @script_tenant, 505, 'menu', '产线管理', 'BasicProductionLine', 'basic:productionLine:query', 2, 1, 1, 3
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id=506);

INSERT INTO sys_menu (id, tenant_id, parent_id, type, name, perm_key, order_num, visible, status, menu_level)
SELECT 513, @script_tenant, 506, 'button', '查询', 'basic:productionLine:query', 1, 1, 1, 4
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id=513);
INSERT INTO sys_menu (id, tenant_id, parent_id, type, name, perm_key, order_num, visible, status, menu_level)
SELECT 512, @script_tenant, 506, 'button', '新增', 'basic:productionLine:add', 2, 1, 1, 4
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id=512);
INSERT INTO sys_menu (id, tenant_id, parent_id, type, name, perm_key, order_num, visible, status, menu_level)
SELECT 511, @script_tenant, 506, 'button', '编辑', 'basic:productionLine:edit', 3, 1, 1, 4
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id=511);
INSERT INTO sys_menu (id, tenant_id, parent_id, type, name, perm_key, order_num, visible, status, menu_level)
SELECT 510, @script_tenant, 506, 'button', '删除', 'basic:productionLine:delete', 4, 1, 1, 4
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id=510);
INSERT INTO sys_menu (id, tenant_id, parent_id, type, name, perm_key, order_num, visible, status, menu_level)
SELECT 509, @script_tenant, 506, 'button', '批量删除', 'basic:productionLine:batchDelete', 5, 1, 1, 4
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id=509);

-- 3.6 工段管理 (507) + 5 按钮
INSERT INTO sys_menu (id, tenant_id, parent_id, type, name, component_key, perm_key, order_num, visible, status, menu_level)
SELECT 507, @script_tenant, 505, 'menu', '工段管理', 'BasicWorkSection', 'basic:workSection:query', 3, 1, 1, 3
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id=507);

INSERT INTO sys_menu (id, tenant_id, parent_id, type, name, perm_key, order_num, visible, status, menu_level)
SELECT 520, @script_tenant, 507, 'button', '查询', 'basic:workSection:query', 1, 1, 1, 4
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id=520);
INSERT INTO sys_menu (id, tenant_id, parent_id, type, name, perm_key, order_num, visible, status, menu_level)
SELECT 519, @script_tenant, 507, 'button', '新增', 'basic:workSection:add', 2, 1, 1, 4
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id=519);
INSERT INTO sys_menu (id, tenant_id, parent_id, type, name, perm_key, order_num, visible, status, menu_level)
SELECT 518, @script_tenant, 507, 'button', '编辑', 'basic:workSection:edit', 3, 1, 1, 4
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id=518);
INSERT INTO sys_menu (id, tenant_id, parent_id, type, name, perm_key, order_num, visible, status, menu_level)
SELECT 517, @script_tenant, 507, 'button', '删除', 'basic:workSection:delete', 4, 1, 1, 4
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id=517);
INSERT INTO sys_menu (id, tenant_id, parent_id, type, name, perm_key, order_num, visible, status, menu_level)
SELECT 516, @script_tenant, 507, 'button', '批量删除', 'basic:workSection:batchDelete', 5, 1, 1, 4
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id=516);

-- 3.7 工序管理 (508) + 5 按钮
INSERT INTO sys_menu (id, tenant_id, parent_id, type, name, component_key, perm_key, order_num, visible, status, menu_level)
SELECT 508, @script_tenant, 505, 'menu', '工序管理', 'BasicProcess', 'basic:process:query', 4, 1, 1, 3
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id=508);

INSERT INTO sys_menu (id, tenant_id, parent_id, type, name, perm_key, order_num, visible, status, menu_level)
SELECT 527, @script_tenant, 508, 'button', '查询', 'basic:process:query', 1, 1, 1, 4
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id=527);
INSERT INTO sys_menu (id, tenant_id, parent_id, type, name, perm_key, order_num, visible, status, menu_level)
SELECT 526, @script_tenant, 508, 'button', '新增', 'basic:process:add', 2, 1, 1, 4
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id=526);
INSERT INTO sys_menu (id, tenant_id, parent_id, type, name, perm_key, order_num, visible, status, menu_level)
SELECT 525, @script_tenant, 508, 'button', '编辑', 'basic:process:edit', 3, 1, 1, 4
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id=525);
INSERT INTO sys_menu (id, tenant_id, parent_id, type, name, perm_key, order_num, visible, status, menu_level)
SELECT 524, @script_tenant, 508, 'button', '删除', 'basic:process:delete', 4, 1, 1, 4
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id=524);
INSERT INTO sys_menu (id, tenant_id, parent_id, type, name, perm_key, order_num, visible, status, menu_level)
SELECT 523, @script_tenant, 508, 'button', '批量删除', 'basic:process:batchDelete', 5, 1, 1, 4
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id=523);

-- ============================================================
-- 四、接口权限 sys_permission
-- ============================================================

-- 4.1 车间 5 项
INSERT INTO sys_permission (id, permission_name, permission_key, url, method, tenant_id) VALUES
 (174, '车间查询',  'basic:workshop:query',         '/basic/workshop/page',          'POST', @script_tenant),
 (175, '车间新增',  'basic:workshop:add',           '/basic/workshop/create',        'POST', @script_tenant),
 (176, '车间编辑',  'basic:workshop:edit',          '/basic/workshop/update',        'POST', @script_tenant),
 (177, '车间删除',  'basic:workshop:delete',        '/basic/workshop/delete',        'POST', @script_tenant),
 (222, '按工厂查询车间', 'basic:workshop:listByFactory', '/basic/workshop/listByFactory', 'POST', @script_tenant)
ON DUPLICATE KEY UPDATE permission_name=VALUES(permission_name);

-- 4.2 产线 8 项
INSERT INTO sys_permission (id, permission_name, permission_key, url, method, tenant_id) VALUES
 (197, '产线查询', 'basic:productionLine:query',             '/basic/productionLine/page',              'POST', @script_tenant),
 (198, '产线列表', 'basic:productionLine:list',              '/basic/productionLine/list',              'POST', @script_tenant),
 (199, '按车间拉取产线下拉', 'basic:productionLine:listByWorkshop', '/basic/productionLine/listByWorkshop', 'POST', @script_tenant),
 (200, '产线详情', 'basic:productionLine:detail',            '/basic/productionLine/detail',            'POST', @script_tenant),
 (201, '产线新增', 'basic:productionLine:add',               '/basic/productionLine/create',            'POST', @script_tenant),
 (202, '产线编辑', 'basic:productionLine:edit',              '/basic/productionLine/update',            'POST', @script_tenant),
 (203, '产线删除', 'basic:productionLine:delete',            '/basic/productionLine/delete',            'POST', @script_tenant),
 (204, '产线批量删除', 'basic:productionLine:batchDelete',   '/basic/productionLine/batchDelete',       'POST', @script_tenant)
ON DUPLICATE KEY UPDATE permission_name=VALUES(permission_name);

-- 4.3 工段 8 项
INSERT INTO sys_permission (id, permission_name, permission_key, url, method, tenant_id) VALUES
 (205, '工段查询', 'basic:workSection:query',                '/basic/workSection/page',                 'POST', @script_tenant),
 (206, '工段列表', 'basic:workSection:list',                 '/basic/workSection/list',                 'POST', @script_tenant),
 (207, '按车间拉取工段下拉', 'basic:workSection:listByWorkshop', '/basic/workSection/listByWorkshop',     'POST', @script_tenant),
 (208, '按产线拉取工段下拉', 'basic:workSection:listByProductionLine', '/basic/workSection/listByProductionLine', 'POST', @script_tenant),
 (209, '工段详情', 'basic:workSection:detail',               '/basic/workSection/detail',               'POST', @script_tenant),
 (210, '工段新增', 'basic:workSection:add',                  '/basic/workSection/create',               'POST', @script_tenant),
 (211, '工段编辑', 'basic:workSection:edit',                 '/basic/workSection/update',               'POST', @script_tenant),
 (212, '工段删除', 'basic:workSection:delete',               '/basic/workSection/delete',               'POST', @script_tenant),
 (213, '工段批量删除', 'basic:workSection:batchDelete',      '/basic/workSection/batchDelete',          'POST', @script_tenant)
ON DUPLICATE KEY UPDATE permission_name=VALUES(permission_name);

-- 4.4 工序 8 项
INSERT INTO sys_permission (id, permission_name, permission_key, url, method, tenant_id) VALUES
 (214, '工序查询', 'basic:process:query',                    '/basic/process/page',                     'POST', @script_tenant),
 (215, '工序列表', 'basic:process:list',                     '/basic/process/list',                     'POST', @script_tenant),
 (216, '按工段拉取工序下拉', 'basic:process:listByWorkSection', '/basic/process/listByWorkSection',     'POST', @script_tenant),
 (217, '工序详情', 'basic:process:detail',                   '/basic/process/detail',                   'POST', @script_tenant),
 (218, '工序新增', 'basic:process:add',                      '/basic/process/create',                   'POST', @script_tenant),
 (219, '工序编辑', 'basic:process:edit',                     '/basic/process/update',                   'POST', @script_tenant),
 (220, '工序删除', 'basic:process:delete',                   '/basic/process/delete',                   'POST', @script_tenant),
 (221, '工序批量删除', 'basic:process:batchDelete',          '/basic/process/batchDelete',              'POST', @script_tenant)
ON DUPLICATE KEY UPDATE permission_name=VALUES(permission_name);

-- ============================================================
-- 五、admin 角色赋权 (id=1, tenant_id=1)
-- ============================================================

-- 5.1 关联菜单（基础数据 + 工厂建模 + 4 子菜单 + 20 按钮）
INSERT INTO sys_role_menu (tenant_id, role_id, menu_id)
SELECT @script_tenant, 1, m.id
FROM sys_menu m
WHERE m.id IN (504, 505, 456, 463, 464, 465, 481, 506, 509, 510, 511, 512, 513,
               507, 516, 517, 518, 519, 520, 508, 523, 524, 525, 526, 527, 530)
ON DUPLICATE KEY UPDATE tenant_id=VALUES(tenant_id);

-- 5.2 关联接口权限（车间 5 + 产线 8 + 工段 9 + 工序 8 = 30 项）
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT 1, p.id
FROM sys_permission p
WHERE p.permission_key IN (
    'basic:workshop:query','basic:workshop:add','basic:workshop:edit','basic:workshop:delete','basic:workshop:listByFactory',
    'basic:productionLine:query','basic:productionLine:list','basic:productionLine:listByWorkshop','basic:productionLine:detail',
    'basic:productionLine:add','basic:productionLine:edit','basic:productionLine:delete','basic:productionLine:batchDelete',
    'basic:workSection:query','basic:workSection:list','basic:workSection:listByWorkshop','basic:workSection:listByProductionLine',
    'basic:workSection:detail','basic:workSection:add','basic:workSection:edit','basic:workSection:delete','basic:workSection:batchDelete',
    'basic:process:query','basic:process:list','basic:process:listByWorkSection','basic:process:detail',
    'basic:process:add','basic:process:edit','basic:process:delete','basic:process:batchDelete'
)
ON DUPLICATE KEY UPDATE role_id=role_id;

-- ============================================================
-- 六、清理 sys_role_menu 历史重复（修复脚本）
-- 说明：sys_role_menu 表没有 (role_id, menu_id) 唯一约束，
--       早期数据可能存在重复，本次清理 admin(role_id=1) 的重复行。
-- ============================================================
DELETE r1 FROM sys_role_menu r1
INNER JOIN sys_role_menu r2
  ON r1.role_id = r2.role_id
  AND r1.menu_id = r2.menu_id
  AND r1.id > r2.id
WHERE r1.role_id = 1;

SELECT 'FACTORY_MODELING_INIT_DONE' AS status;
