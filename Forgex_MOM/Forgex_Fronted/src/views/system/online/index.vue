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

      <fx-dynamic-table
        ref="tableRef"
        :table-code="'OnlineUserTable'"
        :request="handleRequest"
        :fallback-config="fallbackConfig"
        :dict-options="dictOptions"
        :loading="loading"
        row-key="token"
        style="margin-top: 16px"
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
const handleRequest = async (params: any) => {
  loading.value = true
  try {
    // tenantId 由用户 store 提供（未选择时兜底 1）
    const res: any = await listOnlineUsers({
      current: params.current,
      size: params.pageSize,
      account: queryForm.account || undefined,
      tenantId: userStore.tenantId || 1,
    })
    return {
      success: true,
      data: res.records || [],
      total: res.total || 0
    }
  } catch (e) {
    console.error('查询在线用户失败', e)
    return {
      success: false,
      data: [],
      total: 0
    }
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
  await tableRef.value?.refresh()
}

/**
 * 重置查询条件并重新查询。
 */
function handleReset() {
  queryForm.account = ''
  tableRef.value?.refresh()
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
        await tableRef.value?.refresh()
      } catch (e) {
        console.error('强制下线失败', e)
      }
    },
  })
}

// 初始化加载
onMounted(() => {
  tableRef.value?.refresh()
})
</script>

<style scoped lang="less">
.online-user-container {
  padding: 20px;
}
</style>
