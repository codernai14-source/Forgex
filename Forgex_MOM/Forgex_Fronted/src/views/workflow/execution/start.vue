<template>
  <div class="approval-start-page">
    <section class="hero-panel">
      <div>
        <p class="hero-panel__eyebrow">Approval Center</p>
        <h2 class="hero-panel__title">选择要发起的审批流程</h2>
        <p class="hero-panel__desc">
          当前页只负责选择流程，不再直接展开表单。点击卡片后进入下一步填写并提交审批内容。
        </p>
      </div>

      <div class="hero-panel__stats">
        <div class="hero-panel__stat">
          <span>可发起流程</span>
          <strong>{{ taskList.length }}</strong>
        </div>
        <div class="hero-panel__stat">
          <span>筛选结果</span>
          <strong>{{ filteredTasks.length }}</strong>
        </div>
      </div>
    </section>

    <section class="board">
      <aside class="sidebar">
        <div class="panel">
          <div class="panel__title">分类筛选</div>
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
          <div class="panel__title">最近使用</div>
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
              <div class="recent-item__meta">{{ getTaskCategory(task) }}</div>
            </div>
          </button>
        </div>
      </aside>

      <section class="content-panel">
        <div class="toolbar">
          <div>
            <div class="toolbar__title">审批流程列表</div>
            <div class="toolbar__meta">点击任意流程卡片进入第二步填写页面</div>
          </div>

          <a-input
            v-model:value="searchKeyword"
            allow-clear
            class="toolbar__search"
            placeholder="搜索流程名称、编码、说明"
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
                  <div class="task-card__category">{{ getTaskCategory(task) }}</div>
                </div>
                <a-tag :color="task.formType === 1 ? 'blue' : 'green'">
                  {{ task.formType === 1 ? '自定义表单' : '低代码表单' }}
                </a-tag>
              </div>

              <p class="task-card__remark">
                {{ task.remark || '点击进入下一步填写审批表单。' }}
              </p>

              <div class="task-card__footer">
                <span>{{ task.taskCode }}</span>
                <span class="task-card__open">
                  进入填写
                  <ArrowRightOutlined />
                </span>
              </div>
            </div>
          </button>
        </div>

        <a-empty v-else class="state-wrap" description="暂无可发起的审批流程" />
      </section>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
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
import { listTaskConfig, type WfTaskConfigDTO } from '@/api/workflow/taskConfig'

interface TaskCategoryOption {
  key: string
  label: string
  count: number
}

const RECENT_TASK_STORAGE_KEY = 'workflow-recent-task-codes'

const router = useRouter()
const taskListLoading = ref(false)
const taskList = ref<WfTaskConfigDTO[]>([])
const recentTaskCodes = ref<string[]>(loadRecentTaskCodes())
const searchKeyword = ref('')
const activeCategory = ref('all')

const categoryOptions = computed<TaskCategoryOption[]>(() => {
  const counters = new Map<string, number>()
  taskList.value.forEach(task => {
    const category = getTaskCategory(task)
    counters.set(category, (counters.get(category) || 0) + 1)
  })
  return [
    { key: 'all', label: '全部流程', count: taskList.value.length },
    ...Array.from(counters.entries()).map(([label, count]) => ({ key: label, label, count }))
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
      return [task.taskName, task.taskCode, task.remark, getTaskCategory(task)]
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

function getTaskCategory(task: WfTaskConfigDTO) {
  const content = `${task.taskName}${task.taskCode}${task.remark || ''}`.toLowerCase()
  if (content.includes('请假') || content.includes('leave') || content.includes('hr')) return '人事类'
  if (content.includes('合同') || content.includes('contract')) return '合同类'
  if (content.includes('财务') || content.includes('expense') || content.includes('付款')) return '财务类'
  if (content.includes('采购') || content.includes('项目')) return '项目类'
  return '通用类'
}

function getTaskIcon(task: WfTaskConfigDTO) {
  const category = getTaskCategory(task)
  if (task.taskCode === 'LEAVE_APPROVAL_DEMO' || category === '人事类') return CalendarOutlined
  if (category === '合同类' || category === '财务类') return FileTextOutlined
  if (category === '项目类') return TeamOutlined
  return AppstoreOutlined
}

function getTaskAccent(task: WfTaskConfigDTO) {
  if (task.taskCode === 'LEAVE_APPROVAL_DEMO') return 'orange'
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

async function loadTaskList() {
  try {
    taskListLoading.value = true
    taskList.value = (await listTaskConfig({ status: 1 })) || []
  } catch (error: any) {
    message.error(error.message || '加载审批流程失败')
  } finally {
    taskListLoading.value = false
  }
}

function handleOpenTask(task: WfTaskConfigDTO) {
  persistRecentTask(task.taskCode)
  router.push(`/workflow/execution/start/${task.taskCode}`)
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

.hero-panel__eyebrow {
  margin: 0 0 8px;
  font-size: 12px;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: rgba(255, 255, 255, 0.62);
}

.hero-panel__title {
  margin: 0;
  font-size: 28px;
  font-weight: 700;
  color: #fff;
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
}

.hero-panel__stat span {
  display: block;
  margin-bottom: 10px;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.62);
}

.hero-panel__stat strong {
  font-size: 28px;
  color: #fff;
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
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: 22px;
  background: rgba(20, 26, 37, 0.92);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.04);
}

.panel__title,
.toolbar__title {
  font-size: 16px;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.92);
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
  color: rgba(255, 255, 255, 0.78);
  background: rgba(255, 255, 255, 0.04);
}

.filter-item--active {
  color: #fff;
  background: linear-gradient(135deg, rgba(249, 115, 22, 0.32), rgba(59, 130, 246, 0.24));
}

.recent-item {
  display: flex;
  gap: 12px;
  align-items: center;
  margin-top: 10px;
  padding: 12px 14px;
  border-radius: 14px;
  color: rgba(255, 255, 255, 0.82);
  background: rgba(255, 255, 255, 0.04);
}

.recent-item__name {
  font-weight: 600;
}

.recent-item__meta,
.toolbar__meta {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.6);
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
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.05), rgba(255, 255, 255, 0.02));
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.task-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 18px 30px rgba(0, 0, 0, 0.22);
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
  color: #fff;
}

.task-card__category {
  margin-top: 4px;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.58);
}

.task-card__remark {
  min-height: 48px;
  margin: 14px 0;
  line-height: 1.65;
  color: rgba(255, 255, 255, 0.74);
}

.task-card__footer {
  align-items: center;
  color: rgba(255, 255, 255, 0.56);
  font-size: 12px;
}

.task-card__open {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: #f8b84e;
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
