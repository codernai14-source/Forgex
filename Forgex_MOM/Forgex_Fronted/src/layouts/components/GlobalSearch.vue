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
          placeholder="搜索菜单或页面..."
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
          <span class="results-count">找到 {{ filteredResults.length }} 个结果</span>
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
              <div class="result-title" v-html="highlightKeyword(item.title)"></div>
              <div class="result-path">{{ item.breadcrumb }}</div>
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
        <div class="empty-text">未找到相关菜单</div>
        <div class="empty-hint">尝试使用其他关键词</div>
      </div>

      <!-- 默认状态（快捷提示） -->
      <div v-else class="search-tips">
        <div class="tips-title">快捷搜索</div>
        <div class="tips-list">
          <div class="tip-item">
            <kbd>↑</kbd>
            <kbd>↓</kbd>
            <span>导航</span>
          </div>
          <div class="tip-item">
            <kbd>Enter</kbd>
            <span>选择</span>
          </div>
          <div class="tip-item">
            <kbd>ESC</kbd>
            <span>关闭</span>
          </div>
        </div>
      </div>
    </div>
  </a-modal>
</template>

<script setup lang="ts">
import { ref, computed, watch, nextTick } from 'vue'
import { getIcon } from '../../utils/icon'
import {
  SearchOutlined,
  CloseCircleOutlined,
  FileOutlined,
  InboxOutlined,
  EnterOutlined
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

interface SearchResult {
  key: string
  title: string
  icon?: string
  path: string
  breadcrumb: string
}

interface GlobalSearchProps {
  visible: boolean
  menus: MenuItem[]
}

const props = withDefaults(defineProps<GlobalSearchProps>(), {
  visible: false,
  menus: () => []
})

const emit = defineEmits<{
  'update:visible': [visible: boolean]
  'close': []
  'select': [menuKey: string, path: string]
}>()

// 搜索关键词
const keyword = ref('')
// 搜索输入框引用
const searchInputRef = ref<HTMLInputElement>()
// 当前激活的结果索引
const activeIndex = ref(0)

// 扁平化菜单列表（用于搜索）
const flatMenus = computed(() => {
  const result: SearchResult[] = []
  
  const flatten = (menus: MenuItem[], parentPath: string[] = []) => {
    for (const menu of menus) {
      // 只搜索菜单类型，不搜索按钮
      if (menu.type === 'menu') {
        const breadcrumb = [...parentPath, menu.title].join(' / ')
        result.push({
          key: menu.key,
          title: menu.title,
          icon: menu.icon,
          path: menu.path,
          breadcrumb
        })
      }
      
      // 递归处理子菜单
      if (menu.children && menu.children.length > 0) {
        flatten(menu.children, [...parentPath, menu.title])
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
  
  const kw = keyword.value.toLowerCase().trim()
  
  return flatMenus.value.filter(item => {
    // 标题匹配
    if (item.title.toLowerCase().includes(kw)) {
      return true
    }
    
    // 路径匹配
    if (item.breadcrumb.toLowerCase().includes(kw)) {
      return true
    }
    
    // 拼音匹配（简单实现，可以使用 pinyin 库增强）
    // 这里暂时只做简单的字符匹配
    
    return false
  }).slice(0, 20) // 最多显示 20 个结果
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

// 高亮关键词
const highlightKeyword = (text: string) => {
  if (!keyword.value.trim()) {
    return text
  }
  
  const kw = keyword.value.trim()
  const regex = new RegExp(`(${kw})`, 'gi')
  return text.replace(regex, '<mark>$1</mark>')
}
</script>

<style scoped lang="less">
.global-search-modal {
  top: 100px;
  
  :deep(.ant-modal-content) {
    border-radius: 12px;
    overflow: hidden;
    box-shadow: 0 12px 32px rgba(0, 0, 0, 0.12);
  }
}

.search-container {
  display: flex;
  flex-direction: column;
  max-height: 60vh;
}

.search-input-wrapper {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 16px 20px;
  border-bottom: 1px solid var(--fx-border-color, #1f2937);
  background: var(--fx-bg-container, #111827);
}

.search-icon {
  font-size: 18px;
  color: #9ca3af;
}

.search-input {
  flex: 1;
  height: 36px;
  padding: 0;
  font-size: 15px;
  border: none;
  outline: none;
  background: transparent;
  
  &::placeholder {
    color: #9ca3af;
  }
}

.clear-btn {
  color: #9ca3af;
  
  &:hover {
    color: #6b7280;
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
  background: #f9fafb;
  border-bottom: 1px solid #f0f0f0;
}

.results-count {
  font-size: 12px;
  color: #6b7280;
}

.results-list {
  flex: 1;
  overflow-y: auto;
  max-height: 400px;
  
  &::-webkit-scrollbar {
    width: 6px;
  }
  
  &::-webkit-scrollbar-thumb {
    background: rgba(0, 0, 0, 0.2);
    border-radius: 3px;
    
    &:hover {
      background: rgba(0, 0, 0, 0.3);
    }
  }
}

.result-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 20px;
  cursor: pointer;
  transition: background 0.2s;
  
  &:hover,
  &.result-item-active {
    background: #f3f4f6;
  }
  
  &.result-item-active {
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
  background: rgba(22, 119, 255, 0.1);
  border-radius: 6px;
}

.result-content {
  flex: 1;
  min-width: 0;
}

.result-title {
  font-size: 14px;
  font-weight: 500;
  color: #1f2937;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  
  :deep(mark) {
    background: #fef3c7;
    color: #92400e;
    padding: 2px 4px;
    border-radius: 2px;
  }
}

.result-path {
  font-size: 12px;
  color: #9ca3af;
  margin-top: 2px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.result-action {
  flex-shrink: 0;
}

.enter-icon {
  font-size: 14px;
  color: #9ca3af;
  opacity: 0;
  transition: opacity 0.2s;
}

.search-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
}

.empty-icon {
  font-size: 48px;
  color: #d1d5db;
  margin-bottom: 16px;
}

.empty-text {
  font-size: 15px;
  font-weight: 500;
  color: #6b7280;
  margin-bottom: 4px;
}

.empty-hint {
  font-size: 13px;
  color: #9ca3af;
}

.search-tips {
  padding: 24px 20px;
}

.tips-title {
  font-size: 13px;
  font-weight: 500;
  color: #6b7280;
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
  color: #6b7280;
  
  kbd {
    min-width: 24px;
    padding: 4px 8px;
    font-size: 12px;
    font-family: inherit;
    text-align: center;
    color: #4b5563;
    background: #f3f4f6;
    border-radius: 4px;
    border: 1px solid #e5e7eb;
    box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
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
