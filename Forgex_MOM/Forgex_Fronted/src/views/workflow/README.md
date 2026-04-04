# 工作流模块前端接入指南

## 目录结构

```
src/views/workflow/
├── taskConfig/           # 审批任务配置
│   └── index.vue        # 审批任务配置页面
├── execution/           # 审批执行
│   └── start.vue        # 发起审批页面
├── myTask/              # 我的任务
│   ├── pending.vue      # 我的待办
│   ├── processed.vue    # 我已处理
│   └── initiated.vue    # 我发起的
└── README.md            # 接入指南
```

## API 封装

工作流模块的 API 已封装在以下文件中：

- `src/api/workflow/taskConfig.ts` - 审批任务配置相关 API
- `src/api/workflow/execution.ts` - 审批执行相关 API

## 路由配置

在工作流模块的主路由文件中添加以下配置：

```typescript
import workflowRoutes from '@/router/workflow'

// 在主路由配置中添加
{
  path: '/workflow',
  name: 'Workflow',
  children: workflowRoutes
}
```

## 菜单配置

在系统菜单管理中添加以下菜单项：

| 菜单名称 | 路由地址 | 权限标识 | 图标 |
|---------|---------|---------|------|
| 工作流管理 | /workflow | wf:task | FolderOutlined |
| ├─ 审批任务配置 | /workflow/taskConfig | wf:taskConfig:view | SettingOutlined |
| ├─ 发起审批 | /workflow/execution/start | wf:execution:start | PlusOutlined |
| ├─ 我的待办 | /workflow/my/pending | wf:myTask:pending | BellOutlined |
| ├─ 我已处理 | /workflow/my/processed | wf:myTask:processed | CheckCircleOutlined |
| └─ 我发起的 | /workflow/my/initiated | wf:myTask:initiated | SendOutlined |

## 权限配置

在角色权限管理中分配以下权限：

### 审批任务配置权限
- `wf:taskConfig:view` - 查看
- `wf:taskConfig:add` - 新增
- `wf:taskConfig:edit` - 编辑
- `wf:taskConfig:delete` - 删除
- `wf:taskConfig:config` - 节点配置

### 审批执行权限
- `wf:execution:start` - 发起审批
- `wf:execution:approve` - 审批同意
- `wf:execution:reject` - 审批驳回
- `wf:execution:cancel` - 撤销审批

## 页面功能说明

### 1. 审批任务配置页面 (`/workflow/taskConfig`)

**功能**：
- 审批任务列表展示（支持分页、搜索、排序）
- 新增审批任务
- 编辑审批任务
- 删除审批任务
- 启用/禁用审批任务
- 节点配置（待开发）

**字段说明**：
- `taskName` - 任务名称（必填）
- `taskCode` - 任务编码（必填，修改时不可变）
- `interpreterBean` - 审批解释器 Bean 名称（可选）
- `formType` - 表单类型：1=自定义，2=低代码（必填）
- `formPath` - 表单路径（自定义表单时必填）
- `formContent` - 表单内容（低代码表单时必填）
- `status` - 状态：0=禁用，1=启用
- `remark` - 备注

### 2. 发起审批页面 (`/workflow/execution/start`)

**功能**：
- 选择审批任务
- 动态表单渲染（根据任务配置）
- 表单验证
- 提交审批

**使用方式**：
1. 选择一个启用的审批任务
2. 根据表单类型填写相应的表单内容
3. 点击"提交审批"按钮

### 3. 我的待办页面 (`/workflow/my/pending`)

**功能**：
- 待办审批列表展示
- 审批同意
- 审批驳回
- 查看详情
- 审批历史查看（待开发）

**审批操作**：
- **同意**：填写审批意见后提交
- **驳回**：选择驳回类型（驳回任务/返回上一节点），填写审批意见后提交

### 4. 我已处理页面 (`/workflow/my/processed`)

**功能**：
- 已处理审批列表展示
- 查看详情
- 审批历史查看（待开发）

### 5. 我发起的页面 (`/workflow/my/initiated`)

**功能**：
- 我发起的审批列表展示
- 查看详情
- 审批历史查看（待开发）
- 撤销审批（仅支持未审批或审批中的任务）

## 待开发功能

以下功能需要在后续开发中完善：

### 1. 节点配置功能
- 节点配置弹窗/页面
- 节点审批人配置
- 分支条件配置
- 节点流转逻辑

### 2. 低代码表单渲染器
- JSON 表单解析
- 动态表单验证
- 表单数据绑定

### 3. 自定义表单动态加载
- 根据 `formPath` 动态加载表单组件
- 表单组件注册机制

### 4. 审批历史查看
- 审批历史 API 接口
- 审批历史展示组件
- 审批进度可视化

### 5. 审批人选择器
- 部门选择器
- 角色选择器
- 职位选择器
- 人员选择器

## 开发规范

### 代码风格
- 使用 TypeScript 进行类型检查
- 使用 Composition API (`<script setup>`)
- 遵循 Vue 3 最佳实践

### 命名规范
- 文件名：kebab-case（如 `taskConfig.vue`）
- 组件名：PascalCase（如 `BaseFormDialog`）
- 变量名：camelCase
- 常量名：UPPER_SNAKE_CASE

### 注释规范
- 组件文件添加文件级注释
- 复杂逻辑添加行内注释
- 函数添加 JSDoc 注释

### API 调用规范
- 所有 API 调用统一在 `src/api/workflow/` 目录下封装
- 使用统一的 `http` 客户端
- 错误处理统一使用 `message.error()`

## 常见问题

### Q1: 如何添加自定义表单？
A: 在 `src/views/workflow/form/` 目录下创建表单组件，然后在审批任务配置中指定 `formPath`。

### Q2: 如何实现审批解释器？
A: 在后端实现 `IApprovalInterpreter` 接口，并在审批任务配置中指定 `interpreterBean`。

### Q3: 如何配置节点审批人？
A: 节点配置功能开发完成后，可以在审批任务配置页面点击"节点配置"按钮进行配置。

### Q4: 表单内容支持哪些格式？
A: 自定义表单支持任意 JSON 格式的数据；低代码表单需要按照低代码平台的规范编写。

## 更新日志

### v1.0.0 (2026-04-01)
- ✅ 完成审批任务配置页面
- ✅ 完成发起审批页面
- ✅ 完成我的待办页面
- ✅ 完成我已处理页面
- ✅ 完成我发起的页面
- ✅ 完成 API 封装
- ✅ 完成路由配置
- ⏳ 节点配置功能（开发中）
- ⏳ 低代码表单渲染器（待开发）
- ⏳ 审批历史查看（待开发）
