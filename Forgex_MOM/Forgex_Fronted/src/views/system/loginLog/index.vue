<template>
  <div class="login-log-container">
    <FxDynamicTable
      ref="tableRef"
      table-code="sys.loginLog.list"
      :dict-options="dictOptions"
      :request="request"
    >
      <template #toolbar>
        <a-button v-permission="'sys:excel:export:loginLog'" @click="handleExport">导出</a-button>
      </template>
    </FxDynamicTable>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { message } from 'ant-design-vue'
import http from '@/api/http'
import { useUserStore } from '@/stores/user'
import { exportLoginLog } from '@/api/system/excel'
import FxDynamicTable from '@/components/common/FxDynamicTable.vue'

const userStore = useUserStore()

const tableRef = ref<any>()

const dictOptions = {
  status: [
    { label: '成功', value: 1 },
    { label: '失败', value: 0 },
  ],
}

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
    message.success('导出成功')
  } catch (error) {
    console.error('导出登录日志失败', error)
    message.error('导出失败')
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
