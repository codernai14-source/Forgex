#!/usr/bin/env bash
set -euo pipefail

# Forgex KMS 主密钥生成工具
# 生成 32 字节(256 位)随机主密钥, 输出 64 位 hex 字符串
# 详见: Forgex_Doc/后端/安全/KMS 主密钥管理.md

KEY_SIZE_BYTES=32
OUTPUT_FILE=""

usage() {
    cat <<EOF
用法: gen-kms-master-key.sh [-o <output-file>]

生成 32 字节(256 位) Forgex KMS 主密钥, 输出 64 位 hex 字符串。

选项:
  -o <path>   将密钥写入指定文件(权限 600), 同时输出 hex 到 stdout
  -h          显示帮助

示例:
  # 输出 hex 到 stdout
  gen-kms-master-key.sh

  # 写入文件(推荐)
  gen-kms-master-key.sh -o \${FORGEX_LICENSE_DIR}/kms.key

注入方式(二选一):
  export FORGEX_KMS_MASTER_KEY_HEX=<生成的hex>
  # 或
  export FORGEX_KMS_MASTER_KEY_FILE=<密钥文件路径>
EOF
}

while getopts ":o:h" opt; do
    case "${opt}" in
        o)
            OUTPUT_FILE="${OPTARG}"
            ;;
        h)
            usage
            exit 0
            ;;
        \?)
            echo "错误: 未知选项 -${OPTARG}" >&2
            usage >&2
            exit 1
            ;;
        :)
            echo "错误: 选项 -${OPTARG} 需要参数" >&2
            usage >&2
            exit 1
            ;;
    esac
done

# 生成 32 字节随机密钥并转为 hex
generate_hex() {
    if command -v openssl >/dev/null 2>&1; then
        openssl rand -hex "${KEY_SIZE_BYTES}"
    elif [ -r /dev/urandom ]; then
        head -c "${KEY_SIZE_BYTES}" /dev/urandom | od -An -tx1 | tr -d ' \n'
    else
        echo "错误: 需要 openssl 或 /dev/urandom 来生成随机密钥" >&2
        exit 1
    fi
}

KEY_HEX="$(generate_hex)"

# 校验长度: 32 字节 = 64 hex 字符
HEX_LEN="${#KEY_HEX}"
if [ "${HEX_LEN}" -ne 64 ]; then
    echo "错误: 生成的密钥长度异常(期望 64 位 hex, 实际 ${HEX_LEN} 位)" >&2
    exit 1
fi

# 输出 hex 到 stdout(可被脚本捕获)
echo "${KEY_HEX}"

# 写入文件
if [ -n "${OUTPUT_FILE}" ]; then
    mkdir -p "$(dirname "${OUTPUT_FILE}")"
    printf '%s' "${KEY_HEX}" > "${OUTPUT_FILE}"
    chmod 600 "${OUTPUT_FILE}"
    echo "" >&2
    echo "主密钥已写入: ${OUTPUT_FILE} (权限 600)" >&2
fi

# 输出注入提示到 stderr
cat >&2 <<EOF

请在 Forgex 服务环境中配置主密钥(二选一):

  方式一(环境变量, 适用容器/K8s Secret):
    export FORGEX_KMS_MASTER_KEY_HEX=${KEY_HEX}

  方式二(密钥文件, 推荐):
    export FORGEX_KMS_MASTER_KEY_FILE=${OUTPUT_FILE:-\${FORGEX_LICENSE_DIR}/kms.key}
    (默认读取 \$FORGEX_LICENSE_DIR/kms.key, 可不设该变量)

主密钥必须为 32 字节(64 位 hex 字符), 详见 KMS 主密钥管理文档。
EOF
