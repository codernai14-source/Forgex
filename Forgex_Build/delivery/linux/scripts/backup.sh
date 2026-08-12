#!/usr/bin/env bash
set -euo pipefail

# ============================================================
# Forgex 全量备份脚本
#
# 备份对象: MySQL / Redis / Nacos 配置 / uploads 目录 / compose stack 状态与镜像清单
# 输出: $FORGEX_BACKUP_DIR/forgex-backup-YYYYmmdd-HHMMSS.tar.gz (含 manifest.json)
# 保留策略: 默认保留最近 7 份, 可通过 BACKUP_RETAIN 环境变量配置
#
# 用法: bash backup.sh
# ============================================================

INSTANCE_CODE="${FORGEX_INSTANCE_CODE:-ACME_PROD}"
FORGEX_HOME="${FORGEX_HOME:-/opt/Forgex_${INSTANCE_CODE}}"
FORGEX_BACKUP_DIR="${FORGEX_BACKUP_DIR:-${FORGEX_HOME}/backup}"
FORGEX_UPLOAD_DIR="${FORGEX_UPLOAD_DIR:-${FORGEX_HOME}/data/uploads}"
FORGEX_LICENSE_DIR="${FORGEX_LICENSE_DIR:-${FORGEX_HOME}/license}"
BACKUP_RETAIN="${BACKUP_RETAIN:-7}"
COMPOSE_PROJECT_NAME="${COMPOSE_PROJECT_NAME:-forgex_$(echo "${INSTANCE_CODE}" | tr '[:upper:]' '[:lower:]')}"

# MySQL 连接参数 (从环境变量或 .env 读取)
MYSQL_CONTAINER="${MYSQL_CONTAINER:-${COMPOSE_PROJECT_NAME}-mysql-1}"
MYSQL_USER="${MYSQL_USER:-root}"
MYSQL_PASSWORD="${MYSQL_PASSWORD:-${FORGEX_MYSQL_PASSWORD:-forgex}}"
MYSQL_DATABASE="${MYSQL_DATABASE:-forgex}"

# Redis 容器
REDIS_CONTAINER="${REDIS_CONTAINER:-${COMPOSE_PROJECT_NAME}-redis-1}"

# Nacos 容器与数据路径
NACOS_CONTAINER="${NACOS_CONTAINER:-${COMPOSE_PROJECT_NAME}-nacos-1}"
NACOS_DATA_PATH="${NACOS_DATA_PATH:-/home/nacos/data}"

TIMESTAMP="$(date +%Y%m%d-%H%M%S)"
BACKUP_NAME="forgex-backup-${TIMESTAMP}"
BACKUP_ROOT="${FORGEX_BACKUP_DIR}/${BACKUP_NAME}"
COMPOSE_FILE="${FORGEX_HOME}/app/docker-compose.yml"

mkdir -p "${BACKUP_ROOT}"

echo "=========================================="
echo "Forgex 全量备份"
echo "  实例: ${INSTANCE_CODE}"
echo "  时间: ${TIMESTAMP}"
echo "  目录: ${BACKUP_ROOT}"
echo "=========================================="

# ----------------------- 1. MySQL 备份 -----------------------
echo "[1/6] 备份 MySQL (${MYSQL_DATABASE})..."
MYSQL_DUMP_FILE="${BACKUP_ROOT}/mysql-${MYSQL_DATABASE}.sql"
if docker ps --format '{{.Names}}' | grep -q "^${MYSQL_CONTAINER}$"; then
    docker exec "${MYSQL_CONTAINER}" mysqldump \
        --single-transaction \
        --routines \
        --triggers \
        -u"${MYSQL_USER}" \
        -p"${MYSQL_PASSWORD}" \
        "${MYSQL_DATABASE}" > "${MYSQL_DUMP_FILE}" 2>/dev/null
    echo "  MySQL 备份完成: $(wc -c < "${MYSQL_DUMP_FILE}") bytes"
else
    echo "  警告: MySQL 容器 ${MYSQL_CONTAINER} 未运行, 跳过 MySQL 备份"
    echo "  skipped" > "${MYSQL_DUMP_FILE}.skip"
fi

# ----------------------- 2. Redis 备份 -----------------------
echo "[2/6] 备份 Redis..."
REDIS_DUMP_FILE="${BACKUP_ROOT}/redis-dump.rdb"
if docker ps --format '{{.Names}}' | grep -q "^${REDIS_CONTAINER}$"; then
    docker exec "${REDIS_CONTAINER}" redis-cli BGSAVE >/dev/null 2>&1 || true
    sleep 2
    docker cp "${REDIS_CONTAINER}:/data/dump.rdb" "${REDIS_DUMP_FILE}" 2>/dev/null || {
        echo "  警告: Redis dump.rdb 拷贝失败, 尝试 /data/dump.rdb 备用路径"
        docker cp "${REDIS_CONTAINER}:/data/dump.rdb" "${REDIS_DUMP_FILE}" 2>/dev/null || {
            echo "  警告: Redis 备份失败"
            echo "  skipped" > "${REDIS_DUMP_FILE}.skip"
        }
    }
    if [ -f "${REDIS_DUMP_FILE}" ]; then
        echo "  Redis 备份完成: $(wc -c < "${REDIS_DUMP_FILE}") bytes"
    fi
else
    echo "  警告: Redis 容器 ${REDIS_CONTAINER} 未运行, 跳过 Redis 备份"
    echo "  skipped" > "${REDIS_DUMP_FILE}.skip"
fi

# ----------------------- 3. Nacos 配置备份 -----------------------
echo "[3/6] 备份 Nacos 配置..."
NACOS_BACKUP_FILE="${BACKUP_ROOT}/nacos-data.tar.gz"
if docker ps --format '{{.Names}}' | grep -q "^${NACOS_CONTAINER}$"; then
    docker exec "${NACOS_CONTAINER}" tar czf - -C "${NACOS_DATA_PATH}" . > "${NACOS_BACKUP_FILE}" 2>/dev/null || {
        echo "  警告: Nacos 配置导出失败"
        echo "  skipped" > "${NACOS_BACKUP_FILE}.skip"
    }
    if [ -f "${NACOS_BACKUP_FILE}" ]; then
        echo "  Nacos 配置备份完成: $(wc -c < "${NACOS_BACKUP_FILE}") bytes"
    fi
else
    echo "  警告: Nacos 容器 ${NACOS_CONTAINER} 未运行, 跳过 Nacos 备份"
    echo "  skipped" > "${NACOS_BACKUP_FILE}.skip"
fi

# ----------------------- 4. uploads 目录备份 -----------------------
echo "[4/6] 备份 uploads 目录..."
UPLOADS_BACKUP_FILE="${BACKUP_ROOT}/uploads.tar.gz"
if [ -d "${FORGEX_UPLOAD_DIR}" ] && [ "$(ls -A "${FORGEX_UPLOAD_DIR}" 2>/dev/null)" ]; then
    tar czf "${UPLOADS_BACKUP_FILE}" -C "${FORGEX_UPLOAD_DIR}" . 2>/dev/null
    echo "  uploads 备份完成: $(wc -c < "${UPLOADS_BACKUP_FILE}") bytes"
else
    echo "  警告: uploads 目录为空或不存在, 跳过"
    echo "  skipped" > "${UPLOADS_BACKUP_FILE}.skip"
fi

# ----------------------- 5. Compose Stack 状态与镜像清单 -----------------------
echo "[5/6] 备份 compose stack 状态与镜像清单..."
STACK_STATE_FILE="${BACKUP_ROOT}/compose-stack-state.json"
IMAGE_LIST_FILE="${BACKUP_ROOT}/image-list.txt"

if [ -f "${COMPOSE_FILE}" ]; then
    cp "${COMPOSE_FILE}" "${BACKUP_ROOT}/docker-compose.yml.bak"
fi

# 容器状态快照
docker ps -a \
    --filter "label=com.docker.compose.project=${COMPOSE_PROJECT_NAME}" \
    --format '{"name":"{{.Names}}","image":"{{.Image}}","status":"{{.Status}}","state":"{{.State}}"}' \
    > "${STACK_STATE_FILE}" 2>/dev/null || echo "[]" > "${STACK_STATE_FILE}"

# 镜像清单 (tag + digest)
docker images --format '{{.Repository}}:{{.Tag}} {{.ID}} {{.Size}}' \
    | grep -i forgex > "${IMAGE_LIST_FILE}" 2>/dev/null || echo "(no forgex images)" > "${IMAGE_LIST_FILE}"

echo "  stack 状态与镜像清单备份完成"

# ----------------------- 6. 生成 manifest.json -----------------------
echo "[6/6] 生成 manifest.json..."
MANIFEST_FILE="${BACKUP_ROOT}/manifest.json"

MYSQL_SHA=""
REDIS_SHA=""
NACOS_SHA=""
UPLOADS_SHA=""
[ -f "${MYSQL_DUMP_FILE}" ] && MYSQL_SHA=$(sha256sum "${MYSQL_DUMP_FILE}" | awk '{print $1}')
[ -f "${REDIS_DUMP_FILE}" ] && REDIS_SHA=$(sha256sum "${REDIS_DUMP_FILE}" | awk '{print $1}')
[ -f "${NACOS_BACKUP_FILE}" ] && NACOS_SHA=$(sha256sum "${NACOS_BACKUP_FILE}" | awk '{print $1}')
[ -f "${UPLOADS_BACKUP_FILE}" ] && UPLOADS_SHA=$(sha256sum "${UPLOADS_BACKUP_FILE}" | awk '{print $1}')

FORGEX_VERSION="${FORGEX_VERSION:-unknown}"

cat > "${MANIFEST_FILE}" <<EOF
{
  "backupName": "${BACKUP_NAME}",
  "timestamp": "${TIMESTAMP}",
  "instanceCode": "${INSTANCE_CODE}",
  "forgexVersion": "${FORGEX_VERSION}",
  "composeProject": "${COMPOSE_PROJECT_NAME}",
  "components": {
    "mysql": {
      "file": "$(basename "${MYSQL_DUMP_FILE}")",
      "sha256": "${MYSQL_SHA}"
    },
    "redis": {
      "file": "$(basename "${REDIS_DUMP_FILE}")",
      "sha256": "${REDIS_SHA}"
    },
    "nacos": {
      "file": "$(basename "${NACOS_BACKUP_FILE}")",
      "sha256": "${NACOS_SHA}"
    },
    "uploads": {
      "file": "$(basename "${UPLOADS_BACKUP_FILE}")",
      "sha256": "${UPLOADS_SHA}"
    }
  },
  "stackState": "compose-stack-state.json",
  "imageList": "image-list.txt",
  "composeFile": "docker-compose.yml.bak"
}
EOF

echo "  manifest.json 生成完成"

# ----------------------- 打包 -----------------------
echo "打包备份目录..."
ARCHIVE_FILE="${FORGEX_BACKUP_DIR}/${BACKUP_NAME}.tar.gz"
tar czf "${ARCHIVE_FILE}" -C "${FORGEX_BACKUP_DIR}" "${BACKUP_NAME}"
rm -rf "${BACKUP_ROOT}"

ARCHIVE_SHA=$(sha256sum "${ARCHIVE_FILE}" | awk '{print $1}')
echo ""
echo "备份完成:"
echo "  归档: ${ARCHIVE_FILE}"
echo "  大小: $(du -h "${ARCHIVE_FILE}" | awk '{print $1}')"
echo "  SHA256: ${ARCHIVE_SHA}"

# ----------------------- 保留策略清理 -----------------------
echo ""
echo "清理旧备份 (保留最近 ${BACKUP_RETAIN} 份)..."
ls -1t "${FORGEX_BACKUP_DIR}"/forgex-backup-*.tar.gz 2>/dev/null | tail -n +$((BACKUP_RETAIN + 1)) | while read -r old_file; do
    echo "  删除: $(basename "${old_file}")"
    rm -f "${old_file}"
done

echo ""
echo "备份全部完成。"
