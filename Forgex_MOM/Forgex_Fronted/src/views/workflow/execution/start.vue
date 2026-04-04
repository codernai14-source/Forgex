<template>
  <div class="approval-start-page">
    <section class="hero-panel">
      <div>
        <p class="hero-panel__eyebrow">{{ $t('workflow.execution.startCenter') }}</p>
        <h2 class="hero-panel__title">{{ $t('workflow.execution.startTitle') }}</h2>
        <p class="hero-panel__desc">
          {{ $t('workflow.execution.startDesc') }}
        </p>
      </div>

      <div class="hero-panel__stats">
        <div class="hero-panel__stat">
          <span class="hero-panel__stat-label">{{ $t('workflow.execution.enabledTasks') }}</span>
          <strong>{{ taskList.length }}</strong>
        </div>
        <div class="hero-panel__stat">
          <span class="hero-panel__stat-label">{{ $t('workflow.execution.filteredTasks') }}</span>
          <strong>{{ filteredTasks.length }}</strong>
        </div>
      </div>
    </section>

    <section class="start-layout">
      <aside class="filter-panel">
        <div class="filter-card">
          <div class="filter-card__header">
            <span>{{ $t('workflow.execution.categoryTitle') }}</span>
          </div>

          <button
            v-for="category in categoryOptions"
            :key="category.key"
            type="button"
            class="filter-card__item"
            :class="{ 'filter-card__item--active': activeCategory === category.key }"
            @click="activeCategory = category.key"
          >
            <span>{{ category.label }}</span>
            <strong>{{ category.count }}</strong>
          </button>
        </div>

        <div v-if="recentTasks.length" class="filter-card">
          <div class="filter-card__header">
            <span>{{ $t('workflow.execution.recentTitle') }}</span>
          </div>

          <button
            v-for="task in recentTasks"
            :key="task.taskCode"
            type="button"
            class="filter-card__recent"
            @click="handleTaskSelect(task)"
          >
            <component :is="getTaskIcon(task)" class="filter-card__recent-icon" />
            <div>
              <div class="filter-card__recent-name">{{ task.taskName }}</div>
              <div class="filter-card__recent-meta">{{ getTaskCategory(task) }}</div>
            </div>
          </button>
        </div>
      </aside>

      <section class="content-panel">
        <div class="content-toolbar">
          <div class="content-toolbar__title">
            <span>{{ $t('workflow.execution.allFlows') }}</span>
            <em>{{ filteredTasks.length }} {{ $t('workflow.execution.flowCount') }}</em>
          </div>

          <a-input
            v-model:value="searchKeyword"
            allow-clear
            class="content-toolbar__search"
            :placeholder="$t('workflow.execution.searchPlaceholder')"
          >
            <template #prefix>
              <SearchOutlined />
            </template>
          </a-input>
        </div>

        <div v-if="filteredTasks.length" class="task-grid">
          <button
            v-for="task in filteredTasks"
            :key="task.taskCode"
            type="button"
            class="task-card"
            :class="{ 'task-card--active': currentTask?.taskCode === task.taskCode }"
            @click="handleTaskSelect(task)"
          >
            <div class="task-card__icon" :class="`task-card__icon--${getTaskAccent(task)}`">
              <component :is="getTaskIcon(task)" />
            </div>

            <div class="task-card__body">
              <div class="task-card__header">
                <div>
                  <div class="task-card__name">{{ task.taskName }}</div>
                  <div class="task-card__category">{{ getTaskCategory(task) }}</div>
                </div>
                <a-tag :color="task.formType === 1 ? 'blue' : 'green'">
                  {{ task.formType === 1 ? $t('workflow.execution.customForm') : $t('workflow.execution.lowCodeForm') }}
                </a-tag>
              </div>

              <p class="task-card__remark">
                {{ task.remark || $t('workflow.execution.defaultRemark') }}
              </p>
            </div>

            <div class="task-card__action">
              <span>{{ $t('workflow.execution.launchTask') }}</span>
              <ArrowRightOutlined />
            </div>
          </button>
        </div>

        <a-empty v-else class="content-panel__empty" :description="$t('workflow.execution.emptyTask')" />
      </section>
    </section>

    <a-card v-if="currentTask" :bordered="false" class="launch-panel">
      <div class="launch-panel__header">
        <div>
          <div class="launch-panel__eyebrow">{{ $t('workflow.execution.currentSelection') }}</div>
          <div class="launch-panel__title">{{ currentTask.taskName }}</div>
          <div class="launch-panel__meta">
            <span>{{ getTaskCategory(currentTask) }}</span>
            <span>{{ currentTask.taskCode }}</span>
          </div>
        </div>

        <a-space>
          <a-tag :color="currentTask.formType === 1 ? 'blue' : 'green'">
            {{ currentTask.formType === 1 ? $t('workflow.execution.customForm') : $t('workflow.execution.lowCodeForm') }}
          </a-tag>
          <a-button @click="handleReset">{{ $t('common.reset') }}</a-button>
        </a-space>
      </div>

      <div class="launch-panel__remark">
        {{ currentTask.remark || $t('workflow.execution.defaultRemark') }}
      </div>

      <div class="launch-panel__form">
        <component
          :is="dynamicFormComponent"
          v-if="currentTask.formType === 1 && dynamicFormComponent"
          ref="dynamicFormRef"
          v-model="customFormData"
        />

        <a-alert
          v-else-if="currentTask.formType === 1"
          type="warning"
          show-icon
          :message="$t('workflow.execution.formNotRegistered')"
          :description="currentTask.formPath"
        />

        <div
          v-else-if="currentTask.formType === 2 && currentTask.formContent"
          class="lowcode-form"
        >
          <a-alert
            :message="$t('workflow.execution.lowCodeRenderer')"
            :description="$t('workflow.execution.lowCodeDesc')"
            type="info"
            show-icon
          />
          <a-textarea
            v-model:value="customFormData"
            :rows="10"
            :placeholder="$t('workflow.execution.lowCodePlaceholder')"
          />
        </div>
      </div>

      <div class="launch-panel__footer">
        <a-button
          type="primary"
          size="large"
          :loading="submitting"
          v-permission="'wf:execution:start'"
          @click="handleSubmit"
        >
          {{ $t('workflow.execution.submitApproval') }}
        </a-button>
      </div>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { computed, defineAsyncComponent, onMounted, ref } from 'vue'
import { message } from 'ant-design-vue'
import {
  AppstoreOutlined,
  ArrowRightOutlined,
  CalendarOutlined,
  FileTextOutlined,
  SearchOutlined,
  TeamOutlined
} from '@ant-design/icons-vue'
import { useRouter } from 'vue-router'
import {
  listTaskConfig,
  type WfTaskConfigDTO
} from '@/api/workflow/taskConfig'
import {
  startExecution,
  type WfExecutionStartParam
} from '@/api/workflow/execution'

interface DynamicFormExpose {
  validate?: () => Promise<Record<string, any> | void>
  reset?: () => void
}

interface TaskCategoryOption {
  key: string
  label: string
  count: number
}

const RECENT_TASK_STORAGE_KEY = 'workflow-recent-task-codes'

const workflowFormRegistry: Record<string, ReturnType<typeof defineAsyncComponent>> = {
  '/workflow/form/leave': defineAsyncComponent(() => import('@/views/workflow/form/LeaveForm.vue'))
}

const router = useRouter()

const taskListLoading = ref(false)
const taskList = ref<WfTaskConfigDTO[]>([])
const currentTask = ref<WfTaskConfigDTO | null>(null)
const recentTaskCodes = ref<string[]>(loadRecentTaskCodes())
const searchKeyword = ref('')
const activeCategory = ref('all')
const customFormData = ref<Record<string, any> | string>({})
const dynamicFormRef = ref<DynamicFormExpose>()
const submitting = ref(false)

const categoryOptions = computed<TaskCategoryOption[]>(() => {
  const counters = new Map<string, number>()

  taskList.value.forEach(task => {
    const category = getTaskCategory(task)
    counters.set(category, (counters.get(category) || 0) + 1)
  })

  return [
    {
      key: 'all',
      label: '全部流程',
      count: taskList.value.length
    },
    ...Array.from(counters.entries()).map(([label, count]) => ({
      key: label,
      label,
      count
    }))
  ]
})

const filteredTasks = computed(() => {
  const keyword = searchKeyword.value.trim().toLowerCase()

  return [...taskList.value]
    .filter(task => {
      if (activeCategory.value !== 'all' && getTaskCategory(task) !== activeCategory.value) {
        return false
      }

      if (!keyword) {
        return true
      }

      return [
        task.taskName,
        task.taskCode,
        task.remark,
        getTaskCategory(task)
      ]
        .filter(Boolean)
        .some(value => String(value).toLowerCase().includes(keyword))
    })
    .sort((left, right) => {
      const leftRecentIndex = recentTaskCodes.value.indexOf(left.taskCode)
      const rightRecentIndex = recentTaskCodes.value.indexOf(right.taskCode)

      if (leftRecentIndex !== rightRecentIndex) {
        if (leftRecentIndex === -1) return 1
        if (rightRecentIndex === -1) return -1
        return leftRecentIndex - rightRecentIndex
      }

      if (left.taskCode === 'LEAVE_APPROVAL_DEMO') return -1
      if (right.taskCode === 'LEAVE_APPROVAL_DEMO') return 1

      return left.taskName.localeCompare(right.taskName)
    })
})

const recentTasks = computed(() =>
  recentTaskCodes.value
    .map(taskCode => taskList.value.find(task => task.taskCode === taskCode))
    .filter((task): task is WfTaskConfigDTO => Boolean(task))
)

const dynamicFormComponent = computed(() => {
  if (!currentTask.value?.formPath) {
    return null
  }

  return workflowFormRegistry[currentTask.value.formPath] || null
})

function loadRecentTaskCodes(): string[] {
  try {
    const rawValue = localStorage.getItem(RECENT_TASK_STORAGE_KEY)
    if (!rawValue) {
      return []
    }

    const parsed = JSON.parse(rawValue)
    return Array.isArray(parsed) ? parsed.filter(item => typeof item === 'string') : []
  } catch {
    return []
  }
}

function persistRecentTask(taskCode: string) {
  const merged = [taskCode, ...recentTaskCodes.value.filter(code => code !== taskCode)].slice(0, 6)
  recentTaskCodes.value = merged
  localStorage.setItem(RECENT_TASK_STORAGE_KEY, JSON.stringify(merged))
}

function getTaskCategory(task: WfTaskConfigDTO) {
  const content = `${task.taskName}${task.taskCode}${task.remark || ''}`.toLowerCase()

  if (content.includes('请假') || content.includes('leave') || content.includes('hr')) {
    return '人事类'
  }
  if (content.includes('合同') || content.includes('contract')) {
    return '合同类'
  }
  if (content.includes('财务') || content.includes('expense') || content.includes('付款')) {
    return '财务类'
  }
  if (content.includes('采购') || content.includes('项目')) {
    return '项目类'
  }

  return '通用类'
}

function getTaskIcon(task: WfTaskConfigDTO) {
  const category = getTaskCategory(task)

  if (task.taskCode === 'LEAVE_APPROVAL_DEMO' || category === '人事类') {
    return CalendarOutlined
  }
  if (category === '合同类' || category === '财务类') {
    return FileTextOutlined
  }
  if (category === '项目类') {
    return TeamOutlined
  }
  return AppstoreOutlined
}

function getTaskAccent(task: WfTaskConfigDTO) {
  if (task.taskCode === 'LEAVE_APPROVAL_DEMO') {
    return 'orange'
  }

  switch (getTaskCategory(task)) {
    case '人事类':
      return 'blue'
    case '财务类':
      return 'green'
    case '合同类':
      return 'pink'
    default:
      return 'violet'
  }
}

function selectDefaultTask() {
  if (!taskList.value.length) {
    currentTask.value = null
    return
  }

  const preferredTaskCode =
    recentTaskCodes.value.find(taskCode => taskList.value.some(task => task.taskCode === taskCode)) ||
    taskList.value.find(task => task.taskCode === 'LEAVE_APPROVAL_DEMO')?.taskCode ||
    taskList.value[0].taskCode

  const preferredTask = taskList.value.find(task => task.taskCode === preferredTaskCode)
  if (preferredTask) {
    handleTaskSelect(preferredTask, false)
  }
}

function handleTaskSelect(task: WfTaskConfigDTO, storeRecent = true) {
  currentTask.value = task

  if (storeRecent) {
    persistRecentTask(task.taskCode)
  }

  if (task.formType === 1) {
    customFormData.value = {}
    dynamicFormRef.value?.reset?.()
  } else {
    customFormData.value = task.formContent ? task.formContent : '{}'
  }
}

async function loadTaskList() {
  try {
    taskListLoading.value = true
    const result = await listTaskConfig({ status: 1 })
    taskList.value = result || []
    if (!currentTask.value) {
      selectDefaultTask()
    }
  } catch (error: any) {
    message.error(error.message || '加载审批任务列表失败')
  } finally {
    taskListLoading.value = false
  }
}

function handleReset() {
  customFormData.value = {}
  dynamicFormRef.value?.reset?.()
}

async function handleSubmit() {
  if (!currentTask.value) {
    message.error('请选择要发起的审批流程')
    return
  }

  try {
    let formContent = ''

    if (currentTask.value.formType === 1) {
      if (!dynamicFormComponent.value) {
        message.error('当前流程暂未绑定前端表单组件')
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
      if (!customFormData.value) {
        message.warning('请先填写表单内容')
        return
      }

      if (typeof customFormData.value === 'string') {
        JSON.parse(customFormData.value)
        formContent = customFormData.value
      } else {
        formContent = JSON.stringify(customFormData.value)
      }
    }

    submitting.value = true
    const params: WfExecutionStartParam = {
      taskCode: currentTask.value.taskCode,
      formContent
    }

    const executionId = await startExecution(params)
    persistRecentTask(currentTask.value.taskCode)
    message.success('审批已成功发起')

    router.push({
      path: '/workflow/my/initiated',
      query: { executionId }
    })
  } catch (error: any) {
    if (error?.message?.includes('JSON')) {
      message.error('低代码表单内容必须是合法的 JSON')
      return
    }

    message.error(error.message || '发起审批失败')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadTaskList()
})
</script>

<style scoped>
.approval-start-page {
  display: flex;
  flex-direction: column;
  gap: 18px;
  min-height: 100%;
  padding: 16px;
  color: #eef3ff;
}

.hero-panel {
  display: flex;
  justify-content: space-between;
  gap: 24px;
  padding: 24px 28px;
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 24px;
  background:
    radial-gradient(circle at top left, rgba(245, 166, 35, 0.28), transparent 42%),
    linear-gradient(135deg, rgba(19, 25, 37, 0.98), rgba(14, 18, 27, 0.92));
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.18);
}

.hero-panel__eyebrow,
.launch-panel__eyebrow {
  margin: 0 0 8px;
  font-size: 12px;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: rgba(255, 255, 255, 0.62);
}

.hero-panel__title,
.launch-panel__title {
  margin: 0;
  font-size: 28px;
  font-weight: 700;
  color: #ffffff;
}

.hero-panel__desc {
  max-width: 640px;
  margin: 12px 0 0;
  line-height: 1.7;
  color: rgba(255, 255, 255, 0.72);
}

.hero-panel__stats {
  display: grid;
  grid-template-columns: repeat(2, minmax(120px, 1fr));
  gap: 12px;
  min-width: 260px;
}

.hero-panel__stat {
  padding: 16px 18px;
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.04);
  backdrop-filter: blur(10px);
}

.hero-panel__stat-label {
  display: block;
  margin-bottom: 10px;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.62);
}

.hero-panel__stat strong {
  font-size: 28px;
  color: #ffffff;
}

.start-layout {
  display: grid;
  grid-template-columns: 280px minmax(0, 1fr);
  gap: 18px;
  min-height: 0;
}

.filter-panel {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.filter-card {
  padding: 18px;
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: 22px;
  background: rgba(20, 26, 37, 0.92);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.04);
}

.filter-card__header {
  margin-bottom: 14px;
  font-size: 14px;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.88);
}

.filter-card__item,
.filter-card__recent {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  border: 0;
  background: transparent;
  color: rgba(255, 255, 255, 0.74);
  cursor: pointer;
}

.filter-card__item {
  margin-bottom: 8px;
  padding: 12px 14px;
  border-radius: 16px;
  transition: background-color 0.2s ease, color 0.2s ease, transform 0.2s ease;
}

.filter-card__item:last-child {
  margin-bottom: 0;
}

.filter-card__item strong {
  color: rgba(255, 255, 255, 0.48);
}

.filter-card__item:hover,
.filter-card__recent:hover {
  transform: translateY(-1px);
}

.filter-card__item--active {
  background: linear-gradient(135deg, rgba(245, 166, 35, 0.22), rgba(245, 166, 35, 0.08));
  color: #ffffff;
}

.filter-card__item--active strong {
  color: #ffd18a;
}

.filter-card__recent {
  gap: 12px;
  justify-content: flex-start;
  margin-bottom: 12px;
  padding: 12px 14px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.03);
  transition: transform 0.2s ease, background-color 0.2s ease;
}

.filter-card__recent:last-child {
  margin-bottom: 0;
}

.filter-card__recent-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 38px;
  height: 38px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.08);
  color: #ffb84d;
  font-size: 18px;
}

.filter-card__recent-name {
  font-size: 14px;
  font-weight: 600;
  color: #ffffff;
}

.filter-card__recent-meta {
  margin-top: 4px;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.52);
}

.content-panel {
  display: flex;
  flex-direction: column;
  gap: 18px;
  padding: 18px;
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: 24px;
  background: rgba(18, 22, 31, 0.96);
}

.content-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.content-toolbar__title {
  display: flex;
  align-items: baseline;
  gap: 10px;
  font-size: 22px;
  font-weight: 700;
  color: #ffffff;
}

.content-toolbar__title em {
  font-size: 14px;
  font-style: normal;
  font-weight: 500;
  color: rgba(255, 255, 255, 0.52);
}

.content-toolbar__search {
  max-width: 320px;
}

.content-toolbar__search :deep(.ant-input-affix-wrapper) {
  border-radius: 14px;
  border-color: rgba(255, 255, 255, 0.1);
  background: rgba(255, 255, 255, 0.03);
}

.task-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
}

.task-card {
  display: flex;
  flex-direction: column;
  gap: 18px;
  padding: 18px;
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: 22px;
  background: linear-gradient(180deg, rgba(28, 34, 47, 0.95), rgba(20, 25, 35, 0.95));
  color: inherit;
  text-align: left;
  cursor: pointer;
  transition: transform 0.2s ease, border-color 0.2s ease, box-shadow 0.2s ease;
}

.task-card:hover,
.task-card--active {
  border-color: rgba(245, 166, 35, 0.4);
  box-shadow: 0 18px 36px rgba(0, 0, 0, 0.22);
  transform: translateY(-2px);
}

.task-card__icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 54px;
  height: 54px;
  border-radius: 18px;
  font-size: 24px;
}

.task-card__icon--orange {
  background: rgba(245, 166, 35, 0.16);
  color: #ffb84d;
}

.task-card__icon--blue {
  background: rgba(64, 158, 255, 0.18);
  color: #78bbff;
}

.task-card__icon--green {
  background: rgba(82, 196, 26, 0.18);
  color: #8de45c;
}

.task-card__icon--pink {
  background: rgba(245, 34, 45, 0.16);
  color: #ff8b93;
}

.task-card__icon--violet {
  background: rgba(114, 46, 209, 0.16);
  color: #c39cff;
}

.task-card__body {
  display: flex;
  flex: 1;
  flex-direction: column;
  gap: 14px;
}

.task-card__header {
  display: flex;
  justify-content: space-between;
  gap: 12px;
}

.task-card__name {
  font-size: 22px;
  font-weight: 700;
  line-height: 1.3;
  color: #ffffff;
}

.task-card__category {
  margin-top: 6px;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.55);
}

.task-card__remark {
  margin: 0;
  line-height: 1.7;
  color: rgba(255, 255, 255, 0.64);
}

.task-card__action {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 14px;
  font-weight: 600;
  color: #ffcc7a;
}

.content-panel__empty {
  padding: 48px 0;
}

.launch-panel {
  border-radius: 24px;
  background: rgba(18, 22, 31, 0.98);
}

.launch-panel :deep(.ant-card-body) {
  display: flex;
  flex-direction: column;
  gap: 18px;
  padding: 22px 24px 24px;
}

.launch-panel__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 18px;
}

.launch-panel__meta {
  display: flex;
  gap: 14px;
  margin-top: 10px;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.52);
}

.launch-panel__remark {
  padding: 14px 16px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.04);
  line-height: 1.8;
  color: rgba(255, 255, 255, 0.72);
}

.launch-panel__form {
  min-height: 120px;
}

.launch-panel__footer {
  display: flex;
  justify-content: flex-end;
}

.lowcode-form :deep(.ant-alert) {
  margin-bottom: 16px;
}

@media (max-width: 1200px) {
  .start-layout {
    grid-template-columns: 1fr;
  }

  .filter-panel {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .approval-start-page {
    padding: 12px;
  }

  .hero-panel,
  .launch-panel__header,
  .content-toolbar {
    flex-direction: column;
  }

  .hero-panel__stats,
  .filter-panel {
    grid-template-columns: 1fr;
    width: 100%;
  }

  .content-toolbar__search {
    max-width: none;
    width: 100%;
  }

  .launch-panel__footer {
    justify-content: stretch;
  }

  .launch-panel__footer :deep(.ant-btn) {
    width: 100%;
  }
}
</style>
