<template>
  <div class="fx-guide-tour">
    <a-tour
      v-model:current="currentStep"
      :open="open"
      :steps="resolvedSteps"
      :mask="currentMask"
      :placement="defaultPlacement"
      :z-index="2000"
      :gap="guideGap"
      @close="handleClose"
      @finish="handleFinish"
    />
  </div>
</template>

<script setup lang="ts">
import { Button } from 'ant-design-vue'
import { computed, h, nextTick, ref, watch, type VNodeChild } from 'vue'
import type { GuideResolvedStep, FxGuideStep } from '@/types/guide'

interface Props {
  guideCode: string
  version?: string
  steps: FxGuideStep[]
  autoStart?: boolean
  startKey?: string | number
  skipText?: string
}

const props = withDefaults(defineProps<Props>(), {
  version: 'v1',
  steps: () => [],
  autoStart: false,
  startKey: '',
  skipText: '跳过引导',
})

const emit = defineEmits<{
  open: []
  close: []
  finish: [guideCode: string, version?: string]
  skip: [guideCode: string, version?: string]
}>()

const open = ref(false)
const currentStep = ref(0)
const previousStep = ref(0)
const finished = ref(false)
const skipEmitted = ref(false)

const skipText = computed(() => props.skipText || '跳过引导')
const guideGap = { offset: 0, radius: 2 }

const resolvedSteps = computed(() => {
  return props.steps.map<GuideResolvedStep>(step => {
    if (typeof step.target === 'function') {
      return {
        ...step,
        hasTarget: true,
        target: step.target,
        renderPanel: renderGuidePanel,
      }
    }
    if (typeof step.target === 'string') {
      return {
        ...step,
        hasTarget: true,
        target: () => document.querySelector(step.target as string) as HTMLElement | null,
        renderPanel: renderGuidePanel,
      }
    }
    return {
      ...step,
      hasTarget: false,
      target: undefined,
      renderPanel: renderGuidePanel,
    }
  })
})

const defaultPlacement = computed(() => {
  return resolvedSteps.value[currentStep.value]?.placement || 'bottom'
})

const currentMask = computed(() => {
  return resolvedSteps.value[currentStep.value]?.useMask !== false
})

function resolveCategoryText(category: FxGuideStep['category']) {
  switch (category) {
    case 'intro':
      return '页面说明'
    case 'form':
      return '查询筛选'
    case 'action':
      return '功能操作'
    case 'table':
      return '数据列表'
    case 'detail':
      return '明细操作'
    default:
      return '导航提示'
  }
}

function renderGuidePanel(step: any, index: number): VNodeChild {
  const total = resolvedSteps.value.length
  const current = Math.min(Math.max(index, 0), Math.max(total - 1, 0))
  const isFirst = current <= 0
  const isLast = current >= total - 1
  const resolvedStep = resolvedSteps.value[current]
  const category = resolvedStep?.category || 'navigation'
  const title = step?.title ?? resolvedStep?.title ?? ''
  const description = step?.description ?? resolvedStep?.description ?? ''

  return h('div', { class: ['fx-guide-panel', `fx-guide-panel--${category}`] }, [
    h('div', { class: 'fx-guide-panel__header' }, [
      h('span', { class: 'fx-guide-panel__tag' }, resolveCategoryText(category)),
      h('button', {
        type: 'button',
        class: 'fx-guide-panel__close',
        'aria-label': '关闭引导',
        onClick: () => handleClose(),
      }, '×'),
    ]),
    h('div', { class: 'fx-guide-panel__title' }, title),
    h('div', { class: 'fx-guide-panel__description' }, description),
    h('div', { class: 'fx-guide-panel__footer' }, [
      h('div', { class: 'fx-guide-panel__progress', 'aria-label': `第 ${current + 1} 步，共 ${total} 步` }, [
        h('span', { class: 'fx-guide-panel__progress-text' }, `${current + 1} / ${total}`),
        h('span', { class: 'fx-guide-panel__dots' }, resolvedSteps.value.map((_, dotIndex) => h('span', {
          class: ['fx-guide-panel__dot', dotIndex === current ? 'fx-guide-panel__dot--active' : ''],
        }))),
      ]),
      h('div', { class: 'fx-guide-panel__actions' }, [
        h(Button, {
          size: 'small',
          type: 'text',
          class: 'fx-guide-panel__button fx-guide-panel__button--skip',
          onClick: handleSkipClick,
        }, () => skipText.value),
        h(Button, {
          size: 'small',
          class: 'fx-guide-panel__button',
          disabled: isFirst,
          onClick: () => goToStep(current - 1),
        }, () => '上一步'),
        h(Button, {
          size: 'small',
          type: 'primary',
          class: 'fx-guide-panel__button fx-guide-panel__button--primary',
          onClick: () => {
            if (isLast) {
              handleFinish()
              return
            }
            goToStep(current + 1)
          },
        }, () => (isLast ? '完成' : '下一步')),
      ]),
    ]),
  ])
}

function getValidStepIndex(startIndex = 0, direction: 1 | -1 = 1): number {
  const steps = resolvedSteps.value
  if (!steps.length) {
    return -1
  }

  const safeStartIndex = Math.min(Math.max(startIndex, 0), steps.length - 1)
  for (
    let index = safeStartIndex;
    index >= 0 && index < steps.length;
    index += direction
  ) {
    const step = steps[index]
    if (!step?.hasTarget || step?.target?.()) {
      return index
    }
  }
  return -1
}

async function openTour(fromIndex = 0) {
  if (open.value) {
    return
  }
  await nextTick()
  const nextIndex = getValidStepIndex(fromIndex)
  if (nextIndex === -1) {
    emit('skip', props.guideCode, props.version)
    return
  }
  currentStep.value = nextIndex
  previousStep.value = nextIndex
  finished.value = false
  skipEmitted.value = false
  try {
    open.value = true
    emit('open')
  } catch (error) {
    closeTour()
    emit('skip', props.guideCode, props.version)
  }
}

function closeTour() {
  open.value = false
  currentStep.value = 0
  previousStep.value = 0
  finished.value = false
  emit('close')
}

function goToStep(index: number) {
  const direction: 1 | -1 = index >= currentStep.value ? 1 : -1
  const nextIndex = getValidStepIndex(index, direction)
  if (nextIndex !== -1) {
    currentStep.value = nextIndex
  }
}

async function handleClose() {
  const wasFinished = finished.value
  closeTour()
  if (!wasFinished && !skipEmitted.value) {
    skipEmitted.value = true
    emit('skip', props.guideCode, props.version)
  }
}

function handleFinish() {
  finished.value = true
  closeTour()
  emit('finish', props.guideCode, props.version)
}

function handleSkipClick() {
  if (skipEmitted.value) {
    return
  }
  skipEmitted.value = true
  closeTour()
  emit('skip', props.guideCode, props.version)
}

watch(currentStep, async value => {
  if (!open.value) {
    return
  }
  const direction: 1 | -1 = value >= previousStep.value ? 1 : -1
  const step = resolvedSteps.value[value]
  if (!step?.hasTarget || step?.target?.()) {
    previousStep.value = value
    return
  }
  const fallbackStartIndex = direction === 1 ? value + 1 : value - 1
  const nextIndex = getValidStepIndex(fallbackStartIndex, direction)
  if (nextIndex === -1) {
    previousStep.value = value
    return
  }
  currentStep.value = nextIndex
})

watch(
  () => props.autoStart,
  autoStart => {
    if (autoStart) {
      openTour()
    } else if (open.value) {
      closeTour()
    }
  },
  { immediate: true },
)

watch(
  () => props.startKey,
  (value, oldValue) => {
    if (value !== oldValue && value !== '' && value !== undefined && value !== null) {
      openTour()
    }
  },
)

defineExpose({
  openTour,
  closeTour,
})
</script>

<style>
.fx-guide-panel {
  width: 360px;
  max-width: min(360px, calc(100vw - 48px));
  padding: 16px;
  border: 1px solid var(--fx-border-color, rgba(148, 163, 184, 0.2));
  border-radius: 10px;
  color: var(--fx-text-primary, #1f2937);
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.04), rgba(255, 255, 255, 0)),
    var(--fx-bg-container, #ffffff);
  box-shadow: 0 18px 42px rgba(15, 23, 42, 0.2);
  box-sizing: border-box;
}

[data-theme='dark'] .fx-guide-panel,
body[data-theme='dark'] .fx-guide-panel {
  border-color: rgba(148, 163, 184, 0.2);
  color: var(--fx-text-primary, #f8fafc);
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.06), rgba(255, 255, 255, 0)),
    var(--fx-bg-container, #111827);
  box-shadow: 0 22px 48px rgba(0, 0, 0, 0.42);
}

.fx-guide-panel__header,
.fx-guide-panel__footer,
.fx-guide-panel__progress,
.fx-guide-panel__actions,
.fx-guide-panel__dots {
  display: flex;
  align-items: center;
}

.fx-guide-panel__header,
.fx-guide-panel__footer {
  justify-content: space-between;
  gap: 12px;
}

.fx-guide-panel__tag {
  padding: 2px 8px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
  color: var(--fx-primary, #1677ff);
  background: rgba(22, 119, 255, 0.12);
}

.fx-guide-panel--intro .fx-guide-panel__tag {
  color: #f97316;
  background: rgba(249, 115, 22, 0.14);
}

.fx-guide-panel__close {
  width: 24px;
  height: 24px;
  border: 0;
  border-radius: 6px;
  color: var(--fx-text-tertiary, #6b7280);
  background: transparent;
  cursor: pointer;
  line-height: 20px;
  font-size: 18px;
}

.fx-guide-panel__close:hover {
  color: var(--fx-text-primary, #1f2937);
  background: var(--fx-bg-elevated, rgba(148, 163, 184, 0.14));
}

.fx-guide-panel__title {
  margin-top: 12px;
  color: var(--fx-text-primary, #1f2937);
  font-size: 16px;
  font-weight: 600;
  line-height: 1.5;
}

.fx-guide-panel__description {
  margin-top: 6px;
  color: var(--fx-text-secondary, #4b5563);
  font-size: 13px;
  line-height: 1.65;
  white-space: pre-line;
}

.fx-guide-panel__footer {
  margin-top: 16px;
  padding-top: 12px;
  border-top: 1px solid var(--fx-border-color, rgba(148, 163, 184, 0.2));
}

.fx-guide-panel__progress {
  min-width: 86px;
  gap: 8px;
  flex: 1;
  overflow: hidden;
}

.fx-guide-panel__progress-text {
  color: var(--fx-text-tertiary, #6b7280);
  font-size: 12px;
  font-variant-numeric: tabular-nums;
}

.fx-guide-panel__dots {
  gap: 4px;
  max-width: 108px;
  overflow: hidden;
}

.fx-guide-panel__dot {
  width: 5px;
  height: 5px;
  border-radius: 50%;
  background: var(--fx-border-color, rgba(148, 163, 184, 0.45));
}

.fx-guide-panel__dot--active {
  width: 14px;
  border-radius: 999px;
  background: var(--fx-primary, #1677ff);
}

.fx-guide-panel__actions {
  justify-content: flex-end;
  gap: 8px;
  flex-shrink: 0;
}

.fx-guide-panel__button {
  border-radius: 6px;
}

.fx-guide-panel__button--skip {
  color: var(--fx-text-secondary, #4b5563);
}

.fx-guide-panel__button--skip:hover {
  color: var(--fx-primary, #1677ff);
  background: rgba(22, 119, 255, 0.08);
}

.fx-guide-panel__button--primary {
  min-width: 64px;
}

[data-theme='dark'] .fx-guide-panel__title,
body[data-theme='dark'] .fx-guide-panel__title {
  color: var(--fx-text-primary, #f8fafc);
}

[data-theme='dark'] .fx-guide-panel__description,
body[data-theme='dark'] .fx-guide-panel__description {
  color: var(--fx-text-secondary, #cbd5e1);
}

[data-theme='dark'] .fx-guide-panel__progress-text,
body[data-theme='dark'] .fx-guide-panel__progress-text,
[data-theme='dark'] .fx-guide-panel__button--skip,
body[data-theme='dark'] .fx-guide-panel__button--skip {
  color: var(--fx-text-secondary, #cbd5e1);
}

[data-theme='dark'] .fx-guide-panel__dot,
body[data-theme='dark'] .fx-guide-panel__dot {
  background: rgba(148, 163, 184, 0.35);
}

[data-theme='dark'] .fx-guide-panel__close:hover,
body[data-theme='dark'] .fx-guide-panel__close:hover {
  color: var(--fx-text-primary, #f8fafc);
  background: rgba(148, 163, 184, 0.16);
}

.ant-tour-content {
  background: var(--fx-bg-container, #ffffff);
}

.ant-tour-inner {
  color: var(--fx-text-primary, #1f2937);
}
</style>
