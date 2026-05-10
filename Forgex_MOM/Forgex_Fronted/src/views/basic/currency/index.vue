<template>
  <div class="currency-page">
    <div class="page-header">
      <div>
        <a-tag color="blue">Finance MDM</a-tag>
        <h1>{{ t('basic.currency.title') }}</h1>
        <p>{{ t('basic.currency.subtitle') }}</p>
      </div>
      <a-space wrap>
        <a-button v-if="activeTab === 'currency'" v-permission="'basic:currency:add'" type="primary" @click="openCurrency()">
          {{ t('basic.currency.addCurrency') }}
        </a-button>
        <a-button v-if="activeTab === 'rateType'" v-permission="'basic:currency:add'" type="primary" @click="openRateType()">
          {{ t('basic.currency.addRateType') }}
        </a-button>
        <a-button v-if="activeTab === 'rate'" v-permission="'basic:exchangeRate:add'" type="primary" @click="openRate()">
          {{ t('basic.currency.addRate') }}
        </a-button>
      </a-space>
    </div>

    <a-tabs v-model:active-key="activeTab">
      <a-tab-pane key="currency" :tab="t('basic.currency.tabs.currency')">
        <FxDynamicTable ref="currencyTableRef" table-code="CurrencyMasterTable" :request="requestCurrencies" row-key="id">
          <template #isBaseCurrency="{ record }">
            <a-tag :color="record.isBaseCurrency ? 'green' : 'default'">
              {{ record.isBaseCurrency ? t('basic.currency.baseCurrency') : t('basic.currency.foreignCurrency') }}
            </a-tag>
          </template>
          <template #status="{ record }">
            <a-tag :color="record.status === 1 ? 'green' : 'red'">
              {{ record.status === 1 ? t('common.enabled') : t('common.disabled') }}
            </a-tag>
          </template>
          <template #action="{ record }">
            <a-space>
              <a v-permission="'basic:currency:edit'" @click="openCurrency(record)">{{ t('common.edit') }}</a>
              <a v-permission="'basic:currency:setBase'" :class="{ disabled: record.isBaseCurrency }" @click="setBase(record)">
                {{ t('basic.currency.setBaseCurrency') }}
              </a>
              <a v-permission="'basic:currency:edit'" @click="toggleCurrency(record)">
                {{ record.status === 1 ? t('common.disable') : t('common.enable') }}
              </a>
              <a v-permission="'basic:currency:delete'" class="danger-link" @click="deleteCurrency(record)">{{ t('common.delete') }}</a>
            </a-space>
          </template>
        </FxDynamicTable>
      </a-tab-pane>

      <a-tab-pane key="rateType" :tab="t('basic.currency.tabs.rateType')">
        <FxDynamicTable ref="rateTypeTableRef" table-code="ExchangeRateTypeTable" :request="requestRateTypes" row-key="id">
          <template #isDefault="{ record }">
            <a-tag :color="record.isDefault ? 'green' : 'default'">
              {{ record.isDefault ? t('common.default') : t('basic.currency.normal') }}
            </a-tag>
          </template>
          <template #status="{ record }">
            <a-tag :color="record.status === 1 ? 'green' : 'red'">
              {{ record.status === 1 ? t('common.enabled') : t('common.disabled') }}
            </a-tag>
          </template>
          <template #action="{ record }">
            <a-space>
              <a v-permission="'basic:currency:edit'" @click="openRateType(record)">{{ t('common.edit') }}</a>
              <a v-permission="'basic:currency:edit'" :class="{ disabled: record.isDefault }" @click="setDefaultRateType(record)">
                {{ t('basic.currency.setDefault') }}
              </a>
              <a v-permission="'basic:currency:delete'" class="danger-link" @click="deleteRateType(record)">{{ t('common.delete') }}</a>
            </a-space>
          </template>
        </FxDynamicTable>
      </a-tab-pane>

      <a-tab-pane key="rate" :tab="t('basic.currency.tabs.rate')">
        <FxDynamicTable ref="rateTableRef" table-code="CurrencyExchangeRateTable" :request="requestRates" row-key="id">
          <template #pair="{ record }">{{ record.sourceCurrencyCode }} / {{ record.targetCurrencyCode }}</template>
          <template #approveStatus="{ record }">
            <a-tag :color="approveColor(record.approveStatus)">{{ approveLabel(record.approveStatus) }}</a-tag>
          </template>
          <template #action="{ record }">
            <a-space>
              <a v-permission="'basic:exchangeRate:edit'" @click="openRate(record)">{{ t('common.edit') }}</a>
              <a v-permission="'basic:exchangeRate:approval'" :class="{ disabled: record.approveStatus === 3 }" @click="startRateApproval(record)">
                {{ t('basic.currency.startApproval') }}
              </a>
              <a v-permission="'basic:exchangeRate:delete'" class="danger-link" @click="deleteRate(record)">{{ t('common.delete') }}</a>
            </a-space>
          </template>
        </FxDynamicTable>
      </a-tab-pane>

      <a-tab-pane key="log" :tab="t('basic.currency.tabs.log')">
        <FxDynamicTable ref="logTableRef" table-code="ExchangeRateLogTable" :request="requestLogs" row-key="id" />
      </a-tab-pane>
    </a-tabs>

    <BaseFormDialog v-model:open="currencyVisible" :title="currencyForm.id ? t('basic.currency.editCurrency') : t('basic.currency.addCurrency')" width="720px" :loading="saving" @submit="saveCurrency">
      <a-form layout="vertical" :model="currencyForm">
        <a-row :gutter="16">
          <a-col :span="8"><a-form-item :label="t('basic.currency.currencyCode')" required><a-input v-model:value="currencyForm.currencyCode" :disabled="!!currencyForm.id" /></a-form-item></a-col>
          <a-col :span="8"><a-form-item :label="t('basic.currency.numericCode')"><a-input v-model:value="currencyForm.currencyNumCode" /></a-form-item></a-col>
          <a-col :span="8"><a-form-item :label="t('basic.currency.decimalDigits')" required><a-input-number v-model:value="currencyForm.decimalDigits" class="full-width" :min="0" :max="8" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item :label="t('basic.currency.chineseName')" required><a-input v-model:value="currencyForm.currencyNameCn" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item :label="t('basic.currency.englishName')"><a-input v-model:value="currencyForm.currencyNameEn" /></a-form-item></a-col>
          <a-col :span="8"><a-form-item :label="t('basic.currency.symbol')"><a-input v-model:value="currencyForm.currencySymbol" /></a-form-item></a-col>
          <a-col :span="8"><a-form-item :label="t('basic.currency.countryRegion')"><a-input v-model:value="currencyForm.countryRegion" /></a-form-item></a-col>
          <a-col :span="8"><a-form-item :label="t('basic.currency.baseCurrency')"><a-switch v-model:checked="currencyForm.isBaseCurrency" /></a-form-item></a-col>
          <a-col :span="24"><a-form-item :label="t('common.remark')"><a-textarea v-model:value="currencyForm.remark" :rows="2" /></a-form-item></a-col>
        </a-row>
      </a-form>
    </BaseFormDialog>

    <BaseFormDialog v-model:open="rateTypeVisible" :title="rateTypeForm.id ? t('basic.currency.editRateType') : t('basic.currency.addRateType')" width="720px" :loading="saving" @submit="saveRateType">
      <a-form layout="vertical" :model="rateTypeForm">
        <a-row :gutter="16">
          <a-col :span="12"><a-form-item :label="t('basic.currency.typeCode')" required><a-input v-model:value="rateTypeForm.rateTypeCode" :disabled="!!rateTypeForm.id" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item :label="t('basic.currency.typeName')" required><a-input v-model:value="rateTypeForm.rateTypeName" /></a-form-item></a-col>
          <a-col :span="8"><a-form-item :label="t('common.default')"><a-switch v-model:checked="rateTypeForm.isDefault" /></a-form-item></a-col>
          <a-col :span="8"><a-form-item :label="t('common.status')"><a-select v-model:value="rateTypeForm.status" :options="statusOptions" /></a-form-item></a-col>
          <a-col :span="24"><a-form-item :label="t('basic.currency.businessScene')"><a-textarea v-model:value="rateTypeForm.businessScene" :rows="2" /></a-form-item></a-col>
        </a-row>
      </a-form>
    </BaseFormDialog>

    <BaseFormDialog v-model:open="rateVisible" :title="rateForm.id ? t('basic.currency.editRate') : t('basic.currency.addRate')" width="760px" :loading="saving" @submit="saveRate">
      <a-form layout="vertical" :model="rateForm">
        <a-row :gutter="16">
          <a-col :span="8"><a-form-item :label="t('basic.currency.sourceCurrency')" required><a-input v-model:value="rateForm.sourceCurrencyCode" /></a-form-item></a-col>
          <a-col :span="8"><a-form-item :label="t('basic.currency.targetCurrency')" required><a-input v-model:value="rateForm.targetCurrencyCode" /></a-form-item></a-col>
          <a-col :span="8"><a-form-item :label="t('basic.currency.rateType')" required><a-input v-model:value="rateForm.rateTypeCode" /></a-form-item></a-col>
          <a-col :span="8"><a-form-item :label="t('basic.currency.effectiveDate')" required><a-date-picker v-model:value="rateForm.effectiveDate" value-format="YYYY-MM-DD" class="full-width" /></a-form-item></a-col>
          <a-col :span="8"><a-form-item :label="t('basic.currency.expireDate')"><a-date-picker v-model:value="rateForm.expireDate" value-format="YYYY-MM-DD" class="full-width" /></a-form-item></a-col>
          <a-col :span="8"><a-form-item :label="t('basic.currency.exchangeRate')" required><a-input-number v-model:value="rateForm.exchangeRate" class="full-width" :precision="8" /></a-form-item></a-col>
          <a-col :span="8"><a-form-item :label="t('basic.currency.orgId')"><a-input-number v-model:value="rateForm.orgId" class="full-width" /></a-form-item></a-col>
          <a-col :span="24"><a-form-item :label="t('common.remark')"><a-textarea v-model:value="rateForm.remark" :rows="2" /></a-form-item></a-col>
        </a-row>
      </a-form>
    </BaseFormDialog>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { Modal, message } from 'ant-design-vue'
import { useI18n } from 'vue-i18n'
import BaseFormDialog from '@/components/common/BaseFormDialog.vue'
import FxDynamicTable from '@/components/common/FxDynamicTable.vue'
import { currencyApi, exchangeRateApi, rateTypeApi, type Currency, type ExchangeRate, type RateType } from '@/api/basic/currency'

const { t } = useI18n()
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
const statusOptions = computed(() => [
  { label: t('common.enabled'), value: 1 },
  { label: t('common.disabled'), value: 0 },
])

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
function deleteCurrency(record: Currency) { Modal.confirm({ title: t('basic.currency.confirmDeleteCurrency'), async onOk() { await currencyApi.delete(record.id!); await currencyTableRef.value?.refresh?.() } }) }
async function setDefaultRateType(record: RateType) { await rateTypeApi.setDefault(record.id!); await rateTypeTableRef.value?.refresh?.() }
function deleteRateType(record: RateType) { Modal.confirm({ title: t('basic.currency.confirmDeleteRateType'), async onOk() { await rateTypeApi.delete(record.id!); await rateTypeTableRef.value?.refresh?.() } }) }
async function startRateApproval(record: ExchangeRate) { await exchangeRateApi.startApproval(record.id!); message.success(t('basic.currency.approvalStarted')); await rateTableRef.value?.refresh?.() }
function deleteRate(record: ExchangeRate) { Modal.confirm({ title: t('basic.currency.confirmDeleteRate'), async onOk() { await exchangeRateApi.delete(record.id!); await rateTableRef.value?.refresh?.() } }) }
function approveLabel(value?: number) {
  const key = ({ 0: 'basic.currency.approval.pending', 1: 'basic.currency.approval.effective', 2: 'basic.currency.approval.rejected', 3: 'basic.currency.approval.processing' } as Record<number, string>)[Number(value)] || 'common.unknown'
  return t(key)
}
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
