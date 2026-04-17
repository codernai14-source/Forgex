<template>
  <div class="page-container">
    <FxDynamicTable
        ref="tableRef"
        :request="loadData"
        table-code="LabelPrintRecordTable"
        :fallback-config="fallbackConfig"
    >
      <!-- 自定义列渲染 -->
      <template #templateType="{ record }">
        <a-tag color="blue">{{ getTemplateTypeName(record.templateType) }}</a-tag>
      </template>

      <template #isReprint="{ record }">
        <a-tag v-if="record.isReprint" color="orange">补打</a-tag>
        <a-tag v-else color="green">首次</a-tag>
      </template>

      <!-- 行操作 -->
      <template #action="{ record }">
        <a-space>
          <a @click="handleView(record)">详情</a>
          <a @click="handleReprint(record)">补打</a>
        </a-space>
      </template>
    </FxDynamicTable>

    <!-- 详情弹窗 -->
    <a-drawer
        v-model:open="detailVisible"
        title="打印记录详情"
        width="900px"
        placement="right"
    >
      <a-descriptions :column="2" bordered size="small">
        <a-descriptions-item label="打印流水号">
          <a-tag color="blue">{{ detailData.printNo }}</a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="模板类型">
          <a-tag color="blue">{{ getTemplateTypeName(detailData.templateType) }}</a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="模板名称">{{ detailData.templateName }}</a-descriptions-item>
        <a-descriptions-item label="模板版本">v{{ detailData.templateVersion }}</a-descriptions-item>
        <a-descriptions-item label="条码号">
          <a-tag>{{ detailData.barcodeNo || '-' }}</a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="LOT号">{{ detailData.lotNo || '-' }}</a-descriptions-item>
        <a-descriptions-item label="批次号">{{ detailData.batchNo || '-' }}</a-descriptions-item>
        <a-descriptions-item label="物料编码">{{ detailData.materialCode || '-' }}</a-descriptions-item>
        <a-descriptions-item label="物料名称">{{ detailData.materialName || '-' }}</a-descriptions-item>
        <a-descriptions-item label="打印张数">
          <a-tag color="green">{{ detailData.printCount }} 张</a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="打印类型">
          <a-tag v-if="detailData.printType === 'REPRINT'" color="orange">补打</a-tag>
          <a-tag v-else color="green">正常打印</a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="操作人">{{ detailData.operatorName }}</a-descriptions-item>
        <a-descriptions-item label="打印时间">{{ detailData.printTime }}</a-descriptions-item>
        <a-descriptions-item label="备注" :span="2">{{ detailData.remark || '-' }}</a-descriptions-item>
      </a-descriptions>

      <!-- 打印数据快照 -->
      <div class="snapshot-section">
        <a-divider orientation="left">
          <DatabaseOutlined /> 打印数据快照
        </a-divider>

        <a-tabs v-model:activeKey="snapshotActiveKey" type="card" size="small">
          <!-- JSON 视图 -->
          <a-tab-pane key="json" tab="JSON 格式">
            <div class="snapshot-container">
              <pre class="json-viewer">{{ formattedSnapshot }}</pre>
            </div>
          </a-tab-pane>

          <!-- 表格视图 -->
          <a-tab-pane key="table" tab="表格视图" v-if="snapshotDataArray.length > 0">
            <a-table
                :columns="snapshotColumns"
                :data-source="snapshotDataArray"
                :pagination="false"
                size="small"
                :scroll="{ y: 400 }"
                bordered
            >
              <template #bodyCell="{ column, record }">
                <span v-if="column.dataIndex === 'index'">{{ record.index }}</span>
                <span v-else>{{ record[column.dataIndex] || '-' }}</span>
              </template>
            </a-table>
          </a-tab-pane>
        </a-tabs>
      </div>

      <!-- 打印结果 JSON -->
      <div class="snapshot-section" v-if="detailData.printResultJson">
        <a-divider orientation="left">
          <FileTextOutlined /> 打印结果模板
        </a-divider>
        <a-collapse v-model:activeKey="resultCollapseKey">
          <a-collapse-panel key="1" header="查看打印模板内容">
            <div class="snapshot-container">
              <pre class="json-viewer">{{ formattedPrintResult }}</pre>
            </div>
          </a-collapse-panel>
        </a-collapse>
      </div>

      <template #footer>
        <a-space>
          <a-button @click="detailVisible = false">关闭</a-button>
          <a-button type="primary" @click="handleReprintFromDetail">
            <PrinterOutlined /> 补打
          </a-button>
        </a-space>
      </template>
    </a-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { message } from 'ant-design-vue'
import { DatabaseOutlined, FileTextOutlined, PrinterOutlined } from '@ant-design/icons-vue'
import { labelRecordApi } from '@/api/label/record'
import { labelPrintApi } from '@/api/label/print'

const tableRef = ref()
const detailVisible = ref(false)
const detailData = ref<any>({})
const snapshotActiveKey = ref('json')
const resultCollapseKey = ref([])

/**
 * 打印记录表格降级配置
 */
const fallbackConfig = ref({
  tableName: '打印记录',
  rowKey: 'id',
  defaultPageSize: 20,
  columns: [
    { field: 'printNo', title: '打印流水号', width: 180, align: 'center' },
    { field: 'templateName', title: '模板名称', width: 150, align: 'center' },
    { field: 'templateType', title: '模板类型', width: 120, align: 'center' },
    { field: 'barcodeNo', title: '条码号', width: 160, align: 'center' },
    { field: 'materialCode', title: '物料编码', width: 130, align: 'center' },
    { field: 'materialName', title: '物料名称', width: 150, align: 'center' },
    { field: 'lotNo', title: 'LOT号', width: 130, align: 'center' },
    { field: 'batchNo', title: '批次号', width: 130, align: 'center' },
    { field: 'printCount', title: '打印张数', width: 100, align: 'center' },
    { field: 'operatorName', title: '操作人', width: 100, align: 'center' },
    { field: 'printTime', title: '打印时间', width: 170, align: 'center', sortable: true },
    { field: 'printType', title: '打印类型', width: 100, align: 'center' },
    { field: 'remark', title: '备注', width: 150, align: 'center', ellipsis: true },
    { field: 'action', title: '操作', width: 140, align: 'center', fixed: 'right' }
  ],
  queryFields: [
    { field: 'printNo', label: '打印流水号', queryType: 'input' },
    { field: 'templateType', label: '模板类型', queryType: 'select' },
    { field: 'barcodeNo', label: '条码号', queryType: 'input' },
    { field: 'materialCode', label: '物料编码', queryType: 'input' },
    { field: 'materialName', label: '物料名称', queryType: 'input' },
    { field: 'printTime', label: '打印时间', queryType: 'dateRange' }
  ]
})

// 加载数据
async function loadData(params: any) {
  const { page, query, sorter } = params
  const requestParams: any = {
    pageNum: page.current,
    pageSize: page.pageSize,
    ...query
  }
  if (sorter?.field) {
    requestParams.sortField = sorter.field
    requestParams.sortOrder = sorter.order === 'ascend' ? 'asc' : 'desc'
  }
  const res = await labelRecordApi.page(requestParams)
  return res
}

// 查看详情
async function handleView(record: any) {
  try {
    const res = await labelRecordApi.detail(record.id)
    detailData.value = res
    detailVisible.value = true
  } catch (e) {
    message.error('加载详情失败')
  }
}

// 补打
async function handleReprint(record: any) {
  try {
    await labelPrintApi.reprint({
      recordId: record.id,
      reprintCount: 1
    })
    message.success('补打成功')
    tableRef.value?.reload()
  } catch (e: any) {
    message.error(e.message || '补打失败')
  }
}

// 从详情页补打
async function handleReprintFromDetail() {
  try {
    await labelPrintApi.reprint({
      recordId: detailData.value.id,
      reprintCount: 1
    })
    message.success('补打成功')
    detailVisible.value = false
    tableRef.value?.reload()
  } catch (e: any) {
    message.error(e.message || '补打失败')
  }
}

// 获取模板类型名称
function getTemplateTypeName(type: string) {
  const typeMap: Record<string, string> = {
    INCOMING: '来料标签',
    PRODUCT: '产品标签',
    LOT: 'LOT批次标签',
    CUSTOMER_MARK: '客户唛头',
    SPQ_INNER: 'SPQ内箱标签',
    PQ_OUTER: 'PQ外箱标签',
    ENG_CARD_PACKAGE: '工程卡包装标签',
    WORKSTATION: '工位标识标签',
    EQUIPMENT: '设备标识标签'
  }
  return typeMap[type] || type
}

// 格式化快照 JSON
const formattedSnapshot = computed(() => {
  try {
    if (!detailData.value.dataSnapshot) return '暂无快照数据'
    const data = typeof detailData.value.dataSnapshot === 'string'
        ? JSON.parse(detailData.value.dataSnapshot)
        : detailData.value.dataSnapshot
    return JSON.stringify(data, null, 2)
  } catch {
    return detailData.value.dataSnapshot || '暂无快照数据'
  }
})

// 格式化打印结果 JSON
const formattedPrintResult = computed(() => {
  try {
    if (!detailData.value.printResultJson) return ''
    const data = typeof detailData.value.printResultJson === 'string'
        ? JSON.parse(detailData.value.printResultJson)
        : detailData.value.printResultJson
    return JSON.stringify(data, null, 2)
  } catch {
    return detailData.value.printResultJson || ''
  }
})

// 快照数据数组（用于表格视图）
const snapshotDataArray = computed(() => {
  try {
    if (!detailData.value.dataSnapshot) return []
    const data = typeof detailData.value.dataSnapshot === 'string'
        ? JSON.parse(detailData.value.dataSnapshot)
        : detailData.value.dataSnapshot

    // 如果是数组，直接返回
    if (Array.isArray(data)) {
      return data.map((item, index) => ({ ...item, index: index + 1 }))
    }

    // 如果是对象，转换为键值对数组
    if (typeof data === 'object') {
      return Object.entries(data).map(([key, value], index) => ({
        index: index + 1,
        fieldName: key,
        fieldValue: typeof value === 'object' ? JSON.stringify(value) : String(value)
      }))
    }

    return []
  } catch {
    return []
  }
})

// 快照表格列配置
const snapshotColumns = computed(() => {
  if (snapshotDataArray.value.length === 0) return []

  const firstItem = snapshotDataArray.value[0]
  const keys = Object.keys(firstItem).filter(k => k !== 'index')

  if (keys.length === 2 && keys.includes('fieldName')) {
    // 键值对格式
    return [
      { title: '序号', dataIndex: 'index', width: 60, align: 'center' },
      { title: '字段名', dataIndex: 'fieldName', width: 200 },
      { title: '字段值', dataIndex: 'fieldValue' }
    ]
  } else {
    // 对象数组格式
    return [
      { title: '序号', dataIndex: 'index', width: 60, align: 'center' },
      ...keys.map(key => ({
        title: key,
        dataIndex: key,
        ellipsis: true
      }))
    ]
  }
})
</script>

<style scoped lang="less">
.page-container {
  padding: 16px;
  height: 100%;
}

.snapshot-section {
  margin-top: 24px;

  :deep(.ant-divider-inner-text) {
    font-size: 14px;
    font-weight: 500;
    color: #1890ff;
  }
}

.snapshot-container {
  background: #f5f5f5;
  border: 1px solid #e8e8e8;
  border-radius: 4px;
  max-height: 400px;
  overflow-y: auto;

  .json-viewer {
    padding: 16px;
    margin: 0;
    font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
    font-size: 12px;
    line-height: 1.6;
    color: #333;
    white-space: pre-wrap;
    word-wrap: break-word;
  }
}

:deep(.ant-tabs-tab) {
  font-size: 13px;
}

:deep(.ant-descriptions-item-label) {
  font-weight: 500;
  background-color: #fafafa;
}
</style>
