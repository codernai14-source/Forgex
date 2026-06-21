-- Forgex_Job 初始化脚本
-- 说明：Job 菜单固定挂载在 系统管理模块 > 定时任务 下，定时任务是模块下一级目录，不挂到系统管理主页。

CREATE DATABASE IF NOT EXISTS `forgex_job` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

USE `forgex_job`;

CREATE TABLE IF NOT EXISTS `sys_job_task` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户ID',
  `job_code` varchar(100) NOT NULL COMMENT '任务编码',
  `job_name` varchar(200) NOT NULL COMMENT '任务名称',
  `job_group` varchar(100) DEFAULT NULL COMMENT '任务分组',
  `job_type` tinyint NOT NULL DEFAULT 1 COMMENT '任务类型：1 JavaBean，2 HTTP，3 Script，4 RocketMQ，5 DAG',
  `schedule_type` tinyint NOT NULL DEFAULT 3 COMMENT '调度类型：1 Cron，2 固定间隔，3 手动',
  `cron_expression` varchar(120) DEFAULT NULL COMMENT 'Cron 表达式',
  `interval_seconds` int DEFAULT NULL COMMENT '固定间隔秒数',
  `bean_name` varchar(200) DEFAULT NULL COMMENT 'Spring Bean 名称',
  `method_name` varchar(100) DEFAULT NULL COMMENT '方法名',
  `http_url` varchar(500) DEFAULT NULL COMMENT 'HTTP 地址',
  `http_method` varchar(20) DEFAULT NULL COMMENT 'HTTP 方法',
  `http_headers` text COMMENT 'HTTP 请求头 JSON',
  `script_type` varchar(30) DEFAULT NULL COMMENT '脚本类型',
  `script_path` varchar(500) DEFAULT NULL COMMENT '脚本路径',
  `script_args` text COMMENT '脚本参数',
  `mq_topic` varchar(200) DEFAULT NULL COMMENT 'RocketMQ Topic',
  `mq_tags` varchar(200) DEFAULT NULL COMMENT 'RocketMQ Tags',
  `workflow_id` bigint DEFAULT NULL COMMENT 'DAG 编排ID',
  `job_params` text COMMENT '任务参数 JSON',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态：0 停用，1 启用',
  `block_strategy` tinyint NOT NULL DEFAULT 1 COMMENT '阻塞策略',
  `timeout_seconds` int NOT NULL DEFAULT 60 COMMENT '超时时间秒',
  `max_retry_count` int NOT NULL DEFAULT 0 COMMENT '最大重试次数',
  `retry_interval_seconds` int DEFAULT 60 COMMENT '重试间隔秒',
  `shard_total` int NOT NULL DEFAULT 1 COMMENT '分片总数',
  `broadcast_enabled` tinyint NOT NULL DEFAULT 0 COMMENT '是否广播',
  `next_trigger_time` datetime DEFAULT NULL COMMENT '下次触发时间',
  `last_trigger_time` datetime DEFAULT NULL COMMENT '上次触发时间',
  `last_status` tinyint DEFAULT NULL COMMENT '上次执行状态',
  `trigger_count` bigint NOT NULL DEFAULT 0 COMMENT '触发次数',
  `remark` text COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sys_job_task_code` (`tenant_id`, `job_code`, `deleted`),
  KEY `idx_sys_job_task_next` (`status`, `next_trigger_time`),
  KEY `idx_sys_job_task_group` (`tenant_id`, `job_group`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Job任务定义';

CREATE TABLE IF NOT EXISTS `sys_job_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户ID',
  `job_id` bigint NOT NULL COMMENT '任务ID',
  `job_code` varchar(100) NOT NULL COMMENT '任务编码',
  `job_name` varchar(200) DEFAULT NULL COMMENT '任务名称',
  `trigger_type` tinyint NOT NULL COMMENT '触发类型',
  `fire_time` datetime NOT NULL COMMENT '计划触发时间',
  `start_time` datetime DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束时间',
  `duration_ms` bigint DEFAULT NULL COMMENT '耗时毫秒',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态：0 待执行，1 执行中，2 成功，3 失败，4 超时，5 取消',
  `instance_id` varchar(160) DEFAULT NULL COMMENT '实例ID',
  `request_params` text COMMENT '请求参数',
  `result_message` text COMMENT '执行结果',
  `error_stack` mediumtext COMMENT '异常堆栈',
  `retry_count` int NOT NULL DEFAULT 0 COMMENT '重试次数',
  `retry_of_log_id` bigint DEFAULT NULL COMMENT '来源日志ID',
  `shard_index` int DEFAULT 0 COMMENT '分片序号',
  `shard_total` int DEFAULT 1 COMMENT '分片总数',
  `request_id` varchar(120) DEFAULT NULL COMMENT '请求ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_sys_job_log_job_time` (`tenant_id`, `job_id`, `fire_time`),
  KEY `idx_sys_job_log_status` (`tenant_id`, `status`, `create_time`),
  KEY `idx_sys_job_log_request` (`tenant_id`, `request_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Job执行日志';

CREATE TABLE IF NOT EXISTS `sys_job_instance` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户ID',
  `instance_id` varchar(160) NOT NULL COMMENT '实例ID',
  `service_name` varchar(100) DEFAULT NULL COMMENT '服务名',
  `ip` varchar(64) DEFAULT NULL COMMENT 'IP地址',
  `port` int DEFAULT NULL COMMENT '端口',
  `pid` varchar(40) DEFAULT NULL COMMENT '进程ID',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态',
  `running_count` int NOT NULL DEFAULT 0 COMMENT '运行中数量',
  `last_heartbeat_time` datetime DEFAULT NULL COMMENT '最后心跳时间',
  `start_time` datetime DEFAULT NULL COMMENT '启动时间',
  `maintenance` tinyint NOT NULL DEFAULT 0 COMMENT '维护模式',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sys_job_instance` (`tenant_id`, `instance_id`, `deleted`),
  KEY `idx_sys_job_instance_heartbeat` (`tenant_id`, `last_heartbeat_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Job执行器实例';

CREATE TABLE IF NOT EXISTS `sys_job_retry` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户ID',
  `job_id` bigint NOT NULL COMMENT '任务ID',
  `log_id` bigint NOT NULL COMMENT '日志ID',
  `job_code` varchar(100) NOT NULL COMMENT '任务编码',
  `biz_type` varchar(80) DEFAULT NULL COMMENT '业务类型',
  `biz_id` varchar(120) DEFAULT NULL COMMENT '业务ID',
  `retry_count` int NOT NULL DEFAULT 0 COMMENT '已重试次数',
  `max_retry_count` int NOT NULL DEFAULT 0 COMMENT '最大重试次数',
  `next_retry_time` datetime DEFAULT NULL COMMENT '下次重试时间',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：1 待重试，2 死信，3 已处理',
  `last_error` text COMMENT '最后错误',
  `handle_remark` text COMMENT '处理备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_sys_job_retry_next` (`tenant_id`, `status`, `next_retry_time`),
  KEY `idx_sys_job_retry_log` (`tenant_id`, `log_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Job重试与死信';

CREATE TABLE IF NOT EXISTS `sys_job_alarm_rule` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户ID',
  `rule_name` varchar(160) NOT NULL COMMENT '规则名称',
  `job_id` bigint DEFAULT NULL COMMENT '任务ID',
  `job_code` varchar(100) DEFAULT NULL COMMENT '任务编码',
  `alarm_type` tinyint NOT NULL DEFAULT 1 COMMENT '告警类型',
  `threshold_count` int NOT NULL DEFAULT 1 COMMENT '阈值次数',
  `window_minutes` int NOT NULL DEFAULT 5 COMMENT '统计窗口分钟',
  `notify_type` varchar(60) DEFAULT NULL COMMENT '通知方式',
  `notify_target` text COMMENT '通知目标',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态',
  `remark` text COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_sys_job_alarm_rule_job` (`tenant_id`, `job_code`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Job告警规则';

CREATE TABLE IF NOT EXISTS `sys_job_alarm_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户ID',
  `rule_id` bigint DEFAULT NULL COMMENT '规则ID',
  `job_id` bigint DEFAULT NULL COMMENT '任务ID',
  `job_code` varchar(100) DEFAULT NULL COMMENT '任务编码',
  `log_id` bigint DEFAULT NULL COMMENT '日志ID',
  `alarm_type` tinyint DEFAULT NULL COMMENT '告警类型',
  `send_status` tinyint NOT NULL DEFAULT 0 COMMENT '发送状态',
  `notify_type` varchar(60) DEFAULT NULL COMMENT '通知方式',
  `notify_target` text COMMENT '通知目标',
  `content` text COMMENT '告警内容',
  `error_message` text COMMENT '错误信息',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_sys_job_alarm_log_job` (`tenant_id`, `job_code`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Job告警日志';

CREATE TABLE IF NOT EXISTS `sys_job_workflow` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户ID',
  `workflow_code` varchar(100) NOT NULL COMMENT '编排编码',
  `workflow_name` varchar(160) NOT NULL COMMENT '编排名称',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态',
  `graph_json` mediumtext COMMENT '图定义JSON',
  `remark` text COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sys_job_workflow_code` (`tenant_id`, `workflow_code`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Job DAG编排';

CREATE TABLE IF NOT EXISTS `sys_job_workflow_node` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户ID',
  `workflow_id` bigint NOT NULL COMMENT '编排ID',
  `node_code` varchar(100) NOT NULL COMMENT '节点编码',
  `node_name` varchar(160) DEFAULT NULL COMMENT '节点名称',
  `job_id` bigint DEFAULT NULL COMMENT '任务ID',
  `node_type` varchar(50) DEFAULT NULL COMMENT '节点类型',
  `node_config` text COMMENT '节点配置',
  `position_x` int DEFAULT 0 COMMENT 'X坐标',
  `position_y` int DEFAULT 0 COMMENT 'Y坐标',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_sys_job_workflow_node` (`tenant_id`, `workflow_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Job DAG节点';

CREATE TABLE IF NOT EXISTS `sys_job_workflow_edge` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户ID',
  `workflow_id` bigint NOT NULL COMMENT '编排ID',
  `edge_code` varchar(100) DEFAULT NULL COMMENT '连线编码',
  `source_node_code` varchar(100) NOT NULL COMMENT '源节点编码',
  `target_node_code` varchar(100) NOT NULL COMMENT '目标节点编码',
  `condition_expression` varchar(500) DEFAULT NULL COMMENT '条件表达式',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_sys_job_workflow_edge` (`tenant_id`, `workflow_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Job DAG连线';

CREATE TABLE IF NOT EXISTS `sys_job_workflow_execution` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户ID',
  `workflow_id` bigint NOT NULL COMMENT '编排ID',
  `workflow_code` varchar(100) NOT NULL COMMENT '编排编码',
  `root_log_id` bigint DEFAULT NULL COMMENT '根日志ID',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态',
  `start_time` datetime DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束时间',
  `node_status_json` mediumtext COMMENT '节点状态JSON',
  `result_message` text COMMENT '执行结果',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_sys_job_workflow_execution` (`tenant_id`, `workflow_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Job DAG执行记录';

DROP PROCEDURE IF EXISTS `fx_job_add_column`;
DROP PROCEDURE IF EXISTS `fx_job_add_index`;
DROP PROCEDURE IF EXISTS `fx_job_modify_column`;
DROP PROCEDURE IF EXISTS `fx_job_upgrade_legacy_data`;

DELIMITER $$
CREATE PROCEDURE `fx_job_add_column`(
  IN p_table_name varchar(64),
  IN p_column_name varchar(64),
  IN p_column_definition text
)
BEGIN
  IF NOT EXISTS (
    SELECT 1
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = p_table_name
      AND column_name = p_column_name
  ) THEN
    SET @fx_job_sql = CONCAT('ALTER TABLE `', p_table_name, '` ADD COLUMN `', p_column_name, '` ', p_column_definition);
    PREPARE fx_job_stmt FROM @fx_job_sql;
    EXECUTE fx_job_stmt;
    DEALLOCATE PREPARE fx_job_stmt;
  END IF;
END$$

CREATE PROCEDURE `fx_job_add_index`(
  IN p_table_name varchar(64),
  IN p_index_name varchar(64),
  IN p_index_definition text
)
BEGIN
  IF NOT EXISTS (
    SELECT 1
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = p_table_name
      AND index_name = p_index_name
  ) THEN
    SET @fx_job_sql = CONCAT('ALTER TABLE `', p_table_name, '` ADD ', p_index_definition);
    PREPARE fx_job_stmt FROM @fx_job_sql;
    EXECUTE fx_job_stmt;
    DEALLOCATE PREPARE fx_job_stmt;
  END IF;
END$$

CREATE PROCEDURE `fx_job_modify_column`(
  IN p_table_name varchar(64),
  IN p_column_name varchar(64),
  IN p_column_definition text
)
BEGIN
  IF EXISTS (
    SELECT 1
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = p_table_name
      AND column_name = p_column_name
  ) THEN
    SET @fx_job_sql = CONCAT('ALTER TABLE `', p_table_name, '` MODIFY COLUMN `', p_column_name, '` ', p_column_definition);
    PREPARE fx_job_stmt FROM @fx_job_sql;
    EXECUTE fx_job_stmt;
    DEALLOCATE PREPARE fx_job_stmt;
  END IF;
END$$

CREATE PROCEDURE `fx_job_upgrade_legacy_data`()
BEGIN
  IF EXISTS (
    SELECT 1
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'sys_job_log'
      AND column_name = 'status'
      AND data_type IN ('varchar', 'char')
  ) THEN
    UPDATE `sys_job_log`
    SET `status` = CASE
      WHEN `status` = '0' THEN '2'
      WHEN `status` = '1' THEN '3'
      ELSE `status`
    END
    WHERE `deleted` = 0;
  END IF;

  UPDATE `sys_job_log`
  SET `job_code` = COALESCE(NULLIF(`job_code`, ''), NULLIF(`job_name`, ''), CONCAT('legacy-log-', `id`)),
      `fire_time` = COALESCE(`fire_time`, `start_time`, `create_time`),
      `update_time` = COALESCE(`update_time`, `create_time`, NOW())
  WHERE `deleted` = 0;

  IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'sys_job_log' AND column_name = 'stop_time') THEN
    UPDATE `sys_job_log` SET `end_time` = COALESCE(`end_time`, `stop_time`) WHERE `deleted` = 0;
  END IF;

  IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'sys_job_log' AND column_name = 'cost_time') THEN
    UPDATE `sys_job_log` SET `duration_ms` = COALESCE(`duration_ms`, `cost_time`) WHERE `deleted` = 0;
  END IF;

  IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'sys_job_log' AND column_name = 'job_message') THEN
    UPDATE `sys_job_log` SET `result_message` = COALESCE(`result_message`, `job_message`) WHERE `deleted` = 0;
  END IF;

  IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'sys_job_log' AND column_name = 'exception_info') THEN
    UPDATE `sys_job_log` SET `error_stack` = COALESCE(`error_stack`, `exception_info`) WHERE `deleted` = 0;
  END IF;

  IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'sys_job') THEN
    INSERT INTO `sys_job_task` (
      `tenant_id`, `job_code`, `job_name`, `job_group`, `job_type`, `schedule_type`, `cron_expression`,
      `bean_name`, `method_name`, `job_params`, `status`, `block_strategy`, `timeout_seconds`,
      `max_retry_count`, `retry_interval_seconds`, `next_trigger_time`, `remark`, `create_time`, `create_by`,
      `update_time`, `update_by`, `deleted`
    )
    SELECT COALESCE(j.`tenant_id`, 0),
           CONCAT('legacy-', j.`id`),
           j.`job_name`,
           j.`job_group`,
           1,
           1,
           j.`cron_expression`,
           CASE
             WHEN LOCATE('.', j.`invoke_target`) > 0 THEN SUBSTRING_INDEX(j.`invoke_target`, '.', 1)
             ELSE j.`invoke_target`
           END,
           CASE
             WHEN LOCATE('.', j.`invoke_target`) > 0 THEN SUBSTRING_INDEX(SUBSTRING_INDEX(j.`invoke_target`, '(', 1), '.', -1)
             ELSE 'execute'
           END,
           NULL,
           CASE WHEN j.`status` = '0' THEN 1 ELSE 0 END,
           CASE WHEN j.`concurrent` = '1' THEN 1 ELSE 2 END,
           60,
           0,
           60,
           NULL,
           j.`remark`,
           COALESCE(j.`create_time`, NOW()),
           j.`create_by`,
           COALESCE(j.`update_time`, NOW()),
           j.`update_by`,
           0
    FROM `sys_job` j
    WHERE NOT EXISTS (
      SELECT 1
      FROM `sys_job_task` t
      WHERE t.`tenant_id` = COALESCE(j.`tenant_id`, 0)
        AND t.`job_code` = CONCAT('legacy-', j.`id`)
        AND t.`deleted` = 0
    );
  END IF;
END$$
DELIMITER ;

-- 兼容旧版 forgex_job.sql 已创建的 sys_job_log：补齐当前实体和 MyBatis-Plus 逻辑删除所需字段。
CALL `fx_job_add_column`('sys_job_log', 'job_id', 'bigint NOT NULL DEFAULT 0 COMMENT ''任务ID'' AFTER `tenant_id`');
CALL `fx_job_add_column`('sys_job_log', 'job_code', 'varchar(100) NOT NULL DEFAULT '''' COMMENT ''任务编码'' AFTER `job_id`');
CALL `fx_job_add_column`('sys_job_log', 'trigger_type', 'tinyint NOT NULL DEFAULT 1 COMMENT ''触发类型'' AFTER `job_name`');
CALL `fx_job_add_column`('sys_job_log', 'fire_time', 'datetime DEFAULT NULL COMMENT ''计划触发时间'' AFTER `trigger_type`');
CALL `fx_job_add_column`('sys_job_log', 'end_time', 'datetime DEFAULT NULL COMMENT ''结束时间'' AFTER `start_time`');
CALL `fx_job_add_column`('sys_job_log', 'duration_ms', 'bigint DEFAULT NULL COMMENT ''耗时毫秒'' AFTER `end_time`');
CALL `fx_job_add_column`('sys_job_log', 'instance_id', 'varchar(160) DEFAULT NULL COMMENT ''实例ID'' AFTER `status`');
CALL `fx_job_add_column`('sys_job_log', 'request_params', 'text COMMENT ''请求参数'' AFTER `instance_id`');
CALL `fx_job_add_column`('sys_job_log', 'result_message', 'text COMMENT ''执行结果'' AFTER `request_params`');
CALL `fx_job_add_column`('sys_job_log', 'error_stack', 'mediumtext COMMENT ''异常堆栈'' AFTER `result_message`');
CALL `fx_job_add_column`('sys_job_log', 'retry_count', 'int NOT NULL DEFAULT 0 COMMENT ''重试次数'' AFTER `error_stack`');
CALL `fx_job_add_column`('sys_job_log', 'retry_of_log_id', 'bigint DEFAULT NULL COMMENT ''来源日志ID'' AFTER `retry_count`');
CALL `fx_job_add_column`('sys_job_log', 'shard_index', 'int DEFAULT 0 COMMENT ''分片序号'' AFTER `retry_of_log_id`');
CALL `fx_job_add_column`('sys_job_log', 'shard_total', 'int DEFAULT 1 COMMENT ''分片总数'' AFTER `shard_index`');
CALL `fx_job_add_column`('sys_job_log', 'request_id', 'varchar(120) DEFAULT NULL COMMENT ''请求ID'' AFTER `shard_total`');
CALL `fx_job_add_column`('sys_job_log', 'create_by', 'varchar(50) DEFAULT NULL COMMENT ''创建人'' AFTER `create_time`');
CALL `fx_job_add_column`('sys_job_log', 'update_time', 'datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT ''更新时间'' AFTER `create_by`');
CALL `fx_job_add_column`('sys_job_log', 'update_by', 'varchar(50) DEFAULT NULL COMMENT ''更新人'' AFTER `update_time`');
CALL `fx_job_add_column`('sys_job_log', 'deleted', 'tinyint NOT NULL DEFAULT 0 COMMENT ''逻辑删除'' AFTER `update_by`');

CALL `fx_job_upgrade_legacy_data`();
CALL `fx_job_modify_column`('sys_job_log', 'status', 'tinyint NOT NULL DEFAULT 0 COMMENT ''状态：0 待执行，1 执行中，2 成功，3 失败，4 超时，5 取消''');
CALL `fx_job_modify_column`('sys_job_log', 'tenant_id', 'bigint NOT NULL DEFAULT 0 COMMENT ''租户ID''');
CALL `fx_job_modify_column`('sys_job_log', 'job_code', 'varchar(100) NOT NULL COMMENT ''任务编码''');

CALL `fx_job_add_index`('sys_job_log', 'idx_sys_job_log_job_time', 'KEY `idx_sys_job_log_job_time` (`tenant_id`, `job_id`, `fire_time`)');
CALL `fx_job_add_index`('sys_job_log', 'idx_sys_job_log_status', 'KEY `idx_sys_job_log_status` (`tenant_id`, `status`, `create_time`)');
CALL `fx_job_add_index`('sys_job_log', 'idx_sys_job_log_request', 'KEY `idx_sys_job_log_request` (`tenant_id`, `request_id`)');

DROP PROCEDURE IF EXISTS `fx_job_add_column`;
DROP PROCEDURE IF EXISTS `fx_job_add_index`;
DROP PROCEDURE IF EXISTS `fx_job_modify_column`;
DROP PROCEDURE IF EXISTS `fx_job_upgrade_legacy_data`;

USE `forgex_admin`;

SET @TARGET_TENANT_ID = NULL;
SET @TARGET_ROLE_CODE = 'admin';
SET @TENANT_ID = COALESCE(@TARGET_TENANT_ID, (SELECT id FROM sys_tenant WHERE deleted = 0 ORDER BY id LIMIT 1));
SET @SYS_MODULE_ID = (SELECT id FROM sys_module WHERE tenant_id = @TENANT_ID AND code = 'sys' AND deleted = 0 LIMIT 1);
SET @ADMIN_ROLE_ID = (SELECT id FROM sys_role WHERE tenant_id = @TENANT_ID AND role_key = @TARGET_ROLE_CODE AND deleted = 0 LIMIT 1);
SET @SYS_MODULE_ROOT_PARENT_ID = 0;

UPDATE sys_module
SET visible = 0,
    status = 0,
    update_time = NOW(),
    update_by = '20260513_init_forgex_job'
WHERE tenant_id = @TENANT_ID
  AND code = 'job'
  AND deleted = 0;

UPDATE sys_menu m
JOIN sys_module jm ON jm.id = m.module_id
SET m.module_id = @SYS_MODULE_ID,
    m.update_time = NOW(),
    m.update_by = '20260513_init_forgex_job'
WHERE m.tenant_id = @TENANT_ID
  AND jm.tenant_id = @TENANT_ID
  AND jm.code = 'job'
  AND jm.deleted = 0
  AND m.deleted = 0
  AND @SYS_MODULE_ID IS NOT NULL;

UPDATE sys_menu
SET parent_id = @SYS_MODULE_ROOT_PARENT_ID,
    menu_level = 1,
    type = 'catalog',
    component_key = NULL,
    update_time = NOW(),
    update_by = '20260513_init_forgex_job'
WHERE tenant_id = @TENANT_ID
  AND module_id = @SYS_MODULE_ID
  AND path = 'job'
  AND deleted = 0;

INSERT INTO sys_permission (`permission_name`, `permission_key`, `url`, `method`, `tenant_id`, `create_time`, `update_time`, `deleted`)
SELECT seed.permission_name, seed.permission_key, NULL, 'POST', 0, NOW(), NOW(), 0
FROM (
  SELECT 'Job Dashboard View' permission_name, 'job:dashboard:view' permission_key UNION ALL
  SELECT 'Job Task List', 'job:task:list' UNION ALL
  SELECT 'Job Task View', 'job:task:view' UNION ALL
  SELECT 'Job Task Add', 'job:task:add' UNION ALL
  SELECT 'Job Task Edit', 'job:task:edit' UNION ALL
  SELECT 'Job Task Delete', 'job:task:delete' UNION ALL
  SELECT 'Job Task Change Status', 'job:task:changeStatus' UNION ALL
  SELECT 'Job Task Trigger', 'job:task:trigger' UNION ALL
  SELECT 'Job Log List', 'job:log:list' UNION ALL
  SELECT 'Job Log View', 'job:log:view' UNION ALL
  SELECT 'Job Instance List', 'job:instance:list' UNION ALL
  SELECT 'Job Instance Maintenance', 'job:instance:maintenance' UNION ALL
  SELECT 'Job Retry List', 'job:retry:list' UNION ALL
  SELECT 'Job Retry Handle', 'job:retry:handle' UNION ALL
  SELECT 'Job Alarm List', 'job:alarm:list' UNION ALL
  SELECT 'Job Alarm View', 'job:alarm:view' UNION ALL
  SELECT 'Job Alarm Add', 'job:alarm:add' UNION ALL
  SELECT 'Job Alarm Edit', 'job:alarm:edit' UNION ALL
  SELECT 'Job Alarm Delete', 'job:alarm:delete' UNION ALL
  SELECT 'Job Workflow List', 'job:workflow:list' UNION ALL
  SELECT 'Job Workflow View', 'job:workflow:view' UNION ALL
  SELECT 'Job Workflow Add', 'job:workflow:add' UNION ALL
  SELECT 'Job Workflow Edit', 'job:workflow:edit' UNION ALL
  SELECT 'Job Workflow Publish', 'job:workflow:publish' UNION ALL
  SELECT 'Job Workflow Execute', 'job:workflow:execute'
) seed
WHERE NOT EXISTS (SELECT 1 FROM sys_permission p WHERE p.permission_key = seed.permission_key AND p.deleted = 0);

INSERT INTO sys_menu (
  tenant_id, tenant_type, module_id, parent_id, type, path, name, name_i18n_json, icon, component_key,
  perm_key, order_num, visible, status, create_time, create_by, update_time, update_by, deleted,
  menu_level, menu_mode, external_url
)
SELECT @TENANT_ID, 'PUBLIC', @SYS_MODULE_ID, @SYS_MODULE_ROOT_PARENT_ID, 'catalog', 'job', '定时任务',
       JSON_OBJECT('zh-CN', '定时任务', 'en-US', 'Scheduled Jobs', 'ja-JP', '定時ジョブ', 'ko-KR', '예약 작업', 'zh-TW', '定時任務'),
       'ClockCircleOutlined', NULL, 'job:dashboard:view', 90, 1, 1, NOW(), '20260513_init_forgex_job', NOW(), '20260513_init_forgex_job', 0,
       1, 'embedded', NULL
WHERE NOT EXISTS (
  SELECT 1 FROM sys_menu WHERE tenant_id = @TENANT_ID AND module_id = @SYS_MODULE_ID AND parent_id = @SYS_MODULE_ROOT_PARENT_ID AND path = 'job' AND deleted = 0
);

SET @JOB_CATALOG_ID = (
  SELECT id FROM sys_menu
  WHERE tenant_id = @TENANT_ID AND module_id = @SYS_MODULE_ID AND parent_id = @SYS_MODULE_ROOT_PARENT_ID AND path = 'job' AND deleted = 0
  ORDER BY id
  LIMIT 1
);

UPDATE sys_menu
SET visible = 0,
    status = 0,
    update_time = NOW(),
    update_by = '20260513_init_forgex_job'
WHERE tenant_id = @TENANT_ID
  AND module_id = @SYS_MODULE_ID
  AND parent_id = @SYS_MODULE_ROOT_PARENT_ID
  AND path = 'job'
  AND deleted = 0
  AND id <> @JOB_CATALOG_ID
  AND @JOB_CATALOG_ID IS NOT NULL;

UPDATE sys_menu
SET parent_id = @JOB_CATALOG_ID,
    module_id = @SYS_MODULE_ID,
    type = 'menu',
    path = CASE component_key
      WHEN 'JobDashboard' THEN 'dashboard'
      WHEN 'JobTask' THEN 'task'
      WHEN 'JobLog' THEN 'log'
      WHEN 'JobInstance' THEN 'instance'
      WHEN 'JobRetry' THEN 'retry'
      WHEN 'JobAlarm' THEN 'alarm'
      WHEN 'JobAlarmLog' THEN 'alarm-log'
      WHEN 'JobWorkflow' THEN 'workflow'
      ELSE path
    END,
    menu_level = 2,
    menu_mode = 'embedded',
    external_url = NULL,
    visible = 1,
    status = 1,
    update_time = NOW(),
    update_by = '20260513_init_forgex_job'
WHERE tenant_id = @TENANT_ID
  AND deleted = 0
  AND component_key IN ('JobDashboard', 'JobTask', 'JobLog', 'JobInstance', 'JobRetry', 'JobAlarm', 'JobAlarmLog', 'JobWorkflow')
  AND @JOB_CATALOG_ID IS NOT NULL;

INSERT INTO sys_menu (
  tenant_id, tenant_type, module_id, parent_id, type, path, name, name_i18n_json, icon, component_key,
  perm_key, order_num, visible, status, create_time, create_by, update_time, update_by, deleted,
  menu_level, menu_mode, external_url
)
SELECT @TENANT_ID, 'PUBLIC', @SYS_MODULE_ID, @JOB_CATALOG_ID, 'menu', seed.path, seed.name, seed.name_i18n_json,
       seed.icon, seed.component_key, seed.perm_key, seed.order_num, 1, 1, NOW(), '20260513_init_forgex_job', NOW(), '20260513_init_forgex_job', 0,
       2, 'embedded', NULL
FROM (
  SELECT 'dashboard' path, '任务大盘' name, JSON_OBJECT('zh-CN','任务大盘','en-US','Job Dashboard','ja-JP','ジョブダッシュボード','ko-KR','작업 대시보드','zh-TW','任務大盤') name_i18n_json, 'DashboardOutlined' icon, 'JobDashboard' component_key, 'job:dashboard:view' perm_key, 1 order_num UNION ALL
  SELECT 'task', '任务管理', JSON_OBJECT('zh-CN','任务管理','en-US','Job Tasks','ja-JP','ジョブ管理','ko-KR','작업 관리','zh-TW','任務管理'), 'ScheduleOutlined', 'JobTask', 'job:task:list', 2 UNION ALL
  SELECT 'log', '执行日志', JSON_OBJECT('zh-CN','执行日志','en-US','Execution Logs','ja-JP','実行ログ','ko-KR','실행 로그','zh-TW','執行日誌'), 'FileTextOutlined', 'JobLog', 'job:log:list', 3 UNION ALL
  SELECT 'instance', '执行器实例', JSON_OBJECT('zh-CN','执行器实例','en-US','Executor Instances','ja-JP','実行インスタンス','ko-KR','실행기 인스턴스','zh-TW','執行器實例'), 'ClusterOutlined', 'JobInstance', 'job:instance:list', 4 UNION ALL
  SELECT 'retry', '重试/死信', JSON_OBJECT('zh-CN','重试/死信','en-US','Retry / Dead Letter','ja-JP','リトライ/デッドレター','ko-KR','재시도/데드레터','zh-TW','重試/死信'), 'ReloadOutlined', 'JobRetry', 'job:retry:list', 5 UNION ALL
  SELECT 'alarm', '告警规则', JSON_OBJECT('zh-CN','告警规则','en-US','Alarm Rules','ja-JP','アラームルール','ko-KR','알람 규칙','zh-TW','告警規則'), 'BellOutlined', 'JobAlarm', 'job:alarm:list', 6 UNION ALL
  SELECT 'alarm-log', '告警日志', JSON_OBJECT('zh-CN','告警日志','en-US','Alarm Logs','ja-JP','アラームログ','ko-KR','알람 로그','zh-TW','告警日誌'), 'AlertOutlined', 'JobAlarmLog', 'job:alarm:list', 7 UNION ALL
  SELECT 'workflow', 'DAG 编排', JSON_OBJECT('zh-CN','DAG 编排','en-US','DAG Workflow','ja-JP','DAG 編成','ko-KR','DAG 편성','zh-TW','DAG 編排'), 'BranchesOutlined', 'JobWorkflow', 'job:workflow:list', 8
) seed
WHERE @JOB_CATALOG_ID IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM sys_menu m WHERE m.tenant_id = @TENANT_ID AND m.parent_id = @JOB_CATALOG_ID AND m.component_key = seed.component_key AND m.deleted = 0
  );

UPDATE sys_menu
SET menu_level = 2,
    update_time = NOW(),
    update_by = '20260513_init_forgex_job'
WHERE tenant_id = @TENANT_ID
  AND parent_id = @JOB_CATALOG_ID
  AND deleted = 0;

INSERT INTO sys_menu (
  tenant_id, tenant_type, module_id, parent_id, type, path, name, name_i18n_json, icon, component_key,
  perm_key, order_num, visible, status, create_time, create_by, update_time, update_by, deleted,
  menu_level, menu_mode, external_url
)
SELECT @TENANT_ID, 'PUBLIC', @SYS_MODULE_ID, page.id, 'button', seed.path, seed.name,
       JSON_OBJECT('zh-CN', seed.name, 'en-US', seed.en_name), NULL, NULL, seed.perm_key, seed.order_num,
       1, 1, NOW(), '20260513_init_forgex_job', NOW(), '20260513_init_forgex_job', 0, 3, 'embedded', NULL
FROM (
  SELECT 'JobTask' page_key, 'view' path, '查看' name, 'View' en_name, 'job:task:view' perm_key, 1 order_num UNION ALL
  SELECT 'JobTask', 'add', '新增', 'Add', 'job:task:add', 2 UNION ALL
  SELECT 'JobTask', 'edit', '编辑', 'Edit', 'job:task:edit', 3 UNION ALL
  SELECT 'JobTask', 'delete', '删除', 'Delete', 'job:task:delete', 4 UNION ALL
  SELECT 'JobTask', 'change-status', '启停', 'Change Status', 'job:task:changeStatus', 5 UNION ALL
  SELECT 'JobTask', 'trigger', '触发', 'Trigger', 'job:task:trigger', 6 UNION ALL
  SELECT 'JobLog', 'view', '查看', 'View', 'job:log:view', 1 UNION ALL
  SELECT 'JobInstance', 'maintenance', '维护模式', 'Maintenance', 'job:instance:maintenance', 1 UNION ALL
  SELECT 'JobRetry', 'handle', '处理', 'Handle', 'job:retry:handle', 1 UNION ALL
  SELECT 'JobAlarm', 'view', '查看', 'View', 'job:alarm:view', 1 UNION ALL
  SELECT 'JobAlarm', 'add', '新增', 'Add', 'job:alarm:add', 2 UNION ALL
  SELECT 'JobAlarm', 'edit', '编辑', 'Edit', 'job:alarm:edit', 3 UNION ALL
  SELECT 'JobAlarm', 'delete', '删除', 'Delete', 'job:alarm:delete', 4 UNION ALL
  SELECT 'JobWorkflow', 'view', '查看', 'View', 'job:workflow:view', 1 UNION ALL
  SELECT 'JobWorkflow', 'add', '新增', 'Add', 'job:workflow:add', 2 UNION ALL
  SELECT 'JobWorkflow', 'edit', '编辑', 'Edit', 'job:workflow:edit', 3 UNION ALL
  SELECT 'JobWorkflow', 'publish', '发布', 'Publish', 'job:workflow:publish', 4 UNION ALL
  SELECT 'JobWorkflow', 'execute', '执行', 'Execute', 'job:workflow:execute', 5
) seed
JOIN sys_menu page ON page.tenant_id = @TENANT_ID AND page.parent_id = @JOB_CATALOG_ID AND page.component_key = seed.page_key AND page.deleted = 0
WHERE NOT EXISTS (
  SELECT 1 FROM sys_menu btn WHERE btn.tenant_id = @TENANT_ID AND btn.parent_id = page.id AND btn.perm_key = seed.perm_key AND btn.deleted = 0
);

UPDATE sys_menu btn
JOIN sys_menu page ON page.id = btn.parent_id
SET btn.menu_level = 3,
    btn.update_time = NOW(),
    btn.update_by = '20260513_init_forgex_job'
WHERE btn.tenant_id = @TENANT_ID
  AND page.parent_id = @JOB_CATALOG_ID
  AND btn.deleted = 0
  AND page.deleted = 0;

INSERT INTO sys_role_menu (tenant_id, role_id, menu_id)
SELECT @TENANT_ID, @ADMIN_ROLE_ID, m.id
FROM sys_menu m
WHERE m.tenant_id = @TENANT_ID
  AND m.deleted = 0
  AND (m.perm_key LIKE 'job:%' OR m.id = @JOB_CATALOG_ID)
  AND @ADMIN_ROLE_ID IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM sys_role_menu rm WHERE rm.tenant_id = @TENANT_ID AND rm.role_id = @ADMIN_ROLE_ID AND rm.menu_id = m.id
  );

USE `forgex_common`;

INSERT INTO fx_table_config (
  tenant_id, table_code, table_name_i18n_json, table_type, row_key, default_page_size,
  default_sort_json, enabled, version, create_by, update_by, deleted
)
SELECT 0, seed.table_code, JSON_OBJECT('zh-CN', seed.zh_name, 'en-US', seed.en_name), 'NORMAL', 'id', 20,
       JSON_OBJECT('field', 'createTime', 'order', 'desc'), 1, 1, '20260513_init_forgex_job', '20260513_init_forgex_job', 0
FROM (
  SELECT 'JobTaskTable' table_code, '任务管理' zh_name, 'Job Tasks' en_name UNION ALL
  SELECT 'JobLogTable', '执行日志', 'Execution Logs' UNION ALL
  SELECT 'JobInstanceTable', '执行器实例', 'Executor Instances' UNION ALL
  SELECT 'JobRetryTable', '重试/死信', 'Retry / Dead Letter' UNION ALL
  SELECT 'JobAlarmRuleTable', '告警规则', 'Alarm Rules' UNION ALL
  SELECT 'JobAlarmLogTable', '告警日志', 'Alarm Logs' UNION ALL
  SELECT 'JobWorkflowTable', 'DAG 编排', 'DAG Workflow'
) seed
WHERE NOT EXISTS (
  SELECT 1 FROM fx_table_config cfg WHERE cfg.tenant_id = 0 AND cfg.table_code = seed.table_code AND cfg.deleted = 0
);

INSERT INTO fx_table_column_config (
  tenant_id, table_code, field, title_i18n_json, align, width, fixed, ellipsis, sortable,
  sorter_field, queryable, query_type, query_operator, dict_code, render_type, perm_key,
  order_num, enabled, create_by, update_by, deleted
)
SELECT 0, seed.table_code, seed.field, JSON_OBJECT('zh-CN', seed.zh_title, 'en-US', seed.en_title),
       seed.align, seed.width, seed.fixed, seed.ellipsis, seed.sortable, seed.sorter_field,
       seed.queryable, seed.query_type, seed.query_operator, seed.dict_code, NULL, seed.perm_key,
       seed.order_num, 1, '20260513_init_forgex_job', '20260513_init_forgex_job', 0
FROM (
  SELECT 'JobTaskTable' table_code, 'jobCode' field, '任务编码' zh_title, 'Job Code' en_title, 'left' align, 160 width, NULL fixed, 0 ellipsis, 1 sortable, 'job_code' sorter_field, 1 queryable, 'input' query_type, 'like' query_operator, NULL dict_code, NULL perm_key, 1 order_num UNION ALL
  SELECT 'JobTaskTable','jobName','任务名称','Job Name','left',180,NULL,1,1,'job_name',1,'input','like',NULL,NULL,2 UNION ALL
  SELECT 'JobTaskTable','jobGroup','任务分组','Group','left',120,NULL,0,0,NULL,1,'input','like',NULL,NULL,3 UNION ALL
  SELECT 'JobTaskTable','jobType','任务类型','Type','center',110,NULL,0,0,NULL,1,'select','eq','job_type',NULL,4 UNION ALL
  SELECT 'JobTaskTable','scheduleType','调度类型','Schedule','center',120,NULL,0,0,NULL,1,'select','eq','job_schedule_type',NULL,5 UNION ALL
  SELECT 'JobTaskTable','status','状态','Status','center',90,NULL,0,0,NULL,1,'select','eq','job_status',NULL,6 UNION ALL
  SELECT 'JobTaskTable','nextTriggerTime','下次触发','Next Fire Time','center',180,NULL,0,1,'next_trigger_time',0,NULL,NULL,NULL,NULL,7 UNION ALL
  SELECT 'JobTaskTable','lastStatus','上次状态','Last Status','center',100,NULL,0,0,NULL,0,NULL,NULL,'job_log_status',NULL,8 UNION ALL
  SELECT 'JobTaskTable','action','操作','Action','center',260,'right',0,0,NULL,0,NULL,NULL,NULL,NULL,99 UNION ALL
  SELECT 'JobLogTable','jobCode','任务编码','Job Code','left',160,NULL,0,1,'job_code',1,'input','like',NULL,NULL,1 UNION ALL
  SELECT 'JobLogTable','jobName','任务名称','Job Name','left',180,NULL,1,1,'job_name',1,'input','like',NULL,NULL,2 UNION ALL
  SELECT 'JobLogTable','status','状态','Status','center',100,NULL,0,0,NULL,1,'select','eq','job_log_status',NULL,3 UNION ALL
  SELECT 'JobLogTable','fireTime','触发时间','Fire Time','center',180,NULL,0,1,'fire_time',0,NULL,NULL,NULL,NULL,4 UNION ALL
  SELECT 'JobLogTable','durationMs','耗时(ms)','Duration(ms)','right',110,NULL,0,1,'duration_ms',0,NULL,NULL,NULL,NULL,5 UNION ALL
  SELECT 'JobLogTable','instanceId','实例ID','Instance ID','left',180,NULL,1,0,NULL,0,NULL,NULL,NULL,NULL,6 UNION ALL
  SELECT 'JobLogTable','action','操作','Action','center',120,'right',0,0,NULL,0,NULL,NULL,NULL,NULL,99 UNION ALL
  SELECT 'JobInstanceTable','instanceId','实例ID','Instance ID','left',220,NULL,1,0,NULL,1,'input','like',NULL,NULL,1 UNION ALL
  SELECT 'JobInstanceTable','ip','IP','IP','left',120,NULL,0,0,NULL,0,NULL,NULL,NULL,NULL,2 UNION ALL
  SELECT 'JobInstanceTable','port','端口','Port','right',90,NULL,0,0,NULL,0,NULL,NULL,NULL,NULL,3 UNION ALL
  SELECT 'JobInstanceTable','status','状态','Status','center',100,NULL,0,0,NULL,1,'select','eq','job_instance_status',NULL,4 UNION ALL
  SELECT 'JobInstanceTable','runningCount','运行中','Running','right',100,NULL,0,0,NULL,0,NULL,NULL,NULL,NULL,5 UNION ALL
  SELECT 'JobInstanceTable','lastHeartbeatTime','最后心跳','Last Heartbeat','center',180,NULL,0,1,'last_heartbeat_time',0,NULL,NULL,NULL,NULL,6 UNION ALL
  SELECT 'JobInstanceTable','maintenance','维护模式','Maintenance','center',110,NULL,0,0,NULL,0,NULL,NULL,NULL,NULL,7 UNION ALL
  SELECT 'JobInstanceTable','action','操作','Action','center',160,'right',0,0,NULL,0,NULL,NULL,NULL,NULL,99 UNION ALL
  SELECT 'JobRetryTable','jobCode','任务编码','Job Code','left',160,NULL,0,1,'job_code',1,'input','like',NULL,NULL,1 UNION ALL
  SELECT 'JobRetryTable','retryCount','重试次数','Retry Count','right',100,NULL,0,0,NULL,0,NULL,NULL,NULL,NULL,2 UNION ALL
  SELECT 'JobRetryTable','maxRetryCount','最大次数','Max Retry','right',100,NULL,0,0,NULL,0,NULL,NULL,NULL,NULL,3 UNION ALL
  SELECT 'JobRetryTable','nextRetryTime','下次重试','Next Retry','center',180,NULL,0,1,'next_retry_time',0,NULL,NULL,NULL,NULL,4 UNION ALL
  SELECT 'JobRetryTable','status','状态','Status','center',100,NULL,0,0,NULL,1,'select','eq','job_retry_status',NULL,5 UNION ALL
  SELECT 'JobRetryTable','lastError','最后错误','Last Error','left',260,NULL,1,0,NULL,0,NULL,NULL,NULL,NULL,6 UNION ALL
  SELECT 'JobRetryTable','action','操作','Action','center',180,'right',0,0,NULL,0,NULL,NULL,NULL,NULL,99 UNION ALL
  SELECT 'JobAlarmRuleTable','ruleName','规则名称','Rule Name','left',180,NULL,0,1,'rule_name',1,'input','like',NULL,NULL,1 UNION ALL
  SELECT 'JobAlarmRuleTable','jobCode','任务编码','Job Code','left',160,NULL,0,1,'job_code',1,'input','like',NULL,NULL,2 UNION ALL
  SELECT 'JobAlarmRuleTable','alarmType','告警类型','Alarm Type','center',110,NULL,0,0,NULL,0,NULL,NULL,'job_alarm_type',NULL,3 UNION ALL
  SELECT 'JobAlarmRuleTable','thresholdCount','阈值','Threshold','right',90,NULL,0,0,NULL,0,NULL,NULL,NULL,NULL,4 UNION ALL
  SELECT 'JobAlarmRuleTable','windowMinutes','窗口分钟','Window','right',100,NULL,0,0,NULL,0,NULL,NULL,NULL,NULL,5 UNION ALL
  SELECT 'JobAlarmRuleTable','status','状态','Status','center',90,NULL,0,0,NULL,1,'select','eq','job_status',NULL,6 UNION ALL
  SELECT 'JobAlarmRuleTable','action','操作','Action','center',160,'right',0,0,NULL,0,NULL,NULL,NULL,NULL,99 UNION ALL
  SELECT 'JobAlarmLogTable','jobCode','任务编码','Job Code','left',160,NULL,0,1,'job_code',1,'input','like',NULL,NULL,1 UNION ALL
  SELECT 'JobAlarmLogTable','alarmType','告警类型','Alarm Type','center',110,NULL,0,0,NULL,0,NULL,NULL,'job_alarm_type',NULL,2 UNION ALL
  SELECT 'JobAlarmLogTable','sendStatus','发送状态','Send Status','center',100,NULL,0,0,NULL,1,'select','eq','job_send_status',NULL,3 UNION ALL
  SELECT 'JobAlarmLogTable','notifyType','通知方式','Notify Type','left',120,NULL,0,0,NULL,0,NULL,NULL,NULL,NULL,4 UNION ALL
  SELECT 'JobAlarmLogTable','content','告警内容','Content','left',280,NULL,1,0,NULL,0,NULL,NULL,NULL,NULL,5 UNION ALL
  SELECT 'JobWorkflowTable','workflowCode','编排编码','Workflow Code','left',170,NULL,0,1,'workflow_code',1,'input','like',NULL,NULL,1 UNION ALL
  SELECT 'JobWorkflowTable','workflowName','编排名称','Workflow Name','left',180,NULL,0,1,'workflow_name',1,'input','like',NULL,NULL,2 UNION ALL
  SELECT 'JobWorkflowTable','status','状态','Status','center',100,NULL,0,0,NULL,1,'select','eq','job_workflow_status',NULL,3 UNION ALL
  SELECT 'JobWorkflowTable','updateTime','更新时间','Update Time','center',180,NULL,0,1,'update_time',0,NULL,NULL,NULL,NULL,4 UNION ALL
  SELECT 'JobWorkflowTable','action','操作','Action','center',220,'right',0,0,NULL,0,NULL,NULL,NULL,NULL,99
) seed
WHERE NOT EXISTS (
  SELECT 1 FROM fx_table_column_config cfg
  WHERE cfg.tenant_id = 0 AND cfg.table_code = seed.table_code AND cfg.field = seed.field AND cfg.deleted = 0
);

INSERT INTO fx_i18n_message (module, prompt_code, text_i18n_json, enabled, version, create_time, update_time, deleted)
SELECT 'job', seed.prompt_code, JSON_OBJECT('zh-CN', seed.zh_text, 'en-US', seed.en_text), 1, 1, NOW(), NOW(), 0
FROM (
  SELECT 'JOB_PARAM_REQUIRED' prompt_code, '任务参数不能为空' zh_text, 'Job parameters are required' en_text UNION ALL
  SELECT 'JOB_NOT_FOUND', '任务不存在', 'Job not found' UNION ALL
  SELECT 'JOB_CODE_EXISTS', '任务编码已存在', 'Job code already exists' UNION ALL
  SELECT 'JOB_CODE_IMMUTABLE', '任务编码不允许修改', 'Job code cannot be changed' UNION ALL
  SELECT 'JOB_CRON_INVALID', 'Cron 表达式不合法', 'Invalid cron expression' UNION ALL
  SELECT 'JOB_JSON_INVALID', 'JSON 参数不合法', 'Invalid JSON' UNION ALL
  SELECT 'JOB_HANDLER_NOT_FOUND', '任务处理器不存在', 'Job handler not found' UNION ALL
  SELECT 'JOB_TRIGGER_FAILED', '任务触发失败', 'Job trigger failed' UNION ALL
  SELECT 'JOB_HTTP_NOT_ALLOWED', 'HTTP 地址未在白名单内', 'HTTP URL is not allowlisted' UNION ALL
  SELECT 'JOB_SCRIPT_NOT_ALLOWED', '脚本执行未开启或命令未在白名单内', 'Script execution is disabled or not allowlisted' UNION ALL
  SELECT 'JOB_WORKFLOW_CYCLE', 'DAG 编排存在环', 'DAG contains a cycle' UNION ALL
  SELECT 'JOB_WORKFLOW_NOT_FOUND', 'DAG 编排不存在', 'DAG workflow not found' UNION ALL
  SELECT 'JOB_RETRY_NOT_FOUND', '重试记录不存在', 'Retry record not found' UNION ALL
  SELECT 'JOB_ALARM_NOT_FOUND', '告警规则不存在', 'Alarm rule not found'
) seed
WHERE NOT EXISTS (
  SELECT 1 FROM fx_i18n_message msg WHERE msg.module = 'job' AND msg.prompt_code = seed.prompt_code AND msg.deleted = 0
);
