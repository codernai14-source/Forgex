/**
 * 部门管理模块 - 类型定义
 */

/**
 * 部门信息（组织架构）
 */
export interface Department {
  id?: string
  parentId: string
  orgType: 'group' | 'company' | 'subsidiary' | 'department' | 'team'  // 组织类型
  orgLevel: number        // 组织层级：1=集团, 2=公司, 3=子公司, 4=部门, 5=班组
  deptName: string
  deptCode: string
  leader?: string
  phone?: string
  email?: string
  orderNum: number
  status: number
  createTime?: string
  updateTime?: string
  children?: Department[]  // 子部门
}

/**
 * 部门查询参数
 */
export interface DepartmentQuery {
  deptName?: string
  orgType?: string
  status?: number
}

/**
 * 部门树节点
 */
export interface DepartmentTreeNode {
  key: string
  title: string
  value: string
  orgType: string
  orgLevel: number
  children?: DepartmentTreeNode[]
}

/**
 * 组织类型选项
 */
export const ORG_TYPE_OPTIONS = [
  { label: '集团', value: 'group', level: 1 },
  { label: '公司', value: 'company', level: 2 },
  { label: '子公司', value: 'subsidiary', level: 3 },
  { label: '部门', value: 'department', level: 4 },
  { label: '班组', value: 'team', level: 5 },
]

/**
 * 组织层级选项
 */
export const ORG_LEVEL_OPTIONS = [
  { label: '集团', value: 1 },
  { label: '公司', value: 2 },
  { label: '子公司', value: 3 },
  { label: '部门', value: 4 },
  { label: '班组', value: 5 },
]

/**
 * 根据组织类型获取层级
 */
export function getOrgLevelByType(orgType: string): number {
  const option = ORG_TYPE_OPTIONS.find(opt => opt.value === orgType)
  return option?.level || 1
}

/**
 * 根据层级获取组织类型
 */
export function getOrgTypeByLevel(orgLevel: number): string {
  const option = ORG_TYPE_OPTIONS.find(opt => opt.level === orgLevel)
  return option?.value || 'group'
}

/**
 * 根据组织类型获取标签
 */
export function getOrgTypeLabel(orgType: string): string {
  const option = ORG_TYPE_OPTIONS.find(opt => opt.value === orgType)
  return option?.label || '未知'
}
