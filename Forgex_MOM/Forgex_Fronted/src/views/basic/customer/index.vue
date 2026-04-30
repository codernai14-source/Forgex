<template>
  <div class="master-page">
    <div class="page-header">
      <div>
        <a-tag color="blue">Master Data</a-tag>
        <h1>客户主数据</h1>
        <p>维护客户基础信息、联系人、发票抬头、经营扩展信息和客户租户关联。</p>
      </div>
      <a-space wrap>
        <a-button v-permission="'basic:customer:add'" type="primary" @click="openCreate">新增客户</a-button>
      </a-space>
    </div>

    <FxDynamicTable
      ref="tableRef"
      table-code="CustomerMasterTable"
      :request="handleRequest"
      :dict-options="dictOptions"
      :fallback-config="tableFallbackConfig"
      row-key="id"
    >
      <template #customerFullName="{ record }">
        <div class="name-cell">
          <strong>{{ record.customerFullName || record.customerName }}</strong>
          <span>{{ record.customerShortName || '-' }}</span>
        </div>
      </template>
      <template #isRelatedTenant="{ record }">
        <a-tag :color="record.isRelatedTenant || record.relatedTenantCode ? 'green' : 'default'">
          {{ record.isRelatedTenant || record.relatedTenantCode ? '已关联' : '未关联' }}
        </a-tag>
      </template>
      <template #approvalStatus="{ record }">
        <a-tag :color="approvalColor(record.approvalStatus)">
          {{ labelOf(approvalOptions, record.approvalStatus) }}
        </a-tag>
      </template>
      <template #action="{ record }">
        <a-space>
          <a v-permission="'basic:customer:query'" @click="openDetail(record)">详情</a>
          <a v-permission="'basic:customer:edit'" @click="openEdit(record)">编辑</a>
          <a
            v-permission="'basic:customer:generateTenant'"
            :class="{ disabled: record.isRelatedTenant || record.relatedTenantCode }"
            @click="handleGenerateTenant(record)"
          >
            创建客户租户
          </a>
          <a
            v-permission="'basic:customer:approval'"
            :class="{ disabled: record.approvalStatus === 1 }"
            @click="handleStartApproval(record)"
          >
            发起审批
          </a>
          <a
            v-permission="'basic:customer:delete'"
            class="danger-link"
            :class="{ disabled: record.isRelatedTenant || record.relatedTenantCode }"
            @click="handleDelete(record)"
          >
            删除
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
        <a-tab-pane key="main" tab="主数据">
          <a-form layout="vertical" :model="form">
            <a-row :gutter="16">
              <a-col :span="8">
                <a-form-item label="自动生成编码">
                  <a-switch v-model:checked="form.autoGenerateCode" :disabled="!!form.id || readonly" />
                </a-form-item>
              </a-col>
              <a-col :span="8">
                <a-form-item label="客户编码" required>
                  <a-input v-model:value="form.customerCode" :disabled="!!form.id || readonly || form.autoGenerateCode" />
                </a-form-item>
              </a-col>
              <a-col :span="8">
                <a-form-item label="客户全称" required>
                  <a-input v-model:value="form.customerFullName" :disabled="readonly" />
                </a-form-item>
              </a-col>
              <a-col :span="8">
                <a-form-item label="客户简称">
                  <a-input v-model:value="form.customerShortName" :disabled="readonly" />
                </a-form-item>
              </a-col>
              <a-col :span="8">
                <a-form-item label="客户价值等级">
                  <a-select v-model:value="form.customerValueLevel" :disabled="readonly" :options="valueLevelOptions" allow-clear />
                </a-form-item>
              </a-col>
              <a-col :span="8">
                <a-form-item label="客户信用等级">
                  <a-select v-model:value="form.customerCreditLevel" :disabled="readonly" :options="creditOptions" allow-clear />
                </a-form-item>
              </a-col>
              <a-col :span="8">
                <a-form-item label="经营状态">
                  <a-select v-model:value="form.businessStatus" :disabled="readonly" :options="businessStatusOptions" allow-clear />
                </a-form-item>
              </a-col>
              <a-col :span="8">
                <a-form-item label="审批状态">
                  <a-select v-model:value="form.approvalStatus" :disabled="readonly" :options="approvalOptions" allow-clear />
                </a-form-item>
              </a-col>
              <a-col :span="8">
                <a-form-item label="是否关联租户">
                  <a-switch v-model:checked="form.isRelatedTenant" :disabled="readonly" />
                </a-form-item>
              </a-col>
              <a-col :span="8">
                <a-form-item label="关联租户编码">
                  <a-input v-model:value="form.relatedTenantCode" :disabled="readonly" />
                </a-form-item>
              </a-col>
              <a-col :span="8">
                <a-form-item label="运输方式">
                  <a-input v-model:value="form.transportMode" :disabled="readonly" />
                </a-form-item>
              </a-col>
              <a-col :span="8">
                <a-form-item label="支付条件">
                  <a-input v-model:value="form.paymentTerms" :disabled="readonly" />
                </a-form-item>
              </a-col>
              <a-col :span="8">
                <a-form-item label="国家">
                  <a-input v-model:value="form.country" :disabled="readonly" />
                </a-form-item>
              </a-col>
              <a-col :span="8">
                <a-form-item label="企业性质">
                  <a-select v-model:value="form.enterpriseNature" :disabled="readonly" :options="enterpriseNatureOptions" allow-clear />
                </a-form-item>
              </a-col>
              <a-col :span="24">
                <a-form-item label="实际经营地址">
                  <a-textarea v-model:value="form.actualBusinessAddress" :disabled="readonly" :rows="2" />
                </a-form-item>
              </a-col>
              <a-col :span="12">
                <a-form-item label="收款地址">
                  <a-textarea v-model:value="form.collectionAddress" :disabled="readonly" :rows="2" />
                </a-form-item>
              </a-col>
              <a-col :span="12">
                <a-form-item label="收货地址">
                  <a-textarea v-model:value="form.shippingAddress" :disabled="readonly" :rows="2" />
                </a-form-item>
              </a-col>
            </a-row>
          </a-form>
        </a-tab-pane>
        <a-tab-pane key="contacts" tab="联系人">
          <div class="sub-toolbar"><a-button v-if="!readonly" type="dashed" @click="addContact">新增联系人</a-button></div>
          <a-table :data-source="form.contactList" :pagination="false" row-key="id" size="small">
            <a-table-column title="联系人"><template #default="{ record }"><a-input v-model:value="record.contactName" :disabled="readonly" /></template></a-table-column>
            <a-table-column title="职位"><template #default="{ record }"><a-input v-model:value="record.contactPosition" :disabled="readonly" /></template></a-table-column>
            <a-table-column title="联系方式"><template #default="{ record }"><a-input v-model:value="record.contactPhone" :disabled="readonly" /></template></a-table-column>
            <a-table-column v-if="!readonly" title="操作" width="80"><template #default="{ index }"><a class="danger-link" @click="removeContact(index)">删除</a></template></a-table-column>
          </a-table>
        </a-tab-pane>
        <a-tab-pane key="invoice" tab="发票抬头">
          <a-row :gutter="16">
            <a-col :span="8"><a-form-item label="全称"><a-input v-model:value="form.invoice!.invoiceFullName" :disabled="readonly" /></a-form-item></a-col>
            <a-col :span="8"><a-form-item label="税号"><a-input v-model:value="form.invoice!.taxNumber" :disabled="readonly" /></a-form-item></a-col>
            <a-col :span="8"><a-form-item label="开票必备"><a-switch v-model:checked="form.invoice!.invoiceRequired" :disabled="readonly" /></a-form-item></a-col>
            <a-col :span="12"><a-form-item label="注册地址"><a-textarea v-model:value="form.invoice!.registeredAddress" :disabled="readonly" :rows="2" /></a-form-item></a-col>
            <a-col :span="12"><a-form-item label="注册电话"><a-input v-model:value="form.invoice!.registeredPhone" :disabled="readonly" /></a-form-item></a-col>
            <a-col :span="12"><a-form-item label="开户行"><a-input v-model:value="form.invoice!.bankName" :disabled="readonly" /></a-form-item></a-col>
            <a-col :span="12"><a-form-item label="银行账号"><a-input v-model:value="form.invoice!.bankAccount" :disabled="readonly" /></a-form-item></a-col>
          </a-row>
        </a-tab-pane>
        <a-tab-pane key="extra" tab="其它信息">
          <a-row :gutter="16">
            <a-col :span="8"><a-form-item label="企业官网"><a-input v-model:value="form.extra!.officialWebsite" :disabled="readonly" /></a-form-item></a-col>
            <a-col :span="8"><a-form-item label="总机电话"><a-input v-model:value="form.extra!.switchboardPhone" :disabled="readonly" /></a-form-item></a-col>
            <a-col :span="8"><a-form-item label="官方邮箱域名"><a-input v-model:value="form.extra!.officialEmailDomain" :disabled="readonly" /></a-form-item></a-col>
            <a-col :span="8"><a-form-item label="传真号码"><a-input v-model:value="form.extra!.faxNumber" :disabled="readonly" /></a-form-item></a-col>
            <a-col :span="8"><a-form-item label="渠道伙伴等级"><a-select v-model:value="form.extra!.channelPartnerLevel" :disabled="readonly" :options="channelOptions" allow-clear /></a-form-item></a-col>
            <a-col :span="8"><a-form-item label="国标行业编码"><a-input v-model:value="form.extra!.nationalIndustryCode" :disabled="readonly" /></a-form-item></a-col>
            <a-col :span="8"><a-form-item label="注册资本"><a-input-number v-model:value="form.extra!.registeredCapital" :disabled="readonly" class="full-width" /></a-form-item></a-col>
            <a-col :span="8"><a-form-item label="注册资本币种"><a-input v-model:value="form.extra!.registeredCapitalCurrency" :disabled="readonly" /></a-form-item></a-col>
            <a-col :span="8"><a-form-item label="实缴资本"><a-input-number v-model:value="form.extra!.paidInCapital" :disabled="readonly" class="full-width" /></a-form-item></a-col>
            <a-col :span="8"><a-form-item label="实缴资本币种"><a-input v-model:value="form.extra!.paidInCapitalCurrency" :disabled="readonly" /></a-form-item></a-col>
            <a-col :span="24"><a-form-item label="经营范围"><a-textarea v-model:value="form.extra!.businessScope" :disabled="readonly" :rows="3" /></a-form-item></a-col>
          </a-row>
        </a-tab-pane>
      </a-tabs>
    </BaseFormDialog>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { Modal, message } from 'ant-design-vue'
import BaseFormDialog from '@/components/common/BaseFormDialog.vue'
import FxDynamicTable from '@/components/common/FxDynamicTable.vue'
import { customerApi, type Customer, type CustomerContact, type CustomerPageParam } from '@/api/basic/customer'

const tableRef = ref()
const editorVisible = ref(false)
const saving = ref(false)
const readonly = ref(false)
const activeTab = ref('main')
const form = ref<Customer>(emptyCustomer())

const valueLevelOptions = [
  { label: '战略客户', value: 'STRATEGIC' },
  { label: '重点客户', value: 'KEY' },
  { label: '普通客户', value: 'NORMAL' },
  { label: '小微客户', value: 'SMALL' },
]
const creditOptions = ['AAA', 'AA', 'A', 'B', 'C'].map(value => ({ label: value, value }))
const businessStatusOptions = ['存续', '在业', '吊销', '注销', '迁入', '迁出'].map(value => ({ label: value, value }))
const approvalOptions = [
  { label: '未提交', value: 0 },
  { label: '审批中', value: 1 },
  { label: '已通过', value: 2 },
  { label: '已驳回', value: 3 },
]
const enterpriseNatureOptions = [
  { label: '国企', value: 'SOE' },
  { label: '民企', value: 'PRIVATE' },
  { label: '外资', value: 'FOREIGN' },
  { label: '合资', value: 'JOINT' },
  { label: '事业单位', value: 'INSTITUTION' },
  { label: '政府机构', value: 'GOVERNMENT' },
]
const channelOptions = [
  { label: '核心', value: 'CORE' },
  { label: '金牌', value: 'GOLD' },
  { label: '银牌', value: 'SILVER' },
  { label: '普通', value: 'NORMAL' },
]

const dictOptions = computed(() => ({
  customerValueLevel: valueLevelOptions,
  customerCreditLevel: creditOptions,
  businessStatus: businessStatusOptions,
  approvalStatus: approvalOptions,
  isRelatedTenant: [{ label: '已关联', value: true }, { label: '未关联', value: false }],
}))

const tableFallbackConfig = {
  tableCode: 'CustomerMasterTable',
  tableName: '客户主数据',
  tableType: 'BUSINESS',
  rowKey: 'id',
  defaultPageSize: 10,
  columns: [
    { field: 'customerCode', title: '客户编码', width: 140, visible: true, order: 1 },
    { field: 'customerFullName', title: '客户名称', width: 240, visible: true, order: 2 },
    { field: 'customerValueLevel', title: '价值等级', width: 120, visible: true, order: 3 },
    { field: 'customerCreditLevel', title: '信用等级', width: 120, visible: true, order: 4 },
    { field: 'businessStatus', title: '经营状态', width: 120, visible: true, order: 5 },
    { field: 'isRelatedTenant', title: '关联租户', width: 120, visible: true, order: 6 },
    { field: 'approvalStatus', title: '审批状态', width: 120, visible: true, order: 7 },
    { field: 'createTime', title: '创建时间', width: 180, visible: true, order: 8 },
    { field: 'action', title: '操作', width: 340, fixed: 'right', visible: true, order: 9 },
  ],
  queryFields: [
    { field: 'customerCode', label: '客户编码', queryType: 'input', queryOperator: 'like' },
    { field: 'customerFullName', label: '客户名称', queryType: 'input', queryOperator: 'like' },
    { field: 'approvalStatus', label: '审批状态', queryType: 'select', queryOperator: 'eq' },
  ],
}

const editorTitle = computed(() => readonly.value ? '客户详情' : form.value.id ? '编辑客户' : '新增客户')

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
    message.warning('客户编码不能为空')
    return
  }
  if (!form.value.customerFullName?.trim()) {
    message.warning('客户全称不能为空')
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
    title: '确认删除客户？',
    async onOk() {
      await customerApi.delete(record.id!)
      await tableRef.value?.refresh?.()
    },
  })
}

async function handleGenerateTenant(record: Customer) {
  const tenantCode = await customerApi.generateTenant(record.id!)
  message.success(`客户租户已创建：${tenantCode}`)
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

<style scoped lang="less">
.master-page { display: flex; flex-direction: column; height: 100%; min-height: 100%; padding: 20px; overflow: hidden; box-sizing: border-box; background: var(--fx-bg-layout, #f8fafc); }
.page-header { flex-shrink: 0; display: flex; justify-content: space-between; gap: 20px; margin-bottom: 16px; padding: 24px 28px; border: 1px solid var(--fx-border-color, #e5e7eb); border-radius: 8px; background: var(--fx-bg-container, #fff); }
.page-header h1 { margin: 10px 0 8px; font-size: 28px; }
.page-header p { margin: 0; color: var(--fx-text-secondary, #64748b); }
.master-page :deep(.fx-dynamic-table) { flex: 1 1 auto; min-height: 0; }
.name-cell { display: flex; flex-direction: column; gap: 4px; }
.name-cell span { color: var(--fx-text-secondary, #64748b); font-size: 12px; }
.danger-link { color: #ff4d4f; }
.disabled { color: var(--fx-text-disabled, #bfbfbf); cursor: not-allowed; pointer-events: none; }
.sub-toolbar { display: flex; justify-content: flex-end; margin-bottom: 12px; }
.full-width { width: 100%; }
@media (max-width: 768px) { .master-page { padding: 12px; } .page-header { flex-direction: column; padding: 18px; } }
</style>
