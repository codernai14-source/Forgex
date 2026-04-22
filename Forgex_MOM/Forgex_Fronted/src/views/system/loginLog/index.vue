<template>
  <div class="login-log-container">
    <FxDynamicTable
      ref="tableRef"
      table-code="sys.loginLog.list"
      :dict-options="dictOptions"
      :request="request"
      :dynamic-table-config="dynamicTableConfig"
    >
      <template #toolbar>
        <a-button v-permission="'sys:excel:export:loginLog'" @click="handleExport">{{ t('common.export') }}</a-button>
      </template>
    </FxDynamicTable>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { message } from 'ant-design-vue'
import http from '@/api/http'
import { useUserStore } from '@/stores/user'
import { exportLoginLog } from '@/api/system/excel'
import FxDynamicTable from '@/components/common/FxDynamicTable.vue'
import type { FxTableConfig } from '@/api/system/tableConfig'

const { t } = useI18n()
const userStore = useUserStore()

const tableRef = ref<any>()

const dictOptions = computed(() => ({
  status: [
    { label: t('common.success'), value: 1 },
    { label: t('common.failed'), value: 0 },
  ],
}))

const dynamicTableConfig = computed<Partial<FxTableConfig>>(() => ({
  tableCode: 'sys.loginLog.list',
  tableName: '登录日志',
  tableType: 'NORMAL',
  rowKey: 'id',
  defaultPageSize: 20,
  columns: [
    { field: 'account', title: '登录账号', width: 140, align: 'left' },
    { field: 'status', title: '登录状态', width: 110, align: 'center', dictCode: 'status' },
    { field: 'loginTime', title: '登录时间', width: 180, align: 'center' },
    { field: 'loginIp', title: '登录 IP', width: 150, align: 'left' },
    { field: 'loginRegion', title: '归属地', width: 180, align: 'left' },
    { field: 'userAgent', title: '浏览器 UA', width: 220, align: 'left', ellipsis: true },
    { field: 'reason', title: '失败原因', width: 160, align: 'left', ellipsis: true },
  ],
  queryFields: [
    { field: 'account', label: '登录账号', queryType: 'input', queryOperator: 'like' },
    { field: 'status', label: '登录状态', queryType: 'select', queryOperator: 'eq', dictCode: 'status' },
    { field: 'loginTime', label: '登录时间', queryType: 'dateRange', queryOperator: 'between' },
  ],
  version: 2,
}))

const request = async (payload: { page: { current: number; pageSize: number }; query: Record<string, any> }) => {
  const params: any = {
    current: payload.page.current,
    size: payload.page.pageSize,
    tenantId: userStore.tenantId || 1,
    ...payload.query,
  }
  if (Array.isArray(params.loginTime) && params.loginTime.length === 2) {
    params.startTime = params.loginTime[0]
    params.endTime = params.loginTime[1]
    delete params.loginTime
  }

  const res: any = await http.post('/sys/loginLog/list', params)
  return { records: res.records || [], total: res.total || 0 }
}

// 导出
const handleExport = async () => {
  try {
    const q = tableRef.value?.getQuery?.() || {}
    const startTime = Array.isArray(q.loginTime) ? q.loginTime[0] : undefined
    const endTime = Array.isArray(q.loginTime) ? q.loginTime[1] : undefined
    const resp: any = await exportLoginLog({
      tableCode: 'sys_login_log',
      query: {
        account: q.account || undefined,
        status: q.status,
        startTime,
        endTime,
        tenantId: userStore.tenantId || 1,
      },
    })
    const blob = new Blob([resp.data], { type: resp.headers?.['content-type'] || 'application/octet-stream' })
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `login-log-${Date.now()}.xlsx`
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)
    window.URL.revokeObjectURL(url)
    message.success(t('common.exportSuccess'))
  } catch (error) {
    console.error(t('common.exportFailed'), error)
    message.error(t('common.exportFailed'))
  }
}
</script>

<style scoped lang="less">
.login-log-container {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
  padding: 20px;
  box-sizing: border-box;
}

.login-log-container :deep(.fx-dynamic-table) {
  flex: 1 1 auto;
  min-height: 0;
}
</style>
