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

<style scoped lang="less" src="@/styles/views/workflow/execution/start.less"></style>
