<template>
  <div class="system-help-resource-page">
    <div class="help-page-header">
      <div>
        <h1>{{ t('system.helpResource.pageTitle') }}</h1>
        <p>{{ t('system.helpResource.pageDesc') }}</p>
      </div>
    </div>

    <a-tabs v-model:activeKey="activeTab">
      <a-tab-pane key="MANUAL" :tab="t('system.helpResource.manualTab')" />
      <a-tab-pane key="VIDEO" :tab="t('system.helpResource.videoTab')" />
      <a-tab-pane key="contact" :tab="t('system.helpResource.contactTab')" />
    </a-tabs>

    <FxDynamicTable
      v-if="activeTab !== 'contact'"
      :key="activeTab"
      ref="tableRef"
      table-code="SystemHelpResourceTable"
      row-key="id"
      :request="handleRequest"
      :row-selection="rowSelection"
    >
      <template #toolbar>
        <a-space wrap>
          <a-button v-permission="'sys:help:add'" type="primary" @click="openCreate">
            {{ t('common.add') }}
          </a-button>
          <a-button v-permission="'sys:help:add'" :loading="batchUploading" @click="batchUploadDialogVisible = true">
            {{ t('system.helpResource.batchUpload') }}
          </a-button>
          <a-button
            v-permission="'sys:help:delete'"
            danger
            :disabled="!selectedCount"
            @click="handleBatchDelete"
          >
            {{ t('common.batchDelete') }}
          </a-button>
        </a-space>
      </template>
      <template #docType="{ record }">
        <a-tag>{{ record.docType === 'VIDEO' ? t('system.helpResource.videoTab') : t('system.helpResource.manualTab') }}</a-tag>
      </template>
      <template #scopeType="{ record }">
        <a-tag :color="record.scopeType === 'MENU' ? 'blue' : 'default'">
          {{ record.scopeType === 'MENU' ? t('layout.help.scopeMenu') : t('layout.help.scopeGlobal') }}
        </a-tag>
      </template>
      <template #sourceType="{ record }">
        <a-tag>{{ record.sourceType === 'EXTERNAL_URL' ? t('layout.help.sourceExternal') : t('layout.help.sourceFile') }}</a-tag>
      </template>
      <template #fileExt="{ record }">
        {{ formatFileExt(record) }}
      </template>
      <template #fileSize="{ record }">
        {{ formatFileSize(record.fileSize) }}
      </template>
      <template #status="{ record }">
        <a-tag :color="record.status === 1 ? 'green' : 'default'">
          {{ record.status === 1 ? t('common.enabled') : t('common.disabled') }}
        </a-tag>
      </template>
      <template #action="{ record }">
        <a-space>
          <a @click="openPreview(record)">{{ t('system.helpResource.preview') }}</a>
          <a v-permission="'sys:help:edit'" @click="openEdit(record)">{{ t('common.edit') }}</a>
          <a v-permission="'sys:help:edit'" @click="toggleStatus(record)">
            {{ record.status === 1 ? t('common.disable') : t('common.enable') }}
          </a>
          <a v-permission="'sys:help:delete'" class="danger-link" @click="removeRecord(record)">{{ t('common.delete') }}</a>
        </a-space>
      </template>
    </FxDynamicTable>

    <a-card v-else class="help-contact-card">
      <a-row :gutter="32">
        <a-col :xs="24" :md="14">
          <a-form layout="vertical" :model="contactForm">
            <a-form-item :label="t('layout.help.phone')">
              <a-input v-model:value="contactForm.phone" />
            </a-form-item>
            <a-form-item :label="t('layout.help.email')">
              <a-input v-model:value="contactForm.email" />
            </a-form-item>
            <a-form-item :label="t('layout.help.workTime')">
              <a-input v-model:value="contactForm.workTime" />
            </a-form-item>
            <a-form-item :label="t('layout.help.address')">
              <a-input v-model:value="contactForm.address" />
            </a-form-item>
            <a-form-item :label="t('layout.help.remark')">
              <a-textarea v-model:value="contactForm.remark" :rows="3" />
            </a-form-item>
            <a-button v-permission="'sys:help:contact:edit'" type="primary" :loading="savingContact" @click="saveContact">
              {{ t('common.save') }}
            </a-button>
          </a-form>
        </a-col>
        <a-col :xs="24" :md="10">
          <div class="help-qr-panel">
            <div class="help-qr-panel__label">{{ t('layout.help.wechatQr') }}</div>
            <a-button v-permission="'sys:help:contact:edit'" @click="qrUploadDialogVisible = true">
              {{ t('system.helpResource.uploadQr') }}
            </a-button>
            <img v-if="qrPreview" :src="qrPreview" class="help-qr-preview" />
          </div>
        </a-col>
      </a-row>
    </a-card>

    <BatchFileUploadDialog
      v-model:open="batchUploadDialogVisible"
      :title="t('system.helpResource.batchUpload')"
      :accept="batchAccept"
      :loading="batchUploading"
      @submit="onBatchUpload"
    />

    <BatchFileUploadDialog
      v-model:open="singleFileUploadDialogVisible"
      :title="t('system.helpResource.uploadFile')"
      :accept="batchAccept"
      :multiple="false"
      :max-count="1"
      :loading="singleFileUploading"
      :ok-text="t('common.confirm')"
      @submit="onSingleFileUpload"
    />

    <BatchFileUploadDialog
      v-model:open="qrUploadDialogVisible"
      :title="t('system.helpResource.uploadQr')"
      accept=".jpg,.jpeg,.png"
      :multiple="false"
      :max-count="1"
      :loading="qrUploading"
      :ok-text="t('common.confirm')"
      @submit="onQrUpload"
    />

    <BaseFormDialog
      v-model:open="dialogVisible"
      :title="editingId ? t('common.edit') : t('common.add')"
      :width="720"
      :loading="saving"
      @submit="handleSave"
    >
      <a-form layout="vertical" :model="form">
        <a-form-item :label="t('system.helpResource.title')" required>
          <a-input v-model:value="form.title" />
        </a-form-item>
        <a-form-item :label="t('system.helpResource.scopeType')" required>
          <a-radio-group v-model:value="form.scopeType">
            <a-radio value="GLOBAL">{{ t('layout.help.scopeGlobal') }}</a-radio>
            <a-radio value="MENU">{{ t('layout.help.scopeMenu') }}</a-radio>
          </a-radio-group>
        </a-form-item>
        <a-form-item v-if="form.scopeType === 'MENU'" :label="t('system.helpResource.menu')" required>
          <a-tree-select
            v-model:value="form.menuId"
            :tree-data="menuTreeData"
            allow-clear
            tree-default-expand-all
            :placeholder="t('system.helpResource.menuPlaceholder')"
            @change="onMenuChange"
          />
        </a-form-item>
        <a-form-item :label="t('system.helpResource.sourceType')" required>
          <a-radio-group v-model:value="form.sourceType">
            <a-radio value="FILE">{{ t('layout.help.sourceFile') }}</a-radio>
            <a-radio value="EXTERNAL_URL">{{ t('layout.help.sourceExternal') }}</a-radio>
          </a-radio-group>
        </a-form-item>
        <a-form-item v-if="form.sourceType === 'FILE'" :label="t('system.helpResource.file')" required>
          <a-button @click="singleFileUploadDialogVisible = true">{{ t('system.helpResource.uploadFile') }}</a-button>
          <div v-if="form.fileName" class="help-file-name">{{ form.fileName }}</div>
        </a-form-item>
        <a-form-item v-else :label="t('system.helpResource.externalUrl')" required>
          <a-input v-model:value="form.externalUrl" />
        </a-form-item>
        <a-form-item :label="t('system.helpResource.sortOrder')">
          <a-input-number v-model:value="form.sortOrder" :min="0" style="width: 100%" />
        </a-form-item>
        <a-form-item :label="t('common.status')">
          <a-radio-group v-model:value="form.status">
            <a-radio :value="1">{{ t('common.enabled') }}</a-radio>
            <a-radio :value="0">{{ t('common.disabled') }}</a-radio>
          </a-radio-group>
        </a-form-item>
        <a-form-item :label="t('common.remark')">
          <a-textarea v-model:value="form.remark" :rows="2" />
        </a-form-item>
      </a-form>
    </BaseFormDialog>

    <a-modal
      v-model:open="previewVisible"
      :title="previewRecord?.title || t('system.helpResource.preview')"
      :width="1080"
      :footer="null"
      destroy-on-close
    >
      <div class="help-preview-wrap">
        <FilePreview
          v-if="previewRecord"
          :key="`${previewRecord.id || previewRecord.title}-${previewRecord.fileUrl || previewRecord.externalUrl || ''}`"
          :file-url="previewRecord.fileUrl"
          :file-ext="previewRecord.fileExt"
          :file-name="previewRecord.fileName"
          :source-type="previewRecord.sourceType"
          :external-url="previewRecord.externalUrl"
        />
      </div>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
/**
 * 帮助中心维护页。
 * <p>
 * 手册 / 视频走 {@code SystemHelpResourceTable}；联系我们读写 {@code system.help.contact}。
 * 上传只走帮助独立接口，不复用 {@code /sys/file/upload}。
 * 支持单条预览、批量上传和批量删除。
 * </p>
 *
 * @author Forgex Team
 * @version 1.1.0
 * @see helpResourceApi
 * @see FilePreview
 * @see useBatchTableSelection
 */
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { Modal, message } from 'ant-design-vue'
import { useI18n } from 'vue-i18n'
import { useRoute } from 'vue-router'
import BatchFileUploadDialog from '@/components/common/BatchFileUploadDialog.vue'
import BaseFormDialog from '@/components/common/BaseFormDialog.vue'
import FilePreview from '@/components/common/FilePreview.vue'
import FxDynamicTable from '@/components/common/FxDynamicTable.vue'
import { getMenuTree } from '@/api/system/menu'
import { helpResourceApi, type HelpDocType, type SysHelpContact, type SysHelpResource } from '@/api/system/helpResource'
import { useBatchTableSelection } from '@/hooks/useBatchTableSelection'
import { usePermissionStore } from '@/stores/permission'
import { normalizeMediaUrl } from '@/utils/media'
import { buildModuleMenuPath } from '@/utils/workspacePath'

const { t } = useI18n({ useScope: 'global' })
const route = useRoute()
const permissionStore = usePermissionStore()
const tableRef = ref()
const activeTab = ref<'MANUAL' | 'VIDEO' | 'contact'>('MANUAL')
const batchUploadDialogVisible = ref(false)
const singleFileUploadDialogVisible = ref(false)
const qrUploadDialogVisible = ref(false)
const dialogVisible = ref(false)
const previewVisible = ref(false)
const previewRecord = ref<SysHelpResource>()
const saving = ref(false)
const savingContact = ref(false)
const batchUploading = ref(false)
const singleFileUploading = ref(false)
const qrUploading = ref(false)
const editingId = ref<number>()
const menuTreeData = ref<any[]>([])
const menuPathMap = ref<Record<number, string>>({})
const { selectedRowKeys, selectedCount, rowSelection, clearSelection } = useBatchTableSelection<number>()
const contactForm = reactive<SysHelpContact>({
  phone: '',
  email: '',
  wechatQrUrl: '',
  address: '',
  workTime: '',
  remark: '',
})
const form = reactive<SysHelpResource>({
  title: '',
  docType: 'MANUAL',
  scopeType: 'GLOBAL',
  sourceType: 'FILE',
  status: 1,
  sortOrder: 0,
})

const qrPreview = computed(() => normalizeMediaUrl(contactForm.wechatQrUrl) || contactForm.wechatQrUrl || '')
const batchAccept = computed(() => (activeTab.value === 'VIDEO' ? '.mp4' : '.pdf,.doc,.docx,.xls,.xlsx,.txt,.md'))

watch(activeTab, (tab) => {
  clearSelection()
  if (tab === 'contact') {
    void loadContact()
  }
})

watch(
  () => form.scopeType,
  (scope) => {
    if (scope === 'GLOBAL') {
      form.menuId = undefined
      form.menuPath = ''
    }
  }
)

onMounted(async () => {
  const queryTab = String(route.query.tab || '')
  const queryType = String(route.query.docType || '').toUpperCase()
  if (queryTab === 'contact') {
    activeTab.value = 'contact'
  } else if (queryType === 'VIDEO') {
    activeTab.value = 'VIDEO'
  }
  await Promise.all([loadMenuTree(), loadContact()])
  if (route.query.menuPath) {
    form.scopeType = 'MENU'
    form.menuPath = String(route.query.menuPath)
  }
})

/**
 * 动态表格分页请求，强制带当前 Tab 的文档类型。
 *
 * @param payload FxDynamicTable 查询载荷
 * @returns 当前页记录与总数
 */
async function handleRequest(payload: { page: { current: number; pageSize: number }; query: Record<string, any> }) {
  const query = payload.query || {}
  const result = await helpResourceApi.page({
    pageNum: payload.page.current,
    pageSize: payload.page.pageSize,
    title: query.title,
    docType: activeTab.value as HelpDocType,
    scopeType: query.scopeType,
    sourceType: query.sourceType,
    menuPath: query.menuPath,
    status: query.status,
  })
  return {
    records: result.records || [],
    total: result.total || 0,
  }
}

/**
 * 重置新增 / 编辑表单。
 */
function resetForm() {
  editingId.value = undefined
  Object.assign(form, {
    id: undefined,
    title: '',
    docType: activeTab.value === 'VIDEO' ? 'VIDEO' : 'MANUAL',
    scopeType: 'GLOBAL',
    sourceType: 'FILE',
    menuId: undefined,
    menuPath: '',
    fileName: '',
    fileUrl: '',
    fileExt: '',
    fileSize: undefined,
    contentType: '',
    externalUrl: '',
    status: 1,
    sortOrder: 0,
    remark: '',
  })
}

/**
 * 打开新增弹窗。
 */
function openCreate() {
  resetForm()
  dialogVisible.value = true
}

/**
 * 打开编辑弹窗并回填记录。
 *
 * @param record 当前行
 */
function openEdit(record: SysHelpResource) {
  resetForm()
  editingId.value = record.id
  Object.assign(form, record)
  dialogVisible.value = true
}

/**
 * 打开单条预览弹窗，复用 {@link FilePreview}。
 *
 * @param record 当前行
 */
function openPreview(record: SysHelpResource) {
  previewRecord.value = record
  previewVisible.value = true
}

/**
 * 菜单树选中后写入解析后的工作区全路径。
 *
 * @param value 菜单 ID
 */
function onMenuChange(value: number) {
  form.menuId = value
  form.menuPath = menuPathMap.value[value] || ''
}

/**
 * 单文件选择弹窗确认后，先拿 URL 再随表单提交。
 *
 * @param files 已选择的文件
 */
async function onSingleFileUpload(files: File[]) {
  const file = files[0]
  if (!file) {
    return
  }
  singleFileUploading.value = true
  try {
    const result = await helpResourceApi.upload(file, form.docType)
    form.fileName = result.fileName
    form.fileUrl = result.fileUrl
    form.fileExt = result.fileExt
    form.fileSize = result.fileSize
    form.contentType = result.contentType
    singleFileUploadDialogVisible.value = false
  } finally {
    singleFileUploading.value = false
  }
}

/**
 * 二维码选择弹窗确认后调用既有上传接口。
 *
 * @param files 已选择的文件
 */
async function onQrUpload(files: File[]) {
  const file = files[0]
  if (!file) {
    return
  }
  qrUploading.value = true
  try {
    const result = await helpResourceApi.uploadQr(file)
    contactForm.wechatQrUrl = result.fileUrl
    qrUploadDialogVisible.value = false
  } finally {
    qrUploading.value = false
  }
}

/**
 * 批量上传弹窗确认后，每个文件先走独立上传接口，再按文件名创建元数据。
 *
 * @param files 已选择的文件
 */
async function onBatchUpload(files: File[]) {
  if (!files.length) {
    return
  }
  batchUploading.value = true
  let success = 0
  let failed = 0
  const docType = (activeTab.value === 'VIDEO' ? 'VIDEO' : 'MANUAL') as HelpDocType
  try {
    for (const file of files) {
      try {
        const uploaded = await helpResourceApi.upload(file, docType)
        await helpResourceApi.createQuiet({
          title: titleFromFileName(file.name),
          docType,
          scopeType: 'GLOBAL',
          sourceType: 'FILE',
          fileName: uploaded.fileName,
          fileUrl: uploaded.fileUrl,
          fileExt: uploaded.fileExt,
          fileSize: uploaded.fileSize,
          contentType: uploaded.contentType,
          status: 1,
          sortOrder: 0,
        })
        success += 1
      } catch {
        failed += 1
      }
    }
    if (failed === 0) {
      message.success(t('system.helpResource.batchUploadSuccess', { count: success }))
    } else {
      message.warning(t('system.helpResource.batchUploadPartial', { success, failed }))
    }
    tableRef.value?.reload?.()
    batchUploadDialogVisible.value = false
  } finally {
    batchUploading.value = false
  }
}

/**
 * 批量删除选中行。
 */
function handleBatchDelete() {
  const keys = selectedRowKeys.value
  if (!keys.length) {
    return
  }
  Modal.confirm({
    title: t('system.helpResource.batchDeleteConfirm', { count: keys.length }),
    onOk: async () => {
      await helpResourceApi.batchDelete(keys.map(item => Number(item)))
      clearSelection()
      tableRef.value?.reload?.()
    },
  })
}

/**
 * 保存新增或编辑。
 */
async function handleSave() {
  saving.value = true
  try {
    const payload = { ...form, docType: (activeTab.value === 'VIDEO' ? 'VIDEO' : 'MANUAL') as HelpDocType }
    if (payload.scopeType === 'GLOBAL') {
      payload.menuId = null
      payload.menuPath = ''
    }
    if (editingId.value) {
      await helpResourceApi.update({ ...payload, id: editingId.value })
    } else {
      await helpResourceApi.create(payload)
    }
    dialogVisible.value = false
    tableRef.value?.reload?.()
  } finally {
    saving.value = false
  }
}

/**
 * 切换启用状态。
 *
 * @param record 当前行
 */
async function toggleStatus(record: SysHelpResource) {
  if (!record.id) {
    return
  }
  await helpResourceApi.changeStatus(record.id, record.status === 1 ? 0 : 1)
  tableRef.value?.reload?.()
}

/**
 * 删除单条资源。
 *
 * @param record 当前行
 */
function removeRecord(record: SysHelpResource) {
  Modal.confirm({
    title: t('common.confirmDelete'),
    onOk: async () => {
      if (record.id) {
        await helpResourceApi.delete(record.id)
        tableRef.value?.reload?.()
      }
    },
  })
}

/**
 * 读取联系我们配置。
 */
async function loadContact() {
  const data = await helpResourceApi.getContact()
  Object.assign(contactForm, data || {})
}

/**
 * 保存联系我们配置。
 */
async function saveContact() {
  savingContact.value = true
  try {
    await helpResourceApi.saveContact({ ...contactForm })
  } finally {
    savingContact.value = false
  }
}

/**
 * 加载可选菜单树，并建立菜单 ID 到工作区路径的映射。
 */
async function loadMenuTree() {
  const tree = await getMenuTree({})
  const modules = Array.isArray(permissionStore.modules) ? permissionStore.modules : []
  const pathMap: Record<number, string> = {}
  menuTreeData.value = buildSelectableTree(Array.isArray(tree) ? tree : [], modules, pathMap)
  menuPathMap.value = pathMap
}

/**
 * 把菜单树转成 TreeSelect 数据，并记录工作区全路径。
 *
 * @param nodes 原始菜单节点
 * @param modules 当前权限模块
 * @param pathMap 输出的菜单路径映射
 * @param parentSegments 上级路径片段
 * @param inheritedModuleCode 继承的模块编码
 * @returns TreeSelect 节点
 */
function buildSelectableTree(nodes: any[], modules: any[], pathMap: Record<number, string>, parentSegments: string[] = [], inheritedModuleCode = ''): any[] {
  return (nodes || [])
    .filter(node => node && node.type !== 'button')
    .map((node) => {
      const moduleCode = resolveModuleCode(node, modules) || inheritedModuleCode || 'sys'
      const { fullPath, segments } = buildModuleMenuPath(moduleCode, parentSegments, node.path || '')
      if (node.id && fullPath && node.type === 'menu') {
        pathMap[Number(node.id)] = fullPath
      }
      const children = buildSelectableTree(node.children || [], modules, pathMap, segments, moduleCode)
      return {
        value: Number(node.id),
        title: node.name || node.title,
        selectable: node.type === 'menu',
        children,
      }
    })
}

/**
 * 解析菜单所属模块编码。
 *
 * @param node 菜单节点
 * @param modules 权限模块列表
 * @returns 模块编码
 */
function resolveModuleCode(node: any, modules: any[]) {
  const matched = modules.find(item => String(item.id) === String(node.moduleId) || item.code === node.path)
  return matched?.code || node.moduleCode || ''
}

/**
 * 展示文件扩展名，外链显示为链接类型。
 *
 * @param record 当前行
 * @returns 格式文案
 */
function formatFileExt(record: SysHelpResource) {
  if (record.sourceType === 'EXTERNAL_URL') {
    return t('layout.help.sourceExternal')
  }
  const ext = String(record.fileExt || '').replace(/^\./, '').toUpperCase()
  return ext || '-'
}

/**
 * 把字节数格式化为可读大小。
 *
 * @param bytes 文件大小
 * @returns 可读字符串
 */
function formatFileSize(bytes?: number) {
  if (bytes == null || Number.isNaN(bytes) || bytes < 0) {
    return '-'
  }
  if (bytes < 1024) {
    return `${bytes} B`
  }
  if (bytes < 1024 * 1024) {
    return `${(bytes / 1024).toFixed(1)} KB`
  }
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`
}

/**
 * 用文件名（去掉扩展名）作为批量上传标题。
 *
 * @param name 原始文件名
 * @returns 标题
 */
function titleFromFileName(name: string) {
  return name.replace(/\.[^.]+$/, '') || name
}
</script>

<style scoped lang="less">
.help-page-header {
  margin-bottom: 8px;

  h1 {
    margin: 0 0 4px;
    font-size: 20px;
    font-weight: 600;
  }

  p {
    margin: 0;
    color: var(--ant-color-text-secondary);
  }
}

.help-contact-card {
  max-width: 960px;
}

.help-qr-panel {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 8px 0;
}

.help-qr-panel__label {
  color: var(--ant-color-text-secondary);
}

.help-qr-preview {
  display: block;
  width: 240px;
  height: 240px;
  object-fit: contain;
  border: 1px solid var(--ant-color-border);
  border-radius: 8px;
  background: #fff;
}

.help-file-name {
  margin-top: 8px;
  color: var(--ant-color-text-secondary);
}

.help-preview-wrap {
  min-height: 560px;
}

.danger-link {
  color: #ff4d4f;
}
</style>
