#!/usr/bin/env bash
set -euo pipefail

# ============================================================
# Forgex 备份恢复脚本
#
# 用法: bash restore.sh <backup-file> [--force]
#
# 流程: 校验 manifest -> 停 compose 服务 -> 恢复 MySQL/Redis/Nacos/uploads
#       -> 拉起服务 -> 健康检查 -> 输出恢复报告
# 安全: 强制二次确认 (--force 跳过), 恢复前自动做一次当前状态快照
# ============================================================

INSTANCE_CODE="${FORGEX_INSTANCE_CODE:-ACME_PROD}"
FORGEX_HOME="${FORGEX_HOME:-/opt/Forgex_${INSTANCE_CODE}}"
FORGEX_BACKUP_DIR="${FORGEX_BACKUP_DIR:-${FORGEX_HOME}/backup}"
FORGEX_UPLOAD_DIR="${FORGEX_UPLOAD_DIR:-${FORGEX_HOME}/data/uploads}"
FORGEX_LICENSE_DIR="${FORGEX_LICENSE_DIR:-${FORGEX_HOME}/license}"
COMPOSE_PROJECT_NAME="${COMPOSE_PROJECT_NAME:-forgex_$(echo "${INSTANCE_CODE}" | tr '[:upper:]' '[:lower:]')}"
COMPOSE_FILE="${FORGEX_HOME}/app/docker-compose.yml"

MYSQL_CONTAINER="${MYSQL_CONTAINER:-${COMPOSE_PROJECT_NAME}-mysql-1}"
MYSQL_USER="${MYSQL_USER:-root}"
MYSQL_PASSWORD="${MYSQL_PASSWORD:-${FORGEX_MYSQL_PASSWORD:-forgex}}"
MYSQL_DATABASE="${MYSQL_DATABASE:-forgex}"

REDIS_CONTAINER="${REDIS_CONTAINER:-${COMPOSE_PROJECT_NAME}-redis-1}"
NACOS_CONTAINER="${NACOS_CONTAINER:-${COMPOSE_PROJECT_NAME}-nacos-1}"
NACOS_DATA_PATH="${NACOS_DATA_PATH:-/home/nacos/data}"

# ----------------------- 参数校验 -----------------------
BACKUP_FILE=""
FORCE=false

for arg in "$@"; do
    case "${arg}" in
        --force) FORCE=true ;;
        *.tar.gz) BACKUP_FILE="${arg}" ;;
    esac
done

if [ -z "${BACKUP_FILE}" ]; then
    echo "用法: bash restore.sh <backup-file.tar.gz> [--force]"
    echo ""
    echo "参数:"
    echo "  <backup-file>  备份归档文件路径 (.tar.gz)"
    echo "  --force         跳过二次确认, 直接恢复"
    echo ""
    echo "可用备份:"
    ls -1t "${FORGEX_BACKUP_DIR}"/forgex-backup-*.tar.gz 2>/dev/null | head -10 || echo "  (无备份文件)"
    exit 1
fi

if [ ! -f "${BACKUP_FILE}" ]; then
    echo "错误: 备份文件不存在: ${BACKUP_FILE}"
    exit 1
fi

# 如果是相对路径, 补全为绝对路径
BACKUP_FILE="$(cd "$(dirname "${BACKUP_FILE}")" && pwd)/$(basename "${BACKUP_FILE}")"

# ----------------------- 二次确认 -----------------------
echo "=========================================="
echo " Forgex 备份恢复"
echo "=========================================="
echo "  实例: ${INSTANCE_CODE}"
echo "  备份: ${BACKUP_FILE}"
echo "  大小: $(du -h "${BACKUP_FILE}" | awk '{print $1}')"
echo "=========================================="
echo ""
echo "警告: 恢复操作将覆盖当前 MySQL/Redis/Nacos/uploads 数据!"
echo "      恢复前会自动做一次当前状态快照。"
echo ""

if [ "${FORCE}" != "true" ]; then
    echo "确认恢复? 输入 'YES' 继续, 其他任意输入取消:"
    read -r CONFIRM
    if [ "${CONFIRM}" != "YES" ]; then
        echo "已取消恢复。"
        exit 0
    fi
fi

# ----------------------- 恢复前快照 -----------------------
echo ""
echo "[0/6] 恢复前自动快照当前状态..."
PRE_RESTORE_BACKUP="${FORGEX_BACKUP_DIR}/forgex-prerestore-$(date +%Y%m%d-%H%M%S).tar.gz"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
if [ -f "${SCRIPT_DIR}/backup.sh" ]; then
    bash "${SCRIPT_DIR}/backup.sh" 2>/dev/null || {
        echo "  警告: 恢复前快照失败, 继续恢复 (建议手动备份当前状态)"
    }
    # backup.sh 输出的是最新归档, 重命名标记
    LATEST_BACKUP="$(ls -1t "${FORGEX_BACKUP_DIR}"/forgex-backup-*.tar.gz 2>/dev/null | head -1)"
    if [ -n "${LATEST_BACKUP}" ]; then
        mv "${LATEST_BACKUP}" "${PRE_RESTORE_BACKUP}"
        echo "  恢复前快照: ${PRE_RESTORE_BACKUP}"
    fi
else
    echo "  警告: 未找到 backup.sh, 跳过恢复前快照"
fi

# ----------------------- 解包备份 -----------------------
echo ""
echo "[1/6] 解包备份归档..."
RESTORE_TMP="${FORGEX_BACKUP_DIR}/.restore-tmp-$$"
mkdir -p "${RESTORE_TMP}"
tar xzf "${BACKUP_FILE}" -C "${RESTORE_TMP}"
BACKUP_DIR_NAME=$(ls -1 "${RESTORE_TMP}" | head -1)
BACKUP_ROOT="${RESTORE_TMP}/${BACKUP_DIR_NAME}"

if [ ! -f "${BACKUP_ROOT}/manifest.json" ]; then
    echo "错误: 备份归档缺少 manifest.json, 可能不是有效的 Forgex 备份"
    rm -rf "${RESTORE_TMP}"
    exit 1
fi

echo "  备份内容:"
cat "${BACKUP_ROOT}/manifest.json"
echo ""

# ----------------------- 停止 compose 服务 -----------------------
echo ""
echo "[2/6] 停止 compose 服务..."
if [ -f "${COMPOSE_FILE}" ]; then
    docker compose -f "${COMPOSE_FILE}" down 2>/dev/null || {
        echo "  警告: docker compose down 失败, 尝试停止容器..."
        docker ps -a --filter "label=com.docker.compose.project=${COMPOSE_PROJECT_NAME}" -q | xargs -r docker stop 2>/dev/null || true
    }
    echo "  compose 服务已停止"
else
    echo "  警告: 未找到 ${COMPOSE_FILE}, 手动停止 forgex 容器..."
    docker ps -a --filter "label=com.docker.compose.project=${COMPOSE_PROJECT_NAME}" -q | xargs -r docker stop 2>/dev/null || true
fi

# 重新启动中间件服务以便恢复数据
echo "  启动中间件服务以便恢复数据..."
if [ -f "${COMPOSE_FILE}" ]; then
    docker compose -f "${COMPOSE_FILE}" up -d mysql redis nacos 2>/dev/null || true
fi
sleep 10

# ----------------------- 恢复 MySQL -----------------------
echo ""
echo "[3/6] 恢复 MySQL..."
MYSQL_DUMP_FILE="${BACKUP_ROOT}/mysql-${MYSQL_DATABASE}.sql"
if [ -f "${MYSQL_DUMP_FILE}" ]; then
    if docker ps --format '{{.Names}}' | grep -q "^${MYSQL_CONTAINER}$"; then
        docker exec -i "${MYSQL_CONTAINER}" mysql \
            -u"${MYSQL_USER}" \
            -p"${MYSQL_PASSWORD}" \
            "${MYSQL_DATABASE}" < "${MYSQL_DUMP_FILE}" 2>/dev/null
        echo "  MySQL 恢复完成"
    else
        echo "  错误: MySQL 容器 ${MYSQL_CONTAINER} 未运行, 无法恢复"
    fi
else
    echo "  警告: MySQL 备份文件不存在, 跳过"
fi

# ----------------------- 恢复 Redis -----------------------
echo ""
echo "[4/6] 恢复 Redis..."
REDIS_DUMP_FILE="${BACKUP_ROOT}/redis-dump.rdb"
if [ -f "${REDIS_DUMP_FILE}" ]; then
    if docker ps --format '{{.Names}}' | grep -q "^${REDIS_CONTAINER}$"; then
        docker stop "${REDIS_CONTAINER}" 2>/dev/null || true
        docker cp "${REDIS_DUMP_FILE}" "${REDIS_CONTAINER}:/data/dump.rdb" 2>/dev/null
        docker start "${REDIS_CONTAINER}" 2>/dev/null || true
        echo "  Redis 恢复完成"
    else
        echo "  错误: Redis 容器 ${REDIS_CONTAINER} 未运行, 无法恢复"
    fi
else
    echo "  警告: Redis 备份文件不存在, 跳过"
fi

# ----------------------- 恢复 Nacos -----------------------
echo ""
echo "[5/6] 恢复 Nacos 配置..."
NACOS_BACKUP_FILE="${BACKUP_ROOT}/nacos-data.tar.gz"
if [ -f "${NACOS_BACKUP_FILE}" ]; then
    if docker ps --format '{{.Names}}' | grep -q "^${NACOS_CONTAINER}$"; then
        docker exec "${NACOS_CONTAINER}" mkdir -p "${NACOS_DATA_PATH}" 2>/dev/null || true
        docker cp "${NACOS_BACKUP_FILE}" "${NACOS_CONTAINER}:/tmp/nacos-restore.tar.gz" 2>/dev/null
        docker exec "${NACOS_CONTAINER}" tar xzf /tmp/nacos-restore.tar.gz -C "${NACOS_DATA_PATH}" 2>/dev/null
        docker exec "${NACOS_CONTAINER}" rm -f /tmp/nacos-restore.tar.gz 2>/dev/null || true
        docker restart "${NACOS_CONTAINER}" 2>/dev/null || true
        echo "  Nacos 配置恢复完成"
    else
        echo "  错误: Nacos 容器 ${NACOS_CONTAINER} 未运行, 无法恢复"
    fi
else
    echo "  警告: Nacos 备份文件不存在, 跳过"
fi

# ----------------------- 恢复 uploads -----------------------
echo ""
echo "[6/6] 恢复 uploads 目录..."
UPLOADS_BACKUP_FILE="${BACKUP_ROOT}/uploads.tar.gz"
if [ -f "${UPLOADS_BACKUP_FILE}" ]; then
    mkdir -p "${FORGEX_UPLOAD_DIR}"
    tar xzf "${UPLOADS_BACKUP_FILE}" -C "${FORGEX_UPLOAD_DIR}"
    echo "  uploads 恢复完成"
else
    echo "  警告: uploads 备份文件不存在, 跳过"
fi

# ----------------------- 拉起全部服务 -----------------------
echo ""
echo "拉起全部 compose 服务..."
if [ -f "${COMPOSE_FILE}" ]; then
    # 恢复 compose 文件备份 (如果存在)
    if [ -f "${BACKUP_ROOT}/docker-compose.yml.bak" ]; then
        cp "${BACKUP_ROOT}/docker-compose.yml.bak" "${COMPOSE_FILE}"
        echo "  已恢复 compose 文件"
    fi
    docker compose -f "${COMPOSE_FILE}" up -d
    echo "  compose 服务已拉起"
fi

# ----------------------- 健康检查 -----------------------
echo ""
echo "健康检查..."
sleep 15
HEALTH_OK=true
SERVICES="gateway auth sys basic job integration workflow report"
for svc in ${SERVICES}; do
    CONTAINER_NAME="${COMPOSE_PROJECT_NAME}-${svc}-1"
    if docker ps --format '{{.Names}}' | grep -q "^${CONTAINER_NAME}$"; then
        STATE=$(docker inspect -f '{{.State.Status}}' "${CONTAINER_NAME}" 2>/dev/null || echo "unknown")
        if [ "${STATE}" = "running" ]; then
            echo "  ${svc}: running"
        else
            echo "  ${svc}: ${STATE} (异常)"
            HEALTH_OK=false
        fi
    else
        echo "  ${svc}: 未找到容器"
        HEALTH_OK=false
    fi
done

# ----------------------- 清理临时文件 -----------------------
rm -rf "${RESTORE_TMP}"

# ----------------------- 恢复报告 -----------------------
echo ""
echo "=========================================="
echo " 恢复报告"
echo "=========================================="
echo "  备份来源: ${BACKUP_FILE}"
echo "  恢复前快照: ${PRE_RESTORE_BACKUP:-无}"
echo "  服务健康: $([ "${HEALTH_OK}" = "true" ] && echo "全部正常" || echo "存在异常")"
echo "=========================================="

if [ "${HEALTH_OK}" = "true" ]; then
    echo "恢复完成, 所有服务运行正常。"
else
    echo "警告: 部分服务异常, 请检查日志: docker compose -f ${COMPOSE_FILE} logs"
    exit 2
fi
