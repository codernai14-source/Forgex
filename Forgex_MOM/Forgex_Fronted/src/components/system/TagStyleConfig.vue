<template>
  <div class="tag-style-config">
    <!-- 颜色和图标在同一行 -->
    <div class="tag-style-row">
      <div class="tag-style-field">
        <label class="tag-style-label">标签颜色</label>
        <a-select
          v-model:value="formData.color"
          placeholder="请选择标签颜色"
          allowClear
          show-search
          :filter-option="filterOption"
        >
          <a-select-opt-group label="状态颜色">
            <a-select-option value="success">
              <a-tag color="success">成功</a-tag>
            </a-select-option>
            <a-select-option value="processing">
              <a-tag color="processing">进行中</a-tag>
            </a-select-option>
            <a-select-option value="error">
              <a-tag color="error">失败</a-tag>
            </a-select-option>
            <a-select-option value="warning">
              <a-tag color="warning">警告</a-tag>
            </a-select-option>
            <a-select-option value="default">
              <a-tag color="default">默认</a-tag>
            </a-select-option>
          </a-select-opt-group>
          <a-select-opt-group label="预设颜色">
            <a-select-option value="pink">
              <a-tag color="pink">粉色</a-tag>
            </a-select-option>
            <a-select-option value="red">
              <a-tag color="red">红色</a-tag>
            </a-select-option>
            <a-select-option value="orange">
              <a-tag color="orange">橙色</a-tag>
            </a-select-option>
            <a-select-option value="green">
              <a-tag color="green">绿色</a-tag>
            </a-select-option>
            <a-select-option value="cyan">
              <a-tag color="cyan">青色</a-tag>
            </a-select-option>
            <a-select-option value="blue">
              <a-tag color="blue">蓝色</a-tag>
            </a-select-option>
            <a-select-option value="purple">
              <a-tag color="purple">紫色</a-tag>
            </a-select-option>
          </a-select-opt-group>
          <a-select-opt-group label="自定义颜色">
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
        <label class="tag-style-label">图标</label>
        <a-select
          v-model:value="formData.icon"
          placeholder="请选择图标"
          allowClear
          show-search
          :filter-option="filterOption"
        >
          <a-select-option value="CheckCircleOutlined">
            <template #label>
              <span><CheckCircleOutlined /> 成功图标</span>
            </template>
            成功图标
          </a-select-option>
          <a-select-option value="CloseCircleOutlined">
            <template #label>
              <span><CloseCircleOutlined /> 失败图标</span>
            </template>
            失败图标
          </a-select-option>
          <a-select-option value="ExclamationCircleOutlined">
            <template #label>
              <span><ExclamationCircleOutlined /> 警告图标</span>
            </template>
            警告图标
          </a-select-option>
          <a-select-option value="SyncOutlined">
            <template #label>
              <span><SyncOutlined /> 加载图标</span>
            </template>
            加载图标
          </a-select-option>
          <a-select-option value="ClockCircleOutlined">
            <template #label>
              <span><ClockCircleOutlined /> 等待图标</span>
            </template>
            等待图标
          </a-select-option>
          <a-select-option value="InfoCircleOutlined">
            <template #label>
              <span><InfoCircleOutlined /> 信息图标</span>
            </template>
            信息图标
          </a-select-option>
          <a-select-option value="CheckOutlined">
            <template #label>
              <span><CheckOutlined /> 勾选图标</span>
            </template>
            勾选图标
          </a-select-option>
          <a-select-option value="CloseOutlined">
            <template #label>
              <span><CloseOutlined /> 关闭图标</span>
            </template>
            关闭图标
          </a-select-option>
        </a-select>
      </div>
    </div>

    <!-- 预览区域 -->
    <div class="tag-style-preview">
      <label class="tag-style-label">预览</label>
      <div class="tag-style-preview-content">
        <a-tag v-if="formData.color" :color="formData.color">
          <template v-if="currentIconComponent" #icon>
            <component :is="currentIconComponent" />
          </template>
          示例标签
        </a-tag>
        <span v-else class="tag-style-preview-empty">请先配置标签样式</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, computed } from 'vue'
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

<style scoped lang="less">
.tag-style-config {
  .tag-style-row {
    display: flex;
    gap: 16px;
    margin-bottom: 12px;
  }

  .tag-style-field {
    flex: 1;
    min-width: 0;

    .tag-style-label {
      display: block;
      margin-bottom: 6px;
      font-size: 14px;
      color: var(--fx-text-primary, #1f1f1f);
      font-weight: 500;
    }
  }

  .tag-style-preview {
    .tag-style-label {
      display: block;
      margin-bottom: 6px;
      font-size: 14px;
      color: var(--fx-text-primary, #1f1f1f);
      font-weight: 500;
    }

    .tag-style-preview-content {
      padding: 12px 16px;
      background: var(--fx-bg-layout, #f5f5f5);
      border-radius: 6px;
      min-height: 40px;
      display: flex;
      align-items: center;

      .tag-style-preview-empty {
        color: var(--fx-text-disabled, #999);
        font-size: 14px;
      }
    }
  }

  :deep(.ant-select) {
    width: 100%;
  }

  :deep(.ant-select-selection-item) {
    display: flex;
    align-items: center;
  }
}
</style>
