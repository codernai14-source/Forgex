-- ================================================
-- 多语言类型配置表
-- ================================================
CREATE TABLE IF NOT EXISTS fx_i18n_language_type (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    lang_code VARCHAR(20) NOT NULL UNIQUE COMMENT '语言代码，例如：zh-CN, en-US',
    lang_name VARCHAR(100) NOT NULL COMMENT '语言名称，例如：简体中文, English',
    lang_name_en VARCHAR(100) COMMENT '语言英文名称',
    icon VARCHAR(50) COMMENT '语言图标',
    order_num INT NOT NULL DEFAULT 0 COMMENT '排序号',
    enabled TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用：1启用 0禁用',
    is_default TINYINT NOT NULL DEFAULT 0 COMMENT '是否默认语言：1默认 0非默认',
    create_by VARCHAR(64) COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(64) COMMENT '修改人',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='多语言类型配置表';

-- ================================================
-- 初始化多语言类型数据
-- ================================================
INSERT INTO fx_i18n_language_type (lang_code, lang_name, lang_name_en, icon, order_num, enabled, is_default, create_by) VALUES
('zh-CN', '简体中文', 'Simplified Chinese', '🇨🇳', 1, 1, 1, 'system'),
('en-US', 'English', 'English', '🇺🇸', 2, 1, 0, 'system'),
('zh-TW', '繁體中文', 'Traditional Chinese', '🇹🇼', 3, 1, 0, 'system'),
('ja-JP', '日本語', 'Japanese', '🇯🇵', 4, 1, 0, 'system'),
('ko-KR', '한국어', 'Korean', '🇰🇷', 5, 1, 0, 'system');

SELECT '多语言类型配置表创建完成！' AS message;
