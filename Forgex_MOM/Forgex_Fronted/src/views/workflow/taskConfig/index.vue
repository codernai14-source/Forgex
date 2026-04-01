<template>
  <div class="page-wrap">
    <!-- 审批任务配置列表 -->
    <fx-dynamic-table
      ref="tableRef"
      :table-code="'WfTaskConfigTable'"
      :request="handleRequest"
      :dict-options="dictOptions"
      row-key="id"
      :show-query-form="true"
    >
      <template #toolbar>
        <a-space>
          <a-button
            type="primary"
            @click="openAdd"
            v-permission="'wf:taskConfig:add'"
          >
            <template #icon><PlusOutlined /></template>
            {{ $t('workflow.taskConfig.form.addTask') }}
          </a-button>
        </a-space>
      </template>
      
      <template #formType="{ record }">
        <a-tag :color="record.formType === 1 ? 'blue' : 'green'">
          {{ record.formType === 1 ? '自定义表单' : '低代码表单' }}
        </a-tag>
      </template>
      
      <template #status="{ record }">
        <a-tag :color="record.status === 1 ? 'green' : 'red'">
          {{ record.status === 1 ? $t('common.enabled') : $t('common.disabled') }}
        </a-tag>
      </template>

      <template #action="{ record }">
        <a-space>
          <a-button
            type="link"
            size="small"
            @click="openEdit(record)"
            v-permission="'wf:taskConfig:edit'"
          >
            <template #icon><EditOutlined /></template>
            编辑
          </a-button>
          <a-button
            type="link"
            size="small"
            @click="handleNodeConfig(record)"
            v-permission="'wf:taskConfig:config'"
          >
            <template #icon><SettingOutlined /></template>
            节点配置
          </a-button>
          <a-popconfirm
            title="确定要删除这个审批任务吗？"
            :ok-text="$t('common.confirm')"
            :cancel-text="$t('common.cancel')"
            @confirm="handleDelete(record)"
          >
            <a-button
              type="link"
              size="small"
              danger
              v-permission="'wf:taskConfig:delete'"
            >
              <template #icon><DeleteOutlined /></template>
              删除
            </a-button>
          </a-popconfirm>
        </a-space>
      </template>
    </fx-dynamic-table>

    <!-- 新增/编辑表单：使用通用弹窗组件 -->
    <BaseFormDialog
      v-model:open="dialogVisible"
      :title="formData.id ? $t('workflow.taskConfig.form.editTask') : $t('workflow.taskConfig.form.addTask')"
      :loading="saving"
      :width="700"
      @submit="handleSave"
      @cancel="handleCancel"
    >
      <a-form
        ref="formRef"
        :model="formData"
        :rules="rules"
        :label-col="{ span: 6 }"
        :wrapper-col="{ span: 16 }"
      >
        <a-form-item :label="$t('workflow.taskConfig.taskName')" name="taskName">
          <a-input
            v-model:value="formData.taskName"
            :placeholder="$t('workflow.taskConfig.form.taskName')"
          />
        </a-form-item>

        <a-form-item :label="$t('workflow.taskConfig.taskCode')" name="taskCode">
          <a-input
            v-model:value="formData.taskCode"
            :placeholder="$t('workflow.taskConfig.form.taskCode')"
            :disabled="!!formData.id"
          />
        </a-form-item>

        <a-form-item :label="$t('workflow.taskConfig.interpreterBean')" name="interpreterBean">
          <a-input
            v-model:value="formData.interpreterBean"
            :placeholder="$t('workflow.taskConfig.form.interpreterBean')"
          />
        </a-form-item>

        <a-form-item :label="$t('workflow.taskConfig.formType')" name="formType">
          <a-select
            v-model:value="formData.formType"
            :placeholder="$t('workflow.taskConfig.form.formType')"
          >
            <a-select-option :value="1">自定义表单</a-select-option>
            <a-select-option :value="2">低代码表单</a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item 
          v-if="formData.formType === 1" 
          :label="$t('workflow.taskConfig.formPath')" 
          name="formPath"
        >
          <a-input
            v-model:value="formData.formPath"
            :placeholder="$t('workflow.taskConfig.form.formPath')"
          />
        </a-form-item>

        <a-form-item 
          v-if="formData.formType === 2" 
          :label="$t('workflow.taskConfig.formContent')" 
          name="formContent"
        >
          <a-textarea
            v-model:value="formData.formContent"
            :placeholder="$t('workflow.taskConfig.form.formContent')"
            :rows="6"
          />
        </a-form-item>

        <a-form-item :label="$t('workflow.taskConfig.status')" name="status">
          <a-radio-group v-model:value="formData.status">
            <a-radio :value="1">{{ $t('common.enabled') }}</a-radio>
            <a-radio :value="0">{{ $t('common.disabled') }}</a-radio>
          </a-radio-group>
        </a-form-item>

        <a-form-item :label="$t('workflow.taskConfig.remark')" name="remark">
          <a-textarea
            v-model:value="formData.remark"
            :placeholder="$t('workflow.taskConfig.form.remark')"
            :rows="3"
          />
        </a-form-item>
      </a-form>
    </BaseFormDialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  SettingOutlined
} from '@ant-design/icons-vue'
import {
  getTaskConfigPage,
  createTaskConfig,
  updateTaskConfig,
  deleteTaskConfig,
  type WfTaskConfigDTO,
  type WfTaskConfigQueryParam,
  type WfTaskConfigSaveParam
} from '@/api/workflow/taskConfig'
import BaseFormDialog from '@/components/common/BaseFormDialog.vue'
import FxDynamicTable from '@/components/common/FxDynamicTable.vue'
import { useDict } from '@/hooks/useDict'

const { dictItems: statusOptions } = useDict('status')

const formRef = ref()
const tableRef = ref()

const loading = ref(false)

const dialogVisible = ref(false)
const saving = ref(false)

const formData = reactive<WfTaskConfigSaveParam>({
  taskName: '',
  taskCode: '',
  formType: 1,
  status: 1
})

const rules = {
  taskName: [{ required: true, message: '请输入任务名称', trigger: 'blur' }],
  taskCode: [{ required: true, message: '请输入任务编码', trigger: 'blur' }],
  formType: [{ required: true, message: '请选择表单类型', trigger: 'change' }]
}

// 字典配置
const dictOptions = computed(() => ({
  status: statusOptions.value,
  formType: [
    { label: '自定义表单', value: 1 },
    { label: '低代码表单', value: 2 }
  ],
}))

// 处理表格数据请求
const handleRequest = async (payload: { 
  page: { current: number; pageSize: number }; 
  query: Record<string, any>; 
  sorter?: { field?: string; order?: string } 
}) => {
  try {
    loading.value = true
    const params: any = {
      pageNum: payload.page.current,
      pageSize: payload.page.pageSize,
      ...payload.query
    }
    
    // 处理排序
    if (payload.sorter) {
      params.sortField = payload.sorter.field
      params.sortOrder = payload.sorter.order
    }
    
    const data = await getTaskConfigPage(params)
    // 确保 total 是数字类型
    const total = typeof data.total === 'number' ? data.total : parseInt(String(data.total) || '0', 10)
    return { records: data.records || [], total: total }
  } catch (e: any) {
    message.error(e.message || '加载审批任务列表失败')
    return { records: [], total: 0 }
  } finally {
    loading.value = false
  }
}

function openAdd() {
  dialogVisible.value = true
  Object.assign(formData, {
    taskName: '',
    taskCode: '',
    interpreterBean: undefined,
    formType: 1,
    formPath: undefined,
    formContent: undefined,
    status: 1,
    remark: undefined
  })
  formRef.value?.resetFields()
}

function openEdit(record: WfTaskConfigDTO) {
  dialogVisible.value = true
  Object.assign(formData, {
    id: record.id,
    taskName: record.taskName,
    taskCode: record.taskCode,
    interpreterBean: record.interpreterBean,
    formType: record.formType,
    formPath: record.formPath,
    formContent: record.formContent,
    status: record.status,
    remark: record.remark
  })
  
  formRef.value?.resetFields()
}

async function handleSave() {
  try {
    await formRef.value?.validate()
    saving.value = true

    if (formData.id) {
      await updateTaskConfig(formData)
      message.success('更新成功')
    } else {
      await createTaskConfig(formData)
      message.success('新增成功')
    }

    dialogVisible.value = false
    tableRef.value?.refresh?.()
  } catch (e: any) {
    if (e.errorFields) {
      return
    }
    message.error(e.message || '保存失败')
  } finally {
    saving.value = false
  }
}

function handleCancel() {
  dialogVisible.value = false
  formRef.value?.resetFields()
}

async function handleDelete(record: WfTaskConfigDTO) {
  try {
    await deleteTaskConfig({ id: record.id })
    message.success('删除成功')
    await tableRef.value?.refresh?.()
  } catch (e: any) {
    message.error(e.message || '删除失败')
  }
}

function handleNodeConfig(record: WfTaskConfigDTO) {
  message.info('节点配置功能开发中...')
  // TODO: 跳转到节点配置页面或打开节点配置弹窗
}

onMounted(() => {
  tableRef.value?.refresh?.()
})
</script>

<style scoped>
.page-wrap {
  padding: 16px;
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
  overflow: hidden;
  box-sizing: border-box;
}
</style>
