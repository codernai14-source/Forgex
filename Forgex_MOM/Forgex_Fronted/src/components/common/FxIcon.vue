<template>
  <span class="fx-icon" :style="iconStyle">
    <Icon v-if="isResolvedIconify" :icon="normalizedName" />
    <component v-else-if="resolvedIcon" :is="resolvedIcon" />
    <component v-else-if="fallback" :is="fallbackIcon" />
  </span>
</template>

<script setup lang="ts">
import { computed, type Component } from 'vue'
import { Icon } from '@iconify/vue'
import { AppstoreOutlined } from '@ant-design/icons-vue'
import { getIcon, isIconifyName } from '@/utils/icon'

interface Props {
  name?: string
  size?: number | string
  color?: string
  fallback?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  name: '',
  size: 16,
  color: '',
  fallback: true,
})

const fallbackIcon: Component = AppstoreOutlined
const normalizedName = computed(() => props.name?.trim?.() || '')

const resolvedIcon = computed<Component | null>(() => {
  if (!normalizedName.value || isIconifyName(normalizedName.value)) {
    return null
  }
  return getIcon(normalizedName.value)
})

const isResolvedIconify = computed(() => isIconifyName(normalizedName.value))
const shouldRender = computed(() => props.fallback || Boolean(normalizedName.value))

const iconStyle = computed(() => {
  const size = typeof props.size === 'number' ? `${props.size}px` : props.size
  return {
    fontSize: size,
    width: size,
    height: size,
    lineHeight: size,
    color: props.color || undefined,
    display: shouldRender.value ? 'inline-flex' : 'none',
  }
})
</script>

<style scoped lang="less">
.fx-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.fx-icon :deep(svg) {
  display: block;
  width: 1em;
  height: 1em;
}
</style>
