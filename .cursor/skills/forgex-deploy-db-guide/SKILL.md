---
name: forgex-deploy-db-guide
description: Forgex 部署和数据库环境指南，包含 Nacos 配置、数据库地址、Redis、RocketMQ、验证码、sa-token、部署文档、数据库安全/配置文档、SQL 环境问题和数据库初始化脚本。当处理 Forgex 部署、Nacos 配置、数据库连接、Redis/RocketMQ 配置、授权说明、数据库脚本、或修改 Forgex_Doc 部署/数据库相关文件时自动应用此技能。
disable-model-invocation: true
---

# Forgex 部署和数据库环境

## 一、部署架构

### 1.1 服务端口分配

| 服务 | 开发端口 | 生产端口 | 说明 |
|------|---------|---------|------|
| Gateway | 8080 | 9000 | API 网关 |
| Auth | 8081 | 9001 | 认证服务 |
| Sys | 8082 | 9002 | 系统服务 |
| Basic | - | 9003 | 基础服务 |
| Job | 8083 | 9004 | 任务调度 |
| Workflow | - | 9005 | 工作流服务 |
| Integration | - | 9007 | 集成平台 |
| Report | - | 8084 | 报表服务 |

### 1.2 中间件依赖

| 中间件 | 默认端口 | 说明 |
|--------|---------|------|
| MySQL | 3306 | 数据库 |
| Redis | 6379 | 缓存 |
| RocketMQ | 9876 | 消息队列 |
| Nacos | 8848 | 配置中心 |

## 二、Nacos 配置

### 2.1 配置文件位置

```
Forgex_Doc/部署/nacos配置/
├── DEFAULT_GROUP/
│   ├── captcha.yaml           # 验证码配置
│   ├── common.yml             # 公共配置
│   ├── datasource-forgex-dev.yml     # 开发环境数据源
│   ├── datasource-forgex-integration-dev.yml  # 集成环境数据源
│   ├── forgex-common.yml      # Forgex 公共配置
│   ├── lidao-common.yaml      # 理岛公共配置
│   ├── redis.yml              # Redis 配置
│   ├── rocketmq.yml           # RocketMQ 配置
│   └── sa-token.yml           # Sa-Token 配置
└── FORGEX_GROUP/
    └── shared-config.yaml     # 共享配置
```

### 2.2 核心配置说明

#### datasource-forgex-dev.yml

开发环境数据库配置，包含各服务的 JDBC 连接信息。

#### redis.yml

Redis 连接配置：
- host: Redis 服务器地址
- port: Redis 端口（默认 6379）
- password: Redis 密码
- database: 数据库索引

#### rocketmq.yml

RocketMQ 消息队列配置：
- name-server: NameServer 地址
- producer: 生产者配置
- consumer: 消费者配置

#### sa-token.yml

Sa-Token 认证配置：
- token-name: Token 名称（Authorization）
- timeout: Token 有效期（秒）
- is-concurrent: 是否允许并发登录
- token-style: Token 风格

#### captcha.yaml

验证码配置：
- 图片验证码配置
- 滑块验证码配置

## 三、授权说明

### 3.1 授权流程

详细授权流程见 `Forgex_Doc/部署/授权说明使用方式.md`

**关键步骤**：
1. 客户现场生成 `request-info.json`
2. 授权签发端签发 `license.lic`
3. 客户现场导入授权

### 3.2 开发环境授权

```bash
cd Forgex_Build\license-tools
activate-dev-license.bat
```

开发授权 3650 天有效，仅限 `FORGEX_PROFILE=dev` 环境。

### 3.3 环境变量

**开发环境**：
```text
FORGEX_PROFILE=dev
FORGEX_INSTANCE_CODE=DEV_LOCAL
FORGEX_HOME=D:/mine_product/forgex/forgex
FORGEX_LICENSE_DIR=D:/mine_product/forgex/forgex/license
FORGEX_LICENSE_ENABLED=true
```

**生产环境（Linux）**：
```text
FORGEX_INSTANCE_CODE=ACME_PROD
FORGEX_PROFILE=prod
FORGEX_HOME=/opt/Forgex_ACME_PROD
FORGEX_LICENSE_DIR=/opt/Forgex_ACME_PROD/license
```

## 四、数据库

### 4.1 数据库初始化脚本

```
Forgex_Doc/部署/数据库初始化脚本/
├── forgex_admin.sql        # 管理服务数据库
├── forgex_common.sql       # 公共模块数据库
├── forgex_history.sql      # 历史数据数据库
├── forgex_integration.sql  # 集成平台数据库
├── forgex_job.sql          # 任务调度数据库
├── forgex_scada.sql        # SCADA 数据库
└── forgex_workflow.sql     # 工作流数据库
```

### 4.2 数据库脚本与修复

```
Forgex_Doc/数据库/脚本与修复/
├── README.md                        # 脚本导航
├── 20260501_common_import_upgrade.sql
├── 20260502_basic_unit_and_table_upgrade.sql
└── 20260504_invite_code_role_binding.sql
```

### 4.3 数据库设计规范

详见 `Forgex_Doc/开发规范/规范文档/数据库字段统一规范文档.md`

**关键规范**：
- 表名：小写 + 下划线
- 字段名：小写 + 下划线
- 主键：`id`（BIGINT，雪花算法）
- 审计字段：`create_by`、`create_time`、`update_by`、`update_time`、`deleted`
- 租户字段：`tenant_id`

### 4.4 TDE 透明加密

- **实现逻辑**: 见 `Forgex_Doc/数据库/安全与配置/TDE透明加密实现逻辑.md`
- **使用方式**: 见 `Forgex_Doc/数据库/安全与配置/TDE透明加密使用方式.md`

## 五、构建与交付

### 5.1 交付工程结构

```
Forgex_Build/
├── delivery/
│   ├── windows/installer/    # Windows 安装器
│   └── linux/scripts/        # Linux 部署脚本
├── license-tools/            # 授权工具
├── shared/                   # 公共模板
├── manifest/                 # 交付清单
├── staging/                  # 临时收集目录
└── dist/                     # 最终交付产物
```

### 5.2 构建命令

```powershell
# 全量构建
powershell -ExecutionPolicy Bypass -File build-all.ps1 -Version 1.0.0

# 仅构建 Windows 交付包
powershell -ExecutionPolicy Bypass -File build-windows.ps1 -Version 1.0.0

# 仅构建 Linux 交付包
powershell -ExecutionPolicy Bypass -File build-linux.ps1 -Version 1.0.0
```

### 5.3 构建产物

- `dist/windows/Forgex-Windows-Package-{Version}.zip`：Windows 交付包
- `dist/windows/Forgex-Setup-{Version}.exe`：Windows 安装程序
- `dist/linux/forgex-linux-bundle-{Version}.tar.gz`：Linux 交付包

## 六、环境配置

### 6.1 开发环境

- **代码路径**: `D:\mine_product\forgex`
- **配置文件**: `Forgex_MOM/Forgex_Backend/*/src/main/resources/application-dev.yml`
- **Nacos**: 本地或开发服务器 8848 端口
- **Redis**: 本地或开发服务器 6379 端口
- **MySQL**: 本地或开发服务器 3306 端口

### 6.2 测试环境

- **环境标识**: `test`
- **Nacos 命名空间**: `test`

### 6.3 演示环境

- **环境标识**: `yanshi`
- **Nacos 命名空间**: `yanshi`
- **安装器可选**: 是

### 6.4 生产环境

- **环境标识**: `prod`
- **Nacos 命名空间**: `prod`
- **安装器可选**: 是

## 七、常见问题

### 7.1 数据库连接问题

- 检查 Nacos 中的数据源配置
- 确认 MySQL 服务正常运行
- 验证用户名和密码

### 7.2 Redis 连接问题

- 检查 `redis.yml` 配置
- 确认 Redis 服务正常运行
- 验证密码和端口

### 7.3 授权问题

- 开发环境使用 `activate-dev-license.bat`
- 生产环境需要正式授权文件
- 检查 `FORGEX_PROFILE` 环境变量

## 八、关联文档

- **授权说明**: 见 `Forgex_Doc/部署/授权说明使用方式.md`
- **数据库规范**: 见 `Forgex_Doc/开发规范/规范文档/数据库字段统一规范文档.md`
- **TDE 加密**: 见 `Forgex_Doc/数据库/安全与配置/` 目录
- **开发规范**: 见 `forgex-development-standards` skill
