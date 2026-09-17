<template>
  <div class="login-title-style-editor">
    <a-form layout="vertical">
      <a-form-item :label="t('system.config.fontFamily')">
        <a-select v-model:value="draft.fontFamily">
          <a-select-option v-for="item in LOGIN_FONT_PRESETS" :key="item.key" :value="item.value">
            {{ t(`system.config.font${capitalize(item.key)}`) }}
          </a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item :label="t('system.config.fontSize')">
        <a-input-number v-model:value="draft.fontSize" :min="minSize" :max="maxSize" style="width: 100%" />
      </a-form-item>
      <a-form-item :label="t('system.config.fontWeight')">
        <a-radio-group v-model:value="draft.fontWeight" button-style="solid">
          <a-radio-button value="normal">{{ t('system.config.fontWeightNormal') }}</a-radio-button>
          <a-radio-button value="600">{{ t('system.config.fontWeightMedium') }}</a-radio-button>
          <a-radio-button value="700">{{ t('system.config.fontWeightBold') }}</a-radio-button>
        </a-radio-group>
      </a-form-item>
      <a-form-item :label="t('system.config.fontItalic')">
        <a-switch :checked="draft.fontStyle === 'italic'" @change="onItalicChange" />
      </a-form-item>
      <a-form-item :label="t('system.config.letterSpacing')">
        <a-slider v-model:value="draft.letterSpacing" :min="0" :max="12" />
      </a-form-item>
      <template v-if="allowGradient">
        <a-form-item :label="t('system.config.colorMode')">
          <a-radio-group v-model:value="draft.colorMode" button-style="solid">
            <a-radio-button value="solid">{{ t('system.config.colorSolid') }}</a-radio-button>
            <a-radio-button value="gradient">{{ t('system.config.colorGradient') }}</a-radio-button>
          </a-radio-group>
        </a-form-item>
        <a-form-item v-if="draft.colorMode !== 'gradient'" :label="t('system.config.fontColor')">
          <a-color-picker :value="draft.color" format="hex" @change="(value: unknown) => draft.color = toHexColor(value, draft.color)" />
        </a-form-item>
        <template v-else>
          <a-form-item :label="t('system.config.gradientFrom')">
            <a-color-picker :value="draft.gradientFrom" format="hex" @change="(value: unknown) => draft.gradientFrom = toHexColor(value, draft.gradientFrom)" />
          </a-form-item>
          <a-form-item :label="t('system.config.gradientTo')">
            <a-color-picker :value="draft.gradientTo" format="hex" @change="(value: unknown) => draft.gradientTo = toHexColor(value, draft.gradientTo)" />
          </a-form-item>
          <a-form-item :label="t('system.config.gradientAngle')">
            <a-slider v-model:value="draft.gradientAngle" :min="0" :max="180" />
          </a-form-item>
        </template>
      </template>
      <a-form-item v-else :label="t('system.config.fontColor')">
        <a-color-picker :value="draft.color" format="hex" @change="(value: unknown) => draft.color = toHexColor(value, draft.color)" />
      </a-form-item>
    </a-form>
    <div class="login-title-style-editor__preview-label">{{ t('system.config.stylePreview') }}</div>
    <div class="login-title-style-editor__preview" :style="previewCss">{{ previewText }}</div>
  </div>
</template>

<script setup lang="ts">
/**
 * 登录页标题 / 副标题可视化样式编辑器。
 * <p>
 * 只提供字体下拉、字号、颜色选择器和有限的排版开关，不暴露 CSS 输入框。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @see buildLoginTitleCss
 * @see buildLoginSubtitleCss
 */
import { computed, reactive, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import type { LoginSubtitleStyle, LoginTitleStyle } from '@/api/system/config'
import {
  LOGIN_FONT_PRESETS,
  buildLoginSubtitleCss,
  buildLoginTitleCss,
  resolveFontPreset,
  toHexColor,
} from '@/utils/loginTitleStyle'

const props = withDefaults(defineProps<{
  modelValue: LoginTitleStyle | LoginSubtitleStyle
  allowGradient?: boolean
  previewText: string
  minSize?: number
  maxSize?: number
}>(), {
  allowGradient: false,
  minSize: 12,
  maxSize: 96,
})

const emit = defineEmits<{
  'update:modelValue': [value: LoginTitleStyle | LoginSubtitleStyle]
}>()

const { t } = useI18n({ useScope: 'global' })

const draft = reactive({
  fontFamily: resolveFontPreset((props.modelValue as LoginTitleStyle).fontFamily),
  fontSize: Number(props.modelValue.fontSize || (props.allowGradient ? 28 : 13)),
  fontWeight: String((props.modelValue as LoginTitleStyle).fontWeight || (props.allowGradient ? '600' : 'normal')),
  fontStyle: String((props.modelValue as LoginTitleStyle).fontStyle || 'normal'),
  letterSpacing: Number((props.modelValue as LoginTitleStyle).letterSpacing || 0),
  colorMode: ((props.modelValue as LoginTitleStyle).colorMode || 'solid') as 'solid' | 'gradient',
  color: String((props.modelValue as LoginTitleStyle).color || (props.allowGradient ? '#ffffff' : '#9ca3af')),
  gradientFrom: String((props.modelValue as LoginTitleStyle).gradientFrom || '#05d9e8'),
  gradientTo: String((props.modelValue as LoginTitleStyle).gradientTo || '#ff2a6d'),
  gradientAngle: Number((props.modelValue as LoginTitleStyle).gradientAngle ?? 90),
})

const previewCss = computed(() => {
  if (props.allowGradient) {
    return buildLoginTitleCss(draft)
  }
  return buildLoginSubtitleCss(draft)
})

watch(
  () => props.modelValue,
  (value) => {
    Object.assign(draft, {
      fontFamily: resolveFontPreset(value.fontFamily),
      fontSize: Number(value.fontSize || draft.fontSize),
      fontWeight: String((value as LoginTitleStyle).fontWeight || draft.fontWeight),
      fontStyle: String((value as LoginTitleStyle).fontStyle || draft.fontStyle),
      letterSpacing: Number((value as LoginTitleStyle).letterSpacing || 0),
      colorMode: ((value as LoginTitleStyle).colorMode || 'solid') as 'solid' | 'gradient',
      color: String((value as LoginTitleStyle).color || draft.color),
      gradientFrom: String((value as LoginTitleStyle).gradientFrom || draft.gradientFrom),
      gradientTo: String((value as LoginTitleStyle).gradientTo || draft.gradientTo),
      gradientAngle: Number((value as LoginTitleStyle).gradientAngle ?? draft.gradientAngle),
    })
  },
  { deep: true }
)

watch(
  draft,
  () => {
    emit('update:modelValue', { ...draft })
  },
  { deep: true }
)

/**
 * 切换斜体。
 *
 * @param checked 是否斜体
 */
function onItalicChange(checked: boolean | string | number) {
  draft.fontStyle = checked ? 'italic' : 'normal'
}

/**
 * 把预设 key 转成 locale 后缀。
 *
 * @param key 预设编码
 * @returns 首字母大写
 */
function capitalize(key: string) {
  return key.charAt(0).toUpperCase() + key.slice(1)
}
</script>

<style scoped lang="less">
.login-title-style-editor__preview-label {
  margin: 8px 0 8px;
  color: var(--ant-color-text-secondary);
}

.login-title-style-editor__preview {
  min-height: 72px;
  padding: 16px;
  border-radius: 10px;
  background: #0f172a;
  display: flex;
  align-items: center;
  justify-content: center;
  text-align: center;
}
</style>
