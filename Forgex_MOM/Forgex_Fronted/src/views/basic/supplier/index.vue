<template>
  <div class="supplier-page" :class="`supplier-page--${appStore.theme}`">
    <div class="supplier-page__header">
      <div>
        <a-tag color="blue">Master Data</a-tag>
        <h1>供应商主数据</h1>
        <p>统一维护供应商共享主数据，并为租户创建、接口同步和资质审查提供数据来源。</p>
      </div>
      <a-space wrap>
        <a-upload :show-upload-list="false" :before-upload="handleImport">
          <a-button v-permission="'basic:supplier:import'">
            <UploadOutlined />
            导入
          </a-button>
        </a-upload>
        <a-button v-permission="'basic:supplier:import'" @click="downloadTemplate">下载模板</a-button>
        <a-button v-permission="'basic:supplier:export'" @click="handleExport">导出</a-button>
        <a-button v-permission="'basic:supplier:sync'" @click="handleSync">同步第三方</a-button>
        <a-button v-permission="'basic:supplier:add'" type="primary" @click="openCreate">新增供应商</a-button>
      </a-space>
    </div>

    <FxDynamicTable
      ref="tableRef"
      table-code="SupplierMasterTable"
      :request="handleRequest"
      :dict-options="dictOptions"
      :fallback-config="tableFallbackConfig"
      row-key="id"
    >
      <template #logoUrl="{ record }">
        <a-avatar v-if="record.logoUrl" shape="square" :size="32" :src="record.logoUrl" />
        <span v-else>-</span>
      </template>

      <template #supplierFullName="{ record }">
        <div class="supplier-name">
          <strong>{{ record.supplierFullName }}</strong>
          <span>{{ record.supplierShortName || record.englishName || '-' }}</span>
        </div>
      </template>

      <template #cooperationStatus="{ record }">
        <a-tag :color="cooperationColor(record.cooperationStatus)">
          {{ labelOf(cooperationOptions, record.cooperationStatus) }}
        </a-tag>
      </template>

      <template #creditRisk="{ record }">
        <a-space>
          <a-tag>{{ record.creditLevel || '-' }}</a-tag>
          <a-tag :color="riskColor(record.riskLevel)">{{ record.riskLevel || '-' }}</a-tag>
        </a-space>
      </template>

      <template #supplierLevel="{ record }">
        {{ labelOf(levelOptions, record.supplierLevel) }}
      </template>

      <template #hasRelatedTenant="{ record }">
        <a-tag :color="isTenantLinked(record) ? 'green' : 'default'">
          {{ isTenantLinked(record) ? '已关联' : '未关联' }}
        </a-tag>
      </template>

      <template #reviewStatus="{ record }">
        <a-tag :color="reviewColor(record.reviewStatus)">
          {{ labelOf(reviewOptions, record.reviewStatus) }}
        </a-tag>
      </template>

      <template #action="{ record }">
        <a-space>
          <a v-permission="'basic:supplier:query'" @click="openDetail(record)">详情</a>
          <a v-permission="'basic:supplier:edit'" @click="openEdit(record)">编辑</a>
          <a
            v-permission="'basic:supplier:generateTenant'"
            :class="{ disabled: isTenantLinked(record) }"
            @click="handleGenerateTenant(record)"
          >
            生成租户
          </a>
          <a
            v-permission="'basic:supplier:review'"
            :class="{ disabled: record.reviewStatus !== 1 }"
            @click="handleStartReview(record)"
          >
            发起审查
          </a>
          <a
            v-permission="'basic:supplier:delete'"
            class="danger-link"
            :class="{ disabled: isTenantLinked(record) }"
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
      @cancel="handleEditorCancel"
    >
      <a-tabs v-model:active-key="activeTab">
        <a-tab-pane key="main" tab="主数据">
          <a-form ref="formRef" :model="form" layout="vertical">
            <a-row :gutter="16">
              <a-col :span="8">
                <a-form-item label="自动生成编码">
                  <a-switch v-model:checked="form.autoGenerateCode" :disabled="!!form.id || readonly" />
                </a-form-item>
              </a-col>
              <a-col :span="8">
                <a-form-item label="供应商编码" required>
                  <a-input
                    v-model:value="form.supplierCode"
                    :disabled="!!form.id || readonly || form.autoGenerateCode"
                    :placeholder="form.autoGenerateCode ? '保存时按编码规则自动生成' : '请输入供应商编码'"
                  />
                </a-form-item>
              </a-col>
              <a-col :span="8">
                <a-form-item label="供应商全称" required>
                  <a-input v-model:value="form.supplierFullName" :disabled="readonly" />
                </a-form-item>
              </a-col>
              <a-col :span="8">
                <a-form-item label="供应商Logo">
                  <a-upload
                    :show-upload-list="false"
                    :before-upload="handleLogoUpload"
                    accept="image/*"
                    :disabled="readonly"
                  >
                    <div class="logo-uploader">
                      <a-avatar v-if="form.logoUrl" shape="square" :size="52" :src="form.logoUrl" />
                      <PlusOutlined v-else />
                      <span>{{ form.logoUrl ? '更换Logo' : '上传Logo' }}</span>
                    </div>
                  </a-upload>
                </a-form-item>
              </a-col>
              <a-col :span="8">
                <a-form-item label="简称">
                  <a-input v-model:value="form.supplierShortName" :disabled="readonly" />
                </a-form-item>
              </a-col>
              <a-col :span="8">
                <a-form-item label="英文名">
                  <a-input v-model:value="form.englishName" :disabled="readonly" />
                </a-form-item>
              </a-col>
              <a-col :span="8">
                <a-form-item label="主联系人">
                  <a-input v-model:value="form.primaryContact" :disabled="readonly" />
                </a-form-item>
              </a-col>
              <a-col :span="8">
                <a-form-item label="联系电话">
                  <a-input v-model:value="form.contactPhone" :disabled="readonly" />
                </a-form-item>
              </a-col>
              <a-col :span="8">
                <a-form-item label="合作状态">
                  <a-select v-model:value="form.cooperationStatus" :disabled="readonly" allow-clear>
                    <a-select-option v-for="item in cooperationOptions" :key="String(item.value)" :value="item.value">
                      {{ item.label }}
                    </a-select-option>
                  </a-select>
                </a-form-item>
              </a-col>
              <a-col :span="8">
                <a-form-item label="信用等级">
                  <a-select v-model:value="form.creditLevel" :disabled="readonly" allow-clear>
                    <a-select-option v-for="item in creditOptions" :key="String(item.value)" :value="item.value">
                      {{ item.label }}
                    </a-select-option>
                  </a-select>
                </a-form-item>
              </a-col>
              <a-col :span="8">
                <a-form-item label="风险等级">
                  <a-select v-model:value="form.riskLevel" :disabled="readonly" allow-clear>
                    <a-select-option v-for="item in riskOptions" :key="String(item.value)" :value="item.value">
                      {{ item.label }}
                    </a-select-option>
                  </a-select>
                </a-form-item>
              </a-col>
              <a-col :span="8">
                <a-form-item label="分级">
                  <a-select v-model:value="form.supplierLevel" :disabled="readonly" allow-clear>
                    <a-select-option v-for="item in levelOptions" :key="String(item.value)" :value="item.value">
                      {{ item.label }}
                    </a-select-option>
                  </a-select>
                </a-form-item>
              </a-col>
              <a-col :span="8">
                <a-form-item label="关联租户编号">
                  <a-input v-model:value="form.relatedTenantCode" disabled />
                </a-form-item>
              </a-col>
              <a-col :span="8">
                <a-form-item label="审查状态">
                  <a-select v-model:value="form.reviewStatus" :disabled="readonly" allow-clear>
                    <a-select-option v-for="item in reviewOptions" :key="String(item.value)" :value="item.value">
                      {{ item.label }}
                    </a-select-option>
                  </a-select>
                </a-form-item>
              </a-col>
              <a-col :span="24">
                <a-form-item label="现地址">
                  <a-textarea v-model:value="form.currentAddress" :disabled="readonly" :rows="2" />
                </a-form-item>
              </a-col>
              <a-col :span="24">
                <a-form-item label="备注">
                  <a-textarea v-model:value="form.remark" :disabled="readonly" :rows="2" />
                </a-form-item>
              </a-col>
            </a-row>
          </a-form>
        </a-tab-pane>

        <a-tab-pane key="detail" tab="详情信息">
          <a-row :gutter="16">
            <a-col :span="8">
              <a-form-item label="法人代表">
                <a-input v-model:value="form.detail!.legalRepresentative" :disabled="readonly" />
              </a-form-item>
            </a-col>
            <a-col :span="8">
              <a-form-item label="注册资本">
                <a-input-number v-model:value="form.detail!.registeredCapital" :disabled="readonly" class="full-width" />
              </a-form-item>
            </a-col>
            <a-col :span="8">
              <a-form-item label="成立日期">
                <a-date-picker v-model:value="form.detail!.establishmentDate" :disabled="readonly" value-format="YYYY-MM-DD" class="full-width" />
              </a-form-item>
            </a-col>
            <a-col :span="8">
              <a-form-item label="企业性质">
                <a-select v-model:value="form.detail!.enterpriseNature" :disabled="readonly" allow-clear>
                  <a-select-option v-for="item in enterpriseNatureOptions" :key="String(item.value)" :value="item.value">
                    {{ item.label }}
                  </a-select-option>
                </a-select>
              </a-form-item>
            </a-col>
            <a-col :span="8">
              <a-form-item label="行业分类">
                <a-select v-model:value="form.detail!.industryCategory" :disabled="readonly" allow-clear>
                  <a-select-option v-for="item in industryOptions" :key="String(item.value)" :value="item.value">
                    {{ item.label }}
                  </a-select-option>
                </a-select>
              </a-form-item>
            </a-col>
            <a-col :span="8">
              <a-form-item label="邮箱">
                <a-input v-model:value="form.detail!.email" :disabled="readonly" />
              </a-form-item>
            </a-col>
            <a-col :span="8">
              <a-form-item label="税号">
                <a-input v-model:value="form.detail!.taxNumber" :disabled="readonly" />
              </a-form-item>
            </a-col>
            <a-col :span="8">
              <a-form-item label="开户银行">
                <a-input v-model:value="form.detail!.bankName" :disabled="readonly" />
              </a-form-item>
            </a-col>
            <a-col :span="8">
              <a-form-item label="银行账号">
                <a-input v-model:value="form.detail!.bankAccount" :disabled="readonly" />
              </a-form-item>
            </a-col>
            <a-col :span="8">
              <a-form-item label="发票类型">
                <a-select v-model:value="form.detail!.invoiceType" :disabled="readonly" allow-clear>
                  <a-select-option v-for="item in invoiceOptions" :key="String(item.value)" :value="item.value">
                    {{ item.label }}
                  </a-select-option>
                </a-select>
              </a-form-item>
            </a-col>
            <a-col :span="8">
              <a-form-item label="默认税率">
                <a-input-number v-model:value="form.detail!.defaultTaxRate" :disabled="readonly" :min="0" :max="100" class="full-width" />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="注册地址">
                <a-textarea v-model:value="form.detail!.registeredAddress" :disabled="readonly" :rows="2" />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="经营地址">
                <a-textarea v-model:value="form.detail!.businessAddress" :disabled="readonly" :rows="2" />
              </a-form-item>
            </a-col>
          </a-row>
        </a-tab-pane>

        <a-tab-pane key="contacts" tab="联系人">
          <div class="sub-toolbar">
            <a-button v-if="!readonly" type="dashed" @click="addContact">新增联系人</a-button>
          </div>
          <a-table :data-source="form.contactList" :pagination="false" row-key="__rowKey" size="small">
            <a-table-column title="姓名">
              <template #default="{ record }">
                <a-input v-model:value="record.contactName" :disabled="readonly" />
              </template>
            </a-table-column>
            <a-table-column title="电话">
              <template #default="{ record }">
                <a-input v-model:value="record.contactPhone" :disabled="readonly" />
              </template>
            </a-table-column>
            <a-table-column title="职位">
              <template #default="{ record }">
                <a-input v-model:value="record.contactPosition" :disabled="readonly" />
              </template>
            </a-table-column>
            <a-table-column title="邮箱">
              <template #default="{ record }">
                <a-input v-model:value="record.contactEmail" :disabled="readonly" />
              </template>
            </a-table-column>
            <a-table-column v-if="!readonly" title="操作" width="90">
              <template #default="{ index }">
                <a class="danger-link" @click="removeContact(index)">删除</a>
              </template>
            </a-table-column>
          </a-table>
        </a-tab-pane>

        <a-tab-pane key="qualifications" tab="资质">
          <div class="sub-toolbar">
            <a-button v-if="!readonly" type="dashed" @click="addQualification">新增资质</a-button>
          </div>
          <a-table :data-source="form.qualificationList" :pagination="false" row-key="__rowKey" size="small">
            <a-table-column title="资质类型">
              <template #default="{ record }">
                <a-select v-model:value="record.qualificationType" :disabled="readonly" allow-clear class="full-width">
                  <a-select-option v-for="item in qualificationTypeOptions" :key="String(item.value)" :value="item.value">
                    {{ item.label }}
                  </a-select-option>
                </a-select>
              </template>
            </a-table-column>
            <a-table-column title="证书编号">
              <template #default="{ record }">
                <a-input v-model:value="record.certificateNo" :disabled="readonly" />
              </template>
            </a-table-column>
            <a-table-column title="发证日期">
              <template #default="{ record }">
                <a-date-picker v-model:value="record.issueDate" :disabled="readonly" value-format="YYYY-MM-DD" class="full-width" />
              </template>
            </a-table-column>
            <a-table-column title="有效期至">
              <template #default="{ record }">
                <a-date-picker v-model:value="record.expireDate" :disabled="readonly" value-format="YYYY-MM-DD" class="full-width" />
              </template>
            </a-table-column>
            <a-table-column title="附件">
              <template #default="{ record }">
                <a-input v-model:value="record.attachment" :disabled="readonly" placeholder="文件标识或 URL" />
              </template>
            </a-table-column>
            <a-table-column title="有效" width="90">
              <template #default="{ record }">
                <a-switch v-model:checked="record.valid" :disabled="readonly" />
              </template>
            </a-table-column>
            <a-table-column v-if="!readonly" title="操作" width="90">
              <template #default="{ index }">
                <a class="danger-link" @click="removeQualification(index)">删除</a>
              </template>
            </a-table-column>
          </a-table>
        </a-tab-pane>
      </a-tabs>
    </BaseFormDialog>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { Modal, message } from 'ant-design-vue'
import { PlusOutlined, UploadOutlined } from '@ant-design/icons-vue'
import BaseFormDialog from '@/components/common/BaseFormDialog.vue'
import FxDynamicTable from '@/components/common/FxDynamicTable.vue'
import { uploadFile } from '@/api/system/file'
import { supplierApi, type Supplier, type SupplierContact, type SupplierPageParam, type SupplierQualification } from '@/api/basic/supplier'
import { useDict, type DictItemOption } from '@/hooks/useDict'
import { useAppStore } from '@/stores/app'

type OptionValue = string | number | boolean

interface OptionItem {
  label: string
  value: OptionValue
}

const appStore = useAppStore()
const tableRef = ref()
const saving = ref(false)
const editorVisible = ref(false)
const activeTab = ref('main')
const readonly = ref(false)
const form = ref<Supplier>(createEmptySupplier())

const fallbackCooperationOptions: OptionItem[] = [
  { label: '潜在', value: '1' },
  { label: '正式', value: '2' },
  { label: '暂停', value: '3' },
  { label: '淘汰', value: '4' },
]
const fallbackCreditOptions: OptionItem[] = ['A', 'B', 'C', 'D'].map(item => ({ label: item, value: item }))
const fallbackLevelOptions: OptionItem[] = [
  { label: '战略', value: '1' },
  { label: '核心', value: '2' },
  { label: '一般', value: '3' },
]
const fallbackReviewOptions: OptionItem[] = [
  { label: '无需审查', value: 0 },
  { label: '未审查', value: 1 },
  { label: '审查中', value: 2 },
  { label: '已审查', value: 3 },
]
const fallbackEnterpriseNatureOptions: OptionItem[] = [
  { label: '国企', value: '1' },
  { label: '民营', value: '2' },
  { label: '外资', value: '3' },
  { label: '合资', value: '4' },
]

const { dictItems: cooperationDictItems } = useDict('supplier_cooperation_status')
const { dictItems: creditDictItems } = useDict('supplier_credit_level')
const { dictItems: riskDictItems } = useDict('supplier_risk_level')
const { dictItems: levelDictItems } = useDict('supplier_level')
const { dictItems: reviewDictItems } = useDict('supplier_review_status')
const { dictItems: enterpriseNatureDictItems } = useDict('supplier_enterprise_nature')
const { dictItems: industryDictItems } = useDict('supplier_industry_category')
const { dictItems: invoiceDictItems } = useDict('supplier_invoice_type')
const { dictItems: qualificationTypeDictItems } = useDict('supplier_qualification_type')

const normalizeDictOptions = (
  items: DictItemOption[],
  fallback: OptionItem[] = [],
  parseValue: (value: string | number) => OptionValue = value => value,
) => {
  if (!items.length) {
    return fallback
  }
  return items.map(item => ({
    label: item.label,
    value: parseValue(item.value),
  }))
}

const cooperationOptions = computed(() => normalizeDictOptions(cooperationDictItems.value, fallbackCooperationOptions))
const creditOptions = computed(() => normalizeDictOptions(creditDictItems.value, fallbackCreditOptions))
const riskOptions = computed(() => normalizeDictOptions(riskDictItems.value))
const levelOptions = computed(() => normalizeDictOptions(levelDictItems.value, fallbackLevelOptions))
const reviewOptions = computed(() => normalizeDictOptions(reviewDictItems.value, fallbackReviewOptions, value => Number(value)))
const enterpriseNatureOptions = computed(() => normalizeDictOptions(enterpriseNatureDictItems.value, fallbackEnterpriseNatureOptions))
const industryOptions = computed(() => normalizeDictOptions(industryDictItems.value))
const invoiceOptions = computed(() => normalizeDictOptions(invoiceDictItems.value))
const qualificationTypeOptions = computed(() => normalizeDictOptions(qualificationTypeDictItems.value))

const dictOptions = computed<Record<string, any[]>>(() => ({
  cooperationStatus: cooperationOptions.value,
  supplier_cooperation_status: cooperationOptions.value,
  creditLevel: creditOptions.value,
  supplier_credit_level: creditOptions.value,
  riskLevel: riskOptions.value,
  supplier_risk_level: riskOptions.value,
  supplierLevel: levelOptions.value,
  supplier_level: levelOptions.value,
  reviewStatus: reviewOptions.value,
  supplier_review_status: reviewOptions.value,
  hasRelatedTenant: [
    { label: '已关联', value: true },
    { label: '未关联', value: false },
  ],
}))

const tableFallbackConfig = {
  tableCode: 'SupplierMasterTable',
  tableName: '供应商主数据',
  tableType: 'BUSINESS',
  rowKey: 'id',
  defaultPageSize: 10,
  version: 1,
  queryFields: [
    { field: 'supplierCode', label: '供应商编码', queryType: 'input', queryOperator: 'like' },
    { field: 'supplierFullName', label: '供应商名称', queryType: 'input', queryOperator: 'like' },
    { field: 'cooperationStatus', label: '合作状态', queryType: 'select', queryOperator: 'eq', dictCode: 'supplier_cooperation_status' },
    { field: 'reviewStatus', label: '审查状态', queryType: 'select', queryOperator: 'eq', dictCode: 'supplier_review_status' },
  ],
  columns: [
    { field: 'logoUrl', title: 'Logo', width: 80, align: 'center', visible: true, order: 1 },
    { field: 'supplierCode', title: '供应商编码', width: 140, visible: true, order: 2 },
    { field: 'supplierFullName', title: '供应商名称', width: 240, visible: true, order: 3 },
    { field: 'cooperationStatus', title: '合作状态', width: 110, dictCode: 'supplier_cooperation_status', visible: true, order: 4 },
    { field: 'creditRisk', title: '信用/风险', width: 160, visible: true, order: 5 },
    { field: 'supplierLevel', title: '分级', width: 100, dictCode: 'supplier_level', visible: true, order: 6 },
    { field: 'hasRelatedTenant', title: '关联租户', width: 120, visible: true, order: 7 },
    { field: 'reviewStatus', title: '审查状态', width: 120, dictCode: 'supplier_review_status', visible: true, order: 8 },
    { field: 'createTime', title: '创建时间', width: 180, visible: true, order: 9 },
    { field: 'action', title: '操作', width: 330, fixed: 'right', visible: true, order: 10 },
  ],
}

const editorTitle = computed(() => {
  if (readonly.value) {
    return '供应商详情'
  }
  return form.value.id ? '编辑供应商' : '新增供应商'
})

function createEmptySupplier(): Supplier {
  return {
    supplierCode: '',
    autoGenerateCode: true,
    supplierFullName: '',
    cooperationStatus: '1',
    reviewStatus: 1,
    detail: {},
    contactList: [],
    qualificationList: [],
  }
}

async function handleRequest(payload: {
  page: { current: number; pageSize: number }
  query: Record<string, any>
}) {
  const params: SupplierPageParam = {
    pageNum: payload.page.current,
    pageSize: payload.page.pageSize,
    ...payload.query,
  }
  const result = await supplierApi.page(params)
  return {
    records: result.records || [],
    total: Number(result.total || 0),
  }
}

function openCreate() {
  readonly.value = false
  activeTab.value = 'main'
  form.value = createEmptySupplier()
  editorVisible.value = true
}

async function openEdit(record: Supplier) {
  readonly.value = false
  await loadEditor(record)
}

async function openDetail(record: Supplier) {
  readonly.value = true
  await loadEditor(record)
}

async function loadEditor(record: Supplier) {
  activeTab.value = 'main'
  const detail = await supplierApi.detail({ id: record.id! })
  form.value = normalizeEditorData(detail)
  form.value.autoGenerateCode = false
  editorVisible.value = true
}

function normalizeEditorData(data: Supplier): Supplier {
  return {
    ...createEmptySupplier(),
    ...data,
    detail: data.detail || {},
    contactList: data.contactList || [],
    qualificationList: data.qualificationList || [],
  }
}

async function handleSave() {
  if (readonly.value) {
    editorVisible.value = false
    return
  }
  const autoGenerate = Boolean(form.value.autoGenerateCode) && !form.value.id
  if (!autoGenerate && !form.value.supplierCode?.trim()) {
    message.warning('供应商编码不能为空')
    activeTab.value = 'main'
    return
  }
  if (!form.value.supplierFullName?.trim()) {
    message.warning('供应商全称不能为空')
    activeTab.value = 'main'
    return
  }
  saving.value = true
  try {
    const payload = normalizeEditorData(form.value)
    if (payload.id) {
      payload.autoGenerateCode = false
      await supplierApi.update(payload)
    } else {
      payload.autoGenerateCode = autoGenerate
      if (autoGenerate) {
        payload.supplierCode = ''
      }
      await supplierApi.create(payload)
    }
    editorVisible.value = false
    await tableRef.value?.refresh?.()
  } finally {
    saving.value = false
  }
}

function handleEditorCancel() {
  editorVisible.value = false
}

async function handleLogoUpload(file: File) {
  const url = await uploadFile(file, { moduleCode: 'basic_supplier', moduleName: '供应商Logo' })
  form.value.logoUrl = url
  return false
}

function addContact() {
  form.value.contactList = form.value.contactList || []
  form.value.contactList.push({} as SupplierContact)
}

function removeContact(index: number) {
  form.value.contactList?.splice(index, 1)
}

function addQualification() {
  form.value.qualificationList = form.value.qualificationList || []
  form.value.qualificationList.push({ valid: true } as SupplierQualification)
}

function removeQualification(index: number) {
  form.value.qualificationList?.splice(index, 1)
}

function handleDelete(record: Supplier) {
  if (isTenantLinked(record)) {
    message.warning('已关联租户的供应商不允许删除')
    return
  }
  Modal.confirm({
    title: '确认删除供应商？',
    content: `删除后会同步删除 ${record.supplierFullName} 的详情、联系人和资质信息。`,
    async onOk() {
      await supplierApi.delete(record.id!)
      await tableRef.value?.refresh?.()
    },
  })
}

async function handleGenerateTenant(record: Supplier) {
  if (isTenantLinked(record)) {
    return
  }
  const tenantCode = await supplierApi.generateTenant(record.id!)
  message.success(`供应商租户已关联：${tenantCode}`)
  await tableRef.value?.refresh?.()
}

async function handleStartReview(record: Supplier) {
  if (record.reviewStatus !== 1) {
    message.warning('仅未审查供应商允许发起审查')
    return
  }
  await supplierApi.startReview(record.id!)
  await tableRef.value?.refresh?.()
}

async function handleSync() {
  const result = await supplierApi.syncThirdParty({ payload: {} })
  message.success(`同步完成：总数 ${result.totalCount || 0}，失败 ${result.failedCount || 0}`)
}

async function handleImport(file: File) {
  const result = await supplierApi.import(file)
  message.success(`导入完成：新增 ${result.createdCount || 0}，更新 ${result.updatedCount || 0}`)
  await tableRef.value?.refresh?.()
  return false
}

async function downloadTemplate() {
  const response: any = await supplierApi.importTemplate()
  downloadBlob(response?.data || response, '供应商主数据导入模板.xlsx')
}

async function handleExport() {
  const query = tableRef.value?.getQuery?.() || {}
  const response: any = await supplierApi.export(query)
  downloadBlob(response?.data || response, '供应商主数据.xlsx')
}

function downloadBlob(blob: Blob, fileName: string) {
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = fileName
  link.click()
  URL.revokeObjectURL(url)
}

function isTenantLinked(record: Supplier) {
  return Boolean(record.hasRelatedTenant || record.relatedTenantCode)
}

function labelOf(options: OptionItem[] | undefined, value: OptionValue | undefined) {
  const matched = (options || []).find(item => String(item.value) === String(value ?? ''))
  return matched?.label || value || '-'
}

function cooperationColor(value?: string) {
  return ({ '1': 'gold', '2': 'green', '3': 'orange', '4': 'red' } as Record<string, string>)[value || ''] || 'default'
}

function riskColor(value?: string) {
  return ({ LOW: 'green', MEDIUM: 'orange', HIGH: 'red' } as Record<string, string>)[value || ''] || 'default'
}

function reviewColor(value?: number) {
  return ({ 0: 'default', 1: 'orange', 2: 'processing', 3: 'green' } as Record<number, string>)[Number(value)] || 'default'
}
</script>

<style scoped lang="less">
.supplier-page {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 100%;
  padding: 20px;
  overflow: hidden;
  box-sizing: border-box;
  background: var(--supplier-page-bg);
}

.supplier-page--light {
  --supplier-page-bg:
    radial-gradient(circle at 8% 4%, color-mix(in srgb, var(--fx-primary, #1677ff) 12%, transparent), transparent 28%),
    linear-gradient(180deg, var(--fx-bg-layout, #f8fafc), #eef3f8);
}

.supplier-page--dark {
  --supplier-page-bg:
    radial-gradient(circle at 8% 4%, color-mix(in srgb, var(--fx-primary, #1677ff) 18%, transparent), transparent 28%),
    linear-gradient(180deg, #111827 0%, #0b1220 46%, #05070b 100%);
}

.supplier-page__header {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  margin-bottom: 16px;
  padding: 24px 28px;
  border: 1px solid var(--fx-border-color, rgba(148, 163, 184, 0.18));
  border-radius: 8px;
  background: var(--fx-bg-container, #ffffff);
  box-shadow: var(--fx-shadow-secondary, 0 10px 28px rgba(15, 23, 42, 0.06));
}

.supplier-page :deep(.fx-dynamic-table) {
  flex: 1 1 auto;
  min-height: 0;
}

.supplier-page__header h1 {
  margin: 10px 0 8px;
  color: var(--fx-text-primary, #111827);
  font-size: 28px;
}

.supplier-page__header p {
  margin: 0;
  color: var(--fx-text-secondary, #64748b);
}

.supplier-name {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.supplier-name span {
  color: var(--fx-text-secondary, #64748b);
  font-size: 12px;
}

.logo-uploader {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  min-height: 54px;
  color: var(--fx-text-secondary, #64748b);
  cursor: pointer;
}

.danger-link {
  color: #ff4d4f;
}

.disabled {
  color: var(--fx-text-disabled, #bfbfbf);
  cursor: not-allowed;
  pointer-events: none;
}

.full-width {
  width: 100%;
}

.sub-toolbar {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 12px;
}

@media (max-width: 768px) {
  .supplier-page {
    padding: 12px;
  }

  .supplier-page__header {
    align-items: flex-start;
    flex-direction: column;
    padding: 18px;
  }
}
</style>
