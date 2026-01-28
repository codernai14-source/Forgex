<template>
  <div class="tag-style-config">
    <a-form :model="formData" layout="vertical">
      <a-form-item label="标签颜色">
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
              <a-tag color="error">错误</a-tag>
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
      </a-form-item>

      <a-form-item label="图标">
        <a-select
          v-model:value="formData.icon"
          placeholder="请选择图标"
          allowClear
          show-search
          :filter-option="filterOption"
        >
          <a-select-option value="CheckCircleOutlined">
            <CheckCircleOutlined /> 成功图标
          </a-select-option>
          <a-select-option value="CloseCircleOutlined">
            <CloseCircleOutlined /> 错误图标
          </a-select-option>
          <a-select-option value="ExclamationCircleOutlined">
            <ExclamationCircleOutlined /> 警告图标
          </a-select-option>
          <a-select-option value="SyncOutlined">
            <SyncOutlined /> 加载图标
          </a-select-option>
          <a-select-option value="ClockCircleOutlined">
            <ClockCircleOutlined /> 时钟图标
          </a-select-option>
          <a-select-option value="InfoCircleOutlined">
            <InfoCircleOutlined /> 信息图标
          </a-select-option>
          <a-select-option value="CheckOutlined">
            <CheckOutlined /> 勾选图标
          </a-select-option>
          <a-select-option value="CloseOutlined">
            <CloseOutlined /> 关闭图标
          </a-select-option>
        </a-select>
      </a-form-item>

      <a-form-item label="预览">
        <a-tag v-if="formData.color" :color="formData.color">
          <template v-if="formData.icon" #icon>
            <component :is="formData.icon" />
          </template>
          示例标签
        </a-tag>
        <span v-else style="color: #999">请配置标签样式</span>
      </a-form-item>
    </a-form>
  </div>
</template>

<script setup lang="ts">
import { reactive } from 'vue'
import {
  CheckCircleOutlined,
  CloseCircleOutlined,
  ExclamationCircleOutlined,
  SyncOutlined,
  ClockCircleOutlined,
  InfoCircleOutlined,
  CheckOutlined,
  CloseOutlined
} from '@ant-design/icons-vue'

interface TagStyleForm {
  color?: string
  icon?: string
}

const formData = reactive<TagStyleForm>({})

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

const setTagStyleJson = (json: string) => {
  try {
    const data = JSON.parse(json)
    formData.color = data.color || undefined
    formData.icon = data.icon || undefined
  } catch (e) {
    console.error('解析标签样式失败', e)
  }
}

const filterOption = (input: string, option: any) => {
  return option.label?.toLowerCase().includes(input.toLowerCase())
}

defineExpose({
  getTagStyleJson,
  setTagStyleJson
})
</script>

<style scoped lang="less">
.tag-style-config {
  :deep(.ant-select-selection-item) {
    display: flex;
    align-items: center;
  }
}
</style>
