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
              <component v-if="item.icon" :is="getIcon(item.icon)" />
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
import { getIcon } from '../../utils/icon'
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
      const nextModuleName = menu.moduleName || currentModuleName || (menu.type === 'dir' && parentPath.length === 0 ? menu.title : '')

      // 只搜索菜单类型，不搜索按钮
      if (menu.type === 'menu') {
        const breadcrumb = [...parentPath, menu.title].join(' / ')
        const titleTokens = buildPinyinTokens(menu.title)
        const moduleTokens = buildPinyinTokens(nextModuleName)
        result.push({
          key: menu.key,
          title: menu.title,
          icon: menu.icon,
          path: menu.path,
          breadcrumb,
          moduleName: nextModuleName,
          titleNormalized: normalizeSearchValue(menu.title),
          titleCompact: normalizeCompactValue(menu.title),
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
        flatten(menu.children, [...parentPath, menu.title], nextModuleName)
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

<style scoped lang="less">
.global-search-modal {
  top: 100px;
  
  :deep(.ant-modal-content) {
    border-radius: 12px;
    overflow: hidden;
    color: var(--fx-text-primary, #1f2937);
    background: var(--fx-search-bg, var(--fx-bg-elevated, #ffffff));
    border: 1px solid var(--fx-border-color, #e5e7eb);
    box-shadow: var(--fx-search-shadow, var(--fx-shadow-secondary, 0 12px 32px rgba(0, 0, 0, 0.12)));
  }

  :deep(.ant-modal-body) {
    background: var(--fx-search-bg, var(--fx-bg-elevated, #ffffff));
  }
}

.search-container {
  display: flex;
  flex-direction: column;
  max-height: 60vh;
  background: var(--fx-search-bg, var(--fx-bg-elevated, #ffffff));
}

.search-input-wrapper {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 16px 20px;
  border-bottom: 1px solid var(--fx-border-color, #1f2937);
  background: var(--fx-search-input-bg, var(--fx-bg-container, #111827));
}

.search-icon {
  font-size: 18px;
  color: var(--fx-text-tertiary, #9ca3af);
}

.search-input {
  flex: 1;
  height: 36px;
  padding: 0;
  font-size: 15px;
  color: var(--fx-text-primary, #1f2937);
  border: none;
  outline: none;
  background: transparent;
  
  &::placeholder {
    color: var(--fx-text-tertiary, #9ca3af);
  }
}

.clear-btn {
  color: var(--fx-text-tertiary, #9ca3af);

  &:hover {
    color: var(--fx-text-secondary, #6b7280);
  }
}

.search-shortcut {
  padding: 4px 8px;
  font-size: 12px;
  color: var(--fx-header-shortcut-color, #9ca3af);
  background: var(--fx-header-shortcut-bg, #1f2937);
  border-radius: 4px;
  border: 1px solid var(--fx-border-color, #374151);
}

.search-results {
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.results-header {
  padding: 8px 20px;
  background: var(--fx-search-section-bg, var(--fx-fill-alter, #f9fafb));
  border-bottom: 1px solid var(--fx-border-secondary, #f0f0f0);
}

.results-count {
  font-size: 12px;
  color: var(--fx-text-secondary, #6b7280);
}

.results-list {
  flex: 1;
  overflow-y: auto;
  max-height: 400px;
  background: var(--fx-search-bg, var(--fx-bg-elevated, #ffffff));

  &::-webkit-scrollbar {
    width: 6px;
  }
  
  &::-webkit-scrollbar-thumb {
    background: var(--fx-search-scrollbar-thumb, rgba(0, 0, 0, 0.2));
    border-radius: 3px;
    
    &:hover {
      background: var(--fx-search-scrollbar-thumb-hover, rgba(0, 0, 0, 0.3));
    }
  }
}

.result-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 20px;
  cursor: pointer;
  transition: background 0.2s ease, box-shadow 0.2s ease;

  &:hover,
  &.result-item-active {
    background: var(--fx-search-hover-bg, rgba(22, 119, 255, 0.12));
  }
  
  &.result-item-active {
    background: var(--fx-search-active-bg, rgba(22, 119, 255, 0.18));
    box-shadow: inset 0 0 0 1px var(--fx-search-hover-bg, rgba(22, 119, 255, 0.12));

    .enter-icon {
      opacity: 1;
    }
  }
}

.result-icon {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  font-size: 16px;
  color: var(--fx-theme-color, #1677ff);
  background: var(--fx-search-icon-bg, rgba(22, 119, 255, 0.1));
  border-radius: 6px;
}

.result-content {
  flex: 1;
  min-width: 0;
}

.result-title-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.result-title {
  flex: 1;
  min-width: 0;
  font-size: 14px;
  font-weight: 500;
  color: var(--fx-text-primary, #1f2937);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  
  :deep(mark) {
    background: var(--fx-search-highlight-bg, rgba(22, 119, 255, 0.18));
    color: var(--fx-search-highlight-color, var(--fx-theme-color, #1677ff));
    padding: 2px 4px;
    border-radius: 2px;
  }
}

.result-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  margin-top: 6px;
}

.result-match-tag,
.result-module-tag {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  max-width: 100%;
  min-height: 22px;
  padding: 0 8px;
  font-size: 12px;
  line-height: 20px;
  border-radius: 999px;
  border: 1px solid transparent;
  white-space: nowrap;
}

.result-match-tag {
  flex-shrink: 0;
  color: var(--fx-search-badge-color, var(--fx-theme-color, #1677ff));
  background: var(--fx-search-badge-bg, rgba(22, 119, 255, 0.12));
  border-color: var(--fx-search-badge-border, rgba(22, 119, 255, 0.18));
}

.result-tag-icon {
  flex-shrink: 0;
  font-size: 12px;
  opacity: 0.92;
}

.result-tag-label {
  min-width: 0;
}

.result-module-tag {
  max-width: 220px;
  color: var(--fx-search-module-color, var(--fx-text-secondary, #6b7280));
  background: var(--fx-search-module-bg, var(--fx-fill-secondary, #f9fafb));
  border-color: var(--fx-search-module-border, var(--fx-border-color, #e5e7eb));
  overflow: hidden;
  text-overflow: ellipsis;

  .result-tag-label {
    overflow: hidden;
    text-overflow: ellipsis;
  }

  :deep(mark) {
    background: var(--fx-search-highlight-bg, rgba(22, 119, 255, 0.18));
    color: var(--fx-search-highlight-color, var(--fx-theme-color, #1677ff));
    padding: 0 2px;
    border-radius: 2px;
  }
}

.result-path {
  font-size: 12px;
  color: var(--fx-text-tertiary, #9ca3af);
  margin-top: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;

  :deep(mark) {
    background: var(--fx-search-highlight-bg, rgba(22, 119, 255, 0.18));
    color: var(--fx-search-highlight-color, var(--fx-theme-color, #1677ff));
    padding: 0 2px;
    border-radius: 2px;
  }
}

.result-path--secondary {
  font-family: 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, Courier, monospace;
  opacity: 0.9;
}

.result-action {
  flex-shrink: 0;
}

.enter-icon {
  font-size: 14px;
  color: var(--fx-text-tertiary, #9ca3af);
  opacity: 0;
  transition: opacity 0.2s;
}

.search-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  background: var(--fx-search-bg, var(--fx-bg-elevated, #ffffff));
}

.empty-icon {
  font-size: 48px;
  color: var(--fx-text-disabled, #d1d5db);
  margin-bottom: 16px;
}

.empty-text {
  font-size: 15px;
  font-weight: 500;
  color: var(--fx-text-secondary, #6b7280);
  margin-bottom: 4px;
}

.empty-hint {
  font-size: 13px;
  color: var(--fx-text-tertiary, #9ca3af);
}

.search-tips {
  padding: 24px 20px;
  background: var(--fx-search-bg, var(--fx-bg-elevated, #ffffff));
}

.tips-title {
  font-size: 13px;
  font-weight: 500;
  color: var(--fx-text-secondary, #6b7280);
  margin-bottom: 16px;
}

.tips-list {
  display: flex;
  flex-direction: row;
  gap: 24px;
  flex-wrap: wrap;
}

.tip-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: var(--fx-text-secondary, #6b7280);

  kbd {
    min-width: 24px;
    padding: 4px 8px;
    font-size: 12px;
    font-family: inherit;
    text-align: center;
    color: var(--fx-text-secondary, #4b5563);
    background: var(--fx-header-shortcut-bg, #f3f4f6);
    border-radius: 4px;
    border: 1px solid var(--fx-border-color, #e5e7eb);
    box-shadow: 0 1px 2px color-mix(in srgb, var(--fx-bg-mask, rgba(0, 0, 0, 0.45)) 10%, transparent);
  }
}

// 响应式适配
@media (max-width: 768px) {
  .global-search-modal {
    top: 20px;
    
    :deep(.ant-modal) {
      max-width: calc(100vw - 32px);
      margin: 0 auto;
    }
  }
  
  .search-results {
    max-height: 50vh;
  }
}
</style>
