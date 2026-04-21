<template>
  <div ref="tabbarRootRef" class="app-tabbar fx-guide-tabbar">
    <div class="tabbar-inner fx-guide-tabbar-list">
      <div
        v-for="tab in tabs"
        :key="tab.key"
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
      <a-dropdown placement="bottomRight" :get-popup-container="getPopupContainer">
        <a-button type="text" size="small" class="action-btn">
          <MoreOutlined />
        </a-button>
        <template #overlay>
          <a-menu @click="onQuickAction">
            <a-menu-item key="closeOthers">
              <CloseCircleOutlined />
              <span>关闭其他</span>
            </a-menu-item>
            <a-menu-item key="closeAll">
              <CloseSquareOutlined />
              <span>关闭所有</span>
            </a-menu-item>
          </a-menu>
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
            <span>刷新</span>
          </a-menu-item>
          <a-menu-item key="close" :disabled="!contextTab?.closable">
            <CloseOutlined />
            <span>关闭</span>
          </a-menu-item>
          <a-menu-divider />
          <a-menu-item key="closeOthers">
            <CloseCircleOutlined />
            <span>关闭其他</span>
          </a-menu-item>
          <a-menu-item key="closeLeft">
            <VerticalLeftOutlined />
            <span>关闭左侧</span>
          </a-menu-item>
          <a-menu-item key="closeRight">
            <VerticalRightOutlined />
            <span>关闭右侧</span>
          </a-menu-item>
          <a-menu-divider />
          <a-menu-item key="closeAll">
            <CloseSquareOutlined />
            <span>关闭所有</span>
          </a-menu-item>
        </a-menu>
      </template>
    </a-dropdown>
  </div>
</template>

<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref, watch } from 'vue'
import {
  CloseOutlined,
  SyncOutlined,
  CloseCircleOutlined,
  VerticalLeftOutlined,
  VerticalRightOutlined,
  CloseSquareOutlined,
  MoreOutlined
} from '@ant-design/icons-vue'

interface Tab {
  key: string
  title: string
  path: string
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
  () => props.tabs.map(tab => tab.key),
  tabKeys => {
    if (contextTab.value && !tabKeys.includes(contextTab.value.key)) {
      closeContextMenu()
    }
  },
)

onMounted(() => {
  document.addEventListener('mousedown', handleDocumentPointerDown)
  document.addEventListener('keydown', handleDocumentKeydown)
})

onBeforeUnmount(() => {
  document.removeEventListener('mousedown', handleDocumentPointerDown)
  document.removeEventListener('keydown', handleDocumentKeydown)
})
</script>

<style scoped lang="less">
.app-tabbar {
  display: flex;
  align-items: center;
  height: 40px;
  padding: 0 8px;
  background: var(--fx-header-bg, #ffffff);
  border-bottom: 1px solid var(--fx-border-color, #f0f0f0);
  overflow: hidden;
}

.tabbar-inner {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 4px;
  overflow-x: auto;
  overflow-y: hidden;
  
  &::-webkit-scrollbar {
    height: 4px;
  }
  
  &::-webkit-scrollbar-thumb {
    background: rgba(0, 0, 0, 0.2);
    border-radius: 2px;
    
    &:hover {
      background: rgba(0, 0, 0, 0.3);
    }
  }
}

.tabbar-actions {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  margin-left: 8px;
  padding-left: 8px;
  border-left: 1px solid var(--fx-border-color, #f0f0f0);
}

.action-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  color: var(--fx-text-color, #4b5563);
  
  &:hover {
    color: var(--fx-theme-color, #1677ff);
    background: rgba(22, 119, 255, 0.08);
  }
}

.tab-item {
  display: flex;
  align-items: center;
  gap: 6px;
  height: 32px;
  padding: 0 12px;
  font-size: 13px;
  color: var(--fx-text-color, #4b5563);
  background: var(--fx-tab-bg, #f3f4f6);
  border-radius: 6px 6px 0 0;
  cursor: pointer;
  user-select: none;
  white-space: nowrap;
  transition: all 0.2s;
  
  &:hover {
    color: var(--fx-theme-color, #1677ff);
    background: var(--fx-tab-hover-bg, #e5e7eb);
  }
  
  &.tab-item-active {
    color: var(--fx-theme-color, #1677ff);
    background: var(--fx-content-bg, #ffffff);
    border-bottom: 2px solid var(--fx-theme-color, #1677ff);
    font-weight: 500;
  }
  
  &.tab-item-dragging {
    opacity: 0.5;
  }
}

.tab-title {
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.tab-close {
  flex-shrink: 0;
  font-size: 12px;
  opacity: 0.7;
  transition: all 0.2s;
  
  &:hover {
    opacity: 1;
    color: #ef4444;
  }
}

// 响应式适配
@media (max-width: 768px) {
  .app-tabbar {
    padding: 0 4px;
  }
  
  .tab-item {
    padding: 0 8px;
  }
  
  .tab-title {
    max-width: 80px;
  }
}
</style>
