---
name: forgex-project-guide
description: Forgex 项目导航和文档入口技能，帮助识别应该加载哪些 Forgex_Doc 文档。当在 D:\mine_product\forgex 工作、更改 Forgex_MOM 代码、阅读 Forgex_Doc、决定加载哪些前端/后端/部署/数据库文档，或准备后续 Forgex 功能开发时自动应用此技能。
disable-model-invocation: true
---

# Forgex 项目导航

## 一、项目架构概览

Forgex 采用微服务架构：

```
forgex/
├── Forgex_MOM/              # 主工程
│   ├── Forgex_Backend/      # 后端工程
│   │   ├── Forgex_Common/   # 公共模块
│   │   ├── Forgex_Gateway/  # 网关服务 (8080)
│   │   ├── Forgex_Auth/     # 认证服务 (8081)
│   │   ├── Forgex_Sys/      # 系统服务 (8082)
│   │   └── Forgex_Job/      # 任务调度服务 (8083)
│   └── Forgex_Fronted/      # 前端工程
└── Forgex_Doc/              # 文档目录
```

## 二、文档加载决策树

根据开发任务类型，加载对应的 skills：

| 任务类型 | 加载 Skills |
|---------|------------|
| 前端开发 | forgex-frontend-guide + forgex-development-standards |
| 后端开发 | forgex-backend-guide + forgex-development-standards |
| 部署/环境配置 | forgex-deploy-db-guide |
| 数据库设计 | forgex-deploy-db-guide + forgex-development-standards |
| 代码审查/提交 | forgex-feature-self-review + forgex-development-standards |
| 通用开发 | forgex-project-guide + forgex-development-standards |

## 三、文档目录结构

### 3.1 Forgex_Doc 结构

```
Forgex_Doc/
├── 前端/           # 前端功能文档
├── 后端/           # 后端功能文档
├── 部署/           # 部署和授权文档
├── 数据库/         # 数据库规范和脚本
├── 开发规范/       # 开发规范文档
└── README.md       # 文档中心索引
```

### 3.2 文档版本管理

- 文档版本格式：`V{major}.{minor}.{patch}`
- **除非用户明确要求，否则不要修改文档版本**
- 功能更新时同步更新对应文档内容，但保持版本号不变

## 四、快速导航

### 4.1 前端文档入口

详细文档见 `forgex-frontend-guide` skill，核心文件：
- 前端公共能力：`Forgex_Doc/前端/前端公共能力与核心功能手册.md`
- 请求与反馈：`Forgex_Doc/前端/请求与反馈/README.md`
- 配置驱动页面：`Forgex_Doc/前端/配置驱动页面/README.md`
- 组件与页面：`Forgex_Doc/前端/组件与页面/README.md`

### 4.2 后端文档入口

详细文档见 `forgex-backend-guide` skill，核心文件：
- 后端公共能力：`Forgex_Doc/后端/后端公共能力与核心功能手册.md`
- 身份与权限：`Forgex_Doc/后端/身份与权限/README.md`
- 模块专题：`Forgex_Doc/后端/模块专题/README.md`
- 配置与审计：`Forgex_Doc/后端/配置与审计/README.md`

### 4.3 部署文档入口

详细文档见 `forgex-deploy-db-guide` skill，核心文件：
- 授权说明：`Forgex_Doc/部署/授权说明使用方式.md`
- Nacos 配置：`Forgex_Doc/部署/nacos配置/`
- 数据库初始化脚本：`Forgex_Doc/部署/数据库初始化脚本/`

### 4.4 数据库文档入口

详细文档见 `forgex-deploy-db-guide` skill，核心文件：
- 安全与配置：`Forgex_Doc/数据库/安全与配置/README.md`
- 脚本与修复：`Forgex_Doc/数据库/脚本与修复/README.md`

### 4.5 开发规范入口

详细文档见 `forgex-development-standards` skill，核心文件：
- 架构设计：`Forgex_Doc/开发规范/架构设计/项目架构设计文档.md`
- 规范文档：`Forgex_Doc/开发规范/规范文档/README.md`

## 五、开发工作流

### 5.1 功能开发流程

1. 确定任务类型（前端/后端/数据库/部署）
2. 加载对应的 skills
3. 阅读相关文档
4. 实现功能
5. 使用 `forgex-feature-self-review` 进行自审查
6. 更新对应文档内容（不改版本）

### 5.2 文档同步原则

**每次功能更新后**：
- ✅ 更新对应的功能文档内容
- ✅ 保持文档版本不变（除非用户明确要求）
- ✅ 确保文档与代码实现一致
- ❌ 不要修改文档版本号

## 六、关联 Skills

- **forgex-frontend-guide**: 前端开发指南
- **forgex-backend-guide**: 后端开发指南
- **forgex-deploy-db-guide**: 部署和数据库环境
- **forgex-development-standards**: 开发规范
- **forgex-feature-self-review**: 功能自审查和文档同步

## 七、使用建议

1. **优先加载项目导航**：开始任何 Forgex 开发前，先加载此 skill
2. **按需加载专题 skills**：根据任务类型加载对应的专题 skill
3. **保持文档同步**：功能开发完成后，使用自审查 skill 更新文档
4. **遵循开发规范**：始终加载开发规范 skill 作为基线
