/**
 * 工序主数据 API 封装。
 * <p>
 * 对应后端 {@code /basic/process/*} 接口，提供工序主数据的分页查询、列表查询
 * （按工段过滤）、详情、新增、修改、删除（单条 + 批量）能力。
 * 工序类型（{@code processType}）、报工方式（{@code reportType}）、
 * 质检触发点（{@code qcTriggerPoint}）均使用数据字典存储。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0
 * @since 2026-06-19
 * @see BasicProcess
 */
import http from '@/api/http'

/**
 * 工序主数据对象。
 * <p>
 * 与后端 {@code BasicProcess} / {@code ProcessDTO} 字段一一对应，关联工段 / 产线 / 车间
 * 均使用 ID + 编码 + 名称快照存储，避免历史数据随主数据变更而失真。
 * </p>
 */
export interface Process {
  /** 主键 ID */
  id?: number
  /** 工序编码（租户内唯一） */
  processCode?: string
  /** 工序名称 */
  processName?: string
  /** 所属工段 ID */
  workSectionId?: number
  /** 所属工段编码（快照） */
  workSectionCode?: string
  /** 所属工段名称（快照） */
  workSectionName?: string
  /** 所属产线 ID（冗余快照） */
  productionLineId?: number
  /** 所属产线编码（冗余快照） */
  productionLineCode?: string
  /** 所属产线名称（冗余快照） */
  productionLineName?: string
  /** 所属车间 ID（冗余快照） */
  workshopId?: number
  /** 所属车间编码（冗余快照） */
  workshopCode?: string
  /** 所属车间名称（冗余快照） */
  workshopName?: string
  /** 工序类型（字典：process_type） */
  processType?: string
  /** 报工方式（字典：report_type） */
  reportType?: string
  /** 质检触发点（字典：qc_trigger_point） */
  qcTriggerPoint?: string
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
 * 工序分页查询参数。
 */
export interface ProcessPageParam {
  /** 当前页码 */
  pageNum: number
  /** 每页条数 */
  pageSize: number
  /** 工序编码（模糊） */
  processCode?: string
  /** 工序名称（模糊） */
  processName?: string
  /** 所属工段 ID（精确） */
  workSectionId?: number
  /** 所属产线 ID（精确，可选） */
  productionLineId?: number
  /** 所属车间 ID（精确，可选） */
  workshopId?: number
  /** 工序类型（字典：process_type） */
  processType?: string
  /** 报工方式（字典：report_type） */
  reportType?: string
  /** 质检触发点（字典：qc_trigger_point） */
  qcTriggerPoint?: string
  /** 状态：0=禁用，1=启用 */
  status?: number
}

/**
 * 工序 API 客户端。
 */
export const processApi = {
  /**
   * 分页查询工序。
   *
   * @param params 分页 + 查询条件
   * @returns 分页结果（{@code records + total}）
   */
  page(params: ProcessPageParam) {
    return http.post('/basic/process/page', params)
  },

  /**
   * 查询工序列表（不分页，可按工段 / 字典等过滤）。
   *
   * @param params 可选过滤条件
   * @returns 工序列表
   */
  list(params?: Partial<ProcessPageParam>) {
    return http.post<Process[]>('/basic/process/list', params || {})
  },

  /**
   * 按工段 ID 拉取工序下拉（仅启用状态）。
   *
   * @param workSectionId 所属工段 ID
   * @returns 该工段下的工序列表
   */
  listByWorkSection(workSectionId: number) {
    return http.post<Process[]>('/basic/process/listByWorkSection', { workSectionId })
  },

  /**
   * 获取工序详情。
   *
   * @param id 工序主键 ID
   * @returns 工序详情对象
   */
  detail(id: number) {
    return http.post<Process>('/basic/process/detail', { id })
  },

  /**
   * 新增工序。
   *
   * @param data 工序数据
   * @returns 新增工序的主键 ID
   */
  create(data: Process) {
    return http.post<number>('/basic/process/create', data)
  },

  /**
   * 更新工序。
   *
   * @param data 工序数据（必须包含 id）
   * @returns 是否更新成功
   */
  update(data: Process) {
    return http.post<boolean>('/basic/process/update', data)
  },

  /**
   * 删除单条工序。
   *
   * @param id 工序主键 ID
   * @returns 是否删除成功
   */
  delete(id: number) {
    return http.post<boolean>('/basic/process/delete', { id })
  },

  /**
   * 批量删除工序。
   *
   * @param ids 工序主键 ID 数组
   * @returns 是否删除成功
   */
  batchDelete(ids: number[]) {
    return http.post<boolean>('/basic/process/batchDelete', { ids })
  },
}
