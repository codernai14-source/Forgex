<template>
  <div class="datasource-container">
    <FxDynamicTable
      ref="tableRef"
      table-code="ReportDatasourceTable"
      :request="handleRequest"
      :dict-options="dictOptions"
      :show-query-form="true"
      row-key="id"
    >
      <template #toolbar>
        <a-button
          v-permission="'report:datasource:add'"
          type="primary"
          @click="handleAdd"
        >
          <template #icon><PlusOutlined /></template>
          {{ $tl('新增数据源') }}
        </a-button>
      </template>

      <template #type="{ record }">
        <a-tag :color="resolveDbTypeColor(record.type)">
          {{ resolveDbTypeLabel(record.type) }}
        </a-tag>
      </template>

      <template #status="{ record }">
        <a-tag
          v-if="resolveStatusTag(record.status)"
          :color="resolveStatusTag(record.status)?.color"
          :style="resolveStatusTag(record.status)?.style"
        >
          {{ resolveStatusTag(record.status)?.label }}
        </a-tag>
        <span v-else>{{ record.status ?? '-' }}</span>
      </template>

      <template #action="{ record }">
        <a-space>
          <a
            v-permission="'report:datasource:edit'"
            @click="handleEdit(record)"
          >
            {{ $tl('编辑') }}
          </a>
          <a
            v-permission="'report:datasource:test'"
            @click="handleTest(record)"
          >
            {{ $tl('连接测试') }}
          </a>
          <a
            v-permission="'report:datasource:delete'"
            style="color: #ff4d4f"
            @click="handleDelete(record)"
          >
            {{ $tl('删除') }}
          </a>
        </a-space>
      </template>
    </FxDynamicTable>

    <a-modal
      v-model:open="formVisible"
      :title="formTitle"
      width="750px"
      :destroy-on-close="true"
      @ok="handleSubmit"
      @cancel="handleCancel"
    >
      <a-form
        ref="formRef"
        :model="form"
        :rules="formRules"
        :label-col="{ span: 5 }"
        :wrapper-col="{ span: 16, offset: 1 }"
      >
        <a-form-item :label="$tl('数据源名称')" name="name">
          <a-input
            v-model:value="form.name"
            :placeholder="$tl('请输入数据源名称')"
            maxlength="100"
            show-count
          />
        </a-form-item>

        <a-form-item :label="$tl('数据源编码')" name="code">
          <a-input
            v-model:value="form.code"
            :placeholder="$tl('请输入数据源编码，首字母必须为字母，可包含字母、数字和下划线')"
            maxlength="50"
            show-count
            :disabled="!!form.id"
          />
        </a-form-item>

        <a-form-item :label="$tl('数据库类型')" name="type">
          <a-select
            v-model:value="form.type"
            :placeholder="$tl('请选择数据库类型')"
            @change="handleDbTypeChange"
          >
            <a-select-option value="mysql">MySQL</a-select-option>
            <a-select-option value="oracle">Oracle</a-select-option>
            <a-select-option value="postgresql">PostgreSQL</a-select-option>
            <a-select-option value="sqlserver">SQL Server</a-select-option>
            <a-select-option value="h2">H2</a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item :label="$tl('连接地址 URL')" name="url">
          <a-textarea
            v-model:value="form.url"
            :placeholder="$tl('请输入数据库连接地址 URL')"
            :rows="2"
            maxlength="500"
            show-count
          />
        </a-form-item>

        <a-form-item :label="$tl('用户名')" name="username">
          <a-input
            v-model:value="form.username"
            :placeholder="$tl('请输入数据库用户名')"
            maxlength="100"
          />
        </a-form-item>

        <a-form-item :label="$tl('密码')" name="password">
          <a-input-password
            v-model:value="form.password"
            :placeholder="$tl('请输入数据库密码')"
            maxlength="100"
          />
        </a-form-item>

        <a-form-item :label="$tl('驱动类名')" name="driverClass">
          <a-input
            v-model:value="form.driverClass"
            :placeholder="$tl('请输入数据库驱动类名')"
            maxlength="200"
            :disabled="autoFillDriver"
          />
        </a-form-item>

        <a-form-item :label="$tl('连接池配置')" name="poolConfig">
          <a-textarea
            v-model:value="form.poolConfig"
            :placeholder="$tl('请输入连接池配置 JSON 字符串，如无特殊需求可留空')"
            :rows="3"
            maxlength="1000"
            show-count
          />
        </a-form-item>

        <a-form-item :label="$tl('状态')" name="status">
          <a-radio-group v-model:value="form.status">
            <a-radio :value="1">{{ $tl('启用') }}</a-radio>
            <a-radio :value="0">{{ $tl('禁用') }}</a-radio>
          </a-radio-group>
        </a-form-item>

        <a-form-item :label="$tl('备注')" name="remark">
          <a-textarea
            v-model:value="form.remark"
            :placeholder="$tl('请输入备注')"
            :rows="2"
            maxlength="500"
            show-count
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, nextTick } from 'vue'
import { Modal, type FormInstance } from 'ant-design-vue'
import { PlusOutlined } from '@ant-design/icons-vue'
import FxDynamicTable from '@/components/common/FxDynamicTable.vue'
import { useDict } from '@/hooks/useDict'

import type { ReportDatasource, ReportDatasourceParam, DatasourceSaveDTO } from '@/report/types'
import { translateLegacyText } from '@/utils/legacyI18n'
import {
pageDatasource,
  removeDatasource,
  testDatasourceConfig,
  testDatasource,
} from '@/api/report'

const { dictItems: statusOptions } = useDict('status')

const tableRef = ref()
const formVisible = ref(false)
const formRef = ref<FormInstance>()

const form = reactive<DatasourceSaveDTO>({
  id: undefined,
  name: '',
  code: '',
  type: 'mysql',
  url: '',
  username: '',
  password: '',
  driverClass: '',
  poolConfig: undefined,
  status: 1,
  remark: '',
})

const autoFillDriver = ref(false)

const formTitle = computed(() => (form.id ? translateLegacyText('编辑数据源') : translateLegacyText('新增数据源')))

const formRules = {
  name: [
    { required: true, message: translateLegacyText('请输入数据源名称'), trigger: 'blur' },
    { max: 100, message: translateLegacyText('数据源名称不能超过 100 个字符'), trigger: 'blur' },
  ],
  code: [
    { required: true, message: translateLegacyText('请输入数据源编码'), trigger: 'blur' },
    {
      pattern: /^[a-zA-Z][a-zA-Z0-9_]*$/,
      message: translateLegacyText('数据源编码必须以字母开头，且只能包含字母、数字和下划线'),
      trigger: 'blur',
    },
    { max: 50, message: translateLegacyText('数据源编码不能超过 50 个字符'), trigger: 'blur' },
  ],
  type: [{ required: true, message: translateLegacyText('请选择数据库类型'), trigger: 'change' }],
  url: [
    { required: true, message: translateLegacyText('请输入数据库连接地址 URL'), trigger: 'blur' },
    { max: 500, message: translateLegacyText('URL 不能超过 500 个字符'), trigger: 'blur' },
  ],
  username: [
    { required: true, message: translateLegacyText('请输入数据库用户名'), trigger: 'blur' },
    { max: 100, message: translateLegacyText('用户名不能超过 100 个字符'), trigger: 'blur' },
  ],
}

const dictOptions = computed(() => ({
  status: statusOptions.value,
  dbType: [
    { label: 'MySQL', value: 'mysql', color: 'blue' },
    { label: 'Oracle', value: 'oracle', color: 'red' },
    { label: 'PostgreSQL', value: 'postgresql', color: 'green' },
    { label: 'SQL Server', value: 'sqlserver', color: 'orange' },
    { label: 'H2', value: 'h2', color: 'purple' },
  ],
}))


function resolveDbTypeLabel(value: string) {
  const typeMap: Record<string, string> = {
    mysql: 'MySQL',
    oracle: 'Oracle',
    postgresql: 'PostgreSQL',
    sqlserver: 'SQL Server',
    h2: 'H2',
  }
  return typeMap[value] || value
}

function resolveDbTypeColor(value: string) {
  const colorMap: Record<string, string> = {
    mysql: 'blue',
    oracle: 'red',
    postgresql: 'green',
    sqlserver: 'orange',
    h2: 'purple',
  }
  return colorMap[value] || 'default'
}

function resolveStatusTag(value: unknown) {
  const normalizedValue = value === true || value === 1 || value === '1' ? 1 : 0
  const dictItem = statusOptions.value.find((item) => String(item?.value) === String(normalizedValue))
  if (!dictItem) {
    return null
  }
  const style =
    dictItem.tagStyle?.borderColor || dictItem.tagStyle?.backgroundColor
      ? {
          borderColor: dictItem.tagStyle?.borderColor,
          backgroundColor: dictItem.tagStyle?.backgroundColor,
        }
      : undefined

  return {
    label: dictItem.label,
    color: dictItem.tagStyle?.color || dictItem.color || 'blue',
    style,
  }
}

function handleDbTypeChange(value: string) {
  autoFillDriver.value = true
  const driverMap: Record<string, string> = {
    mysql: 'com.mysql.cj.jdbc.Driver',
    oracle: 'oracle.jdbc.OracleDriver',
    postgresql: 'org.postgresql.Driver',
    sqlserver: 'com.microsoft.sqlserver.jdbc.SQLServerDriver',
    h2: 'org.h2.Driver',
  }
  form.driverClass = driverMap[value] || ''
}

const handleRequest = async (payload: {
  page: { current: number; pageSize: number }
  query: Record<string, any>
}) => {
  try {
    const res = await pageDatasource({
      pageNum: payload.page.current,
      pageSize: payload.page.pageSize,
      name: payload.query?.name,
      code: payload.query?.code,
      type: payload.query?.type,
      status: payload.query?.status,
    } as ReportDatasourceParam)
    return {
      records: res.records || [],
      total: Number(res.total || 0),
    }
  } catch (error) {
    console.error('加载数据源分页数据失败', error)
    return {
      records: [],
      total: 0,
    }
  }
}

function resetForm() {
  form.id = undefined
  form.name = ''
  form.code = ''
  form.type = 'mysql'
  form.url = ''
  form.username = ''
  form.password = ''
  form.driverClass = ''
  form.poolConfig = undefined
  form.status = 1
  form.remark = ''
  autoFillDriver.value = false
  formRef.value?.resetFields()
}

function loadFormData(data: ReportDatasource) {
  form.id = data.id
  form.name = data.name || ''
  form.code = data.code || ''
  form.type = data.type || 'mysql'
  form.url = data.url || ''
  form.username = data.username || ''
  form.password = data.password || ''
  form.driverClass = data.driverClass || ''
  form.poolConfig = data.poolConfig
  form.status = data.status ?? 1
  form.remark = data.remark || ''

  if (form.driverClass) {
    autoFillDriver.value = true
  }
}

function handleAdd() {
  resetForm()
  formVisible.value = true
}

function handleEdit(record: ReportDatasource) {
  nextTick(() => {
    loadFormData(record)
    formVisible.value = true
  })
}

function handleDelete(record: ReportDatasource) {
  Modal.confirm({
    title: translateLegacyText('提示'),
    content: translateLegacyText(`确定要删除数据源“${record.name}”吗？`),
    okText: translateLegacyText('确定'),
    cancelText: translateLegacyText('取消'),
    onOk: async () => {
      try {
        await removeDatasource(record.id)
        await tableRef.value?.refresh?.()
      } catch (error) {
        console.error('删除失败', error)
      }
    },
  })
}

async function handleTest(record: ReportDatasource) {
  try {
    await testDatasource(record.id)
  } catch (error) {
    console.error('连接测试失败', error)
  }
}

async function handleSubmit() {
  try {
    await formRef.value?.validate()
    await testDatasourceConfig(form)
    formVisible.value = false
    await tableRef.value?.refresh?.()
  } catch (error: any) {
    if (error?.errorFields) {
      return
    }
    console.error('保存数据源失败', error)
  }
}

function handleCancel() {
  resetForm()
  formVisible.value = false
}

onMounted(async () => {
  await tableRef.value?.refresh?.()
})
</script>

<style scoped lang="less" src="@/styles/views/report/datasource/index.less"></style>
