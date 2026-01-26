<template>
  <div class="online-user-container">
    <a-card :bordered="false" class="online-table-card">
      <fx-dynamic-table
        ref="tableRef"
        :table-code="'OnlineUserTable'"
        :request="handleRequest"
        :fallback-config="fallbackConfig"
        :dict-options="dictOptions"
        :loading="loading"
        row-key="token"
      >
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
    </a-card>
  </div>
</template>

<script setup lang="ts">
/**
 * 在线用户页面。
 *
 * 功能：
 * - 在线用户分页查询（基于后端在线会话统计）
 * - 强制下线（踢下线）
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 */
import { ref, reactive, onMounted } from 'vue'
import { Modal, message } from 'ant-design-vue'
import { useUserStore } from '@/stores/user'
import { kickoutOnlineUser, listOnlineUsers } from '@/api/system/online'

// 当前登录用户信息（含 tenantId）
const userStore = useUserStore()

// 查询表单
const queryForm = reactive({
  account: '',
})

// 表格相关
const tableRef = ref()
const loading = ref(false)

// fallback配置
const fallbackConfig = ref({
  columns: [
    { title: '账号', dataIndex: 'account', key: 'account', width: 140 },
    { title: '用户ID', dataIndex: 'userId', key: 'userId', width: 110 },
    { title: '租户ID', dataIndex: 'tenantId', key: 'tenantId', width: 110 },
    { title: '最后登录时间', dataIndex: 'lastLoginTime', key: 'lastLoginTime', width: 180 },
    { title: '最后登录IP', dataIndex: 'lastLoginIp', key: 'lastLoginIp', width: 150 },
    { title: '最后登录地区', dataIndex: 'lastLoginRegion', key: 'lastLoginRegion', width: 150 },
    { title: '会话剩余', key: 'ttlSeconds', width: 120 },
    { title: '操作', key: 'action', width: 110, fixed: 'right' },
  ]
})

// 字典配置
const dictOptions = ref({})

// 处理表格数据请求
const handleRequest = async (payload: { 
  page: { current: number; pageSize: number }; 
  query: Record<string, any>; 
  sorter?: { field?: string; order?: string } 
}) => {
  loading.value = true
  try {
    const params: any = {
      current: payload.page.current,
      size: payload.page.pageSize,
      account: queryForm.account || undefined,
      tenantId: userStore.tenantId || 1,
      ...payload.query
    }
    
    const res: any = await listOnlineUsers(params)
    // 确保total是数字类型
    const total = typeof res.total === 'number' ? res.total : parseInt(String(res.total) || '0', 10)
    return { records: res.records || [], total: total }
  } catch (e) {
    return { records: [], total: 0 }
  } finally {
    loading.value = false
  }
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
 * 查询在线用户列表。
 */
async function handleQuery() {
  await tableRef.value?.refresh?.()
}

/**
 * 重置查询条件并重新查询。
 */
function handleReset() {
  queryForm.account = ''
  tableRef.value?.refresh?.()
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

</script>

<style scoped lang="less">
.online-user-container {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
  padding: 20px;
  box-sizing: border-box;
}

.online-table-card {
  display: flex;
  flex-direction: column;
  flex: 1 1 auto;
  min-height: 0;
}

.online-table-card :deep(.ant-card-body) {
  display: flex;
  flex-direction: column;
  flex: 1 1 auto;
  min-height: 0;
  height: 100%;
}

.online-table-card :deep(.fx-dynamic-table) {
  flex: 1 1 auto;
  min-height: 0;
}
</style>
