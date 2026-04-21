<template>
  <div class="i18n-message-container">
    <FxDynamicTable
      ref="tableRef"
      table-code="I18nMessageTable"
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
        <a-button v-permission="'sys:i18nMessage:add'" type="primary" @click="handleAdd">
          {{ t('common.add') }}
        </a-button>
      </template>

      <template #textI18nJson="{ record }">
        <span class="text-preview">{{ getI18nValue(record.textI18nJson) || '-' }}</span>
      </template>

      <template #enabled="{ record }">
        <a-tag :color="record.enabled ? 'green' : 'red'">
          {{ record.enabled ? t('common.enabled') : t('common.disabled') }}
        </a-tag>
      </template>

      <template #action="{ record }">
        <a-space>
          <a v-permission="'sys:i18nMessage:edit'" @click="handleEdit(record)">{{ t('common.edit') }}</a>
          <a v-permission="'sys:i18nMessage:delete'" style="color: #ff4d4f" @click="handleDelete(record)">
            {{ t('common.delete') }}
          </a>
        </a-space>
      </template>
    </FxDynamicTable>

    <a-modal
      v-model:open="dialogVisible"
      :title="dialogTitle"
      width="900px"
      :confirm-loading="saving"
      @ok="handleSubmit"
      @cancel="handleDialogClose"
    >
      <a-form
        ref="formRef"
        :model="form"
        :rules="formRules"
        :label-col="{ span: 4 }"
        :wrapper-col="{ span: 19 }"
      >
        <a-form-item label="模块" name="module">
          <a-input v-model:value="form.module" placeholder="请输入模块编码" />
        </a-form-item>
        <a-form-item label="消息编码" name="promptCode">
          <a-input v-model:value="form.promptCode" placeholder="请输入消息编码" />
        </a-form-item>
        <a-form-item label="多语言内容" name="textI18nJson">
          <I18nInput
            v-model="form.textI18nJson"
            mode="simple"
            type="textarea"
            :rows="6"
            placeholder="请输入消息内容，点击右侧图标配置多语言"
            :show-placeholders="true"
          />
        </a-form-item>
        <a-form-item label="状态" name="enabled">
          <a-radio-group v-model:value="form.enabled">
            <a-radio :value="true">{{ t('common.enabled') }}</a-radio>
            <a-radio :value="false">{{ t('common.disabled') }}</a-radio>
          </a-radio-group>
        </a-form-item>
        <a-form-item label="版本" name="version">
          <a-input-number v-model:value="form.version" :min="1" style="width: 100%" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { message, Modal } from 'ant-design-vue'
import type { Rule } from 'ant-design-vue/es/form'
import FxDynamicTable from '@/components/common/FxDynamicTable.vue'
import I18nInput from '@/components/common/I18nInput.vue'
import type { FxTableConfig } from '@/api/system/tableConfig'
import {
  createI18nMessage,
  deleteI18nMessage,
  getI18nMessage,
  pageI18nMessages,
  updateI18nMessage,
  type I18nMessageItem,
} from '@/api/system/i18nMessage'
import { getI18nValue } from '@/utils/i18n'

const { t } = useI18n()

const tableRef = ref()
const formRef = ref()
const dialogVisible = ref(false)
const dialogTitle = ref('')
const saving = ref(false)

const form = reactive<Partial<I18nMessageItem>>({
  id: undefined,
  module: '',
  promptCode: '',
  textI18nJson: '',
  enabled: true,
  version: 1,
})

const formRules: Record<string, Rule[]> = {
  module: [{ required: true, message: '请输入模块编码', trigger: 'blur' }],
  promptCode: [{ required: true, message: '请输入消息编码', trigger: 'blur' }],
  textI18nJson: [{ required: true, message: '请配置多语言内容', trigger: 'change' }],
}

const dynamicTableConfig = computed<Partial<FxTableConfig>>(() => ({
  tableCode: 'I18nMessageTable',
  tableName: '多语言消息',
  tableType: 'NORMAL',
  rowKey: 'id',
  defaultPageSize: 10,
  columns: [
    { field: 'module', title: '模块', width: 160, align: 'left' },
    { field: 'promptCode', title: '消息编码', width: 220, align: 'left' },
    { field: 'textI18nJson', title: '当前语言内容', width: 280, align: 'left', ellipsis: true },
    { field: 'enabled', title: t('common.status'), width: 100, align: 'center' },
    { field: 'version', title: '版本', width: 90, align: 'center' },
    { field: 'updateTime', title: t('common.updateTime'), width: 180, align: 'center' },
    { field: 'action', title: t('common.action'), width: 140, align: 'center', fixed: 'right' },
  ],
  queryFields: [
    { field: 'module', label: '模块', queryType: 'input', queryOperator: 'like' },
    { field: 'promptCode', label: '消息编码', queryType: 'input', queryOperator: 'like' },
    {
      field: 'enabled',
      label: t('common.status'),
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
    const res = await pageI18nMessages({
      pageNum: payload.page.current,
      pageSize: payload.page.pageSize,
      module: payload.query?.module,
      promptCode: payload.query?.promptCode,
      enabled: payload.query?.enabled,
    })
    return {
      records: res?.records || [],
      total: Number(res?.total || 0),
    }
  } catch (error) {
    console.error('加载多语言消息失败', error)
    return {
      records: [],
      total: 0,
    }
  }
}

function resetForm() {
  form.id = undefined
  form.module = ''
  form.promptCode = ''
  form.textI18nJson = ''
  form.enabled = true
  form.version = 1
  formRef.value?.clearValidate?.()
}

function handleAdd() {
  resetForm()
  dialogTitle.value = '新增多语言消息'
  dialogVisible.value = true
}

async function handleEdit(row: I18nMessageItem) {
  resetForm()
  dialogTitle.value = '编辑多语言消息'
  try {
    const detail = await getI18nMessage(Number(row.id))
    form.id = detail.id
    form.module = detail.module
    form.promptCode = detail.promptCode
    form.textI18nJson = detail.textI18nJson
    form.enabled = detail.enabled
    form.version = detail.version || 1
    dialogVisible.value = true
  } catch (error) {
    console.error('加载多语言消息详情失败', error)
    message.error('加载详情失败')
  }
}

function handleDelete(row: I18nMessageItem) {
  Modal.confirm({
    title: t('common.tip'),
    content: '确认删除该多语言消息吗？',
    okText: t('common.ok'),
    cancelText: t('common.cancel'),
    onOk: async () => {
      await deleteI18nMessage(Number(row.id))
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
      await updateI18nMessage(form as I18nMessageItem)
      message.success(t('common.updateSuccess'))
    } else {
      await createI18nMessage(form as I18nMessageItem)
      message.success(t('common.createSuccess'))
    }
    dialogVisible.value = false
    await tableRef.value?.refresh?.()
  } catch (error) {
    console.error('保存多语言消息失败', error)
  } finally {
    saving.value = false
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
.i18n-message-container {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
  overflow: hidden;
}

.text-preview {
  display: -webkit-box;
  overflow: hidden;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}
</style>
