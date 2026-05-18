<template>
  <div class="label-sheet" :style="sheetStyle">
    <div v-for="item in components" :key="item.sortNo + '-' + item.componentType" class="label-component" :style="componentStyle(item)">
      <template v-if="item.componentType === 'HORIZONTAL_LINE' || item.componentType === 'VERTICAL_LINE'"></template>
      <img v-else-if="item.componentType === 'IMAGE' && item.content" :src="item.content" />
      <div v-else class="label-content">{{ displayContent(item) }}</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{ template: any }>()
const scale = 4
const components = computed(() => props.template?.components || [])
const sheetStyle = computed(() => ({
  width: `${(props.template?.paperWidth || 100) * scale}px`,
  height: `${(props.template?.paperHeight || 60) * scale}px`
}))

function componentStyle(item: any) {
  const style = item.styleJson ? JSON.parse(item.styleJson) : {}
  const isLine = item.componentType === 'HORIZONTAL_LINE' || item.componentType === 'VERTICAL_LINE'
  return {
    left: `${(item.positionX || 0) * scale}px`,
    top: `${(item.positionY || 0) * scale}px`,
    width: `${(item.componentWidth || 10) * scale}px`,
    height: `${(item.componentHeight || 6) * scale}px`,
    fontSize: `${style.fontSize || 12}px`,
    fontWeight: style.fontWeight || 400,
    textAlign: style.textAlign || 'left',
    border: isLine ? '0' : '1px dashed transparent',
    background: isLine ? '#111' : 'transparent'
  }
}

function displayContent(item: any) {
  if (item.componentType === 'QRCODE') return item.content || 'QR'
  if (item.componentType === 'BARCODE') return item.content || 'BARCODE'
  return item.content || ''
}
</script>

<style scoped>
.label-sheet {
  position: relative;
  background: #fff;
  border: 1px solid #d9d9d9;
  box-shadow: 0 8px 24px rgba(0,0,0,.08);
}
.label-component {
  position: absolute;
  overflow: hidden;
  display: flex;
  align-items: center;
}
.label-content {
  width: 100%;
  white-space: pre-wrap;
}
img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}
</style>
