-- Integration platform homepage component catalog.
-- Idempotent script: safe to run repeatedly.

USE `forgex_admin`;

SET @script_user := 'codex';
SET @now := NOW();

INSERT INTO `sys_homepage_component_category`
(`tenant_id`,`category_code`,`category_name`,`module_code`,`remark`,`create_time`,`update_time`,`create_by`,`update_by`,`deleted`)
SELECT 0, 'integration_dashboard', 'Integration Dashboard', 'integration',
       'Integration platform homepage dashboard widgets', @now, @now, @script_user, @script_user, 0
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_homepage_component_category` c
    WHERE c.tenant_id = 0
      AND c.module_code = 'integration'
      AND c.category_code = 'integration_dashboard'
      AND c.deleted = 0
);

SET @integration_component_category_id := (
    SELECT id FROM `sys_homepage_component_category`
    WHERE tenant_id = 0
      AND module_code = 'integration'
      AND category_code = 'integration_dashboard'
      AND deleted = 0
    ORDER BY id ASC
    LIMIT 1
);

INSERT INTO `sys_homepage_component_config`
(`tenant_id`,`category_id`,`scope_level`,`component_code`,`component_name`,`component_path`,`icon`,`use_desc`,`default_params_json`,`enabled`,`order_num`,`remark`,`create_time`,`update_time`,`create_by`,`update_by`,`deleted`)
SELECT 0, @integration_component_category_id, 'PUBLIC', seed.component_code, seed.component_name, seed.component_path,
       seed.icon, seed.use_desc, '{}', 1, seed.order_num, seed.remark, @now, @now, @script_user, @script_user, 0
FROM (
    SELECT 'integrationSummary' AS component_code, 'API Capability Summary' AS component_name, 'integrationSummary' AS component_path,
           'DashboardOutlined' AS icon, 'Third-party systems, external APIs, today calls, and success rate' AS use_desc,
           10 AS order_num, 'Integration homepage capability summary widget' AS remark
    UNION ALL SELECT 'integrationStatusComparison', 'Success Fail Comparison', 'integrationStatusComparison',
           'BarChartOutlined', 'Success and failed call comparison',
           20, 'Integration homepage success/fail comparison chart'
    UNION ALL SELECT 'integrationStatusPie', 'Call Status Share', 'integrationStatusPie',
           'PieChartOutlined', 'Call status distribution chart',
           30, 'Integration homepage status share chart'
    UNION ALL SELECT 'integrationCallTrend', 'Call Trend', 'integrationCallTrend',
           'LineChartOutlined', 'Last 14 days call count trend',
           40, 'Integration homepage call trend chart'
    UNION ALL SELECT 'integrationTopApis', 'Top APIs', 'integrationTopApis',
           'OrderedListOutlined', 'High-frequency API ranking',
           50, 'Integration homepage top APIs widget'
    UNION ALL SELECT 'integrationRecentFailures', 'Recent Failed Calls', 'integrationRecentFailures',
           'WarningOutlined', 'Recent failed calls and error messages',
           60, 'Integration homepage recent failures widget'
) seed
WHERE @integration_component_category_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM `sys_homepage_component_config` c
      WHERE c.scope_level = 'PUBLIC'
        AND c.tenant_id = 0
        AND c.component_code = seed.component_code
        AND c.deleted = 0
  );

UPDATE `sys_homepage_component_config` c
JOIN (
    SELECT 'integrationSummary' AS component_code, 'API Capability Summary' AS component_name, 'integrationSummary' AS component_path,
           'DashboardOutlined' AS icon, 'Third-party systems, external APIs, today calls, and success rate' AS use_desc,
           10 AS order_num, 'Integration homepage capability summary widget' AS remark
    UNION ALL SELECT 'integrationStatusComparison', 'Success Fail Comparison', 'integrationStatusComparison',
           'BarChartOutlined', 'Success and failed call comparison',
           20, 'Integration homepage success/fail comparison chart'
    UNION ALL SELECT 'integrationStatusPie', 'Call Status Share', 'integrationStatusPie',
           'PieChartOutlined', 'Call status distribution chart',
           30, 'Integration homepage status share chart'
    UNION ALL SELECT 'integrationCallTrend', 'Call Trend', 'integrationCallTrend',
           'LineChartOutlined', 'Last 14 days call count trend',
           40, 'Integration homepage call trend chart'
    UNION ALL SELECT 'integrationTopApis', 'Top APIs', 'integrationTopApis',
           'OrderedListOutlined', 'High-frequency API ranking',
           50, 'Integration homepage top APIs widget'
    UNION ALL SELECT 'integrationRecentFailures', 'Recent Failed Calls', 'integrationRecentFailures',
           'WarningOutlined', 'Recent failed calls and error messages',
           60, 'Integration homepage recent failures widget'
) seed ON seed.component_code = c.component_code
SET c.category_id = @integration_component_category_id,
    c.component_name = seed.component_name,
    c.component_path = seed.component_path,
    c.icon = seed.icon,
    c.use_desc = seed.use_desc,
    c.order_num = seed.order_num,
    c.remark = seed.remark,
    c.enabled = 1,
    c.update_time = @now,
    c.update_by = @script_user
WHERE @integration_component_category_id IS NOT NULL
  AND c.scope_level = 'PUBLIC'
  AND c.tenant_id = 0
  AND c.deleted = 0;
