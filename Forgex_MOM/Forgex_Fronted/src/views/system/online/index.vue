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
      :row-selection="{
        selectedRowKeys,
        onChange: handleSelectionChange
      }"
      row-key="token"
    >
      <template #toolbar>
        <a-space :size="8">
          <a-button
            data-guide-id="sys-online-kickout"
            v-permission="'sys:online:kickout'"
            danger
            :disabled="selectedRowKeys.length === 0"
            @click="handleBatchKickout"
          >
            {{ t('system.online.action.batchKickout') }}
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
        <a v-permission="'sys:online:kickout'" style="color: #ff4d4f" @click="handleKickout(record.token)">
          {{ t('system.online.action.kickout') }}
        </a>
      </template>
    </FxDynamicTable>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { Modal } from 'ant-design-vue'
import { useI18n } from 'vue-i18n'
import FxDynamicTable from '@/components/common/FxDynamicTable.vue'
import { kickoutOnlineUser, listOnlineUsers } from '@/api/system/online'
import { useUserStore } from '@/stores/user'

type TerminalKey = 'ALL' | 'B' | 'C' | 'THIRD_PARTY'

const { t } = useI18n()
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

const terminalOptions = computed(() => [
  { value: 'B', label: t('system.online.terminal.b'), color: 'blue' },
  { value: 'C', label: t('system.online.terminal.c'), color: 'green' },
  { value: 'THIRD_PARTY', label: t('system.online.terminal.thirdParty'), color: 'orange' },
])

const dictOptions = computed(() => ({
  loginTerminal: terminalOptions.value.map(({ value, label }) => ({ value, label })),
}))

const terminalTabs = computed(() => [
  { key: 'ALL', label: `${t('system.online.terminal.all')} (${tabCounts.value.ALL || 0})` },
  { key: 'B', label: `${t('system.online.terminal.b')} (${tabCounts.value.B || 0})` },
  { key: 'C', label: `${t('system.online.terminal.c')} (${tabCounts.value.C || 0})` },
  { key: 'THIRD_PARTY', label: `${t('system.online.terminal.thirdParty')} (${tabCounts.value.THIRD_PARTY || 0})` },
])

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
    console.error('Failed to load online user tab counts', error)
  }
}

function resolveTerminalMeta(value: string) {
  return terminalOptions.value.find((item) => item.value === value)
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
    console.error('Failed to query online users', error)
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
  if (n < 0) return t('system.online.ttl.longTerm')
  const minute = Math.floor(n / 60)
  const second = Math.floor(n % 60)
  if (minute <= 0) return t('system.online.ttl.second', { count: second })
  return t('system.online.ttl.minuteSecond', { minute, second })
}

function handleTerminalChange() {
  selectedRowKeys.value = []
  tableRef.value?.refresh?.()
}

function handleKickout(token: string) {
  if (!token) return
  Modal.confirm({
    title: t('system.online.confirm.kickoutTitle'),
    content: t('system.online.confirm.kickoutContent'),
    okText: t('common.confirm'),
    cancelText: t('common.cancel'),
    onOk: async () => {
      await kickoutOnlineUser({ token })
      await tableRef.value?.refresh?.()
      await fetchTabCounts(currentQuery.value)
    },
  })
}

function handleBatchKickout() {
  if (selectedRowKeys.value.length === 0) return
  Modal.confirm({
    title: t('system.online.confirm.batchKickoutTitle'),
    content: t('system.online.confirm.batchKickoutContent', { count: selectedRowKeys.value.length }),
    okText: t('common.confirm'),
    cancelText: t('common.cancel'),
    onOk: async () => {
      await Promise.all(selectedRowKeys.value.map((token) => kickoutOnlineUser({ token }, { showSuccessMessage: false })))
      selectedRowKeys.value = []
      await tableRef.value?.refresh?.()
      await fetchTabCounts(currentQuery.value)
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
  overflow: hidden;
}

.terminal-tabs {
  flex: 0 0 auto;

  :deep(.ant-tabs-nav) {
    margin-bottom: 12px;
  }
}
</style>
