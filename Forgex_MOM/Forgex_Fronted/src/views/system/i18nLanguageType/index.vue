<template>
  <div class="i18n-language-type-container">
    <FxDynamicTable
      ref="tableRef"
      table-code="I18nLanguageTypeTable"
      :request="handleRequest"
      :dynamic-table-config="dynamicTableConfig"
      :show-query-form="true"
      row-key="id"
      :pagination="{
        showSizeChanger: true,
        showQuickJumper: true,
        showTotal: (total: number) => t('common.total', { total }),
      }"
    >
      <template #toolbar>
        <a-button data-guide-id="sys-i18n-language-add" type="primary" @click="handleAdd">{{ t('common.add') }}</a-button>
        <a-upload
          data-guide-id="sys-i18n-language-import"
          :show-upload-list="false"
          :before-upload="handleImport"
          accept=".xlsx,.xls"
        >
          <a-button>导入 Excel</a-button>
        </a-upload>
        <a-button data-guide-id="sys-i18n-language-template" @click="downloadTemplate">{{ t('system.excel.downloadTemplate') }}</a-button>
      </template>

      <template #enabled="{ record }">
        <a-tag :color="record.enabled ? 'green' : 'red'">
          {{ record.enabled ? t('common.enabled') : t('common.disabled') }}
        </a-tag>
      </template>

      <template #isDefault="{ record }">
        <a-tag v-if="record.isDefault" color="gold">
          {{ t('system.i18n.defaultLang') }}
        </a-tag>
        <span v-else>-</span>
      </template>

      <template #action="{ record }">
        <a-space>
          <a v-if="!record.isDefault" @click="handleSetDefault(record)">{{ t('system.i18n.setDefault') }}</a>
          <a @click="handleEdit(record)">{{ t('common.edit') }}</a>
          <a style="color: #ff4d4f" @click="handleDelete(record)">{{ t('common.delete') }}</a>
        </a-space>
      </template>
    </FxDynamicTable>

    <a-modal
      v-model:open="dialogVisible"
      :title="dialogTitle"
      width="700px"
      :confirm-loading="saving"
      @ok="handleSubmit"
      @cancel="handleDialogClose"
    >
      <a-form
        ref="formRef"
        :model="form"
        :rules="formRules"
        :label-col="{ span: 5 }"
        :wrapper-col="{ span: 17 }"
      >
        <a-form-item :label="t('system.i18n.langCode')" name="langCode">
          <a-input
            v-model:value="form.langCode"
            :placeholder="t('system.i18n.langCodePlaceholder')"
            :disabled="!!form.id"
          />
        </a-form-item>
        <a-form-item :label="t('system.i18n.langName')" name="langName">
          <a-input v-model:value="form.langName" :placeholder="t('system.i18n.langNamePlaceholder')" />
        </a-form-item>
        <a-form-item :label="t('system.i18n.langNameEn')" name="langNameEn">
          <a-input v-model:value="form.langNameEn" :placeholder="t('system.i18n.langNameEnPlaceholder')" />
        </a-form-item>
        <a-form-item :label="t('system.i18n.icon')" name="icon">
          <a-input v-model:value="form.icon" :placeholder="t('system.i18n.iconPlaceholder')" />
        </a-form-item>
        <a-form-item :label="t('system.i18n.orderNum')" name="orderNum">
          <a-input-number v-model:value="form.orderNum" :min="0" style="width: 100%" />
        </a-form-item>
        <a-form-item :label="t('system.i18n.enabled')" name="enabled">
          <a-radio-group v-model:value="form.enabled">
            <a-radio :value="true">{{ t('common.enabled') }}</a-radio>
            <a-radio :value="false">{{ t('common.disabled') }}</a-radio>
          </a-radio-group>
        </a-form-item>
        <a-form-item :label="t('system.i18n.isDefault')" name="isDefault">
          <a-switch v-model:checked="form.isDefault" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { Modal, message } from 'ant-design-vue'
import type { FxTableConfig } from '@/api/system/tableConfig'
import type { Rule } from 'ant-design-vue/es/form'
import FxDynamicTable from '@/components/common/FxDynamicTable.vue'
import type { LanguageType } from '@/api/system/i18n'
import {
  pageLanguages,
  getLanguageById,
  createLanguage,
  updateLanguage,
  deleteLanguage,
  setDefaultLanguage,
  importLanguages,
  downloadImportTemplate,
} from '@/api/system/i18n'

const { t } = useI18n()

const tableRef = ref()
const formRef = ref()
const dialogVisible = ref(false)
const dialogTitle = ref('')
const saving = ref(false)

const form = reactive<Partial<LanguageType>>({
  id: null,
  langCode: '',
  langName: '',
  langNameEn: '',
  icon: '',
  orderNum: 0,
  enabled: true,
  isDefault: false,
})

const formRules: Record<string, Rule[]> = {
  langCode: [{ required: true, message: t('system.i18n.langCodeRequired'), trigger: 'blur' }],
  langName: [{ required: true, message: t('system.i18n.langNameRequired'), trigger: 'blur' }],
}

const dynamicTableConfig = computed<Partial<FxTableConfig>>(() => ({
  tableCode: 'I18nLanguageTypeTable',
  tableName: t('system.i18n.languageType'),
  tableType: 'NORMAL',
  rowKey: 'id',
  defaultPageSize: 10,
  columns: [
    { field: 'langCode', title: t('system.i18n.langCode'), width: 120, align: 'left' },
    { field: 'langName', title: t('system.i18n.langName'), width: 150, align: 'left' },
    { field: 'langNameEn', title: t('system.i18n.langNameEn'), width: 150, align: 'left' },
    { field: 'icon', title: t('system.i18n.icon'), width: 100, align: 'center' },
    { field: 'orderNum', title: t('system.i18n.orderNum'), width: 90, align: 'center', sortable: true },
    { field: 'enabled', title: t('system.i18n.enabled'), width: 100, align: 'center' },
    { field: 'isDefault', title: t('system.i18n.isDefault'), width: 120, align: 'center' },
    { field: 'createTime', title: t('common.createTime'), width: 180, align: 'center' },
    { field: 'action', title: t('common.action'), width: 200, align: 'center', fixed: 'right' },
  ],
  queryFields: [
    { field: 'langCode', label: t('system.i18n.langCode'), queryType: 'input', queryOperator: 'like' },
    { field: 'langName', label: t('system.i18n.langName'), queryType: 'input', queryOperator: 'like' },
    {
      field: 'enabled',
      label: t('system.i18n.enabled'),
      queryType: 'select',
      queryOperator: 'eq',
      options: [
        { label: t('common.enabled'), value: true },
        { label: t('common.disabled'), value: false },
      ],
    },
  ],
  version: 1,
}))

const handleRequest = async (payload: {
  page: { current: number; pageSize: number }
  query: Record<string, any>
}) => {
  try {
    const res = await pageLanguages({
      pageNum: payload.page.current,
      pageSize: payload.page.pageSize,
      langCode: payload.query?.langCode,
      langName: payload.query?.langName,
      enabled: payload.query?.enabled,
    })
    return {
      records: res?.records || [],
      total: Number(res?.total || 0),
    }
  } catch (error) {
    console.error('加载语言配置列表失败', error)
    return {
      records: [],
      total: 0,
    }
  }
}

function resetForm() {
  form.id = null
  form.langCode = ''
  form.langName = ''
  form.langNameEn = ''
  form.icon = ''
  form.orderNum = 0
  form.enabled = true
  form.isDefault = false
  formRef.value?.clearValidate?.()
}

function handleAdd() {
  resetForm()
  dialogTitle.value = t('common.add')
  dialogVisible.value = true
}

async function handleEdit(row: any) {
  resetForm()
  dialogTitle.value = t('common.edit')
  try {
    const data = await getLanguageById(row.id)
    form.id = data.id
    form.langCode = data.langCode
    form.langName = data.langName
    form.langNameEn = data.langNameEn
    form.icon = data.icon
    form.orderNum = data.orderNum
    form.enabled = data.enabled
    form.isDefault = data.isDefault
    dialogVisible.value = true
  } catch (error) {
    console.error('加载语言详情失败', error)
    message.error(t('common.getDetailFailed'))
  }
}

function handleDelete(row: any) {
  Modal.confirm({
    title: t('common.tip'),
    content: t('common.confirmDelete'),
    okText: t('common.ok'),
    cancelText: t('common.cancel'),
    onOk: async () => {
      await deleteLanguage(row.id)
      await tableRef.value?.refresh?.()
      message.success(t('common.deleteSuccess'))
    },
  })
}

async function handleSubmit() {
  try {
    await formRef.value?.validate?.()
    saving.value = true
    if (form.id) {
      await updateLanguage(form)
      message.success(t('common.updateSuccess'))
    } else {
      await createLanguage(form)
      message.success(t('common.createSuccess'))
    }
    dialogVisible.value = false
    await tableRef.value?.refresh?.()
  } catch (error) {
    console.error('保存语言配置失败', error)
  } finally {
    saving.value = false
  }
}

async function handleSetDefault(row: any) {
  Modal.confirm({
    title: t('common.tip'),
    content: t('system.i18n.confirmSetDefault'),
    okText: t('common.ok'),
    cancelText: t('common.cancel'),
    onOk: async () => {
      await setDefaultLanguage(row.id)
      await tableRef.value?.refresh?.()
      message.success(t('common.updateSuccess'))
    },
  })
}

async function handleImport(file: File) {
  try {
    const res = await importLanguages(file)
    const { successCount, failCount, errorMessage } = res
    if (failCount > 0 && errorMessage) {
      Modal.warning({
        title: t('system.excel.importResult'),
        content: `${t('system.excel.importSuccess')}: ${successCount}, ${t('system.excel.importFail')}: ${failCount}`,
        okText: t('common.ok'),
      })
    } else {
      message.success(`${t('system.excel.importSuccess')}: ${successCount}`)
    }
    await tableRef.value?.refresh?.()
  } catch (error) {
    console.error('导入语言失败', error)
    message.error(t('system.excel.importFailed'))
  }
  return false
}

async function downloadTemplate() {
  try {
    const blob = await downloadImportTemplate()
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = 'import-template-I18nLanguageTypeTable.xlsx'
    link.click()
    window.URL.revokeObjectURL(url)
    message.success(t('system.excel.downloadSuccess'))
  } catch (error) {
    console.error('下载模板失败', error)
    message.error(t('system.excel.downloadFailed'))
  }
}

function handleDialogClose() {
  dialogVisible.value = false
  formRef.value?.clearValidate?.()
}

onMounted(() => {
  tableRef.value?.refresh?.()
})
</script>

<style scoped lang="less">
.i18n-language-type-container {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
  overflow: hidden;
}
</style>
