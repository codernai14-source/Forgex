package com.forgex.integration.mapper;

import com.github.yulichang.base.MPJBaseMapper;
import com.forgex.integration.domain.entity.ThirdAuthorization;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 第三方授权 Mapper 接口
 * <p>
 * 负责第三方授权信息的数据访问
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-14
 */
@Mapper
public interface ThirdAuthorizationMapper extends MPJBaseMapper<ThirdAuthorization> {

    /**
     * 根据第三方系统 ID 查询授权信息
     * <p>
     * 一个第三方系统只能有一个授权配置
     * </p>
     *
     * @param thirdSystemId 第三方系统 ID
     * @return 授权信息，不存在返回 null
     */
    ThirdAuthorization getByThirdSystemId(@Param("thirdSystemId") Long thirdSystemId);

    /**
     * 根据 Token 值查询授权信息
     * <p>
     * 用于 Token 校验
     * </p>
     *
     * @param tokenValue Token 值
     * @return 授权信息，不存在返回 null
     */
    ThirdAuthorization getByTokenValue(@Param("tokenValue") String tokenValue);

    /**
     * 查询第三方系统的授权列表
     * <p>
     * 用于查看某个第三方系统的所有授权记录
     * </p>
     *
     * @param thirdSystemId 第三方系统 ID
     * @return 授权列表
     */
    List<ThirdAuthorization> listByThirdSystemId(@Param("thirdSystemId") Long thirdSystemId);
}
