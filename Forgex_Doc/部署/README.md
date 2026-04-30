# 部署文档导航

> 版本：**V0.6.1**  
> 更新时间：**2026-04-22**

本目录用于统一维护 Forgex 的部署、授权、环境配置与交付说明。

## 一、核心专题

| 主题 | 实现逻辑 | 使用方式 |
|---|---|---|
| 授权说明 | [进入](./授权说明实现逻辑.md) | [进入](./授权说明使用方式.md) |

## 二、目录内容

| 位置 | 说明 |
|---|---|
| `README.md` | 部署文档导航 |
| `授权说明.md` | 兼容索引页 |
| `授权说明实现逻辑.md` | 授权文件生成、校验、密钥管理等技术实现 |
| `授权说明使用方式.md` | 开发、测试、演示、生产环境授权申请与导入流程 |
| `nacos配置/` | Nacos 配置中心示例配置文件 |

## 三、构建与交付工程

Forgex_Build 是 Forgex 的统一交付工程，负责生成交付物：

### 3.1 目录结构

| 目录 | 说明 |
|---|---|
| `delivery/windows/installer/` | Windows 安装器（Inno Setup 脚本、启停脚本、WinSW 服务包装） |
| `delivery/linux/scripts/` | Linux 部署脚本（install.sh、upgrade.sh、rollback.sh、backup.sh、restore.sh） |
| `license-tools/` | 授权签发工具与请求客户端 |
| `shared/` | 公共模板（Nacos、Nginx、授权示例、安装配置） |
| `manifest/` | 交付清单（服务清单、版本清单、画像） |
| `staging/` | 临时收集目录，打包前汇总 |
| `dist/` | 最终交付产物目录 |

前端为 Vue/Vite 项目，构建交付包时会自动在 `Forgex_MOM/Forgex_Fronted` 执行 `npm run build`，并将生成的 `dist` 静态文件复制到交付包的 `frontend/` 目录。若未安装 Node.js/npm，或前端构建失败，交付收集会直接失败，避免把旧版 `dist` 打入安装包。

Windows 交付包会把 `Forgex_Build/shared/nginx/windows` 中的 Windows 版 Nginx 运行时复制到安装包的 `nginx/` 目录，并同时带上 `nginx/forgex.conf.template`。安装阶段会基于前端端口、网关端口和安装目录生成 `nginx/forgex.conf`，控制中心启动前端时优先使用安装目录内置的 `nginx/nginx.exe`。

### 3.2 构建脚本

| 脚本 | 说明 |
|---|---|
| `build-all.ps1` | 一键执行全量构建 |
| `build-windows.ps1` | 组装 Windows 交付包 |
| `build-linux.ps1` | 组装 Linux 交付包 |
| `collect-artifacts.ps1` | 收集前端、后端、模板、授权客户端到 staging |

### 3.3 构建命令

在 Forgex_Build 目录执行：

```powershell
# 全量构建
powershell -ExecutionPolicy Bypass -File build-all.ps1 -Version 1.0.0

# 仅构建 Windows 交付包
powershell -ExecutionPolicy Bypass -File build-windows.ps1 -Version 1.0.0

# 构建 Windows 交付包并直接编译安装 EXE
powershell -ExecutionPolicy Bypass -File build-windows.ps1 -Version 1.0.0 -CompileInstaller

# 仅构建 Linux 交付包
powershell -ExecutionPolicy Bypass -File build-linux.ps1 -Version 1.0.0
```

构建产物：
- `dist/windows/Forgex-Windows-Package-{Version}.zip`：Windows 交付包
- `dist/windows/Forgex-Setup-{Version}.exe`：Windows 安装程序（使用 `-CompileInstaller` 或手工通过 Inno Setup 编译生成）
- `dist/linux/forgex-linux-bundle-{Version}.tar.gz`：Linux 交付包

## 四、Windows 部署

### 4.1 安装流程

1. 解压 `Forgex-Windows-Package-{Version}.zip`
2. 使用 Inno Setup 编译 `ForgexSetup.iss` 生成安装包
3. 运行安装程序，输入实例编码和部署环境
4. 安装程序自动创建目录结构和配置文件

### 4.2 安装程序特性

安装程序（ForgexSetup.iss）提供：
- 安装器语言选择（中文 / English），内置按钮、退出确认、安装完成页跟随所选语言显示
- 实例编码输入页面（默认 `ACME_PROD`）
- 部署环境选择页面（dev/test/prod/yanshi）
- 中间件地址输入页面（Nacos、Redis、RocketMQ、MySQL、前端端口）
- 安装完成后可打开 Forgex 控制中心，控制中心支持中文 / English 运行时切换
- 自动创建目录结构
- 自动生成 `config/install-config.yml`
- 自动生成 `nginx/forgex.conf`，前端根目录指向安装后的 `frontend/`，`/api/` 反向代理到网关服务端口
- 创建开始菜单快捷方式（控制中心、启停服务、打开前端）

控制中心启动前端时优先使用安装目录中的 `nginx.exe` 或系统 PATH 中的 Nginx，并加载 `nginx/forgex.conf`；如果客户机器没有 Nginx，则自动回退到控制中心内置静态 Web 服务，保证安装包仍可一键预览和调试。

### 4.3 目录结构

安装后的目录结构：

```text
D:\Forgex_{INSTANCE_CODE}\
├── app\                    # 应用程序（JAR 文件）
├── config\                 # 配置文件
│   └── install-config.yml  # 安装配置
├── data\                   # 数据目录
│   └── uploads\            # 上传文件
├── license\                # 授权目录
│   ├── license.lic         # 授权文件
│   ├── public-key.base64   # 公钥文件
│   └── request-info.json   # 请求文件
├── logs\                   # 日志目录
├── scripts\                # 脚本目录
│   ├── install.ps1         # 安装脚本
│   ├── start-all.ps1       # 启动脚本
│   └── stop-all.ps1        # 停止脚本
├── backup\                 # 备份目录
├── tools\                  # 工具目录
├── winsw\                  # Windows 服务包装
├── frontend\               # 前端静态文件
├── services\               # 后端服务 JAR
├── nginx\                  # Windows Nginx 运行时、配置模板、生成后的 forgex.conf
├── nacos\                  # Nacos 环境变量
└── license-tools\          # 授权客户端
    └── request-client\     # 请求授权客户端
```

## 五、Linux 部署

### 5.1 安装流程

执行 `install.sh` 脚本：

```bash
./install.sh ACME_PROD prod
```

参数：
- 第一个参数：实例编码（默认 `ACME_PROD`）
- 第二个参数：部署环境（默认 `prod`）

### 5.2 安装脚本功能

安装脚本自动创建：
- 目录结构（app、config、data、license、logs、backup、tools）
- `.env` 环境变量文件
- 各服务端口配置

### 5.3 目录结构

安装后的目录结构：

```text
/opt/Forgex_{INSTANCE_CODE}/
├── app\                    # 应用程序
├── config\                 # 配置文件
├── data\                   # 数据目录
│   └── uploads\            # 上传文件
├── license\                # 授权目录
├── logs\                   # 日志目录
│   ├── gateway\            # 网关日志
│   ├── auth\               # 认证日志
│   ├── sys\                # 系统日志
│   └── ...                 # 其他服务日志
├── scripts\                # 脚本目录
├── backup\                 # 备份目录
├── tools\                  # 工具目录
├── frontend\               # 前端静态文件
├── services\               # 后端服务 JAR
├── nginx\                  # Nginx 配置
├── nacos\                  # Nacos 环境变量
├── license-tools\          # 授权客户端
├── .env                    # 环境变量
├── docker-compose.yml      # Docker Compose 配置
└── *.sh                    # 部署脚本
```

### 5.4 Docker Compose 部署

使用 Docker Compose 部署各服务：

```bash
# 生成 docker-compose.yml
envsubst < docker-compose.yml.template > docker-compose.yml

# 启动服务
docker compose up -d
```

服务清单（参考 `docker-compose.yml.template`）：

| 服务 | 默认端口 | 说明 |
|---|---|---|
| gateway | 9000 | 网关服务，需授权目录挂载 |
| auth | 9001 | 认证服务 |
| sys | 9002 | 系统服务，需授权目录和上传目录挂载 |
| basic | 9003 | 基础服务 |
| job | 9004 | 任务调度服务，需授权目录挂载 |
| workflow | 9005 | 工作流服务 |
| integration | 9007 | 集成服务 |
| report | 8084 | 报表服务 |

### 5.5 部署脚本清单

| 脚本 | 说明 | 状态 |
|---|---|---|
| `install.sh` | 初始化安装，创建目录结构和环境变量 | 完成 |
| `upgrade.sh` | 升级部署，替换应用程序版本 | TODO |
| `rollback.sh` | 回滚部署，恢复到上一版本 | TODO |
| `backup.sh` | 备份数据和配置 | TODO |
| `restore.sh` | 恢复数据和配置 | TODO |

## 六、环境变量配置

### 6.1 核心环境变量

| 变量 | 说明 | 示例 |
|---|---|---|
| `FORGEX_INSTANCE_CODE` | 实例编码 | `ACME_PROD` |
| `FORGEX_DEPLOYMENT_PROFILE` | 部署环境展示标识，不再驱动 Spring profile | `dev/test/prod/yanshi` |
| `FORGEX_NACOS_NAMESPACE` | Nacos 命名空间 | `forgex_dev` |
| `FORGEX_NACOS_GROUP` | Nacos 分组 | `DEFAULT_GROUP` |
| `FORGEX_NACOS_DISCOVERY_IP` | 服务注册到 Nacos 的 IP，开发环境建议固定 | `127.0.0.1` |
| `FORGEX_DATASOURCE_CONFIG` | 默认数据源 Nacos 配置文件名 | `datasource-forgex-dev.yml` |
| `FORGEX_INTEGRATION_DATASOURCE_CONFIG` | 集成平台数据源 Nacos 配置文件名 | `datasource-forgex-integration-dev.yml` |
| `FORGEX_HOME` | 安装根目录 | `/opt/Forgex_ACME_PROD` |
| `FORGEX_LICENSE_DIR` | 授权目录 | `/opt/Forgex_ACME_PROD/license` |
| `FORGEX_UPLOAD_DIR` | 上传目录 | `/opt/Forgex_ACME_PROD/data/uploads` |
| `FORGEX_LOG_DIR` | 日志目录 | `/opt/Forgex_ACME_PROD/logs` |
| `FORGEX_BACKUP_DIR` | 备份目录 | `/opt/Forgex_ACME_PROD/backup` |
| `FORGEX_NACOS_ADDR` | Nacos 地址 | `127.0.0.1:8848` |
| `FORGEX_REDIS_ADDR` | Redis 地址 | `127.0.0.1:6379` |
| `FORGEX_MYSQL_URL` | MySQL URL | `jdbc:mysql://127.0.0.1:3306/forgex` |
| `FORGEX_VERSION` | 应用版本 | `1.0.0` |

### 6.2 服务端口变量

| 变量 | 默认值 | 说明 |
|---|---|---|
| `FORGEX_GATEWAY_PORT` | 9000 | 网关端口 |
| `FORGEX_AUTH_PORT` | 9001 | 认证服务端口 |
| `FORGEX_SYS_PORT` | 9002 | 系统服务端口 |
| `FORGEX_BASIC_PORT` | 9003 | 基础服务端口 |
| `FORGEX_JOB_PORT` | 9004 | 任务调度端口 |
| `FORGEX_INTEGRATION_PORT` | 9007 | 集成服务端口 |
| `FORGEX_WORKFLOW_PORT` | 9005 | 工作流端口 |
| `FORGEX_REPORT_PORT` | 8084 | 报表服务端口 |

## 七、Nacos 配置

Nacos 配置示例存放在 `nacos配置/DEFAULT_GROUP/` 目录：

| 文件 | 说明 |
|---|---|---|
| `common.yml` | 公共配置 |
| `forgex-common.yml` | Forgex 公共配置 |
| `datasource-forgex-dev.yml` | 开发环境数据源配置 |
| `datasource-forgex-integration-dev.yml` | 集成模块数据源配置 |
| `redis.yml` | Redis 配置 |
| `sa-token.yml` | Sa-Token 认证配置 |
| `captcha.yaml` | 验证码配置 |
| `rocketmq.yml` | RocketMQ 配置 |

## 八、配套资料

- [文档中心首页](../README.md)
- [网关与路由使用方式](../后端/模块专题/网关与路由使用方式.md)
- [Forgex_Build README](../../Forgex_Build/README.md)

## 九、Windows 控制台部署补充

Windows 安装包会把前端静态资源、后端 Java 服务 JAR、授权公钥、脚本、Windows 版 Nginx 运行时和 Forgex Control Center 一起放入安装目录。Nacos、Redis、RocketMQ、MySQL 按外部中间件处理，安装时填写地址，不由 Forgex 安装包自动安装。

安装后关键文件如下：

```text
D:\Forgex_{INSTANCE_CODE}\
├─ config\install-config.yml
├─ config\forgex-control.json
├─ frontend\
├─ nginx\nginx.exe
├─ nginx\conf\mime.types
├─ nginx\forgex.conf
├─ services\
├─ license\public-key.base64
├─ license\request-info.json
├─ license\license.lic
├─ tools\control-center\ForgexControlCenter.exe
└─ scripts\start-all.ps1 / stop-all.ps1
```

Forgex Control Center 功能：
- 读取 `config/forgex-control.json` 控制本机 Java 服务。
- 显示客户机器码。
- 生成 `license/request-info.json`，用于提交给授权中心。
- 导入授权中心签发的 `license.lic`。
- 启动、停止、查看各后端 Java 服务状态。

服务控制策略：
- 如果安装包内存在 WinSW.exe，安装脚本会注册 Windows 服务。
- 如果没有 WinSW.exe，控制台会以 Java 进程模式启动和停止服务，并在 `data/service-state` 记录 pid。
- 启动服务时会注入 `FORGEX_HOME`、`FORGEX_LICENSE_DIR`、`FORGEX_NACOS_ADDR`、`FORGEX_REDIS_ADDR`、`FORGEX_ROCKETMQ_NAME_SERVER`、`FORGEX_MYSQL_URL` 等环境变量。
