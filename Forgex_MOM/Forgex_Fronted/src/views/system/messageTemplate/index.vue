<template>
  <div class="message-template-page">
    <!-- 查询表单 -->
    <a-card :bordered="false" class="search-card">
      <a-form layout="inline" :model="searchForm">
        <a-form-item label="模板编号">
          <a-input v-model:value="searchForm.templateCode" placeholder="请输入模板编号" allow-clear />
        </a-form-item>
        <a-form-item label="模板名称">
          <a-input v-model:value="searchForm.templateName" placeholder="请输入模板名称" allow-clear />
        </a-form-item>
        <a-form-item label="消息类型">
          <a-select v-model:value="searchForm.messageType" placeholder="请选择消息类型" allow-clear style="width: 150px">
            <a-select-option value="NOTICE">通知</a-select-option>
            <a-select-option value="WARNING">警告</a-select-option>
            <a-select-option value="ALARM">报警</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="searchForm.status" placeholder="请选择状态" allow-clear style="width: 120px">
            <a-select-option :value="true">启用</a-select-option>
            <a-select-option :value="false">禁用</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="handleSearch">
              <template #icon><SearchOutlined /></template>
              查询
            </a-button>
            <a-button @click="handleReset">
              <template #icon><ReloadOutlined /></template>
              重置
            </a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <!-- 操作按钮 -->
    <a-card :bordered="false" class="table-card">
      <div class="table-toolbar">
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
      </div>

      <!-- 数据表格 -->
      <a-table
        :columns="columns"
        :data-source="dataSource"
        :loading="loading"
        :pagination="pagination"
        :row-selection="{ selectedRowKeys, onChange: onSelectChange }"
        row-key="id"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'messageType'">
            <a-tag :color="getMessageTypeColor(record.messageType)">
              {{ getMessageTypeText(record.messageType) }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'status'">
            <a-tag :color="record.status ? 'success' : 'default'">
              {{ record.status ? '启用' : '禁用' }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="handleEdit(record)">编辑</a-button>
              <a-button type="link" size="small" danger @click="handleDelete(record)">删除</a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

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
              <a-input v-model:value="formData.templateVersion" placeholder="请输入模板版本，默认1.0" />
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
          <div v-for="(receiver, index) in formData.receivers" :key="index" class="receiver-item">
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
            <a-button type="primary" @click="handlePreview" :disabled="formData.contents.length === 0">
              <template #icon><EyeOutlined /></template>
              预览效果
            </a-button>
          </a-space>
          <div v-for="(content, index) in formData.contents" :key="index" class="content-item">
            <a-card size="small">
              <template #title>
                <span>{{ getPlatformName(content.platform) || `内容配置 ${index + 1}` }}</span>
              </template>
              <template #extra>
                <a-button type="link" danger size="small" @click="handleRemoveContent(index)">删除</a-button>
              </template>
              <a-form :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }">
                <a-form-item label="消息平台" required>
                  <a-select v-model:value="content.platform" placeholder="请选择消息平台">
                    <a-select-option value="INTERNAL">站内消息</a-select-option>
                    <a-select-option value="WECHAT">企业微信</a-select-option>
                    <a-select-option value="SMS">短信</a-select-option>
                    <a-select-option value="EMAIL">邮件</a-select-option>
                  </a-select>
                </a-form-item>
                <a-form-item label="消息标题">
                  <I18nInput
                    v-model="content.contentTitleI18nJson"
                    mode="simple"
                    placeholder="请输入消息标题（可选）"
                  />
                  <template #extra>
                    <span style="color: #999; font-size: 12px;">
                      支持占位符，如 ${userName}、${tenantName} 等
                    </span>
                  </template>
                </a-form-item>
                <a-form-item label="消息内容" required>
                  <PlaceholderInput
                    v-model="content.contentBodyI18nJson"
                    placeholder="请输入消息内容"
                    :rows="6"
                  />
                </a-form-item>
                <a-form-item label="跳转链接">
                  <a-input v-model:value="content.linkUrl" placeholder="请输入跳转链接（可选）" />
                  <template #extra>
                    <span style="color: #999; font-size: 12px;">
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
import { ref, reactive, onMounted } from 'vue'
import { message, Modal } from 'ant-design-vue'
import {
  SearchOutlined,
  ReloadOutlined,
  PlusOutlined,
  DeleteOutlined,
  EyeOutlined
} from '@ant-design/icons-vue'
import I18nInput from '@/components/common/I18nInput.vue'
import PlaceholderInput from '@/components/common/PlaceholderInput.vue'
import ReceiverSelector from '@/components/common/ReceiverSelector.vue'
import TemplatePreview from '@/components/common/TemplatePreview.vue'
import { getI18nValue } from '@/utils/i18n'
import {
  pageMessageTemplate,
  getMessageTemplate,
  saveMessageTemplate,
  deleteMessageTemplate,
  deleteBatchMessageTemplate
} from '../../api/message'

// 查询表单
const searchForm = reactive({
  templateCode: '',
  templateName: '',
  messageType: undefined,
  status: undefined,
  pageNum: 1,
  pageSize: 10
})

// 表格列定义
const columns = [
  { title: '模板编号', dataIndex: 'templateCode', key: 'templateCode' },
  { title: '模板名称', dataIndex: 'displayName', key: 'displayName' },
  { title: '模板版本', dataIndex: 'templateVersion', key: 'templateVersion' },
  { title: '消息类型', dataIndex: 'messageType', key: 'messageType' },
  { title: '状态', dataIndex: 'status', key: 'status' },
  { title: '创建时间', dataIndex: 'createTime', key: 'createTime' },
  { title: '操作', key: 'action', width: 180 }
]

// 表格数据
const dataSource = ref([])
const loading = ref(false)
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total: number) => `共 ${total} 条`
})

// 选中的行
const selectedRowKeys = ref<number[]>([])

// 弹窗相关
const modalVisible = ref(false)
const modalTitle = ref('新增消息模板')
const modalLoading = ref(false)
const activeTab = ref('basic')

// 预览相关
const previewVisible = ref(false)

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

// 查询数据
const loadData = async () => {
  loading.value = true
  try {
    const params = {
      ...searchForm,
      pageNum: pagination.current,
      pageSize: pagination.pageSize
    }
    const res = await pageMessageTemplate(params)
    // 处理多语言显示
    const processedRecords = (res.records || []).map((item: any) => ({
      ...item,
      displayName: getI18nValue(item.templateNameI18nJson, item.templateName)
    }))
    dataSource.value = processedRecords
    pagination.total = res.total || 0
  } catch (error) {
    console.error('查询失败:', error)
  } finally {
    loading.value = false
  }
}

// 查询
const handleSearch = () => {
  pagination.current = 1
  loadData()
}

// 重置
const handleReset = () => {
  Object.assign(searchForm, {
    templateCode: '',
    templateName: '',
    messageType: undefined,
    status: undefined
  })
  handleSearch()
}

// 表格变化
const handleTableChange = (pag: any) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  loadData()
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
  try {
    const res = await getMessageTemplate(record.id)
    Object.assign(formData, res)
    modalVisible.value = true
  } catch (error) {
    message.error('获取详情失败')
  }
}

// 删除
const handleDelete = (record: any) => {
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除模板"${record.templateName}"吗？`,
    onOk: async () => {
      try {
        await deleteMessageTemplate(record.id)
        message.success('删除成功')
        loadData()
      } catch (error) {
        message.error('删除失败')
      }
    }
  })
}

// 批量删除
const handleBatchDelete = () => {
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除选中的 ${selectedRowKeys.value.length} 条记录吗？`,
    onOk: async () => {
      try {
        await deleteBatchMessageTemplate(selectedRowKeys.value)
        message.success('删除成功')
        selectedRowKeys.value = []
        loadData()
      } catch (error) {
        message.error('删除失败')
      }
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
    await saveMessageTemplate(formData)
    message.success('保存成功')
    modalVisible.value = false
    loadData()
  } catch (error) {
    message.error('保存失败')
  } finally {
    modalLoading.value = false
  }
}

// 弹窗取消
const handleModalCancel = () => {
  modalVisible.value = false
}

// 初始化
onMounted(() => {
  loadData()
})
</script>

<style scoped lang="less">
.message-template-page {
  padding: 16px;
}

.search-card {
  margin-bottom: 16px;
}

.table-card {
  .table-toolbar {
    margin-bottom: 16px;
  }
}

.receiver-item,
.content-item {
  margin-bottom: 16px;

  &:last-child {
    margin-bottom: 0;
  }
  
  :deep(.ant-card) {
    border: 1px solid #e8e8e8;
    transition: all 0.3s;
    
    &:hover {
      border-color: #1890ff;
      box-shadow: 0 2px 8px rgba(24, 144, 255, 0.15);
    }
    
    .ant-card-head {
      background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
      border-bottom: 1px solid #e8e8e8;
      
      .ant-card-head-title {
        font-weight: 600;
        color: #333;
      }
    }
  }
}

:deep(.ant-modal) {
  .ant-tabs {
    .ant-tabs-nav {
      margin-bottom: 24px;
      
      .ant-tabs-tab {
        font-size: 15px;
        padding: 12px 20px;
        
        &.ant-tabs-tab-active {
          font-weight: 600;
        }
      }
    }
  }
}
</style>

