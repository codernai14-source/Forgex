<template>
  <div class="online-user-container">
    <a-card :bordered="false">
      <a-form layout="inline" :model="queryForm">
        <a-form-item label="账号">
          <a-input v-model:value="queryForm.account" placeholder="请输入账号" allow-clear style="width: 220px" />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button v-permission="'sys:online:list'" type="primary" @click="handleQuery">查询</a-button>
            <a-button @click="handleReset">重置</a-button>
          </a-space>
        </a-form-item>
      </a-form>

      <a-table
        :columns="columns"
        :data-source="tableData"
        :loading="loading"
        :pagination="{
          current: pagination.current,
          pageSize: pagination.size,
          total: pagination.total,
          showSizeChanger: true,
          showQuickJumper: true,
          showTotal: (total: number) => `共 ${total} 条`,
        }"
        row-key="token"
        @change="handleTableChange"
        style="margin-top: 16px"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'ttlSeconds'">
            {{ formatTtl(record.ttlSeconds) }}
          </template>
          <template v-else-if="column.key === 'action'">
            <a
              v-permission="'sys:online:kickout'"
              style="color: #ff4d4f;"
              @click="handleKickout(record.token)"
            >
              强制下线
            </a>
          </template>
        </template>
      </a-table>
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

// 分页参数
const pagination = reactive({
  current: 1,
  size: 20,
  total: 0,
})

// 表格数据与状态
const tableData = ref<any[]>([])
const loading = ref(false)

// 表格列定义
const columns = [
  { title: '账号', dataIndex: 'account', key: 'account', width: 140 },
  { title: '用户ID', dataIndex: 'userId', key: 'userId', width: 110 },
  { title: '租户ID', dataIndex: 'tenantId', key: 'tenantId', width: 110 },
  { title: '最后登录时间', dataIndex: 'lastLoginTime', key: 'lastLoginTime', width: 180 },
  { title: '最后登录IP', dataIndex: 'lastLoginIp', key: 'lastLoginIp', width: 150 },
  { title: '最后登录地区', dataIndex: 'lastLoginRegion', key: 'lastLoginRegion', width: 150 },
  { title: '会话剩余', key: 'ttlSeconds', width: 120 },
  { title: '操作', key: 'action', width: 110, fixed: 'right' },
]

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
  loading.value = true
  try {
    // tenantId 由用户 store 提供（未选择时兜底 1）
    const res: any = await listOnlineUsers({
      current: pagination.current,
      size: pagination.size,
      account: queryForm.account || undefined,
      tenantId: userStore.tenantId || 1,
    })
    tableData.value = res.records || []
    pagination.total = res.total || 0
  } catch (e) {
    console.error('查询在线用户失败', e)
  } finally {
    loading.value = false
  }
}

/**
 * 重置查询条件并重新查询。
 */
function handleReset() {
  queryForm.account = ''
  pagination.current = 1
  handleQuery()
}

/**
 * 分页变更处理。
 *
 * @param pag 分页对象
 */
function handleTableChange(pag: any) {
  pagination.current = pag.current
  pagination.size = pag.pageSize
  handleQuery()
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
        handleQuery()
      } catch (e) {
        console.error('强制下线失败', e)
      }
    },
  })
}

// 初始化加载
onMounted(() => {
  handleQuery()
})
</script>

<style scoped lang="less">
.online-user-container {
  padding: 20px;
}
</style>
