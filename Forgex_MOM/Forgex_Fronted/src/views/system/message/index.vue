<template>
  <div class="message-list-page">
    <!-- 查询表单 -->
    <a-card :bordered="false" class="search-card">
      <a-form layout="inline" :model="searchForm">
        <a-form-item label="消息类型">
          <a-select v-model:value="searchForm.messageType" placeholder="请选择消息类型" allow-clear style="width: 150px">
            <a-select-option value="NOTICE">通知</a-select-option>
            <a-select-option value="WARNING">警告</a-select-option>
            <a-select-option value="ALARM">报警</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="searchForm.status" placeholder="请选择状态" allow-clear style="width: 120px">
            <a-select-option :value="0">未读</a-select-option>
            <a-select-option :value="1">已读</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="标题">
          <a-input v-model:value="searchForm.title" placeholder="请输入标题" allow-clear />
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
            <a-button @click="handleMarkAllRead">
              <template #icon><CheckOutlined /></template>
              全部已读
            </a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <!-- 数据列表 -->
    <a-card :bordered="false" class="list-card">
      <a-list
        :loading="loading"
        :data-source="dataSource"
        :pagination="pagination"
        @change="handlePageChange"
      >
        <template #renderItem="{ item }">
          <a-list-item
            :class="['message-item', item.status === 0 ? 'unread' : 'read']"
            @click="handleItemClick(item)"
          >
            <a-list-item-meta>
              <template #avatar>
                <a-badge :dot="item.status === 0">
                  <a-avatar :style="{ backgroundColor: getMessageTypeColor(item.messageType) }">
                    <template #icon>
                      <component :is="getMessageTypeIcon(item.messageType)" />
                    </template>
                  </a-avatar>
                </a-badge>
              </template>
              <template #title>
                <div class="message-title">
                  <span>{{ item.title }}</span>
                  <a-tag :color="getMessageTypeColor(item.messageType)" size="small">
                    {{ getMessageTypeText(item.messageType) }}
                  </a-tag>
                </div>
              </template>
              <template #description>
                <div class="message-content">{{ item.content }}</div>
                <div class="message-meta">
                  <span>发送人：{{ item.senderName }}</span>
                  <span>发送时间：{{ item.createTime }}</span>
                  <span v-if="item.readTime">阅读时间：{{ item.readTime }}</span>
                </div>
              </template>
            </a-list-item-meta>
            <template #actions>
              <a-button v-if="item.status === 0" type="link" size="small" @click.stop="handleMarkRead(item)">
                标记已读
              </a-button>
              <a-button v-if="item.linkUrl" type="link" size="small" @click.stop="handleGoToLink(item)">
                查看详情
              </a-button>
            </template>
          </a-list-item>
        </template>
      </a-list>
    </a-card>

    <!-- 消息详情弹窗 -->
    <a-modal
      v-model:open="detailVisible"
      title="消息详情"
      width="600px"
      :footer="null"
    >
      <div v-if="currentMessage" class="message-detail">
        <a-descriptions :column="1" bordered>
          <a-descriptions-item label="消息标题">
            {{ currentMessage.title }}
          </a-descriptions-item>
          <a-descriptions-item label="消息类型">
            <a-tag :color="getMessageTypeColor(currentMessage.messageType)">
              {{ getMessageTypeText(currentMessage.messageType) }}
            </a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="消息内容">
            <div style="white-space: pre-wrap">{{ currentMessage.content }}</div>
          </a-descriptions-item>
          <a-descriptions-item label="发送人">
            {{ currentMessage.senderName }}
          </a-descriptions-item>
          <a-descriptions-item label="发送时间">
            {{ currentMessage.createTime }}
          </a-descriptions-item>
          <a-descriptions-item v-if="currentMessage.readTime" label="阅读时间">
            {{ currentMessage.readTime }}
          </a-descriptions-item>
          <a-descriptions-item v-if="currentMessage.linkUrl" label="相关链接">
            <a :href="currentMessage.linkUrl" target="_blank">{{ currentMessage.linkUrl }}</a>
          </a-descriptions-item>
        </a-descriptions>
      </div>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import {
  SearchOutlined,
  ReloadOutlined,
  CheckOutlined,
  BellOutlined,
  WarningOutlined,
  AlertOutlined
} from '@ant-design/icons-vue'
import {
  pageMessage,
  markMessageRead,
  markAllMessageRead
} from '../../api/message'

// 查询表单
const searchForm = reactive({
  messageType: undefined,
  platform: 'INTERNAL',
  status: undefined,
  title: '',
  pageNum: 1,
  pageSize: 10
})

// 列表数据
const dataSource = ref([])
const loading = ref(false)
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total: number) => `共 ${total} 条`,
  onChange: (page: number, pageSize: number) => {
    pagination.current = page
    pagination.pageSize = pageSize
    loadData()
  }
})

// 详情弹窗
const detailVisible = ref(false)
const currentMessage = ref<any>(null)

// 获取消息类型颜色
const getMessageTypeColor = (type: string) => {
  const colorMap: Record<string, string> = {
    NOTICE: '#1890ff',
    WARNING: '#faad14',
    ALARM: '#ff4d4f'
  }
  return colorMap[type] || '#1890ff'
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

// 获取消息类型图标
const getMessageTypeIcon = (type: string) => {
  const iconMap: Record<string, any> = {
    NOTICE: BellOutlined,
    WARNING: WarningOutlined,
    ALARM: AlertOutlined
  }
  return iconMap[type] || BellOutlined
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
    const res = await pageMessage(params)
    dataSource.value = res.records || []
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
    messageType: undefined,
    status: undefined,
    title: ''
  })
  handleSearch()
}

// 分页变化
const handlePageChange = (page: number, pageSize: number) => {
  pagination.current = page
  pagination.pageSize = pageSize
  loadData()
}

// 列表项点击
const handleItemClick = async (item: any) => {
  currentMessage.value = item
  detailVisible.value = true
  
  // 如果是未读消息，标记为已读
  if (item.status === 0) {
    try {
      await markMessageRead(item.id)
      item.status = 1
      item.readTime = new Date().toLocaleString()
    } catch (error) {
      console.error('标记已读失败:', error)
    }
  }
}

// 标记已读
const handleMarkRead = async (item: any) => {
  try {
    await markMessageRead(item.id)
    message.success('标记成功')
    item.status = 1
    item.readTime = new Date().toLocaleString()
  } catch (error) {
    message.error('标记失败')
  }
}

// 全部已读
const handleMarkAllRead = async () => {
  try {
    await markAllMessageRead()
    message.success('全部标记成功')
    loadData()
  } catch (error) {
    message.error('标记失败')
  }
}

// 跳转链接
const handleGoToLink = (item: any) => {
  if (item.linkUrl) {
    window.open(item.linkUrl, '_blank')
  }
}

// 初始化
onMounted(() => {
  loadData()
})
</script>

<style scoped lang="less">
.message-list-page {
  padding: 16px;
}

.search-card {
  margin-bottom: 16px;
}

.list-card {
  .message-item {
    cursor: pointer;
    transition: all 0.3s;

    &.unread {
      background-color: #f0f7ff;
    }

    &:hover {
      background-color: #e6f7ff;
    }

    .message-title {
      display: flex;
      align-items: center;
      gap: 8px;
      font-weight: 500;
    }

    .message-content {
      color: rgba(0, 0, 0, 0.65);
      margin-bottom: 8px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .message-meta {
      display: flex;
      gap: 16px;
      font-size: 12px;
      color: rgba(0, 0, 0, 0.45);
    }
  }
}

.message-detail {
  :deep(.ant-descriptions-item-label) {
    width: 100px;
  }
}
</style>



