<template>
  <div class="fx-icon-picker">
    <a-input-group compact>
      <a-input
        v-model:value="innerValue"
        :placeholder="placeholder"
        :maxlength="maxlength"
        style="width: calc(100% - 40px)"
        allow-clear
        @change="onInputChange"
      />
      <a-button type="default" @click="open = true">
        <template #icon>
          <component :is="previewIcon" v-if="previewIcon" />
          <AppstoreOutlined v-else />
        </template>
      </a-button>
    </a-input-group>

    <a-modal
      v-model:open="open"
      :title="title"
      width="720px"
      :footer="null"
      destroy-on-close
      @ok="open = false"
    >
      <a-input
        v-model:value="keyword"
        allow-clear
        :placeholder="searchPlaceholder"
        style="margin-bottom: 12px"
      />
      <div class="fx-icon-picker-grid">
        <div
          v-for="name in filteredNames"
          :key="name"
          class="fx-icon-picker-item"
          :class="{ active: name === innerValue }"
          @click="select(name)"
        >
          <component :is="iconMap[name]" />
          <span class="lbl">{{ name }}</span>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
/**
 * 公共图标选择器（Ant Design Vue 图标名）
 * <p>用于模块、菜单等字段存储图标组件名字符串（如 UserOutlined），与布局侧动态解析一致。</p>
 *
 * @author Forgex Team
 * @version 1.0.0
 */
import { computed, ref, watch, type Component } from 'vue'
import * as Icons from '@ant-design/icons-vue'
import { AppstoreOutlined } from '@ant-design/icons-vue'

const props = withDefaults(
  defineProps<{
    /** 当前选中的图标名，与 @ant-design/icons-vue 导出的图标名称一致 */
    value?: string
    /** 输入框占位提示文本 */
    placeholder?: string
    /** 弹窗标题，默认为"选择图标" */
    title?: string
    /** 搜索框占位提示文本 */
    searchPlaceholder?: string
    /** 输入框最大长度，默认为 100 */
    maxlength?: number
  }>(),
  {
    value: '',
    placeholder: '',
    title: '选择图标',
    searchPlaceholder: '',
    maxlength: 100
  }
)

const emit = defineEmits<{
  /**
   * 图标值更新事件
   * 触发时机：用户选择图标或手动输入图标名时触发
   * @param v 新的图标名，如果为空则返回 undefined
   */
  (e: 'update:value', v: string | undefined): void
}>()

const open = ref(false)
const keyword = ref('')
const innerValue = ref(props.value || '')

watch(
  () => props.value,
  v => {
    innerValue.value = v || ''
  }
)

/** 排除非图标导出 */
const iconNames = computed(() => {
  return Object.keys(Icons).filter(
    k =>
      k.endsWith('Outlined') ||
      k.endsWith('Filled') ||
      k.endsWith('TwoTone')
  )
})

const iconMap = computed(() => {
  const m: Record<string, Component> = {}
  for (const name of iconNames.value) {
    const c = (Icons as any)[name]
    if (typeof c === 'object' || typeof c === 'function') {
      m[name] = c as Component
    }
  }
  return m
})

const filteredNames = computed(() => {
  const q = keyword.value.trim().toLowerCase()
  if (!q) {
    return iconNames.value
  }
  return iconNames.value.filter(n => n.toLowerCase().includes(q))
})

const previewIcon = computed(() => {
  const n = innerValue.value
  if (!n) {
    return undefined
  }
  return (Icons as any)[n] as Component | undefined
})

function onInputChange() {
  emit('update:value', innerValue.value || undefined)
}

function select(name: string) {
  innerValue.value = name
  emit('update:value', name)
  open.value = false
}
</script>

<style scoped lang="less">
.fx-icon-picker-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 8px;
  max-height: 420px;
  overflow: auto;
}

.fx-icon-picker-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 8px 4px;
  border: 1px solid #f0f0f0;
  border-radius: 6px;
  cursor: pointer;
  font-size: 20px;
  transition: background 0.2s;
}

.fx-icon-picker-item:hover {
  background: #f5f5f5;
}

.fx-icon-picker-item.active {
  border-color: #fa8c16;
  background: #fff7e6;
}

.fx-icon-picker-item .lbl {
  font-size: 11px;
  color: #666;
  text-align: center;
  word-break: break-all;
  line-height: 1.2;
}
</style>
