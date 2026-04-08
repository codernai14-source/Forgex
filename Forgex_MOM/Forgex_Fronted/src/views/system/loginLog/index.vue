<template>
  <div class="login-log-container">
    <FxDynamicTable
      ref="tableRef"
      table-code="sys.loginLog.list"
      :dict-options="dictOptions"
      :request="request"
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

const { t } = useI18n()
const userStore = useUserStore()

const tableRef = ref<any>()

const dictOptions = computed(() => ({
  status: [
    { label: t('common.success'), value: 1 },
    { label: t('common.failed'), value: 0 },
  ],
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
