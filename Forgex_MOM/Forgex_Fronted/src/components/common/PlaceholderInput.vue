<template>
  <div class="placeholder-input">
    <a-textarea
      v-model:value="localValue"
      :placeholder="placeholder"
      :rows="rows"
      @change="handleChange"
    />
    <div class="placeholder-toolbar">
      <a-space wrap>
        <span class="toolbar-label">常用占位符：</span>
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
    <div class="placeholder-preview" v-if="showPreview && previewText">
      <div class="preview-label">预览效果：</div>
      <div class="preview-content">{{ previewText }}</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, computed } from 'vue'
import { PlusOutlined } from '@ant-design/icons-vue'

interface Placeholder {
  key: string
  label: string
  example: string
}

interface Props {
  /** v-model 绑定的值，包含占位符的文本内容 */
  modelValue?: string
  /** 输入框占位提示文本 */
  placeholder?: string
  /** 输入框行数，默认 4 行 */
  rows?: number
  /** 是否显示预览区域，默认 true */
  showPreview?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: '',
  placeholder: '请输入内容',
  rows: 4,
  showPreview: true
})

const emit = defineEmits<{
  /**
   * 值更新事件
   * 触发时机：用户输入内容或点击占位符标签时触发
   * @param value 新的文本内容
   */
  'update:modelValue': [value: string]
}>()

// 常用占位符列表
const placeholders: Placeholder[] = [
  { key: '${userName}', label: '用户名', example: '张三' },
  { key: '${userAccount}', label: '用户账号', example: 'zhangsan' },
  { key: '${tenantName}', label: '租户名称', example: '示例企业' },
  { key: '${currentTime}', label: '当前时间', example: '2026-01-27 10:30:00' },
  { key: '${title}', label: '标题', example: '系统通知' },
  { key: '${content}', label: '内容', example: '这是一条测试消息' },
  { key: '${linkUrl}', label: '链接地址', example: 'https://example.com' }
]

const localValue = ref(props.modelValue)

// 预览文本（将占位符替换为示例值）
const previewText = computed(() => {
  let text = localValue.value
  placeholders.forEach(ph => {
    text = text.replace(new RegExp('\\' + ph.key.replace(/[.*+?^${}()|[\]\\]/g, '\\$&'), 'g'), ph.example)
  })
  return text
})

// 插入占位符
const insertPlaceholder = (placeholder: string) => {
  const textarea = document.querySelector('.placeholder-input textarea') as HTMLTextAreaElement
  if (textarea) {
    const start = textarea.selectionStart
    const end = textarea.selectionEnd
    const text = localValue.value
    localValue.value = text.substring(0, start) + placeholder + text.substring(end)
    
    // 设置光标位置
    setTimeout(() => {
      textarea.focus()
      textarea.setSelectionRange(start + placeholder.length, start + placeholder.length)
    }, 0)
    
    handleChange()
  }
}

// 处理变化
const handleChange = () => {
  emit('update:modelValue', localValue.value)
}

// 监听外部值变化
watch(() => props.modelValue, (newVal) => {
  localValue.value = newVal
})
</script>

<style scoped lang="less">
.placeholder-input {
  .placeholder-toolbar {
    margin-top: 8px;
    padding: 8px;
    background: #f5f5f5;
    border-radius: 4px;
    
    .toolbar-label {
      color: #666;
      font-size: 12px;
    }
    
    .placeholder-tag {
      cursor: pointer;
      user-select: none;
      transition: all 0.3s;
      
      &:hover {
        transform: scale(1.05);
        box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
      }
    }
  }
  
  .placeholder-preview {
    margin-top: 12px;
    padding: 12px;
    background: #fafafa;
    border: 1px solid #e8e8e8;
    border-radius: 4px;
    
    .preview-label {
      font-size: 12px;
      color: #666;
      margin-bottom: 8px;
      font-weight: 500;
    }
    
    .preview-content {
      color: #333;
      line-height: 1.6;
      white-space: pre-wrap;
      word-break: break-word;
    }
  }
}
</style>

