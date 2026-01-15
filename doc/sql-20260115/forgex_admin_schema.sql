-- ================================================
-- Forgex Admin 数据库表结构
-- 数据库: forgex_admin
-- 说明: 系统管理相关表结构
-- ================================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS forgex_admin DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

USE forgex_admin;

-- ================================================
-- 租户表
-- ================================================
CREATE TABLE IF NOT EXISTS sys_tenant (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '租户ID',
    tenant_name VARCHAR(100) NOT NULL COMMENT '租户名称',
    tenant_code VARCHAR(100) UNIQUE COMMENT '租户编码',
    description VARCHAR(255) COMMENT '描述',
    logo TEXT COMMENT 'Logo(Base64/URL)',
    status TINYINT DEFAULT 1 COMMENT '状态 (1:启用, 0:禁用)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除 (0:未删除, 1:已删除)',
    create_by VARCHAR(50) COMMENT '创建人',
    update_by VARCHAR(50) COMMENT '更新人'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='租户表';

-- ================================================
-- 用户表
-- ================================================
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    account VARCHAR(50) NOT NULL UNIQUE COMMENT '账号',
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码 (加密)',
    email VARCHAR(100) COMMENT '邮箱',
    phone VARCHAR(20) COMMENT '手机号',
    status TINYINT DEFAULT 1 COMMENT '状态 (1:启用, 0:禁用)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    create_by VARCHAR(50) COMMENT '创建人',
    update_by VARCHAR(50) COMMENT '更新人',
    INDEX idx_username (username),
    INDEX idx_account (account)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ================================================
-- 用户附属信息表
-- ================================================
CREATE TABLE IF NOT EXISTS sys_user_profile (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    political_status VARCHAR(50) NULL COMMENT '政治面貌',
    education VARCHAR(50) NULL COMMENT '学历',
    birth_place VARCHAR(255) NULL COMMENT '籍贯',
    intro VARCHAR(512) NULL COMMENT '个人简介',
    home_address VARCHAR(255) NULL COMMENT '家庭住址',
    emergency_contact VARCHAR(50) NULL COMMENT '紧急联系人',
    emergency_phone VARCHAR(20) NULL COMMENT '紧急联系人电话',
    referrer VARCHAR(50) NULL COMMENT '引荐人',
    work_history JSON NULL COMMENT '工作经历(JSON)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    create_by VARCHAR(50) COMMENT '创建人',
    update_by VARCHAR(50) COMMENT '更新人',
    UNIQUE KEY uk_profile_user_tenant (tenant_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户附属信息表';

-- ================================================
-- 用户租户关联表
-- ================================================
CREATE TABLE IF NOT EXISTS sys_user_tenant (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    pref_order INT DEFAULT 0 COMMENT '喜好排序（越大越靠前）',
    is_default TINYINT DEFAULT 0 COMMENT '是否默认租户：1默认 0非默认',
    last_used DATETIME NULL COMMENT '最后使用时间',
    UNIQUE KEY uk_user_tenant (user_id, tenant_id),
    FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE,
    FOREIGN KEY (tenant_id) REFERENCES sys_tenant(id) ON DELETE CASCADE,
    INDEX idx_user_pref (user_id, pref_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户租户关联表';

-- ================================================
-- 角色表
-- ================================================
CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '角色ID',
    role_name VARCHAR(50) NOT NULL COMMENT '角色名称',
    role_key VARCHAR(50) NOT NULL UNIQUE COMMENT '角色标识',
    description VARCHAR(500) COMMENT '描述',
    status TINYINT DEFAULT 1 COMMENT '状态',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    create_by VARCHAR(50) COMMENT '创建人',
    update_by VARCHAR(50) COMMENT '更新人',
    INDEX idx_role_key (role_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- ================================================
-- 权限表
-- ================================================
CREATE TABLE IF NOT EXISTS sys_permission (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '权限ID',
    permission_name VARCHAR(100) NOT NULL COMMENT '权限名称',
    permission_key VARCHAR(100) NOT NULL UNIQUE COMMENT '权限标识',
    url VARCHAR(255) COMMENT 'URL',
    method VARCHAR(10) COMMENT '方法 (GET/POST等)',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    create_by VARCHAR(50) COMMENT '创建人',
    update_by VARCHAR(50) COMMENT '更新人'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

-- ================================================
-- 用户角色关联表
-- ================================================
CREATE TABLE IF NOT EXISTS sys_user_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    INDEX idx_user_role_tenant (tenant_id, user_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联';

-- ================================================
-- 角色权限关联表
-- ================================================
CREATE TABLE IF NOT EXISTS sys_role_permission (
    role_id BIGINT NOT NULL COMMENT '角色ID',
    permission_id BIGINT NOT NULL COMMENT '权限ID',
    PRIMARY KEY (role_id, permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联';

-- ================================================
-- 系统配置表
-- ================================================
CREATE TABLE IF NOT EXISTS sys_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '配置ID',
    config_key VARCHAR(100) NOT NULL UNIQUE COMMENT '配置键',
    config_value TEXT COMMENT '配置值 (JSON)',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    create_by VARCHAR(50) COMMENT '创建人',
    update_by VARCHAR(50) COMMENT '更新人'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';

-- ================================================
-- 定时任务表
-- ================================================
CREATE TABLE IF NOT EXISTS sys_job (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '任务ID',
    job_name VARCHAR(100) NOT NULL COMMENT '任务名称',
    cron_expression VARCHAR(255) NOT NULL COMMENT 'Cron表达式',
    status TINYINT DEFAULT 1 COMMENT '状态',
    invoke_target VARCHAR(255) NOT NULL COMMENT '调用目标 (方法路径)',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    create_by VARCHAR(50) COMMENT '创建人',
    update_by VARCHAR(50) COMMENT '更新人'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='定时任务表';

-- ================================================
-- 租户隔离跳过配置表
-- ================================================
CREATE TABLE IF NOT EXISTS sys_tenant_ignore (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    scope VARCHAR(20) NOT NULL COMMENT '作用域：TABLE/SERVICE/MAPPER',
    matcher VARCHAR(255) NOT NULL COMMENT '匹配内容：表名/全限定类名/全限定类名#方法名',
    enabled TINYINT DEFAULT 1 COMMENT '是否启用：1启用 0禁用',
    remark VARCHAR(255) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    create_by VARCHAR(50) COMMENT '创建人',
    update_by VARCHAR(50) COMMENT '更新人'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='租户隔离跳过配置表';

-- ================================================
-- 数据字典表（树结构，支持多语言）
-- ================================================
CREATE TABLE IF NOT EXISTS sys_dict (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    parent_id BIGINT NOT NULL DEFAULT 0 COMMENT '父节点ID（0表示根节点）',
    dict_name VARCHAR(100) NOT NULL COMMENT '字典名称(兼容字段)',
    dict_code VARCHAR(50) NOT NULL COMMENT '字典编码',
    dict_value VARCHAR(50) NULL COMMENT '字典键',
    dict_value_i18n_json TEXT NULL COMMENT '字典显示值国际化(JSON)',
    node_path VARCHAR(512) NOT NULL COMMENT '节点路径(dict_code路径，用/分割)',
    level INT NOT NULL DEFAULT 1 COMMENT '层级',
    children_count INT NOT NULL DEFAULT 0 COMMENT '直接子节点数量',
    order_num INT NOT NULL DEFAULT 0 COMMENT '排序号',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1启用，0禁用',
    remark VARCHAR(500) NULL COMMENT '备注',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    create_by BIGINT NULL COMMENT '创建人',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by BIGINT NULL COMMENT '修改人',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记：0未删除，1已删除',
    INDEX idx_dict_parent (parent_id),
    INDEX idx_dict_code (dict_code),
    INDEX idx_dict_tenant (tenant_id),
    UNIQUE KEY uk_dict_sibling_code (tenant_id, parent_id, dict_code, deleted),
    UNIQUE KEY uk_dict_path (tenant_id, node_path, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据字典表';

-- ================================================
-- 模块表
-- ================================================
CREATE TABLE IF NOT EXISTS sys_module (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    code VARCHAR(50) NOT NULL COMMENT '模块编码',
    name VARCHAR(100) NOT NULL COMMENT '模块名称',
    name_i18n_json JSON NULL COMMENT '模块名称国际化(JSON)',
    icon VARCHAR(50) COMMENT '图标',
    order_num INT DEFAULT 0 COMMENT '排序',
    visible TINYINT DEFAULT 1 COMMENT '是否可见',
    status TINYINT DEFAULT 1 COMMENT '状态',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    create_by VARCHAR(50) COMMENT '创建人',
    update_by VARCHAR(50) COMMENT '更新人',
    UNIQUE KEY uk_module_code_tenant (tenant_id, code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模块表';

-- ================================================
-- 菜单表（含目录/菜单/按钮）
-- ================================================
CREATE TABLE IF NOT EXISTS sys_menu (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    module_id BIGINT NOT NULL COMMENT '模块ID',
    parent_id BIGINT DEFAULT 0 COMMENT '父菜单ID',
    type VARCHAR(20) NOT NULL COMMENT '类型：dir/menu/button',
    path VARCHAR(255) COMMENT '路由路径',
    name VARCHAR(100) NOT NULL COMMENT '显示名称',
    name_i18n_json JSON NULL COMMENT '菜单名称国际化(JSON)',
    icon VARCHAR(50) COMMENT '图标',
    component_key VARCHAR(100) COMMENT '前端组件键',
    perm_key VARCHAR(100) COMMENT '按钮权限键',
    order_num INT DEFAULT 0 COMMENT '排序',
    visible TINYINT DEFAULT 1 COMMENT '是否可见',
    status TINYINT DEFAULT 1 COMMENT '状态',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    create_by VARCHAR(50) COMMENT '创建人',
    update_by VARCHAR(50) COMMENT '更新人',
    menu_level INT COMMENT '菜单级别',
    menu_mode VARCHAR(20) COMMENT '菜单模式',
    external_url VARCHAR(255) COMMENT '外部链接',
    INDEX idx_menu_module (module_id),
    INDEX idx_menu_parent (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单表';

-- ================================================
-- 角色菜单授权表
-- ================================================
CREATE TABLE IF NOT EXISTS sys_role_menu (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    menu_id BIGINT NOT NULL COMMENT '菜单ID',
    INDEX idx_role_menu (tenant_id, role_id, menu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色菜单授权';

-- ================================================
-- 登录日志表
-- ================================================
CREATE TABLE IF NOT EXISTS sys_login_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    user_id BIGINT NULL COMMENT '用户ID',
    account VARCHAR(50) NULL COMMENT '登录账号',
    tenant_id BIGINT NULL COMMENT '租户ID',
    login_ip VARCHAR(64) NULL COMMENT '登录IP',
    login_region VARCHAR(255) NULL COMMENT 'IP归属地',
    user_agent VARCHAR(512) NULL COMMENT '浏览器UA',
    login_time DATETIME NULL COMMENT '登录时间',
    logout_time DATETIME NULL COMMENT '登出时间',
    token_value VARCHAR(512) NULL COMMENT 'tokenValue',
    logout_reason VARCHAR(50) NULL COMMENT '登出原因',
    status TINYINT DEFAULT 1 COMMENT '状态：1成功 0失败',
    reason VARCHAR(255) NULL COMMENT '失败原因',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_login_tenant_time (tenant_id, login_time),
    INDEX idx_login_user_tenant (user_id, tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='登录日志';

-- ================================================
-- 部门表
-- ================================================
CREATE TABLE IF NOT EXISTS sys_department (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    name VARCHAR(100) NOT NULL COMMENT '部门名称',
    parent_id BIGINT DEFAULT 0 COMMENT '父部门ID',
    code VARCHAR(50) COMMENT '部门编码',
    leader VARCHAR(50) COMMENT '负责人',
    phone VARCHAR(20) COMMENT '联系电话',
    email VARCHAR(100) COMMENT '邮箱',
    order_num INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    create_by VARCHAR(50) COMMENT '创建人',
    update_by VARCHAR(50) COMMENT '更新人',
    INDEX idx_dept_parent (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部门表';

-- ================================================
-- 职位表
-- ================================================
CREATE TABLE IF NOT EXISTS sys_position (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    name VARCHAR(100) NOT NULL COMMENT '职位名称',
    code VARCHAR(50) COMMENT '职位编码',
    description VARCHAR(255) COMMENT '描述',
    order_num INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    create_by VARCHAR(50) COMMENT '创建人',
    update_by VARCHAR(50) COMMENT '更新人'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='职位表';

-- 完成提示
SELECT 'forgex_admin数据库表结构创建完成！' AS message;
