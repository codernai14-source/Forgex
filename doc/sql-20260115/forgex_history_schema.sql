-- ================================================
-- Forgex History 数据库表结构
-- 数据库: forgex_history
-- 说明: 历史数据表结构
-- ================================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS forgex_history DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

USE forgex_history;

-- ================================================
-- 数据变更历史表
-- ================================================
CREATE TABLE IF NOT EXISTS data_change_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    table_name VARCHAR(100) NOT NULL COMMENT '表名',
    record_id BIGINT NOT NULL COMMENT '记录ID',
    operation_type VARCHAR(20) NOT NULL COMMENT '操作类型：INSERT/UPDATE/DELETE',
    operation_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    operator_id BIGINT COMMENT '操作人ID',
    operator_name VARCHAR(50) COMMENT '操作人姓名',
    tenant_id BIGINT COMMENT '租户ID',
    old_value JSON COMMENT '变更前值(JSON)',
    new_value JSON COMMENT '变更后值(JSON)',
    change_fields JSON COMMENT '变更字段列表(JSON)',
    remark VARCHAR(500) COMMENT '备注',
    INDEX idx_table_record (table_name, record_id),
    INDEX idx_operator (operator_id),
    INDEX idx_tenant (tenant_id),
    INDEX idx_operation_time (operation_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据变更历史表';

-- ================================================
-- 操作日志表
-- ================================================
CREATE TABLE IF NOT EXISTS operation_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    module_name VARCHAR(50) COMMENT '模块名称',
    business_type VARCHAR(50) COMMENT '业务类型',
    method VARCHAR(200) COMMENT '方法名称',
    request_method VARCHAR(10) COMMENT '请求方式',
    operator_name VARCHAR(50) COMMENT '操作人员',
    dept_name VARCHAR(50) COMMENT '部门名称',
    operator_url VARCHAR(255) COMMENT '请求URL',
    request_ip VARCHAR(128) COMMENT '主机地址',
    operation_location VARCHAR(255) COMMENT '操作地点',
    operation_param TEXT COMMENT '请求参数',
    json_result TEXT COMMENT '返回参数',
    operation_status TINYINT DEFAULT 0 COMMENT '操作状态（0正常 1异常）',
    error_msg VARCHAR(2000) COMMENT '错误消息',
    operation_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    cost_time BIGINT COMMENT '消耗时间(毫秒)',
    tenant_id BIGINT COMMENT '租户ID',
    INDEX idx_operation_time (operation_time),
    INDEX idx_tenant (tenant_id),
    INDEX idx_operator (operator_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- ================================================
-- 系统访问日志表
-- ================================================
CREATE TABLE IF NOT EXISTS access_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    trace_id VARCHAR(64) COMMENT '追踪ID',
    user_id BIGINT COMMENT '用户ID',
    username VARCHAR(50) COMMENT '用户名',
    tenant_id BIGINT COMMENT '租户ID',
    request_method VARCHAR(10) COMMENT '请求方法',
    request_url VARCHAR(500) COMMENT '请求URL',
    request_params TEXT COMMENT '请求参数',
    response_status INT COMMENT '响应状态码',
    response_time BIGINT COMMENT '响应时间(毫秒)',
    client_ip VARCHAR(64) COMMENT '客户端IP',
    user_agent VARCHAR(512) COMMENT '用户代理',
    browser VARCHAR(100) COMMENT '浏览器',
    os VARCHAR(100) COMMENT '操作系统',
    device VARCHAR(100) COMMENT '设备',
    access_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '访问时间',
    INDEX idx_access_time (access_time),
    INDEX idx_user (user_id),
    INDEX idx_tenant (tenant_id),
    INDEX idx_trace_id (trace_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统访问日志表';

-- ================================================
-- 数据备份记录表
-- ================================================
CREATE TABLE IF NOT EXISTS data_backup_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    backup_name VARCHAR(100) NOT NULL COMMENT '备份名称',
    backup_type VARCHAR(20) NOT NULL COMMENT '备份类型：FULL/INCREMENTAL',
    database_name VARCHAR(50) COMMENT '数据库名称',
    table_names TEXT COMMENT '表名列表',
    backup_file_path VARCHAR(500) COMMENT '备份文件路径',
    backup_size BIGINT COMMENT '备份文件大小(字节)',
    backup_status VARCHAR(20) COMMENT '备份状态：SUCCESS/FAILED/IN_PROGRESS',
    backup_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '备份时间',
    backup_operator VARCHAR(50) COMMENT '备份操作人',
    remark VARCHAR(500) COMMENT '备注',
    tenant_id BIGINT COMMENT '租户ID',
    INDEX idx_backup_time (backup_time),
    INDEX idx_tenant (tenant_id),
    INDEX idx_status (backup_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据备份记录表';

-- 完成提示
SELECT 'forgex_history数据库表结构创建完成！' AS message;
