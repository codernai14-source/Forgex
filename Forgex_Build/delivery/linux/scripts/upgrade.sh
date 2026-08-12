#!/usr/bin/env bash
set -euo pipefail

# ============================================================
# Forgex 滚动升级脚本
#
# 用法: bash upgrade.sh <new-version>
#
# 流程: 拉取新镜像 -> 自动 backup -> 逐服务滚动重启
#       (gateway->auth->sys->basic->job->integration->workflow->report)
#       -> 每服务健康检查 -> 失败自动触发 rollback
# 记录升级历史到 $FORGEX_HOME/upgrade.log
# ============================================================

INSTANCE_CODE="${FORGEX_INSTANCE_CODE:-ACME_PROD}"
FORGEX_HOME="${FORGEX_HOME:-/opt/Forgex_${INSTANCE_CODE}}"
COMPOSE_FILE="${FORGEX_HOME}/app/docker-compose.yml"
COMPOSE_PROJECT_NAME="${COMPOSE_PROJECT_NAME:-forgex_$(echo "${INSTANCE_CODE}" | tr '[:upper:]' '[:lower:]')}"
UPGRADE_LOG="${FORGEX_HOME}/upgrade.log"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

NEW_VERSION="${1:-}"
if [ -z "${NEW_VERSION}" ]; then
    echo "用法: bash upgrade.sh <new-version>"
    echo "示例: bash upgrade.sh 1.1.0"
    exit 1
fi

# 获取当前版本
CURRENT_VERSION="${FORGEX_VERSION:-1.0.0}"
if [ -f "${COMPOSE_FILE}" ]; then
    CURRENT_VERSION=$(grep -oP 'FORGEX_VERSION:-\K[0-9.]+' "${COMPOSE_FILE}" 2>/dev/null || echo "${CURRENT_VERSION}")
fi

TIMESTAMP="$(date '+%Y-%m-%d %H:%M:%S')"

echo "=========================================="
echo " Forgex 滚动升级"
echo "=========================================="
echo "  实例: ${INSTANCE_CODE}"
echo "  当前版本: ${CURRENT_VERSION}"
echo "  目标版本: ${NEW_VERSION}"
echo "  时间: ${TIMESTAMP}"
echo "=========================================="

# ----------------------- 1. 拉取新镜像 -----------------------
echo ""
echo "[1/4] 拉取新版本镜像..."
SERVICES="gateway auth sys basic job integration workflow report"
for svc in ${SERVICES}; do
    IMAGE="forgex/forgex-${svc}:${NEW_VERSION}"
    echo "  拉取 ${IMAGE}..."
    if ! docker pull "${IMAGE}" 2>/dev/null; then
        echo "  错误: 拉取 ${IMAGE} 失败"
        echo "${TIMESTAMP} | upgrade ${CURRENT_VERSION} -> ${NEW_VERSION} | FAILED | image pull failed: ${IMAGE}" >> "${UPGRADE_LOG}"
        echo ""
        echo "升级失败: 镜像拉取失败, 未做任何变更。"
        exit 1
    fi
done
echo "  全部镜像拉取完成"

# ----------------------- 2. 自动备份 -----------------------
echo ""
echo "[2/4] 升级前自动备份..."
BACKUP_SCRIPT="${SCRIPT_DIR}/backup.sh"
if [ -f "${BACKUP_SCRIPT}" ]; then
    # 保存当前版本号, backup.sh 会读取 FORGEX_VERSION
    export FORGEX_VERSION="${CURRENT_VERSION}"
    if ! bash "${BACKUP_SCRIPT}"; then
        echo "  错误: 升级前备份失败, 中止升级"
        echo "${TIMESTAMP} | upgrade ${CURRENT_VERSION} -> ${NEW_VERSION} | FAILED | backup failed" >> "${UPGRADE_LOG}"
        exit 1
    fi
    echo "  升级前备份完成"
else
    echo "  警告: 未找到 backup.sh, 跳过自动备份 (有风险!)"
fi

# 记录最新备份文件路径 (供回滚使用)
LATEST_BACKUP=$(ls -1t "${FORGEX_BACKUP_DIR:-${FORGEX_HOME}/backup}"/forgex-backup-*.tar.gz 2>/dev/null | head -1)

# ----------------------- 3. 更新 compose 文件版本 -----------------------
echo ""
echo "[3/4] 更新 compose 文件镜像版本..."
if [ -f "${COMPOSE_FILE}" ]; then
    # 备份当前 compose 文件
    cp "${COMPOSE_FILE}" "${COMPOSE_FILE}.pre-upgrade"
    # 替换版本号
    sed -i.bak "s/\${FORGEX_VERSION:-[0-9.]*}/\${FORGEX_VERSION:-${NEW_VERSION}}/g" "${COMPOSE_FILE}"
    rm -f "${COMPOSE_FILE}.bak"
    echo "  compose 文件已更新到版本 ${NEW_VERSION}"
else
    echo "  错误: compose 文件不存在: ${COMPOSE_FILE}"
    exit 1
fi

export FORGEX_VERSION="${NEW_VERSION}"

# ----------------------- 4. 逐服务滚动重启 -----------------------
echo ""
echo "[4/4] 逐服务滚动重启..."
ROLLBACK_NEEDED=false

for svc in ${SERVICES}; do
    echo ""
    echo "  --- 升级服务: ${svc} ---"
    CONTAINER_NAME="${COMPOSE_PROJECT_NAME}-${svc}-1"

    # 停止旧容器
    echo "    停止 ${svc}..."
    docker compose -f "${COMPOSE_FILE}" stop "${svc}" 2>/dev/null || {
        docker stop "${CONTAINER_NAME}" 2>/dev/null || true
    }

    # 删除旧容器
    docker compose -f "${COMPOSE_FILE}" rm -f "${svc}" 2>/dev/null || {
        docker rm -f "${CONTAINER_NAME}" 2>/dev/null || true
    }

    # 启动新版本容器
    echo "    启动 ${svc} (版本 ${NEW_VERSION})..."
    docker compose -f "${COMPOSE_FILE}" up -d "${svc}" 2>/dev/null || {
        echo "    错误: 启动 ${svc} 失败"
        ROLLBACK_NEEDED=true
        break
    }

    # 健康检查 (最多等待 60 秒)
    echo "    健康检查 ${svc}..."
    HEALTH_OK=false
    for i in $(seq 1 12); do
        sleep 5
        STATE=$(docker inspect -f '{{.State.Status}}' "${CONTAINER_NAME}" 2>/dev/null || echo "not_found")
        if [ "${STATE}" = "running" ]; then
            # 检查容器是否健康 (如果有 healthcheck)
            HEALTH_STATUS=$(docker inspect -f '{{.State.Health.Status}}' "${CONTAINER_NAME}" 2>/dev/null || echo "none")
            if [ "${HEALTH_STATUS}" = "healthy" ] || [ "${HEALTH_STATUS}" = "none" ]; then
                HEALTH_OK=true
                break
            elif [ "${HEALTH_STATUS}" = "unhealthy" ]; then
                break
            fi
        elif [ "${STATE}" = "exited" ]; then
            break
        fi
        echo "    等待 ${svc} 启动... (${i}/12)"
    done

    if [ "${HEALTH_OK}" = "true" ]; then
        echo "    ${svc}: 健康"
    else
        echo "    ${svc}: 健康检查失败 (状态: ${STATE})"
        ROLLBACK_NEEDED=true
        break
    fi
done

# ----------------------- 升级结果处理 -----------------------
if [ "${ROLLBACK_NEEDED}" = "true" ]; then
    echo ""
    echo "=========================================="
    echo " 升级失败, 自动触发回滚..."
    echo "=========================================="

    # 恢复 compose 文件
    if [ -f "${COMPOSE_FILE}.pre-upgrade" ]; then
        cp "${COMPOSE_FILE}.pre-upgrade" "${COMPOSE_FILE}"
    fi
    export FORGEX_VERSION="${CURRENT_VERSION}"

    if [ -f "${SCRIPT_DIR}/rollback.sh" ]; then
        bash "${SCRIPT_DIR}/rollback.sh" "${CURRENT_VERSION}" --force 2>/dev/null || {
            echo "  警告: 自动回滚失败, 请手动执行: bash ${SCRIPT_DIR}/rollback.sh ${CURRENT_VERSION}"
        }
    fi

    echo "${TIMESTAMP} | upgrade ${CURRENT_VERSION} -> ${NEW_VERSION} | FAILED | rolled back to ${CURRENT_VERSION}" >> "${UPGRADE_LOG}"
    echo ""
    echo "升级失败, 已回滚到版本 ${CURRENT_VERSION}。"
    echo "回滚后如有异常, 可使用备份恢复: bash ${SCRIPT_DIR}/restore.sh ${LATEST_BACKUP} --force"
    exit 2
fi

# ----------------------- 清理与记录 -----------------------
rm -f "${COMPOSE_FILE}.pre-upgrade"

echo "${TIMESTAMP} | upgrade ${CURRENT_VERSION} -> ${NEW_VERSION} | SUCCESS | backup: ${LATEST_BACKUP:-none}" >> "${UPGRADE_LOG}"

echo ""
echo "=========================================="
echo " 升级完成"
echo "=========================================="
echo "  版本: ${CURRENT_VERSION} -> ${NEW_VERSION}"
echo "  升级前备份: ${LATEST_BACKUP:-无}"
echo "  升级日志: ${UPGRADE_LOG}"
echo "=========================================="
echo ""
echo "如需回滚: bash ${SCRIPT_DIR}/rollback.sh ${CURRENT_VERSION}"
