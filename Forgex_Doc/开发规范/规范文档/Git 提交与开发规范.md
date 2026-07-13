# Git 提交与开发规范

> 分类：开发规范 / 规范文档
> 版本：**V0.8.0**

本规范约束 Forgex 团队的 Git 提交格式、分支模型、合并流程与 Code Review 要求，确保版本历史可追溯、协作高效、质量可控。

---

## 一、Conventional Commits 提交格式

### 1.1 格式

所有提交必须遵循 Conventional Commits 规范：

```
<type>(<scope>): <subject>

<body>

<footer>
```

### 1.2 type 类型

| type | 说明 | 示例 |
|---|---|---|
| `feat` | 新功能 | `feat(auth): 新增短信验证码登录` |
| `fix` | Bug 修复 | `fix(kms): 主密钥缺失时启动失败并输出指引` |
| `docs` | 文档变更 | `docs(android): 补全 core/feature 模块清单` |
| `refactor` | 重构（不改功能、不加功能） | `refactor(sys): 移除 ConfigService 主密钥读取逻辑` |
| `perf` | 性能优化 | `perf(frontend): Vite manualChunks 分包优化` |
| `test` | 测试相关 | `test(kms): 补充主密钥来源单测` |
| `build` | 构建系统 / 依赖变更 | `build(frontend): 锁定 @formily/antdv-x3 版本` |
| `ci` | CI 配置变更 | `ci: 调整流水线缓存策略` |
| `chore` | 杂项（不修改 src 也不修改 test） | `chore: 更新 .gitignore` |

### 1.3 scope 范围

`scope` 为可选的模块标识，建议与工程模块对齐：

| 端 | scope 示例 |
|---|---|
| 后端 | `auth` `sys` `basic` `workflow` `gateway` `integration` `report` `kms` |
| 前端 | `frontend` `vite` `router` `store` |
| 安卓 | `android` `auth` `workflow` `network` `datastore` |
| 运维 | `deploy` `docker` `backup` |
| 文档 | `docs` |

### 1.4 subject 规范

- 使用祈使句（中文可用动词开头），首字母不大写，结尾不加句号。
- 长度不超过 50 字符，超出用 body 补充。
- 描述「做了什么」而非「做了多少」。

### 1.5 body 与 footer

- **body**：说明「为什么」做这个改动（动机、背景），与代码注释的「怎么做」区分。每行不超过 72 字符。
- **footer**：
  - 关联 issue：`Closes #123` `Refs #456`
  - 不兼容变更：`BREAKING CHANGE: 主密钥不再从数据库读取，必须配置环境变量`
  - 署名（可选）：`Co-authored-by: name <email>`

### 1.6 完整示例

```
fix(kms): 主密钥改为外部注入，不再从数据库自举

主密钥原先存储在业务数据库，存在拖库即全盘失守的风险。
改为按优先级从环境变量 FORGEX_KMS_MASTER_KEY_HEX 或
FORGEX_KMS_MASTER_KEY_FILE 读取，缺失时启动失败并输出指引。

BREAKING CHANGE: 部署时必须配置主密钥环境变量或密钥文件，
详见 Forgex_Doc/后端/安全/KMS 主密钥管理.md

Closes #8
```

---

## 二、分支模型

### 2.1 分支类型与命名

| 分支 | 命名 | 来源 | 生命周期 | 说明 |
|---|---|---|---|---|
| `main` | `main` | — | 永久 | 生产分支，始终可发布，受保护 |
| `develop` | `develop` | `main` | 永久 | 集成分支，最新开发成果 |
| `feature/*` | `feature/<issue-id>-<short-desc>` | `develop` | 临时 | 功能开发，完成后合回 develop |
| `release/*` | `release/v<version>` | `develop` | 临时 | 发布准备，仅修 bug，合回 main 与 develop |
| `hotfix/*` | `hotfix/v<version>-<short-desc>` | `main` | 临时 | 生产紧急修复，合回 main 与 develop |

### 2.2 命名示例

```
feature/128-sms-login
fix/kms-master-key            # 简短修复可省略 issue-id
release/v0.8.0
hotfix/v0.8.1-auth-redirect
```

### 2.3 分支规则

- 分支名全小写，用短横线分隔，禁止空格与中文。
- `feature` / `hotfix` 分支必须关联 issue（名称含 issue-id 或提交 footer 引用）。
- `main` 与 `develop` 为受保护分支，禁止直接 push，必须通过 PR/MR 合并。
- 临时分支合并后删除，避免残留。

---

## 三、日常开发与合并流程

### 3.1 功能开发流程

```
1. 从 develop 拉取最新代码
   git checkout develop && git pull origin develop

2. 创建 feature 分支
   git checkout -b feature/128-sms-login

3. 开发并提交（遵循 Conventional Commits）
   git commit -m "feat(auth): 新增短信验证码发送接口"

4. 推送并创建 PR/MR 到 develop
   git push origin feature/128-sms-login

5. Code Review 通过后合并，删除 feature 分支
```

### 3.2 PR / MR 规范

PR/MR 标题使用 Conventional Commits 格式，描述需包含：

- **变更摘要**：本次改动做了什么。
- **关联 issue**：`Closes #128`。
- **测试说明**：如何验证（测试用例 / 手动验证步骤）。
- **不兼容变更**：如有，明确标注并说明影响与迁移方式。
- **检查清单**：
  - [ ] 代码通过本地编译
  - [ ] 单元测试通过
  - [ ] 遵循代码规范（见下文入口链接）
  - [ ] 新增公开 API 有注释
  - [ ] 无调试代码遗留（console.log / print / TODO）

### 3.3 合并策略

| 场景 | 策略 | 说明 |
|---|---|---|
| feature -> develop | Squash Merge | 压缩为一个提交，保持历史整洁 |
| release -> main | Merge Commit | 保留 release 节点，便于版本追溯 |
| hotfix -> main | Merge Commit | 保留修复节点 |
| hotfix -> develop | Cherry-pick / Merge | 同步修复到开发分支 |

### 3.4 发布流程（release 分支）

```
1. develop 达到发布标准，创建 release 分支
   git checkout -b release/v0.8.0 develop

2. 在 release 分支仅修 bug、版本号、文档，不加新功能

3. 验证通过后合并到 main 并打 tag
   git checkout main && git merge release/v0.8.0
   git tag v0.8.0

4. 同步回 develop
   git checkout develop && git merge release/v0.8.0

5. 删除 release 分支
```

---

## 四、Code Review 要求

### 4.1 评审范围

- 所有合入 `main` / `develop` 的 PR/MR 必须至少 1 名评审人 approve。
- 安全相关改动（认证、加密、KMS、权限）需指定安全负责人评审。
- 架构级改动（新增模块、接口契约变更）需架构师评审。

### 4.2 评审要点

| 维度 | 检查项 |
|---|---|
| 功能 | 是否实现 PR 描述的需求，边界条件是否覆盖 |
| 规范 | 是否符合代码规范（见入口链接）、提交格式是否合规 |
| 安全 | 是否有注入、越权、密钥泄露风险 |
| 性能 | 是否有 N+1 查询、全表扫描、内存泄漏 |
| 可维护性 | 命名是否清晰、注释是否充分、是否过度设计 |
| 测试 | 是否有对应单测，测试是否有效（非凑覆盖率） |

### 4.3 评审礼仪

- 评审意见针对代码，不针对人。
- 区分「必须修改」（blocking）与「建议」（non-blocking），建议用 `nit:` 前缀。
- 评审人在 1 个工作日内响应，避免阻塞。

---

## 五、Java / 前端基础代码规范入口

| 规范 | 入口 |
|---|---|
| Java 代码规范 | [代码注释规范](./代码注释规范.md)（Javadoc 部分）+ Google Java Style |
| MyBatis-Plus 数据访问规范 | [MyBatis-Plus 使用规范](./MyBatis-Plus 使用规范.md) |
| 数据库字段规范 | [数据库字段统一规范文档](./数据库字段统一规范文档.md) |
| 架构设计 | [项目架构设计文档](../架构设计/项目架构设计文档.md) |
| 前端规范 | 遵循 Vue 3 + TypeScript 官方风格指南，组件注释见 [代码注释规范](./代码注释规范.md) |

---

## 六、推荐阅读路径

1. [项目架构设计文档](../架构设计/项目架构设计文档.md)
2. [代码注释规范](./代码注释规范.md)
3. [MyBatis-Plus 使用规范](./MyBatis-Plus 使用规范.md)
4. [数据库字段统一规范文档](./数据库字段统一规范文档.md)
