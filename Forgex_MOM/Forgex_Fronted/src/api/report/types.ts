/**
 * 鎶ヨ〃妯″潡 TypeScript 绫诲瀷瀹氫箟
 * 瀹氫箟鎶ヨ〃绠＄悊銆佹暟鎹簮绠＄悊鐩稿叧鐨勭被鍨嬫帴鍙?
 * @author Forgex Team
 * @version 1.0
 * @since 2026-04-09
 */

/**
 * 鎶ヨ〃寮曟搸绫诲瀷鏋氫妇
 */
export type ReportEngineType = 'UREPORT' | 'JIMU'

/**
 * 鎶ヨ〃妯℃澘鐘舵€佹灇涓?
 */
export type Report状态 = 0 | 1

/**
 * 鎶ヨ〃妯℃澘绫诲瀷瀹氫箟
 * 瀵瑰簲鏁版嵁搴撹〃锛歠x_report_template
 * @see ReportDatasource - 鍏宠仈鐨勬暟鎹簮
 * @see ReportCategory - 鍏宠仈鐨勫垎绫?
 */
export interface ReportTemplate {
  /**
   * 涓婚敭 ID锛堥洩鑺辩畻娉曠敓鎴愶級
   */
  id: number

  /**
   * 鎶ヨ〃鍚嶇О
   */
  name: string

  /**
   * 鎶ヨ〃缂栫爜锛堝敮涓€鏍囪瘑锛?
   */
  code: string

  /**
   * 鎶ヨ〃寮曟搸绫诲瀷
   * @see ReportEngineType#UREPORT - UReport2 寮曟搸
   * @see ReportEngineType#JIMU - JimuReport 寮曟搸
   */
  engineType: ReportEngineType

  /**
   * 鍒嗙被 ID
   */
  categoryId?: number

  /**
   * 鍒嗙被鍚嶇О锛堝彧璇伙紝鐢ㄤ簬灞曠ず锛?
   */
  categoryName?: string

  /**
   * 鏁版嵁婧?ID
   */
  datasourceId?: number

  /**
   * 鏁版嵁婧愬悕绉帮紙鍙锛岀敤浜庡睍绀猴級
   */
  datasourceName?: string

  /**
   * 鎶ヨ〃鍐呭锛圝SON 鏍煎紡锛屽瓨鍌ㄦ姤琛ㄨ璁″櫒閰嶇疆锛?
   */
  content?: string

  /**
   * 鎶ヨ〃鐘舵€侊細0-绂佺敤锛?-鍚敤
   * @see Report状态#DISABLED - 绂佺敤
   * @see Report状态#ENABLED - 鍚敤
   */
  status: Report状态

  /**
   * 澶囨敞
   */
  remark?: string

  /**
   * 鍒涘缓鏃堕棿
   */
  createTime?: string

  /**
   * 鏇存柊鏃堕棿
   */
  updateTime?: string
}

/**
 * 鎶ヨ〃鍒嗙被绫诲瀷瀹氫箟
 * 瀵瑰簲鏁版嵁搴撹〃锛歠x_report_category
 */
export interface ReportCategory {
  /**
   * 涓婚敭 ID
   */
  id: number

  /**
   * 鍒嗙被鍚嶇О
   */
  name: string

  /**
   * 鍒嗙被缂栫爜
   */
  code?: string

  /**
   * 鐖跺垎绫?ID锛? 琛ㄧず椤剁骇鍒嗙被锛?
   */
  parentId?: number

  /**
   * 鎺掑簭鍙?
   */
  orderNum?: number

  /**
   * 澶囨敞
   */
  remark?: string

  /**
   * 鍒涘缓鏃堕棿
   */
  createTime?: string

  /**
   * 鏇存柊鏃堕棿
   */
  updateTime?: string

  /**
   * 瀛愬垎绫伙紙鏍戝舰缁撴瀯浣跨敤锛?
   */
  children?: ReportCategory[]
}

/**
 * 鎶ヨ〃鏁版嵁婧愮被鍨嬪畾涔?
 * 瀵瑰簲鏁版嵁搴撹〃锛歠x_report_datasource
 */
export interface ReportDatasource {
  /**
   * 涓婚敭 ID
   */
  id: number

  /**
   * 鏁版嵁婧愬悕绉?
   */
  name: string

  /**
   * 鏁版嵁婧愮紪鐮侊紙鍞竴鏍囪瘑锛?
   */
  code: string

  /**
   * 鏁版嵁婧愮被鍨嬶細mysql, oracle, postgresql, sqlserver 绛?
   */
  type: string

  /**
   * 鏁版嵁搴?URL
   */
  url: string

  /**
   * 鏁版嵁搴撶敤鎴峰悕
   */
  username: string

  /**
   * 鏁版嵁搴撳瘑鐮侊紙鍔犲瘑瀛樺偍锛?
   */
  password?: string

  /**
   * 鏁版嵁搴撻┍鍔ㄧ被鍚?
   */
  driverClass?: string

  /**
   * 杩炴帴姹犻厤缃紙JSON 鏍煎紡锛?
   */
  poolConfig?: string

  /**
   * 鐘舵€侊細0-绂佺敤锛?-鍚敤
   */
  status: Report状态

  /**
   * 澶囨敞
   */
  remark?: string

  /**
   * 鍒涘缓鏃堕棿
   */
  createTime?: string

  /**
   * 鏇存柊鏃堕棿
   */
  updateTime?: string
}

/**
 * 鎶ヨ〃妯℃澘鏌ヨ鍙傛暟
 */
export interface ReportTemplateParam {
  /**
   * 椤电爜锛堜粠 1 寮€濮嬶級
   */
  pageNum: number

  /**
   * 姣忛〉澶у皬
   */
  pageSize: number

  /**
   * 鎶ヨ〃鍚嶇О锛堟ā绯婃煡璇級
   */
  name?: string

  /**
   * 鎶ヨ〃缂栫爜锛堟ā绯婃煡璇級
   */
  code?: string

  /**
   * 寮曟搸绫诲瀷
   */
  engineType?: ReportEngineType

  /**
   * 鍒嗙被 ID
   */
  categoryId?: number

  /**
   * 鐘舵€?
   */
  status?: Report状态
}

/**
 * 鎶ヨ〃鍒嗙被鏌ヨ鍙傛暟
 */
export interface ReportCategoryParam {
  /**
   * 椤电爜锛堜粠 1 寮€濮嬶級
   */
  pageNum: number

  /**
   * 姣忛〉澶у皬
   */
  pageSize: number

  /**
   * 鍒嗙被鍚嶇О锛堟ā绯婃煡璇級
   */
  name?: string

  /**
   * 鍒嗙被缂栫爜锛堟ā绯婃煡璇級
   */
  code?: string

  /**
   * 鐖跺垎绫?ID
   */
  parentId?: number
}

/**
 * 鎶ヨ〃鏁版嵁婧愭煡璇㈠弬鏁?
 */
export interface ReportDatasourceParam {
  /**
   * 椤电爜锛堜粠 1 寮€濮嬶級
   */
  pageNum: number

  /**
   * 姣忛〉澶у皬
   */
  pageSize: number

  /**
   * 鏁版嵁婧愬悕绉帮紙妯＄硦鏌ヨ锛?
   */
  name?: string

  /**
   * 鏁版嵁婧愮紪鐮侊紙妯＄硦鏌ヨ锛?
   */
  code?: string

  /**
   * 鏁版嵁婧愮被鍨?
   */
  type?: string

  /**
   * 鐘舵€?
   */
  status?: Report状态
}

/**
 * 鍒嗛〉缁撴灉绫诲瀷
 * @template T - 鏁版嵁绫诲瀷
 */
export interface PageResult<T> {
  /**
   * 鏁版嵁鍒楄〃
   */
  records: T[]

  /**
   * 鎬昏褰曟暟
   */
  total: number

  /**
   * 褰撳墠椤电爜
   */
  pageNum: number

  /**
   * 姣忛〉澶у皬
   */
  pageSize: number
}

/**
 * 鎶ヨ〃棰勮鍙傛暟
 */
export interface ReportPreviewParam {
  /**
   * 鎶ヨ〃缂栫爜
   */
  code: string

  /**
   * 寮曟搸绫诲瀷
   */
  engineType: ReportEngineType

  /**
   * 鎶ヨ〃鍙傛暟锛圝SON 鏍煎紡锛?
   */
  params?: Record<string, any>
}

/**
 * 鎶ヨ〃淇濆瓨 DTO
 */
export interface ReportSaveDTO {
  /**
   * 涓婚敭 ID锛堟柊澧炴椂涓嶄紶锛?
   */
  id?: number

  /**
   * 鎶ヨ〃鍚嶇О
   */
  name: string

  /**
   * 鎶ヨ〃缂栫爜
   */
  code: string

  /**
   * 寮曟搸绫诲瀷
   */
  engineType: ReportEngineType

  /**
   * 鍒嗙被 ID
   */
  categoryId?: number

  /**
   * 鏁版嵁婧?ID
   */
  datasourceId?: number

  /**
   * 鎶ヨ〃鍐呭
   */
  content?: string

  /**
   * 鐘舵€?
   */
  status: Report状态

  /**
   * 澶囨敞
   */
  remark?: string
}

/**
 * 鏁版嵁婧愪繚瀛?DTO
 */
export interface DatasourceSaveDTO {
  /**
   * 涓婚敭 ID锛堟柊澧炴椂涓嶄紶锛?
   */
  id?: number

  /**
   * 鏁版嵁婧愬悕绉?
   */
  name: string

  /**
   * 鏁版嵁婧愮紪鐮?
   */
  code: string

  /**
   * 鏁版嵁婧愮被鍨?
   */
  type: string

  /**
   * 鏁版嵁搴?URL
   */
  url: string

  /**
   * 鏁版嵁搴撶敤鎴峰悕
   */
  username: string

  /**
   * 鏁版嵁搴撳瘑鐮?
   */
  password?: string

  /**
   * 鏁版嵁搴撻┍鍔ㄧ被鍚?
   */
  driverClass?: string

  /**
   * 杩炴帴姹犻厤缃?
   */
  poolConfig?: string

  /**
   * 鐘舵€?
   */
  status: Report状态

  /**
   * 澶囨敞
   */
  remark?: string
}
