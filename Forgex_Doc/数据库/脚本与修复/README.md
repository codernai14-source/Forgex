# 数据库 · 脚本与修复

> 版本：**V0.6.0**
> 更新时间：**2026-04-13**

本分类聚焦数据库初始化、诊断、修复脚本的阅读与使用入口。

## 一、主入口

- [数据库文档导航](../README.md)
- [数据库安全与配置](../安全与配置/README.md)

## 二、当前脚本来源

| 位置 | 说明 |
|---|---|
| `doc/sql/` | 初始化、诊断、修复脚本主目录 |
| `doc/过程文件/` | 修复说明、阶段性 SQL 与问题排查资料 |

## 三、推荐阅读方式

1. 先在 [数据库文档导航](../README.md) 查看脚本索引
2. 再按问题类型进入具体 SQL 或说明文档
3. 涉及数据库加密时继续阅读 [TDE 透明加密镜像页](../安全与配置/tde.md)

## 四、关联文档

- [数据库安全与配置](../安全与配置/README.md)
- [模块文档映射](../../开发规范/模块文档映射/README.md)

## 五、近期修复脚本

| 脚本 | 说明 |
|---|---|
| `doc/sql/20260429_basic_homepage_module_homepage_guide.sql` | 基础信息主页一级菜单、客户/物料菜单权限、模块首页配置、系统管理菜单打开次数表与管理员授权修复 |
| `doc/sql/20260429_fix_basic_info_material_customer_encode_rule.sql` | 删除空基础信息目录关系，合并物料菜单为单入口，补齐客户主数据字段和子表，新增 `CUSTOMER_CODE` 公共编码规则，并补齐 `CustomerMasterTable`、`MaterialTable` 公共表格配置 |
| `doc/sql/20260430_fix_invite_encode_codegen_unit.sql` | 补齐 `InviteCodeTable` 公共表格配置、编码规则明细字段、代码生成本地数据源和 `basic_unit` 计量单位表 |
| `Forgex_Doc/数据库/脚本与修复/20260501_common_import_upgrade.sql` | 公共导入升级脚本，补齐导入配置处理器 Bean、导入权限、多 Sheet 字段，初始化用户、供应商、语言类型导入配置，并写入 Excel 导入提示五语言国际化文案 |
| `Forgex_Doc/数据库/脚本与修复/20260502_basic_unit_and_table_upgrade.sql` | 计量单位三表升级脚本，在 `forgex_admin` 新增 `basic_unit_type`、`basic_unit`、`basic_unit_conversion`，兼容旧版 `basic_unit` 单表并补齐 `unit_type_id` 与默认类型，在 `forgex_common` 初始化 `BasicUnitTable` 公共表格配置，补齐计量单位菜单、按钮权限、管理员授权、页面文案和后端返回消息 |
