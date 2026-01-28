/**
 * 用户管理模块 - 类型定义
 */

/**
 * 用户基本信息
 */
export interface User {
  id?: string
  username: string
  email: string
  phone: string
  gender: 0 | 1 | 2  // 0=未知，1=男，2=女
  entryDate: string  // 入职时间
  departmentId: string
  departmentName?: string  // 部门名称（关联查询）
  positionId: string
  positionName?: string    // 职位名称（关联查询）
  tenantId?: string    // 租户ID（用户绑定的主租户ID）
  status: boolean | number
  createTime?: string
  updateTime?: string
}

/**
 * 用户租户关联信息
 */
export interface UserTenant {
  userId: string
  tenantId: string
  tenantName?: string
  prefOrder: number
  isDefault: boolean
  lastUsed?: string
}

/**
 * 用户附属信息
 */
export interface UserProfile {
  id?: string
  userId: string
  politicalStatus?: string  // 政治面貌
  homeAddress?: string      // 家庭住址
  emergencyContact?: string // 紧急联系人
  emergencyPhone?: string   // 紧急联系人电话
  referrer?: string         // 引荐人
  education?: string        // 学历
  birthPlace?: string       // 籍贯
  intro?: string            // 个人简介
  workHistory?: WorkHistory[] // 历史工作信息
}

/**
 * 工作经历
 */
export interface WorkHistory {
  company: string      // 公司名称
  position: string     // 职位
  startDate: string    // 开始时间
  endDate: string      // 结束时间
  description?: string // 工作描述
}

/**
 * 用户查询参数
 */
export interface UserQuery {
  username?: string
  phone?: string
  departmentId?: string
  positionId?: string
  status?: boolean
  pageNum: number
  pageSize: number
}

/**
 * 部门信息（组织架构）
 */
export interface Department {
  id: string
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
  children?: Department[]  // 子部门
}

/**
 * 职位信息
 */
export interface Position {
  id: string
  positionName: string
  positionCode: string
  positionLevel?: number
  orderNum: number
  status: number
  remark?: string
}

/**
 * 性别选项
 */
export const GENDER_OPTIONS = [
  { label: '未知', value: 0 },
  { label: '男', value: 1 },
  { label: '女', value: 2 },
]

/**
 * 政治面貌选项
 */
export const POLITICAL_STATUS_OPTIONS = [
  { label: '群众', value: '群众' },
  { label: '共青团员', value: '共青团员' },
  { label: '中共党员', value: '中共党员' },
  { label: '民主党派', value: '民主党派' },
  { label: '无党派人士', value: '无党派人士' },
]

/**
 * 学历选项
 */
export const EDUCATION_OPTIONS = [
  { label: '小学', value: '小学' },
  { label: '初中', value: '初中' },
  { label: '高中', value: '高中' },
  { label: '中专', value: '中专' },
  { label: '大专', value: '大专' },
  { label: '本科', value: '本科' },
  { label: '硕士', value: '硕士' },
  { label: '博士', value: '博士' },
]
