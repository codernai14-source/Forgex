<template>
  <div class="page-container">
    <FxDynamicTable
        ref="tableRef"
        :request="loadData"
        table-code="LabelPrintRecordTable"
    >
      <!-- 自定义列渲染 -->
      <template #templateType="{ record }">
        <a-tag color="blue">{{ getTemplateTypeName(record.templateType) }}</a-tag>
      </template>

      <template #isReprint="{ record }">
        <a-tag v-if="record.isReprint" color="orange">{{ t('label.record.reprint') }}</a-tag>
        <a-tag v-else color="green">{{ t('label.record.firstPrint') }}</a-tag>
      </template>

      <!-- 行操作 -->
      <template #action="{ record }">
        <a-space>
          <a @click="handleView(record)">{{ t('common.detail') }}</a>
          <a @click="handleReprint(record)">{{ t('label.record.reprint') }}</a>
        </a-space>
      </template>
    </FxDynamicTable>

    <!-- 详情弹窗 -->
    <a-drawer
        v-model:open="detailVisible"
        :title="t('label.record.detailTitle')"
        width="900px"
        placement="right"
    >
      <a-descriptions :column="2" bordered size="small">
        <a-descriptions-item :label="t('label.record.printNo')">
          <a-tag color="blue">{{ detailData.printNo }}</a-tag>
        </a-descriptions-item>
        <a-descriptions-item :label="t('label.record.templateType')">
          <a-tag color="blue">{{ getTemplateTypeName(detailData.templateType) }}</a-tag>
        </a-descriptions-item>
        <a-descriptions-item :label="t('label.record.templateName')">{{ detailData.templateName }}</a-descriptions-item>
        <a-descriptions-item :label="t('label.record.templateVersion')">v{{ detailData.templateVersion }}</a-descriptions-item>
        <a-descriptions-item :label="t('label.record.barcodeNo')">
          <a-tag>{{ detailData.barcodeNo || '-' }}</a-tag>
        </a-descriptions-item>
        <a-descriptions-item :label="t('label.record.lotNo')">{{ detailData.lotNo || '-' }}</a-descriptions-item>
        <a-descriptions-item :label="t('label.record.batchNo')">{{ detailData.batchNo || '-' }}</a-descriptions-item>
        <a-descriptions-item :label="t('label.record.materialCode')">{{ detailData.materialCode || '-' }}</a-descriptions-item>
        <a-descriptions-item :label="t('label.record.materialName')">{{ detailData.materialName || '-' }}</a-descriptions-item>
        <a-descriptions-item :label="t('label.record.printCount')">
          <a-tag color="green">{{ detailData.printCount }} {{ t('label.record.sheetUnit') }}</a-tag>
        </a-descriptions-item>
        <a-descriptions-item :label="t('label.record.printType')">
          <a-tag v-if="detailData.printType === 'REPRINT'" color="orange">{{ t('label.record.reprint') }}</a-tag>
          <a-tag v-else color="green">{{ t('label.record.normalPrint') }}</a-tag>
        </a-descriptions-item>
        <a-descriptions-item :label="t('label.record.operator')">{{ detailData.operatorName }}</a-descriptions-item>
        <a-descriptions-item :label="t('label.record.printTime')">{{ detailData.printTime }}</a-descriptions-item>
        <a-descriptions-item :label="t('common.remark')" :span="2">{{ detailData.remark || '-' }}</a-descriptions-item>
      </a-descriptions>

      <!-- 打印数据快照 -->
      <div class="snapshot-section">
        <a-divider orientation="left">
          <DatabaseOutlined /> {{ t('label.record.snapshot') }}
        </a-divider>

        <a-tabs v-model:activeKey="snapshotActiveKey" type="card" size="small">
          <!-- JSON 视图 -->
          <a-tab-pane key="json" :tab="t('label.record.jsonFormat')">
            <div class="snapshot-container">
              <pre class="json-viewer">{{ formattedSnapshot }}</pre>
            </div>
          </a-tab-pane>

          <!-- 表格视图 -->
          <a-tab-pane key="table" :tab="t('label.record.tableView')" v-if="snapshotDataArray.length > 0">
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
          <FileTextOutlined /> {{ t('label.record.printResultTemplate') }}
        </a-divider>
        <a-collapse v-model:activeKey="resultCollapseKey">
          <a-collapse-panel key="1" :header="t('label.record.viewPrintTemplateContent')">
            <div class="snapshot-container">
              <pre class="json-viewer">{{ formattedPrintResult }}</pre>
            </div>
          </a-collapse-panel>
        </a-collapse>
      </div>

      <template #footer>
        <a-space>
          <a-button @click="detailVisible = false">{{ t('common.close') }}</a-button>
          <a-button type="primary" @click="handleReprintFromDetail">
            <PrinterOutlined /> {{ t('label.record.reprint') }}
          </a-button>
        </a-space>
      </template>
    </a-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { message } from 'ant-design-vue'
import { DatabaseOutlined, FileTextOutlined, PrinterOutlined } from '@ant-design/icons-vue'
import { labelRecordApi } from '@/api/label/record'
import { labelPrintApi } from '@/api/label/print'
const { t } = useI18n()
const tableRef = ref()
const detailVisible = ref(false)
const detailData = ref<any>({})
const snapshotActiveKey = ref('json')
const resultCollapseKey = ref([])

/**
 * 打印记录表格降级配置
 */

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
    message.error(t('label.record.loadDetailFailed'))
  }
}

// 补打
async function handleReprint(record: any) {
  try {
    await labelPrintApi.reprint({
      recordId: record.id,
      reprintCount: 1
    })
    message.success(t('label.record.reprintSuccess'))
    tableRef.value?.reload()
  } catch (e: any) {
    message.error(e.message || t('label.record.reprintFailed'))
  }
}

// 从详情页补打
async function handleReprintFromDetail() {
  try {
    await labelPrintApi.reprint({
      recordId: detailData.value.id,
      reprintCount: 1
    })
    message.success(t('label.record.reprintSuccess'))
    detailVisible.value = false
    tableRef.value?.reload()
  } catch (e: any) {
    message.error(e.message || t('label.record.reprintFailed'))
  }
}

// 获取模板类型名称
function getTemplateTypeName(type: string) {
  const typeMap: Record<string, string> = {
    INCOMING: t('label.templateTypes.INCOMING'),
    PRODUCT: t('label.templateTypes.PRODUCT'),
    LOT: t('label.templateTypes.LOT'),
    CUSTOMER_MARK: t('label.templateTypes.CUSTOMER_MARK'),
    SPQ_INNER: t('label.templateTypes.SPQ_INNER'),
    PQ_OUTER: t('label.templateTypes.PQ_OUTER'),
    ENG_CARD_PACKAGE: t('label.templateTypes.ENG_CARD_PACKAGE'),
    WORKSTATION: t('label.templateTypes.WORKSTATION'),
    EQUIPMENT: t('label.templateTypes.EQUIPMENT')
  }
  return typeMap[type] || type
}

// 格式化快照 JSON
const formattedSnapshot = computed(() => {
  try {
    if (!detailData.value.dataSnapshot) return t('label.record.noSnapshotData')
    const data = typeof detailData.value.dataSnapshot === 'string'
        ? JSON.parse(detailData.value.dataSnapshot)
        : detailData.value.dataSnapshot
    return JSON.stringify(data, null, 2)
  } catch {
    return detailData.value.dataSnapshot || t('label.record.noSnapshotData')
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
      { title: t('label.record.sequence'), dataIndex: 'index', width: 60, align: 'center' },
      { title: t('label.record.fieldName'), dataIndex: 'fieldName', width: 200 },
      { title: t('label.record.fieldValue'), dataIndex: 'fieldValue' }
    ]
  } else {
    // 对象数组格式
    return [
      { title: t('label.record.sequence'), dataIndex: 'index', width: 60, align: 'center' },
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

