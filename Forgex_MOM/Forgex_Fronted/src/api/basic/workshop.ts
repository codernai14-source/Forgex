/**
 * 车间主数据 API 封装。
 * <p>
 * 对应后端 {@code /basic/workshop/*} 接口，提供车间主数据的分页查询、列表查询
 * （全量 / 按工厂过滤）、详情、新增、修改、删除（单条 + 批量）能力。
 * 工厂建模升级后，车间实体新增了 {@code workshopType / workshopManagerId / workshopManagerName}
 * 三个字段，对应车间类型（字典 workshop_type）与负责人员工快照。
 * </p>
 *
 * @author Forgex Team
 * @version 1.1
 * @since 2026-06-19
 * @see BasicWorkshop
 */
import http from '@/api/http'

/**
 * 车间主数据对象。
 * <p>
 * 与后端 {@code BasicWorkshop} / {@code WorkshopDTO} 字段一一对应，
 * 所属工厂、负责人均使用 ID + 编码 / 名称快照存储。
 * </p>
 */
export interface Workshop {
  /** 主键 ID */
  id?: number
  /** 车间编码 */
  workshopCode?: string
  /** 车间名称 */
  workshopName?: string
  /** 所属工厂 ID */
  factoryId?: number
  /** 所属工厂编码（快照） */
  factoryCode?: string
  /** 所属工厂名称（快照） */
  factoryName?: string
  /** 车间类型（字典：workshop_type） */
  workshopType?: string
  /** 负责人 ID（关联 basic_employee） */
  workshopManagerId?: number
  /** 负责人姓名（快照） */
  workshopManagerName?: string
  /** 是否启用：true=启用，false=禁用 */
  status?: boolean
  /** 备注 */
  remark?: string
  /** 创建时间 */
  createTime?: string
  /** 更新时间 */
  updateTime?: string
}

/**
 * 车间分页查询参数。
 */
export interface WorkshopPageParam {
  /** 当前页码 */
  pageNum: number
  /** 每页条数 */
  pageSize: number
  /** 车间编码（模糊） */
  workshopCode?: string
  /** 车间名称（模糊） */
  workshopName?: string
  /** 所属工厂 ID（精确） */
  factoryId?: number
  /** 车间类型（字典：workshop_type） */
  workshopType?: string
  /** 状态 */
  status?: boolean
}

/**
 * 车间 API 客户端。
 */
export const workshopApi = {
  /**
   * 分页查询车间。
   *
   * @param params 分页 + 查询条件
   * @returns 分页结果（{@code records + total}）
   */
  page(params: WorkshopPageParam) {
    return http.post('/basic/workshop/page', params)
  },

  /**
   * 查询车间列表（不分页，可按工厂 / 状态等过滤）。
   *
   * @param params 可选过滤条件
   * @returns 车间列表
   */
  list(params?: Partial<WorkshopPageParam>) {
    return http.post<Workshop[]>('/basic/workshop/list', params || {})
  },

  /**
   * 按工厂 ID 拉取车间下拉（仅启用状态）。
   * <p>
   * 工厂建模升级后新增接口，供产线 / 工段等下拉筛选使用，
   * 避免一次性全量加载车间数据。
   * </p>
   *
   * @param factoryId 所属工厂 ID
   * @returns 该工厂下的车间列表
   */
  listByFactory(factoryId: number) {
    return http.post<Workshop[]>('/basic/workshop/listByFactory', { factoryId })
  },

  /**
   * 获取车间详情。
   *
   * @param id 车间主键 ID
   * @returns 车间详情对象
   */
  detail(id: number) {
    return http.post<Workshop>('/basic/workshop/detail', { id })
  },

  /**
   * 新增车间。
   *
   * @param data 车间数据
   * @returns 新增车间的主键 ID
   */
  create(data: Workshop) {
    return http.post<number>('/basic/workshop/create', data)
  },

  /**
   * 更新车间。
   *
   * @param data 车间数据（必须包含 id）
   * @returns 是否更新成功
   */
  update(data: Workshop) {
    return http.post<boolean>('/basic/workshop/update', data)
  },

  /**
   * 删除单条车间。
   *
   * @param id 车间主键 ID
   * @returns 是否删除成功
   */
  delete(id: number) {
    return http.post<boolean>('/basic/workshop/delete', { id })
  },

  /**
   * 批量删除车间。
   *
   * @param ids 车间主键 ID 数组
   * @returns 是否删除成功
   */
  batchDelete(ids: number[]) {
    return http.post<boolean>('/basic/workshop/batchDelete', { ids })
  },
}
