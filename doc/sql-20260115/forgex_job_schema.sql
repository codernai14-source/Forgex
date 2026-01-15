-- ================================================
-- Forgex Job 数据库表结构
-- 数据库: forgex_job
-- 说明: 定时任务相关表结构
-- ================================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS forgex_job DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

USE forgex_job;

-- ================================================
-- 定时任务信息表
-- ================================================
CREATE TABLE IF NOT EXISTS sys_job (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '任务ID',
    job_name VARCHAR(100) NOT NULL COMMENT '任务名称',
    job_group VARCHAR(100) NOT NULL DEFAULT 'DEFAULT' COMMENT '任务组名',
    invoke_target VARCHAR(255) NOT NULL COMMENT '调用目标字符串',
    cron_expression VARCHAR(255) NOT NULL COMMENT 'Cron执行表达式',
    misfire_policy VARCHAR(20) DEFAULT '3' COMMENT '计划执行错误策略（1立即执行 2执行一次 3放弃执行）',
    concurrent VARCHAR(20) DEFAULT '0' COMMENT '是否并发执行（0允许 1禁止）',
    status VARCHAR(20) DEFAULT '0' COMMENT '状态（0正常 1暂停）',
    tenant_id BIGINT COMMENT '租户ID',
    create_by VARCHAR(64) COMMENT '创建者',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(64) COMMENT '更新者',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    remark VARCHAR(500) COMMENT '备注信息',
    INDEX idx_job_group (job_group),
    INDEX idx_status (status),
    INDEX idx_tenant (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='定时任务调度表';

-- ================================================
-- 定时任务执行日志表
-- ================================================
CREATE TABLE IF NOT EXISTS sys_job_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '任务日志ID',
    job_name VARCHAR(100) NOT NULL COMMENT '任务名称',
    job_group VARCHAR(100) NOT NULL COMMENT '任务组名',
    invoke_target VARCHAR(255) NOT NULL COMMENT '调用目标字符串',
    job_message VARCHAR(500) COMMENT '日志信息',
    status VARCHAR(20) DEFAULT '0' COMMENT '执行状态（0正常 1失败）',
    exception_info TEXT COMMENT '异常信息',
    start_time DATETIME COMMENT '开始时间',
    stop_time DATETIME COMMENT '结束时间',
    cost_time BIGINT COMMENT '执行时长(毫秒)',
    tenant_id BIGINT COMMENT '租户ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_job_name (job_name),
    INDEX idx_job_group (job_group),
    INDEX idx_status (status),
    INDEX idx_start_time (start_time),
    INDEX idx_tenant (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='定时任务执行日志表';

-- ================================================
-- 任务调度器配置表
-- ================================================
CREATE TABLE IF NOT EXISTS sys_job_scheduler (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    scheduler_name VARCHAR(100) NOT NULL COMMENT '调度器名称',
    scheduler_type VARCHAR(50) NOT NULL COMMENT '调度器类型：QUARTZ/XXL-JOB',
    config_json TEXT COMMENT '配置信息JSON',
    status TINYINT DEFAULT 1 COMMENT '状态：1启用 0禁用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    remark VARCHAR(500) COMMENT '备注'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务调度器配置表';

-- 完成提示
SELECT 'forgex_job数据库表结构创建完成！' AS message;
