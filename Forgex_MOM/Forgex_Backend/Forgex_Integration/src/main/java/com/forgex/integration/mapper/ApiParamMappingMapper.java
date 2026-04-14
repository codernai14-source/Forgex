package com.forgex.integration.mapper;

import com.github.yulichang.base.MPJBaseMapper;
import com.forgex.integration.domain.entity.ApiParamMapping;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 接口参数映射 Mapper 接口
 * <p>
 * 负责接口参数映射关系的数据访问
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-14
 */
@Mapper
public interface ApiParamMappingMapper extends MPJBaseMapper<ApiParamMapping> {

    /**
     * 根据接口配置 ID 和方向查询参数映射列表
     * <p>
     * 用于参数转换时获取所有映射规则
     * </p>
     *
     * @param apiConfigId 接口配置 ID
     * @param direction 映射方向（INBOUND/OUTBOUND）
     * @return 参数映射列表
     */
    List<ApiParamMapping> listByApiConfigIdAndDirection(
        @Param("apiConfigId") Long apiConfigId,
        @Param("direction") String direction
    );

    /**
     * 根据接口配置 ID 查询所有参数映射
     * <p>
     * 用于批量加载配置
     * </p>
     *
     * @param apiConfigId 接口配置 ID
     * @return 参数映射列表
     */
    List<ApiParamMapping> listByApiConfigId(@Param("apiConfigId") Long apiConfigId);
}
