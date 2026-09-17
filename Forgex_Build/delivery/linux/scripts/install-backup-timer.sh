#!/usr/bin/env bash
set -euo pipefail

# 注册 Linux systemd timer，每日调用 backup.sh，并可选 rsync 异地。
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
UNIT_DIR="${1:-/etc/systemd/system}"

cp "${SCRIPT_DIR}/forgex-backup.service" "${UNIT_DIR}/forgex-backup.service"
cp "${SCRIPT_DIR}/forgex-backup.timer" "${UNIT_DIR}/forgex-backup.timer"
systemctl daemon-reload
systemctl enable --now forgex-backup.timer
systemctl list-timers forgex-backup.timer --no-pager
