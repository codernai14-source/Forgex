<template>
  <a-sub-menu v-if="hasChildren" :key="item.key" @titleClick="onTitleClick">
    <template #icon>
      <FxIcon v-if="item.icon" :name="item.icon" />
      <component v-else :is="menuIcon" />
    </template>
    <template #title>
      <span class="menu-title-shell">
        <span class="menu-text" :title="item.title">{{ item.title }}</span>
      </span>
      <button
        v-if="showFavoriteAction"
        type="button"
        class="menu-favorite"
        :class="{ 'menu-favorite--active': isFavorite }"
        :title="isFavorite ? '取消收藏本页' : '收藏本页'"
        @click.stop.prevent="onFavoriteClick"
      >
        <StarFilled v-if="isFavorite" />
        <StarOutlined v-else />
      </button>
    </template>
    <SidebarMenuNode
      v-for="child in childItems"
      :key="child.key"
      :item="child"
      :active-path="activePath"
      :favorite-paths="favoritePaths"
      :favorite-loading-path="favoriteLoadingPath"
      @submenu-title-click="emit('submenu-title-click', $event)"
      @favorite-toggle="emit('favorite-toggle', $event)"
    />
  </a-sub-menu>
  <a-menu-item v-else :key="item.key">
    <template #icon>
      <FxIcon v-if="item.icon" :name="item.icon" />
      <component v-else :is="menuIcon" />
    </template>
    <span class="menu-title-shell">
      <span class="menu-text" :title="item.title">{{ item.title }}</span>
    </span>
    <button
      v-if="showFavoriteAction"
      type="button"
      class="menu-favorite"
      :class="{ 'menu-favorite--active': isFavorite }"
      :title="isFavorite ? '取消收藏本页' : '收藏本页'"
      @click.stop.prevent="onFavoriteClick"
    >
      <StarFilled v-if="isFavorite" />
      <StarOutlined v-else />
    </button>
  </a-menu-item>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { FolderOutlined, FileOutlined, StarFilled, StarOutlined } from '@ant-design/icons-vue'
import FxIcon from '@/components/common/FxIcon.vue'

export interface SidebarMenuNodeItem {
  key: string
  title: string
  icon?: string
  path: string
  moduleCode: string
  moduleName?: string
  parentKey?: string
  menuLevel?: number
  children?: SidebarMenuNodeItem[]
  type: 'module' | 'catalog' | 'menu' | 'button'
}

const props = defineProps<{
  item: SidebarMenuNodeItem
  activePath?: string
  favoritePaths?: string[]
  favoriteLoadingPath?: string
}>()

const emit = defineEmits<{
  'submenu-title-click': [item: SidebarMenuNodeItem]
  'favorite-toggle': [path: string]
}>()

const hasChildren = computed(() => Array.isArray(props.item.children) && props.item.children.length > 0)
const childItems = computed(() => props.item.children || [])
const normalizedActivePath = computed(() => normalizePath(props.activePath))
const normalizedFavoritePaths = computed(() => new Set((props.favoritePaths || []).map(path => normalizePath(path)).filter(Boolean)))
const favoritePath = computed(() => resolveFavoritePath(props.item))
const isFavorite = computed(() => !!favoritePath.value && normalizedFavoritePaths.value.has(favoritePath.value))
const showFavoriteAction = computed(() => {
  const path = favoritePath.value
  return !!path && path !== props.favoriteLoadingPath
})

const menuIcon = computed(() => {
  return hasChildren.value ? FolderOutlined : FileOutlined
})

function normalizePath(path?: string | null) {
  return String(path || '').split('?')[0].split('#')[0]
}

function resolveFavoritePath(item: SidebarMenuNodeItem): string {
  const path = normalizePath(item.path)
  if (!path) {
    return ''
  }
  if (normalizedActivePath.value && normalizedActivePath.value === path) {
    return path
  }
  if (hasChildren.value && containsPath(childItems.value, normalizedActivePath.value)) {
    return normalizedActivePath.value
  }
  return hasChildren.value ? '' : path
}

function containsPath(nodes: SidebarMenuNodeItem[], path: string): boolean {
  if (!path) {
    return false
  }
  for (const node of nodes) {
    const normalized = normalizePath(node.path)
    if (normalized && normalized === path) {
      return true
    }
    if (node.children?.length && containsPath(node.children, path)) {
      return true
    }
  }
  return false
}

function onTitleClick() {
  emit('submenu-title-click', props.item)
}

function onFavoriteClick() {
  if (favoritePath.value) {
    emit('favorite-toggle', favoritePath.value)
  }
}
</script>
