<template>
  <a-modal
    v-model:open="modalOpen"
    :title="t('common.templatePreview.title')"
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
          <div class="platform-header">
            <component :is="getPlatformIcon(content.platform)" class="platform-icon" />
            <span class="platform-name">{{ getPlatformName(content.platform) }}</span>
          </div>

          <div class="message-card" :class="`platform-${content.platform.toLowerCase()}`">
            <div class="message-header">
              <div class="message-type">
                <a-tag :color="getMessageTypeColor(messageType)">
                  {{ getMessageTypeText(messageType) }}
                </a-tag>
              </div>
              <div class="message-time">{{ currentTime }}</div>
            </div>

            <div v-if="getPreviewTitle(content)" class="message-title">
              {{ getPreviewTitle(content) }}
            </div>

            <div class="message-body">
              {{ getPreviewBody(content) }}
            </div>

            <div v-if="content.linkUrl" class="message-link">
              <a :href="content.linkUrl" target="_blank">
                <LinkOutlined /> {{ t('common.templatePreview.viewDetail') }}
              </a>
            </div>
          </div>

          <a-alert
            :message="t('common.templatePreview.placeholderTitle')"
            type="info"
            show-icon
            style="margin-top: 16px"
          >
            <template #description>
              <div class="placeholder-desc">
                <p>{{ t('common.templatePreview.placeholderDesc') }}</p>
                <ul>
                  <li><code>${'${userName}'}</code> -> {{ t('common.placeholderInput.userName') }}</li>
                  <li><code>${'${userAccount}'}</code> -> {{ t('common.placeholderInput.userAccount') }}</li>
                  <li><code>${'${tenantName}'}</code> -> {{ t('common.placeholderInput.tenantName') }}</li>
                  <li><code>${'${currentTime}'}</code> -> {{ t('common.placeholderInput.currentTime') }}</li>
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
import { computed, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import {
  BellOutlined,
  LinkOutlined,
  MailOutlined,
  MessageOutlined,
  WechatOutlined,
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
  visible: boolean
  contents: TemplateContent[]
  messageType: string
}

const props = defineProps<Props>()

const emit = defineEmits<{
  'update:visible': [value: boolean]
}>()

const { t, locale } = useI18n()

const modalOpen = computed({
  get: () => props.visible,
  set: (value: boolean) => emit('update:visible', value),
})

const activeTab = ref('INTERNAL')
const currentTime = computed(() => new Date().toLocaleString(String(locale.value || 'zh-CN')))

const exampleData = computed(() => ({
  userName: t('common.placeholderInput.userName'),
  userAccount: 'zhangsan',
  tenantName: t('common.placeholderInput.tenantName'),
  currentTime: currentTime.value,
  title: t('common.placeholderInput.exampleTitle'),
  content: t('common.placeholderInput.exampleContent'),
}))

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
  { immediate: true, deep: true },
)

watch(
  () => props.visible,
  (visible) => {
    if (visible && props.contents.length > 0) {
      activeTab.value = props.contents[0].platform
    }
  },
)

const getPlatformName = (platform: string) => {
  const nameMap: Record<string, string> = {
    INTERNAL: t('common.templatePreview.platform.internal'),
    WECHAT: t('common.templatePreview.platform.wechat'),
    SMS: t('common.templatePreview.platform.sms'),
    EMAIL: t('common.templatePreview.platform.email'),
  }
  return nameMap[platform] || platform
}

const getPlatformIcon = (platform: string) => {
  const iconMap: Record<string, any> = {
    INTERNAL: BellOutlined,
    WECHAT: WechatOutlined,
    SMS: MessageOutlined,
    EMAIL: MailOutlined,
  }
  return iconMap[platform] || BellOutlined
}

const getMessageTypeColor = (type: string) => {
  const colorMap: Record<string, string> = {
    NOTICE: 'blue',
    WARNING: 'orange',
    ALARM: 'red',
  }
  return colorMap[type] || 'default'
}

const getMessageTypeText = (type: string) => {
  const textMap: Record<string, string> = {
    NOTICE: t('common.templatePreview.messageType.notice'),
    WARNING: t('common.templatePreview.messageType.warning'),
    ALARM: t('common.templatePreview.messageType.alarm'),
  }
  return textMap[type] || type
}

const replacePlaceholders = (text: string) => {
  let result = text
  Object.entries(exampleData.value).forEach(([key, value]) => {
    result = result.replace(new RegExp(`\\$\\{${key}\\}`, 'g'), value)
  })
  return result
}

const getPreviewTitle = (content: TemplateContent) => {
  const title = getI18nValue(content.contentTitleI18nJson, content.contentTitle)
  return title ? replacePlaceholders(title) : ''
}

const getPreviewBody = (content: TemplateContent) => {
  const body = getI18nValue(content.contentBodyI18nJson, content.contentBody)
  return body ? replacePlaceholders(body) : t('common.noData')
}

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
    border: 1px solid var(--fx-border-color);
    border-radius: var(--fx-radius, 8px);
    color: var(--fx-text-primary);
    background: var(--fx-fill-secondary);
    box-shadow: var(--fx-shadow-secondary);

    .platform-icon {
      color: var(--fx-theme-color);
      font-size: 24px;
    }

    .platform-name {
      font-size: 16px;
      font-weight: 600;
    }
  }

  .message-card {
    padding: 20px;
    border: 1px solid var(--fx-border-color);
    border-radius: var(--fx-radius, 8px);
    background: var(--fx-bg-container);
    box-shadow: var(--fx-shadow-secondary);

    &.platform-internal {
      border-left: 4px solid var(--fx-primary);
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
      align-items: center;
      justify-content: space-between;
      margin-bottom: 12px;

      .message-time {
        color: var(--fx-text-tertiary);
        font-size: 12px;
      }
    }

    .message-title {
      margin-bottom: 12px;
      color: var(--fx-text-primary);
      font-size: 16px;
      font-weight: 600;
      line-height: 1.5;
    }

    .message-body {
      margin-bottom: 12px;
      color: var(--fx-text-secondary);
      line-height: 1.8;
      white-space: pre-wrap;
      word-break: break-word;
    }

    .message-link {
      padding-top: 12px;
      border-top: 1px solid var(--fx-border-color);

      a {
        display: inline-flex;
        align-items: center;
        gap: 4px;
        color: var(--fx-primary);
        text-decoration: none;

        &:hover {
          color: var(--fx-primary-hover);
        }
      }
    }
  }

  .placeholder-desc {
    color: var(--fx-text-secondary);

    p {
      margin-bottom: 8px;
    }

    ul {
      margin: 0;
      padding-left: 20px;

      li {
        margin-bottom: 4px;

        code {
          padding: 2px 6px;
          border: 1px solid var(--fx-border-color);
          border-radius: var(--fx-radius-sm, 4px);
          color: var(--fx-primary);
          background: var(--fx-fill-alter);
          font-family: 'Courier New', monospace;
        }
      }
    }
  }
}
</style>
