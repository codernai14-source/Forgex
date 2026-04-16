<template>
  <div class="page-container">
    <FxDynamicTable
        ref="tableRef"
        :request="loadData"
        table-code="label_print_record_table"
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
    <a-modal v-model:open="detailVisible" title="打印记录详情" width="800px" :footer="null">
      <a-descriptions :column="2" bordered>
        <a-descriptions-item label="打印流水号">{{ detailData.printNo }}</a-descriptions-item>
        <a-descriptions-item label="模板类型">
          {{ getTemplateTypeName(detailData.templateType) }}
        </a-descriptions-item>
        <a-descriptions-item label="模板名称">{{ detailData.templateName }}</a-descriptions-item>
        <a-descriptions-item label="模板版本">{{ detailData.templateVersion }}</a-descriptions-item>
        <a-descriptions-item label="打印张数">{{ detailData.printCount }}</a-descriptions-item>
        <a-descriptions-item label="打印时间">{{ detailData.printTime }}</a-descriptions-item>
        <a-descriptions-item label="操作人">{{ detailData.operatorName }}</a-descriptions-item>
        <a-descriptions-item label="是否补打">
          <a-tag v-if="detailData.isReprint" color="orange">是</a-tag>
          <a-tag v-else color="green">否</a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="备注" :span="2">{{ detailData.remark }}</a-descriptions-item>
      </a-descriptions>

      <a-divider>打印数据快照</a-divider>
      <pre class="snapshot-json">{{ detailData.snapshotJson }}</pre>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { message } from 'ant-design-vue'
import { labelRecordApi } from '@/api/label/record'
import { labelPrintApi } from '@/api/label/print'

const tableRef = ref()
const detailVisible = ref(false)
const detailData = ref<any>({})

// 加载数据
function loadData(params: any) {
  return labelRecordApi.page(params)
}

// 查看详情
async function handleView(record: any) {
  try {
    detailData.value = await labelRecordApi.detail(record.id)
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
</script>

<style scoped lang="less">
.page-container {
  padding: 16px;
  height: 100%;
}

.snapshot-json {
  background: #f5f5f5;
  padding: 12px;
  border-radius: 4px;
  max-height: 300px;
  overflow-y: auto;
  font-size: 12px;
}
</style>
