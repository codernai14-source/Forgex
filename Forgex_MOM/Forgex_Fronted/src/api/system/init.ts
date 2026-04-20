import http from '../http'

/**
 * 鑾峰彇绯荤粺鍒濆鍖栫姸鎬?
 * @returns 鍒濆鍖栫姸鎬?
 */
export function getInit状态() {
  return http.get('/sys/init/status')
}

/**
 * 搴旂敤鍒濆鍖?
 * @param body 鍒濆鍖栨暟鎹?
 * @returns 缁撴灉
 */
export function applyInit(body: any) {
  return http.post('/sys/init/apply', body)
}
