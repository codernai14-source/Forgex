<template>
  <div class="datasource-container">
    <FxDynamicTable
      ref="tableRef"
      table-code="ReportDatasourceTable"
      :request="handleRequest"
      :dict-options="dictOptions"
      :降级方案-config="降级方案Config"
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
          鏂板
        </a-button>
      </template>

      <template #type="{ record }">
        <a-tag :color="resolveDbTypeColor(record.type)">
          {{ resolveDbTypeLabel(record.type) }}
        </a-tag>
      </template>

      <template #status="{ record }">
        <a-tag
          v-if="resolve状态Tag(record.status)"
          :color="resolve状态Tag(record.status)?.color"
          :style="resolve状态Tag(record.status)?.style"
        >
          {{ resolve状态Tag(record.status)?.label }}
        </a-tag>
        <span v-else>{{ record.status ?? '-' }}</span>
      </template>

      <template #action="{ record }">
        <a-space>
          <a 
            v-permission="'report:datasource:edit'"
            @click="handleEdit(record)"
          >
            缂栬緫
          </a>
          <a 
            v-permission="'report:datasource:test'"
            @click="handleTest(record)"
          >
            娴嬭瘯杩炴帴
          </a>
          <a 
            v-permission="'report:datasource:delete'"
            style="color: #ff4d4f" 
            @click="handleDelete(record)"
          >
            鍒犻櫎
          </a>
        </a-space>
      </template>
    </FxDynamicTable>

    <!-- 鏁版嵁婧愯〃鍗曞脊绐?-->
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
        <a-form-item label="数据源名称" name="name">
          <a-input
            v-model:value="form.name"
            placeholder="璇疯緭鍏ユ暟鎹簮鍚嶇О"
            maxlength="100"
            show-count
          />
        </a-form-item>

        <a-form-item label="数据源编码" name="code">
          <a-input
            v-model:value="form.code"
            placeholder="璇疯緭鍏ユ暟鎹簮缂栫爜锛堣嫳鏂囧瓧姣嶅紑澶达級"
            maxlength="50"
            show-count
            :disabled="!!form.id"
          />
        </a-form-item>

        <a-form-item label="数据库类型" name="type">
          <a-select
            v-model:value="form.type"
            placeholder="请选择数据库类型"
            @change="handleDbTypeChange"
          >
            <a-select-option value="mysql">MySQL</a-select-option>
            <a-select-option value="oracle">Oracle</a-select-option>
            <a-select-option value="postgresql">PostgreSQL</a-select-option>
            <a-select-option value="sqlserver">SQL Server</a-select-option>
            <a-select-option value="h2">H2</a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item label="杩炴帴 URL" name="url">
          <a-textarea
            v-model:value="form.url"
            placeholder="璇疯緭鍏ユ暟鎹簱杩炴帴 URL"
            :rows="2"
            maxlength="500"
            show-count
          />
        </a-form-item>

        <a-form-item label="用户名" name="username">
          <a-input
            v-model:value="form.username"
            placeholder="请输入数据库用户名"
            maxlength="100"
          />
        </a-form-item>

        <a-form-item label="瀵嗙爜" name="password">
          <a-input-password
            v-model:value="form.password"
            placeholder="璇疯緭鍏ユ暟鎹簱瀵嗙爜"
            maxlength="100"
          />
        </a-form-item>

        <a-form-item label="椹卞姩绫诲悕" name="driverClass">
          <a-input
            v-model:value="form.driverClass"
            placeholder="璇疯緭鍏ユ暟鎹簱椹卞姩绫诲悕"
            maxlength="200"
            :disabled="autoFillDriver"
          />
        </a-form-item>

        <a-form-item label="连接池配置" name="poolConfig">
          <a-textarea
            v-model:value="form.poolConfig"
            placeholder="璇疯緭鍏ヨ繛鎺ユ睜閰嶇疆锛圝SON 鏍煎紡锛屽彲閫夛級"
            :rows="3"
            maxlength="1000"
            show-count
          />
        </a-form-item>

        <a-form-item label="状态" name="status">
          <a-radio-group v-model:value="form.status">
            <a-radio :value="1">启用</a-radio>
            <a-radio :value="0">禁用</a-radio>
          </a-radio-group>
        </a-form-item>

        <a-form-item label="澶囨敞" name="remark">
          <a-textarea
            v-model:value="form.remark"
            placeholder="请输入备注"
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
import { Modal, type 表单Instance } from 'ant-design-vue'
import { PlusOutlined } from '@ant-design/icons-vue'
import FxDynamicTable from '@/components/common/FxDynamicTable.vue'
import { useDict } from '@/hooks/useDict'
import type { FxTableConfig } from '@/api/system/tableConfig'
import type { ReportDatasource, ReportDatasourceParam, DatasourceSaveDTO } from '@/api/report/types'
import {
  pageDatasource,
  removeDatasource,
  testDatasourceConfig,
  testDatasource,
} from '@/api/report'

const { dictItems: statusOptions } = useDict('status')

const tableRef = ref()
const formVisible = ref(false)
const formRef = ref<表单Instance>()

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

const formTitle = computed(() => (form.id ? '编辑数据源' : '新增数据源'))

const formRules = {
  name: [
    { required: true, message: '璇疯緭鍏ユ暟鎹簮鍚嶇О', trigger: 'blur' },
    { max: 100, message: '数据源名称不能超过 100 个字符', trigger: 'blur' },
  ],
  code: [
    { required: true, message: '璇疯緭鍏ユ暟鎹簮缂栫爜', trigger: 'blur' },
    {
      pattern: /^[a-zA-Z][a-zA-Z0-9_]*$/,
      message: '数据源编码必须以字母开头，且只能包含字母、数字和下划线',
      trigger: 'blur',
    },
    { max: 50, message: '数据源编码不能超过 50 个字符', trigger: 'blur' },
  ],
  type: [
    { required: true, message: '请选择数据库类型', trigger: 'change' },
  ],
  url: [
    { required: true, message: '璇疯緭鍏ユ暟鎹簱杩炴帴 URL', trigger: 'blur' },
    { max: 500, message: 'URL 不能超过 500 个字符', trigger: 'blur' },
  ],
  username: [
    { required: true, message: '请输入数据库用户名', trigger: 'blur' },
    { max: 100, message: '用户名不能超过 100 个字符', trigger: 'blur' },
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

const 降级方案Config = computed<Partial<FxTableConfig>>(() => ({
  tableCode: 'ReportDatasourceTable',
  tableName: '数据源管理',
  tableType: 'NORMAL',
  rowKey: 'id',
  defaultPageSize: 10,
  columns: [
    { field: 'id', title: 'ID', width: 80, align: 'center' },
    { field: 'name', title: '数据源名称', width: 180, align: 'left' },
    { field: 'code', title: '数据源编码', width: 150, align: 'left' },
    { field: 'type', title: '数据库类型', width: 120, align: 'center', dictCode: 'dbType' },
    { field: 'url', title: '杩炴帴 URL', width: 300, align: 'left' },
    { field: 'username', title: '用户名', width: 120, align: 'center' },
    { field: 'status', title: '状态', width: 100, align: 'center', dictCode: 'status' },
    { field: 'createTime', title: '鍒涘缓鏃堕棿', width: 180, align: 'center' },
    { field: 'action', title: '操作', width: 220, align: 'center', fixed: 'right' },
  ],
  queryFields: [
    { field: 'name', label: '数据源名称', queryType: 'input', queryOperator: 'like' },
    { field: 'code', label: '数据源编码', queryType: 'input', queryOperator: 'like' },
    { field: 'type', label: '数据库类型', queryType: 'select', queryOperator: 'eq', dictCode: 'dbType' },
    { field: 'status', label: '状态', queryType: 'select', queryOperator: 'eq', dictCode: 'status' },
  ],
  version: 1,
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

function resolve状态Tag(value: unknown) {
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

function reset表单() {
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

function load表单Data(data: ReportDatasource) {
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
  reset表单()
  formVisible.value = true
}

function handleEdit(record: ReportDatasource) {
  nextTick(() => {
    load表单Data(record)
    formVisible.value = true
  })
}

function handleDelete(record: ReportDatasource) {
  Modal.confirm({
    title: '提示',
    content: `确定要删除数据源“${record.name}”吗？`,
    okText: '确定',
    cancelText: '取消',
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
  reset表单()
  formVisible.value = false
}

onMounted(async () => {
  await tableRef.value?.refresh?.()
})
</script>

<style scoped lang="less">
.datasource-container {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
  overflow: hidden;
  padding: 16px;
  background-color: #fff;
}
</style>
