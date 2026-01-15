-- ================================================
-- Forgex SCADA 数据库表结构
-- 数据库: forgex_scada
-- 说明: 监控与数据采集系统表结构
-- ================================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS forgex_scada DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

USE forgex_scada;

-- ================================================
-- 设备信息表
-- ================================================
CREATE TABLE IF NOT EXISTS scada_device (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    device_code VARCHAR(50) NOT NULL COMMENT '设备编码',
    device_name VARCHAR(200) NOT NULL COMMENT '设备名称',
    device_type VARCHAR(50) NOT NULL COMMENT '设备类型',
    device_category VARCHAR(100) COMMENT '设备分类',
    manufacturer VARCHAR(100) COMMENT '制造商',
    model VARCHAR(100) COMMENT '型号',
    serial_number VARCHAR(100) COMMENT '序列号',
    installation_date DATE COMMENT '安装日期',
    location VARCHAR(200) COMMENT '安装位置',
    status VARCHAR(20) DEFAULT 'NORMAL' COMMENT '状态：NORMAL/MAINTENANCE/Fault/SCRAPPED',
    ip_address VARCHAR(50) COMMENT 'IP地址',
    port INT COMMENT '端口',
    protocol VARCHAR(50) COMMENT '通信协议：MODBUS/OPC-UA/PROFINET等',
    tenant_id BIGINT COMMENT '租户ID',
    create_by VARCHAR(64) COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(64) COMMENT '更新人',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    UNIQUE KEY uk_device_code (device_code),
    INDEX idx_device_type (device_type),
    INDEX idx_status (status),
    INDEX idx_tenant (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备信息表';

-- ================================================
-- 设备点位表
-- ================================================
CREATE TABLE IF NOT EXISTS scada_point (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    device_id BIGINT NOT NULL COMMENT '设备ID',
    point_code VARCHAR(100) NOT NULL COMMENT '点位编码',
    point_name VARCHAR(200) NOT NULL COMMENT '点位名称',
    point_type VARCHAR(20) NOT NULL COMMENT '点位类型：AI/AO/DI/DO',
    data_type VARCHAR(20) COMMENT '数据类型：INT/FLOAT/BOOL/STRING',
    unit VARCHAR(20) COMMENT '单位',
    address VARCHAR(100) COMMENT '地址',
    description VARCHAR(500) COMMENT '描述',
    sampling_rate INT COMMENT '采样频率(毫秒)',
    alarm_enabled TINYINT DEFAULT 0 COMMENT '是否启用报警',
    alarm_high DECIMAL(20,4) COMMENT '报警上限',
    alarm_low DECIMAL(20,4) COMMENT '报警下限',
    tenant_id BIGINT COMMENT '租户ID',
    create_by VARCHAR(64) COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(64) COMMENT '更新人',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    UNIQUE KEY uk_device_point (device_id, point_code),
    INDEX idx_point_type (point_type),
    INDEX idx_tenant (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备点位表';

-- ================================================
-- 实时数据表
-- ================================================
CREATE TABLE IF NOT EXISTS scada_realtime_data (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    device_id BIGINT NOT NULL COMMENT '设备ID',
    point_id BIGINT NOT NULL COMMENT '点位ID',
    point_code VARCHAR(100) COMMENT '点位编码',
    value DECIMAL(20,4) COMMENT '数值',
    quality TINYINT COMMENT '质量码：192-好 0-坏',
    collect_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '采集时间',
    tenant_id BIGINT COMMENT '租户ID',
    INDEX idx_device_point (device_id, point_id),
    INDEX idx_collect_time (collect_time),
    INDEX idx_tenant (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='实时数据表';

-- ================================================
-- 历史数据表
-- ================================================
CREATE TABLE IF NOT EXISTS scada_history_data (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    device_id BIGINT NOT NULL COMMENT '设备ID',
    point_id BIGINT NOT NULL COMMENT '点位ID',
    point_code VARCHAR(100) COMMENT '点位编码',
    value DECIMAL(20,4) COMMENT '数值',
    quality TINYINT COMMENT '质量码',
    collect_time DATETIME NOT NULL COMMENT '采集时间',
    tenant_id BIGINT COMMENT '租户ID',
    INDEX idx_device_point (device_id, point_id),
    INDEX idx_collect_time (collect_time),
    INDEX idx_tenant (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='历史数据表';

-- ================================================
-- 报警记录表
-- ================================================
CREATE TABLE IF NOT EXISTS scada_alarm (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    alarm_code VARCHAR(100) NOT NULL COMMENT '报警编码',
    alarm_type VARCHAR(50) NOT NULL COMMENT '报警类型：HIGH/LOW/DEVICE/COMMUNICATION',
    alarm_level VARCHAR(20) COMMENT '报警级别：INFO/WARNING/ERROR/CRITICAL',
    device_id BIGINT COMMENT '设备ID',
    point_id BIGINT COMMENT '点位ID',
    point_code VARCHAR(100) COMMENT '点位编码',
    alarm_value DECIMAL(20,4) COMMENT '报警值',
    threshold_value DECIMAL(20,4) COMMENT '阈值',
    alarm_message VARCHAR(500) COMMENT '报警信息',
    alarm_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '报警时间',
    recover_time DATETIME COMMENT '恢复时间',
    status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE/RECOVERED/ACKNOWLEDGED',
    operator_id BIGINT COMMENT '处理人ID',
    operator_name VARCHAR(50) COMMENT '处理人姓名',
    handle_time DATETIME COMMENT '处理时间',
    handle_comment VARCHAR(500) COMMENT '处理意见',
    tenant_id BIGINT COMMENT '租户ID',
    INDEX idx_device (device_id),
    INDEX idx_alarm_time (alarm_time),
    INDEX idx_status (status),
    INDEX idx_tenant (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报警记录表';

-- ================================================
-- 设备维护记录表
-- ================================================
CREATE TABLE IF NOT EXISTS scada_maintenance (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    device_id BIGINT NOT NULL COMMENT '设备ID',
    maintenance_type VARCHAR(50) NOT NULL COMMENT '维护类型：PREVENTIVE/CORRECTIVE/PREDICTIVE',
    maintenance_plan_id BIGINT COMMENT '维护计划ID',
    maintenance_content TEXT COMMENT '维护内容',
    start_time DATETIME COMMENT '开始时间',
    end_time DATETIME COMMENT '结束时间',
    duration INT COMMENT '持续时间(小时)',
    maintainer_id BIGINT COMMENT '维护人ID',
    maintainer_name VARCHAR(50) COMMENT '维护人姓名',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态：PENDING/IN_PROGRESS/COMPLETED',
    cost DECIMAL(20,2) COMMENT '维护费用',
    remark VARCHAR(500) COMMENT '备注',
    tenant_id BIGINT COMMENT '租户ID',
    create_by VARCHAR(64) COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(64) COMMENT '更新人',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_device (device_id),
    INDEX idx_maintenance_time (start_time),
    INDEX idx_status (status),
    INDEX idx_tenant (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备维护记录表';

-- ================================================
-- 生产数据表
-- ================================================
CREATE TABLE IF NOT EXISTS scada_production_data (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    device_id BIGINT NOT NULL COMMENT '设备ID',
    production_batch VARCHAR(100) COMMENT '生产批次',
    product_code VARCHAR(50) COMMENT '产品编码',
    product_name VARCHAR(200) COMMENT '产品名称',
    production_count DECIMAL(20,4) COMMENT '生产数量',
    qualified_count DECIMAL(20,4) COMMENT '合格数量',
    defective_count DECIMAL(20,4) COMMENT '不合格数量',
    qualified_rate DECIMAL(10,4) COMMENT '合格率',
    start_time DATETIME COMMENT '开始时间',
    end_time DATETIME COMMENT '结束时间',
    duration INT COMMENT '生产时长(秒)',
    tenant_id BIGINT COMMENT '租户ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_device (device_id),
    INDEX idx_production_batch (production_batch),
    INDEX idx_start_time (start_time),
    INDEX idx_tenant (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='生产数据表';

-- ================================================
-- 设备状态统计表
-- ================================================
CREATE TABLE IF NOT EXISTS scada_device_statistics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    device_id BIGINT NOT NULL COMMENT '设备ID',
    stat_date DATE NOT NULL COMMENT '统计日期',
    run_time INT COMMENT '运行时间(秒)',
    idle_time INT COMMENT '空闲时间(秒)',
    fault_time INT COMMENT '故障时间(秒)',
    maintenance_time INT COMMENT '维护时间(秒)',
    production_count DECIMAL(20,4) COMMENT '生产数量',
    energy_consumption DECIMAL(20,4) COMMENT '能耗',
    availability DECIMAL(10,4) COMMENT '可用性',
    performance DECIMAL(10,4) COMMENT '性能',
    quality DECIMAL(10,4) COMMENT '质量',
    oee DECIMAL(10,4) COMMENT 'OEE(设备综合效率)',
    tenant_id BIGINT COMMENT '租户ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_device_date (device_id, stat_date),
    INDEX idx_stat_date (stat_date),
    INDEX idx_tenant (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备状态统计表';

-- 完成提示
SELECT 'forgex_scada数据库表结构创建完成！' AS message;
