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
EOF

cat <<EOF
Forgex Linux instance initialized:
  ${FORGEX_HOME}

Next steps:
  1. Review ${FORGEX_HOME}/.env and set the correct profile and middleware endpoints
  2. Import images into Docker or Portainer
  3. Generate the compose stack from docker-compose.yml.template
  4. Deploy with docker compose up -d or import the stack into Portainer
EOF
