<template>
  <div class="homepage-component-page">
    <a-card :bordered="false">
      <div class="homepage-component-page__toolbar">
        <a-space wrap>
          <a-radio-group v-model:value="query.scopeLevel" button-style="solid" @change="reload">
            <a-radio-button value="TENANT">{{ $t('personalHomepage.library.scopeTenant') }}</a-radio-button>
            <a-radio-button value="PUBLIC">{{ $t('personalHomepage.library.scopePublic') }}</a-radio-button>
          </a-radio-group>
          <a-input-search
            v-model:value="query.keyword"
            allow-clear
            style="width: 280px"
            :placeholder="$t('system.homepageComponent.searchPlaceholder')"
            @search="reload"
          />
          <a-button @click="reload">
            <template #icon><ReloadOutlined /></template>
            {{ $t('common.refresh') }}
          </a-button>
          <a-button v-if="query.scopeLevel === 'TENANT'" @click="handlePullPublic">
            {{ $t('system.homepageComponent.pullPublic') }}
          </a-button>
          <a-button type="primary" @click="openCreate">
            <template #icon><PlusOutlined /></template>
            {{ $t('common.add') }}
          </a-button>
        </a-space>
      </div>

      <a-table
        row-key="id"
        :columns="columns"
        :data-source="records"
        :loading="loading"
        :pagination="pagination"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'icon'">
            <FxIcon :name="record.icon" :size="18" />
          </template>
          <template v-else-if="column.key === 'scopeLevel'">
            <a-tag :color="record.scopeLevel === 'PUBLIC' ? 'blue' : 'green'">{{ resolveScopeTagLabel(record.scopeLevel) }}</a-tag>
          </template>
          <template v-else-if="column.key === 'enabled'">
            <a-tag :color="record.enabled ? 'green' : 'default'">
              {{ record.enabled ? $t('common.enabled') : $t('common.disabled') }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a @click="openEdit(record)">{{ $t('common.edit') }}</a>
              <a style="color: #ff4d4f" @click="handleDelete(record)">{{ $t('common.delete') }}</a>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <BaseFormDialog
      v-model:open="dialogOpen"
      :title="editingId ? $t('system.homepageComponent.edit') : $t('system.homepageComponent.add')"
      mode="drawer"
      width="640px"
      :loading="saving"
      @submit="handleSubmit"
    >
      <a-form ref="formRef" :model="formModel" layout="vertical">
        <a-form-item :label="$t('system.homepageComponent.categoryCode')" name="categoryCode">
          <a-input v-model:value="formModel.categoryCode" />
        </a-form-item>
        <a-form-item :label="$t('system.homepageComponent.categoryName')" name="categoryName">
          <a-input v-model:value="formModel.categoryName" />
        </a-form-item>
        <a-form-item :label="$t('system.homepageComponent.moduleCode')" name="moduleCode">
          <a-select v-model:value="formModel.moduleCode">
            <a-select-option value="personal">personal</a-select-option>
            <a-select-option value="basic">basic</a-select-option>
            <a-select-option value="approval">approval</a-select-option>
            <a-select-option value="sys">sys</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item :label="$t('system.homepageComponent.componentCode')" name="componentCode" required>
          <a-input v-model:value="formModel.componentCode" />
        </a-form-item>
        <a-form-item :label="$t('system.homepageComponent.componentName')" name="componentName" required>
          <a-input v-model:value="formModel.componentName" />
        </a-form-item>
        <a-form-item :label="$t('system.homepageComponent.componentPath')" name="componentPath">
          <a-input v-model:value="formModel.componentPath" />
        </a-form-item>
        <a-form-item :label="$t('system.homepageComponent.icon')" name="icon">
          <IconPicker v-model:value="formModel.icon" />
        </a-form-item>
        <a-form-item :label="$t('system.homepageComponent.useDesc')" name="useDesc">
          <a-textarea v-model:value="formModel.useDesc" :rows="3" />
        </a-form-item>
        <a-form-item :label="$t('system.homepageComponent.defaultParamsJson')" name="defaultParamsJson">
          <a-textarea v-model:value="formModel.defaultParamsJson" :rows="4" />
        </a-form-item>
        <a-form-item :label="$t('system.homepageComponent.orderNum')" name="orderNum">
          <a-input-number v-model:value="formModel.orderNum" style="width: 100%" />
        </a-form-item>
        <a-form-item :label="$t('system.homepageComponent.enabled')" name="enabled">
          <a-switch v-model:checked="formModel.enabled" />
        </a-form-item>
        <a-form-item :label="$t('system.homepageComponent.remark')" name="remark">
          <a-textarea v-model:value="formModel.remark" :rows="2" />
        </a-form-item>
      </a-form>
    </BaseFormDialog>
  </div>
</template>

<script setup lang="ts">
import { computed, createVNode, onMounted, reactive, ref } from 'vue'
import { Modal, message, type TablePaginationConfig } from 'ant-design-vue'
import { PlusOutlined, ReloadOutlined } from '@ant-design/icons-vue'
import BaseFormDialog from '@/components/common/BaseFormDialog.vue'
import FxIcon from '@/components/common/FxIcon.vue'
import IconPicker from '@/components/common/IconPicker.vue'
import {
  deleteHomepageComponent,
  pageHomepageComponents,
  pullPublicHomepageComponents,
  saveHomepageComponent,
  type HomepageComponentSaveParam,
  type HomepageComponentVO,
} from '@/api/system/personalHomepage'

const loading = ref(false)
const saving = ref(false)
const dialogOpen = ref(false)
const editingId = ref<number | undefined>()
const records = ref<HomepageComponentVO[]>([])
const total = ref(0)
const query = reactive({
  scopeLevel: 'TENANT' as 'PUBLIC' | 'TENANT',
  keyword: '',
  current: 1,
  pageSize: 10,
})

const formModel = reactive<HomepageComponentSaveParam>({
  scopeLevel: 'TENANT',
  moduleCode: 'personal',
  categoryCode: 'personal_common',
  categoryName: '通用组件',
  componentCode: '',
  componentName: '',
  componentPath: '',
  icon: 'AppstoreOutlined',
  enabled: true,
  orderNum: 0,
})

const columns = [
  { title: '分类', dataIndex: 'categoryName', key: 'categoryName', width: 120 },
  { title: '编码', dataIndex: 'componentCode', key: 'componentCode', width: 150 },
  { title: '名称', dataIndex: 'componentName', key: 'componentName', width: 150 },
  { title: '图标', dataIndex: 'icon', key: 'icon', width: 70 },
  { title: '路径', dataIndex: 'componentPath', key: 'componentPath', ellipsis: true },
  { title: '作用', dataIndex: 'useDesc', key: 'useDesc', ellipsis: true },
  { title: '层级', dataIndex: 'scopeLevel', key: 'scopeLevel', width: 90 },
  { title: '启用', dataIndex: 'enabled', key: 'enabled', width: 90 },
  { title: '排序', dataIndex: 'orderNum', key: 'orderNum', width: 80 },
  { title: '操作', key: 'action', width: 120, fixed: 'right' },
]

const pagination = computed<TablePaginationConfig>(() => ({
  current: query.current,
  pageSize: query.pageSize,
  total: total.value,
  showSizeChanger: true,
  showTotal: value => `Total ${value}`,
}))

function resetForm() {
  Object.assign(formModel, {
    id: undefined,
    scopeLevel: query.scopeLevel,
    moduleCode: 'personal',
    categoryCode: 'personal_common',
    categoryName: '通用组件',
    componentCode: '',
    componentName: '',
    componentPath: '',
    icon: 'AppstoreOutlined',
    useDesc: '',
    defaultParamsJson: '',
    enabled: true,
    orderNum: 0,
    remark: '',
  })
}

async function reload() {
  loading.value = true
  try {
    const data = await pageHomepageComponents({
      scopeLevel: query.scopeLevel,
      keyword: query.keyword || undefined,
      pageNum: query.current,
      pageSize: query.pageSize,
    })
    records.value = data?.records || []
    total.value = Number(data?.total || 0)
  } finally {
    loading.value = false
  }
}

function handleTableChange(page: TablePaginationConfig) {
  query.current = Number(page.current || 1)
  query.pageSize = Number(page.pageSize || 10)
  reload()
}

function openCreate() {
  editingId.value = undefined
  resetForm()
  dialogOpen.value = true
}

function openEdit(record: HomepageComponentVO) {
  editingId.value = record.id
  Object.assign(formModel, {
    ...record,
    scopeLevel: query.scopeLevel,
    categoryCode: record.categoryCode || 'personal_common',
    categoryName: record.categoryName || '通用组件',
    moduleCode: record.moduleCode || 'personal',
  })
  dialogOpen.value = true
}

function resolveScopeTagLabel(scopeLevel?: string) {
  if (scopeLevel === 'PUBLIC') {
    return '公共'
  }
  if (scopeLevel === 'TENANT') {
    return '租户'
  }
  if (scopeLevel === 'USER') {
    return '个人'
  }
  return scopeLevel || '-'
}

async function handleSubmit() {
  if (!formModel.componentCode || !formModel.componentName) {
    message.warning('请输入组件编码和组件名称')
    return
  }
  saving.value = true
  try {
    await saveHomepageComponent({ ...formModel, scopeLevel: query.scopeLevel, id: editingId.value })
    dialogOpen.value = false
    await reload()
  } finally {
    saving.value = false
  }
}

function handleDelete(record: HomepageComponentVO) {
  if (!record.id) {
    return
  }
  Modal.confirm({
    title: '确认删除组件配置',
    content: createVNode('div', null, `${record.componentName || ''}`),
    onOk: async () => {
      await deleteHomepageComponent(record.id!)
      await reload()
    },
  })
}

async function handlePullPublic() {
  const count = await pullPublicHomepageComponents()
  message.success(`已拉取 ${Number(count || 0)} 条公共组件配置`)
  await reload()
}

onMounted(reload)
</script>

<style scoped lang="less" src="@/styles/views/system/homepageComponent/index.less"></style>
