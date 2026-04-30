export type GuideStatus = 'PENDING' | 'SKIPPED' | 'COMPLETED'

export interface GuideState {
  status: GuideStatus
  version?: string
  updatedAt?: string
}

export interface GuidePreferenceConfig {
  babyModeEnabled: boolean
  guideStates: Record<string, GuideState>
}

export interface FxGuideStep {
  title: string
  description: string
  target?: string | (() => HTMLElement | null)
  placement?: 'top' | 'bottom' | 'left' | 'right' | 'center'
  useMask?: boolean
  category?: 'intro' | 'navigation' | 'form' | 'action' | 'table' | 'detail'
}

export interface GuideResolvedStep extends Omit<FxGuideStep, 'target'> {
  target?: () => HTMLElement | null
  hasTarget: boolean
  renderPanel?: (step: any, current: number) => unknown
}
