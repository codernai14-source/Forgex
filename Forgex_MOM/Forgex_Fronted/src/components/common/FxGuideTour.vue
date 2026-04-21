<template>
  <a-tour
    v-model:current="currentStep"
    :open="open"
    :steps="resolvedSteps"
    :mask="currentMask"
    :placement="defaultPlacement"
    :z-index="2000"
    @close="handleClose"
    @finish="handleFinish"
  />
</template>

<script setup lang="ts">
import { computed, nextTick, ref, watch } from 'vue'
import type { GuideResolvedStep, FxGuideStep } from '@/types/guide'

interface Props {
  guideCode: string
  version?: string
  steps: FxGuideStep[]
  autoStart?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  version: 'v1',
  steps: () => [],
  autoStart: false,
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

const resolvedSteps = computed(() => {
  return props.steps.map<GuideResolvedStep>(step => ({
    ...step,
    target: typeof step.target === 'function'
      ? step.target
      : typeof step.target === 'string'
        ? () => document.querySelector(step.target) as HTMLElement | null
        : () => null,
  }))
})

const defaultPlacement = computed(() => {
  return resolvedSteps.value[currentStep.value]?.placement || 'bottom'
})

const currentMask = computed(() => {
  return resolvedSteps.value[currentStep.value]?.useMask !== false
})

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
    const target = steps[index]?.target?.()
    if (target) {
      return index
    }
  }
  return -1
}

async function openTour(fromIndex = 0) {
  await nextTick()
  const nextIndex = getValidStepIndex(fromIndex)
  if (nextIndex === -1) {
    emit('skip', props.guideCode, props.version)
    return
  }
  currentStep.value = nextIndex
  previousStep.value = nextIndex
  finished.value = false
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

async function handleClose() {
  const wasFinished = finished.value
  closeTour()
  if (!wasFinished) {
    emit('skip', props.guideCode, props.version)
  }
}

function handleFinish() {
  finished.value = true
  closeTour()
  emit('finish', props.guideCode, props.version)
}

watch(currentStep, async value => {
  if (!open.value) {
    return
  }
  const direction: 1 | -1 = value >= previousStep.value ? 1 : -1
  const target = resolvedSteps.value[value]?.target?.()
  if (target) {
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

defineExpose({
  openTour,
  closeTour,
})
</script>
