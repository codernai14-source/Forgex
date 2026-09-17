import http from '../http'

/**
 * 查询备份记录。
 */
export function pageBackup(data: { current: number; size: number }) {
  return http.post('/sys/backup/page', data)
}

/**
 * 手动触发备份。
 */
export function runBackup() {
  return http.post('/sys/backup/run')
}
