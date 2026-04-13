<template>
  <a-tag v-if="tagInfo" :color="tagInfo.color">
    {{ tagInfo.label }}
  </a-tag>
  <span v-else>{{ 降级方案Text }}</span>
</template>

<script setup lang="ts">
import { computed } from 'vue'

interface Props {
  /** 璁板綍瀵硅薄锛屽寘鍚瓧鍏稿瓧娈电殑鍘熷鍊煎拰缈昏瘧鍚庣殑 JSON 鍊?*/
  record?: Record<string, any>
  /** 瀛楀吀瀛楁鍚嶏紝鐢ㄤ簬浠?record 涓幏鍙栧搴旂殑瀛楀吀鍊?*/
  dictField?: string
  /** 闄嶇骇鏂囨湰锛屽綋鏃犳硶瑙ｆ瀽瀛楀吀 JSON 鏃舵樉绀虹殑鍘熷鍊?*/
  降级方案Text?: string
}

const props = withDefaults(defineProps<Props>(), {
  record: () => ({}),
  dictField: '',
  降级方案Text: ''
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
