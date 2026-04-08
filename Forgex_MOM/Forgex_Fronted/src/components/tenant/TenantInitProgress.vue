<template>
  <a-modal
    v-model:open="visible"
    title="租户创建进度"
    :footer="null"
    :closable="false"
    width="600px"
  >
    <div class="progress-container">
      <!-- 进度条 -->
      <a-progress
        :percent="progress"
        :status="taskStatus"
        :format="formatProgress"
      />
      
      <!-- 当前步骤 -->
      <div class="current-step">
        <a-spin :spinning="loading">
          {{ currentStep }}
        </a-spin>
      </div>
      
      <!-- 详细步骤列表 -->
      <a-timeline class="step-timeline">
        <a-timeline-item
          v-for="step in steps"
          :key="step.key"
          :color="getStepColor(step)"
        >
          <template #dot>
            <CheckCircleOutlined v-if="step.status === 'finished'" style="color: #52c41a;" />
            <LoadingOutlined v-else-if="step.status === 'processing'" style="color: #1890ff;" />
            <CloseCircleOutlined v-else style="color: #d9d9d9;" />
          </template>
          {{ step.name }}
        </a-timeline-item>
      </a-timeline>
      
      <!-- 错误信息 -->
      <a-alert
        v-if="errorMessage"
        type="error"
        :message="errorMessage"
        show-icon
        style="margin-top: 16px;"
      />
    </div>
  </a-modal>
</template>

<script setup lang="ts">
/**
 * 租户初始化进度组件
 * 
 * 功能：
 * 1. 通过 SSE 实时订阅租户初始化任务进度
 * 2. 可视化展示初始化进度和步骤
 * 3. 支持成功/失败状态显示
 * 
 * @author Forgex Team
 * @version 1.0.0
 */
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { 
  CheckCircleOutlined, 
  LoadingOutlined, 
  CloseCircleOutlined 
} from '@ant-design/icons-vue'
import { subscribeTaskProgress, getTaskDetail, type ProgressPushData } from '@/api/system/tenantInitTask'

/**
 * 步骤接口定义
 */
interface Step {
  /** 步骤标识 */
  key: string
  /** 步骤名称 */
  name: string
  /** 步骤状态 */
  status: 'waiting' | 'processing' | 'finished'
}

/**
 * 组件 Props 定义
 */
const props = defineProps<{
  /** 任务 ID */
  taskId: number
}>()

/**
 * 组件事件定义
 */
const emit = defineEmits<{
  /** 初始化完成事件 */
  (e: 'finish'): void
}>()

/**
 * 模态框显示状态
 */
const visible = ref(true)

/**
 * 加载状态
 */
const loading = ref(true)

/**
 * 当前进度百分比
 */
const progress = ref(0)

/**
 * 当前步骤描述
 */
const currentStep = ref('')

/**
 * 任务状态：active-进行中，exception-异常，success-成功
 */
const taskStatus = ref<'active' | 'exception' | 'success'>('active')

/**
 * 错误信息
 */
const errorMessage = ref('')

/**
 * SSE 事件源实例
 */
let eventSource: EventSource | null = null

/**
 * 初始化步骤列表
 */
const steps = reactive<Step[]>([
  { key: 'module', name: '复制系统模块', status: 'waiting' },
  { key: 'menu', name: '复制菜单权限', status: 'waiting' },
  { key: 'role', name: '创建管理员角色', status: 'waiting' },
  { key: 'user', name: '创建管理员账号', status: 'waiting' },
  { key: 'bind_role', name: '绑定用户角色', status: 'waiting' },
  { key: 'bind_menu', name: '绑定角色菜单', status: 'waiting' },
  { key: 'template', name: '同步模板配置', status: 'waiting' },
  { key: 'table_config', name: '同步表格配置', status: 'waiting' }
])

/**
 * 格式化进度显示文本
 * 
 * @param percent 进度百分比
 * @returns 格式化后的文本
 */
const formatProgress = (percent?: number) => {
  if (taskStatus.value === 'success') {
    return '初始化完成'
  } else if (taskStatus.value === 'exception') {
    return '初始化失败'
  }
  return `${percent}%`
}

/**
 * 获取步骤颜色
 * 
 * @param step 步骤对象
 * @returns 颜色值
 */
const getStepColor = (step: Step) => {
  if (step.status === 'finished') return 'green'
  if (step.status === 'processing') return 'blue'
  return 'gray'
}

/**
 * 更新步骤状态
 * 
 * @param stepName 当前步骤名称
 */
const updateStepStatus = (stepName: string) => {
  const stepIndex = steps.findIndex(s => stepName.includes(s.name))
  steps.forEach((step, index) => {
    if (index < stepIndex) {
      step.status = 'finished'
    } else if (index === stepIndex) {
      step.status = 'processing'
    } else {
      step.status = 'waiting'
    }
  })
}

/**
 * 初始化 SSE 连接
 */
const initSSE = async () => {
  try {
    // 先获取任务详情，了解当前状态
    const taskDetail = await getTaskDetail(props.taskId)
    if (taskDetail) {
      progress.value = taskDetail.progress || 0
      currentStep.value = taskDetail.currentStep || ''
      
      // 如果任务已完成或失败，直接显示结果
      if (taskDetail.status === 'SUCCESS') {
        taskStatus.value = 'success'
        loading.value = false
        setTimeout(() => {
          visible.value = false
          emit('finish')
        }, 1500)
        return
      } else if (taskDetail.status === 'FAILED') {
        taskStatus.value = 'exception'
        errorMessage.value = taskDetail.errorMessage || '初始化失败'
        loading.value = false
        return
      }
    }
    
    // 创建 SSE 连接
    eventSource = subscribeTaskProgress(props.taskId)
    
    // 监听消息
    eventSource.onmessage = (event) => {
      const data = JSON.parse(event.data) as ProgressPushData
      progress.value = data.progress
      currentStep.value = data.currentStep
      
      // 更新步骤状态
      updateStepStatus(data.currentStep)
      
      // 检查是否完成
      if (data.progress === 100) {
        taskStatus.value = 'success'
        loading.value = false
        setTimeout(() => {
          visible.value = false
          emit('finish')
        }, 1500)
      }
    }
    
    // 监听错误
    eventSource.onerror = () => {
      console.error('SSE 连接错误')
      taskStatus.value = 'exception'
      errorMessage.value = '连接服务器失败，请刷新页面重试'
      loading.value = false
      
      if (eventSource) {
        eventSource.close()
        eventSource = null
      }
    }
  } catch (error) {
    console.error('初始化 SSE 失败:', error)
    taskStatus.value = 'exception'
    errorMessage.value = '获取任务状态失败'
    loading.value = false
  }
}

/**
 * 组件挂载时初始化
 */
onMounted(() => {
  initSSE()
})

/**
 * 组件卸载时清理资源
 */
onUnmounted(() => {
  if (eventSource) {
    eventSource.close()
    eventSource = null
  }
})
</script>

<style scoped>
.progress-container {
  padding: 20px;
}

.current-step {
  margin: 24px 0;
  text-align: center;
  font-size: 16px;
  color: #1890ff;
  min-height: 24px;
}

.step-timeline {
  margin-top: 24px;
  padding: 16px;
  background: #f5f5f5;
  border-radius: 4px;
}

.step-timeline :deep(.ant-timeline-item) {
  padding-bottom: 12px;
}

.step-timeline :deep(.ant-timeline-item-content) {
  font-size: 14px;
}
</style>
