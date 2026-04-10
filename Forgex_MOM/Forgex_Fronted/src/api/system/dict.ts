import http from '../http'

/**
 * 字典树节点
 */
export interface DictTreeNode {
  id: number
  dictCode: string
  dictName: string
  pId?: number
  children?: DictTreeNode[]
}

/**
 * 获取字典列表
 * @param params 查询参数
 */
export function getDictList(params?: any) {
  return http.get('/sys/dict/tree', params)
}

/**
 * 获取字典树
 * @param params 查询参数
 */
export function getDictTree(params?: any) {
  return http.get('/sys/dict/tree', params)
}
