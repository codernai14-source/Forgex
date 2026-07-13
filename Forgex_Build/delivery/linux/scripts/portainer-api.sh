#!/usr/bin/env bash
set -euo pipefail

# ============================================================
# Forgex Portainer API 封装脚本
#
# 封装 Portainer 2.x REST API 调用, 供运维脚本复用。
# 认证: Bearer token (POST /api/auth)
# 端点: /api/stacks, /api/endpoints/{id}/docker/containers/json
#
# 用法:
#   source portainer-api.sh          # 加载函数
#   portainer_login                  # 认证获取 token
#   portainer_list_containers        # 列出容器
#   portainer_deploy_stack <file>    # 部署/更新 stack
#   portainer_get_stack_logs <name>  # 获取 stack 日志
#
# 环境变量:
#   PORTAINER_URL      - Portainer 地址 (默认 http://localhost:9443)
#   PORTAINER_USER     - 管理员用户名 (默认 admin)
#   PORTAINER_PASSWORD - 管理员密码 (首次需设置)
#   PORTAINER_ENDPOINT_ID - 端点 ID (默认 1, 即 local)
# ============================================================

PORTAINER_URL="${PORTAINER_URL:-http://localhost:9443}"
PORTAINER_USER="${PORTAINER_USER:-admin}"
PORTAINER_PASSWORD="${PORTAINER_PASSWORD:-${FORGEX_PORTAINER_PASSWORD:-}}"
PORTAINER_ENDPOINT_ID="${PORTAINER_ENDPOINT_ID:-1}"

# Portainer token 缓存文件
PORTAINER_TOKEN_FILE="/tmp/.forgex-portainer-token-$$"

# ----------------------- 内部工具 -----------------------

# 获取 token (如已缓存则复用)
_portainer_get_token() {
    # 检查缓存
    if [ -f "${PORTAINER_TOKEN_FILE}" ]; then
        local cached
        cached=$(cat "${PORTAINER_TOKEN_FILE}")
        if [ -n "${cached}" ]; then
            echo "${cached}"
            return 0
        fi
    fi

    if [ -z "${PORTAINER_PASSWORD}" ]; then
        echo "错误: 未设置 PORTAINER_PASSWORD 或 FORGEX_PORTAINER_PASSWORD 环境变量" >&2
        return 1
    fi

    local response
    response=$(curl -sk -X POST "${PORTAINER_URL}/api/auth" \
        -H "Content-Type: application/json" \
        -d "{\"username\":\"${PORTAINER_USER}\",\"password\":\"${PORTAINER_PASSWORD}\"}" 2>/dev/null) || {
        echo "错误: Portainer 认证失败, 请检查 URL 和凭据" >&2
        return 1
    }

    local token
    token=$(echo "${response}" | grep -oP '"jwt":"\K[^"]+' || echo "")
    if [ -z "${token}" ]; then
        echo "错误: 无法从 Portainer 响应中提取 token" >&2
        echo "  响应: ${response}" >&2
        return 1
    fi

    echo "${token}" > "${PORTAINER_TOKEN_FILE}"
    chmod 600 "${PORTAINER_TOKEN_FILE}" 2>/dev/null || true
    echo "${token}"
}

# 带认证的 GET 请求
_portainer_get() {
    local path="$1"
    local token
    token=$(_portainer_get_token) || return 1
    curl -sk "${PORTAINER_URL}${path}" \
        -H "Authorization: Bearer ${token}" \
        -H "Content-Type: application/json"
}

# 带认证的 POST 请求
_portainer_post() {
    local path="$1"
    local data="$2"
    local token
    token=$(_portainer_get_token) || return 1
    curl -sk -X POST "${PORTAINER_URL}${path}" \
        -H "Authorization: Bearer ${token}" \
        -H "Content-Type: application/json" \
        -d "${data}"
}

# 带认证的 PUT 请求
_portainer_put() {
    local path="$1"
    local data="$2"
    local token
    token=$(_portainer_get_token) || return 1
    curl -sk -X PUT "${PORTAINER_URL}${path}" \
        -H "Authorization: Bearer ${token}" \
        -H "Content-Type: application/json" \
        -d "${data}"
}

# ----------------------- 公共 API -----------------------

# Portainer 认证
portainer_login() {
    local token
    token=$(_portainer_get_token) || return 1
    echo "Portainer 认证成功 (${PORTAINER_URL})"
    echo "  Token: ${token:0:20}..."
}

# 列出所有容器
portainer_list_containers() {
    _portainer_get "/api/endpoints/${PORTAINER_ENDPOINT_ID}/docker/containers/json?all=true"
}

# 列出所有 stack
portainer_list_stacks() {
    _portainer_get "/api/stacks"
}

# 查找指定名称的 stack ID
_portainer_find_stack_id() {
    local stack_name="$1"
    local stacks
    stacks=$(_portainer_get "/api/stacks" 2>/dev/null)
    echo "${stacks}" | grep -oP "\"Id\":\d+,\"Name\":\"${stack_name}\"" | grep -oP '"Id":\K\d+' || echo ""
}

# 部署或更新 stack
# 参数: <compose-file-path> [stack-name]
portainer_deploy_stack() {
    local compose_file="$1"
    local stack_name="${2:-forgex}"

    if [ ! -f "${compose_file}" ]; then
        echo "错误: compose 文件不存在: ${compose_file}" >&2
        return 1
    fi

    local compose_content
    compose_content=$(cat "${compose_file}")

    # 检查 stack 是否已存在
    local stack_id
    stack_id=$(_portainer_find_stack_id "${stack_name}")

    if [ -n "${stack_id}" ]; then
        # 更新现有 stack
        echo "更新 stack '${stack_name}' (ID: ${stack_id})..."
        _portainer_put "/api/stacks/${stack_id}" \
            "{\"stackFileContent\":\"$(echo "${compose_content}" | sed ':a;N;$!ba;s/\n/\\n/g' | sed 's/"/\\"/g')\"}"
        echo "Stack '${stack_name}' 更新完成"
    else
        # 创建新 stack (type=1=standalone, endpointId=1)
        echo "创建 stack '${stack_name}'..."
        _portainer_post "/api/stacks?type=1&method=string&endpointId=${PORTAINER_ENDPOINT_ID}" \
            "{\"Name\":\"${stack_name}\",\"StackFileContent\":\"$(echo "${compose_content}" | sed ':a;N;$!ba;s/\n/\\n/g' | sed 's/"/\\"/g')\"}"
        echo "Stack '${stack_name}' 创建完成"
    fi
}

# 获取容器日志
# 参数: <container-name-or-id> [tail-lines]
portainer_get_container_logs() {
    local container_id="$1"
    local tail_lines="${2:-100}"
    _portainer_get "/api/endpoints/${PORTAINER_ENDPOINT_ID}/docker/containers/${container_id}/logs?stdout=true&stderr=true&tail=${tail_lines}"
}

# 获取 stack 下所有容器
# 参数: <stack-name>
portainer_get_stack_containers() {
    local stack_name="$1"
    local containers
    containers=$(_portainer_get "/api/endpoints/${PORTAINER_ENDPOINT_ID}/docker/containers/json?all=true&filters=%7B%22label%22%3A%5B%22com.docker.compose.project%3D${stack_name}%22%5D%7D" 2>/dev/null)
    echo "${containers}"
}

# 清理 token 缓存
portainer_cleanup() {
    rm -f "${PORTAINER_TOKEN_FILE}" 2>/dev/null || true
}

# 注册退出清理
trap portainer_cleanup EXIT

# 如果直接执行此脚本 (非 source), 打印帮助
if [ "${BASH_SOURCE[0]}" = "${0}" ]; then
    echo "Forgex Portainer API 封装脚本"
    echo ""
    echo "用法: source portainer-api.sh   # 加载函数后调用"
    echo ""
    echo "可用函数:"
    echo "  portainer_login                  - 认证获取 token"
    echo "  portainer_list_containers        - 列出所有容器"
    echo "  portainer_list_stacks            - 列出所有 stack"
    echo "  portainer_deploy_stack <file> [name] - 部署/更新 stack"
    echo "  portainer_get_container_logs <id> [lines] - 获取容器日志"
    echo "  portainer_get_stack_containers <name>   - 获取 stack 下容器"
    echo ""
    echo "环境变量:"
    echo "  PORTAINER_URL      (默认 http://localhost:9443)"
    echo "  PORTAINER_USER     (默认 admin)"
    echo "  PORTAINER_PASSWORD (必填)"
    echo "  PORTAINER_ENDPOINT_ID (默认 1)"
fi
