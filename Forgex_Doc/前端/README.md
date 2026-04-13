# 前端文档导航

> 版本：**预览版 V0.5.0**  
> 更新时间：**2026-04-13**

本目录聚焦 Forgex Web 管理端的公共组件、页面基础能力与接入规范，用于回答两个问题：

1. **当前前端已经支持哪些能力？**
2. **新页面开发应该优先复用哪些组件与链路？**

## 一、快速入口

1. [前端公共能力与核心功能手册](./前端公共能力与核心功能手册.md)
2. [FxDynamicTable 动态表格组件使用指南](../../doc/开发规范/使用指南/FxDynamicTable%20动态表格组件使用指南.md)
3. [前端 HTTP 请求与消息提示使用指南](../../doc/开发规范/04-功能模块/07-公共组件/前端%20HTTP%20请求与消息提示使用指南.md)

## 二、前端能力清单

| 能力域 | 当前支持情况 | 主要入口 | 文档 |
|---|---|---|---|
| 统一 HTTP 请求 | 已支持 | `src/api/http.ts` | [公共能力手册](./前端公共能力与核心功能手册.md) |
| 自动成功/失败消息 | 已支持 | `http` / `httpSuccess` / `silentHttp` | [HTTP 手册](../../doc/开发规范/04-功能模块/07-公共组件/前端%20HTTP%20请求与消息提示使用指南.md) |
| 动态表格 | 已支持 | `FxDynamicTable.vue` | [FxDynamicTable 指南](../../doc/开发规范/使用指南/FxDynamicTable%20动态表格组件使用指南.md) |
| 用户列设置 | 已支持 | `ColumnSettingButton.vue` | [公共能力手册](./前端公共能力与核心功能手册.md) |
| 字典标签渲染 | 已支持 | `DictTag.vue` | 数据字典/前端公共能力文档 |
| 多语言输入 | 已支持 | `I18nInput.vue`、`I18nJsonEditor.vue` | 组件目录与历史手册 |
| 公共弹窗 | 已支持 | `BaseFormDialog.vue` | 旧手册保留 |
| 图标选择器 | 已支持 | `IconPicker.vue` | 使用指南中已有专题 |
| 消息模板预览/接收人选择 | 已支持 | `TemplatePreview.vue`、`ReceiverSelector.vue` | 消息模板专题 |
| 动态路由 / 权限控制 | 已支持 | 登录态、菜单路由链路 | 架构设计与模块文档 |
| 工作流页面能力 | 已支持核心闭环 | `src/views/workflow/*` | 工作流模块使用指南 |
| 报表前端能力 | 已支持基础闭环 | `src/views/report/*` | 根 README 功能概览 |

## 三、推荐开发路径

### 1. 新增标准列表页

- 优先使用 `FxDynamicTable`
- 数据请求统一放在 `src/api/*`
- 成功/失败提示优先依赖 `http.ts`
- 字典标签优先复用 `DictTag`

### 2. 新增表单或配置页

- 复用 `BaseFormDialog`
- 需要多语言文本时优先使用 `I18nInput` / `I18nJsonEditor`
- 需要图标选择时复用 `IconPicker`

### 3. 对接后端写操作接口

- 优先让后端返回明确 `message`
- 页面尽量不重复调用 `message.success`
- 只在确有特殊交互时使用 `showSuccessMessage` / `silentError`

## 四、组件目录速览

`Forgex_MOM/Forgex_Fronted/src/components/common`

- `FxDynamicTable.vue`
- `ColumnSettingButton.vue`
- `DictTag.vue`
- `BaseFormDialog.vue`
- `I18nInput.vue`
- `I18nJsonEditor.vue`
- `IconPicker.vue`
- `ReceiverSelector.vue`
- `TemplatePreview.vue`

## 五、说明

本目录负责输出 V0.5.0 的**前端能力清单**与**最新接入导航**。更细的组件示例、专题实现和页面细节，继续保留在 `doc/开发规范/使用指南` 与 `doc/开发规范/04-功能模块` 中。

