<template>
  <div class="start-form-page">
    <div class="page-head">
      <div>
        <a-button type="link" class="page-head__back" @click="navigateAndCloseCurrent(approvalRoutePaths.executionStartList)">
          返回流程选择
        </a-button>
        <h2>{{ taskConfig?.taskName || '填写审批表单' }}</h2>
        <p>{{ taskConfig?.remark || '请确认流程信息后填写表单并发起审批。' }}</p>
      </div>

      <a-space>
        <a-tag v-if="taskConfig" :color="taskConfig.formType === 1 ? 'blue' : 'green'">
          {{ taskConfig.formType === 1 ? '自定义表单' : '低代码表单' }}
        </a-tag>
        <a-tag v-if="taskConfig" color="gold">{{ taskConfig.taskCode }}</a-tag>
      </a-space>
    </div>

    <a-alert
      v-if="errorMessage"
      type="error"
      show-icon
      :message="errorMessage"
      class="error-alert"
    >
      <template #description>
        <a-button type="link" @click="navigateAndCloseCurrent(approvalRoutePaths.executionStartList)">返回选择页</a-button>
      </template>
    </a-alert>

    <div v-else class="form-shell">
      <a-spin :spinning="loading">
        <div class="form-shell__inner">
          <div class="form-shell__summary">
            <div class="summary-item">
              <span>流程名称</span>
              <strong>{{ taskConfig?.taskName }}</strong>
            </div>
            <div class="summary-item">
              <span>任务编码</span>
              <strong>{{ taskConfig?.taskCode }}</strong>
            </div>
            <div class="summary-item">
              <span>流程说明</span>
              <strong>{{ taskConfig?.remark || '未填写流程说明' }}</strong>
            </div>
          </div>

          <div class="form-shell__body">
            <component
              :is="dynamicFormComponent"
              v-if="taskConfig?.formType === 1 && dynamicFormComponent"
              ref="dynamicFormRef"
              v-model="customFormData"
            />

            <a-alert
              v-else-if="taskConfig?.formType === 1"
              type="warning"
              show-icon
              message="未注册对应的前端表单组件"
              :description="taskConfig?.formPath || ''"
            />

            <div v-else-if="taskConfig?.formType === 2" class="lowcode-form">
              <a-alert
                type="info"
                show-icon
                message="低代码表单内容"
                description="当前项目暂时使用 JSON 文本方式编辑低代码表单内容。"
              />
              <a-textarea
                v-model:value="lowCodeFormText"
                :rows="12"
                placeholder="请输入合法 JSON"
              />
            </div>
          </div>

          <div class="form-shell__footer">
            <a-button @click="handleReset">重置</a-button>
            <a-button type="primary" :loading="submitting" @click="handleSubmit">
              发起审批
            </a-button>
          </div>
        </div>
      </a-spin>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { message } from 'ant-design-vue'
import { type LocationQueryRaw, useRoute, useRouter } from 'vue-router'
import { approvalRoutePaths, TAB_CLOSE_QUERY_KEY } from '@/router/approvalRoutePaths'
import { startExecution, type WfExecutionStartParam } from '@/api/workflow/execution'
import { getTaskConfigByCode, type WfTaskConfigDTO } from '@/api/workflow/taskConfig'
import { workflowFormRegistry } from './formRegistry'

interface DynamicFormExpose {
  validate?: () => Promise<Record<string, any> | void>
  reset?: () => void
}

const route = useRoute()
const router = useRouter()

const taskConfig = ref<WfTaskConfigDTO | null>(null)
const loading = ref(false)
const submitting = ref(false)
const errorMessage = ref('')
const customFormData = ref<Record<string, any>>({})
const lowCodeFormText = ref('{}')
const dynamicFormRef = ref<DynamicFormExpose>()

const taskCode = computed(() => String(route.params.taskCode || '').trim())
const dynamicFormComponent = computed(() => {
  if (!taskConfig.value?.formPath) {
    return null
  }
  return workflowFormRegistry[taskConfig.value.formPath] || null
})

function navigateAndCloseCurrent(path: string, query: LocationQueryRaw = {}) {
  router.push({
    path,
    query: {
      ...query,
      [TAB_CLOSE_QUERY_KEY]: route.path,
    }
  })
}

async function loadTaskConfig() {
  if (!taskCode.value) {
    errorMessage.value = '缺少审批流程编码，无法加载表单。'
    return
  }

  try {
    loading.value = true
    errorMessage.value = ''
    const result = await getTaskConfigByCode({ taskCode: taskCode.value })
    taskConfig.value = result
    lowCodeFormText.value = result.formContent || '{}'

    if (result.formType === 1 && result.formPath && !workflowFormRegistry[result.formPath]) {
      errorMessage.value = `未注册表单组件：${result.formPath}`
    }
  } catch (error: any) {
    errorMessage.value = error.message || '未找到当前已发布流程，无法发起审批。'
  } finally {
    loading.value = false
  }
}

function handleReset() {
  customFormData.value = {}
  lowCodeFormText.value = taskConfig.value?.formContent || '{}'
  dynamicFormRef.value?.reset?.()
}

async function handleSubmit() {
  if (!taskConfig.value) {
    return
  }

  try {
    let formContent = ''
    if (taskConfig.value.formType === 1) {
      if (!dynamicFormComponent.value) {
        message.error('当前流程未绑定可用的前端表单组件')
        return
      }

      const validatedFormData = await dynamicFormRef.value?.validate?.()
      const payload = validatedFormData || customFormData.value
      if (!payload || typeof payload !== 'object' || Object.keys(payload).length === 0) {
        message.warning('请先填写表单内容')
        return
      }
      customFormData.value = payload
      formContent = JSON.stringify(payload)
    } else {
      JSON.parse(lowCodeFormText.value)
      formContent = lowCodeFormText.value
    }

    submitting.value = true
    const params: WfExecutionStartParam = {
      taskCode: taskConfig.value.taskCode,
      formContent
    }
    const executionId = await startExecution(params)
    navigateAndCloseCurrent(approvalRoutePaths.myInitiated, { executionId })
  } catch (error: any) {
    if (error?.message?.includes('JSON')) {
      message.error('低代码表单内容必须是合法 JSON')
      return
    }
    if (error?.errorFields) {
      return
    }
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadTaskConfig()
})
</script>

<style scoped>
.start-form-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 16px;
}

.page-head,
.form-shell {
  border-radius: 22px;
  border: 1px solid var(--fx-border-color);
  background: var(--fx-bg-container);
  box-shadow: 0 18px 40px rgba(15, 23, 42, 0.06);
}

.page-head {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  padding: 22px 24px;
}

.page-head h2 {
  margin: 8px 0 6px;
  font-size: 26px;
  color: var(--fx-text-primary);
}

.page-head p,
.page-head__back {
  margin: 0;
  padding: 0;
  color: var(--fx-text-secondary);
}

.error-alert {
  background: var(--fx-bg-container);
}

.form-shell__inner {
  padding: 24px;
}

.form-shell__summary {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 20px;
}

.summary-item {
  padding: 14px 16px;
  border-radius: 16px;
  background: var(--fx-fill-alter);
}

.summary-item span {
  display: block;
  margin-bottom: 8px;
  font-size: 12px;
  color: var(--fx-text-tertiary);
}

.summary-item strong {
  color: var(--fx-text-primary);
  line-height: 1.5;
}

.form-shell__body {
  min-height: 240px;
}

.lowcode-form {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.form-shell__footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 20px;
}

@media (max-width: 960px) {
  .page-head,
  .form-shell__summary {
    grid-template-columns: 1fr;
    flex-direction: column;
  }
}
</style>
