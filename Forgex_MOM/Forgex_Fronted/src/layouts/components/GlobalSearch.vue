<template>
  <a-modal
    :open="visible"
    :footer="null"
    :closable="false"
    :width="600"
    :body-style="{ padding: 0 }"
    class="global-search-modal"
    @cancel="onClose"
  >
    <div class="search-container">
      <!-- 搜索输入框 -->
      <div class="search-input-wrapper">
        <SearchOutlined class="search-icon" />
        <input
          ref="searchInputRef"
          v-model="keyword"
          type="text"
          class="search-input"
          :placeholder="t('layout.globalSearchPlaceholder')"
          @input="onSearch"
          @keydown="onKeyDown"
        />
        <a-button
          v-if="keyword"
          type="text"
          size="small"
          class="clear-btn"
          @click="onClear"
        >
          <CloseCircleOutlined />
        </a-button>
        <kbd class="search-shortcut">ESC</kbd>
      </div>

      <!-- 搜索结果列表 -->
      <div v-if="filteredResults.length > 0" class="search-results">
        <div class="results-header">
          <span class="results-count">{{ t('layout.globalSearchResultsCount', { count: filteredResults.length }) }}</span>
        </div>
        <div class="results-list">
          <div
            v-for="(item, index) in filteredResults"
            :key="item.key"
            class="result-item"
            :class="{ 'result-item-active': index === activeIndex }"
            @click="onSelect(item)"
            @mouseenter="activeIndex = index"
          >
            <div class="result-icon">
              <FxIcon v-if="item.icon" :name="item.icon" />
              <FileOutlined v-else />
            </div>
            <div class="result-content">
              <div class="result-title-row">
                <div class="result-title" v-html="highlightText(item.title)"></div>
                <span v-if="item.primaryMatch" class="result-match-tag">
                  <component :is="getMatchIcon(item.primaryMatch)" class="result-tag-icon" />
                  <span class="result-tag-label">{{ getMatchLabel(item.primaryMatch) }}</span>
                </span>
              </div>
              <div v-if="item.moduleName" class="result-meta">
                <span class="result-module-tag">
                  <AppstoreOutlined class="result-tag-icon result-tag-icon--module" />
                  <span class="result-tag-label" v-html="highlightText(item.moduleName)"></span>
                </span>
              </div>
              <div class="result-path" v-html="highlightText(item.breadcrumb)"></div>
              <div v-if="shouldShowPathHint(item)" class="result-path result-path--secondary" v-html="highlightText(item.path)"></div>
            </div>
            <div class="result-action">
              <EnterOutlined class="enter-icon" />
            </div>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-else-if="keyword" class="search-empty">
        <InboxOutlined class="empty-icon" />
        <div class="empty-text">{{ t('layout.globalSearchEmptyTitle') }}</div>
        <div class="empty-hint">{{ t('layout.globalSearchEmptyHint') }}</div>
      </div>

      <!-- 默认状态（快捷提示） -->
      <div v-else class="search-tips">
        <div class="tips-title">{{ t('layout.globalSearchTipsTitle') }}</div>
        <div class="tips-list">
          <div class="tip-item">
            <kbd>↑</kbd>
            <kbd>↓</kbd>
            <span>{{ t('layout.globalSearchTipNavigate') }}</span>
          </div>
          <div class="tip-item">
            <kbd>Enter</kbd>
            <span>{{ t('layout.globalSearchTipSelect') }}</span>
          </div>
          <div class="tip-item">
            <kbd>ESC</kbd>
            <span>{{ t('layout.globalSearchTipClose') }}</span>
          </div>
        </div>
      </div>
    </div>
  </a-modal>
</template>

<script setup lang="ts">
import { ref, computed, watch, nextTick } from 'vue'
import { useI18n } from 'vue-i18n'
import { pinyin } from 'pinyin-pro'
import FxIcon from '@/components/common/FxIcon.vue'
import { resolveMenuDisplayName, resolveModuleDisplayName } from '@/utils/menuI18n'
import {
  SearchOutlined,
  CloseCircleOutlined,
  FileOutlined,
  InboxOutlined,
  EnterOutlined,
  AppstoreOutlined,
  ApartmentOutlined,
  LinkOutlined,
  FontSizeOutlined,
} from '@ant-design/icons-vue'

interface MenuItem {
  key: string
  title: string
  icon?: string
  path: string
  moduleCode: string
  moduleName?: string
  parentKey?: string
  parentTitle?: string
  children?: MenuItem[]
  type: 'dir' | 'menu' | 'button'
}

type SearchMatchType =
  | 'title-exact'
  | 'title'
  | 'title-initials'
  | 'title-pinyin'
  | 'module-exact'
  | 'module'
  | 'module-initials'
  | 'module-pinyin'
  | 'breadcrumb'
  | 'path'

interface SearchResult {
  key: string
  title: string
  icon?: string
  path: string
  breadcrumb: string
  moduleName?: string
  titleNormalized: string
  titleCompact: string
  moduleNormalized: string
  moduleCompact: string
  breadcrumbNormalized: string
  breadcrumbCompact: string
  pathNormalized: string
  titlePinyin: string
  titleInitials: string
  modulePinyin: string
  moduleInitials: string
  primaryMatch?: SearchMatchType
  matchTypes?: SearchMatchType[]
}

interface GlobalSearchProps {
  /** 弹窗是否可见，用于控制组件的显示/隐藏状态 */
  visible: boolean
  /** 菜单项数组，包含所有菜单的层级结构数据，用于搜索 */
  menus: MenuItem[]
}

const props = withDefaults(defineProps<GlobalSearchProps>(), {
  visible: false,
  menus: () => []
})

const { t } = useI18n()

const emit = defineEmits<{
  /**
   * 更新弹窗可见性
   * 触发时机：用户关闭弹窗时触发
   * @param visible 新的可见性状态
   */
  'update:visible': [visible: boolean]
  /**
   * 关闭事件
   * 触发时机：用户点击关闭按钮或按 ESC 键时触发
   */
  'close': []
  /**
   * 选择菜单事件
   * 触发时机：用户点击搜索结果或按 Enter 键时触发
   * @param menuKey 选中的菜单 key
   * @param path 菜单对应的路由路径
   */
  'select': [menuKey: string, path: string]
}>()

// 搜索关键词
const keyword = ref('')
// 搜索输入框引用
const searchInputRef = ref<HTMLInputElement>()
// 当前激活的结果索引
const activeIndex = ref(0)

function normalizeSearchValue(value: unknown): string {
  return String(value ?? '')
    .trim()
    .toLowerCase()
    .replace(/\s+/g, ' ')
}

function normalizeCompactValue(value: unknown): string {
  return normalizeSearchValue(value).replace(/[\s/_-]+/g, '')
}

function buildPinyinTokens(value: unknown) {
  const text = String(value ?? '').trim()
  if (!text) {
    return {
      full: '',
      initials: '',
    }
  }

  try {
    return {
      full: pinyin(text, {
        toneType: 'none',
        pattern: 'pinyin',
        type: 'array',
        nonZh: 'consecutive',
        v: true,
      }).join('').toLowerCase(),
      initials: pinyin(text, {
        toneType: 'none',
        pattern: 'first',
        type: 'array',
        nonZh: 'consecutive',
        v: true,
      }).join('').toLowerCase(),
    }
  } catch (error) {
    console.warn('[GlobalSearch] 拼音索引生成失败:', error)
    return {
      full: '',
      initials: '',
    }
  }
}

function includesKeyword(source: string, keywordText: string): boolean {
  return !!keywordText && !!source && source.includes(keywordText)
}

function resolveMatchInfo(item: SearchResult, normalizedKeyword: string, compactKeyword: string) {
  if (!normalizedKeyword) {
    return {
      score: 0,
      primaryMatch: undefined,
      matchTypes: [] as SearchMatchType[],
    }
  }

  const candidates: Array<{ type: SearchMatchType; score: number; matched: boolean }> = [
    {
      type: 'title-exact',
      score: 1200,
      matched: item.titleNormalized === normalizedKeyword || (!!compactKeyword && item.titleCompact === compactKeyword),
    },
    {
      type: 'module-exact',
      score: 1100,
      matched: item.moduleNormalized === normalizedKeyword || (!!compactKeyword && item.moduleCompact === compactKeyword),
    },
    {
      type: 'title',
      score: 1000,
      matched: includesKeyword(item.titleNormalized, normalizedKeyword) || includesKeyword(item.titleCompact, compactKeyword),
    },
    {
      type: 'title-initials',
      score: 960,
      matched: !!compactKeyword && item.titleInitials.startsWith(compactKeyword),
    },
    {
      type: 'title-pinyin',
      score: 940,
      matched: !!compactKeyword && includesKeyword(item.titlePinyin, compactKeyword),
    },
    {
      type: 'title-initials',
      score: 920,
      matched: !!compactKeyword && includesKeyword(item.titleInitials, compactKeyword),
    },
    {
      type: 'module',
      score: 900,
      matched: includesKeyword(item.moduleNormalized, normalizedKeyword) || includesKeyword(item.moduleCompact, compactKeyword),
    },
    {
      type: 'module-initials',
      score: 860,
      matched: !!compactKeyword && item.moduleInitials.startsWith(compactKeyword),
    },
    {
      type: 'module-pinyin',
      score: 840,
      matched: !!compactKeyword && includesKeyword(item.modulePinyin, compactKeyword),
    },
    {
      type: 'module-initials',
      score: 820,
      matched: !!compactKeyword && includesKeyword(item.moduleInitials, compactKeyword),
    },
    {
      type: 'breadcrumb',
      score: 760,
      matched: includesKeyword(item.breadcrumbNormalized, normalizedKeyword) || includesKeyword(item.breadcrumbCompact, compactKeyword),
    },
    {
      type: 'path',
      score: 680,
      matched: includesKeyword(item.pathNormalized, normalizedKeyword),
    },
  ]

  const matches = candidates.filter(candidate => candidate.matched)
  if (matches.length === 0) {
    return {
      score: 0,
      primaryMatch: undefined,
      matchTypes: [] as SearchMatchType[],
    }
  }

  matches.sort((a, b) => b.score - a.score)

  return {
    score: matches[0].score,
    primaryMatch: matches[0].type,
    matchTypes: [...new Set(matches.map(match => match.type))],
  }
}

function escapeRegExp(value: string): string {
  return value.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
}

function escapeHtml(value: string): string {
  return value
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;')
}

function highlightText(text: string): string {
  const rawText = String(text ?? '')
  const trimmedKeyword = keyword.value.trim()
  if (!trimmedKeyword) {
    return escapeHtml(rawText)
  }

  const regex = new RegExp(escapeRegExp(trimmedKeyword), 'gi')
  let lastIndex = 0
  let result = ''
  let matched = false
  let match: RegExpExecArray | null

  while ((match = regex.exec(rawText)) !== null) {
    matched = true
    result += escapeHtml(rawText.slice(lastIndex, match.index))
    result += `<mark>${escapeHtml(match[0])}</mark>`
    lastIndex = match.index + match[0].length

    if (match.index === regex.lastIndex) {
      regex.lastIndex += 1
    }
  }

  if (!matched) {
    return escapeHtml(rawText)
  }

  result += escapeHtml(rawText.slice(lastIndex))
  return result
}

function getMatchLabel(matchType?: SearchMatchType): string {
  switch (matchType) {
    case 'module-exact':
    case 'module':
      return t('layout.globalSearchMatchModule')
    case 'title-initials':
    case 'title-pinyin':
      return t('layout.globalSearchMatchTitlePinyin')
    case 'module-initials':
    case 'module-pinyin':
      return t('layout.globalSearchMatchModulePinyin')
    case 'breadcrumb':
      return t('layout.globalSearchMatchBreadcrumb')
    case 'path':
      return t('layout.globalSearchMatchPath')
    case 'title-exact':
    case 'title':
    default:
      return t('layout.globalSearchMatchTitle')
  }
}

function getMatchIcon(matchType?: SearchMatchType) {
  switch (matchType) {
    case 'module-exact':
    case 'module':
      return AppstoreOutlined
    case 'title-initials':
    case 'title-pinyin':
    case 'module-initials':
    case 'module-pinyin':
      return FontSizeOutlined
    case 'breadcrumb':
      return ApartmentOutlined
    case 'path':
      return LinkOutlined
    case 'title-exact':
    case 'title':
    default:
      return SearchOutlined
  }
}

function shouldShowPathHint(item: SearchResult): boolean {
  return item.primaryMatch === 'path'
}

// 扁平化菜单列表（用于搜索）
const flatMenus = computed(() => {
  const result: SearchResult[] = []
  
  const flatten = (menus: MenuItem[], parentPath: string[] = [], currentModuleName = '') => {
    for (const menu of menus) {
      const resolvedTitle = resolveMenuDisplayName({
        path: menu.path,
        title: menu.title,
        moduleCode: menu.moduleCode,
        moduleName: menu.moduleName,
      })
      const resolvedModuleName = resolveModuleDisplayName(menu.moduleCode, menu.moduleName)
      const nextModuleName = resolvedModuleName || currentModuleName || (menu.type === 'dir' && parentPath.length === 0 ? resolvedTitle : '')

      // 只搜索菜单类型，不搜索按钮
      if (menu.type === 'menu') {
        const breadcrumb = [...parentPath, resolvedTitle].join(' / ')
        const titleTokens = buildPinyinTokens(resolvedTitle)
        const moduleTokens = buildPinyinTokens(nextModuleName)
        result.push({
          key: menu.key,
          title: resolvedTitle,
          icon: menu.icon,
          path: menu.path,
          breadcrumb,
          moduleName: nextModuleName,
          titleNormalized: normalizeSearchValue(resolvedTitle),
          titleCompact: normalizeCompactValue(resolvedTitle),
          moduleNormalized: normalizeSearchValue(nextModuleName),
          moduleCompact: normalizeCompactValue(nextModuleName),
          breadcrumbNormalized: normalizeSearchValue(breadcrumb),
          breadcrumbCompact: normalizeCompactValue(breadcrumb),
          pathNormalized: normalizeSearchValue(menu.path),
          titlePinyin: titleTokens.full,
          titleInitials: titleTokens.initials,
          modulePinyin: moduleTokens.full,
          moduleInitials: moduleTokens.initials,
        })
      }
      
      // 递归处理子菜单
      if (menu.children && menu.children.length > 0) {
        flatten(menu.children, [...parentPath, resolvedTitle], nextModuleName)
      }
    }
  }
  
  flatten(props.menus)
  return result
})

// 过滤后的搜索结果
const filteredResults = computed(() => {
  if (!keyword.value.trim()) {
    return []
  }
  
  const normalizedKeyword = normalizeSearchValue(keyword.value)
  const compactKeyword = normalizeCompactValue(keyword.value)

  return flatMenus.value
    .map(item => {
      const matchInfo = resolveMatchInfo(item, normalizedKeyword, compactKeyword)
      return {
        item: {
          ...item,
          primaryMatch: matchInfo.primaryMatch,
          matchTypes: matchInfo.matchTypes,
        },
        score: matchInfo.score,
      }
    })
    .filter(entry => entry.score > 0)
    .sort((a, b) => {
      if (b.score !== a.score) {
        return b.score - a.score
      }
      return a.item.breadcrumb.localeCompare(b.item.breadcrumb, 'zh-CN')
    })
    .map(entry => entry.item)
    .slice(0, 20)
})

// 监听 visible 变化
watch(
  () => props.visible,
  (newVal) => {
    if (newVal) {
      // 打开时聚焦输入框
      nextTick(() => {
        searchInputRef.value?.focus()
      })
      // 重置状态
      keyword.value = ''
      activeIndex.value = 0
    }
  }
)

// 监听搜索结果变化
watch(filteredResults, () => {
  activeIndex.value = 0
})

// 搜索
const onSearch = () => {
  // 实时搜索，无需额外处理
}

// 清空
const onClear = () => {
  keyword.value = ''
  searchInputRef.value?.focus()
}

// 关闭
const onClose = () => {
  emit('update:visible', false)
  emit('close')
}

// 选择结果
const onSelect = (item: SearchResult) => {
  emit('select', item.key, item.path)
  onClose()
}

// 键盘导航
const onKeyDown = (e: KeyboardEvent) => {
  const results = filteredResults.value
  
  if (results.length === 0) {
    return
  }
  
  switch (e.key) {
    case 'ArrowDown':
      e.preventDefault()
      activeIndex.value = (activeIndex.value + 1) % results.length
      scrollToActive()
      break
    case 'ArrowUp':
      e.preventDefault()
      activeIndex.value = (activeIndex.value - 1 + results.length) % results.length
      scrollToActive()
      break
    case 'Enter':
      e.preventDefault()
      if (results[activeIndex.value]) {
        onSelect(results[activeIndex.value])
      }
      break
    case 'Escape':
      e.preventDefault()
      onClose()
      break
  }
}

// 滚动到激活的结果
const scrollToActive = () => {
  nextTick(() => {
    const activeElement = document.querySelector('.result-item-active')
    if (activeElement) {
      activeElement.scrollIntoView({
        block: 'nearest',
        behavior: 'smooth'
      })
    }
  })
}

</script>

<style scoped lang="less" src="@/styles/layout/components/global-search.less"></style>
