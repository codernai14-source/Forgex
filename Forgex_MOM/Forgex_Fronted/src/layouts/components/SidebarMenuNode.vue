<template>
  <a-sub-menu v-if="hasChildren" :key="item.key">
    <template #icon>
      <FxIcon v-if="item.icon" :name="item.icon" />
      <component v-else :is="menuIcon" />
    </template>
    <template #title>
      <span class="menu-text" :title="item.title">{{ item.title }}</span>
    </template>
    <SidebarMenuNode
      v-for="child in childItems"
      :key="child.key"
      :item="child"
    />
  </a-sub-menu>
  <a-menu-item v-else :key="item.key">
    <template #icon>
      <FxIcon v-if="item.icon" :name="item.icon" />
      <component v-else :is="menuIcon" />
    </template>
    <span class="menu-text" :title="item.title">{{ item.title }}</span>
  </a-menu-item>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { FolderOutlined, FileOutlined } from '@ant-design/icons-vue'
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
}>()

const hasChildren = computed(() => Array.isArray(props.item.children) && props.item.children.length > 0)
const childItems = computed(() => props.item.children || [])

const menuIcon = computed(() => {
  return hasChildren.value ? FolderOutlined : FileOutlined
})
</script>
