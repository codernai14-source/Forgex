<template>
  <div ref="tabbarRootRef" class="app-tabbar fx-guide-tabbar">
    <div
      ref="tabbarInnerRef"
      class="tabbar-inner fx-guide-tabbar-list"
      @scroll="scheduleMeasureClippedTabs"
    >
      <div
        v-for="tab in tabs"
        :key="tab.key"
        :ref="el => bindTabBarTabEl(tab.key, el)"
        class="tab-item"
        :class="{
          'tab-item-active': tab.key === activeKey,
          'tab-item-dragging': draggingKey === tab.key
        }"
        :draggable="draggable && tab.closable"
        @click="onTabClick(tab)"
        @dragstart="onDragStart(tab, $event)"
        @dragover="onDragOver($event)"
        @drop="onDrop(tab, $event)"
        @dragend="onDragEnd"
        @contextmenu.prevent="onContextMenu(tab, $event)"
      >
        <FxIcon
          v-if="tab.icon"
          :name="tab.icon"
          :fallback="false"
          class="tab-icon"
          :size="14"
        />
        <span class="tab-title">{{ tab.title }}</span>
        <CloseOutlined
          v-if="tab.closable"
          class="tab-close"
          @click.stop="onTabClose(tab)"
        />
      </div>
    </div>

    <!-- 右侧操作按钮 -->
    <div class="tabbar-actions">
      <a-dropdown
        placement="bottomRight"
        :get-popup-container="getPopupContainer"
        @open-change="onOverflowDropdownOpenChange"
      >
        <a-button type="text" size="small" class="action-btn">
          <MoreOutlined />
        </a-button>
        <template #overlay>
          <div class="tabbar-more-panel">
            <div v-if="clippedTabsList.length > 0" class="tabbar-more-panel__search">
              <a-input
                v-model:value="overflowTabSearch"
                allow-clear
                size="small"
                :placeholder="t('layout.tab.filterOverflowTabs')"
              >
                <template #prefix>
                  <SearchOutlined class="tabbar-more-panel__search-icon" />
                </template>
              </a-input>
            </div>
            <a-menu class="tabbar-more-menu" @click="onQuickAction">
              <a-menu-item-group
                v-if="overflowTabsFiltered.length > 0"
                :title="t('layout.tab.overflowTabs')"
              >
                <a-menu-item
                  v-for="tab in overflowTabsFiltered"
                  :key="'overflow-tab:' + tab.key"
                  class="tabbar-overflow-menu-item"
                >
                  <FxIcon
                    v-if="tab.icon"
                    :name="tab.icon"
                    :fallback="false"
                    class="tab-icon"
                    :size="14"
                  />
                  <span class="tabbar-overflow-menu-item__title">{{ tab.title }}</span>
                </a-menu-item>
              </a-menu-item-group>
              <a-menu-divider v-if="overflowTabsFiltered.length > 0" />
              <a-menu-item key="closeOthers">
                <CloseCircleOutlined />
                <span>{{ t('layout.tab.closeOthers') }}</span>
              </a-menu-item>
              <a-menu-item key="closeAll">
                <CloseSquareOutlined />
                <span>{{ t('layout.tab.closeAll') }}</span>
              </a-menu-item>
            </a-menu>
          </div>
        </template>
      </a-dropdown>
    </div>

    <!-- 右键菜单 -->
    <a-dropdown
      v-model:open="contextMenuVisible"
      :trigger="[]"
      :get-popup-container="getPopupContainer"
    >
      <div
        :style="{
          position: 'fixed',
          left: contextMenuPosition.x + 'px',
          top: contextMenuPosition.y + 'px',
          width: '1px',
          height: '1px'
        }"
      />
      <template #overlay>
        <a-menu @click="onContextMenuClick">
          <a-menu-item key="refresh">
            <SyncOutlined />
            <span>{{ t('layout.tab.refresh') }}</span>
          </a-menu-item>
          <a-menu-item key="close" :disabled="!contextTab?.closable">
            <CloseOutlined />
            <span>{{ t('layout.tab.close') }}</span>
          </a-menu-item>
          <a-menu-divider />
          <a-menu-item key="closeOthers">
            <CloseCircleOutlined />
            <span>{{ t('layout.tab.closeOthers') }}</span>
          </a-menu-item>
          <a-menu-item key="closeLeft">
            <VerticalLeftOutlined />
            <span>{{ t('layout.tab.closeLeft') }}</span>
          </a-menu-item>
          <a-menu-item key="closeRight">
            <VerticalRightOutlined />
            <span>{{ t('layout.tab.closeRight') }}</span>
          </a-menu-item>
          <a-menu-divider />
          <a-menu-item key="closeAll">
            <CloseSquareOutlined />
            <span>{{ t('layout.tab.closeAll') }}</span>
          </a-menu-item>
        </a-menu>
      </template>
    </a-dropdown>
  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, watch, nextTick } from 'vue'
import { useI18n } from 'vue-i18n'
import FxIcon from '@/components/common/FxIcon.vue'
import {
  CloseOutlined,
  SyncOutlined,
  CloseCircleOutlined,
  VerticalLeftOutlined,
  VerticalRightOutlined,
  CloseSquareOutlined,
  MoreOutlined,
  SearchOutlined
} from '@ant-design/icons-vue'

const { t } = useI18n()

interface Tab {
  key: string
  title: string
  path: string
  icon?: string
  closable: boolean
}

interface AppTabBarProps {
  /** 标签页数组，包含所有打开的标签页信息 */
  tabs: Tab[]
  /** 当前激活的标签页 key，用于高亮显示 */
  activeKey?: string
  /** 是否允许拖拽排序，默认 true */
  draggable?: boolean
  /** 最大标签页数量，默认 10 */
  maxTabs?: number
}

const props = withDefaults(defineProps<AppTabBarProps>(), {
  tabs: () => [],
  activeKey: '',
  draggable: true,
  maxTabs: 10
})

const emit = defineEmits<{
  /**
   * 标签页点击事件
   * 触发时机：用户点击标签页时触发
   * @param tab 被点击的标签页对象
   */
  'tab-click': [tab: Tab]
  /**
   * 标签页关闭事件
   * 触发时机：用户点击标签页关闭按钮时触发
   * @param tab 被关闭的标签页对象
   */
  'tab-close': [tab: Tab]
  /**
   * 标签页拖拽排序事件
   * 触发时机：用户拖拽标签页到其他位置时触发
   * @param fromIndex 原始索引位置
   * @param toIndex 目标索引位置
   */
  'tab-drag': [fromIndex: number, toIndex: number]
  /**
   * 标签页刷新事件
   * 触发时机：用户右键菜单选择刷新时触发
   * @param tab 要刷新的标签页对象
   */
  'tab-refresh': [tab: Tab]
  /**
   * 批量关闭标签页事件
   * 触发时机：用户右键菜单或快速操作选择关闭时触发
   * @param action 关闭动作：others=关闭其他，left=关闭左侧，right=关闭右侧，all=关闭所有
   * @param tab 参考标签页对象（关闭其他/左侧/右侧时使用）
   */
  'tabs-close': [action: 'others' | 'left' | 'right' | 'all', tab?: Tab]
}>()

// 拖拽相关
const draggingKey = ref<string>('')
const dragFromIndex = ref<number>(-1)

// 容器引用（用于稳定挂载下拉层，避免切页/卸载时访问失效的 $el）
const tabbarRootRef = ref<HTMLElement | null>(null)

/** 标签横向滚动容器：用于判定哪些 Tab 完全滚出可视区域 */
const tabbarInnerRef = ref<HTMLElement | null>(null)

/** 标签 DOM 引用（tab.key → 元素），用于几何判定溢出 */
const tabElByKey = new Map<string, HTMLElement>()

/** 完全不在可视区域内的 Tab key 列表（需在「⋯」中列出） */
const clippedTabKeys = ref<string[]>([])

/** 「⋯」面板内筛选溢出标签关键字 */
const overflowTabSearch = ref('')

let clippedTabsMeasureRaf: number | null = null

let tabbarResizeObserver: ResizeObserver | null = null

/**
 * 绑定单个 Tab 项的根元素引用（Vue ref 回调）
 *
 * @param key 标签 key
 * @param el DOM 或卸载时的 null
 */
function bindTabBarTabEl(key: string, el: unknown) {
  if (!el || typeof el === 'boolean') {
    tabElByKey.delete(key)
    scheduleMeasureClippedTabs()
    return
  }
  const node = el as HTMLElement
  if (node && node.nodeType === Node.ELEMENT_NODE) {
    tabElByKey.set(key, node)
    scheduleMeasureClippedTabs()
  }
}

/**
 * 测量当前完全滚出可视区域的 Tab（双通道：横向滚动仍可通过「⋯」直达）
 */
function measureClippedTabs() {
  const root = tabbarInnerRef.value
  if (!root) {
    clippedTabKeys.value = []
    return
  }
  const rootRect = root.getBoundingClientRect()
  const clipped: string[] = []
  for (const tab of props.tabs) {
    const el = tabElByKey.get(tab.key)
    if (!el) {
      continue
    }
    const r = el.getBoundingClientRect()
    const fullyOutside = r.right <= rootRect.left + 1 || r.left >= rootRect.right - 1
    if (fullyOutside) {
      clipped.push(tab.key)
    }
  }
  clippedTabKeys.value = clipped
}

/**
 * rAF 合并测量，避免滚动事件高频触发
 */
function scheduleMeasureClippedTabs() {
  if (clippedTabsMeasureRaf != null) {
    cancelAnimationFrame(clippedTabsMeasureRaf)
  }
  clippedTabsMeasureRaf = requestAnimationFrame(() => {
    clippedTabsMeasureRaf = null
    measureClippedTabs()
  })
}

/** 当前不可见的 Tab 列表（保持路由顺序） */
const clippedTabsList = computed(() =>
  props.tabs.filter(tab => clippedTabKeys.value.includes(tab.key)),
)

/** 「⋯」中展示的溢出 Tab（支持关键字筛选标题 / 路径） */
const overflowTabsFiltered = computed(() => {
  const q = overflowTabSearch.value.trim().toLowerCase()
  const base = clippedTabsList.value
  if (!q) {
    return base
  }
  return base.filter(tab => {
    const title = (tab.title || '').toLowerCase()
    const path = (tab.path || '').toLowerCase()
    return title.includes(q) || path.includes(q)
  })
})

/**
 * 「⋯」下拉开关：关闭时清空筛选；打开时复查溢出列表
 *
 * @param open 是否展开
 */
function onOverflowDropdownOpenChange(open: boolean) {
  if (!open) {
    overflowTabSearch.value = ''
  } else {
    scheduleMeasureClippedTabs()
  }
}

function bindTabbarResizeObserver() {
  tabbarResizeObserver?.disconnect()
  tabbarResizeObserver = null
  const root = tabbarInnerRef.value
  if (!root || typeof ResizeObserver === 'undefined') {
    return
  }
  tabbarResizeObserver = new ResizeObserver(() => scheduleMeasureClippedTabs())
  tabbarResizeObserver.observe(root)
}

// 右键菜单相关
const contextMenuVisible = ref(false)
const contextMenuPosition = ref({ x: 0, y: 0 })
const contextTab = ref<Tab | null>(null)

const closeContextMenu = () => {
  contextMenuVisible.value = false
  contextTab.value = null
}

function getPopupContainer() {
  if (typeof document === 'undefined') {
    return tabbarRootRef.value as any
  }
  return tabbarRootRef.value || document.body
}

// 标签点击
const onTabClick = (tab: Tab) => {
  emit('tab-click', tab)
}

// 标签关闭
const onTabClose = (tab: Tab) => {
  closeContextMenu()
  emit('tab-close', tab)
}

// 拖拽开始
const onDragStart = (tab: Tab, event: DragEvent) => {
  if (!props.draggable || !tab.closable) {
    event.preventDefault()
    return
  }
  
  draggingKey.value = tab.key
  dragFromIndex.value = props.tabs.findIndex(t => t.key === tab.key)
  
  if (event.dataTransfer) {
    event.dataTransfer.effectAllowed = 'move'
  }
}

// 拖拽经过
const onDragOver = (event: DragEvent) => {
  if (!props.draggable) return
  event.preventDefault()
  if (event.dataTransfer) {
    event.dataTransfer.dropEffect = 'move'
  }
}

// 拖拽放下
const onDrop = (tab: Tab, event: DragEvent) => {
  if (!props.draggable) return
  event.preventDefault()
  
  const toIndex = props.tabs.findIndex(t => t.key === tab.key)
  
  if (dragFromIndex.value !== -1 && dragFromIndex.value !== toIndex) {
    emit('tab-drag', dragFromIndex.value, toIndex)
  }
}

// 拖拽结束
const onDragEnd = () => {
  draggingKey.value = ''
  dragFromIndex.value = -1
}

// 右键菜单
const onContextMenu = (tab: Tab, event: MouseEvent) => {
  contextTab.value = tab
  contextMenuPosition.value = {
    x: event.clientX,
    y: event.clientY
  }
  contextMenuVisible.value = true
}

const handleDocumentPointerDown = (event: MouseEvent) => {
  if (!contextMenuVisible.value) {
    return
  }

  const root = tabbarRootRef.value
  const target = event.target as Node | null
  if (root && target && root.contains(target)) {
    return
  }

  closeContextMenu()
}

const handleDocumentKeydown = (event: KeyboardEvent) => {
  if (event.key === 'Escape' && contextMenuVisible.value) {
    closeContextMenu()
  }
}

// 右键菜单点击
const onContextMenuClick = (info: any) => {
  const key = info.key as string
  
  if (!contextTab.value) return
  
  switch (key) {
    case 'refresh':
      emit('tab-refresh', contextTab.value)
      break
    case 'close':
      if (contextTab.value.closable) {
        emit('tab-close', contextTab.value)
      }
      break
    case 'closeOthers':
      emit('tabs-close', 'others', contextTab.value)
      break
    case 'closeLeft':
      emit('tabs-close', 'left', contextTab.value)
      break
    case 'closeRight':
      emit('tabs-close', 'right', contextTab.value)
      break
    case 'closeAll':
      emit('tabs-close', 'all')
      break
  }
  
  closeContextMenu()
}

// 快速操作
const onQuickAction = (info: any) => {
  const key = info.key as string

  if (key.startsWith('overflow-tab:')) {
    const tabKey = key.slice('overflow-tab:'.length)
    const tab = props.tabs.find(item => item.key === tabKey)
    if (tab) {
      emit('tab-click', tab)
    }
    return
  }

  switch (key) {
    case 'closeOthers':
      // 找到当前激活的标签
      const activeTab = props.tabs.find(t => t.key === props.activeKey)
      if (activeTab) {
        emit('tabs-close', 'others', activeTab)
      }
      break
    case 'closeAll':
      emit('tabs-close', 'all')
      break
  }
}

watch(
  () => props.tabs,
  tabs => {
    const tabKeys = tabs.map(tab => tab.key)
    if (contextTab.value && !tabKeys.includes(contextTab.value.key)) {
      closeContextMenu()
    }
    nextTick(() => {
      scheduleMeasureClippedTabs()
    })
  },
  { deep: true },
)

watch(
  () => props.activeKey,
  () => {
    nextTick(() => scheduleMeasureClippedTabs())
  },
)

onMounted(() => {
  document.addEventListener('mousedown', handleDocumentPointerDown)
  document.addEventListener('keydown', handleDocumentKeydown)

  nextTick(() => {
    bindTabbarResizeObserver()
    scheduleMeasureClippedTabs()
  })

  window.addEventListener('resize', scheduleMeasureClippedTabs)
})

onBeforeUnmount(() => {
  document.removeEventListener('mousedown', handleDocumentPointerDown)
  document.removeEventListener('keydown', handleDocumentKeydown)
  window.removeEventListener('resize', scheduleMeasureClippedTabs)

  tabbarResizeObserver?.disconnect()
  tabbarResizeObserver = null

  if (clippedTabsMeasureRaf != null) {
    cancelAnimationFrame(clippedTabsMeasureRaf)
    clippedTabsMeasureRaf = null
  }

  tabElByKey.clear()
})
</script>

<style scoped lang="less" src="@/styles/layout/components/app-tab-bar.less"></style>
