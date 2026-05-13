<template>
  <a-modal
    v-model:open="visible"
    :title="t('system.tenant.initProgress.title')"
    :footer="null"
    :closable="false"
    width="600px"
  >
    <div class="progress-container">
      <a-progress :percent="progress" :status="taskStatus" :format="formatProgress" />

      <div class="current-step">
        <a-spin :spinning="loading">
          {{ currentStepLabel }}
        </a-spin>
      </div>

      <a-timeline class="step-timeline">
        <a-timeline-item v-for="step in steps" :key="step.key" :color="getStepColor(step)">
          <template #dot>
            <CheckCircleOutlined v-if="step.status === 'finished'" style="color: #52c41a" />
            <LoadingOutlined v-else-if="step.status === 'processing'" style="color: #1890ff" />
            <CloseCircleOutlined v-else style="color: #d9d9d9" />
          </template>
          {{ step.name }}
        </a-timeline-item>
      </a-timeline>

      <a-alert
        v-if="errorMessage"
        type="error"
        :message="errorMessage"
        show-icon
        style="margin-top: 16px"
      />
    </div>
  </a-modal>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { CheckCircleOutlined, CloseCircleOutlined, LoadingOutlined } from '@ant-design/icons-vue'
import { getTaskDetail, subscribeTaskProgress, type ProgressPushData } from '@/api/system/tenantInitTask'

interface Step {
  key: string
  name: string
  status: 'waiting' | 'processing' | 'finished'
}

const props = defineProps<{ taskId: number }>()
const emit = defineEmits<{ (e: 'finish'): void }>()
const { t } = useI18n()

const visible = ref(true)
const loading = ref(true)
const progress = ref(0)
const currentStepText = ref('')
const taskStatus = ref<'active' | 'exception' | 'success'>('active')
const errorMessage = ref('')
let eventSource: EventSource | null = null

const steps = reactive<Step[]>([
  { key: 'module', name: t('system.tenant.initProgress.steps.module'), status: 'waiting' },
  { key: 'menu', name: t('system.tenant.initProgress.steps.menu'), status: 'waiting' },
  { key: 'role', name: t('system.tenant.initProgress.steps.role'), status: 'waiting' },
  { key: 'user', name: t('system.tenant.initProgress.steps.user'), status: 'waiting' },
  { key: 'bind_role', name: t('system.tenant.initProgress.steps.bindRole'), status: 'waiting' },
  { key: 'bind_menu', name: t('system.tenant.initProgress.steps.bindMenu'), status: 'waiting' },
  { key: 'template', name: t('system.tenant.initProgress.steps.template'), status: 'waiting' },
  { key: 'table_config', name: t('system.tenant.initProgress.steps.tableConfig'), status: 'waiting' },
])

const formatProgress = (percent?: number) => {
  if (taskStatus.value === 'success') return t('system.tenant.initProgress.success')
  if (taskStatus.value === 'exception') return t('system.tenant.initProgress.failed')
  return `${percent || 0}%`
}

const currentStepLabel = computed(() => currentStepText.value || t('system.tenant.initProgress.currentStepUnknown'))

function getStepColor(step: Step) {
  if (step.status === 'finished') return 'green'
  if (step.status === 'processing') return 'blue'
  return 'gray'
}

function updateStepStatus(stepName: string) {
  const index = steps.findIndex(step => stepName.includes(step.name))
  steps.forEach((step, i) => {
    if (index === -1) {
      step.status = 'waiting'
    } else if (i < index) {
      step.status = 'finished'
    } else if (i === index) {
      step.status = 'processing'
    } else {
      step.status = 'waiting'
    }
  })
}

async function initSSE() {
  try {
    const taskDetail = await getTaskDetail(props.taskId)
    if (taskDetail) {
      progress.value = taskDetail.progress || 0
      currentStepText.value = taskDetail.currentStep || ''

      if (taskDetail.status === 'SUCCESS') {
        taskStatus.value = 'success'
        loading.value = false
        setTimeout(() => {
          visible.value = false
          emit('finish')
        }, 1200)
        return
      }

      if (taskDetail.status === 'FAILED') {
        taskStatus.value = 'exception'
        errorMessage.value = taskDetail.errorMessage || t('system.tenant.initProgress.failed')
        loading.value = false
        return
      }
    }

    eventSource = subscribeTaskProgress(props.taskId)
    eventSource.onmessage = event => {
      const data = JSON.parse(event.data) as ProgressPushData
      progress.value = data.progress
      currentStepText.value = data.currentStep
      updateStepStatus(data.currentStep)
      if (data.progress >= 100) {
        taskStatus.value = 'success'
        loading.value = false
        setTimeout(() => {
          visible.value = false
          emit('finish')
        }, 1200)
      }
    }
    eventSource.onerror = () => {
      taskStatus.value = 'exception'
      errorMessage.value = t('system.tenant.initProgress.connectFailed')
      loading.value = false
      eventSource?.close()
      eventSource = null
    }
  } catch (error) {
    console.error('Tenant init SSE failed:', error)
    taskStatus.value = 'exception'
    errorMessage.value = t('system.tenant.initProgress.fetchFailed')
    loading.value = false
  }
}

onMounted(initSSE)

onUnmounted(() => {
  eventSource?.close()
  eventSource = null
})
</script>

<style scoped lang="less" src="@/styles/components/tenant/tenant-init-progress.less"></style>
