<template>
  <a-modal
    v-model:open="modalOpen"
    title="模板预览"
    width="800px"
    :footer="null"
    @cancel="handleClose"
  >
    <a-tabs v-model:activeKey="activeTab">
      <a-tab-pane
        v-for="content in contents"
        :key="content.platform"
        :tab="getPlatformName(content.platform)"
      >
        <div class="preview-container">
          <!-- 平台图标 -->
          <div class="platform-header">
            <component :is="getPlatformIcon(content.platform)" class="platform-icon" />
            <span class="platform-name">{{ getPlatformName(content.platform) }}</span>
          </div>
          
          <!-- 消息预览卡片 -->
          <div class="message-card" :class="`platform-${content.platform.toLowerCase()}`">
            <div class="message-header">
              <div class="message-type">
                <a-tag :color="getMessageTypeColor(messageType)">
                  {{ getMessageTypeText(messageType) }}
                </a-tag>
              </div>
              <div class="message-time">{{ currentTime }}</div>
            </div>
            
            <div class="message-title" v-if="getPreviewTitle(content)">
              {{ getPreviewTitle(content) }}
            </div>
            
            <div class="message-body">
              {{ getPreviewBody(content) }}
            </div>
            
            <div class="message-link" v-if="content.linkUrl">
              <a :href="content.linkUrl" target="_blank">
                <LinkOutlined /> 查看详情
              </a>
            </div>
          </div>
          
          <!-- 占位符说明 -->
          <a-alert
            message="占位符说明"
            type="info"
            show-icon
            style="margin-top: 16px"
          >
            <template #description>
              <div class="placeholder-desc">
                <p>以下占位符将在实际发送时被替换为真实数据：</p>
                <ul>
                  <li><code>${'${userName}'}</code> → 用户名（示例：张三）</li>
                  <li><code>${'${userAccount}'}</code> → 用户账号（示例：zhangsan）</li>
                  <li><code>${'${tenantName}'}</code> → 租户名称（示例：示例企业）</li>
                  <li><code>${'${currentTime}'}</code> → 当前时间（示例：2026-01-27 10:30:00）</li>
                </ul>
              </div>
            </template>
          </a-alert>
        </div>
      </a-tab-pane>
    </a-tabs>
  </a-modal>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import {
  BellOutlined,
  WechatOutlined,
  MessageOutlined,
  MailOutlined,
  LinkOutlined
} from '@ant-design/icons-vue'
import { getI18nValue } from '@/utils/i18n'

interface TemplateContent {
  platform: string
  contentTitle?: string
  contentTitleI18nJson?: string
  contentBody?: string
  contentBodyI18nJson?: string
  linkUrl?: string
}

interface Props {
  /** 弹窗是否可见，用于控制组件的显示/隐藏状态 */
  visible: boolean
  /** 模板内容数组，包含各平台的标题、内容和链接 */
  contents: TemplateContent[]
  /** 消息类型，用于显示对应的标签颜色，如 NOTICE、WARNING、ALARM */
  messageType: string
}

const props = defineProps<Props>()

const emit = defineEmits<{
  /**
   * 更新弹窗可见性
   * 触发时机：点击关闭按钮或取消操作时触发
   * @param value 新的可见性状态
   */
  'update:visible': [value: boolean]
}>()

const modalOpen = computed({
  get: () => props.visible,
  set: (value: boolean) => emit('update:visible', value)
})

const activeTab = ref('INTERNAL')
const currentTime = ref(new Date().toLocaleString('zh-CN'))

watch(
  () => props.contents,
  (contents) => {
    if (!contents.length) {
      return
    }
    if (!contents.some(item => item.platform === activeTab.value)) {
      activeTab.value = contents[0].platform
    }
  },
  { immediate: true, deep: true }
)

watch(
  () => props.visible,
  (visible) => {
    if (visible && props.contents.length > 0) {
      activeTab.value = props.contents[0].platform
    }
  }
)

// 示例数据
const exampleData = {
  userName: '张三',
  userAccount: 'zhangsan',
  tenantName: '示例企业',
  currentTime: currentTime.value,
  title: '系统通知',
  content: '这是一条测试消息'
}

// 获取平台名称
const getPlatformName = (platform: string) => {
  const nameMap: Record<string, string> = {
    INTERNAL: '站内消息',
    WECHAT: '企业微信',
    SMS: '短信',
    EMAIL: '邮件'
  }
  return nameMap[platform] || platform
}

// 获取平台图标
const getPlatformIcon = (platform: string) => {
  const iconMap: Record<string, any> = {
    INTERNAL: BellOutlined,
    WECHAT: WechatOutlined,
    SMS: MessageOutlined,
    EMAIL: MailOutlined
  }
  return iconMap[platform] || BellOutlined
}

// 获取消息类型颜色
const getMessageTypeColor = (type: string) => {
  const colorMap: Record<string, string> = {
    NOTICE: 'blue',
    WARNING: 'orange',
    ALARM: 'red'
  }
  return colorMap[type] || 'default'
}

// 获取消息类型文本
const getMessageTypeText = (type: string) => {
  const textMap: Record<string, string> = {
    NOTICE: '通知',
    WARNING: '警告',
    ALARM: '报警'
  }
  return textMap[type] || type
}

// 替换占位符
const replacePlaceholders = (text: string) => {
  let result = text
  Object.entries(exampleData).forEach(([key, value]) => {
    result = result.replace(new RegExp(`\\$\\{${key}\\}`, 'g'), value)
  })
  return result
}

// 获取预览标题
const getPreviewTitle = (content: TemplateContent) => {
  const title = getI18nValue(content.contentTitleI18nJson, content.contentTitle)
  return title ? replacePlaceholders(title) : ''
}

// 获取预览内容
const getPreviewBody = (content: TemplateContent) => {
  const body = getI18nValue(content.contentBodyI18nJson, content.contentBody)
  return body ? replacePlaceholders(body) : '暂无内容'
}

// 关闭弹窗
const handleClose = () => {
  emit('update:visible', false)
}
</script>

<style scoped lang="less">
.preview-container {
  .platform-header {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 16px;
    padding: 14px 16px;
    background: linear-gradient(135deg, var(--fx-fill-alter, #f5f7fa) 0%, var(--fx-fill-secondary, #eef2ff) 100%);
    border: 1px solid var(--fx-border-color, #e8e8e8);
    border-radius: var(--fx-radius, 8px);
    box-shadow: var(--fx-shadow-secondary, 0 6px 18px rgba(15, 23, 42, 0.08));
    color: var(--fx-text-primary, #111827);
    
    .platform-icon {
      font-size: 24px;
      color: var(--fx-theme-color, #1677ff);
    }
    
    .platform-name {
      font-size: 16px;
      font-weight: 600;
    }
  }
  
  .message-card {
    padding: 20px;
    background: var(--fx-bg-elevated, #ffffff);
    border: 1px solid var(--fx-border-color, #e8e8e8);
    border-radius: var(--fx-radius, 8px);
    box-shadow: var(--fx-shadow-secondary, 0 6px 18px rgba(15, 23, 42, 0.08));
    
    &.platform-internal {
      border-left: 4px solid var(--fx-primary, #1890ff);
    }
    
    &.platform-wechat {
      border-left: 4px solid #07c160;
    }
    
    &.platform-sms {
      border-left: 4px solid #ff6b6b;
    }
    
    &.platform-email {
      border-left: 4px solid #4ecdc4;
    }
    
    .message-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 12px;
      
      .message-time {
        color: var(--fx-text-tertiary, #999);
        font-size: 12px;
      }
    }
    
    .message-title {
      font-size: 16px;
      font-weight: 600;
      color: var(--fx-text-primary, #111827);
      margin-bottom: 12px;
      line-height: 1.5;
    }
    
    .message-body {
      color: var(--fx-text-secondary, #666);
      line-height: 1.8;
      white-space: pre-wrap;
      word-break: break-word;
      margin-bottom: 12px;
    }
    
    .message-link {
      padding-top: 12px;
      border-top: 1px solid var(--fx-border-secondary, #f0f0f0);
      
      a {
        color: var(--fx-primary, #1890ff);
        text-decoration: none;
        display: inline-flex;
        align-items: center;
        gap: 4px;
        
        &:hover {
          color: var(--fx-primary-hover, #40a9ff);
        }
      }
    }
  }
  
  .placeholder-desc {
    color: var(--fx-text-secondary, #666);

    p {
      margin-bottom: 8px;
    }
    
    ul {
      margin: 0;
      padding-left: 20px;
      
      li {
        margin-bottom: 4px;
        
        code {
          background: var(--fx-fill-alter, #f5f5f5);
          padding: 2px 6px;
          border: 1px solid var(--fx-border-secondary, #e8e8e8);
          border-radius: var(--fx-radius-sm, 4px);
          font-family: 'Courier New', monospace;
          color: var(--fx-theme-color, #d63384);
        }
      }
    }
  }
}
</style>

