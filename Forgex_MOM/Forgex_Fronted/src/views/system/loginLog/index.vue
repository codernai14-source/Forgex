<template>
  <div class="login-log-container">
    <a-card :bordered="false">
      <!-- 查询表单 -->
      <a-form layout="inline" :model="queryForm">
        <a-form-item label="登录账号">
          <a-input v-model:value="queryForm.account" placeholder="请输入账号" allow-clear style="width: 200px" />
        </a-form-item>
        <a-form-item label="登录状态">
          <a-select v-model:value="queryForm.status" placeholder="请选择状态" allow-clear style="width: 150px">
            <a-select-option :value="1">成功</a-select-option>
            <a-select-option :value="0">失败</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="登录时间">
          <a-range-picker
            v-model:value="timeRange"
            show-time
            format="YYYY-MM-DD HH:mm:ss"
            :placeholder="['开始时间', '结束时间']"
            style="width: 380px"
          />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="handleQuery">查询</a-button>
            <a-button @click="handleReset">重置</a-button>
            <a-button v-permission="'sys:excel:export:loginLog'" @click="handleExport">导出</a-button>
          </a-space>
        </a-form-item>
      </a-form>

      <!-- 表格 -->
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
        row-key="id"
        @change="handleTableChange"
        style="margin-top: 16px"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="record.status === 1 ? 'success' : 'error'">
              {{ record.status === 1 ? '成功' : '失败' }}
            </a-tag>
          </template>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import http from '@/api/http'
import dayjs, { Dayjs } from 'dayjs'
import { useUserStore } from '@/stores/user'
import { exportLoginLog } from '@/api/system/excel'

const userStore = useUserStore()

// 查询表单
const queryForm = reactive({
  account: '',
  status: undefined as number | undefined
})

// 时间范围
const timeRange = ref<[Dayjs, Dayjs] | undefined>(undefined)

// 分页
const pagination = reactive({
  current: 1,
  size: 20,
  total: 0
})

// 表格数据
const tableData = ref<any[]>([])
const loading = ref(false)

// 表格列定义
const columns = [
  { title: '登录账号', dataIndex: 'account', key: 'account', width: 120 },
  { title: '登录IP', dataIndex: 'loginIp', key: 'loginIp', width: 150 },
  { title: '归属地', dataIndex: 'loginRegion', key: 'loginRegion', width: 180 },
  { title: '浏览器UA', dataIndex: 'userAgent', key: 'userAgent', ellipsis: true },
  { title: '登录时间', dataIndex: 'loginTime', key: 'loginTime', width: 180 },
  { title: '登出时间', dataIndex: 'logoutTime', key: 'logoutTime', width: 180 },
  { title: '登出原因', dataIndex: 'logoutReason', key: 'logoutReason', width: 120 },
  { title: '状态', key: 'status', width: 80, align: 'center' },
  { title: '失败原因', dataIndex: 'reason', key: 'reason', width: 150, ellipsis: true }
]

// 查询
const handleQuery = async () => {
  loading.value = true
  try {
    const params: any = {
      current: pagination.current,
      size: pagination.size,
      account: queryForm.account || undefined,
      status: queryForm.status,
      tenantId: userStore.tenantId || 1
    }

    // 添加时间范围
    if (timeRange.value && timeRange.value.length === 2) {
      params.startTime = timeRange.value[0].format('YYYY-MM-DD HH:mm:ss')
      params.endTime = timeRange.value[1].format('YYYY-MM-DD HH:mm:ss')
    }

    const res = await http.post('/sys/loginLog/list', params)
    tableData.value = res.records
    pagination.total = res.total
  } catch (error) {
    console.error('查询登录日志失败', error)
  } finally {
    loading.value = false
  }
}

// 导出
const handleExport = async () => {
  try {
    const startTime = timeRange.value?.[0] ? timeRange.value[0].format('YYYY-MM-DD HH:mm:ss') : undefined
    const endTime = timeRange.value?.[1] ? timeRange.value[1].format('YYYY-MM-DD HH:mm:ss') : undefined
    const resp: any = await exportLoginLog({
      tableCode: 'sys_login_log',
      query: {
        account: queryForm.account || undefined,
        status: queryForm.status,
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

// 重置
const handleReset = () => {
  queryForm.account = ''
  queryForm.status = undefined
  timeRange.value = undefined
  pagination.current = 1
  handleQuery()
}

// 表格分页改变
const handleTableChange = (pag: any) => {
  pagination.current = pag.current
  pagination.size = pag.pageSize
  handleQuery()
}

// 初始化
onMounted(() => {
  handleQuery()
})
</script>

<style scoped lang="less">
.login-log-container {
  padding: 20px;
}
</style>
