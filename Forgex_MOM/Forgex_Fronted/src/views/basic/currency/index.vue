<template>
  <div class="currency-page">
    <div class="page-header">
      <div>
        <a-tag color="blue">Finance MDM</a-tag>
        <h1>币种与汇率管理</h1>
        <p>维护 ISO 币种、汇率类型、汇率生效记录和操作日志。</p>
      </div>
      <a-space wrap>
        <a-button v-if="activeTab === 'currency'" v-permission="'basic:currency:add'" type="primary" @click="openCurrency()">新增币种</a-button>
        <a-button v-if="activeTab === 'rateType'" v-permission="'basic:currency:add'" type="primary" @click="openRateType()">新增汇率类型</a-button>
        <a-button v-if="activeTab === 'rate'" v-permission="'basic:exchangeRate:add'" type="primary" @click="openRate()">新增汇率</a-button>
      </a-space>
    </div>

    <a-tabs v-model:active-key="activeTab">
      <a-tab-pane key="currency" tab="币种主数据">
        <FxDynamicTable ref="currencyTableRef" table-code="CurrencyMasterTable" :request="requestCurrencies" :fallback-config="currencyTableConfig" row-key="id">
          <template #isBaseCurrency="{ record }"><a-tag :color="record.isBaseCurrency ? 'green' : 'default'">{{ record.isBaseCurrency ? '本位币' : '外币' }}</a-tag></template>
          <template #status="{ record }"><a-tag :color="record.status === 1 ? 'green' : 'red'">{{ record.status === 1 ? '启用' : '禁用' }}</a-tag></template>
          <template #action="{ record }">
            <a-space>
              <a v-permission="'basic:currency:edit'" @click="openCurrency(record)">编辑</a>
              <a v-permission="'basic:currency:setBase'" :class="{ disabled: record.isBaseCurrency }" @click="setBase(record)">设为本位币</a>
              <a v-permission="'basic:currency:edit'" @click="toggleCurrency(record)">{{ record.status === 1 ? '禁用' : '启用' }}</a>
              <a v-permission="'basic:currency:delete'" class="danger-link" @click="deleteCurrency(record)">删除</a>
            </a-space>
          </template>
        </FxDynamicTable>
      </a-tab-pane>

      <a-tab-pane key="rateType" tab="汇率类型">
        <FxDynamicTable ref="rateTypeTableRef" table-code="ExchangeRateTypeTable" :request="requestRateTypes" :fallback-config="rateTypeTableConfig" row-key="id">
          <template #isDefault="{ record }"><a-tag :color="record.isDefault ? 'green' : 'default'">{{ record.isDefault ? '默认' : '普通' }}</a-tag></template>
          <template #status="{ record }"><a-tag :color="record.status === 1 ? 'green' : 'red'">{{ record.status === 1 ? '启用' : '禁用' }}</a-tag></template>
          <template #action="{ record }">
            <a-space>
              <a v-permission="'basic:currency:edit'" @click="openRateType(record)">编辑</a>
              <a v-permission="'basic:currency:edit'" :class="{ disabled: record.isDefault }" @click="setDefaultRateType(record)">设为默认</a>
              <a v-permission="'basic:currency:delete'" class="danger-link" @click="deleteRateType(record)">删除</a>
            </a-space>
          </template>
        </FxDynamicTable>
      </a-tab-pane>

      <a-tab-pane key="rate" tab="汇率明细">
        <FxDynamicTable ref="rateTableRef" table-code="CurrencyExchangeRateTable" :request="requestRates" :fallback-config="rateTableConfig" row-key="id">
          <template #pair="{ record }">{{ record.sourceCurrencyCode }} / {{ record.targetCurrencyCode }}</template>
          <template #approveStatus="{ record }"><a-tag :color="approveColor(record.approveStatus)">{{ approveLabel(record.approveStatus) }}</a-tag></template>
          <template #action="{ record }">
            <a-space>
              <a v-permission="'basic:exchangeRate:edit'" @click="openRate(record)">编辑</a>
              <a v-permission="'basic:exchangeRate:approval'" :class="{ disabled: record.approveStatus === 3 }" @click="startRateApproval(record)">发起审批</a>
              <a v-permission="'basic:exchangeRate:delete'" class="danger-link" @click="deleteRate(record)">删除</a>
            </a-space>
          </template>
        </FxDynamicTable>
      </a-tab-pane>

      <a-tab-pane key="log" tab="操作日志">
        <FxDynamicTable ref="logTableRef" table-code="ExchangeRateLogTable" :request="requestLogs" :fallback-config="logTableConfig" row-key="id" />
      </a-tab-pane>
    </a-tabs>

    <BaseFormDialog v-model:open="currencyVisible" :title="currencyForm.id ? '编辑币种' : '新增币种'" width="720px" :loading="saving" @submit="saveCurrency">
      <a-form layout="vertical" :model="currencyForm">
        <a-row :gutter="16">
          <a-col :span="8"><a-form-item label="币种编码" required><a-input v-model:value="currencyForm.currencyCode" :disabled="!!currencyForm.id" /></a-form-item></a-col>
          <a-col :span="8"><a-form-item label="数字编码"><a-input v-model:value="currencyForm.currencyNumCode" /></a-form-item></a-col>
          <a-col :span="8"><a-form-item label="小数位" required><a-input-number v-model:value="currencyForm.decimalDigits" class="full-width" :min="0" :max="8" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="中文名称" required><a-input v-model:value="currencyForm.currencyNameCn" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="英文名称"><a-input v-model:value="currencyForm.currencyNameEn" /></a-form-item></a-col>
          <a-col :span="8"><a-form-item label="符号"><a-input v-model:value="currencyForm.currencySymbol" /></a-form-item></a-col>
          <a-col :span="8"><a-form-item label="国家/地区"><a-input v-model:value="currencyForm.countryRegion" /></a-form-item></a-col>
          <a-col :span="8"><a-form-item label="本位币"><a-switch v-model:checked="currencyForm.isBaseCurrency" /></a-form-item></a-col>
          <a-col :span="24"><a-form-item label="备注"><a-textarea v-model:value="currencyForm.remark" :rows="2" /></a-form-item></a-col>
        </a-row>
      </a-form>
    </BaseFormDialog>

    <BaseFormDialog v-model:open="rateTypeVisible" :title="rateTypeForm.id ? '编辑汇率类型' : '新增汇率类型'" width="720px" :loading="saving" @submit="saveRateType">
      <a-form layout="vertical" :model="rateTypeForm">
        <a-row :gutter="16">
          <a-col :span="12"><a-form-item label="类型编码" required><a-input v-model:value="rateTypeForm.rateTypeCode" :disabled="!!rateTypeForm.id" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="类型名称" required><a-input v-model:value="rateTypeForm.rateTypeName" /></a-form-item></a-col>
          <a-col :span="8"><a-form-item label="默认"><a-switch v-model:checked="rateTypeForm.isDefault" /></a-form-item></a-col>
          <a-col :span="8"><a-form-item label="状态"><a-select v-model:value="rateTypeForm.status" :options="statusOptions" /></a-form-item></a-col>
          <a-col :span="24"><a-form-item label="业务场景"><a-textarea v-model:value="rateTypeForm.businessScene" :rows="2" /></a-form-item></a-col>
        </a-row>
      </a-form>
    </BaseFormDialog>

    <BaseFormDialog v-model:open="rateVisible" :title="rateForm.id ? '编辑汇率' : '新增汇率'" width="760px" :loading="saving" @submit="saveRate">
      <a-form layout="vertical" :model="rateForm">
        <a-row :gutter="16">
          <a-col :span="8"><a-form-item label="源币种" required><a-input v-model:value="rateForm.sourceCurrencyCode" /></a-form-item></a-col>
          <a-col :span="8"><a-form-item label="目标币种" required><a-input v-model:value="rateForm.targetCurrencyCode" /></a-form-item></a-col>
          <a-col :span="8"><a-form-item label="汇率类型" required><a-input v-model:value="rateForm.rateTypeCode" /></a-form-item></a-col>
          <a-col :span="8"><a-form-item label="生效日期" required><a-date-picker v-model:value="rateForm.effectiveDate" value-format="YYYY-MM-DD" class="full-width" /></a-form-item></a-col>
          <a-col :span="8"><a-form-item label="失效日期"><a-date-picker v-model:value="rateForm.expireDate" value-format="YYYY-MM-DD" class="full-width" /></a-form-item></a-col>
          <a-col :span="8"><a-form-item label="汇率值" required><a-input-number v-model:value="rateForm.exchangeRate" class="full-width" :precision="8" /></a-form-item></a-col>
          <a-col :span="8"><a-form-item label="组织ID"><a-input-number v-model:value="rateForm.orgId" class="full-width" /></a-form-item></a-col>
          <a-col :span="24"><a-form-item label="备注"><a-textarea v-model:value="rateForm.remark" :rows="2" /></a-form-item></a-col>
        </a-row>
      </a-form>
    </BaseFormDialog>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { Modal, message } from 'ant-design-vue'
import BaseFormDialog from '@/components/common/BaseFormDialog.vue'
import FxDynamicTable from '@/components/common/FxDynamicTable.vue'
import { currencyApi, exchangeRateApi, rateTypeApi, type Currency, type ExchangeRate, type RateType } from '@/api/basic/currency'

const activeTab = ref('currency')
const currencyTableRef = ref()
const rateTypeTableRef = ref()
const rateTableRef = ref()
const saving = ref(false)
const currencyVisible = ref(false)
const rateTypeVisible = ref(false)
const rateVisible = ref(false)
const currencyForm = ref<Currency>(emptyCurrency())
const rateTypeForm = ref<RateType>(emptyRateType())
const rateForm = ref<ExchangeRate>(emptyRate())
const statusOptions = [{ label: '启用', value: 1 }, { label: '禁用', value: 0 }]

const currencyTableConfig = tableConfig('CurrencyMasterTable', '币种主数据', [
  ['currencyCode', '币种编码', 120], ['currencyNameCn', '中文名称', 160], ['currencyNameEn', '英文名称', 180],
  ['currencySymbol', '符号', 80], ['decimalDigits', '小数位', 90], ['isBaseCurrency', '本位币', 100],
  ['status', '状态', 90], ['action', '操作', 260],
])
const rateTypeTableConfig = tableConfig('ExchangeRateTypeTable', '汇率类型', [
  ['rateTypeCode', '类型编码', 140], ['rateTypeName', '类型名称', 160], ['businessScene', '业务场景', 260],
  ['isDefault', '默认', 90], ['status', '状态', 90], ['action', '操作', 220],
])
const rateTableConfig = tableConfig('CurrencyExchangeRateTable', '汇率明细', [
  ['pair', '币种对', 140], ['rateTypeCode', '汇率类型', 130], ['effectiveDate', '生效日期', 120],
  ['expireDate', '失效日期', 120], ['exchangeRate', '汇率值', 130], ['approveStatus', '审批状态', 120],
  ['action', '操作', 240],
])
const logTableConfig = tableConfig('ExchangeRateLogTable', '汇率日志', [
  ['rateId', '汇率ID', 100], ['operationType', '操作类型', 120], ['operationContent', '操作内容', 260], ['createTime', '操作时间', 180],
])

function tableConfig(tableCode: string, tableName: string, defs: any[]) {
  return {
    tableCode, tableName, tableType: 'BUSINESS', rowKey: 'id', defaultPageSize: 10,
    columns: defs.map((item, index) => ({ field: item[0], title: item[1], width: item[2], visible: true, order: index + 1, fixed: item[0] === 'action' ? 'right' : undefined })),
    queryFields: [],
  }
}

function emptyCurrency(): Currency { return { currencyCode: '', decimalDigits: 2, status: 1, isBaseCurrency: false } }
function emptyRateType(): RateType { return { rateTypeCode: '', status: 1, isDefault: false } }
function emptyRate(): ExchangeRate { return { sourceCurrencyCode: '', targetCurrencyCode: '', rateTypeCode: '', approveStatus: 0 } }

async function requestCurrencies(payload: any) {
  const result = await currencyApi.page({ pageNum: payload.page.current, pageSize: payload.page.pageSize, ...payload.query })
  return { records: result.records || [], total: Number(result.total || 0) }
}
async function requestRateTypes(payload: any) {
  const result = await rateTypeApi.page({ pageNum: payload.page.current, pageSize: payload.page.pageSize, ...payload.query })
  return { records: result.records || [], total: Number(result.total || 0) }
}
async function requestRates(payload: any) {
  const result = await exchangeRateApi.page({ pageNum: payload.page.current, pageSize: payload.page.pageSize, ...payload.query })
  return { records: result.records || [], total: Number(result.total || 0) }
}
async function requestLogs(payload: any) {
  const result = await exchangeRateApi.logPage({ pageNum: payload.page.current, pageSize: payload.page.pageSize, ...payload.query })
  return { records: result.records || [], total: Number(result.total || 0) }
}

function openCurrency(record?: Currency) { currencyForm.value = record ? { ...record } : emptyCurrency(); currencyVisible.value = true }
function openRateType(record?: RateType) { rateTypeForm.value = record ? { ...record } : emptyRateType(); rateTypeVisible.value = true }
function openRate(record?: ExchangeRate) { rateForm.value = record ? { ...record } : emptyRate(); rateVisible.value = true }

async function saveCurrency() {
  saving.value = true
  try {
    currencyForm.value.id ? await currencyApi.update(currencyForm.value) : await currencyApi.create(currencyForm.value)
    currencyVisible.value = false
    await currencyTableRef.value?.refresh?.()
  } finally { saving.value = false }
}
async function saveRateType() {
  saving.value = true
  try {
    rateTypeForm.value.id ? await rateTypeApi.update(rateTypeForm.value) : await rateTypeApi.create(rateTypeForm.value)
    rateTypeVisible.value = false
    await rateTypeTableRef.value?.refresh?.()
  } finally { saving.value = false }
}
async function saveRate() {
  saving.value = true
  try {
    rateForm.value.id ? await exchangeRateApi.update(rateForm.value) : await exchangeRateApi.create(rateForm.value)
    rateVisible.value = false
    await rateTableRef.value?.refresh?.()
  } finally { saving.value = false }
}

async function setBase(record: Currency) { await currencyApi.setBase(record.id!); await currencyTableRef.value?.refresh?.() }
async function toggleCurrency(record: Currency) { record.status === 1 ? await currencyApi.disable(record.id!) : await currencyApi.enable(record.id!); await currencyTableRef.value?.refresh?.() }
function deleteCurrency(record: Currency) { Modal.confirm({ title: '确认删除币种？', async onOk() { await currencyApi.delete(record.id!); await currencyTableRef.value?.refresh?.() } }) }
async function setDefaultRateType(record: RateType) { await rateTypeApi.setDefault(record.id!); await rateTypeTableRef.value?.refresh?.() }
function deleteRateType(record: RateType) { Modal.confirm({ title: '确认删除汇率类型？', async onOk() { await rateTypeApi.delete(record.id!); await rateTypeTableRef.value?.refresh?.() } }) }
async function startRateApproval(record: ExchangeRate) { await exchangeRateApi.startApproval(record.id!); message.success('已发起审批'); await rateTableRef.value?.refresh?.() }
function deleteRate(record: ExchangeRate) { Modal.confirm({ title: '确认删除汇率？', async onOk() { await exchangeRateApi.delete(record.id!); await rateTableRef.value?.refresh?.() } }) }
function approveLabel(value?: number) { return ({ 0: '待审批', 1: '已生效', 2: '已驳回', 3: '审批中' } as Record<number, string>)[Number(value)] || '-' }
function approveColor(value?: number) { return ({ 0: 'orange', 1: 'green', 2: 'red', 3: 'processing' } as Record<number, string>)[Number(value)] || 'default' }
</script>

<style scoped lang="less">
.currency-page { min-height: 100%; padding: 20px; background: var(--fx-bg-layout, #f8fafc); }
.page-header { display: flex; justify-content: space-between; gap: 20px; margin-bottom: 16px; padding: 24px 28px; border: 1px solid var(--fx-border-color, #e5e7eb); border-radius: 8px; background: var(--fx-bg-container, #fff); }
.page-header h1 { margin: 10px 0 8px; font-size: 28px; }
.page-header p { margin: 0; color: var(--fx-text-secondary, #64748b); }
.danger-link { color: #ff4d4f; }
.disabled { color: var(--fx-text-disabled, #bfbfbf); cursor: not-allowed; pointer-events: none; }
.full-width { width: 100%; }
@media (max-width: 768px) { .currency-page { padding: 12px; } .page-header { flex-direction: column; padding: 18px; } }
</style>
