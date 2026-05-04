# 部署文档导航

> 版本：**V0.6.1**  
> 更新时间：**2026-04-22**

本目录用于统一维护 Forgex 的部署、授权、环境配置与交付说明。

## 一、核心专题

| 主题 | 实现逻辑 | 使用方式 |
|---|---|---|
| 授权说明 | [进入](./授权说明实现逻辑.md) | [进入](./授权说明使用方式.md) |
| 部署软件 | [进入](./部署软件实现逻辑与二开说明.md) | [进入](#三构建与交付工程) |

## 二、目录内容

| 位置 | 说明 |
|---|---|
| `README.md` | 部署文档导航 |
| `授权说明.md` | 兼容索引页 |
| `授权说明实现逻辑.md` | 授权文件生成、校验、密钥管理等技术实现 |
| `授权说明使用方式.md` | 开发、测试、演示、生产环境授权申请与导入流程 |
| `部署软件实现逻辑与二开说明.md` | Windows 安装器、控制中心、授权工具、构建脚本的编译工具、技术栈、实现链路和二开入口 |
| `nacos配置/` | Nacos 配置中心示例配置文件 |
| `数据库初始化脚本/` | Windows 交付包一键初始化 MySQL 使用的建表与基础数据脚本 |

## 三、构建与交付工程

Forgex_Build 是 Forgex 的统一交付工程，负责生成交付物：

### 3.1 目录结构

| 目录 | 说明 |
|---|---|
| `delivery/windows/installer/` | Windows 安装器（Inno Setup 脚本、启停脚本、WinSW 服务包装） |
| `delivery/linux/scripts/` | Linux 部署脚本（install.sh、upgrade.sh、rollback.sh、backup.sh、restore.sh） |
| `license-tools/` | 授权签发工具与请求客户端（交付包内仅保留请求客户端发布产物） |
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
- 部署环境选择页面仅开放 `prod`（生产环境）和 `yanshi`（演示环境）；`dev`、`test` 为公司内部环境，不在客户安装器中提供
- Nacos 命名空间默认跟随部署环境：选择 `prod` 时默认为 `prod`，选择 `yanshi` 时默认为 `yanshi`；用户也可以在中间件页面手工覆盖
- 中间件地址输入页面（Nacos、Redis、RocketMQ、MySQL、前端端口）
- 运行目录输入页面，日志目录默认跟随安装目录的 `logs` 子目录，也可指定到服务器已有且可写的其他磁盘
- 安装完成后可打开 Forgex 控制中心，控制中心支持中文 / English 运行时切换
- 控制中心启动服务时会逐个记录启动结果；单个后端服务启动失败不会导致控制中心退出，控制中心自身异常会写入 `logs/control-center/control-center-error.log`
- 自动创建目录结构
- 自动生成 `config/install-config.yml`
- 自动生成 `nginx/forgex.conf`，前端根目录指向安装后的 `frontend/`，`/api/` 反向代理到网关服务端口
- 自动创建日志根目录及 `auth`、`sys`、`basic`、`job`、`workflow`、`integration`、`report`、`gateway`、`nginx` 子目录，服务启动时也会再次兜底创建
- 自动携带 Windows JRE 到 `app/jre/`，客户机器无需再单独安装 Java
- 安装前会先尝试停止当前安装目录关联的 Forgex 服务与进程，并清理旧的 `app/jre/`；卸载时也会执行 `scripts/uninstall-cleanup.ps1`，尽量避免 Java 文件占用导致 JRE 目录残留
- 创建开始菜单快捷方式（控制中心、启停服务、打开前端、导入数据库、导入 Nacos 配置、修复运行配置）
- 交付包携带 `database-init/` 与 `nacos/DEFAULT_GROUP/`，可在服务器上直接一键导入 MySQL 数据库、Nacos 命名空间和配置

控制中心启动前端时优先使用安装目录中的 `nginx.exe` 或系统 PATH 中的 Nginx，并加载 `nginx/forgex.conf`；如果客户机器没有 Nginx，则自动回退到控制中心内置静态 Web 服务，保证安装包仍可一键预览和调试。

前端对外访问端口默认是 `18080`，网关服务端口默认是 `9000`。浏览器访问 `http://服务器IP:18080/api/...` 时会先进入 Nginx，再由 Nginx 转发到网关。`nginx/forgex.conf` 中 `/api/` 的 `proxy_pass` 必须配置为 `http://127.0.0.1:9000`，不能写成带尾部斜杠的 `http://127.0.0.1:9000/`，否则 Nginx 会提前剥掉 `/api` 前缀，导致网关无法匹配 `/api/sys/**` 等路由，表现为 HTTP 状态 200 但业务响应 `code=404`、提示“接口不存在”。旧安装目录可先停止服务，运行 `scripts/repair-runtime-config.bat` 重新生成 Nginx 配置，再重新启动服务。

Windows 默认安装根目录已调整为 `C:\Forgex_{INSTANCE_CODE}`，便于只有 `C` 盘的服务器直接部署。

卸载后如果只残留 `app/jre/`，通常是 `java.exe` 或已注册的 Windows 服务仍占用 JRE 文件。此残留不会改变业务数据和授权文件，但同目录再次安装时可能导致覆盖失败。可先重启服务器后删除残留目录，或直接运行新版安装器；新版安装器会在安装开始前自动执行清理。

### 4.3 目录结构

安装后的目录结构：

```text
C:\Forgex_{INSTANCE_CODE}\
├── app\                    # 应用程序目录
│   └── jre\                # Windows 内置 JRE
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
│   ├── stop-all.ps1        # 停止脚本
│   ├── import-database.bat # 一键导入 MySQL 数据库
│   ├── import-nacos-config.bat # 一键导入 Nacos 命名空间与配置
│   └── repair-runtime-config.bat # 修复已安装环境的 profile、namespace 和服务 XML
├── backup\                 # 备份目录
├── database-init\          # MySQL 初始化脚本，按 forgex_*.sql 文件名建库并导入
├── tools\                  # 工具目录
├── winsw\                  # Windows 服务包装
├── frontend\               # 前端静态文件
├── services\               # 后端服务 JAR
├── nginx\                  # Windows Nginx 运行时、配置模板、生成后的 forgex.conf
├── nacos\                  # Nacos 环境变量与配置导入文件
│   └── DEFAULT_GROUP\      # Nacos 配置中心 dataId 文件
└── license-tools\          # 授权客户端
    └── request-client\     # 请求授权客户端发布产物（Windows: FxLicenseRequest.exe，Linux: FxLicenseRequest）
```

### 4.4 首次初始化顺序

Windows 服务器首次部署时，中间件和业务服务按以下顺序处理：

1. 安装并启动 MySQL、Redis、RocketMQ、Nacos。
2. 运行 Forgex 安装程序，部署环境默认选择 `yanshi`；Nacos 命名空间默认跟随部署环境，默认也是 `yanshi`；日志目录默认是安装目录下的 `logs`，服务器没有 D 盘时不要指定到 `D:\forgex\log`。
3. 在开始菜单 `Forgex` 目录运行 `导入数据库`，脚本会读取安装目录 `database-init/*.sql`，按文件名创建 `forgex_admin`、`forgex_common`、`forgex_history`、`forgex_job`、`forgex_workflow`、`forgex_scada`、`forgex_integration` 并导入数据。
4. 在开始菜单 `Forgex` 目录运行 `导入 Nacos 配置`，脚本会创建/复用 `yanshi` 命名空间，并导入 `nacos/DEFAULT_GROUP` 下的配置文件。
5. 打开 Forgex 控制中心，点击启动全部服务。

`import-database.bat` 默认使用 Nacos 数据源配置中的 MySQL 账号密码；当前交付配置默认是 `root / 123456`。如服务器密码不同，可运行：

```powershell
powershell -ExecutionPolicy Bypass -File C:\Forgex_ACME_PROD\scripts\import-database.ps1 -InstallRoot C:\Forgex_ACME_PROD -User root -PromptPassword
```

数据库脚本默认只在目标库为空时导入，避免误覆盖已有数据。干净重装需要重建库时，可显式追加 `-ResetDatabase`。

如果服务器已经按旧安装器生成了 `prod` 或错误日志目录配置，先停止服务，再运行开始菜单中的 `修复运行配置`；它会把 `config/forgex-control.json`、`config/install-config.yml` 和 `services/wrappers/*.xml` 统一修正为 `yanshi`，并把日志目录修正为安装目录下的 `logs` 后再重新启动服务。从新交付包的 `scripts/repair-runtime-config.bat` 临时运行时，脚本也会自动识别服务器上唯一的 `C:\Forgex_*` / `D:\Forgex_*` 安装目录。

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
├── license-tools\          # 授权客户端发布产物
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
| `FORGEX_DEPLOYMENT_PROFILE` | 部署环境展示标识，不再驱动 Spring profile；客户 Windows 安装器仅允许 `prod` / `yanshi` | `prod` |
| `FORGEX_NACOS_NAMESPACE` | Nacos 命名空间 | `forgex_dev` |
| `FORGEX_NACOS_GROUP` | Nacos 分组 | `DEFAULT_GROUP` |
| `FORGEX_NACOS_DISCOVERY_IP` | 服务注册到 Nacos 的 IP，开发环境建议固定 | `127.0.0.1` |
| `FORGEX_DATASOURCE_CONFIG` | 默认数据源 Nacos 配置文件名 | `datasource-forgex-dev.yml` |
| `FORGEX_INTEGRATION_DATASOURCE_CONFIG` | 集成平台数据源 Nacos 配置文件名 | `datasource-forgex-integration-dev.yml` |
| `FORGEX_HOME` | 安装根目录 | `/opt/Forgex_ACME_PROD` 或 `C:/forgex` |
| `FORGEX_LICENSE_DIR` | 授权目录 | `/opt/Forgex_ACME_PROD/license` |
| `FORGEX_UPLOAD_DIR` | 上传目录 | `/opt/Forgex_ACME_PROD/data/uploads` 或 `C:/forgex/data/uploads` |
| `FORGEX_LOG_DIR` | 日志目录 | `/opt/Forgex_ACME_PROD/logs` 或 `C:/forgex/logs` |
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

### 6.3 上传目录与日志目录说明

1. 文件上传目录优先走系统数据库配置 `file.upload.settings.localUploadPath`，部署时也可以通过 `FORGEX_UPLOAD_DIR` 提供启动兜底值。
2. 上传后的访问地址会写入 `sys_file_record.access_url`，前端页面直接使用该地址即可。
3. 运行日志目录不能走数据库配置。原因是日志系统在应用启动早期就要初始化，此时数据库连接和系统配置服务尚未可用。
4. 因此日志目录统一通过 `FORGEX_LOG_DIR` / `forgex.deployment.log-dir` 控制，上传目录与日志目录的配置方式是不同的。

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

Windows 安装包会把上述配置复制到安装目录 `nacos/DEFAULT_GROUP/`。如果 Nacos 控制台命名空间和配置列表为空，在安装目录执行：

```bat
scripts\import-nacos-config.bat
```

脚本会读取 `config/forgex-control.json` 中的 `nacosAddr`、`nacosNamespace`、`nacosGroup`，自动创建缺失命名空间并发布 `nacos/DEFAULT_GROUP/` 下的配置文件。重复执行会覆盖同名 `dataId`，适合初始化和配置修复。

如果需要手工指定命名空间或 Nacos 地址：

```bat
scripts\import-nacos-config.bat -NacosAddr 127.0.0.1:8848 -Namespace yanshi -Group DEFAULT_GROUP
```

## 八、配套资料

- [文档中心首页](../README.md)
- [网关与路由使用方式](../后端/模块专题/网关与路由使用方式.md)
- [Forgex_Build README](../../Forgex_Build/README.md)

## 九、Windows 控制台部署补充

Windows 安装包会把前端静态资源、后端 Java 服务 JAR、Windows JRE、授权公钥、脚本、Windows 版 Nginx 运行时和 Forgex Control Center 一起放入安装目录。Nacos、Redis、RocketMQ、MySQL 按外部中间件处理，安装时填写地址，不由 Forgex 安装包自动安装。

安装后关键文件如下：

```text
C:\Forgex_{INSTANCE_CODE}\
├─ config\install-config.yml
├─ config\forgex-control.json
├─ app\jre\bin\java.exe
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
- 启动服务时会注入 `FORGEX_HOME`、`FORGEX_LICENSE_DIR`、`FORGEX_UPLOAD_DIR`、`FORGEX_LOG_DIR`、`FORGEX_NACOS_ADDR`、`FORGEX_REDIS_ADDR`、`FORGEX_ROCKETMQ_NAME_SERVER`、`FORGEX_MYSQL_URL` 等环境变量。
