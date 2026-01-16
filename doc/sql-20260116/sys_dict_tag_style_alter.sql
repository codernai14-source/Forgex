-- 字典标签样式配置字段
-- 在sys_dict表中新增tag_style_json字段，用于存储标签样式配置
-- 支持配置：color（颜色）、icon（图标）

ALTER TABLE `sys_dict` 
ADD COLUMN `tag_style_json` TEXT COMMENT '标签样式配置JSON，格式：{"color":"success","icon":"CheckCircleOutlined"}' AFTER `dict_value_i18n_json`;
