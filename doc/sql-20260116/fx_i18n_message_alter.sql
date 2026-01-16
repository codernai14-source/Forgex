-- ================================================
-- 修改返回消息表结构，移除租户ID字段
-- ================================================

USE forgex_common;

-- 删除旧表（如果存在）
DROP TABLE IF EXISTS fx_i18n_message;

-- ================================================
-- 返回消息国际化配置表（单表承载多模块，跳过租户隔离）
-- ================================================
CREATE TABLE IF NOT EXISTS fx_i18n_message (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    module VARCHAR(64) NOT NULL COMMENT '模块标识',
    prompt_code VARCHAR(128) NOT NULL COMMENT '消息模板编号',
    text_i18n_json TEXT NOT NULL COMMENT '文本国际化(JSON)',
    enabled TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用：1启用 0禁用',
    version INT NOT NULL DEFAULT 1 COMMENT '版本',
    create_by VARCHAR(64) COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(64) COMMENT '修改人',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除',
    UNIQUE KEY uk_i18n_msg (module, prompt_code, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='返回消息国际化配置表';

SELECT '返回消息表结构修改完成！' AS message;
