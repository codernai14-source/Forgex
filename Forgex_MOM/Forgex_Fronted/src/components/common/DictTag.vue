<template>
  <a-tag v-if="displayItem" :color="displayItem.tagStyle?.color">
    <template v-if="displayItem.tagStyle?.icon" #icon>
      <FxIcon :name="displayItem.tagStyle.icon" :fallback="false" />
    </template>
    {{ displayItem.label }}
  </a-tag>
  <span v-else>{{ fallbackText }}</span>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import FxIcon from './FxIcon.vue'
import type { DictItemOption } from '@/hooks/useDict'

interface Props {
  items?: DictItemOption[]
  value?: string | number | null
  fallbackText?: string
}

const props = withDefaults(defineProps<Props>(), {
  items: () => [],
  value: undefined,
  fallbackText: '',
})

const displayItem = computed(() => {
  const normalizedValue = String(props.value ?? '')
  if (!normalizedValue) {
    return null
  }
  return props.items.find(item => String(item?.value ?? '') === normalizedValue) || null
})

</script>
