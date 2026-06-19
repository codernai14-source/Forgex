/**
 * 工段主数据 API 封装。
 * <p>
 * 对应后端 {@code /basic/workSection/*} 接口，提供工段主数据的分页查询、列表查询
 * （按车间 / 产线过滤）、详情、新增、修改、删除（单条 + 批量）能力。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0
 * @since 2026-06-19
 * @see BasicWorkSection
 */
import http from '@/api/http'

/**
 * 工段主数据对象。
 * <p>
 * 与后端 {@code BasicWorkSection} / {@code WorkSectionDTO} 字段一一对应，
 * 关联车间与产线均使用 ID + 编码 + 名称快照存储。
 * </p>
 */
export interface WorkSection {
  /** 主键 ID */
  id?: number
  /** 工段编码（租户内唯一） */
  workSectionCode?: string
  /** 工段名称 */
  workSectionName?: string
  /** 所属车间 ID */
  workshopId?: number
  /** 所属车间编码（快照） */
  workshopCode?: string
  /** 所属车间名称（快照） */
  workshopName?: string
  /** 所属产线 ID（逻辑外键，可空） */
  productionLineId?: number
  /** 所属产线编码（快照） */
  productionLineCode?: string
  /** 所属产线名称（快照） */
  productionLineName?: string
  /** 顺序号 */
  sortOrder?: number
  /** 是否启用：0=禁用，1=启用 */
  status?: number
  /** 备注 */
  remark?: string
  /** 创建时间 */
  createTime?: string
  /** 更新时间 */
  updateTime?: string
}

/**
 * 工段分页查询参数。
 */
export interface WorkSectionPageParam {
  /** 当前页码 */
  pageNum: number
  /** 每页条数 */
  pageSize: number
  /** 工段编码（模糊） */
  workSectionCode?: string
  /** 工段名称（模糊） */
  workSectionName?: string
  /** 所属车间 ID（精确） */
  workshopId?: number
  /** 所属产线 ID（精确） */
  productionLineId?: number
  /** 状态：0=禁用，1=启用 */
  status?: number
}

/**
 * 工段 API 客户端。
 */
export const workSectionApi = {
  /**
   * 分页查询工段。
   *
   * @param params 分页 + 查询条件
   * @returns 分页结果（{@code records + total}）
   */
  page(params: WorkSectionPageParam) {
    return http.post('/basic/workSection/page', params)
  },

  /**
   * 查询工段列表（不分页，可按车间 / 产线 / 状态等过滤）。
   *
   * @param params 可选过滤条件
   * @returns 工段列表
   */
  list(params?: Partial<WorkSectionPageParam>) {
    return http.post<WorkSection[]>('/basic/workSection/list', params || {})
  },

  /**
   * 按车间 ID 拉取工段下拉。
   *
   * @param workshopId 所属车间 ID
   * @returns 该车间下的工段列表
   */
  listByWorkshop(workshopId: number) {
    return http.post<WorkSection[]>('/basic/workSection/listByWorkshop', { workshopId })
  },

  /**
   * 按产线 ID 拉取工段下拉（仅启用状态）。
   *
   * @param productionLineId 所属产线 ID
   * @returns 该产线下的工段列表
   */
  listByProductionLine(productionLineId: number) {
    return http.post<WorkSection[]>('/basic/workSection/listByProductionLine', { productionLineId })
  },

  /**
   * 获取工段详情。
   *
   * @param id 工段主键 ID
   * @returns 工段详情对象
   */
  detail(id: number) {
    return http.post<WorkSection>('/basic/workSection/detail', { id })
  },

  /**
   * 新增工段。
   *
   * @param data 工段数据
   * @returns 新增工段的主键 ID
   */
  create(data: WorkSection) {
    return http.post<number>('/basic/workSection/create', data)
  },

  /**
   * 更新工段。
   *
   * @param data 工段数据（必须包含 id）
   * @returns 是否更新成功
   */
  update(data: WorkSection) {
    return http.post<boolean>('/basic/workSection/update', data)
  },

  /**
   * 删除单条工段。
   *
   * @param id 工段主键 ID
   * @returns 是否删除成功
   */
  delete(id: number) {
    return http.post<boolean>('/basic/workSection/delete', { id })
  },

  /**
   * 批量删除工段。
   *
   * @param ids 工段主键 ID 数组
   * @returns 是否删除成功
   */
  batchDelete(ids: number[]) {
    return http.post<boolean>('/basic/workSection/batchDelete', { ids })
  },
}
