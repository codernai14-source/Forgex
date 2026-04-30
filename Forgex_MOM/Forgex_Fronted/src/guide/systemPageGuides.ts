import type { FxGuideStep } from '@/types/guide'

export interface SystemPageGuideConfig {
  guideCode: string
  version: string
  steps: FxGuideStep[]
}

interface PageGuideMeta {
  title: string
  intro: string
  actions?: Array<{
    id: string
    title: string
    description: string
    placement?: FxGuideStep['placement']
  }>
  includeQuery?: boolean
  includeToolbar?: boolean
  includeColumnSetting?: boolean
  includeTable?: boolean
  includePagination?: boolean
  includeRowActions?: boolean
  extraSteps?: FxGuideStep[]
}

const SYSTEM_GUIDE_VERSION = 'v2'
const guideTarget = (id: string) => `[data-guide-id="${id}"]`

const commonQueryStep: FxGuideStep = {
  title: '查询筛选',
  description: '这里用于按关键字段筛选数据。设置条件后点击搜索，可以快速缩小列表范围。',
  target: guideTarget('fx-table-query'),
  placement: 'bottom',
  category: 'form',
}

const commonToolbarStep: FxGuideStep = {
  title: '工具栏',
  description: '这里集中放置当前页面的主要操作按钮，例如新增、导入、导出、批量处理等。',
  target: guideTarget('fx-table-toolbar'),
  placement: 'bottom',
  category: 'action',
}

const commonColumnSettingStep: FxGuideStep = {
  title: '列设置',
  description: '点击这里可以调整表格列的显示、隐藏和顺序，让列表更符合个人查看习惯。',
  target: guideTarget('fx-table-column-setting'),
  placement: 'left',
  category: 'table',
}

const commonTableStep: FxGuideStep = {
  title: '数据列表',
  description: '列表展示当前页面的业务数据。可配合排序、分页和行操作完成日常管理。',
  target: guideTarget('fx-table-content'),
  placement: 'top',
  category: 'table',
}

const commonPaginationStep: FxGuideStep = {
  title: '分页区域',
  description: '这里可以切换页码、调整每页数量，也能查看当前查询结果总数。',
  target: guideTarget('fx-table-pagination'),
  placement: 'top',
  category: 'table',
}

const commonRowActionStep: FxGuideStep = {
  title: '行操作',
  description: '每行右侧通常提供编辑、删除、授权、查看详情等操作，请先确认目标记录再点击。',
  target: '.ant-table-tbody .ant-table-cell-fix-right, .ant-table-tbody .ant-table-cell:last-child',
  placement: 'left',
  category: 'detail',
}

const pageGuides: Record<string, PageGuideMeta> = {
  dashboard: {
    title: '系统管理主页',
    intro: '系统管理主页用于集中查看系统运行、权限配置和基础参数入口。这里更适合从整体上进入用户、角色、日志和系统配置等能力。',
    includeQuery: false,
    includeToolbar: false,
    includeColumnSetting: false,
    includeTable: false,
    includePagination: false,
    extraSteps: [
      {
        title: '模块首页内容',
        description: '主页卡片会按系统管理常用场景聚合入口，点击卡片右侧进入对应功能页面。',
        target: '[data-guide-id="module-homepage-stage"]',
        placement: 'top',
        category: 'navigation',
      },
    ],
  },
  user: {
    title: '用户管理',
    intro: '用户管理用于维护登录账号、人员状态、所属部门、岗位和角色关系，是系统权限分配的基础入口。',
    actions: [
      { id: 'sys-user-add', title: '新增用户', description: '点击这里创建新的系统账号，填写账号、姓名、部门、岗位等基础信息。' },
      { id: 'sys-user-sync-third-party', title: '同步第三方', description: '点击这里把本系统用户变更同步给已接入的第三方系统。' },
      { id: 'sys-user-pull-third-party', title: '从第三方拉取', description: '点击这里从第三方系统拉取用户数据，并刷新当前用户列表。' },
      { id: 'sys-user-import', title: '导入用户', description: '点击这里上传用户 Excel 文件，适合批量创建或维护账号。' },
      { id: 'sys-user-download-template', title: '下载模板', description: '点击这里下载标准导入模板，按模板填写后再执行导入。' },
      { id: 'sys-user-batch-delete', title: '批量删除', description: '先勾选列表左侧复选框，再点击这里批量删除选中的用户。' },
      { id: 'sys-user-export', title: '导出用户', description: '点击这里按当前筛选条件导出用户列表。' },
      { id: 'sys-user-row-edit', title: '编辑用户', description: '在行操作中点击编辑，可以维护当前用户的基础资料。', placement: 'left' },
      { id: 'sys-user-row-status', title: '启用或停用', description: '这里用于快速切换用户账号状态，停用后该账号不能正常使用。', placement: 'left' },
      { id: 'sys-user-row-reset-password', title: '重置密码', description: '点击这里为当前用户重置登录密码，适合用户忘记密码时使用。', placement: 'left' },
      { id: 'sys-user-row-assign-role', title: '分配角色', description: '点击这里调整当前用户拥有的角色，从而控制菜单和按钮权限。', placement: 'left' },
      { id: 'sys-user-row-delete', title: '删除用户', description: '点击这里删除当前用户，操作前请确认账号不再使用。', placement: 'left' },
    ],
  },
  department: {
    title: '部门管理',
    intro: '部门管理用于维护企业组织结构，用户、岗位和权限数据会关联到部门层级。',
    actions: [
      { id: 'sys-dept-add', title: '新增下级部门', description: '选择左侧部门后点击这里，可以在当前部门下创建子部门。' },
      { id: 'sys-dept-edit', title: '编辑部门', description: '点击这里维护所选部门名称、编码、负责人等信息。' },
      { id: 'sys-dept-delete', title: '删除部门', description: '点击这里删除所选部门。删除前请确认没有下级部门或关联用户。' },
      { id: 'sys-dept-save', title: '保存部门', description: '编辑完成后点击保存，让部门信息生效。' },
    ],
    includeQuery: false,
  },
  position: {
    title: '职位管理',
    intro: '职位管理用于维护员工岗位名称和排序，可在用户资料中关联岗位。',
    actions: [
      { id: 'sys-position-add', title: '新增职位', description: '点击这里新增岗位，例如主管、工程师、质检员等。' },
    ],
  },
  inviteCode: {
    title: '邀请码管理',
    intro: '邀请码管理用于生成和维护注册邀请凭证，适合控制外部用户或租户注册入口。',
    actions: [
      { id: 'sys-invite-add', title: '新增邀请码', description: '点击这里创建新的邀请码，并设置有效期、使用次数等规则。' },
    ],
  },
  role: {
    title: '角色管理',
    intro: '角色管理用于维护权限集合。用户被授予角色后，会继承角色对应的菜单和按钮权限。',
    actions: [
      { id: 'sys-role-add', title: '新增角色', description: '点击这里创建新的角色，例如系统管理员、业务管理员、只读用户等。' },
      { id: 'sys-role-batch-delete', title: '批量删除角色', description: '先勾选角色，再点击这里批量删除不再使用的角色。' },
      { id: 'sys-role-row-edit', title: '编辑角色', description: '点击这里维护角色名称、编码、状态和排序。', placement: 'left' },
      { id: 'sys-role-row-menu-grant', title: '菜单授权', description: '点击这里进入菜单授权页面，为角色配置可访问的菜单和按钮。', placement: 'left' },
      { id: 'sys-role-row-user-grant', title: '人员授权', description: '点击这里进入人员授权页面，为角色批量绑定或移除用户。', placement: 'left' },
      { id: 'sys-role-row-delete', title: '删除角色', description: '点击这里删除当前角色。删除前请确认没有用户依赖该角色。', placement: 'left' },
    ],
  },
  'role/MenuGrant': {
    title: '菜单授权',
    intro: '菜单授权用于为角色勾选可访问的模块、菜单和按钮，是角色权限生效的关键步骤。',
    includeQuery: false,
    includeToolbar: false,
    includeTable: false,
    includeColumnSetting: false,
    includePagination: false,
    actions: [
      { id: 'sys-role-menu-grant-save', title: '保存授权', description: '勾选菜单或按钮后，点击这里保存当前角色的授权结果。' },
      { id: 'sys-role-menu-grant-select-all', title: '全选权限', description: '点击这里选中当前模块下全部菜单和按钮，适合快速授予完整权限。' },
      { id: 'sys-role-menu-grant-invert', title: '反选权限', description: '点击这里把已选和未选权限互换，适合批量调整授权范围。' },
      { id: 'sys-role-menu-grant-clear', title: '清空权限', description: '点击这里清空当前模块下已选择的菜单和按钮。' },
      { id: 'sys-role-menu-grant-search', title: '搜索菜单', description: '在这里输入菜单名称或权限关键字，可以快速定位要授权的菜单项。' },
    ],
    extraSteps: [
      {
        title: '选择授权模块',
        description: '左侧按模块过滤菜单树。先选择模块，再在右侧勾选该模块下的菜单和按钮权限。',
        target: guideTarget('sys-role-menu-grant-module-filter'),
        placement: 'right',
        category: 'navigation',
      },
      {
        title: '权限树',
        description: '在这里勾选角色可访问的菜单和按钮权限，保存后该角色下的用户会获得对应能力。',
        target: guideTarget('fx-table-content'),
        placement: 'top',
        category: 'detail',
      },
    ],
  },
  'role/UserGrant': {
    title: '人员授权',
    intro: '人员授权用于把用户批量加入或移出当前角色，适合快速调整一组用户的权限。',
    includeQuery: false,
    includeToolbar: false,
    includeColumnSetting: false,
    actions: [
      { id: 'sys-role-user-grant-add', title: '加入授权', description: '选择左侧用户、部门或职位后，点击这里把对象加入当前角色。' },
      { id: 'sys-role-user-grant-select-all', title: '全选对象', description: '点击这里选中当前页签下的全部授权对象。' },
      { id: 'sys-role-user-grant-clear', title: '清空选择', description: '点击这里清空当前页签下已经选中的对象。' },
      { id: 'sys-role-user-grant-batch-revoke', title: '批量移除', description: '先勾选右侧已授权列表，再点击这里批量移除这些对象的角色授权。' },
      { id: 'sys-role-user-grant-row-revoke', title: '单条移除', description: '在行操作里点击移除，可以取消单个对象的角色授权。', placement: 'left' },
    ],
    extraSteps: [
      {
        title: '选择授权对象',
        description: '左侧可以按用户、部门或职位选择授权对象，适合不同组织维度的批量授权。',
        target: guideTarget('sys-role-user-grant-object-panel'),
        placement: 'right',
        category: 'form',
      },
    ],
  },
  tenant: {
    title: '租户管理',
    intro: '租户管理用于维护系统中的租户主体，控制租户名称、状态、类型和隔离边界。',
    actions: [
      { id: 'sys-tenant-add', title: '新增租户', description: '点击这里创建租户，填写租户名称、编码和基础配置。' },
    ],
  },
  menu: {
    title: '菜单管理',
    intro: '菜单管理用于维护模块菜单、路由组件和按钮权限，是动态路由和权限控制的基础。',
    actions: [
      { id: 'sys-menu-add', title: '新增菜单', description: '点击这里新增目录、菜单或按钮权限，并设置路由、组件和权限标识。' },
      { id: 'sys-menu-batch-delete', title: '批量删除菜单', description: '先勾选菜单，再点击这里删除不再使用的菜单项。' },
    ],
  },
  module: {
    title: '模块管理',
    intro: '模块管理用于维护系统顶部模块入口，控制模块名称、图标、排序和可见状态。',
    actions: [
      { id: 'sys-module-add', title: '新增模块', description: '点击这里新增一个业务模块入口。' },
      { id: 'sys-module-batch-delete', title: '批量删除模块', description: '先勾选模块，再点击这里批量删除。' },
    ],
  },
  dict: {
    title: '字典管理',
    intro: '字典管理用于维护下拉选项、状态枚举和标签样式，多个业务页面会复用这些字典值。',
    actions: [
      { id: 'sys-dict-add', title: '新增字典', description: '点击这里新增字典类型或字典项。' },
    ],
  },
  excelImportConfig: {
    title: '导入配置',
    intro: '导入配置用于维护 Excel 导入模板、字段映射和校验规则，让批量导入保持一致。',
    actions: [
      { id: 'sys-excel-import-edit', title: '编辑导入配置', description: '点击这里维护当前导入配置的字段映射、模板和规则。' },
    ],
  },
  excelExportConfig: {
    title: '导出配置',
    intro: '导出配置用于维护 Excel 导出字段、排序和格式规则，控制不同页面导出文件的结构。',
    actions: [
      { id: 'sys-excel-export-edit', title: '编辑导出配置', description: '点击这里维护导出字段和显示顺序。' },
    ],
  },
  tableConfig: {
    title: '表格配置',
    intro: '表格配置用于维护动态表格的列、查询条件和默认展示规则，是配置驱动页面的基础。',
    actions: [
      { id: 'sys-table-config-pull-public', title: '拉取公共配置', description: '点击这里把公共级表格配置拉取到当前租户，便于租户级调整。' },
      { id: 'sys-table-config-add', title: '新增表格配置', description: '点击这里新增一个动态表格配置。' },
      { id: 'sys-table-config-batch-delete', title: '批量删除', description: '先勾选表格配置，再点击这里批量删除。' },
    ],
  },
  userTableConfig: {
    title: '用户列设置',
    intro: '用户列设置用于查看和调整用户自己的表格列偏好，解决不同用户列表展示习惯不同的问题。',
    actions: [
      { id: 'sys-user-table-search', title: '查询列设置', description: '点击这里按当前条件查询用户列配置。' },
      { id: 'sys-user-table-reset', title: '重置查询', description: '点击这里清空筛选条件并恢复默认查询。' },
    ],
    includeToolbar: false,
  },
  loginLog: {
    title: '登录日志',
    intro: '登录日志用于追踪用户登录行为，便于排查账号异常、登录失败和安全审计问题。',
    actions: [
      { id: 'sys-login-log-export', title: '导出登录日志', description: '点击这里按当前筛选条件导出登录日志。' },
    ],
  },
  online: {
    title: '在线用户',
    intro: '在线用户用于查看当前登录会话，并支持强制下线异常或不再允许保留的会话。',
    actions: [
      { id: 'sys-online-kickout', title: '批量下线', description: '先勾选在线会话，再点击这里强制下线。' },
    ],
  },
  operationLog: {
    title: '操作日志',
    intro: '操作日志用于审计用户在系统中的关键操作，包含请求、响应和执行结果信息。',
    actions: [
      { id: 'sys-operation-log-export', title: '导出操作日志', description: '点击这里按当前筛选条件导出操作日志。' },
    ],
  },
  config: {
    title: '系统配置',
    intro: '系统配置用于维护门户、登录、安全、邮件、上传、加密和个人首页等系统级参数。',
    includeQuery: false,
    includeToolbar: false,
    includeTable: false,
    actions: [
      { id: 'sys-config-save', title: '保存当前配置', description: '在各配置页签调整参数后，点击保存让配置生效。' },
      { id: 'sys-config-reset', title: '重置当前配置', description: '点击这里恢复当前配置表单的默认或最近保存状态。' },
    ],
    extraSteps: [
      {
        title: '配置页签',
        description: '系统配置按能力拆成多个页签，切换页签可以维护不同类型的系统参数。',
        target: '.ant-tabs-nav',
        placement: 'bottom',
        category: 'navigation',
      },
    ],
  },
  messageTemplate: {
    title: '消息模板',
    intro: '消息模板用于配置系统通知、站内信等内容模板，统一管理不同租户或公共级的消息格式。',
    actions: [
      { id: 'sys-message-template-pull-public', title: '拉取公共配置', description: '点击这里把公共模板拉取到当前租户，便于租户级维护。' },
      { id: 'sys-message-template-add', title: '新增模板', description: '点击这里新增消息模板。' },
      { id: 'sys-message-template-batch-delete', title: '批量删除', description: '先勾选模板，再点击这里批量删除。' },
    ],
  },
  tenantMessageWhitelist: {
    title: '租户消息白名单',
    intro: '租户消息白名单用于控制租户可使用的消息模板或消息能力，避免未授权消息发送。',
    actions: [
      { id: 'sys-tenant-message-whitelist-add', title: '新增白名单', description: '点击这里为租户新增一条消息白名单配置。' },
      { id: 'sys-tenant-message-whitelist-batch-delete', title: '批量删除', description: '先勾选白名单记录，再点击这里批量删除。' },
    ],
  },
  i18nLanguageType: {
    title: '语言配置',
    intro: '语言配置用于维护系统支持的语言类型，是多语言消息和页面显示切换的基础。',
    actions: [
      { id: 'sys-i18n-language-add', title: '新增语言', description: '点击这里新增系统支持的语言类型。' },
      { id: 'sys-i18n-language-import', title: '导入语言配置', description: '点击这里上传语言配置 Excel 文件。' },
      { id: 'sys-i18n-language-template', title: '下载模板', description: '点击这里下载语言配置导入模板。' },
    ],
  },
  i18nMessage: {
    title: '多语言消息',
    intro: '多语言消息用于维护页面文案、提示消息和业务枚举的多语言翻译。',
    actions: [
      { id: 'sys-i18n-message-add', title: '新增消息', description: '点击这里新增一条多语言消息。' },
    ],
  },
  file: {
    title: '文件管理',
    intro: '文件管理用于查看系统上传文件、访问地址和基础元数据，便于排查文件上传和引用问题。',
  },
  codegen: {
    title: '代码生成',
    intro: '代码生成用于根据数据表和模板生成后端、前端代码，提高标准管理页面的开发效率。',
    actions: [
      { id: 'sys-codegen-add', title: '新增代码生成', description: '点击这里新增一项代码生成任务或表配置。' },
    ],
  },
  codegenDatasource: {
    title: '代码生成数据源',
    intro: '代码生成数据源用于维护代码生成可连接的数据库信息，供生成器读取表结构。',
    actions: [
      { id: 'sys-codegen-datasource-add', title: '新增数据源', description: '点击这里新增代码生成使用的数据源。' },
    ],
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

function buildFallbackMeta(key: string): PageGuideMeta {
  const title = key
    .split('/')
    .filter(Boolean)
    .pop()
    ?.replace(/([A-Z])/g, ' $1')
    .trim() || '系统页面'
  return {
    title,
    intro: `${title}用于维护系统管理相关数据。请先通过查询区定位数据，再使用工具栏或行操作完成维护。`,
  }
}

function createSteps(meta: PageGuideMeta): FxGuideStep[] {
  const steps: FxGuideStep[] = [
    {
      title: meta.title,
      description: meta.intro,
      placement: 'center',
      useMask: false,
      category: 'intro',
    },
  ]

  if (meta.includeQuery !== false) {
    steps.push(commonQueryStep)
  }

  if (meta.includeToolbar !== false) {
    steps.push(commonToolbarStep)
  }

  for (const action of meta.actions || []) {
    steps.push({
      title: action.title,
      description: action.description,
      target: guideTarget(action.id),
      placement: action.placement || 'bottom',
      category: 'action',
    })
  }

  if (meta.includeColumnSetting !== false) {
    steps.push(commonColumnSettingStep)
  }

  if (meta.includeTable !== false) {
    steps.push(commonTableStep)
  }

  if (meta.includeRowActions === true) {
    steps.push(commonRowActionStep)
  }

  if (meta.includePagination !== false) {
    steps.push(commonPaginationStep)
  }

  if (meta.extraSteps?.length) {
    steps.push(...meta.extraSteps)
  }

  return steps
}

export function resolveSystemPageGuide(path: string): SystemPageGuideConfig {
  const key = normalizeSystemGuidePath(path)
  const meta = pageGuides[key] || buildFallbackMeta(key)
  return {
    guideCode: buildSystemGuideCode(key),
    version: SYSTEM_GUIDE_VERSION,
    steps: createSteps(meta),
  }
}

export function listSystemPageGuideCodes() {
  return Object.keys(pageGuides).map(buildSystemGuideCode)
}
