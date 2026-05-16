<template>
  <FxDynamicTable
    ref="tableRef"
    table-code="SystemHomepageComponentTable"
    :request="handleRequest"
    row-key="id"
  >
    <template #toolbar>
      <a-space wrap>
        <a-radio-group v-model:value="query.scopeLevel" button-style="solid" @change="handleSearch">
          <a-radio-button value="TENANT">{{ t('personalHomepage.library.scopeTenant') }}</a-radio-button>
          <a-radio-button value="PUBLIC">{{ t('personalHomepage.library.scopePublic') }}</a-radio-button>
        </a-radio-group>
        <a-button @click="handleReload">
          <template #icon><ReloadOutlined /></template>
          {{ t('common.refresh') }}
        </a-button>
        <a-button v-if="query.scopeLevel === 'TENANT'" @click="handlePullPublic">
          {{ t('system.homepageComponent.pullPublic') }}
        </a-button>
        <a-button type="primary" @click="openCreate">
          <template #icon><PlusOutlined /></template>
          {{ t('common.add') }}
        </a-button>
      </a-space>
    </template>

    <template #categoryName="{ record }">
      <a-tag color="blue">{{ record.categoryName || '-' }}</a-tag>
    </template>

    <template #scopeLevel="{ record }">
      <a-tag :color="record.scopeLevel === 'PUBLIC' ? 'blue' : record.scopeLevel === 'TENANT' ? 'green' : 'purple'">
        {{ resolveScopeTagLabel(record.scopeLevel) }}
      </a-tag>
    </template>

    <template #enabled="{ record }">
      <a-tag :color="record.enabled ? 'green' : 'default'">
        {{ record.enabled ? t('common.enabled') : t('common.disabled') }}
      </a-tag>
    </template>

    <template #action="{ record }">
      <FxActionGroup :actions="getRowActions(record)" />
    </template>
  </FxDynamicTable>

  <BaseFormDialog
    v-model:open="dialogOpen"
    :title="editingId ? t('system.homepageComponent.edit') : t('system.homepageComponent.add')"
    mode="drawer"
    width="680px"
    :loading="saving"
    @submit="handleSubmit"
  >
    <a-form ref="formRef" :model="formModel" layout="vertical">
      <a-form-item :label="t('system.homepageComponent.categoryCode')" name="categoryCode">
        <a-input v-model:value="formModel.categoryCode" />
      </a-form-item>
      <a-form-item :label="t('system.homepageComponent.categoryName')" name="categoryName">
        <a-input v-model:value="formModel.categoryName" />
      </a-form-item>
      <a-form-item :label="t('system.homepageComponent.moduleCode')" name="moduleCode">
        <a-select v-model:value="formModel.moduleCode">
          <a-select-option value="personal">personal</a-select-option>
          <a-select-option value="basic">basic</a-select-option>
          <a-select-option value="approval">approval</a-select-option>
          <a-select-option value="sys">sys</a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item :label="t('system.homepageComponent.componentCode')" name="componentCode" required>
        <a-input v-model:value="formModel.componentCode" />
      </a-form-item>
      <a-form-item :label="t('system.homepageComponent.componentName')" name="componentName" required>
        <a-input v-model:value="formModel.componentName" />
      </a-form-item>
      <a-form-item :label="t('system.homepageComponent.componentPath')" name="componentPath">
        <a-input v-model:value="formModel.componentPath" />
      </a-form-item>
      <a-form-item :label="t('system.homepageComponent.icon')" name="icon">
        <IconPicker v-model:value="formModel.icon" />
      </a-form-item>
      <a-form-item :label="t('system.homepageComponent.useDesc')" name="useDesc">
        <a-textarea v-model:value="formModel.useDesc" :rows="3" />
      </a-form-item>
      <a-form-item :label="t('system.homepageComponent.defaultParamsJson')" name="defaultParamsJson">
        <a-textarea v-model:value="formModel.defaultParamsJson" :rows="4" />
      </a-form-item>
      <a-form-item :label="t('system.homepageComponent.orderNum')" name="orderNum">
        <a-input-number v-model:value="formModel.orderNum" style="width: 100%" />
      </a-form-item>
      <a-form-item :label="t('system.homepageComponent.enabled')" name="enabled">
        <a-switch v-model:checked="formModel.enabled" />
      </a-form-item>
      <a-form-item :label="t('system.homepageComponent.remark')" name="remark">
        <a-textarea v-model:value="formModel.remark" :rows="2" />
      </a-form-item>
    </a-form>
  </BaseFormDialog>
</template>

<script setup lang="ts">
import { computed, createVNode, onMounted, reactive, ref } from 'vue'
import { Modal, message } from 'ant-design-vue'
import { PlusOutlined, ReloadOutlined } from '@ant-design/icons-vue'
import { useI18n } from 'vue-i18n'
import BaseFormDialog from '@/components/common/BaseFormDialog.vue'
import FxActionGroup, { type ActionItem } from '@/components/common/FxActionGroup.vue'
import FxDynamicTable from '@/components/common/FxDynamicTable.vue'
import IconPicker from '@/components/common/IconPicker.vue'
import {
  deletePublicHomepageComponent,
  deleteTenantHomepageComponent,
  pageHomepageComponents,
  pullPublicHomepageComponents,
  saveHomepageComponent,
  type HomepageComponentQueryParam,
  type HomepageComponentSaveParam,
  type HomepageComponentVO,
} from '@/api/system/personalHomepage'

const { t } = useI18n()
const tableRef = ref<InstanceType<typeof FxDynamicTable>>()
const loading = ref(false)
const saving = ref(false)
const dialogOpen = ref(false)
const editingId = ref<number | undefined>()
const query = reactive<HomepageComponentQueryParam>({
  scopeLevel: 'TENANT',
  keyword: '',
  pageNum: 1,
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

function handleRequest(payload: any) {
  query.pageNum = payload.page.current
  query.pageSize = payload.page.pageSize
  const tableQuery = payload.query || {}
  return pageHomepageComponents({
    scopeLevel: query.scopeLevel,
    keyword: tableQuery.keyword || undefined,
    componentCode: tableQuery.componentCode || undefined,
    componentName: tableQuery.componentName || undefined,
    categoryName: tableQuery.categoryName || undefined,
    moduleCode: tableQuery.moduleCode || undefined,
    enabled: tableQuery.enabled,
    pageNum: query.pageNum,
    pageSize: query.pageSize,
  }).then((res: any) => ({
    records: res?.records || [],
    total: Number(res?.total || 0),
  }))
}

function handleReload() {
  tableRef.value?.refresh?.()
}

function handleSearch() {
  query.pageNum = 1
  handleReload()
}

function openCreate() {
  editingId.value = undefined
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
  if (scopeLevel === 'PUBLIC') return t('personalHomepage.library.scopePublic')
  if (scopeLevel === 'TENANT') return t('personalHomepage.library.scopeTenant')
  if (scopeLevel === 'USER') return t('personalHomepage.library.scopeUser')
  return scopeLevel || '-'
}

function getRowActions(record: HomepageComponentVO): ActionItem[] {
  const actions: ActionItem[] = [
    {
      key: 'edit',
      label: t('common.edit'),
      permission: 'sys:homepageComponent:edit',
      onClick: () => openEdit(record),
    },
  ]

  if (record.scopeLevel === 'PUBLIC') {
    actions.push({
      key: 'delete-public',
      label: t('common.delete'),
      permission: 'sys:homepageComponent:deletePublic',
      danger: true,
      onClick: () => handleDelete(record),
    })
  } else {
    actions.push({
      key: 'delete-tenant',
      label: t('common.delete'),
      permission: 'sys:homepageComponent:deleteTenant',
      danger: true,
      onClick: () => handleDelete(record),
    })
  }

  return actions
}

async function handleSubmit() {
  if (!formModel.componentCode || !formModel.componentName) {
    message.warning(t('system.homepageComponent.requiredFields'))
    return
  }
  saving.value = true
  try {
    await saveHomepageComponent({ ...formModel, id: editingId.value, scopeLevel: query.scopeLevel })
    dialogOpen.value = false
    handleReload()
  } finally {
    saving.value = false
  }
}

function handleDelete(record: HomepageComponentVO) {
  if (!record.id) return
  const deleteFn = record.scopeLevel === 'PUBLIC' ? deletePublicHomepageComponent : deleteTenantHomepageComponent
  Modal.confirm({
    title: t('system.homepageComponent.deleteConfirmTitle'),
    content: createVNode('div', null, `${record.componentName || ''}`),
    onOk: async () => {
      await deleteFn(record.id!)
      handleReload()
    },
  })
}

async function handlePullPublic() {
  const count = await pullPublicHomepageComponents()
  message.success(t('system.homepageComponent.pullPublicSuccess', { count: Number(count || 0) }))
  handleReload()
}

onMounted(() => {
  handleReload()
})
</script>

<style scoped lang="less" src="@/styles/views/system/homepageComponent/index.less"></style>
