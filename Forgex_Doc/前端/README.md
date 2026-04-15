# 前端文档导航

> 版本：**V0.5.0**  
> 更新时间：**2026-04-13**

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
| 图标选择器 | `IconPicker.vue` | 已支持 | 组件目录 |
| 消息模板预览 | `TemplatePreview.vue`、`ReceiverSelector.vue` | 已支持 | 消息模板专题 |
| 动态路由 / 权限控制 | 登录态、菜单路由链路 | 已支持 | 架构设计与模块文档 |
| 个人首页拖拽配置 | `PersonalHomepageDesigner.vue` | 已支持 | [个人首页与可拖拽布局](./国际化与布局/个人首页与可拖拽布局.md) |
| 工作流页面能力 | `src/views/workflow/*` | 已支持 | 工作流模块使用指南 |
| 报表前端能力 | `src/views/report/*` | 已支持 | 根 README 功能概览 |

## 三、推荐开发路径

### 3.1 新增标准列表页

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

### 3.2 新增表单或配置页

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

### 3.3 对接后端写操作接口

1. 优先让后端返回明确 `message`
2. 页面尽量不重复调用 `message.success`
3. 只在确有特殊交互时使用 `showSuccessMessage` / `silentError`

### 3.4 左树右表页面

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

## 四、分类阅读入口

| 分类 | 说明 | 入口 |
|---|---|---|
| [前端组件与页面](./组件与页面/README.md) | 部门树、组织选择器等系统组件 | 进入 |
| [请求与反馈](./请求与反馈/README.md) | HTTP 请求、公共弹窗、消息提示 | 进入 |
| [配置驱动页面](./配置驱动页面/README.md) | FxDynamicTable、列设置、表格配置 | 进入 |
| [国际化与布局](./国际化与布局/README.md) | 多语言输入、个人首页拖拽布局 | 进入 |
| [模块文档映射](../开发规范/模块文档映射/README.md) | 模块文档与功能点对应关系 | 进入 |

## 五、组件目录速览

### 5.1 公共组件 (`src/components/common`)

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

### 5.2 系统组件 (`src/components/system`)

| 组件 | 用途 |
|---|---|
| `DeptTree.vue` | 部门树与组织选择 |

### 5.3 个人首页组件 (`src/components/personal-homepage`)

| 组件 | 用途 |
|---|---|
| `PersonalHomepageDesigner.vue` | 个人首页拖拽设计器 |

## 六、HTTP 请求封装

### 6.1 三种请求方式

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

### 6.2 API 文件组织

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
│   ├── task.ts               # 工作流任务 API
│   └── process.ts            # 流程配置 API
└── ...
```

## 七、国际化支持

### 7.1 多语言输入组件

| 组件 | 用途 | 适用场景 |
|---|---|---|
| `I18nInput` | 多语言文本输入 | 单行/多行文本翻译 |
| `I18nJsonEditor` | 多语言 JSON 编辑 | 复杂 JSON 结构翻译 |

### 7.2 语言类型

系统已支持以下语言：

| 语言代码 | 语言名称 |
|---|---|
| `zh-CN` | 简体中文 |
| `zh-TW` | 繁体中文 |
| `en-US` | 英语（美国） |
| `ja-JP` | 日语 |
| `ko-KR` | 韩语 |

### 7.3 切换语言

```typescript
import { setLocale } from '@/utils/language'

// 切换为英文
await setLocale('en-US')
```

## 八、实践建议

1. **优先复用现有组件**，不要重复造轮子
2. **列表页优先使用 FxDynamicTable**，而不是手写 a-table
3. **成功/失败提示交给 http.ts 托管**，减少重复代码
4. **多语言文本优先使用 I18nInput**，而不是硬编码中文
5. **部门筛选优先使用 DeptTree**，而不是手写树形结构
6. **个人首页优先使用 PersonalHomepageDesigner**，支持拖拽配置

## 九、说明

本目录负责输出 V0.5.0 的**前端能力清单**与**最新接入导航**。后续更细的组件示例、专题实现和页面细节，优先继续补到 `Forgex_Doc/前端` 内部。
