# Portainer 管理手册

> 分类：部署 / 运维
> 版本：V0.8.0
> 关联模块：Forgex_Build/delivery/linux/scripts

本文说明 Forgex 通过 Portainer 管理容器全生命周期的操作流程，包括 Stack 导入、镜像管理、日志查看、升级/回滚、API 调用与常见问题。

---

## 一、概述

Forgex Linux 部署采用 **Portainer CE** 作为容器管理平台，实现全栈可视化管理：

- 8 个业务服务（gateway/auth/sys/basic/job/integration/workflow/report）
- 3 个中间件服务（MySQL/Redis/Nacos，仅 dev/test 环境内嵌）
- 1 个 Portainer 管理容器

双路径管理：
- **路径一（推荐）**：通过 Portainer Web UI 导入 Stack、管理容器/镜像/日志/卷
- **路径二（脚本）**：运维脚本通过 Portainer REST API（curl）调用 Stack 更新，兼容纯命令行环境回退到 `docker compose`

---

## 二、Portainer 部署

### 2.1 自动部署

`install.sh` 在初始化实例时自动检测并部署 Portainer CE：

```bash
# 初始化实例（自动部署 Portainer）
bash install.sh ACME_PROD prod
```

自动部署行为：
1. 检测 Docker 是否可用
2. 检测 Portainer CE 是否已运行（`docker ps` 查 `portainer/portainer-ce`）
3. 未运行则拉取镜像、创建数据卷、启动容器
4. 端口映射：`9443:9443`（HTTPS UI）、`9001:9000`（HTTP API，避开网关 9000）

### 2.2 手动部署

如自动部署失败，可手动执行：

```bash
docker pull portainer/portainer-ce:latest
docker volume create portainer_data
docker run -d \
  --name portainer \
  --restart=unless-stopped \
  -p 9443:9443 \
  -p 9001:9000 \
  -v /var/run/docker.sock:/var/run/docker.sock \
  -v portainer_data:/data \
  portainer/portainer-ce:latest
```

### 2.3 首次登录

1. 浏览器访问 `https://<服务器IP>:9443`
2. 首次访问需设置管理员密码（至少 12 位）
3. 选择 `Get Started` 使用本地 Docker 环境（自动检测到 `local` endpoint）
4. 点击 `local` 进入管理界面

> 注意：Portainer 使用自签名 HTTPS 证书，浏览器会提示不安全连接，选择「继续前往」即可。

---

## 三、Stack 导入与部署

### 3.1 通过 Web UI 导入 Stack

1. 进入 Portainer ->左侧菜单 `Stacks` ->点击 `Add stack`
2. 填写 Stack 名称：`forgex`
3. 选择 `Web editor`，粘贴 `portainer-stack.yml` 内容（或在 `Upload` 选项卡上传文件）
4. 在 `Environment variables` 区域添加 `.env` 中的变量
5. 点击 `Deploy the stack`

Stack 文件位置：`${FORGEX_HOME}/app/portainer-stack.yml`

### 3.2 环境变量

Stack 部署时需配置以下环境变量（与 `.env` 文件一致）：

| 变量 | 说明 | 示例 |
|------|------|------|
| `FORGEX_INSTANCE_CODE` | 实例编码 | `ACME_PROD` |
| `FORGEX_PROFILE` | 运行环境 | `prod` |
| `FORGEX_VERSION` | 镜像版本 | `1.0.0` |
| `FORGEX_LICENSE_DIR` | License 目录 | `/opt/Forgex_ACME_PROD/license` |
| `FORGEX_UPLOAD_DIR` | 上传目录 | `/opt/Forgex_ACME_PROD/data/uploads` |
| `FORGEX_LOG_DIR` | 日志目录 | `/opt/Forgex_ACME_PROD/logs` |
| `FORGEX_NACOS_ADDR` | Nacos 地址 | `nacos:8848`（内嵌）或 `127.0.0.1:8848`（外部） |
| `FORGEX_REDIS_ADDR` | Redis 地址 | `redis:6379`（内嵌）或 `127.0.0.1:6379`（外部） |
| `FORGEX_MYSQL_URL` | MySQL JDBC URL | `jdbc:mysql://mysql:3306/forgex`（内嵌）或外部地址 |
| `FORGEX_KMS_MASTER_KEY_FILE` | KMS 主密钥文件 | `${FORGEX_LICENSE_DIR}/kms.key` |
| `FORGEX_GATEWAY_PORT` | 网关端口 | `9000` |
| `COMPOSE_PROJECT_NAME` | Compose 项目名 | `forgex_acme_prod` |

### 3.3 中间件服务

`portainer-stack.yml` 中的 MySQL/Redis/Nacos 标注了 `profiles: ["dev", "test"]`：

- **dev/test 环境**：启用中间件服务（Stack 部署时勾选对应 profile，或命令行 `--profile dev`）
- **生产环境**：不启用，使用外部中间件，移除 middleware 服务定义

### 3.4 通过 Portainer API 部署

```bash
# 加载 Portainer API 封装
source ${FORGEX_HOME}/scripts/portainer-api.sh

# 设置凭据
export PORTAINER_URL="https://127.0.0.1:9443"
export PORTAINER_USER="admin"
export PORTAINER_PASSWORD="<你的密码>"

# 认证
portainer_login

# 部署或更新 Stack
portainer_deploy_stack "${FORGEX_HOME}/app/portainer-stack.yml" "forgex"

# 列出容器
portainer_list_containers

# 获取容器日志
portainer_get_container_logs "forgex_acme_prod-gateway-1" 200
```

---

## 四、镜像管理

### 4.1 通过 Web UI

1. 进入 `Images` 页面
2. `Pull image`：输入镜像名（如 `forgex/forgex-gateway:1.1.0`），点击 Pull
3. `List`：查看本地所有镜像，可按名称过滤
4. 点击镜像可查看详情、Tag、删除

### 4.2 通过命令行

```bash
# 拉取新版本镜像
docker pull forgex/forgex-gateway:1.1.0

# 列出 forgex 镜像
docker images | grep forgex

# 清理未使用镜像
docker image prune -a
```

---

## 五、日志查看

### 5.1 通过 Web UI

1. 进入 `Containers` 页面
2. 找到目标容器，点击 `Logs` 图标
3. 可设置 `Follow`（实时跟踪）、`Tail`（最后 N 行）、`Since`（时间范围）

### 5.2 通过命令行

```bash
# 实时查看日志
docker logs -f forgex_acme_prod-gateway-1

# 查看最后 200 行
docker logs --tail 200 forgex_acme_prod-sys-1

# 查看指定时间后的日志
docker logs --since "2026-06-21T10:00:00" forgex_acme_prod-auth-1
```

---

## 六、升级操作

### 6.1 通过运维脚本（推荐）

```bash
# 升级到新版本（自动备份 -> 滚动重启 -> 健康检查 -> 失败自动回滚）
bash ${FORGEX_HOME}/scripts/upgrade.sh 1.1.0
```

脚本流程：
1. 拉取新版本全部镜像
2. 自动执行 `backup.sh` 全量备份
3. 更新 compose 文件镜像版本
4. 按顺序逐服务滚动重启：gateway -> auth -> sys -> basic -> job -> integration -> workflow -> report
5. 每个服务启动后健康检查（最多 60 秒）
6. 任意服务失败自动触发 `rollback.sh`
7. 升级历史记录到 `${FORGEX_HOME}/upgrade.log`

### 6.2 通过 Portainer Web UI

1. `Images` -> 拉取新版本镜像
2. `Stacks` -> `forgex` -> `Editor`
3. 修改 `FORGEX_VERSION` 变量值
4. `Update the stack` -> 勾选 `Re-pull image and redeploy`
5. 等待服务重启完成

---

## 七、回滚操作

### 7.1 通过运维脚本

```bash
# 回滚到指定版本
bash ${FORGEX_HOME}/scripts/rollback.sh 1.0.0

# 自动回滚到上一版本（从 upgrade.log 读取）
bash ${FORGEX_HOME}/scripts/rollback.sh
```

### 7.2 从备份恢复

如镜像不可用，可从备份完全恢复：

```bash
# 查看可用备份
ls -1t ${FORGEX_HOME}/backup/forgex-backup-*.tar.gz

# 恢复指定备份（交互确认）
bash ${FORGEX_HOME}/scripts/restore.sh ${FORGEX_HOME}/backup/forgex-backup-20260621-100000.tar.gz

# 强制恢复（跳过确认）
bash ${FORGEX_HOME}/scripts/restore.sh ${FORGEX_HOME}/backup/forgex-backup-20260621-100000.tar.gz --force
```

---

## 八、备份操作

### 8.1 手动备份

```bash
# 全量备份（MySQL + Redis + Nacos + uploads + stack 状态）
bash ${FORGEX_HOME}/scripts/backup.sh

# 自定义保留份数
BACKUP_RETAIN=14 bash ${FORGEX_HOME}/scripts/backup.sh
```

### 8.2 备份内容

| 组件 | 备份方式 | 归档内文件 |
|------|---------|-----------|
| MySQL | `mysqldump --single-transaction --routines --triggers` | `mysql-forgex.sql` |
| Redis | `BGSAVE` 后拷贝 `dump.rdb` | `redis-dump.rdb` |
| Nacos | 容器内 `tar czf` 导出 `/home/nacos/data` | `nacos-data.tar.gz` |
| uploads | `tar czf` 归档 uploads 目录 | `uploads.tar.gz` |
| Stack 状态 | `docker ps` 容器状态 JSON | `compose-stack-state.json` |
| 镜像清单 | `docker images` 过滤 forgex | `image-list.txt` |
| manifest | 版本/校验和元数据 | `manifest.json` |

### 8.3 备份保留策略

默认保留最近 7 份备份，可通过 `BACKUP_RETAIN` 环境变量配置。旧备份自动删除。

---

## 九、Portainer API 调用示例

### 9.1 认证

```bash
# 获取 JWT token
curl -sk -X POST https://127.0.0.1:9443/api/auth \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"<密码>"}'
```

### 9.2 列出容器

```bash
TOKEN="<上一步获取的 jwt>"

curl -sk https://127.0.0.1:9443/api/endpoints/1/docker/containers/json?all=true \
  -H "Authorization: Bearer ${TOKEN}"
```

### 9.3 列出 Stack

```bash
curl -sk https://127.0.0.1:9443/api/stacks \
  -H "Authorization: Bearer ${TOKEN}"
```

### 9.4 部署/更新 Stack

```bash
curl -sk -X POST \
  "https://127.0.0.1:9443/api/stacks?type=1&method=string&endpointId=1" \
  -H "Authorization: Bearer ${TOKEN}" \
  -H "Content-Type: application/json" \
  -d "{\"Name\":\"forgex\",\"StackFileContent\":\"<compose内容JSON转义>\"}"
```

### 9.5 获取容器日志

```bash
curl -sk \
  "https://127.0.0.1:9443/api/endpoints/1/docker/containers/<container_id>/logs?stdout=true&stderr=true&tail=200" \
  -H "Authorization: Bearer ${TOKEN}"
```

---

## 十、常见问题

### 10.1 Portainer 无法访问

**现象**：浏览器访问 `https://<IP>:9443` 无响应。

**排查**：
```bash
# 检查 Portainer 容器状态
docker ps | grep portainer

# 检查端口监听
ss -tlnp | grep 9443

# 查看 Portainer 日志
docker logs portainer
```

**解决**：
- 防火墙未放行 9443：`firewall-cmd --add-port=9443/tcp --permanent && firewall-cmd --reload`
- 容器未启动：`docker start portainer`

### 10.2 Stack 部署失败

**现象**：Portainer 部署 Stack 报错。

**排查**：
- 检查环境变量是否全部设置
- 检查镜像是否已拉取（`Images` 页面或 `docker images | grep forgex`）
- 检查端口冲突（`docker ps` 查看端口占用）
- 查看 Portainer 事件日志（`Containers` -> portainer -> Logs）

### 10.3 中间件健康检查失败

**现象**：MySQL/Redis/Nacos 容器状态为 `unhealthy`。

**解决**：
- MySQL：首次启动需初始化，等待 `start_period: 30s` 后再检查
- Nacos：依赖 MySQL 就绪，检查 `depends_on` 配置
- 调整 healthcheck 参数：增大 `start_period` 或 `retries`

### 10.4 端口冲突

Forgex 网关占用 9000 端口，Portainer HTTP 映射到 9001 避开冲突。如 9001 也被占用，修改 `install.sh` 中的 `PORTAINER_HTTP_PORT`。

### 10.5 升级后服务不健康

```bash
# 查看升级日志
cat ${FORGEX_HOME}/upgrade.log

# 查看异常服务日志
docker logs --tail 100 <服务容器名>

# 手动回滚
bash ${FORGEX_HOME}/scripts/rollback.sh <旧版本号>

# 从备份完全恢复
bash ${FORGEX_HOME}/scripts/restore.sh <backup-file> --force
```

---

## 十一、关联文档

- [KMS 主密钥管理](../后端/安全/KMS%20主密钥管理.md)
- [Git 提交与开发规范](../开发规范/规范文档/Git%20提交与开发规范.md)
