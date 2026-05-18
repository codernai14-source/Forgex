<template>
  <div class="master-page">
    <div class="page-header">
      <div>
        <a-tag color="blue">Master Data</a-tag>
        <h1>{{ t('basic.customer.title') }}</h1>
        <p>{{ t('basic.customer.subtitle') }}</p>
      </div>
      <a-space wrap>
        <a-button v-permission="'basic:customer:add'" type="primary" @click="openCreate">{{ t('basic.customer.addCustomer') }}</a-button>
      </a-space>
    </div>

    <FxDynamicTable
      ref="tableRef"
      table-code="CustomerMasterTable"
      :request="handleRequest"
      :dict-options="dictOptions"
      row-key="id"
      :row-selection="rowSelection"
    >
      <template #toolbar>
        <a-button
          v-permission="'basic:customer:batchDelete'"
          danger
          :disabled="!selectedCount"
          @click="handleBatchDelete"
        >
          {{ t('common.batchDelete') }}
        </a-button>
      </template>
      <template #customerFullName="{ record }">
        <div class="name-cell">
          <strong>{{ record.customerFullName || record.customerName }}</strong>
          <span>{{ record.customerShortName || '-' }}</span>
        </div>
      </template>
      <template #isRelatedTenant="{ record }">
        <a-tag :color="record.isRelatedTenant || record.relatedTenantCode ? 'green' : 'default'">
          {{ record.isRelatedTenant || record.relatedTenantCode ? t('basic.customer.related') : t('basic.customer.notRelated') }}
        </a-tag>
      </template>
      <template #approvalStatus="{ record }">
        <a-tag :color="approvalColor(record.approvalStatus)">
          {{ labelOf(approvalOptions, record.approvalStatus) }}
        </a-tag>
      </template>
      <template #action="{ record }">
        <a-space>
          <a v-permission="'basic:customer:query'" @click="openDetail(record)">{{ t('common.detail') }}</a>
          <a v-permission="'basic:customer:edit'" @click="openEdit(record)">{{ t('common.edit') }}</a>
          <a
            v-permission="'basic:customer:generateTenant'"
            :class="{ disabled: record.isRelatedTenant || record.relatedTenantCode }"
            @click="handleGenerateTenant(record)"
          >
            {{ t('basic.customer.createCustomerTenant') }}
          </a>
          <a
            v-permission="'basic:customer:approval'"
            :class="{ disabled: record.approvalStatus === 1 }"
            @click="handleStartApproval(record)"
          >
            {{ t('basic.customer.startApproval') }}
          </a>
          <a
            v-permission="'basic:customer:delete'"
            class="danger-link"
            :class="{ disabled: record.isRelatedTenant || record.relatedTenantCode }"
            @click="handleDelete(record)"
          >
            {{ t('common.delete') }}
          </a>
        </a-space>
      </template>
    </FxDynamicTable>

    <BaseFormDialog
      v-model:open="editorVisible"
      :title="editorTitle"
      width="1080px"
      :loading="saving"
      :mask-closable="true"
      :body-style="{ maxHeight: '72vh', overflowY: 'auto' }"
      @submit="handleSave"
      @cancel="editorVisible = false"
    >
      <a-tabs v-model:active-key="activeTab">
        <a-tab-pane key="main" :tab="t('basic.customer.tabs.main')">
          <a-form layout="vertical" :model="form">
            <a-row :gutter="16">
              <a-col :span="8">
                <a-form-item :label="t('basic.customer.autoGenerateCode')">
                  <a-switch v-model:checked="form.autoGenerateCode" :disabled="!!form.id || readonly" />
                </a-form-item>
              </a-col>
              <a-col :span="8">
                <a-form-item :label="t('basic.customer.customerCode')" required>
                  <a-input v-model:value="form.customerCode" :disabled="!!form.id || readonly || form.autoGenerateCode" />
                </a-form-item>
              </a-col>
              <a-col :span="8">
                <a-form-item :label="t('basic.customer.customerFullName')" required>
                  <a-input v-model:value="form.customerFullName" :disabled="readonly" />
                </a-form-item>
              </a-col>
              <a-col :span="8">
                <a-form-item :label="t('basic.customer.customerShortName')">
                  <a-input v-model:value="form.customerShortName" :disabled="readonly" />
                </a-form-item>
              </a-col>
              <a-col :span="8">
                <a-form-item :label="t('basic.customer.customerValueLevel')">
                  <a-select v-model:value="form.customerValueLevel" :disabled="readonly" :options="valueLevelOptions" allow-clear />
                </a-form-item>
              </a-col>
              <a-col :span="8">
                <a-form-item :label="t('basic.customer.customerCreditLevel')">
                  <a-select v-model:value="form.customerCreditLevel" :disabled="readonly" :options="creditOptions" allow-clear />
                </a-form-item>
              </a-col>
              <a-col :span="8">
                <a-form-item :label="t('basic.customer.businessStatus')">
                  <a-select v-model:value="form.businessStatus" :disabled="readonly" :options="businessStatusOptions" allow-clear />
                </a-form-item>
              </a-col>
              <a-col :span="8">
                <a-form-item :label="t('basic.customer.approvalStatus')">
                  <a-select v-model:value="form.approvalStatus" :disabled="readonly" :options="approvalOptions" allow-clear />
                </a-form-item>
              </a-col>
              <a-col :span="8">
                <a-form-item :label="t('basic.customer.isRelatedTenant')">
                  <a-switch v-model:checked="form.isRelatedTenant" :disabled="readonly" />
                </a-form-item>
              </a-col>
              <a-col :span="8">
                <a-form-item :label="t('basic.customer.relatedTenantCode')">
                  <a-input v-model:value="form.relatedTenantCode" :disabled="readonly" />
                </a-form-item>
              </a-col>
              <a-col :span="8">
                <a-form-item :label="t('basic.customer.transportMode')">
                  <a-input v-model:value="form.transportMode" :disabled="readonly" />
                </a-form-item>
              </a-col>
              <a-col :span="8">
                <a-form-item :label="t('basic.customer.paymentTerms')">
                  <a-input v-model:value="form.paymentTerms" :disabled="readonly" />
                </a-form-item>
              </a-col>
              <a-col :span="8">
                <a-form-item :label="t('basic.customer.country')">
                  <a-input v-model:value="form.country" :disabled="readonly" />
                </a-form-item>
              </a-col>
              <a-col :span="8">
                <a-form-item :label="t('basic.customer.enterpriseNature')">
                  <a-select v-model:value="form.enterpriseNature" :disabled="readonly" :options="enterpriseNatureOptions" allow-clear />
                </a-form-item>
              </a-col>
              <a-col :span="24">
                <a-form-item :label="t('basic.customer.actualBusinessAddress')">
                  <a-textarea v-model:value="form.actualBusinessAddress" :disabled="readonly" :rows="2" />
                </a-form-item>
              </a-col>
              <a-col :span="12">
                <a-form-item :label="t('basic.customer.collectionAddress')">
                  <a-textarea v-model:value="form.collectionAddress" :disabled="readonly" :rows="2" />
                </a-form-item>
              </a-col>
              <a-col :span="12">
                <a-form-item :label="t('basic.customer.shippingAddress')">
                  <a-textarea v-model:value="form.shippingAddress" :disabled="readonly" :rows="2" />
                </a-form-item>
              </a-col>
            </a-row>
          </a-form>
        </a-tab-pane>
        <a-tab-pane key="contacts" :tab="t('basic.customer.tabs.contacts')">
          <div class="sub-toolbar"><a-button v-if="!readonly" type="dashed" @click="addContact">{{ t('basic.customer.addContact') }}</a-button></div>
          <a-table :data-source="form.contactList" :pagination="false" row-key="id" size="small">
            <a-table-column :title="t('basic.customer.contactName')"><template #default="{ record }"><a-input v-model:value="record.contactName" :disabled="readonly" /></template></a-table-column>
            <a-table-column :title="t('basic.customer.contactPosition')"><template #default="{ record }"><a-input v-model:value="record.contactPosition" :disabled="readonly" /></template></a-table-column>
            <a-table-column :title="t('basic.customer.contactPhone')"><template #default="{ record }"><a-input v-model:value="record.contactPhone" :disabled="readonly" /></template></a-table-column>
            <a-table-column v-if="!readonly" :title="t('common.operation')" width="80"><template #default="{ index }"><a class="danger-link" @click="removeContact(index)">{{ t('common.delete') }}</a></template></a-table-column>
          </a-table>
        </a-tab-pane>
        <a-tab-pane key="invoice" :tab="t('basic.customer.tabs.invoice')">
          <a-row :gutter="16">
            <a-col :span="8"><a-form-item :label="t('basic.customer.invoiceFullName')"><a-input v-model:value="form.invoice!.invoiceFullName" :disabled="readonly" /></a-form-item></a-col>
            <a-col :span="8"><a-form-item :label="t('basic.customer.taxNumber')"><a-input v-model:value="form.invoice!.taxNumber" :disabled="readonly" /></a-form-item></a-col>
            <a-col :span="8"><a-form-item :label="t('basic.customer.invoiceRequired')"><a-switch v-model:checked="form.invoice!.invoiceRequired" :disabled="readonly" /></a-form-item></a-col>
            <a-col :span="12"><a-form-item :label="t('basic.customer.registeredAddress')"><a-textarea v-model:value="form.invoice!.registeredAddress" :disabled="readonly" :rows="2" /></a-form-item></a-col>
            <a-col :span="12"><a-form-item :label="t('basic.customer.registeredPhone')"><a-input v-model:value="form.invoice!.registeredPhone" :disabled="readonly" /></a-form-item></a-col>
            <a-col :span="12"><a-form-item :label="t('basic.customer.bankName')"><a-input v-model:value="form.invoice!.bankName" :disabled="readonly" /></a-form-item></a-col>
            <a-col :span="12"><a-form-item :label="t('basic.customer.bankAccount')"><a-input v-model:value="form.invoice!.bankAccount" :disabled="readonly" /></a-form-item></a-col>
          </a-row>
        </a-tab-pane>
        <a-tab-pane key="extra" :tab="t('basic.customer.tabs.extra')">
          <a-row :gutter="16">
            <a-col :span="8"><a-form-item :label="t('basic.customer.officialWebsite')"><a-input v-model:value="form.extra!.officialWebsite" :disabled="readonly" /></a-form-item></a-col>
            <a-col :span="8"><a-form-item :label="t('basic.customer.switchboardPhone')"><a-input v-model:value="form.extra!.switchboardPhone" :disabled="readonly" /></a-form-item></a-col>
            <a-col :span="8"><a-form-item :label="t('basic.customer.officialEmailDomain')"><a-input v-model:value="form.extra!.officialEmailDomain" :disabled="readonly" /></a-form-item></a-col>
            <a-col :span="8"><a-form-item :label="t('basic.customer.faxNumber')"><a-input v-model:value="form.extra!.faxNumber" :disabled="readonly" /></a-form-item></a-col>
            <a-col :span="8"><a-form-item :label="t('basic.customer.channelPartnerLevel')"><a-select v-model:value="form.extra!.channelPartnerLevel" :disabled="readonly" :options="channelOptions" allow-clear /></a-form-item></a-col>
            <a-col :span="8"><a-form-item :label="t('basic.customer.nationalIndustryCode')"><a-input v-model:value="form.extra!.nationalIndustryCode" :disabled="readonly" /></a-form-item></a-col>
            <a-col :span="8"><a-form-item :label="t('basic.customer.registeredCapital')"><a-input-number v-model:value="form.extra!.registeredCapital" :disabled="readonly" class="full-width" /></a-form-item></a-col>
            <a-col :span="8"><a-form-item :label="t('basic.customer.registeredCapitalCurrency')"><a-input v-model:value="form.extra!.registeredCapitalCurrency" :disabled="readonly" /></a-form-item></a-col>
            <a-col :span="8"><a-form-item :label="t('basic.customer.paidInCapital')"><a-input-number v-model:value="form.extra!.paidInCapital" :disabled="readonly" class="full-width" /></a-form-item></a-col>
            <a-col :span="8"><a-form-item :label="t('basic.customer.paidInCapitalCurrency')"><a-input v-model:value="form.extra!.paidInCapitalCurrency" :disabled="readonly" /></a-form-item></a-col>
            <a-col :span="24"><a-form-item :label="t('basic.customer.businessScope')"><a-textarea v-model:value="form.extra!.businessScope" :disabled="readonly" :rows="3" /></a-form-item></a-col>
          </a-row>
        </a-tab-pane>
      </a-tabs>
    </BaseFormDialog>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { Modal, message } from 'ant-design-vue'
import { useI18n } from 'vue-i18n'
import BaseFormDialog from '@/components/common/BaseFormDialog.vue'
import FxDynamicTable from '@/components/common/FxDynamicTable.vue'
import { useBatchTableSelection } from '@/hooks/useBatchTableSelection'
import { customerApi, type Customer, type CustomerContact, type CustomerPageParam } from '@/api/basic/customer'

const { t } = useI18n()
const tableRef = ref()
const { selectedRowKeys, selectedCount, rowSelection, clearSelection } = useBatchTableSelection<number>()
const editorVisible = ref(false)
const saving = ref(false)
const readonly = ref(false)
const activeTab = ref('main')
const form = ref<Customer>(emptyCustomer())

const valueLevelOptions = computed(() => [
  { label: t('basic.customer.valueLevel.strategic'), value: 'STRATEGIC' },
  { label: t('basic.customer.valueLevel.key'), value: 'KEY' },
  { label: t('basic.customer.valueLevel.normal'), value: 'NORMAL' },
  { label: t('basic.customer.valueLevel.small'), value: 'SMALL' },
])
const creditOptions = ['AAA', 'AA', 'A', 'B', 'C'].map(value => ({ label: value, value }))
const businessStatusOptions = computed(() => [
  { label: t('basic.customer.businessStatusOptions.continuing'), value: '\u5b58\u7eed' },
  { label: t('basic.customer.businessStatusOptions.operating'), value: '\u5728\u4e1a' },
  { label: t('basic.customer.businessStatusOptions.revoked'), value: '\u540a\u9500' },
  { label: t('basic.customer.businessStatusOptions.cancelled'), value: '\u6ce8\u9500' },
  { label: t('basic.customer.businessStatusOptions.movedIn'), value: '\u8fc1\u5165' },
  { label: t('basic.customer.businessStatusOptions.movedOut'), value: '\u8fc1\u51fa' },
])
const approvalOptions = computed(() => [
  { label: t('basic.customer.approval.notSubmitted'), value: 0 },
  { label: t('basic.customer.approval.processing'), value: 1 },
  { label: t('basic.customer.approval.approved'), value: 2 },
  { label: t('basic.customer.approval.rejected'), value: 3 },
])
const enterpriseNatureOptions = computed(() => [
  { label: t('basic.customer.enterpriseNatureOptions.soe'), value: 'SOE' },
  { label: t('basic.customer.enterpriseNatureOptions.private'), value: 'PRIVATE' },
  { label: t('basic.customer.enterpriseNatureOptions.foreign'), value: 'FOREIGN' },
  { label: t('basic.customer.enterpriseNatureOptions.joint'), value: 'JOINT' },
  { label: t('basic.customer.enterpriseNatureOptions.institution'), value: 'INSTITUTION' },
  { label: t('basic.customer.enterpriseNatureOptions.government'), value: 'GOVERNMENT' },
])
const channelOptions = computed(() => [
  { label: t('basic.customer.channel.core'), value: 'CORE' },
  { label: t('basic.customer.channel.gold'), value: 'GOLD' },
  { label: t('basic.customer.channel.silver'), value: 'SILVER' },
  { label: t('basic.customer.channel.normal'), value: 'NORMAL' },
])

const dictOptions = computed(() => ({
  customerValueLevel: valueLevelOptions.value,
  customerCreditLevel: creditOptions,
  businessStatus: businessStatusOptions.value,
  approvalStatus: approvalOptions.value,
  isRelatedTenant: [{ label: t('basic.customer.related'), value: true }, { label: t('basic.customer.notRelated'), value: false }],
}))


const editorTitle = computed(() => readonly.value ? t('basic.customer.customerDetail') : form.value.id ? t('basic.customer.editCustomer') : t('basic.customer.addCustomer'))

function emptyCustomer(): Customer {
  return { autoGenerateCode: true, status: 1, approvalStatus: 0, isRelatedTenant: false, contactList: [], invoice: {}, extra: {} }
}

async function handleRequest(payload: { page: { current: number; pageSize: number }; query: Record<string, any> }) {
  const params: CustomerPageParam = { pageNum: payload.page.current, pageSize: payload.page.pageSize, ...payload.query }
  const result = await customerApi.page(params)
  return { records: result.records || [], total: Number(result.total || 0) }
}

function openCreate() {
  readonly.value = false
  activeTab.value = 'main'
  form.value = emptyCustomer()
  editorVisible.value = true
}

async function openEdit(record: Customer) {
  readonly.value = false
  await load(record)
}

async function openDetail(record: Customer) {
  readonly.value = true
  await load(record)
}

async function load(record: Customer) {
  activeTab.value = 'main'
  const detail = await customerApi.detail({ id: record.id! })
  form.value = { ...emptyCustomer(), ...detail, autoGenerateCode: false, contactList: detail.contactList || [], invoice: detail.invoice || {}, extra: detail.extra || {} }
  editorVisible.value = true
}

async function handleSave() {
  if (readonly.value) {
    editorVisible.value = false
    return
  }
  if (!form.value.autoGenerateCode && !form.value.customerCode?.trim()) {
    message.warning(t('basic.customer.customerCodeRequired'))
    return
  }
  if (!form.value.customerFullName?.trim()) {
    message.warning(t('basic.customer.customerFullNameRequired'))
    return
  }
  saving.value = true
  try {
    if (form.value.id) {
      await customerApi.update(form.value)
    } else {
      await customerApi.create(form.value)
    }
    editorVisible.value = false
    await tableRef.value?.refresh?.()
  } finally {
    saving.value = false
  }
}

function addContact() {
  form.value.contactList = form.value.contactList || []
  form.value.contactList.push({} as CustomerContact)
}

function removeContact(index: number) {
  form.value.contactList?.splice(index, 1)
}

function handleDelete(record: Customer) {
  Modal.confirm({
    title: t('basic.customer.confirmDeleteCustomer'),
    async onOk() {
      await customerApi.delete(record.id!)
      await tableRef.value?.refresh?.()
    },
  })
}

function handleBatchDelete() {
  if (!selectedCount.value) return
  Modal.confirm({
    title: t('common.confirmBatchDelete'),
    content: t('common.confirmBatchDeleteMessage', { count: selectedCount.value }),
    async onOk() {
      await customerApi.batchDelete(selectedRowKeys.value)
      clearSelection()
      await tableRef.value?.refresh?.()
    },
  })
}

async function handleGenerateTenant(record: Customer) {
  const tenantCode = await customerApi.generateTenant(record.id!)
  message.success(t('basic.customer.customerTenantCreated', { code: tenantCode }))
  await tableRef.value?.refresh?.()
}

async function handleStartApproval(record: Customer) {
  await customerApi.startApproval(record.id!)
  await tableRef.value?.refresh?.()
}

function labelOf(options: any[], value: any) {
  return options.find(item => String(item.value) === String(value))?.label || '-'
}

function approvalColor(value?: number) {
  return ({ 0: 'default', 1: 'processing', 2: 'green', 3: 'red' } as Record<number, string>)[Number(value)] || 'default'
}
</script>

<style scoped lang="less" src="@/styles/views/basic/customer/index.less"></style>
