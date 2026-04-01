<template>
  <div class="page-wrap">
    <a-card :bordered="false">
      <a-form
        ref="formRef"
        :model="formData"
        :rules="rules"
        :label-col="{ span: 4 }"
        :wrapper-col="{ span: 18 }"
      >
        <a-form-item :label="$t('workflow.execution.taskCode')" name="taskCode">
          <a-select
            v-model:value="formData.taskCode"
            :placeholder="$t('workflow.execution.form.selectTask')"
            @change="handleTaskChange"
            :loading="taskListLoading"
          >
            <a-select-option
              v-for="task in taskList"
              :key="task.id"
              :value="task.taskCode"
            >
              {{ task.taskName }}
            </a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item :label="$t('workflow.execution.taskName')" name="taskName">
          <a-input
            v-model:value="currentTask?.taskName"
            disabled
          />
        </a-form-item>

        <a-form-item :label="$t('workflow.execution.formType')" name="formType">
          <a-tag :color="currentTask?.formType === 1 ? 'blue' : 'green'">
            {{ currentTask?.formType === 1 ? '自定义表单' : '低代码表单' }}
          </a-tag>
        </a-form-item>

        <!-- 自定义表单：根据 formPath 动态加载 -->
        <a-form-item
          v-if="currentTask?.formType === 1 && currentTask?.formPath"
          :label="$t('workflow.execution.formContent')"
        >
          <component
            :is="dynamicFormComponent"
            v-model="customFormData"
            @change="handleCustomFormChange"
          />
        </a-form-item>

        <!-- 低代码表单：渲染 JSON 表单 -->
        <a-form-item
          v-if="currentTask?.formType === 2 && currentTask?.formContent"
          :label="$t('workflow.execution.formContent')"
        >
          <div class="lowcode-form">
            <!-- TODO: 实现低代码表单渲染器 -->
            <a-alert
              message="低代码表单渲染器开发中"
              description="将表单内容以 JSON 格式提交"
              type="info"
              show-icon
            />
            <a-textarea
              v-model:value="customFormData"
              :rows="10"
              placeholder="请输入表单数据（JSON 格式）"
              @change="handleCustomFormChange"
            />
          </div>
        </a-form-item>

        <a-form-item :wrapper-col="{ offset: 4, span: 18 }">
          <a-space>
            <a-button type="primary" @click="handleSubmit" :loading="submitting">
              提交审批
            </a-button>
            <a-button @click="handleReset">重置</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import {
  listTaskConfig,
  type WfTaskConfigDTO
} from '@/api/workflow/taskConfig'
import {
  startExecution,
  type WfExecutionStartParam
} from '@/api/workflow/execution'
import { useRouter } from 'vue-router'

const router = useRouter()
const formRef = ref()

const taskListLoading = ref(false)
const taskList = ref<WfTaskConfigDTO[]>([])
const currentTask = ref<WfTaskConfigDTO | null>(null)

const submitting = ref(false)

const formData = reactive({
  taskCode: '',
  taskName: ''
})

const customFormData = ref<any>({})

const rules = {
  taskCode: [{ required: true, message: '请选择审批任务', trigger: 'change' }]
}

// 动态表单组件（根据 formPath 动态加载）
const dynamicFormComponent = computed(() => {
  if (!currentTask.value?.formPath) {
    return null
  }
  // TODO: 根据 formPath 动态加载组件
  // 例如：formPath = '/workflow/form/leave' -> LeaveForm 组件
  return null
})

// 加载启用的审批任务列表
const loadTaskList = async () => {
  try {
    taskListLoading.value = true
    const result = await listTaskConfig({ status: 1 })
    taskList.value = result || []
  } catch (e: any) {
    message.error(e.message || '加载任务列表失败')
  } finally {
    taskListLoading.value = false
  }
}

// 处理任务选择变化
const handleTaskChange = (taskCode: string) => {
  const task = taskList.value.find(t => t.taskCode === taskCode)
  if (task) {
    currentTask.value = task
    formData.taskName = task.taskName
    // 重置表单数据
    customFormData.value = {}
  }
}

// 处理自定义表单数据变化
const handleCustomFormChange = () => {
  // 可以在这里进行表单数据验证或处理
}

// 提交审批
const handleSubmit = async () => {
  try {
    await formRef.value?.validate()

    if (!currentTask.value) {
      message.error('请选择审批任务')
      return
    }

    // 检查表单数据
    let formContent: string
    if (currentTask.value.formType === 1) {
      // 自定义表单
      if (Object.keys(customFormData.value).length === 0) {
        message.warning('请填写表单内容')
        return
      }
      formContent = JSON.stringify(customFormData.value)
    } else {
      // 低代码表单
      if (!customFormData.value) {
        message.warning('请填写表单内容')
        return
      }
      // 如果是字符串，尝试解析为 JSON
      if (typeof customFormData.value === 'string') {
        try {
          JSON.parse(customFormData.value)
          formContent = customFormData.value
        } catch {
          message.error('表单内容必须是有效的 JSON 格式')
          return
        }
      } else {
        formContent = JSON.stringify(customFormData.value)
      }
    }

    submitting.value = true
    const param: WfExecutionStartParam = {
      taskCode: formData.taskCode,
      formContent
    }

    const executionId = await startExecution(param)
    message.success('发起审批成功')

    // 跳转到审批详情或我的发起页面
    router.push({
      path: '/workflow/my/initiated',
      query: { executionId }
    })
  } catch (e: any) {
    if (e.errorFields) {
      return
    }
    message.error(e.message || '发起审批失败')
  } finally {
    submitting.value = false
  }
}

// 重置表单
const handleReset = () => {
  formRef.value?.resetFields()
  currentTask.value = null
  customFormData.value = {}
}

onMounted(() => {
  loadTaskList()
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

.page-wrap :deep(.ant-card) {
  flex: 1;
  overflow: auto;
}

.lowcode-form {
  width: 100%;
}

.lowcode-form :deep(.ant-alert) {
  margin-bottom: 16px;
}
</style>
