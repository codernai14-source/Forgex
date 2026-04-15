package com.forgex.integration.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.forgex.integration.domain.dto.ApiParamMappingDTO;
import com.forgex.integration.domain.entity.ApiParamMapping;
import com.forgex.integration.domain.param.ApiParamMappingParam;

import java.util.List;

/**
 * 接口参数映射服务接口
 * <p>
 * 提供接口参数映射关系的增删改查、批量保存等功能
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-14
 */
public interface IApiParamMappingService extends IService<ApiParamMapping> {

    /**
     * 查询参数映射列表
     * <p>
     * 根据接口配置 ID 和映射方向查询所有映射关系
     * </p>
     *
     * @param param 查询参数
     *              - apiConfigId: 接口配置 ID
     *              - direction: 映射方向
     *              - sourceFieldPath: 源字段路径（模糊查询）
     *              - targetFieldPath: 目标字段路径（模糊查询）
     * @return 参数映射 DTO 列表
     */
    List<ApiParamMappingDTO> listMappings(ApiParamMappingParam param);

    /**
     * 根据 ID 获取参数映射详情
     * <p>
     * 用于编辑时回显数据
     * </p>
     *
     * @param id 参数映射 ID
     * @return 参数映射 DTO
     */
    ApiParamMappingDTO getById(Long id);

    /**
     * 创建参数映射
     * <p>
     * 自动校验映射关系的唯一性
     * </p>
     *
     * @param dto 参数映射 DTO
     */
    void create(ApiParamMappingDTO dto);

    /**
     * 更新参数映射
     * <p>
     * 自动校验映射关系的唯一性（排除自身）
     * </p>
     *
     * @param dto 参数映射 DTO
     */
    void update(ApiParamMappingDTO dto);

    /**
     * 删除参数映射
     * <p>
     * 删除单个映射关系
     * </p>
     *
     * @param id 参数映射 ID
     */
    void delete(Long id);

    /**
     * 批量删除参数映射
     * <p>
     * 支持批量删除多个映射关系
     * </p>
     *
     * @param ids 参数映射 ID 列表
     */
    void batchDelete(List<Long> ids);

    /**
     * 批量保存参数映射
     * <p>
     * 先删除指定接口配置和方向的所有映射，再批量插入新映射
     * 用于一次性保存多个映射关系
     * </p>
     *
     * @param apiConfigId 接口配置 ID
     * @param direction 映射方向
     * @param dtos 参数映射 DTO 列表
     */
    void batchSave(Long apiConfigId, String direction, List<ApiParamMappingDTO> dtos);
}
