<template>
  <div class="message-template-page">
    <FxDynamicTable
      ref="tableRef"
      table-code="MessageTemplateTable"
      :request="handleRequest"
      :dict-options="dictOptions"
      :row-selection="{ selectedRowKeys, onChange: onSelectChange }"
      row-key="id"
    >
      <template #toolbar>
        <a-space>
          <a-button type="primary" @click="handleAdd">
            <template #icon><PlusOutlined /></template>
            新增
          </a-button>
          <a-button danger :disabled="selectedRowKeys.length === 0" @click="handleBatchDelete">
            <template #icon><DeleteOutlined /></template>
            批量删除
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
          {{ record.status ? '启用' : '禁用' }}
        </a-tag>
      </template>

      <template #action="{ record }">
        <a-space>
          <a-button type="link" size="small" @click="handleEdit(record)">编辑</a-button>
          <a-button type="link" size="small" danger @click="handleDelete(record)">删除</a-button>
        </a-space>
      </template>
    </FxDynamicTable>

    <!-- 新增/编辑弹窗 -->
    <a-modal
      v-model:open="modalVisible"
      :title="modalTitle"
      width="900px"
      :confirm-loading="modalLoading"
      @ok="handleModalOk"
      @cancel="handleModalCancel"
    >
      <a-tabs v-model:activeKey="activeTab">
        <!-- Tab1: 模板主信息 -->
        <a-tab-pane key="basic" tab="模板主信息">
          <a-form :model="formData" :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }">
            <a-form-item label="模板编号" required>
              <a-input v-model:value="formData.templateCode" placeholder="请输入模板编号" />
            </a-form-item>
            <a-form-item label="模板名称" required>
              <I18nInput
                v-model="formData.templateNameI18nJson"
                mode="simple"
                placeholder="请输入模板名称"
              />
            </a-form-item>
            <a-form-item label="模板版本">
              <a-input v-model:value="formData.templateVersion" placeholder="请输入模板版本，默认 1.0" />
            </a-form-item>
            <a-form-item label="消息类型" required>
              <a-select v-model:value="formData.messageType" placeholder="请选择消息类型">
                <a-select-option value="NOTICE">通知</a-select-option>
                <a-select-option value="WARNING">警告</a-select-option>
                <a-select-option value="ALARM">报警</a-select-option>
              </a-select>
            </a-form-item>
            <a-form-item label="状态">
              <a-switch v-model:checked="formData.status" checked-children="启用" un-checked-children="禁用" />
            </a-form-item>
            <a-form-item label="备注">
              <a-textarea v-model:value="formData.remark" placeholder="请输入备注" :rows="3" />
            </a-form-item>
          </a-form>
        </a-tab-pane>

        <!-- Tab2: 接收人配置 -->
        <a-tab-pane key="receiver" tab="接收人配置">
          <a-alert
            message="接收人配置说明"
            description="可以配置多个接收人规则，系统会根据规则自动确定消息接收人。支持指定用户、角色、部门、职位等多种方式。"
            type="info"
            show-icon
            style="margin-bottom: 16px"
          />
          <a-button type="dashed" block @click="handleAddReceiver" style="margin-bottom: 16px">
            <template #icon><PlusOutlined /></template>
            添加接收人配置
          </a-button>
          <div v-for="(receiver, index) in formData.receivers" :key="index" class="receiver-items">
            <a-card size="small">
              <template #title>
                <span>接收人配置 {{ index + 1 }}</span>
              </template>
              <template #extra>
                <a-button type="link" danger size="small" @click="handleRemoveReceiver(index)">删除</a-button>
              </template>
              <ReceiverSelector
                v-model="formData.receivers[index]"
                @update:modelValue="(val) => handleReceiverUpdate(index, val)"
              />
            </a-card>
          </div>
          <a-empty v-if="formData.receivers.length === 0" description="暂无接收人配置，请点击上方按钮添加" />
        </a-tab-pane>

        <!-- Tab3: 模板内容配置 -->
        <a-tab-pane key="content" tab="模板内容配置">
          <a-space style="margin-bottom: 16px; width: 100%; justify-content: space-between;">
            <a-button type="dashed" @click="handleAddContent">
              <template #icon><PlusOutlined /></template>
              添加内容配置
            </a-button>
            <a-button @click="handleTestSend" :loading="testSendLoading" :disabled="formData.contents.length === 0">
              <template #icon><SendOutlined /></template>
              试发送给自己
            </a-button>
            <a-button type="primary" @click="handlePreview" :disabled="formData.contents.length === 0">
              <template #icon><EyeOutlined /></template>
              预览效果
            </a-button>
          </a-space>
          <div v-for="(content, index) in formData.contents" :key="index" class="content-item">
            <a-card size="small">
              <template #title>
                <span>{{ getPlatformName(formData.contents[index].platform) || `内容配置 ${index + 1}` }}</span>
              </template>
              <template #extra>
                <a-button type="link" danger size="small" @click="handleRemoveContent(index)">删除</a-button>
              </template>
              <a-form :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }">
                <a-form-item label="消息平台" required>
                  <a-select v-model:value="formData.contents[index].platform" placeholder="请选择消息平台">
                    <a-select-option value="INTERNAL">站内消息</a-select-option>
                    <a-select-option value="WECHAT">企业微信</a-select-option>
                    <a-select-option value="SMS">短信</a-select-option>
                    <a-select-option value="EMAIL">邮件</a-select-option>
                  </a-select>
                </a-form-item>
                <a-form-item label="消息标题">
                  <I18nInput
                    v-model="formData.contents[index].contentTitleI18nJson"
                    mode="simple"
                    placeholder="请输入消息标题（可选）"
                  />
                  <template #extra>
                    <span class="field-hint">
                      支持占位符，如 ${userName}、${tenantName} 等
                    </span>
                  </template>
                </a-form-item>
                <a-form-item label="消息内容" required>
                  <PlaceholderInput
                    v-model="formData.contents[index].contentBodyI18nJson"
                    placeholder="请输入消息内容"
                    :rows="6"
                  />
                </a-form-item>
                <a-form-item label="跳转链接">
                  <a-input v-model:value="formData.contents[index].linkUrl" placeholder="请输入跳转链接（可选）" />
                  <template #extra>
                    <span class="field-hint">
                      用户点击消息后跳转的链接地址
                    </span>
                  </template>
                </a-form-item>
              </a-form>
            </a-card>
          </div>
          <a-empty v-if="formData.contents.length === 0" description="暂无内容配置，请点击上方按钮添加" />
        </a-tab-pane>
      </a-tabs>
    </a-modal>

    <!-- 模板预览弹窗 -->
    <TemplatePreview
      v-model:visible="previewVisible"
      :contents="formData.contents"
      :message-type="formData.messageType || 'NOTICE'"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { message, Modal } from 'ant-design-vue'
import {
  PlusOutlined,
  DeleteOutlined,
  EyeOutlined,
  SendOutlined
} from '@ant-design/icons-vue'
import I18nInput from '@/components/common/I18nInput.vue'
import PlaceholderInput from '@/components/common/PlaceholderInput.vue'
import ReceiverSelector from '@/components/common/ReceiverSelector.vue'
import TemplatePreview from '@/components/common/TemplatePreview.vue'
import { getI18nValue, toI18nJson } from '@/utils/i18n'
import {
  pageMessageTemplate,
  getMessageTemplate,
  saveMessageTemplate,
  deleteMessageTemplate,
  deleteBatchMessageTemplate
} from '@/api/message'
import { sendMessage as sendSystemMessage } from '@/api/system/message'
import FxDynamicTable from '@/components/common/FxDynamicTable.vue'
import { useUserStore } from '@/stores/user'

const tableRef = ref<any>()
const userStore = useUserStore()

const dictOptions = {
  messageType: [
    { label: '通知', value: 'NOTICE' },
    { label: '警告', value: 'WARNING' },
    { label: '报警', value: 'ALARM' },
  ],
  status: [
    { label: '启用', value: true },
    { label: '禁用', value: false },
  ],
}

// 选中的行
const selectedRowKeys = ref<number[]>([])

// 弹窗相关
const modalVisible = ref(false)
const modalTitle = ref('新增消息模板')
const modalLoading = ref(false)
const activeTab = ref('basic')

// 预览相关
const previewVisible = ref(false)
const testSendLoading = ref(false)

// 表单数据
const formData = reactive({
  id: undefined,
  templateCode: '',
  templateName: '',
  templateNameI18nJson: '',
  templateVersion: '1.0',
  messageType: undefined,
  status: true,
  remark: '',
  receivers: [] as any[],
  contents: [] as any[]
})

const resolveI18nText = (i18nJson?: string, fallback: string = '') => {
  return getI18nValue(i18nJson, fallback, 'zh-CN').trim()
}

const ensureI18nJsonValue = (i18nJson?: string, fallback: string = '') => {
  if (typeof i18nJson === 'string' && i18nJson.trim()) {
    return i18nJson
  }
  const text = fallback.trim()
  return text ? toI18nJson({ 'zh-CN': text }) : ''
}

const normalizeFormData = (data: any) => {
  const next = data || {}
  return {
    ...next,
    templateNameI18nJson: ensureI18nJsonValue(next.templateNameI18nJson, next.templateName || ''),
    receivers: Array.isArray(next.receivers) ? next.receivers : [],
    contents: Array.isArray(next.contents)
      ? next.contents.map((content: any) => ({
          ...content,
          contentTitleI18nJson: ensureI18nJsonValue(content?.contentTitleI18nJson, content?.contentTitle || ''),
          contentBodyI18nJson: ensureI18nJsonValue(content?.contentBodyI18nJson, content?.contentBody || '')
        }))
      : []
  }
}

const buildSavePayload = () => {
  return {
    ...formData,
    templateName: resolveI18nText(formData.templateNameI18nJson, formData.templateName),
    templateNameI18nJson: ensureI18nJsonValue(formData.templateNameI18nJson, formData.templateName),
    receivers: formData.receivers.map((receiver: any) => ({
      ...receiver,
      receiverIds: Array.isArray(receiver.receiverIds) ? receiver.receiverIds : []
    })),
    contents: formData.contents.map((content: any) => ({
      ...content,
      contentTitle: resolveI18nText(content.contentTitleI18nJson, content.contentTitle),
      contentTitleI18nJson: ensureI18nJsonValue(content.contentTitleI18nJson, content.contentTitle),
      contentBody: resolveI18nText(content.contentBodyI18nJson, content.contentBody),
      contentBodyI18nJson: ensureI18nJsonValue(content.contentBodyI18nJson, content.contentBody),
      linkUrl: content.linkUrl || ''
    }))
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
  return template.replace(/\$\{([^}]+)\}/g, (_, key: string) => values[key] ?? '')
}

const ensureCurrentUserId = async () => {
  // 如果已经有用户 ID，直接返回
  if (typeof userStore.userInfo?.id === 'number') {
    return userStore.userInfo.id
  }
  
  // 尝试从 sessionStorage 恢复
  await userStore.restoreFromSession()
  
  // 再次检查是否有用户 ID
  if (typeof userStore.userInfo?.id === 'number') {
    return userStore.userInfo.id
  }
  
  // 如果还是没有，尝试从 sessionStorage 获取基本的 account 信息
  const account = sessionStorage.getItem('account')
  if (account) {
    // 使用 account 作为临时 ID（转换为数字）
    const tempId = parseInt(account.replace(/\D/g, '') || '0', 10)
    if (tempId > 0) {
      return tempId
    }
  }
  
  // 如果所有方法都失败，返回 undefined
  return undefined
}

// 表单验证规则
const validateForm = () => {
  if (!formData.templateCode || !formData.templateCode.trim()) {
    message.error('请输入模板编号')
    activeTab.value = 'basic'
    return false
  }
  
  if (!formData.templateNameI18nJson || !formData.templateNameI18nJson.trim()) {
    message.error('请输入模板名称')
    activeTab.value = 'basic'
    return false
  }
  
  if (!formData.messageType) {
    message.error('请选择消息类型')
    activeTab.value = 'basic'
    return false
  }
  
  if (formData.receivers.length === 0) {
    message.error('请至少添加一个接收人配置')
    activeTab.value = 'receiver'
    return false
  }
  
  // 验证接收人配置
  for (let i = 0; i < formData.receivers.length; i++) {
    const receiver = formData.receivers[i]
    if (!receiver.receiverType) {
      message.error(`接收人配置 ${i + 1}：请选择接收类型`)
      activeTab.value = 'receiver'
      return false
    }
    if (!receiver.receiverIds || receiver.receiverIds.length === 0) {
      message.error(`接收人配置 ${i + 1}：请选择接收人`)
      activeTab.value = 'receiver'
      return false
    }
  }
  
  if (formData.contents.length === 0) {
    message.error('请至少添加一个内容配置')
    activeTab.value = 'content'
    return false
  }
  
  // 验证内容配置
  for (let i = 0; i < formData.contents.length; i++) {
    const content = formData.contents[i]
    if (!content.platform) {
      message.error(`内容配置 ${i + 1}：请选择消息平台`)
      activeTab.value = 'content'
      return false
    }
    if (!content.contentBodyI18nJson || !content.contentBodyI18nJson.trim()) {
      message.error(`内容配置 ${i + 1}：请输入消息内容`)
      activeTab.value = 'content'
      return false
    }
  }
  
  return true
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

// 打开预览
const handlePreview = () => {
  if (formData.contents.length === 0) {
    message.warning('请先添加内容配置')
    return
  }
  previewVisible.value = true
}

const handleTestSend = async () => {
  const internalContent = formData.contents.find((item: any) => item.platform === 'INTERNAL')
  if (!internalContent) {
    activeTab.value = 'content'
    message.warning('请先配置站内消息内容后再试发送')
    return
  }

  // 确保用户信息已加载
  const receiverUserId = await ensureCurrentUserId()
  if (!receiverUserId) {
    // 尝试从 sessionStorage 直接获取 account 作为备用方案
    const account = sessionStorage.getItem('account')
    if (!account) {
      message.error('未找到用户信息，请重新登录')
      return
    }
    // 如果 account 存在但没有 id，使用 account 本身
    message.info(`使用账号 ${account} 发送测试消息`)
  }

  const rawTitle = resolveI18nText(internalContent.contentTitleI18nJson, internalContent.contentTitle)
  const rawBody = resolveI18nText(internalContent.contentBodyI18nJson, internalContent.contentBody)
  const fallbackTitle = resolveI18nText(formData.templateNameI18nJson, formData.templateName) || '消息模板试发送'

  const placeholderValues: Record<string, string> = {
    userName: userStore.userInfo?.username || userStore.userInfo?.account || sessionStorage.getItem('account') || '当前用户',
    userAccount: userStore.userInfo?.account || sessionStorage.getItem('account') || 'current.user',
    tenantName: userStore.userInfo?.tenantName || '当前租户',
    currentTime: formatCurrentTime(),
    title: fallbackTitle,
    content: '这是一条消息模板的试发送消息',
    linkUrl: internalContent.linkUrl || '/workspace'
  }

  const title = renderPlaceholderText(rawTitle, placeholderValues) || fallbackTitle
  const content = renderPlaceholderText(rawBody, { ...placeholderValues, title }) || placeholderValues.content
  const linkUrl = renderPlaceholderText(internalContent.linkUrl || '', { ...placeholderValues, title, content })

  testSendLoading.value = true
  try {
    await sendSystemMessage({
      receiverUserId: receiverUserId || receiverUserId,
      scope: 'INTERNAL',
      messageType: formData.messageType || 'NOTICE',
      title,
      content,
      linkUrl: linkUrl || undefined,
      bizType: 'MESSAGE_TEMPLATE_TEST',
    })
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

// 选择变化
const onSelectChange = (keys: number[]) => {
  selectedRowKeys.value = keys
}

// 新增
const handleAdd = () => {
  modalTitle.value = '新增消息模板'
  activeTab.value = 'basic'
  Object.assign(formData, {
    id: undefined,
    templateCode: '',
    templateName: '',
    templateNameI18nJson: '',
    templateVersion: '1.0',
    messageType: undefined,
    status: true,
    remark: '',
    receivers: [],
    contents: []
  })
  modalVisible.value = true
}

// 编辑
const handleEdit = async (record: any) => {
  modalTitle.value = '编辑消息模板'
  activeTab.value = 'basic'
  const res = await getMessageTemplate(record.id)
  Object.assign(formData, normalizeFormData(res))
  modalVisible.value = true
}

// 删除
const handleDelete = (record: any) => {
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除模板"${record.templateName}"吗？`,
    onOk: async () => {
      await deleteMessageTemplate(record.id)
      await tableRef.value?.reload?.()
    }
  })
}

// 批量删除
const handleBatchDelete = () => {
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除选中的 ${selectedRowKeys.value.length} 条记录吗？`,
    onOk: async () => {
      await deleteBatchMessageTemplate(selectedRowKeys.value)
      selectedRowKeys.value = []
      await tableRef.value?.reload?.()
    }
  })
}

// 添加接收人配置
const handleAddReceiver = () => {
  formData.receivers.push({
    receiverType: undefined,
    receiverIds: []
  })
}

// 移除接收人配置
const handleRemoveReceiver = (index: number) => {
  formData.receivers.splice(index, 1)
}

// 更新接收人配置
const handleReceiverUpdate = (index: number, value: any) => {
  formData.receivers[index] = value
}

// 添加内容配置
const handleAddContent = () => {
  formData.contents.push({
    platform: undefined,
    contentTitle: '',
    contentTitleI18nJson: '',
    contentBody: '',
    contentBodyI18nJson: '',
    linkUrl: ''
  })
}

// 移除内容配置
const handleRemoveContent = (index: number) => {
  formData.contents.splice(index, 1)
}

// 获取平台名称
const getPlatformName = (platform: string) => {
  const nameMap: Record<string, string> = {
    INTERNAL: '站内消息',
    WECHAT: '企业微信',
    SMS: '短信',
    EMAIL: '邮件'
  }
  return nameMap[platform] || ''
}

// 弹窗确定
const handleModalOk = async () => {
  // 表单验证
  if (!validateForm()) {
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

// 弹窗取消
const handleModalCancel = () => {
  modalVisible.value = false
}
</script>

<style scoped lang="less">
.message-template-page {
  padding: 16px;
  background: var(--fx-bg-layout);
  color: var(--fx-text-primary);
}

.search-card {
  margin-bottom: 16px;
}

.table-card {
  .table-toolbar {
    margin-bottom: 16px;
  }
}

.field-hint {
  color: var(--fx-text-secondary);
  font-size: 12px;
}

.receiver-items,
.content-item {
  margin-bottom: 16px;

  &:last-child {
    margin-bottom: 0;
  }
  
  :deep(.ant-card) {
    // 使用更深的背景色来区分层次
    background: var(--fx-bg-container);
    border: 1px solid var(--fx-border-color);
    box-shadow: var(--fx-shadow-secondary);
    transition: border-color 0.3s ease, box-shadow 0.3s ease, transform 0.3s ease;
    
    &:hover {
      border-color: var(--fx-primary);
      box-shadow: var(--fx-shadow);
      transform: translateY(-1px);
    }
    
    .ant-card-head {
      // 表头使用更深的填充色
      background: var(--fx-fill-secondary);
      border-bottom: 1px solid var(--fx-border-color);
      
      .ant-card-head-title {
        font-weight: 600;
        color: var(--fx-text-primary);
      }
    }

    .ant-card-body {
      // 内容区域使用与卡片一致的容器背景色
      background: var(--fx-bg-container);
      color: var(--fx-text-primary);
    }
  }
}

:deep(.ant-modal) {
  background: var(--fx-bg-elevated);
  color: var(--fx-text-primary);
  
  .ant-modal-content {
    background: var(--fx-bg-elevated);
    color: var(--fx-text-primary);
  }
  
  .ant-modal-header {
    background: var(--fx-bg-elevated);
    color: var(--fx-text-primary);
    border-bottom: 1px solid var(--fx-border-color);
  }
  
  .ant-modal-footer {
    border-top: 1px solid var(--fx-border-color);
  }
  
  .ant-tabs {
    .ant-tabs-nav {
      margin-bottom: 24px;
      
      .ant-tabs-tab {
        font-size: 15px;
        padding: 12px 20px;
        color: var(--fx-text-secondary);
        
        &.ant-tabs-tab-active {
          font-weight: 600;
          color: var(--fx-text-primary);
        }
      }
    }
  }
  
  // 优化表单字段提示文本
  .field-hint {
    color: var(--fx-text-secondary);
    font-size: 12px;
  }
  
  // 优化内容配置区域的占位符工具栏
  :deep(.placeholder-input) {
    .placeholder-toolbar {
      background: var(--fx-fill-secondary);
      border-color: var(--fx-border-color);
      
      .toolbar-label {
        color: var(--fx-text-secondary);
      }
    }
    
    .placeholder-preview {
      background: var(--fx-bg-container);
      border-color: var(--fx-border-color);
      
      .preview-label {
        color: var(--fx-text-secondary);
      }
      
      .preview-content {
        color: var(--fx-text-primary);
      }
    }
  }
}
</style>
