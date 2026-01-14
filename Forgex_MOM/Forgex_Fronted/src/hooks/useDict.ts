/**
 * 字典查询 Hook
 * 用于在组件中方便地获取字典数据
 */
import { ref } from 'vue'
import http from '@/api/http'

// 字典缓存
const dictCache = new Map<string, any[]>()

/**
 * 获取字典项
 * @param dictCode 字典编码
 * @returns 字典项数组 { label, value }
 */
export function useDict(dictCode: string) {
  const dictItems = ref<any[]>([])
  const loading = ref(false)

  const loadDict = async () => {
    // 先从缓存读取
    if (dictCache.has(dictCode)) {
      dictItems.value = dictCache.get(dictCode) || []
      return
    }

    // 从服务器加载
    loading.value = true
    try {
      const res = await http.post('/sys/dict/items', { dictCode })
      dictItems.value = res
      // 写入缓存
      dictCache.set(dictCode, res)
    } catch (error) {
      console.error(`加载字典失败: ${dictCode}`, error)
    } finally {
      loading.value = false
    }
  }

  // 立即加载
  loadDict()

  return {
    dictItems,
    loading,
    reload: loadDict
  }
}

/**
 * 清除字典缓存
 * @param dictCode 字典编码（可选，不传则清除所有）
 */
export function clearDictCache(dictCode?: string) {
  if (dictCode) {
    dictCache.delete(dictCode)
  } else {
    dictCache.clear()
  }
}
