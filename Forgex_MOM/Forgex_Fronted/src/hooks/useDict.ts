/**
 * 字典查询逻辑封装
 * 
 * 用于在组件中方便地获取字典数据，支持国际化缓存。
 * 
 * @author Forgex
 * @version 1.0.0
 */
import { ref, watch } from 'vue'
import http from '@/api/http'
import i18n, { getLocale } from '@/locales'

export interface DictTagStyle {
  color?: string
  icon?: string
}

export interface DictItemOption {
  label: string
  value: string | number
  tagStyle?: DictTagStyle | null
}

// 字典缓存（按语言分离）
const dictCache = new Map<string, DictItemOption[]>()

export function findDictItem(items: DictItemOption[] | undefined, value: string | number | null | undefined) {
  const normalizedValue = String(value ?? '')
  return (items || []).find(item => String(item?.value ?? '') === normalizedValue) || null
}

export function getDictItemLabel(items: DictItemOption[] | undefined, value: string | number | null | undefined, fallback = '') {
  return findDictItem(items, value)?.label || fallback
}

export function getDictItemTagStyle(items: DictItemOption[] | undefined, value: string | number | null | undefined) {
  return findDictItem(items, value)?.tagStyle || null
}

/**
 * 获取字典项
 * 
 * 执行步骤：
 * 1. 创建响应式的字典项数组和加载状态
 * 2. 生成缓存键（字典编码 + 语言）
 * 3. 尝试从缓存读取
 * 4. 缓存未命中时从服务器加载
 * 5. 将结果写入缓存
 * 6. 返回字典项、加载状态和重载方法
 * 
 * @param dictCode 字典编码
 * @returns 包含 dictItems（字典项数组）、loading（加载状态）、reload（重载方法）的对象
 * @throws 加载失败时在控制台输出错误日志
 */
export function useDict(dictCode: string) {
  const dictItems = ref<DictItemOption[]>([])
  const loading = ref(false)

  const loadDict = async (forceReload = false) => {
    const lang = getLocale()
    const cacheKey = `${dictCode}@@${lang}`
    // 先从缓存读取
    if (!forceReload && dictCache.has(cacheKey)) {
      dictItems.value = dictCache.get(cacheKey) || []
      return
    }

    // 从服务器加载
    loading.value = true
    try {
      const res = await http.post<DictItemOption[]>('/sys/dict/items', { dictCode })
      dictItems.value = Array.isArray(res) ? res : []
      // 写入缓存
      dictCache.set(cacheKey, dictItems.value)
    } catch (error) {
      console.error(`加载字典失败：${dictCode}`, error)
    } finally {
      loading.value = false
    }
  }

  watch(
    () => i18n.global.locale.value,
    () => {
      loadDict()
    },
    { immediate: true },
  )

  return {
    dictItems,
    loading,
    reload: loadDict
  }
}

/**
 * 根据路径获取字典项
 * 
 * 执行步骤：
 * 1. 创建响应式的字典项数组和加载状态
 * 2. 生成缓存键（节点路径 + 语言）
 * 3. 尝试从缓存读取
 * 4. 缓存未命中时从服务器加载
 * 5. 将结果写入缓存
 * 6. 返回字典项、加载状态和重载方法
 * 
 * @param nodePath 字典节点路径
 * @returns 包含 dictItems（字典项数组）、loading（加载状态）、reload（重载方法）的对象
 * @throws 加载失败时在控制台输出错误日志
 */
export function useDictByPath(nodePath: string) {
  const dictItems = ref<DictItemOption[]>([])
  const loading = ref(false)

  const loadDict = async (forceReload = false) => {
    const lang = getLocale()
    const cacheKey = `${nodePath}@@${lang}`
    if (!forceReload && dictCache.has(cacheKey)) {
      dictItems.value = dictCache.get(cacheKey) || []
      return
    }
    loading.value = true
    try {
      const res = await http.post<DictItemOption[]>('/sys/dict/itemsByPath', { nodePath })
      dictItems.value = Array.isArray(res) ? res : []
      dictCache.set(cacheKey, dictItems.value)
    } catch (error) {
      console.error(`加载字典失败：${nodePath}`, error)
    } finally {
      loading.value = false
    }
  }

  watch(
    () => i18n.global.locale.value,
    () => {
      loadDict()
    },
    { immediate: true },
  )

  return { dictItems, loading, reload: loadDict }
}

/**
 * 清除字典缓存
 * 
 * 执行步骤：
 * 1. 如果指定了字典编码，删除所有匹配该编码的缓存键
 * 2. 如果未指定字典编码，清空所有缓存
 * 
 * @param dictCode 字典编码（可选，不传则清除所有）
 */
export function clearDictCache(dictCode?: string) {
  if (dictCode) {
    for (const k of Array.from(dictCache.keys())) {
      if (k.startsWith(`${dictCode}@@`)) {
        dictCache.delete(k)
      }
    }
  } else {
    dictCache.clear()
  }
}
