-- Fix missing user_source dictionary data for public and enabled tenants.
-- The script copies labels/i18n JSON from an existing healthy tenant row, so it
-- avoids client encoding issues when executed from Windows PowerShell.

USE forgex_admin;

START TRANSACTION;

SET @source_tenant_id := (
  SELECT tenant_id
  FROM sys_dict
  WHERE deleted = 0
    AND node_path = 'user_source'
  ORDER BY CASE WHEN tenant_id <> 0 THEN 0 ELSE 1 END, tenant_id
  LIMIT 1
);

CREATE TEMPORARY TABLE tmp_user_source_tenants (
  tenant_id BIGINT NOT NULL PRIMARY KEY
) ENGINE=Memory;

INSERT IGNORE INTO tmp_user_source_tenants (tenant_id)
VALUES (0);

INSERT IGNORE INTO tmp_user_source_tenants (tenant_id)
SELECT t.id
FROM sys_tenant t
WHERE t.deleted = 0
  AND t.status = 1;

INSERT INTO sys_dict (
  parent_id,
  dict_name,
  dict_code,
  module_id,
  dict_value,
  dict_value_i18n_json,
  node_path,
  level,
  children_count,
  order_num,
  status,
  remark,
  tenant_id,
  create_by,
  create_time,
  update_by,
  update_time,
  deleted,
  tag_style_json
)
SELECT
  0,
  src.dict_name,
  src.dict_code,
  src.module_id,
  src.dict_value,
  src.dict_value_i18n_json,
  src.node_path,
  src.level,
  src.children_count,
  src.order_num,
  src.status,
  src.remark,
  target.tenant_id,
  src.create_by,
  NOW(),
  src.update_by,
  NOW(),
  0,
  src.tag_style_json
FROM tmp_user_source_tenants target
JOIN sys_dict src
  ON src.tenant_id = @source_tenant_id
 AND src.deleted = 0
 AND src.node_path = 'user_source'
WHERE NOT EXISTS (
  SELECT 1
  FROM sys_dict exists_root
  WHERE exists_root.tenant_id = target.tenant_id
    AND exists_root.node_path = 'user_source'
    AND exists_root.deleted = 0
);

INSERT INTO sys_dict (
  parent_id,
  dict_name,
  dict_code,
  module_id,
  dict_value,
  dict_value_i18n_json,
  node_path,
  level,
  children_count,
  order_num,
  status,
  remark,
  tenant_id,
  create_by,
  create_time,
  update_by,
  update_time,
  deleted,
  tag_style_json
)
SELECT
  target_root.id,
  src_child.dict_name,
  src_child.dict_code,
  src_child.module_id,
  src_child.dict_value,
  src_child.dict_value_i18n_json,
  src_child.node_path,
  src_child.level,
  src_child.children_count,
  src_child.order_num,
  src_child.status,
  src_child.remark,
  target_root.tenant_id,
  src_child.create_by,
  NOW(),
  src_child.update_by,
  NOW(),
  0,
  src_child.tag_style_json
FROM tmp_user_source_tenants target
JOIN sys_dict target_root
  ON target_root.tenant_id = target.tenant_id
 AND target_root.deleted = 0
 AND target_root.node_path = 'user_source'
JOIN sys_dict src_child
  ON src_child.tenant_id = @source_tenant_id
 AND src_child.deleted = 0
 AND src_child.node_path LIKE 'user_source/%'
WHERE NOT EXISTS (
  SELECT 1
  FROM sys_dict exists_child
  WHERE exists_child.tenant_id = target_root.tenant_id
    AND exists_child.node_path = src_child.node_path
    AND exists_child.deleted = 0
);

UPDATE sys_dict root
JOIN (
  SELECT
    root_inner.id,
    COUNT(child.id) AS child_count
  FROM sys_dict root_inner
  JOIN tmp_user_source_tenants target
    ON target.tenant_id = root_inner.tenant_id
  LEFT JOIN sys_dict child
    ON child.parent_id = root_inner.id
   AND child.deleted = 0
  WHERE root_inner.deleted = 0
    AND root_inner.node_path = 'user_source'
  GROUP BY root_inner.id
) stat
  ON stat.id = root.id
SET root.children_count = stat.child_count;

DROP TEMPORARY TABLE IF EXISTS tmp_user_source_tenants;

COMMIT;

SELECT
  tenant_id,
  COUNT(*) AS total,
  SUM(node_path = 'user_source') AS roots,
  SUM(parent_id <> 0) AS children
FROM sys_dict
WHERE deleted = 0
  AND (node_path = 'user_source' OR node_path LIKE 'user_source/%')
GROUP BY tenant_id
ORDER BY tenant_id;
