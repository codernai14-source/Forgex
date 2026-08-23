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
          <FxIcon :name="innerValue" />
        </template>
      </a-button>
    </a-input-group>

    <a-modal
      v-model:open="open"
      :title="resolvedTitle"
      width="720px"
      :footer="null"
      destroy-on-close
      @ok="open = false"
    >
      <a-input
        v-model:value="keyword"
        allow-clear
        :placeholder="resolvedSearchPlaceholder"
        style="margin-bottom: 12px"
      />
      <a-space class="fx-icon-picker-controls" wrap>
        <label class="fx-icon-picker-control">
          <span>{{ t('common.iconPicker.color') }}</span>
          <a-input v-model:value="iconColor" type="color" />
        </label>
        <label class="fx-icon-picker-control fx-icon-picker-control--size">
          <span>{{ t('common.iconPicker.size') }}</span>
          <a-input-number v-model:value="iconSize" :min="12" :max="64" />
        </label>
        <span class="fx-icon-picker-preview">
          <FxIcon :name="innerValue" :size="iconSize" :color="iconColor" />
        </span>
      </a-space>
      <div class="fx-icon-picker-grid">
        <div
          v-for="item in filteredIcons"
          :key="item.name"
          class="fx-icon-picker-item"
          :class="{ active: item.name === innerValue }"
          @click="select(item.name)"
        >
          <FxIcon :name="item.name" :size="iconSize" :color="iconColor" />
          <span class="lbl">{{ item.name }}</span>
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
import { computed, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import * as Icons from '@ant-design/icons-vue'
import FxIcon from './FxIcon.vue'
import { ICONIFY_PRESET_NAMES } from '@/utils/icon'

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
    iconColor?: string
    iconSize?: number
  }>(),
  {
    value: '',
    placeholder: '',
    title: '',
    searchPlaceholder: '',
    maxlength: 100,
    iconColor: '#1677ff',
    iconSize: 22,
  }
)

const emit = defineEmits<{
  /**
   * 图标值更新事件
   * 触发时机：用户选择图标或手动输入图标名时触发
   * @param v 新的图标名，如果为空则返回 undefined
   */
  (e: 'update:value', v: string | undefined): void
  (e: 'update:iconColor', v: string): void
  (e: 'update:iconSize', v: number): void
}>()

const open = ref(false)
const keyword = ref('')
const innerValue = ref(props.value || '')
const iconColor = ref(props.iconColor)
const iconSize = ref(props.iconSize)
const { t } = useI18n()
const resolvedTitle = computed(() => props.title || t('common.iconPicker.title'))
const resolvedSearchPlaceholder = computed(() => props.searchPlaceholder || t('common.iconPicker.searchPlaceholder'))

watch(
  () => props.value,
  v => {
    innerValue.value = v || ''
  }
)

watch(() => props.iconColor, value => {
  iconColor.value = value || '#1677ff'
})

watch(() => props.iconSize, value => {
  iconSize.value = value || 22
})

watch(iconColor, value => emit('update:iconColor', value))
watch(iconSize, value => emit('update:iconSize', Number(value || 22)))

/** 排除非图标导出 */
const antIconNames = computed(() => {
  return Object.keys(Icons).filter(
    k =>
      k.endsWith('Outlined') ||
      k.endsWith('Filled') ||
      k.endsWith('TwoTone')
  )
})

const iconItems = computed(() => {
  return [
    ...ICONIFY_PRESET_NAMES.map(name => ({ name, type: 'iconify' })),
    ...antIconNames.value.map(name => ({ name, type: 'ant' })),
  ]
})

const filteredIcons = computed(() => {
  const q = keyword.value.trim().toLowerCase()
  if (!q) {
    return iconItems.value
  }
  return iconItems.value.filter(item => item.name.toLowerCase().includes(q))
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

<style scoped lang="less" src="@/styles/components/common/icon-picker.less"></style>

