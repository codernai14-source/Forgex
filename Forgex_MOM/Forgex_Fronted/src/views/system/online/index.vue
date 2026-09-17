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
      row-key="userId"
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
          {{ resolveTerminalLabel(record.loginTerminal) }}
        </a-tag>
        <span v-else>{{ record.loginTerminal || '-' }}</span>
      </template>

      <template #ttlSeconds="{ record }">
        {{ formatTtl(record.ttlSeconds) }}
      </template>

      <template #action="{ record }">
        <a-space :size="4">
          <a-button type="link" size="small" @click="openDetail(record)">
            <template #icon><EyeOutlined /></template>
            {{ t('system.online.action.detail') }}
          </a-button>
          <a-button
            v-permission="'sys:online:kickout'"
            type="link"
            size="small"
            danger
            @click="openKickoutDialog(getSessionTokens(record), record.account)"
          >
            <template #icon><LogoutOutlined /></template>
            {{ t('system.online.action.kickout') }}
          </a-button>
        </a-space>
      </template>
    </FxDynamicTable>

    <a-modal v-model:open="detailOpen" :title="t('system.online.detail.title')" width="820px" :footer="null">
      <a-descriptions v-if="detailRecord" :column="2" size="small" bordered>
        <a-descriptions-item :label="t('system.online.detail.account')">{{ detailRecord.account || '-' }}</a-descriptions-item>
        <a-descriptions-item :label="t('system.online.detail.username')">{{ detailRecord.username || '-' }}</a-descriptions-item>
        <a-descriptions-item :label="t('system.online.detail.tenant')">{{ detailRecord.tenantName || '-' }}</a-descriptions-item>
        <a-descriptions-item :label="t('system.online.detail.sessionCount')">{{ detailRecord.sessions?.length || 0 }}</a-descriptions-item>
      </a-descriptions>
      <a-table
        v-if="detailRecord"
        class="online-session-table"
        :columns="sessionColumns"
        :data-source="detailRecord.sessions || []"
        :pagination="false"
        row-key="token"
        size="small"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'token'">
            <a-typography-text :content="record.token" copyable>{{ record.token }}</a-typography-text>
          </template>
          <template v-else-if="column.key === 'loginTerminal'">
            {{ resolveTerminalLabel(record.loginTerminal) }}
          </template>
          <template v-else-if="column.key === 'ttlSeconds'">
            {{ formatTtl(record.ttlSeconds) }}
          </template>
          <template v-else-if="column.key === 'action'">
            <a-button
              v-permission="'sys:online:kickout'"
              type="link"
              size="small"
              danger
              @click="openKickoutDialog([record.token], detailRecord?.account)"
            >
              {{ t('system.online.action.kickout') }}
            </a-button>
          </template>
        </template>
      </a-table>
    </a-modal>

    <a-modal
      v-model:open="kickoutDialogOpen"
      :title="t('system.online.confirm.kickoutTitle')"
      :confirm-loading="kickoutSubmitting"
      @ok="submitKickout"
    >
      <p>{{ t('system.online.confirm.kickoutContent', { target: kickoutTargetLabel }) }}</p>
      <a-radio-group v-model:value="kickoutDisableUser">
        <a-radio :value="false">{{ t('system.online.confirm.onlyKickout') }}</a-radio>
        <a-radio :value="true">{{ t('system.online.confirm.kickoutAndDisable') }}</a-radio>
      </a-radio-group>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { EyeOutlined, LogoutOutlined } from '@ant-design/icons-vue'
import { useI18n } from 'vue-i18n'
import FxDynamicTable from '@/components/common/FxDynamicTable.vue'
import { kickoutOnlineUser, listOnlineUsers } from '@/api/system/online'
import { useUserStore } from '@/stores/user'

type TerminalKey = 'ALL' | 'B' | 'C' | 'THIRD_PARTY'
type OnlineSession = {
  token: string
  loginTerminal?: string
  clientIp?: string
  loginRegion?: string
  loginTime?: string
  ttlSeconds?: number
}
type OnlineRecord = {
  userId?: string | number
  account?: string
  username?: string
  tenantName?: string
  sessions?: OnlineSession[]
}

const { t } = useI18n()
const userStore = useUserStore()
const tableRef = ref()
const selectedRowKeys = ref<string[]>([])
const activeTerminal = ref<TerminalKey>('ALL')
const currentQuery = ref<Record<string, any>>({})
const currentRecords = ref<OnlineRecord[]>([])
const detailOpen = ref(false)
const detailRecord = ref<OnlineRecord | null>(null)
const kickoutDialogOpen = ref(false)
const kickoutSubmitting = ref(false)
const kickoutDisableUser = ref(false)
const kickoutTargets = ref<string[]>([])
const kickoutTargetLabel = ref('')

const sessionColumns = computed(() => [
  { title: t('system.online.detail.token'), key: 'token', dataIndex: 'token', width: 280 },
  { title: t('system.online.detail.terminal'), key: 'loginTerminal', dataIndex: 'loginTerminal', width: 100 },
  { title: t('system.online.detail.region'), key: 'loginRegion', dataIndex: 'loginRegion', width: 140 },
  { title: t('system.online.detail.ttl'), key: 'ttlSeconds', dataIndex: 'ttlSeconds', width: 120 },
  { title: t('common.operation'), key: 'action', width: 100 },
])
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

function normalizeTerminalValue(value: any) {
  return String(value ?? '').trim().toUpperCase()
}

function resolveTerminalMeta(value: any) {
  const normalized = normalizeTerminalValue(value)
  return terminalOptions.value.find((item) => item.value === normalized)
}

function resolveTerminalLabel(value: any) {
  return resolveTerminalMeta(value)?.label || String(value || '-')
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
    currentRecords.value = res.records || []
    return { records: currentRecords.value, total }
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

function openDetail(record: OnlineRecord) {
  detailRecord.value = record
  detailOpen.value = true
}

function getSessionTokens(record: OnlineRecord) {
  return (record.sessions || []).map((session) => session.token)
}

function openKickoutDialog(tokens: string[], label = '') {
  const validTokens = tokens.filter(Boolean)
  if (validTokens.length === 0) return
  kickoutTargets.value = validTokens
  kickoutTargetLabel.value = label || t('system.online.detail.sessionCount', { count: validTokens.length })
  kickoutDisableUser.value = false
  kickoutDialogOpen.value = true
}

async function submitKickout() {
  if (kickoutTargets.value.length === 0) return
  kickoutSubmitting.value = true
  try {
    await Promise.all(kickoutTargets.value.map((token) => kickoutOnlineUser({
      token,
      disableUser: kickoutDisableUser.value,
    }, { showSuccessMessage: false })))
    kickoutDialogOpen.value = false
    detailOpen.value = false
    await tableRef.value?.refresh?.()
    await fetchTabCounts(currentQuery.value)
  } finally {
    kickoutSubmitting.value = false
  }
}

function handleBatchKickout() {
  if (selectedRowKeys.value.length === 0) return
  const selected = currentRecords.value.filter((record) => selectedRowKeys.value.includes(String(record.userId)))
  openKickoutDialog(selected.flatMap((record) => (record.sessions || []).map((session) => session.token)), t('system.online.confirm.batchTarget', { count: selected.length }))
  selectedRowKeys.value = []
}
</script>

<style scoped lang="less" src="@/styles/views/system/online/index.less"></style>
