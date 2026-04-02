<template>
  <a-tag v-if="tagInfo" :color="tagInfo.color">
    {{ tagInfo.label }}
  </a-tag>
  <span v-else>{{ fallbackText }}</span>
</template>

<script setup lang="ts">
import { computed } from 'vue'

interface Props {
  /** 记录对象，包含字典字段的原始值和翻译后的 JSON 值 */
  record?: Record<string, any>
  /** 字典字段名，用于从 record 中获取对应的字典值 */
  dictField?: string
  /** 降级文本，当无法解析字典 JSON 时显示的原始值 */
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
