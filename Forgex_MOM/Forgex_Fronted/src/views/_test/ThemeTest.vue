<template>
  <div class="theme-test-page">
    <a-card title="主题系统测试页面" class="test-card">
      <a-space direction="vertical" :size="24" style="width: 100%">
        <a-alert
          :message="`当前主题模式: ${currentThemeInfo}`"
          type="info"
          show-icon
        />

        <div class="test-section">
          <h3>颜色系统测试</h3>
          <a-row :gutter="[16, 16]">
            <a-col :span="8">
              <div class="color-box" style="background: var(--fx-primary)">
                <span>主题色</span>
                <small>--fx-primary</small>
              </div>
            </a-col>
            <a-col :span="8">
              <div class="color-box" style="background: var(--fx-success)">
                <span>成功色</span>
                <small>--fx-success</small>
              </div>
            </a-col>
            <a-col :span="8">
              <div class="color-box" style="background: var(--fx-warning)">
                <span>警告色</span>
                <small>--fx-warning</small>
              </div>
            </a-col>
            <a-col :span="8">
              <div class="color-box" style="background: var(--fx-error)">
                <span>错误色</span>
                <small>--fx-error</small>
              </div>
            </a-col>
            <a-col :span="8">
              <div class="color-box" style="background: var(--fx-bg-container); border: 1px solid var(--fx-border-color)">
                <span style="color: var(--fx-text-primary)">容器背景</span>
                <small style="color: var(--fx-text-secondary)">--fx-bg-container</small>
              </div>
            </a-col>
            <a-col :span="8">
              <div class="color-box" style="background: var(--fx-fill)">
                <span style="color: var(--fx-text-primary)">填充色</span>
                <small style="color: var(--fx-text-secondary)">--fx-fill</small>
              </div>
            </a-col>
          </a-row>
        </div>

        <div class="test-section">
          <h3>按钮组件测试</h3>
          <a-space wrap>
            <a-button type="primary">主要按钮</a-button>
            <a-button>默认按钮</a-button>
            <a-button type="dashed">虚线按钮</a-button>
            <a-button type="text">文本按钮</a-button>
            <a-button type="link">链接按钮</a-button>
            <a-button danger>危险按钮</a-button>
            <a-button disabled>禁用按钮</a-button>
          </a-space>
        </div>

        <div class="test-section">
          <h3>表格组件测试</h3>
          <a-table
            :columns="tableColumns"
            :data-source="tableData"
            :pagination="false"
            size="middle"
          />
        </div>

        <div class="test-section">
          <h3>表单组件测试</h3>
          <a-form layout="vertical" style="max-width: 600px">
            <a-form-item label="输入框">
              <a-input placeholder="请输入内容" />
            </a-form-item>
            <a-form-item label="选择器">
              <a-select placeholder="请选择" style="width: 100%">
                <a-select-option value="1">选项1</a-select-option>
                <a-select-option value="2">选项2</a-select-option>
              </a-select>
            </a-form-item>
            <a-form-item label="日期选择">
              <a-date-picker style="width: 100%" />
            </a-form-item>
            <a-form-item label="开关">
              <a-switch />
            </a-form-item>
          </a-form>
        </div>

        <div class="test-section">
          <h3>卡片组件测试</h3>
          <a-row :gutter="16">
            <a-col :span="8">
              <a-card title="卡片标题" size="small">
                <p>卡片内容</p>
                <p>测试文本颜色和背景</p>
              </a-card>
            </a-col>
            <a-col :span="8">
              <a-card title="带操作卡片" size="small">
                <template #extra>
                  <a-button type="link" size="small">更多</a-button>
                </template>
                <p>卡片内容</p>
              </a-card>
            </a-col>
            <a-col :span="8">
              <a-card size="small">
                <a-statistic
                  title="统计数值"
                  :value="11280"
                  :precision="2"
                  suffix="元"
                />
              </a-card>
            </a-col>
          </a-row>
        </div>

        <div class="test-section">
          <h3>消息提示测试</h3>
          <a-space wrap>
            <a-button @click="showMessage('success')">成功消息</a-button>
            <a-button @click="showMessage('info')">信息消息</a-button>
            <a-button @click="showMessage('warning')">警告消息</a-button>
            <a-button @click="showMessage('error')">错误消息</a-button>
            <a-button @click="showModal">打开弹窗</a-button>
          </a-space>
        </div>

        <div class="test-section">
          <h3>CSS 变量列表</h3>
          <a-collapse>
            <a-collapse-panel key="1" header="查看所有 CSS 变量">
              <div class="css-vars-list">
                <div v-for="varName in cssVarNames" :key="varName" class="css-var-item">
                  <span class="var-name">{{ varName }}</span>
                  <span class="var-value" :style="{ background: `var(${varName})` }">
                    {{ getCSSVarValue(varName) }}
                  </span>
                </div>
              </div>
            </a-collapse-panel>
          </a-collapse>
        </div>
      </a-space>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { message, Modal } from 'ant-design-vue'

const tableColumns = [
  { title: '姓名', dataIndex: 'name', key: 'name' },
  { title: '年龄', dataIndex: 'age', key: 'age' },
  { title: '地址', dataIndex: 'address', key: 'address' },
]

const tableData = [
  { key: '1', name: '张三', age: 32, address: '北京市朝阳区' },
  { key: '2', name: '李四', age: 28, address: '上海市浦东新区' },
  { key: '3', name: '王五', age: 35, address: '广州市天河区' },
]

const cssVarNames = [
  '--fx-primary',
  '--fx-primary-hover',
  '--fx-primary-active',
  '--fx-success',
  '--fx-warning',
  '--fx-error',
  '--fx-bg-base',
  '--fx-bg-container',
  '--fx-bg-elevated',
  '--fx-bg-layout',
  '--fx-text-primary',
  '--fx-text-secondary',
  '--fx-text-tertiary',
  '--fx-border-color',
  '--fx-border-secondary',
  '--fx-fill',
  '--fx-fill-secondary',
  '--fx-fill-alter',
]

function getCSSVarValue(varName: string): string {
  if (typeof window === 'undefined') return ''
  return getComputedStyle(document.documentElement).getPropertyValue(varName).trim()
}

const currentThemeInfo = computed(() => {
  const mode = getCSSVarValue('--fx-theme-mode') || '未知'
  return mode
})

function showMessage(type: 'success' | 'info' | 'warning' | 'error') {
  message[type](`这是一条${type}消息`)
}

function showModal() {
  Modal.info({
    title: '测试弹窗',
    content: '这是一个测试弹窗，用于验证主题样式是否正确应用。',
  })
}
</script>

<style scoped lang="less" src="@/styles/views/_test/theme-test.less"></style>

