<template>
  <a-drawer
    :open="open"
    :title="drawerTitle"
    :width="1280"
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
            <a-col :span="8">
              <a-form-item :label="t('system.codegen.pageType')" name="pageType">
                <a-radio-group v-model:value="formData.pageType">
                  <a-radio-button value="SINGLE">{{ t('system.codegen.pageTypeSingle') }}</a-radio-button>
                  <a-radio-button value="MASTER_DETAIL">{{ t('system.codegen.pageTypeMasterDetail') }}</a-radio-button>
                </a-radio-group>
              </a-form-item>
            </a-col>
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
            <a-col v-if="formData.pageType === 'MASTER_DETAIL'" :span="8">
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
            <a-col v-if="formData.pageType === 'MASTER_DETAIL'" :span="8">
              <a-form-item :label="t('system.codegen.subEntityName')" name="subEntityName">
                <a-input v-model:value="formData.subEntityName" :placeholder="t('system.codegen.form.subEntityName')" />
              </a-form-item>
            </a-col>
            <a-col :span="8">
              <a-form-item :label="t('system.codegen.packageName')" name="packageName">
                <a-input v-model:value="formData.packageName" :placeholder="t('system.codegen.form.packageName')" />
              </a-form-item>
            </a-col>
          </a-row>

          <a-row :gutter="16">
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
            <a-col :span="8">
              <a-form-item :label="t('system.codegen.parentMenuPath')" name="parentMenuPath">
                <a-input v-model:value="formData.parentMenuPath" :placeholder="t('system.codegen.form.parentMenuPath')" />
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
            <a-col v-if="formData.pageType === 'MASTER_DETAIL'" :span="6">
              <a-form-item :label="t('system.codegen.subFkColumn')" name="subFkColumn">
                <a-select
                  v-model:value="formData.subFkColumn"
                  :options="subColumnOptions"
                  :placeholder="t('system.codegen.form.subFkColumn')"
                />
              </a-form-item>
            </a-col>
            <a-col v-if="formData.pageType === 'MASTER_DETAIL'" :span="6">
              <a-form-item :label="t('system.codegen.subPkColumn')" name="subPkColumn">
                <a-select
                  v-model:value="formData.subPkColumn"
                  :options="subColumnOptions"
                  :placeholder="t('system.codegen.form.subPkColumn')"
                />
              </a-form-item>
            </a-col>
            <a-col :span="6">
              <a-form-item :label="t('system.codegen.tableCodePrefix')" name="tableCodePrefix">
                <a-input
                  v-model:value="formData.tableCodePrefix"
                  :placeholder="t('system.codegen.form.tableCodePrefix')"
                />
              </a-form-item>
            </a-col>
          </a-row>

          <a-row :gutter="16">
            <a-col :span="8">
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
              <template v-if="column.key === 'columnComment'">
                <a-input v-model:value="record.columnComment" size="small" />
              </template>
              <template v-else-if="column.key === 'queryable'">
                <a-switch v-model:checked="record.queryable" size="small" />
              </template>
              <template v-else-if="column.key === 'tableShow'">
                <a-switch v-model:checked="record.tableShow" size="small" />
              </template>
              <template v-else-if="column.key === 'formShow'">
                <a-switch v-model:checked="record.formShow" size="small" />
              </template>
              <template v-else-if="column.key === 'required'">
                <a-switch v-model:checked="record.required" size="small" />
              </template>
              <template v-else-if="column.key === 'queryType'">
                <a-select v-model:value="record.queryType" size="small" :options="queryTypeOptions" />
              </template>
              <template v-else-if="column.key === 'queryOperator'">
                <a-select v-model:value="record.queryOperator" size="small" :options="queryOperatorOptions" />
              </template>
              <template v-else-if="column.key === 'formType'">
                <a-select v-model:value="record.formType" size="small" :options="formTypeOptions" />
              </template>
              <template v-else-if="column.key === 'dictCode'">
                <a-input v-model:value="record.dictCode" size="small" />
              </template>
              <template v-else-if="column.key === 'defaultSort'">
                <a-switch v-model:checked="record.defaultSort" size="small" />
              </template>
              <template v-else-if="column.key === 'sortDirection'">
                <a-select v-model:value="record.sortDirection" size="small" :options="sortDirectionOptions" />
              </template>
            </template>
          </a-table>
        </a-card>

        <a-card
          v-if="formData.pageType === 'MASTER_DETAIL'"
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
              <template v-if="column.key === 'columnComment'">
                <a-input v-model:value="record.columnComment" size="small" />
              </template>
              <template v-else-if="column.key === 'queryable'">
                <a-switch v-model:checked="record.queryable" size="small" />
              </template>
              <template v-else-if="column.key === 'tableShow'">
                <a-switch v-model:checked="record.tableShow" size="small" />
              </template>
              <template v-else-if="column.key === 'formShow'">
                <a-switch v-model:checked="record.formShow" size="small" />
              </template>
              <template v-else-if="column.key === 'required'">
                <a-switch v-model:checked="record.required" size="small" />
              </template>
              <template v-else-if="column.key === 'queryType'">
                <a-select v-model:value="record.queryType" size="small" :options="queryTypeOptions" />
              </template>
              <template v-else-if="column.key === 'queryOperator'">
                <a-select v-model:value="record.queryOperator" size="small" :options="queryOperatorOptions" />
              </template>
              <template v-else-if="column.key === 'formType'">
                <a-select v-model:value="record.formType" size="small" :options="formTypeOptions" />
              </template>
              <template v-else-if="column.key === 'dictCode'">
                <a-input v-model:value="record.dictCode" size="small" />
              </template>
              <template v-else-if="column.key === 'defaultSort'">
                <a-switch v-model:checked="record.defaultSort" size="small" />
              </template>
              <template v-else-if="column.key === 'sortDirection'">
                <a-select v-model:value="record.sortDirection" size="small" :options="sortDirectionOptions" />
              </template>
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
import { computed, reactive, ref, watch } from 'vue'
import { message, type FormInstance, type Rule } from 'ant-design-vue'
import { useI18n } from 'vue-i18n'
import {
  downloadCodegenZip,
  listCodegenColumns,
  listCodegenSchemas,
  listCodegenTables,
  previewCodegen,
  type CodegenColumnConfig,
  type CodegenMetaOption,
  type CodegenPreviewFile,
  type CodegenPreviewResult,
  type CodegenRequest,
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

const formData = reactive<CodegenConfigSaveParam>({
  id: undefined,
  configName: '',
  datasourceId: 0,
  schemaName: '',
  pageType: 'SINGLE',
  mainTableName: '',
  subTableName: '',
  mainPkColumn: '',
  subFkColumn: '',
  subPkColumn: '',
  moduleName: 'system',
  bizName: '',
  entityName: '',
  subEntityName: '',
  packageName: 'com.forgex.system',
  author: 'Forgex CodeGen',
  menuName: '',
  menuIcon: 'TableOutlined',
  parentMenuPath: '/system',
  tableCodePrefix: 'system',
  generateItems: ['backend', 'frontend', 'sql'],
  mainColumns: [],
  subColumns: [],
  remark: '',
})

const drawerTitle = computed(() =>
  formData.id ? t('system.codegen.edit') : t('system.codegen.add'),
)

const datasourceOptions = computed(() =>
  datasourceList.value.map((item) => ({
    label: `${item.datasourceName}(${item.datasourceCode})`,
    value: item.id!,
  })),
)

const subTableOptions = computed(() =>
  tableOptions.value.filter((item) => item.value !== formData.mainTableName),
)

const mainColumnOptions = computed(() =>
  formData.mainColumns.map((item) => ({
    label: `${item.columnName}${item.columnComment ? `(${item.columnComment})` : ''}`,
    value: item.columnName,
  })),
)

const subColumnOptions = computed(() =>
  (formData.subColumns || []).map((item) => ({
    label: `${item.columnName}${item.columnComment ? `(${item.columnComment})` : ''}`,
    value: item.columnName,
  })),
)

const generateItemOptions = [
  { label: 'Backend', value: 'backend' },
  { label: 'Frontend', value: 'frontend' },
  { label: 'SQL', value: 'sql' },
]

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

const activeFile = computed<CodegenPreviewFile | undefined>(() =>
  previewResult.files.find((item) => item.path === selectedFilePath.value),
)

const selectedKeys = computed(() => (selectedFilePath.value ? [selectedFilePath.value] : []))
const fileTreeData = computed<FileTreeNode[]>(() => buildFileTree(previewResult.files))

function resetForm() {
  formData.id = undefined
  formData.configName = ''
  formData.datasourceId = 0
  formData.datasourceCode = undefined
  formData.schemaName = ''
  formData.pageType = 'SINGLE'
  formData.mainTableName = ''
  formData.subTableName = ''
  formData.mainPkColumn = ''
  formData.subFkColumn = ''
  formData.subPkColumn = ''
  formData.moduleName = 'system'
  formData.bizName = ''
  formData.entityName = ''
  formData.subEntityName = ''
  formData.packageName = 'com.forgex.system'
  formData.author = 'Forgex CodeGen'
  formData.menuName = ''
  formData.menuIcon = 'TableOutlined'
  formData.parentMenuPath = '/system'
  formData.tableCodePrefix = 'system'
  formData.generateItems = ['backend', 'frontend', 'sql']
  formData.mainColumns = []
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
  formData.subTableName = detail.subTableName || ''
  formData.mainPkColumn = detail.mainPkColumn || ''
  formData.subFkColumn = detail.subFkColumn || ''
  formData.subPkColumn = detail.subPkColumn || ''
  formData.moduleName = detail.moduleName
  formData.bizName = detail.bizName
  formData.entityName = detail.entityName
  formData.subEntityName = detail.subEntityName || ''
  formData.packageName = detail.packageName
  formData.author = detail.author
  formData.menuName = detail.menuName
  formData.menuIcon = detail.menuIcon || 'TableOutlined'
  formData.parentMenuPath = detail.parentMenuPath || '/system'
  formData.tableCodePrefix = detail.tableCodePrefix || 'system'
  formData.generateItems = detail.generateItems || ['backend', 'frontend', 'sql']
  formData.mainColumns = enhanceColumns(detail.mainColumns || [])
  formData.subColumns = enhanceColumns(detail.subColumns || [])
  formData.remark = detail.remark || ''
}

async function handleDatasourceChange() {
  formData.schemaName = ''
  formData.mainTableName = ''
  formData.subTableName = ''
  formData.mainColumns = []
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
  formData.subTableName = ''
  formData.mainColumns = []
  formData.subColumns = []
  if (!formData.datasourceId || !formData.schemaName) {
    tableOptions.value = []
    return
  }
  tableOptions.value = await listCodegenTables(formData.datasourceId, formData.schemaName)
}

async function handleMainTableChange() {
  if (!formData.datasourceId || !formData.schemaName || !formData.mainTableName) {
    return
  }
  const columns = await listCodegenColumns(formData.datasourceId, formData.schemaName, formData.mainTableName)
  formData.mainColumns = enhanceColumns(columns)
  const mainPk = formData.mainColumns.find((item) => item.isPrimaryKey)?.columnName
  formData.mainPkColumn = mainPk || formData.mainPkColumn || 'id'
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
}

async function handleSubTableChange() {
  if (!formData.datasourceId || !formData.schemaName || !formData.subTableName) {
    return
  }
  const columns = await listCodegenColumns(formData.datasourceId, formData.schemaName, formData.subTableName)
  formData.subColumns = enhanceColumns(columns)
  const subPk = formData.subColumns.find((item) => item.isPrimaryKey)?.columnName
  formData.subPkColumn = subPk || formData.subPkColumn || 'id'
  if (!formData.subEntityName) {
    formData.subEntityName = toPascalCase(formData.subTableName)
  }
}

async function handleNext() {
  if (currentStep.value === 0) {
    await basicFormRef.value?.validate()
  }
  if (currentStep.value === 1) {
    if (formData.pageType === 'MASTER_DETAIL' && !formData.subTableName) {
      message.warning(t('system.codegen.form.subTableName'))
      return
    }
    await detailFormRef.value?.validate()
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
  if (formData.pageType === 'MASTER_DETAIL') {
    await detailFormRef.value?.validate()
  }
  saveLoading.value = true
  try {
    await saveCodegenConfig({ ...buildRequestPayload(), id: formData.id, configName: formData.configName, remark: formData.remark })
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
    mainColumns: formData.mainColumns.map(normalizeColumnPayload),
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

function toPascalCase(value: string) {
  return String(value || '')
    .split('_')
    .filter(Boolean)
    .map((part) => part.charAt(0).toUpperCase() + part.slice(1).toLowerCase())
    .join('')
}

function toLowerCamel(value: string) {
  if (!value) {
    return value
  }
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
