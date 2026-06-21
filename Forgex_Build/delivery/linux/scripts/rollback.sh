#!/usr/bin/env bash
set -euo pipefail

# ============================================================
# Forgex 版本回滚脚本
#
# 用法: bash rollback.sh [version] [--force]
#
# 依赖: upgrade 前的 backup 与镜像快照
# 流程: 校验目标版本镜像存在 -> 停服 -> 切换镜像 tag -> 起服 -> 健康检查
# ============================================================

INSTANCE_CODE="${FORGEX_INSTANCE_CODE:-ACME_PROD}"
FORGEX_HOME="${FORGEX_HOME:-/opt/Forgex_${INSTANCE_CODE}}"
COMPOSE_FILE="${FORGEX_HOME}/app/docker-compose.yml"
COMPOSE_PROJECT_NAME="${COMPOSE_PROJECT_NAME:-forgex_$(echo "${INSTANCE_CODE}" | tr '[:upper:]' '[:lower:]')}"
UPGRADE_LOG="${FORGEX_HOME}/upgrade.log"
FORGEX_BACKUP_DIR="${FORGEX_BACKUP_DIR:-${FORGEX_HOME}/backup}"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# ----------------------- 参数解析 -----------------------
TARGET_VERSION=""
FORCE=false

for arg in "$@"; do
    case "${arg}" in
        --force) FORCE=true ;;
        [0-9]*) TARGET_VERSION="${arg}" ;;
    esac
done

# ----------------------- 确定回滚目标版本 -----------------------
CURRENT_VERSION="${FORGEX_VERSION:-1.0.0}"
if [ -f "${COMPOSE_FILE}" ]; then
    CURRENT_VERSION=$(grep -oP 'FORGEX_VERSION:-\K[0-9.]+' "${COMPOSE_FILE}" 2>/dev/null || echo "${CURRENT_VERSION}")
fi

if [ -z "${TARGET_VERSION}" ]; then
    echo "未指定回滚版本, 从升级日志查找上一版本..."
    if [ -f "${UPGRADE_LOG}" ]; then
        # 查找最近一条成功的升级记录, 取其"from"版本
        TARGET_VERSION=$(grep "SUCCESS" "${UPGRADE_LOG}" | tail -1 | grep -oP 'upgrade \K[0-9.]+' || echo "")
    fi
    if [ -z "${TARGET_VERSION}" ]; then
        echo "错误: 无法确定回滚目标版本, 请手动指定: bash rollback.sh <version>"
        echo "  例如: bash rollback.sh 1.0.0"
        exit 1
    fi
    echo "  自动检测到上一版本: ${TARGET_VERSION}"
fi

if [ "${TARGET_VERSION}" = "${CURRENT_VERSION}" ]; then
    echo "当前版本已是 ${CURRENT_VERSION}, 无需回滚。"
    exit 0
fi

TIMESTAMP="$(date '+%Y-%m-%d %H:%M:%S')"

echo "=========================================="
echo " Forgex 版本回滚"
echo "=========================================="
echo "  实例: ${INSTANCE_CODE}"
echo "  当前版本: ${CURRENT_VERSION}"
echo "  回滚目标: ${TARGET_VERSION}"
echo "  时间: ${TIMESTAMP}"
echo "=========================================="

# ----------------------- 二次确认 -----------------------
if [ "${FORCE}" != "true" ]; then
    echo ""
    echo "警告: 回滚将停止所有服务并切换到版本 ${TARGET_VERSION}!"
    echo "确认回滚? 输入 'YES' 继续, 其他任意输入取消:"
    read -r CONFIRM
    if [ "${CONFIRM}" != "YES" ]; then
        echo "已取消回滚。"
        exit 0
    fi
fi

# ----------------------- 1. 校验目标版本镜像存在 -----------------------
echo ""
echo "[1/4] 校验目标版本镜像..."
SERVICES="gateway auth sys basic job integration workflow report"
MISSING_IMAGES=()
for svc in ${SERVICES}; do
    IMAGE="forgex/forgex-${svc}:${TARGET_VERSION}"
    if ! docker image inspect "${IMAGE}" >/dev/null 2>&1; then
        # 尝试拉取
        echo "  本地未找到 ${IMAGE}, 尝试拉取..."
        if ! docker pull "${IMAGE}" 2>/dev/null; then
            MISSING_IMAGES+=("${IMAGE}")
        fi
    fi
done

if [ ${#MISSING_IMAGES[@]} -gt 0 ]; then
    echo "  错误: 以下镜像不可用, 无法回滚:"
    for img in "${MISSING_IMAGES[@]}"; do
        echo "    - ${img}"
    done
    echo ""
    echo "  可能原因: 该版本镜像从未构建或已被清理。"
    echo "  替代方案: 使用备份恢复: bash ${SCRIPT_DIR}/restore.sh <backup-file> --force"
    exit 1
fi
echo "  全部目标版本镜像可用"

# ----------------------- 2. 停止服务 -----------------------
echo ""
echo "[2/4] 停止 compose 服务..."
if [ -f "${COMPOSE_FILE}" ]; then
    docker compose -f "${COMPOSE_FILE}" down 2>/dev/null || {
        docker ps -a --filter "label=com.docker.compose.project=${COMPOSE_PROJECT_NAME}" -q | xargs -r docker stop 2>/dev/null || true
    }
    echo "  服务已停止"
else
    echo "  错误: compose 文件不存在: ${COMPOSE_FILE}"
    exit 1
fi

# ----------------------- 3. 切换镜像版本 -----------------------
echo ""
echo "[3/4] 切换 compose 文件镜像版本到 ${TARGET_VERSION}..."
sed -i.bak "s/\${FORGEX_VERSION:-[0-9.]*}/\${FORGEX_VERSION:-${TARGET_VERSION}}/g" "${COMPOSE_FILE}"
rm -f "${COMPOSE_FILE}.bak"
export FORGEX_VERSION="${TARGET_VERSION}"
echo "  compose 文件已更新"

# ----------------------- 4. 拉起服务与健康检查 -----------------------
echo ""
echo "[4/4] 拉起服务并健康检查..."
docker compose -f "${COMPOSE_FILE}" up -d

sleep 15
HEALTH_OK=true
for svc in ${SERVICES}; do
    CONTAINER_NAME="${COMPOSE_PROJECT_NAME}-${svc}-1"
    STATE=$(docker inspect -f '{{.State.Status}}' "${CONTAINER_NAME}" 2>/dev/null || echo "not_found")
    if [ "${STATE}" = "running" ]; then
        echo "  ${svc}: running"
    else
        echo "  ${svc}: ${STATE} (异常)"
        HEALTH_OK=false
    fi
done

# ----------------------- 记录 -----------------------
echo "${TIMESTAMP} | rollback ${CURRENT_VERSION} -> ${TARGET_VERSION} | $([ "${HEALTH_OK}" = "true" ] && echo "SUCCESS" || echo "PARTIAL")" >> "${UPGRADE_LOG}"

echo ""
echo "=========================================="
echo " 回滚报告"
echo "=========================================="
echo "  版本: ${CURRENT_VERSION} -> ${TARGET_VERSION}"
echo "  服务健康: $([ "${HEALTH_OK}" = "true" ] && echo "全部正常" || echo "存在异常")"
echo "  升级日志: ${UPGRADE_LOG}"
echo "=========================================="

if [ "${HEALTH_OK}" = "true" ]; then
    echo "回滚完成, 所有服务运行正常。"
else
    echo "警告: 部分服务异常, 请检查日志: docker compose -f ${COMPOSE_FILE} logs"
    echo "如需从备份恢复: bash ${SCRIPT_DIR}/restore.sh <backup-file> --force"
    exit 2
fi
