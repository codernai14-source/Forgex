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
            <a-col :span="8">
              <a-form-item :label="t('system.codegen.schemaName')" name="schemaName">
                <a-select
                  v-model:value="formData.schemaName"
                  :options="schemaOptions"
                  :placeholder="t('system.codegen.form.schemaName')"
                  @change="handleSchemaChange"
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
              <a-form-item :label="t('system.codegen.moduleName')" name="moduleName">
                <a-input v-model:value="formData.moduleName" :placeholder="t('system.codegen.form.moduleName')" />
              </a-form-item>
            </a-col>
            <a-col :span="8">
              <a-form-item :label="t('system.codegen.bizName')" name="bizName">
                <a-input v-model:value="formData.bizName" :placeholder="t('system.codegen.form.bizName')" />
              </a-form-item>
            </a-col>
            <a-col :span="8">
              <a-form-item :label="t('system.codegen.author')" name="author">
                <a-input v-model:value="formData.author" :placeholder="t('system.codegen.form.author')" />
              </a-form-item>
            </a-col>
          </a-row>

          <a-row :gutter="16">
            <a-col :span="8">
              <a-form-item :label="t('system.codegen.entityName')" name="entityName">
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
              <a-form-item :label="t('system.codegen.packageName')" name="packageName">
                <a-input v-model:value="formData.packageName" :placeholder="t('system.codegen.form.packageName')" />
              </a-form-item>
            </a-col>
          </a-row>

          <a-row :gutter="16">
            <a-col v-if="showTreeTable || showSubTable" :span="8">
              <a-form-item :label="t('system.codegen.packageName')" name="packageName">
                <a-input v-model:value="formData.packageName" :placeholder="t('system.codegen.form.packageName')" />
              </a-form-item>
            </a-col>
            <a-col :span="8">
              <a-form-item :label="t('system.codegen.menuName')" name="menuName">
                <a-input v-model:value="formData.menuName" :placeholder="t('system.codegen.form.menuName')" />
              </a-form-item>
            </a-col>
            <a-col :span="8">
              <a-form-item :label="t('system.codegen.menuIcon')" name="menuIcon">
                <a-input v-model:value="formData.menuIcon" :placeholder="t('system.codegen.form.menuIcon')" />
              </a-form-item>
            </a-col>
          </a-row>

          <a-row :gutter="16">
            <a-col :span="8">
              <a-form-item :label="t('system.codegen.parentMenuPath')" name="parentMenuPath">
                <a-input v-model:value="formData.parentMenuPath" :placeholder="t('system.codegen.form.parentMenuPath')" />
              </a-form-item>
            </a-col>
            <a-col :span="8">
              <a-form-item :label="t('system.codegen.tableCodePrefix')" name="tableCodePrefix">
                <a-input v-model:value="formData.tableCodePrefix" :placeholder="t('system.codegen.form.tableCodePrefix')" />
              </a-form-item>
            </a-col>
            <a-col :span="8">
              <a-form-item :label="t('system.codegen.androidFeatureKey')" name="androidFeatureKey">
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
            :scroll="{ x: 1350 }"
          >
            <template #bodyCell="{ column, record }">
              <ColumnEditorCell :column="column" :record="record" :query-type-options="queryTypeOptions" :query-operator-options="queryOperatorOptions" :form-type-options="formTypeOptions" :sort-direction-options="sortDirectionOptions" />
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
            :scroll="{ x: 1350 }"
          >
            <template #bodyCell="{ column, record }">
              <ColumnEditorCell :column="column" :record="record" :query-type-options="queryTypeOptions" :query-operator-options="queryOperatorOptions" :form-type-options="formTypeOptions" :sort-direction-options="sortDirectionOptions" />
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
            :scroll="{ x: 1350 }"
          >
            <template #bodyCell="{ column, record }">
              <ColumnEditorCell :column="column" :record="record" :query-type-options="queryTypeOptions" :query-operator-options="queryOperatorOptions" :form-type-options="formTypeOptions" :sort-direction-options="sortDirectionOptions" />
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
import { Input, InputNumber, Select, Switch, message, type FormInstance, type Rule } from 'ant-design-vue'
import { useI18n } from 'vue-i18n'
import {
  downloadCodegenZip,
  listCodegenColumns,
  listCodegenSchemas,
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
import {
  getCodegenConfigDetail,
  saveCodegenConfig,
  type CodegenConfigItem,
  type CodegenConfigSaveParam,
} from '@/api/system/codegenConfig'

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

const ColumnEditorCell = defineComponent({
  name: 'ColumnEditorCell',
  props: {
    column: { type: Object, required: true },
    record: { type: Object as () => CodegenColumnConfig, required: true },
    queryTypeOptions: { type: Array as () => Array<{ label: string; value: string }>, required: true },
    queryOperatorOptions: { type: Array as () => Array<{ label: string; value: string }>, required: true },
    formTypeOptions: { type: Array as () => Array<{ label: string; value: string }>, required: true },
    sortDirectionOptions: { type: Array as () => Array<{ label: string; value: string }>, required: true },
  },
  setup(props) {
    return () => {
      const key = (props.column as any).key
      const record = props.record
      if (key === 'columnComment') return h(Input, { value: record.columnComment, size: 'small', 'onUpdate:value': (value: string) => (record.columnComment = value) })
      if (key === 'queryable') return h(Switch, { checked: record.queryable, size: 'small', 'onUpdate:checked': (value: boolean) => (record.queryable = value) })
      if (key === 'tableShow') return h(Switch, { checked: record.tableShow, size: 'small', 'onUpdate:checked': (value: boolean) => (record.tableShow = value) })
      if (key === 'formShow') return h(Switch, { checked: record.formShow, size: 'small', 'onUpdate:checked': (value: boolean) => (record.formShow = value) })
      if (key === 'required') return h(Switch, { checked: record.required, size: 'small', 'onUpdate:checked': (value: boolean) => (record.required = value) })
      if (key === 'queryType') return h(Select, { value: record.queryType, size: 'small', options: props.queryTypeOptions, 'onUpdate:value': (value: string) => (record.queryType = value) })
      if (key === 'queryOperator') return h(Select, { value: record.queryOperator, size: 'small', options: props.queryOperatorOptions, 'onUpdate:value': (value: string) => (record.queryOperator = value) })
      if (key === 'formType') return h(Select, { value: record.formType, size: 'small', options: props.formTypeOptions, 'onUpdate:value': (value: string) => (record.formType = value) })
      if (key === 'dictCode') return h(Input, { value: record.dictCode, size: 'small', 'onUpdate:value': (value: string) => (record.dictCode = value) })
      if (key === 'defaultSort') return h(Switch, { checked: record.defaultSort, size: 'small', 'onUpdate:checked': (value: boolean) => (record.defaultSort = value) })
      if (key === 'sortDirection') return h(Select, { value: record.sortDirection, size: 'small', options: props.sortDirectionOptions, 'onUpdate:value': (value: string) => (record.sortDirection = value) })
      return null
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
const schemaOptions = ref<CodegenMetaOption[]>([])
const tableOptions = ref<CodegenMetaOption[]>([])
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
  { label: 'input', value: 'input' },
  { label: 'select', value: 'select' },
  { label: 'date', value: 'date' },
]

const queryOperatorOptions = [
  { label: 'like', value: 'like' },
  { label: 'eq', value: 'eq' },
  { label: 'gt', value: 'gt' },
  { label: 'lt', value: 'lt' },
]

const formTypeOptions = [
  { label: 'input', value: 'input' },
  { label: 'textarea', value: 'textarea' },
  { label: 'number', value: 'number' },
  { label: 'select', value: 'select' },
  { label: 'date', value: 'date' },
  { label: 'datetime', value: 'datetime' },
]

const sortDirectionOptions = [
  { label: 'ASC', value: 'ASC' },
  { label: 'DESC', value: 'DESC' },
]

const basicRules: Record<string, Rule[]> = {
  configName: [{ required: true, message: t('system.codegen.form.configName'), trigger: 'blur' }],
  datasourceId: [{ required: true, message: t('system.codegen.form.datasource'), trigger: 'change' }],
  schemaName: [{ required: true, message: t('system.codegen.form.schemaName'), trigger: 'change' }],
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
  { title: t('system.codegen.columnName'), dataIndex: 'columnName', key: 'columnName', width: 160, fixed: 'left' },
  { title: t('system.codegen.columnComment'), dataIndex: 'columnComment', key: 'columnComment', width: 180 },
  { title: t('system.codegen.queryable'), dataIndex: 'queryable', key: 'queryable', width: 90, align: 'center' },
  { title: t('system.codegen.tableShow'), dataIndex: 'tableShow', key: 'tableShow', width: 90, align: 'center' },
  { title: t('system.codegen.formShow'), dataIndex: 'formShow', key: 'formShow', width: 90, align: 'center' },
  { title: t('system.codegen.required'), dataIndex: 'required', key: 'required', width: 90, align: 'center' },
  { title: t('system.codegen.queryType'), dataIndex: 'queryType', key: 'queryType', width: 120 },
  { title: t('system.codegen.queryOperator'), dataIndex: 'queryOperator', key: 'queryOperator', width: 120 },
  { title: t('system.codegen.formType'), dataIndex: 'formType', key: 'formType', width: 120 },
  { title: t('system.codegen.dictCode'), dataIndex: 'dictCode', key: 'dictCode', width: 140 },
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
  schemaOptions.value = []
  tableOptions.value = []
  currentStep.value = 0
  previewResult.zipFileName = ''
  previewResult.files = []
  selectedFilePath.value = ''
}

async function loadDatasources() {
  datasourceList.value = await listCodegenDatasources()
}

async function loadDetail(id: number) {
  const detail = await getCodegenConfigDetail(id)
  fillForm(detail)
  if (formData.datasourceId) {
    schemaOptions.value = await listCodegenSchemas(formData.datasourceId)
  }
  if (formData.datasourceId && formData.schemaName) {
    tableOptions.value = await listCodegenTables(formData.datasourceId, formData.schemaName)
  }
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
  formData.schemaName = ''
  formData.mainTableName = ''
  formData.treeTableName = ''
  formData.subTableName = ''
  formData.mainColumns = []
  formData.treeColumns = []
  formData.subColumns = []
  if (!formData.datasourceId) {
    schemaOptions.value = []
    tableOptions.value = []
    return
  }
  schemaOptions.value = await listCodegenSchemas(formData.datasourceId)
}

async function handleSchemaChange() {
  formData.mainTableName = ''
  formData.treeTableName = ''
  formData.subTableName = ''
  formData.mainColumns = []
  formData.treeColumns = []
  formData.subColumns = []
  if (!formData.datasourceId || !formData.schemaName) {
    tableOptions.value = []
    return
  }
  tableOptions.value = await listCodegenTables(formData.datasourceId, formData.schemaName)
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

function toColumnOptions(columns: CodegenColumnConfig[]) {
  return (columns || []).map((item) => ({
    label: `${item.columnName}${item.columnComment ? `(${item.columnComment})` : ''}`,
    value: item.columnName,
  }))
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
    if (props.configId) {
      await loadDetail(props.configId)
    } else {
      resetForm()
    }
  },
  { immediate: true },
)
</script>

<style scoped lang="less">
.codegen-drawer {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.codegen-drawer__steps {
  margin-bottom: 8px;
}

.codegen-drawer__step {
  min-height: 480px;
}

.codegen-drawer__subcard {
  margin-top: 16px;
}

.codegen-drawer__preview {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.codegen-drawer__preview-toolbar {
  display: flex;
  justify-content: flex-end;
}

.codegen-drawer__preview-content {
  display: grid;
  grid-template-columns: 260px 1fr;
  gap: 16px;
  min-height: 520px;
}

.codegen-drawer__tree {
  padding: 12px;
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  overflow: auto;
}

.codegen-drawer__viewer {
  display: flex;
  flex-direction: column;
  min-width: 0;
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  overflow: hidden;
}

.codegen-drawer__viewer-header {
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f0;
  background: #fafafa;
  font-weight: 500;
}

.codegen-drawer__code {
  flex: 1;
  margin: 0;
  padding: 16px;
  overflow: auto;
  background: #0f172a;
  color: #e2e8f0;
  font-size: 12px;
  line-height: 1.6;
}

.codegen-drawer__footer {
  display: flex;
  justify-content: flex-end;
}
</style>
