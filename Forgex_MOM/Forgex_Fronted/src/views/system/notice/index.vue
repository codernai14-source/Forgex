<template>
  <div class="system-notice-page">
    <FxDynamicTable
      ref="tableRef"
      table-code="SystemNoticeTable"
      :request="handleRequest"
      :dict-options="dictOptions"
      row-key="id"
    >
      <template #toolbar>
        <a-space>
          <a-button v-permission="'sys:notice:add'" type="primary" @click="openCreate">新增通知</a-button>
        </a-space>
      </template>

      <template #scope="{ record }">
        <a-tag :color="record.scope === 'PUBLIC' ? 'blue' : 'green'">{{ labelOf(scopeOptions, record.scope) }}</a-tag>
      </template>

      <template #status="{ record }">
        <a-tag :color="statusColor(record.status)">{{ labelOf(statusOptions, record.status) }}</a-tag>
      </template>

      <template #forceRemind="{ record }">
        <a-tag :color="record.forceRemind ? 'orange' : 'default'">{{ record.forceRemind ? '是' : '否' }}</a-tag>
      </template>

      <template #action="{ record }">
        <a-space>
          <a v-permission="'sys:notice:view'" @click="openDetail(record)">详情</a>
          <a v-permission="'sys:notice:edit'" @click="openEdit(record)">编辑</a>
          <a
            v-if="record.status !== 'PUBLISHED'"
            v-permission="'sys:notice:publish'"
            @click="handlePublishAction(record)"
          >
            发布
          </a>
          <a
            v-else
            v-permission="'sys:notice:publish'"
            @click="handleDisableAction(record)"
          >
            停用
          </a>
          <a v-permission="'sys:notice:delete'" class="danger-link" @click="handleDeleteAction(record)">删除</a>
        </a-space>
      </template>
    </FxDynamicTable>

    <BaseFormDialog
      v-model:open="dialogVisible"
      :title="dialogTitle"
      :width="1040"
      :loading="saving"
      :mask-closable="true"
      :body-style="{ maxHeight: '74vh', overflowY: 'auto' }"
      @submit="handleSaveSubmit"
      @cancel="dialogVisible = false"
    >
      <a-form layout="vertical" :model="form">
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="通知标题" required>
              <a-input v-model:value="form.title" :disabled="readonly" />
            </a-form-item>
          </a-col>
          <a-col :span="6">
            <a-form-item label="通知范围" required>
              <a-select v-model:value="form.scope" :disabled="readonly" :options="scopeOptions" />
            </a-form-item>
          </a-col>
          <a-col :span="6">
            <a-form-item label="状态">
              <a-select v-model:value="form.status" :disabled="readonly" :options="statusOptions" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="生效时间">
              <a-date-picker v-model:value="form.startTime" :disabled="readonly" value-format="YYYY-MM-DD HH:mm:ss" show-time class="full-width" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="失效时间">
              <a-date-picker v-model:value="form.endTime" :disabled="readonly" value-format="YYYY-MM-DD HH:mm:ss" show-time class="full-width" />
            </a-form-item>
          </a-col>
          <a-col :span="4">
            <a-form-item label="排序">
              <a-input-number v-model:value="form.orderNum" :disabled="readonly" class="full-width" />
            </a-form-item>
          </a-col>
          <a-col :span="4">
            <a-form-item label="强提醒">
              <a-switch v-model:checked="form.forceRemind" :disabled="readonly" />
            </a-form-item>
          </a-col>
          <a-col :span="24">
            <a-form-item label="摘要">
              <a-textarea v-model:value="form.summary" :disabled="readonly" :rows="2" />
            </a-form-item>
          </a-col>
          <a-col :span="24">
            <a-form-item label="通知内容" required>
              <Toolbar
                v-if="!readonly"
                :editor="editorRef"
                :default-config="toolbarConfig"
                mode="default"
                class="notice-editor-toolbar"
              />
              <Editor
                v-if="!readonly"
                v-model="form.contentHtml"
                :default-config="editorConfig"
                mode="default"
                class="notice-editor"
                @on-created="handleEditorCreated"
              />
              <div v-else class="notice-readonly-content" v-html="form.contentHtml"></div>
            </a-form-item>
          </a-col>
          <a-col :span="24">
            <a-form-item label="附件">
              <a-upload
                :file-list="uploadFileList"
                :before-upload="handleAttachmentUpload"
                :remove="handleAttachmentRemove"
                :disabled="readonly"
              >
                <a-button v-if="!readonly">上传附件</a-button>
              </a-upload>
            </a-form-item>
          </a-col>
        </a-row>
      </a-form>
    </BaseFormDialog>
  </div>
</template>

<script setup lang="ts">
import '@wangeditor/editor/dist/css/style.css'
import { computed, onBeforeUnmount, ref, shallowRef } from 'vue'
import { message, Modal } from 'ant-design-vue'
import type { UploadProps } from 'ant-design-vue'
import { Editor, Toolbar } from '@wangeditor/editor-for-vue'
import type { IDomEditor, IEditorConfig, IToolbarConfig } from '@wangeditor/editor'
import { useI18n } from 'vue-i18n'
import BaseFormDialog from '@/components/common/BaseFormDialog.vue'
import FxDynamicTable from '@/components/common/FxDynamicTable.vue'
import { uploadFile } from '@/api/system/file'
import { noticeApi, type NoticeScope, type NoticeStatus, type SysNotice, type SysNoticeAttachment, type SysNoticePageParam } from '@/api/system/notice'

const { t } = useI18n()
const tableRef = ref()
const dialogVisible = ref(false)
const saving = ref(false)
const readonly = ref(false)
const form = ref<SysNotice>(emptyForm())
const editorRef = shallowRef<IDomEditor>()

const scopeOptions: Array<{ label: string; value: NoticeScope }> = [
  { label: '公共', value: 'PUBLIC' },
  { label: '租户', value: 'TENANT' },
]
const statusOptions: Array<{ label: string; value: NoticeStatus }> = [
  { label: '草稿', value: 'DRAFT' },
  { label: '已发布', value: 'PUBLISHED' },
  { label: '已停用', value: 'DISABLED' },
]
const dictOptions = computed(() => ({
  scope: scopeOptions,
  notice_scope: scopeOptions,
  status: statusOptions,
  notice_status: statusOptions,
  forceRemind: [
    { label: '是', value: true },
    { label: '否', value: false },
  ],
}))
const dialogTitle = computed(() => readonly.value ? '通知详情' : form.value.id ? '编辑通知' : '新增通知')
const uploadFileList = computed(() => (form.value.attachments || []).map((item, index) => ({
  uid: String(item.id || item.fileUrl || index),
  name: item.fileName || item.fileUrl || `附件${index + 1}`,
  status: 'done',
  url: item.fileUrl,
})))
const toolbarConfig: Partial<IToolbarConfig> = {}
const editorConfig: Partial<IEditorConfig> = {
  placeholder: '请输入通知内容',
  MENU_CONF: {
    uploadImage: {
      async customUpload(file: File, insertFn: (url: string, alt?: string, href?: string) => void) {
        const url = await uploadFile(file, { moduleCode: 'sys_notice', moduleName: '系统通知图片' })
        insertFn(url, file.name, url)
      },
    },
  },
}

function emptyForm(): SysNotice {
  return {
    title: '',
    scope: 'TENANT',
    contentHtml: '',
    summary: '',
    status: 'DRAFT',
    orderNum: 0,
    forceRemind: false,
    attachments: [],
  }
}

async function handleRequest(payload: { page: { current: number; pageSize: number }; query: Record<string, any> }) {
  const params: SysNoticePageParam = {
    pageNum: payload.page.current,
    pageSize: payload.page.pageSize,
    ...payload.query,
  }
  const result = await noticeApi.page(params)
  return { records: result.records || [], total: Number(result.total || 0) }
}

function handleEditorCreated(editor: IDomEditor) {
  editorRef.value = editor
}

function openCreate() {
  readonly.value = false
  form.value = emptyForm()
  dialogVisible.value = true
}

async function openEdit(record: SysNotice) {
  readonly.value = false
  form.value = await noticeApi.detail(record.id!)
  dialogVisible.value = true
}

async function openDetail(record: SysNotice) {
  readonly.value = true
  form.value = await noticeApi.detail(record.id!)
  dialogVisible.value = true
}

async function handleSave() {
  if (readonly.value) {
    dialogVisible.value = false
    return
  }
  if (!form.value.title?.trim()) {
    message.warning('通知标题不能为空')
    return
  }
  if (!form.value.contentHtml?.trim()) {
    message.warning('通知内容不能为空')
    return
  }
  saving.value = true
  try {
    await noticeApi.save(form.value)
    dialogVisible.value = false
    await tableRef.value?.refresh?.()
  } finally {
    saving.value = false
  }
}

async function handleAttachmentUpload(file: File) {
  const url = await uploadFile(file, { moduleCode: 'sys_notice', moduleName: '系统通知附件' })
  const attachment: SysNoticeAttachment = {
    fileName: file.name,
    fileUrl: url,
    fileSize: file.size,
    fileType: file.type || file.name.split('.').pop(),
  }
  form.value.attachments = [...(form.value.attachments || []), attachment]
  return false
}

const handleAttachmentRemove: UploadProps['onRemove'] = (file) => {
  form.value.attachments = (form.value.attachments || []).filter(item => (item.fileUrl || item.fileName) !== (file.url || file.name))
  return true
}

function handlePublish(record: SysNotice) {
  Modal.confirm({
    title: '确认发布该通知？',
    async onOk() {
      await noticeApi.publish(record.id!)
      await tableRef.value?.refresh?.()
    },
  })
}

function handleDisable(record: SysNotice) {
  Modal.confirm({
    title: '确认停用该通知？',
    async onOk() {
      await noticeApi.disable(record.id!)
      await tableRef.value?.refresh?.()
    },
  })
}

function handleDelete(record: SysNotice) {
  Modal.confirm({
    title: '确认删除该通知？',
    async onOk() {
      await noticeApi.delete(record.id!)
      await tableRef.value?.refresh?.()
    },
  })
}

function labelOf(options: Array<{ label: string; value: any }>, value: any) {
  return options.find(item => String(item.value) === String(value))?.label || value || '-'
}

function statusColor(status?: NoticeStatus) {
  return ({ DRAFT: 'default', PUBLISHED: 'green', DISABLED: 'red' } as Record<string, string>)[status || ''] || 'default'
}

function dispatchSystemNoticeRefresh() {
  if (typeof window === 'undefined') {
    return
  }
  window.dispatchEvent(new CustomEvent('fx:system-notice-refresh'))
}

async function handleSaveSubmit() {
  if (readonly.value) {
    dialogVisible.value = false
    return
  }
  if (!form.value.title?.trim()) {
    message.warning(t('system.notice.titleRequired'))
    return
  }
  if (!form.value.contentHtml?.trim()) {
    message.warning(t('system.notice.contentRequired'))
    return
  }
  saving.value = true
  try {
    await noticeApi.save(form.value)
    message.success(t('system.notice.saveSuccess'))
    dispatchSystemNoticeRefresh()
    dialogVisible.value = false
    await tableRef.value?.refresh?.()
  } finally {
    saving.value = false
  }
}

function handlePublishAction(record: SysNotice) {
  Modal.confirm({
    title: t('system.notice.confirmPublish'),
    async onOk() {
      await noticeApi.publish(record.id!)
      message.success(t('system.notice.publishSuccess'))
      dispatchSystemNoticeRefresh()
      await tableRef.value?.refresh?.()
    },
  })
}

function handleDisableAction(record: SysNotice) {
  Modal.confirm({
    title: t('system.notice.confirmDisable'),
    async onOk() {
      await noticeApi.disable(record.id!)
      message.success(t('system.notice.disableSuccess'))
      dispatchSystemNoticeRefresh()
      await tableRef.value?.refresh?.()
    },
  })
}

function handleDeleteAction(record: SysNotice) {
  Modal.confirm({
    title: t('system.notice.confirmDelete'),
    async onOk() {
      await noticeApi.delete(record.id!)
      message.success(t('system.notice.deleteSuccess'))
      dispatchSystemNoticeRefresh()
      await tableRef.value?.refresh?.()
    },
  })
}

onBeforeUnmount(() => {
  editorRef.value?.destroy()
})
</script>

<style scoped lang="less" src="@/styles/views/system/notice/index.less"></style>
