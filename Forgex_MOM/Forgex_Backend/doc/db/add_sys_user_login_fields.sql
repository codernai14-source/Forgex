-- 补充 sys_user 表缺失的最后登录信息字段
-- 执行此脚本前请确保已连接到 forgex_admin 数据库
-- 此脚本使用存储过程来安全地添加字段，兼容MySQL 5.7+

USE forgex_admin;

-- 创建存储过程：如果字段不存在则添加
DELIMITER $$

DROP PROCEDURE IF EXISTS add_column_if_not_exists$$

CREATE PROCEDURE add_column_if_not_exists(
    IN table_name VARCHAR(255),
    IN column_name VARCHAR(255),
    IN column_definition VARCHAR(500)
)
BEGIN
    DECLARE column_count INT;
    
    SELECT COUNT(*) INTO column_count 
    FROM information_schema.columns 
    WHERE table_schema = DATABASE() 
    AND table_name = table_name 
    AND column_name = column_name;
    
    IF column_count = 0 THEN
        SET @sql = CONCAT('ALTER TABLE ', table_name, ' ADD COLUMN ', column_name, ' ', column_definition);
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
        SELECT CONCAT('已添加字段: ', table_name, '.', column_name) AS result;
    ELSE
        SELECT CONCAT('字段已存在，跳过: ', table_name, '.', column_name) AS result;
    END IF;
END$$

DELIMITER ;

-- 调用存储过程添加缺失的字段
CALL add_column_if_not_exists('sys_user', 'avatar', 'VARCHAR(255) NULL COMMENT ''头像URL''');
CALL add_column_if_not_exists('sys_user', 'last_login_ip', 'VARCHAR(64) NULL COMMENT ''最后登录IP''');
CALL add_column_if_not_exists('sys_user', 'last_login_region', 'VARCHAR(255) NULL COMMENT ''最后登录地区''');
CALL add_column_if_not_exists('sys_user', 'last_login_time', 'DATETIME NULL COMMENT ''最后登录时间''');
CALL add_column_if_not_exists('sys_user', 'create_by', 'VARCHAR(50) NULL COMMENT ''创建人''');
CALL add_column_if_not_exists('sys_user', 'update_by', 'VARCHAR(50) NULL COMMENT ''更新人''');

-- 删除临时存储过程
DROP PROCEDURE IF EXISTS add_column_if_not_exists;

SELECT 'sys_user表字段补充完成！' AS message;
