<template>
  <div class="menu-container">
    <a-card :bordered="false" class="main-card">
      <div class="terminal-tabs">
        <a-tabs v-model:activeKey="activeTerminal" @change="handleTerminalChange">
          <a-tab-pane key="B" tab="B端" />
          <a-tab-pane key="C" tab="C端" />
        </a-tabs>
      </div>
      <div class="menu-layout">
        <div class="module-tabs">
          <a-tabs
            v-model:activeKey="activeModuleId"
            tab-position="left"
            @change="handleModuleChange"
          >
            <a-tab-pane
              v-for="module in modules"
              :key="String(module.id)"
              :tab="module.name"
            />
          </a-tabs>
        </div>

        <div class="content-area">
          <fx-dynamic-table
            ref="tableRef"
            table-code="MenuTable"
            :request="handleRequest"
            :fallback-config="fallbackConfig"
            :dict-options="dictOptions"
            :row-selection="{
              selectedRowKeys,
              onChange: handleSelectionChange
            }"
            :pagination="false"
            row-key="id"
            :default-expand-all-rows="true"
          >
            <template #toolbar>
              <a-space>
                <a-button
                  v-permission="'sys:menu:add'"
                  type="primary"
                  @click="handleAdd"
                >
                  <template #icon><PlusOutlined /></template>
                  {{ t('system.menu.form.addMenu') }}
                </a-button>

                <a-button
                  v-permission="'sys:menu:delete'"
                  danger
                  :disabled="selectedRowKeys.length === 0"
                  @click="handleBatchDelete"
                >
                  <template #icon><DeleteOutlined /></template>
                  {{ t('common.batchDelete') }}
                </a-button>
              </a-space>
            </template>

            <template #type="{ record }">
              <a-tag v-if="record.type === 'catalog'" color="blue">{{ t('system.menu.typeDirectory') }}</a-tag>
              <a-tag v-else-if="record.type === 'menu'" color="green">{{ t('system.menu.typeMenu') }}</a-tag>
              <a-tag v-else-if="record.type === 'button'" color="orange">{{ t('system.menu.typeButton') }}</a-tag>
              <span v-else>-</span>
            </template>

            <template #menuMode="{ record }">
              <a-tag v-if="record.menuMode === 'embedded'" color="blue">{{ t('system.menu.modeEmbedded') }}</a-tag>
              <a-tag v-else-if="record.menuMode === 'external'" color="purple">{{ t('system.menu.modeExternal') }}</a-tag>
              <span v-else>-</span>
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

            <template #icon="{ record }">
              <component v-if="record.icon" :is="getIcon(record.icon)" />
              <span v-else>-</span>
            </template>

            <template #action="{ record }">
              <a-space>
                <a
                  v-permission="'sys:menu:edit'"
                  @click="handleEdit(record)"
                >
                  {{ t('common.edit') }}
                </a>
                <a-divider type="vertical" />
                <a
                  v-permission="'sys:menu:delete'"
                  class="danger-link"
                  @click="handleDelete(record.id)"
                >
                  {{ t('common.delete') }}
                </a>
              </a-space>
            </template>
          </fx-dynamic-table>
        </div>
      </div>
    </a-card>

    <BaseFormDialog
      v-model:open="visible"
      :title="formTitle"
      :loading="submitLoading"
      width="800px"
      @submit="handleSubmit"
      @cancel="handleCancel"
    >
      <a-form
        ref="formRef"
        :model="formData"
        :rules="rules"
        :label-col="{ span: 6 }"
        :wrapper-col="{ span: 16 }"
      >
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item :label="t('system.menu.module')" name="moduleId">
              <a-select
                v-model:value="formData.moduleId"
                :placeholder="t('system.menu.form.modulePlaceholder')"
              >
                <a-select-option
                  v-for="module in modules"
                  :key="String(module.id)"
                  :value="String(module.id)"
                >
                  {{ module.name }}
                </a-select-option>
              </a-select>
            </a-form-item>
          </a-col>

          <a-col :span="12">
            <a-form-item :label="t('system.menu.parentMenu')" name="parentId">
              <a-tree-select
                v-model:value="formData.parentId"
                :tree-data="menuTreeData"
                :placeholder="t('system.menu.form.parentMenuPlaceholder')"
                tree-default-expand-all
                allow-clear
              />
            </a-form-item>
          </a-col>

          <a-col :span="12">
            <a-form-item :label="t('system.menu.menuType')" name="type">
              <a-select
                v-model:value="formData.type"
                :placeholder="t('system.menu.form.menuTypePlaceholder')"
                @change="handleTypeChange"
              >
                <a-select-option
                  v-for="item in menuTypeOptions"
                  :key="item.value"
                  :value="item.value"
                >
                  {{ item.label }}
                </a-select-option>
              </a-select>
            </a-form-item>
          </a-col>

          <a-col :span="24">
            <a-form-item :label="t('system.menu.menuName')" name="nameI18nJson">
              <I18nInput
                v-model="formData.nameI18nJson"
                mode="simple"
                :placeholder="t('system.menu.form.menuNamePlaceholder')"
              />
              <template #extra>
                <span style="color: #999; font-size: 12px;">
                  {{ t('system.menu.form.menuNameTip') }}
                </span>
              </template>
            </a-form-item>
          </a-col>

          <a-col v-if="showPath" :span="12">
            <a-form-item :label="t('system.menu.path')" name="path">
              <a-input
                v-model:value="formData.path"
                :placeholder="t('system.menu.form.pathPlaceholder')"
              />
            </a-form-item>
          </a-col>

          <a-col :span="12">
            <a-form-item :label="t('system.menu.icon')" name="icon">
              <IconPicker
                v-model:value="formData.icon"
                :placeholder="t('system.menu.form.iconPlaceholder')"
              />
            </a-form-item>
          </a-col>

          <a-col v-if="formData.type !== 'button'" :span="12">
            <a-form-item :label="t('system.menu.menuMode')" name="menuMode">
              <a-select
                v-model:value="formData.menuMode"
                :placeholder="t('system.menu.form.menuModePlaceholder')"
                @change="handleModeChange"
              >
                <a-select-option
                  v-for="item in menuModeOptions"
                  :key="item.value"
                  :value="item.value"
                >
                  {{ item.label }}
                </a-select-option>
              </a-select>
            </a-form-item>
          </a-col>

          <a-col v-if="showComponentKey" :span="12">
            <a-form-item :label="t('system.menu.componentKey')" name="componentKey">
              <a-input
                v-model:value="formData.componentKey"
                :placeholder="t('system.menu.form.componentKeyPlaceholder')"
              />
            </a-form-item>
          </a-col>

          <a-col v-if="showExternalUrl" :span="12">
            <a-form-item :label="t('system.menu.externalUrl')" name="externalUrl">
              <a-input
                v-model:value="formData.externalUrl"
                :placeholder="t('system.menu.form.externalUrlPlaceholder')"
              />
            </a-form-item>
          </a-col>

          <a-col v-if="showPermKey" :span="12">
            <a-form-item :label="t('system.menu.permission')" name="permKey">
              <a-input
                v-model:value="formData.permKey"
                :placeholder="t('system.menu.form.permissionPlaceholder')"
              />
            </a-form-item>
          </a-col>

          <a-col :span="12">
            <a-form-item :label="t('system.menu.sort')" name="orderNum">
              <a-input-number
                v-model:value="formData.orderNum"
                :min="0"
                style="width: 100%"
              />
            </a-form-item>
          </a-col>

          <a-col :span="12">
            <a-form-item :label="t('system.menu.visible')" name="visible">
              <a-radio-group v-model:value="formData.visible">
                <a-radio :value="true">{{ t('system.menu.visibleYes') }}</a-radio>
                <a-radio :value="false">{{ t('system.menu.visibleNo') }}</a-radio>
              </a-radio-group>
            </a-form-item>
          </a-col>

          <a-col :span="12">
            <a-form-item :label="t('system.menu.status')" name="status">
              <a-radio-group v-model:value="formData.status">
                <a-radio :value="true">{{ t('common.enabled') }}</a-radio>
                <a-radio :value="false">{{ t('common.disabled') }}</a-radio>
              </a-radio-group>
            </a-form-item>
          </a-col>
        </a-row>
      </a-form>
    </BaseFormDialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { DeleteOutlined, PlusOutlined } from '@ant-design/icons-vue'
import { message, Modal } from 'ant-design-vue'
import { useI18n } from 'vue-i18n'
import BaseFormDialog from '@/components/common/BaseFormDialog.vue'
import FxDynamicTable from '@/components/common/FxDynamicTable.vue'
import I18nInput from '@/components/common/I18nInput.vue'
import IconPicker from '@/components/common/IconPicker.vue'
import { listModules } from '@/api/system/module'
import {
  addMenu,
  addCMenu,
  batchDeleteCMenus,
  batchDeleteMenus,
  deleteCMenu,
  deleteMenu,
  getCMenuById,
  getCMenuTree,
  getMenuById,
  getMenuTree,
  updateCMenu,
  updateMenu,
} from '@/api/system/menu'
import { useDict } from '@/hooks/useDict'
import { getIcon } from '@/utils/icon'
import { getI18nValue } from '@/utils/i18n'

type MenuTreeRecord = {
  id?: string | number
  moduleId?: string | number
  parentId?: string | number
  type?: string
  menuMode?: string
  menuLevel?: number
  name?: string
  nameI18nJson?: string
  path?: string
  icon?: string
  componentKey?: string
  permKey?: string
  externalUrl?: string
  orderNum?: number
  visible?: boolean | number | string
  status?: boolean | number | string
  children?: MenuTreeRecord[]
}

type Menu表单Data = {
  id?: string | number
  moduleId: string
  parentId: string
  type: string
  menuLevel: number
  path: string
  name: string
  nameI18nJson: string
  icon?: string
  componentKey?: string
  permKey?: string
  menuMode: 'embedded' | 'external'
  externalUrl?: string
  orderNum: number
  visible: boolean
  status: boolean
}

const { t } = useI18n()

const activeTerminal = ref<'B' | 'C'>('B')
const modules = ref<any[]>([])
const activeModuleId = ref('')
const tableRef = ref()
const formRef = ref()
const loading = ref(false)
const visible = ref(false)
const submitLoading = ref(false)
const selectedRowKeys = ref<string[]>([])
const isEdit = ref(false)
const menuTreeData = ref<any[]>([])

const { dictItems: menuTypeOptions } = useDict('menu_type')
const { dictItems: menuModeOptions } = useDict('menu_mode')
const { dictItems: statusOptions } = useDict('status')

const dictOptions = computed(() => ({
  menu_type: menuTypeOptions.value,
  menu_mode: menuModeOptions.value,
  status: statusOptions.value,
}))

const fallbackConfig = computed(() => ({
  tableCode: 'MenuTable',
  tableName: t('system.menu.title'),
  tableType: 'NORMAL',
  rowKey: 'id',
  defaultPageSize: 20,
  columns: [
    { field: 'name', title: t('system.menu.menuName'), minWidth: 180, ellipsis: true },
    { field: 'type', title: t('system.menu.menuType'), width: 90 },
    { field: 'path', title: t('system.menu.path'), minWidth: 180, ellipsis: true },
    { field: 'permKey', title: t('system.menu.permission'), minWidth: 180, ellipsis: true },
    { field: 'menuMode', title: t('system.menu.menuMode'), width: 110 },
    { field: 'status', title: t('system.menu.status'), width: 90, dictCode: 'status' },
    { field: 'orderNum', title: t('system.menu.sort'), width: 80 },
    { field: 'action', title: t('common.action'), width: 140 },
  ],
  queryFields: [
    { field: 'name', label: t('system.menu.menuName'), queryType: 'input', queryOperator: 'like' },
    { field: 'status', label: t('system.menu.status'), queryType: 'select', queryOperator: 'eq', dictCode: 'status' },
  ],
  version: 1,
}))

const formTitle = computed(() => (
  isEdit.value ? t('system.menu.form.editMenu') : t('system.menu.form.addMenu')
))

const currentMenuApi = computed(() => (
  activeTerminal.value === 'C'
    ? {
        getTree: getCMenuTree,
        getDetail: getCMenuById,
        add: addCMenu,
        update: updateCMenu,
        remove: deleteCMenu,
        batchRemove: batchDeleteCMenus,
      }
    : {
        getTree: getMenuTree,
        getDetail: getMenuById,
        add: addMenu,
        update: updateMenu,
        remove: deleteMenu,
        batchRemove: batchDeleteMenus,
      }
))

function getRootTreeNode(children: any[] = []) {
  return [{
    key: '0',
    title: t('system.menu.rootMenu'),
    value: '0',
    children,
  }]
}

function createDefault表单(moduleId = activeModuleId.value): Menu表单Data {
  return {
    id: undefined,
    moduleId: moduleId || '',
    parentId: '0',
    type: 'menu',
    menuLevel: 1,
    path: '',
    name: '',
    nameI18nJson: '',
    icon: undefined,
    componentKey: undefined,
    permKey: undefined,
    menuMode: 'embedded',
    externalUrl: undefined,
    orderNum: 0,
    visible: true,
    status: true,
  }
}

const formData = ref<Menu表单Data>(createDefault表单())

const showPath = computed(() => formData.value.type !== 'button')
const showComponentKey = computed(() => formData.value.type === 'menu' && formData.value.menuMode === 'embedded')
const showPermKey = computed(() => formData.value.type === 'menu' || formData.value.type === 'button')
const showExternalUrl = computed(() => formData.value.type === 'menu' && formData.value.menuMode === 'external')

const rules = computed(() => ({
  moduleId: [{ required: true, message: t('system.menu.form.modulePlaceholder'), trigger: 'change' }],
  type: [{ required: true, message: t('system.menu.form.menuTypePlaceholder'), trigger: 'change' }],
  nameI18nJson: [
    {
      validator: async (_rule: unknown, value: string) => {
        if (!value || value === '{}' || !String(value).trim()) {
          throw new Error(t('system.menu.form.nameRequired'))
        }
      },
      trigger: 'change',
    },
  ],
}))

function normalizeBoolean(value: unknown): boolean | undefined {
  if (value === undefined || value === null || value === '') {
    return undefined
  }
  if (value === true || value === 1 || value === '1') {
    return true
  }
  if (value === false || value === 0 || value === '0') {
    return false
  }
  return Boolean(value)
}

function normalize状态ForTag(value: unknown): number {
  return normalizeBoolean(value) ? 1 : 0
}

function resolve状态Tag(value: unknown) {
  const normalizedValue = normalize状态ForTag(value)
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

function cloneTree(nodes: MenuTreeRecord[] = []): MenuTreeRecord[] {
  return nodes.map((node) => ({
    ...node,
    children: cloneTree(node.children || []),
  }))
}

function countTreeNodes(nodes: MenuTreeRecord[] = []): number {
  return nodes.reduce((count, node) => count + 1 + countTreeNodes(node.children || []), 0)
}

function matchMenuName(node: MenuTreeRecord, keyword?: string) {
  if (!keyword) {
    return true
  }
  const text = keyword.trim().toLowerCase()
  if (!text) {
    return true
  }
  const displayName = getI18nValue(node.nameI18nJson, node.name)
  return [node.name, node.nameI18nJson, displayName]
    .filter(Boolean)
    .some((item) => String(item).toLowerCase().includes(text))
}

function filterMenuTree(nodes: MenuTreeRecord[] = [], filters: Record<string, any> = {}): MenuTreeRecord[] {
  const expected状态 = normalizeBoolean(filters.status)

  return nodes.reduce<MenuTreeRecord[]>((result, node) => {
    const selfMatched = matchMenuName(node, filters.name)
      && (expected状态 === undefined || normalizeBoolean(node.status) === expected状态)

    if (selfMatched) {
      result.push({
        ...node,
        children: cloneTree(node.children || []),
      })
      return result
    }

    const children = filterMenuTree(node.children || [], filters)
    if (children.length > 0) {
      result.push({
        ...node,
        children,
      })
    }
    return result
  }, [])
}

function normalizeTreeRows(nodes: MenuTreeRecord[] = []): MenuTreeRecord[] {
  return nodes.map((node) => ({
    ...node,
    id: node.id != null ? String(node.id) : node.id,
    moduleId: node.moduleId != null ? String(node.moduleId) : node.moduleId,
    parentId: node.parentId != null ? String(node.parentId) : '0',
    name: getI18nValue(node.nameI18nJson, node.name),
    status: normalize状态ForTag(node.status),
    children: normalizeTreeRows(node.children || []),
  }))
}

async function handleRequest(payload: {
  page: { current: number; pageSize: number }
  query: Record<string, any>
  sorter?: { field?: string; order?: string }
}) {
  const tenantId = sessionStorage.getItem('tenantId')
  if (!tenantId || !activeModuleId.value) {
    return { records: [], total: 0 }
  }

  loading.value = true
  try {
    const tree = await currentMenuApi.value.getTree({
      tenantId: Number(tenantId),
      moduleId: Number(activeModuleId.value),
    })

    const filteredTree = filterMenuTree(tree || [], payload.query || {})
    return {
      records: normalizeTreeRows(filteredTree),
      total: countTreeNodes(filteredTree),
    }
  } catch (error) {
    console.error('load menu list failed:', error)
    message.error(t('system.menu.message.loadListFailed'))
    return { records: [], total: 0 }
  } finally {
    loading.value = false
  }
}

function handleSelectionChange(keys: Array<string | number>) {
  selectedRowKeys.value = keys.map((item) => String(item))
}

function removeExcludedNodes(nodes: MenuTreeRecord[] = [], excludeId?: string): MenuTreeRecord[] {
  return nodes
    .filter((node) => !excludeId || String(node.id) !== String(excludeId))
    .map((node) => ({
      ...node,
      children: removeExcludedNodes(node.children || [], excludeId),
    }))
}

function buildTreeSelectOptions(nodes: MenuTreeRecord[] = []): any[] {
  return nodes
    .filter((node) => node.type !== 'button')
    .map((node) => ({
      key: String(node.id),
      title: getI18nValue(node.nameI18nJson, node.name),
      value: String(node.id),
      children: buildTreeSelectOptions(node.children || []),
    }))
}

async function loadParentMenuTree(moduleId: string, excludeId?: string) {
  const tenantId = sessionStorage.getItem('tenantId')
  if (!tenantId || !moduleId) {
    menuTreeData.value = getRootTreeNode()
    return
  }

  try {
    const tree = await currentMenuApi.value.getTree({
      tenantId: Number(tenantId),
      moduleId: Number(moduleId),
    })
    const filteredTree = removeExcludedNodes(tree || [], excludeId)
    menuTreeData.value = getRootTreeNode(buildTreeSelectOptions(filteredTree))
  } catch (error) {
    console.error('load parent menu failed:', error)
    message.error(t('system.menu.message.loadParentFailed'))
    menuTreeData.value = getRootTreeNode()
  }
}

function resolveMenuNameForSave(nameI18nJson: string, 降级方案Name = '') {
  const resolved = getI18nValue(nameI18nJson, 降级方案Name)
  if (resolved && String(resolved).trim()) {
    return String(resolved).trim()
  }
  try {
    const parsed = JSON.parse(nameI18nJson || '{}')
    const firstText = Object.values(parsed).find((item) => String(item || '').trim())
    return firstText ? String(firstText).trim() : ''
  } catch {
    return 降级方案Name?.trim?.() || ''
  }
}

function findNodeLevel(nodes: any[] = [], targetId: string, level = 1): number | undefined {
  for (const node of nodes) {
    if (String(node.value) === String(targetId)) {
      return level
    }
    const childLevel = findNodeLevel(node.children || [], targetId, level + 1)
    if (childLevel !== undefined) {
      return childLevel
    }
  }
  return undefined
}

function calculateMenuLevel(parentId: string) {
  if (!parentId || parentId === '0') {
    return 1
  }
  const level = findNodeLevel(menuTreeData.value?.[0]?.children || [], parentId, 1)
  return (level || 0) + 1
}

async function handleAdd() {
  isEdit.value = false
  formData.value = createDefault表单()
  visible.value = true
  await loadParentMenuTree(formData.value.moduleId)
}

async function handleEdit(record: MenuTreeRecord) {
  try {
    const detail = await currentMenuApi.value.getDetail(String(record.id))
    isEdit.value = true
    formData.value = {
      ...createDefault表单(String(detail?.moduleId || activeModuleId.value)),
      ...detail,
      id: detail?.id != null ? String(detail.id) : record.id,
      moduleId: String(detail?.moduleId || activeModuleId.value),
      parentId: String(detail?.parentId ?? '0'),
      visible: normalizeBoolean(detail?.visible) !== false,
      status: normalizeBoolean(detail?.status) !== false,
      nameI18nJson: detail?.nameI18nJson || '',
    }
    visible.value = true
    await loadParentMenuTree(formData.value.moduleId, String(record.id))
  } catch (error) {
    console.error('load menu detail failed:', error)
    message.error(t('system.menu.message.loadDetailFailed'))
  }
}

function handleTypeChange() {
  if (formData.value.type === 'button') {
    formData.value.path = ''
    formData.value.componentKey = undefined
    formData.value.menuMode = 'embedded'
    formData.value.externalUrl = undefined
  }

  if (formData.value.type === 'catalog') {
    formData.value.componentKey = undefined
    formData.value.externalUrl = undefined
  }
}

function handleModeChange() {
  if (formData.value.menuMode === 'external') {
    formData.value.componentKey = undefined
  } else {
    formData.value.externalUrl = undefined
  }
}

function handleCancel() {
  visible.value = false
  isEdit.value = false
  formData.value = createDefault表单()
}

async function handleSubmit() {
  const tenantId = sessionStorage.getItem('tenantId')
  if (!tenantId) {
    message.error(t('system.menu.message.missingTenant'))
    return
  }

  try {
    await formRef.value?.validate()
    submitLoading.value = true

    const name = resolveMenuNameForSave(formData.value.nameI18nJson, formData.value.name)
    if (!name) {
      message.warning(t('system.menu.form.nameRequired'))
      return
    }

    const payload = {
      ...formData.value,
      id: formData.value.id ? Number(formData.value.id) : undefined,
      tenantId: Number(tenantId),
      moduleId: Number(formData.value.moduleId),
      parentId: !formData.value.parentId || formData.value.parentId === '0'
        ? 0
        : Number(formData.value.parentId),
      menuLevel: calculateMenuLevel(formData.value.parentId),
      name,
      path: showPath.value ? formData.value.path : '',
      componentKey: showComponentKey.value ? formData.value.componentKey : undefined,
      externalUrl: showExternalUrl.value ? formData.value.externalUrl : undefined,
      permKey: showPermKey.value ? formData.value.permKey : undefined,
      visible: !!formData.value.visible,
      status: !!formData.value.status,
    }

    if (payload.id) {
      await currentMenuApi.value.update(payload as any)
      // 鎴愬姛鎻愮ず鐢卞悗绔繑鍥烇紝鍦?http 鎷︽埅鍣ㄤ腑缁熶竴澶勭悊
    } else {
      await currentMenuApi.value.add(payload as any)
      // 鎴愬姛鎻愮ず鐢卞悗绔繑鍥烇紝鍦?http 鎷︽埅鍣ㄤ腑缁熶竴澶勭悊
    }

    visible.value = false
    isEdit.value = false
    formData.value = createDefault表单()
    await tableRef.value?.refresh?.()
  } catch (error: any) {
    if (error?.errorFields) {
      return
    }
    console.error('submit menu failed:', error)
    message.error(t('system.menu.message.submitFailed'))
  } finally {
    submitLoading.value = false
  }
}

function handleDelete(id: string | number) {
  Modal.confirm({
    title: t('common.confirmDelete'),
    content: t('system.menu.message.deleteConfirm'),
    okText: t('common.confirm'),
    cancelText: t('common.cancel'),
    onOk: async () => {
      try {
        await currentMenuApi.value.remove(String(id))
        selectedRowKeys.value = selectedRowKeys.value.filter((item) => item !== String(id))
        // 鎴愬姛鎻愮ず鐢卞悗绔繑鍥烇紝鍦?http 鎷︽埅鍣ㄤ腑缁熶竴澶勭悊
        await tableRef.value?.refresh?.()
      } catch (error) {
        console.error('delete menu failed:', error)
        message.error(t('system.menu.message.deleteFailed'))
      }
    },
  })
}

function handleBatchDelete() {
  if (selectedRowKeys.value.length === 0) {
    message.warning(t('system.menu.message.selectToDelete'))
    return
  }

  Modal.confirm({
    title: t('common.confirmBatchDelete'),
    content: t('system.menu.message.batchDeleteConfirm', { count: selectedRowKeys.value.length }),
    okText: t('common.confirm'),
    cancelText: t('common.cancel'),
    onOk: async () => {
      try {
        await currentMenuApi.value.batchRemove(selectedRowKeys.value)
        selectedRowKeys.value = []
        // 鎴愬姛鎻愮ず鐢卞悗绔繑鍥烇紝鍦?http 鎷︽埅鍣ㄤ腑缁熶竴澶勭悊
        await tableRef.value?.refresh?.()
      } catch (error) {
        console.error('batch delete menu failed:', error)
        message.error(t('system.menu.message.batchDeleteFailed'))
      }
    },
  })
}

async function loadModules() {
  try {
    const tenantId = sessionStorage.getItem('tenantId')
    if (!tenantId) {
      return
    }
    const res = await listModules({ tenantId: Number(tenantId) })
    modules.value = res || []
    if (modules.value.length > 0) {
      activeModuleId.value = String(modules.value[0].id)
      await tableRef.value?.refresh?.()
    }
  } catch (error) {
    console.error('load modules failed:', error)
    message.error(t('system.menu.message.loadModulesFailed'))
  }
}

async function handleModuleChange(moduleId: string) {
  activeModuleId.value = moduleId
  selectedRowKeys.value = []
  await tableRef.value?.refresh?.()
}

async function handleTerminalChange(terminal: string) {
  activeTerminal.value = terminal === 'C' ? 'C' : 'B'
  selectedRowKeys.value = []
  visible.value = false
  formData.value = createDefault表单(activeModuleId.value)
  await tableRef.value?.refresh?.()
}

watch(
  () => formData.value.moduleId,
  async (moduleId) => {
    if (!visible.value || !moduleId) {
      return
    }
    await loadParentMenuTree(moduleId, formData.value.id ? String(formData.value.id) : undefined)
  },
)

watch(
  () => t('system.menu.rootMenu'),
  () => {
    if (visible.value) {
      loadParentMenuTree(formData.value.moduleId, formData.value.id ? String(formData.value.id) : undefined)
    } else {
      menuTreeData.value = getRootTreeNode()
    }
  },
)

onMounted(async () => {
  menuTreeData.value = getRootTreeNode()
  await loadModules()
})
</script>

<style scoped lang="less">
.menu-container {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
  padding: 16px;
  box-sizing: border-box;

  .main-card {
    display: flex;
    flex-direction: column;
    flex: 1 1 auto;
    min-height: 0;

    :deep(.ant-card-body) {
      padding: 0;
      display: flex;
      flex-direction: column;
      flex: 1 1 auto;
      min-height: 0;
      height: 100%;
    }
  }

  .terminal-tabs {
    padding: 12px 16px 0;
    border-bottom: 1px solid var(--fx-border-color);
    background: var(--fx-bg-container);
  }

  .menu-layout {
    display: flex;
    flex: 1 1 auto;
    width: 100%;
    min-width: 0;
    min-height: 0;

    .module-tabs {
      width: 140px;
      flex-shrink: 0;
      border-right: 1px solid var(--fx-border-color);
      background: var(--fx-bg-container);
      min-height: 0;

      :deep(.ant-tabs) {
        height: 100%;

        .ant-tabs-nav {
          margin: 0;
          width: 100%;
        }

        .ant-tabs-nav-list {
          width: 100%;
        }

        .ant-tabs-tab {
          padding: 12px 24px;
          margin: 0;
          width: 100%;
          justify-content: flex-start;
          transition: all 0.3s;

          &:hover {
            background: var(--fx-tab-hover-bg);
          }

          &.ant-tabs-tab-active {
            background: var(--fx-tab-bg);

            .ant-tabs-tab-btn {
              color: var(--fx-theme-color, #1677ff);
            }
          }
        }
      }
    }

    .content-area {
      flex: 1;
      min-width: 0;
      overflow: hidden;
      padding: 12px 16px 8px;
      background: var(--fx-bg-container);
      display: flex;
      flex-direction: column;
      min-height: 0;

      :deep(.fx-dynamic-table) {
        flex: 1 1 auto;
        min-height: 0;
      }
    }
  }

  .danger-link {
    color: #ff4d4f;

    &:hover {
      color: #ff7875;
    }
  }
}
</style>
