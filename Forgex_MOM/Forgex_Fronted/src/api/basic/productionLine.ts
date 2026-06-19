/**
 * 产线主数据 API 封装。
 * <p>
 * 对应后端 {@code /basic/productionLine/*} 接口，提供产线主数据的分页查询、列表查询（按车间过滤）、
 * 详情、新增、修改、删除（单条 + 批量）能力。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0
 * @since 2026-06-19
 * @see BasicProductionLine
 */
import http from '@/api/http'

/**
 * 产线主数据对象。
 * <p>
 * 与后端 {@code BasicProductionLine} / {@code ProductionLineDTO} 字段一一对应，
 * 关联车间（{@code workshopId / workshopCode / workshopName}）、负责人
 * （{@code managerEmployeeId / managerEmployeeNo / managerEmployeeName}）使用 ID + 名称快照存储。
 * </p>
 */
export interface ProductionLine {
  /** 主键 ID */
  id?: number
  /** 产线编码（租户内唯一） */
  productionLineCode?: string
  /** 产线名称 */
  productionLineName?: string
  /** 所属车间 ID */
  workshopId?: number
  /** 所属车间编码（快照） */
  workshopCode?: string
  /** 所属车间名称（快照） */
  workshopName?: string
  /** 产线类型（字典：prod_line_type） */
  productionLineType?: string
  /** 负责人 ID */
  managerEmployeeId?: number
  /** 负责人工号（快照） */
  managerEmployeeNo?: string
  /** 负责人姓名（快照） */
  managerEmployeeName?: string
  /** 排序号 */
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
 * 产线分页查询参数。
 */
export interface ProductionLinePageParam {
  /** 当前页码 */
  pageNum: number
  /** 每页条数 */
  pageSize: number
  /** 产线编码（模糊） */
  productionLineCode?: string
  /** 产线名称（模糊） */
  productionLineName?: string
  /** 所属车间 ID（精确） */
  workshopId?: number
  /** 产线类型（字典：prod_line_type） */
  productionLineType?: string
  /** 状态：0=禁用，1=启用 */
  status?: number
}

/**
 * 产线 API 客户端。
 * <p>
 * 所有方法统一通过 {@code http.post} 调用后端 R&lt;T&gt; 接口，列表与下拉返回 {@code ProductionLine} 数组，
 * 分页接口依赖 FxDynamicTable 约定的 {@code { records, total }} 结构。
 * </p>
 */
export const productionLineApi = {
  /**
   * 分页查询产线。
   *
   * @param params 分页 + 查询条件
   * @returns 分页结果（{@code records + total}）
   */
  page(params: ProductionLinePageParam) {
    return http.post('/basic/productionLine/page', params)
  },

  /**
   * 查询产线列表（不分页，可按车间、状态等过滤）。
   *
   * @param params 可选过滤条件
   * @returns 产线列表
   */
  list(params?: Partial<ProductionLinePageParam>) {
    return http.post<ProductionLine[]>('/basic/productionLine/list', params || {})
  },

  /**
   * 按车间 ID 拉取产线下拉（仅启用状态）。
   *
   * @param workshopId 所属车间 ID
   * @returns 该车间下的产线列表
   */
  listByWorkshop(workshopId: number) {
    return http.post<ProductionLine[]>('/basic/productionLine/listByWorkshop', { workshopId })
  },

  /**
   * 获取产线详情。
   *
   * @param id 产线主键 ID
   * @returns 产线详情对象
   */
  detail(id: number) {
    return http.post<ProductionLine>('/basic/productionLine/detail', { id })
  },

  /**
   * 新增产线。
   *
   * @param data 产线数据
   * @returns 新增产线的主键 ID
   */
  create(data: ProductionLine) {
    return http.post<number>('/basic/productionLine/create', data)
  },

  /**
   * 更新产线。
   *
   * @param data 产线数据（必须包含 id）
   * @returns 是否更新成功
   */
  update(data: ProductionLine) {
    return http.post<boolean>('/basic/productionLine/update', data)
  },

  /**
   * 删除单条产线。
   *
   * @param id 产线主键 ID
   * @returns 是否删除成功
   */
  delete(id: number) {
    return http.post<boolean>('/basic/productionLine/delete', { id })
  },

  /**
   * 批量删除产线。
   *
   * @param ids 产线主键 ID 数组
   * @returns 是否删除成功
   */
  batchDelete(ids: number[]) {
    return http.post<boolean>('/basic/productionLine/batchDelete', { ids })
  },
}
