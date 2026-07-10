<template>
  <a-drawer
    :open="open"
    :title="drawerTitle"
    :width="1320"
    destroy-on-close
    @close="handleCancel"
  >
    <div class="codegen-drawer">
      <a-steps :current="currentStep" class="codegen-drawer__steps">
        <a-step :title="t('system.codegen.steps.basic')" />
        <a-step :title="t('system.codegen.steps.detail')" />
        <a-step :title="t('system.codegen.steps.preview')" />
      </a-steps>

      <div v-show="currentStep === 0" class="codegen-drawer__step">
        <a-form ref="basicFormRef" :model="formData" :rules="basicRules" layout="vertical">
          <a-row :gutter="16">
            <a-col :span="8">
              <a-form-item :label="t('system.codegen.configName')" name="configName">
                <a-input v-model:value="formData.configName" :placeholder="t('system.codegen.form.configName')" />
              </a-form-item>
            </a-col>
            <a-col :span="8">
              <a-form-item :label="t('system.codegen.datasource')" name="datasourceId">
                <a-select
                  v-model:value="formData.datasourceId"
                  :options="datasourceOptions"
                  :placeholder="t('system.codegen.form.datasource')"
                  @change="handleDatasourceChange"
                />
              </a-form-item>
            </a-col>
          </a-row>

          <a-row :gutter="16">
            <a-col :span="12">
              <a-form-item :label="t('system.codegen.pageType')" name="pageType">
                <a-radio-group v-model:value="formData.pageType" @change="handlePageTypeChange">
                  <a-radio-button value="SINGLE">{{ t('system.codegen.pageTypeSingle') }}</a-radio-button>
                  <a-radio-button value="MASTER_DETAIL">{{ t('system.codegen.pageTypeMasterDetail') }}</a-radio-button>
                  <a-radio-button value="TREE_SINGLE">{{ t('system.codegen.pageTypeTreeSingle') }}</a-radio-button>
                  <a-radio-button value="TREE_DOUBLE">{{ t('system.codegen.pageTypeTreeDouble') }}</a-radio-button>
                </a-radio-group>
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-alert :message="pageTypeTip" type="info" show-icon />
            </a-col>
          </a-row>

          <a-row :gutter="16">
            <a-col :span="8">
              <a-form-item :label="t('system.codegen.mainTableName')" name="mainTableName">
                <a-select
                  v-model:value="formData.mainTableName"
                  show-search
                  :options="tableOptions"
                  :placeholder="t('system.codegen.form.mainTableName')"
                  @change="handleMainTableChange"
                />
              </a-form-item>
            </a-col>
            <a-col v-if="showTreeTable" :span="8">
              <a-form-item :label="t('system.codegen.treeTableName')" name="treeTableName">
                <a-select
                  v-model:value="formData.treeTableName"
                  show-search
                  :options="treeTableOptions"
                  :placeholder="t('system.codegen.form.treeTableName')"
                  @change="handleTreeTableChange"
                />
              </a-form-item>
            </a-col>
            <a-col v-if="showSubTable" :span="8">
              <a-form-item :label="t('system.codegen.subTableName')" name="subTableName">
                <a-select
                  v-model:value="formData.subTableName"
                  show-search
                  :options="subTableOptions"
                  :placeholder="t('system.codegen.form.subTableName')"
                  @change="handleSubTableChange"
                />
              </a-form-item>
            </a-col>
          </a-row>

          <a-row :gutter="16">
            <a-col :span="8">
              <a-form-item name="moduleName">
                <template #label>
                  <LabelHelp :label="t('system.codegen.moduleName')" :help="t('system.codegen.help.moduleName')" />
                </template>
                <a-input v-model:value="formData.moduleName" :placeholder="t('system.codegen.form.moduleName')" />
              </a-form-item>
            </a-col>
            <a-col :span="8">
              <a-form-item name="bizName">
                <template #label>
                  <LabelHelp :label="t('system.codegen.bizName')" :help="t('system.codegen.help.bizName')" />
                </template>
                <a-input v-model:value="formData.bizName" :placeholder="t('system.codegen.form.bizName')" />
              </a-form-item>
            </a-col>
            <a-col :span="8">
              <a-form-item name="author">
                <template #label>
                  <LabelHelp :label="t('system.codegen.author')" :help="t('system.codegen.help.author')" />
                </template>
                <a-input v-model:value="formData.author" :placeholder="t('system.codegen.form.author')" />
              </a-form-item>
            </a-col>
          </a-row>

          <a-row :gutter="16">
            <a-col :span="8">
              <a-form-item name="entityName">
                <template #label>
                  <LabelHelp :label="t('system.codegen.entityName')" :help="t('system.codegen.help.entityName')" />
                </template>
                <a-input v-model:value="formData.entityName" :placeholder="t('system.codegen.form.entityName')" />
              </a-form-item>
            </a-col>
            <a-col v-if="showTreeTable" :span="8">
              <a-form-item :label="t('system.codegen.treeEntityName')" name="treeEntityName">
                <a-input v-model:value="formData.treeEntityName" :placeholder="t('system.codegen.form.treeEntityName')" />
              </a-form-item>
            </a-col>
            <a-col v-if="showSubTable" :span="8">
              <a-form-item :label="t('system.codegen.subEntityName')" name="subEntityName">
                <a-input v-model:value="formData.subEntityName" :placeholder="t('system.codegen.form.subEntityName')" />
              </a-form-item>
            </a-col>
            <a-col v-if="!showTreeTable && !showSubTable" :span="8">
              <a-form-item name="packageName">
                <template #label>
                  <LabelHelp :label="t('system.codegen.packageName')" :help="t('system.codegen.help.packageName')" />
                </template>
                <a-input v-model:value="formData.packageName" :placeholder="t('system.codegen.form.packageName')" />
              </a-form-item>
            </a-col>
          </a-row>

          <a-row :gutter="16">
            <a-col v-if="showTreeTable || showSubTable" :span="8">
              <a-form-item name="packageName">
                <template #label>
                  <LabelHelp :label="t('system.codegen.packageName')" :help="t('system.codegen.help.packageName')" />
                </template>
                <a-input v-model:value="formData.packageName" :placeholder="t('system.codegen.form.packageName')" />
              </a-form-item>
            </a-col>
            <a-col :span="8">
              <a-form-item name="menuName">
                <template #label>
                  <LabelHelp :label="t('system.codegen.menuName')" :help="t('system.codegen.help.menuName')" />
                </template>
                <a-input v-model:value="formData.menuName" :placeholder="t('system.codegen.form.menuName')" />
              </a-form-item>
            </a-col>
            <a-col :span="8">
              <a-form-item name="menuIcon">
                <template #label>
                  <LabelHelp :label="t('system.codegen.menuIcon')" :help="t('system.codegen.help.menuIcon')" />
                </template>
                <a-input v-model:value="formData.menuIcon" :placeholder="t('system.codegen.form.menuIcon')" />
              </a-form-item>
            </a-col>
          </a-row>

          <a-row :gutter="16">
            <a-col :span="8">
              <a-form-item name="parentMenuPath">
                <template #label>
                  <LabelHelp :label="t('system.codegen.parentMenuPath')" :help="t('system.codegen.help.parentMenuPath')" />
                </template>
                <a-input v-model:value="formData.parentMenuPath" :placeholder="t('system.codegen.form.parentMenuPath')" />
              </a-form-item>
            </a-col>
            <a-col :span="8">
              <a-form-item name="tableCodePrefix">
                <template #label>
                  <LabelHelp :label="t('system.codegen.tableCodePrefix')" :help="t('system.codegen.help.tableCodePrefix')" />
                </template>
                <a-input v-model:value="formData.tableCodePrefix" :placeholder="t('system.codegen.form.tableCodePrefix')" />
              </a-form-item>
            </a-col>
            <a-col :span="8">
              <a-form-item name="androidFeatureKey">
                <template #label>
                  <LabelHelp :label="t('system.codegen.androidFeatureKey')" :help="t('system.codegen.help.androidFeatureKey')" />
                </template>
                <a-input v-model:value="formData.androidFeatureKey" :placeholder="t('system.codegen.form.androidFeatureKey')" />
              </a-form-item>
            </a-col>
          </a-row>
        </a-form>
      </div>

      <div v-show="currentStep === 1" class="codegen-drawer__step">
        <a-form ref="detailFormRef" :model="formData" :rules="detailRules" layout="vertical">
          <a-row :gutter="16">
            <a-col :span="6">
              <a-form-item :label="t('system.codegen.mainPkColumn')" name="mainPkColumn">
                <a-select
                  v-model:value="formData.mainPkColumn"
                  :options="mainColumnOptions"
                  :placeholder="t('system.codegen.form.mainPkColumn')"
                />
              </a-form-item>
            </a-col>
            <a-col v-if="showSubTable" :span="6">
              <a-form-item :label="t('system.codegen.subFkColumn')" name="subFkColumn">
                <a-select
                  v-model:value="formData.subFkColumn"
                  :options="subColumnOptions"
                  :placeholder="t('system.codegen.form.subFkColumn')"
                />
              </a-form-item>
            </a-col>
            <a-col v-if="showSubTable" :span="6">
              <a-form-item :label="t('system.codegen.subPkColumn')" name="subPkColumn">
                <a-select
                  v-model:value="formData.subPkColumn"
                  :options="subColumnOptions"
                  :placeholder="t('system.codegen.form.subPkColumn')"
                />
              </a-form-item>
            </a-col>
            <a-col v-if="showTreeTable" :span="6">
              <a-form-item :label="t('system.codegen.treePkColumn')" name="treePkColumn">
                <a-select
                  v-model:value="formData.treePkColumn"
                  :options="treeColumnOptions"
                  :placeholder="t('system.codegen.form.treePkColumn')"
                />
              </a-form-item>
            </a-col>
            <a-col v-if="showTreeTable" :span="6">
              <a-form-item :label="t('system.codegen.treeParentColumn')" name="treeParentColumn">
                <a-select
                  v-model:value="formData.treeParentColumn"
                  :options="treeColumnOptions"
                  :placeholder="t('system.codegen.form.treeParentColumn')"
                />
              </a-form-item>
            </a-col>
            <a-col v-if="showTreeTable" :span="6">
              <a-form-item :label="t('system.codegen.treeLabelColumn')" name="treeLabelColumn">
                <a-select
                  v-model:value="formData.treeLabelColumn"
                  :options="treeColumnOptions"
                  :placeholder="t('system.codegen.form.treeLabelColumn')"
                />
              </a-form-item>
            </a-col>
            <a-col v-if="showTreeTable" :span="6">
              <a-form-item :label="t('system.codegen.treeSortColumn')" name="treeSortColumn">
                <a-select
                  v-model:value="formData.treeSortColumn"
                  :options="treeColumnOptions"
                  :placeholder="t('system.codegen.form.treeSortColumn')"
                />
              </a-form-item>
            </a-col>
            <a-col v-if="formData.pageType === 'TREE_DOUBLE'" :span="6">
              <a-form-item :label="t('system.codegen.treeFilterColumn')" name="treeFilterColumn">
                <a-select
                  v-model:value="formData.treeFilterColumn"
                  :options="mainColumnOptions"
                  :placeholder="t('system.codegen.form.treeFilterColumn')"
                />
              </a-form-item>
            </a-col>
          </a-row>

          <a-row :gutter="16">
            <a-col :span="12">
              <a-form-item :label="t('system.codegen.generateItems')" name="generateItems">
                <a-checkbox-group v-model:value="formData.generateItems" :options="generateItemOptions" />
              </a-form-item>
            </a-col>
          </a-row>
        </a-form>

        <a-card size="small" :title="t('system.codegen.mainColumnsTitle')" class="codegen-drawer__subcard">
          <a-table
            :columns="columnEditorColumns"
            :data-source="formData.mainColumns"
            :pagination="false"
            size="small"
            row-key="columnName"
            bordered
            :scroll="{ x: 3400 }"
          >
            <template #bodyCell="{ column, record }">
              <ColumnEditorCell :column="column" :record="record" :query-type-options="queryTypeOptions" :query-operator-options="queryOperatorOptions" :form-type-options="formTypeOptions" :option-source-type-options="optionSourceTypeOptions" :sort-direction-options="sortDirectionOptions" :dict-tree-options="dictTreeOptions" />
            </template>
          </a-table>
        </a-card>

        <a-card
          v-if="showTreeTable"
          size="small"
          :title="t('system.codegen.treeColumnsTitle')"
          class="codegen-drawer__subcard"
        >
          <a-table
            :columns="columnEditorColumns"
            :data-source="formData.treeColumns"
            :pagination="false"
            size="small"
            row-key="columnName"
            bordered
            :scroll="{ x: 3400 }"
          >
            <template #bodyCell="{ column, record }">
              <ColumnEditorCell :column="column" :record="record" :query-type-options="queryTypeOptions" :query-operator-options="queryOperatorOptions" :form-type-options="formTypeOptions" :option-source-type-options="optionSourceTypeOptions" :sort-direction-options="sortDirectionOptions" :dict-tree-options="dictTreeOptions" />
            </template>
          </a-table>
        </a-card>

        <a-card
          v-if="showSubTable"
          size="small"
          :title="t('system.codegen.subColumnsTitle')"
          class="codegen-drawer__subcard"
        >
          <a-table
            :columns="columnEditorColumns"
            :data-source="formData.subColumns"
            :pagination="false"
            size="small"
            row-key="columnName"
            bordered
            :scroll="{ x: 3400 }"
          >
            <template #bodyCell="{ column, record }">
              <ColumnEditorCell :column="column" :record="record" :query-type-options="queryTypeOptions" :query-operator-options="queryOperatorOptions" :form-type-options="formTypeOptions" :option-source-type-options="optionSourceTypeOptions" :sort-direction-options="sortDirectionOptions" :dict-tree-options="dictTreeOptions" />
            </template>
          </a-table>
        </a-card>
      </div>

      <div v-show="currentStep === 2" class="codegen-drawer__step codegen-drawer__preview">
        <div class="codegen-drawer__preview-toolbar">
          <a-space>
            <a-button :loading="previewLoading" @click="handlePreview">
              {{ t('common.preview') }}
            </a-button>
            <a-button type="primary" :loading="downloadLoading" @click="handleDownload">
              {{ t('system.codegen.downloadZip') }}
            </a-button>
          </a-space>
        </div>

        <a-empty v-if="!previewResult.files.length" :description="t('system.codegen.previewEmpty')" />
        <div v-else class="codegen-drawer__preview-content">
          <div class="codegen-drawer__tree">
            <a-tree
              :tree-data="fileTreeData"
              :selected-keys="selectedKeys"
              block-node
              @select="handleFileSelect"
            />
          </div>
          <div class="codegen-drawer__viewer">
            <div class="codegen-drawer__viewer-header">
              <span>{{ activeFile?.path || previewResult.zipFileName }}</span>
            </div>
            <pre class="codegen-drawer__code">{{ activeFile?.content || '' }}</pre>
          </div>
        </div>
      </div>
    </div>

    <template #footer>
      <div class="codegen-drawer__footer">
        <a-space>
          <a-button @click="handleCancel">{{ t('common.cancel') }}</a-button>
          <a-button v-if="currentStep > 0" @click="handlePrev">{{ t('common.previous') }}</a-button>
          <a-button v-if="currentStep < 2" type="primary" @click="handleNext">{{ t('common.next') }}</a-button>
          <a-button v-if="currentStep === 2" @click="handlePreview">{{ t('common.refresh') }}</a-button>
          <a-button type="primary" :loading="saveLoading" @click="handleSave">
            {{ t('system.codegen.saveConfig') }}
          </a-button>
        </a-space>
      </div>
    </template>
  </a-drawer>
</template>

<script setup lang="ts">
import { computed, defineComponent, h, reactive, ref, watch } from 'vue'
import { QuestionCircleOutlined } from '@ant-design/icons-vue'
import { Input, Select, Switch, Tooltip, TreeSelect, message, type FormInstance, type Rule } from 'ant-design-vue'
import { useI18n } from 'vue-i18n'
import {
  downloadCodegenZip,
  listCodegenColumns,
  listCodegenTables,
  previewCodegen,
  type CodegenColumnConfig,
  type CodegenGenerateItem,
  type CodegenMetaOption,
  type CodegenPageType,
  type CodegenPreviewFile,
  type CodegenPreviewResult,
} from '@/api/system/codegen'
import { listCodegenDatasources, type CodegenDatasourceItem } from '@/api/system/codegenDatasource'
import { getDictTree, type DictTreeNode } from '@/api/system/dict'
import {
  getCodegenConfigDetail,
  saveCodegenConfig,
  type CodegenConfigItem,
  type CodegenConfigSaveParam,
} from '@/api/system/codegenConfig'
import type { CodegenOptionSourceType } from '@/api/system/codegen'

interface Props {
  open: boolean
  configId?: number
}

interface Emits {
  (e: 'update:open', value: boolean): void
  (e: 'success'): void
}

interface FileTreeNode {
  title: string
  key: string
  children?: FileTreeNode[]
}

interface SelectOption {
  label: string
  value: string
}

interface OptionSourceTypeOption {
  label: string
  value: CodegenOptionSourceType
}

interface TreeSelectOption {
  title: string
  value: string
  selectable?: boolean
  children?: TreeSelectOption[]
}

const LabelHelp = defineComponent({
  name: 'LabelHelp',
  props: {
    label: { type: String, required: true },
    help: { type: String, required: true },
  },
  setup(props) {
    return () =>
      h('span', { class: 'codegen-drawer__label-help' }, [
        h('span', props.label),
        h(
          Tooltip,
          { title: props.help },
          {
            default: () => h(QuestionCircleOutlined, { class: 'codegen-drawer__label-help-icon' }),
          },
        ),
      ])
  },
})

const ColumnEditorCell = defineComponent({
  name: 'ColumnEditorCell',
  props: {
    column: { type: Object, required: true },
    record: { type: Object as () => CodegenColumnConfig, required: true },
    queryTypeOptions: { type: Array as () => SelectOption[], required: true },
    queryOperatorOptions: { type: Array as () => SelectOption[], required: true },
    formTypeOptions: { type: Array as () => SelectOption[], required: true },
    optionSourceTypeOptions: { type: Array as () => OptionSourceTypeOption[], required: true },
    sortDirectionOptions: { type: Array as () => SelectOption[], required: true },
    dictTreeOptions: { type: Array as () => TreeSelectOption[], required: true },
  },
  setup(props) {
    const selectProps = {
      class: 'codegen-drawer__cell-control',
      size: 'small' as const,
      dropdownMatchSelectWidth: 220,
    }
    return () => {
      const key = (props.column as any).key
      const record = props.record
      if (key === 'columnName') return h('span', { class: 'codegen-drawer__column-name', title: record.columnName }, record.columnName)
      if (key === 'columnComment') return h(Input, { value: record.columnComment, size: 'small', class: 'codegen-drawer__cell-control', 'onUpdate:value': (value: string) => (record.columnComment = value) })
      if (key === 'queryable') return h(Switch, { checked: record.queryable, size: 'small', 'onUpdate:checked': (value: boolean) => (record.queryable = value) })
      if (key === 'tableShow') return h(Switch, { checked: record.tableShow, size: 'small', 'onUpdate:checked': (value: boolean) => (record.tableShow = value) })
      if (key === 'formShow') return h(Switch, { checked: record.formShow, size: 'small', 'onUpdate:checked': (value: boolean) => (record.formShow = value) })
      if (key === 'required') return h(Switch, { checked: record.required, size: 'small', 'onUpdate:checked': (value: boolean) => (record.required = value) })
      if (key === 'queryType') return h(Select, { ...selectProps, value: record.queryType, options: props.queryTypeOptions, 'onUpdate:value': (value: string) => (record.queryType = value) })
      if (key === 'queryOperator') return h(Select, { ...selectProps, value: record.queryOperator, options: props.queryOperatorOptions, 'onUpdate:value': (value: string) => (record.queryOperator = value) })
      if (key === 'formType') return h(Select, { ...selectProps, value: record.formType, options: props.formTypeOptions, 'onUpdate:value': (value: string) => (record.formType = value) })
      if (key === 'optionSourceType') return h(Select, {
        ...selectProps,
        value: record.optionSourceType,
        options: props.optionSourceTypeOptions,
        allowClear: true,
        'onUpdate:value': (value: CodegenOptionSourceType | undefined) => {
          record.optionSourceType = value
          if (value === 'DICT') {
            record.queryType = 'select'
            record.formType = 'select'
            record.queryOperator = 'eq'
          }
          if (value === 'API') {
            record.queryType = 'select'
            record.formType = record.formType === 'treeSelect' ? 'treeSelect' : 'select'
            record.queryOperator = 'eq'
            record.optionApiMethod = record.optionApiMethod || 'GET'
            record.optionParamsJson = record.optionParamsJson || '{}'
            record.optionResponsePath = record.optionResponsePath || 'data'
            record.optionLabelField = record.optionLabelField || 'label'
            record.optionValueField = record.optionValueField || 'value'
            record.optionChildrenField = record.optionChildrenField || 'children'
          }
        },
      })
      if (key === 'optionApiMethod') return h(Select, {
        ...selectProps,
        value: record.optionApiMethod || 'GET',
        options: [
          { label: 'GET', value: 'GET' },
          { label: 'POST', value: 'POST' },
        ],
        'onUpdate:value': (value: string) => (record.optionApiMethod = value),
      })
      if ([
        'optionApiUrl',
        'optionParamsJson',
        'optionResponsePath',
        'optionLabelField',
        'optionValueField',
        'optionChildrenField',
      ].includes(key)) return h(Input, {
        value: (record as any)[key],
        size: 'small',
        class: 'codegen-drawer__cell-control',
        'onUpdate:value': (value: string) => {
          ;(record as any)[key] = value
          if (key === 'optionApiUrl' && value) {
            record.optionSourceType = 'API'
            record.queryType = 'select'
            record.formType = record.formType === 'treeSelect' ? 'treeSelect' : 'select'
            record.queryOperator = 'eq'
            record.optionApiMethod = record.optionApiMethod || 'GET'
            record.optionParamsJson = record.optionParamsJson || '{}'
            record.optionResponsePath = record.optionResponsePath || 'data'
            record.optionLabelField = record.optionLabelField || 'label'
            record.optionValueField = record.optionValueField || 'value'
            record.optionChildrenField = record.optionChildrenField || 'children'
          }
        },
      })
      if (key === 'dictCode') return h(TreeSelect, {
        value: record.dictCode || undefined,
        class: 'codegen-drawer__cell-control',
        size: 'small',
        allowClear: true,
        showSearch: true,
        treeDefaultExpandAll: false,
        treeData: props.dictTreeOptions,
        dropdownMatchSelectWidth: 280,
        treeNodeFilterProp: 'title',
        'onUpdate:value': (value?: string) => {
          record.dictCode = value || ''
          if (value) {
            record.optionSourceType = 'DICT'
            record.queryType = 'select'
            record.formType = 'select'
            record.queryOperator = 'eq'
          }
        },
      })
      if (key === 'defaultSort') return h(Switch, { checked: record.defaultSort, size: 'small', 'onUpdate:checked': (value: boolean) => (record.defaultSort = value) })
      if (key === 'sortDirection') return h(Select, { ...selectProps, value: record.sortDirection, options: props.sortDirectionOptions, 'onUpdate:value': (value: string) => (record.sortDirection = value) })
      return h('span', (record as any)[key] ?? '')
    }
  },
})

const props = defineProps<Props>()
const emit = defineEmits<Emits>()
const { t } = useI18n({ useScope: 'global' })

const basicFormRef = ref<FormInstance>()
const detailFormRef = ref<FormInstance>()
const currentStep = ref(0)
const previewLoading = ref(false)
const downloadLoading = ref(false)
const saveLoading = ref(false)
const datasourceList = ref<CodegenDatasourceItem[]>([])
const tableOptions = ref<CodegenMetaOption[]>([])
const dictTreeOptions = ref<TreeSelectOption[]>([])
const selectedFilePath = ref('')

const previewResult = reactive<CodegenPreviewResult>({
  zipFileName: '',
  files: [],
})

const defaultGenerateItems: CodegenGenerateItem[] = ['backend', 'frontend', 'sql']

const formData = reactive<CodegenConfigSaveParam>({
  id: undefined,
  configName: '',
  datasourceId: 0,
  schemaName: '',
  pageType: 'SINGLE',
  mainTableName: '',
  treeTableName: '',
  subTableName: '',
  mainPkColumn: '',
  treePkColumn: '',
  treeParentColumn: '',
  treeLabelColumn: '',
  treeSortColumn: '',
  treeFilterColumn: '',
  subFkColumn: '',
  subPkColumn: '',
  moduleName: 'system',
  bizName: '',
  entityName: '',
  treeEntityName: '',
  subEntityName: '',
  packageName: 'com.forgex.system',
  author: 'Forgex CodeGen',
  menuName: '',
  menuIcon: 'TableOutlined',
  parentMenuPath: '/system',
  tableCodePrefix: 'system',
  androidFeatureKey: '',
  generateItems: [...defaultGenerateItems],
  mainColumns: [],
  treeColumns: [],
  subColumns: [],
  remark: '',
})

const drawerTitle = computed(() => (formData.id ? t('system.codegen.edit') : t('system.codegen.add')))
const showSubTable = computed(() => formData.pageType === 'MASTER_DETAIL')
const showTreeTable = computed(() => formData.pageType === 'TREE_SINGLE' || formData.pageType === 'TREE_DOUBLE')
const treeSingle = computed(() => formData.pageType === 'TREE_SINGLE')
const treeDouble = computed(() => formData.pageType === 'TREE_DOUBLE')

const pageTypeTip = computed(() => {
  switch (formData.pageType) {
    case 'MASTER_DETAIL':
      return t('system.codegen.pageTypeMasterDetailTip')
    case 'TREE_SINGLE':
      return t('system.codegen.pageTypeTreeSingleTip')
    case 'TREE_DOUBLE':
      return t('system.codegen.pageTypeTreeDoubleTip')
    default:
      return t('system.codegen.pageTypeSingleTip')
  }
})

const datasourceOptions = computed(() =>
  datasourceList.value.map((item) => ({
    label: `${item.datasourceName}(${item.datasourceCode})`,
    value: item.id!,
  })),
)

const subTableOptions = computed(() =>
  tableOptions.value.filter((item) => item.value !== formData.mainTableName && item.value !== formData.treeTableName),
)

const treeTableOptions = computed(() => {
  if (formData.pageType === 'TREE_SINGLE') {
    return tableOptions.value.filter((item) => item.value === formData.mainTableName)
  }
  return tableOptions.value.filter((item) => item.value !== formData.mainTableName)
})

const mainColumnOptions = computed(() => toColumnOptions(formData.mainColumns))
const treeColumnOptions = computed(() => toColumnOptions(formData.treeColumns))
const subColumnOptions = computed(() => toColumnOptions(formData.subColumns))

const generateItemOptions = computed(() => [
  { label: 'Backend', value: 'backend' },
  { label: 'Frontend', value: 'frontend' },
  { label: 'SQL', value: 'sql' },
  { label: 'Android', value: 'android', disabled: formData.pageType === 'MASTER_DETAIL' },
])

const queryTypeOptions = [
  { label: t('system.codegen.option.queryTypeInput'), value: 'input' },
  { label: t('system.codegen.option.queryTypeSelect'), value: 'select' },
  { label: t('system.codegen.option.queryTypeDate'), value: 'date' },
]

const queryOperatorOptions = [
  { label: t('system.codegen.option.operatorLike'), value: 'like' },
  { label: t('system.codegen.option.operatorEq'), value: 'eq' },
  { label: t('system.codegen.option.operatorGt'), value: 'gt' },
  { label: t('system.codegen.option.operatorLt'), value: 'lt' },
]

const optionSourceTypeOptions = [
  { label: t('system.codegen.option.optionSourceDict'), value: 'DICT' },
  { label: t('system.codegen.option.optionSourceApi'), value: 'API' },
]

const formTypeOptions = [
  { label: t('system.codegen.option.formInput'), value: 'input' },
  { label: t('system.codegen.option.formTextarea'), value: 'textarea' },
  { label: t('system.codegen.option.formNumber'), value: 'number' },
  { label: t('system.codegen.option.formSelect'), value: 'select' },
  { label: t('system.codegen.option.formTreeSelect'), value: 'treeSelect' },
  { label: t('system.codegen.option.formDate'), value: 'date' },
  { label: t('system.codegen.option.formDateTime'), value: 'datetime' },
]

const sortDirectionOptions = [
  { label: 'ASC', value: 'ASC' },
  { label: 'DESC', value: 'DESC' },
]

const basicRules: Record<string, Rule[]> = {
  configName: [{ required: true, message: t('system.codegen.form.configName'), trigger: 'blur' }],
  datasourceId: [{ required: true, message: t('system.codegen.form.datasource'), trigger: 'change' }],
  mainTableName: [{ required: true, message: t('system.codegen.form.mainTableName'), trigger: 'change' }],
  treeTableName: [{ required: true, message: t('system.codegen.form.treeTableName'), trigger: 'change' }],
  subTableName: [{ required: true, message: t('system.codegen.form.subTableName'), trigger: 'change' }],
  moduleName: [{ required: true, message: t('system.codegen.form.moduleName'), trigger: 'blur' }],
  bizName: [{ required: true, message: t('system.codegen.form.bizName'), trigger: 'blur' }],
  entityName: [{ required: true, message: t('system.codegen.form.entityName'), trigger: 'blur' }],
  packageName: [{ required: true, message: t('system.codegen.form.packageName'), trigger: 'blur' }],
  author: [{ required: true, message: t('system.codegen.form.author'), trigger: 'blur' }],
  menuName: [{ required: true, message: t('system.codegen.form.menuName'), trigger: 'blur' }],
}

const detailRules: Record<string, Rule[]> = {
  mainPkColumn: [{ required: true, message: t('system.codegen.form.mainPkColumn'), trigger: 'change' }],
  subFkColumn: [{ required: true, message: t('system.codegen.form.subFkColumn'), trigger: 'change' }],
  subPkColumn: [{ required: true, message: t('system.codegen.form.subPkColumn'), trigger: 'change' }],
  treePkColumn: [{ required: true, message: t('system.codegen.form.treePkColumn'), trigger: 'change' }],
  treeParentColumn: [{ required: true, message: t('system.codegen.form.treeParentColumn'), trigger: 'change' }],
  treeLabelColumn: [{ required: true, message: t('system.codegen.form.treeLabelColumn'), trigger: 'change' }],
  treeSortColumn: [{ required: true, message: t('system.codegen.form.treeSortColumn'), trigger: 'change' }],
  treeFilterColumn: [{ required: true, message: t('system.codegen.form.treeFilterColumn'), trigger: 'change' }],
}

const columnEditorColumns = computed(() => [
  { title: t('system.codegen.columnName'), dataIndex: 'columnName', key: 'columnName', width: 190, fixed: 'left' },
  { title: t('system.codegen.columnComment'), dataIndex: 'columnComment', key: 'columnComment', width: 230 },
  { title: t('system.codegen.queryable'), dataIndex: 'queryable', key: 'queryable', width: 90, align: 'center' },
  { title: t('system.codegen.tableShow'), dataIndex: 'tableShow', key: 'tableShow', width: 90, align: 'center' },
  { title: t('system.codegen.formShow'), dataIndex: 'formShow', key: 'formShow', width: 90, align: 'center' },
  { title: t('system.codegen.required'), dataIndex: 'required', key: 'required', width: 90, align: 'center' },
  { title: t('system.codegen.queryType'), dataIndex: 'queryType', key: 'queryType', width: 150 },
  { title: t('system.codegen.queryOperator'), dataIndex: 'queryOperator', key: 'queryOperator', width: 150 },
  { title: t('system.codegen.formType'), dataIndex: 'formType', key: 'formType', width: 170 },
  { title: t('system.codegen.dictCode'), dataIndex: 'dictCode', key: 'dictCode', width: 240 },
  { title: t('system.codegen.optionSourceType'), dataIndex: 'optionSourceType', key: 'optionSourceType', width: 130 },
  { title: t('system.codegen.optionApiUrl'), dataIndex: 'optionApiUrl', key: 'optionApiUrl', width: 240 },
  { title: t('system.codegen.optionApiMethod'), dataIndex: 'optionApiMethod', key: 'optionApiMethod', width: 120 },
  { title: t('system.codegen.optionParamsJson'), dataIndex: 'optionParamsJson', key: 'optionParamsJson', width: 240 },
  { title: t('system.codegen.optionResponsePath'), dataIndex: 'optionResponsePath', key: 'optionResponsePath', width: 150 },
  { title: t('system.codegen.optionLabelField'), dataIndex: 'optionLabelField', key: 'optionLabelField', width: 120 },
  { title: t('system.codegen.optionValueField'), dataIndex: 'optionValueField', key: 'optionValueField', width: 120 },
  { title: t('system.codegen.optionChildrenField'), dataIndex: 'optionChildrenField', key: 'optionChildrenField', width: 140 },
  { title: t('system.codegen.defaultSort'), dataIndex: 'defaultSort', key: 'defaultSort', width: 100, align: 'center' },
  { title: t('system.codegen.sortDirection'), dataIndex: 'sortDirection', key: 'sortDirection', width: 120 },
])

const activeFile = computed<CodegenPreviewFile | undefined>(() => previewResult.files.find((item) => item.path === selectedFilePath.value))
const selectedKeys = computed(() => (selectedFilePath.value ? [selectedFilePath.value] : []))
const fileTreeData = computed<FileTreeNode[]>(() => buildFileTree(previewResult.files))

function resetForm() {
  formData.id = undefined
  formData.configId = undefined
  formData.configName = ''
  formData.datasourceId = 0
  formData.datasourceCode = undefined
  formData.schemaName = ''
  formData.pageType = 'SINGLE'
  formData.mainTableName = ''
  formData.treeTableName = ''
  formData.subTableName = ''
  formData.mainPkColumn = ''
  formData.treePkColumn = ''
  formData.treeParentColumn = ''
  formData.treeLabelColumn = ''
  formData.treeSortColumn = ''
  formData.treeFilterColumn = ''
  formData.subFkColumn = ''
  formData.subPkColumn = ''
  formData.moduleName = 'system'
  formData.bizName = ''
  formData.entityName = ''
  formData.treeEntityName = ''
  formData.subEntityName = ''
  formData.packageName = 'com.forgex.system'
  formData.author = 'Forgex CodeGen'
  formData.menuName = ''
  formData.menuIcon = 'TableOutlined'
  formData.parentMenuPath = '/system'
  formData.tableCodePrefix = 'system'
  formData.androidFeatureKey = ''
  formData.generateItems = [...defaultGenerateItems]
  formData.mainColumns = []
  formData.treeColumns = []
  formData.subColumns = []
  formData.remark = ''
  tableOptions.value = []
  currentStep.value = 0
  previewResult.zipFileName = ''
  previewResult.files = []
  selectedFilePath.value = ''
}

async function loadDatasources() {
  datasourceList.value = await listCodegenDatasources()
}

async function loadDictTreeOptions() {
  const tree = await getDictTree() as DictTreeNode[]
  dictTreeOptions.value = buildDictTreeOptions(tree || [])
}

function getSelectedDatasource() {
  return datasourceList.value.find((item) => item.id === formData.datasourceId)
}

async function loadTables() {
  if (!formData.datasourceId || !formData.schemaName) {
    tableOptions.value = []
    return
  }
  tableOptions.value = await listCodegenTables(formData.datasourceId, formData.schemaName)
}

async function loadDetail(id: number) {
  const detail = await getCodegenConfigDetail(id)
  fillForm(detail)
  formData.schemaName = getSelectedDatasource()?.schemaName || formData.schemaName
  await loadTables()
}

function fillForm(detail: CodegenConfigItem) {
  resetForm()
  formData.id = detail.id
  formData.configId = detail.configId
  formData.configName = detail.configName
  formData.datasourceId = detail.datasourceId
  formData.datasourceCode = detail.datasourceCode
  formData.schemaName = detail.schemaName
  formData.pageType = detail.pageType
  formData.mainTableName = detail.mainTableName
  formData.treeTableName = detail.treeTableName || ''
  formData.subTableName = detail.subTableName || ''
  formData.mainPkColumn = detail.mainPkColumn || ''
  formData.treePkColumn = detail.treePkColumn || ''
  formData.treeParentColumn = detail.treeParentColumn || ''
  formData.treeLabelColumn = detail.treeLabelColumn || ''
  formData.treeSortColumn = detail.treeSortColumn || ''
  formData.treeFilterColumn = detail.treeFilterColumn || ''
  formData.subFkColumn = detail.subFkColumn || ''
  formData.subPkColumn = detail.subPkColumn || ''
  formData.moduleName = detail.moduleName
  formData.bizName = detail.bizName
  formData.entityName = detail.entityName
  formData.treeEntityName = detail.treeEntityName || ''
  formData.subEntityName = detail.subEntityName || ''
  formData.packageName = detail.packageName
  formData.author = detail.author
  formData.menuName = detail.menuName
  formData.menuIcon = detail.menuIcon || 'TableOutlined'
  formData.parentMenuPath = detail.parentMenuPath || '/system'
  formData.tableCodePrefix = detail.tableCodePrefix || 'system'
  formData.androidFeatureKey = detail.androidFeatureKey || ''
  formData.generateItems = detail.generateItems?.length ? detail.generateItems : [...defaultGenerateItems]
  formData.mainColumns = enhanceColumns(detail.mainColumns || [])
  formData.treeColumns = enhanceColumns(detail.treeColumns || [])
  formData.subColumns = enhanceColumns(detail.subColumns || [])
  formData.remark = detail.remark || ''
}

async function handleDatasourceChange() {
  formData.schemaName = getSelectedDatasource()?.schemaName || ''
  formData.mainTableName = ''
  formData.treeTableName = ''
  formData.subTableName = ''
  formData.mainColumns = []
  formData.treeColumns = []
  formData.subColumns = []
  await loadTables()
}

function handlePageTypeChange() {
  resetPageTypeRelatedValues()
  if (formData.pageType === 'TREE_SINGLE' && formData.mainTableName) {
    formData.treeTableName = formData.mainTableName
    formData.treeEntityName = formData.entityName
    formData.treeColumns = enhanceColumns(formData.mainColumns || [])
    inferTreeFieldsFromColumns(formData.treeColumns, 'tree')
  }
  if (formData.pageType === 'MASTER_DETAIL') {
    formData.generateItems = formData.generateItems.filter((item) => item !== 'android')
  }
}

async function handleMainTableChange() {
  if (!formData.datasourceId || !formData.schemaName || !formData.mainTableName) {
    return
  }
  const columns = await listCodegenColumns(formData.datasourceId, formData.schemaName, formData.mainTableName)
  formData.mainColumns = enhanceColumns(columns)
  formData.mainPkColumn = detectPrimaryKey(formData.mainColumns) || formData.mainPkColumn || 'id'
  if (!formData.bizName) {
    formData.bizName = toLowerCamel(toPascalCase(formData.mainTableName))
  }
  if (!formData.entityName) {
    formData.entityName = toPascalCase(formData.mainTableName)
  }
  if (!formData.menuName) {
    formData.menuName = formData.mainTableName
  }
  if (!formData.configName) {
    formData.configName = formData.menuName
  }
  if (!formData.androidFeatureKey) {
    formData.androidFeatureKey = formData.bizName || toLowerCamel(toPascalCase(formData.mainTableName))
  }
  if (treeSingle.value && !formData.treeTableName) {
    formData.treeTableName = formData.mainTableName
    formData.treeEntityName = formData.entityName
    formData.treeColumns = [...formData.mainColumns]
    inferTreeFieldsFromColumns(formData.mainColumns, 'main')
  }
}

async function handleTreeTableChange() {
  if (!formData.datasourceId || !formData.schemaName || !formData.treeTableName) {
    formData.treeColumns = []
    return
  }
  if (treeSingle.value && formData.treeTableName === formData.mainTableName) {
    formData.treeColumns = enhanceColumns(formData.mainColumns || [])
  } else {
    const columns = await listCodegenColumns(formData.datasourceId, formData.schemaName, formData.treeTableName)
    formData.treeColumns = enhanceColumns(columns)
  }
  if (!formData.treeEntityName) {
    formData.treeEntityName = toPascalCase(formData.treeTableName)
  }
  inferTreeFieldsFromColumns(formData.treeColumns, 'tree')
}

async function handleSubTableChange() {
  if (!formData.datasourceId || !formData.schemaName || !formData.subTableName) {
    formData.subColumns = []
    return
  }
  const columns = await listCodegenColumns(formData.datasourceId, formData.schemaName, formData.subTableName)
  formData.subColumns = enhanceColumns(columns)
  formData.subPkColumn = detectPrimaryKey(formData.subColumns) || formData.subPkColumn || 'id'
  if (!formData.subEntityName) {
    formData.subEntityName = toPascalCase(formData.subTableName)
  }
}

async function handleNext() {
  if (currentStep.value === 0) {
    await basicFormRef.value?.validate()
    validateDatasourceSchema()
    validatePageTypeCombination()
  }
  if (currentStep.value === 1) {
    await detailFormRef.value?.validate()
    validatePageTypeCombination()
  }
  if (currentStep.value < 2) {
    currentStep.value += 1
  }
  if (currentStep.value === 2 && previewResult.files.length === 0) {
    await handlePreview()
  }
}

function handlePrev() {
  currentStep.value = Math.max(0, currentStep.value - 1)
}

async function handlePreview() {
  validateDatasourceSchema()
  validatePageTypeCombination()
  previewLoading.value = true
  try {
    const result = await previewCodegen(buildRequestPayload())
    previewResult.zipFileName = result.zipFileName
    previewResult.files = result.files || []
    selectedFilePath.value = previewResult.files[0]?.path || ''
    message.success(t('system.codegen.generateSuccess'))
  } finally {
    previewLoading.value = false
  }
}

async function handleDownload() {
  validateDatasourceSchema()
  validatePageTypeCombination()
  downloadLoading.value = true
  try {
    const { blob, fileName } = await downloadCodegenZip(buildRequestPayload())
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = fileName
    link.click()
    URL.revokeObjectURL(url)
  } finally {
    downloadLoading.value = false
  }
}

function handleFileSelect(keys: string[]) {
  selectedFilePath.value = keys[0] || ''
}

async function handleSave() {
  await basicFormRef.value?.validate()
  await detailFormRef.value?.validate()
  validateDatasourceSchema()
  validatePageTypeCombination()
  saveLoading.value = true
  try {
    await saveCodegenConfig({
      ...buildRequestPayload(),
      id: formData.id,
      configName: formData.configName,
      remark: formData.remark,
    })
    emit('success')
    emit('update:open', false)
  } finally {
    saveLoading.value = false
  }
}

function handleCancel() {
  emit('update:open', false)
}

function buildRequestPayload(): CodegenConfigSaveParam {
  return {
    ...formData,
    pageType: formData.pageType as CodegenPageType,
    generateItems: [...new Set((formData.generateItems || []).filter(Boolean))] as CodegenGenerateItem[],
    mainColumns: formData.mainColumns.map(normalizeColumnPayload),
    treeColumns: (formData.treeColumns || []).map(normalizeColumnPayload),
    subColumns: (formData.subColumns || []).map(normalizeColumnPayload),
  }
}

function enhanceColumns(columns: CodegenColumnConfig[]) {
  return (columns || []).map((item) => ({
    ...item,
    queryable: item.queryable ?? (!item.isPrimaryKey && !item.isBaseField),
    tableShow: item.tableShow ?? true,
    formShow: item.formShow ?? (!item.isPrimaryKey && !item.isAutoIncrement),
    required: item.required ?? !item.isNullable,
    optionSourceType: item.optionSourceType || (item.optionApiUrl ? 'API' : item.dictCode ? 'DICT' : undefined),
    optionApiMethod: item.optionApiMethod || (item.optionApiUrl ? 'GET' : undefined),
    optionParamsJson: item.optionParamsJson || (item.optionApiUrl ? '{}' : undefined),
    optionResponsePath: item.optionResponsePath || (item.optionApiUrl ? 'data' : undefined),
    optionLabelField: item.optionLabelField || (item.optionApiUrl ? 'label' : undefined),
    optionValueField: item.optionValueField || (item.optionApiUrl ? 'value' : undefined),
    optionChildrenField: item.optionChildrenField || (item.optionApiUrl ? 'children' : undefined),
    queryType: item.queryType || inferQueryType(item),
    queryOperator: item.queryOperator || inferQueryOperator(item),
    formType: item.formType || inferFormType(item),
    defaultSort: item.defaultSort ?? false,
    sortDirection: item.sortDirection || 'DESC',
  }))
}

function normalizeColumnPayload(column: CodegenColumnConfig) {
  return {
    ...column,
    queryable: !!column.queryable,
    tableShow: !!column.tableShow,
    formShow: !!column.formShow,
    required: !!column.required,
    defaultSort: !!column.defaultSort,
  }
}

function buildFileTree(files: CodegenPreviewFile[]) {
  const rootMap = new Map<string, any>()
  files.forEach((file) => {
    const parts = file.path.split('/').filter(Boolean)
    let currentMap = rootMap
    let currentPath = ''
    parts.forEach((part, index) => {
      currentPath = currentPath ? `${currentPath}/${part}` : part
      if (!currentMap.has(part)) {
        currentMap.set(part, {
          title: part,
          key: index === parts.length - 1 ? file.path : currentPath,
          children: new Map<string, any>(),
        })
      }
      const node = currentMap.get(part)
      currentMap = node.children
    })
  })
  return mapToTree(rootMap)
}

function mapToTree(source: Map<string, any>): FileTreeNode[] {
  return Array.from(source.values()).map((item) => ({
    title: item.title,
    key: item.key,
    children: item.children.size > 0 ? mapToTree(item.children) : undefined,
  }))
}

function buildDictTreeOptions(nodes: DictTreeNode[]): TreeSelectOption[] {
  return (nodes || []).map((node) => {
    const value = node.dictCode || `__dict_node_${node.id}`
    return {
      title: node.dictCode ? `${node.dictName}(${node.dictCode})` : node.dictName,
      value,
      selectable: !!node.dictCode,
      children: buildDictTreeOptions(node.children || []).map((child) => ({
        ...child,
        selectable: false,
      })),
    }
  })
}

function toColumnOptions(columns: CodegenColumnConfig[]) {
  return (columns || []).map((item) => ({
    label: `${item.columnName}${item.columnComment ? `(${item.columnComment})` : ''}`,
    value: item.columnName,
  }))
}

function inferQueryType(column: CodegenColumnConfig) {
  if (isOptionColumn(column)) {
    return 'select'
  }
  if (column.javaType === 'LocalDate' || column.javaType === 'LocalDateTime') {
    return 'date'
  }
  return 'input'
}

function inferQueryOperator(column: CodegenColumnConfig) {
  if (column.javaType === 'String' && !isOptionColumn(column)) {
    return 'like'
  }
  return 'eq'
}

function inferFormType(column: CodegenColumnConfig) {
  if (isOptionColumn(column)) {
    return 'select'
  }
  if (column.javaType === 'LocalDateTime') {
    return 'datetime'
  }
  if (column.javaType === 'LocalDate') {
    return 'date'
  }
  if (['Integer', 'Long', 'Float', 'Double', 'BigDecimal'].includes(column.javaType || '')) {
    return 'number'
  }
  return 'input'
}

function isOptionColumn(column: CodegenColumnConfig) {
  return !!(column.dictCode || column.optionSourceType || column.optionApiUrl)
}

function detectPrimaryKey(columns: CodegenColumnConfig[]) {
  return columns.find((item) => item.isPrimaryKey)?.columnName
}

function detectTreeParentColumn(columns: CodegenColumnConfig[]) {
  return findByCandidates(columns, ['parent_id', 'parentId', 'pid'])
}

function detectTreeLabelColumn(columns: CodegenColumnConfig[]) {
  return findByCandidates(columns, ['name', 'title', 'label', 'dept_name', 'menu_name']) || columns.find((item) => item.javaType === 'String')?.columnName || ''
}

function detectTreeSortColumn(columns: CodegenColumnConfig[]) {
  return findByCandidates(columns, ['order_num', 'sort_no', 'sort_order', 'sort_num', 'sort']) || ''
}

function detectTreeFilterColumn(mainColumns: CodegenColumnConfig[], treeColumns: CodegenColumnConfig[]) {
  const treePk = detectPrimaryKey(treeColumns)
  const treePkType = treeColumns.find((item) => item.columnName === treePk)?.javaType
  const candidate = mainColumns.find((item) => item.columnName.endsWith('_id') && item.javaType === treePkType)
  return candidate?.columnName || ''
}

function findByCandidates(columns: CodegenColumnConfig[], candidates: string[]) {
  return candidates.find((candidate) => columns.some((item) => item.columnName === candidate)) || ''
}

function inferTreeFieldsFromColumns(columns: CodegenColumnConfig[], source: 'main' | 'tree') {
  const treePk = detectPrimaryKey(columns) || 'id'
  const treeParent = detectTreeParentColumn(columns)
  const treeLabel = detectTreeLabelColumn(columns)
  const treeSort = detectTreeSortColumn(columns)
  formData.treePkColumn = formData.treePkColumn || treePk
  formData.treeParentColumn = formData.treeParentColumn || treeParent
  formData.treeLabelColumn = formData.treeLabelColumn || treeLabel
  formData.treeSortColumn = formData.treeSortColumn || treeSort
  if (treeDouble.value && !formData.treeFilterColumn) {
    formData.treeFilterColumn = detectTreeFilterColumn(formData.mainColumns, source === 'main' ? columns : formData.treeColumns)
  }
}

function resetPageTypeRelatedValues() {
  if (!showTreeTable.value) {
    formData.treeTableName = ''
    formData.treePkColumn = ''
    formData.treeParentColumn = ''
    formData.treeLabelColumn = ''
    formData.treeSortColumn = ''
    formData.treeFilterColumn = ''
    formData.treeEntityName = ''
    formData.treeColumns = []
  }
  if (!showSubTable.value) {
    formData.subTableName = ''
    formData.subFkColumn = ''
    formData.subPkColumn = ''
    formData.subEntityName = ''
    formData.subColumns = []
  }
}

function validatePageTypeCombination() {
  validateDatasourceSchema()
  if (showSubTable.value && !formData.subTableName) {
    message.warning(t('system.codegen.form.subTableName'))
    throw new Error('subTableName required')
  }
  if (showTreeTable.value && !formData.treeTableName) {
    message.warning(t('system.codegen.form.treeTableName'))
    throw new Error('treeTableName required')
  }
  if (treeSingle.value && formData.treeTableName !== formData.mainTableName) {
    message.warning(t('system.codegen.pageTypeTreeSingleValidate'))
    throw new Error('tree single invalid')
  }
  if (treeDouble.value) {
    const treePkType = formData.treeColumns.find((item) => item.columnName === formData.treePkColumn)?.javaType
    const filterType = formData.mainColumns.find((item) => item.columnName === formData.treeFilterColumn)?.javaType
    if (treePkType && filterType && treePkType !== filterType) {
      message.warning(t('system.codegen.pageTypeTreeDoubleValidate'))
      throw new Error('tree double invalid')
    }
  }
  if (formData.pageType === 'MASTER_DETAIL' && formData.generateItems.includes('android')) {
    message.warning(t('system.codegen.androidMasterDetailDisabled'))
    formData.generateItems = formData.generateItems.filter((item) => item !== 'android')
  }
}

function validateDatasourceSchema() {
  if (!formData.schemaName) {
    message.warning(t('system.codegen.form.datasourceSchemaRequired'))
    throw new Error('datasource schema required')
  }
}

function toPascalCase(value: string) {
  return String(value || '')
    .split('_')
    .filter(Boolean)
    .map((part) => part.charAt(0).toUpperCase() + part.slice(1).toLowerCase())
    .join('')
}

function toLowerCamel(value: string) {
  if (!value) return value
  return value.charAt(0).toLowerCase() + value.slice(1)
}

watch(
  () => props.open,
  async (visible) => {
    if (!visible) {
      resetForm()
      basicFormRef.value?.resetFields()
      detailFormRef.value?.resetFields()
      return
    }
    await loadDatasources()
    await loadDictTreeOptions()
    if (props.configId) {
      await loadDetail(props.configId)
    } else {
      resetForm()
    }
  },
  { immediate: true },
)
</script>

<style scoped lang="less" src="@/styles/views/system/codegen/components/codegen-config-drawer.less"></style>
