<template>
  <div class="operation-log-container">
    <!-- 查询表单 -->
    <a-card :bordered="false" style="margin-bottom: 16px">
      <a-form layout="inline" :model="queryForm">
        <a-form-item label="模块名称">
          <a-input
            v-model:value="queryForm.module"
            placeholder="请输入模块名称"
            style="width: 200px"
            allowClear
          />
        </a-form-item>
        <a-form-item label="操作类型">
          <a-select
            v-model:value="queryForm.operationType"
            placeholder="请选择操作类型"
            style="width: 150px"
            allowClear
          >
            <a-select-option value="CREATE">新增</a-select-option>
            <a-select-option value="UPDATE">修改</a-select-option>
            <a-select-option value="DELETE">删除</a-select-option>
            <a-select-option value="QUERY">查询</a-select-option>
            <a-select-option value="EXPORT">导出</a-select-option>
            <a-select-option value="IMPORT">导入</a-select-option>
            <a-select-option value="LOGIN">登录</a-select-option>
            <a-select-option value="LOGOUT">登出</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="操作时间">
          <a-range-picker
            v-model:value="timeRange"
            style="width: 300px"
            :show-time="{ format: 'HH:mm:ss' }"
            format="YYYY-MM-DD HH:mm:ss"
            :placeholder="['开始时间', '结束时间']"
          />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="handleQuery">
              <template #icon><SearchOutlined /></template>
              查询
            </a-button>
            <a-button @click="handleReset">
              <template #icon><ReloadOutlined /></template>
              重置
            </a-button>
            <a-button @click="handleExport" v-permission="'sys:operation-log:export'">
              <template #icon><ExportOutlined /></template>
              导出
            </a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <!-- 数据表格 -->
    <a-card :bordered="false">
      <a-table
        :columns="columns"
        :data-source="dataSource"
        :loading="loading"
        :pagination="pagination"
        @change="handleTableChange"
        row-key="id"
        :scroll="{ x: 1500 }"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'operationType'">
            <a-tag :color="getOperationTypeColor(record.operationType)">
              {{ getOperationTypeText(record.operationType) }}
            </a-tag>
          </template>
          <template v-if="column.key === 'responseStatus'">
            <a-tag :color="record.responseStatus === 200 ? 'success' : 'error'">
              {{ record.responseStatus }}
            </a-tag>
          </template>
          <template v-if="column.key === 'costTime'">
            <span :style="{ color: getCostTimeColor(record.costTime) }">
              {{ record.costTime }}ms
            </span>
          </template>
          <template v-if="column.key === 'operationTime'">
            {{ formatDateTime(record.operationTime) }}
          </template>
          <template v-if="column.key === 'action'">
            <a-button type="link" size="small" @click="handleViewDetail(record)">
              详情
            </a-button>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 详情弹窗 -->
    <a-modal
      v-model:open="detailVisible"
      title="操作日志详情"
      :width="900"
      :footer="null"
    >
      <a-descriptions :column="2" bordered v-if="currentRecord">
        <a-descriptions-item label="用户名">
          {{ currentRecord.username }}
        </a-descriptions-item>
        <a-descriptions-item label="操作时间">
          {{ formatDateTime(currentRecord.operationTime) }}
        </a-descriptions-item>
        <a-descriptions-item label="模块名称">
          {{ currentRecord.module }}
        </a-descriptions-item>
        <a-descriptions-item label="操作类型">
          <a-tag :color="getOperationTypeColor(currentRecord.operationType)">
            {{ getOperationTypeText(currentRecord.operationType) }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="请求方法">
          {{ currentRecord.requestMethod }}
        </a-descriptions-item>
        <a-descriptions-item label="响应状态">
          <a-tag :color="currentRecord.responseStatus === 200 ? 'success' : 'error'">
            {{ currentRecord.responseStatus }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="请求URL" :span="2">
          <a-typography-text copyable>{{ currentRecord.requestUrl }}</a-typography-text>
        </a-descriptions-item>
        <a-descriptions-item label="IP地址">
          {{ currentRecord.ip }}
        </a-descriptions-item>
        <a-descriptions-item label="耗时">
          <span :style="{ color: getCostTimeColor(currentRecord.costTime) }">
            {{ currentRecord.costTime }}ms
          </span>
        </a-descriptions-item>
        <a-descriptions-item label="请求参数" :span="2">
          <pre style="max-height: 200px; overflow: auto; background: #f5f5f5; padding: 8px; border-radius: 4px;">{{ formatJson(currentRecord.requestParams) }}</pre>
        </a-descriptions-item>
        <a-descriptions-item label="响应结果" :span="2" v-if="currentRecord.responseResult">
          <pre style="max-height: 200px; overflow: auto; background: #f5f5f5; padding: 8px; border-radius: 4px;">{{ formatJson(currentRecord.responseResult) }}</pre>
        </a-descriptions-item>
        <a-descriptions-item label="错误堆栈" :span="2" v-if="currentRecord.errorStack">
          <pre style="max-height: 300px; overflow: auto; background: #fff2f0; padding: 8px; border-radius: 4px; color: #cf1322;">{{ currentRecord.errorStack }}</pre>
        </a-descriptions-item>
        <a-descriptions-item label="操作详情" :span="2" v-if="currentRecord.detailText">
          {{ currentRecord.detailText }}
        </a-descriptions-item>
        <a-descriptions-item label="User-Agent" :span="2">
          {{ currentRecord.userAgent }}
        </a-descriptions-item>
      </a-descriptions>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import {
  SearchOutlined,
  ReloadOutlined,
  ExportOutlined
} from '@ant-design/icons-vue'
import { pageOperationLog, exportOperationLog } from '@/api/operationLog'
import dayjs from 'dayjs'

// 查询表单
const queryForm = reactive({
  module: undefined,
  operationType: undefined
})

// 时间范围
const timeRange = ref<any[]>([])

// 表格数据
const dataSource = ref([])
const loading = ref(false)

// 分页配置
const pagination = reactive({
  current: 1,
  pageSize: 20,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total: number) => `共 ${total} 条`
})

// 表格列配置
const columns = [
  {
    title: '用户名',
    dataIndex: 'username',
    key: 'username',
    width: 120,
    fixed: 'left'
  },
  {
    title: '模块名称',
    dataIndex: 'module',
    key: 'module',
    width: 150
  },
  {
    title: '操作类型',
    dataIndex: 'operationType',
    key: 'operationType',
    width: 100,
    align: 'center'
  },
  {
    title: '请求方法',
    dataIndex: 'requestMethod',
    key: 'requestMethod',
    width: 100,
    align: 'center'
  },
  {
    title: '请求URL',
    dataIndex: 'requestUrl',
    key: 'requestUrl',
    width: 300,
    ellipsis: true
  },
  {
    title: '响应状态',
    dataIndex: 'responseStatus',
    key: 'responseStatus',
    width: 100,
    align: 'center'
  },
  {
    title: '耗时',
    dataIndex: 'costTime',
    key: 'costTime',
    width: 100,
    align: 'center'
  },
  {
    title: 'IP地址',
    dataIndex: 'ip',
    key: 'ip',
    width: 140
  },
  {
    title: '操作时间',
    dataIndex: 'operationTime',
    key: 'operationTime',
    width: 180,
    align: 'center'
  },
  {
    title: '操作',
    key: 'action',
    width: 100,
    align: 'center',
    fixed: 'right'
  }
]

// 详情弹窗
const detailVisible = ref(false)
const currentRecord = ref<any>(null)

/**
 * 查询数据
 * <p>根据查询条件分页查询操作日志数据。</p>
 */
const loadData = async () => {
  loading.value = true
  try {
    const params: any = {
      current: pagination.current,
      size: pagination.pageSize,
      ...queryForm
    }
    
    // 处理时间范围
    if (timeRange.value && timeRange.value.length === 2) {
      params.startTime = dayjs(timeRange.value[0]).format('YYYY-MM-DD HH:mm:ss')
      params.endTime = dayjs(timeRange.value[1]).format('YYYY-MM-DD HH:mm:ss')
    }
    
    // http拦截器已经处理了响应，成功时直接返回data字段（即Page对象）
    const res = await pageOperationLog(params)
    dataSource.value = res.records || []
    // 确保 total 是数字类型
    pagination.total = typeof res.total === 'number' ? res.total : parseInt(String(res.total) || '0', 10)
  } catch (error) {
    message.error('查询失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

// 查询
const handleQuery = () => {
  pagination.current = 1
  loadData()
}

// 重置
const handleReset = () => {
  queryForm.module = undefined
  queryForm.operationType = undefined
  timeRange.value = []
  handleQuery()
}

// 表格变化
const handleTableChange = (pag: any) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  loadData()
}

// 查看详情
const handleViewDetail = (record: any) => {
  currentRecord.value = record
  detailVisible.value = true
}

// 导出
const handleExport = async () => {
  try {
    const params: any = {
      ...queryForm
    }
    
    // 处理时间范围
    if (timeRange.value && timeRange.value.length === 2) {
      params.startTime = dayjs(timeRange.value[0]).format('YYYY-MM-DD HH:mm:ss')
      params.endTime = dayjs(timeRange.value[1]).format('YYYY-MM-DD HH:mm:ss')
    }
    
    await exportOperationLog(params)
    message.success('导出成功')
  } catch (error) {
    message.error('导出失败')
    console.error(error)
  }
}

// 获取操作类型颜色
const getOperationTypeColor = (type: string) => {
  const colorMap: Record<string, string> = {
    CREATE: 'green',
    UPDATE: 'blue',
    DELETE: 'red',
    QUERY: 'default',
    EXPORT: 'purple',
    IMPORT: 'orange',
    LOGIN: 'cyan',
    LOGOUT: 'default'
  }
  return colorMap[type] || 'default'
}

// 获取操作类型文本
const getOperationTypeText = (type: string) => {
  const textMap: Record<string, string> = {
    CREATE: '新增',
    UPDATE: '修改',
    DELETE: '删除',
    QUERY: '查询',
    EXPORT: '导出',
    IMPORT: '导入',
    LOGIN: '登录',
    LOGOUT: '登出'
  }
  return textMap[type] || type
}

// 获取耗时颜色
const getCostTimeColor = (costTime: number) => {
  if (costTime < 100) return '#52c41a'
  if (costTime < 500) return '#faad14'
  return '#f5222d'
}

// 格式化日期时间
const formatDateTime = (dateTime: string) => {
  return dateTime ? dayjs(dateTime).format('YYYY-MM-DD HH:mm:ss') : '-'
}

// 格式化JSON
const formatJson = (jsonStr: string) => {
  if (!jsonStr) return '-'
  try {
    const obj = JSON.parse(jsonStr)
    return JSON.stringify(obj, null, 2)
  } catch (e) {
    return jsonStr
  }
}

// 初始化
onMounted(() => {
  loadData()
})
</script>

<style scoped lang="less">
.operation-log-container {
  padding: 16px;
}
</style>

