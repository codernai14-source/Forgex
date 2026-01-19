<template>
  <div class="app-tabbar">
    <div class="tabbar-inner" ref="tabbarRef">
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
      <a-dropdown placement="bottomRight">
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
      :trigger="['contextmenu']"
      :get-popup-container="() => $el"
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
import { ref } from 'vue'
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
  tabs: Tab[]
  activeKey?: string
  draggable?: boolean
  maxTabs?: number
}

const props = withDefaults(defineProps<AppTabBarProps>(), {
  tabs: () => [],
  activeKey: '',
  draggable: true,
  maxTabs: 10
})

const emit = defineEmits<{
  'tab-click': [tab: Tab]
  'tab-close': [tab: Tab]
  'tab-drag': [fromIndex: number, toIndex: number]
  'tab-refresh': [tab: Tab]
  'tabs-close': [action: 'others' | 'left' | 'right' | 'all', tab?: Tab]
}>()

// 拖拽相关
const draggingKey = ref<string>('')
const dragFromIndex = ref<number>(-1)

// 右键菜单相关
const contextMenuVisible = ref(false)
const contextMenuPosition = ref({ x: 0, y: 0 })
const contextTab = ref<Tab | null>(null)

// 标签点击
const onTabClick = (tab: Tab) => {
  emit('tab-click', tab)
}

// 标签关闭
const onTabClose = (tab: Tab) => {
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
  
  contextMenuVisible.value = false
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
