#!/usr/bin/env bash
set -euo pipefail

INSTANCE_CODE="${1:-ACME_PROD}"
DEPLOY_PROFILE="${2:-prod}"
FORGEX_HOME="/opt/Forgex_${INSTANCE_CODE}"
FORGEX_LICENSE_DIR="${FORGEX_HOME}/license"
FORGEX_UPLOAD_DIR="${FORGEX_HOME}/data/uploads"
FORGEX_LOG_DIR="${FORGEX_HOME}/logs"
FORGEX_BACKUP_DIR="${FORGEX_HOME}/backup"

mkdir -p \
  "${FORGEX_HOME}/app" \
  "${FORGEX_HOME}/config" \
  "${FORGEX_HOME}/data" \
  "${FORGEX_UPLOAD_DIR}" \
  "${FORGEX_LICENSE_DIR}" \
  "${FORGEX_LOG_DIR}" \
  "${FORGEX_HOME}/scripts" \
  "${FORGEX_BACKUP_DIR}" \
  "${FORGEX_HOME}/tools"

cat > "${FORGEX_HOME}/.env" <<EOF
FORGEX_INSTANCE_CODE=${INSTANCE_CODE}
FORGEX_PROFILE=${DEPLOY_PROFILE}
FORGEX_HOME=${FORGEX_HOME}
FORGEX_LICENSE_DIR=${FORGEX_LICENSE_DIR}
FORGEX_UPLOAD_DIR=${FORGEX_UPLOAD_DIR}
FORGEX_LOG_DIR=${FORGEX_LOG_DIR}
FORGEX_BACKUP_DIR=${FORGEX_BACKUP_DIR}
FORGEX_NACOS_ADDR=127.0.0.1:8848
FORGEX_REDIS_ADDR=127.0.0.1:6379
FORGEX_MYSQL_URL=jdbc:mysql://127.0.0.1:3306/forgex
FORGEX_GATEWAY_PORT=9000
FORGEX_AUTH_PORT=9001
FORGEX_SYS_PORT=9002
FORGEX_BASIC_PORT=9003
FORGEX_JOB_PORT=9004
FORGEX_INTEGRATION_PORT=9007
FORGEX_WORKFLOW_PORT=9005
FORGEX_REPORT_PORT=8084
COMPOSE_PROJECT_NAME=forgex_$(echo "${INSTANCE_CODE}" | tr '[:upper:]' '[:lower:]')
FORGEX_KMS_MASTER_KEY_FILE=${FORGEX_LICENSE_DIR}/kms.key
EOF

# ======================== KMS 主密钥生成 ========================
# 调用 gen-kms-master-key.sh 生成 32 字节主密钥到 license 目录
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
GEN_KEY_SCRIPT="${SCRIPT_DIR}/gen-kms-master-key.sh"

KMS_KEY_FILE="${FORGEX_LICENSE_DIR}/kms.key"
if [ -f "${KMS_KEY_FILE}" ]; then
  echo "KMS: 主密钥文件已存在, 跳过生成: ${KMS_KEY_FILE}"
else
  if [ -x "${GEN_KEY_SCRIPT}" ] || [ -f "${GEN_KEY_SCRIPT}" ]; then
    echo "KMS: 正在生成主密钥..."
    bash "${GEN_KEY_SCRIPT}" -o "${KMS_KEY_FILE}" >/dev/null 2>&1 || {
      echo "KMS: 警告 - 主密钥生成失败, 请手动执行: bash ${GEN_KEY_SCRIPT} -o ${KMS_KEY_FILE}" >&2
    }
    if [ -f "${KMS_KEY_FILE}" ]; then
      echo "KMS: 主密钥已生成: ${KMS_KEY_FILE} (权限 600)"
    fi
  else
    echo "KMS: 警告 - 未找到 gen-kms-master-key.sh, 请手动生成主密钥:" >&2
    echo "     openssl rand -hex 32 > ${KMS_KEY_FILE} && chmod 600 ${KMS_KEY_FILE}" >&2
  fi
fi
# ======================== KMS 主密钥生成结束 ========================

# ======================== Portainer 自动部署 ========================
# 检测并自动部署 Portainer CE, 用于容器可视化管理
PORTAINER_HTTPS_PORT="${PORTAINER_HTTPS_PORT:-9443}"
PORTAINER_HTTP_PORT="${PORTAINER_HTTP_PORT:-9001}"

# 检测 Docker 是否可用
if command -v docker >/dev/null 2>&1; then
  # 检测 Portainer 是否已运行
  PORTAINER_RUNNING=$(docker ps --format '{{.Names}} {{.Image}}' 2>/dev/null | grep -c 'portainer/portainer-ce' || echo "0")

  if [ "${PORTAINER_RUNNING}" -gt 0 ]; then
    echo "Portainer: 已在运行, 跳过部署"
  else
    echo "Portainer: 检测到未运行, 开始自动部署 Portainer CE..."

    # 拉取 Portainer CE 镜像
    echo "  拉取 portainer/portainer-ce:latest..."
    if docker pull portainer/portainer-ce:latest 2>/dev/null; then
      # 创建 Portainer 数据卷
      docker volume create portainer_data >/dev/null 2>&1 || true

      # 启动 Portainer CE 容器
      # 端口映射: 9443->9443 (HTTPS UI), 9001->9000 (HTTP API, 避开网关 9000)
      # 注意: 网关已占用 9000 端口, Portainer HTTP 映射到 9001
      echo "  启动 Portainer 容器..."
      docker run -d \
        --name portainer \
        --restart=unless-stopped \
        -p "${PORTAINER_HTTPS_PORT}:9443" \
        -p "${PORTAINER_HTTP_PORT}:9000" \
        -v /var/run/docker.sock:/var/run/docker.sock \
        -v portainer_data:/data \
        portainer/portainer-ce:latest 2>/dev/null

      if docker ps --format '{{.Names}}' | grep -q '^portainer$'; then
        echo "  Portainer CE 已启动"
      else
        echo "  警告: Portainer 容器启动失败, 请手动部署" >&2
      fi
    else
      echo "  警告: Portainer 镜像拉取失败, 请检查网络后手动部署" >&2
    fi
  fi

  # 复制 Portainer Stack 文件到 app 目录
  if [ -f "${SCRIPT_DIR}/portainer-stack.yml" ]; then
    cp "${SCRIPT_DIR}/portainer-stack.yml" "${FORGEX_HOME}/app/portainer-stack.yml"
    echo "Portainer: Stack 文件已复制到 ${FORGEX_HOME}/app/portainer-stack.yml"
  fi

  # 复制 docker-compose 模板到 app 目录
  if [ -f "${SCRIPT_DIR}/docker-compose.yml.template" ]; then
    cp "${SCRIPT_DIR}/docker-compose.yml.template" "${FORGEX_HOME}/app/docker-compose.yml"
    echo "Compose: 模板已复制到 ${FORGEX_HOME}/app/docker-compose.yml"
  fi

  # 复制运维脚本到 scripts 目录
  for script_file in backup.sh restore.sh upgrade.sh rollback.sh portainer-api.sh gen-kms-master-key.sh; do
    if [ -f "${SCRIPT_DIR}/${script_file}" ]; then
      cp "${SCRIPT_DIR}/${script_file}" "${FORGEX_HOME}/scripts/${script_file}"
      chmod +x "${FORGEX_HOME}/scripts/${script_file}" 2>/dev/null || true
    fi
  done
  echo "Scripts: 运维脚本已复制到 ${FORGEX_HOME}/scripts/"
else
  echo "Portainer: Docker 未安装或不可用, 跳过 Portainer 部署" >&2
fi
# ======================== Portainer 自动部署结束 ========================

# 在 .env 中补充 Portainer 配置
cat >> "${FORGEX_HOME}/.env" <<EOF
FORGEX_PORTAINER_URL=https://127.0.0.1:${PORTAINER_HTTPS_PORT}
FORGEX_PORTAINER_HTTP_PORT=${PORTAINER_HTTP_PORT}
FORGEX_PORTAINER_ENDPOINT_ID=1
EOF

cat <<EOF

==========================================
 Forgex Linux 实例初始化完成
==========================================
  实例目录: ${FORGEX_HOME}
  环境配置: ${FORGEX_HOME}/.env

Portainer 管理:
  HTTPS UI:  https://<服务器IP>:${PORTAINER_HTTPS_PORT}
  HTTP API:  http://<服务器IP>:${PORTAINER_HTTP_PORT}
  首次登录:  用户名 admin, 首次访问需设置密码

部署方式 (二选一):
  方式一 (推荐 - Portainer UI):
    1. 浏览器访问 https://<服务器IP>:${PORTAINER_HTTPS_PORT}
    2. 首次设置管理员密码
    3. 进入 Stack -> Add stack -> 上传 ${FORGEX_HOME}/app/portainer-stack.yml
    4. 点击 Deploy the stack

  方式二 (命令行):
    1. cd ${FORGEX_HOME}/app
    2. docker compose --profile ${DEPLOY_PROFILE} up -d

运维脚本:
  备份:   bash ${FORGEX_HOME}/scripts/backup.sh
  恢复:   bash ${FORGEX_HOME}/scripts/restore.sh <backup-file>
  升级:   bash ${FORGEX_HOME}/scripts/upgrade.sh <new-version>
  回滚:   bash ${FORGEX_HOME}/scripts/rollback.sh [version]
  Portainer API: source ${FORGEX_HOME}/scripts/portainer-api.sh

KMS 主密钥:
  密钥文件: ${FORGEX_LICENSE_DIR}/kms.key
  环境变量: FORGEX_KMS_MASTER_KEY_FILE=${FORGEX_LICENSE_DIR}/kms.key

详细操作请参考: Forgex_Doc/部署/Portainer 管理手册.md
==========================================
EOF
