# Forgex_Build

`Forgex_Build` 是 Forgex 的统一交付工程，负责生成以下交付物：

- Windows 安装包与安装脚本
- Linux 离线部署包与部署脚本
- 授权签发工具与现场请求授权客户端
- Nacos、Nginx、安装配置等公共模板

业务源码保留在 `../Forgex_MOM`，本目录只负责构建、收集、打包、安装与部署。

## 目录说明

### 1. 交付目录

- `delivery/windows/installer/`
  - Windows 安装器相关内容。
  - `ForgexSetup.iss`：Inno Setup 安装脚本。
  - `scripts/`：Windows 安装、启停、Nacos 配置导入、数据库导入、运行配置修复与卸载清理脚本。
  - `winsw/`：Windows 服务包装模板。
- `delivery/linux/scripts/`
  - Linux 部署脚本目录。
  - 包含 `install.sh`、`upgrade.sh`、`rollback.sh`、`restore.sh`、`backup.sh`。
  - 包含 Linux 使用的 `.env.template` 与 `docker-compose.yml.template`。

### 2. 授权工具目录

- `license-tools/issuer/FxLicenseGenerator/`
  - 授权签发工具。
  - 用于授权中心或开发侧生成正式授权。
- `license-tools/issuer/FxLicenseIssuer/`
  - 授权签发端 Windows 图形化工具。
  - 用于授权人员选择 `request-info.json`、填写授权参数并生成 `license.lic`。
- `license-tools/publish-license-issuer.ps1`
  - 发布签发端图形化 EXE。
  - 默认输出到 `license-tools/issuer/publish/win-x64/FxLicenseIssuer.exe`。
- `license-tools/request-client/FxLicenseRequest/`
  - 现场请求授权客户端。
  - 用于生成 `request-info.json`、导入 `license.lic`。
- `license-tools/activate-dev-license.bat`
  - 开发环境一键授权脚本。
  - 仅对 `FORGEX_PROFILE=dev` 生效。
  - 用于开发公司统一分发执行，不可替代 test/yanshi/prod 正式授权流程。

### 3. 公共模板目录

- `shared/config-templates/`
  - 安装配置模板。
- `shared/license-templates/`
  - 授权示例模板，例如 `request-info.sample.json`。
- `shared/nacos/`
  - 部署时需要下发的环境变量模板。
- `Forgex_Doc/部署/nacos配置/`
  - Nacos 配置中心示例配置。
  - 构建时会复制到交付包 `nacos/DEFAULT_GROUP/`，供 `scripts/import-nacos-config.bat` 一键导入。
- `shared/nginx/`
  - Nginx 模板。

### 4. 构建产物目录

- `staging/`
  - 临时收集目录，供打包前汇总前端、后端、脚本和授权客户端。
- `dist/`
  - 最终交付产物目录。
  - `dist/windows/`：Windows 交付包。
  - `dist/linux/`：Linux 交付包。

### 5. 清单与构建脚本

- `manifest/`
  - 交付画像、服务清单、版本清单。
- `collect-artifacts.ps1`
  - 收集前端、后端、模板、授权客户端到 `staging/`。
- `build-windows.ps1`
  - 组装 Windows 交付包。
- `build-linux.ps1`
  - 组装 Linux 交付包。
- `build-all.ps1`
  - 一键执行全量构建。

## 当前分类约定

- Windows 安装软件放在 `delivery/windows/installer/`
- Linux 安装脚本放在 `delivery/linux/scripts/`
- 授权软件放在 `license-tools/issuer/`
- 请求授权客户端软件放在 `license-tools/request-client/`

这样区分后，交付人员可以明确知道：

- 哪些内容只给 Windows 安装包使用
- 哪些内容只给 Linux 部署现场使用
- 哪些内容属于授权签发端
- 哪些内容属于客户现场申请授权端
## Windows 轻量升级

`delivery/windows/installer/scripts/upgrade.ps1` 用于现场只替换前端 `frontend/` 和后端 `services/*.jar`。脚本不会重新安装 JRE、不会导入数据库、不会导入 Nacos；执行时会备份旧文件、停止服务、替换文件、更新控制配置和 WinSW JAR 路径，再按需启动服务。

示例：

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File C:\Forgex_ACME_PROD\scripts\upgrade.ps1 -InstallRoot C:\Forgex_ACME_PROD -PackageRoot D:\Forgex-Windows-Package-1.0.1
```