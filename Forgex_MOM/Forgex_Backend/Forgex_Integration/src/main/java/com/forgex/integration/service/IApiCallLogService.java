package com.forgex.integration.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.forgex.integration.domain.dto.ApiCallLogDTO;
import com.forgex.integration.domain.entity.ApiCallLog;
import com.forgex.integration.domain.param.ApiCallLogParam;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 接口调用记录服务接口
 * <p>
 * 提供接口调用记录的保存、查询、统计等服务
 * 支持按月分表查询和异步保存
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-14
 * @see com.forgex.integration.domain.entity.ApiCallLog
 * @see com.forgex.integration.domain.dto.ApiCallLogDTO
 */
public interface IApiCallLogService extends IService<ApiCallLog> {

    /**
     * 异步保存调用记录
     * <p>
     * 将调用记录异步保存到对应的月份表中，不阻塞主流程
     * </p>
     *
     * @param log 调用记录
     * @return 保存结果，true-成功，false-失败
     * @see ApiCallLog
     */
    boolean asyncSaveLog(ApiCallLog log);

    /**
     * 分页查询调用记录
     * <p>
     * 支持按时间范围、接口配置 ID、调用方向、调用状态等条件查询
     * 自动根据时间范围确定需要查询的月份表
     * </p>
     *
     * @param param 查询参数
     * @return 分页结果
     * @see ApiCallLogParam
     * @see ApiCallLogDTO
     */
    Page<ApiCallLogDTO> pageCallLogs(ApiCallLogParam param);

    /**
     * 根据 ID 查询调用记录详情
     * <p>
     * 根据调用时间自动从对应的月份表中查询
     * </p>
     *
     * @param id 调用记录 ID
     * @param callTime 调用时间（用于确定查询哪个月份表）
     * @return 调用记录详情，不存在返回 null
     * @see ApiCallLogDTO
     */
    ApiCallLogDTO getCallLogById(Long id, LocalDateTime callTime);

    /**
     * 查询调用记录列表
     * <p>
     * 不分页查询，用于导出等场景
     * </p>
     *
     * @param param 查询参数
     * @return 调用记录列表
     * @see ApiCallLogParam
     * @see ApiCallLogDTO
     */
    List<ApiCallLogDTO> listCallLogs(ApiCallLogParam param);

    /**
     * 统计调用次数
     * <p>
     * 根据接口配置 ID 统计指定时间范围内的调用次数
     * </p>
     *
     * @param apiConfigId 接口配置 ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 调用次数
     */
    long countCallLogs(Long apiConfigId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 统计调用成功率
     * <p>
     * 计算指定时间范围内的调用成功率
     * </p>
     *
     * @param apiConfigId 接口配置 ID（可选，null 表示统计所有接口）
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 成功率（0-100 之间的数值）
     */
    double calculateSuccessRate(Long apiConfigId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取月份表名
     * <p>
     * 根据日期生成对应的月份表名（如：fx_api_call_log_202604）
     * </p>
     *
     * @param date 日期
     * @return 表名
     */
    String getMonthTableName(LocalDateTime date);
}
