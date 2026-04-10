<template>
  <div class="online-user-container">
    <a-tabs v-model:activeKey="activeTerminal" class="terminal-tabs" @change="handleTerminalChange">
      <a-tab-pane v-for="tab in terminalTabs" :key="tab.key" :tab="tab.label" />
    </a-tabs>

    <FxDynamicTable
      ref="tableRef"
      table-code="OnlineUserTable"
      :show-query-form="true"
      :request="handleRequest"
      :dict-options="dictOptions"
      :fallback-config="fallbackConfig"
      :row-selection="{
        selectedRowKeys,
        onChange: handleSelectionChange
      }"
      row-key="token"
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

      <template #loginTerminal="{ record }">
        <a-tag v-if="resolveTerminalMeta(record.loginTerminal)" :color="resolveTerminalMeta(record.loginTerminal)?.color">
          {{ resolveTerminalMeta(record.loginTerminal)?.label }}
        </a-tag>
        <span v-else>{{ record.loginTerminal || '-' }}</span>
      </template>

      <template #ttlSeconds="{ record }">
        {{ formatTtl(record.ttlSeconds) }}
      </template>

      <template #action="{ record }">
        <a
          v-permission="'sys:online:kickout'"
          style="color: #ff4d4f"
          @click="handleKickout(record.token)"
        >
          强制下线
        </a>
      </template>
    </FxDynamicTable>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { Modal, message } from 'ant-design-vue'
import type { FxTableConfig } from '@/api/system/tableConfig'
import FxDynamicTable from '@/components/common/FxDynamicTable.vue'
import { kickoutOnlineUser, listOnlineUsers } from '@/api/system/online'
import { useUserStore } from '@/stores/user'

type TerminalKey = 'ALL' | 'B' | 'C' | 'THIRD_PARTY'

const userStore = useUserStore()
const tableRef = ref()
const selectedRowKeys = ref<string[]>([])
const activeTerminal = ref<TerminalKey>('ALL')
const currentQuery = ref<Record<string, any>>({})
const tabCounts = ref<Record<TerminalKey, number>>({
  ALL: 0,
  B: 0,
  C: 0,
  THIRD_PARTY: 0,
})

const terminalOptions = [
  { value: 'B', label: 'B端', color: 'blue' },
  { value: 'C', label: 'C端', color: 'green' },
  { value: 'THIRD_PARTY', label: '第三方', color: 'orange' },
]

const dictOptions = computed(() => ({
  loginTerminal: terminalOptions.map(({ value, label }) => ({ value, label })),
}))

const terminalTabs = computed(() => [
  { key: 'ALL', label: `全部 (${tabCounts.value.ALL || 0})` },
  { key: 'B', label: `B端 (${tabCounts.value.B || 0})` },
  { key: 'C', label: `C端 (${tabCounts.value.C || 0})` },
  { key: 'THIRD_PARTY', label: `第三方 (${tabCounts.value.THIRD_PARTY || 0})` },
])

const fallbackConfig = computed<Partial<FxTableConfig>>(() => ({
  tableCode: 'OnlineUserTable',
  tableName: '在线用户',
  tableType: 'NORMAL',
  rowKey: 'token',
  defaultPageSize: 20,
  columns: [
    { field: 'account', title: '账号', width: 160, align: 'left' },
    { field: 'username', title: '用户名', width: 160, align: 'left' },
    { field: 'loginTerminal', title: '登录端', width: 120, align: 'center', dictCode: 'loginTerminal' },
    { field: 'tenantName', title: '租户', width: 180, align: 'left' },
    { field: 'lastLoginTime', title: '最后登录时间', width: 180, align: 'center' },
    { field: 'lastLoginIp', title: '最后登录IP', width: 150, align: 'left' },
    { field: 'lastLoginRegion', title: '登录地区', width: 160, align: 'left' },
    { field: 'ttlSeconds', title: '会话剩余时长', width: 140, align: 'center' },
    { field: 'action', title: '操作', width: 120, align: 'center', fixed: 'right' },
  ],
  queryFields: [
    { field: 'account', label: '账号', queryType: 'input', queryOperator: 'like' },
  ],
  version: 1,
}))

function getTenantId() {
  return Number(userStore.tenantId || sessionStorage.getItem('tenantId') || 1)
}

function buildListParams(query: Record<string, any>, terminal?: TerminalKey) {
  const params: Record<string, any> = {
    tenantId: getTenantId(),
    account: query?.account,
  }
  if (terminal && terminal !== 'ALL') {
    params.loginTerminal = terminal
  }
  return params
}

async function fetchTabCounts(query: Record<string, any> = {}) {
  try {
    const [allRes, bRes, cRes, thirdRes] = await Promise.all([
      listOnlineUsers({ current: 1, size: 1, ...buildListParams(query, 'ALL') }),
      listOnlineUsers({ current: 1, size: 1, ...buildListParams(query, 'B') }),
      listOnlineUsers({ current: 1, size: 1, ...buildListParams(query, 'C') }),
      listOnlineUsers({ current: 1, size: 1, ...buildListParams(query, 'THIRD_PARTY') }),
    ])
    tabCounts.value = {
      ALL: Number(allRes?.total || 0),
      B: Number(bRes?.total || 0),
      C: Number(cRes?.total || 0),
      THIRD_PARTY: Number(thirdRes?.total || 0),
    }
  } catch (error) {
    console.error('加载在线用户 Tab 计数失败', error)
  }
}

function resolveTerminalMeta(value: string) {
  return terminalOptions.find((item) => item.value === value)
}

const handleRequest = async (payload: {
  page: { current: number; pageSize: number }
  query: Record<string, any>
  sorter?: { field?: string; order?: string }
}) => {
  try {
    currentQuery.value = { ...payload.query }
    const res: any = await listOnlineUsers({
      current: payload.page.current,
      size: payload.page.pageSize,
      ...buildListParams(payload.query || {}, activeTerminal.value),
    })
    void fetchTabCounts(currentQuery.value)
    const total = typeof res.total === 'number' ? res.total : parseInt(String(res.total) || '0', 10)
    return { records: res.records || [], total }
  } catch (error) {
    console.error('查询在线用户失败', error)
    return { records: [], total: 0 }
  }
}

function handleSelectionChange(keys: Array<string | number>) {
  selectedRowKeys.value = keys.map((item) => String(item))
}

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

function handleTerminalChange() {
  selectedRowKeys.value = []
  tableRef.value?.refresh?.()
}

function handleKickout(token: string) {
  if (!token) return
  Modal.confirm({
    title: '确认强制下线？',
    content: '该操作会让对应会话立即失效。',
    okText: '确定',
    cancelText: '取消',
    onOk: async () => {
      await kickoutOnlineUser({ token })
      message.success('已强制下线')
      await tableRef.value?.refresh?.()
      await fetchTabCounts(currentQuery.value)
    },
  })
}

function handleBatchKickout() {
  if (selectedRowKeys.value.length === 0) return
  Modal.confirm({
    title: '确认批量强制下线？',
    content: `将对选中的 ${selectedRowKeys.value.length} 个会话执行强制下线。`,
    okText: '确定',
    cancelText: '取消',
    onOk: async () => {
      await Promise.all(selectedRowKeys.value.map((token) => kickoutOnlineUser({ token })))
      message.success('批量强制下线成功')
      selectedRowKeys.value = []
      await tableRef.value?.refresh?.()
      await fetchTabCounts(currentQuery.value)
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

.terminal-tabs {
  flex: 0 0 auto;

  :deep(.ant-tabs-nav) {
    margin-bottom: 12px;
  }
}
</style>
