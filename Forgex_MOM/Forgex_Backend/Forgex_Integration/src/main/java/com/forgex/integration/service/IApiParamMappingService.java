package com.forgex.integration.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.forgex.integration.domain.dto.ApiParamMappingDTO;
import com.forgex.integration.domain.entity.ApiParamMapping;
import com.forgex.integration.domain.param.ApiParamMappingParam;

import java.util.List;

/**
 * 接口参数映射服务接口。
 * <p>
 * 负责维护接口字段映射关系，包括单条增删改查和按接口配置、出站目标、参数方向批量保存。
 * </p>
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 * @since 2026-04-01
 */
public interface IApiParamMappingService extends IService<ApiParamMapping> {

    /**
     * 查询字段映射列表。
     *
     * @param param 查询参数
     * @return 字段映射 DTO 列表
     */
    List<ApiParamMappingDTO> listMappings(ApiParamMappingParam param);

    /**
     * 根据 ID 查询字段映射。
     *
     * @param id 映射 ID
     * @return 字段映射 DTO
     */
    ApiParamMappingDTO getById(Long id);

    /**
     * 新增字段映射。
     *
     * @param dto 字段映射 DTO
     */
    void create(ApiParamMappingDTO dto);

    /**
     * 更新字段映射。
     *
     * @param dto 字段映射 DTO
     */
    void update(ApiParamMappingDTO dto);

    /**
     * 删除字段映射。
     *
     * @param id 映射 ID
     */
    void delete(Long id);

    /**
     * 批量删除字段映射。
     *
     * @param ids 映射 ID 列表
     */
    void batchDelete(List<Long> ids);

    /**
     * 批量保存字段映射。
     *
     * @param apiConfigId      接口配置 ID
     * @param outboundTargetId 出站目标 ID，可为空
     * @param direction        参数方向
     * @param dtos             字段映射列表
     */
    void batchSave(Long apiConfigId, Long outboundTargetId, String direction, List<ApiParamMappingDTO> dtos);

    /**
     * 批量保存字段映射。
     *
     * @param apiConfigId 接口配置 ID
     * @param direction   参数方向
     * @param dtos        字段映射列表
     */
    default void batchSave(Long apiConfigId, String direction, List<ApiParamMappingDTO> dtos) {
      /**
       * 执行接口参数映射的batchsave操作。
       */
        batchSave(apiConfigId, null, direction, dtos);
    }
}
