# 前端文档导航

> 版本：**V0.5.0**  
> 更新时间：**2026-04-15**

本目录聚焦 Forgex Web 管理端的公共组件、页面基础能力与接入规范，用于回答两个问题：

1. **当前前端已经支持哪些能力？**
2. **新页面开发应该优先复用哪些组件与链路？**

## 一、快速入口

1. [前端公共能力与核心功能手册](./前端公共能力与核心功能手册.md)
2. [FxDynamicTable 与列设置](./配置驱动页面/FxDynamicTable 与列设置.md)
3. [HTTP 请求与消息提示](./请求与反馈/HTTP 请求与消息提示.md)
4. [前端组件与页面分类页](./组件与页面/README.md)

## 二、前端能力清单

### 2.1 公共组件

| 组件名 | 文件路径 | 功能 | 状态 |
|---|---|---|---|
| `FxDynamicTable` | `src/components/common/FxDynamicTable.vue` | 动态表格、列设置、字典翻译 | 已支持 |
| `ColumnSettingButton` | `src/components/common/ColumnSettingButton.vue` | 用户列设置按钮 | 已支持 |
| `DictTag` | `src/components/common/DictTag.vue` | 字典标签渲染 | 已支持 |
| `BaseFormDialog` | `src/components/common/BaseFormDialog.vue` | 公共表单弹窗 | 已支持 |
| `I18nInput` | `src/components/common/I18nInput.vue` | 多语言输入框 | 已支持 |
| `I18nJsonEditor` | `src/components/common/I18nJsonEditor.vue` | 多语言 JSON 编辑器 | 已支持 |
| `IconPicker` | `src/components/common/IconPicker.vue` | 图标选择器 | 已支持 |
| `ReceiverSelector` | `src/components/common/ReceiverSelector.vue` | 消息接收人选择 | 已支持 |
| `TemplatePreview` | `src/components/common/TemplatePreview.vue` | 消息模板预览 | 已支持 |
| `DeptTree` | `src/components/system/DeptTree.vue` | 部门树与组织选择 | 已支持 |
| `PersonalHomepageDesigner` | `src/components/personal-homepage/PersonalHomepageDesigner.vue` | 个人首页拖拽设计器 | 已支持 |

### 2.2 公共能力

| 能力域 | 主要入口 | 状态 | 文档 |
|---|---|---|---|
| 统一 HTTP 请求 | `src/api/http.ts` | 已支持 | [HTTP 请求与消息提示](./请求与反馈/HTTP 请求与消息提示.md) |
| 自动成功/失败消息 | `http` / `httpSuccess` / `silentHttp` | 已支持 | [HTTP 手册](./请求与反馈/HTTP 请求与消息提示.md) |
| 动态表格配置 | `FxDynamicTable.vue` | 已支持 | [FxDynamicTable 指南](./配置驱动页面/FxDynamicTable 与列设置.md) |
| 用户列设置 | `ColumnSettingButton.vue` | 已支持 | [FxDynamicTable 指南](./配置驱动页面/FxDynamicTable 与列设置.md) |
| 字典标签渲染 | `DictTag.vue` | 已支持 | [数据字典文档](../后端/配置与审计/数据字典与日志.md) |
| 多语言输入 | `I18nInput.vue`、`I18nJsonEditor.vue` | 已支持 | [多语言输入与动态布局](./国际化与布局/多语言输入与动态布局.md) |
| 公共弹窗 | `BaseFormDialog.vue` | 已支持 | [公共弹窗](./请求与反馈/公共弹窗.md) |
| 部门树与组织选择 | `DeptTree.vue` | 已支持 | [部门树与组织选择](./组件与页面/部门树与组织选择.md) |
| 图标选择器 | `IconPicker.vue` | 已支持 | [前端组件与页面](./组件与页面/README.md) |
| 消息模板预览 | `TemplatePreview.vue`、`ReceiverSelector.vue` | 已支持 | [前端组件与页面](./组件与页面/README.md) |
| 动态路由 / 权限控制 | 登录态、菜单路由链路 | 已支持 | [认证授权](../后端/身份与权限/认证授权.md) |
| 个人首页拖拽配置 | `PersonalHomepageDesigner.vue` | 已支持 | [个人首页与可拖拽布局](./国际化与布局/个人首页与可拖拽布局.md) |
| 工作流页面能力 | `src/views/workflow/*` | 已支持 | [工作流](../后端/模块专题/工作流.md) |
| 报表前端能力 | `src/views/report/*` | 已支持 | [报表中心](../后端/模块专题/报表中心.md) |
| 集成平台页面能力 | `src/views/integrationPlatform/*` | 已支持 | [集成平台](../后端/模块专题/集成平台.md) |
| 基础业务页面能力 | `src/views/basic/*` | 已支持 | [基础数据与物料](../后端/模块专题/基础数据与物料.md) |
| 系统级配置页面能力 | `src/views/system/*` | 已支持 | [编码规则、授权与初始化](../后端/配置与审计/编码规则、授权与初始化.md) |

## 三、页面与 API 导航

### 3.1 认证与个人入口

| 页面 | API | 说明 |
|---|---|---|
| `src/views/auth/login/index.vue` | `src/api/auth/login.ts`、`src/api/auth/captcha.ts` | 登录、验证码、租户链路 |
| `src/views/auth/register/index.vue` | `src/api/auth/login.ts` | 注册与邀请码链路 |
| `src/views/profile/index.vue` | `src/api/profile.ts` | 个人资料 |
| `src/views/home/index.vue` | `src/api/system/personalHomepage.ts` | 个人首页配置 |
| `src/views/home/favorite-management/index.vue` | `src/api/system/personalHomepage.ts` | 常用菜单与收藏管理 |

### 3.2 基础与集成平台

| 页面 | API | 说明 |
|---|---|---|
| `src/views/basic/dashboard/index.vue` | 聚合首页或模块探针接口 | 基础业务首页 |
| `src/views/basic/encodeRule/index.vue` | `src/api/system/encodeRule.ts` | 基础业务下的编码规则使用入口 |
| `src/views/integrationPlatform/thirdSystem/index.vue` | `src/api/system/integration.ts` | 第三方系统管理 |
| `src/views/integrationPlatform/apiConfig/index.vue` | `src/api/system/integration.ts` | API 配置 |
| `src/views/integrationPlatform/apiCallLog/index.vue` | `src/api/system/integration.ts` | 调用日志查看 |

### 3.3 系统管理

| 页面 | API | 说明 |
|---|---|---|
| `src/views/system/user/index.vue` | `src/api/system/user.ts` | 用户管理 |
| `src/views/system/role/index.vue` | `src/api/system/role.ts` | 角色管理 |
| `src/views/system/department/index.vue` | `src/api/system/department.ts` | 部门管理 |
| `src/views/system/position/index.vue` | `src/api/system/position.ts` | 职位管理 |
| `src/views/system/menu/index.vue` | `src/api/system/menu.ts`、`src/api/system/route.ts` | 菜单与动态路由 |
| `src/views/system/module/index.vue` | `src/api/system/module.ts` | 模块管理 |
| `src/views/system/tableConfig/index.vue` | `src/api/system/tableConfig.ts` | 表格配置 |
| `src/views/system/userTableConfig/index.vue` | `src/api/system/tableConfig.ts` | 用户个性化列配置 |
| `src/views/system/dict/index.vue` | `src/api/system/dict.ts` | 数据字典 |
| `src/views/system/message/index.vue` | `src/api/system/message.ts` | 站内消息 |
| `src/views/system/messageTemplate/index.vue` | `src/api/system/message.ts` | 消息模板 |
| `src/views/system/loginLog/index.vue` | `src/api/operationLog.ts` | 登录日志 |
| `src/views/system/operationLog/index.vue` | `src/api/operationLog.ts` | 操作日志 |
| `src/views/system/online/index.vue` | `src/api/system/online.ts` | 在线用户 |
| `src/views/system/config/index.vue` | `src/api/system/config.ts` | 系统配置 |
| `src/views/system/tenant/index.vue` | `src/api/system/tenant.ts` | 租户管理 |
| `src/views/system/tenantMessageWhitelist/index.vue` | `src/api/tenantMessageWhitelist.ts` | 租户消息白名单 |
| `src/views/system/inviteCode/index.vue` | `src/api/system/inviteCode.ts` | 邀请码管理 |
| `src/views/system/i18nLanguageType/index.vue` | `src/api/system/i18n.ts` | 语言类型 |
| `src/views/system/i18nMessage/index.vue` | `src/api/system/i18nMessage.ts` | 国际化消息 |
| `src/views/system/encodeRule/index.vue` | `src/api/system/encodeRule.ts` | 编码规则 |
| `src/views/system/excelImportConfig/index.vue` | `src/api/system/excel.ts` | 导入配置 |
| `src/views/system/excelExportConfig/index.vue` | `src/api/system/excel.ts` | 导出配置 |

### 3.4 工作流 / 报表页面

| 页面 | API | 说明 |
|---|---|---|
| `src/views/workflow/dashboard/index.vue` | `src/api/workflow/execution.ts` | 工作流首页与统计 |
| `src/views/workflow/taskConfig/*` | `src/api/workflow/taskConfig.ts` | 审批任务配置与低代码表单设计 |
| `src/views/workflow/execution/*` | `src/api/workflow/execution.ts` | 发起审批与表单渲染 |
| `src/views/workflow/myTask/*` | `src/api/workflow/execution.ts` | 待办、已办、已发起、追踪 |
| `src/views/workflow/governance/compensation/index.vue` | `src/api/workflow/execution.ts` | 补偿治理 |
| `src/views/report/index.vue` | `src/api/report/index.ts` | 报表模板管理 |
| `src/views/report/datasource/index.vue` | `src/api/report/index.ts` | 数据源管理 |
| `src/views/report/components/*` | `src/api/report/index.ts` | 设计器、表单、预览 |

## 四、推荐开发路径

### 4.1 新增标准列表页

1. 优先使用 `FxDynamicTable`
2. 数据请求统一放在 `src/api/*`
3. 成功/失败提示优先依赖 `http.ts`
4. 字典标签优先复用 `DictTag`

**示例结构：**

```vue
<template>
  <div class="page-container">
    <FxDynamicTable
      ref="tableRef"
      :request="loadData"
      table-code="my_module_table"
      :show-checkbox="true"
      :show-pagination="true"
    />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { myModuleApi } from '@/api/my-module'

const tableRef = ref()

function loadData(params: any) {
  return myModuleApi.getList(params)
}
</script>
```

### 4.2 新增表单或配置页

1. 复用 `BaseFormDialog`
2. 需要多语言文本时优先使用 `I18nInput` / `I18nJsonEditor`
3. 需要图标选择时复用 `IconPicker`

**示例结构：**

```vue
<template>
  <BaseFormDialog
    v-model:visible="dialogVisible"
    :title="formTitle"
    :form-items="formItems"
    @save="handleSave"
  >
    <template #customField="{ value, onChange }">
      <I18nInput v-model="value" @change="onChange" />
    </template>
  </BaseFormDialog>
</template>
```

### 4.3 对接后端写操作接口

1. 优先让后端返回明确 `message`
2. 页面尽量不重复调用 `message.success`
3. 只在确有特殊交互时使用 `showSuccessMessage` / `silentError`

### 4.4 左树右表页面

1. 使用 `DeptTree` 作为左侧组织选择器
2. 右侧使用 `FxDynamicTable` 展示数据
3. 通过 `select` 事件联动筛选条件

**示例结构：**

```vue
<template>
  <div class="page-layout">
    <DeptTree ref="deptTreeRef" @select="handleDeptSelect" />
    <div class="right-content">
      <FxDynamicTable
        ref="tableRef"
        :request="loadData"
        table-code="sys_user_table"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
const currentDeptId = ref<string>('')
const tableRef = ref()

function handleDeptSelect(keys: string[], node: any) {
  currentDeptId.value = String(node?.id || '')
  tableRef.value?.reload()
}

function loadData(params: any) {
  return userApi.getList({ ...params, deptId: currentDeptId.value })
}
</script>
```

## 五、分类阅读入口

| 分类 | 说明 | 入口 |
|---|---|---|
| [前端组件与页面](./组件与页面/README.md) | 部门树、组织选择器等系统组件 | 进入 |
| [请求与反馈](./请求与反馈/README.md) | HTTP 请求、公共弹窗、消息提示 | 进入 |
| [配置驱动页面](./配置驱动页面/README.md) | FxDynamicTable、列设置、表格配置 | 进入 |
| [国际化与布局](./国际化与布局/README.md) | 多语言输入、个人首页拖拽布局 | 进入 |
| [模块文档映射](../开发规范/模块文档映射/README.md) | 模块文档与功能点对应关系 | 进入 |

## 六、组件目录速览

### 6.1 公共组件 (`src/components/common`)

| 组件 | 用途 |
|---|---|
| `FxDynamicTable.vue` | 动态表格核心组件 |
| `ColumnSettingButton.vue` | 列设置按钮 |
| `DictTag.vue` | 字典标签 |
| `BaseFormDialog.vue` | 基础表单弹窗 |
| `I18nInput.vue` | 多语言输入框 |
| `I18nJsonEditor.vue` | 多语言 JSON 编辑器 |
| `IconPicker.vue` | 图标选择器 |
| `ReceiverSelector.vue` | 消息接收人选择器 |
| `TemplatePreview.vue` | 消息模板预览 |

### 6.2 系统组件 (`src/components/system`)

| 组件 | 用途 |
|---|---|
| `DeptTree.vue` | 部门树与组织选择 |

### 6.3 个人首页组件 (`src/components/personal-homepage`)

| 组件 | 用途 |
|---|---|
| `PersonalHomepageDesigner.vue` | 个人首页拖拽设计器 |

## 七、HTTP 请求封装

### 7.1 三种请求方式

```typescript
// 1. 普通请求（自动处理成功/失败消息）
import { http } from '@/api/http'
const result = await http.post('/api/sys/user/list', params)

// 2. 只要成功消息（失败时不自动提示）
import { httpSuccess } from '@/api/http'
const result = await httpSuccess.get('/api/sys/user/detail', { id })

// 3. 静默请求（不自动提示，自行处理）
import { silentHttp } from '@/api/http'
const result = await silentHttp.post('/api/sys/user/list', params)
```

### 7.2 API 文件组织

```text
src/api/
├── http.ts                    # HTTP 客户端封装
├── system/
│   ├── user.ts               # 用户管理 API
│   ├── role.ts               # 角色管理 API
│   ├── department.ts         # 部门管理 API
│   ├── config.ts             # 系统配置 API
│   └── personalHomepage.ts   # 个人首页 API
├── workflow/
│   ├── taskConfig.ts         # 工作流任务配置 API
│   └── execution.ts          # 流程执行 API
└── ...
```

## 八、国际化支持

### 8.1 多语言输入组件

| 组件 | 用途 | 适用场景 |
|---|---|---|
| `I18nInput` | 多语言文本输入 | 单行/多行文本翻译 |
| `I18nJsonEditor` | 多语言 JSON 编辑 | 复杂 JSON 结构翻译 |

### 8.2 语言类型

系统已支持以下语言：

| 语言代码 | 语言名称 |
|---|---|
| `zh-CN` | 简体中文 |
| `zh-TW` | 繁体中文 |
| `en-US` | 英语（美国） |
| `ja-JP` | 日语 |
| `ko-KR` | 韩语 |

### 8.3 切换语言

```typescript
import { setLocale } from '@/utils/language'

// 切换为英文
await setLocale('en-US')
```

## 九、实践建议

1. **优先复用现有组件**，不要重复造轮子
2. **列表页优先使用 FxDynamicTable**，而不是手写 a-table
3. **成功/失败提示交给 http.ts 托管**，减少重复代码
4. **多语言文本优先使用 I18nInput**，而不是硬编码中文
5. **部门筛选优先使用 DeptTree**，而不是手写树形结构
6. **个人首页优先使用 PersonalHomepageDesigner**，支持拖拽配置

## 十、说明

本目录负责输出 V0.5.0 的**前端能力清单**与**最新接入导航**。后续更细的组件示例、专题实现和页面细节，优先继续补到 `Forgex_Doc/前端` 内部。
