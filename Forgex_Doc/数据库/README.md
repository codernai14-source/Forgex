# 数据库文档导航

> 版本：**预览版 V0.5.0**  
> 更新时间：**2026-04-13**

本目录用于沉淀 Forgex 的数据库设计规范、SQL 脚本导航与排查/修复资料入口。

## 一、推荐先读

1. [数据库字段统一规范文档](../../doc/开发规范/规范文档/数据库字段统一规范文档.md)
2. [项目架构设计文档](../开发规范/架构设计/项目架构设计文档.md)
3. 本页中的 SQL 脚本索引

## 二、当前数据库资料来源

| 位置 | 说明 |
|---|---|
| `doc/sql/` | 当前已整理的数据库脚本、诊断脚本、修复脚本 |
| `doc/过程文件/数据库文件/` | 过程资料与阶段性数据库文件 |
| `doc/开发规范/规范文档/数据库字段统一规范文档.md` | 统一字段与规范口径 |

## 三、脚本索引

### 3.1 `doc/sql`

| 脚本 | 说明 |
|---|---|
| [20260413_add_common_prompt_i18n_configs.sql](../../doc/sql/20260413_add_common_prompt_i18n_configs.sql) | 补充公共提示国际化配置 |
| [20260413_check_workflow_and_encode.sql](../../doc/sql/20260413_check_workflow_and_encode.sql) | 工作流与编码规则检查 |
| [20260413_create_kms_tables.sql](../../doc/sql/20260413_create_kms_tables.sql) | KMS 相关表创建 |
| [20260413_diagnose_dict_tag_style.sql](../../doc/sql/20260413_diagnose_dict_tag_style.sql) | 字典标签样式问题诊断 |
| [20260413_diagnose_frontend_garbled_config.sql](../../doc/sql/20260413_diagnose_frontend_garbled_config.sql) | 前端乱码配置诊断 |
| [20260413_fix_common_prompt_i18n_missing.sql](../../doc/sql/20260413_fix_common_prompt_i18n_missing.sql) | 公共提示国际化缺失修复 |
| [20260413_fix_controller_return_messages.sql](../../doc/sql/20260413_fix_controller_return_messages.sql) | 控制器返回消息修复相关脚本 |
| [20260413_fix_dict_tag_style.sql](../../doc/sql/20260413_fix_dict_tag_style.sql) | 字典标签样式修复 |
| [20260413_fix_frontend_garbled_config.sql](../../doc/sql/20260413_fix_frontend_garbled_config.sql) | 前端乱码配置修复 |
| [20260413_fix_workflow_status_i18n_and_tag_style.sql](../../doc/sql/20260413_fix_workflow_status_i18n_and_tag_style.sql) | 工作流状态国际化与标签样式修复 |

### 3.2 过程文件中的相关资料

| 文件 | 说明 |
|---|---|
| [20260413_check_workflow_and_encode_i18n_diagnose.sql](../../doc/过程文件/20260413_check_workflow_and_encode_i18n_diagnose.sql) | 联合诊断脚本 |
| [20260413_fix_workflow_and_encode_i18n.sql](../../doc/过程文件/20260413_fix_workflow_and_encode_i18n.sql) | 工作流与编码规则国际化修复 |
| [20260413_fix_controller_return_messages_guide.md](../../doc/过程文件/20260413_fix_controller_return_messages_guide.md) | 控制器返回消息修复说明 |

## 四、数据库设计重点

| 主题 | 说明 |
|---|---|
| 租户字段 | 所有租户业务表统一使用 `tenant_id` |
| 通用审计字段 | `create_time`、`update_time`、`create_by`、`update_by`、`deleted` |
| 国际化字段 | 名称/标题类字段优先使用 JSON 或长文本承载多语言配置 |
| 配置中心表 | 动态表格、字典、国际化、模板消息等配置型能力使用独立配置表 |

## 五、后续补充建议

1. 后续将各核心业务表的设计文档逐步补到本目录。
2. 新增正式建表/升级脚本时，建议同步更新本页索引。
3. 涉及跨模块修复的 SQL，建议同时补充“诊断脚本 + 修复脚本 + 说明文档”三件套。

