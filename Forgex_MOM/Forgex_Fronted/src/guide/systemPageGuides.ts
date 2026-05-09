import i18n from '@/locales'
import type { FxGuideStep } from '@/types/guide'

export interface SystemPageGuideConfig {
  guideCode: string
  version: string
  steps: FxGuideStep[]
}

interface PageGuideMeta {
  key: string
  actions?: Array<{
    id: string
    key: string
    placement?: FxGuideStep['placement']
  }>
  includeQuery?: boolean
  includeToolbar?: boolean
  includeColumnSetting?: boolean
  includeTable?: boolean
  includePagination?: boolean
  includeRowActions?: boolean
  extraSteps?: Array<{
    key: string
    target: string
    placement?: FxGuideStep['placement']
    category?: FxGuideStep['category']
  }>
}

const SYSTEM_GUIDE_VERSION = 'v3'
const guideTarget = (id: string) => `[data-guide-id="${id}"]`
const tr = (key: string, params?: Record<string, unknown>) => i18n.global.t(key, params || {})

const commonStep = (
  key: string,
  target: string,
  placement: FxGuideStep['placement'],
  category: FxGuideStep['category']
): FxGuideStep => ({
  title: tr(`guide.systemPage.common.${key}.title`),
  description: tr(`guide.systemPage.common.${key}.description`),
  target,
  placement,
  category,
})

const pageGuides: Record<string, PageGuideMeta> = {
  dashboard: {
    key: 'dashboard',
    includeQuery: false,
    includeToolbar: false,
    includeColumnSetting: false,
    includeTable: false,
    includePagination: false,
    extraSteps: [
      {
        key: 'moduleHomepageStage',
        target: '[data-guide-id="module-homepage-stage"]',
        placement: 'top',
        category: 'navigation',
      },
    ],
  },
  user: {
    key: 'user',
    actions: [
      { id: 'sys-user-add', key: 'userAdd' },
      { id: 'sys-user-sync-third-party', key: 'userSyncThirdParty' },
      { id: 'sys-user-pull-third-party', key: 'userPullThirdParty' },
      { id: 'sys-user-import', key: 'userImport' },
      { id: 'sys-user-download-template', key: 'userDownloadTemplate' },
      { id: 'sys-user-batch-delete', key: 'userBatchDelete' },
      { id: 'sys-user-export', key: 'userExport' },
      { id: 'sys-user-row-edit', key: 'userRowEdit', placement: 'left' },
      { id: 'sys-user-row-status', key: 'userRowStatus', placement: 'left' },
      { id: 'sys-user-row-reset-password', key: 'userRowResetPassword', placement: 'left' },
      { id: 'sys-user-row-assign-role', key: 'userRowAssignRole', placement: 'left' },
      { id: 'sys-user-row-delete', key: 'userRowDelete', placement: 'left' },
    ],
  },
  department: {
    key: 'department',
    includeQuery: false,
    actions: [
      { id: 'sys-dept-add', key: 'deptAdd' },
      { id: 'sys-dept-edit', key: 'deptEdit' },
      { id: 'sys-dept-delete', key: 'deptDelete' },
      { id: 'sys-dept-save', key: 'deptSave' },
    ],
  },
  position: {
    key: 'position',
    actions: [{ id: 'sys-position-add', key: 'positionAdd' }],
  },
  inviteCode: {
    key: 'inviteCode',
    actions: [{ id: 'sys-invite-add', key: 'inviteAdd' }],
  },
  role: {
    key: 'role',
    actions: [
      { id: 'sys-role-add', key: 'roleAdd' },
      { id: 'sys-role-batch-delete', key: 'roleBatchDelete' },
      { id: 'sys-role-row-edit', key: 'roleRowEdit', placement: 'left' },
      { id: 'sys-role-row-menu-grant', key: 'roleRowMenuGrant', placement: 'left' },
      { id: 'sys-role-row-user-grant', key: 'roleRowUserGrant', placement: 'left' },
      { id: 'sys-role-row-delete', key: 'roleRowDelete', placement: 'left' },
    ],
  },
  'role/MenuGrant': {
    key: 'roleMenuGrant',
    includeQuery: false,
    includeToolbar: false,
    includeTable: false,
    includeColumnSetting: false,
    includePagination: false,
    actions: [
      { id: 'sys-role-menu-grant-save', key: 'roleMenuGrantSave' },
      { id: 'sys-role-menu-grant-select-all', key: 'roleMenuGrantSelectAll' },
      { id: 'sys-role-menu-grant-invert', key: 'roleMenuGrantInvert' },
      { id: 'sys-role-menu-grant-clear', key: 'roleMenuGrantClear' },
      { id: 'sys-role-menu-grant-search', key: 'roleMenuGrantSearch' },
    ],
    extraSteps: [
      {
        key: 'roleMenuGrantModuleFilter',
        target: guideTarget('sys-role-menu-grant-module-filter'),
        placement: 'right',
        category: 'navigation',
      },
      {
        key: 'roleMenuGrantTree',
        target: guideTarget('fx-table-content'),
        placement: 'top',
        category: 'detail',
      },
    ],
  },
  'role/UserGrant': {
    key: 'roleUserGrant',
    includeQuery: false,
    includeToolbar: false,
    includeColumnSetting: false,
    actions: [
      { id: 'sys-role-user-grant-add', key: 'roleUserGrantAdd' },
      { id: 'sys-role-user-grant-select-all', key: 'roleUserGrantSelectAll' },
      { id: 'sys-role-user-grant-clear', key: 'roleUserGrantClear' },
      { id: 'sys-role-user-grant-batch-revoke', key: 'roleUserGrantBatchRevoke' },
      { id: 'sys-role-user-grant-row-revoke', key: 'roleUserGrantRowRevoke', placement: 'left' },
    ],
    extraSteps: [
      {
        key: 'roleUserGrantObjectPanel',
        target: guideTarget('sys-role-user-grant-object-panel'),
        placement: 'right',
        category: 'form',
      },
    ],
  },
  tenant: {
    key: 'tenant',
    actions: [{ id: 'sys-tenant-add', key: 'tenantAdd' }],
  },
  menu: {
    key: 'menu',
    actions: [
      { id: 'sys-menu-add', key: 'menuAdd' },
      { id: 'sys-menu-batch-delete', key: 'menuBatchDelete' },
    ],
  },
  module: {
    key: 'module',
    actions: [
      { id: 'sys-module-add', key: 'moduleAdd' },
      { id: 'sys-module-batch-delete', key: 'moduleBatchDelete' },
    ],
  },
  dict: {
    key: 'dict',
    actions: [{ id: 'sys-dict-add', key: 'dictAdd' }],
  },
  excelImportConfig: {
    key: 'excelImportConfig',
    actions: [{ id: 'sys-excel-import-edit', key: 'excelImportEdit' }],
  },
  excelExportConfig: {
    key: 'excelExportConfig',
    actions: [{ id: 'sys-excel-export-edit', key: 'excelExportEdit' }],
  },
  tableConfig: {
    key: 'tableConfig',
    actions: [
      { id: 'sys-table-config-pull-public', key: 'tableConfigPullPublic' },
      { id: 'sys-table-config-add', key: 'tableConfigAdd' },
      { id: 'sys-table-config-batch-delete', key: 'tableConfigBatchDelete' },
    ],
  },
  userTableConfig: {
    key: 'userTableConfig',
    includeToolbar: false,
    actions: [
      { id: 'sys-user-table-search', key: 'userTableSearch' },
      { id: 'sys-user-table-reset', key: 'userTableReset' },
    ],
  },
  loginLog: {
    key: 'loginLog',
    actions: [{ id: 'sys-login-log-export', key: 'loginLogExport' }],
  },
  online: {
    key: 'online',
    actions: [{ id: 'sys-online-kickout', key: 'onlineKickout' }],
  },
  operationLog: {
    key: 'operationLog',
    actions: [{ id: 'sys-operation-log-export', key: 'operationLogExport' }],
  },
  config: {
    key: 'config',
    includeQuery: false,
    includeToolbar: false,
    includeTable: false,
    actions: [
      { id: 'sys-config-save', key: 'configSave' },
      { id: 'sys-config-reset', key: 'configReset' },
    ],
    extraSteps: [
      {
        key: 'configTabs',
        target: '.ant-tabs-nav',
        placement: 'bottom',
        category: 'navigation',
      },
    ],
  },
  messageTemplate: {
    key: 'messageTemplate',
    actions: [
      { id: 'sys-message-template-pull-public', key: 'messageTemplatePullPublic' },
      { id: 'sys-message-template-add', key: 'messageTemplateAdd' },
      { id: 'sys-message-template-batch-delete', key: 'messageTemplateBatchDelete' },
    ],
  },
  tenantMessageWhitelist: {
    key: 'tenantMessageWhitelist',
    actions: [
      { id: 'sys-tenant-message-whitelist-add', key: 'tenantMessageWhitelistAdd' },
      { id: 'sys-tenant-message-whitelist-batch-delete', key: 'tenantMessageWhitelistBatchDelete' },
    ],
  },
  i18nLanguageType: {
    key: 'i18nLanguageType',
    actions: [
      { id: 'sys-i18n-language-add', key: 'i18nLanguageAdd' },
      { id: 'sys-i18n-language-import', key: 'i18nLanguageImport' },
      { id: 'sys-i18n-language-template', key: 'i18nLanguageTemplate' },
    ],
  },
  i18nMessage: {
    key: 'i18nMessage',
    actions: [{ id: 'sys-i18n-message-add', key: 'i18nMessageAdd' }],
  },
  file: {
    key: 'file',
  },
  codegen: {
    key: 'codegen',
    actions: [{ id: 'sys-codegen-add', key: 'codegenAdd' }],
  },
  codegenDatasource: {
    key: 'codegenDatasource',
    actions: [{ id: 'sys-codegen-datasource-add', key: 'codegenDatasourceAdd' }],
  },
}

export function normalizeSystemGuidePath(path: string) {
  const key = String(path || '')
    .split('?')[0]
    .replace(/^\/workspace\/sys\/?/, '')
    .replace(/^\/+|\/+$/g, '') || 'dashboard'
  const lowerKey = key.toLowerCase()
  if (lowerKey.startsWith('role/menugrant') || lowerKey.startsWith('authorization/role/menu-grant')) {
    return 'role/MenuGrant'
  }
  if (lowerKey.startsWith('role/usergrant') || lowerKey.startsWith('authorization/role/user-grant')) {
    return 'role/UserGrant'
  }
  return key
}

export function buildSystemGuideCode(key: string) {
  return `system.menu.${key.replace(/[/:]+/g, '.').replace(/^\.+|\.+$/g, '') || 'dashboard'}`
}

function buildFallbackMeta(): PageGuideMeta {
  return {
    key: 'fallback',
  }
}

function createSteps(meta: PageGuideMeta): FxGuideStep[] {
  const pageKey = `guide.systemPage.pages.${meta.key}`
  const steps: FxGuideStep[] = [
    {
      title: tr(`${pageKey}.title`),
      description: tr(`${pageKey}.intro`),
      placement: 'center',
      category: 'intro',
    },
  ]

  if (meta.includeQuery !== false) {
    steps.push(commonStep('query', guideTarget('fx-table-query'), 'bottom', 'form'))
  }

  if (meta.includeToolbar !== false) {
    steps.push(commonStep('toolbar', guideTarget('fx-table-toolbar'), 'bottom', 'action'))
  }

  for (const action of meta.actions || []) {
    steps.push({
      title: tr(`guide.systemPage.actions.${action.key}.title`),
      description: tr(`guide.systemPage.actions.${action.key}.description`),
      target: guideTarget(action.id),
      placement: action.placement || 'bottom',
      category: 'action',
    })
  }

  if (meta.includeColumnSetting !== false) {
    steps.push(commonStep('columnSetting', guideTarget('fx-table-column-setting'), 'left', 'table'))
  }

  if (meta.includeTable !== false) {
    steps.push(commonStep('table', guideTarget('fx-table-content'), 'top', 'table'))
  }

  if (meta.includeRowActions === true) {
    steps.push(commonStep('rowAction', '.ant-table-tbody .ant-table-cell-fix-right, .ant-table-tbody .ant-table-cell:last-child', 'left', 'detail'))
  }

  if (meta.includePagination !== false) {
    steps.push(commonStep('pagination', guideTarget('fx-table-pagination'), 'top', 'table'))
  }

  for (const step of meta.extraSteps || []) {
    steps.push({
      title: tr(`guide.systemPage.extra.${step.key}.title`),
      description: tr(`guide.systemPage.extra.${step.key}.description`),
      target: step.target,
      placement: step.placement || 'bottom',
      category: step.category || 'detail',
    })
  }

  return steps
}

export function resolveSystemPageGuide(path: string): SystemPageGuideConfig {
  const key = normalizeSystemGuidePath(path)
  const meta = pageGuides[key] || buildFallbackMeta()
  return {
    guideCode: buildSystemGuideCode(key),
    version: SYSTEM_GUIDE_VERSION,
    steps: createSteps(meta),
  }
}

export function listSystemPageGuideCodes() {
  return Object.keys(pageGuides).map(buildSystemGuideCode)
}
