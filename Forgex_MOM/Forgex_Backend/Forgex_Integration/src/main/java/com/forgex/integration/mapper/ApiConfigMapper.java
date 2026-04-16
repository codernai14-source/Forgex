package com.forgex.integration.mapper;

import com.github.yulichang.base.MPJBaseMapper;
import com.forgex.integration.domain.entity.ApiConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 接口配置 Mapper 接口
 * <p>
 * 负责接口配置信息的数据访问
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-14
 */
@Mapper
public interface ApiConfigMapper extends MPJBaseMapper<ApiConfig> {

    /**
     * 根据接口编码查询接口配置
     * <p>
     * 用于校验接口编码唯一性
     * </p>
     *
     * @param apiCode 接口编码
     * @param tenantId 租户 ID
     * @return 接口配置信息，不存在返回 null
     */
    ApiConfig selectByApiCode(@Param("apiCode") String apiCode, @Param("tenantId") Long tenantId);

    /**
     * 根据处理器 bean 名称查询接口配置
     * <p>
     * 用于接口路由时查找对应的接口配置
     * </p>
     *
     * @param processorBean 处理器 bean 名称
     * @return 接口配置信息，不存在返回 null
     */
    ApiConfig selectByProcessorBean(@Param("processorBean") String processorBean);

    /**
     * 根据接口路径查询接口配置
     * <p>
     * 用于外对内接口路由
     * </p>
     *
     * @param apiPath 接口路径
     * @param tenantId 租户 ID
     * @return 接口配置信息，不存在返回 null
     */
    ApiConfig selectByApiPath(@Param("apiPath") String apiPath, @Param("tenantId") Long tenantId);
}
