# 数据库 · 初始化与升级说明

> 版本：**V0.6.5**
> 更新时间：**2026-04-13**

本分类只说明正式交付中的数据库初始化与升级执行原则。开发过程中的阶段性 SQL、诊断脚本和排查资料统一放在 `doc/` 目录，不放入 `Forgex_Doc` 文档中心。

## 一、主入口

- [数据库文档导航](../README.md)
- [数据库安全与配置](../安全与配置/README.md)

## 二、正式脚本来源

| 位置 | 说明 |
|---|---|
| `Forgex_Doc/部署/数据库初始化脚本/` | 首次部署使用的数据库初始化脚本说明 |
| `database-upgrade/` | 交付包内的数据库升级 SQL 目录 |
| `doc/sql/upgrade/` | 构建升级包时收集 `database-upgrade/` 的源码目录 |

## 三、专项修复脚本

| 脚本 | 目标库 | 说明 |
|---|---|---|
| `20260508_i18n_five_language_repair.sql` | `forgex_admin`、`forgex_common` | 幂等补齐菜单、字典、动态表格、提示消息相关 JSON 字段的五语言键；不新增表结构，执行前按脚本头部说明导出备份。 |
| `20260512_material_import_sync_upgrade.sql` | `forgex_common`、`forgex_admin`、`forgex_integration` | 幂等补齐物料公共导入模板、附属字段结构表 `basic_material_extend_schema`、附属字段配置权限、物料导入/第三方拉取/第三方同步按钮权限，以及 `basic_material_sync`、`basic_material_pull` 默认接口编码。 |
| `20260512_supplier_tenant_login_fix.sql` | `forgex_common`、`forgex_admin` | 幂等补齐供应商生成租户后的初始化任务表、菜单复制规则、管理员账号、`sys_user_tenant` 租户绑定、管理员角色和系统菜单授权；用于修复已生成但无法登录的供应商租户。 |
| `20260515_label_template_refactor.sql` | `forgex_admin`、`forgex_common` | 幂等补齐标签类型、标签字段、标签模板主表、标签模板详情表、导入配置、菜单与权限；用于支撑标签模板重构与打印渲染链路。 |
| `20260515_label_menu_permission_seed.sql` | `forgex_admin` | 幂等补齐标签类型、标签字段菜单，以及标签模块当前后端权限、默认管理员菜单/权限授权。 |

## 四、推荐阅读方式

1. 首次部署先阅读 [部署文档](../../部署/README.md) 和数据库初始化说明
2. 已有环境升级时，先查看交付包内 `database-upgrade/README.md`
3. 涉及数据库加密时继续阅读 [TDE 透明加密镜像页](../安全与配置/tde.md)

## 五、关联文档

- [数据库安全与配置](../安全与配置/README.md)
- [模块文档映射](../../开发规范/模块文档映射/README.md)

## 六、升级脚本执行原则

- 升级前必须备份目标库，并确认当前环境、租户和命名空间。
- 按 SQL 文件名中的日期或交付说明顺序执行，不跨版本跳过依赖脚本。
- 同一个脚本重复执行前，先阅读脚本内的幂等判断和影响范围。
- 应用升级脚本不会自动执行 SQL，数据库升级由现场运维或实施人员确认后手工执行。
