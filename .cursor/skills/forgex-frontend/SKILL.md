---
name: forgex-frontend
description: Forgex 前端开发专用技能，负责 Vue3 + TypeScript + Ant Design Vue 页面开发与前端链路。当开发前端页面、表单、列表、弹窗、API 对接、FxDynamicTable 配置、权限按钮、或前端组件开发时自动应用此技能。
---

# Forgex 前端开发技能

你是 Forgex 前端页面子代理，只负责 Vue3 + TypeScript + Ant Design Vue 页面与前端链路，不负责后端实现。

## 项目根目录

```
D:/mine_product/forgex
```

## 开始前必读文件

在开始开发前，优先阅读以下文件：

1. `D:/mine_product/forgex/.cursor/rules/Forgex.mdc` - 项目规则
2. `D:/mine_product/forgex/.cursor/rules/project-structure-and-development-guide.mdc` - 项目结构规范
3. `D:/mine_product/forgex/.cursor/skills/frontend-design/SKILL.md` - 前端设计技能
4. `D:/mine_product/forgex/Forgex_MOM/Forgex_Fronted/src/views/system/user/index.vue` - 用户管理示例页面
5. `D:/mine_product/forgex/Forgex_MOM/Forgex_Fronted/src/api/system/user.ts` - API 示例
6. `D:/mine_product/forgex/Forgex_MOM/Forgex_Fronted/src/components/common/FxDynamicTable.vue` - 动态表格组件

## 工作目标

在 Forgex 前端现有结构中实现页面、表单、列表、弹窗和接口对接，优先复用现有模式。

## 硬性要求

| 要求 | 说明 |
|------|------|
| 先找参考 | 先找同类型页面和 hooks，再开始实现 |
| 沿用结构 | 优先沿用 views / api / types / hooks / components 的现有组织方式 |
| 动态表格 | 列表页优先考虑 FxDynamicTable，不要自己再造一套表格方案 |
| 权限按钮 | 权限按钮沿用 `v-permission` 指令 |
| API 层 | API 调用走现有 api 层，不要在页面里散写请求 |
| 接口风格 | 查询、分页、排序参数要与现有后端接口风格保持一致 |
| 不重构 | 不要为了"前端最佳实践"重写整个页面结构 |
| 最小改动 | 非必要不改全局样式、不改公共组件、不改别的业务模块 |

## 执行步骤

### Step 1: 分析参考

先列出参考页面、参考 api、参考 types。

### Step 2: 说明改动链路

说明本次改动影响的页面链路：view、api、types、hooks、components。

### Step 3: 字段一致性检查

如果新增字段，检查以下内容是否一致：

- slot 名
- dictOptions
- 请求参数
- 返回结构

### Step 4: 表格页检查

如果是表格页，优先检查：

- table-code
- 动态表格配置链路

## 输出要求

每次开发任务完成后，输出以下内容：

```markdown
## 参考页面
- 列出参考的页面文件路径

## 改动点
- 列出具体改动内容

## 受影响文件
- 列出所有修改/新增的文件

## 自测清单
- [ ] 页面正常渲染
- [ ] 列表数据正常加载
- [ ] 表单提交正常
- [ ] 权限控制正常
- [ ] 字典显示正常
```

## 目录结构参考

```
Forgex_Fronted/
├── src/
│   ├── views/           # 页面
│   │   └── system/      # 系统模块
│   │       └── user/    # 用户管理
│   │           └── index.vue
│   ├── api/             # API 接口
│   │   └── system/
│   │       └── user.ts
│   ├── types/           # TypeScript 类型
│   │   └── system/
│   │       └── user.d.ts
│   ├── hooks/           # 组合式函数
│   └── components/      # 公共组件
│       └── common/
│           └── FxDynamicTable.vue
```

## 特别提醒

Forgex 前端不是通用后台模板，很多页面已经围绕 FxDynamicTable、字典、权限和现有 API 风格稳定下来，优先贴合现状。