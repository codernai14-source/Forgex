-- ================================================
-- Forgex Common 数据库表结构
-- 数据库: forgex_common
-- 说明: 公共配置和通用功能表结构
-- ================================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS forgex_common DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

USE forgex_common;

-- ================================================
-- 用户页面样式配置表
-- ================================================
CREATE TABLE IF NOT EXISTS sys_user_style_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    config_key VARCHAR(100) NOT NULL COMMENT '配置键，例如：layout.style',
    config_json TEXT COMMENT '配置内容JSON',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除',
    UNIQUE KEY uk_user_tenant_key (user_id, tenant_id, config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户页面样式配置表';

-- ================================================
-- Excel导出配置主表
-- ================================================
CREATE TABLE IF NOT EXISTS fx_excel_export_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    table_name VARCHAR(255) NOT NULL COMMENT '表名',
    table_code VARCHAR(100) NOT NULL COMMENT '表编号',
    header_style_json TEXT NULL COMMENT '表头样式(JSON)',
    title VARCHAR(255) NULL COMMENT '标题列名称',
    subtitle VARCHAR(255) NULL COMMENT '标题列小字说明',
    export_format VARCHAR(20) NOT NULL DEFAULT 'xlsx' COMMENT '导出格式(xlsx/csv)',
    enable_total TINYINT NOT NULL DEFAULT 0 COMMENT '是否开启总计',
    version INT NOT NULL DEFAULT 1 COMMENT '版本',
    create_by VARCHAR(64) NULL COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(64) NULL COMMENT '修改人',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除',
    UNIQUE KEY uk_export_tenant_code (tenant_id, table_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Excel导出配置主表';

-- ================================================
-- Excel导出配置子表
-- ================================================
CREATE TABLE IF NOT EXISTS fx_excel_export_config_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    config_id BIGINT NOT NULL COMMENT '主表ID',
    export_field VARCHAR(128) NOT NULL COMMENT '导出字段',
    field_name VARCHAR(255) NULL COMMENT '字段名(默认列头)',
    i18n_json TEXT NULL COMMENT '字段多语言配置(JSON)',
    header_style_json TEXT NULL COMMENT '列头样式(JSON)',
    cell_style_json TEXT NULL COMMENT '列内容样式(JSON)',
    order_num INT NOT NULL DEFAULT 0 COMMENT '顺序',
    create_by VARCHAR(64) NULL COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(64) NULL COMMENT '修改人',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除',
    INDEX idx_export_cfg (tenant_id, config_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Excel导出配置子表';

-- ================================================
-- Excel导入配置主表
-- ================================================
CREATE TABLE IF NOT EXISTS fx_excel_import_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    table_name VARCHAR(255) NOT NULL COMMENT '表名',
    table_code VARCHAR(100) NOT NULL COMMENT '表编号',
    title VARCHAR(255) NULL COMMENT '标题列名称',
    subtitle VARCHAR(255) NULL COMMENT '标题列小字说明',
    version INT NOT NULL DEFAULT 1 COMMENT '版本',
    create_by VARCHAR(64) NULL COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(64) NULL COMMENT '修改人',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除',
    UNIQUE KEY uk_import_tenant_code (tenant_id, table_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Excel导入配置主表';

-- ================================================
-- Excel导入配置子表
-- ================================================
CREATE TABLE IF NOT EXISTS fx_excel_import_config_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    config_id BIGINT NOT NULL COMMENT '主表ID',
    i18n_json TEXT NULL COMMENT '字段多语言配置(JSON)',
    import_field VARCHAR(128) NOT NULL COMMENT '导入字段',
    field_type VARCHAR(50) NOT NULL DEFAULT 'string' COMMENT '字段类型(time/date/datetime/dict/...)',
    dict_code VARCHAR(100) NULL COMMENT '字典编号',
    required TINYINT NOT NULL DEFAULT 0 COMMENT '是否必填',
    order_num INT NOT NULL DEFAULT 0 COMMENT '顺序',
    create_by VARCHAR(64) NULL COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(64) NULL COMMENT '修改人',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除',
    INDEX idx_import_cfg (tenant_id, config_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Excel导入配置子表';

-- ================================================
-- 通用表格配置主表
-- ================================================
CREATE TABLE IF NOT EXISTS fx_table_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    table_code VARCHAR(128) NOT NULL COMMENT '表格代码',
    table_name_i18n_json TEXT NULL COMMENT '表格名称国际化(JSON)',
    table_type VARCHAR(20) NOT NULL DEFAULT 'NORMAL' COMMENT '表格类型(NORMAL/LAZY/TREE)',
    row_key VARCHAR(64) NULL COMMENT '行Key字段',
    default_page_size INT NOT NULL DEFAULT 20 COMMENT '默认分页大小',
    default_sort_json TEXT NULL COMMENT '默认排序(JSON)',
    enabled TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用：1启用 0禁用',
    version INT NOT NULL DEFAULT 1 COMMENT '版本',
    create_by VARCHAR(64) NULL COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(64) NULL COMMENT '修改人',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除',
    UNIQUE KEY uk_table_cfg_tenant_code (tenant_id, table_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通用表格配置主表';

-- ================================================
-- 通用表格列配置子表
-- ================================================
CREATE TABLE IF NOT EXISTS fx_table_column_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    table_code VARCHAR(128) NOT NULL COMMENT '表格代码',
    field VARCHAR(128) NOT NULL COMMENT '字段名',
    title_i18n_json TEXT NULL COMMENT '列头国际化(JSON)',
    align VARCHAR(16) NULL COMMENT '对齐方式(left/center/right)',
    width INT NULL COMMENT '列宽',
    fixed VARCHAR(16) NULL COMMENT '固定列(left/right)',
    ellipsis TINYINT NOT NULL DEFAULT 0 COMMENT '是否省略',
    sortable TINYINT NOT NULL DEFAULT 0 COMMENT '是否可排序',
    sorter_field VARCHAR(128) NULL COMMENT '排序字段映射',
    queryable TINYINT NOT NULL DEFAULT 0 COMMENT '是否支持查询',
    query_type VARCHAR(32) NULL COMMENT '查询类型(input/select/dateRange/numberRange/...)',
    query_operator VARCHAR(32) NULL COMMENT '查询操作符(eq/like/in/between/...)',
    dict_code VARCHAR(100) NULL COMMENT '字典编码',
    render_type VARCHAR(32) NULL COMMENT '渲染类型(text/tag/date/dictTag/...)',
    perm_key VARCHAR(100) NULL COMMENT '权限Key',
    order_num INT NOT NULL DEFAULT 0 COMMENT '顺序',
    enabled TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用：1启用 0禁用',
    create_by VARCHAR(64) NULL COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(64) NULL COMMENT '修改人',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除',
    INDEX idx_table_col_tenant_code (tenant_id, table_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通用表格列配置子表';

-- ================================================
-- 用户表格个性化配置表（预留）
-- ================================================
CREATE TABLE IF NOT EXISTS fx_user_table_pref (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    table_code VARCHAR(128) NOT NULL COMMENT '表格代码',
    hidden_fields_json TEXT NULL COMMENT '隐藏字段(JSON)',
    field_order_json TEXT NULL COMMENT '字段顺序(JSON)',
    page_size INT NULL COMMENT '分页大小',
    sort_json TEXT NULL COMMENT '排序(JSON)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除',
    UNIQUE KEY uk_user_table_pref (tenant_id, user_id, table_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表格个性化配置表';

-- ================================================
-- 返回消息国际化配置表（单表承载多模块）
-- ================================================
CREATE TABLE IF NOT EXISTS fx_i18n_message (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    module VARCHAR(64) NOT NULL COMMENT '模块标识',
    prompt_code VARCHAR(128) NOT NULL COMMENT '消息模板编号',
    text_i18n_json TEXT NOT NULL COMMENT '文本国际化(JSON)',
    enabled TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用：1启用 0禁用',
    version INT NOT NULL DEFAULT 1 COMMENT '版本',
    create_by VARCHAR(64) NULL COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(64) NULL COMMENT '修改人',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除',
    UNIQUE KEY uk_i18n_msg_tenant (tenant_id, module, prompt_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='返回消息国际化配置表';

-- 完成提示
SELECT 'forgex_common数据库表结构创建完成！' AS message;
