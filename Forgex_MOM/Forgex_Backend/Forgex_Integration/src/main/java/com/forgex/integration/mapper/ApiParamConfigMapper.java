package com.forgex.integration.mapper;

import com.github.yulichang.base.MPJBaseMapper;
import com.forgex.integration.domain.entity.ApiParamConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 接口参数配置 Mapper 接口
 * <p>
 * 负责接口参数配置的数据访问
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-14
 */
@Mapper
public interface ApiParamConfigMapper extends MPJBaseMapper<ApiParamConfig> {

    /**
     * 根据接口配置 ID 和方向查询参数配置列表
     * <p>
     * 用于获取接口的所有参数配置
     * </p>
     *
     * @param apiConfigId 接口配置 ID
     * @param direction 参数方向（REQUEST/RESPONSE）
     * @return 参数配置列表
     */
    List<ApiParamConfig> listByApiConfigIdAndDirection(
        @Param("apiConfigId") Long apiConfigId,
        @Param("direction") String direction
    );

    /**
     * 根据接口配置 ID 和父节点 ID 查询子节点
     * <p>
     * 用于构建树形结构
     * </p>
     *
     * @param apiConfigId 接口配置 ID
     * @param parentId 父节点 ID（null 表示根节点）
     * @return 子节点列表
     */
    List<ApiParamConfig> listChildrenByParentId(
        @Param("apiConfigId") Long apiConfigId,
        @Param("parentId") Long parentId
    );

    /**
     * 根据字段路径查询参数配置
     * <p>
     * 用于参数映射时查找对应的字段
     * </p>
     *
     * @param apiConfigId 接口配置 ID
     * @param fieldPath 字段路径
     * @param direction 参数方向
     * @return 参数配置信息，不存在返回 null
     */
    ApiParamConfig getByFieldPath(
        @Param("apiConfigId") Long apiConfigId,
        @Param("fieldPath") String fieldPath,
        @Param("direction") String direction
    );
}
