<template>
  <div class="material-page" :class="`material-page--${appStore.theme}`">
    <div class="material-page__header">
      <div>
        <a-tag color="blue">{{ t('basic.material.sectionTag') }}</a-tag>
        <h1>{{ t('basic.material.title') }}</h1>
        <p>{{ t('basic.material.description') }}</p>
      </div>
      <a-space wrap>
        <a-button v-permission="'basic:material:extendConfig:query'" @click="openConfigManager">
          <SettingOutlined />
          {{ t('basic.material.extendConfig.manage') }}
        </a-button>
        <a-button v-permission="'basic:material:import'" @click="importDialogVisible = true">
          <UploadOutlined />
          {{ t('system.excel.commonImport.title') }}
        </a-button>
        <a-button
          v-permission="'basic:material:pullThirdParty'"
          :loading="pullingThirdParty"
          @click="handlePullThirdParty"
        >
          <CloudDownloadOutlined />
          {{ t('basic.material.pullThirdParty') }}
        </a-button>
        <a-button
          v-permission="'basic:material:sync'"
          :loading="syncingThirdParty"
          @click="handleSyncThirdParty"
        >
          <CloudSyncOutlined />
          {{ t('basic.material.syncThirdParty') }}
        </a-button>
        <a-button v-permission="'basic:material:add'" type="primary" @click="openCreate">
          <PlusOutlined /> {{ t('basic.material.add') }}
        </a-button>
      </a-space>
    </div>

    <a-tabs v-model:active-key="activeFilterTab" class="material-tabs" @change="handleTabChange">
      <a-tab-pane v-for="tab in materialTabs" :key="tab.value" :tab="tab.label" />
    </a-tabs>

    <FxDynamicTable
      ref="tableRef"
      table-code="MaterialTable"
      :request="handleRequest"
      :dict-options="dictOptions"
      row-key="id"
    >
      <template #materialType="{ record }">
        <a-tag :color="materialTypeColor(record.materialType)">
          {{ labelOf(materialTypeOptions, record.materialType) }}
        </a-tag>
      </template>

      <template #status="{ record }">
        <a-tag :color="record.status === 1 ? 'success' : 'default'">
          {{ record.status === 1 ? t('common.enable') : t('common.disable') }}
        </a-tag>
      </template>

      <template #approvalStatus="{ record }">
        <a-tag :color="approvalStatusColor(record.approvalStatus)">
          {{ labelOf(approvalStatusOptions, record.approvalStatus) }}
        </a-tag>
      </template>

      <template #action="{ record }">
        <a-space>
          <a v-permission="'basic:material:query'" @click="openDetail(record)">{{ t('common.detail') }}</a>
          <a v-permission="'basic:material:edit'" @click="openEdit(record)">{{ t('common.edit') }}</a>
          <a v-permission="'basic:material:delete'" class="danger-link" @click="handleDelete(record)">
            {{ t('common.delete') }}
          </a>
        </a-space>
      </template>
    </FxDynamicTable>

    <BaseFormDialog
      v-model:open="editorVisible"
      mode="drawer"
      :title="editorTitle"
      width="82vw"
      :loading="saving || editorLoading"
      :readonly="readonly"
      :cancel-text="readonly ? t('common.close') : ''"
      :mask-closable="true"
      :body-style="{ maxHeight: 'calc(100vh - 128px)', overflowY: 'auto' }"
      @submit="handleEditorSubmit"
      @cancel="handleEditorCancel"
    >
      <a-spin :spinning="editorLoading">
        <a-tabs v-model:active-key="editorTab">
          <a-tab-pane key="main" :tab="t('basic.material.editorTabs.main')">
            <a-form :model="form" layout="vertical">
              <a-row :gutter="16">
                <a-col :xs="24" :md="12">
                  <a-form-item :label="t('basic.material.fields.code')" required>
                    <a-input v-model:value="form.materialCode" :disabled="readonly" :placeholder="t('basic.material.placeholder.code')" />
                  </a-form-item>
                </a-col>
                <a-col :xs="24" :md="12">
                  <a-form-item :label="t('basic.material.fields.name')" required>
                    <a-input v-model:value="form.materialName" :disabled="readonly" :placeholder="t('basic.material.placeholder.name')" />
                  </a-form-item>
                </a-col>
              </a-row>
              <a-row :gutter="16">
                <a-col :xs="24" :md="12">
                  <a-form-item :label="t('basic.material.fields.type')" required>
                    <a-select
                      v-model:value="form.materialType"
                      :disabled="readonly || activeFilterTab !== ALL_TAB"
                      :options="materialTypeOptions"
                      :placeholder="t('basic.material.placeholder.type')"
                      @change="reloadEditorExtendSchema"
                    />
                  </a-form-item>
                </a-col>
                <a-col :xs="24" :md="12">
                  <a-form-item :label="t('basic.material.fields.category')">
                    <a-input v-model:value="form.materialCategory" :disabled="readonly" :placeholder="t('basic.material.placeholder.category')" />
                  </a-form-item>
                </a-col>
              </a-row>
              <a-row :gutter="16">
                <a-col :xs="24" :md="12">
                  <a-form-item :label="t('basic.material.fields.specification')">
                    <a-input v-model:value="form.specification" :disabled="readonly" :placeholder="t('basic.material.placeholder.specification')" />
                  </a-form-item>
                </a-col>
                <a-col :xs="24" :md="12">
                  <a-form-item :label="t('basic.material.fields.unit')">
                    <a-select v-model:value="form.unit" :disabled="readonly" :placeholder="t('basic.material.placeholder.unit')" allow-clear show-search>
                      <a-select-option v-for="item in unitOptions" :key="item.id" :value="item.unitName">
                        {{ item.unitName }} ({{ item.unitSymbol }})
                      </a-select-option>
                    </a-select>
                  </a-form-item>
                </a-col>
              </a-row>
              <a-row :gutter="16">
                <a-col :xs="24" :md="12">
                  <a-form-item :label="t('basic.material.fields.brand')">
                    <a-input v-model:value="form.brand" :disabled="readonly" :placeholder="t('basic.material.placeholder.brand')" />
                  </a-form-item>
                </a-col>
                <a-col :xs="24" :md="12">
                  <a-form-item :label="t('basic.material.fields.status')">
                    <a-select v-model:value="form.status" :disabled="readonly" :options="statusOptions" />
                  </a-form-item>
                </a-col>
              </a-row>
              <a-form-item :label="t('basic.material.fields.remark')">
                <a-textarea v-model:value="form.remark" :disabled="readonly" :rows="2" :placeholder="t('basic.material.placeholder.remark')" />
              </a-form-item>
              <a-form-item :label="t('basic.material.fields.description')">
                <a-textarea v-model:value="form.description" :disabled="readonly" :rows="3" :placeholder="t('basic.material.placeholder.description')" />
              </a-form-item>
            </a-form>
          </a-tab-pane>

          <a-tab-pane key="extend" :tab="t('basic.material.editorTabs.extend')">
            <div class="extend-toolbar">
              <div>
                <strong>{{ t('basic.material.extend.title') }}</strong>
                <span>{{ t('basic.material.extend.subtitle') }}</span>
              </div>
              <a-button v-permission="'basic:material:extendConfig:query'" type="primary" ghost size="small" @click="openConfigManager">
                <SettingOutlined /> {{ t('basic.material.extendConfig.manage') }}
              </a-button>
            </div>

            <a-tabs v-model:active-key="activeExtendModule" tab-position="left" class="extend-module-tabs">
              <a-tab-pane v-for="module in extendModuleOptions" :key="module.value" :tab="module.label">
                <a-empty
                  v-if="!fieldsByModule(module.value).length"
                  :description="t('basic.material.extend.noFields')"
                />
                <a-table
                  v-else
                  size="small"
                  :columns="extendFieldColumns"
                  :data-source="fieldsByModule(module.value)"
                  :pagination="false"
                  row-key="fieldName"
                >
                  <template #bodyCell="{ column, record }">
                    <template v-if="column.key === 'fieldLabel'">
                      <a-space>
                        <span>{{ record.fieldLabel }}</span>
                        <a-tag v-if="record.required === 1" color="red">{{ t('basic.material.extend.required') }}</a-tag>
                      </a-space>
                    </template>
                    <template v-else-if="column.key === 'fieldType'">
                      <a-tag>{{ fieldTypeLabel(record.fieldType) }}</a-tag>
                    </template>
                    <template v-else-if="column.key === 'value'">
                      <a-input-number
                        v-if="record.fieldType === 'NUMBER'"
                        v-model:value="record.value"
                        :disabled="readonly"
                        class="field-control"
                      />
                      <a-date-picker
                        v-else-if="record.fieldType === 'DATE'"
                        v-model:value="record.value"
                        :disabled="readonly"
                        value-format="YYYY-MM-DD"
                        class="field-control"
                      />
                      <a-switch
                        v-else-if="record.fieldType === 'BOOLEAN'"
                        v-model:checked="record.value"
                        :disabled="readonly"
                        :checked-value="true"
                        :un-checked-value="false"
                      />
                      <a-select
                        v-else-if="record.fieldType === 'SELECT'"
                        v-model:value="record.value"
                        :disabled="readonly"
                        :options="record.options || []"
                        allow-clear
                        class="field-control"
                      />
                      <a-select
                        v-else-if="record.fieldType === 'MULTI_SELECT'"
                        v-model:value="record.value"
                        :disabled="readonly"
                        :options="record.options || []"
                        mode="multiple"
                        allow-clear
                        class="field-control"
                      />
                      <a-textarea
                        v-else-if="record.fieldType === 'TEXT'"
                        v-model:value="record.value"
                        :disabled="readonly"
                        :auto-size="{ minRows: 2, maxRows: 5 }"
                        class="field-control"
                      />
                      <a-input v-else v-model:value="record.value" :disabled="readonly" class="field-control" />
                    </template>
                    <template v-else-if="column.key === 'action'">
                      <a @click="openFieldValueDetail(record)">{{ t('common.detail') }}</a>
                    </template>
                  </template>
                </a-table>

                <a-collapse v-if="Object.keys(unknownValuesByModule(module.value)).length" class="unknown-collapse">
                  <a-collapse-panel key="unknown" :header="t('basic.material.extend.unknownFields')">
                    <a-descriptions size="small" bordered :column="1">
                      <a-descriptions-item v-for="(value, key) in unknownValuesByModule(module.value)" :key="key" :label="key">
                        {{ displayRawValue(value) }}
                      </a-descriptions-item>
                    </a-descriptions>
                  </a-collapse-panel>
                </a-collapse>
              </a-tab-pane>
            </a-tabs>
          </a-tab-pane>

          <a-tab-pane key="packaging" :tab="t('basic.material.editorTabs.packaging')">
            <a-spin :spinning="packagingRelationLoading">
              <a-row :gutter="16">
                <a-col :xs="24" :md="8">
                  <a-form-item :label="t('basic.material.packaging.small')">
                    <a-select
                      v-model:value="packagingRelation.smallPackagingTypeId"
                      :disabled="readonly"
                      :options="packagingTypeOptions"
                      :placeholder="t('basic.material.packaging.placeholder')"
                      allow-clear
                      show-search
                      :filter-option="filterOption"
                    />
                  </a-form-item>
                </a-col>
                <a-col :xs="24" :md="8">
                  <a-form-item :label="t('basic.material.packaging.medium')">
                    <a-select
                      v-model:value="packagingRelation.mediumPackagingTypeId"
                      :disabled="readonly"
                      :options="packagingTypeOptions"
                      :placeholder="t('basic.material.packaging.placeholder')"
                      allow-clear
                      show-search
                      :filter-option="filterOption"
                    />
                  </a-form-item>
                </a-col>
                <a-col :xs="24" :md="8">
                  <a-form-item :label="t('basic.material.packaging.large')">
                    <a-select
                      v-model:value="packagingRelation.largePackagingTypeId"
                      :disabled="readonly"
                      :options="packagingTypeOptions"
                      :placeholder="t('basic.material.packaging.placeholder')"
                      allow-clear
                      show-search
                      :filter-option="filterOption"
                    />
                  </a-form-item>
                </a-col>
              </a-row>

              <a-descriptions bordered size="small" :column="1" class="packaging-preview">
                <a-descriptions-item :label="t('basic.material.packaging.preview')">
                  {{ packagingPreviewText }}
                </a-descriptions-item>
              </a-descriptions>
            </a-spin>
          </a-tab-pane>
        </a-tabs>
      </a-spin>
    </BaseFormDialog>

    <BaseFormDialog
      v-model:open="fieldDetailVisible"
      mode="modal"
      :title="t('basic.material.extend.fieldDetail')"
      width="560px"
      :readonly="true"
      :cancel-text="t('common.close')"
    >
      <a-descriptions v-if="selectedField" bordered size="small" :column="1">
        <a-descriptions-item :label="t('basic.material.extend.fieldLabel')">{{ selectedField.fieldLabel }}</a-descriptions-item>
        <a-descriptions-item :label="t('basic.material.extend.fieldName')">{{ selectedField.fieldName }}</a-descriptions-item>
        <a-descriptions-item :label="t('basic.material.extend.fieldType')">{{ fieldTypeLabel(selectedField.fieldType) }}</a-descriptions-item>
        <a-descriptions-item :label="t('basic.material.extend.currentValue')">{{ displayRawValue(selectedField.value) }}</a-descriptions-item>
      </a-descriptions>
    </BaseFormDialog>

    <BaseFormDialog
      v-model:open="configVisible"
      mode="drawer"
      :title="t('basic.material.extendConfig.title')"
      width="900px"
      :loading="configLoading"
      :footer="false"
      :body-style="{ maxHeight: 'calc(100vh - 88px)', overflowY: 'auto' }"
    >
      <div class="config-filter">
        <a-select v-model:value="configQuery.materialType" :options="materialTypeOptions" class="config-select" @change="loadConfigFields" />
        <a-select v-model:value="configQuery.module" :options="extendModuleOptions" class="config-select" @change="loadConfigFields" />
        <a-button v-permission="'basic:material:extendConfig:add'" type="primary" @click="openConfigCreate">
          <PlusOutlined /> {{ t('basic.material.extendConfig.addField') }}
        </a-button>
      </div>
      <a-table
        size="small"
        :loading="configLoading"
        :columns="configColumns"
        :data-source="configFields"
        :pagination="false"
        row-key="id"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'fieldType'">
            <a-tag>{{ fieldTypeLabel(record.fieldType) }}</a-tag>
          </template>
          <template v-else-if="column.key === 'required'">
            <a-tag :color="record.required === 1 ? 'red' : 'default'">
              {{ record.required === 1 ? t('common.yes') : t('common.no') }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'status'">
            <a-switch
              :checked="record.status === 1"
              size="small"
              @change="(checked: boolean) => handleConfigStatus(record, checked)"
            />
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a @click="openConfigDetail(record)">{{ t('common.detail') }}</a>
              <a v-permission="'basic:material:extendConfig:edit'" @click="openConfigEdit(record)">{{ t('common.edit') }}</a>
              <a v-permission="'basic:material:extendConfig:delete'" class="danger-link" @click="handleConfigDelete(record)">
                {{ t('common.delete') }}
              </a>
            </a-space>
          </template>
        </template>
      </a-table>
    </BaseFormDialog>

    <BaseFormDialog
      v-model:open="configEditorVisible"
      mode="modal"
      :title="configEditorTitle"
      width="680px"
      :loading="configSaving"
      :readonly="configReadonly"
      :cancel-text="configReadonly ? t('common.close') : ''"
      @submit="handleConfigSubmit"
    >
      <a-form :model="configForm" layout="vertical">
        <a-row :gutter="16">
          <a-col :xs="24" :md="12">
            <a-form-item :label="t('basic.material.extend.module')" required>
              <a-select v-model:value="configForm.module" :disabled="configReadonly" :options="extendModuleOptions" />
            </a-form-item>
          </a-col>
          <a-col :xs="24" :md="12">
            <a-form-item :label="t('basic.material.fields.type')" required>
              <a-select v-model:value="configForm.materialType" :disabled="configReadonly" :options="materialTypeOptions" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :xs="24" :md="12">
            <a-form-item :label="t('basic.material.extend.fieldName')" required>
              <a-input v-model:value="configForm.fieldName" :disabled="configReadonly" />
            </a-form-item>
          </a-col>
          <a-col :xs="24" :md="12">
            <a-form-item :label="t('basic.material.extend.fieldLabel')" required>
              <a-input v-model:value="configForm.fieldLabel" :disabled="configReadonly" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :xs="24" :md="12">
            <a-form-item :label="t('basic.material.extend.fieldType')" required>
              <a-select v-model:value="configForm.fieldType" :disabled="configReadonly" :options="fieldTypeOptions" />
            </a-form-item>
          </a-col>
          <a-col :xs="24" :md="12">
            <a-form-item :label="t('basic.material.extend.orderNum')">
              <a-input-number v-model:value="configForm.orderNum" :disabled="configReadonly" class="full-width" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :xs="24" :md="12">
            <a-form-item :label="t('basic.material.extend.required')">
              <a-switch v-model:checked="configFormRequired" :disabled="configReadonly" />
            </a-form-item>
          </a-col>
          <a-col :xs="24" :md="12">
            <a-form-item :label="t('basic.material.extend.defaultValue')">
              <a-input v-model:value="configForm.defaultValue" :disabled="configReadonly" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item v-if="['SELECT', 'MULTI_SELECT'].includes(configForm.fieldType)" :label="t('basic.material.extend.options')">
          <a-textarea
            v-model:value="configOptionsText"
            :disabled="configReadonly"
            :rows="4"
            :placeholder="t('basic.material.extend.optionsPlaceholder')"
          />
        </a-form-item>
        <a-form-item :label="t('basic.material.fields.remark')">
          <a-textarea v-model:value="configForm.remark" :disabled="configReadonly" :rows="2" />
        </a-form-item>
      </a-form>
    </BaseFormDialog>

    <CommonImportDialog
      v-model:open="importDialogVisible"
      table-code="basic_material"
      @success="handleImportSuccess"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, reactive, ref } from 'vue'
import { message, Modal } from 'ant-design-vue'
import {
  CloudDownloadOutlined,
  CloudSyncOutlined,
  PlusOutlined,
  SettingOutlined,
  UploadOutlined,
} from '@ant-design/icons-vue'
import { useI18n } from 'vue-i18n'
import {
  materialApi,
  type Material,
  type MaterialExtendConfig,
  type MaterialExtendView,
  type MaterialExtendViewField,
  type MaterialPageParam,
} from '@/api/basic/material'
import {
  getAllPackagingTypes,
  listPackagingByMaterial,
  savePackagingByMaterial,
  type MaterialPackagingRelation,
  type PackagingType,
} from '@/api/basic/packaging'
import { getAllUnits } from '@/api/basic/unit'
import FxDynamicTable from '@/components/common/FxDynamicTable.vue'
import BaseFormDialog from '@/components/common/BaseFormDialog.vue'
import CommonImportDialog from '@/components/excel/CommonImportDialog.vue'
import { useAppStore } from '@/stores/app'

const { t } = useI18n()
const appStore = useAppStore()

const ALL_TAB = 'ALL'
const DEFAULT_MATERIAL_TYPE = 'RAW_MATERIAL'

type ExtendEditorModule = MaterialExtendView & { fields: Array<MaterialExtendViewField & { __module: string }> }
type MaterialForm = Partial<Material> & { extendViewList: ExtendEditorModule[] }
type PackagingRelationForm = {
  smallPackagingTypeId?: number | null
  mediumPackagingTypeId?: number | null
  largePackagingTypeId?: number | null
}

const tableRef = ref()
const activeFilterTab = ref(ALL_TAB)
const editorVisible = ref(false)
const importDialogVisible = ref(false)
const editorLoading = ref(false)
const saving = ref(false)
const syncingThirdParty = ref(false)
const pullingThirdParty = ref(false)
const packagingRelationLoading = ref(false)
const isEdit = ref(false)
const readonly = ref(false)
const editorTab = ref('main')
const activeExtendModule = ref('PURCHASE')
const unitOptions = ref<any[]>([])
const packagingTypes = ref<PackagingType[]>([])
const fieldDetailVisible = ref(false)
const selectedField = ref<MaterialExtendViewField | null>(null)
const configVisible = ref(false)
const configEditorVisible = ref(false)
const configLoading = ref(false)
const configSaving = ref(false)
const configReadonly = ref(false)
const configFields = ref<MaterialExtendConfig[]>([])
const configOptionsText = ref('')

const configQuery = reactive({
  module: 'PURCHASE',
  materialType: DEFAULT_MATERIAL_TYPE,
})

const configForm = reactive<MaterialExtendConfig>({
  module: 'PURCHASE',
  materialType: DEFAULT_MATERIAL_TYPE,
  fieldName: '',
  fieldLabel: '',
  fieldType: 'STRING',
  fieldOptions: '',
  required: 0,
  orderNum: 0,
  status: 1,
  defaultValue: '',
  remark: '',
})

const form = reactive<MaterialForm>({
  id: undefined,
  materialCode: '',
  materialName: '',
  materialType: DEFAULT_MATERIAL_TYPE,
  materialCategory: '',
  specification: '',
  unit: '',
  brand: '',
  status: 1,
  remark: '',
  description: '',
  extendViewList: [],
})

const packagingRelation = reactive<PackagingRelationForm>({
  smallPackagingTypeId: null,
  mediumPackagingTypeId: null,
  largePackagingTypeId: null,
})

const configFormRequired = computed({
  get: () => configForm.required === 1,
  set: (value: boolean) => {
    configForm.required = value ? 1 : 0
  },
})

const materialTabs = computed(() => [
  { value: ALL_TAB, label: t('basic.material.tabs.all') },
  { value: 'RAW_MATERIAL', label: t('basic.material.types.raw') },
  { value: 'SEMI_FINISHED', label: t('basic.material.types.semiFinished') },
  { value: 'FINISHED_GOODS', label: t('basic.material.types.finished') },
])

const materialTypeOptions = computed(() => [
  { value: 'RAW_MATERIAL', label: t('basic.material.types.raw') },
  { value: 'SEMI_FINISHED', label: t('basic.material.types.semiFinished') },
  { value: 'FINISHED_GOODS', label: t('basic.material.types.finished') },
  { value: 'OTHER', label: t('basic.material.types.other') },
])

const approvalStatusOptions = computed(() => [
  { value: 'NO_APPROVAL_REQUIRED', label: t('basic.material.approval.noApprovalRequired') },
  { value: 'PENDING', label: t('basic.material.approval.pending') },
  { value: 'APPROVED', label: t('basic.material.approval.approved') },
  { value: 'REJECTED', label: t('basic.material.approval.rejected') },
])

const statusOptions = computed(() => [
  { value: 1, label: t('common.enable') },
  { value: 0, label: t('common.disable') },
])

const packagingTypeOptions = computed(() => packagingTypes.value.map((item) => ({
  value: item.id,
  label: `${item.packagingCode || ''} ${item.packagingName || ''}`.trim(),
})))

const packagingPreviewText = computed(() => {
  const segments = [
    `${t('basic.material.packaging.small')}: ${packagingName(packagingRelation.smallPackagingTypeId)}`,
    `${t('basic.material.packaging.medium')}: ${packagingName(packagingRelation.mediumPackagingTypeId)}`,
    `${t('basic.material.packaging.large')}: ${packagingName(packagingRelation.largePackagingTypeId)}`,
  ]
  return segments.join(' / ')
})

const extendModuleOptions = computed(() => [
  { value: 'PURCHASE', label: t('basic.material.extend.modules.purchase') },
  { value: 'INVENTORY', label: t('basic.material.extend.modules.inventory') },
  { value: 'PRODUCTION', label: t('basic.material.extend.modules.production') },
  { value: 'SALES', label: t('basic.material.extend.modules.sales') },
])

const fieldTypeOptions = computed(() => [
  { value: 'STRING', label: t('basic.material.extend.types.string') },
  { value: 'TEXT', label: t('basic.material.extend.types.text') },
  { value: 'NUMBER', label: t('basic.material.extend.types.number') },
  { value: 'DATE', label: t('basic.material.extend.types.date') },
  { value: 'BOOLEAN', label: t('basic.material.extend.types.boolean') },
  { value: 'SELECT', label: t('basic.material.extend.types.select') },
  { value: 'MULTI_SELECT', label: t('basic.material.extend.types.multiSelect') },
])

const extendFieldColumns = computed(() => [
  { title: t('basic.material.extend.fieldLabel'), dataIndex: 'fieldLabel', key: 'fieldLabel', width: 220 },
  { title: t('basic.material.extend.fieldType'), dataIndex: 'fieldType', key: 'fieldType', width: 120 },
  { title: t('basic.material.extend.currentValue'), dataIndex: 'value', key: 'value' },
  { title: t('common.action'), dataIndex: 'action', key: 'action', width: 100 },
])

const configColumns = computed(() => [
  { title: t('basic.material.extend.fieldLabel'), dataIndex: 'fieldLabel', key: 'fieldLabel' },
  { title: t('basic.material.extend.fieldName'), dataIndex: 'fieldName', key: 'fieldName' },
  { title: t('basic.material.extend.fieldType'), dataIndex: 'fieldType', key: 'fieldType', width: 120 },
  { title: t('basic.material.extend.required'), dataIndex: 'required', key: 'required', width: 90 },
  { title: t('basic.material.extend.orderNum'), dataIndex: 'orderNum', key: 'orderNum', width: 90 },
  { title: t('common.status'), dataIndex: 'status', key: 'status', width: 90 },
  { title: t('common.action'), dataIndex: 'action', key: 'action', width: 180, fixed: 'right' },
])

const dictOptions = computed(() => ({
  materialType: materialTypeOptions.value,
  material_type: materialTypeOptions.value,
  status: statusOptions.value,
  common_status: statusOptions.value,
  approvalStatus: approvalStatusOptions.value,
  material_approval_status: approvalStatusOptions.value,
}))

const editorTitle = computed(() => {
  if (readonly.value) return t('basic.material.detail')
  return isEdit.value ? t('basic.material.edit') : t('basic.material.add')
})

const configEditorTitle = computed(() => {
  if (configReadonly.value) return t('basic.material.extendConfig.fieldDetail')
  return configForm.id ? t('basic.material.extendConfig.editField') : t('basic.material.extendConfig.addField')
})

async function handleRequest(payload: { page: { current: number; pageSize: number }; query: Record<string, any> }) {
  const params: MaterialPageParam = {
    pageNum: payload.page.current,
    pageSize: payload.page.pageSize,
    ...payload.query,
  }
  if (activeFilterTab.value !== ALL_TAB) {
    params.materialType = activeFilterTab.value
  }
  const result: any = await materialApi.page(params)
  return { records: result?.records || [], total: Number(result?.total || 0) }
}

function handleTabChange() {
  nextTick(() => tableRef.value?.refresh?.())
}

function currentCreateType() {
  return activeFilterTab.value === ALL_TAB ? DEFAULT_MATERIAL_TYPE : activeFilterTab.value
}

function resetForm() {
  Object.assign(form, {
    id: undefined,
    materialCode: '',
    materialName: '',
    materialType: currentCreateType(),
    materialCategory: '',
    specification: '',
    unit: '',
    brand: '',
    status: 1,
    remark: '',
    description: '',
    extendViewList: [],
  })
  resetPackagingRelation()
}

function resetPackagingRelation() {
  packagingRelation.smallPackagingTypeId = null
  packagingRelation.mediumPackagingTypeId = null
  packagingRelation.largePackagingTypeId = null
}

async function openCreate() {
  resetForm()
  isEdit.value = false
  readonly.value = false
  editorTab.value = 'main'
  editorVisible.value = true
  await reloadEditorExtendSchema()
}

function openEdit(record: any) {
  openEditor(record, false)
}

function openDetail(record: any) {
  openEditor(record, true)
}

async function openEditor(record: any, readonlyMode: boolean) {
  resetForm()
  isEdit.value = !readonlyMode
  readonly.value = readonlyMode
  editorTab.value = 'main'
  editorVisible.value = true
  editorLoading.value = true
  try {
    const result = await materialApi.detail({ id: record.id })
    applyEditorData({ ...record, ...normalizeDetail(result) })
    await loadMaterialPackaging(form.id)
  } finally {
    editorLoading.value = false
  }
}

function normalizeDetail(result: any) {
  if (!result) return {}
  if (result.baseInfo) {
    return { ...result.baseInfo, extendViewList: result.extendViewList || [] }
  }
  return result
}

function applyEditorData(data: any) {
  Object.assign(form, {
    id: data.id,
    materialCode: data.materialCode || '',
    materialName: data.materialName || '',
    materialType: data.materialType || DEFAULT_MATERIAL_TYPE,
    materialCategory: data.materialCategory || '',
    specification: data.specification || '',
    unit: data.unit || '',
    brand: data.brand || '',
    status: data.status ?? 1,
    remark: data.remark || '',
    description: data.description || '',
    extendViewList: normalizeExtendViewList(data.extendViewList || []),
  })
  packagingRelation.smallPackagingTypeId = null
  packagingRelation.mediumPackagingTypeId = null
  packagingRelation.largePackagingTypeId = null
}

async function reloadEditorExtendSchema() {
  if (!form.materialType) return
  editorLoading.value = true
  try {
    const previous = new Map<string, ExtendEditorModule>()
    form.extendViewList.forEach((item) => previous.set(item.module, item))
    const modules: ExtendEditorModule[] = []
    for (const module of extendModuleOptions.value) {
      const schema: any = await materialApi.extendSchema({ module: module.value, materialType: form.materialType })
      const oldModule = previous.get(module.value)
      const fields = (schema?.fields || []).map((field: MaterialExtendConfig) => ({
        configId: field.id,
        fieldName: field.fieldName,
        fieldLabel: field.fieldLabel,
        fieldType: field.fieldType,
        fieldTypeName: field.fieldTypeName,
        options: field.options || [],
        required: field.required,
        defaultValue: field.defaultValue,
        orderNum: field.orderNum,
        value: oldModule?.fields.find((item) => item.fieldName === field.fieldName)?.value ?? field.defaultValue,
        __module: module.value,
      }))
      modules.push({
        module: module.value,
        moduleName: module.label,
        materialType: form.materialType,
        fields,
        unknownValues: oldModule?.unknownValues || {},
      })
    }
    form.extendViewList = modules
  } finally {
    editorLoading.value = false
  }
}

async function loadPackagingTypes() {
  if (packagingTypes.value.length) {
    return
  }
  const result: any = await getAllPackagingTypes()
  packagingTypes.value = Array.isArray(result) ? result : (result?.records || [])
}

async function loadMaterialPackaging(materialId?: number) {
  resetPackagingRelation()
  if (!materialId) {
    return
  }
  packagingRelationLoading.value = true
  try {
    await loadPackagingTypes()
    const result: any = await listPackagingByMaterial(materialId)
    const rows: MaterialPackagingRelation[] = Array.isArray(result) ? result : (result?.records || [])
    rows.forEach((item) => {
      if (item.packagingSlot === 'SMALL') {
        packagingRelation.smallPackagingTypeId = item.packagingTypeId || null
      } else if (item.packagingSlot === 'MEDIUM') {
        packagingRelation.mediumPackagingTypeId = item.packagingTypeId || null
      } else if (item.packagingSlot === 'LARGE') {
        packagingRelation.largePackagingTypeId = item.packagingTypeId || null
      }
    })
  } catch {
    resetPackagingRelation()
  } finally {
    packagingRelationLoading.value = false
  }
}

function normalizeExtendViewList(list: MaterialExtendView[]): ExtendEditorModule[] {
  const map = new Map<string, MaterialExtendView>()
  list.forEach((item) => map.set(item.module, item))
  return extendModuleOptions.value.map((module) => {
    const source = map.get(module.value)
    return {
      module: module.value,
      moduleName: module.label,
      materialType: form.materialType || DEFAULT_MATERIAL_TYPE,
      extendId: source?.extendId,
      extendJson: source?.extendJson,
      unknownValues: source?.unknownValues || {},
      fields: (source?.fields || []).map((field) => ({ ...field, __module: module.value })),
    }
  })
}

function fieldsByModule(module: string) {
  return form.extendViewList.find((item) => item.module === module)?.fields || []
}

function unknownValuesByModule(module: string) {
  return form.extendViewList.find((item) => item.module === module)?.unknownValues || {}
}

function handleEditorCancel() {
  editorVisible.value = false
}

function handleEditorSubmit() {
  if (readonly.value) {
    editorVisible.value = false
    return
  }
  handleSave()
}

async function handleSave() {
  if (!form.materialCode || !form.materialName || !form.materialType) {
    message.warning(t('validation.required'))
    return
  }
  const extendValueList = buildExtendValuePayload()
  if (!extendValueList) return

  saving.value = true
  try {
    const payload = {
      ...form,
      materialCode: form.materialCode?.trim(),
      materialName: form.materialName?.trim(),
      extendValueList,
      extendViewList: undefined,
    }
    if (isEdit.value) {
      await materialApi.update(payload)
      message.success(t('common.updateSuccess'))
    } else {
      const createdId = await materialApi.create(payload)
      form.id = createdId
      message.success(t('common.createSuccess'))
    }
    if (form.id) {
      await savePackagingByMaterial({
        materialId: form.id,
        smallPackagingTypeId: packagingRelation.smallPackagingTypeId,
        mediumPackagingTypeId: packagingRelation.mediumPackagingTypeId,
        largePackagingTypeId: packagingRelation.largePackagingTypeId,
      })
    }
    editorVisible.value = false
    tableRef.value?.refresh?.()
  } finally {
    saving.value = false
  }
}

function buildExtendValuePayload() {
  const payload = []
  for (const module of form.extendViewList) {
    const values: Record<string, any> = { ...(module.unknownValues || {}) }
    for (const field of module.fields) {
      if (field.required === 1 && isBlankValue(field.value)) {
        message.warning(`${module.moduleName} - ${field.fieldLabel}${t('basic.material.extend.requiredSuffix')}`)
        return null
      }
      if (!isBlankValue(field.value)) {
        values[field.fieldName] = field.value
      }
    }
    payload.push({ module: module.module, values })
  }
  return payload
}

function isBlankValue(value: any) {
  return value === undefined || value === null || value === '' || (Array.isArray(value) && value.length === 0)
}

function openFieldValueDetail(record: MaterialExtendViewField) {
  selectedField.value = record
  fieldDetailVisible.value = true
}

async function openConfigManager() {
  configQuery.materialType = form.materialType || currentCreateType()
  configVisible.value = true
  await loadConfigFields()
}

async function loadConfigFields() {
  configLoading.value = true
  try {
    const result: any = await materialApi.extendSchemaPage({
      module: configQuery.module,
      materialType: configQuery.materialType,
      pageNum: 1,
      pageSize: 200,
    })
    configFields.value = result?.records || []
  } finally {
    configLoading.value = false
  }
}

function resetConfigForm() {
  Object.assign(configForm, {
    id: undefined,
    module: configQuery.module,
    materialType: configQuery.materialType,
    fieldName: '',
    fieldLabel: '',
    fieldType: 'STRING',
    fieldOptions: '',
    required: 0,
    orderNum: configFields.value.length + 1,
    status: 1,
    defaultValue: '',
    remark: '',
  })
  configOptionsText.value = ''
}

function openConfigCreate() {
  resetConfigForm()
  configReadonly.value = false
  configEditorVisible.value = true
}

function openConfigEdit(record: MaterialExtendConfig) {
  Object.assign(configForm, record)
  configOptionsText.value = normalizeOptionsText(record)
  configReadonly.value = false
  configEditorVisible.value = true
}

function openConfigDetail(record: MaterialExtendConfig) {
  Object.assign(configForm, record)
  configOptionsText.value = normalizeOptionsText(record)
  configReadonly.value = true
  configEditorVisible.value = true
}

async function handleConfigSubmit() {
  if (configReadonly.value) {
    configEditorVisible.value = false
    return
  }
  if (!configForm.module || !configForm.materialType || !configForm.fieldName || !configForm.fieldLabel || !configForm.fieldType) {
    message.warning(t('validation.required'))
    return
  }
  const optionsJson = buildOptionsJson()
  if (optionsJson === null) return
  configSaving.value = true
  try {
    const payload = { ...configForm, fieldOptions: optionsJson }
    if (payload.id) {
      await materialApi.updateExtendField(payload)
      message.success(t('common.updateSuccess'))
    } else {
      await materialApi.createExtendField(payload)
      message.success(t('common.createSuccess'))
    }
    configEditorVisible.value = false
    await loadConfigFields()
    if (editorVisible.value && form.materialType === configQuery.materialType) {
      await reloadEditorExtendSchema()
    }
  } finally {
    configSaving.value = false
  }
}

function buildOptionsJson() {
  if (!['SELECT', 'MULTI_SELECT'].includes(configForm.fieldType)) return ''
  const rows = configOptionsText.value.split('\n').map((item) => item.trim()).filter(Boolean)
  const options = rows.map((row) => {
    const [label, value] = row.includes('|') ? row.split('|') : [row, row]
    return { label: label.trim(), value: (value || label).trim() }
  })
  if (!options.length) {
    message.warning(t('basic.material.extend.optionsRequired'))
    return null
  }
  return JSON.stringify(options)
}

function normalizeOptionsText(record: MaterialExtendConfig) {
  const options = record.options || parseOptions(record.fieldOptions)
  return options.map((item) => `${item.label}|${item.value}`).join('\n')
}

function parseOptions(value?: string) {
  if (!value) return []
  try {
    return JSON.parse(value)
  } catch {
    return []
  }
}

async function handleConfigStatus(record: MaterialExtendConfig, checked: boolean) {
  await materialApi.updateExtendFieldStatus({ id: record.id!, status: checked ? 1 : 0 })
  await loadConfigFields()
}

function handleConfigDelete(record: MaterialExtendConfig) {
  Modal.confirm({
    title: t('common.delete'),
    content: t('basic.material.extendConfig.confirmDelete', { name: record.fieldLabel }),
    onOk: async () => {
      await materialApi.deleteExtendField({ id: record.id! })
      message.success(t('common.deleteSuccess'))
      await loadConfigFields()
    },
  })
}

function handleDelete(record: any) {
  Modal.confirm({
    title: t('common.delete'),
    content: t('basic.material.confirmDelete', { name: record.materialName || '' }),
    onOk: async () => {
      await materialApi.delete({ id: record.id })
      message.success(t('common.deleteSuccess'))
      tableRef.value?.refresh?.()
    },
  })
}

function packagingSlotOptions() {
  return [
    { value: 'SMALL', label: t('basic.material.packaging.small') },
    { value: 'MEDIUM', label: t('basic.material.packaging.medium') },
    { value: 'LARGE', label: t('basic.material.packaging.large') },
  ]
}

async function handleSyncThirdParty() {
  syncingThirdParty.value = true
  try {
    const result: any = await materialApi.syncThirdParty()
    message.success(t('basic.material.syncSuccess', { total: result?.totalCount || 0 }))
  } finally {
    syncingThirdParty.value = false
  }
}

async function handlePullThirdParty() {
  pullingThirdParty.value = true
  try {
    const result: any = await materialApi.pullFromThirdParty()
    message.success(t('basic.material.pullSuccess', { total: result?.totalCount || 0 }))
    tableRef.value?.refresh?.()
  } finally {
    pullingThirdParty.value = false
  }
}

function handleImportSuccess() {
  importDialogVisible.value = false
  tableRef.value?.refresh?.()
}

function labelOf(options: Array<{ value: string | number; label: string }>, value: any) {
  const item = options.find((option) => option.value === value)
  return item ? item.label : (value || '-')
}

function packagingName(id?: number | null) {
  if (!id) {
    return '-'
  }
  const item = packagingTypes.value.find((packaging) => packaging.id === id)
  return item ? `${item.packagingCode || ''} ${item.packagingName || ''}`.trim() : '-'
}

function filterOption(input: string, option: any) {
  return String(option?.label || '').toLowerCase().includes(input.toLowerCase())
}

function fieldTypeLabel(type: string) {
  return labelOf(fieldTypeOptions.value, type)
}

function displayRawValue(value: any) {
  if (value === undefined || value === null || value === '') return '-'
  if (Array.isArray(value)) return value.join(', ')
  if (typeof value === 'object') return JSON.stringify(value)
  return String(value)
}

function approvalStatusColor(status: string) {
  const colorMap: Record<string, string> = {
    NO_APPROVAL_REQUIRED: 'default',
    PENDING: 'warning',
    APPROVED: 'success',
    REJECTED: 'error',
  }
  return colorMap[status] || 'default'
}

function materialTypeColor(type: string) {
  const colorMap: Record<string, string> = {
    RAW_MATERIAL: 'blue',
    SEMI_FINISHED: 'gold',
    FINISHED_GOODS: 'green',
    OTHER: 'default',
  }
  return colorMap[type] || 'default'
}

onMounted(async () => {
  try {
    const res: any = await getAllUnits()
    unitOptions.value = Array.isArray(res) ? res : (res?.records || res?.data || [])
  } catch {
    unitOptions.value = []
  }
  try {
    await loadPackagingTypes()
  } catch {
    packagingTypes.value = []
  }
})
</script>

<style scoped lang="less" src="@/styles/views/basic/material/index.less"></style>
