# 前端文档导航

> 版本：**V0.6.6**  
> 更新时间：**2026-05-17**

本目录聚焦 Forgex Web 管理端的公共能力、核心组件、页面接入方式与专题文档。

**唯一推荐入口**：本 `README.md`（历史文件 [前端公共能力与核心功能手册](./前端公共能力与核心功能手册.md) 已重定向至此，请勿在手册文件中追加正文）。

## 架构说明（精要）

Web 管理端基于 **Vue 3 + TypeScript + Vite + Ant Design Vue**。与《项目架构设计文档》对齐的分层如下：

| 层级 | 路径 | 职责 |
|---|---|---|
| API | `Forgex_MOM/Forgex_Fronted/src/api` | 统一经 `http.ts` 封装请求、错误提示与鉴权头 |
| 公共组件 | `Forgex_MOM/Forgex_Fronted/src/components/common` | 动态表格、弹窗、导入、图标选择器等 |
| 业务页面 | `Forgex_MOM/Forgex_Fronted/src/views` | 系统管理、基础数据、工作流、报表等领域页面 |
| 状态 | `Forgex_MOM/Forgex_Fronted/src/stores` | Pinia Store（用户、标签、主题等） |
| 国际化 | `Forgex_MOM/Forgex_Fronted/src/locales` | `vue-i18n` 文案与异步语言包 |
| 路由 | `Forgex_MOM/Forgex_Fronted/src/router` | 静态底座路由 + 登录后动态菜单注入 |
| 样式 | `Forgex_MOM/Forgex_Fronted/src/styles`、`src/theme` | Less 视图样式与主题 Token |

**完整说明**：[项目架构设计文档 - 五、前端架构分层](../开发规范/架构设计/项目架构设计文档.md#五前端架构分层)。

## 分类目录（辅链）

| 分类 | 说明 |
|---|---|
| [请求与反馈](./请求与反馈/README.md) | HTTP、消息提示、弹窗容器 |
| [配置驱动页面](./配置驱动页面/README.md) | FxDynamicTable、列设置 |
| [组件与页面](./组件与页面/README.md) | 业务通用组件与页面专题 |
| [国际化与布局](./国际化与布局/README.md) | 多语言、个人首页布局 |
| [基础设施](./基础设施/README.md) | 状态、路由菜单、权限、主题、表单校验 |

## 功能清单

| 功能 | 主要入口 | 实现逻辑 | 使用方式 |
|---|---|---|---|
| HTTP 请求与消息提示 | `src/api/http.ts` | [实现逻辑](./请求与反馈/HTTP请求实现逻辑.md) | [使用方式](./请求与反馈/HTTP请求使用方式.md) |
| 公共弹窗 | `src/components/common/BaseFormDialog.vue` | [实现逻辑](./请求与反馈/公共弹窗实现逻辑.md) | [使用方式](./请求与反馈/公共弹窗使用方式.md) |
| FxDynamicTable | `src/components/common/FxDynamicTable.vue` | [实现逻辑](./配置驱动页面/FxDynamicTable实现逻辑.md) | [使用方式](./配置驱动页面/FxDynamicTable使用方式.md) |
| 部门树与组织选择 | `src/components/system/DeptTree.vue` | [实现逻辑](./组件与页面/部门树与组织选择实现逻辑.md) | [使用方式](./组件与页面/部门树与组织选择使用方式.md) |
| 图标选择器 | `src/components/common/IconPicker.vue` | [实现逻辑](./组件与页面/图标选择器实现逻辑.md) | [使用方式](./组件与页面/图标选择器使用方式.md) |
| 消息模板预览与接收人选择 | `TemplatePreview.vue` / `ReceiverSelector.vue` | [实现逻辑](./组件与页面/消息模板预览与接收人选择实现逻辑.md) | [使用方式](./组件与页面/消息模板预览与接收人选择使用方式.md) |
| 系统页面引导 | `FxGuideTour.vue` / `systemPageGuides.ts` | [实现逻辑](./组件与页面/系统页面引导实现逻辑.md) | [使用方式](./组件与页面/系统页面引导使用方式.md) |
| 公共 Excel 导入 | `src/components/excel/CommonImportDialog.vue` | [实现逻辑](./组件与页面/公共导入组件实现逻辑.md) | [使用方式](./组件与页面/公共导入组件使用方式.md) |
| 缺省页（403/404/离线） | `src/views/fallback/index.vue` | [实现逻辑](./组件与页面/缺省页实现逻辑.md) | [使用方式](./组件与页面/缺省页使用方式.md) |
| 多语言输入 | `I18nInput.vue` / `I18nJsonEditor.vue` | [实现逻辑](./国际化与布局/多语言输入实现逻辑.md) | [使用方式](./国际化与布局/多语言输入使用方式.md) |
| 个人首页与可拖拽布局 | `PersonalHomepageDesigner.vue` | [实现逻辑](./国际化与布局/个人首页与可拖拽布局实现逻辑.md) | [使用方式](./国际化与布局/个人首页与可拖拽布局使用方式.md) |
| 状态管理 | `src/stores` | [实现逻辑](./基础设施/状态管理实现逻辑.md) | [使用方式](./基础设施/状态管理使用方式.md) |
| 路由与菜单 | `src/router/index.ts` / `MainLayout.vue` | [实现逻辑](./基础设施/路由与菜单实现逻辑.md) | [使用方式](./基础设施/路由与菜单使用方式.md) |
| 权限指令 | `src/directives/permission.ts` | [实现逻辑](./基础设施/权限指令实现逻辑.md) | [使用方式](./基础设施/权限指令使用方式.md) |
| 主题与样式体系 | `src/theme` / `src/styles` | [实现逻辑](./基础设施/主题与样式体系实现逻辑.md) | [使用方式](./基础设施/主题与样式体系使用方式.md) |
| 表单验证 | Ant Design Vue `a-form` | [实现逻辑](./基础设施/表单验证实现逻辑.md) | [使用方式](./基础设施/表单验证使用方式.md) |

## 阅读建议

1. 新页面优先对照 **功能清单** 找相近范例，再读「使用方式」。
2. 排查联动问题（权限、路由、租户、HTTP）时，从 [基础设施](./基础设施/README.md) 切入。
3. 与后端契约、工作流回调、导入模板相关问题，跳转 [后端文档导航](../后端/README.md)。

## 关联文档

- [模块文档映射](../开发规范/模块文档映射/README.md)
- [后端文档导航](../后端/README.md)
- [根目录文档中心](../README.md)
