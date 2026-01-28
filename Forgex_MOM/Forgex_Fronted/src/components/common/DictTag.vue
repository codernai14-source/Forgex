<template>
  <a-tag v-if="tagInfo" :color="tagInfo.color">
    {{ tagInfo.label }}
  </a-tag>
  <span v-else>{{ fallbackText }}</span>
</template>

<script setup lang="ts">
import { computed } from 'vue'

interface Props {
  record?: Record<string, any>
  dictField?: string
  fallbackText?: string
}

const props = withDefaults(defineProps<Props>(), {
  record: () => ({}),
  dictField: '',
  fallbackText: ''
})

const tagInfo = computed(() => {
  if (!props.record || !props.dictField) {
    return null
  }
  
  const value = props.record[props.dictField]
  
  if (!value) {
    return null
  }
  
  try {
    const parsed = JSON.parse(value)
    if (parsed.color && parsed.color !== 'default') {
      return {
        label: parsed.label || value,
        color: parsed.color
      }
    }
  } catch {
    return null
  }
})
</script>
