import http, { silentHttp } from '../http'

/**
 * 获取用户路由
 * @param body 参数 { account, tenantId }
 * @returns 路由列表
 */
export function getRoutes(
  body: { account: string; tenantId: string },
  options: { silent?: boolean } = {},
) {
  const client = options.silent ? silentHttp : http
  return client.post('/sys/menu/routes', body, {
    actionKey: options.silent ? `permission-routes:${body.account}:${body.tenantId}` : undefined,
    dedupeMode: options.silent ? 'drop' : undefined,
  })
}
