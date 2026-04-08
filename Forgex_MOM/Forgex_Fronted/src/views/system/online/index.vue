<template>
  <div class="online-user-container">
    <fx-dynamic-table
      ref="tableRef"
      :table-code="'OnlineUserTable'"
      :show-query-form="true"
      :request="handleRequest"
      :dict-options="dictOptions"
      :row-selection="{
        selectedRowKeys: selectedRowKeys,
        onChange: handleSelectionChange
      }"
    >
      <template #toolbar>
        <a-space :size="8">
          <a-button
            v-permission="'sys:online:kickout'"
            danger
            :disabled="selectedRowKeys.length === 0"
            @click="handleBatchKickout"
          >
            批量强制下线
          </a-button>
        </a-space>
      </template>
      
      <template #ttlSeconds="{ record }">
        {{ formatTtl(record.ttlSeconds) }}
      </template>
      
      <template #action="{ record }">
        <a
          v-permission="'sys:online:kickout'"
          style="color: #ff4d4f;"
          @click="handleKickout(record.token)"
        >
          强制下线
        </a>
      </template>
    </fx-dynamic-table>
  </div>
</template>

<script setup lang="ts">
/**
 * 在线用户页面。
 *
 * 功能：
 * - 在线用户分页查询（基于后端在线会话统计）
 * - 强制下线（踢下线）
 * - 批量强制下线
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 */
import { ref } from 'vue'
import { Modal, message } from 'ant-design-vue'
import { useUserStore } from '@/stores/user'
import { kickoutOnlineUser, listOnlineUsers } from '@/api/system/online'

// 当前登录用户信息（含 tenantId）
const userStore = useUserStore()

// 选中的行 ID 列表
const selectedRowKeys = ref<string[]>([])

// 表格引用
const tableRef = ref()

// 字典配置
const dictOptions = ref({})

// 处理表格数据请求
const handleRequest = async (payload: { 
  page: { current: number; pageSize: number }; 
  query: Record<string, any>; 
  sorter?: { field?: string; order?: string } 
}) => {
  try {
    const params: any = {
      current: payload.page.current,
      size: payload.page.pageSize,
      tenantId: userStore.tenantId || 1,
      ...payload.query
    }
    
    const res: any = await listOnlineUsers(params)
    // 确保 total 是数字类型
    const total = typeof res.total === 'number' ? res.total : parseInt(String(res.total) || '0', 10)
    return { records: res.records || [], total: total }
  } catch (e) {
    console.error('查询在线用户失败', e)
    return { records: [], total: 0 }
  }
}

/**
 * 行选择变化。
 *
 * @param keys 选中的行 key 列表
 */
function handleSelectionChange(keys: string[]) {
  selectedRowKeys.value = keys
}

/**
 * 格式化 TTL 显示。
 *
 * @param ttl 剩余秒数
 * @returns 格式化后的字符串
 */
function formatTtl(ttl: any) {
  if (ttl == null) return '-'
  const n = Number(ttl)
  if (Number.isNaN(n)) return '-'
  if (n < 0) return '长期'
  const m = Math.floor(n / 60)
  const s = Math.floor(n % 60)
  if (m <= 0) return `${s}s`
  return `${m}m ${s}s`
}

/**
 * 强制下线确认与执行。
 *
 * @param token 会话 token
 */
function handleKickout(token: string) {
  if (!token) return
  Modal.confirm({
    title: '确认强制下线？',
    content: '该操作会让对应会话立即失效。',
    okText: '确定',
    cancelText: '取消',
    onOk: async () => {
      try {
        await kickoutOnlineUser({ token })
        message.success('已强制下线')
        await tableRef.value?.refresh?.()
      } catch (e) {
        console.error('强制下线失败', e)
      }
    },
  })
}

/**
 * 批量强制下线。
 */
function handleBatchKickout() {
  if (selectedRowKeys.value.length === 0) return
  Modal.confirm({
    title: '确认批量强制下线？',
    content: `将对选中的 ${selectedRowKeys.value.length} 个会话执行强制下线操作。`,
    okText: '确定',
    cancelText: '取消',
    onOk: async () => {
      try {
        const promises = selectedRowKeys.value.map(token => kickoutOnlineUser({ token }))
        await Promise.all(promises)
        message.success('批量强制下线成功')
        selectedRowKeys.value = []
        await tableRef.value?.refresh?.()
      } catch (e) {
        console.error('批量强制下线失败', e)
      }
    },
  })
}
</script>

<style scoped lang="less">
.online-user-container {
  /* 移除 padding: 20px（现在由 MainLayout 的 .fx-content-inner 统一处理） */
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
  overflow: hidden;
}
</style>
