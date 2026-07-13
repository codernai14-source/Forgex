# Forgex 六项遗留问题 · 修复方案

> 主理人：齐活林（Qi）· 交付总监
> 编排日期：2026-06-21
> 适用版本：Forgex V0.8.0
> 状态：**待用户确认** → 确认后启动标准 SOP 执行

---

## 一、方案总览

| 编号 | 问题 | 严重度 | 执行组 | 负责人 | 工作流 |
|------|------|--------|--------|--------|--------|
| 4 | Linux 运维脚本大面积 TODO | 🟡 中 | 组B 运维 | 寇豆码（工程师） | 快速模式 |
| 5 | 安卓端文档空白 | 🟡 中 | 组A 文档 | 寇豆码（工程师） | 部分工作流 |
| 6 | 三份规范空壳文档 | 🟡 中 | 组A 文档 | 寇豆码（工程师） | 快速模式 |
| 7 | Vite 生产优化缺失 | 🟢 低 | 组C 前端 | 寇豆码（工程师） | 快速模式 |
| 8 | 主密钥"自举"风险 | 🟡 中 | 组D 后端安全 | 高见远+寇豆码 | 标准 SOP |
| 9 | 移动端无离线能力 | 🟡 中 | 组E 移动端 | 高见远+寇豆码 | 标准 SOP |

**执行策略**：5 个执行组按依赖关系分批调度。组A/B/C 可并行（互不依赖）；组D/组E 涉及架构决策，需架构师先出设计再交工程师实现。整体由主理人统一编排，所有跨成员信息流经主理人中转。

---

## 二、问题 4：Linux 运维脚本 + Portainer 集成

### 现状
- `backup.sh` / `restore.sh` / `rollback.sh` / `upgrade.sh` 均为 3 行空壳（仅 `echo TODO`）
- `install.sh` 已有实例初始化逻辑，且已提到 Portainer，但未落地
- `docker-compose.yml.template` 已定义 8 个业务服务（gateway/auth/sys/basic/job/integration/workflow/report），**但 MySQL/Redis/Nacos 中间件未纳入 compose**，Portainer 无法统一管理全栈
- 缺少 Portainer Stack 文件和 API 调用封装

### 修复方案

#### 4.1 补全四个运维脚本（寇豆码 · 快速模式）

**backup.sh** — 全量备份
- 备份对象：MySQL（mysqldump --single-transaction --routines --triggers）、Redis（BGSAVE 后拷贝 dump.rdb）、Nacos 配置（/nacos/data 导出）、uploads 目录、当前 compose stack 状态与镜像清单
- 输出：`$FORGEX_BACKUP_DIR/forgex-backup-YYYYmmdd-HHMMSS.tar.gz`，含 `manifest.json`（记录版本、镜像 tag、校验和）
- 保留策略：默认保留最近 7 份，可配置 `BACKUP_RETAIN=7`

**restore.sh** — 指定备份包恢复
- 参数：`restore.sh <backup-file>`
- 流程：校验 manifest → 停 compose 服务 → 恢复 MySQL/Redis/Nacos/uploads → 拉起服务 → 健康检查 → 输出恢复报告
- 安全：强制二次确认（`--force` 跳过），恢复前自动做一次当前状态快照

**upgrade.sh** — 滚动升级
- 参数：`upgrade.sh <new-version>`
- 流程：拉取新镜像 → 自动 backup → 逐服务滚动重启（gateway→auth→sys→basic→job→integration→workflow→report）→ 每服务健康检查 → 失败自动触发 rollback
- 记录升级历史到 `$FORGEX_HOME/upgrade.log`

**rollback.sh** — 版本回滚
- 参数：`rollback.sh [version]`（默认回滚到上一版本）
- 依赖：upgrade 前的 backup 与镜像快照
- 流程：校验目标版本镜像存在 → 停服 → 切换镜像 tag → 起服 → 健康检查

#### 4.2 Portainer 集成（核心诉求）

- **新增 `portainer-stack.yml`**：兼容 docker-compose 格式，可直接作为 Portainer Stack 导入；补充 MySQL/Redis/Nacos 中间件服务（带 healthcheck + 数据卷），实现全栈在 Portainer 内统一管理
- **`install.sh` 增强**：检测本机是否已运行 Portainer，未运行则提示一键部署命令（Portainer CE 容器）；生成 Stack 时自动填充 `.env` 变量
- **双路径管理**：
  - 路径1（推荐）：通过 Portainer Web UI 导入 Stack、管理容器/镜像/日志/卷
  - 路径2（脚本）：运维脚本通过 Portainer REST API（`curl`）调用 Stack 更新，兼容纯命令行环境回退到 `docker compose`
- **新增 `portainer-api.sh`**：封装 Portainer API 调用（认证、stack 部署、容器列表、日志拉取），供四个运维脚本复用
- **文档**：在 `Forgex_Doc/部署/` 下补 `Portainer 管理手册.md`，说明 Stack 导入、镜像管理、日志查看、升级/回滚操作流程

#### 涉及文件
| 文件 | 动作 |
|------|------|
| `Forgex_Build/delivery/linux/scripts/backup.sh` | 重写 |
| `Forgex_Build/delivery/linux/scripts/restore.sh` | 重写 |
| `Forgex_Build/delivery/linux/scripts/upgrade.sh` | 重写 |
| `Forgex_Build/delivery/linux/scripts/rollback.sh` | 重写 |
| `Forgex_Build/delivery/linux/scripts/install.sh` | 增强 Portainer 检测 |
| `Forgex_Build/delivery/linux/scripts/docker-compose.yml.template` | 补中间件服务 |
| `Forgex_Build/delivery/linux/scripts/portainer-stack.yml` | 新增 |
| `Forgex_Build/delivery/linux/scripts/portainer-api.sh` | 新增 |
| `Forgex_Doc/部署/Portainer 管理手册.md` | 新增 |

---

## 三、问题 5：安卓端文档空白

### 现状
- `Forgex_Doc/安卓端/README.md` 版本为 V0.6.5（应 V0.8.0）
- core 模块实际 **11 个**（architecture/common/component/datastore/designsystem/device/model/navigation/network/testing/ui），文档仅列 5 个
- feature 模块实际 **13 个**（auth/basic/equipment/home/integration/label/message/production/profile/quality/report/warehouse/workflow），文档仅列 5 个
- 内容偏导航入口，缺乏专题深度

### 修复方案（寇豆码 · 部分工作流）

#### 5.1 主入口更新
- 版本号升级至 V0.8.0
- 补全 core 11 / feature 13 模块清单（含职责说明）
- 更新技术栈描述（Compose BOM 2024.06、AGP 8.13、Kotlin 1.9.24）

#### 5.2 新增专题页（在 `Forgex_Doc/安卓端/` 下）
| 专题文件 | 内容 |
|----------|------|
| `网络层与统一结果.md` | Retrofit 封装、拦截器、R<T> 模型对齐、错误处理 |
| `DataStore 会话持久化.md` | Token/Tenant 存储、迁移、加密 |
| `Compose 页面容器.md` | 设备识别 MOBILE/TABLET、通用容器、主题 |
| `导航与模块规范.md` | Navigation Compose、feature 模块边界、路由表 |
| `离线能力.md` | Room 本地库 + WorkManager 同步队列（联动问题9） |

#### 涉及文件
| 文件 | 动作 |
|------|------|
| `Forgex_Doc/安卓端/README.md` | 更新 |
| `Forgex_Doc/安卓端/网络层与统一结果.md` | 新增 |
| `Forgex_Doc/安卓端/DataStore 会话持久化.md` | 新增 |
| `Forgex_Doc/安卓端/Compose 页面容器.md` | 新增 |
| `Forgex_Doc/安卓端/导航与模块规范.md` | 新增 |
| `Forgex_Doc/安卓端/离线能力.md` | 新增（联动问题9） |

---

## 四、问题 6：三份规范空壳文档

### 现状
- `MyBatis-Plus 使用规范.md` / `Git 提交与开发规范.md` / `代码注释规范.md` 均只有索引页，无正文
- Git 规范版本号仍为 V0.6.5

### 修复方案（寇豆码 · 快速模式）

**MyBatis-Plus 使用规范.md**：
- 分页：`IPage` + `PaginationInnerInterceptor` 约定
- `LambdaQueryWrapper` / `LambdaUpdateWrapper` 条件拼装规范与禁忌
- 通用字段自动填充：`MetaObjectHandler` 实现（create_time/update_time/deleted/tenant_id）
- 多租户：`TenantLineInnerInterceptor` + SQL 约束注意事项
- 逻辑删除、乐观锁、批量操作规范
- 代码示例（Service/Mapper/Entity 标准写法）

**Git 提交与开发规范.md**：
- Conventional Commits 格式（feat/fix/docs/refactor/test/chore/perf）
- 分支模型（main/develop/feature/release/hotfix）与命名规范
- 日常开发与合并流程（PR/MR 规范、Code Review 入口）
- Java / 前端基础代码规范入口链接
- 升级至 V0.8.0

**代码注释规范.md**：
- 类/方法/参数注释完整性要求
- Java：Javadoc 规范
- TypeScript/Vue：JSDoc + 组件注释
- 业务注释与实现说明边界（什么必须写，什么不该写）
- TODO/FIXME 规范（署名 + 日期 + 关联 issue）

#### 涉及文件
| 文件 | 动作 |
|------|------|
| `Forgex_Doc/开发规范/规范文档/MyBatis-Plus 使用规范.md` | 补正文 |
| `Forgex_Doc/开发规范/规范文档/Git 提交与开发规范.md` | 补正文 + 升版本 |
| `Forgex_Doc/开发规范/规范文档/代码注释规范.md` | 补正文 |

---

## 五、问题 7：Vite 生产优化缺失

### 现状
- `vite.config.ts` 极简：仅 `vue()` 插件、`@` alias、dev server proxy
- **无 `build` 配置**：无 manualChunks、无 target、无 sourcemap、无 chunk 大小告警
- 依赖很重：three、echarts、xlsx、wangeditor、formily、vue-flow、tresjs 等，首屏不分包会非常大

### 修复方案（寇豆码 · 快速模式）

在 `vite.config.ts` 增加 `build` 配置：

```ts
build: {
  target: 'es2018',
  cssCodeSplit: true,
  sourcemap: false,            // 生产关闭，按需开启 hidden
  chunkSizeWarningLimit: 1500,
  rollupOptions: {
    output: {
      manualChunks: {
        'vendor-vue': ['vue', 'vue-router', 'pinia', 'vue-i18n'],
        'vendor-antd': ['ant-design-vue', '@ant-design/icons-vue'],
        'vendor-echarts': ['echarts', 'vue-echarts', 'apexcharts', 'vue3-apexcharts'],
        'vendor-editor': ['@wangeditor/editor', '@wangeditor/editor-for-vue'],
        'vendor-form': ['@form-create/ant-design-vue', '@formily/core', '@formily/vue', '@formily/antdv-x3'],
        'vendor-three': ['three', '@tresjs/core', '@tresjs/cientos'],
        'vendor-flow': ['@vue-flow/core', '@vue-flow/background', '@vue-flow/controls', '@vue-flow/minimap'],
        'vendor-xlsx': ['xlsx'],
      }
    }
  }
}
```

- 检查 `@formily/antdv-x3` 固定版本（当前 `latest`，建议锁定）
- Tree Shaking：确认重依赖均为 ESM（echarts/three 已支持），按需引入
- 可选：`build.reportCompressedSize` 关闭以加速构建

#### 涉及文件
| 文件 | 动作 |
|------|------|
| `Forgex_MOM/Forgex_Fronted/vite.config.ts` | 增强 build 配置 |
| `Forgex_MOM/Forgex_Fronted/package.json` | 锁定 @formily/antdv-x3 版本 |

---

## 六、问题 8：主密钥"自举"风险

### 现状
- `KmsServiceImpl.getMasterKey()`：从 `ConfigService`（数据库表）读取 `security.kms.master`
- **不存在则自动生成随机密钥并写回数据库** —— 主密钥与业务密钥同库存储
- 风险：数据库被攻破（SQL 注入 / 备份泄露 / 拖库）→ 主密钥暴露 → 所有业务密钥可解密 → 全盘失守

### 修复方案（高见远 设计 + 寇豆码 实现 · 标准 SOP）

#### 8.1 架构师先出设计（高见远）
- 主密钥来源优先级与回退策略
- 迁移兼容方案（存量环境平滑切换）
- 密钥轮换流程

#### 8.2 实现要点（寇豆码）

**主密钥来源改为外部注入，不再落库**，按优先级读取：
1. 环境变量 `FORGEX_KMS_MASTER_KEY_HEX`（推荐，容器/K8s 通过 Secret 注入）
2. 外部密钥文件 `FORGEX_KMS_MASTER_KEY_FILE`（默认 `${FORGEX_LICENSE_DIR}/kms.key`，权限 600，由运维保管）

**移除自动生成写回数据库逻辑**：
- 主密钥缺失时，启动直接失败并输出明确指引（如何生成、放哪、设什么变量）
- 不再"自举"——杜绝主密钥与业务密钥同库

**新增密钥生成工具**：
- `Forgex_Build/delivery/linux/scripts/gen-kms-master-key.sh`：生成 32 字节随机密钥，输出 hex 与写入文件两种模式，提示运维设置环境变量

**配置开关兼容迁移**：
- `security.kms.master-source=env|file|legacy-db`（默认 `env`）
- `legacy-db` 仅作为迁移期回退，启动时打 WARN 日志，并在文档中标注不推荐

**文档**：
- `Forgex_Doc/后端/安全/KMS 主密钥管理.md`：密钥生成、注入、轮换、备份恢复流程

#### 涉及文件
| 文件 | 动作 |
|------|------|
| `Forgex_MOM/Forgex_Backend/Forgex_Sys/src/main/java/com/forgex/sys/service/impl/KmsServiceImpl.java` | 改造主密钥获取逻辑 |
| `Forgex_MOM/Forgex_Backend/Forgex_Sys/src/main/java/com/forgex/sys/domain/config/CryptoConfig.java` | 增加来源配置 |
| `Forgex_Build/delivery/linux/scripts/gen-kms-master-key.sh` | 新增 |
| `Forgex_Build/delivery/linux/scripts/install.sh` | 生成 kms.key 提示 |
| `Forgex_Doc/后端/安全/KMS 主密钥管理.md` | 新增 |

---

## 七、问题 9：移动端无离线能力

### 现状
- `libs.versions.toml` **已声明 room 依赖**（room-runtime/room-ktx/room-compiler），但全工程无任何 `Database` 类 —— 依赖声明了却未使用
- 无 WorkManager，无同步队列，无离线缓存
- 网络断开即完全不可用

### 修复方案（高见远 设计 + 寇豆码 实现 · 标准 SOP）

#### 9.1 架构师先出设计（高见远）
- 离线数据模型与同步策略（last-write-wins / 服务端权威）
- 冲突解决策略
- 同步队列数据结构与 WorkManager 调度

#### 9.2 实现要点（寇豆码）

**新增 `core/database` 模块**（Room）：
- `ForgexDatabase`（@Database，版本管理）
- 基础 Entity / Dao 抽象（`BaseEntity`、`BaseDao`）
- `Converters`（时间/JSON 转换）
- Hilt 提供 `@Provides RoomDatabase`

**新增 `core/sync` 模块**（同步队列 + WorkManager）：
- `SyncQueue` 本地操作队列表（pending/processing/success/failed）
- `SyncWorker`（WorkManager）周期性/触发式上传本地变更
- 冲突解决器接口 + 默认 LWW 实现
- 网络状态监听（`NetworkMonitor`），联网自动触发同步

**WorkManager 依赖加入 version catalog**：
- `androidx-work-runtime`、`androidx-work-runtime-ktx`、`androidx-hilt-work`

**首批落地场景**（验证离线链路）：
- `feature/workflow` 待办列表离线缓存（只读缓存优先，断网可查看历史待办）
- `feature/message` 消息离线缓存 + 离线已读回执入队

**文档联动**：问题5 的 `离线能力.md` 同步说明

#### 涉及文件
| 文件 | 动作 |
|------|------|
| `Forgex_MOM/Forgex_Mobile_Android/core/database/` | 新增模块 |
| `Forgex_MOM/Forgex_Mobile_Android/core/sync/` | 新增模块 |
| `Forgex_MOM/Forgex_Mobile_Android/gradle/libs.versions.toml` | 增 WorkManager 依赖 |
| `Forgex_MOM/Forgex_Mobile_Android/settings.gradle.kts` | 注册新模块 |
| `Forgex_MOM/Forgex_Mobile_Android/feature/workflow/` | 接入离线缓存 |
| `Forgex_MOM/Forgex_Mobile_Android/feature/message/` | 接入离线缓存 |
| `Forgex_Doc/安卓端/离线能力.md` | 新增（联动问题5） |

---

## 八、执行编排（确认后启动）

```
主理人齐活林
   │
   ├─【第一波·可并行】
   │   ├─ 组A 文档（问题5+6）→ 寇豆码
   │   ├─ 组B 运维+Portainer（问题4）→ 寇豆码
   │   └─ 组C 前端（问题7）→ 寇豆码
   │
   ├─【第二波·需架构设计】
   │   ├─ 组D 后端安全（问题8）→ 高见远设计 → 寇豆码实现 → 严过关测试
   │   └─ 组E 移动端离线（问题9）→ 高见远设计 → 寇豆码实现 → 严过关测试
   │
   └─ 汇总交付 → 用户验收
```

- 第一波三组互不依赖，由寇豆码按 A→B→C 顺序承接（同一工程师，避免上下文切换）
- 第二波两组需架构师先出设计，工程师实现后交 QA 测试
- 所有产出经主理人中转，最终汇总交付

---

## 九、待确认事项

1. **Portainer 部署形态**：是否需要 `install.sh` 自动部署 Portainer CE 容器，还是假定 Portainer 已由运维独立部署？
2. **中间件纳入 compose**：MySQL/Redis/Nacos 是否一并纳入 `portainer-stack.yml`（生产环境通常中间件独立部署，建议纳入 compose 但标注"仅适用 dev/test，生产用外部中间件"）？
3. **KMS 迁移策略**：存量环境主密钥目前在数据库，是否需要提供 `legacy-db` 回退开关做平滑迁移？还是强制切换（需停服一次）？
4. **离线能力首批范围**：是否同意首批仅落地 workflow 待办 + message 消息两个场景，其余 feature 后续按需接入？
5. **执行顺序**：是否同意第一波并行 + 第二波架构先行的编排？还是希望调整优先级（例如优先解决问题8 安全风险）？

---

> 请逐条确认上述方案与待确认事项，确认后我将正式建立团队 `software-forgex-fixes` 并按编排启动标准 SOP 执行。
