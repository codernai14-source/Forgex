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
              <a-tag color="success">鎴愬姛</a-tag>
            </a-select-option>
            <a-select-option value="processing">
              <a-tag color="processing">进行中</a-tag>
            </a-select-option>
            <a-select-option value="error">
              <a-tag color="error">閿欒</a-tag>
            </a-select-option>
            <a-select-option value="warning">
              <a-tag color="warning">璀﹀憡</a-tag>
            </a-select-option>
            <a-select-option value="default">
              <a-tag color="default">榛樿</a-tag>
            </a-select-option>
          </a-select-opt-group>
          <a-select-opt-group label="预设颜色">
            <a-select-option value="pink">
              <a-tag color="pink">绮夎壊</a-tag>
            </a-select-option>
            <a-select-option value="red">
              <a-tag color="red">绾㈣壊</a-tag>
            </a-select-option>
            <a-select-option value="orange">
              <a-tag color="orange">姗欒壊</a-tag>
            </a-select-option>
            <a-select-option value="green">
              <a-tag color="green">缁胯壊</a-tag>
            </a-select-option>
            <a-select-option value="cyan">
              <a-tag color="cyan">闈掕壊</a-tag>
            </a-select-option>
            <a-select-option value="blue">
              <a-tag color="blue">钃濊壊</a-tag>
            </a-select-option>
            <a-select-option value="purple">
              <a-tag color="purple">绱壊</a-tag>
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
            <CheckCircleOutlined /> 鎴愬姛鍥炬爣
          </a-select-option>
          <a-select-option value="CloseCircleOutlined">
            <CloseCircleOutlined /> 閿欒鍥炬爣
          </a-select-option>
          <a-select-option value="ExclamationCircleOutlined">
            <ExclamationCircleOutlined /> 璀﹀憡鍥炬爣
          </a-select-option>
          <a-select-option value="SyncOutlined">
            <SyncOutlined /> 鍔犺浇鍥炬爣
          </a-select-option>
          <a-select-option value="ClockCircleOutlined">
            <ClockCircleOutlined /> 鏃堕挓鍥炬爣
          </a-select-option>
          <a-select-option value="InfoCircleOutlined">
            <InfoCircleOutlined /> 淇℃伅鍥炬爣
          </a-select-option>
          <a-select-option value="CheckOutlined">
            <CheckOutlined /> 勾选图标
          </a-select-option>
          <a-select-option value="CloseOutlined">
            <CloseOutlined /> 鍏抽棴鍥炬爣
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

interface TagStyle表单 {
  color?: string
  icon?: string
}

const formData = reactive<TagStyle表单>({})

const getTagStyleJson = (): string => {
  const data: TagStyle表单 = {}
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
