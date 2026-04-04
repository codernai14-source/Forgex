<template>
  <div class="page-wrap">
    <FxDynamicTable
      ref="tableRef"
      :table-code="'WfTaskConfigTable'"
      :request="handleRequest"
      :dict-options="dictOptions"
      row-key="id"
    >
      <template #toolbar>
        <a-button type="primary" v-permission="'wf:taskConfig:add'" @click="openCreate">
          新建流程
        </a-button>
      </template>

      <template #formType="{ record }">
        <a-tag :color="record.formType === 1 ? 'blue' : 'green'">
          {{ record.formType === 1 ? '自定义表单' : '低代码表单' }}
        </a-tag>
      </template>

      <template #status="{ record }">
        <a-tag :color="record.status === 1 ? 'success' : 'default'">
          {{ record.status === 1 ? '启用' : '禁用' }}
        </a-tag>
      </template>

      <template #version="{ record }">
        <div class="version-cell">
          <a-tag v-if="record.publishedVersion" color="blue">
            已发布 v{{ record.publishedVersion }}
          </a-tag>
          <a-tag v-if="record.draftVersion" color="orange">
            草稿 v{{ record.draftVersion }}
          </a-tag>
          <span v-if="!record.publishedVersion && record.draftVersion" class="version-hint">
            未发布
          </span>
          <span v-if="!record.publishedVersion && !record.draftVersion">-</span>
        </div>
      </template>

      <template #remark="{ record }">
        <span class="remark-text">{{ record.remark || '-' }}</span>
      </template>

      <template #action="{ record }">
        <a-space>
          <a-button
            type="link"
            size="small"
            v-permission="'wf:taskConfig:edit'"
            @click="openEdit(record)"
          >
            编辑
          </a-button>
          <a-button
            type="link"
            size="small"
            v-permission="'wf:taskConfig:config'"
            @click="handleNodeConfig(record)"
          >
            节点配置
          </a-button>
          <a-button
            type="link"
            size="small"
            :disabled="!record.hasDraft"
            v-permission="'wf:taskConfig:config'"
            @click="handlePublish(record)"
          >
            发布
          </a-button>
          <a-popconfirm
            title="确认删除当前流程吗？当前草稿会删除，当前已发布版本会归档保留。"
            ok-text="确认"
            cancel-text="取消"
            @confirm="handleDelete(record)"
          >
            <a-button type="link" size="small" danger v-permission="'wf:taskConfig:delete'">
              删除
            </a-button>
          </a-popconfirm>
        </a-space>
      </template>
    </FxDynamicTable>

    <a-modal
      v-model:open="dialogVisible"
      :title="dialogTitle"
      :confirm-loading="saving"
      width="720px"
      destroy-on-close
      @ok="handleSave"
    >
      <a-form ref="formRef" :model="formState" :rules="rules" layout="vertical">
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="流程名称" name="taskName">
              <a-input v-model:value="formState.taskName" placeholder="请输入流程名称" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="流程编码" name="taskCode">
              <a-input
                v-model:value="formState.taskCode"
                :disabled="Boolean(currentEditor?.hasPublished)"
                placeholder="请输入流程编码"
              />
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="审批解释器" name="interpreterBean">
              <a-input v-model:value="formState.interpreterBean" placeholder="如 leaveApprovalInterpreter" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="表单类型" name="formType">
              <a-select v-model:value="formState.formType" placeholder="请选择表单类型">
                <a-select-option :value="1">自定义表单</a-select-option>
                <a-select-option :value="2">低代码表单</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
        </a-row>

        <a-form-item v-if="formState.formType === 1" label="表单路径" name="formPath">
          <a-input v-model:value="formState.formPath" placeholder="例如 /workflow/form/leave" />
        </a-form-item>

        <a-form-item v-else label="表单内容 JSON" name="formContent">
          <a-textarea
            v-model:value="formState.formContent"
            :rows="6"
            placeholder="请输入低代码表单 JSON"
          />
        </a-form-item>

        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="状态" name="status">
              <a-radio-group v-model:value="formState.status">
                <a-radio :value="1">启用</a-radio>
                <a-radio :value="0">禁用</a-radio>
              </a-radio-group>
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="版本信息">
              <a-space>
                <a-tag v-if="currentEditor?.publishedVersion" color="blue">
                  已发布 v{{ currentEditor.publishedVersion }}
                </a-tag>
                <a-tag v-if="currentEditor?.draftVersion" color="orange">
                  草稿 v{{ currentEditor.draftVersion }}
                </a-tag>
                <span v-if="!currentEditor">新建草稿</span>
              </a-space>
            </a-form-item>
          </a-col>
        </a-row>

        <a-form-item label="流程说明" name="remark">
          <a-textarea v-model:value="formState.remark" :rows="3" placeholder="请输入流程说明" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import type { FormInstance } from 'ant-design-vue'
import { useRoute, useRouter } from 'vue-router'
import FxDynamicTable from '@/components/common/FxDynamicTable.vue'
import { useDict } from '@/hooks/useDict'
import {
  deleteTaskConfig,
  getOrCreateDraftEditor,
  getTaskConfigPage,
  publishDraft,
  saveDraftBaseInfo,
  type WfTaskConfigSaveParam,
  type WfTaskConfigSummaryDTO,
  type WfTaskDraftEditorDTO
} from '@/api/workflow/taskConfig'

type TableRequestPayload = {
  page: { current: number; pageSize: number }
  query: Record<string, any>
  sorter?: { field?: string; order?: string }
}

const router = useRouter()
const route = useRoute()
const { dictItems: statusOptions } = useDict('status')

const tableRef = ref<any>()
const formRef = ref<FormInstance>()
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const currentEditor = ref<WfTaskDraftEditorDTO | null>(null)

const formState = reactive<WfTaskConfigSaveParam>({
  taskName: '',
  taskCode: '',
  interpreterBean: '',
  formType: 1,
  formPath: '',
  formContent: '',
  status: 1,
  remark: ''
})

const dictOptions = computed(() => ({
  status: statusOptions.value || []
}))

const rules = {
  taskName: [{ required: true, message: '请输入流程名称', trigger: 'blur' }],
  taskCode: [{ required: true, message: '请输入流程编码', trigger: 'blur' }],
  formType: [{ required: true, message: '请选择表单类型', trigger: 'change' }]
}

const dialogTitle = computed(() => (formState.id ? '编辑草稿基础信息' : '新建流程草稿'))
const silentErrorConfig = { silentError: true }

function resetFormState() {
  Object.assign(formState, {
    id: undefined,
    taskName: '',
    taskCode: '',
    interpreterBean: '',
    formType: 1,
    formPath: '',
    formContent: '',
    status: 1,
    remark: ''
  })
  currentEditor.value = null
}

function fillForm(editor: WfTaskDraftEditorDTO) {
  currentEditor.value = editor
  Object.assign(formState, {
    id: editor.draftId,
    taskName: editor.taskName,
    taskCode: editor.taskCode,
    interpreterBean: editor.interpreterBean || '',
    formType: editor.formType,
    formPath: editor.formPath || '',
    formContent: editor.formContent || '',
    status: editor.status,
    remark: editor.remark || ''
  })
}

async function reloadTable() {
  await tableRef.value?.reload?.()
}

const handleRequest = async (payload: TableRequestPayload) => {
  try {
    loading.value = true
    const params: Record<string, any> = {
      pageNum: payload.page.current,
      pageSize: payload.page.pageSize,
      ...payload.query
    }

    if (payload.sorter?.field) {
      params.sortField = payload.sorter.field
      params.sortOrder = payload.sorter.order
    }

    const result = await getTaskConfigPage(params as any)
    return {
      records: result.records || [],
      total: Number(result.total || 0)
    }
  } catch (error: any) {
    message.error(error.message || '加载审批任务配置失败')
    return { records: [], total: 0 }
  } finally {
    loading.value = false
  }
}

function openCreate() {
  resetFormState()
  dialogVisible.value = true
}

async function openEdit(record: WfTaskConfigSummaryDTO) {
  try {
    const editor = await getOrCreateDraftEditor({ taskCode: record.taskCode }, silentErrorConfig)
    fillForm(editor)
    dialogVisible.value = true
  } catch (error: any) {
    message.error(error.message || '加载草稿失败')
  }
}

async function handleNodeConfig(record: WfTaskConfigSummaryDTO) {
  try {
    const editor = await getOrCreateDraftEditor({ taskCode: record.taskCode }, silentErrorConfig)
    router.push({
      path: `/workflow/taskConfig/${editor.taskCode}/nodes`,
      query: {
        from: route.fullPath
      }
    })
  } catch (error: any) {
    message.error(error.message || '进入节点配置失败')
  }
}

async function handlePublish(record: WfTaskConfigSummaryDTO) {
  if (!record.hasDraft) {
    message.info('当前没有可发布的草稿')
    return
  }
  try {
    await publishDraft({ taskCode: record.taskCode })
    message.success('发布成功')
    await reloadTable()
  } catch (error: any) {
    message.error(error.message || '发布失败')
  }
}

async function handleDelete(record: WfTaskConfigSummaryDTO) {
  try {
    await deleteTaskConfig({ id: record.draftId || record.publishedId || record.id })
    message.success('删除成功')
    await reloadTable()
  } catch (error: any) {
    message.error(error.message || '删除失败')
  }
}

async function handleSave() {
  try {
    await formRef.value?.validate()
    saving.value = true
    const editor = await saveDraftBaseInfo({ ...formState })
    fillForm(editor)
    dialogVisible.value = false
    message.success('草稿保存成功')
    await reloadTable()
  } catch (error: any) {
    if (error?.errorFields) {
      return
    }
    message.error(error.message || '保存草稿失败')
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.page-wrap {
  height: 100%;
}

.version-cell {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}

.version-hint {
  color: #d97706;
  font-size: 12px;
}

.remark-text {
  color: #4b5563;
}
</style>
