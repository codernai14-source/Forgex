package com.forgex.integration.domain.param;

import com.forgex.integration.domain.dto.ApiParamMappingDTO;
import lombok.Data;

import java.util.List;

/**
 * 接口参数映射批量保存参数
 * <p>
 * 用于一次性替换指定接口和映射方向下的字段映射关系。
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-17
 */
@Data
public class ApiParamMappingBatchSaveParam {

    /**
     * 接口配置 ID
     */
    private Long apiConfigId;

    /**
     * 出站目标 ID。
     */
    private Long outboundTargetId;

    /**
     * 映射方向：INBOUND/OUTBOUND
     */
    private String direction;

    /**
     * 字段映射列表
     */
    private List<ApiParamMappingDTO> mappings;
}
