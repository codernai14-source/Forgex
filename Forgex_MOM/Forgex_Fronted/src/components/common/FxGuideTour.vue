<template>
  <div class="fx-guide-tour">
    <div v-if="open && showBackdropMask" class="fx-guide-tour__mask" aria-hidden="true" />
    <a-tour
      v-model:current="currentStep"
      :open="open"
      :steps="resolvedSteps"
      :mask="tourMask"
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
import { useI18n } from 'vue-i18n'
import type { GuideResolvedStep, FxGuideStep } from '@/types/guide'

interface Props {
  guideCode: string
  version?: string
  steps: FxGuideStep[]
  autoStart?: boolean
  startKey?: string | number
  skipText?: string
  skipAllText?: string
  showSkipAll?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  version: 'v1',
  steps: () => [],
  autoStart: false,
  startKey: '',
  skipText: '',
  skipAllText: '',
  showSkipAll: false,
})

const emit = defineEmits<{
  open: []
  close: []
  finish: [guideCode: string, version?: string]
  skip: [guideCode: string, version?: string]
  skipAll: [guideCode: string, version?: string]
}>()

const open = ref(false)
const currentStep = ref(0)
const previousStep = ref(0)
const finished = ref(false)
const skipEmitted = ref(false)
const { t } = useI18n()

const skipText = computed(() => props.skipText || t('common.guide.skip'))
const skipAllText = computed(() => props.skipAllText || t('common.guide.skipAll'))
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

const currentResolvedStep = computed(() => resolvedSteps.value[currentStep.value])

const currentMask = computed(() => {
  return currentResolvedStep.value?.useMask !== false
})

const currentStepHasTarget = computed(() => {
  const step = currentResolvedStep.value
  return !!(step?.hasTarget && step.target?.())
})

const showBackdropMask = computed(() => {
  return currentMask.value && !currentStepHasTarget.value
})

const tourMask = computed(() => {
  return currentMask.value && currentStepHasTarget.value
    ? {
        color: 'rgba(15, 23, 42, 0.42)',
      }
    : false
})

function resolveCategoryText(category: FxGuideStep['category']) {
  switch (category) {
    case 'intro':
      return t('common.guide.category.intro')
    case 'form':
      return t('common.guide.category.form')
    case 'action':
      return t('common.guide.category.action')
    case 'table':
      return t('common.guide.category.table')
    case 'detail':
      return t('common.guide.category.detail')
    default:
      return t('common.guide.category.navigation')
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
  const actionNodes = [
    h(Button, {
      key: 'skip',
      size: 'small',
      type: 'text',
      class: 'fx-guide-panel__button fx-guide-panel__button--skip',
      onClick: handleSkipClick,
    }, () => skipText.value),
    props.showSkipAll
      ? h(Button, {
          key: 'skip-all',
          size: 'small',
          type: 'text',
          class: 'fx-guide-panel__button fx-guide-panel__button--skip-all',
          onClick: handleSkipAllClick,
        }, () => skipAllText.value)
      : null,
    h(Button, {
      key: 'prev',
      size: 'small',
      class: 'fx-guide-panel__button',
      disabled: isFirst,
      onClick: () => goToStep(current - 1),
    }, () => t('common.previous')),
    h(Button, {
      key: 'next',
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
    }, () => (isLast ? t('common.completed') : t('common.next'))),
  ].filter(Boolean) as VNodeChild[]

  return h('div', { class: ['fx-guide-panel', `fx-guide-panel--${category}`] }, [
    h('div', { class: 'fx-guide-panel__header' }, [
      h('span', { class: 'fx-guide-panel__tag' }, resolveCategoryText(category)),
      h('button', {
        type: 'button',
        class: 'fx-guide-panel__close',
        'aria-label': t('common.guide.close'),
        onClick: () => handleClose(),
      }, '×'),
    ]),
    h('div', { class: 'fx-guide-panel__title' }, title),
    h('div', { class: 'fx-guide-panel__description' }, description),
    h('div', { class: 'fx-guide-panel__footer' }, [
      h('div', { class: 'fx-guide-panel__progress', 'aria-label': t('common.guide.progress', { current: current + 1, total }) }, [
        h('span', { class: 'fx-guide-panel__progress-text' }, `${current + 1} / ${total}`),
        h('span', { class: 'fx-guide-panel__dots' }, resolvedSteps.value.map((_, dotIndex) => h('span', {
          class: ['fx-guide-panel__dot', dotIndex === current ? 'fx-guide-panel__dot--active' : ''],
        }))),
      ]),
      h('div', { class: 'fx-guide-panel__actions' }, actionNodes),
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

function handleSkipAllClick() {
  if (skipEmitted.value) {
    return
  }
  skipEmitted.value = true
  closeTour()
  emit('skipAll', props.guideCode, props.version)
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

<style lang="less" src="@/styles/components/common/fx-guide-tour-global.less"></style>
