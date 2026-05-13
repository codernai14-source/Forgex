<template>
  <div class="message-list-page">
    <a-card :bordered="false" class="search-card">
      <a-form layout="inline" :model="searchForm">
        <a-form-item :label="$tl('消息类型')">
          <a-select v-model:value="searchForm.messageType" :placeholder="$tl('请选择消息类型')" allow-clear style="width: 150px">
            <a-select-option value="NOTICE">{{ $tl('通知') }}</a-select-option>
            <a-select-option value="WARNING">{{ $tl('预警') }}</a-select-option>
            <a-select-option value="ALARM">{{ $tl('告警') }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item :label="$tl('状态')">
          <a-select v-model:value="searchForm.status" :placeholder="$tl('请选择状态')" allow-clear style="width: 120px">
            <a-select-option :value="0">{{ $tl('未读') }}</a-select-option>
            <a-select-option :value="1">{{ $tl('已读') }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item :label="$tl('标题')">
          <a-input v-model:value="searchForm.title" :placeholder="$tl('请输入标题')" allow-clear />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="handleSearch">
              <template #icon><SearchOutlined /></template>
              {{ $tl('查询') }}
            </a-button>
            <a-button @click="handleReset">
              <template #icon><ReloadOutlined /></template>
              {{ $tl('重置') }}
            </a-button>
            <a-button @click="handleMarkAllRead">
              <template #icon><CheckOutlined /></template>
              {{ $tl('全部已读') }}
            </a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

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
                  <span>{{ $tl('发送人') }}：{{ item.senderName }}</span>
                  <span>{{ $tl('发送时间') }}：{{ item.createTime }}</span>
                  <span v-if="item.readTime">{{ $tl('阅读时间') }}：{{ item.readTime }}</span>
                </div>
              </template>
            </a-list-item-meta>
            <template #actions>
              <a-button v-if="item.status === 0" type="link" size="small" @click.stop="handleMarkRead(item)">
                {{ $tl('标记已读') }}
              </a-button>
              <a-button v-if="item.linkUrl" type="link" size="small" @click.stop="handleGoToLink(item)">
                {{ $tl('查看详情') }}
              </a-button>
            </template>
          </a-list-item>
        </template>
      </a-list>
    </a-card>

    <a-modal
      v-model:open="detailVisible"
      :title="$tl('消息详情')"
      width="600px"
      :footer="null"
    >
      <div v-if="currentMessage" class="message-detail">
        <a-descriptions :column="1" bordered>
          <a-descriptions-item :label="$tl('消息标题')">
            {{ currentMessage.title }}
          </a-descriptions-item>
          <a-descriptions-item :label="$tl('消息类型')">
            <a-tag :color="getMessageTypeColor(currentMessage.messageType)">
              {{ getMessageTypeText(currentMessage.messageType) }}
            </a-tag>
          </a-descriptions-item>
          <a-descriptions-item :label="$tl('消息内容')">
            <div style="white-space: pre-wrap">{{ currentMessage.content }}</div>
          </a-descriptions-item>
          <a-descriptions-item :label="$tl('发送人')">
            {{ currentMessage.senderName }}
          </a-descriptions-item>
          <a-descriptions-item :label="$tl('发送时间')">
            {{ currentMessage.createTime }}
          </a-descriptions-item>
          <a-descriptions-item v-if="currentMessage.readTime" :label="$tl('阅读时间')">
            {{ currentMessage.readTime }}
          </a-descriptions-item>
          <a-descriptions-item v-if="currentMessage.linkUrl" :label="$tl('相关链接')">
            <a :href="currentMessage.linkUrl" target="_blank">{{ currentMessage.linkUrl }}</a>
          </a-descriptions-item>
        </a-descriptions>
      </div>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import {
  SearchOutlined,
  ReloadOutlined,
  CheckOutlined,
  BellOutlined,
  WarningOutlined,
  AlertOutlined,
} from '@ant-design/icons-vue'
import {
  markAllMessageRead,
  markMessageRead,
  pageMessage,
} from '@/api/message'
import { translateLegacyText } from '@/utils/legacyI18n'

const searchForm = reactive({
  messageType: undefined,
  platform: 'INTERNAL',
  status: undefined,
  title: '',
  pageNum: 1,
  pageSize: 10,
})

const dataSource = ref<any[]>([])
const loading = ref(false)
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total: number) => translateLegacyText(`共 ${total} 条`),
  onChange: (page: number, pageSize: number) => {
    pagination.current = page
    pagination.pageSize = pageSize
    loadData()
  },
})

const detailVisible = ref(false)
const currentMessage = ref<any>(null)

function getMessageTypeColor(type: string) {
  const colorMap: Record<string, string> = {
    NOTICE: '#1890ff',
    WARNING: '#faad14',
    ALARM: '#ff4d4f',
  }
  return colorMap[type] || '#1890ff'
}

function getMessageTypeText(type: string) {
  const textMap: Record<string, string> = {
    NOTICE: translateLegacyText('通知'),
    WARNING: translateLegacyText('预警'),
    ALARM: translateLegacyText('告警'),
  }
  return textMap[type] || type
}

function getMessageTypeIcon(type: string) {
  const iconMap: Record<string, any> = {
    NOTICE: BellOutlined,
    WARNING: WarningOutlined,
    ALARM: AlertOutlined,
  }
  return iconMap[type] || BellOutlined
}

async function loadData() {
  loading.value = true
  try {
    const params = {
      ...searchForm,
      pageNum: pagination.current,
      pageSize: pagination.pageSize,
    }
    const res: any = await pageMessage(params)
    dataSource.value = res.records || []
    pagination.total = res.total || 0
  } catch (error) {
    console.error('加载消息列表失败', error)
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.current = 1
  loadData()
}

function handleReset() {
  Object.assign(searchForm, {
    messageType: undefined,
    status: undefined,
    title: '',
  })
  handleSearch()
}

function handlePageChange(page: number, pageSize: number) {
  pagination.current = page
  pagination.pageSize = pageSize
  loadData()
}

async function handleItemClick(item: any) {
  currentMessage.value = item
  detailVisible.value = true

  if (item.status === 0) {
    try {
      await markMessageRead(item.id, { showSuccessMessage: false })
      item.status = 1
      item.readTime = new Date().toLocaleString()
    } catch (error) {
      console.error('标记消息已读失败', error)
    }
  }
}

async function handleMarkRead(item: any) {
  try {
    await markMessageRead(item.id)
    item.status = 1
    item.readTime = new Date().toLocaleString()
  } catch (error) {
    console.error('标记已读失败', error)
  }
}

async function handleMarkAllRead() {
  try {
    await markAllMessageRead()
    loadData()
  } catch (error) {
    console.error('全部标记已读失败', error)
  }
}

function handleGoToLink(item: any) {
  if (item.linkUrl) {
    window.open(item.linkUrl, '_blank')
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="less" src="@/styles/views/system/message/index.less"></style>
