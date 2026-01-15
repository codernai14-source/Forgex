-- ================================================
-- Forgex Workflow 数据库表结构
-- 数据库: forgex_workflow
-- 说明: 工作流相关表结构
-- ================================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS forgex_workflow DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

USE forgex_workflow;

-- ================================================
-- 流程定义表
-- ================================================
CREATE TABLE IF NOT EXISTS wf_process_definition (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    process_key VARCHAR(100) NOT NULL COMMENT '流程定义键',
    process_name VARCHAR(200) NOT NULL COMMENT '流程名称',
    version INT NOT NULL DEFAULT 1 COMMENT '版本号',
    category VARCHAR(100) COMMENT '流程分类',
    description VARCHAR(500) COMMENT '流程描述',
    bpmn_xml TEXT COMMENT 'BPMN XML内容',
    tenant_id BIGINT COMMENT '租户ID',
    status TINYINT DEFAULT 1 COMMENT '状态：1启用 0禁用',
    create_by VARCHAR(64) COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(64) COMMENT '更新人',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    UNIQUE KEY uk_process_key_version (process_key, version),
    INDEX idx_process_key (process_key),
    INDEX idx_tenant (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='流程定义表';

-- ================================================
-- 流程实例表
-- ================================================
CREATE TABLE IF NOT EXISTS wf_process_instance (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    process_instance_id VARCHAR(64) NOT NULL COMMENT '流程实例ID',
    process_key VARCHAR(100) NOT NULL COMMENT '流程定义键',
    process_name VARCHAR(200) COMMENT '流程名称',
    version INT COMMENT '流程版本',
    business_key VARCHAR(100) COMMENT '业务键',
    initiator_id BIGINT COMMENT '发起人ID',
    initiator_name VARCHAR(50) COMMENT '发起人姓名',
    tenant_id BIGINT COMMENT '租户ID',
    status VARCHAR(20) DEFAULT 'RUNNING' COMMENT '状态：RUNNING/SUSPENDED/COMPLETED/CANCELLED',
    start_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '开始时间',
    end_time DATETIME COMMENT '结束时间',
    duration BIGINT COMMENT '持续时间(毫秒)',
    delete_reason VARCHAR(500) COMMENT '删除原因',
    UNIQUE KEY uk_instance_id (process_instance_id),
    INDEX idx_process_key (process_key),
    INDEX idx_business_key (business_key),
    INDEX idx_initiator (initiator_id),
    INDEX idx_tenant (tenant_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='流程实例表';

-- ================================================
-- 流程任务表
-- ================================================
CREATE TABLE IF NOT EXISTS wf_task (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    task_id VARCHAR(64) NOT NULL COMMENT '任务ID',
    process_instance_id VARCHAR(64) NOT NULL COMMENT '流程实例ID',
    process_key VARCHAR(100) COMMENT '流程定义键',
    task_name VARCHAR(200) NOT NULL COMMENT '任务名称',
    task_key VARCHAR(100) COMMENT '任务键',
    task_type VARCHAR(50) COMMENT '任务类型：USER_TASK/SERVICE_TASK等',
    assignee_id BIGINT COMMENT '办理人ID',
    assignee_name VARCHAR(50) COMMENT '办理人姓名',
    candidate_users TEXT COMMENT '候选用户ID列表',
    candidate_groups TEXT COMMENT '候选组ID列表',
    tenant_id BIGINT COMMENT '租户ID',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态：PENDING/COMPLETED/CANCELLED',
    priority INT DEFAULT 50 COMMENT '优先级',
    due_date DATETIME COMMENT '到期时间',
    start_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '开始时间',
    end_time DATETIME COMMENT '结束时间',
    duration BIGINT COMMENT '持续时间(毫秒)',
    delete_reason VARCHAR(500) COMMENT '删除原因',
    UNIQUE KEY uk_task_id (task_id),
    INDEX idx_process_instance (process_instance_id),
    INDEX idx_assignee (assignee_id),
    INDEX idx_tenant (tenant_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='流程任务表';

-- ================================================
-- 流程变量表
-- ================================================
CREATE TABLE IF NOT EXISTS wf_variable (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    process_instance_id VARCHAR(64) NOT NULL COMMENT '流程实例ID',
    task_id VARCHAR(64) COMMENT '任务ID',
    variable_name VARCHAR(100) NOT NULL COMMENT '变量名',
    variable_type VARCHAR(50) COMMENT '变量类型',
    variable_value TEXT COMMENT '变量值',
    tenant_id BIGINT COMMENT '租户ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_process_instance (process_instance_id),
    INDEX idx_task (task_id),
    INDEX idx_variable_name (variable_name),
    INDEX idx_tenant (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='流程变量表';

-- ================================================
-- 流程审批记录表
-- ================================================
CREATE TABLE IF NOT EXISTS wf_approval_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    process_instance_id VARCHAR(64) NOT NULL COMMENT '流程实例ID',
    task_id VARCHAR(64) COMMENT '任务ID',
    task_name VARCHAR(200) COMMENT '任务名称',
    approver_id BIGINT NOT NULL COMMENT '审批人ID',
    approver_name VARCHAR(50) COMMENT '审批人姓名',
    approval_action VARCHAR(20) NOT NULL COMMENT '审批动作：APPROVE/REJECT/TRANSFER/DELEGATE',
    approval_comment VARCHAR(500) COMMENT '审批意见',
    approval_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '审批时间',
    tenant_id BIGINT COMMENT '租户ID',
    INDEX idx_process_instance (process_instance_id),
    INDEX idx_approver (approver_id),
    INDEX idx_tenant (tenant_id),
    INDEX idx_approval_time (approval_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='流程审批记录表';

-- ================================================
-- 流程委托表
-- ================================================
CREATE TABLE IF NOT EXISTS wf_delegation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    delegator_id BIGINT NOT NULL COMMENT '委托人ID',
    delegate_id BIGINT NOT NULL COMMENT '被委托人ID',
    process_key VARCHAR(100) COMMENT '流程定义键（为空表示全部流程）',
    start_time DATETIME NOT NULL COMMENT '开始时间',
    end_time DATETIME COMMENT '结束时间',
    status TINYINT DEFAULT 1 COMMENT '状态：1启用 0禁用',
    tenant_id BIGINT COMMENT '租户ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_delegator (delegator_id),
    INDEX idx_delegate (delegate_id),
    INDEX idx_tenant (tenant_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='流程委托表';

-- 完成提示
SELECT 'forgex_workflow数据库表结构创建完成！' AS message;
