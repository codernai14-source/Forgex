<template>
  <div class="tag-style-config">
    <!-- 颜色和图标在同一行 -->
    <div class="tag-style-row">
      <div class="tag-style-field">
        <label class="tag-style-label">{{ t('common.tagStyle.color') }}</label>
        <a-select
          v-model:value="formData.color"
          :placeholder="t('common.tagStyle.selectColor')"
          allowClear
          show-search
          :filter-option="filterOption"
        >
          <a-select-opt-group :label="t('common.tagStyle.statusColors')">
            <a-select-option value="success">
              <a-tag color="success">{{ t('common.success') }}</a-tag>
            </a-select-option>
            <a-select-option value="processing">
              <a-tag color="processing">{{ t('common.processing') }}</a-tag>
            </a-select-option>
            <a-select-option value="error">
              <a-tag color="error">{{ t('common.failed') }}</a-tag>
            </a-select-option>
            <a-select-option value="warning">
              <a-tag color="warning">{{ t('common.warning') }}</a-tag>
            </a-select-option>
            <a-select-option value="default">
              <a-tag color="default">{{ t('common.default') }}</a-tag>
            </a-select-option>
          </a-select-opt-group>
          <a-select-opt-group :label="t('common.tagStyle.presetColors')">
            <a-select-option value="pink">
              <a-tag color="pink">{{ t('common.tagStyle.colors.pink') }}</a-tag>
            </a-select-option>
            <a-select-option value="red">
              <a-tag color="red">{{ t('common.tagStyle.colors.red') }}</a-tag>
            </a-select-option>
            <a-select-option value="orange">
              <a-tag color="orange">{{ t('common.tagStyle.colors.orange') }}</a-tag>
            </a-select-option>
            <a-select-option value="green">
              <a-tag color="green">{{ t('common.tagStyle.colors.green') }}</a-tag>
            </a-select-option>
            <a-select-option value="cyan">
              <a-tag color="cyan">{{ t('common.tagStyle.colors.cyan') }}</a-tag>
            </a-select-option>
            <a-select-option value="blue">
              <a-tag color="blue">{{ t('common.tagStyle.colors.blue') }}</a-tag>
            </a-select-option>
            <a-select-option value="purple">
              <a-tag color="purple">{{ t('common.tagStyle.colors.purple') }}</a-tag>
            </a-select-option>
          </a-select-opt-group>
          <a-select-opt-group :label="t('common.tagStyle.customColors')">
            <a-select-option value="#f50">
              <a-tag color="#f50">#f50</a-tag>
            </a-select-option>
            <a-select-option value="#2db7f5">
              <a-tag color="#2db7f5">#2db7f5</a-tag>
            </a-select-option>
            <a-select-option value="#87d068">
              <a-tag color="#87d068">#87d068</a-tag>
            </a-select-option>
            <a-select-option value="#108ee9">
              <a-tag color="#108ee9">#108ee9</a-tag>
            </a-select-option>
          </a-select-opt-group>
        </a-select>
      </div>

      <div class="tag-style-field">
        <label class="tag-style-label">{{ t('common.tagStyle.icon') }}</label>
        <a-select
          v-model:value="formData.icon"
          :placeholder="t('common.tagStyle.selectIcon')"
          allowClear
          show-search
          :filter-option="filterOption"
        >
          <a-select-option value="CheckCircleOutlined">
            <template #label>
              <span><CheckCircleOutlined /> {{ t('common.tagStyle.icons.success') }}</span>
            </template>
            {{ t('common.tagStyle.icons.success') }}
          </a-select-option>
          <a-select-option value="CloseCircleOutlined">
            <template #label>
              <span><CloseCircleOutlined /> {{ t('common.tagStyle.icons.failed') }}</span>
            </template>
            {{ t('common.tagStyle.icons.failed') }}
          </a-select-option>
          <a-select-option value="ExclamationCircleOutlined">
            <template #label>
              <span><ExclamationCircleOutlined /> {{ t('common.tagStyle.icons.warning') }}</span>
            </template>
            {{ t('common.tagStyle.icons.warning') }}
          </a-select-option>
          <a-select-option value="SyncOutlined">
            <template #label>
              <span><SyncOutlined /> {{ t('common.tagStyle.icons.loading') }}</span>
            </template>
            {{ t('common.tagStyle.icons.loading') }}
          </a-select-option>
          <a-select-option value="ClockCircleOutlined">
            <template #label>
              <span><ClockCircleOutlined /> {{ t('common.tagStyle.icons.pending') }}</span>
            </template>
            {{ t('common.tagStyle.icons.pending') }}
          </a-select-option>
          <a-select-option value="InfoCircleOutlined">
            <template #label>
              <span><InfoCircleOutlined /> {{ t('common.tagStyle.icons.info') }}</span>
            </template>
            {{ t('common.tagStyle.icons.info') }}
          </a-select-option>
          <a-select-option value="CheckOutlined">
            <template #label>
              <span><CheckOutlined /> {{ t('common.tagStyle.icons.check') }}</span>
            </template>
            {{ t('common.tagStyle.icons.check') }}
          </a-select-option>
          <a-select-option value="CloseOutlined">
            <template #label>
              <span><CloseOutlined /> {{ t('common.tagStyle.icons.close') }}</span>
            </template>
            {{ t('common.tagStyle.icons.close') }}
          </a-select-option>
        </a-select>
      </div>
    </div>

    <!-- 预览区域 -->
    <div class="tag-style-preview">
      <label class="tag-style-label">{{ t('common.preview') }}</label>
      <div class="tag-style-preview-content">
        <a-tag v-if="formData.color" :color="formData.color">
          <template v-if="currentIconComponent" #icon>
            <component :is="currentIconComponent" />
          </template>
          {{ t('common.tagStyle.exampleTag') }}
        </a-tag>
        <span v-else class="tag-style-preview-empty">{{ t('common.tagStyle.emptyPreview') }}</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { getIcon } from '@/utils/icon'
import {
  CheckCircleOutlined,
  CheckOutlined,
  ClockCircleOutlined,
  CloseCircleOutlined,
  CloseOutlined,
  ExclamationCircleOutlined,
  InfoCircleOutlined,
  SyncOutlined,
} from '@ant-design/icons-vue'

interface TagStyleForm {
  color?: string
  icon?: string
}

const formData = reactive<TagStyleForm>({
  color: undefined,
  icon: undefined,
})

const { t } = useI18n()

/**
 * 当前选中的图标组件
 */
const currentIconComponent = computed(() => {
  return getIcon(formData.icon)
})

/**
 * 获取标签样式 JSON 字符串
 * @returns 标签样式 JSON 字符串
 */
const getTagStyleJson = (): string => {
  const data: TagStyleForm = {}
  if (formData.color) {
    data.color = formData.color
  }
  if (formData.icon) {
    data.icon = formData.icon
  }
  return Object.keys(data).length > 0 ? JSON.stringify(data) : ''
}

/**
 * 设置标签样式
 * @param json 标签样式 JSON 字符串
 */
const setTagStyleJson = (json: string) => {
  if (!json || json.trim() === '') {
    formData.color = undefined
    formData.icon = undefined
    return
  }
  try {
    const data = JSON.parse(json)
    // 直接赋值触发响应式更新
    formData.color = data.color || undefined
    formData.icon = data.icon || undefined
  } catch (e) {
    console.error('[TagStyleConfig] 解析标签样式失败:', e, json)
  }
}

const filterOption = (input: string, option: any) => {
  const text = option.children?.[0]?.children || option.label || ''
  return String(text).toLowerCase().includes(input.toLowerCase())
}

defineExpose({
  getTagStyleJson,
  setTagStyleJson,
})
</script>

<style scoped lang="less" src="@/styles/components/system/tag-style-config.less"></style>
