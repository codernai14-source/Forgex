/**
 * 字典查询 Hook
 * 用于在组件中方便地获取字典数据
 */
import { ref } from 'vue';
import http from '@/api/http';
import { getLocale } from '@/locales';
// 字典缓存
const dictCache = new Map();
/**
 * 获取字典项
 * @param dictCode 字典编码
 * @returns 字典项数组 { label, value }
 */
export function useDict(dictCode) {
    const dictItems = ref([]);
    const loading = ref(false);
    const loadDict = async () => {
        const lang = getLocale();
        const cacheKey = `${dictCode}@@${lang}`;
        // 先从缓存读取
        if (dictCache.has(cacheKey)) {
            dictItems.value = dictCache.get(cacheKey) || [];
            return;
        }
        // 从服务器加载
        loading.value = true;
        try {
            const res = await http.post('/sys/dict/items', { dictCode });
            dictItems.value = res;
            // 写入缓存
            dictCache.set(cacheKey, res);
        }
        catch (error) {
            console.error(`加载字典失败: ${dictCode}`, error);
        }
        finally {
            loading.value = false;
        }
    };
    // 立即加载
    loadDict();
    return {
        dictItems,
        loading,
        reload: loadDict
    };
}
export function useDictByPath(nodePath) {
    const dictItems = ref([]);
    const loading = ref(false);
    const loadDict = async () => {
        const lang = getLocale();
        const cacheKey = `${nodePath}@@${lang}`;
        if (dictCache.has(cacheKey)) {
            dictItems.value = dictCache.get(cacheKey) || [];
            return;
        }
        loading.value = true;
        try {
            const res = await http.post('/sys/dict/itemsByPath', { nodePath });
            dictItems.value = res;
            dictCache.set(cacheKey, res);
        }
        catch (error) {
            console.error(`加载字典失败: ${nodePath}`, error);
        }
        finally {
            loading.value = false;
        }
    };
    loadDict();
    return { dictItems, loading, reload: loadDict };
}
/**
 * 清除字典缓存
 * @param dictCode 字典编码（可选，不传则清除所有）
 */
export function clearDictCache(dictCode) {
    if (dictCode) {
        for (const k of Array.from(dictCache.keys())) {
            if (k.startsWith(`${dictCode}@@`)) {
                dictCache.delete(k);
            }
        }
    }
    else {
        dictCache.clear();
    }
}
