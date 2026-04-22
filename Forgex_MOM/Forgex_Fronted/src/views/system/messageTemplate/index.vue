<template>
  <div class="message-template-page">
    <FxDynamicTable
      ref="tableRef"
      table-code="MessageTemplateTable"
      :show-query-form="true"
      :request="handleRequest"
      :dict-options="dictOptions"
      :row-selection="{ selectedRowKeys, onChange: onSelectChange }"
      row-key="id"
    >
      <template #toolbar>
        <a-space>
          <a-radio-group v-model:value="publicConfig" button-style="solid" @change="handleConfigModeChange">
            <a-radio-button :value="false">{{ t('system.messageTemplate.configMode.tenant') }}</a-radio-button>
            <a-radio-button :value="true">{{ t('system.messageTemplate.configMode.public') }}</a-radio-button>
          </a-radio-group>
          <a-button v-if="!publicConfig" @click="handlePullPublicConfig">
            <template #icon><SyncOutlined /></template>
            {{ t('system.messageTemplate.toolbar.pullPublicConfig') }}
          </a-button>
          <a-button type="primary" @click="handleAdd">
            <template #icon><PlusOutlined /></template>
            {{ t('common.add') }}
          </a-button>
          <a-button danger :disabled="selectedRowKeys.length === 0" @click="handleBatchDelete">
            <template #icon><DeleteOutlined /></template>
            {{ t('common.batchDelete') }}
          </a-button>
        </a-space>
      </template>

      <template #messageType="{ record }">
        <a-tag :color="getMessageTypeColor(record.messageType)">
          {{ getMessageTypeText(record.messageType) }}
        </a-tag>
      </template>

      <template #status="{ record }">
        <a-tag :color="record.status ? 'success' : 'default'">
          {{ record.status ? t('common.enabled') : t('common.disabled') }}
        </a-tag>
      </template>

      <template #action="{ record }">
        <a-space>
          <a-button type="link" size="small" @click="handleEdit(record)">{{ t('common.edit') }}</a-button>
          <a-button type="link" size="small" danger @click="handleDelete(record)">{{ t('common.delete') }}</a-button>
        </a-space>
      </template>
    </FxDynamicTable>

    <BaseFormDialog
      v-model:open="modalVisible"
      :title="modalTitle"
      width="900px"
      :loading="modalLoading"
      @submit="handleModalOk"
      @cancel="handleModalCancel"
    >
      <a-tabs v-model:activeKey="activeTab">
        <a-tab-pane key="basic" :tab="t('system.messageTemplate.tab.basic')">
          <a-form :model="formData" :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }">
            <a-form-item :label="t('system.messageTemplate.form.templateCode')" required>
              <a-input
                v-model:value="formData.templateCode"
                :placeholder="t('system.messageTemplate.form.templateCodePlaceholder')"
              />
            </a-form-item>
            <a-form-item :label="t('system.messageTemplate.form.templateName')" required>
              <I18nInput
                v-model="formData.templateNameI18nJson"
                mode="simple"
                :placeholder="t('system.messageTemplate.form.templateNamePlaceholder')"
              />
            </a-form-item>
            <a-form-item :label="t('system.messageTemplate.form.templateVersion')">
              <a-input
                v-model:value="formData.templateVersion"
                :placeholder="t('system.messageTemplate.form.templateVersionPlaceholder')"
              />
            </a-form-item>
            <a-form-item :label="t('system.messageTemplate.form.messageType')" required>
              <a-select
                v-model:value="formData.messageType"
                :placeholder="t('system.messageTemplate.form.messageTypePlaceholder')"
              >
                <a-select-option v-for="item in messageTypeOptions" :key="item.value" :value="item.value">
                  {{ item.label }}
                </a-select-option>
              </a-select>
            </a-form-item>
            <a-form-item :label="t('system.messageTemplate.form.bizType')">
              <a-input
                v-model:value="formData.bizType"
                :placeholder="t('system.messageTemplate.form.bizTypePlaceholder')"
              />
            </a-form-item>
            <a-form-item :label="t('common.status')">
              <a-switch
                v-model:checked="formData.status"
                :checked-children="t('common.enabled')"
                :un-checked-children="t('common.disabled')"
              />
            </a-form-item>
            <a-form-item :label="t('common.remark')">
              <a-textarea
                v-model:value="formData.remark"
                :placeholder="t('system.messageTemplate.form.remarkPlaceholder')"
                :rows="3"
              />
            </a-form-item>
          </a-form>
        </a-tab-pane>

        <a-tab-pane key="receiver" :tab="t('system.messageTemplate.tab.receiver')">
          <a-alert
            :message="t('system.messageTemplate.receiver.tipTitle')"
            :description="t('system.messageTemplate.receiver.tipDesc')"
            type="info"
            show-icon
            style="margin-bottom: 16px"
          />
          <a-button type="dashed" block @click="handleAddReceiver" style="margin-bottom: 16px">
            <template #icon><PlusOutlined /></template>
            {{ t('system.messageTemplate.receiver.add') }}
          </a-button>
          <div v-for="(_, index) in formData.receivers" :key="index" class="receiver-item">
            <a-card size="small">
              <template #title>
                <span>{{ t('system.messageTemplate.receiver.itemTitle', { index: index + 1 }) }}</span>
              </template>
              <template #extra>
                <a-button type="link" danger size="small" @click="handleRemoveReceiver(index)">
                  {{ t('common.delete') }}
                </a-button>
              </template>
              <ReceiverSelector
                v-model="formData.receivers[index]"
                @update:modelValue="(val) => handleReceiverUpdate(index, val)"
              />
            </a-card>
          </div>
          <a-empty v-if="formData.receivers.length === 0" :description="t('system.messageTemplate.receiver.empty')" />
        </a-tab-pane>

        <a-tab-pane key="content" :tab="t('system.messageTemplate.tab.content')">
          <a-space style="margin-bottom: 16px; width: 100%; justify-content: space-between;">
            <a-button type="dashed" @click="handleAddContent">
              <template #icon><PlusOutlined /></template>
              {{ t('system.messageTemplate.content.add') }}
            </a-button>
            <a-button @click="handleTestSend" :loading="testSendLoading" :disabled="formData.contents.length === 0">
              <template #icon><SendOutlined /></template>
              {{ t('system.messageTemplate.content.testSend') }}
            </a-button>
            <a-button type="primary" @click="handlePreview" :disabled="formData.contents.length === 0">
              <template #icon><EyeOutlined /></template>
              {{ t('system.messageTemplate.content.preview') }}
            </a-button>
          </a-space>
          <div v-for="(content, index) in formData.contents" :key="index" class="content-item">
            <a-card size="small">
              <template #title>
                <span>{{ getPlatformName(content.platform) || t('system.messageTemplate.content.itemTitle', { index: index + 1 }) }}</span>
              </template>
              <template #extra>
                <a-button type="link" danger size="small" @click="handleRemoveContent(index)">
                  {{ t('common.delete') }}
                </a-button>
              </template>
              <a-form :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }">
                <a-form-item :label="t('system.messageTemplate.content.platform')" required>
                  <a-select
                    v-model:value="formData.contents[index].platform"
                    :placeholder="t('system.messageTemplate.content.platformPlaceholder')"
                  >
                    <a-select-option v-for="item in platformOptions" :key="item.value" :value="item.value">
                      {{ item.label }}
                    </a-select-option>
                  </a-select>
                </a-form-item>
                <a-form-item :label="t('system.messageTemplate.content.title')">
                  <I18nInput
                    v-model="formData.contents[index].contentTitleI18nJson"
                    mode="simple"
                    :placeholder="t('system.messageTemplate.content.titlePlaceholder')"
                  />
                  <template #extra>
                    <span class="field-hint">
                      {{ t('system.messageTemplate.content.titleHint') }}
                    </span>
                  </template>
                </a-form-item>
                <a-form-item :label="t('system.messageTemplate.content.body')" required>
                  <I18nInput
                    v-model="formData.contents[index].contentBodyI18nJson"
                    mode="simple"
                    type="textarea"
                    :rows="6"
                    :show-placeholders="true"
                    :placeholder="t('system.messageTemplate.content.bodyPlaceholder')"
                  />
                </a-form-item>
                <a-form-item :label="t('system.messageTemplate.content.linkUrl')">
                  <a-input
                    v-model:value="formData.contents[index].linkUrl"
                    :placeholder="t('system.messageTemplate.content.linkUrlPlaceholder')"
                  />
                  <template #extra>
                    <span class="field-hint">
                      {{ t('system.messageTemplate.content.linkUrlHint') }}
                    </span>
                  </template>
                </a-form-item>
              </a-form>
            </a-card>
          </div>
          <a-empty v-if="formData.contents.length === 0" :description="t('system.messageTemplate.content.empty')" />
        </a-tab-pane>
      </a-tabs>
    </BaseFormDialog>

    <TemplatePreview
      v-model:visible="previewVisible"
      :contents="formData.contents"
      :message-type="formData.messageType || 'NOTICE'"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { message, Modal } from 'ant-design-vue'
import { useI18n } from 'vue-i18n'
import {
  PlusOutlined,
  DeleteOutlined,
  EyeOutlined,
  SendOutlined,
  SyncOutlined,
} from '@ant-design/icons-vue'
import I18nInput from '@/components/common/I18nInput.vue'
import ReceiverSelector from '@/components/common/ReceiverSelector.vue'
import TemplatePreview from '@/components/common/TemplatePreview.vue'
import BaseFormDialog from '@/components/common/BaseFormDialog.vue'
import { getI18nValue, toI18nJson } from '@/utils/i18n'
import {
  pageMessageTemplate,
  getMessageTemplate,
  saveMessageTemplate,
  deleteMessageTemplate,
  deleteBatchMessageTemplate,
  pullPublicMessageTemplate,
} from '@/api/message'
import { sendMessage as sendSystemMessage } from '@/api/system/message'
import FxDynamicTable from '@/components/common/FxDynamicTable.vue'
import { useUserStore } from '@/stores/user'

const { t } = useI18n()
const tableRef = ref<any>()
const userStore = useUserStore()

const messageTypeOptions = computed(() => ([
  { label: t('system.messageTemplate.messageType.notice'), value: 'NOTICE' },
  { label: t('system.messageTemplate.messageType.warning'), value: 'WARNING' },
  { label: t('system.messageTemplate.messageType.alarm'), value: 'ALARM' },
]))

const platformOptions = computed(() => ([
  { label: t('system.messageTemplate.platform.internal'), value: 'INTERNAL' },
  { label: t('system.messageTemplate.platform.wechat'), value: 'WECHAT' },
  { label: t('system.messageTemplate.platform.sms'), value: 'SMS' },
  { label: t('system.messageTemplate.platform.email'), value: 'EMAIL' },
]))

const dictOptions = computed(() => ({
  messageType: messageTypeOptions.value,
  status: [
    { label: t('common.enabled'), value: true },
    { label: t('common.disabled'), value: false },
  ],
}))


const selectedRowKeys = ref<Array<number | string>>([])
const publicConfig = ref(false)

const modalVisible = ref(false)
const isEditMode = ref(false)
const modalLoading = ref(false)
const activeTab = ref('basic')

const previewVisible = ref(false)
const testSendLoading = ref(false)

const modalTitle = computed(() => (
  isEditMode.value
    ? t('system.messageTemplate.modal.editTitle')
    : t('system.messageTemplate.modal.addTitle')
))

const formData = reactive({
  id: undefined as number | undefined,
  templateCode: '',
  templateName: '',
  templateNameI18nJson: '',
  templateVersion: '1.0',
  messageType: undefined as string | undefined,
  bizType: '',
  status: true,
  remark: '',
  receivers: [] as any[],
  contents: [] as any[],
})

const resolveI18nText = (i18nJson?: string, 降级方案 = '') => {
  return getI18nValue(i18nJson, 降级方案).trim()
}

const ensureI18nJsonValue = (i18nJson?: string, 降级方案 = '') => {
  if (typeof i18nJson === 'string' && i18nJson.trim()) {
    return i18nJson
  }
  const text = 降级方案.trim()
  return text ? toI18nJson({ 'zh-CN': text }) : ''
}

const normalize表单Data = (data: any) => {
  const next = data || {}
  return {
    ...next,
    bizType: typeof next.bizType === 'string' ? next.bizType : '',
    templateNameI18nJson: ensureI18nJsonValue(next.templateNameI18nJson, next.templateName || ''),
    receivers: Array.isArray(next.receivers) ? next.receivers : [],
    contents: Array.isArray(next.contents)
      ? next.contents.map((content: any) => ({
          ...content,
          contentTitleI18nJson: ensureI18nJsonValue(content?.contentTitleI18nJson, content?.contentTitle || ''),
          contentBodyI18nJson: ensureI18nJsonValue(content?.contentBodyI18nJson, content?.contentBody || ''),
        }))
      : [],
  }
}

const buildSavePayload = () => {
  return {
    ...formData,
    publicConfig: publicConfig.value,
    bizType: formData.bizType?.trim() || undefined,
    templateName: resolveI18nText(formData.templateNameI18nJson, formData.templateName),
    templateNameI18nJson: ensureI18nJsonValue(formData.templateNameI18nJson, formData.templateName),
    receivers: formData.receivers.map((receiver: any) => ({
      ...receiver,
      receiverIds: Array.isArray(receiver.receiverIds) ? receiver.receiverIds : [],
    })),
    contents: formData.contents.map((content: any) => ({
      ...content,
      contentTitle: resolveI18nText(content.contentTitleI18nJson, content.contentTitle),
      contentTitleI18nJson: ensureI18nJsonValue(content.contentTitleI18nJson, content.contentTitle),
      contentBody: resolveI18nText(content.contentBodyI18nJson, content.contentBody),
      contentBodyI18nJson: ensureI18nJsonValue(content.contentBodyI18nJson, content.contentBody),
      linkUrl: content.linkUrl || '',
    })),
  }
}

const formatCurrentTime = () => {
  const now = new Date()
  const pad = (value: number) => String(value).padStart(2, '0')
  return `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())} ${pad(now.getHours())}:${pad(now.getMinutes())}:${pad(now.getSeconds())}`
}

const renderPlaceholderText = (template: string, values: Record<string, string>) => {
  if (!template) {
    return ''
  }
  return template.replace(/\$\{([^}]+)}/g, (_, key: string) => values[key] ?? '')
}

const ensureCurrentUserId = async () => {
  if (typeof userStore.userInfo?.id === 'number') {
    return userStore.userInfo.id
  }

  await userStore.restoreFromSession()

  if (typeof userStore.userInfo?.id === 'number') {
    return userStore.userInfo.id
  }

  const account = sessionStorage.getItem('account')
  if (account) {
    const tempId = parseInt(account.replace(/\D/g, '') || '0', 10)
    if (tempId > 0) {
      return tempId
    }
  }

  return undefined
}

const validate表单 = () => {
  if (!formData.templateCode || !formData.templateCode.trim()) {
    message.error(t('system.messageTemplate.validate.templateCodeRequired'))
    activeTab.value = 'basic'
    return false
  }

  if (!formData.templateNameI18nJson || !formData.templateNameI18nJson.trim()) {
    message.error(t('system.messageTemplate.validate.templateNameRequired'))
    activeTab.value = 'basic'
    return false
  }

  if (!formData.messageType) {
    message.error(t('system.messageTemplate.validate.messageTypeRequired'))
    activeTab.value = 'basic'
    return false
  }

  if (formData.receivers.length === 0) {
    message.error(t('system.messageTemplate.receiver.atLeastOne'))
    activeTab.value = 'receiver'
    return false
  }

  for (let i = 0; i < formData.receivers.length; i += 1) {
    const receiver = formData.receivers[i]
    if (!receiver.receiverType) {
      message.error(t('system.messageTemplate.receiver.typeRequired', { index: i + 1 }))
      activeTab.value = 'receiver'
      return false
    }
    // 鑷畾涔夌被鍨嬩笉闇€瑕佹寚瀹氭帴鏀朵汉 ID
    if (receiver.receiverType !== 'CUSTOM' && (!receiver.receiverIds || receiver.receiverIds.length === 0)) {
      message.error(t('system.messageTemplate.receiver.targetRequired', { index: i + 1 }))
      activeTab.value = 'receiver'
      return false
    }
  }

  if (formData.contents.length === 0) {
    message.error(t('system.messageTemplate.content.atLeastOne'))
    activeTab.value = 'content'
    return false
  }

  for (let i = 0; i < formData.contents.length; i += 1) {
    const content = formData.contents[i]
    if (!content.platform) {
      message.error(t('system.messageTemplate.content.platformRequired', { index: i + 1 }))
      activeTab.value = 'content'
      return false
    }
    if (!content.contentBodyI18nJson || !content.contentBodyI18nJson.trim()) {
      message.error(t('system.messageTemplate.content.bodyRequired', { index: i + 1 }))
      activeTab.value = 'content'
      return false
    }
  }

  return true
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
  const option = messageTypeOptions.value.find((item) => item.value === type)
  return option?.label || type
}

const getPlatformName = (platform: string) => {
  const option = platformOptions.value.find((item) => item.value === platform)
  return option?.label || ''
}

const handlePreview = () => {
  if (formData.contents.length === 0) {
    message.warning(t('system.messageTemplate.message.previewNeedContent'))
    return
  }
  previewVisible.value = true
}

const handleTestSend = async () => {
  const internalContent = formData.contents.find((item: any) => item.platform === 'INTERNAL')
  if (!internalContent) {
    activeTab.value = 'content'
    message.warning(t('system.messageTemplate.content.needInternalForTest'))
    return
  }

  const receiverUserId = await ensureCurrentUserId()
  if (!receiverUserId) {
    message.error(t('system.messageTemplate.message.resolveUserFailed'))
    return
  }

  const rawTitle = resolveI18nText(internalContent.contentTitleI18nJson, internalContent.contentTitle)
  const rawBody = resolveI18nText(internalContent.contentBodyI18nJson, internalContent.contentBody)
  const 降级方案Title = resolveI18nText(formData.templateNameI18nJson, formData.templateName)
    || t('system.messageTemplate.testData.defaultTemplateTitle')

  const placeholderValues: Record<string, string> = {
    userName:
      userStore.userInfo?.username
      || userStore.userInfo?.account
      || sessionStorage.getItem('account')
      || t('system.messageTemplate.testData.currentUser'),
    userAccount: userStore.userInfo?.account || sessionStorage.getItem('account') || 'current.user',
    tenantName: userStore.userInfo?.tenantName || t('system.messageTemplate.testData.currentTenant'),
    currentTime: formatCurrentTime(),
    title: 降级方案Title,
    content: t('system.messageTemplate.testData.defaultContent'),
    linkUrl: internalContent.linkUrl || '/workspace',
  }

  const title = renderPlaceholderText(rawTitle, placeholderValues) || 降级方案Title
  const content = renderPlaceholderText(rawBody, { ...placeholderValues, title }) || placeholderValues.content
  const linkUrl = renderPlaceholderText(internalContent.linkUrl || '', { ...placeholderValues, title, content })

  testSendLoading.value = true
  try {
    await sendSystemMessage({
      receiverUserId,
      scope: 'INTERNAL',
      messageType: formData.messageType || 'NOTICE',
      title,
      content,
      linkUrl: linkUrl || undefined,
      bizType: 'MESSAGE_TEMPLATE_TEST',
    })
    message.success(t('system.messageTemplate.message.testSendSuccess'))
  } finally {
    testSendLoading.value = false
  }
}

const handleRequest = async (payload: {
  page: { current: number; pageSize: number }
  query: Record<string, any>
  sorter?: { field?: string; order?: string }
}) => {
  const params: any = {
    pageNum: payload.page.current,
    pageSize: payload.page.pageSize,
    ...payload.query,
    publicConfig: publicConfig.value,
  }

  if (typeof params.bizType === 'string') {
    params.bizType = params.bizType.trim() || undefined
  }

  if (payload.sorter?.field) {
    params.sortField = payload.sorter.field
    params.sortOrder = payload.sorter.order
  }

  const res: any = await pageMessageTemplate(params)
  const total = typeof res.total === 'number' ? res.total : parseInt(String(res.total) || '0', 10)
  const records = (res.records || []).map((item: any) => ({
    ...item,
    templateName: getI18nValue(item.templateNameI18nJson, item.templateName),
  }))
  return { records, total }
}

const onSelectChange = (keys: Array<number | string>) => {
  selectedRowKeys.value = keys
}

const handleConfigModeChange = async () => {
  selectedRowKeys.value = []
  await tableRef.value?.reload?.()
}

const handlePullPublicConfig = async () => {
  await pullPublicMessageTemplate()
  message.success(t('system.messageTemplate.message.pullSuccess'))
  await tableRef.value?.reload?.()
}

const reset表单Data = () => {
  Object.assign(formData, {
    id: undefined,
    templateCode: '',
    templateName: '',
    templateNameI18nJson: '',
    templateVersion: '1.0',
    messageType: undefined,
    bizType: '',
    status: true,
    remark: '',
    receivers: [],
    contents: [],
  })
}

const handleAdd = () => {
  isEditMode.value = false
  activeTab.value = 'basic'
  reset表单Data()
  modalVisible.value = true
}

const handleEdit = async (record: any) => {
  isEditMode.value = true
  activeTab.value = 'basic'
  const res = await getMessageTemplate(record.id, publicConfig.value)
  Object.assign(formData, normalize表单Data(res))
  modalVisible.value = true
}

const handleDelete = (record: any) => {
  const templateName = record.templateName || record.templateCode || record.id
  Modal.confirm({
    title: t('system.messageTemplate.confirm.deleteTitle'),
    content: t('system.messageTemplate.confirm.deleteContent', { name: templateName }),
    onOk: async () => {
      await deleteMessageTemplate(record.id, publicConfig.value)
      await tableRef.value?.reload?.()
    },
  })
}

const handleBatchDelete = () => {
  Modal.confirm({
    title: t('system.messageTemplate.confirm.batchDeleteTitle'),
    content: t('system.messageTemplate.confirm.batchDeleteContent', { count: selectedRowKeys.value.length }),
    onOk: async () => {
      await deleteBatchMessageTemplate(selectedRowKeys.value as any, publicConfig.value)
      selectedRowKeys.value = []
      await tableRef.value?.reload?.()
    },
  })
}

const handleAddReceiver = () => {
  formData.receivers.push({
    receiverType: undefined,
    receiverIds: [],
  })
}

const handleRemoveReceiver = (index: number) => {
  formData.receivers.splice(index, 1)
}

const handleReceiverUpdate = (index: number, value: any) => {
  formData.receivers[index] = value
}

const handleAddContent = () => {
  formData.contents.push({
    platform: undefined,
    contentTitle: '',
    contentTitleI18nJson: '',
    contentBody: '',
    contentBodyI18nJson: '',
    linkUrl: '',
  })
}

const handleRemoveContent = (index: number) => {
  formData.contents.splice(index, 1)
}

const handleModalOk = async () => {
  if (!validate表单()) {
    return
  }

  modalLoading.value = true
  try {
    await saveMessageTemplate(buildSavePayload())
    modalVisible.value = false
    await tableRef.value?.reload?.()
  } finally {
    modalLoading.value = false
  }
}

const handleModalCancel = () => {
  modalVisible.value = false
}
</script>

<style scoped lang="less">
.message-template-page {
  padding: 16px;
  background: var(--fx-bg-layout);
}

.receiver-item,
.content-item {
  margin-bottom: 16px;
}

.field-hint {
  color: var(--fx-text-secondary);
  font-size: 12px;
}
</style>
