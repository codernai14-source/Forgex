-- 供应商租户登录修复脚本
-- 适用库：forgex_admin / forgex_common
-- 说明：脚本可重复执行；用于补齐供应商生成租户后的初始化任务表、菜单复制规则、管理员账号、租户绑定、角色和系统菜单授权。

SET @script_user := '20260512_supplier_tenant_login_fix';
SET @default_password_hash := '$2a$10$D9IQgkg4SLm8tktsy75RY.KlJBOeN1d0.VZb1PWSlepMNqQmCTuGq';

USE `forgex_common`;

CREATE TABLE IF NOT EXISTS `sys_tenant_init_task` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    `tenant_id` bigint NOT NULL COMMENT '租户 ID',
    `tenant_name` varchar(100) NOT NULL COMMENT '租户名称',
    `tenant_type` varchar(50) NOT NULL COMMENT '租户类型',
    `status` varchar(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态：PENDING-等待中，RUNNING-进行中，SUCCESS-成功，FAILED-失败',
    `progress` int DEFAULT 0 COMMENT '进度百分比（0-100）',
    `current_step` varchar(200) DEFAULT NULL COMMENT '当前步骤描述',
    `error_message` text COMMENT '错误信息',
    `start_time` datetime DEFAULT NULL COMMENT '开始时间',
    `end_time` datetime DEFAULT NULL COMMENT '结束时间',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_tenant_id` (`tenant_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='租户初始化任务表';

USE `forgex_admin`;

CREATE TABLE IF NOT EXISTS `sys_tenant_menu_copy_rule` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    `tenant_type` varchar(50) NOT NULL COMMENT '租户类型编码',
    `perm_prefix` varchar(255) NOT NULL COMMENT '排除复制的权限前缀',
    `enabled` tinyint NOT NULL DEFAULT 1 COMMENT '是否启用：0=禁用，1=启用',
    `remark` varchar(500) DEFAULT NULL COMMENT '备注',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `update_by` varchar(50) DEFAULT NULL COMMENT '修改人',
    `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=未删除，1=已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_tenant_menu_copy_rule` (`tenant_type`, `perm_prefix`, `deleted`),
    KEY `idx_tenant_menu_copy_rule_type` (`tenant_type`, `enabled`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='租户菜单复制规则表';

INSERT INTO `sys_tenant_menu_copy_rule` (
    tenant_type, perm_prefix, enabled, remark, create_time, create_by, update_time, update_by, deleted
)
SELECT tenant_type, perm_prefix, 1, remark, NOW(), @script_user, NOW(), @script_user, 0
FROM (
    SELECT 'SUPPLIER_TENANT' AS tenant_type, 'sys:tenant' AS perm_prefix, CONVERT(0xE4BE9BE5BA94E59586E7A79FE688B7E4B88DE5A48DE588B6E7A79FE688B7E7AEA1E79086E79BB8E585B3E88F9CE58D95 USING utf8mb4) AS remark
    UNION ALL
    SELECT 'CUSTOMER_TENANT', 'sys:tenant', CONVERT(0xE5AEA2E688B7E7A79FE688B7E4B88DE5A48DE588B6E7A79FE688B7E7AEA1E79086E79BB8E585B3E88F9CE58D95 USING utf8mb4)
    UNION ALL
    SELECT 'PARTNER_TENANT', 'sys:tenant', CONVERT(0xE4BC99E4BCB4E7A79FE688B7E4B88DE5A48DE588B6E7A79FE688B7E7AEA1E79086E79BB8E585B3E88F9CE58D95 USING utf8mb4)
) seed
WHERE NOT EXISTS (
    SELECT 1
    FROM `sys_tenant_menu_copy_rule` existing
    WHERE existing.tenant_type = seed.tenant_type
      AND existing.perm_prefix = seed.perm_prefix
      AND existing.deleted = 0
);

DROP PROCEDURE IF EXISTS `repair_supplier_tenant_login`;

DELIMITER $$
CREATE PROCEDURE `repair_supplier_tenant_login`()
BEGIN
    DECLARE done int DEFAULT 0;
    DECLARE v_tenant_id bigint;
    DECLARE v_tenant_code varchar(100);
    DECLARE v_tenant_name varchar(100);
    DECLARE v_normalized_code varchar(100);
    DECLARE v_account varchar(50);
    DECLARE v_user_id bigint;
    DECLARE v_role_id bigint;
    DECLARE v_module_id bigint;
    DECLARE v_template_tenant_id bigint;
    DECLARE v_template_module_id bigint;

    DECLARE tenant_cur CURSOR FOR
        SELECT id, tenant_code, tenant_name
        FROM `sys_tenant`
        WHERE deleted = 0
          AND tenant_type = 'SUPPLIER_TENANT';

    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1;

    CREATE TEMPORARY TABLE IF NOT EXISTS `tmp_supplier_menu_map` (
        old_menu_id bigint NOT NULL PRIMARY KEY,
        new_menu_id bigint NOT NULL,
        KEY `idx_tmp_supplier_menu_map_new` (`new_menu_id`)
    ) ENGINE=MEMORY;

    CREATE TEMPORARY TABLE IF NOT EXISTS `tmp_supplier_menu_parent_map` (
        old_menu_id bigint NOT NULL PRIMARY KEY,
        new_menu_id bigint NOT NULL,
        KEY `idx_tmp_supplier_menu_parent_map_new` (`new_menu_id`)
    ) ENGINE=MEMORY;

    SET v_template_tenant_id := (
        SELECT id
        FROM `sys_tenant`
        WHERE deleted = 0
          AND tenant_type = 'MAIN_TENANT'
        ORDER BY id
        LIMIT 1
    );

    SET v_template_module_id := (
        SELECT id
        FROM `sys_module`
        WHERE tenant_id = v_template_tenant_id
          AND code = 'sys'
          AND deleted = 0
        ORDER BY id
        LIMIT 1
    );

    OPEN tenant_cur;

    tenant_loop: LOOP
        FETCH tenant_cur INTO v_tenant_id, v_tenant_code, v_tenant_name;
        IF done = 1 THEN
            LEAVE tenant_loop;
        END IF;

        SET v_normalized_code := LOWER(REGEXP_REPLACE(COALESCE(NULLIF(v_tenant_code, ''), 'tenant'), '[^a-zA-Z0-9]', ''));
        IF v_normalized_code IS NULL OR v_normalized_code = '' THEN
            SET v_normalized_code := 'tenant';
        END IF;
        SET v_normalized_code := LEFT(v_normalized_code, 39);
        SET v_account := CONCAT('admin_', v_normalized_code, '_', RIGHT(CAST(v_tenant_id AS char), 4));

        SET v_module_id := (
            SELECT id
            FROM `sys_module`
            WHERE tenant_id = v_tenant_id
              AND code = 'sys'
              AND deleted = 0
            ORDER BY id
            LIMIT 1
        );

        IF v_module_id IS NULL AND v_template_module_id IS NOT NULL THEN
            INSERT INTO `sys_module` (
                tenant_id, code, name, name_i18n_json, icon, order_num, visible, status,
                create_time, create_by, update_time, update_by, deleted
            )
            SELECT
                v_tenant_id, code, name, name_i18n_json, icon, order_num, visible, status,
                NOW(), @script_user, NOW(), @script_user, 0
            FROM `sys_module`
            WHERE id = v_template_module_id;

            SET v_module_id := LAST_INSERT_ID();
        END IF;

        IF v_module_id IS NOT NULL
           AND v_template_module_id IS NOT NULL
           AND NOT EXISTS (
               SELECT 1
               FROM `sys_menu`
               WHERE tenant_id = v_tenant_id
                 AND module_id = v_module_id
                 AND deleted = 0
               LIMIT 1
           ) THEN
            TRUNCATE TABLE `tmp_supplier_menu_map`;

            INSERT INTO `sys_menu` (
                tenant_id, tenant_type, module_id, parent_id, type, path, name, name_i18n_json,
                icon, component_key, perm_key, order_num, visible, status,
                create_time, create_by, update_time, update_by, deleted,
                menu_level, menu_mode, external_url
            )
            SELECT
                v_tenant_id,
                m.tenant_type,
                v_module_id,
                m.parent_id,
                m.type,
                m.path,
                m.name,
                m.name_i18n_json,
                m.icon,
                m.component_key,
                m.perm_key,
                m.order_num,
                m.visible,
                m.status,
                NOW(),
                CONCAT('src:', m.id),
                NOW(),
                @script_user,
                0,
                m.menu_level,
                m.menu_mode,
                m.external_url
            FROM `sys_menu` m
            WHERE m.tenant_id = v_template_tenant_id
              AND m.module_id = v_template_module_id
              AND m.deleted = 0
              AND (
                  m.tenant_type IS NULL
                  OR TRIM(m.tenant_type) = ''
                  OR m.tenant_type = 'PUBLIC'
                  OR m.tenant_type = 'SUPPLIER_TENANT'
              )
              AND NOT EXISTS (
                  SELECT 1
                  FROM `sys_tenant_menu_copy_rule` rule_cfg
                  WHERE rule_cfg.tenant_type = 'SUPPLIER_TENANT'
                    AND rule_cfg.enabled = 1
                    AND rule_cfg.deleted = 0
                    AND m.perm_key IS NOT NULL
                    AND m.perm_key LIKE CONCAT(rule_cfg.perm_prefix, '%')
              );

        END IF;

        TRUNCATE TABLE `tmp_supplier_menu_map`;
        TRUNCATE TABLE `tmp_supplier_menu_parent_map`;

        INSERT INTO `tmp_supplier_menu_map` (old_menu_id, new_menu_id)
        SELECT CAST(SUBSTRING(create_by, 5) AS unsigned), id
        FROM `sys_menu`
        WHERE tenant_id = v_tenant_id
          AND module_id = v_module_id
          AND create_by LIKE 'src:%';

        INSERT INTO `tmp_supplier_menu_parent_map` (old_menu_id, new_menu_id)
        SELECT old_menu_id, new_menu_id
        FROM `tmp_supplier_menu_map`;

        UPDATE `sys_menu` new_menu
        JOIN `tmp_supplier_menu_map` map_self ON map_self.new_menu_id = new_menu.id
        JOIN `sys_menu` template_menu ON template_menu.id = map_self.old_menu_id
        LEFT JOIN `tmp_supplier_menu_parent_map` map_parent ON map_parent.old_menu_id = template_menu.parent_id
        SET new_menu.parent_id = CASE
                WHEN template_menu.parent_id IS NULL OR template_menu.parent_id = 0 THEN template_menu.parent_id
                ELSE map_parent.new_menu_id
            END,
            new_menu.create_by = @script_user,
            new_menu.update_by = @script_user,
            new_menu.update_time = NOW()
        WHERE new_menu.tenant_id = v_tenant_id
          AND new_menu.module_id = v_module_id;

        SET v_role_id := (
            SELECT id
            FROM `sys_role`
            WHERE tenant_id = v_tenant_id
              AND role_key = 'admin'
              AND deleted = 0
            ORDER BY id
            LIMIT 1
        );

        IF v_role_id IS NULL THEN
            INSERT INTO `sys_role` (
                role_name, role_key, description, status, data_scope, tenant_id,
                create_time, create_by, update_time, update_by, deleted
            )
            VALUES (
                CONVERT(0xE7B3BBE7BB9FE7AEA1E79086E59198 USING utf8mb4), 'admin', CONVERT(0xE7B3BBE7BB9FE7AEA1E79086E59198EFBC8CE68BA5E69C89E5BD93E5898DE4BE9BE5BA94E59586E7A79FE688B7E69D83E99990 USING utf8mb4), 1, 'ALL', v_tenant_id,
                NOW(), @script_user, NOW(), @script_user, 0
            );
            SET v_role_id := LAST_INSERT_ID();
        END IF;

        SET v_user_id := (
            SELECT id
            FROM `sys_user`
            WHERE account = v_account
              AND deleted = 0
            ORDER BY id
            LIMIT 1
        );

        IF v_user_id IS NULL THEN
            INSERT INTO `sys_user` (
                account, username, password, email, status,
                tenant_id, user_source, create_time, create_by, update_time, update_by, deleted
            )
            VALUES (
                v_account, CONVERT(0xE7B3BBE7BB9FE7AEA1E79086E59198 USING utf8mb4), @default_password_hash, CONCAT(v_account, '@tenant.local'), 1,
                v_tenant_id, 1, NOW(), @script_user, NOW(), @script_user, 0
            );
            SET v_user_id := LAST_INSERT_ID();
        ELSE
            UPDATE `sys_user`
            SET tenant_id = v_tenant_id,
                status = 1,
                update_by = @script_user,
                update_time = NOW()
            WHERE id = v_user_id;
        END IF;

        INSERT INTO `sys_user_tenant` (user_id, tenant_id, pref_order, is_default, last_used)
        SELECT v_user_id, v_tenant_id, 1, 1, NOW()
        WHERE NOT EXISTS (
            SELECT 1
            FROM `sys_user_tenant`
            WHERE user_id = v_user_id
              AND tenant_id = v_tenant_id
        );

        INSERT INTO `sys_user_role` (user_id, role_id, tenant_id)
        SELECT v_user_id, v_role_id, v_tenant_id
        WHERE NOT EXISTS (
            SELECT 1
            FROM `sys_user_role`
            WHERE user_id = v_user_id
              AND role_id = v_role_id
              AND tenant_id = v_tenant_id
        );

        IF v_module_id IS NOT NULL THEN
            INSERT INTO `sys_role_menu` (tenant_id, role_id, menu_id)
            SELECT v_tenant_id, v_role_id, menu.id
            FROM `sys_menu` menu
            WHERE menu.tenant_id = v_tenant_id
              AND menu.module_id = v_module_id
              AND menu.deleted = 0
              AND NOT EXISTS (
                  SELECT 1
                  FROM `sys_role_menu` existing
                  WHERE existing.tenant_id = v_tenant_id
                    AND existing.role_id = v_role_id
                    AND existing.menu_id = menu.id
              );
        END IF;

        INSERT INTO `forgex_common`.`sys_tenant_init_task` (
            tenant_id, tenant_name, tenant_type, status, progress, current_step,
            error_message, start_time, end_time, create_time, update_time
        )
        SELECT
            v_tenant_id, v_tenant_name, 'SUPPLIER_TENANT', 'SUCCESS', 100, CONVERT(0xE4BE9BE5BA94E59586E7A79FE688B7E799BBE5BD95E4BFAEE5A48DE5AE8CE68890 USING utf8mb4),
            NULL, NOW(), NOW(), NOW(), NOW()
        WHERE NOT EXISTS (
            SELECT 1
            FROM `forgex_common`.`sys_tenant_init_task`
            WHERE tenant_id = v_tenant_id
        );

        UPDATE `forgex_common`.`sys_tenant_init_task`
        SET status = 'SUCCESS',
            progress = 100,
            current_step = CONVERT(0xE4BE9BE5BA94E59586E7A79FE688B7E799BBE5BD95E4BFAEE5A48DE5AE8CE68890 USING utf8mb4),
            error_message = NULL,
            end_time = NOW(),
            update_time = NOW()
        WHERE tenant_id = v_tenant_id
          AND status <> 'SUCCESS';
    END LOOP;

    CLOSE tenant_cur;
    DROP TEMPORARY TABLE IF EXISTS `tmp_supplier_menu_parent_map`;
    DROP TEMPORARY TABLE IF EXISTS `tmp_supplier_menu_map`;
END$$
DELIMITER ;

CALL `repair_supplier_tenant_login`();

DROP PROCEDURE IF EXISTS `repair_supplier_tenant_login`;

SELECT
    t.id AS tenant_id,
    t.tenant_code,
    t.tenant_name,
    u.id AS user_id,
    u.account,
    u.status,
    ut.id AS user_tenant_bind_id,
    r.id AS role_id,
    COUNT(DISTINCT rm.menu_id) AS granted_menu_count
FROM `sys_tenant` t
LEFT JOIN `sys_user` u
    ON u.tenant_id = t.id
   AND u.deleted = 0
   AND u.account = CONCAT(
       'admin_',
       LEFT(
           CASE
               WHEN LOWER(REGEXP_REPLACE(COALESCE(NULLIF(t.tenant_code, ''), 'tenant'), '[^a-zA-Z0-9]', '')) = '' THEN 'tenant'
               ELSE LOWER(REGEXP_REPLACE(COALESCE(NULLIF(t.tenant_code, ''), 'tenant'), '[^a-zA-Z0-9]', ''))
           END,
           39
       ),
       '_',
       RIGHT(CAST(t.id AS char), 4)
   )
LEFT JOIN `sys_user_tenant` ut
    ON ut.user_id = u.id
   AND ut.tenant_id = t.id
LEFT JOIN `sys_role` r
    ON r.tenant_id = t.id
   AND r.role_key = 'admin'
   AND r.deleted = 0
LEFT JOIN `sys_role_menu` rm
    ON rm.tenant_id = t.id
   AND rm.role_id = r.id
WHERE t.deleted = 0
  AND t.tenant_type = 'SUPPLIER_TENANT'
GROUP BY t.id, t.tenant_code, t.tenant_name, u.id, u.account, u.status, ut.id, r.id
ORDER BY t.id;
