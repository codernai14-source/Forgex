<template>
  <div class="placeholder-input">
    <a-textarea
      v-model:value="localValue"
      :placeholder="resolvedPlaceholder"
      :rows="rows"
      @update:value="handleChange"
    />
    <div class="placeholder-toolbar">
      <a-space wrap>
        <span class="toolbar-label">{{ t('common.placeholderInput.toolbarLabel') }}</span>
        <a-tag
          v-for="ph in placeholders"
          :key="ph.key"
          color="blue"
          class="placeholder-tag"
          @click="insertPlaceholder(ph.key)"
        >
          <template #icon><PlusOutlined /></template>
          {{ ph.label }}
        </a-tag>
      </a-space>
    </div>
    <div v-if="showPreview && previewText" class="placeholder-preview">
      <div class="preview-label">{{ t('common.placeholderInput.previewLabel') }}</div>
      <div class="preview-content">{{ previewText }}</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { PlusOutlined } from '@ant-design/icons-vue'
import { useI18n } from 'vue-i18n'

interface Placeholder {
  key: string
  label: string
  example: string
}

interface Props {
  /** v-model value containing placeholders. */
  modelValue?: string
  /** Input placeholder text. */
  placeholder?: string
  /** Textarea rows. */
  rows?: number
  /** Whether to show rendered preview. */
  showPreview?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: '',
  placeholder: '',
  rows: 4,
  showPreview: true,
})

const emit = defineEmits<{
  'update:modelValue': [value: string]
}>()

const { t } = useI18n()
const localValue = ref(props.modelValue)
const resolvedPlaceholder = computed(() => props.placeholder || t('common.pleaseInput'))

const placeholders = computed<Placeholder[]>(() => [
  { key: '${userName}', label: t('common.placeholderInput.userName'), example: 'Alice' },
  { key: '${userAccount}', label: t('common.placeholderInput.userAccount'), example: 'alice01' },
  { key: '${tenantName}', label: t('common.placeholderInput.tenantName'), example: 'Example Co.' },
  { key: '${currentTime}', label: t('common.placeholderInput.currentTime'), example: '2026-01-27 10:30:00' },
  { key: '${title}', label: t('common.placeholderInput.title'), example: t('common.placeholderInput.exampleTitle') },
  { key: '${content}', label: t('common.placeholderInput.content'), example: t('common.placeholderInput.exampleContent') },
  { key: '${linkUrl}', label: t('common.placeholderInput.linkUrl'), example: 'https://example.com' },
])

const previewText = computed(() => {
  let text = localValue.value
  placeholders.value.forEach((ph) => {
    text = text.replace(new RegExp('\\' + ph.key.replace(/[.*+?^${}()|[\]\\]/g, '\\$&'), 'g'), ph.example)
  })
  return text
})

function insertPlaceholder(placeholder: string) {
  const textarea = document.querySelector('.placeholder-input textarea') as HTMLTextAreaElement
  if (!textarea) {
    return
  }

  const start = textarea.selectionStart
  const end = textarea.selectionEnd
  const text = localValue.value
  localValue.value = text.substring(0, start) + placeholder + text.substring(end)

  setTimeout(() => {
    textarea.focus()
    textarea.setSelectionRange(start + placeholder.length, start + placeholder.length)
  }, 0)

  handleChange()
}

function handleChange(value?: string) {
  if (typeof value === 'string') {
    localValue.value = value
  }
  emit('update:modelValue', localValue.value)
}

watch(() => props.modelValue, (newVal) => {
  localValue.value = newVal
})
</script>

<style scoped lang="less" src="@/styles/components/common/placeholder-input.less"></style>
