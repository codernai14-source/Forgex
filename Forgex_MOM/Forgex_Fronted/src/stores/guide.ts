import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { getUserGuidePreference, saveUserGuidePreference } from '@/api/system/userGuide'
import type { GuidePreferenceConfig, GuideState, GuideStatus } from '@/types/guide'

function createDefaultGuidePreference(): GuidePreferenceConfig {
  return {
    babyModeEnabled: false,
    guideStates: {},
  }
}

function normalizeGuidePreference(config?: Partial<GuidePreferenceConfig> | null): GuidePreferenceConfig {
  return {
    babyModeEnabled: config?.babyModeEnabled === true,
    guideStates: config?.guideStates && typeof config.guideStates === 'object'
      ? { ...config.guideStates }
      : {},
  }
}

function createGuideState(status: GuideStatus, version?: string): GuideState {
  return {
    status,
    version,
    updatedAt: new Date().toISOString(),
  }
}

export const useGuideStore = defineStore('guide', () => {
  const preference = ref<GuidePreferenceConfig>(createDefaultGuidePreference())
  const loaded = ref(false)
  const loading = ref(false)
  const saving = ref(false)
  const currentGuideCode = ref('')
  const sessionOverrides = ref<Record<string, GuideState>>({})

  const babyModeEnabled = computed(() => preference.value.babyModeEnabled === true)

  function getMergedGuideState(guideCode: string): GuideState | undefined {
    if (!guideCode) {
      return undefined
    }
    return sessionOverrides.value[guideCode] || preference.value.guideStates[guideCode]
  }

  function shouldAutoStartGuide(guideCode: string, version?: string): boolean {
    if (!guideCode) {
      return false
    }
    const state = getMergedGuideState(guideCode)
    if (!state) {
      return true
    }
    if (babyModeEnabled.value) {
      return true
    }
    if (version && state.version && version !== state.version) {
      return true
    }
    return state.status === 'PENDING'
  }

  async function loadPreference(force = false) {
    if (loading.value) {
      return preference.value
    }
    if (loaded.value && !force) {
      return preference.value
    }

    const account = sessionStorage.getItem('account')
    const tenantId = sessionStorage.getItem('tenantId')
    if (!account || !tenantId) {
      preference.value = createDefaultGuidePreference()
      loaded.value = true
      return preference.value
    }

    loading.value = true
    try {
      const result = await getUserGuidePreference({ account, tenantId })
      preference.value = normalizeGuidePreference(result)
      sessionOverrides.value = {}
    } catch (error) {
      preference.value = normalizeGuidePreference(preference.value)
    } finally {
      loading.value = false
      loaded.value = true
    }
    return preference.value
  }

  async function persistPreference(nextConfig?: GuidePreferenceConfig) {
    const account = sessionStorage.getItem('account')
    const tenantId = sessionStorage.getItem('tenantId')
    const normalized = normalizeGuidePreference(nextConfig || preference.value)
    preference.value = normalized

    if (!account || !tenantId) {
      return normalized
    }

    saving.value = true
    try {
      await saveUserGuidePreference({
        account,
        tenantId,
        config: normalized,
      })
      sessionOverrides.value = {}
    } catch (error) {
      sessionOverrides.value = { ...normalized.guideStates }
    } finally {
      saving.value = false
    }
    return normalized
  }

  async function setBabyModeEnabled(enabled: boolean) {
    const next = normalizeGuidePreference(preference.value)
    next.babyModeEnabled = enabled
    await persistPreference(next)
  }

  async function updateGuideState(guideCode: string, status: GuideStatus, version?: string) {
    if (!guideCode) {
      return
    }
    const next = normalizeGuidePreference(preference.value)
    next.guideStates[guideCode] = createGuideState(status, version)
    sessionOverrides.value = {
      ...sessionOverrides.value,
      [guideCode]: next.guideStates[guideCode],
    }
    await persistPreference(next)
  }

  async function markGuideSkipped(guideCode: string, version?: string) {
    await updateGuideState(guideCode, 'SKIPPED', version)
  }

  async function markGuideCompleted(guideCode: string, version?: string) {
    await updateGuideState(guideCode, 'COMPLETED', version)
  }

  async function resetGuideState(guideCode: string, version?: string) {
    if (!guideCode) {
      return
    }
    const next = normalizeGuidePreference(preference.value)
    next.guideStates[guideCode] = createGuideState('PENDING', version)
    sessionOverrides.value = {
      ...sessionOverrides.value,
      [guideCode]: next.guideStates[guideCode],
    }
    await persistPreference(next)
  }

  function startGuide(guideCode: string) {
    currentGuideCode.value = guideCode
  }

  function finishCurrentGuide() {
    currentGuideCode.value = ''
  }

  function resetStore() {
    preference.value = createDefaultGuidePreference()
    loaded.value = false
    loading.value = false
    saving.value = false
    currentGuideCode.value = ''
    sessionOverrides.value = {}
  }

  return {
    preference,
    loaded,
    loading,
    saving,
    currentGuideCode,
    babyModeEnabled,
    loadPreference,
    persistPreference,
    setBabyModeEnabled,
    shouldAutoStartGuide,
    getMergedGuideState,
    markGuideSkipped,
    markGuideCompleted,
    resetGuideState,
    startGuide,
    finishCurrentGuide,
    resetStore,
  }
})
