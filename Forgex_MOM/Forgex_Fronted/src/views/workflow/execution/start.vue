<template>
  <div class="approval-start-page">
    <section class="hero-panel">
      <div>
        <p class="hero-panel__eyebrow">{{ t('workflow.execution.startPage.eyebrow') }}</p>
        <h2 class="hero-panel__title">{{ t('workflow.execution.startPage.title') }}</h2>
        <p class="hero-panel__desc">
          {{ t('workflow.execution.startPage.desc') }}
        </p>
      </div>

      <div class="hero-panel__stats">
        <div class="hero-panel__stat">
          <span>{{ t('workflow.execution.startPage.availableCount') }}</span>
          <strong>{{ taskList.length }}</strong>
        </div>
        <div class="hero-panel__stat">
          <span>{{ t('workflow.execution.startPage.filteredCount') }}</span>
          <strong>{{ filteredTasks.length }}</strong>
        </div>
      </div>
    </section>

    <section class="board">
      <aside class="sidebar">
        <div class="panel">
          <div class="panel__title">{{ t('workflow.execution.startPage.categoryFilter') }}</div>
          <button
            v-for="category in categoryOptions"
            :key="category.key"
            type="button"
            class="filter-item"
            :class="{ 'filter-item--active': activeCategory === category.key }"
            @click="activeCategory = category.key"
          >
            <span>{{ category.label }}</span>
            <strong>{{ category.count }}</strong>
          </button>
        </div>

        <div v-if="recentTasks.length" class="panel">
          <div class="panel__title">{{ t('workflow.execution.startPage.recentTitle') }}</div>
          <button
            v-for="task in recentTasks"
            :key="task.taskCode"
            type="button"
            class="recent-item"
            @click="handleOpenTask(task)"
          >
            <component :is="getTaskIcon(task)" />
            <div>
              <div class="recent-item__name">{{ task.taskName }}</div>
              <div class="recent-item__meta">{{ getTaskCategoryLabel(getTaskCategoryKey(task)) }}</div>
            </div>
          </button>
        </div>
      </aside>

      <section class="content-panel">
        <div class="toolbar">
          <div>
            <div class="toolbar__title">{{ t('workflow.execution.startPage.taskListTitle') }}</div>
            <div class="toolbar__meta">{{ t('workflow.execution.startPage.taskListDesc') }}</div>
          </div>

          <a-input
            v-model:value="searchKeyword"
            allow-clear
            class="toolbar__search"
            :placeholder="t('workflow.execution.startPage.searchPlaceholder')"
          >
            <template #prefix>
              <SearchOutlined />
            </template>
          </a-input>
        </div>

        <div v-if="taskListLoading" class="state-wrap">
          <a-spin />
        </div>

        <div v-else-if="filteredTasks.length" class="task-grid">
          <button
            v-for="task in filteredTasks"
            :key="task.taskCode"
            type="button"
            class="task-card"
            @click="handleOpenTask(task)"
          >
            <div class="task-card__icon" :class="`task-card__icon--${getTaskAccent(task)}`">
              <component :is="getTaskIcon(task)" />
            </div>

            <div class="task-card__body">
              <div class="task-card__header">
                <div>
                  <div class="task-card__name">{{ task.taskName }}</div>
                  <div class="task-card__category">{{ getTaskCategoryLabel(getTaskCategoryKey(task)) }}</div>
                </div>
                <a-tag :color="task.formType === 1 ? 'blue' : 'green'">
                  {{ task.formType === 1 ? t('workflow.execution.startPage.customForm') : t('workflow.execution.startPage.lowCodeForm') }}
                </a-tag>
              </div>

              <p class="task-card__remark">
                {{ task.remark || t('workflow.execution.startPage.remarkFallback') }}
              </p>

              <div class="task-card__footer">
                <span>{{ task.taskCode }}</span>
              </div>

              <div class="task-card__action">
                <span class="task-card__open">
                  {{ t('workflow.execution.startPage.openForm') }}
                  <ArrowRightOutlined />
                </span>
              </div>
            </div>
          </button>
        </div>

        <a-empty v-else class="state-wrap" :description="t('workflow.execution.startPage.empty')" />
      </section>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { message } from 'ant-design-vue'
import { useI18n } from 'vue-i18n'
import {
  AppstoreOutlined,
  ArrowRightOutlined,
  CalendarOutlined,
  FileTextOutlined,
  SearchOutlined,
  TeamOutlined,
} from '@ant-design/icons-vue'
import { useRouter } from 'vue-router'
import { approvalRoutePaths } from '@/router/approvalRoutePaths'
import { useDict } from '@/hooks/useDict'
import { listTaskConfig, type WfTaskConfigDTO } from '@/api/workflow/taskConfig'

interface TaskCategoryOption {
  key: string
  label: string
  count: number
}

const RECENT_TASK_STORAGE_KEY = 'workflow-recent-task-codes'

const { t } = useI18n({ useScope: 'global' })
const router = useRouter()
const { dictItems: taskCategoryOptions } = useDict('wf_task_category')
const taskListLoading = ref(false)
const taskList = ref<WfTaskConfigDTO[]>([])
const recentTaskCodes = ref<string[]>(loadRecentTaskCodes())
const searchKeyword = ref('')
const activeCategory = ref('all')

const categoryOptions = computed<TaskCategoryOption[]>(() => {
  const counters = new Map<string, number>()
  taskList.value.forEach(task => {
    const category = getTaskCategoryKey(task)
    counters.set(category, (counters.get(category) || 0) + 1)
  })
  return [
    { key: 'all', label: t('workflow.execution.startPage.categoryAll'), count: taskList.value.length },
    ...Array.from(counters.entries()).map(([key, count]) => ({
      key,
      label: getTaskCategoryLabel(key),
      count,
    })),
  ]
})

const filteredTasks = computed(() => {
  const keyword = searchKeyword.value.trim().toLowerCase()
  return [...taskList.value]
    .filter(task => {
      if (activeCategory.value !== 'all' && getTaskCategoryKey(task) !== activeCategory.value) {
        return false
      }
      if (!keyword) {
        return true
      }
      return [task.taskName, task.taskCode, task.remark, getTaskCategoryLabel(getTaskCategoryKey(task))]
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
      return left.taskName.localeCompare(right.taskName)
    })
})

const recentTasks = computed(() =>
  recentTaskCodes.value
    .map(taskCode => taskList.value.find(task => task.taskCode === taskCode))
    .filter((task): task is WfTaskConfigDTO => Boolean(task))
)

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

function getTaskCategoryKey(task: WfTaskConfigDTO) {
  return task.categoryCode || 'general'
}

function getTaskCategoryLabel(categoryKey: string) {
  const matched = (taskCategoryOptions.value || []).find((item: { value: string | number; label: string }) =>
    String(item.value) === categoryKey
  )
  if (matched?.label) {
    return matched.label
  }
  return categoryKey === 'general'
    ? t('workflow.execution.startPage.categoryGeneral')
    : categoryKey
}

function getTaskIcon(task: WfTaskConfigDTO) {
  const category = getTaskCategoryKey(task)
  if (task.taskCode === 'LEAVE_APPROVAL_DEMO' || category === 'hr') return CalendarOutlined
  if (category === 'contract' || category === 'finance') return FileTextOutlined
  if (category === 'project') return TeamOutlined
  return AppstoreOutlined
}

function getTaskAccent(task: WfTaskConfigDTO) {
  if (task.taskCode === 'LEAVE_APPROVAL_DEMO') return 'orange'
  switch (getTaskCategoryKey(task)) {
    case 'hr':
      return 'blue'
    case 'finance':
      return 'green'
    case 'contract':
      return 'pink'
    default:
      return 'violet'
  }
}

async function loadTaskList() {
  try {
    taskListLoading.value = true
    taskList.value = (await listTaskConfig({ status: 1 })) || []
  } catch (error: any) {
    message.error(error.message || t('workflow.execution.startPage.loadFailed'))
  } finally {
    taskListLoading.value = false
  }
}

function handleOpenTask(task: WfTaskConfigDTO) {
  persistRecentTask(task.taskCode)
  router.push(approvalRoutePaths.executionStartForm(task.taskCode))
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
  color: var(--fx-text-primary);
}

.hero-panel {
  display: flex;
  justify-content: space-between;
  gap: 24px;
  padding: 24px 28px;
  border: 1px solid var(--fx-border-color);
  border-radius: 24px;
  background: var(--fx-bg-container);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.08);
}

.hero-panel__eyebrow {
  margin: 0 0 8px;
  font-size: 12px;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: var(--fx-text-tertiary);
}

.hero-panel__title {
  margin: 0;
  font-size: 28px;
  font-weight: 700;
  color: var(--fx-text-primary);
}

.hero-panel__desc {
  max-width: 640px;
  margin: 12px 0 0;
  line-height: 1.7;
  color: var(--fx-text-secondary);
}

.hero-panel__stats {
  display: grid;
  grid-template-columns: repeat(2, minmax(120px, 1fr));
  gap: 12px;
  min-width: 260px;
}

.hero-panel__stat {
  padding: 16px 18px;
  border: 1px solid var(--fx-border-color);
  border-radius: 18px;
  background: var(--fx-fill-alter);
}

.hero-panel__stat span {
  display: block;
  margin-bottom: 10px;
  font-size: 13px;
  color: var(--fx-text-tertiary);
}

.hero-panel__stat strong {
  font-size: 28px;
  color: var(--fx-text-primary);
}

.board {
  display: grid;
  grid-template-columns: 280px minmax(0, 1fr);
  gap: 18px;
  min-height: 0;
}

.sidebar,
.content-panel {
  min-height: 0;
}

.sidebar {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.panel,
.content-panel {
  padding: 18px;
  border: 1px solid var(--fx-border-color);
  border-radius: 22px;
  background: var(--fx-bg-container);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.04);
}

.panel__title,
.toolbar__title {
  font-size: 16px;
  font-weight: 600;
  color: var(--fx-text-primary);
}

.filter-item,
.recent-item,
.task-card {
  width: 100%;
  border: 0;
  cursor: pointer;
}

.filter-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 10px;
  padding: 12px 14px;
  border-radius: 14px;
  color: var(--fx-text-secondary);
  background: var(--fx-fill-alter);
  transition: background-color 0.2s ease, color 0.2s ease, box-shadow 0.2s ease;
}

.filter-item--active {
  color: var(--fx-primary);
  background: color-mix(in srgb, var(--fx-primary, #1677ff) 12%, var(--fx-bg-container, #ffffff));
  box-shadow: inset 0 0 0 1px color-mix(in srgb, var(--fx-primary, #1677ff) 28%, transparent);
}

.recent-item {
  display: flex;
  gap: 12px;
  align-items: center;
  margin-top: 10px;
  padding: 12px 14px;
  border-radius: 14px;
  color: var(--fx-text-secondary);
  background: var(--fx-fill-alter);
}

.recent-item__name {
  font-weight: 600;
}

.recent-item__meta,
.toolbar__meta {
  font-size: 12px;
  color: var(--fx-text-tertiary);
}

.toolbar {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: center;
  margin-bottom: 18px;
}

.toolbar__search {
  max-width: 320px;
}

.task-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
}

.task-card {
  display: flex;
  gap: 16px;
  padding: 18px;
  text-align: left;
  border-radius: 20px;
  background: var(--fx-bg-elevated);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.task-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 18px 30px rgba(0, 0, 0, 0.12);
}

.task-card__icon {
  width: 52px;
  height: 52px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  color: #fff;
}

.task-card__icon--orange { background: linear-gradient(135deg, #f59e0b, #f97316); }
.task-card__icon--blue { background: linear-gradient(135deg, #3b82f6, #06b6d4); }
.task-card__icon--green { background: linear-gradient(135deg, #22c55e, #14b8a6); }
.task-card__icon--pink { background: linear-gradient(135deg, #ec4899, #f97316); }
.task-card__icon--violet { background: linear-gradient(135deg, #8b5cf6, #6366f1); }

.task-card__body {
  flex: 1;
  min-width: 0;
}

.task-card__header,
.task-card__footer {
  display: flex;
  justify-content: space-between;
  gap: 12px;
}

.task-card__name {
  font-size: 16px;
  font-weight: 700;
  color: var(--fx-text-primary);
}

.task-card__category {
  margin-top: 4px;
  font-size: 12px;
  color: var(--fx-text-tertiary);
}

.task-card__remark {
  min-height: 48px;
  margin: 14px 0;
  line-height: 1.65;
  color: var(--fx-text-secondary);
}

.task-card__footer {
  align-items: center;
  color: var(--fx-text-tertiary);
  font-size: 12px;
}

.task-card__action {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
}

.task-card__open {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: var(--fx-primary);
  font-weight: 600;
}

.state-wrap {
  min-height: 320px;
  display: flex;
  align-items: center;
  justify-content: center;
}

@media (max-width: 960px) {
  .hero-panel,
  .toolbar,
  .board {
    grid-template-columns: 1fr;
    flex-direction: column;
  }

  .hero-panel__stats,
  .board {
    min-width: 0;
  }
}
</style>
