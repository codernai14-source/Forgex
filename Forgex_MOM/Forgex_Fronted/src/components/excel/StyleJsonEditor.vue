<template>
  <div class="style-json-editor">
    <a-card size="small" :bordered="false">
      <a-form layout="vertical">
        <!-- 背景色选择 -->
        <a-form-item :label="t('common.styleJsonEditor.backgroundColor')">
          <a-color-picker
            v-model:value="styleConfig.backgroundColor"
            format="hex"
            @change="handleStyleChange"
          />
        </a-form-item>
        
        <!-- 字体大小 -->
        <a-form-item :label="t('common.styleJsonEditor.fontSize')">
          <a-input-number
            v-model:value="styleConfig.fontSize"
            :min="10"
            :max="32"
            :step="1"
            style="width: 100%"
            @change="handleStyleChange"
          >
            <template #addonAfter>px</template>
          </a-input-number>
        </a-form-item>
        
        <!-- 是否换行 -->
        <a-form-item :label="t('common.styleJsonEditor.wordWrap')">
          <a-switch
            v-model:checked="styleConfig.wordWrap"
            @change="handleStyleChange"
          />
        </a-form-item>
        
        <!-- 文本对齐方式 -->
        <a-form-item :label="t('common.styleJsonEditor.align')">
          <a-radio-group
            v-model:value="styleConfig.align"
            button-style="solid"
            @change="handleStyleChange"
          >
            <a-radio-button value="left">{{ t('common.styleJsonEditor.alignLeft') }}</a-radio-button>
            <a-radio-button value="center">{{ t('common.styleJsonEditor.alignCenter') }}</a-radio-button>
            <a-radio-button value="right">{{ t('common.styleJsonEditor.alignRight') }}</a-radio-button>
          </a-radio-group>
        </a-form-item>
        
        <!-- 字体加粗 -->
        <a-form-item :label="t('common.styleJsonEditor.fontWeight')">
          <a-switch
            v-model:checked="styleConfig.fontWeight"
            checked-value="bold"
            unchecked-value="normal"
            @change="handleStyleChange"
          />
        </a-form-item>
      </a-form>
      
      <!-- JSON 预览 -->
      <a-divider orientation="left">{{ t('common.styleJsonEditor.jsonPreview') }}</a-divider>
      <pre class="json-preview">{{ jsonPreview }}</pre>
      
      <!-- 样式预览 -->
      <a-divider orientation="left">{{ t('common.styleJsonEditor.stylePreview') }}</a-divider>
      <div class="style-preview" :style="previewStyle">
        {{ t('common.styleJsonEditor.previewText') }}
      </div>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useI18n } from 'vue-i18n'

/**
 * 样式配置编辑器组件
 * 
 * 功能：
 * 1. 支持背景色选择
 * 2. 支持字体大小配置
 * 3. 支持是否换行配置
 * 4. 支持对齐方式配置
 * 5. 支持字体加粗配置
 * 6. 实时预览样式效果
 * 7. 支持 v-model 双向绑定 JSON 字符串
 * 
 * 使用示例：
 * <StyleJsonEditor v-model="form.styleJson" />
 * 
 * @author Forgex
 * @version 1.0.0
 * @since 2026-04-09
 */

interface Props {
  /** v-model 绑定的 JSON 字符串 */
  modelValue?: string
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: ''
})

const emit = defineEmits<{
  /**
   * 样式值更新事件
   * 触发时机：用户修改任意样式配置时触发
   * @param value 新的 JSON 字符串
   */
  'update:modelValue': [value: string]
}>()

const { t } = useI18n()

/**
 * 样式配置接口
 */
interface StyleConfig {
  backgroundColor: string
  fontSize: number
  wordWrap: boolean
  align: 'left' | 'center' | 'right'
  fontWeight: 'normal' | 'bold'
}

// 样式配置
const styleConfig = ref<StyleConfig>({
  backgroundColor: '#ffffff',
  fontSize: 14,
  wordWrap: false,
  align: 'left',
  fontWeight: 'normal'
})

/**
 * 解析初始值
 */
const parseInitialValue = (): Partial<StyleConfig> => {
  try {
    if (props.modelValue) {
      const parsed = JSON.parse(props.modelValue)
      return {
        backgroundColor: parsed.backgroundColor || '#ffffff',
        fontSize: parsed.fontSize || 14,
        wordWrap: parsed.wordWrap ?? false,
        align: parsed.align || 'left',
        fontWeight: parsed.fontWeight || 'normal'
      }
    }
  } catch (error) {
    console.error('解析 JSON 失败:', error)
  }
  return {}
}

/**
 * 初始化样式配置
 */
const initStyleConfig = () => {
  const initialValue = parseInitialValue()
  styleConfig.value = {
    ...styleConfig.value,
    ...initialValue
  }
}

/**
 * 生成 JSON 预览
 */
const jsonPreview = computed(() => {
  return JSON.stringify(styleConfig.value, null, 2)
})

/**
 * 生成预览样式
 */
const previewStyle = computed(() => ({
  backgroundColor: styleConfig.value.backgroundColor,
  fontSize: `${styleConfig.value.fontSize}px`,
  wordWrap: styleConfig.value.wordWrap ? 'break-word' : 'nowrap',
  textAlign: styleConfig.value.align,
  fontWeight: styleConfig.value.fontWeight,
  padding: '12px',
  border: '1px solid #d9d9d9',
  borderRadius: '4px',
  minHeight: '60px'
}))

/**
 * 处理样式变化
 */
const handleStyleChange = () => {
  emit('update:modelValue', jsonPreview.value)
}

/**
 * 监听外部值变化
 */
watch(() => props.modelValue, (newVal) => {
  if (newVal !== undefined) {
    initStyleConfig()
  }
}, { deep: true, immediate: true })

// 初始化
initStyleConfig()
</script>

<style scoped lang="less">
.style-json-editor {
  width: 100%;
  
  .json-preview {
    background-color: #f5f5f5;
    border: 1px solid #d9d9d9;
    border-radius: 4px;
    padding: 12px;
    font-family: 'Courier New', monospace;
    font-size: 13px;
    line-height: 1.6;
    color: #333;
    white-space: pre-wrap;
    word-wrap: break-word;
    min-height: 100px;
    margin-top: 8px;
  }
  
  .style-preview {
    background-color: #fff;
    border: 1px solid #d9d9d9;
    border-radius: 4px;
    min-height: 60px;
    display: flex;
    align-items: center;
    margin-top: 8px;
    color: #333;
  }
  
  :deep(.ant-form-item) {
    margin-bottom: 16px;
  }
  
  :deep(.ant-color-picker) {
    width: 100%;
  }
  
  :deep(.ant-input-number) {
    width: 100%;
  }
}
</style>
