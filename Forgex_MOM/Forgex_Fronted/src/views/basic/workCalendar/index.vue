<template>
  <div class="work-calendar-page">
    <div class="calendar-toolbar">
      <div class="toolbar-title">
        <a-tag color="blue">Manufacturing Calendar</a-tag>
        <h1>工作日历</h1>
        <p>{{ currentYear }} 年 {{ currentMonth }} 月 · {{ monthSummary }}</p>
      </div>
      <a-space wrap>
        <a-segmented v-model:value="viewMode" :options="viewModeOptions" />
        <a-switch v-model:checked="syncHoliday" checked-children="节假日" un-checked-children="本地" @change="reloadMonth" />
        <a-button @click="goToday">今天</a-button>
        <a-button @click="shiftMonth(-1)"><LeftOutlined /></a-button>
        <a-date-picker v-model:value="monthPicker" picker="month" value-format="YYYY-MM" :allow-clear="false" @change="onMonthPicked" />
        <a-button @click="shiftMonth(1)"><RightOutlined /></a-button>
        <a-button type="primary" @click="openCreateFromSelection"><PlusOutlined /> 新增日程</a-button>
      </a-space>
    </div>

    <div class="calendar-filters">
      <a-checkbox-group v-model:value="calendarScopes" :options="scopeOptions" @change="reloadMonth" />
      <a-space wrap>
        <a-tag color="green">工作日 {{ typeCounts.workday }}</a-tag>
        <a-tag color="orange">休假 {{ typeCounts.offday }}</a-tag>
        <a-tag color="purple">调休 {{ typeCounts.makeup }}</a-tag>
      </a-space>
    </div>

    <div class="calendar-shell">
      <div class="weekday-row">
        <div v-for="item in weekdays" :key="item" class="weekday-cell">{{ item }}</div>
      </div>
      <div class="month-grid">
        <button
          v-for="cell in calendarCells"
          :key="cell.date"
          class="day-cell"
          :class="{ muted: !cell.currentMonth, selected: selectedDates.includes(cell.date), today: cell.isToday }"
          type="button"
          @click="toggleDate(cell.date)"
          @dblclick="openCreate(cell.date)"
        >
          <span class="day-head">
            <strong>{{ cell.day }}</strong>
            <span>{{ cell.dayInfo?.publicWeek || '' }}</span>
          </span>
          <span class="day-tags">
            <a-tag v-if="cell.dayInfo" :color="dateTypeMeta(cell.dayInfo.dateType).color">
              {{ dateTypeMeta(cell.dayInfo.dateType).label }}
            </a-tag>
            <a-tag v-if="cell.dayInfo?.holidayName" color="red">{{ cell.dayInfo.holidayName }}</a-tag>
          </span>
          <span class="event-list">
            <button
              v-for="event in eventsByDate[cell.date]?.slice(0, 4) || []"
              :key="`${event.scope}-${event.id}`"
              class="event-pill"
              type="button"
              :class="event.scope?.toLowerCase()"
              @click.stop="openEdit(event)"
            >
              <span>{{ eventTime(event) }}</span>{{ event.eventTitle }}
            </button>
            <em v-if="(eventsByDate[cell.date]?.length || 0) > 4">+{{ (eventsByDate[cell.date]?.length || 0) - 4 }} 更多</em>
          </span>
        </button>
      </div>
    </div>

    <div class="agenda-panel">
      <div class="agenda-head">
        <h2>选中日期</h2>
        <a-button v-if="selectedDates.length" type="link" @click="selectedDates = []">清空</a-button>
      </div>
      <div class="selected-strip">
        <a-tag v-for="date in selectedDates" :key="date" color="blue" closable @close.prevent="toggleDate(date)">{{ date }}</a-tag>
        <span v-if="!selectedDates.length">点击或拖选日历格子后，新增日程会自动带出时间区间。</span>
      </div>
    </div>

    <a-drawer v-model:open="eventDrawerOpen" width="560" :title="eventForm.id ? '编辑日程' : '新增日程'" :destroy-on-close="true">
      <a-form layout="vertical" :model="eventForm">
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="记录类型" required>
              <a-select v-model:value="eventForm.recordType" :options="recordTypeOptions" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="日历范围">
              <a-segmented v-model:value="eventForm.scope" :options="scopeSegmentOptions" />
            </a-form-item>
          </a-col>
          <a-col :span="24">
            <a-form-item label="标题" required>
              <a-input v-model:value="eventForm.eventTitle" placeholder="输入日程标题" />
            </a-form-item>
          </a-col>
          <a-col :span="24">
            <a-form-item label="时间区间" required>
              <a-range-picker v-model:value="eventRange" show-time value-format="YYYY-MM-DD HH:mm:ss" class="full-width" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="提醒提前分钟">
              <a-input-number v-model:value="eventForm.remindMinutes" class="full-width" :min="0" :max="10080" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="模板编码">
              <a-input v-model:value="eventForm.messageTemplateCode" />
            </a-form-item>
          </a-col>
          <a-col :span="24">
            <a-form-item label="通知人 ID">
              <a-select v-model:value="eventForm.notifyUserIds" mode="tags" class="full-width" placeholder="输入用户 ID 后回车" />
            </a-form-item>
          </a-col>
          <a-col :span="24">
            <a-form-item label="内容">
              <a-textarea v-model:value="eventForm.eventContent" :rows="4" placeholder="会议室、事项说明或来访信息" />
            </a-form-item>
          </a-col>
          <a-col v-if="eventForm.scope === 'TENANT'" :span="24">
            <a-checkbox v-model:checked="defaultPushTenant">以后默认推送租户</a-checkbox>
          </a-col>
        </a-row>
      </a-form>
      <template #footer>
        <div class="drawer-actions">
          <a-button v-if="eventForm.id" danger @click="deleteEvent">删除</a-button>
          <span />
          <a-space>
            <a-button @click="eventDrawerOpen = false">取消</a-button>
            <a-button v-if="eventForm.scope === 'TENANT'" @click="confirmPushTenant">推送租户</a-button>
            <a-button type="primary" :loading="saving" @click="saveEvent">保存</a-button>
          </a-space>
        </div>
      </template>
    </a-drawer>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { Modal, message } from 'ant-design-vue'
import { LeftOutlined, PlusOutlined, RightOutlined } from '@ant-design/icons-vue'
import dayjs from 'dayjs'
import { workCalendarApi, type WorkCalendarDay, type WorkCalendarEvent } from '@/api/basic/workCalendar'

const weekdays = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
const today = dayjs()
const currentYear = ref(today.year())
const currentMonth = ref(today.month() + 1)
const monthPicker = ref(today.format('YYYY-MM'))
const syncHoliday = ref(true)
const viewMode = ref('month')
const calendarScopes = ref<string[]>(['USER', 'TENANT'])
const selectedDates = ref<string[]>([])
const days = ref<WorkCalendarDay[]>([])
const events = ref<WorkCalendarEvent[]>([])
const loading = ref(false)
const saving = ref(false)
const eventDrawerOpen = ref(false)
const eventRange = ref<[string, string] | undefined>()
const eventForm = ref<WorkCalendarEvent>(emptyEvent())
const defaultPushTenant = ref(localStorage.getItem('work-calendar-default-push-tenant') === '1')

const viewModeOptions = [{ label: '月', value: 'month' }, { label: '日程', value: 'agenda' }]
const scopeOptions = [{ label: '个人日历', value: 'USER' }, { label: '租户日历', value: 'TENANT' }]
const scopeSegmentOptions = [{ label: '个人', value: 'USER' }, { label: '租户', value: 'TENANT' }]
const recordTypeOptions = [
  { label: '会议', value: 'MEETING' },
  { label: '备忘', value: 'MEMO' },
  { label: '客户来访', value: 'CUSTOMER_VISIT' },
  { label: '检查', value: 'INSPECTION' },
]

const dayMap = computed(() => Object.fromEntries(days.value.map(item => [item.calendarDate, item])))
const eventsByDate = computed(() => {
  const map: Record<string, WorkCalendarEvent[]> = {}
  events.value.forEach(event => {
    const start = dayjs(event.startTime)
    const end = dayjs(event.endTime)
    let cursor = start.startOf('day')
    const last = end.startOf('day')
    while (cursor.isBefore(last) || cursor.isSame(last)) {
      const key = cursor.format('YYYY-MM-DD')
      map[key] = map[key] || []
      map[key].push(event)
      cursor = cursor.add(1, 'day')
    }
  })
  Object.values(map).forEach(list => list.sort((a, b) => a.startTime.localeCompare(b.startTime)))
  return map
})

const calendarCells = computed(() => {
  const first = dayjs(`${currentYear.value}-${String(currentMonth.value).padStart(2, '0')}-01`)
  const gridStart = first.subtract((first.day() + 6) % 7, 'day')
  return Array.from({ length: 42 }).map((_, index) => {
    const date = gridStart.add(index, 'day')
    const key = date.format('YYYY-MM-DD')
    return {
      date: key,
      day: date.date(),
      currentMonth: date.month() + 1 === currentMonth.value,
      isToday: date.isSame(today, 'day'),
      dayInfo: dayMap.value[key],
    }
  })
})

const typeCounts = computed(() => {
  const counts = { workday: 0, offday: 0, makeup: 0 }
  days.value.forEach(item => {
    if ([3, 5].includes(item.dateType)) counts.offday += 1
    else if (item.dateType === 4) counts.makeup += 1
    else if (item.dateType === 1) counts.workday += 1
  })
  return counts
})
const monthSummary = computed(() => `${events.value.length} 条日程`)

watch(eventRange, value => {
  if (!value) return
  eventForm.value.startTime = value[0]
  eventForm.value.endTime = value[1]
})

onMounted(reloadMonth)

function emptyEvent(): WorkCalendarEvent {
  return {
    scope: 'USER',
    recordType: 'MEETING',
    eventTitle: '',
    startTime: dayjs().format('YYYY-MM-DD 09:00:00'),
    endTime: dayjs().format('YYYY-MM-DD 10:00:00'),
    notifyUserIds: [],
    remindMinutes: 15,
    messageTemplateCode: 'CALENDAR_REMINDER',
  }
}

async function reloadMonth() {
  loading.value = true
  try {
    const result = await workCalendarApi.month({
      year: currentYear.value,
      month: currentMonth.value,
      syncHoliday: syncHoliday.value,
      calendarScopes: calendarScopes.value,
    })
    days.value = result.days || []
    events.value = result.events || []
  } finally {
    loading.value = false
  }
}

function onMonthPicked(value: string) {
  const date = dayjs(`${value}-01`)
  currentYear.value = date.year()
  currentMonth.value = date.month() + 1
  selectedDates.value = []
  reloadMonth()
}

function shiftMonth(offset: number) {
  const next = dayjs(`${currentYear.value}-${String(currentMonth.value).padStart(2, '0')}-01`).add(offset, 'month')
  currentYear.value = next.year()
  currentMonth.value = next.month() + 1
  monthPicker.value = next.format('YYYY-MM')
  selectedDates.value = []
  reloadMonth()
}

function goToday() {
  currentYear.value = today.year()
  currentMonth.value = today.month() + 1
  monthPicker.value = today.format('YYYY-MM')
  selectedDates.value = [today.format('YYYY-MM-DD')]
  reloadMonth()
}

function toggleDate(date: string) {
  selectedDates.value = selectedDates.value.includes(date)
    ? selectedDates.value.filter(item => item !== date)
    : [...selectedDates.value, date].sort()
}

function openCreateFromSelection() {
  openCreate(selectedDates.value[0] || dayjs(`${monthPicker.value}-01`).format('YYYY-MM-DD'))
}

function openCreate(date: string) {
  const selected = selectedDates.value.length ? selectedDates.value : [date]
  const sorted = [...selected].sort()
  eventForm.value = emptyEvent()
  eventForm.value.startTime = `${sorted[0]} 09:00:00`
  eventForm.value.endTime = `${sorted[sorted.length - 1]} 18:00:00`
  eventRange.value = [eventForm.value.startTime, eventForm.value.endTime]
  eventDrawerOpen.value = true
}

function openEdit(event: WorkCalendarEvent) {
  eventForm.value = { ...event, notifyUserIds: event.notifyUserIds?.map(Number) || [] }
  eventRange.value = [event.startTime, event.endTime]
  eventDrawerOpen.value = true
}

async function saveEvent() {
  saving.value = true
  try {
    const payload = normalizeEventForm()
    await workCalendarApi.saveEvent(payload)
    eventDrawerOpen.value = false
    await reloadMonth()
  } finally {
    saving.value = false
  }
}

function confirmPushTenant() {
  const preferKey = 'work-calendar-default-push-tenant'
  if (defaultPushTenant.value || localStorage.getItem(preferKey) === '1') {
    pushTenant()
    return
  }
  Modal.confirm({
    title: '推送到租户日历',
    content: '该日程会对租户内可见，请确认已获得授权。',
    okText: '推送',
    cancelText: '取消',
    onOk() {
      if (defaultPushTenant.value) localStorage.setItem(preferKey, '1')
      return pushTenant()
    },
  })
}

async function pushTenant() {
  localStorage.setItem('work-calendar-default-push-tenant', defaultPushTenant.value ? '1' : '0')
  saving.value = true
  try {
    await workCalendarApi.pushTenant({ ...normalizeEventForm(), scope: 'TENANT' })
    message.success('已推送到租户日历')
    eventDrawerOpen.value = false
    await reloadMonth()
  } finally {
    saving.value = false
  }
}

function deleteEvent() {
  Modal.confirm({
    title: '删除日程',
    content: '删除后未发送提醒会同步取消。',
    async onOk() {
      await workCalendarApi.deleteEvent(eventForm.value.id!, eventForm.value.scope || 'USER')
      eventDrawerOpen.value = false
      await reloadMonth()
    },
  })
}

function normalizeEventForm(): WorkCalendarEvent {
  return {
    ...eventForm.value,
    notifyUserIds: (eventForm.value.notifyUserIds || []).map(item => Number(item)).filter(Number.isFinite),
  }
}

function dateTypeMeta(type?: number) {
  const map: Record<number, { label: string; color: string }> = {
    1: { label: '班', color: 'green' },
    2: { label: '休', color: 'default' },
    3: { label: '法休', color: 'red' },
    4: { label: '调班', color: 'purple' },
    5: { label: '自休', color: 'orange' },
    6: { label: '活动', color: 'blue' },
  }
  return map[Number(type)] || { label: '班', color: 'green' }
}

function eventTime(event: WorkCalendarEvent) {
  return dayjs(event.startTime).format('HH:mm')
}
</script>

<style scoped lang="less" src="@/styles/views/basic/workCalendar/index.less"></style>
